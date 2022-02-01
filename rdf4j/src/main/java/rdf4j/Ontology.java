/**
 * 
 */
package rdf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 * @author Jr
 *
 */
public class Ontology {

	static Namespace ex = Values.namespace("ex", "http://example.org/");
		
	public static void main(String[] args) {
	
		buildOntology();
		
//		Repository rep = new SailRepository(new MemoryStore());
//		
//		try (RepositoryConnection conn = rep.getConnection()) {
//			
//			createIndividuals(conn);
//			
//			RepositoryResult<Statement> statements = conn.getStatements(null, null, null);
//			Model model = QueryResults.asModel(statements);
//			model.setNamespace(RDF.NS);
//			model.setNamespace(RDFS.NS);
//			model.setNamespace(FOAF.NS);
//			model.setNamespace(ex);
//				
//			Rio.write(model, System.out, RDFFormat.TURTLE);
//			
//			execQueryGetAuthors(conn);
//		
//		}
			
	}
	
	// création structure complète (RDFS)
	static void buildOntology() {
		
		try {
			java.net.URL documentUrl = new URL("https://raw.githubusercontent.com/IIVR/mas-rad-web-semantic/main/ontology/ontology.owl");
				
			RDFFormat inputFormat = Rio.getParserFormatForFileName(documentUrl.toString()).orElse(RDFFormat.RDFXML);
			
			RDFParser rdfParser = Rio.createParser(inputFormat);
			RDFWriter rdfWriter = Rio.createWriter(RDFFormat.TURTLE, new FileOutputStream("./output.ttl"));
			
			Model model = new LinkedHashModel();
			
			rdfParser.setRDFHandler(new StatementCollector(model));
				
			InputStream inputStream = documentUrl.openStream();
			rdfParser.parse(inputStream, documentUrl.toString());
			

			rdfWriter.startRDF();
			for(Statement st: model) {
				rdfWriter.handleStatement(st);
			}
			rdfWriter.endRDF();
			
			
			
		} catch (IOException e ) {

		}
			
		// load owl
//		conn.add(Values.iri(ex, "Book"), RDF.TYPE, RDFS.CLASS);
//		conn.add(Values.iri(ex, "Author"), RDF.TYPE, RDFS.CLASS);
//		conn.add(Values.iri(ex, "Author"), RDFS.SUBCLASSOF, FOAF.PERSON);
		
	}
	
	// création de tous les individus (RDF)
	static void createIndividuals(RepositoryConnection conn) {
		
		createIndividualsBook(conn, "Learning Java", "1449355730", Values.iri(ex, "Learning_Java"));
		
		createIndividualsAuthor(conn, "John", Values.iri(ex, "john"));
		createIndividualsAuthor(conn, "Jane", Values.iri(ex, "jane"));
	}
	
	// Chaque type d’individu (instance) doit être réalisé dans une méthode distincte
	static void createIndividualsBook(RepositoryConnection conn, String title, String isbn, IRI iri) {
		conn.add(iri, RDF.TYPE, Values.iri(ex, "Book"));
		conn.add(iri, Values.iri(ex, "title"), Values.literal(title));
		conn.add(iri, Values.iri(ex, "isbn"), Values.literal(isbn));

	}
	
	static void createIndividualsAuthor(RepositoryConnection conn, String name, IRI iri) {
		conn.add(iri, RDF.TYPE, Values.iri(ex, "Author"));
		conn.add(iri, FOAF.FIRST_NAME, Values.literal(name));
	}
	
	static void execQueryGetAuthors(RepositoryConnection conn) {
		
		String queryString = 
				"PREFIX ex: <http://example.org/> \n"+
				"PREFIX foaf: <" + FOAF.NAMESPACE + "> \n"+
				"SELECT ?s ?n \n"+
				"WHERE { \n"+
				"    ?s a ex:Author; \n"+
				"       foaf:firstName ?n ."+
				"}";
		
		TupleQuery query = conn.prepareTupleQuery(queryString);
		
		try (TupleQueryResult result = query.evaluate()) {
			System.out.println("\n-- execQueryGetAuthors --\n");
			for (BindingSet solution : result) {
				
				System.out.println("?s = " + solution.getValue("s"));
				System.out.println("?n = " + solution.getValue("n"));
				
			}
			
		}
			
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
