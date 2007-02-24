package org.netbeans.modules.uml.core.roundtripframework.roundtripevents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEnumEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEnumLiteralEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripPackageEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripRelationEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public class RoundTripInterfaceEventsTestCase extends AbstractUMLTestCase implements IRoundTripAttributeEventsSink, IRoundTripClassEventsSink, IRoundTripEnumEventsSink, IRoundTripEnumLiteralEventsSink, IRoundTripOperationEventsSink, IRoundTripPackageEventsSink, IRoundTripRelationEventsSink
{
	private int elemKind;
	private int elemKindFromInputFile;
	private int eventState;
	private int eventStateFromInputFile;	
	private File propFile;
	private String evntInfoFile = "EventInfo.txt";
	static String testDataPath = null;
	int i = 0;
	
	// This variable is to check if any events other than expected are thrown. If so , its value becomes greater than 0
	private int count = 0;
	
	
	/**
     * Here the Roundtrip Controller, dispatcher created and all listeners are attached.
     */
	public void setUp()
	{
		String infoFile = getDataDir()+ evntInfoFile;
		try
		{
			propFile = new File(infoFile);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		IRoundTripController rt = product.getRoundTripController();
        IRoundTripEventDispatcher disp = rt.getRoundTripDispatcher();
        
        
        disp.registerForRoundTripAttributeEvents(this, "Java");
        disp.registerForRoundTripOperationEvents(this, "Java");
        disp.registerForRoundTripClassEvents(this, "Java");
        disp.registerForRoundTripEnumEvents(this, "Java");
        disp.registerForRoundTripEnumLiteralEvents(this, "Java");
        disp.registerForRoundTripPackageEvents(this, "Java");
        disp.registerForRoundTripRelationEvents(this, "Java");
        
	}

	 /**
     * InterfaceCreationTestCase
     */
	public void testInterfaceCreate()
	{
		resetVals();
		IInterface intrfce = createInterface("TestInterface1");
		readFromFile(propFile,"Interface","create");

		assertEquals(elemKind,elemKindFromInputFile);
		assertEquals(eventState,eventStateFromInputFile);
		assertEquals(0, count);
	}
	 
	 /**
     * InterfaceModificationTestCase
     */
	public void testInterfaceModify()
	{
		IInterface intrfce = createInterface("TestInterface2");
		resetVals();
		intrfce.setName("NewInterface");
		readFromFile(propFile,"Interface","modify");

		assertEquals(elemKind,elemKindFromInputFile);
		assertEquals(eventState,eventStateFromInputFile);
		assertEquals(0, count);
	}
	
	 /**
     * InterfaceDeletionTestCase
     */
	public void testInterfaceDelete()
	{
		IInterface intrfce = createInterface("TestInterface3");
		resetVals();
		intrfce.delete();
		readFromFile(propFile,"Interface","delete");
		
		assertEquals(elemKind,elemKindFromInputFile);
		assertEquals(eventState,eventStateFromInputFile);
		assertEquals(0, count);
	}

	/**
     * Fired before an interface has been created. We have nothing to check here.
     */
	public void onPreClassChangeRequest(IChangeRequest newVal, IResultCell cell) {
		
	}
	
	/**
     * Fired after an interface has been created. 
     */
	public void onClassChangeRequest(IChangeRequest chngReq, IResultCell cell) {
		elemKind = chngReq.getElementType();
		eventState = chngReq.getState();
		
	}
	
	/**
     * Attribute preCreation event - Not needed to be implemented in this test case
     */
	public void onPreAttributeChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	/**
     * Attribute creation event - Not needed to be implemented in this test case
     */
	public void onAttributeChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	/**
     * Relation preCreation event - Not needed to be implemented in this test case. 
     */
	public void onPreRelationChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	/**
     * Relation Creation event - Not needed to be implemented in this test case. 
     */
	public void onRelationChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	/**
     * Enum precreation event - Not needed to be implemented in this test case. 
     */
	public void onPreEnumChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	/**
     * Enum creation event - Not needed to be implemented in this test case. 
     */
	public void onEnumChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	/**
     * Enum preLiteralCreation event - Not needed to be implemented in this test case. 
     */	
	public void onPreEnumLiteralChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	/**
     * Enum LiteralCreation event - Not needed to be implemented in this test case. 
     */
	public void onEnumLiteralChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	 /**
     * Operation preCreation event - Not needed to be implemented in this test case. 
     */
	public void onPreOperationChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	 /**
     * Operation Creation event - Not needed to be implemented in this test case. 
     */
	public void onOperationChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	 /**
     * Package preCreation event - Not needed to be implemented in this test case. 
     */
	public void onPrePackageChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	/**
     * Package Creation event - Not needed to be implemented in this test case. 
     */
	public void onPackageChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	 /**
     * Get the path of  test Data Folder
     */
	
	private String getDataDir() {
		if (testDataPath != null)
			return testDataPath;

		testDataPath = System.getProperty("xtest.data");
		if (testDataPath != null)
			return (testDataPath += File.separator
					+ "RTEventInfoFiles" + File.separator);

		String s1 = getClass().getName();
		URL url = getClass().getResource(
				s1.substring(s1.lastIndexOf('.') + 1) + ".class");
		File file = (new File(url.getFile())).getParentFile();
		for (int i = 0; (i = s1.indexOf('.', i) + 1) > 0;)
			file = file.getParentFile();
		testDataPath = file.getParent() + File.separator + "data"
				+ File.separator + "RTEventInfoFiles" + File.separator;
		return testDataPath;
	}
	

	 /**
	 * @propFile - the input file
	 * @findElemType - the element being acted upon like attribute, operation
	 * @findChangeKind - the action on the element like create,delete etc
     * Reading from the input file
     */
	protected void readFromFile(File propFile, String findElemType, String findChangeKind)
	{
		String str, retValString = null ;
		try 
		{
			BufferedReader in = new BufferedReader(new FileReader(propFile));
			while ((str = in.readLine()) != null)
			{
				if((str.indexOf(findElemType) != -1))
				{
					if((str.indexOf(findChangeKind) != -1))
					{
						retValString = str.substring(str.lastIndexOf("-")+1);
						eventStateFromInputFile = Integer.parseInt(retValString);
						String subStr1 = str.substring((str.indexOf("-")+1),str.length());
						String tempStr = subStr1.substring(0,subStr1.indexOf("-"));
						elemKindFromInputFile = Integer.parseInt(tempStr);
						break;
					}
				}
			}
			in.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		catch(IOException ioe)
		{
			
		}  
	}
	
	/**
     * Reset the values for certain variables
     */
	private void resetVals()
	{
		count= 0;
		elemKind = -1;	
		eventState = -1;

	}
}
