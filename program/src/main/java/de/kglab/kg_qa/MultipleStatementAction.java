package de.kglab.kg_qa;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;

abstract class MultipleStatementAction extends RecommendedAction {

    Statement[] statements;
    String[] objects;

    MultipleStatementAction(Recommender recommender, Statement[] statements) {
        super(recommender);
        this.statements = statements;

        List<String> objectsList = new ArrayList<>();
        for (Statement stm : statements) {
            RDFNode object = stm.getObject();
            objectsList.add(object.toString());
        }
        String[] objects = new String[objectsList.size()];
        objects = objectsList.toArray(objects);
        this.objects = objects;
    }

}
