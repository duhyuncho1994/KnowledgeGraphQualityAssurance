package de.kglab.kg_qa;

import java.util.Arrays;

import org.apache.jena.rdf.model.Statement;

class Remove extends SingleStatementAction implements SingleChoiceAction {

    private static final String[] OPTIONS = {"skip", "delete"};

    Remove(Recommender recommender,Statement statement) {
        super(recommender, statement);
    }

	@Override
	public String requestInput() {
        return this.ui.chooseFromOptions(Remove.OPTIONS);
	}

    @Override
    public void actOnInput(String userInput) {
        switch (userInput) {
            case "delete":
                this.model.remove(this.statement);
                this.ui.showInfo("Removed one triple: "+this.statement.getSubject()+" "+this.statement.getPredicate()+" "+this.statement.getObject()+".");
                this.logger.logRemoval(this.statement);
                break;
            case "skip": 
                this.ui.showInfo("Skipped removal.");
                break;
            default:
                this.ui.showError("Not a valid Input. Please choose from one of the options in: "+Arrays.toString(Remove.OPTIONS));
                break;
        }
    }

}
