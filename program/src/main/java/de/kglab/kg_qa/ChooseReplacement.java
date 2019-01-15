package de.kglab.kg_qa;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

class ChooseReplacement extends SingleStatementAction implements SingleChoiceAction {

    private RDFNode[] options;
    private String[] optionsAsStrings;

    ChooseReplacement(Recommender recommender, Statement statement, RDFNode[] options) {
        super(recommender, statement);
        String[] optionsAsStrings = new String[options.length];
        for (int i = 0; i< options.length; i++) {
            optionsAsStrings[i] = options[i].toString();
        }
        this.options = options;
        this.optionsAsStrings =  (String[]) ArrayUtils.addAll(optionsAsStrings, "skip");
    }

	@Override
	public String requestInput() {
        this.ui.showInfo("With which item do you want to replace?");
        return this.ui.chooseFromOptions(optionsAsStrings);
	}

    @Override
    public void actOnInput(String userInput) {
        if (userInput.equals("skip")) {
            this.ui.showInfo("Skipped replacement.");
        }
        else if (Arrays.asList(optionsAsStrings).contains(userInput)) {
            Resource subject = this.statement.getSubject();
            Property predicate = this.statement.getPredicate();
            RDFNode object = this.statement.getObject();
            this.model.remove(statement);
            this.ui.showInfo("Removed one triple: "+subject+" "+predicate+" "+object+".");
            
            RDFNode newObject = null;
            for (int i = 0; i< optionsAsStrings.length; i++) {
                if (optionsAsStrings[i].equals(userInput)) {
                    newObject = options[i];
                }
            }
            Statement newStatement = this.model.createStatement(subject, predicate, newObject);
            this.model.add(newStatement);
            this.ui.showInfo("Added one triple: "+subject+" "+predicate+" "+userInput+".");
            this.logger.logChange(this.statement, newStatement);
        }
        else {
            this.ui.showError("Not a valid Input. Please choose from one of the options in: "+Arrays.toString(options));
        }
    }

}
