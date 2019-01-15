package de.kglab.kg_qa;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;

class PlainTextLogger implements Logger {

    FileWriter logFile;

    PlainTextLogger() {
        try {
            logFile = new FileWriter("./assets/log.txt", true);
        } catch (IOException e) {
            System.out.println("Couldn't open log file.");
            e.printStackTrace();
        }
    }

    public void logChange(Statement oldStatement, Statement newStatement) {
        String oldString = getStatementString(oldStatement);
        String newString = getStatementString(newStatement);
        try {
            logFile.write("Change: "+oldString+" \"to\" "+newString+" \n");
        } catch (IOException e) {
            System.out.println("IO Error occured.");
            e.printStackTrace();
        }
    }

    public void logRemoval(Statement statement) {
        String asString = getStatementString(statement);
        try {
            logFile.write("Removal: "+asString+" \n");
        } catch (IOException e) {
            System.out.println("IO Error occured.");
            e.printStackTrace();
        }
    }

    public void logAddition(Statement statement) {
        String asString = getStatementString(statement);
        try {
            logFile.write("Addition: "+asString+" \n");
        } catch (Exception e) {
            System.out.println("IO Error occured.");
            e.printStackTrace();
        }
    }

    void closeLogFile() {
        try {
            logFile.close();
        } catch (IOException e) {
            System.out.println("IO Error occured.");
            e.printStackTrace();
        }
    }

    private String getStatementString(Statement statement) {
        RDFNode object = statement.getObject();
        String objectString = "";
        if (object.isResource()) {
            objectString = object.toString();
        } 
        else {
            objectString = " \"" + object.toString() + "\"";
        }
        return statement.getSubject()+" "+statement.getPredicate()+" "+objectString;
    }
}
