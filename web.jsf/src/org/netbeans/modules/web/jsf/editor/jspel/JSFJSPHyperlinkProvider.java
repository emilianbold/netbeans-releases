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

package org.netbeans.modules.web.jsf.editor.jspel;

import java.awt.Toolkit;
import java.io.IOException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.editor.JSFEditorUtilities;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Tomasz.Slota@Sun.COM
 * @author Petr Pisl
 */
public class JSFJSPHyperlinkProvider implements HyperlinkProvider {
    
    /** Creates a new instance of JSFJSPHyperlinkProvider */
    public JSFJSPHyperlinkProvider() {
    }
    
    /**
     * Should determine whether there should be a hyperlink on the given offset
     * in the given document. May be called any number of times for given parameters.
     * <br>
     * This method is called from event dispatch thread.
     * It should run very fast as it is called very often.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     * @return true if the provided offset should be in a hyperlink
     *         false otherwise
     */
    public boolean isHyperlinkPoint(Document doc, int offset) {
        if (!(doc instanceof BaseDocument))
            return false;
        
        BaseDocument bdoc = (BaseDocument) doc;
        bdoc.readLock();
        try {
        JTextComponent target = Utilities.getFocusedComponent();
        
        if (target == null || target.getDocument() != bdoc)
            return false;
        SyntaxSupport sup = bdoc.getSyntaxSupport();
        JspSyntaxSupport jspSup = (JspSyntaxSupport)sup.get(JspSyntaxSupport.class);
        
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(bdoc);
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();
        if(tokenSequence.move(offset) == Integer.MAX_VALUE) {
            return false; //no token found
        }
        
        if(!tokenSequence.moveNext()) {
            return false; //no token
        }
        
        Token token = tokenSequence.token();
        TokenSequence elTokenSequence = tokenSequence.embedded(ELTokenId.language());
        
        if (elTokenSequence != null){
            FileObject fObject = NbEditorUtilities.getFileObject(doc);
            WebModule wm = WebModule.getWebModule(fObject);
            
            if (wm != null){
                JSFELExpression exp = new JSFELExpression(wm, (JspSyntaxSupport)bdoc.getSyntaxSupport());
                elTokenSequence.move(offset);
                if(!elTokenSequence.moveNext()) {
                    return false; //no token
                }
                
                if (elTokenSequence.token().id() == ELTokenId.DOT){
                    return false;
                }
                
                int endOfEL = elTokenSequence.offset() + elTokenSequence.token().length();
                int res = exp.parse(endOfEL);
                
                if (res == JSFELExpression.EL_START){
                    res = exp.parse(endOfEL + 1);
                }
                
                return res == JSFELExpression.EL_JSF_BEAN;
            }
        }
        
        } finally {
            bdoc.readUnlock();
        }
        
        return false;
    }
    
    /**
     * Should determine the span of hyperlink on given offset. Generally, if
     * isHyperlinkPoint returns true for a given parameters, this class should
     * return a valid span, but it is not strictly required.
     * <br>
     * This method is called from event dispatch thread.
     * This method should run very fast as it is called very often.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     * @return a two member array which contains starting and ending offset of a hyperlink
     *         that should be on a given offset
     */
    public int[] getHyperlinkSpan(Document doc, int offset) {
        if (!(doc instanceof BaseDocument))
            return null;
        
        BaseDocument bdoc = (BaseDocument) doc;
        JTextComponent target = Utilities.getFocusedComponent();
        
        if (target == null || target.getDocument() != bdoc)
            return null;
        
        SyntaxSupport sup = bdoc.getSyntaxSupport();
        JspSyntaxSupport jspSup = (JspSyntaxSupport)sup.get(JspSyntaxSupport.class);
        
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(bdoc);
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();
        if(tokenSequence.move(offset) == Integer.MAX_VALUE) {
            return null; //no token found
        }
        if(!tokenSequence.moveNext()) {
            return null; //no token
        }
        
        Token token = tokenSequence.token();
        
        // is it a bean in EL ?
        TokenSequence elTokenSequence = tokenSequence.embedded(ELTokenId.language());
        
        if (elTokenSequence != null){
            FileObject fObject = NbEditorUtilities.getFileObject(doc);
            WebModule wm = WebModule.getWebModule(fObject);
            if (wm != null){
                JSFELExpression exp = new JSFELExpression(wm, (JspSyntaxSupport)bdoc.getSyntaxSupport());
                elTokenSequence.move(offset);
                if(!elTokenSequence.moveNext()) {
                    return null; //no token
                }
                
                int elEnd = elTokenSequence.offset() + elTokenSequence.token().length();
                
                int res = exp.parse(elEnd);
                if (res == JSFELExpression.EL_JSF_BEAN || res == JSFELExpression.EL_START )
                    return new int[] {elTokenSequence.offset(), elEnd};
            }
        }
        return null;
    }
    
