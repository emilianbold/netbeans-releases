/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.css.visual;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.css.editor.api.CssCslParserResult;
import org.netbeans.modules.css.lib.api.model.Stylesheet;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.css.lib.api.model.Rule;
import org.netbeans.modules.css.lib.api.model.Declaration;
import org.netbeans.modules.css.visual.api.CssRuleContext;
import org.netbeans.modules.css.visual.api.StyleBuilderTopComponent;
import org.netbeans.modules.css.visual.ui.preview.CssPreviewTopComponent;
import org.netbeans.modules.css.visual.ui.preview.CssPreviewable;
import org.netbeans.modules.css.visual.ui.preview.CssPreviewable.Listener;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.openide.filesystems.FileUtil;

/**
 * Editor Support for document of type text/css
 *
 * @author Winston Prakash
 * @author Marek Fukala
 *
 * @version 1.0
 */
public class CssEditorSupport {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private CssRuleContent selected = null;
    private Document document = null;
    private List<CssPreviewable.Listener> previewableListeners = new ArrayList<CssPreviewable.Listener>();
    private static final Logger LOGGER = Logger.getLogger(org.netbeans.modules.css.visual.Utilities.VISUAL_EDITOR_LOGGER);
    private static final CssEditorSupport INSTANCE = new CssEditorSupport();
    private static final boolean DEBUG = Boolean.getBoolean("issue_129209_debug");

    //current values
    private CssCslParserResult result;
    private Stylesheet model;
    
