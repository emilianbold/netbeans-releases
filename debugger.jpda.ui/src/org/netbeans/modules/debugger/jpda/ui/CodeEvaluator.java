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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.ui.models.WatchesNodeModel;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.CompoundModel;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.datatransfer.PasteType;
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

    private JEditorPane codePane;
    private HistoryPanel historyPanel;
    private Reference<JPDADebugger> debuggerRef = new WeakReference(null);
    private DbgManagerListener dbgManagerListener;
    private EvaluatorModelListener viewModelListener;
    private PropertyChangeListener csfListener;
    private ResultView resultView;
    private static volatile CodeEvaluator currentEvaluator;
    private Variable result;
    private RequestProcessor.Task evalTask =
            new RequestProcessor("Debugger Evaluator", 1).  // NOI18N
            create(new EvaluateTask());

    /** Creates new form CodeEvaluator */
    public CodeEvaluator() {
        initComponents();
        codePane = new JEditorPane();
        historyPanel = new HistoryPanel();

        historyToggleButton.setMargin(new Insets(2,3,2,3));
        historyToggleButton.setFocusable(false);

        final Document[] documentPtr = new Document[] { null };
        ActionListener contextUpdated = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (codePane.getDocument() != documentPtr[0]) {
                    codePane.getDocument().addDocumentListener(CodeEvaluator.this);
                }
            }
        };
        WatchPanel.setupContext(codePane, contextUpdated);
        editorScrollPane.setViewportView(codePane);
        codePane.getDocument().addDocumentListener(this);
        codePane.addKeyListener(this);
        documentPtr[0] = codePane.getDocument();

        currentEvaluator = this; // [TODO]

        dbgManagerListener = new DbgManagerListener (this);
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                DebuggerManager.PROP_CURRENT_SESSION,
                dbgManagerListener
        );
        checkDebuggerState();
    }

    public static synchronized CodeEvaluator getInstance() {
        CodeEvaluator instance = (CodeEvaluator) WindowManager.getDefault().findTopComponent(ID);
        if (instance == null) {
            instance = new CodeEvaluator();
        }
        return instance;
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
                    lastDebugger.removePropertyChangeListener(
                            JPDADebugger.PROP_CURRENT_THREAD,
                            CodeEvaluator.this);
                    debuggerRef = new WeakReference(null);
                    displayResult(null);
                }
                if (debugger != null) {
                    debuggerRef = new WeakReference(debugger);
                    debugger.addPropertyChangeListener(
                            JPDADebugger.PROP_CURRENT_THREAD,
                            CodeEvaluator.this);
                } else {
                    historyPanel.clearHistory();
                }
                computeEvaluationButtonState();
            }
        });
    }

    private void computeEvaluationButtonState() {
        JPDADebugger debugger = debuggerRef.get();
        boolean isEnabled = debugger != null && debugger.getCurrentThread() != null &&
                codePane.getDocument().getLength() > 0 &&
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
        historyToggleButton = new javax.swing.JToggleButton();
        emptyPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        editorScrollPane.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(editorScrollPane, gridBagConstraints);

        separatorPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Separator.foreground"));
        separatorPanel.setPreferredSize(new java.awt.Dimension(1, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        add(separatorPanel, gridBagConstraints);

        rightPanel.setPreferredSize(new java.awt.Dimension(94, 209));
        rightPanel.setLayout(new java.awt.GridBagLayout());

        evaluateButton.setText(org.openide.util.NbBundle.getMessage(CodeEvaluator.class, "CodeEvaluator.evaluateButton.text")); // NOI18N
        evaluateButton.setToolTipText(org.openide.util.NbBundle.getMessage(CodeEvaluator.class, "HINT_Evaluate_Button")); // NOI18N
        evaluateButton.setEnabled(false);
        evaluateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                evaluateButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        rightPanel.add(evaluateButton, gridBagConstraints);

        historyToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/debugger/jpda/resources/info_big.png"))); // NOI18N
        historyToggleButton.setText(org.openide.util.NbBundle.getMessage(CodeEvaluator.class, "CodeEvaluator.historyToggleButton.text")); // NOI18N
        historyToggleButton.setToolTipText(org.openide.util.NbBundle.getMessage(CodeEvaluator.class, "HINT_Show_History")); // NOI18N
        historyToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                historyToggleButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        rightPanel.add(historyToggleButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        rightPanel.add(emptyPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        add(rightPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void evaluateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_evaluateButtonActionPerformed
        evaluate();
    }//GEN-LAST:event_evaluateButtonActionPerformed

    private void historyToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_historyToggleButtonActionPerformed
        boolean toggled = historyToggleButton.isSelected();
        if (toggled) {
            // show history
            editorScrollPane.setViewportView(historyPanel);
        } else {
            // show editor pane
            editorScrollPane.setViewportView(codePane);
        }
        computeEvaluationButtonState();
    }//GEN-LAST:event_historyToggleButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane editorScrollPane;
    private javax.swing.JPanel emptyPanel;
    private javax.swing.JButton evaluateButton;
    private javax.swing.JToggleButton historyToggleButton;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JPanel separatorPanel;
    // End of variables declaration//GEN-END:variables

    public static void openEvaluator() {
        CodeEvaluator evaluator = getInstance();
        evaluator.open ();
        evaluator.requestActive ();
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
        //DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(var.getValue()));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (resultView == null) {
                    resultView = getResultViewInstance();
                }
                if (result != null) {
                    resultView.open();
                }
                resultView.requestActive();
                viewModelListener.updateModel();
            }
        });
        // viewModelListener.updateModel();
    }

    private void addResultToHistory(String expr, Variable result) {
        int index = expr.indexOf('\n');
        if (index < 0) {
            index = expr.length();
        }
        index = Math.min(index, 15); // [TODO] constant
        String shortExpr = expr.substring(0, index);
        String type = result.getType();
        String value = result.getValue();
        historyPanel.addItem(shortExpr, type, value);
    }

    // KeyListener implementation ..........................................

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
            e.consume();
            evaluate();
        }
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

    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (this) {
                    JPDADebugger debugger = debuggerRef.get();
                    if (debugger != null) {
                        computeEvaluationButtonState();
                    }
                }
            }
        });
    }

    // ..........................................................................

    public static synchronized TopComponent getResultView() {
        return new ResultView();
    }

    private synchronized ResultView getResultViewInstance() {
        /** unique ID of <code>TopComponent</code> (singleton) */
        ResultView instance = (ResultView) WindowManager.getDefault().findTopComponent(ResultView.ID);
        if (instance == null) {
            instance = new ResultView();
        }
        initResult(instance); // [TODO]
        return instance;
    }

    private void initResult(ResultView view) {
        javax.swing.JComponent tree = Models.createView (Models.EMPTY_MODEL);
        Container hackedFCR = (Container) ((Container) ((Container) tree.getComponents()[0]).getComponents()[0]).getComponents()[0];
        hackedFCR = (Container) tree.getComponents()[0];
        try {
            java.lang.reflect.Field treeTableField = hackedFCR.getClass().getSuperclass().getDeclaredField("treeTable");
            treeTableField.setAccessible(true);
            hackedFCR = (Container) treeTableField.get(hackedFCR);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        hackedFCR.setFocusCycleRoot(false);
        hackedFCR.setFocusTraversalPolicy(null);
        hackedFCR.setFocusTraversalPolicyProvider(false);
        view.add (tree, BorderLayout.CENTER);
        viewModelListener = new EvaluatorModelListener (
            tree
        );
        Dimension tps = tree.getPreferredSize();
        tps.height = tps.width/2;
        tree.setPreferredSize(tps);
        tree.setName(NbBundle.getMessage(Evaluator2.class, "Evaluator.ResultA11YName"));
        tree.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(Evaluator2.class, "Evaluator.ResultA11YDescr"));
        // view.setLabelFor(tree);
        JTextField referenceTextField = new JTextField();
        Set<AWTKeyStroke> tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        tree.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, tfkeys);
        tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        tree.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, tfkeys);
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
                Variable var = debugger.evaluate(exp);
                addResultToHistory(exp, var);
                displayResult(var);
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

    /**
     * Inspired by org.netbeans.modules.debugger.jpda.ui.views.ViewModelListener.
     */
    private static class EvaluatorModelListener extends DebuggerManagerAdapter {

        private String          viewType;
        private JComponent      view;
        private List models = new ArrayList(11);


        public EvaluatorModelListener(JComponent view) {
            this.viewType = "LocalsView"; // NOI18N
            this.view = view;
            DebuggerManager.getDebuggerManager ().addDebuggerListener (
                DebuggerManager.PROP_CURRENT_ENGINE,
                this
            );
            updateModel ();
        }

        public void destroy () {
            DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                DebuggerManager.PROP_CURRENT_ENGINE,
                this
            );
            Models.setModelsToView (
                view,
                Models.EMPTY_MODEL
            );
        }

        @Override
        public void propertyChange (PropertyChangeEvent e) {
            CodeEvaluator eval = currentEvaluator;
            if (eval != null) {
                eval.csfListener = null;
//                DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine();
//                if (de == null) return;
//                JPDADebugger debugger = de.lookupFirst(null, JPDADebugger.class);
//                eval.setDebugger(debugger);
                eval.checkDebuggerState();
            }
            updateModel ();
        }

        public synchronized void updateModel () {
            DebuggerManager dm = DebuggerManager.getDebuggerManager ();
            DebuggerEngine e = dm.getCurrentEngine ();

            List treeModels;
            List treeModelFilters;
            List treeExpansionModels;
            List nodeModels;
            List nodeModelFilters;
            List tableModels;
            List tableModelFilters;
            List nodeActionsProviders;
            List nodeActionsProviderFilters;
            List columnModels;
            List mm;
            ContextProvider cp = e != null ? DebuggerManager.join(e, dm) : dm;
            treeModels =            cp.lookup (viewType, TreeModel.class);
            treeModelFilters =      cp.lookup (viewType, TreeModelFilter.class);
            treeExpansionModels =   cp.lookup (viewType, TreeExpansionModel.class);
            nodeModels =            cp.lookup (viewType, NodeModel.class);
            nodeModelFilters =      cp.lookup (viewType, NodeModelFilter.class);
            tableModels =           cp.lookup (viewType, TableModel.class);
            tableModelFilters =     cp.lookup (viewType, TableModelFilter.class);
            nodeActionsProviders =  cp.lookup (viewType, NodeActionsProvider.class);
            nodeActionsProviderFilters = cp.lookup (viewType, NodeActionsProviderFilter.class);
            columnModels =          cp.lookup (viewType, ColumnModel.class);
            mm =                    cp.lookup (viewType, Model.class);

            List treeNodeModelsCompound = new ArrayList(11);
            treeNodeModelsCompound.add(treeModels);
            for (int i = 0; i < 2; i++) {
                treeNodeModelsCompound.add(Collections.EMPTY_LIST);
            }
            treeNodeModelsCompound.add(nodeModels);
            for (int i = 0; i < 7; i++) {
                treeNodeModelsCompound.add(Collections.EMPTY_LIST);
            }
            CompoundModel treeNodeModel = Models.createCompoundModel(treeNodeModelsCompound);
            /*
            List nodeModelsCompound = new ArrayList(11);
            nodeModelsCompound.add(new ArrayList()); // An empty tree model will be added
            for (int i = 0; i < 2; i++) {
                nodeModelsCompound.add(Collections.EMPTY_LIST);
            }
            nodeModelsCompound.add(nodeModels);
            for (int i = 0; i < 7; i++) {
                nodeModelsCompound.add(Collections.EMPTY_LIST);
            }
            CompoundModel nodeModel = Models.createCompoundModel(nodeModelsCompound);
             */
            EvaluatorModel eTreeNodeModel = new EvaluatorModel(treeNodeModel, treeNodeModel);

            models.clear();
            treeModels.clear();
            treeModels.add(eTreeNodeModel);
            models.add(treeModels);
            models.add(treeModelFilters);
            models.add(treeExpansionModels);
            nodeModels.clear();
            nodeModels.add(eTreeNodeModel);
            models.add(nodeModels);
            models.add(nodeModelFilters);
            models.add(tableModels);
            models.add(tableModelFilters);
            models.add(nodeActionsProviders);
            models.add(nodeActionsProviderFilters);
            models.add(columnModels);
            models.add(mm);

            Models.setModelsToView (
                view,
                Models.createCompoundModel (models)
            );
        }

    }

    private static class EvaluatorModel implements TreeModel, ExtendedNodeModel {

        private CompoundModel treeModel;
        private CompoundModel nodeModel;

        public EvaluatorModel(CompoundModel treeModel, CompoundModel nodeModel) {
            this.treeModel = treeModel;
            this.nodeModel = nodeModel;
        }

        public void addModelListener(ModelListener l) {
            treeModel.addModelListener(l);
        }

        public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
            if (TreeModel.ROOT.equals(parent)) {
                CodeEvaluator eval = currentEvaluator;
                if (eval == null || eval.result == null) {
                    return new Object[] {};
                } else {
                    return new Object[] { eval.result };
                }
            } else {
                return treeModel.getChildren(parent, from, to);
            }
        }

        public int getChildrenCount(Object node) throws UnknownTypeException {
            if (TreeModel.ROOT.equals(node)) {
                return currentEvaluator == null ? 0 : 1;
            } else {
                return treeModel.getChildrenCount(node);
            }
        }

        public Object getRoot() {
            return TreeModel.ROOT;
        }

        public boolean isLeaf(Object node) throws UnknownTypeException {
            if (TreeModel.ROOT.equals(node)) {
                return false;
            } else {
                return treeModel.isLeaf(node);
            }
        }

        public void removeModelListener(ModelListener l) {
            treeModel.removeModelListener(l);
        }

        public String getDisplayName(Object node) throws UnknownTypeException {
            CodeEvaluator eval = currentEvaluator;
            if (eval != null && eval.result != null) {
                if (node == eval.result) {
                    return eval.getExpression();
                }
            }
            return nodeModel.getDisplayName(node);
        }

        public String getIconBase(Object node) throws UnknownTypeException {
            throw new UnsupportedOperationException("Not supported.");
        }

        public String getShortDescription(Object node) throws UnknownTypeException {
            CodeEvaluator eval = currentEvaluator;
            if (eval != null && eval.result != null) {
                if (node == eval.result) {
                    return eval.getExpression();
                }
            }
            return nodeModel.getShortDescription(node);
        }

        public boolean canRename(Object node) throws UnknownTypeException {
            return nodeModel.canRename(node);
        }

        public boolean canCopy(Object node) throws UnknownTypeException {
            return nodeModel.canCopy(node);
        }

        public boolean canCut(Object node) throws UnknownTypeException {
            return nodeModel.canCut(node);
        }

        public Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException {
            return nodeModel.clipboardCopy(node);
        }

        public Transferable clipboardCut(Object node) throws IOException, UnknownTypeException {
            return nodeModel.clipboardCut(node);
        }

        public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
            return nodeModel.getPasteTypes(node, t);
        }

        public void setName(Object node, String name) throws UnknownTypeException {
            nodeModel.setName(node, name);
        }

        public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
            CodeEvaluator eval = currentEvaluator;
            if (eval != null && eval.result != null) {
                if (node == eval.result) {
                    return WatchesNodeModel.WATCH;
                }
            }
            return nodeModel.getIconBaseWithExtension(node);
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

}
