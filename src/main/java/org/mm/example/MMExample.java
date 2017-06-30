package org.mm.example;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

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


    } catch (OWLOntologyCreationException e) {
      System.err.println("Error creating OWL ontology: " + e.getMessage());
      System.exit(-1);
    } catch (RuntimeException e) {
      System.err.println("Error starting application: " + e.getMessage());
      System.exit(-1);
    }
  }

  private static void Usage()
  {
    System.err.println("Usage: " + MMExample.class.getName() + " [ <owlFileName> ]");
    System.exit(1);
  }
}
