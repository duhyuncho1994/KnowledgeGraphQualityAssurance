package de.kglab.kg_qa;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.rdf.model.Statement;

class ChooseRemoval extends MultipleStatementAction implements MultipleChoiceAction {

    private String[] options;
    private int numberToKeep;

    ChooseRemoval(Recommender recommender, Statement[] statements, int numberToKeep) {
        super(recommender, statements);
        this.numberToKeep = numberToKeep;
        options = (String[]) ArrayUtils.addAll(objects, "skip");
    }

	@Override
	public String[] requestInput() {
        this.ui.showInfo("Which items do you want to delete?");
        return this.ui.chooseFromMultiOptions(options, statements.length-numberToKeep);
	}

    @Override
    public void actOnInput(String[] userInput) {
        for (String inputElement : userInput) {
            if (Arrays.asList(objects).contains(inputElement)) {
                for (int i = 0; i < objects.length;i++) {
                    if (objects[i].equals(inputElement)) {
                        this.model.remove(statements[i]);
                        this.ui.showInfo("Removed one triple: "+this.statements[i].getSubject()+" "+this.statements[i].getPredicate()+" "+this.objects[i]+".");
                        this.logger.logRemoval(statements[i]);
                    }
                }
            }
            else if (inputElement.equals("skip")) {
                this.ui.showInfo("Skipped removal.");
            }
            else {
                this.ui.showError("Not a valid Input. Please choose from one of the options in: "+Arrays.toString(options));
            }
        }
    }

}
