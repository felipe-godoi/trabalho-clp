package com.clp.trabalho;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import com.fazecast.jSerialComm.*;

import javax.swing.*;
import java.util.*;

import static javax.swing.JOptionPane.showMessageDialog;

public class ProcessarController {
    @FXML
    private TextArea processarInput;

    @FXML
    private TextArea entradas;
    @FXML
    private TextArea saidas;
    @FXML
    private TextArea variaveis;

    public static List<Character> palavrasReservadas = Arrays.asList('+', '*', '!', '=', '(',  ')');

    @FXML
    public void initialize()
    {
        entradas.setText(getEntradas());
        saidas.setText(getSaidas());
        variaveis.setText(getVariaveis());
    }

    @FXML
    protected void onProcessarButtonClick() {
        Status.textareaInput = processarInput.getText().split("\n");

        ProcessarController.processarInput(Status.textareaInput);
        initialize();
    }

    protected static void processarInput(String[] input) {
        if (input[0].length() == 0) {
            JOptionPane.showMessageDialog(null,
                    "Por favor, digite o código.",
                    "ALERTA",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            for (int i = 0; i < input.length; i++) {
                String[] splitted = input[i].split("->");
                String texto = converterInput(splitted[0].replace(" ", ""));
                String output = splitted[0].replace(" ", "");
                boolean result = false;
                    result = processarExpressao(texto);

                if (Status.outputs.containsKey(output)) {
                    Status.outputs.put(output, result);
                } else {
                    Status.variaveis.put(output, result);
                }
            }
            JOptionPane.showMessageDialog(null,
                    "Código compilado com Sucesso!",
                    "SUCESSO",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception error)
        {
            if(error.getMessage().contains("Unexpected value"))
            {
                JOptionPane.showMessageDialog(null,
                        "Código não identificado, verifique-o novamente.",
                        "ERRO",
                        JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                JOptionPane.showMessageDialog(null,
                        "Houve um erro, por favor tente novamente.",
                        "ERRO",
                        JOptionPane.ERROR_MESSAGE);
            }
            System.out.println("Erro identificado: " + error);
        }
    }

    @FXML
    protected void onResetarButtonClick(){
        processarInput.setText("");
        lerInputs();
    }

    public static void enviarOutputs() {
        String output = "";

        for (int i = 8; i > 0; i--) {
            String outputKey = "O" + i;
            Boolean value = Status.outputs.get(outputKey);

            if (value) {
                output += "1";
            } else {
                output += "0";
            }
        }

        Status.comPort.writeBytes(output.getBytes(), output.length());
    }

    public static String binToDec(String bin) {
        int sum = 0;
        int j = 1;
        for (int i = 7; i >= 0; i --) {
            if (bin.charAt(i) == '1') {
                sum += j;
            }

            j = j*2;
        }

        return String.valueOf(sum);
    }

    @FXML
    public static void lerInputs(){
        Status.comPort.addDataListener(new SerialPortDataListener() {
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
        Status.comPort.writeBytes(new byte[]{'1', '0', '0', '0', '0', '1', '1', '1', '0'}, 9);
    }


    protected static String getEntradas() {

        String entradaToSet = "";

        for (int i = 0; i < 9; i++) {
            String inputKey = "I" + (i+1);
            Boolean value = Status.inputs.get(inputKey);

            if (value) {
                entradaToSet += "I"+(i+1)+" => Ligada\n";
            } else {
                entradaToSet += "I"+(i+1)+" => Desligada\n";
            }
        }
        return(entradaToSet);
    }

    protected static String getSaidas() {

        String saidaToSet = "";

        for (int i = 0; i < 8; i++) {
            String outputKey = "O" + (i+1);
            Boolean value = Status.outputs.get(outputKey);

            if (value) {
                saidaToSet += "I"+(i+1)+" => Ligada\n";
            } else {
                saidaToSet += "I"+(i+1)+" => Desligada\n";
            }
        }

        return(saidaToSet);
    }
    protected static String getVariaveis() {

        System.out.println(Status.variaveis);
        String variaveisToSet = "";
        for (String var: Status.variaveis.keySet())
        {
            Boolean value = Status.variaveis.get(var);

            if (value) {
                variaveisToSet += var + " => " + value + "\n";
            } else {
                variaveisToSet += var + " => " + value + "\n";
            }
        }

        return(variaveisToSet);
    }

    protected static String converterInput(String texto) {
        return texto.replace("&&", "*").replace("||", "+").replace(" ", "");
    }

    protected static int getConteudoParenteses(String texto) {
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

    protected static boolean avaliarExpressao(boolean valorEsquerda, char operador, boolean valorDireita) {
        if (operador == '*') {
            return valorEsquerda && valorDireita;
        } else {
            return valorEsquerda || valorDireita;
        }
    }

    protected static boolean getValue(String key) {
        if (Status.inputs.containsKey(key)) {
            return Status.inputs.get(key);
        } else if (Status.outputs.containsKey(key)) {
            return Status.outputs.get(key);
        } else if (Status.variaveis.containsKey(key)) {
            return Status.variaveis.get(key);
        }

        throw new IllegalStateException("Unexpected value: " + key);
    }

    protected static boolean processarExpressao(String texto) {
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
                valorDireita = !getValue(bufferDireita.replace("!", ""));
            } else {
                valorDireita = getValue(bufferDireita);
            }
            valorEsquerda = avaliarExpressao(valorEsquerda, operador, valorDireita);
        }

        return valorEsquerda;
    }


    @FXML
    protected void onHelpButtonClick(){

        JLabel helpMessage = new JLabel(
                "<html>" +
                        "<div style='font-size: 11px;'>" +
                            "<div><b>Instruções:</b></div>" +
                            "<div style='margin: 10px 0 0 15px;'>" +
                                "<div>" +
                                    "<b>• Para programar: </b> 'VariávelDeDestino -> VariávelAReceber' e clique em 'Processar';" +
                                "</div>" +
                                "<br/>" +
                                "<div>" +
                                    "<b>• Ao receber um Erro: </b> Verifique a formatação do código e clique em 'Processar' novamente." +
                                "</div>" +
                                "<br/><br/>" +
                            "</div>" +
                        "</div>" +
                    "</html>");

        JOptionPane.showMessageDialog(null,
                helpMessage,
                "INSTRUÇÕES",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @FXML
    protected void onInfoButtonClick(){
        JLabel infoMessage = new JLabel(
                "<html>" +
                        "<div style='font-size: 11px;'>" +
                            "<b>Autores:</b>" +
                            "<div style='margin-left: 10px;'>" +
                                "Felipe Costa Godoi,<br/> " +
                                "Guilherme Rodrigues de Melo,<br/> " +
                                "Paulo Victor Ferreira da Cruz.<br/>" +
                            "</div>" +
                        "</div>" +
                    "</html>");
        JOptionPane.showMessageDialog(null,
                infoMessage,
                "INTEGRANTES",
                JOptionPane.INFORMATION_MESSAGE);
    }


}