package de.kglab.kg_qa;

import org.apache.jena.rdf.model.Statement;

interface Logger {
    void logChange(Statement oldStatement, Statement newStatement);
    void logRemoval(Statement statement);
	void logAddition(Statement statement);
}
