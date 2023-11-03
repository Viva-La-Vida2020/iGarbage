import torch
import numpy as np
import os
import librosa
import torch.nn as nn
import torch.nn.functional as F
import random


input_feat_dim = 257  # Audio feature dimension for 16KHz
enc_hidden_dim = 128  # Encoder RNN hidden dimension
n_layers_enc = 4  # Number of Encoder layers
vocab_size = 30  # VOcabulary size
max_out_len = 100  # Maximum output length
dec_hidden_dim = 256  # Decoder RNN hidden dimension
n_layers_dec = 1  # Number of decoder layers
sos_id = 1  # start of sentence ID in vocab
eos_id = 2  # ENd of sentence ID in vocab
use_gpu = 1

CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))


if torch.cuda.is_available():
    import torch.cuda as device
else:
    import torch as device


def preprocess_audio(audio_filepath):
    audio_data, fs = librosa.load(audio_filepath)
    features = librosa.stft(audio_data,
                            n_fft=512,
                            win_length=400,
                            hop_length=160)
    mag, _ = librosa.magphase(features.T)
    mag_T = mag.T
    spec_mag = mag_T

    # preprocessing, subtract mean, divided by time-wise var
    mu = np.mean(spec_mag, 0, keepdims=True)
    std = np.std(spec_mag, 0, keepdims=True)
    norm_spec = (spec_mag - mu) / (std + 1e-5)

    return {
        'features': torch.from_numpy(np.ascontiguousarray(norm_spec.T)),
    }


def predict(file):
    encoder = Encoder(input_feat_dim, enc_hidden_dim, n_layers_enc).to('cpu')
    decoder = Decoder(vocab_size,
                      max_out_len,
                      dec_hidden_dim,
                      enc_hidden_dim,
                      sos_id,
                      eos_id,
                      n_layers_dec,
                      rnn_celltye='gru').to('cpu')
    model = Seq2seq(encoder, decoder).to('cpu')
    checkpoint = torch.load(f'{CURRENT_DIR}/best_check_point_46',
                            map_location='cpu')
    model.load_state_dict(checkpoint['model'])

    model.eval()
    with torch.no_grad():
        test_data = preprocess_audio(file)
        pred = model(test_data['features'])
        return pred


class Attention(nn.Module):
    """
    https://arxiv.org/pdf/1506.07503.pdf
    """
    def __init__(self, dec_hidden_dim, enc_hidden_dim, attn_dim):
        super(Attention, self).__init__()
        self.dec_hidden_dim = dec_hidden_dim
        self.attn_dim = attn_dim
        self.enc_hidden_dim = enc_hidden_dim

        self.W = nn.Linear(self.dec_hidden_dim, self.attn_dim, bias=False)
        self.V = nn.Linear(self.enc_hidden_dim, self.attn_dim, bias=False)

        self.fc = nn.Linear(self.attn_dim, 1, bias=True)
        self.b = nn.Parameter(torch.rand(attn_dim))

        self.tanh = nn.Tanh()
        self.softmax = nn.Softmax(dim=-1)

    def forward(self, Si_1, Hj):
        score = self.fc(self.tanh(
         self.W(Si_1) + self.V(Hj) + self.b
        )).squeeze(dim=-1)
        # print(score.shape)
        attn_weight = self.softmax(score) 
        # print(attn_weight.shape)
        context = torch.bmm(attn_weight.unsqueeze(dim=1), Hj)
        # print(context.shape)

        return context, attn_weight
    

