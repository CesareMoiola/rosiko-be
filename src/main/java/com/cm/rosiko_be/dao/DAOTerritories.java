package com.cm.rosiko_be.dao;

import com.cm.rosiko_be.data.Territory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DAOTerritories {
    public static List<Territory> getTerritories(){
        List<Territory> territories = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            territories = objectMapper.readValue(new File("src/main/resources/territories.json"), new TypeReference<List<Territory>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }

        return territories;
    }
}
