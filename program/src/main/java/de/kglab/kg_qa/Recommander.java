package de.kglab.kg_qa;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

class Recommender {

    ValidationReporter reporter;
    UserInterface ui;
    Logger logger;
    Model mo; // The model
    Model coMo; // The constraint model

    List<RecommendedAction> recommendations;
    Map<RecommendedAction,String> decisions;
    
    final boolean autoRemoveAllFaults;
    final boolean automateDeletion;

    Recommender(ValidationReporter reporter, UserInterface ui, Logger logger) {
        this.reporter = reporter;
        this.ui = ui;
        this.logger = logger;
        this.mo = this.reporter.getModel();
        this.recommendations = new ArrayList<>();
        this.coMo = this.reporter.getConstraintModel();
        this.decisions = new HashMap<>();
        
        boolean[] config = configRecommender();
        autoRemoveAllFaults = config[0];
        automateDeletion = config[1];
    }

	private boolean[] configRecommender() {
        boolean[] config = new boolean[2];
		ui.showInfo("Do you want to automatically delete all faulty triples?");
        config[0] = ui.chooseFromOptions(new String[] {"auto delete", "selective correction"}).equals("auto delete");
        if (!config[0]) {
            ui.showInfo("Do you wanto to automate parts of the enhancement?");
            config[1] = ui.chooseFromOptions(new String[] {"auto enhancement", "selective correction"}).equals("auto enhancement");
        }
        else {
            config[1] = false;
        }
        return config;
	}

	void recommendActions() {
        if (!reporter.getConformation()) {
            for (Resource result: this.reporter.getResults()) {
                if (autoRemoveAllFaults) {
                    removeFaults(result);
                }
                else {
                    recommendAction(result);
                }
            }
        }
        else {
            ui.showInfo("The Report conforms. There is nothing to recommend.");
        }
    }
    
