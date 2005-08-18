/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jsf.editor;


import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.web.jsf.config.model.NavigationRule;
import org.openide.ErrorManager;

/**
 *
 * @author Petr Pisl
 */

public class JSFEditorUtilities {
    
    /** The constant from XML editor
     */
    // The constant are taken from class org.netbeans.modules.xml.text.syntax.XMLTokenIDs
    protected static int XML_ELEMENT = 4;
    protected static int XML_TEXT = 1;
    
    /** Returns the value of from-view-id element of navigation rule definition on the offset possition.
     *  If there is not the navigation rule definition on the offset, then returns null. 
     */
    public static String getNavigationRule(BaseDocument doc, int offset){
        try {
            ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
            TokenItem token = sup.getTokenChain(offset, offset+1);
            // find the srart of the navigation rule definition
            while (token != null
                    && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                    && (token.getImage().equals("<navigation-rule")
                    || token.getImage().equals("<managed-bean"))))
                token = token.getPrevious();
            if (token != null && token.getImage().equals("<navigation-rule")){
                // find the from-view-ide element
                while (token != null
                        && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                        && (token.getImage().equals("</navigation-rule")
                        || token.getImage().equals("<from-view-id"))))
                    token = token.getNext();
                if (token!= null && token.getImage().equals("<from-view-id")){
                    token = token.getNext();
                    while (token!=null 
                            && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                            && token.getImage().equals(">")))
                        token = token.getNext();
                    while (token != null
                            && token.getTokenID().getNumericID() != JSFEditorUtilities.XML_TEXT
                            && token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT)
                        token = token.getNext();
                    if (token != null && token.getTokenID().getNumericID() == JSFEditorUtilities.XML_TEXT)
                        return token.getImage().trim();
                }
            }
        } 
        catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
        return null;
    }
    
    
}
