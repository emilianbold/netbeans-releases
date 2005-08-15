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

package org.netbeans.modules.web.struts.editor;


import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.openide.ErrorManager;

/**
 *
 * @author Petr Pisl
 */

public class StrutsEditorUtilities {
    
    /** The constant from XML editor
     */
    protected static int XML_ATTRIBUTE = 5;
    protected static int XML_ELEMENT = 4;
    protected static int XML_ATTRIBUTE_VALUE = 7;
    
    
    /** Returns the value of the path attribute, when there is an action
     * definition on the offset possition. Otherwise returns null.
     */
    public static String getActionPath(BaseDocument doc, int offset){
        try {
            ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
            TokenItem token = sup.getTokenChain(offset, offset+1);
            // find the element, which is on the offset
            while (token != null
                    && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ELEMENT)
                token = token.getPrevious();
            if (token != null && token.getImage().equals("<action")){   //NOI18N
                token = token.getNext();
                while (token!= null 
                        && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ELEMENT
                        && !(token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ATTRIBUTE
                        && token.getImage().equals("path")))   // NOI18N
                    token = token.getNext();
                if (token != null && token.getImage().equals("path")){ // NOI18N
                    token = token.getNext();
                    while (token != null 
                            && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ATTRIBUTE_VALUE    
                            && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ELEMENT
                            && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ATTRIBUTE)
                        token = token.getNext();
                    if (token != null && token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ATTRIBUTE_VALUE){
                        String value = token.getImage().trim();
                        //remove ""
                        value = value.substring(1);
                        value = value.substring(0, value.length()-1);
                        return value;
                    }
                }
            }   
            return null;
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
        return null;
    }
    
}
