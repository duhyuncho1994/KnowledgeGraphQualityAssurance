package de.kglab.kg_qa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

class ValidationReporter {

    Resource report;
    Model mo;
    Model reportModel;
    Model coMo;
    UserInterface ui;
    List<Resource> results;
    boolean reportConforms;

    ValidationReporter(Resource report, Model model, Model constraintModel, UserInterface ui) {
        this.report = report;
        this.mo = model;
        this.reportModel = report.getModel();
        this.coMo = constraintModel;
        this.ui = ui;
        this.results = new ArrayList<>();
        this.reportConforms = false;
    }

    Model getModel() {
        return this.mo;
    }

    Model getReportModel() {
        return this.reportModel;
    }

    Model getConstraintModel() {
        return this.coMo;
    }

    void parseReport() {
        this.reportConforms = doesReportConform();
        if (!reportConforms) {
            Property resultProperty = this.reportModel.createProperty(this.reportModel.expandPrefix("sh:result"));
            NodeIterator resultIterator = this.reportModel.listObjectsOfProperty(this.report, resultProperty);
            while (resultIterator.hasNext()) {
                RDFNode node = resultIterator.nextNode();
                Resource result = node.asResource();
                this.results.add(result);
            }
        }
    }

    private boolean doesReportConform() {
        Property conformProperty = this.reportModel.createProperty(this.reportModel.expandPrefix("sh:conforms"));
        NodeIterator iterator = this.reportModel.listObjectsOfProperty(this.report, conformProperty);
        boolean conforms = false;
        while (iterator.hasNext()) {
            boolean value = iterator.next().asLiteral().getBoolean();
            conforms = conforms || value;
        }
        return conforms;
    }

    List<Resource> getResults() {
        return this.results;
    }

    void reportStatistics() {
        int numOfFaultySubs = 0;
        int numOfFaultyPreds = 0;
        int numOfFaultyObs = 0;
        int numOfFaultyTrips = 0;
        int numOfFaultyNods = 0;
        Set<String> faultySubjects = new HashSet<>();
        Set<String> faultyPredicates = new HashSet<>();
        Set<String> faultyObjects = new HashSet<>();
        Set<String> faultyNodes = new HashSet<>();

        if (!getConformation()) {
            for (Resource result: getResults()) {
                numOfFaultyTrips = getFaultyElements(numOfFaultyTrips, faultySubjects, faultyPredicates, faultyObjects, result);
            }
        }
        faultyNodes.addAll(faultySubjects);
        faultyNodes.addAll(faultyObjects);
        numOfFaultySubs = faultySubjects.size();
        numOfFaultyPreds = faultyPredicates.size();
        numOfFaultyObs = faultyObjects.size();
        numOfFaultyNods = faultyNodes.size();

        reportFaultiness(numOfFaultySubs,"subject", "subjects");
        reportFaultiness(numOfFaultyPreds,"predicate", "predicates");
        reportFaultiness(numOfFaultyObs,"object", "objects");
        reportFaultiness(numOfFaultyNods,"nodes", "nodes");
        reportFaultiness(numOfFaultyTrips,"triple", "triples");
    }

	private int getFaultyElements(int numOfFaultyTrips, Set<String> faultySubjects, Set<String> faultyPredicates,
			Set<String> faultyObjects, Resource result) {
		Property sourceShapeProp = mo.createProperty(mo.expandPrefix("sh:sourceShape"));
		Property focusNodeProp = mo.createProperty(mo.expandPrefix("sh:focusNode"));
		Property resultPathProp = mo.createProperty(mo.expandPrefix("sh:resultPath"));
		Property sourceConstraintProp = mo.createProperty(mo.expandPrefix("sh:sourceConstraintComponent"));
		Property valueProp = mo.createProperty(mo.expandPrefix("sh:value"));
		
		String predicateURI = result.getPropertyResourceValue(resultPathProp).toString();
		String sourceConstraintString = result.getPropertyResourceValue(sourceConstraintProp).toString();
		
		Resource sourceShape = result.getPropertyResourceValue(sourceShapeProp);
		Resource subject = result.getPropertyResourceValue(focusNodeProp).inModel(mo);
		Property predicate = mo.createProperty(predicateURI);
		
		if (sourceConstraintString.equals(mo.expandPrefix("sh:MaxCountConstraintComponent"))) {
		    Statement[] statements = getConcernedStatements(subject, predicate);
		    Property maxCountProp = coMo.createProperty(coMo.expandPrefix("sh:maxCount"));
		    int maxCount = coMo.getRequiredProperty(sourceShape.inModel(coMo), maxCountProp).getObject().asLiteral().getInt();

		    faultySubjects.add(subject.toString());
		    faultyPredicates.add(predicate.toString());
		    for (Statement statement : statements) {
		        faultyObjects.add(statement.getObject().toString());
		    }
		    numOfFaultyTrips += statements.length - maxCount;
		}
		else if (sourceConstraintString.equals(mo.expandPrefix("sh:InConstraintComponent"))) {
		    RDFNode object = result.getRequiredProperty(valueProp).getObject().inModel(mo);

		    faultySubjects.add(subject.toString());
		    faultyPredicates.add(predicate.toString());
		    faultyObjects.add(object.toString());
		    numOfFaultyTrips++;
		}
		else if (sourceShape.toString().equals(mo.expandPrefix("db:Name"))) {
		    Property labelProp = mo.createProperty(mo.expandPrefix("rdfs:label"));
		    RDFNode object = result.getRequiredProperty(valueProp).getObject().inModel(mo);
		    Statement newStatement = mo.getRequiredProperty(subject, labelProp);
		    
		    faultySubjects.add(subject.toString());
		    faultyPredicates.add(mo.expandPrefix("rdfs:label"));
		    faultyPredicates.add(mo.expandPrefix("db:genericName"));
		    faultyObjects.add(object.toString());
		    faultyObjects.add(newStatement.getObject().toString());
		    numOfFaultyTrips += 2;
		}
		else if (sourceShape.toString().equals(mo.expandPrefix("db:ProperDates"))) {
		    faultySubjects.add(subject.toString());
		    faultyPredicates.add(subject.toString());
		    numOfFaultyTrips++;
		}
		return numOfFaultyTrips;
	}

    private void reportFaultiness(int num, String singular, String plural) {
        ui.showInfo("There "+(num==1?"is":"are")+" "+num+" faulty "+(num==1?singular:plural)+".");
	}

	Statement[] getConcernedStatements(Resource subject, Property predicate) {
		List<Statement> statementsList = new ArrayList<>();
        StmtIterator stmtIterator = subject.listProperties(predicate);
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.nextStatement();
            statementsList.add(statement);
        }
        Statement[] statements = new Statement[statementsList.size()];
        statements = statementsList.toArray(statements);
		return statements;
    }

    void printReport() {
        for (Resource result : this.results) {
            System.out.println("This is a result:");
            StmtIterator statements = result.listProperties();
            while (statements.hasNext()) {
                Statement curStatement = statements.nextStatement();
                Resource subject = curStatement.getSubject();
                Property predicate = curStatement.getPredicate();
                RDFNode object = curStatement.getObject();
                System.out.print(subject.toString());
                System.out.print(" " + predicate.toString() + " ");
                if (object.isResource()) {
                    System.out.print(object.toString());
                } else {
                    // object is a literal
                    System.out.print(" \"" + object.toString() + "\"");
                }
                System.out.println(" .");
            }
        }
    }

    boolean getConformation() {
        return this.reportConforms;
    }

}
