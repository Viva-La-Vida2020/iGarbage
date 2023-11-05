package com.rubbishclassification.service;

import com.rubbishclassification.entity.Rubbish;
import com.rubbishclassification.entity.Type;
import com.rubbishclassification.repository.RubbishRepository;
import com.rubbishclassification.repository.TypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @program: RubbishClassification
 * @description:
 * @author: ZXY
 * @create: 2022-03-03 21:36
 **/
@Service
public class TypeService {

    private final Integer PAGE_SIZE = 10;
    @Autowired
    private TypeRepository typeRepository;
    @Autowired
    private RubbishRepository rubbishRepository;

    public List<Result> getTypesByCityAndRubbishName(String city, String name) {

        List<Rubbish> rubbishes = rubbishRepository.getRubbishesByNameLike("%" + name + "%");
        List<Result> results = new ArrayList<>();

        int i = 0;
        for (Rubbish r : rubbishes) {

            if (i == PAGE_SIZE) break;

            Optional<Type> optionalType = typeRepository.findByRubbishesIdAndCityName(r.getId(), city);

            if (optionalType.isPresent()) {
                Type t = optionalType.get();
                results.add(new Result(r.getName(), t.getType()));
                i++;
            }

        }

        return results;
    }

    public TypeAndRubbish getTypeByCityAndRubbishID(String city, Long id) {

        Optional<Rubbish> optionalRubbish = rubbishRepository.getRubbishById(id);

        if (!optionalRubbish.isPresent()) {
            return null;
        }

        Optional<Type> optionalType = typeRepository.findByRubbishesIdAndCityName(optionalRubbish.get().getId(), city);
        return new TypeAndRubbish(optionalType.get(), optionalRubbish.get());
    }


    public class Result {

        String name;
        String type;

        public Result(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public TypeAndRubbish findTypeAndRubbishByCityAndRubbishName(String city, String name) {

        Optional<Rubbish> optionalRubbish = rubbishRepository.getRubbishesByName(name);
        if (!optionalRubbish.isPresent()) {
            return null;
        }
        Rubbish rubbish = optionalRubbish.get();

        return getTypeByCityAndRubbishID(city, rubbish.getId());
    }

    public class TypeAndRubbish {
        Type type;
        Rubbish rubbish;

        public TypeAndRubbish(Type type, Rubbish rubbish) {
            this.type = type;
            this.rubbish = rubbish;
        }

        public TypeAndRubbish() {

        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public Rubbish getRubbish() {
            return rubbish;
        }

        public void setRubbish(Rubbish rubbish) {
            this.rubbish = rubbish;
        }
    }
}
