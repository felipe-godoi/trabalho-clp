package com.clp.trabalho;

import com.fazecast.jSerialComm.SerialPort;

import java.util.HashMap;
import java.util.Map;

public class Status {
    public static Map<String, Boolean> inputs = new HashMap<String, Boolean>() {{
        put("0", false);
        put("1", true);
        put("I1", false);
        put("I2", false);
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
    public static int varredura = 200;
    public static String[] textareaInput = {""};

    public static void connectPort() throws Exception {
        SerialPort[] comPorts = SerialPort.getCommPorts();
        for(SerialPort port : comPorts){
            if(port.getPortDescription().equals("USB2.0-Serial")){
                comPort = port;
            }
        }

        if(comPort != null)
            comPort.openPort();
        else
            throw new Exception("A comunicação com Arduino não pode ser estabelecida com sucesso! Tente novamente!.");

        if (!comPort.isOpen()) {
            throw new Exception("Não foi possível estabelecer a conexão com o Arduino!");
        }
    }

    public static void resetOutput() {
        outputs.clear();
        outputs.put("O1", false);
        outputs.put("O2", false);
        outputs.put("O3", false);
        outputs.put("O4", false);
        outputs.put("O5", false);
        outputs.put("O6", false);
        outputs.put("O7", false);
        outputs.put("O8", false);
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
