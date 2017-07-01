package org.mm.example;

import org.mm.core.TransformationRule;
import org.mm.core.settings.ReferenceSettings;
import org.mm.parser.ASTExpression;
import org.mm.parser.MappingMasterParser;
import org.mm.parser.ParseException;
import org.mm.parser.SimpleNode;
import org.mm.parser.node.ExpressionNode;
import org.mm.parser.node.MMExpressionNode;
import org.mm.renderer.Renderer;
import org.mm.rendering.Rendering;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Optional;

public class MMExample
{
  public static void main(String[] args)
  {
    if (args.length > 1)
      Usage();

    Optional<String> owlFilename = args.length == 0 ? Optional.empty() : Optional.of(args[0]);
    Optional<File> owlFile = (owlFilename != null && owlFilename.isPresent()) ?
      Optional.of(new File(owlFilename.get())) :
      Optional.empty();

    try {
      // Create an OWL ontology using the OWLAPI
      OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = owlFile.isPresent() ?
        ontologyManager.loadOntologyFromOntologyDocument(owlFile.get()) :
        ontologyManager.createOntology();

      //  public OWLRenderer(OWLOntologySource ontologySource, SpreadSheetDataSource dataSource) {

      Renderer renderer = null;
      TransformationRule rule = null;
      String ruleString = rule.getRuleString();
      MappingMasterParser parser = new MappingMasterParser(new ByteArrayInputStream(ruleString.getBytes()), new ReferenceSettings(), -1);
      SimpleNode simpleNode = parser.expression();
      MMExpressionNode ruleNode = new ExpressionNode((ASTExpression) simpleNode).getMMExpressionNode();
      Optional<? extends Rendering> renderingResult = renderer.render(ruleNode);
      if (renderingResult.isPresent()) {
        renderingResult.toString();
      }


    } catch (OWLOntologyCreationException e) {
      System.err.println("Error creating OWL ontology: " + e.getMessage());
      System.exit(-1);
    } catch (RuntimeException e) {
      System.err.println("Error starting application: " + e.getMessage());
      System.exit(-1);
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  private static void Usage()
  {
    System.err.println("Usage: " + MMExample.class.getName() + " [ <owlFileName> ]");
    System.exit(1);
  }
}
