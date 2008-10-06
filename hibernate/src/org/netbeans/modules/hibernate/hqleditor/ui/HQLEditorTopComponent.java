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
package org.netbeans.modules.hibernate.hqleditor.ui;

import java.awt.CardLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import org.hibernate.HibernateException;
import org.hibernate.QueryException;
import org.hibernate.SessionFactory;
import org.hibernate.engine.query.HQLQueryPlan;
import org.hibernate.hql.QueryTranslator;
import org.hibernate.hql.ast.QuerySyntaxException;
import org.hibernate.impl.SessionFactoryImpl;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.hqleditor.HQLEditorController;
import org.netbeans.modules.hibernate.hqleditor.HQLResult;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.openide.awt.MouseUtils.PopupMouseAdapter;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.util.Utilities;

/**
 * HQL editor top component.
 *
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public final class HQLEditorTopComponent extends TopComponent {

    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/hibernate/hqleditor/ui/resources/queryEditor16X16.png"; //NOI18N
    private Logger logger = Logger.getLogger(HQLEditorTopComponent.class.getName());
    private HashMap<String, FileObject> hibernateConfigMap = new HashMap<String, FileObject>();
    private static List<Integer> windowCounts = new ArrayList<Integer>();
    private Integer thisWindowCount = new Integer(0);
    private HQLEditorController controller = null;
    private HibernateEnvironment env = null;
    private ProgressHandle ph = null;
    private RequestProcessor requestProcessor;
    private RequestProcessor.Task hqlParserTask;
    private boolean isSqlTranslationProcessDone = false;

    private static int getNextWindowCount() {
        int count = 0;
        while (windowCounts.contains(count)) {
            count++;
        }
        windowCounts.add(count);
        return count;
    }

    public static HQLEditorTopComponent getInstance() {
        return new HQLEditorTopComponent(null);
    }

    public HQLEditorTopComponent(HQLEditorController controller) {
        this.controller = controller;
        initComponents();
        this.thisWindowCount = getNextWindowCount();
        setName(NbBundle.getMessage(HQLEditorTopComponent.class, "CTL_HQLEditorTopComponent") + thisWindowCount);
        setToolTipText(NbBundle.getMessage(HQLEditorTopComponent.class, "HINT_HQLEditorTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

        sqlToggleButton.setSelected(true);
        hqlEditor.getDocument().addDocumentListener(new HQLDocumentListener());
        hqlEditor.addMouseListener(new HQLEditorPopupMouseAdapter());

    }

    private class HQLEditorPopupMouseAdapter extends PopupMouseAdapter {

        private JPopupMenu popupMenu;
        private JMenuItem runHQLMenuItem;
        private JMenuItem cutMenuItem;
        private JMenuItem copyMenuItem;
        private JMenuItem pasteMenuItem;
        private JMenuItem selectAllMenuItem;
        private final String RUN_HQL_COMMAND = NbBundle.getMessage(HQLEditorTopComponent.class, "CTL_RUN_HQL_COMMAND");
        private final String CUT_COMMAND = NbBundle.getMessage(HQLEditorTopComponent.class, "CTL_CUT_COMMAND");
        private final String COPY_COMMAND = NbBundle.getMessage(HQLEditorTopComponent.class, "CTL_COPY_COMMAND");
        private final String PASTE_COMMAND = NbBundle.getMessage(HQLEditorTopComponent.class, "CTL_PASTE_COMMAND");
        private final String SELECT_ALL_COMMAND = NbBundle.getMessage(HQLEditorTopComponent.class, "CTL_SELECT_ALL_COMMAND");
        private Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        public HQLEditorPopupMouseAdapter() {
            super();
            popupMenu = new JPopupMenu();
            ActionListener actionListener = new PopupActionListener();
            runHQLMenuItem = popupMenu.add(RUN_HQL_COMMAND);
            runHQLMenuItem.setMnemonic('Q');
            runHQLMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK, false));
            runHQLMenuItem.addActionListener(actionListener);

            popupMenu.addSeparator();

            cutMenuItem = popupMenu.add(CUT_COMMAND);
            cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK, true));
            cutMenuItem.setMnemonic('t');
            cutMenuItem.addActionListener(actionListener);

            copyMenuItem = popupMenu.add(COPY_COMMAND);
            copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, true));
            copyMenuItem.setMnemonic('y');
            copyMenuItem.addActionListener(actionListener);

            pasteMenuItem = popupMenu.add(PASTE_COMMAND);
            pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK, true));
            pasteMenuItem.setMnemonic('P');
            pasteMenuItem.addActionListener(actionListener);
            
            popupMenu.addSeparator();
            
            selectAllMenuItem = popupMenu.add(SELECT_ALL_COMMAND);
            selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK, true));
            selectAllMenuItem.setMnemonic('A');
            selectAllMenuItem.addActionListener(actionListener);
        }

        @Override
        protected void showPopup(MouseEvent evt) {
            // Series of checks.. to enable or disable menus.
            if (hqlEditor.getText().trim().equals("")) {
                runHQLMenuItem.setEnabled(false);
                selectAllMenuItem.setEnabled(false);
            } else {
                runHQLMenuItem.setEnabled(true);
                selectAllMenuItem.setEnabled(true);
            }
            if (hqlEditor.getSelectedText() == null || hqlEditor.getSelectedText().trim().equals("")) {
                cutMenuItem.setEnabled(false);
                copyMenuItem.setEnabled(false);
            } else {
                cutMenuItem.setEnabled(true);
                copyMenuItem.setEnabled(true);
            }

            Transferable transferable = (Transferable) systemClipboard.getContents(null);
            if (transferable.getTransferDataFlavors().length == 0) {
                pasteMenuItem.setEnabled(false);
            } else {
                pasteMenuItem.setEnabled(true);
            }


            popupMenu.show(hqlEditor, evt.getX(), evt.getY());
        }

        private class PopupActionListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals(RUN_HQL_COMMAND)) {
                    runHQLButtonActionPerformed(e);
                } else if(e.getActionCommand().equals(SELECT_ALL_COMMAND)) {
                    hqlEditor.selectAll();
                } else if (e.getActionCommand().equals(CUT_COMMAND)) {
                    StringSelection stringSelection = new StringSelection(hqlEditor.getSelectedText());
                    systemClipboard.setContents(stringSelection, stringSelection);
                    hqlEditor.setText(
                            hqlEditor.getText().substring(0, hqlEditor.getSelectionStart()) +
                            hqlEditor.getText().substring(hqlEditor.getSelectionEnd()));

                } else if (e.getActionCommand().equals(COPY_COMMAND)) {
                    StringSelection stringSelection = new StringSelection(hqlEditor.getSelectedText());
                    systemClipboard.setContents(stringSelection, stringSelection);

                } else if (e.getActionCommand().equals(PASTE_COMMAND)) {
                    Transferable transferable = (Transferable) systemClipboard.getContents(null);
                    String clipboardContents = "";
                    try {
                        if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                            clipboardContents = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                        } else if (transferable.isDataFlavorSupported(DataFlavor.getTextPlainUnicodeFlavor())) {
                            clipboardContents = (String) transferable.getTransferData(DataFlavor.getTextPlainUnicodeFlavor());
                        }
                    } catch (UnsupportedFlavorException ex) {
                        logger.log(Level.INFO, "Unsupported transfer flavor", ex);
                    } catch (IOException ex) {
                        logger.log(Level.INFO, "IOException during paste operation", ex);
                    }
                    if(!clipboardContents.equals("")) {
                        if(hqlEditor.getSelectedText() != null) {
                            hqlEditor.replaceSelection(clipboardContents);
                        } else {
                            hqlEditor.setText(
                                    hqlEditor.getText().substring(0, hqlEditor.getCaretPosition()) +
                                    clipboardContents +
                                    hqlEditor.getText().substring(hqlEditor.getCaretPosition())
                                    );
                        }
                    }
                }
            }
        }

        // Future..
//        private class HQLTransferHandler extends TransferHandler {
//
//        }
    }

    public void setFocusToEditor() {
        hqlEditor.requestFocus();
    }

    private class ParseHQL extends Thread {

        @Override
        public void run() {
            while (!isSqlTranslationProcessDone) {
                if (hqlEditor.getText().trim().equals("")) {
                    return;
                }
                if (hibernateConfigurationComboBox.getSelectedItem() == null) {
                    logger.info("hibernate configuration combo box is empty.");
                    return;
                }
                FileObject selectedConfigObject = hibernateConfigMap.get(
                        hibernateConfigurationComboBox.getSelectedItem().toString());

                if (Thread.interrupted() || isSqlTranslationProcessDone) {
                    return;    // Cancel the task
                }
                if (selectedConfigObject != null) {
                    Project enclosingProject = FileOwnerQuery.getOwner(selectedConfigObject);
                    env = enclosingProject.getLookup().lookup(HibernateEnvironment.class);
                    if (env == null) {
                        logger.warning("HiberEnv is not found in enclosing project.");
                        return;
                    }
                    if (Thread.interrupted() || isSqlTranslationProcessDone) {
                        return;    // Cancel the task
                    }
                    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
                    try {
                        ClassLoader ccl = env.getProjectClassLoader(
                                env.getProjectClassPath(selectedConfigObject).toArray(new URL[]{}));

                        Thread.currentThread().setContextClassLoader(ccl);
                        SessionFactory sessionFactory =
                                controller.processAndConstructSessionFactory(
                                hqlEditor.getText(), selectedConfigObject, ccl, enclosingProject);
                        if (Thread.interrupted() || isSqlTranslationProcessDone) {
                            return;    // Cancel the task
                        }
                        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;

                        if (Thread.interrupted() || isSqlTranslationProcessDone) {
                            return;    // Cancel the task
                        }
                        StringBuilder stringBuff = new StringBuilder();

                        HQLQueryPlan queryPlan = sessionFactoryImpl.getQueryPlanCache().getHQLQueryPlan(hqlEditor.getText(), true, Collections.EMPTY_MAP);
                        QueryTranslator[] queryTranslators = queryPlan.getTranslators();
                        for (QueryTranslator t : queryTranslators) {
                            logger.info("SQL String = " + t.getSQLString());
                            stringBuff.append(t.getSQLString() + "\n");
                        }
                        if (Thread.interrupted() || isSqlTranslationProcessDone) {
                            return;    // Cancel the task
                        }
                        showSQL(stringBuff.toString());

                    } catch (QuerySyntaxException qe) {
                        logger.log(Level.INFO, "", qe);
                        showSQLError("MalformedQuery");
                    } catch (QueryException qe) {
                        logger.log(Level.INFO, "", qe);
                        showSQLError("MalformedQuery");
                    } catch (IllegalArgumentException ie) {
                        logger.log(Level.INFO, "", ie);
                        showSQLError("MalformedQuery");
                    } catch (HibernateException se) { // Database related exception!
                        logger.log(Level.INFO, "", se);
                        showSQLError("DbError");
                    } catch (Exception e) {
                        logger.log(Level.INFO, "", e);
                        showSQLError("GeneralError");
                    } finally {
                        isSqlTranslationProcessDone = true;
                        Thread.currentThread().setContextClassLoader(oldClassLoader);
                    }
                }

            }
        }
    }

    private void showSQL(String sql) {
        sqlEditorPane.setText(sql);
        switchToSQLView();
    }

    private void showSQLError(String errorResourceKey) {
        sqlEditorPane.setText(
                NbBundle.getMessage(HQLEditorTopComponent.class, errorResourceKey));
        switchToSQLView();
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        requestProcessor = new RequestProcessor("hql-parser", 1, true);
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        requestProcessor.stop();
    }

    private class HQLDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            process();
        }

        public void removeUpdate(DocumentEvent e) {
            process();
        }

        public void changedUpdate(DocumentEvent e) {
            process();
        }

        private void process() {
            if (hqlParserTask != null && !hqlParserTask.isFinished() && (hqlParserTask.getDelay() != 0)) {
                hqlParserTask.cancel();
            }
            hqlParserTask = requestProcessor.post(new ParseHQL(), 1000);
            isSqlTranslationProcessDone = false;
        }
    }

    public void fillHibernateConfigurations(Node[] activatedNodes) {
        Node node = activatedNodes[0];
        DataObject dO = node.getCookie(DataObject.class);
        if (dO instanceof HibernateCfgDataObject) {

            Project enclosingProject = FileOwnerQuery.getOwner(dO.getPrimaryFile());
            env = enclosingProject.getLookup().lookup(HibernateEnvironment.class);
            if (env == null) {
                logger.warning("HiberEnv is not found in enclosing project.");
                return;
            }
            List<FileObject> configFileObjects = env.getAllHibernateConfigFileObjects();
            for (FileObject configFileObject : configFileObjects) {
                try {
                    HibernateCfgDataObject hibernateCfgDataObject = (HibernateCfgDataObject) DataObject.find(configFileObject);
                    String configName = hibernateCfgDataObject.getHibernateConfiguration().getSessionFactory().getAttributeValue("name"); //NOI18N
                    if (configName == null || configName.equals("")) {
                        configName = configFileObject.getName();
                    }
                    hibernateConfigMap.put(configName, configFileObject);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            hibernateConfigurationComboBox.setModel(new DefaultComboBoxModel(hibernateConfigMap.keySet().toArray()));
            HibernateConfiguration config = ((HibernateCfgDataObject) dO).getHibernateConfiguration();
            String selectedConfigName = config.getSessionFactory().getAttributeValue("name"); //NOI18N
            if (selectedConfigName == null || selectedConfigName.equals("")) {
                selectedConfigName = dO.getPrimaryFile().getName();
            }
            hibernateConfigurationComboBox.setSelectedItem(selectedConfigName);

        } else {
            //TODO Don't know whether this case will actually arise..
        }

    }

    public void setResult(HQLResult result, ClassLoader ccl) {
        Thread.currentThread().setContextClassLoader(ccl);
        if (result.getExceptions().size() == 0) {
            // logger.info(r.getQueryResults().toString());
            switchToResultView();
            StringBuilder strBuffer = new StringBuilder();
            String space = " ", separator = "; "; //NOI18N
            strBuffer.append(result.getUpdateOrDeleteResult());
            strBuffer.append(space);
            strBuffer.append(NbBundle.getMessage(HQLEditorTopComponent.class, "queryUpdatedOrDeleted"));
            strBuffer.append(separator);

            strBuffer.append(space);
            strBuffer.append(result.getQueryResults().size());
            strBuffer.append(space);
            strBuffer.append(NbBundle.getMessage(HQLEditorTopComponent.class, "rowsSelected"));

            setStatus(strBuffer.toString());

            Vector<String> tableHeaders = new Vector<String>();
            Vector<Vector> tableData = new Vector<Vector>();

            if (result.getQueryResults().size() != 0) {

                Object firstObject = result.getQueryResults().get(0);
                if (firstObject instanceof Object[]) {
                    // Join query result.
                    for (Object oneObject : (Object[]) firstObject) {
                        createTableHeaders(tableHeaders, oneObject);
                    }

                    for (Object row : result.getQueryResults()) {
                        createTableData(tableData, (Object[]) row);
                    }

                } else {
                    // Construct the table headers
                    createTableHeaders(tableHeaders, firstObject);
                    for (Object oneObject : result.getQueryResults()) {
                        createTableData(tableData, oneObject);
                    }
                }

            }
            resultsTable.setModel(new HQLEditorResultTableModel(tableData, tableHeaders)); //new DefaultTableModel(tableData, tableHeaders));


        } else {
            logger.info("HQL query execution resulted in following " + result.getExceptions().size() + " errors.");

            switchToErrorView();
            setStatus(NbBundle.getMessage(HQLEditorTopComponent.class, "queryExecutionError"));
            errorTextArea.setText("");
            for (Throwable t : result.getExceptions()) {
                StringWriter sWriter = new StringWriter();
                PrintWriter pWriter = new PrintWriter(sWriter);
                t.printStackTrace(pWriter);
                errorTextArea.append(
                        removeHibernateModuleCodelines(sWriter.toString()));

            }

        }
        ph.progress(99);
        ph.setDisplayName(NbBundle.getMessage(HQLEditorTopComponent.class, "queryExecutionDone"));

        runHQLButton.setEnabled(true);
        ph.finish();

    }

    private void createTableHeaders(Vector<String> tableHeaders, Object oneObject) {
        for (java.lang.reflect.Method m : oneObject.getClass().getDeclaredMethods()) {
            String methodName = m.getName();
            if (methodName.startsWith("get")) { //NOI18N
                if (!tableHeaders.contains(methodName)) {
                    tableHeaders.add(m.getName().substring(3));
                }
            }
        }
    }

    private void createTableData(Vector<Vector> tableData, Object... rowObject) {
        Vector<Object> oneRow = new Vector<Object>();
        for (Object oneObject : rowObject) {
            for (java.lang.reflect.Method m : oneObject.getClass().getDeclaredMethods()) {
                String methodName = m.getName();
                if (methodName.startsWith("get")) { //NOI18N
                    try {
                        Object methodReturnValue = m.invoke(oneObject, new Object[]{});
                        if (methodReturnValue == null) {
                            oneRow.add("NULL"); //NOI18N
                            continue;
                        }
                        if (methodReturnValue instanceof java.util.Collection) {
                            continue;
                        }
                        oneRow.add(methodReturnValue.toString());
                    } catch (IllegalAccessException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IllegalArgumentException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (InvocationTargetException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        tableData.add(oneRow);
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    /*
     * Creates custom table model with read only cell customization.
     */
    private class HQLEditorResultTableModel extends DefaultTableModel {

        public HQLEditorResultTableModel(Vector<Vector> tableData, Vector<String> tableHeaders) {
            super(tableData, tableHeaders);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private String removeHibernateModuleCodelines(String exceptionTrace) {
        StringTokenizer tokenizer = new StringTokenizer(exceptionTrace, "\n");
        StringBuilder filteredExceptionTrace = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (!token.contains("org.netbeans.modules.hibernate")) {
                filteredExceptionTrace.append(token).append("\n");
            }
        }
        return filteredExceptionTrace.toString();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolBar = new javax.swing.JToolBar();
        sessionLabel = new javax.swing.JLabel();
        hibernateConfigurationComboBox = new javax.swing.JComboBox();
        toolbarSeparator = new javax.swing.JToolBar.Separator();
        runHQLButton = new javax.swing.JButton();
        toolbarSeparator1 = new javax.swing.JToolBar.Separator();
        splitPane = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        hqlEditor = new javax.swing.JEditorPane();
        containerPanel = new javax.swing.JPanel();
        toolBar2 = new javax.swing.JToolBar();
        resultToggleButton = new javax.swing.JToggleButton();
        sqlToggleButton = new javax.swing.JToggleButton();
        spacerPanel1 = new javax.swing.JPanel();
        spacerPanel2 = new javax.swing.JPanel();
        setMaxRowCountPanel = new javax.swing.JPanel();
        setMaxRowCountLabel = new javax.swing.JLabel();
        setMaxRowCountComboBox = new javax.swing.JComboBox();
        executionPanel = new javax.swing.JPanel();
        resultContainerPanel = new javax.swing.JPanel();
        statusPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        resultsOrErrorPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        errorTextArea = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        resultsTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        sqlEditorPane = new javax.swing.JTextPane();

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(sessionLabel, org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "HQLEditorTopComponent.sessionLabel.text")); // NOI18N
        toolBar.add(sessionLabel);

        toolBar.add(hibernateConfigurationComboBox);
        toolBar.add(toolbarSeparator);

        runHQLButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/hibernate/hqleditor/ui/resources/run_hql_query_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(runHQLButton, org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "HQLEditorTopComponent.runHQLButton.text")); // NOI18N
        runHQLButton.setToolTipText(org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "runHQLQueryButtonToolTip")); // NOI18N
        runHQLButton.setFocusable(false);
        runHQLButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        runHQLButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        runHQLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runHQLButtonActionPerformed(evt);
            }
        });
        toolBar.add(runHQLButton);

        toolbarSeparator1.setSeparatorSize(new java.awt.Dimension(300, 10));
        toolBar.add(toolbarSeparator1);

        splitPane.setBorder(null);
        splitPane.setDividerLocation(180);
        splitPane.setDividerSize(7);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        hqlEditor.setContentType("text/x-hql");
        jScrollPane1.setViewportView(hqlEditor);

        splitPane.setTopComponent(jScrollPane1);

        toolBar2.setFloatable(false);
        toolBar2.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(resultToggleButton, org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "HQLEditorTopComponent.resultToggleButton.text")); // NOI18N
        resultToggleButton.setToolTipText(org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "showResultTooltipText")); // NOI18N
        resultToggleButton.setFocusable(false);
        resultToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        resultToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        resultToggleButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                resultToggleButtonItemStateChanged(evt);
            }
        });
        toolBar2.add(resultToggleButton);

        org.openide.awt.Mnemonics.setLocalizedText(sqlToggleButton, org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "HQLEditorTopComponent.sqlToggleButton.text")); // NOI18N
        sqlToggleButton.setToolTipText(org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "showSQLTooltipText")); // NOI18N
        sqlToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        sqlToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        sqlToggleButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sqlToggleButtonItemStateChanged(evt);
            }
        });
        toolBar2.add(sqlToggleButton);

        org.jdesktop.layout.GroupLayout spacerPanel1Layout = new org.jdesktop.layout.GroupLayout(spacerPanel1);
        spacerPanel1.setLayout(spacerPanel1Layout);
        spacerPanel1Layout.setHorizontalGroup(
            spacerPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 202, Short.MAX_VALUE)
        );
        spacerPanel1Layout.setVerticalGroup(
            spacerPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 27, Short.MAX_VALUE)
        );

        toolBar2.add(spacerPanel1);

        org.jdesktop.layout.GroupLayout spacerPanel2Layout = new org.jdesktop.layout.GroupLayout(spacerPanel2);
        spacerPanel2.setLayout(spacerPanel2Layout);
        spacerPanel2Layout.setHorizontalGroup(
            spacerPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 98, Short.MAX_VALUE)
        );
        spacerPanel2Layout.setVerticalGroup(
            spacerPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 27, Short.MAX_VALUE)
        );

        toolBar2.add(spacerPanel2);

        org.openide.awt.Mnemonics.setLocalizedText(setMaxRowCountLabel, org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "HQLEditorTopComponent.setMaxRowCountLabel.text")); // NOI18N

        setMaxRowCountComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "100", "1000", "10000", "100000" }));
        setMaxRowCountComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "setMaxRowToolTip")); // NOI18N

        org.jdesktop.layout.GroupLayout setMaxRowCountPanelLayout = new org.jdesktop.layout.GroupLayout(setMaxRowCountPanel);
        setMaxRowCountPanel.setLayout(setMaxRowCountPanelLayout);
        setMaxRowCountPanelLayout.setHorizontalGroup(
            setMaxRowCountPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(setMaxRowCountPanelLayout.createSequentialGroup()
                .add(setMaxRowCountLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(setMaxRowCountComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        setMaxRowCountPanelLayout.setVerticalGroup(
            setMaxRowCountPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(setMaxRowCountPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(setMaxRowCountLabel)
                .add(setMaxRowCountComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        toolBar2.add(setMaxRowCountPanel);

        executionPanel.setLayout(new java.awt.CardLayout());

        resultContainerPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(statusLabel, org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "HQLEditorTopComponent.statusLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 607, Short.MAX_VALUE)
            .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(statusPanelLayout.createSequentialGroup()
                    .add(0, 303, Short.MAX_VALUE)
                    .add(statusLabel)
                    .add(0, 304, Short.MAX_VALUE)))
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
            .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(statusPanelLayout.createSequentialGroup()
                    .add(0, 0, Short.MAX_VALUE)
                    .add(statusLabel)
                    .add(0, 0, Short.MAX_VALUE)))
        );

        resultContainerPanel.add(statusPanel, java.awt.BorderLayout.NORTH);

        resultsOrErrorPanel.setLayout(new java.awt.CardLayout());

        errorTextArea.setColumns(20);
        errorTextArea.setEditable(false);
        errorTextArea.setForeground(new java.awt.Color(255, 102, 102));
        errorTextArea.setRows(5);
        jScrollPane4.setViewportView(errorTextArea);

        resultsOrErrorPanel.add(jScrollPane4, "card2");

        resultsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(resultsTable);

        resultsOrErrorPanel.add(jScrollPane3, "card3");

        resultContainerPanel.add(resultsOrErrorPanel, java.awt.BorderLayout.CENTER);

        executionPanel.add(resultContainerPanel, "card2");

        sqlEditorPane.setEditable(false);
        jScrollPane2.setViewportView(sqlEditorPane);

        executionPanel.add(jScrollPane2, "card1");

        org.jdesktop.layout.GroupLayout containerPanelLayout = new org.jdesktop.layout.GroupLayout(containerPanel);
        containerPanel.setLayout(containerPanelLayout);
        containerPanelLayout.setHorizontalGroup(
            containerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(toolBar2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
            .add(executionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
        );
        containerPanelLayout.setVerticalGroup(
            containerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(containerPanelLayout.createSequentialGroup()
                .add(toolBar2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(executionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE))
        );

        splitPane.setRightComponent(containerPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(toolBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
            .add(splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(toolBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private int getMaxRowCount() {
        String selectedMaxCount = setMaxRowCountComboBox.getSelectedItem().toString();
        try {
            return Integer.parseInt(selectedMaxCount);
        } catch (NumberFormatException e) {
            logger.warning("Number Format Error during parsing the max. row count");
        }
        return 1000; // Optimum value.
    }

private void resultToggleButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_resultToggleButtonItemStateChanged
    if (resultToggleButton.isSelected()) {//GEN-LAST:event_resultToggleButtonItemStateChanged
            ((CardLayout) (executionPanel.getLayout())).first(executionPanel);
            sqlToggleButton.setSelected(false);
        }
    }

private void sqlToggleButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_sqlToggleButtonItemStateChanged
    if (sqlToggleButton.isSelected()) {//GEN-HEADEREND:event_sqlToggleButtonItemStateChanged
        ((CardLayout) (executionPanel.getLayout())).last(executionPanel);//GEN-LAST:event_sqlToggleButtonItemStateChanged
            resultToggleButton.setSelected(false);
        }
    }

private void runHQLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runHQLButtonActionPerformed
    // Fix - 138856
    if(hqlEditor.getText().trim().equals("")) {
        switchToResultView();
        setStatus(NbBundle.getMessage(HQLEditorTopComponent.class, "emptyQuery"));
        return;
    }
    runHQLButton.setEnabled(false);                                            
    try {
        ph = ProgressHandleFactory.createHandle(//GEN-HEADEREND:event_runHQLButtonActionPerformed
                NbBundle.getMessage(HQLEditorTopComponent.class, "progressTaskname"));//GEN-LAST:event_runHQLButtonActionPerformed
            FileObject selectedConfigFile = (FileObject) hibernateConfigMap.get(hibernateConfigurationComboBox.getSelectedItem());
            ph.start(100);
            controller.executeHQLQuery(hqlEditor.getText(),
                    selectedConfigFile,
                    getMaxRowCount(),
                    ph);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel containerPanel;
    private javax.swing.JTextArea errorTextArea;
    private javax.swing.JPanel executionPanel;
    private javax.swing.JComboBox hibernateConfigurationComboBox;
    private javax.swing.JEditorPane hqlEditor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPanel resultContainerPanel;
    private javax.swing.JToggleButton resultToggleButton;
    private javax.swing.JPanel resultsOrErrorPanel;
    private javax.swing.JTable resultsTable;
    private javax.swing.JButton runHQLButton;
    private javax.swing.JLabel sessionLabel;
    private javax.swing.JComboBox setMaxRowCountComboBox;
    private javax.swing.JLabel setMaxRowCountLabel;
    private javax.swing.JPanel setMaxRowCountPanel;
    private javax.swing.JPanel spacerPanel1;
    private javax.swing.JPanel spacerPanel2;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JTextPane sqlEditorPane;
    private javax.swing.JToggleButton sqlToggleButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JToolBar toolBar2;
    private javax.swing.JToolBar.Separator toolbarSeparator;
    private javax.swing.JToolBar.Separator toolbarSeparator1;
    // End of variables declaration//GEN-END:variables

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    protected void componentClosed() {
        windowCounts.remove(thisWindowCount);
    }

    private void switchToResultView() {
        resultToggleButton.setSelected(true);
        ((CardLayout) resultsOrErrorPanel.getLayout()).last(resultsOrErrorPanel);
    }

    private void switchToSQLView() {
        sqlToggleButton.setSelected(true);
    }

    private void switchToErrorView() {
        resultToggleButton.setSelected(true);
        ((CardLayout) resultsOrErrorPanel.getLayout()).first(resultsOrErrorPanel);
    }
}
