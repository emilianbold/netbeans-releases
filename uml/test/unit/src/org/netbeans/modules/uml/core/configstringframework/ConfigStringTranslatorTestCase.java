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


package org.netbeans.modules.uml.core.configstringframework;

import junit.framework.TestCase;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.PropertyDefinition;

/**
 * Test cases for ConfigStringTranslator.
 */
public class ConfigStringTranslatorTestCase extends TestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ConfigStringTranslatorTestCase.class);
    }

    private ConfigStringTranslator configStringTranslator = null;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        configStringTranslator = new ConfigStringTranslator();
        //project.addElement(configStringTranslator);
    }
    
	public void testTranslateIntoPSK() {
		// PropertyDefinitionNullAndExistingValue
		assertEquals("PSK_TRUE", configStringTranslator.translateIntoPSK(null,
				"True"));

		// PropertyDefinitionAndExistingValue
		IPropertyDefinition pDef = new PropertyDefinition();
		assertEquals("", configStringTranslator.translateIntoPSK(pDef, "True"));

		// PropertyDefinitionAndValidValues
		pDef = new PropertyDefinition();
		pDef.setValidValues2("PSK_NONE,PSK_TRUE,PSK_FALSE,");
		assertEquals("PSK_TRUE", configStringTranslator.translateIntoPSK(pDef,
				"True"));

		// PropertyDefinitionAndInValidValues
		pDef = new PropertyDefinition();
		pDef.setValidValues2("PSK_NONE,ABCD,PSK_FALSE,");
		assertEquals("", configStringTranslator.translateIntoPSK(pDef, "True"));

		// TestWithTranslator
		pDef = new PropertyDefinition();
		pDef
				.addToAttrMap("translator",
						"org.netbeans.modules.uml.core.configstringframework.NewTestTranslator");
		pDef.setValidValues2("PSK_NONE,PSK_TRUEKEY,ABCD,PSK_FALSE,");
		assertEquals("PSK_TRUE", configStringTranslator.translateIntoPSK(
				pDef, "True"));

		// PropertyDefinitionNullAndNonExistingValue
		assertEquals("", configStringTranslator.translateIntoPSK(null,
				"NotExistingValue"));

		// PropertyDefinitionAndNonExistingValue
		pDef = new PropertyDefinition();
		assertEquals("", configStringTranslator.translateIntoPSK(pDef,
				"NotExistingValue"));

	}

	public void testTranslate() {
		// PropertyDefinitionAndExistingTranslator
		IPropertyDefinition pDef = new PropertyDefinition();
		pDef
				.addToAttrMap("translator",
						"org.netbeans.modules.uml.core.configstringframework.TestTranslator");

		assertEquals("Package Background Color", configStringTranslator
				.translate(pDef, "PSK_PKGFILLCOLOR"));

		// PropertyDefinitionAndNewTranslator
		pDef = new PropertyDefinition();
		pDef
				.addToAttrMap("translator",
						"org.netbeans.modules.uml.core.configstringframework.NewTestTranslator");
		assertEquals("Window Color", configStringTranslator.translate(pDef,
				"PSK_PKGFILLCOLOR"));

		// TestWithList
		pDef = new PropertyDefinition();
		pDef.setControlType("list");
		assertEquals("Package Background Color", configStringTranslator
				.translate(pDef, "PSK_PKGFILLCOLOR"));

		// PropertyDefinitionNullAndExistingValue
		assertEquals("Package Background Color", configStringTranslator
				.translate(null, "PSK_PKGFILLCOLOR"));

		// PropertyDefinitionAndExistingValue
		pDef = new PropertyDefinition();
		assertEquals("Package Background Color", configStringTranslator
				.translate(pDef, "PSK_PKGFILLCOLOR"));

		// PropertyDefinitionNullAndNonExistingValue  -- Alternate scenario
		assertEquals("PSK_NOTEXISTINGKEY", configStringTranslator.translate(
				null, "PSK_NOTEXISTINGKEY"));

		// TestWithListAndNonExistingValue
		pDef = new PropertyDefinition();
		pDef.setControlType("list");
		assertEquals("PSK_NOTEXISTINGKEY", configStringTranslator.translate(pDef,
				"PSK_NOTEXISTINGKEY"));

	}

	public void testLookUpinMap() {
		// PassPSKAndTrueValue
		assertEquals("Package Background Color", configStringTranslator
				.lookUpInMap("PSK_PKGFILLCOLOR", true));

		// PassPSKAndFalseValue
		assertEquals("Package Background Color", configStringTranslator
				.lookUpInMap("PSK_PKGFILLCOLOR", false));

		// PassNonPSKAndFalseValue
		assertEquals("Package Background Color", configStringTranslator
				.lookUpInMap("IDS_STRING287", false));

		// PassNonPSKAndTrueValue
		assertEquals("IDS_STRING287", configStringTranslator.lookUpInMap(
				"IDS_STRING287", true));

		// PassPSKNonExistingValueAndTrueValue
		assertEquals("", configStringTranslator.lookUpInMap(
				"PSK_NOTEXISTINGKEY", true));

	
		// PassPSKNonExistingValueAndFalseValue
		assertEquals("", configStringTranslator.lookUpInMap(
				"PSK_NOTEXISTINGKEY", false));

		// PassNonPSKNonExistingValueAndTrueValue
		assertEquals("IDS_NOTEXISTINGKEY", configStringTranslator.lookUpInMap(
				"IDS_NOTEXISTINGKEY", true));


		// PassNonPSKNonExistingValueAndFalseValue
		assertEquals("", configStringTranslator.lookUpInMap(
				"IDS_NOTEXISTINGKEY", false));
	}

	public void testTranslateWord() {
		// PassNonPSKAndExistingValue
		assertEquals("Package Background Color", configStringTranslator
				.translateWord("IDS_STRING287"));

		// PassPSKAndExistingValue
		assertEquals("Package Background Color", configStringTranslator
				.translateWord("PSK_PKGFILLCOLOR"));

		// PassPSKAndNonExistingValue
		assertEquals("", configStringTranslator
				.translateWord("PSK_NOTEXISTINGKEY"));

		// PassNonPSKAndNonExistingValue
		assertEquals("", configStringTranslator
				.translateWord("IDS_NOTEXISTINGKEY"));
	}

	public void testAddToMap() {
		// testWithTwoArguments
		configStringTranslator.addToMap("TrueKey", "IDS_STRING105");
		assertEquals("True", configStringTranslator.translateWord("TrueKey"));

		// testWithThreeArguments
		configStringTranslator.addToMap("TrueKey", "PSK_TrueKey",
				"IDS_STRING105");
		assertEquals("True", configStringTranslator.translateWord("TrueKey"));
		assertEquals("True", configStringTranslator
				.translateWord("PSK_TrueKey"));

	}

	public void testClearMap() {

		configStringTranslator.addToMap("TestKey", "IDS_STRING287");
		configStringTranslator.clearMap();
		assertEquals("", configStringTranslator.translateWord("TestKey"));

		configStringTranslator.addToMap("TestKey", "IDS_STRING287");
		assertEquals("Package Background Color", configStringTranslator
				.translateWord("TestKey"));
	}

}