class Decoder(nn.Module):
    def __init__(self, vocab_size, max_len, dec_hidden_size, encoder_hidden_size,
                 sos_id, eos_id, n_layers=1, rnn_celltye='gru',):
        super().__init__()
        self.output_size = vocab_size
        self.vocab_size = vocab_size
        self.dec_hidden_size = dec_hidden_size
        self.encoder_output_size = encoder_hidden_size
        self.n_layers = n_layers
        self.max_length = max_len
        self.eos_id = eos_id
        self.sos_id = sos_id
        
        self.embedding = nn.Embedding(self.vocab_size, self.dec_hidden_size)
        if rnn_celltye == 'lstm':
            self.rnn = nn.LSTM(self.dec_hidden_size + self.encoder_output_size, self.dec_hidden_size, self.n_layers,
                                 batch_first=True, dropout=0.2, bidirectional=False)
        elif rnn_celltye == 'gru':
            self.rnn = nn.GRU(self.dec_hidden_size + self.encoder_output_size, self.dec_hidden_size, self.n_layers,
                            batch_first=True, dropout=0.2, bidirectional=False)
        else:
            print('Error in rnn type')
            
        self.attention = Attention(self.dec_hidden_size, self.encoder_output_size, attn_dim=self.dec_hidden_size)
        self.fc = nn.Linear(self.dec_hidden_size + self.encoder_output_size, self.output_size)
        
    def forward_step(self, input_var, hidden, encoder_outputs, context, attn_w, function):
        if self.training:
            self.rnn.flatten_parameters()
        
        if torch.cuda.is_available():
            input_var = input_var.cuda()
        embedded = self.embedding(input_var)
        #  print(embedded.shape)
    
        y_all = []
        attn_w_all = []
        for i in range(embedded.size(1)):
            embedded_inputs = embedded[:, i, :]
            # print(embedded_inputs.shape)
            rnn_input = torch.cat([embedded_inputs, context], dim=1)
            # print(rnn_input.shape)
            rnn_input = rnn_input.unsqueeze(1) 
            # print(rnn_input.shape)
            output, hidden = self.rnn(rnn_input, hidden)
            # print(output.shape)
            # print(hidden.shape)
            context, attn_w = self.attention(output, encoder_outputs)
            # print(context.shape)
            # print(attn_w.shape)
            attn_w_all.append(attn_w)
            
            context = context.squeeze(1)
            #  print(context.shape)
            output = output.squeeze(1) 
            #  print(output.shape)
            output = torch.cat((output, context), dim=1) 
            #  print(output.shape)

            pred = function(self.fc(output), dim=-1)
            # print(pred.shape)
            y_all.append(pred)

        if embedded.size(1) != 1:
            y_all = torch.stack(y_all, dim=1) 
            attn_w_all = torch.stack(attn_w_all, dim=1) 
        else:
            y_all = y_all[0].unsqueeze(1) 
            attn_w_all = attn_w_all[0] 
        
        return y_all, hidden, context, attn_w_all

    def forward(self, inputs=None, encoder_hidden=None, encoder_outputs=None, function=F.log_softmax, teacher_forcing_ratio=0):
        """
        param:inputs: Decoder inputs sequence, Shape=(B, dec_T)
        param:encoder_hidden: Encoder last hidden states, Default : None
        param:encoder_outputs: Encoder outputs, Shape=(B,enc_T,enc_D)
        """

        use_teacher_forcing = True if random.random() < teacher_forcing_ratio else False

        if teacher_forcing_ratio != 0:
            inputs, batch_size, max_length = self._validate_args(inputs, encoder_hidden, encoder_outputs,
                                                                function, teacher_forcing_ratio)
        else:
            batch_size = encoder_outputs.size(0)
            inputs = torch.LongTensor([self.sos_id] * batch_size).view(batch_size, 1)
            # if torch.cuda.is_available():
            #     inputs = inputs.cuda()
            max_length = self.max_length

        decoder_hidden = None
        context = encoder_outputs.new_zeros(batch_size, encoder_outputs.size(2))  # (B, D)
        # print(context.shape) 
        attn_w = encoder_outputs.new_zeros(batch_size, encoder_outputs.size(1))  # (B, T)
        # print(attn_w.shape)
        decoder_outputs = []
        sequence_symbols = []
        lengths = np.array([max_length] * batch_size)

        def decode(step, step_output):
            decoder_outputs.append(step_output)
            symbols = decoder_outputs[-1].topk(1)[1]
            sequence_symbols.append(symbols)

            eos_batches = symbols.data.eq(self.eos_id)
            if eos_batches.dim() > 0:
                eos_batches = eos_batches.cpu().view(-1).numpy()
                update_idx = ((lengths > step) & eos_batches) != 0
                lengths[update_idx] = len(sequence_symbols)
            return symbols

        if use_teacher_forcing:
            decoder_input = inputs[:, :-1]
            decoder_output, decoder_hidden, context, attn_w = self.forward_step(decoder_input, 
                                                                                decoder_hidden, 
                                                                                encoder_outputs,
                                                                                context,    
                                                                                attn_w, 
                                                                                function=function)

            for di in range(decoder_output.size(1)):
                step_output = decoder_output[:, di, :]
                decode(di, step_output)
        else:
            decoder_input = inputs[:, 0].unsqueeze(1)
            # print(decoder_input.shape)
            for di in range(max_length):
                decoder_output, decoder_hidden, context, attn_w = self.forward_step(decoder_input, 
                                                                                    decoder_hidden,
                                                                                    encoder_outputs,
                                                                                    context,
                                                                                    attn_w,
                                                                                    function=function)
                # print(decoder_output.shape)
                step_output = decoder_output.squeeze(1)
                # print(step_output.shape)
                symbols = decode(di, step_output)
                # print(symbols.shape)
                decoder_input = symbols

        return decoder_outputs, sequence_symbols

    def _init_state(self, encoder_hidden):
        """ Initialize the encoder hidden state. """
        if encoder_hidden is None:
            return None
        if isinstance(encoder_hidden, tuple):
            encoder_hidden = tuple([self._cat_directions(h) for h in encoder_hidden])
        else:
            encoder_hidden = self._cat_directions(encoder_hidden)
        return encoder_hidden

    def _cat_directions(self, h):
        """ If the encoder is bidirectional, do the following transformation.
            (# directions * # layers, # batch, hidden_size) -> (# layers, # batch, # directions * hidden_size)
        """
        if self.bidirectional_encoder:
            h = torch.cat([h[0:h.size(0):2], h[1:h.size(0):2]], 2)
        return h

    def _validate_args(self, inputs, encoder_hidden, encoder_outputs, function, teacher_forcing_ratio):
        if self.use_attention:
            if encoder_outputs is None:
                raise ValueError("Argument encoder_outputs cannot be None when attention is used.")

        batch_size = encoder_outputs.size(0)

        if inputs is None:
            if teacher_forcing_ratio > 0:
                raise ValueError("Teacher forcing has to be disabled (set 0) when no inputs is provided.")
            inputs = torch.LongTensor([self.sos_id] * batch_size).view(batch_size, 1)
            max_length = self.max_length
        else:
            max_length = inputs.size(1) - 1  # minus the start of sequence symbol

        return inputs, batch_size, max_length


