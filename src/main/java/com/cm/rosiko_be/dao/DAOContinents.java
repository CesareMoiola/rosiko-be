package com.cm.rosiko_be.dao;

import com.cm.rosiko_be.data.Continent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class DAOContinents {

    public static List<Continent> getContinents(){
        List<Continent> continents = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            continents = objectMapper.readValue(new File("src/main/resources/continents.json"), new TypeReference<List<Continent>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }

        return continents;
    }
}
