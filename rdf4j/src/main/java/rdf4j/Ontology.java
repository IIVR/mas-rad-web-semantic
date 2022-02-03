/**
 * 
 */
package rdf4j;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
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

	static Namespace ex = Values.namespace("ex", "http://www.semanticweb.org/vv/ontologies/2022/0/bookstore#");
		
	public static void main(String[] args) {
	
		
		Repository rep = new SailRepository(new MemoryStore());
		
		try (RepositoryConnection conn = rep.getConnection()) {
			
			buildOntology(conn);
			createIndividuals(conn);
			
			displayRepository(conn);
							
			execQueryGetDevelopersOrderBy(conn);
			execQueryGetBooksStartBy(conn);
			execQueryGetDevelopersOptional(conn);
			execQueryGetDevelopersUnion(conn);
			execQueryGetBookByTitle(conn);
			execQueryGetBookByCategory(conn);
		
		}
			
	}
	
	static void displayRepository(RepositoryConnection conn) {
		RepositoryResult<Statement> statements = conn.getStatements(null, null, null);
		Model model = QueryResults.asModel(statements);
		Rio.write(model, System.out, RDFFormat.TURTLE);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	//
	// Création structure complète (RDFS) (Récupération fichier owl)
	//
	///////////////////////////////////////////////////////////////////////////////////////

	static void buildOntology(RepositoryConnection conn) {
			
		try {
			
			java.net.URL documentUrl = new URL("https://raw.githubusercontent.com/IIVR/mas-rad-web-semantic/main/ontology/ontology.owl");
			
			RDFFormat inputFormat = Rio.getParserFormatForFileName(documentUrl.toString()).orElse(RDFFormat.RDFXML);
			
			RDFParser rdfParser = Rio.createParser(inputFormat);
			RDFWriter rdfWriter = Rio.createWriter(RDFFormat.RDFXML, new FileOutputStream("./output.rdf"));
			
			Model model = new LinkedHashModel();
			rdfParser.setRDFHandler(new StatementCollector(model));
				
			InputStream inputStream = documentUrl.openStream();
			rdfParser.parse(inputStream, documentUrl.toString());
			
			rdfWriter.startRDF();
			for(Statement st: model) {
				rdfWriter.handleStatement(st);
				// add statement to MemoryStore
				conn.add(st);
			}
			rdfWriter.endRDF();
				
		} catch (IOException e ) {

		}
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	//
	// Création de tous les individus (RDF)
	//
	///////////////////////////////////////////////////////////////////////////////////////
	
	static void createIndividuals(RepositoryConnection conn) {
			
		createIndividualsCountry(conn,"Canada",Values.iri(ex, "country3"));
		createIndividualsPrice(conn,(float)42,Values.iri(ex, "price2"));
		createIndividualsCurrency(conn, "US","$", Values.iri(ex, "currency2"));
		createIndividualsWindows(conn,"windows",Values.iri(ex, "windows1"));
		createIndividualsPublisher(conn,"Goodwill of North Georgia",Values.iri(ex, "publisher2"));
		createIndividualsDeveloper(conn, "Sergii","Baidachnyi", "principal author",Values.iri(ex, "developer2"),Values.iri(ex, "country3"),Values.iri(ex, "windows1"));
		createIndividualsDeveloper(conn, "John","Doe", "preface author",Values.iri(ex, "developer3"), null,null);
		createIndividualsBook(conn, "Developing Windows 10 Applications with C#", "1522894918", Values.iri(ex, "book2"), Values.iri(ex, "developer2"), Values.iri(ex, "publisher2"), Values.iri(ex, "windows1"));
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	//
	// Chaque type d’individu (instance) doit être réalisé dans une méthode distincte
	//
	///////////////////////////////////////////////////////////////////////////////////////

	static void createIndividualsBook(RepositoryConnection conn, String title, String isbn, IRI iri, IRI hasAuthor, IRI hasPublisher, IRI belongsTo) {
		
		conn.add(iri, RDF.TYPE, Values.iri(ex, "Book")); 
		conn.add(iri, Values.iri(ex, "title"), Values.literal(title));
		conn.add(iri, Values.iri(ex, "isbn"), Values.literal(isbn));
		if(hasAuthor != null) { conn.add(iri, Values.iri(ex, "hasAuthor"),hasAuthor); }
		if(hasPublisher != null) { conn.add(iri, Values.iri(ex, "hasPublisher"),hasPublisher); }
		if(belongsTo != null) { conn.add(iri, Values.iri(ex, "belongsTo"),belongsTo); }
	}
	
	static void createIndividualsDeveloper(RepositoryConnection conn, String firstname, String lastname, String role, IRI iri, IRI liveIn, IRI publishFor) {
		
		conn.add(iri, RDF.TYPE, Values.iri(ex, "Developer"));
		conn.add(iri, Values.iri(ex, "role"), Values.literal(role));
		conn.add(iri, FOAF.FIRST_NAME, Values.literal(firstname));
		conn.add(iri, FOAF.LAST_NAME, Values.literal(lastname));
		if(liveIn != null) { conn.add(iri, Values.iri(ex, "liveIn"),liveIn); }
		if(publishFor != null) { conn.add(iri, Values.iri(ex, "publishFor"),publishFor); }
	
	}
	
	static void createIndividualsCountry(RepositoryConnection conn, String name, IRI iri) {
		
		conn.add(iri, RDF.TYPE, Values.iri(ex, "Country"));
		conn.add(iri, Values.iri(ex, "name"), Values.literal(name));
	}
	
	static void createIndividualsPublisher(RepositoryConnection conn, String name, IRI iri) {
		
		conn.add(iri, RDF.TYPE, Values.iri(ex, "Publisher"));
		conn.add(iri, Values.iri(ex, "name"), Values.literal(name));
	}
	
	static void createIndividualsPrice(RepositoryConnection conn, Float amount, IRI iri) {
		
		conn.add(iri, RDF.TYPE, Values.iri(ex, "Price"));
		conn.add(iri, Values.iri(ex, "amount"), Values.literal(amount));
	}
	
	static void createIndividualsCurrency(RepositoryConnection conn, String code, String symbol, IRI iri) {
		
		conn.add(iri, RDF.TYPE, Values.iri(ex, "Currency"));
		conn.add(iri, Values.iri(ex, "code"), Values.literal(code));
		conn.add(iri, Values.iri(ex, "symbol"), Values.literal(symbol));
	}
	
	static void createIndividualsWindows(RepositoryConnection conn, String name, IRI iri) {
		
		conn.add(iri, RDF.TYPE, Values.iri(ex, "Windows"));
		conn.add(iri, Values.iri(ex, "name"), Values.literal(name));
	}
	
    ///////////////////////////////////////////////////////////////////////////////////////
    //
    // SPARQL Query
    //
    ///////////////////////////////////////////////////////////////////////////////////////
	
	static void Query(RepositoryConnection conn,String queryString) {
		
		TupleQuery query = conn.prepareTupleQuery(queryString);
		
		try (TupleQueryResult result = query.evaluate()) {
			
			for (BindingSet solution : result) {
				System.out.println(solution);
			}	
		}
	}
	
	static void execQueryGetDevelopersOrderBy(RepositoryConnection conn) {

		String queryString = 	
				
				"PREFIX ex: <http://www.semanticweb.org/vv/ontologies/2022/0/bookstore#> \n"+
				"PREFIX foaf: <" + FOAF.NAMESPACE + "> \n"+
				"SELECT ?n \n"+
				"WHERE { \n"+
				"    ?s a ex:Developer; \n"+
				"       foaf:firstName ?n ."+
				"}ORDER BY ASC (?n)";
		
		System.out.println("\n-- exec Query Get Developers --\n");
		Query(conn,queryString);
			
	}
	
	static void execQueryGetBooksStartBy(RepositoryConnection conn) {
		
		String queryString = 
				
				"PREFIX ex: <http://www.semanticweb.org/vv/ontologies/2022/0/bookstore#> \n"+
				"SELECT ?n \n"+
				"WHERE { \n"+
				"    ?s a ex:Book; \n"+
				"       ex:title ?n ."+
				"      FILTER regex(?n, '^Linux')"+
				"}";
		
		System.out.println("\n-- exec Query Get Books --\n");
		Query(conn,queryString);
			
	}
	
	static void execQueryGetDevelopersOptional(RepositoryConnection conn) {

		String queryString = 
				
				"PREFIX ex: <http://www.semanticweb.org/vv/ontologies/2022/0/bookstore#> \n"+
				"PREFIX foaf: <" + FOAF.NAMESPACE + "> \n"+
				"SELECT ?n ?country \n"+
				"WHERE { \n"+
				"    ?s a ex:Developer; \n"+
				"       foaf:firstName ?n ."+
				"       OPTIONAL{?s ex:liveIn ?country}"+
				"}";
		
		System.out.println("\n-- exec Query Get Developers Optional --\n");
		Query(conn,queryString);
			
	}
	
	static void execQueryGetDevelopersUnion(RepositoryConnection conn) {

		String queryString = 
				
				"PREFIX ex: <http://www.semanticweb.org/vv/ontologies/2022/0/bookstore#> \n"+
				"PREFIX foaf: <" + FOAF.NAMESPACE + "> \n"+
				"SELECT ?n \n"+
				"WHERE { \n"+
				"    ?s a ex:Developer; \n"+
				"       foaf:firstName ?n ."+
				"       {?s ex:publishFor ex:windows1}"+
				"       UNION"+
				"       {?s ex:publishFor ex:development1}"+
				"}";
		
		System.out.println("\n-- exec Query Get Developers Union --\n");
		Query(conn,queryString);
			
	}
	
	static void execQueryGetBookByTitle(RepositoryConnection conn) {

		String queryString = 
				
				"PREFIX ex: <http://www.semanticweb.org/vv/ontologies/2022/0/bookstore#> \n"+
				"PREFIX foaf: <" + FOAF.NAMESPACE + "> \n"+
				"SELECT ?s \n"+
				"WHERE { \n"+
				"    ?s a ex:Book; \n"+
				"      ex:title 'Linux Kernel Development'."+
				"}";
		
		System.out.println("\n-- exec Query Get Book By Title --\n");
		Query(conn,queryString);
			
	}

	static void execQueryGetBookByCategory(RepositoryConnection conn) {

		String queryString = 
				
				"PREFIX ex: <http://www.semanticweb.org/vv/ontologies/2022/0/bookstore#> \n"+
				"PREFIX foaf: <" + FOAF.NAMESPACE + "> \n"+
				"SELECT ?s \n"+
				"WHERE { \n"+
				"    ?s a ex:Book; \n"+
				"      ex:belongsTo ex:windows1."+
				"}";
		
		System.out.println("\n-- exec Query Get Book By Category --\n");
		Query(conn,queryString);
			
	}
	
}
