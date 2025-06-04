/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package main;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.swing.JTextPane;

/**
 *
 * @author speedchela
 */
public class Intermedio {

    /*
     Base del proyecto
     Se deben de realizar varios pasos para que se realize bien el codigo intermedio:
        1.- Desglose de operaciones por variables temporales (t1,t2,t3,etc)
        2.- Crear las etiquetas en cuestion de las condicionales los cuales son
             if -> si
             if else -> si sino
             while -> mentre
             and -> y
             or -> o
             not -> non
        
        Estas deben de tener sus etiquetas las cuales cumplan el proposito a manera de ifs
        Ya los errores no deben de detectar
    
       El programa debe de ir leyendo los renglones y hara lo siguiente:
        Detectara las variables que sean numero
    */    
    
    // Mapa para almacenar variables y sus valores iniciales
    private Map<String, String> variablesConValores = new HashMap<>();
    private int temporalCounter = 1; // Contador para nombres de temporales (t1, t2, etc.)

    public String[] guardarCodigoEnArreglo(JTextPane textPane) {
        // Obtener el texto del JTextPane
        String texto = textPane.getText();
        
        // Dividir el texto en líneas usando split() con el salto de línea como delimitador
        String[] lineas = texto.split("\n");
        
        // Retornar el arreglo con las líneas del código
        return lineas;
    }
    
    public List<String> detectarVariablesNumericas(String[] lineasCodigo) {
        List<String> variables = new ArrayList<>();
        String[] tipos = {"entier", "dobro", "flottan"};

        // Recorrer cada línea del arreglo
        for (String linea : lineasCodigo) {
            linea = linea.trim();
            if (linea.isEmpty()) continue;

            // Verificar si la línea comienza con un tipo numérico
            boolean esDeclaracion = false;
            for (String tipo : tipos) {
                if (linea.startsWith(tipo)) {
                    esDeclaracion = true;
                    // Extraer la parte después del tipo
                    String restoLinea = linea.substring(tipo.length()).trim();
                    // Dividir por comas para manejar múltiples variables
                    String[] partes = restoLinea.split(",");

                    for (String parte : partes) {
                        String variable = parte.trim();
                        String valorInicial = null;

                        // Verificar si hay un valor inicial (contiene "=")
                        if (variable.contains("=")) {
                            String[] asignacion = variable.split("=");
                            variable = asignacion[0].trim();
                            valorInicial = asignacion[1].replaceAll("[;].*", "").trim();
                        } else {
                            // Limpiar ; si no hay asignación
                            variable = variable.replaceAll("[;].*", "").trim();
                        }

                        if (!variable.isEmpty()) {
                            variables.add(variable);
                            // Guardar en el mapa, incluso si no hay valor inicial
                            variablesConValores.put(variable, valorInicial);
                        }
                    }
                    break;
                }
            }
            // Si no es una declaración, verificar si es una asignación a una variable existente
            if (!esDeclaracion && linea.contains("=")) {
                String[] asignacion = linea.split("=");
                String variable = asignacion[0].trim();
                if (variables.contains(variable)) {
                    String valor = asignacion[1].replaceAll("[;].*", "").trim();
                    variablesConValores.put(variable, valor);
                }
            }
        }
        return variables;
    }

    // Método para consultar el valor inicial de una variable
    public String obtenerValorInicial(String nombreVariable) {
        return variablesConValores.getOrDefault(nombreVariable, "No inicializada");
    }

    // Método para desglosar operaciones matemáticas
    public List<String> desglosarOperacion(String linea) {
        List<String> pasos = new ArrayList<>();
        if (!linea.contains("=")) return pasos; // Si no es una asignación, retornar vacío

        // Separar variable asignada y expresión
        String[] partes = linea.split("=");
        if (partes.length != 2) return pasos; // Formato inválido
        String variableAsignada = partes[0].trim();
        String expresion = partes[1].replaceAll("[;].*", "").trim();

        // Validar que la variable asignada exista
        if (!variablesConValores.containsKey(variableAsignada)) {
            pasos.add("Error: Variable " + variableAsignada + " no declarada.");
            return pasos;
        }

        // Procesar la expresión
        pasos.addAll(descomponerExpresion(expresion, variableAsignada));
        return pasos;
    }

    private List<String> descomponerExpresion(String expresion, String variableAsignada) {
        List<String> pasos = new ArrayList<>();
        Stack<Character> operadores = new Stack<>();
        Stack<String> operandos = new Stack<>();
        StringBuilder token = new StringBuilder();
        int i = 0;

        while (i < expresion.length()) {
            char c = expresion.charAt(i);

            // Manejar paréntesis
            if (c == '(') {
                int nivel = 1;
                i++;
                StringBuilder subExpresion = new StringBuilder();
                while (i < expresion.length() && nivel > 0) {
                    if (expresion.charAt(i) == '(') nivel++;
                    if (expresion.charAt(i) == ')') nivel--;
                    if (nivel > 0) subExpresion.append(expresion.charAt(i));
                    i++;
                }
                // Procesar subexpresión dentro de paréntesis
                String nombreTemporal = "t" + temporalCounter++;
                pasos.addAll(descomponerExpresion(subExpresion.toString(), nombreTemporal));
                operandos.push(nombreTemporal);
                continue;
            }

            // Acumular tokens (números o variables)
            if (Character.isLetterOrDigit(c) || c == '.') {
                token.append(c);
                i++;
                continue;
            }

            // Procesar token acumulado
            if (token.length() > 0) {
                String operando = token.toString().trim();
                if (!operando.isEmpty()) {
                    operandos.push(operando);
                }
                token.setLength(0);
            }

            // Manejar operadores
            if (c == '+' || c == '-' || c == '*' || c == '/') {
                while (!operadores.isEmpty() && debeProcesarOperador(operadores.peek(), c)) {
                    generarPasoIntermedio(pasos, operadores, operandos);
                }
                operadores.push(c);
                i++;
                continue;
            }

            i++;
        }

        // Procesar el último token
        if (token.length() > 0) {
            String operando = token.toString().trim();
            if (!operando.isEmpty()) {
                operandos.push(operando);
            }
        }

        // Procesar operadores restantes
        while (!operadores.isEmpty()) {
            generarPasoIntermedio(pasos, operadores, operandos);
        }

        // Asignar el resultado final a la variable
        if (!operandos.isEmpty()) {
            pasos.add(variableAsignada + "=" + operandos.pop());
        }

        return pasos;
    }

    private boolean debeProcesarOperador(char operadorActual, char nuevoOperador) {
        int precedenciaActual = getPrecedencia(operadorActual);
        int precedenciaNuevo = getPrecedencia(nuevoOperador);
        return precedenciaActual >= precedenciaNuevo;
    }

    private int getPrecedencia(char operador) {
        switch (operador) {
            case '*':
            case '/':
                return 2;
            case '+':
            case '-':
                return 1;
            default:
                return 0;
        }
    }

    private void generarPasoIntermedio(List<String> pasos, Stack<Character> operadores, Stack<String> operandos) {
        if (operandos.size() < 2 || operadores.isEmpty()) return;
        String operando2 = operandos.pop();
        String operando1 = operandos.pop();
        char operador = operadores.pop();
        String temporal = "t" + temporalCounter++;
        pasos.add(temporal + "=" + operando1 + operador + operando2);
        operandos.push(temporal);
    }
}