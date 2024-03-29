package com.clp.trabalho;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.w3c.dom.Text;

import javax.swing.*;
import java.io.*;
import java.io.BufferedReader;
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

    @FXML
    private Label hadEdit;

    @FXML
    private TextField varredura;

    public static List<Character> palavrasReservadas = Arrays.asList('+', '*', '!', '=', '(',  ')');

    @FXML
    public void initialize()
    {
        lerInputs();
        atualizarInterface();
    }

    public void atualizarInterface() {
        entradas.setText(getEntradas());
        saidas.setText(getSaidas());
        variaveis.setText(getVariaveis());
        varredura.setText(String.valueOf(Status.varredura));
        checkarAlteracoes();
    }

    @FXML
    protected void onProcessarButtonClick() {
        Status.textareaInput = processarInput.getText().split("\n");

        if (Status.textareaInput[0].length() == 0) {
            JOptionPane.showMessageDialog(null,
                    "Por favor, digite o código.",
                    "ALERTA",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            hadEdit.setText("");
            ProcessarController.processarInput(Status.textareaInput);
            atualizarInterface();

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
        }
    }

    @FXML
    protected void checkarAlteracoes() {
        var anterior = Status.textareaInput;
        var atual = processarInput.getText().split("\n");

        if(concatenar(anterior).equals(concatenar(atual)))
        {
            hadEdit.setText("");
        }
        else {
            hadEdit.setText("*");
        }

    }

    public String concatenar(String[] toConcatena)
    {
        var retorno = "";
        for (String concatena: toConcatena)
        {
            retorno += concatena;
        }
        return (retorno);
    }

    protected static void processarInput(String[] input) {
        Status.variaveis.clear();
        if (input[0].length() != 0) {
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
        }
    }

    @FXML
    protected void onResetarButtonClick(){
        processarInput.setText("");
        Status.textareaInput = new String[]{""};
        Status.variaveis.clear();
        Status.resetOutput();
        atualizarInterface();
    }

    public void enviarOutputs(OutputStream outputStream) {
        String output = "";
        System.out.println("ENVIANDO OUTPUTS");
        for (int i = 8; i > 0; i--) {
            String outputKey = "O" + i;
            Boolean value = Status.outputs.get(outputKey);

            if (value) {
                output += "1";
            } else {
                output += "0";
            }
        }

        System.out.println(output);

        try {
            outputStream.write(output.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //.writeBytes(output.getBytes(), output.length());
    }

    @FXML
    public void lerInputs(){
        new Thread(() -> {
            OutputStream outputStream = Status.comPort.getOutputStream();
            InputStream inputStream = Status.comPort.getInputStream();
            try {
                while (true) {
                    System.out.println("Ler inputs...");

                    Thread.sleep(Status.varredura);
                    System.out.println(inputStream.available());
                    if(inputStream.available() > 0){

                        int readOneByASCII;
                        String inputs = "";
                        do{
                            readOneByASCII = inputStream.read();
                            char letraEscrita = (char)readOneByASCII;
                            inputs = inputs+letraEscrita;
                        }
                        while (readOneByASCII != 10);

                        inputs.trim();
                        System.out.println(inputs.length());
                        if(inputs.length() == 11){
                            System.out.println("aqui");
                            Status.initializeStatus(inputs.substring(0,9));
                            this.enviarOutputs(outputStream);

                            atualizarInterface();
                        }
                    }
                }
            } catch (InterruptedException e) {
                //throw new RuntimeException(e);
            } catch (Exception e) {
                //throw new RuntimeException(e);
            }
        }).start();
    }

    protected static String getEntradas() {

        String entradaToSet = "";

        for (int i = 0; i < 9; i++) {
            String inputKey = "I" + (i+1);
            Boolean value = Status.inputs.get(inputKey);

            if (value) {
                entradaToSet += inputKey+" ➜ ✔ Ligada";
            } else {
                entradaToSet += inputKey+" ➜ ❌ Desligada";
            }

            if(i!=8){
                entradaToSet += "\n";
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
                saidaToSet += outputKey+" ➜ ✔ Ligada\n";
            } else {
                saidaToSet += outputKey+" ➜ ❌ Desligada\n";
            }
        }

        return(saidaToSet);
    }
    protected static String getVariaveis() {

        String variaveisToSet = "";
        for (String var: Status.variaveis.keySet())
        {
            Boolean value = Status.variaveis.get(var);

            if (value) {
                variaveisToSet += var + " ➜ ✔ Verdadeiro\n";
            } else {
                variaveisToSet += var + " ➜ ❌ Falso\n";
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
    protected void onVarreduraBtnClck(){

        try {
            if(varredura != null && Integer.parseInt(varredura.getText()) > 0 && Integer.parseInt(varredura.getText()) < 9999)
            {
                var newVarredura = Integer.parseInt(varredura.getText());
                Status.varredura = newVarredura;
                System.out.println("Varredura = " + Status.varredura);
            }
            else{
                JOptionPane.showMessageDialog(null,
                        "Varredura inválida! Verifique a regra no botão de Ajuda ('?').",
                        "ERRO",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        catch(NumberFormatException e)
        {
            JOptionPane.showMessageDialog(null,
                    "Verifique a varredura a ser atualizada. Digite um valor válido!",
                    "ERRO",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML
    protected void saveCode() throws IOException{
        try {
            String filename = "src\\" + (JOptionPane.showInputDialog("Nome do arquivo: ")) + ".txt";
            FileWriter myWriter = new FileWriter(filename);
            myWriter.write(processarInput.getText());
            myWriter.close();
            JOptionPane.showMessageDialog(null,
                    "Código salvo com sucesso!",
                    "SUCESSO!",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        catch (IOException e)
        {
            System.out.println("erro: " + e);
            JOptionPane.showMessageDialog(null,
                    "Erro ao exportar o Código:" + e,
                    "ERRO",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML
    protected void importCode() throws IOException{
        try {
            String filePath = "src\\" + (JOptionPane.showInputDialog("Nome do arquivo a ser importado da raíz do projeto: ")) + ".txt";
            FileInputStream fis = new FileInputStream(filePath);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String line = br.readLine();
            String code = "";

            while (line != null) {
                code += line + "\n";
                line = br.readLine();
            }

            processarInput.setText(code.replaceAll("null", ""));
            br.close();
        }
        catch (IOException e)
        {
            System.out.println("erro: " + e);
            JOptionPane.showMessageDialog(null,
                    "Erro ao importar o Código:" + e,
                    "ERRO",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    @FXML
    protected void onHelpButtonClick(){
        JLabel helpMessage = new JLabel(
                "<html>" +
                        "<div style='font-size: 10px;'>" +
                            "<div><b>Instruções:</b></div>" +
                            "<div style='margin: 10px 0 0 15px;'>" +
                                "<div>" +
                                    "<b>• Para programar: </b> Formate como 'Variável/Entrada -> Variável/Saída' e clique em 'Processar';" +
                                "</div>" +
                                "<br/>" +
                                "<div>" +
                                    "<b>• Símbolos Reservados: </b> '(' e ')' para priorizar, '&&' ou '*' usados como 'and', '||' ou '+' usados como 'or' e '!' para negar a senteça seguinte." +
                                "</div>" +
                                "<br/>" +
                                "<div>" +
                                    "<b>• Varredura: </b> É medida em milisegundos, pode ser atualizada digitando o número no campo de texto (0 - 9999) e clicando em 'Atualizar'." +
                                "</div>" +
                                "<br/>" +
                                "<div>" +
                                    "<b>• Importar Arquivo: </b> É importado da pasta src do projeto." +
                                    "<b>• Exportar Arquivo: </b> É exportado da pasta src do projeto." +
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
                        "<div style='font-size: 10px;'>" +
                            "<b>Autores:</b>" +
                            "<div style='margin: auto 5px 15px 10px;'>" +
                                "Felipe Costa Godoi,<br/> " +
                                "Guilherme Rodrigues de Melo,<br/> " +
                                "Paulo Victor Ferreira da Cruz." +
                            "</div>" +
                        "</div>" +
                    "</html>");
        JOptionPane.showMessageDialog(null,
                infoMessage,
                "INTEGRANTES",
                JOptionPane.INFORMATION_MESSAGE);
    }


}