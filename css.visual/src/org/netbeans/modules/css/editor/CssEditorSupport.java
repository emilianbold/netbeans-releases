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

package org.netbeans.modules.css.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.css.loader.CssDataObject;
import org.netbeans.modules.css.model.CssModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import org.netbeans.modules.css.model.CssRule;
import org.netbeans.modules.css.model.CssRuleItem;
import org.netbeans.modules.css.visual.api.CssRuleContext;
import org.netbeans.modules.css.visual.ui.StyleBuilderTopComponent;
import org.netbeans.modules.css.visual.ui.preview.CSSTCController;
import org.netbeans.modules.css.visual.ui.preview.CssPreviewable;
import org.netbeans.modules.editor.NbEditorDocument;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;
import org.openide.windows.CloneableOpenSupport;


/**
 * Editor Support for document of type text/css
 *
 * @author Winston Prakash
 * @author Marek Fukala
 *
 * @version 1.0
 */
public class CssEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie,
        EditorCookie.Observable, PrintCookie, PropertyChangeListener, CssPreviewable {
    
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    private CSSTCController windowsController;
    
    private CssRule selected = null;
    
    private List<CssPreviewable.Listener> previewableListeners = new ArrayList<CssPreviewable.Listener>();
    
    private static final Logger LOGGER = Logger.getLogger(org.netbeans.modules.css.Utilities.VISUAL_EDITOR_LOGGER);
    
    private PropertyChangeListener CSS_STYLE_DATA_LISTENER = new PropertyChangeListener() {
        public void propertyChange(final PropertyChangeEvent evt) {
            final NbEditorDocument doc = (NbEditorDocument)getDocument();
            if(doc != null)
                doc.runAtomic(new Runnable() {
                    public void run() {
                        CssRuleItem oldRule = (CssRuleItem)evt.getOldValue();
                        CssRuleItem newRule = (CssRuleItem)evt.getNewValue();
                        
                        if(selected == null) {
                            throw new IllegalStateException("CssRuleContent event fired, but selected rule is null!");
                        }
                        
                        //remember the selected rule since it synchronously
                        //turns to null after each document modification
                        CssRule myRule = selected; 
                        
                        try {
                            if(oldRule != null && newRule == null) {
                                //remove the old rule line - maybe we should just cut the exact part?!?!
                                int offset = oldRule.key().offset();
                                int lineStart = Utilities.getRowStart(doc, offset);
                                
                                //do not remove the rule opening bracket if we are on it's line
                                int ruleOpenBracketOffset = myRule.getRuleOpenBracketOffset();
                                if(lineStart <= ruleOpenBracketOffset) {
                                    lineStart = ruleOpenBracketOffset + 1;
                                }
                                
                                int lineEnd = Utilities.getRowEnd(doc, offset) + LINE_SEPARATOR.length();
                                
                                //do not remove the rule closing bracket if we are on it's line
                                int ruleCloseBracketOffset = myRule.getRuleCloseBracketOffset();
                                if(lineEnd > ruleCloseBracketOffset) {
                                    lineEnd = ruleCloseBracketOffset;
                                }
                                
                                doc.remove(lineStart, lineEnd - lineStart);
                                
                            } else if(oldRule == null && newRule != null) {
                                //add the new rule at the end of the rule block:
                                List<CssRuleItem> items = myRule.ruleContent().ruleItems();
                                int offset = -1;
                                int ruleCloseBracketOffset = myRule.getRuleCloseBracketOffset();
                                
                                boolean increaseIndent = false;
                                if(items.isEmpty()) {
                                    //no item so far, lets generate the position from the rule
                                    //opening bracket
                                    offset = myRule.getRuleOpenBracketOffset();
                                    increaseIndent = true;
                                } else {
                                    //find latest rule and add the item behind
                                    CssRuleItem last = items.get(items.size() - 1);
                                    offset = last.key().offset();
                                    
                                    //check if the last item has semicolon
                                    //add it if there is no semicolon
                                    if(last.semicolonOffset() == -1) {
                                        doc.insertString(last.value().offset() + last.value().name().length(), ";", null); //NOI18N
                                        ruleCloseBracketOffset++; //we shifted the brace
                                    }
                                    
                                }
                                
                                int line = Utilities.getLineOffset(doc, offset);
                                int lineEnd = Utilities.getRowEnd(doc, offset);
                                
                                //check the case where the rule closing bracket is on the same line as the last item
                                // h1 { color: red; }
                                if(lineEnd > ruleCloseBracketOffset) {
                                    lineEnd = ruleCloseBracketOffset;
                                }
                                
                                int indent = Utilities.getRowIndent(doc, offset);
                                doc.insertString(lineEnd, LINE_SEPARATOR, null); //NOI18N
                                int new_line_start = Utilities.getRowStartFromLineOffset(doc, line + 1);
                                doc.getFormatter().changeRowIndent(doc, new_line_start, indent + (increaseIndent ? doc.getFormatter().getShiftWidth() : 0));
                                int insertOffset = Utilities.getRowEnd(doc, new_line_start);
                                
                                if(lineEnd == ruleCloseBracketOffset) {
                                    //the new item's line has rule close bracket at the end
                                    insertOffset--; //move before the '}' char
                                }
                                
                                doc.insertString(insertOffset, newRule.key().name() + ": " + newRule.value().name() + ";", null);
                                
                            } else if (oldRule != null && newRule != null) {
                                //update the existing rule in document
                                //replace attribute name
                                doc.remove(oldRule.key().offset(), oldRule.key().name().length());
                                doc.insertString(oldRule.key().offset(), newRule.key().name(), null);
                                //replace the attribute value
                                int diff = newRule.key().name().length() - oldRule.key().name().length();
                                doc.remove(oldRule.value().offset() + diff, oldRule.value().name().length());
                                doc.insertString(oldRule.value().offset() + diff, newRule.value().name(), null);
                                
                            } else {
                                //new rule and old rule is null
                                throw new IllegalArgumentException("Invalid PropertyChangeEvent - both old and new values are null!");
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
        }
    };
    
    private CaretListener CARET_LISTENER = new CaretListener() {
        public void caretUpdate(CaretEvent ce) {
            if(ce.getSource() instanceof JEditorPane){
                updateSelectedRule(ce.getDot());
            }
        }
    };
    
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
        windowsController = CSSTCController.getDefault();
        addPropertyChangeListener(this);
    }
    
    /**
     * Add the Save Cookie becuase the file is modified
     */
    protected boolean notifyModified() {
        if (!super.notifyModified()) return false;
        ((CssDataObject)getDataObject()).addSaveCookie(saveCookie);
        return true;
    }
    
    /**
     * Remove the Save Cookie becuase the file is saved
     */
    protected void notifyUnmodified() {
        super.notifyUnmodified();
        ((CssDataObject)getDataObject()).removeSaveCookie(saveCookie);
    }
    
    public void propertyChange(PropertyChangeEvent evt){
        if (evt.getPropertyName().equals(EditorCookie.Observable.PROP_OPENED_PANES)){
            JEditorPane[] panes = this.getOpenedPanes();
            if (panes != null){
                final JEditorPane activePane = panes[0];
                if(activePane != null){
                    Document document = activePane.getDocument();
                    if(document == null) {
                        //pane about to closed, document unloaded
                        return ;
                    }
                    //listen on the model and update selected rule
                    CssModel.get(document).addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            if(evt.getPropertyName().equals(CssModel.MODEL_UPDATED)) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        updateSelectedRule(activePane.getCaret().getDot());
                                        activePane.addCaretListener(CARET_LISTENER);
                                    }
                                });
                            } else {
                                //either MODEL_INVALID or MODEL_PARSING fired
                                final boolean invalid = evt.getPropertyName().equals(CssModel.MODEL_INVALID);
                                
                                //remove the CssStyleData listener to disallow StyleBuilder editing
                                //until the parser finishes parsing. If I do not do that, the parsed
                                //data from the CssModel are inaccurate and hence,
                                //when user uses StyleBuilder, the source may become broken.
                                if(selected != null) {
                                    selected.ruleContent().removePropertyChangeListener(CSS_STYLE_DATA_LISTENER);
                                    selected = null;
                                }
                                activePane.removeCaretListener(CARET_LISTENER);
                                
                                //disable editing on the StyleBuilder
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        if(invalid) {
                                            //model invalid - switch the stylebuilder UI to an error panel
                                            StyleBuilderTopComponent.findInstance().setPanelMode(StyleBuilderTopComponent.MODEL_ERROR);
                                            firePreviewableDeactivated();
                                        } else {
                                            //model is about the be updated - just disable the SB editing
                                            StyleBuilderTopComponent.findInstance().setPanelMode(StyleBuilderTopComponent.MODEL_UPDATING);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }
    
    void cssTCActivated(CssCloneableEditor editor) {
        //we need to refresh the StyleBuilder content when switching between more css files
        if(selected != null) {
            selected.ruleContent().removePropertyChangeListener(CSS_STYLE_DATA_LISTENER);
            selected = null;
        }
        updateSelectedRule(editor.getEditorPane().getCaret().getDot());
    }
    
    void cssTCDeactivated(CssCloneableEditor editor) {
    }
    
    private synchronized void updateSelectedRule(int dotPos) {
        Document document = getDocument();
        if(document == null) {
            //document unloaded, just return
            return ;
        }
        
        CssModel model = CssModel.get(document);
        
        LOGGER.log(Level.FINE, "updateSelectedRule(" + dotPos + ")");
        if(model.rules() == null) {
            return ;//css not parsed yet, we need to wait for a parser event
        }
        
        //find rule on the offset
        final CssRule selectedRule = model.ruleForOffset(dotPos);
        
        LOGGER.log(Level.FINE, selectedRule == null ? "NO rule" : "found a rule");
        
        if(selectedRule == null) {
            //remove the listeners from selected
            if(selected != null) {
                selected.ruleContent().removePropertyChangeListener(CSS_STYLE_DATA_LISTENER);
                //reset saved selected rule
                selected = null;
            }
            //show no selected rule panel
            StyleBuilderTopComponent.findInstance().setPanelMode(StyleBuilderTopComponent.OUT_OF_RULE);
            
            //disable preview
            firePreviewableDeactivated();
        } else {
            //something was selected
            
            if(selectedRule == selected) {
                return ; //trying to select already selected rule, ignore
            }
            
            //remove listener from the old rule
            if(selected != null) {
                selected.ruleContent().removePropertyChangeListener(CSS_STYLE_DATA_LISTENER);
            }
            selected = selectedRule;
            
            //listen on changes possibly made by the stylebuilder and update the document accordingly
            selectedRule.ruleContent().addPropertyChangeListener(CSS_STYLE_DATA_LISTENER);
            
            //TODO make activation of the selected rule consistent for StyleBuilder and CSSPreview,
            //now one uses direct call to TC, second property change listening on this class
            
            
            //update the css preview
            CssRuleContext content =
                    new CssRuleContext(selectedRule, model, document, getDataObject().getPrimaryFile());

            //activate the selected rule in stylebuilder
            StyleBuilderTopComponent sbTC = StyleBuilderTopComponent.findInstance();
            sbTC.setContent(content);
            sbTC.setPanelMode(StyleBuilderTopComponent.MODEL_OK);
            
            firePreviewableActivated(content);
        }
    }
    
    /** CssPreviewable implementation */
    public void addListener(Listener l) {
        previewableListeners.add(l);
    }
    
    public void removeListener(Listener l) {
        previewableListeners.remove(l);
    }
    
    public CssRuleContext content() {
        Document document = getDocument();
        if(document == null) {
            //already unloaded
            return null;
        }
        if(selected == null) {
            return null;
        } else {
            return new CssRuleContext(selected, CssModel.get(document), document, getDataObject().getPrimaryFile());
        }
    }
    
    private void firePreviewableActivated(CssRuleContext content) {
        for(CssPreviewable.Listener l : previewableListeners) {
            l.activate(content);
        }
    }
    
    private void firePreviewableDeactivated() {
        for(CssPreviewable.Listener l : previewableListeners) {
            l.deactivate();
        }
    }
    
    @Override
    protected CloneableEditor createCloneableEditor() {
        return new CssCloneableEditor(this);
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
            return cssDataObject.getCookie(CssEditorSupport.class);
            
        }
    }
    
}
