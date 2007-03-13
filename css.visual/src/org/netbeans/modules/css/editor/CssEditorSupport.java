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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * CssEditorSupport.java
 *
 * Created on December 8, 2004, 11:07 PM
 */

package org.netbeans.modules.css.editor;

import java.util.logging.Logger;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.css.loader.CssDataObject;
import org.netbeans.modules.css.visual.model.CssMetaModel;
import org.netbeans.modules.css.visual.model.CssStyleData;
import org.netbeans.modules.css.visual.parser.CssStyleParser;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.windows.CloneableOpenSupport;


/**
 * Editor Support for document of type text/css
 * @author Winston Prakash
 * @version 1.0
 */
public class CssEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie,
        EditorCookie.Observable, PrintCookie, PropertyChangeListener {
    
    private final SimpleAttributeSet ATTR_ADD = new SimpleAttributeSet();
    private final SimpleAttributeSet ATTR_REMOVE = new SimpleAttributeSet();
    
    CssDataObject cssDataObject = null;
    
    int currentOffset = -1;
    int currentLength = -1;
    int currentHighlightStart = -1;
    int currentHighlightEnd = -1;
    
    EditorCookie editorCookie = null;
    
    boolean highLighted = false;
    
    private CssCustomEditor cssCustomEditor = null;
    private CssCloneableEditor cssCloneableEditor = null;
    
    private JEditorPane activePane;
    
    /** Implements <code>SaveCookie</code> interface. */
    private final SaveCookie saveCookie = new SaveCookie() {
        public void save() throws IOException {
            CssEditorSupport.this.saveDocument();
            CssEditorSupport.this.getDataObject().setModified(false);
        }
    };
    
    /** Creates a new instance of CssEditorSupport */
    public CssEditorSupport(CssDataObject dataObject) {
        super(dataObject, new CssEnvironment(dataObject));
        cssDataObject = dataObject;
        addPropertyChangeListener(this);
        ATTR_ADD.addAttribute(StyleConstants.Background, Color.red);
        ATTR_REMOVE.addAttribute(StyleConstants.Background, Color.white);
    }
    
    /**
     * Add the Save Cookie becuase the file is modified
     */
    protected boolean notifyModified() {
        if (!super.notifyModified()) return false;
        cssDataObject.addSaveCookie(saveCookie);
        return true;
    }
    
    public void updateRules(){
        //        if(cssRuleNavigationSupport != null){
        //            cssRuleNavigationSupport.startRuleNavigationUpdate();
        //        }
    }
    
    /**
     * Remove the Save Cookie becuase the file is saved
     */
    protected void notifyUnmodified() {
        super.notifyUnmodified();
        cssDataObject.removeSaveCookie(saveCookie);
    }
    
    protected CloneableEditor createCloneableEditor() {
        return new CssCloneableEditor(this);
    }
    
    /**
     * Listen to the Editor Pane opened event and attach the Caret Lister
     * to the editor Pane. Strange the PropertyChangeEvent does not have the
     * JEditor Pane (expect to get is using PropertyChangeEvent.getNewValue())
     * Using workaround - ugly but works - Winston
     */
    public void propertyChange(PropertyChangeEvent evt){
        if (evt.getPropertyName().equals(EditorCookie.Observable.PROP_OPENED_PANES)){
            CssMetaModel.setDataObject(cssDataObject);
            //            cssSyntaxErrorSupport = new CssSyntaxErrorSupport(this);
            JEditorPane[] panes = this.getOpenedPanes();
            if (panes != null){
                activePane = panes[0];
                if(activePane != null){
                    cssCustomEditor = getCssCustomEditor(activePane);
                    cssCloneableEditor = getCssCloneableEditor(activePane);
                    if(cssCustomEditor != null) {
                        cssCustomEditor.setDataObject(cssDataObject);
                    }else{
                        System.out.println("CssEditorSupport.propertyChange - Warning! CSS Custome Editor Can not be null. Check! - Winston");
                    }
                    if(cssCloneableEditor != null) {
                        cssCloneableEditor.requestActive();
                        cssCloneableEditor.setDataObject(cssDataObject);
                    }else{
                        System.out.println("CssEditorSupport.propertyChange - Warning! CSS Cloneable Editor Can not be null. Check! - Winston");
                    }
                    
                    //                    cssRuleNavigationSupport = new CssRuleNavigationSupport(activePane);
                    activePane.addCaretListener(new CaretListener() {
                        public void caretUpdate(CaretEvent ce) {
                            if(ce.getSource() instanceof JEditorPane){
                                JEditorPane edPane = (JEditorPane)ce.getSource();
                                setActiveNode(edPane.getDocument(), ce.getDot(), false);
                            }
                        }
                    });
                    
                    Document doc = activePane.getDocument();
                    //                                        syntaxSupport = new CssSyntaxSupport((BaseDocument)doc);
                    
                    doc.addDocumentListener(new DocumentListener(){
                        public void insertUpdate(DocumentEvent de){
                            setActiveNode(de.getDocument(), activePane.getCaret().getDot(), true);
                            //                                                cssSyntaxErrorSupport.startSyntaxCheck();
                            //                            cssRuleNavigationSupport.startRuleNavigationUpdate();
                        }
                        public void removeUpdate(DocumentEvent de){
                            setActiveNode(de.getDocument(), activePane.getCaret().getDot(), true);
                            //                                                cssSyntaxErrorSupport.startSyntaxCheck();
                            //                            cssRuleNavigationSupport.startRuleNavigationUpdate();
                        }
                        public void changedUpdate(DocumentEvent de){
                        }
                    });
                    
                    // Find the first rule block and set the active node.
                    try{
                        if(doc.getLength() > 0){
                            boolean searchDone = false;
                            String searchText;
                            int searchPos = 0;
                            do{
                                searchText = doc.getText(searchPos, 1);
                                if (searchText.equals("{")){
                                    if(!isInComment(doc, searchPos)) {
                                        break;
                                    }
                                    //                                    TokenItem ti = syntaxSupport.getTokenChain(searchPos, searchPos+1);
                                    //                                    if((ti.getTokenID() != null) && (ti.getTokenID() != CssTokenContext.COMMENT)){
                                    //                                        break;
                                    //                                    }
                                }
                                searchPos++;
                                if(searchPos > doc.getLength()) break;
                            }while(!searchDone);
                            
                            if(doc.getLength() > (searchPos + 1)){
                                activePane.getCaret().setDot(searchPos + 2);
                            }
                        }
                    }catch(BadLocationException ble){
                        
                    }
                }
            }
        }
    }
    
    private boolean isInComment(Document doc, int dotPos) {
        //find css language
        Language cssl = Language.find("text/x-css");
        if(cssl != null) {
            //find css comment token id
            TokenId cssCommentTI = cssl.tokenId("css_comment");
            if(cssCommentTI != null) {
                
                TokenHierarchy th = TokenHierarchy.get(doc);
                TokenSequence ts = th.tokenSequence();
                ts.move(dotPos);
                if(ts.moveNext()) {
                    if(ts.token().id() == cssCommentTI) {
                        return true; //do not activate node in comment
                    }
                }
            } else {
                Logger.getLogger(this.getClass().getName()).info("No text/x-css language doesn't contain 'css_comment' token ID!");
            }
        } else {
            Logger.getLogger(this.getClass().getName()).info("No text/x-css language found!");
        }
        return false;
    }
    
    /**
     * Get the CSS properties of the selected selector, create a CSS class node
     * and set it as active node.
     */
    private void setActiveNode(final Document doc, int dotPos, boolean reparse){
        //        try{
        if(isInComment(doc, dotPos)) {
            return ;
        }
        
        //            TokenItem ti = syntaxSupport.getTokenChain(dotPos, dotPos+1);
        //            if((ti != null) && (ti.getTokenID() != null) && (ti.getTokenID() == CssTokenContext.COMMENT)){
        //                return;
        //            }
        //        }catch (Exception exc){
        //            exc.printStackTrace();
        //        }
        int initialPos = -1;
        int lastPos = -1;
        int searchPos = -1;
        if((currentOffset != -1) && (currentLength != -1) && !reparse){
            if ((dotPos >= currentOffset) && (dotPos <= (currentOffset + currentLength))){
                highlight((StyledDocument)doc, currentOffset, currentLength);
                return;
            }
        }
        try {
            searchPos = (dotPos -1) < 0 ? 0 : dotPos -1;
            String txtBefore = doc.getText(searchPos, 1);
            while(!txtBefore.equals("{")){
                searchPos--;
                if((searchPos < 0) || txtBefore.equals("}")) break;
                txtBefore = doc.getText(searchPos, 1);
            }
            if(txtBefore.equals("{")){
                initialPos = searchPos;
                searchPos = (dotPos -1) < 0 ? 0 : dotPos -1;
                String txtAfter = doc.getText(searchPos, 1);
                while(!txtAfter.equals("}")){
                    searchPos++;
                    if((searchPos > doc.getLength()) || txtAfter.equals("{")) break;
                    txtAfter = doc.getText(searchPos, 1);
                }
                if(txtAfter.equals("}")){
                    lastPos = searchPos;
                }
            }
            if( (initialPos > -1) && (lastPos > -1) ){
                currentOffset = initialPos + 1;
                currentLength = lastPos - initialPos -1;
                highlight((StyledDocument)doc, currentOffset, currentLength);
                String cssProperties = doc.getText(currentOffset, currentLength);
                
                // XXX This is silly. CssSelectorNode node is created all the time.
                // Create and set as active node only actual style values changes.
                CssStyleParser cssStyleParser = new CssStyleParser();
                final CssStyleData cssStyleData = cssStyleParser.parse(cssProperties.trim());
                cssStyleData.addPropertyChangeListener(new PropertyChangeListener(){
                    public void propertyChange(PropertyChangeEvent evt){
                        replaceText((BaseDocument)doc, cssStyleData.getFormattedString());
                    }
                });
                if(cssCustomEditor != null) {
                    cssCustomEditor.setCssStyleData(cssStyleData);
                }else{
                    System.out.println("CssEditorSupport.setActiveNode - Warning! CSS Custome Editor Can not be null. Check! - Winston"); //NOI18N
                }
                
                if(cssCloneableEditor != null) {
                    cssCloneableEditor.setCssStyleData(cssStyleData);
                }else{
                    System.out.println("CssEditorSupport.setActiveNode - Warning! CSS Cloneable Editor Can not be null. Check! - Winston"); //NOI18N
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Highlight the selected CSS Rule
     */
    private void highlight(StyledDocument doc, int offset, int length){
        int initialPos = offset - 1;
        int lastPos = length + initialPos + 1;
        int firstLine = NbDocument.findLineNumber(doc,initialPos);
        int lastLine = NbDocument.findLineNumber(doc,lastPos);
        // Change highlight only if highlight region (CSS selector) has changed
        if((firstLine != currentHighlightStart) || (lastLine != currentHighlightEnd)){
            if (highLighted && (currentHighlightStart != -1) && (currentHighlightEnd != -1)){
                mark(doc, currentHighlightStart, currentHighlightEnd, false);
            }
            mark(doc, firstLine, lastLine, true);
            highLighted = true;
            
            // Find the rule name and set it as selected rule to the meta model
            initialPos = -1;
            lastPos = -1;
            try {
                int searchPos = offset - 1;
                String txtBefore = doc.getText(searchPos, 1);
                while(!txtBefore.equals("{")){
                    searchPos--;
                    if((searchPos < 0) || txtBefore.equals("}")) break;
                    txtBefore = doc.getText(searchPos, 1);
                }
                if(txtBefore.equals("{")){
                    lastPos = searchPos - 1;
                    while(!txtBefore.equals("}")){
                        searchPos--;
                        if((searchPos < 0) || txtBefore.equals("}") || txtBefore.equals("/")) break;
                        txtBefore = doc.getText(searchPos, 1);
                    }
                    if(txtBefore.equals("}") || txtBefore.equals("/")){
                        initialPos = searchPos + 2;
                    }else{
                        initialPos = searchPos + 1;
                    }
                }
                
                if ((initialPos != -1) && (lastPos != -1)){
                    String ruleName = doc.getText(initialPos, lastPos - initialPos);
                    CssMetaModel.getInstance().setSelectedRuleName(ruleName.trim());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentHighlightStart = firstLine;
            currentHighlightEnd = lastLine;
        }
    }
    
    /**
     * Mark/Unmark the lines from startLine to endLine in the Styled Document
     */
    public void mark(StyledDocument doc, int startLine, int endLine, boolean mark){
        if ((startLine < 0) || (endLine < 0)) return;
        int lastDoctLine = NbDocument.findLineNumber(doc, doc.getLength());
        if ((startLine > lastDoctLine) || (endLine > lastDoctLine)) return;
        for(int i=startLine; i <=  endLine; i++){
            int start = NbDocument.findLineOffset(doc, i);
            if (mark){
                Style bp = doc.getStyle(NbDocument.CURRENT_STYLE_NAME);
                if (bp == null) {
                    bp = doc.addStyle(NbDocument.CURRENT_STYLE_NAME, null);
                    bp.addAttribute(StyleConstants.ColorConstants.Background, new Color(225,236,247));
                }
                doc.setLogicalStyle(start, bp);
            }else{
                Style st = doc.getStyle(NbDocument.NORMAL_STYLE_NAME);
                if (st == null){
                    st = doc.addStyle(NbDocument.NORMAL_STYLE_NAME, null);
                }
                doc.setLogicalStyle(start, st);
            }
        }
    }
    
    /**
     * Replace the selector text
     */
    boolean replaceText(BaseDocument doc, String text) {
        int dotPos = activePane.getCaret().getDot();
        doc.atomicLock();
        try {
            if (highLighted){
                mark((StyledDocument)doc, currentHighlightStart, currentHighlightEnd, false);
                highLighted = false;
                currentHighlightStart = -1;
                currentHighlightEnd = -1;
            }
            doc.remove(currentOffset, currentLength);
            doc.insertString(currentOffset, text, null);
            currentLength = text.length();
            activePane.getCaret().setDot(currentOffset + 1);
        } catch( BadLocationException exc ) {
            return false;
        } finally {
            doc.atomicUnlock();
        }
        return true;
    }
    
    /**
     * Find the CSS cloneable Editor
     */
    private CssCloneableEditor getCssCloneableEditor(JEditorPane activePane) {
        CssCloneableEditor cssCloneableEditor = (CssCloneableEditor)SwingUtilities.getAncestorOfClass(CssCloneableEditor.class, activePane);
        return cssCloneableEditor;
    }
    
    /**
     * Find the Css Custom Editor
     */
    private CssCustomEditor getCssCustomEditor(JEditorPane activePane) {
        CssCustomEditor cssCustomEditor = (CssCustomEditor)SwingUtilities.getAncestorOfClass(CssCustomEditor.class, activePane);
        return cssCustomEditor;
    }
    
    /**
     * Environment that connects the CSS data object and the EditorSupport
     */
    private static class CssEnvironment extends DataEditorSupport.Env {
        CssDataObject cssDataObject = null;
        
        public CssEnvironment(CssDataObject dataObject) {
            super(dataObject);
            cssDataObject = dataObject;
        }
        
        protected FileObject getFile() {
            return cssDataObject.getPrimaryFile();
        }
        
        protected FileLock takeLock() throws IOException {
            return cssDataObject.getPrimaryEntry().takeLock();
        }
        
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (CssEditorSupport)cssDataObject.getCookie(CssEditorSupport.class);
            
        }
    }
}
