/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.webkit.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.netbeans.modules.css.editor.api.CssCslParserResult;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelVisitor;
import org.netbeans.modules.css.model.api.SelectorsGroup;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.visual.api.RuleEditorController;
import org.netbeans.modules.css.visual.api.RuleEditorTC;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.inspect.CSSUtils;
import org.netbeans.modules.web.inspect.PageInspectorImpl;
import org.netbeans.modules.web.inspect.PageModel;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.netbeans.modules.web.inspect.webkit.WebKitPageModel;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.css.Rule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.WindowManager;

/**
 * WebKit-based CSS Styles view.
 *
 * @author Jan Stola
 */
public class CSSStylesPanel extends JPanel {
    /** Action command for switching to document view. */
    static final String DOCUMENT_ACTION_COMMAND = "document"; // NOI18N
    /** Action command for switching to selection view. */
    static final String SELECTION_ACTION_COMMAND = "selection"; // NOI18N
    /** The default instance of this class. */
    private static final CSSStylesPanel DEFAULT = new CSSStylesPanel();
    /** Request processor used by this class. */
    private static final RequestProcessor RP = new RequestProcessor(CSSStylesPanel.class);
    /** Document section of CSS Styles view. */
    private CSSStylesDocumentPanel documentPanel = new CSSStylesDocumentPanel();
    /** Selection section of CSS Styles view. */
    private JPanel selectionPanel = new JPanel();
    /** The current inspected page. */
    WebKitPageModel pageModel;
    /** Lookup of this panel. */
    private CSSStylesLookup lookup = new CSSStylesLookup();
    /** Lookup result with rules selected in the panel. */
    Lookup.Result<Rule> ruleLookupResult;

    /**
     * Creates a new {@code CSSStylesPanel}.
     */
    private CSSStylesPanel() {
        setLayout(new BorderLayout());
        add(createToolbar(), BorderLayout.PAGE_START);
        PageInspectorImpl.getDefault().addPropertyChangeListener(getListener());
        updatePageModel();
        updateVisiblePanel(false);
        putClientProperty("lookup", lookup); // NOI18N
        ruleLookupResult = lookup.lookupResult(Rule.class);
        ruleLookupResult.addLookupListener(getListener());
    }

    /**
     * Creates the toolbar for this view.
     *
     * @return toolbar for this view.
     */
    private JToolBar createToolbar() {
        // The toolbar itself
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        // Button group for document and source buttons
        ButtonGroup buttonGroup = new ButtonGroup();

        // Document button
        JToggleButton documentButton = new JToggleButton("D"); // PENDING
        documentButton.setActionCommand(DOCUMENT_ACTION_COMMAND);
        initToolbarButton(documentButton, toolBar, buttonGroup);

        // Selection button
        JToggleButton selectionButton = new JToggleButton("S"); // PENDING
        selectionButton.setActionCommand(SELECTION_ACTION_COMMAND);
        initToolbarButton(selectionButton, toolBar, buttonGroup);
        selectionButton.setSelected(true);

        return toolBar;
    }

    /**
     * Initializes the specified toolbar button.
     *
     * @param button button to initialize.
     * @param toolBar toolbar where the button belongs to.
     * @param buttonGroup group where the button belongs to.
     */
    private void initToolbarButton(AbstractButton button, JToolBar toolBar, ButtonGroup buttonGroup) {
        button.setFocusPainted(false);
        button.addActionListener(getListener());
        buttonGroup.add(button);
        toolBar.add(button);
    }

    /**
     * Returns the default instance of this class.
     *
     * @return the default instance of this class.
     */
    public static CSSStylesPanel getDefault() {
        return DEFAULT;
    }

    /** Listener for various events this instance is interested in. */
    private Listener listener;
    /**
     * Returns the listener.
     *
     * @return the listener.
     */
    private Listener getListener() {
        if (listener == null) {
            listener = new Listener();
        }
        return listener;
    }

    /**
     * Updates the panel to match the currently inspected page.
     */
    final void updatePageModel() {
        if (pageModel != null) {
            pageModel.removePropertyChangeListener(listener);
        }
        PageModel page = PageInspectorImpl.getDefault().getPage();
        if (page instanceof WebKitPageModel) {
            pageModel = (WebKitPageModel)page;
        } else {
            pageModel = null;
        }
        if (pageModel != null) {
            pageModel.addPropertyChangeListener(listener);
        }
        updateContent();
    }

    /**
     * Switches the visible panel (either document or selection view).
     *
     * @param showDocumentPanel if {@code true} then the document view
     * should be shown, otherwise the selection view should be shown.
     */
    private void updateVisiblePanel(boolean showDocumentPanel) {
        boolean documentPanelVisible = (documentPanel.getParent() != null);
        if (documentPanelVisible != showDocumentPanel) {
            if (showDocumentPanel) {
                remove(selectionPanel);
                add(documentPanel, BorderLayout.CENTER);
                lookup.updateLookup(documentPanel.getLookup());
            } else {
                remove(documentPanel);
                add(selectionPanel, BorderLayout.CENTER);
                // PENDING
                // lookup.updateLookup(selectionPanel.getLookup());
            }
        }
        revalidate();
        repaint();
    }

