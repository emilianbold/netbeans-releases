package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for LanguageSyntax.
 */
public class LanguageSyntaxTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(LanguageSyntaxTestCase.class);
    }

    private ILanguageSyntax ls = 
            product.getLanguageManager().getLanguage("Java").getSyntax();

    public void testGetCharacterDelimiter()
    {
        assertEquals("'", ls.getCharacterDelimiter().getName());
    }

    public void testGetStringDelimiter()
    {
        assertEquals("\"", ls.getStringDelimiter().getName());
    }

    public void testGetTokensByCategory()
    {
        assertEquals(11, ls.getTokensByCategory(TokenKind.TK_KEYWORD, "Modifier").size());
    }

//    public void testGetTokensByKind()
//    {
//        assertEquals(40, ls.getTokensByKind(TokenKind.TK_KEYWORD).size());
//    }
}