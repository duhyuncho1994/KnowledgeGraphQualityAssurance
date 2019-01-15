package de.kglab.kg_qa;

import org.apache.jena.rdf.model.Model;

abstract class RecommendedAction {

    Model model;
    UserInterface ui;
    Logger logger;

    RecommendedAction(Recommender recommender) {
        this.model = recommender.getModel();
        this.ui = recommender.getUI();
        this.logger = recommender.getLogger();
    }

    boolean isAutomatable() {
        return false;
    }
}
