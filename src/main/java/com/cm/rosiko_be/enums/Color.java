package com.cm.rosiko_be.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Random;

public enum Color {
    @JsonProperty("GREEN")
    GREEN,
    @JsonProperty("RED")
    RED,
    @JsonProperty("YELLOW")
    YELLOW,
    @JsonProperty("BLUE")
    BLUE,
    @JsonProperty("PURPLE")
    PURPLE,
    @JsonProperty("BLACK")
    BLACK;

    public static Color getRandomColor(){
        return getColor(new Random().nextInt(6));
    }

    //Ritorna un colore casuale escludendo quelli passati come parametro
    public static Color getRandomColor(List<Color> excludedColors) throws Exception {
        Color targetColor = null;
        boolean isOk = false;
        int index = new Random().nextInt(6);

        if(excludedColors.size()>=6) throw new Exception("There are no more colors available for players. All colors have already been taken.");

        do{
            targetColor = getColor(index);
            isOk = true;
            for (Color color : excludedColors) {
                if(targetColor.equals(color)){
                    isOk = false;
                    index++;
                    if(index>=6) index=0;
                }
            }
        }
        while (!isOk);

        return targetColor;
    }

    public static Color getColor(int id){
        Color color = null;
        switch (id){
            case 0: color = GREEN; break;
            case 1: color = BLACK; break;
            case 2: color = YELLOW; break;
            case 3: color = BLUE; break;
            case 4: color = RED; break;
            case 5: color = PURPLE; break;
        }
        return color;
    }
}
