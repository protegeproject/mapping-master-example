package org.mm.example;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.mm.core.OWLAPIOntology;
import org.mm.core.OWLOntologySource;
import org.mm.core.TransformationRule;
import org.mm.core.settings.ReferenceSettings;
import org.mm.parser.ASTExpression;
import org.mm.parser.MappingMasterParser;
import org.mm.parser.ParseException;
import org.mm.parser.node.ExpressionNode;
import org.mm.parser.node.MMExpressionNode;
import org.mm.renderer.owlapi.OWLRenderer;
import org.mm.rendering.Rendering;
import org.mm.ss.SpreadSheetDataSource;
import org.mm.ss.SpreadsheetLocation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class MMExample
{
  public static void main(String[] args)
  {

    try {
      File owlFile = new File(MMExample.class.getClassLoader().getResource("ExampleOWLOntology.owl").getFile());
      File spreadsheetFile = new File(MMExample.class.getClassLoader().getResource("ExampleSpreadsheet.xlsx").getFile());

      // Create an OWL ontology using the OWLAPI
      OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = ontologyManager.loadOntologyFromOntologyDocument(owlFile);

      // Create a workbook using POI
      Workbook workbook = WorkbookFactory.create(spreadsheetFile);

      // Create a Mapping Master expression
      TransformationRule mmExpression = new TransformationRule("MySheet", "A", "A", "1",
        "3", "Creating car instances", "Individual: @A* Types: Car");

      // Create a Mapping Master parser for the expression
      MappingMasterParser parser = new MappingMasterParser(
        new ByteArrayInputStream(mmExpression.getRuleString().getBytes()), new ReferenceSettings(), -1);

      // Create a Mapping Master renderer with the context of an OWL ontology and a spreadsheet
      OWLOntologySource ontologySource = new OWLAPIOntology(ontology);
      SpreadSheetDataSource dataSource = new SpreadSheetDataSource(workbook);
      dataSource.setCurrentLocation(new SpreadsheetLocation("MySheet", 1, 1));
      OWLRenderer renderer = new OWLRenderer(ontologySource, dataSource);

      // Render the expression
      MMExpressionNode mmExpresionNode = new ExpressionNode((ASTExpression)parser.expression()).getMMExpressionNode();
      Optional<? extends Rendering> renderingResult = renderer.render(mmExpresionNode);

      // Display the rendering
      if (renderingResult.isPresent())
        System.out.println(renderingResult.toString());

    } catch (OWLOntologyCreationException | RuntimeException | ParseException | InvalidFormatException | IOException e) {
      System.err.println("Exception: " + e.getMessage());
      e.printStackTrace();
      System.exit(-1);
    }
  }
}
