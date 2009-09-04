/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.ui;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.ui.views.VariablesViewButtons;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.spi.viewmodel.Models;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Daniel Prusa
 */
public class CodeEvaluator extends TopComponent implements HelpCtx.Provider,
    DocumentListener, KeyListener, PropertyChangeListener {

    /** unique ID of <code>TopComponent</code> (singleton) */
    private static final String ID = "evaluator"; //NOI18N
    private static final String PROP_RESULT_CHANGED = "resultChanged"; // NOI18N

    final private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private static WeakReference<CodeEvaluator> instanceRef;

    private JEditorPane codePane;
    private History history;
    private Reference<JPDADebugger> debuggerRef = new WeakReference(null);
    private DbgManagerListener dbgManagerListener;
    private TopComponent resultView;
    private Set<String> editItemsSet = new HashSet<String>();
    private ArrayList<String> editItemsList = new ArrayList<String>();
    private JButton dropDownButton;

    private Preferences preferences = NbPreferences.forModule(ContextProvider.class).node(VariablesViewButtons.PREFERENCES_NAME);

    private HistoryRecord lastEvaluationRecord = null;
    private Variable result;
    private RequestProcessor.Task evalTask =
            new RequestProcessor("Debugger Evaluator", 1).  // NOI18N
            create(new EvaluateTask());


    /** Creates new form CodeEvaluator */
    public CodeEvaluator() {
        initComponents();
        codePane = new JEditorPane();
        codePane.setMinimumSize(new Dimension(0,0));
        history = new History();

        rightPanel.setPreferredSize(new Dimension(evaluateButton.getPreferredSize().width + 6, 0));

        dropDownButton = createDropDownButton();
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
        rightPanel.add(dropDownButton, gridBagConstraints);
        setupContext();
        editorScrollPane.setViewportView(codePane);
        codePane.addKeyListener(this);
        dbgManagerListener = new DbgManagerListener (this);
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                DebuggerManager.PROP_CURRENT_SESSION,
                dbgManagerListener
        );
        checkDebuggerState();
    }

    public void pasteExpression(String expr) {
        codePane.setText(expr);
        if (!isOpened()) {
            open();
        }
        requestActive();
    }

    private JButton createDropDownButton() {
        Icon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/jpda/resources/drop_down_arrow.png", false);
        final JButton button = new DropDownButton();
        button.setIcon(icon);
        String tooltipText = NbBundle.getMessage(CodeEvaluator.class, "CTL_Expressions_Dropdown_tooltip");
        button.setToolTipText(tooltipText);
        button.setEnabled(false);
        Dimension size = new Dimension(icon.getIconWidth() + 3, icon.getIconHeight() + 2);
        button.setPreferredSize(size);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setFocusable(false);
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if ("pressed".equals(e.getActionCommand())) {
                    JComponent jc = (JComponent) e.getSource();
                    Point p = new Point(jc.getWidth(), jc.getHeight());
                    SwingUtilities.convertPointToScreen(p, jc);
                    if (!ButtonPopupSwitcher.isShown()) {
                        SwitcherTableItem[] items = createSwitcherItems();
                        ButtonPopupSwitcher.selectItem(jc, items, p.x, p.y);
                    }
                    //Other portion of issue 37487, looks funny if the
                    //button becomes pressed
                    if (jc instanceof AbstractButton) {
                        AbstractButton jb = (AbstractButton) jc;
                        jb.getModel().setPressed(false);
                        jb.getModel().setRollover(false);
                        jb.getModel().setArmed(false);
                        jb.repaint();
                    }
                }
            } // actionPerformed

            @Override
            public boolean isEnabled() {
                return !editItemsList.isEmpty();
            }

        };
        action.putValue(Action.SMALL_ICON, icon);
        action.putValue(Action.SHORT_DESCRIPTION, tooltipText);
        button.setAction(action);
        return button;
    }

    private Timer setupContextTimer;

    private void setupContext() {
        if (setupContextTimer == null) {
            setupContextTimer = new Timer(500, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setupContextLazily();
                }
            });
            setupContextTimer.setRepeats(false);
        }
        // Setting up a context takes time.
        setupContextTimer.restart();
    }

    private void setupContextLazily() {
        final String text = codePane.getText();
        final Document[] documentPtr = new Document[] { null };
        ActionListener contextUpdated = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (codePane.getDocument() != documentPtr[0]) {
                    codePane.getDocument().addDocumentListener(CodeEvaluator.this);
                    if (text != null) {
                        codePane.setText(text);
                    }
                }
            }
        };
        WatchPanel.setupContext(codePane, contextUpdated);
        codePane.getDocument().addDocumentListener(this);
        if (text != null) {
            codePane.setText(text);
        }
        documentPtr[0] = codePane.getDocument();
    }

    private SwitcherTableItem[] createSwitcherItems() {
        SwitcherTableItem[] items = new SwitcherTableItem[editItemsList.size()];
        int x = 0;
        for (String item : editItemsList) {
            items[x++] = new SwitcherTableItem(new MenuItemActivatable(item), item);
        }
        return items;
    }

    public void recomputeDropDownItems() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (String str : editItemsList) {
                    StringTokenizer tok = new StringTokenizer(str, "\n"); // NOI18N
                    String dispName = "";
                    while (dispName.trim().length() == 0 && tok.hasMoreTokens()) {
                        dispName = tok.nextToken();
                    }
                }
                dropDownButton.setEnabled(!editItemsList.isEmpty());
            }
        });
    }

    public static synchronized CodeEvaluator getInstance() {
        CodeEvaluator instance = (CodeEvaluator) WindowManager.getDefault().findTopComponent(ID);
        if (instance == null) {
            instance = new CodeEvaluator();
        }
        return instance;
    }

    private static CodeEvaluator getDefaultInstance() {
        CodeEvaluator evaluator = instanceRef != null ? instanceRef.get() : null;
        if (evaluator != null) {
            return evaluator;
        }
        final CodeEvaluator result[] = new CodeEvaluator[1];
        if (SwingUtilities.isEventDispatchThread()) {
            result[0] = getInstance();
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        result[0] = getInstance();
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        instanceRef = new WeakReference(result[0]);
        return result[0];
    }

    public static ArrayList<History.Item> getHistory() {
        CodeEvaluator defaultInstance = getDefaultInstance();
        return defaultInstance != null ? defaultInstance.history.getItems() : new ArrayList<History.Item>();
    }

    public static Variable getResult() {
        CodeEvaluator defaultInstance = getDefaultInstance();
        return defaultInstance != null ? defaultInstance.result : null;
    }

    public static String getExpressionText() {
        CodeEvaluator defaultInstance = getDefaultInstance();
        return defaultInstance != null ? defaultInstance.getExpression() : ""; // NOI18N
    }

    public static void addResultListener(final PropertyChangeListener listener) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                CodeEvaluator defaultInstance = getDefaultInstance();
                if (defaultInstance != null) {
                    synchronized(defaultInstance.pcs) {
                        defaultInstance.pcs.addPropertyChangeListener(listener);
                    }
                }
            }
        });
    }

    public static void removeResultListener(final PropertyChangeListener listener) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                CodeEvaluator defaultInstance = getDefaultInstance();
                if (defaultInstance != null) {
                    synchronized(defaultInstance.pcs) {
                        defaultInstance.pcs.removePropertyChangeListener(listener);
                    }
                }
            }
        });
    }

    private static void fireResultChange() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                CodeEvaluator defaultInstance = getDefaultInstance();
                if (defaultInstance != null) {
                    synchronized (defaultInstance.pcs) {
                        defaultInstance.pcs.firePropertyChange(PROP_RESULT_CHANGED, null, null);
                    }
                }
            }
        });
    }

    private synchronized void checkDebuggerState() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine();
                JPDADebugger debugger = null;
                if (de != null) {
                    debugger = de.lookupFirst(null, JPDADebugger.class);
                }
                JPDADebugger lastDebugger = debuggerRef.get();
                if (lastDebugger != null && debugger != lastDebugger) {
                    lastDebugger.removePropertyChangeListener(JPDADebugger.PROP_CURRENT_THREAD, CodeEvaluator.this);
                    lastDebugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, CodeEvaluator.this);
                    debuggerRef = new WeakReference(null);
                    displayResult(null);
                }
                if (debugger != null) {
                    debuggerRef = new WeakReference(debugger);
                    debugger.addPropertyChangeListener(JPDADebugger.PROP_CURRENT_THREAD, CodeEvaluator.this);
                    debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, CodeEvaluator.this);
                } else {
                    history.clear();
                }
                computeEvaluationButtonState();
            }
        });
    }

    private void computeEvaluationButtonState() {
        JPDADebugger debugger = debuggerRef.get();
        boolean isEnabled = debugger != null && debugger.getCurrentThread() != null &&
                debugger.getState() == JPDADebugger.STATE_STOPPED && codePane.getDocument().getLength() > 0 &&
                editorScrollPane.getViewport().getView() == codePane;
        evaluateButton.setEnabled(isEnabled);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        DebuggerManager.getDebuggerManager().removeDebuggerListener(
                DebuggerManager.PROP_CURRENT_ENGINE,
                dbgManagerListener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        editorScrollPane = new javax.swing.JScrollPane();
        separatorPanel = new javax.swing.JPanel();
        rightPanel = new javax.swing.JPanel();
        evaluateButton = new javax.swing.JButton();
        emptyPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        editorScrollPane.setBorder(null);
        editorScrollPane.setMinimumSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(editorScrollPane, gridBagConstraints);

        separatorPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Separator.foreground"));
        separatorPanel.setMaximumSize(new java.awt.Dimension(1, 32767));
        separatorPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        separatorPanel.setPreferredSize(new java.awt.Dimension(1, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        add(separatorPanel, gridBagConstraints);

        rightPanel.setPreferredSize(new java.awt.Dimension(48, 0));
        rightPanel.setLayout(new java.awt.GridBagLayout());

        evaluateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/debugger/jpda/resources/evaluate.png"))); // NOI18N
        evaluateButton.setText(org.openide.util.NbBundle.getMessage(CodeEvaluator.class, "CodeEvaluator.evaluateButton.text")); // NOI18N
        evaluateButton.setToolTipText(org.openide.util.NbBundle.getMessage(CodeEvaluator.class, "HINT_Evaluate_Button")); // NOI18N
        evaluateButton.setEnabled(false);
        evaluateButton.setPreferredSize(new java.awt.Dimension(40, 22));
        evaluateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                evaluateButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 3);
        rightPanel.add(evaluateButton, gridBagConstraints);

        emptyPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        emptyPanel.setPreferredSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        rightPanel.add(emptyPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        add(rightPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void evaluateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_evaluateButtonActionPerformed
        evaluate();
    }//GEN-LAST:event_evaluateButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane editorScrollPane;
    private javax.swing.JPanel emptyPanel;
    private javax.swing.JButton evaluateButton;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JPanel separatorPanel;
    // End of variables declaration//GEN-END:variables

    public static void openEvaluator() {
        String selectedText = null;
        JEditorPane editor = EditorContextDispatcher.getDefault().getMostRecentEditor();
        if (editor != null) {
            selectedText = editor.getSelectedText();
        }
        CodeEvaluator evaluator = getInstance();
        evaluator.open ();
        if (selectedText != null) {
            evaluator.codePane.setText(selectedText);
        }
        evaluator.codePane.selectAll();
        evaluator.requestActive ();
    }

    @Override
    public boolean requestFocusInWindow() {
        codePane.requestFocusInWindow(); // [TODO}
        return super.requestFocusInWindow();
    }

    @Override
    protected String preferredID() {
        return this.getClass().getName();
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage (CodeEvaluator.class, "CTL_Code_Evaluator_name"); // NOI18N
    }

    @Override
    public String getToolTipText() {
        return NbBundle.getMessage (CodeEvaluator.class, "CTL_Code_Evaluator_tooltip"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("EvaluateCode"); // NOI18N
    }

    // ..........................................................................

    public String getExpression() {
        return codePane.getText();
    }

    public void evaluate() {
        evalTask.schedule(10);
    }

    private void displayResult(Variable var) {
        this.result = var;
        if (var == null) {
            fireResultChange();
            return ;
        }
        //DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(var.getValue()));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (preferences.getBoolean("show_evaluator_result", false)) {
                    TopComponent view = WindowManager.getDefault().findTopComponent("localsView"); // NOI18N [TODO]
                    view.open();
                    view.requestActive();
                } else {
                    if (resultView == null) {
                        resultView = getResultViewInstance();
                    }
                    if (result != null) {
                        resultView.open();
                        resultView.requestActive();
                    }
                }
                getInstance().requestActive();
                fireResultChange();
            }
        });
    }

    private void addResultToHistory(final String expr, Variable result) {
        if (lastEvaluationRecord != null) {
            history.addItem(lastEvaluationRecord.expr, lastEvaluationRecord.type,
                    lastEvaluationRecord.value, lastEvaluationRecord.toString);
        }
        String type = result.getType();
        String value = result.getValue();
        String toString = ""; // NOI18N
        if (result instanceof ObjectVariable) {
            try {
                toString = ((ObjectVariable) result).getToStringValue ();
            } catch (InvalidExpressionException ex) {
            }
        } else {
            toString = value;
        }
        lastEvaluationRecord = new HistoryRecord(expr, type, value, toString);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String expr2 = expr.trim();
                if (editItemsSet.contains(expr2)) {
                    editItemsList.remove(expr2);
                    editItemsList.add(0, expr2);
                } else {
                    editItemsList.add(0, expr2);
                    editItemsSet.add(expr2);
                    if (editItemsList.size() > 20) { // [TODO] constant
                        String removed = editItemsList.remove(editItemsList.size() - 1);
                        editItemsSet.remove(removed);
                    }
                }
                recomputeDropDownItems();
            }
        });
    }

    // KeyListener implementation ..........................................

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
            e.consume();
            if (debuggerRef.get() != null) {
                evaluate();
            }
        }
