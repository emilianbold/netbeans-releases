package org.netbeans.modules.xml.xdm.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.XDMModel;
import javax.swing.text.Document;

/**
 *
 * @author Owner
 */
public class DiffFinderTest extends TestCase {
	
	public DiffFinderTest(String testName) {
		super(testName);
	}

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(DiffFinderTest.class);		
		return suite;
	}

	public void testFindDiff1() throws Exception {
		System.out.println("testFindDiff1");

		String FILE1 = "diff/schema3.xsd";
		Document d1 = Util.getResourceAsDocument(FILE1);
		XDMModel m1 = new XDMModel(d1);
		m1.sync();

		String FILE2 = "diff/schema4.xsd";
		Document d2 = Util.getResourceAsDocument(FILE2);
		XDMModel m2 = new XDMModel(d2);
		m2.sync();	
		
		//establish DOM element identities
		ElementIdentity eID = new ElementIdentity();
		String[][] selfID = null;		
		String[][] nameRefID = { { "name" }, { "ref" } };
		String[][] refID = { { "ref" } };
		String[][] nameID = { { "name" } };
		eID.setDefaultID( selfID );
		eID.setIDFor( "schema"+"{"+SCHEMA_URI+"}", selfID );		
		eID.setIDFor( "complexType"+"{"+SCHEMA_URI+"}", nameID );
		eID.setIDFor( "simpleType"+"{"+SCHEMA_URI+"}", nameID );
		eID.setIDFor( "element"+"{"+SCHEMA_URI+"}", nameRefID );
		eID.setIDFor( "attribute"+"{"+SCHEMA_URI+"}", nameRefID );
		eID.setIDFor( "attributeGroup"+"{"+SCHEMA_URI+"}", nameRefID );
		
        long startTime=System.currentTimeMillis();	
		DiffFinder dv = new DiffFinder(eID);
		List<DiffEvent> deList = dv.findDiff(m1.getDocument(), m2.getDocument());
		long endTime=System.currentTimeMillis();
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
				"Total time to compare schema1.xsd to schema2. "+(endTime-startTime)+"ms");
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");	
		
		assertEquals( 8 , deList.size() );
		
		DiffFinder.printDeList( deList );		
	}

	public void testFindDiff2() throws Exception {
		System.out.println("testFindDiff2");

		String FILE1 = "diff/schema3.xsd";
		Document d1 = Util.getResourceAsDocument(FILE1);
		XDMModel m1 = new XDMModel(d1);
		m1.sync();

		String FILE2 = "diff/schema5.xsd";
		Document d2 = Util.getResourceAsDocument(FILE2);
		XDMModel m2 = new XDMModel(d2);
		m2.sync();	
		
		//establish DOM element identities
		ElementIdentity eID = new ElementIdentity();
		String[][] selfID = null;		
		String[][] nameRefID = { { "name" }, { "ref" } };
		String[][] refID = { { "ref" } };
		String[][] nameID = { { "name" } };
		eID.setDefaultID( selfID );
		eID.setIDFor( "schema"+"{"+SCHEMA_URI+"}", selfID );		
		eID.setIDFor( "complexType"+"{"+SCHEMA_URI+"}", nameID );
		eID.setIDFor( "simpleType"+"{"+SCHEMA_URI+"}", nameID );
		eID.setIDFor( "element"+"{"+SCHEMA_URI+"}", nameRefID );
		eID.setIDFor( "attribute"+"{"+SCHEMA_URI+"}", nameRefID );
		eID.setIDFor( "attributeGroup"+"{"+SCHEMA_URI+"}", nameRefID );
		
        long startTime=System.currentTimeMillis();	
		DiffFinder dv = new DiffFinder(eID);
		List<DiffEvent> deList = dv.findDiff(m1.getDocument(), m2.getDocument());
		long endTime=System.currentTimeMillis();
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
				"Total time to compare schema1.xsd to schema3. "+(endTime-startTime)+"ms");
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");	
		
		assertEquals( 12, deList.size());
		DiffFinder.printDeList( deList );
	}

	public void testFindDiff3() throws Exception {
		System.out.println("testFindDiff3");

		String FILE1 = "diff/PurchaseOrder.xsd";
		Document d1 = Util.getResourceAsDocument(FILE1);
		XDMModel m1 = new XDMModel(d1);
		m1.sync();

		String FILE2 = "diff/PurchaseOrderSyncTest.xsd";
		Document d2 = Util.getResourceAsDocument(FILE2);
		XDMModel m2 = new XDMModel(d2);
		m2.sync();	
		
		//establish DOM element identities
		ElementIdentity eID = new ElementIdentity();
		String[][] selfID = null;		
		String[][] nameRefID = { { "name" }, { "ref" } };
		String[][] refID = { { "ref" } };
		String[][] nameID = { { "name" } };
		eID.setDefaultID( selfID );
		eID.setIDFor( "schema"+"{"+SCHEMA_URI+"}", selfID );		
		eID.setIDFor( "complexType"+"{"+SCHEMA_URI+"}", nameID );
		eID.setIDFor( "simpleType"+"{"+SCHEMA_URI+"}", nameID );
		eID.setIDFor( "element"+"{"+SCHEMA_URI+"}", nameRefID );
		eID.setIDFor( "attribute"+"{"+SCHEMA_URI+"}", nameRefID );
		eID.setIDFor( "attributeGroup"+"{"+SCHEMA_URI+"}", nameRefID );
		
        long startTime=System.currentTimeMillis();	
		DiffFinder dv = new DiffFinder( eID );
		List<DiffEvent> deList = dv.findDiff(m1.getDocument(), m2.getDocument());
		long endTime=System.currentTimeMillis();
		System.out.println("\n\n::::::::::::::::::::::::::::::::::::::::::::\n" +
				"Total time to compare PurchaseOrder. "+(endTime-startTime)+"ms");
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");	
		DiffFinder.printDeList( deList );
		assertEquals( 8, deList.size() );	
	}

	public void testFindDiffPerf() throws Exception {
		System.out.println("testFindDiffPerf");

		String FILE1 = "perf/J1_TravelItinerary.xsd";
		Document d1 = Util.getResourceAsDocument(FILE1);
		XDMModel m1 = new XDMModel(d1);
		m1.sync();

		String FILE2 = "perf/J1_TravelItinerary.xsd";
		Document d2 = Util.getResourceAsDocument(FILE2);
		XDMModel m2 = new XDMModel(d2);
		m2.sync();	
		
		//establish DOM element identities
		ElementIdentity eID = new ElementIdentity();
		String[][] selfID = null;		
		String[][] nameRefID = { { "name" }, { "ref" } };
		String[][] refID = { { "ref" } };
		String[][] nameID = { { "name" } };
		eID.setDefaultID( selfID );
		eID.setIDFor( "schema"+"{"+SCHEMA_URI+"}", selfID );		
		eID.setIDFor( "complexType"+"{"+SCHEMA_URI+"}", nameID );
		eID.setIDFor( "simpleType"+"{"+SCHEMA_URI+"}", nameID );
		eID.setIDFor( "element"+"{"+SCHEMA_URI+"}", nameRefID );
		eID.setIDFor( "attribute"+"{"+SCHEMA_URI+"}", nameRefID );
		eID.setIDFor( "attributeGroup"+"{"+SCHEMA_URI+"}", nameRefID );
		
        long startTime=System.currentTimeMillis();	
		DiffFinder dv = new DiffFinder( eID );
		List<DiffEvent> deList = dv.findDiff(m1.getDocument(), m2.getDocument());
		long endTime=System.currentTimeMillis();
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
				"Total time to compare TravelItinerary: "+(endTime-startTime)+"ms");
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");	
		
		DiffFinder.printDeList( deList );
		assertEquals( 0, deList.size() );
	}	

	public void testFindDiffPerf2() throws Exception {
		System.out.println("testFindDiffPerf");

		String FILE1 = "diff/TravelItinerary1.xsd";
		Document d1 = Util.getResourceAsDocument(FILE1);
		XDMModel m1 = new XDMModel(d1);
		m1.sync();

		String FILE2 = "diff/TravelItinerary2.xsd";
		Document d2 = Util.getResourceAsDocument(FILE2);
		XDMModel m2 = new XDMModel(d2);
		m2.sync();	
		
		//establish DOM element identities
		ElementIdentity eID = new ElementIdentity();
		String[][] selfID = null;		
		String[][] nameRefID = { { "name" }, { "ref" } };
		String[][] refID = { { "ref" } };
		String[][] nameID = { { "name" } };
		eID.setDefaultID( selfID );
		eID.setIDFor( "schema"+"{"+SCHEMA_URI+"}", selfID );		
		eID.setIDFor( "complexType"+"{"+SCHEMA_URI+"}", nameID );
		eID.setIDFor( "simpleType"+"{"+SCHEMA_URI+"}", nameID );
		eID.setIDFor( "element"+"{"+SCHEMA_URI+"}", nameRefID );
		eID.setIDFor( "attribute"+"{"+SCHEMA_URI+"}", nameRefID );
		eID.setIDFor( "attributeGroup"+"{"+SCHEMA_URI+"}", nameRefID );
		
        long startTime=System.currentTimeMillis();	
		DiffFinder dv = new DiffFinder( eID );
		List<DiffEvent> deList = dv.findDiff(m1.getDocument(), m2.getDocument());
		long endTime=System.currentTimeMillis();
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
				"Total time to compare TravelItinerary: "+(endTime-startTime)+"ms");
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");	
		
		assertEquals( 4, deList.size() );
		
		DiffFinder.printDeList( deList );
	}	
	
	String SCHEMA_URI = "http://www.w3.org/2001/XMLSchema";	
}
