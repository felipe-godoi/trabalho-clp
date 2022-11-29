package com.clp.trabalho;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.util.*;

import com.fazecast.jSerialComm.*;

public class ProcessarController {
    @FXML
    private TextArea processarInput;
    public List<Character> palavrasReservadas = Arrays.asList('+', '*', '!', '=', '('   );

    @FXML
    protected void onProcessarButtonClick() {
        String[] input = processarInput.getText().split("\n");

        if (input[0].length() == 0) {
            return;
        }

        for (int i = 0; i < input.length; i++) {
            String[] splitted = input[i].split("->");
            String texto = converterInput(splitted[0].replace(" ", ""));
            String output = splitted[1].replace(" ", "");

            boolean result = processarExpressao(texto);

            if (Status.outputs.containsKey(output)) {
                Status.outputs.put(output, result);
            } else {
                Status.variaveis.put(output, result);
            }
        }

        System.out.println(Status.inputs);
        System.out.println(Status.outputs);
        System.out.println(Status.variaveis);
    }

    @FXML
    protected void onEnviarButtonClick(){
        SerialPort comPort = SerialPort.getCommPorts()[1];
        comPort.openPort();

        Set<String> keys = Status.outputs.keySet();
        String output = "";

        for (String key: keys) {
            Boolean value = Status.outputs.get(key);

            if (value) {
                output += "1";
            } else {
                output += "0";
            }
        }

        comPort.writeBytes(output.getBytes(), output.length());
        comPort.closePort();
    }

    @FXML
    public static void lerInputs(){
        SerialPort comPort = SerialPort.getCommPorts()[1];
        comPort.openPort();

        comPort.writeBytes(new byte[]{'2', '7', '0'}, 3);
        comPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }
            @Override
            public void serialEvent(SerialPortEvent event)
            {
                String inputs = "";
                byte[] newData = event.getReceivedData();
                for (int i = 0; i < newData.length; ++i)
                    inputs += (char)newData[i];

                Status.initializeStatus(inputs);
            }
        });
    }

    protected String converterInput(String texto) {
        return texto.replace("&&", "*").replace("||", "+").replace(" ", "");
    }

    protected int getConteudoParenteses(String texto) {
        int abrindo = 0;
        int fechando = 0;

        for (int i = 0; i < texto.length(); i++) {
            char letra = texto.charAt(i);

            if (letra == '(') {
                abrindo++;
            } else if (letra == ')') {
                fechando++;
            }

            if (abrindo == fechando) {
                return i;
            }
        }

        return 0;
    }

    protected boolean avaliarExpressao(boolean valorEsquerda, char operador, boolean valorDireita) {
        System.out.println(String.valueOf(valorEsquerda) + operador + valorDireita);
        if (operador == '*') {
            return valorEsquerda && valorDireita;
        } else {
            return valorEsquerda || valorDireita;
        }
    }

    protected boolean getValue(String key) {
        if (Status.inputs.containsKey(key)) {
            return Status.inputs.get(key);
        } else if (Status.outputs.containsKey(key)) {
            return Status.outputs.get(key);
        } else if (Status.variaveis.containsKey(key)) {
            return Status.variaveis.get(key);
        }

        throw new IllegalStateException("Unexpected value: " + key);
    }

    protected boolean processarExpressao(String texto) {
        System.out.println(texto);

        String bufferEsquerda = "";
        String bufferDireita = "";
        boolean inverter = false;

        char operador = ' ';
        boolean valorEsquerda = false;
        boolean valorDireita;

        for (int i = 0; i < texto.length(); i++) {
            char letra = texto.charAt(i);
            if (palavrasReservadas.contains(letra)) {
                switch (letra) {
                    case '!': {
                        if (texto.charAt(i+1) == '(') {
                            inverter = true;
                        } else {
                            if (operador == ' ') {
                                bufferEsquerda = bufferEsquerda.concat(String.valueOf(letra));
                            } else {
                                bufferDireita = bufferDireita.concat(String.valueOf(letra));
                            }
                        }
                        break;
                    }
                    case '(': {
                        int acrescimo = getConteudoParenteses(texto.substring(i));
                        String substring = texto.substring(i + 1, i + acrescimo);
                        if (operador != ' ') {
                            valorDireita = processarExpressao(substring);
                            if (inverter) {
                                valorDireita = !valorDireita;
                                inverter = false;
                            }
                            valorEsquerda = avaliarExpressao(valorEsquerda, operador, valorDireita);
                        } else {
                            valorEsquerda = processarExpressao(substring);
                            if (inverter) {
                                valorEsquerda = !valorEsquerda;
                                inverter = false;
                            }
                        }

                        bufferDireita = "";
                        i += acrescimo;
                        break;
                    }
                    case '+': {
                        if (operador != ' ') {
                            if (bufferDireita != "") {
                                if (bufferDireita.contains("!")) {
                                    valorDireita = !getValue(bufferDireita.replace("!", ""));
                                } else {
                                    valorDireita = getValue(bufferDireita);
                                }
                                bufferDireita = "";
                                valorEsquerda = avaliarExpressao(valorEsquerda, operador, valorDireita);
                            }
                        } else {
                            if (bufferEsquerda != "") {
                                if (bufferEsquerda.contains("!")) {
                                    valorEsquerda = !getValue(bufferEsquerda.replace("!", ""));
                                } else {
                                    valorEsquerda = getValue(bufferEsquerda);
                                }
                            }
                        }

                        operador = '+';
                        break;
                    }
                    case '*':
                        if (operador != ' ') {
                            if (bufferDireita != "") {
                                if (bufferDireita.contains("!")) {
                                    valorDireita = !getValue(bufferDireita.replace("!", ""));
                                } else {
                                    valorDireita = getValue(bufferDireita);
                                }
                                bufferDireita = "";
                                valorEsquerda = avaliarExpressao(valorEsquerda, operador, valorDireita);
                            }
                        } else {
                            if (bufferEsquerda != "") {
                                if (bufferEsquerda.contains("!")) {
                                    valorEsquerda = !getValue(bufferEsquerda.replace("!", ""));
                                } else {
                                    valorEsquerda = getValue(bufferEsquerda);
                                }
                            }
                        }

                        operador = '*';
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + letra);
                }
            } else {
                if (operador == ' ') {
                    bufferEsquerda = bufferEsquerda.concat(String.valueOf(letra));
                } else {
                    bufferDireita = bufferDireita.concat(String.valueOf(letra));
                }
            }
        }

        if (operador == ' ' && bufferEsquerda != "") {
            if (bufferEsquerda.contains("!")) {
                valorEsquerda = !getValue(bufferEsquerda.replace("!", ""));
            } else {
                valorEsquerda = getValue(bufferEsquerda);
            }
        }

        if (bufferDireita != "") {
            if (bufferDireita.contains("!")) {
                valorDireita = !getValue(bufferDireita);
            } else {
                valorDireita = getValue(bufferDireita);
            }
            valorEsquerda = avaliarExpressao(valorEsquerda, operador, valorDireita);
        }

        return valorEsquerda;
    }
}