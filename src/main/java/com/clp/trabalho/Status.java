package com.clp.trabalho;

import java.util.HashMap;
import java.util.Map;

public class Status {
    public static Map<String, Boolean> inputs = new HashMap<String, Boolean>() {{
        put("0", false);
        put("1", true);
        put("I1", false);
        put("I2", true);
        put("I3", false);
        put("I4", false);
        put("I5", false);
        put("I6", false);
        put("I7", false);
        put("I8", false);
        put("I9", false);
    }};
    public static Map<String, Boolean> outputs = new HashMap<String, Boolean>() {{
        put("O1", false);
        put("O2", false);
        put("O3", false);
        put("O4", false);
        put("O5", false);
        put("O6", false);
        put("O7", false);
        put("O8", false);
    }};
    public static Map<String, Boolean> variaveis = new HashMap<String, Boolean>();

    public static void initializeStatus(String inputString) {
        inputs.clear();
        outputs.clear();
        variaveis.clear();

        outputs.put("O1", false);
        outputs.put("O2", false);
        outputs.put("O3", false);
        outputs.put("O4", false);
        outputs.put("O5", false);
        outputs.put("O6", false);
        outputs.put("O7", false);
        outputs.put("O8", false);

        for (int i = 0; i < inputString.length(); i++){
            char input = inputString.charAt(i);

            if (input == '1') {
                inputs.put("I"+ (i+1), true);
            } else {
                inputs.put("I"+ (i+1), false);
            }
        }



        System.out.println(inputs);
    }
}