    private void removeFaults(Resource result) {
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
            Statement[] statements = reporter.getConcernedStatements(subject, predicate);
            Property maxCountProp = coMo.createProperty(coMo.expandPrefix("sh:maxCount"));
        
            int maxCount = coMo.getRequiredProperty(sourceShape.inModel(coMo), maxCountProp).getObject().asLiteral().getInt();

            Statement[] statementsToRemove = Arrays.copyOfRange(statements, 0, statements.length - maxCount);

            removeStatements(statementsToRemove);
        }
        else if (sourceConstraintString.equals(mo.expandPrefix("sh:InConstraintComponent")) || sourceShape.toString().equals(mo.expandPrefix("db:Name"))) {
            RDFNode object = result.getRequiredProperty(valueProp).getObject().inModel(mo);
            
            Statement statement = mo.createStatement(subject, predicate, object);
            removeStatement(statement);
        }
	}

	private void removeStatements(Statement[] statements) {
        for (Statement statement : statements) {
            removeStatement(statement);
        }
	}

	private void removeStatement(Statement statement) {
		mo.remove(statement);
		ui.showInfo("Removed one triple: "+statement.getSubject()+" "+statement.getPredicate()+" "+statement.getObject()+".");
	}

	private void recommendAction(Resource result) {
        Property sourceShapeProp = mo.createProperty(mo.expandPrefix("sh:sourceShape"));
        Property focusNodeProp = mo.createProperty(mo.expandPrefix("sh:focusNode"));
        Property resultPathProp = mo.createProperty(mo.expandPrefix("sh:resultPath"));
        Property sourceConstraintProp = mo.createProperty(mo.expandPrefix("sh:sourceConstraintComponent"));
        Property valueProp = mo.createProperty(mo.expandPrefix("sh:value"));
        Property inProp = coMo.createProperty(coMo.expandPrefix("sh:in"));
        
        String predicateURI = result.getPropertyResourceValue(resultPathProp).toString();
        String sourceConstraintString = result.getPropertyResourceValue(sourceConstraintProp).toString();
        
		Resource sourceShape = result.getPropertyResourceValue(sourceShapeProp);
        Resource subject = result.getPropertyResourceValue(focusNodeProp).inModel(mo);
        Property predicate = mo.createProperty(predicateURI);
        
		if (sourceConstraintString.equals(mo.expandPrefix("sh:MaxCountConstraintComponent"))) {
            Statement[] statements = reporter.getConcernedStatements(subject, predicate);
            
            Property maxCountProp = coMo.createProperty(coMo.expandPrefix("sh:maxCount"));

            int maxCount = coMo.getRequiredProperty(sourceShape.inModel(coMo), maxCountProp).getObject().asLiteral().getInt();

            ChooseRemoval removal = new ChooseRemoval(this, statements, maxCount);
            recommendations.add(removal);
        }
        else if (sourceConstraintString.equals(mo.expandPrefix("sh:InConstraintComponent"))) {
            RDFNode object = result.getRequiredProperty(valueProp).getObject().inModel(mo);
            Resource inList = coMo.getRequiredProperty(sourceShape.inModel(coMo), inProp).getObject().asResource();
            
            Statement statement = mo.createStatement(subject, predicate, object);
            RDFNode[] inArray = parseRDFList(inList);

            ChooseReplacement replacement = new ChooseReplacement(this, statement, inArray);

            recommendations.add(replacement);
        }
        else if (sourceShape.toString().equals(mo.expandPrefix("db:Name"))) {
            Property labelProp = mo.createProperty(mo.expandPrefix("rdfs:label"));

            RDFNode object = result.getRequiredProperty(valueProp).getObject().inModel(mo);
            
            Statement statement = mo.createStatement(subject, predicate, object);
            Statement newStatement = mo.getRequiredProperty(subject, labelProp);

            Statement[] statements = {statement, newStatement};
            Replacement replacement = new Replacement(this, statements);

            recommendations.add(replacement);
        }
        else if (sourceShape.toString().equals(mo.expandPrefix("db:ProperDates"))) {
            RDFNode object = mo.createLiteral(getFormattedTime());
            Statement statement = mo.createStatement(subject, predicate, object);

            Addition replacement = new Addition(this, statement);
            recommendations.add(replacement);
        }
    }
    
    private RDFNode[] parseRDFList(Resource listResource) {
        List<RDFNode> javaList = new ArrayList<>();
        Property restProp = mo.createProperty(mo.expandPrefix("rdf:rest"));
        Property firstProp = mo.createProperty(mo.expandPrefix("rdf:first"));
        Resource cur = listResource;
        while (!mo.expandPrefix("rdf:nil").equals(cur.getURI())) {
            javaList.add(cur.getRequiredProperty(firstProp).getObject());
            cur = cur.getRequiredProperty(restProp).getResource();
        }
        RDFNode[] array = new RDFNode[javaList.size()];
        array = javaList.toArray(array);
        return array;
    }
    
    void actOnRecommendations() {
        for(RecommendedAction action : this.recommendations) {
            if (action.isAutomatable()) {
                handleAutomatableAction(action);
            }
            else if (action instanceof SingleChoiceAction) {
                SingleChoiceAction castedAction = (SingleChoiceAction) action;
                String userInput = castedAction.requestInput();
                castedAction.actOnInput(userInput);
            }
            else if (action instanceof MultipleChoiceAction) {
                MultipleChoiceAction castedAction = (MultipleChoiceAction) action;
                String[] userInput = castedAction.requestInput();
                castedAction.actOnInput(userInput);
            }
            else {
                ui.showError("Unknown type of action found.");
            }
        }
    }

	private void handleAutomatableAction(RecommendedAction action) {
		if (action instanceof Replacement) {
		    handleReplacement(action);
        }
        else if (action instanceof Addition) {
            handleAddition(action);
        }
	}

	private void handleAddition(RecommendedAction action) {
        SingleChoiceAction castedAction = (SingleChoiceAction) action;
        if (automateDeletion) {
            castedAction.actOnInput("add");
        }
        else {
            String userInput = ((Addition) action).requestInput();
            castedAction.actOnInput(userInput);
        }
	}

	private void handleReplacement(RecommendedAction action) {
		Replacement castedAction = (Replacement) action;
		String userInput;
		if (automateDeletion) {
		    String recommendedInput = getRecommendedInput(action);
		    if (recommendedInput != null) {
		        userInput = recommendedInput;
		    }
		    else {
		        userInput = castedAction.requestInput();
		    }
		    decisions.put(action, userInput);
		}
		else {
		    userInput = castedAction.requestInput();
		}
		castedAction.actOnInput(userInput);
	}

	private String getRecommendedInput(RecommendedAction action) {
        if (getNumberOfDecisions(action) >= 3) {
            return getMostCommonDecision(action);
        }
        else {
            return null;
        }
	}

	private String getMostCommonDecision(RecommendedAction action) {
        Map<String, Integer> inputFrequency = new HashMap<>();
        for (RecommendedAction curAction : decisions.keySet()) {
            if (curAction instanceof Replacement) {
                String input = decisions.get(curAction);
                if (inputFrequency.putIfAbsent(input, 1) != null){
                    inputFrequency.put(input, inputFrequency.get(input)+1);
                }
            }
        }

        String returnVal = null;
        int highestFrequency = -1;
        for (String input : inputFrequency.keySet()) {
            if (inputFrequency.get(input) > highestFrequency) {
                returnVal = input;
                highestFrequency = inputFrequency.get(input);
            }
        }
		return returnVal;
	}

	private int getNumberOfDecisions(RecommendedAction action) {
        int number = 0;
        for (RecommendedAction curAction: decisions.keySet()) {
            if (curAction instanceof Replacement) {
                number++;
            }
        }
		return number;
	}

	Model getModel() {
        return this.mo;
    }

    UserInterface getUI() {
        return this.ui;
    }

    Logger getLogger() {
        return this.logger;
    }

    private String getFormattedTime() {
        LocalDateTime dt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        String year = dt.getYear()+"-"+fillUp(dt.getMonthValue())+"-"+fillUp(dt.getDayOfMonth());
        String time = dt.getHour()+":"+fillUp(dt.getMinute())+":"+fillUp(dt.getSecond());
        String output = year+" "+time+" UTC";
        return output;
    }

    private static String fillUp(int value) {
        return (value>=10?"":"0")+String.valueOf(value);
    }
}