    /**
     * Updates the content of this panel.
     */
    void updateContent() {
        WebKitDebugging webKit = (pageModel == null) ? null : pageModel.getWebKit();
        documentPanel.updateContent(webKit);
        //selectionPanel.updateContent(newDocument);
    }

    /**
     * Updates the rules editor window to show information about the selected rule.
     *
     * @param rules rules selected in this panel.
     */
    void updateRulesEditor(final Collection<? extends Rule> rules) {
        Resource resource = lookup.lookup(Resource.class);
        final String resourceName = (resource == null) ? null : resource.getName();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                RuleEditorTC ruleEditor = (RuleEditorTC)WindowManager.getDefault().findTopComponent(RuleEditorTC.ID);
                final RuleEditorController controller = ruleEditor.getRuleEditorController();
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean fileResource = (resourceName == null) ? false : resourceName.startsWith("file://"); // NOI18N
                        if (fileResource && rules.size() == 1) {
                            Rule rule = rules.iterator().next();
                            try {
                                URI uri = new URI(resourceName);
                                if ((uri.getAuthority() != null) || (uri.getFragment() != null) || (uri.getQuery() != null)) {
                                    uri = new URI(uri.getScheme(), null, uri.getPath(), null, null);
                                }
                                File file = new File(uri);
                                file = FileUtil.normalizeFile(file);
                                FileObject fob = FileUtil.toFileObject(file);
                                Source source = Source.create(fob);
                                ParserManager.parse(Collections.singleton(source), new RuleEditorTask(rule, controller));
                            } catch (URISyntaxException ex) {
                                Logger.getLogger(CSSStylesPanel.class.getName()).log(Level.INFO, null, ex);
                            } catch (ParseException ex) {
                                Logger.getLogger(CSSStylesPanel.class.getName()).log(Level.INFO, null, ex);
                            }
                        } else {
                            controller.setNoRuleState();
                        }
                    }
                });
            }
        });
    }

    /**
     * Listener for various events important for {@code CSSStylesPanel}.
     */
    class Listener implements ActionListener, PropertyChangeListener, LookupListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (DOCUMENT_ACTION_COMMAND.equals(command)) {
                updateVisiblePanel(true);
            } else if (SELECTION_ACTION_COMMAND.equals(command)) {
                updateVisiblePanel(false);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (PageInspectorImpl.PROP_MODEL.equals(propName)) {
                updatePageModel();
            } else if (PageModel.PROP_DOCUMENT.equals(propName)) {
                updateContent();
            }
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            Collection<? extends Rule> rules = ruleLookupResult.allInstances();
            updateRulesEditor(rules);
        }

    }

    /**
     * Lookup of {@code CSSStylesPanel}.
     */
    static class CSSStylesLookup extends ProxyLookup {
        protected final void updateLookup(Lookup lookup) {
            if (lookup == null) {
                setLookups();
            } else {
                setLookups(lookup);
            }
        }
    }

    /**
     * User task that updates the rules editor window (to show the specified rule).
     */
    static class RuleEditorTask extends UserTask {
        /** Rule to show in the rules editor. */
        private Rule rule;
        /** Controller of the rule editor where the rule should be shown. */
        private RuleEditorController controller;

        /**
         * Creates a new {@code RuleEditorTask}.
         *
         * @param rule rule to show in the rules editor.
         * @param controller controller of the rule editor where the rule
         * should be shown.
         */
        RuleEditorTask(Rule rule, RuleEditorController controller) {
            this.rule = rule;
            this.controller = controller;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            final String selector = CSSUtils.normalizeSelector(rule.getSelector());
            CssCslParserResult result = (CssCslParserResult)resultIterator.getParserResult();
            final Model sourceModel = result.getModel();
            final AtomicBoolean visitorCancelled = new AtomicBoolean();
            sourceModel.runReadTask(new Model.ModelTask() {

                @Override
                public void run(StyleSheet styleSheet) {
                    styleSheet.accept(new ModelVisitor.Adapter() {

                        @Override
                        public void visitRule(org.netbeans.modules.css.model.api.Rule modelRule) {
                            if (visitorCancelled.get()) {
                                return;
                            }
                            SelectorsGroup selectorGroup = modelRule.getSelectorsGroup();
                            CharSequence image = sourceModel.getElementSource(selectorGroup);
                            String selectorInFile = CSSUtils.normalizeSelector(image.toString());
                            if (selector.equals(selectorInFile)) {
                                //found
                                visitorCancelled.set(true);
                                controller.setModel(sourceModel);
                                controller.setRule(modelRule);
                            }
                        }

                    });
                    if (!visitorCancelled.get()) {
                        // Rule not found
                        controller.setNoRuleState();
                    }
                }

            });
        }

    }

}
