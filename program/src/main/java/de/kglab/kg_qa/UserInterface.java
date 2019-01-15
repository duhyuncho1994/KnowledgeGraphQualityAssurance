package de.kglab.kg_qa;

import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

interface UserInterface {

    String chooseFromOptions(String[] options);
    String[] chooseFromMultiOptions(String[] multioptions, int numberOfChoices);
    void showError(String errorMsg);
    void showInfo(String infoMsg);
    void close();
    void printStatements(StmtIterator iterator);
    void printStatements(Statement[] statements);
}
