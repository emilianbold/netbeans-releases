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


import java.io.IOException;
import java.io.StringWriter;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.web.jsf.config.model.ManagedBean;
import org.netbeans.modules.web.jsf.config.model.NavigationCase;
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
    static public String END_LINE = System.getProperty("line.separator");  //NOI18N
    
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
    
    public static int[] getNavigationRuleDefinition(BaseDocument doc, String ruleName){
        try{
            String text = doc.getText(0, doc.getLength());
            int offset = text.indexOf(ruleName);
            int start = 0;
            int end = 0;
            ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
            TokenItem token;
            
            while (offset != -1){
                token = sup.getTokenChain(offset, offset+1);
                if (token != null && token.getTokenID().getNumericID() == JSFEditorUtilities.XML_TEXT){
                    while (token!=null 
                            && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                            && !token.getImage().equals(">")))
                        token = token.getPrevious();
                    if (token != null && token.getImage().equals("<from-view-id")){
                        while (token != null
                                && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                && token.getImage().equals("<navigation-rule")))
                            token = token.getPrevious();
                        if(token != null && token.getImage().equals("<navigation-rule")){
                            start = token.getOffset();
                            token = sup.getTokenChain(offset, offset+1);
                            while (token != null
                                    && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                    && token.getImage().equals("</navigation-rule")))
                                token = token.getNext();
                            if (token!=null && token.getImage().equals("</navigation-rule")){
                                while (token != null
                                        && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                        && token.getImage().equals(">")))
                                    token = token.getNext();
                                if (token!=null && token.getImage().equals(">")){
                                    end = token.getOffset()+1;
                                    return new int[]{start, end};
                                }
                            }
                            return new int[]{start, text.length()};
                        }
                    }
                }
                offset = text.indexOf(ruleName, offset+ruleName.length());
            }
        }
        catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        } 
        return new int []{-1,-1};
    }
    
    public static int writeCaseIntoRule(BaseDocument doc, String fromViewID, NavigationCase navigationCase) throws IOException{
        int possition = -1;
        int [] definition = getNavigationRuleDefinition(doc, fromViewID);
        ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
        String sBean = addNewLines(navigationCase);
        TokenItem token;
        try{
            if (definition [0] > -1){
                if (definition[1] < doc.getLength()){
                    token = sup.getTokenChain(definition[1]-2, definition[1]-1);
                    while (token != null
                            && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                            && token.getImage().equals(">")))
                        token = token.getPrevious();
                    if (token != null ){
                        possition = writeString(doc, sBean, token.getOffset());
                    }
                }
            }
        }
        catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
        return possition;
    }


    /** Writes new bean to the document directly under <faces-config> element
     */
    public static int writeBean(BaseDocument doc, BaseBean bean, String element) throws IOException{
        String sBean = addNewLines(bean);
        int possition = -1;
        ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
        TokenItem token;
        try {
            String docText = doc.getText(0, doc.getLength());
            //Check whether there is root element
            if (docText.indexOf("<faces-config") == -1){                //NOI18N
                doc.insertString(doc.getLength(), "<faces-config>"      //NOI18N
                        + END_LINE + "</faces-config>", null);          //NOI18N       
                docText = doc.getText(0, doc.getLength());
            }
            String findString = "</" + element;
            
            //find index of last definition 
            int offset = docText.lastIndexOf(findString);
            if (offset == -1)
                offset = docText.length() - 2;
            token = sup.getTokenChain(offset, offset+1);
            if (offset < (docText.length() - 2)
                    && token != null && token.getTokenID().getNumericID() == XML_ELEMENT){
                while (token != null
                        && !(token.getTokenID().getNumericID() == XML_ELEMENT
                        && token.getImage().equals(">")))               //NOI18N
                    token = token.getNext();
                if (token != null)
                    possition = writeString(doc, sBean, token.getOffset());    
            }
            else {
                // write to end
                if (token != null && token.getImage().equals(">"))      //NOI18N
                    token = token.getPrevious();
                while (token != null
                        && !(token.getTokenID().getNumericID() == XML_ELEMENT
                        && token.getImage().equals(">")))               //NOI18N
                    token = token.getPrevious();
                if (token != null)
                    possition = writeString(doc, sBean, token.getOffset());    
            }
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return possition;
    }
    
    private static String addNewLines(final BaseBean bean) throws IOException {
        StringWriter sWriter = new StringWriter();
        bean.writeNode(sWriter);
        String sBean = sWriter.toString();
        sBean = sBean.replaceAll("><", ">"+END_LINE+"<");               //NOI18N
        return sBean;
    }
    
    private static int writeString(BaseDocument doc, String text, int offset){
        int formatLength = 0;
        try{
            doc.atomicLock();
            offset = doc.getFormatter().indentNewLine(doc, offset+1);
            doc.insertString(offset, text, null );
            formatLength = doc.getFormatter().reformat(doc, offset, offset + text.length()-1);
        }
        catch(BadLocationException ex){
            ErrorManager.getDefault().notify(ex);
        }
        finally {
            doc.atomicUnlock();
        }
        return offset + formatLength + 1;
    }
}
