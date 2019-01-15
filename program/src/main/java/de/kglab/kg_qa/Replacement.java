package de.kglab.kg_qa;

import java.util.Arrays;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

class Replacement extends MultipleStatementAction implements SingleChoiceAction {

    private static final String[] OPTIONS = {"replace", "skip"};

    Replacement(Recommender recommender,Statement[] statements) {
        super(recommender, statements);
    }

	@Override
	public String requestInput() {
        this.ui.showInfo("Do you want to replace the item?");
        return this.ui.chooseFromOptions(Replacement.OPTIONS);
	}

    @Override
    public void actOnInput(String userInput) {
        switch (userInput) {
            case "replace": 
                Resource subject = this.statements[0].getSubject();
                Property predicate = this.statements[0].getPredicate();
                RDFNode object = this.statements[0].getObject();
                this.model.remove(this.statements[0]);
			    this.ui.showInfo("Removed one triple: "+subject+" "+predicate+" "+object+".");
                
                RDFNode newObject = this.statements[1].getObject();
                Statement newStatement = this.model.createStatement(subject, predicate, newObject);
                this.model.add(newStatement);
                this.ui.showInfo("Added one triple: "+subject+" "+predicate+" "+newObject+".");
                this.logger.logChange(this.statements[0], newStatement);;
                break;
            case "skip": 
                this.ui.showInfo("Skipped removal.");
                break;
            default:
                this.ui.showError("Not a valid Input. Please choose from one of the options in: "+Arrays.toString(Replacement.OPTIONS));
                break;
        }
    }

    @Override
    boolean isAutomatable() {
        return true;
    }

}
