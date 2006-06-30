/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.struts.editor;


import java.io.IOException;
import java.io.StringWriter;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.web.struts.config.model.FormProperty;
import org.netbeans.modules.web.struts.config.model.Forward;
import org.netbeans.modules.web.struts.config.model.StrutsException;
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
    
    static public String END_LINE = System.getProperty("line.separator");  //NOI18N
    
    
    /** Returns the value of the path attribute, when there is an action
     * definition on the offset possition. Otherwise returns null.
     */
    public static String getActionPath(BaseDocument doc, int offset){
        try {
            ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
            TokenItem token = sup.getTokenChain(offset, offset+1);
            // find the element, which is on the offset
            while (token != null
                    && !(token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ELEMENT
                        && !( token.getImage().equals("/>") 
                            || token.getImage().equals(">")
                            || token.getImage().equals("<forward")
                            || token.getImage().equals("<exception")
                            || token.getImage().equals("</action")
                            || token.getImage().equals("<description")
                            || token.getImage().equals("<display-name")
                            || token.getImage().equals("<set-property")
                            || token.getImage().equals("<icon"))))
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
    
    /** Returns the value of the name attribute, when there is an ActionForm Bean
     * definition on the offset possition. Otherwise returns null.
     */
    public static String getActionFormBeanName(BaseDocument doc, int offset){
        try {
            ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
            TokenItem token = sup.getTokenChain(offset, offset+1);
            // find the start of element, which is on the offset
            while (token != null
                    && !(token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ELEMENT
                        && !( token.getImage().equals("/>") 
                            || token.getImage().equals(">")
                            || token.getImage().equals("<form-property")
                            || token.getImage().equals("</form-bean")
                            || token.getImage().equals("<description")
                            || token.getImage().equals("<display-name")
                            || token.getImage().equals("<set-property")
                            || token.getImage().equals("<icon"))))
                token = token.getPrevious();
            if (token != null && token.getImage().equals("<form-bean")){   //NOI18N
                token = token.getNext();
                while (token!= null 
                        && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ELEMENT
                        && !(token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ATTRIBUTE
                        && token.getImage().equals("name")))   // NOI18N
                    token = token.getNext();
                if (token != null && token.getImage().equals("name")){ // NOI18N
                    token = token.getNext();
                    while (token != null 
                            && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ATTRIBUTE_VALUE    
                            && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ELEMENT
                            && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ATTRIBUTE)
                        token = token.getNext();
                    if (token != null && token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ATTRIBUTE_VALUE){
                        String value = token.getImage().trim();
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
    
    public static int writeForwardIntoAction(BaseDocument doc, Forward forward, String actionName)
                    throws IOException{
        return writeElementIntoFather(doc, forward, "action", actionName, "forward");           //NOI18N
    }
    
    public static int writeExceptionIntoAction(BaseDocument doc, StrutsException ex, String actionName)
                    throws IOException{
        return writeElementIntoFather(doc, ex, "action", actionName, "exception");              //NOI18N
    }
    
    public static int writePropertyIntoBean(BaseDocument doc, FormProperty prop, String beanName)
                    throws IOException{
        return writeElementIntoFather(doc, prop, "form-bean", beanName, "form-property");       //NOI18N
    }
    
    private static int writeElementIntoFather(BaseDocument doc, BaseBean bean, 
            String father, String fatherName, String element) throws IOException{
        int possition = -1;
        ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
        TokenItem token =  null;
        try {
            int offset = 0;
            // find the name as an attribute value
            do{
                offset = doc.getText(0, doc.getLength()).indexOf("\""+fatherName+"\"", offset+1);       //NOI18N
                token = sup.getTokenChain(offset, offset+1);
                if (token != null && token.getTokenID().getNumericID() == XML_ATTRIBUTE_VALUE)
                    while ( token != null 
                            && token.getTokenID().getNumericID() != XML_ELEMENT)
                        token = token.getPrevious();
            }
            while (offset > 0 && token != null 
                        && !(token.getTokenID().getNumericID() == XML_ELEMENT
                        && token.getImage().equals("<"+father)));                       //NOI18N
            if (token != null && token.getTokenID().getNumericID() == XML_ELEMENT
                    && token.getImage().equals("<"+father)){                            //NOI18N
                token = token.getNext();
                // find the /> or >
                while (token != null && token.getTokenID().getNumericID() != XML_ELEMENT )
                    token = token.getNext();
                if (token != null && token.getImage().equals("/>")){                    //NOI18N
                    StringBuffer text = new StringBuffer();
                    offset = token.getOffset();
                    text.append(">");                                                   //NOI18N
                    text.append(END_LINE);
                    text.append(addNewLines(bean));
                    text.append(END_LINE);
                    text.append("</"); text.append(father); text.append(">");           //NOI18N
                    try{
                        doc.atomicLock();
                        doc.remove(offset, 2);
                        doc.insertString(offset, text.toString(), null);
                        offset += doc.getFormatter().reformat(doc, offset, offset + text.length()-1);
                        possition = offset;
                    }
                    finally{
                        doc.atomicUnlock();
                    }
                }
                if (token != null && token.getImage().equals(">")){                     //NOI18N
                    offset = -1;
                    while (token != null 
                            && !(token.getTokenID().getNumericID() == XML_ELEMENT
                            && token.getImage().equals("</"+father))){                  //NOI18N
                        while(token != null
                                && !(token.getTokenID().getNumericID() == XML_ELEMENT
                                && (token.getImage().equals("<"+element)                //NOI18N
                                || token.getImage().equals("</"+father))))              //NOI18N
                            token = token.getNext();
                        if (token != null && token.getImage().equals("<"+element)){     //NOI18N
                            while (token!= null
                                    && !(token.getTokenID().getNumericID() == XML_ELEMENT
                                    && (token.getImage().equals("/>")                   //NOI18N
                                    || token.getImage().equals("</"+element))))         //NOI18N
                                token = token.getNext();
                            if (token != null && token.getImage().equals("</"+element)) //NOI18N
                                while (token!= null
                                        && !(token.getTokenID().getNumericID() == XML_ELEMENT
                                        && token.getImage().equals(">")))               //NOI18N
                                    token = token.getNext();
                            if (token != null )
                                offset = token.getOffset() + token.getImage().length()-1;
                        }    
                        if (token != null && token.getImage().equals("</"+father) && offset == -1){     //NOI18N
                            while (token!= null
                                    && !(token.getTokenID().getNumericID() == XML_ELEMENT
                                    && (token.getImage().equals("/>")                   //NOI18N
                                    || token.getImage().equals(">"))))                  //NOI18N
                                token = token.getPrevious();
                            offset = token.getOffset()+token.getImage().length()-1;
                        }   
                    }
                    if (offset > 0)
                        possition = writeString(doc, addNewLines(bean), offset);
                }
            }
                
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return possition;
    }
    
    public static int writeBean(BaseDocument doc, BaseBean bean, String element, String section) throws IOException{
        int possition = -1;
        boolean addroot = false;
        boolean addsection = false;
        String sBean = addNewLines(bean);
        StringBuffer appendText = new StringBuffer();
        ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
        TokenItem token;
        int offset;
        try {
            String docText = doc.getText(0, doc.getLength());
            //Check whether there is root element
            String findString = "</" + element;                             //NOI18N
            
            //find index of last definition 
            offset = docText.lastIndexOf(findString);
            if (offset == -1){
                if ((offset = findEndOfElement(doc, "struts-config")) == -1){                 //NOI18N
                    offset = doc.getLength();
                    appendText = new StringBuffer();
                    appendText.append("<struts-config>");                   //NOI18N
                    appendText.append(END_LINE);
                    if (section != null && section.length()>0 ){
                        appendText.append("<"+section+">");                 //NOI18N
                        appendText.append(END_LINE);
                        appendText.append(sBean);
                        appendText.append(END_LINE);
                        appendText.append("</"+section+">");                //NOI18N
                    }
                    else{
                        appendText.append(sBean);
                    }
                    appendText.append(END_LINE);
                    appendText.append("</struts-config>");                  //NOI18N
                    appendText.append(END_LINE);
                    possition = writeString(doc, appendText.toString(), offset);
                }
                else{
                    if (section != null && section.length()>0){
                        int offsetSection;
                        if ((offsetSection = findEndOfElement(doc, section)) == -1){
                            appendText.append("<"+section+">");             //NOI18N
                            appendText.append(END_LINE);
                            appendText.append(sBean);
                            appendText.append(END_LINE);
                            appendText.append("</"+section+">");            //NOI18N
                        }
                        else {
                            appendText.append(sBean);
                            offset = offsetSection;
                        }
                    }
                    else 
                        appendText.append(sBean);
                    token = sup.getTokenChain(offset, offset+1);
                    if (token != null) token = token.getPrevious();
                    while (token != null
                            && !(token.getTokenID().getNumericID() == XML_ELEMENT
                            && token.getImage().equals(">")))               //NOI18N
                        token = token.getPrevious();
                    if (token != null)
                        offset = token.getOffset();
                    possition=writeString(doc, appendText.toString(), offset);
                }
            }
            else{
                token = sup.getTokenChain(offset, offset+1);
                if (token != null && token.getTokenID().getNumericID() == XML_ELEMENT){
                    while (token != null
                            && !(token.getTokenID().getNumericID() == XML_ELEMENT
                            && token.getImage().equals(">")))               //NOI18N
                        token = token.getPrevious();
                    if (token != null)
                        possition = writeString(doc, sBean, token.getOffset());    
                }
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
    
    private static int writeString(BaseDocument doc, String text, int offset) throws BadLocationException {
        int formatLength = 0;      
        try{
            doc.atomicLock();
            offset = doc.getFormatter().indentNewLine(doc, offset+1);
            doc.insertString(Math.min(offset, doc.getLength()), text, null );
            formatLength = doc.getFormatter().reformat(doc, offset, offset + text.length()-1);
        }
        finally{
            doc.atomicUnlock();
        }
        return Math.min(offset + formatLength + 1, doc.getLength());
    }
    
    private static int findStartOfElement(BaseDocument doc, String element) throws BadLocationException{
        String docText = doc.getText(0, doc.getLength());
        int offset = doc.getText(0, doc.getLength()).indexOf("<" + element);            //NOI18N
        ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
        TokenItem token;
        while (offset > 0){
           token = sup.getTokenChain(offset, offset+1); 
           if (token != null && token.getTokenID().getNumericID() == XML_ELEMENT)
               return token.getOffset();
           offset = doc.getText(0, doc.getLength()).indexOf("<" + element);             //NOI18N
        }
        return -1;
    }
    
    private static int findEndOfElement(BaseDocument doc, String element) throws BadLocationException{
        String docText = doc.getText(0, doc.getLength());
        int offset = doc.getText(0, doc.getLength()).indexOf("</" + element);           //NOI18N
        ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
        TokenItem token;
        while (offset > 0){
           token = sup.getTokenChain(offset, offset+1); 
           if (token != null && token.getTokenID().getNumericID() == XML_ELEMENT){
                offset = token.getOffset();
                token = token.getNext();
                while (token != null
                        && !(token.getTokenID().getNumericID() == XML_ELEMENT
                        && token.getImage().equals(">")))               //NOI18N
                    token = token.getNext();
                if (token != null)
                    offset = token.getOffset();
               return offset;
           }
           offset = doc.getText(0, doc.getLength()).indexOf("</" + element);            //NOI18N
        }
        return -1;
    }
    
}