    public static synchronized CssEditorSupport getDefault() {
        return INSTANCE;
    }
    private PropertyChangeListener CSS_STYLE_DATA_LISTENER = new PropertyChangeListener() {

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            //detach myself from the source so next UI changes are not propagated to the 
            //document until the parser finishes. Then new listener will be added
            if (selected != null && !aggregated_events) {
                d("css style data listener - detachinf from rule content.");
                selected.removePropertyChangeListener(CSS_STYLE_DATA_LISTENER);
            }

            final NbEditorDocument doc = (NbEditorDocument) document;
            if (doc != null) {
                doc.runAtomic(new Runnable() {

                    @Override
                    public void run() {
                        Declaration oldRule = (Declaration) evt.getOldValue();
                        Declaration newRule = (Declaration) evt.getNewValue();

                        if (selected == null) {
                            throw new IllegalStateException("CssRuleContent event fired, but selected rule is null!");
                        }

                        //remember the selected rule since it synchronously
                        //turns to null after each document modification
                        Rule myRule = selected.rule();

                        try {
                            if (oldRule != null && newRule == null) {
                                //remove the old rule line - maybe we should just cut the exact part?!?!
                                int start = oldRule.getProperty().offset();
                                int end = oldRule.getValue().offset() + oldRule.getValue().name().length();

                                //cut off also the semicolon if there is any
                                end = oldRule.semicolonOffset() != -1 ? oldRule.semicolonOffset() + 1 : end;

                                doc.remove(start, end - start);

                                //check if the line is empty and possibly remove it
                                if (Utilities.isRowWhite(doc, start)) {
                                    int lineStart = Utilities.getRowStart(doc, start);
                                    int lineOffset = Utilities.getLineOffset(doc, start);
                                    int nextLineStart = Utilities.getRowStartFromLineOffset(doc, lineOffset + 1);

                                    doc.remove(lineStart, nextLineStart - lineStart);
                                }

                            } else if (oldRule == null && newRule != null) {
                                //add the new rule at the end of the rule block:
                                List<Declaration> items = myRule.items();
                                final int INDENT = IndentUtils.indentLevelSize(document);
                                int insertOffset = myRule.getRuleCloseBracketOffset();

                                boolean initialNewLine = false;
                                if (!items.isEmpty()) {
                                    //find latest rule and add the item behind
                                    Declaration last = items.get(items.size() - 1); 

                                    //check if the last item has semicolon
                                    //add it if there is no semicolon
                                    if (last.semicolonOffset() == -1) {
                                        doc.insertString(last.getValue().offset() + last.getValue().name().trim().length(), ";", null); //NOI18N
                                        insertOffset++; //shift the insert offset because of the added semicolon
                                    }

                                    initialNewLine = Utilities.getLineOffset(doc, myRule.getRuleCloseBracketOffset()) == Utilities.getLineOffset(doc, last.getProperty().offset());
                                } else {
                                    initialNewLine = Utilities.getLineOffset(doc, myRule.getRuleCloseBracketOffset()) == Utilities.getLineOffset(doc, myRule.getRuleOpenBracketOffset());
                                }

                                String text = (initialNewLine ? LINE_SEPARATOR : "") +
                                        IndentUtils.createIndentString(document, INDENT) +
                                        newRule.getProperty().name() + ": " + newRule.getValue().name() + ";" +
                                        LINE_SEPARATOR;

                                doc.insertString(insertOffset, text, null);

                            } else if (oldRule != null && newRule != null) {
                                //update the existing rule in document
                                //replace attribute name
                                doc.remove(oldRule.getProperty().offset(), oldRule.getProperty().name().length());
                                doc.insertString(oldRule.getProperty().offset(), newRule.getProperty().name(), null);
                                //replace the attribute value
                                int diff = newRule.getProperty().name().length() - oldRule.getProperty().name().length();
                                doc.remove(oldRule.getValue().offset() + diff, oldRule.getValue().name().length());
                                doc.insertString(oldRule.getValue().offset() + diff, newRule.getValue().name(), null);

                            } else {
                                //new rule and old rule is null
                                throw new IllegalArgumentException("Invalid PropertyChangeEvent - both old and new values are null!");
                            }
                        } catch (Throwable e) {
                            //ignore
                        }
                    }
                });
            }
        }
    };


    void parsed(final CssCslParserResult result, final int caretOffset) {
        d("model updated");

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                d("model updated from AWT");
                updateSelectedRule(result, caretOffset);
            }
        });
    }

    void parsedWithError(CssCslParserResult result) {
        d("model invalid");
        //disable editing on the StyleBuilder
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                //remove the CssStyleData listener to disallow StyleBuilder editing
                //until the parser finishes parsing. If I do not do that, the parsed
                //data from the CssModel are inaccurate and hence,
                //when user uses StyleBuilder, the source may become broken.
                if (selected != null) {
                    selected.removePropertyChangeListener(CSS_STYLE_DATA_LISTENER);
                    d("removed css style data listener from " + selected);
                    selected = null;
                }

                StyleBuilderTopComponent.findInstance().setPanelMode(StyleBuilderTopComponent.MODEL_ERROR);
                firePreviewableDeactivated();
            }
        });
    }

    private synchronized void updateSelectedRule(CssCslParserResult result, int dotPos) {
        LOGGER.log(Level.FINE, "updateSelectedRule({0})", dotPos);

        if(this.result != result) {
            //parser result changed, need to rebuild model
            model = Stylesheet.create(result.getWrappedCssParserResult());
            this.result = result;
        }

        //find rule on the offset
        Rule selectedRule = model.ruleForOffset(dotPos);

        LOGGER.log(Level.FINE, selectedRule == null ? "NO rule" : "found a rule");

        d("selected rule:" + selectedRule);

        if (selectedRule == null) {
            //remove the listeners from selected
            if (selected != null) {
                selected.removePropertyChangeListener(CSS_STYLE_DATA_LISTENER);
                d("no selected rule, removing css style data listener");
                //reset saved selected rule
                selected = null;
            }
            //show no selected rule panel
            StyleBuilderTopComponent.findInstance().setPanelMode(StyleBuilderTopComponent.OUT_OF_RULE);

            //disable preview
            firePreviewableDeactivated();
        } else {
            //something was selected

            //remove listener from the old rule
            if (selected != null) {
                if (selectedRule.equals(selected.rule())) {
                    d("already selected rule selected, exiting");
                    return; //trying to select already selected rule, ignore
                }
                //else
                selected.removePropertyChangeListener(CSS_STYLE_DATA_LISTENER);
                d("removed css style data listener from previous rule: " + selected);
            }
            selected = CssRuleContent.create(selectedRule);
            document = result.getSnapshot().getSource().getDocument(false);

            //TODO make activation of the selected rule consistent for StyleBuilder and CSSPreview,
            //now one uses direct call to TC, second property change listening on this class

            //update the css preview
            CssRuleContext context =
                    new CssRuleContext(selected,
                    model,
                    document,
                    FileUtil.toFile(result.getSnapshot().getSource().getFileObject()));

            //activate the selected rule in stylebuilder
            StyleBuilderTopComponent sbTC = StyleBuilderTopComponent.findInstance();
            sbTC.setContent(context);
            sbTC.setPanelMode(StyleBuilderTopComponent.MODEL_OK);
            d("stylebuilder UI updated");

            //listen on changes possibly made by the stylebuilder and update the document accordingly
            selected.addPropertyChangeListener(CSS_STYLE_DATA_LISTENER);
            d("added property change listener to the new rule: " + selected);

            firePreviewableActivated(context);
        }
        d("updateselected rule exit");
    }

    /** CssPreviewable implementation */
    public void addListener(Listener l) {
        previewableListeners.add(l);
    }

    public void removeListener(Listener l) {
        previewableListeners.remove(l);
    }

    private void firePreviewableActivated(CssRuleContext content) {
        CssPreviewTopComponent.findInstance().activate(content);
    }

    private void firePreviewableDeactivated() {
        CssPreviewTopComponent.findInstance().deactivate();
    }

    private void d(String s) {
        if (DEBUG) { //should be if(DEBUG) { d("") } but will be commented out later
            LOGGER.log(Level.INFO, s);
        }
    }
    // >>> #149518 hack
    private boolean aggregated_events = false;
    //called from EDT

    public void firstAggregatedEventWillFire() {
        aggregated_events = true;
        d("firstAggregatedEventWillFire");
    }

    //called from EDT
    public void lastAggregatedEventFired() {
        aggregated_events = false;
        //remove the listener here since normally for single event the detaching
        //is done in the event handler
        if (selected != null) {
            d("lastAggregatedEventFired: css style data listener - detaching from rule content.");
            selected.removePropertyChangeListener(CSS_STYLE_DATA_LISTENER);
        }
    }
    //<<< eof hack
}
