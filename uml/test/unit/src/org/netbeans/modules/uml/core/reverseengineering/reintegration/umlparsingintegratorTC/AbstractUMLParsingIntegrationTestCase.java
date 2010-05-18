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


package org.netbeans.modules.uml.core.reverseengineering.reintegration.umlparsingintegratorTC;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.StringTokenizer;

import org.dom4j.Document;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.Project;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ClassEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.DependencyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.PackageEvent;
import org.netbeans.modules.uml.core.reverseengineering.reintegration.ReverseEngineerTask;
import org.netbeans.modules.uml.core.reverseengineering.reintegration.UMLParsingIntegrator;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.openide.util.RequestProcessor;

public abstract class AbstractUMLParsingIntegrationTestCase extends
		AbstractUMLTestCase {
	/* Begin:Initialize Product */
	// private static ICoreProduct prod;
	// static {
	// CoreProductManager.instance().setCoreProduct(new ADProduct());
	// prod = CoreProductManager.instance().getCoreProduct();
	// prod.initialize();
	// }
	/* End: Initialize Product */
	static String testDataPath = null;

	final static String possibleClassRoot = "Class|Enumeration|Interface";

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void execute(String className) {

		final IProject project = getProject();

// conover - RE is a n NB task now, so can't call it directly anymore
//		final UMLParsingIntegrator integrator = new UMLParsingIntegrator();
//		integrator.reverseEngineer(project, false, false, true, true);

                ReverseEngineerTask reTask = new ReverseEngineerTask(
                    project, null, false, false, false, true, null);

                final UMLParsingIntegrator integrator =
                    (UMLParsingIntegrator)reTask.getParsingIntegrator();
                
                reTask.run();
                
//                RequestProcessor processor = 
//                    new RequestProcessor("uml/ReverseEngineer"); // NOI18N
//                processor.post(reTask);

                String fileNameInput = getDataDir() + "InputFiles" + File.separator
				+ className + "InputFile.txt";
		
		assertTrue("Input File Not Found", new File(fileNameInput).exists());
		
		String xmiDatas = readFile(fileNameInput);
		assertNotSame("Input File contains Empty String:", xmiDatas, "");
		
		integrator.onBeginParseFile(className + "File.java", null);

		StringTokenizer strTokens = new StringTokenizer(xmiDatas, "\r\n");
		while (strTokens.hasMoreTokens()) {
			String xmiData = (String) strTokens.nextElement();
			if (xmiData.trim().equals(""))
				continue;
			buildModel(integrator, xmiData);
		}

		integrator.integrate(project);

		String etdStr = project.getDocument().asXML();
		String newetdStr = getXMIRemovedStr(etdStr);
//		compareModel(newetdStr, className);
	}

	private void buildModel(UMLParsingIntegrator integrator, String xmiData) {
		int spaceIndex = xmiData.indexOf(" ");

		// Remove "<UML:" and find root Name
		String rootName = xmiData.substring(5, spaceIndex);

		Document doc = XMLManip.loadXML(xmiData);

		if (possibleClassRoot.indexOf(rootName) != -1) {
			onClassFound(integrator, doc);
		} else if (rootName.equals("Package")) {
			onPackageFound(integrator, doc);
		} else if (rootName.equals("Dependency")) {
			onDependencyFound(integrator, doc);
		}

	}

	private void onClassFound(UMLParsingIntegrator integrator, Document doc) {
		ClassEvent classEvent = new ClassEvent();
		classEvent.setEventData(doc);
		integrator.onClassFound(classEvent, null);
	}

	private void onPackageFound(UMLParsingIntegrator integrator, Document doc) {
		PackageEvent packageEvent = new PackageEvent();
		packageEvent.setEventData(doc);
		integrator.onPackageFound(packageEvent, null);
	}

	private void onDependencyFound(UMLParsingIntegrator integrator, Document doc) {
		DependencyEvent dependencyEvent = new DependencyEvent();
		dependencyEvent.setEventData(doc);
		integrator.onDependencyFound(dependencyEvent, null);
	}

	private void compareModel(String modelData, String className) {
		String resultFile = getDataDir() + "ResultFiles" + File.separator
				+ className + "ResultFile.txt";
		assertTrue("Result File Not Found", new File(resultFile).exists());
		
		String expectedModelData = readFile(resultFile);		
		assertNotSame("Result File contains Empty String:", expectedModelData,
				"");
		
		expectedModelData = expectedModelData.replace("\n", "");
		expectedModelData = expectedModelData.replace("\r", "");
		if (!expectedModelData.equals(modelData))
			System.out.println("\nClass Name: " + className + "\n\n"
					+ modelData);
		assertEquals("Model Data are Different ", expectedModelData, modelData);
	}

	private IProject getProject() {
		IProject project = new Project();
		Document doc = XMLManip.getDOMDocument();
		project.setDocument(doc);
		project.prepareNode();
		project.setMode("Implementation");
		project.setName("TestReIntegration");
		return project;
	}

	private static String readFile(String fileName) {
		String str = "";
		try {
			FileInputStream p = new FileInputStream(fileName);
			int ch = -1;
			while ((ch = p.read()) != -1)
				str += (char) ch;
		} catch (Exception ewe) {
			ewe.printStackTrace();
		}
		return str;
	}

	private String getXMIRemovedStr(String etdStr) {
		String retVal = etdStr;
		int index = -1;
		int newxmiID = 0;
		while ((index = retVal.indexOf("xmi.id=")) != -1) {

			String partI = retVal.substring(0, index);
			String partII = "xmi1.id=\"";
			String partIII = retVal.substring(index + 8);

			int idEndPos = partIII.indexOf("\"");
			String xmiId = partIII.substring(0, idEndPos);
			retVal = partI + partII + partIII;

			retVal = retVal.replace(xmiId, "" + ++newxmiID);
		}

		retVal = retVal.replace("xmi1.id=", "xmi.id=");
		retVal = retVal.replace(" neverSavedBefore=\"true\"", "");
		retVal = retVal.replace("\n", "");
		retVal = retVal.replace("\r", "");
		return retVal;
	}

	/**
	 * Get the path of test Data Folder
	 */
	private String getDataDir() {
		if (testDataPath != null)
			return testDataPath;

		testDataPath = System.getProperty("xtest.data");
		if (testDataPath != null)
			return (testDataPath += File.separator
					+ "ReIntegrationTestFiles" + File.separator);

		String s1 = getClass().getName();
		URL url = getClass().getResource(
				s1.substring(s1.lastIndexOf('.') + 1) + ".class");
		File file = (new File(url.getFile())).getParentFile();
		for (int i = 0; (i = s1.indexOf('.', i) + 1) > 0;)
			file = file.getParentFile();
		testDataPath = file.getParent() + File.separator + "data"
				+ File.separator + "ReIntegrationTestFiles" + File.separator;
		return testDataPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
