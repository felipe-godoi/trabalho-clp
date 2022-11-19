package com.clp.trabalho;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessarController {
    @FXML
    private TextArea processarInput;
    public List<Character> palavrasReservadas = Arrays.asList('+', '*', '!', '=', '('   );
    public Map<String, Boolean> inputs = new HashMap<String, Boolean>() {{
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
    public Map<String, Boolean> outputs = new HashMap<String, Boolean>() {{
       put("O1", false);
       put("O2", false);
       put("O3", false);
       put("O4", false);
       put("O5", false);
       put("O6", false);
       put("O7", false);
       put("O8", false);
    }};
    public Map<String, Boolean> variaveis = new HashMap<String, Boolean>();

    @FXML
    protected void onProcessarButtonClick() {
        String[] input = processarInput.getText().split("\n");

        for (int i = 0; i < input.length; i++) {
            String[] splitted = input[i].split("->");
            String texto = converterInput(splitted[0].replace(" ", ""));
            String output = splitted[1].replace(" ", "");

            boolean result = processarExpressao(texto);
            System.out.println(result);

            if (outputs.containsKey(output)) {
                outputs.put(output, result);
            } else {
                variaveis.put(output, result);
            }
        }

        System.out.println(outputs);
        System.out.println(variaveis);
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
        if (inputs.containsKey(key)) {
            return inputs.get(key);
        } else if (outputs.containsKey(key)) {
            return outputs.get(key);
        } else if (variaveis.containsKey(key)) {
            return variaveis.get(key);
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