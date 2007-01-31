/*
 * SyntaxParserListener.java
 *
 * Created on January 25, 2007, 4:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.editor.ext.html.parser;

import java.util.List;

/**
 * Allows clients to listen to SyntaxParser changes.
 * Implementation of this class can be registered following way:
 * <code>
 *      SyntaxParser.get(document).addSyntaxParserListener(impl);
 * </code>
 * If the document is already parsed and the data are up-to-date, the 
 * parsingFinished() method will be called synchronously from the 
 * addSyntaxParserListener() method, otherwise will be called asynchronously
 * once the parser finishes.
 *
 * @author Marek.Fukala@Sun.com
 */
public interface SyntaxParserListener {
    
    /** Called when HTML parser finishes parsing.
     * @return a list of SyntaxElement-s.
     */
    public void parsingFinished(List<SyntaxElement> elements);
    
}