    /**
     * The implementor should perform an action
     * corresponding to clicking on the hyperlink on the given offset. The
     * nature of the action is given by the nature of given hyperlink, but
     * generally should open some resource or move cursor
     * to certain place in the current document.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     */
    public void performClickAction(Document doc, int offset) {
        
        BaseDocument bdoc = (BaseDocument) doc;
        JTextComponent target = Utilities.getFocusedComponent();
        
        if (target == null || target.getDocument() != bdoc)
            return;
        
        SyntaxSupport sup = bdoc.getSyntaxSupport();
        JspSyntaxSupport jspSup = (JspSyntaxSupport)sup.get(JspSyntaxSupport.class);
        
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(bdoc);
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();
        if(tokenSequence.move(offset) == Integer.MAX_VALUE) {
            return; //no token found
        }
        if(!tokenSequence.moveNext()) {
            return ; //no token
        }
        
        Token token = tokenSequence.token();
        
        // is it a bean in EL
        TokenSequence elTokenSequence = tokenSequence.embedded(ELTokenId.language());
        if (elTokenSequence != null){
            FileObject fObject = NbEditorUtilities.getFileObject(doc);
            WebModule wm = WebModule.getWebModule(fObject);
            if (wm != null){
                JSFELExpression exp = new JSFELExpression(wm, (JspSyntaxSupport)bdoc.getSyntaxSupport());
                elTokenSequence.move(offset);
                if(!elTokenSequence.moveNext()) {
                    return; //no token
                }
                
                int res = exp.parse(elTokenSequence.offset() + elTokenSequence.token().length());
                if (res == JSFELExpression.EL_START ){
                    (new OpenConfigFile(wm, elTokenSequence.token().text().toString())).run();
                    return;
                }
                if (res == JSFELExpression.EL_JSF_BEAN){
                    if (!exp.gotoPropertyDeclaration(exp.getObjectClass())){
                        String msg = NbBundle.getBundle(JSFJSPHyperlinkProvider.class).getString("MSG_source_not_found");
                        StatusDisplayer.getDefault().setStatusText(msg);
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        }
    }
    
    private static class OpenConfigFile implements Runnable {
        private String beanName;
        private WebModule wm;
        
        OpenConfigFile(WebModule wm, String beanName){
            this.beanName = beanName;
            this.wm = wm;
        }
        
        public void run(){
            if (wm == null) return;
            
            FileObject config = ConfigurationUtils.findFacesConfigForManagedBean(wm, beanName);
            if (config != null) {
                try{
                    DataObject dobj = DataObject.find(config);
                    if (dobj != null) {
                        LineCookie lineCookie = dobj.getCookie(LineCookie.class);
                        EditorCookie editorCookie = dobj.getCookie(EditorCookie.class);
                        // EditCookie is needed, because we want to open the source editor.
                        // If we use OpenCookie, then the PageFlow editor will be displayed
                        EditCookie editCookie = dobj.getCookie(EditCookie.class);
                        if (editorCookie != null) {
                            StyledDocument document = editorCookie.openDocument();
                            int[] definition = JSFEditorUtilities.getManagedBeanDefinition((BaseDocument)document, beanName);
                            // line number in the document
                            int lineNumber = NbDocument.findLineNumber(document, definition[0]);
                            int lineOffset = NbDocument.findLineOffset(document, lineNumber);
                            // column at the line
                            int column = lineOffset - definition[0];

                            if (lineNumber != -1) {
                                Line line = lineCookie.getLineSet().getCurrent(lineNumber);

                                if(line != null) {
                                    // show the line
                                    line.show(Line.SHOW_TOFRONT, column);
                                }
                            }

                            if (editCookie != null) {
                                // open the editor with source file
                                editCookie.edit();
                            }
                        }
                    }
                }
                catch (DataObjectNotFoundException exception) {
                    Exceptions.printStackTrace(exception);
                }
                catch (IOException exception) {
                    Exceptions.printStackTrace(exception);
                }
            }
        }
    }
}
