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