//        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//            e.consume();
//            close();
//        }
    }

    public void keyReleased(KeyEvent e) {
    }

    // DocumentListener implementation ..........................................

    public void insertUpdate(DocumentEvent e) {
        updateWatch();
    }

    // DocumentListener
    public void removeUpdate(DocumentEvent e) {
        updateWatch();
    }

    // DocumentListener
    public void changedUpdate(DocumentEvent e) {
        updateWatch();
    }

    private void updateWatch() {
        // Update this LAZILY to prevent from deadlocks!
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                computeEvaluationButtonState();
            }
        });
    }

    // PropertyChangeListener on current thread .................................

    public void propertyChange(PropertyChangeEvent event) {
        if (JPDADebugger.PROP_CURRENT_THREAD.equals(event.getPropertyName())) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    synchronized (this) {
                        JPDADebugger debugger = debuggerRef.get();
                        if (debugger != null) {
                            computeEvaluationButtonState();
                            setupContext();
                        }
                    }
                }
            });
        } else if (JPDADebugger.PROP_STATE.equals(event.getPropertyName())) {
            synchronized (this) {
                JPDADebugger debugger = debuggerRef.get();
                if (debugger != null && debugger.getState() != JPDADebugger.STATE_STOPPED) {
                    if (result != null) {
                        history.addItem(lastEvaluationRecord.expr, lastEvaluationRecord.type,
                            lastEvaluationRecord.value, lastEvaluationRecord.toString);
                        lastEvaluationRecord = null;
                        result = null;
                        fireResultChange();
                    } // if
                } // if
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JPDADebugger debugger = debuggerRef.get();
                        if (debugger != null && debugger.getState() == JPDADebugger.STATE_STOPPED) {
                            setupContext();
                        }
                        computeEvaluationButtonState();
                    }
                });
            } // synchronized
        }
    }

    // ..........................................................................

    public static synchronized TopComponent getResultView() {
        return new ResultView();
    }

    private synchronized TopComponent getResultViewInstance() {
        /** unique ID of <code>TopComponent</code> (singleton) */
        TopComponent instance = WindowManager.getDefault().findTopComponent("resultsView"); // NOI18N [TODO]
        if (instance == null) {
            instance = new ResultView();
        }
        //initResult(instance); // [TODO]
        return instance;
    }

    private void initResult(ResultView view) {
        javax.swing.JComponent tree = Models.createView (Models.EMPTY_MODEL);
        view.add (tree, BorderLayout.CENTER);
        Dimension tps = tree.getPreferredSize();
        tps.height = tps.width/2;
        tree.setPreferredSize(tps);
        tree.setName(NbBundle.getMessage(CodeEvaluator.class, "Evaluator.ResultA11YName"));
        tree.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(CodeEvaluator.class, "Evaluator.ResultA11YDescr"));
        // view.setLabelFor(tree);
        JTextField referenceTextField = new JTextField();
        Set<AWTKeyStroke> tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        tree.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, tfkeys);
        tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        tree.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, tfkeys);
    }

    // History ..................................................................

    public static class History {

        private static final int MAX_ITEMS = 100;

        private ArrayList<Item> historyItems = new ArrayList<Item>();

        private void addItem(String expr, String type, String value, String toString) {
            Item item = new Item(expr, type, value, toString);
            historyItems.add(0, item);
            if (historyItems.size() > MAX_ITEMS) {
                historyItems.remove(MAX_ITEMS);
            }
        }

        public ArrayList<Item> getItems() {
            return historyItems;
        }

        public void clear() {
            historyItems.clear();
        }

        public class Item {
            public String expr;
            public String type;
            public String value;
            public String toString;
            public String tooltip;
            public String exprFormatted;

            Item(String expr, String type, String value, String toString) {
                this.expr = expr;
                this.type = type;
                this.value = value;
                this.toString = toString;
                StringBuffer buf = new StringBuffer();
                buf.append("<html>");
                String text = expr.replaceAll ("&", "&amp;");
                text = text.replaceAll ("<", "&lt;");
                text = text.replaceAll (">", "&gt;");
                text = text.replaceAll ("\n", "<br/>");
                text = text.replaceAll ("\r", "");
                buf.append(text);
                buf.append("</html>");
                this.tooltip = buf.toString();
            }

            @Override
            public String toString() {
                return expr;
            }
        }

    }

    // ResultView ...............................................................

    private static class ResultView extends TopComponent implements HelpCtx.Provider {

        private static final String ID = "evaluator_result"; //NOI18N

        ResultView() {
            setLayout(new BorderLayout());
        }

        @Override
        protected String preferredID() {
            return this.getClass().getName();
        }

        @Override
        public int getPersistenceType() {
            return PERSISTENCE_ALWAYS;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage (CodeEvaluator.class, "CTL_Evaluator_Result_name"); // NOI18N
        }

        @Override
        public String getToolTipText() {
            return NbBundle.getMessage (CodeEvaluator.class, "CTL_Evaluator_Result_tooltip"); // NOI18N
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new org.openide.util.HelpCtx("EvaluationResult"); // NOI18N
        }
    }

    // EvaluateTask .............................................................

    private class EvaluateTask implements Runnable {
        public void run() {
            String exp = getExpression();
            if (exp == null || "".equals(exp)) {
                //System.out.println("Can not evaluate '"+exp+"'");
                return ;
            }
            //System.out.println("evaluate: '"+exp+"'");
            try {
                JPDADebugger debugger = debuggerRef.get();
                if (debugger != null) {
                    Variable var = debugger.evaluate(exp);
                    addResultToHistory(exp, var);
                    displayResult(var);
                }
            } catch (InvalidExpressionException ieex) {
                String message = ieex.getLocalizedMessage();
                Throwable t = ieex.getTargetException();
                if (t != null && t instanceof org.omg.CORBA.portable.ApplicationException) {
                    java.io.StringWriter s = new java.io.StringWriter();
                    java.io.PrintWriter p = new java.io.PrintWriter(s);
                    t.printStackTrace(p);
                    p.close();
                    message += " \n" + s.toString();
                }
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(message));
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        //evalDialog.requestFocus();
                        codePane.requestFocusInWindow();
                    }
                });
            }
        }
    }

    private static class DbgManagerListener extends DebuggerManagerAdapter {

        private Reference<CodeEvaluator> codeEvaluatorRef;

        public DbgManagerListener(CodeEvaluator evaluator) {
            codeEvaluatorRef = new WeakReference<CodeEvaluator>(evaluator);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            CodeEvaluator evaluator = (CodeEvaluator) codeEvaluatorRef.get();
            if (evaluator != null) {
                evaluator.checkDebuggerState();
            }
        }

    }

    private class MenuItemActivatable implements SwitcherTableItem.Activatable {

        String text;

        MenuItemActivatable(String str) {
            text = str;
        }

        public void activate() {
            codePane.setText(text);
        }

    }

    private static class DropDownButton extends JButton {

        @Override
        protected void processMouseEvent(MouseEvent me) {
            super.processMouseEvent(me);
            if (isEnabled() && me.getID() == MouseEvent.MOUSE_PRESSED) {
                getAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "pressed"));
            }
        }

        protected String getTabActionCommand(ActionEvent e) {
            return null;
        }

        void performAction( ActionEvent e ) {
        }

    }

    private static class HistoryRecord {
        String expr;
        String type;
        String value;
        String toString;

        HistoryRecord(String expr, String type, String value, String toString) {
            this.expr = expr;
            this.type = type;
            this.value = value;
            this.toString = toString;
        }
    }

}
