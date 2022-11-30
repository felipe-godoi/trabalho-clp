package com.clp.trabalho;

import com.fazecast.jSerialComm.SerialPort;

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
    public static SerialPort comPort;

    public static String[] textareaInput = {""};

    public static void connectPort() {
        comPort = SerialPort.getCommPorts()[0];
        comPort.openPort();
    }

    public static void initializeStatus(String inputString) {
        inputString = inputString.strip();

        inputs.clear();
        variaveis.clear();

        int j = inputString.length();
        for (int i = 0; i < inputString.length(); i++){
            char input = inputString.charAt(i);

            String inputKey = "I"+ j;

            if (input == '1') {
                inputs.put(inputKey, true);
            } else {
                inputs.put(inputKey, false);
            }

            j--;
        }

        inputs.put("1", true);
        inputs.put("0", false);

        ProcessarController.processarInput(Status.textareaInput);
        ProcessarController.enviarOutputs();
    }
}