class Convolution_Block(nn.Module):
    def __init__(self, input_dim=40, cnn_out_channels=64):
        super(Convolution_Block, self).__init__()
        self.conv = nn.Sequential(
            nn.Conv1d(input_dim,
                      cnn_out_channels,
                      kernel_size=3,
                      stride=1,
                      padding=3), nn.BatchNorm1d(cnn_out_channels), nn.ReLU(),
            nn.Conv1d(cnn_out_channels,
                      cnn_out_channels,
                      kernel_size=5,
                      stride=2,
                      padding=0), nn.BatchNorm1d(cnn_out_channels), nn.ReLU())

    def forward(
        self, inputs
    ):  # inputs:torch.Size([64, 257, 509]) out:torch.Size([64, 64, 255])
        # print(inputs.shape)
        out = self.conv(inputs)
        # print(out.shape)
        return out


class DropSubsampler(nn.Module):
    """Subsample by droping input frames."""
    def __init__(self, factor):
        super(DropSubsampler, self).__init__()

        self.factor = factor

    def forward(self, xs, xlens):
        if self.factor == 1:
            return xs, xlens

        xs = xs[:, ::self.factor, :]

        xlens = [max(1, (i + self.factor - 1) // self.factor) for i in xlens]
        xlens = torch.IntTensor(xlens)
        return xs, xlens


class ConcatSubsampler(nn.Module):
    """Subsample by concatenating successive input frames."""
    def __init__(self, factor, n_units):
        super(ConcatSubsampler, self).__init__()

        self.factor = factor
        if factor > 1:
            self.proj = nn.Linear(n_units * factor, n_units)

    def forward(self, xs, xlens):
        if self.factor == 1:
            return xs, xlens

        xs = xs.transpose(1, 0).contiguous()
        xs = [
            torch.cat(
                [xs[t - r:t - r + 1] for r in range(self.factor - 1, -1, -1)],
                dim=-1) for t in range(xs.size(0))
            if (t + 1) % self.factor == 0
        ]
        xs = torch.cat(xs, dim=0).transpose(1, 0)
        # NOTE: Exclude the last frames if the length is not divisible

        xs = torch.relu(self.proj(xs))
        xlens //= self.factor
        return xs, xlens


class Encoder(nn.Module):
    def __init__(self,
                 input_dim,
                 hidden_dim,
                 n_layers=1,
                 dropout=0.3,
                 cnn_out_channels=64,
                 rnn_celltype='gru'):
        super().__init__()
        self.input_dim = input_dim
        self.hidden_dim = hidden_dim
        self.n_layers = n_layers
        self.dropout_p = dropout

        self.conv = Convolution_Block(self.input_dim,
                                      cnn_out_channels=cnn_out_channels)

        if rnn_celltype == 'lstm':
            self.rnn = nn.LSTM(cnn_out_channels,
                               self.hidden_dim,
                               self.n_layers,
                               dropout=self.dropout_p,
                               bidirectional=False,
                               batch_first=True)
        else:
            self.rnn = nn.GRU(cnn_out_channels,
                              self.hidden_dim,
                              self.n_layers,
                              dropout=self.dropout_p,
                              bidirectional=False,
                              batch_first=True)

    def forward(self, inputs, input_lengths):
        # print(inputs.shape)
        output_lengths = self.get_conv_out_lens(input_lengths)
        out = self.conv(inputs)
        # print(out.shape)
        out = out.permute(0, 2, 1)
        # print(out.shape)
        # B* T 如果batch_first的话，并且按照长短排序，先长后短，需要传入各个句子的长度
        output_lengths = output_lengths.long()
        output_lengths = output_lengths.to(torch.device('cpu'))
        out = nn.utils.rnn.pack_padded_sequence(out,
                                                output_lengths,
                                                enforce_sorted=False,
                                                batch_first=True)  # 按行给它压紧

        out, rnn_hidden_state = self.rnn(out)

        out, _ = nn.utils.rnn.pad_packed_sequence(out)  # 把压缩的还原回来，pad该还还得还
        # print(out.shape)
        rnn_out = out.transpose(0, 1)
        # print(rnn_out.shape)
        # print(rnn_hidden_state.shape)
        return rnn_out, rnn_hidden_state  # rnn_hidden_state:torch.Size([4, 64, 128])

    def get_conv_out_lens(self, input_length):
        seq_len = input_length
        for m in self.conv.modules():
            if type(m) == nn.modules.conv.Conv1d:
                seq_len = ((seq_len + 2 * m.padding[0] - m.dilation[0] *
                            (m.kernel_size[0] - 1) - 1) / m.stride[0] + 1)

        return seq_len.int()


class Seq2seq(nn.Module):
    def __init__(self, encoder, decoder, decode_function=F.log_softmax):
        super().__init__()
        self.encoder = encoder
        self.decoder = decoder
        self.decode_function = decode_function

    def flatten_parameters(self):
        self.encoder.rnn.flatten_parameters()
        self.decoder.rnn.flatten_parameters()

    def forward(self, input_variable, input_lengths=None, target_variable=None,
                teacher_forcing_ratio=0):
        '''
        input_variable -->[B,Feat_Dim, Feats_Len]
        input_lengths --->[B,Feats_Len]
        target_variable ---> [B, Dec_T)]
        '''
        encoder_outputs, encoder_hidden = self.encoder(input_variable, input_lengths)
        decoder_outputs, sequence_symbols = self.decoder(inputs=target_variable,
                              encoder_hidden = encoder_hidden,
                              encoder_outputs = encoder_outputs, function = self.decode_function,
                              teacher_forcing_ratio = teacher_forcing_ratio)

        final_dec_outputs = torch.stack(decoder_outputs,dim=2)
        final_sequence_symbols = torch.stack(sequence_symbols,dim=1)
        return final_dec_outputs, final_sequence_symbols