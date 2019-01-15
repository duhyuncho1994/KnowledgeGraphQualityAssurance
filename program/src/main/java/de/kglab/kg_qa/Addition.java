package de.kglab.kg_qa;

import java.util.Arrays;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

class Addition extends SingleStatementAction implements SingleChoiceAction {

    private static final String[] OPTIONS = {"add", "skip"};

    Addition(Recommender recommender,Statement statement) {
        super(recommender, statement);
    }

	@Override
	public String requestInput() {
        this.ui.showInfo("Do you want to add the item?");
        return this.ui.chooseFromOptions(Addition.OPTIONS);
	}

    @Override
    public void actOnInput(String userInput) {
        switch (userInput) {
            case "add": 
                Resource subject = this.statement.getSubject();
                Property predicate = this.statement.getPredicate();
                RDFNode object = this.statement.getObject();

                this.model.add(this.statement);
                this.ui.showInfo("Added one triple: "+subject+" "+predicate+" "+object+".");
                this.logger.logAddition(this.statement);
                break;
            case "skip": 
                this.ui.showInfo("Skipped removal.");
                break;
            default:
                this.ui.showError("Not a valid Input. Please choose from one of the options in: "+Arrays.toString(Addition.OPTIONS));
                break;
        }
    }

    @Override
    boolean isAutomatable() {
        return true;
    }

}
