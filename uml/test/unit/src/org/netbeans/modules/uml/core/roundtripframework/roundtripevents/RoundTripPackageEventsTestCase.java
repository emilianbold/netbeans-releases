/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.uml.core.roundtripframework.roundtripevents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;
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

public class RoundTripPackageEventsTestCase extends AbstractUMLTestCase implements IRoundTripAttributeEventsSink, IRoundTripClassEventsSink, IRoundTripEnumEventsSink, IRoundTripEnumLiteralEventsSink, IRoundTripOperationEventsSink, IRoundTripPackageEventsSink, IRoundTripRelationEventsSink 
{
	private int elemKind;
	private int elemKindFromInputFile;
	private int eventState;
	private int eventStateFromInputFile;	
	private File propFile;
	private String evntInfoFile = "EventInfo.txt";
	static String testDataPath = null;
	
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
     * PackageCreationTestCase
     */
	public void testPackageCreate()
	{
		IPackage tstPackage = createType("Package");
		resetVals();
		tstPackage.setName("TestPackage1");
		readFromFile(propFile,"Package","create");
		assertEquals(elemKind,elemKindFromInputFile);
		assertEquals(eventState,eventStateFromInputFile);
		assertEquals(0, count);
	}

	/**
     * PackageModificationTestCase
     */
	public void testPackageModify()
	{
		IPackage tstPackage = createType("Package");
		tstPackage.setName("TestPackage2");
		resetVals();
		tstPackage.setVisibility(IVisibilityKind.VK_PROTECTED);
		readFromFile(propFile,"Package","modify");
		assertEquals(elemKind,elemKindFromInputFile);
		assertEquals(eventState,eventStateFromInputFile);
		assertEquals(0, count);
	}

	/**
     * PackageDeletionTestCase
     */
	public void testPackageDelete()
	{
		IPackage tstPackage = createType("Package");
		tstPackage.setName("TestPackage3");
		resetVals();
		tstPackage.delete();
		readFromFile(propFile,"Package","delete");
		assertEquals(elemKind,elemKindFromInputFile);
		assertEquals(eventState,eventStateFromInputFile);
		assertEquals(0, count);
	}


	/**
     * Fired before a package has been created. We have nothing to check here.
     */
	public void onPrePackageChangeRequest(IChangeRequest newVal, IResultCell cell) {
				
	}
	
	/**
     * Fired after a package has been created. 
     */
	public void onPackageChangeRequest(IChangeRequest chngReq, IResultCell cell) 
	{
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
    * Class preCreation event - Not needed to be implemented in this test case
    */
	public void onPreClassChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	/**
     * Class Creation event - Not needed to be implemented in this test case
     */
	public void onClassChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	/**
     * Enum preCreation event - Not needed to be implemented in this test case. 
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
		count = 0;
		elemKind = -1;	
		eventState = -1;

	}
}
