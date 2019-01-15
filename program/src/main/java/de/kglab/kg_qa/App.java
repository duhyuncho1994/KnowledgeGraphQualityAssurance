package de.kglab.kg_qa;

import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.validation.ValidationUtil;

/**
 * Hello world!
 *
 */
public class App {

    private Model model;
    private Model constraintModel;
    private UserInterface ui;
    private ValidationReporter reporter;
    private Recommender recommender;
    private Logger logger;
    private static final String ASSETS_FOLDER = "./assets/";
    private static final String GRAPH_FILE_NAME = ASSETS_FOLDER + "drugbank_whole_dataset.nt";
    private static final String CONSTRAINT_FILE_NAME = ASSETS_FOLDER+ "A_drugbank_part-constraints.ttl";
    private static final String PREFIX_FILE_NAME = ASSETS_FOLDER+ "Prefixes.ttl";

    private App() {
        loadGraphFile();
        loadConstraintFile();
        loadPrefixes();
        createUI();
        createValReporter();
        createLogger();
        createRecommender();
    }
    
    
    private void createLogger() {
        this.logger = new PlainTextLogger();
	}


	public static void main(String[] args) {
        App app = new App();
        app.mainMethod();
    }
    
    private void mainMethod() {
        reporter.parseReport();
        reporter.reportStatistics();
        recommender.recommendActions();
        recommender.actOnRecommendations();
        
        Resource report = ValidationUtil.validateModel(this.model, this.constraintModel, true);
        System.out.println(ModelPrinter.get().print(report.getModel()));
        if (logger instanceof PlainTextLogger) {
            ((PlainTextLogger) logger).closeLogFile();
        }
    }
        
    private void loadGraphFile() {
        loadFile(true, "N3");
    }
    
    private void loadConstraintFile() {
        loadFile(false, "TTL");
    }
    
    private void loadFile(boolean dataGraph, String lang) {
        String fileName = dataGraph?GRAPH_FILE_NAME:CONSTRAINT_FILE_NAME;
        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(fileName);
        if (in == null) {
            throw new IllegalArgumentException("File: " + fileName + " not found");
        }
        model.read(in, null, lang);
        if (dataGraph) {
            this.model = model;
        }
        else {
            this.constraintModel = model;
        }
    }
    
    private void createValReporter() {
        Resource report = ValidationUtil.validateModel(this.model, this.constraintModel, true);
        this.reporter = new ValidationReporter(report, this.model, this.constraintModel, this.ui);
        System.out.println(ModelPrinter.get().print(report.getModel()));
    }

    private void createRecommender() {
        this.recommender = new Recommender(this.reporter, this.ui, this.logger);
    }

    private void createUI() {
        this.ui = new CLInterface();
    }

    private void loadPrefixes() {
        Model prefixModel = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(PREFIX_FILE_NAME);
        if (in == null) {
            throw new IllegalArgumentException("File: " + PREFIX_FILE_NAME + " not found");
        }
        prefixModel.read(in, null, "TTL");
        this.model.setNsPrefixes(prefixModel);
    }
}
