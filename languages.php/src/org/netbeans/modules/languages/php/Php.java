/*
 * PHP.java
 *
 * Created on February 22, 2007, 3:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.languages.php;

import java.util.Collections;

import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.Language;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.php.lexer.PhpTokenId;

/**
 *
 * @author ads
 */
public class Php {
    
    /**
     * This is PHP grammatic state ( see nbs file ).
     */
    private static final String DEFAULT = "DEFAULT";


    /** Creates a new instance of PHP */
    public Php() {
    }
    
    public static String functionName(SyntaxContext context) {
        ASTPath path = context.getASTPath();
        ASTNode n = (ASTNode) path.getLeaf();
        String name = null;
        ASTNode nameNode = n.getNode("FunctionName");
        
        if (nameNode != null) {
            name = nameNode.getAsText();
        }
        
        String parameters = "";
        ASTNode parametersNode = n.getNode("FormalParameterList");
        
        if (parametersNode != null) {
            parameters = parametersNode.getAsText();
        }
        
        if (name != null) {
            return name + "(" + parameters + ")";
        }

        return "?";
    }
    
    public static String className(SyntaxContext context) {
        ASTPath path = context.getASTPath();
        ASTNode n = (ASTNode) path.getLeaf();
        ASTNode nameNode = n.getNode("ClassName");

        if (nameNode != null) {
            return nameNode.getAsText();
        }
        
        return "?";
    }
    
    public static String constantName(SyntaxContext context) {
        ASTPath path = context.getASTPath();
        ASTNode n = (ASTNode) path.getLeaf();
        ASTNode nameNode = n.getNode("ConstantName");

        if (nameNode != null) {
            String name = nameNode.getAsText();
            
            return name.substring(1, name.length() - 1);
        }
        
        return "?";
    }
    
    public static Object[] getEODString( CharInput input) {
        EODTokenizer tokenizer = new EODTokenizer( input );
        ASTToken token = tokenizer.getToken();
        
        return new Object[] {  token, DEFAULT };
    }
    
    private static String getAsText (ASTNode n) {
        if (n == null) return "";
        return n.getAsText ();
    }
}
