package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for LanguageParserSettings.
 */
public class LanguageParserSettingsTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(LanguageParserSettingsTestCase.class);
    }

    private ILanguageParserSettings lps;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        lps = new LanguageParserSettings();
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testAddMacro()
    {
        LanguageMacro lm = new LanguageMacro();
        lm.setName("Dog");
        lps.addMacro("Java", lm);
        assertEquals(lm, lps.getMacro("Java", "Dog"));
        
        ETList<ILanguageMacro> lms = lps.getMacros("Java");
        assertEquals(1, lms.size());
        assertEquals(lm, lms.get(0));
    }

    public void testAddSetting()
    {
        lps.addSetting("Java", "cat", "hat");
        assertEquals("hat", lps.getSetting("Java", "cat"));
    }
}