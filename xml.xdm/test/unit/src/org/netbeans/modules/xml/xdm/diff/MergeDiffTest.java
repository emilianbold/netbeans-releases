/*
 * MergeDiffTest.java
 * JUnit based test
 *
 * Created on February 2, 2006, 4:15 PM
 */

package org.netbeans.modules.xml.xdm.diff;

import java.util.ArrayList;
import java.util.Collections;
import junit.framework.*;
import java.util.List;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.XDMModel;
import javax.swing.text.Document;

/**
 *
 * @author Owner
 */
public class MergeDiffTest extends TestCase {
	
	public MergeDiffTest(String testName) {
		super(testName);
	}

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(MergeDiffTest.class);
		
		return suite;
	}

	public void testMerge1() throws Exception {
		System.out.println("testMerge1");
		
		String FILE1 = "diff/schema3.xsd";
		Document originalDocument = Util.getResourceAsDocument(FILE1);
		XDMModel originalModel = new XDMModel(originalDocument);
		originalModel.sync();

		String FILE2 = "diff/schema5.xsd";
		Document d2 = Util.getResourceAsDocument(FILE2);
		XDMModel m2 = new XDMModel(d2);
		m2.sync();
		
        long startTime=System.currentTimeMillis();
		
		XDMTreeDiff treeDiff = new XDMTreeDiff();
		treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
		
		long endTime=System.currentTimeMillis();
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
				"Total time to merge schema1.xsd to schema3.xsd: "+(endTime-startTime)+"ms");
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");		
		
		originalModel.flush();		

		System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
		assertEquals("original document should be equivalent to merged document",
			d2.getText(0,d2.getLength()), originalDocument.getText(0,originalDocument.getLength()));		
	}
	
	public void testMerge2() throws Exception {
		System.out.println("testMerge2");
		
		String FILE1 = "diff/PurchaseOrder.xsd";
		Document originalDocument = Util.getResourceAsDocument(FILE1);
		XDMModel originalModel = new XDMModel(originalDocument);
		originalModel.sync();

		String FILE2 = "diff/PurchaseOrderSyncTest.xsd";
		Document d2 = Util.getResourceAsDocument(FILE2);
		XDMModel m2 = new XDMModel(d2);
		m2.sync();
		
        long startTime=System.currentTimeMillis();
		
		XDMTreeDiff treeDiff = new XDMTreeDiff();
		treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
		
		long endTime=System.currentTimeMillis();
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
				"Total time to merge PurchaseOrder.xsd: "+(endTime-startTime)+"ms");
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");		
		
		originalModel.flush();		

		System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
		assertEquals("original document should be equivalent to merged document",
			d2.getText(0,d2.getLength()), 
			originalDocument.getText(0,originalDocument.getLength()));		
	}
	
	public void testMerge3() throws Exception {
		System.out.println("testMerge3");
		
		String FILE1 = "diff/TravelItinerary1.xsd";
		Document originalDocument = Util.getResourceAsDocument(FILE1);
		XDMModel originalModel = new XDMModel(originalDocument);
		originalModel.sync();

		String FILE2 = "diff/TravelItinerary2.xsd";
		Document d2 = Util.getResourceAsDocument(FILE2);
		XDMModel m2 = new XDMModel(d2);
		m2.sync();	
		
        long startTime=System.currentTimeMillis();
		
		XDMTreeDiff treeDiff = new XDMTreeDiff();
		treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
		
		long endTime=System.currentTimeMillis();
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
				"Total time to merge TravelItinerary: "+(endTime-startTime)+"ms");
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");		
		
		originalModel.flush();		

		//System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
		//System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
		assertEquals("original document should be equivalent to merged document",
			d2.getText(0,d2.getLength()), 
			originalDocument.getText(0,originalDocument.getLength()));		
	}
	
	public void testMerge4() throws Exception {
		System.out.println("testMerge4");
		
		String FILE1 = "diff/testaddshape.xml";
		Document originalDocument = Util.getResourceAsDocument(FILE1);
		XDMModel originalModel = new XDMModel(originalDocument);
		originalModel.sync();

		String FILE2 = "diff/addshape.xml";
		Document d2 = Util.getResourceAsDocument(FILE2);
		XDMModel m2 = new XDMModel(d2);
		m2.sync();	
		
        long startTime=System.currentTimeMillis();
		
		XDMTreeDiff treeDiff = new XDMTreeDiff();
		treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
		
		long endTime=System.currentTimeMillis();
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
				"Total time to merge addshape.xml: "+(endTime-startTime)+"ms");
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");		
		
		originalModel.flush();		

		System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
		assertEquals("original document should be equivalent to merged document",
			d2.getText(0,d2.getLength()), 
			originalDocument.getText(0,originalDocument.getLength()));		
	}	

	public void testMerge5() throws Exception {
		System.out.println("testMerge5");
		
		String FILE1 = "diff/testbase.xml";
		Document originalDocument = Util.getResourceAsDocument(FILE1);
		XDMModel originalModel = new XDMModel(originalDocument);
		originalModel.sync();

		String FILE2 = "diff/commentTextChanged.xml";
		Document d2 = Util.getResourceAsDocument(FILE2);
		XDMModel m2 = new XDMModel(d2);
		m2.sync();	
		
        long startTime=System.currentTimeMillis();
		
		XDMTreeDiff treeDiff = new XDMTreeDiff();
		treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
		
		long endTime=System.currentTimeMillis();
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
				"Total time to merge textchange.xml: "+(endTime-startTime)+"ms");
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");		
		
		originalModel.flush();		

		System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
		assertEquals("original document should be equivalent to merged document",
			d2.getText(0,d2.getLength()), 
			originalDocument.getText(0,originalDocument.getLength()));		
	}
	
	public void testMerge6() throws Exception {
		System.out.println("testMerge6");
		
		String FILE1 = "diff/TestOperations.wsdl";
		Document originalDocument = Util.getResourceAsDocument(FILE1);
		XDMModel originalModel = new XDMModel(originalDocument);
		originalModel.sync();

		String FILE2 = "diff/TestOperations_after.wsdl";
		Document d2 = Util.getResourceAsDocument(FILE2);
		XDMModel m2 = new XDMModel(d2);
		m2.sync();	
		
        long startTime=System.currentTimeMillis();
		
		XDMTreeDiff treeDiff = new XDMTreeDiff();
		treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
		
		long endTime=System.currentTimeMillis();
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
				"Total time to merge textchange.xml: "+(endTime-startTime)+"ms");
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");		
		
		originalModel.flush();		

		System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
		assertEquals("original document should be equivalent to merged document",
			d2.getText(0,d2.getLength()), 
			originalDocument.getText(0,originalDocument.getLength()));		
	}	
	
	public void testMerge7() throws Exception {
		System.out.println("testMerge7");
		
		String FILE1 = "diff/VehicleReservationService.wsdl";
		Document originalDocument = Util.getResourceAsDocument(FILE1);
		XDMModel originalModel = new XDMModel(originalDocument);
		originalModel.sync();

		String FILE2 = "diff/Vehicle_PartnerLinkChanged2.wsdl";
		Document d2 = Util.getResourceAsDocument(FILE2);
		XDMModel m2 = new XDMModel(d2);
		m2.sync();	
		
        long startTime=System.currentTimeMillis();
		
		XDMTreeDiff treeDiff = new XDMTreeDiff();
		treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
		
		long endTime=System.currentTimeMillis();
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
				"Total time to merge textchange.xml: "+(endTime-startTime)+"ms");
		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");		
		
		originalModel.flush();		

		System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
		assertEquals("original document should be equivalent to merged document",
			d2.getText(0,d2.getLength()), 
			originalDocument.getText(0,originalDocument.getLength()));		
	}	
	
	String SCHEMA_URI = "http://www.w3.org/2001/XMLSchema";	
}
