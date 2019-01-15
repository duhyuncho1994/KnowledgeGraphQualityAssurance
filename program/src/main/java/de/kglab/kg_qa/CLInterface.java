package de.kglab.kg_qa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

class CLInterface implements UserInterface {

    Scanner scanner;

    CLInterface() {
        this.scanner = new Scanner(System.in);
    }

    public String chooseFromOptions(String[] options){
        System.out.println("Please choose from one of the options below:");
        System.out.println("For the input you have to type the number.");
        for (int i = 0; i < options.length; i++) {
            System.out.println(i+": "+options[i]);
        }
        String input = "";
        int inputAsInt = -1;
        for (boolean validInput = false; !validInput;) {
            input = scanner.next(); 
            try {
                inputAsInt = Integer.parseInt(input);
            }
            catch (Exception exception) {
                showError("Please input a valid integer.");
                continue;
            }
            if (inputAsInt < 0 || inputAsInt >= options.length) {
                showError("Please input an integer in the provided range.");
                continue;
            }
            validInput = true;
        }
        return options[inputAsInt];
    }

    public String[] chooseFromMultiOptions(String[] multioptions, int numberOfChoices) {
        List<String> availableOptions = new ArrayList<>(Arrays.asList(multioptions));
        List<String> currentOptions = new ArrayList<>();
        for (int i = 0; i < numberOfChoices; i++) {
            String[] availOptsArray = new String[availableOptions.size()];
            availOptsArray = availableOptions.toArray(availOptsArray);
            String userInput = chooseFromOptions(availOptsArray);
            currentOptions.add(userInput);
            if (!userInput.equals("skip")) {
                availableOptions.remove(userInput);
            }
        }
        String[] chosenOptions = new String[currentOptions.size()];
        chosenOptions = currentOptions.toArray(chosenOptions);
        return chosenOptions;
    }

    public void showError(String errorMsg) {
        System.out.println("Error: "+errorMsg);
    }

    public void showInfo(String infoMsg) {
        System.out.println("Info: "+infoMsg);
    }

    public void close() {
        this.scanner.close();
    }

    public void printStatements(Statement[] statements) {
        for (Statement statement : statements) {
            Resource  subject   = statement.getSubject();     // get the subject
            Property  predicate = statement.getPredicate();   // get the predicate
            RDFNode   object    = statement.getObject();      // get the object

            System.out.print(subject.toString());
            System.out.print(" " + predicate.toString() + " ");
            if (object instanceof Resource) {
                System.out.print(object.toString());
            } else {
                // object is a literal
                System.out.print(" \"" + object.toString() + "\"");
            }

            System.out.println(" .");
        }
    }

    public void printStatements(StmtIterator iterator) {
        List<Statement> statementList = new ArrayList<>();
        while (iterator.hasNext()) {
            statementList.add(iterator.next());
        }
        Statement[] statements = new Statement[statementList.size()];
        statements = statementList.toArray(statements);
        printStatements(statements);
    }
}
