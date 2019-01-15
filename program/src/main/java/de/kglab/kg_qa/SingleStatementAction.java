package de.kglab.kg_qa;

import org.apache.jena.rdf.model.Statement;

abstract class SingleStatementAction extends RecommendedAction {

    Statement statement;

    SingleStatementAction(Recommender recommender, Statement statement) {
        super(recommender);
        this.statement = statement;
    }

}
