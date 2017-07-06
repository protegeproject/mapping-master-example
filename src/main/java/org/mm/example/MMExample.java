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
import org.mm.rendering.owlapi.OWLRendering;
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

import static org.mm.ss.SpreadSheetUtil.columnNumber2Name;

public class MMExample
{
  public static void main(String[] args)
  {
    try {
      File owlFile = new File(MMExample.class.getClassLoader().getResource("ExampleOWLOntology.owl").getFile());
      File spreadsheetFile = new File(
        MMExample.class.getClassLoader().getResource("ExampleSpreadsheet.xlsx").getFile());

      // Create an OWL ontology using the OWLAPI
      OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = ontologyManager.loadOntologyFromOntologyDocument(owlFile);

      // Create a workbook using POI
      Workbook workbook = WorkbookFactory.create(spreadsheetFile);

      // Create a Mapping Master expression
      final String sheetName = "MySheet";
      final Integer startColumnNumber = 1, finishColumnNumber = 1, startRowNumber = 1, finishRowNumber = 3;
      TransformationRule mmExpression = new TransformationRule(sheetName, columnNumber2Name(startColumnNumber),
        columnNumber2Name(finishColumnNumber), startRowNumber.toString(), finishRowNumber.toString(),
        "Creating car instances", "Individual: @A* Types: Car");

      // Create a Mapping Master parser for the expression, parse it, and return an AST node representing the expression
      MappingMasterParser parser = new MappingMasterParser(
        new ByteArrayInputStream(mmExpression.getRuleString().getBytes()), new ReferenceSettings(), -1);
      MMExpressionNode mmExpressionNode = new ExpressionNode((ASTExpression)parser.expression())
        .getMMExpressionNode();

      // Create an ontology source and a data source (which wrap an OWLAPI OWL ontology and a POI workbook, respectively)
      OWLOntologySource ontologySource = new OWLAPIOntology(ontology);
      SpreadSheetDataSource dataSource = new SpreadSheetDataSource(workbook);

      // Create an OWL renderer. An OWL renderer renders a set of OWLAPI-based OWL axioms.
      OWLRenderer owlRenderer = new OWLRenderer(ontologySource, dataSource);

      // Loop through the cells specified by the transformation rule
      for (int columnNumber = startColumnNumber; columnNumber <= finishColumnNumber; columnNumber++) {
        for (int rowNumber = startRowNumber; rowNumber <= finishRowNumber; rowNumber++) {
          // A Mapping Master expression is rendered in the context of a location in a spreadsheet
          dataSource.setCurrentLocation(new SpreadsheetLocation(sheetName, columnNumber, rowNumber));

          // Render the Mapping Master expression as an OWL rendering (which will contain a set of OWLAPI-based OWL axioms)
          Optional<OWLRendering> owlRendering = owlRenderer.render(mmExpressionNode);

          // Display the OWL axioms rendered by the Mapping Master expression
          if (owlRendering.isPresent())
            System.out.println("Rendered OWL axioms: " + owlRendering.get().getOWLAxioms());
        }
      }
    } catch (OWLOntologyCreationException | RuntimeException | ParseException | InvalidFormatException | IOException e) {
      System.err.println("Exception: " + e.getMessage());
      e.printStackTrace();
      System.exit(-1);
    }
  }
}
