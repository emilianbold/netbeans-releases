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

import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.syntax.JSPHyperlinkProvider;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.modules.web.core.syntax.deprecated.ELTokenContext;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.editor.JSFEditorUtilities;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 *
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
        
        try {
            BaseDocument bdoc = (BaseDocument) doc;
            JTextComponent target = Utilities.getFocusedComponent();
            
            if (target == null || target.getDocument() != bdoc)
                return false;
            SyntaxSupport sup = bdoc.getSyntaxSupport();
            JspSyntaxSupport jspSup = (JspSyntaxSupport)sup.get(JspSyntaxSupport.class);
            
            TokenItem token = jspSup.getTokenChain(offset, offset+1);
            if (token == null) return false;
            TokenID tokenID = token.getTokenID();
            if (tokenID == null) return false;
            
            if (token.getTokenContextPath().contains(ELTokenContext.contextPath)){
                FileObject fObject = NbEditorUtilities.getFileObject(doc);
                WebModule wm = WebModule.getWebModule(fObject);
                if (wm != null){
                    JSFELExpression exp = new JSFELExpression(wm, (JspSyntaxSupport)bdoc.getSyntaxSupport());
                    int res = exp.parse(token.getOffset() + token.getImage().length());
                    if (res == JSFELExpression.EL_START)
                        res = exp.parse(token.getOffset() + token.getImage().length() + 1);
                    return res == JSFELExpression.EL_JSF_BEAN;
                }
            }
            
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
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
        
        try {
            BaseDocument bdoc = (BaseDocument) doc;
            JTextComponent target = Utilities.getFocusedComponent();
            
            if (target == null || target.getDocument() != bdoc)
                return null;
            
            SyntaxSupport sup = bdoc.getSyntaxSupport();
            JspSyntaxSupport jspSup = (JspSyntaxSupport)sup.get(JspSyntaxSupport.class);
            
            TokenItem token = jspSup.getTokenChain(offset, offset+1);
            if (token == null) return null;
            
            if (token.getTokenContextPath().contains(ELTokenContext.contextPath)){
                FileObject fObject = NbEditorUtilities.getFileObject(doc);
                WebModule wm = WebModule.getWebModule(fObject);
                if (wm != null){
                    JSFELExpression exp = new JSFELExpression(wm, (JspSyntaxSupport)bdoc.getSyntaxSupport());
                    int res = exp.parse(token.getOffset() + token.getImage().length());
                    if (res == JSFELExpression.EL_JSF_BEAN || res == JSFELExpression.EL_START )
                        return new int[] {token.getOffset(), token.getOffset() + token.getImage().length()};
                }
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
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
        TokenItem token = null;
        try {
            BaseDocument bdoc = (BaseDocument) doc;
            JTextComponent target = Utilities.getFocusedComponent();
            
            if (target == null || target.getDocument() != bdoc)
                return;
            
            SyntaxSupport sup = bdoc.getSyntaxSupport();
            JspSyntaxSupport jspSup = (JspSyntaxSupport)sup.get(JspSyntaxSupport.class);
            
            token = jspSup.getTokenChain(offset, offset+1);
            
            if (token == null) return;
            
            // is it a bean in EL
            if (token.getTokenContextPath().contains(ELTokenContext.contextPath)){
                FileObject fObject = NbEditorUtilities.getFileObject(doc);
                WebModule wm = WebModule.getWebModule(fObject);
                if (wm != null){
                    JSFELExpression exp = new JSFELExpression(wm, (JspSyntaxSupport)bdoc.getSyntaxSupport());
                    int res = exp.parse(token.getOffset() + token.getImage().length());
                    if (res == JSFELExpression.EL_START ){
                        (new OpenConfigFile(wm, token.getImage())).run();
                        return;
                    }
                    if (res == JSFELExpression.EL_JSF_BEAN){
                        //TODO: RETOUCHE
                        /*JavaClass bean = exp.getBean(exp.getExpression());
                        Object item = exp.getPropertyDeclaration(exp.getExpression(), bean);
                        if (item == null)
                            item = exp.getMethodDeclaration(exp.getExpression(), bean);
                        if (item != null){
                            Runnable run = new OpenJavaItem(item, sup);
                            JavaMetamodel.getManager().invokeAfterScanFinished(run, NbBundle.getMessage(JSPHyperlinkProvider.class, "MSG_goto-source"));
                        }
                         **/
                    }
                }
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
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
            
            FileObject config = JSFConfigUtilities.findFacesConfigForManagedBean(wm, beanName);
            if (config != null){
                DataObject dobj = null;
                try{
                    dobj = DataObject.find(config);
                } catch (DataObjectNotFoundException e){
                    ErrorManager.getDefault().notify(e);
                    return;
                }
                
                if (dobj != null){
                    final EditorCookie.Observable ec = (EditorCookie.Observable)dobj.getCookie(EditorCookie.Observable.class);
                    if (ec != null) {
                        StatusDisplayer.getDefault().setStatusText(/*NbBundle.getMessage(JMIUtils.class, "opening-element", element instanceof NamedElement ? ((NamedElement)element).getName() : "")*/"otvirani"); // NOI18N
                        Utilities.runInEventDispatchThread(new Runnable() {
                            public void run() {
                                JEditorPane[] panes = ec.getOpenedPanes();
                                if (panes != null && panes.length > 0) {
                                    openPane(panes[0], beanName);
                                    //ec.open();
                                } else {
                                    ec.addPropertyChangeListener(new PropertyChangeListener() {
                                        public void propertyChange(PropertyChangeEvent evt) {
                                            if (EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
                                                final JEditorPane[] panes = ec.getOpenedPanes();
                                                if (panes != null && panes.length > 0) 
                                                    openPane(panes[0], beanName);
                                                ec.removePropertyChangeListener(this);
                                            }
                                        }
                                    });
                                   // ec.open();
                                }
                            }
                        });
                        ec.open();
                    }
                }
            }
        }
        
        private void openPane(JEditorPane pane, String beanName){
            final Cursor editCursor = pane.getCursor();
            pane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            int[] definition = JSFEditorUtilities.getManagedBeanDefinition((BaseDocument)pane.getDocument(), beanName);
            if (definition [0] > -1)
                pane.setCaretPosition(definition[0]);
            pane.setCursor(editCursor);
            StatusDisplayer.getDefault().setStatusText(""); //NOI18N
        }
    }
    
    
    /* This thread open a java element in the editor
     */
    public static class OpenJavaItem implements Runnable{
        private Object item;
        private SyntaxSupport sup;
        
        OpenJavaItem(Object item, SyntaxSupport sup){
            super();
            this.item = item;
            this.sup = sup;
        }
        
        public void run() {
            //TODO: RETOUCHE
            /*JspJavaSyntaxSupport javaSup = (JspJavaSyntaxSupport)sup.get(JspJavaSyntaxSupport.class);
            if (item != null && javaSup != null) {
                String itemDesc = null;
                if ((itemDesc = javaSup.openSource(item, true)) != null){
                    String msg = NbBundle.getBundle(JSPHyperlinkProvider.class).getString("MSG_source_not_found");                            
                    org.openide.awt.StatusDisplayer.getDefault().setStatusText(msg);
                }
            }*/
        }
    }
}
