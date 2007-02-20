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

package org.netbeans.modules.web.jsf.editor;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.JSFFrameworkProvider;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;

/**
 *
 * @author Petr Pisl
 */

public class JSFEditorUtilities {
    
    /** The constant from XML editor
     */
    // The constant are taken from class org.netbeans.modules.xml.text.syntax.XMLTokenIDs
    protected final static int XML_ELEMENT = 4;
    protected final static int XML_TEXT = 1;
    public final static String END_LINE = System.getProperty("line.separator");  //NOI18N
    
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
            //find first possition of text that is the ruleName
            int offset = text.indexOf(ruleName);
            int start = 0;
            int end = 0;
            ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
            TokenItem token;
            
            while (offset != -1){
                token = sup.getTokenChain(offset, offset+1);
                if (token != null && token.getTokenID().getNumericID() == JSFEditorUtilities.XML_TEXT){
                    // find first xml element before the ruleName
                    while (token!=null 
                            && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                            && !token.getImage().equals(">")))
                        token = token.getPrevious();
                    // is it the rule definition?
                    if (token != null && token.getImage().equals("<from-view-id")){
                        // find start of the rule definition
                        while (token != null
                                && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                && token.getImage().equals("<navigation-rule")))
                            token = token.getPrevious();
                        if(token != null && token.getImage().equals("<navigation-rule")){
                            start = token.getOffset();
                            token = sup.getTokenChain(offset, offset+1);
                            // find the end of the rule definition
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
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        } 
        return new int []{-1,-1};
    }
    
    public static int[] getConverterDefinition(BaseDocument doc, String converterForClass){
        try{
            String text = doc.getText(0, doc.getLength());
            //find first possition of text that is the ruleName
            int offset = text.indexOf(converterForClass);
            int start = 0;
            int end = 0;
            ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
            TokenItem token;
            
            while (offset != -1){
                token = sup.getTokenChain(offset, offset+1);
                if (token != null && token.getTokenID().getNumericID() == JSFEditorUtilities.XML_TEXT){
                    // find first xml element before the ruleName
                    while (token!=null 
                            && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                            && !token.getImage().equals(">")))
                        token = token.getPrevious();
                    // is it the rule definition?
                    if (token != null && token.getImage().equals("<converter-for-class")){
                        // find start of the rule definition
                        while (token != null
                                && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                && token.getImage().equals("<converter")))
                            token = token.getPrevious();
                        if(token != null && token.getImage().equals("<converter")){
                            start = token.getOffset();
                            token = sup.getTokenChain(offset, offset+1);
                            // find the end of the rule definition
                            while (token != null
                                    && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                    && token.getImage().equals("</converter")))
                                token = token.getNext();
                            if (token!=null && token.getImage().equals("</converter")){
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
                offset = text.indexOf(converterForClass, offset+converterForClass.length());
            }
            
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        } 
        return new int []{-1,-1};
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
    
    /* Returns offset, where starts the definition of the manage bean
     **/
    public static int[] getManagedBeanDefinition(BaseDocument doc, String beanName){
        try{
            String text = doc.getText(0, doc.getLength());
            int offset = text.indexOf(beanName);
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
                    if (token != null && token.getImage().equals("<managed-bean-name")){    //NOI18N
                        while (token != null
                                && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                && token.getImage().equals("<managed-bean")))
                            token = token.getPrevious();
                        if(token != null && token.getImage().equals("<managed-bean")){
                            start = token.getOffset();
                            token = sup.getTokenChain(offset, offset+1);
                            while (token != null
                                    && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                    && token.getImage().equals("</managed-bean")))
                                token = token.getNext();
                            if (token!=null && token.getImage().equals("</managed-bean")){
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
                offset = text.indexOf(beanName, offset+beanName.length());
            }
        }
        catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        } 
        return new int []{-1,-1};
    }
    
    /**
     * Method that allows to find its
     * CloneableEditorSupport from given DataObject
     * @return the support or null if the CloneableEditorSupport 
     * was not found
     * This method is hot fix for issue #53309
     * this methd was copy/pasted from OpenSupport.Env class
     * @param dob an instance of DataObject
     */
    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        Node.Cookie obj = dob.getCookie(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        obj = dob.getCookie(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        return null;
    }
    
    private static class CreateXMLPane implements Runnable{
        JEditorPane ep;

        public void run (){
            ep = new JEditorPane("text/xml", "");
        }

        public JEditorPane getPane (){
            return ep;
        }
    }
    
    /** This method returns a BaseDocument for the configuration file. If the configuration
     *  file is not opened, then the document is not created yet and this method returns
     *  in this case fake document. 
     */
    public static BaseDocument getBaseDocument(JSFConfigDataObject config){
        BaseDocument document = null;
        CloneableEditorSupport editor = JSFEditorUtilities.findCloneableEditorSupport(config);
        if (editor != null){
            document = (BaseDocument)editor.getDocument();
            if (document == null) {
                JEditorPane ep = null;
                CreateXMLPane run = new CreateXMLPane();
                try {
                    SwingUtilities.invokeAndWait(run);
                    document = new BaseDocument(run.getPane().getEditorKit().getClass(), false);
                    String text = "";
                    text = JSFFrameworkProvider.readResource(config.getPrimaryFile().getInputStream(), "UTF-8");
                    document.remove(0, document.getLength());
                    document.insertString(0, text, null);
                } catch (InterruptedException ex) {
                    ErrorManager.getDefault().notify(ex);
                } catch (InvocationTargetException ex) {
                    ErrorManager.getDefault().notify(ex);   
                } catch (FileNotFoundException ex) {
                    ErrorManager.getDefault().notify(ex);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);    
                } catch (BadLocationException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
        return document;
    }
}
