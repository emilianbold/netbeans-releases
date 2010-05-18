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
package org.netbeans.modules.visualweb.faces.dt.std.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlInputSecret;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlInputTextarea;
import javax.faces.component.html.HtmlMessage;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.component.html.HtmlSelectOneRadio;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.ResultMessage;
import com.sun.rave.designtime.faces.FacesDesignProject;

public class HtmlDataTableCustomizerColumnsPanel extends JPanel {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(
    HtmlDataTableCustomizerColumnsPanel.class);

    private JLabel lblAvailable = new JLabel();
    private JLabel lblDisplayed = new JLabel();
    private JScrollPane scrollAvailable = new JScrollPane();
    private JScrollPane scrollDisplayed = new JScrollPane();
    private JList listAvailable = new JList();
    private JList listDisplayed = new JList();
    private ResultSetColumnListModel modelAvailable = new ResultSetColumnListModel();
    private DisplayColumnListModel modelDisplayed = new DisplayColumnListModel();
    private CompTypeComboModel modelCompType = new CompTypeComboModel();
    private JButton btnAdd = new JButton();
    private JButton btnRemoveOne = new JButton();
    private JButton btnUp = new JButton();
    private JButton btnDown = new JButton();
    private JButton btnNew = new JButton();
    private JButton btnRemoveAll = new JButton();
    private JPanel panColProps = new JPanel();
    private TitledBorder titledBorder1;
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JLabel lblHeaderText = new JLabel();
    private JLabel lblFooterText = new JLabel();
    private JLabel lblCompType = new JLabel();
    private JLabel lblValue = new JLabel();
    private JTextField tfHeaderText = new JTextField();
    private JComboBox comboCompType = new JComboBox();
    private GridBagLayout gridBagLayout2 = new GridBagLayout();
    private JPanel jPanel2 = new JPanel();
    private JTextField tfFooterText = new JTextField();
    private JTextField tfValue = new JTextField();
    private HtmlDataTableState table;
    private DesignContext context;
    private JComboBox sourceCombo = new JComboBox();
    private DefaultComboBoxModel sourceComboModel = new DefaultComboBoxModel();
    private JLabel sourceLabel = new JLabel();
    
    HtmlDataTableCustomizerColumnsPanel() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    void setTable(HtmlDataTableState table) {
        this.table = table;
    }
    
    void setContext(DesignContext context) {
        updating = true;
        this.context = context;
        populateSourceComboModel();
        sourceCombo.setModel(sourceComboModel);
        updating = false;
        initState();
    }
    
    // For performance improvement. No need to get all the contexts in the project
    private DesignContext[] getDesignContexts(DesignContext context){
        DesignProject designProject = context.getProject();
        DesignContext[] contexts;
        if (designProject instanceof FacesDesignProject) {
            contexts = ((FacesDesignProject)designProject).findDesignContexts(new String[] {
                "request",
                "session",
                "application"
            });
        } else {
            contexts = new DesignContext[0];
        }
        DesignContext[] designContexts = new DesignContext[contexts.length + 1];
        designContexts[0] = context;
        System.arraycopy(contexts, 0, designContexts, 1, contexts.length);
        return designContexts;
    }
    
    private void populateSourceComboModel() {
        sourceComboModel.removeAllElements();
        sourceComboModel.addElement(null);
        
        java.util.List sortedContexts = new ArrayList();
        sortedContexts.add(this.context);
        java.util.List sessionContexts = new ArrayList();
        java.util.List applicationContexts = new ArrayList();
        //DesignContext[] contexts = context.getProject().getDesignContexts();
        DesignContext[] contexts = getDesignContexts(context);
        for (int i = 0; i < contexts.length; i++) {
            if (contexts[i] == this.context) {
                continue;
            }
            Object scope = contexts[i].getContextData(Constants.ContextData.SCOPE);
            if ("session".equals(scope)) { //NOI18N
                sessionContexts.add(contexts[i]);
            } else if ("application".equals(scope)) { //NOI18N
                applicationContexts.add(contexts[i]);
            }
        }
        sortedContexts.addAll(sessionContexts);
        sortedContexts.addAll(applicationContexts);
        
        for (Iterator iter = sortedContexts.iterator(); iter.hasNext(); ) {
            DesignContext aContext = (DesignContext)iter.next();
            DesignBean root = aContext.getRootContainer();
            fillCombo(root.getChildBeans());
        }
    }
    
    private void fillCombo(DesignBean[] beans) {
        for (int i = 0; i < beans.length; i++) {
            Object inst = beans[i].getInstance();
            if (inst instanceof javax.faces.model.DataModel || inst instanceof java.util.List ||
            inst instanceof Object[] || inst instanceof ResultSet) {
                if (!(inst instanceof com.sun.jsfcl.data.RowSetDataModel) &&  //do not put in RowSetDataModels
                        !(inst instanceof com.sun.rave.faces.data.CachedRowSetDataModel)) {  //do not put in CachedRowSetDataModels
                    sourceComboModel.addElement(beans[i]);
                }
            }
        }
    }
    
    private void columnsPanel_actionPerformed(ActionEvent e) {
        Object s = e.getSource();
        if (s == sourceCombo) {
            sourceCombo_actionPerformed(e);
        } else if (s == btnAdd) {
            btnAdd_actionPerformed(e);
        } else if (s == btnRemoveOne) {
            btnRemoveOne_actionPerformed(e);
        } else if (s == btnUp) {
            btnUp_actionPerformed(e);
        } else if (s == btnDown) {
            btnDown_actionPerformed(e);
        } else if (s == btnNew) {
            btnNew_actionPerformed(e);
        } else if (s == btnRemoveAll) {
            btnRemoveAll_actionPerformed(e);
        } else if (s == comboCompType) {
            comboCompType_actionPerformed(e);
        }
    }
    
    private void tf_changed(DocumentEvent e) {
        Object s = e.getDocument();
        if (s == tfHeaderText.getDocument()) {
            tfHeaderText_changed(e);
        } else if (s == tfFooterText.getDocument()) {
            tfFooterText_changed(e);
        } else if (s == tfValue.getDocument()) {
            tfValue_changed(e);
        }
    }
    
    private void list_valueChanged(ListSelectionEvent e) {
        Object s = e.getSource();
        if (s == listAvailable.getSelectionModel()) {
            listAvailable_valueChanged(e);
        } else if (s == listDisplayed.getSelectionModel()) {
            listDisplayed_valueChanged(e);
        }
    }
    
    private void columnsPanel_mouseClicked(MouseEvent e) {
        Object s = e.getSource();
        if (s == listAvailable) {
            if (e.getClickCount() == 2) {
                btnAdd_actionPerformed(null);
            }
        }
    }
    
       private void listDisplayed_mouseClicked(MouseEvent e) {
        Object s = e.getSource();
        if (s == listDisplayed) {
            if (e.getClickCount() == 2) {
                btnRemoveOne_actionPerformed(null);
            }
        }
    }
    
    private void jbInit() throws Exception {
        updating = true;
        
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                columnsPanel_actionPerformed(e);
            }
        };
        DocumentListener documentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                tf_changed(e);
            }
            
            public void removeUpdate(DocumentEvent e) {
                tf_changed(e);
            }
            
            public void changedUpdate(DocumentEvent e) {
                tf_changed(e);
            }
        };
        ListSelectionListener listSelectionListener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                list_valueChanged(e);
            }
        };
        
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                columnsPanel_mouseClicked(e);
            }
        };
        
        MouseListener listDisplayedMouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                listDisplayed_mouseClicked(e);
            }
        };
        
        sourceLabel.setText(bundle.getMessage("tableCustDataSource")); //NOI18N
        sourceCombo.setRenderer(new SourceComboRenderer());
        sourceCombo.setEditable(false);
        sourceCombo.addActionListener(actionListener);
        titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,
        new Color(165, 163, 151)), " " + bundle.getMessage("colProps") + " "); //NOI18N
        lblAvailable.setText(bundle.getMessage("avail")); //NOI18N
        this.setLayout(gridBagLayout1);
        lblDisplayed.setToolTipText(""); //NOI18N
        lblDisplayed.setText(bundle.getMessage("displ")); //NOI18N
        btnAdd.setMargin(new Insets(2, 6, 2, 6));
        btnAdd.setText(">"); //NOI18N
        btnAdd.addActionListener(actionListener);
        btnRemoveOne.setText("<"); //NOI18N
        btnRemoveOne.addActionListener(actionListener);
        btnRemoveOne.setMargin(new Insets(2, 6, 2, 6));
        btnUp.setText(bundle.getMessage("up")); //NOI18N
        btnUp.addActionListener(actionListener);
        btnUp.setMargin(new Insets(2, 10, 2, 10));
        btnDown.setMargin(new Insets(2, 10, 2, 10));
        btnDown.setText(bundle.getMessage("down")); //NOI18N
        btnDown.addActionListener(actionListener);
        btnNew.setMargin(new Insets(2, 10, 2, 10));
        btnNew.setText(bundle.getMessage("new")); //NOI18N
        btnNew.addActionListener(actionListener);
        btnRemoveAll.setMargin(new Insets(2, 6, 2, 6));
        btnRemoveAll.setText("<<"); //NOI18N
        btnRemoveAll.addActionListener(actionListener);
        panColProps.setBorder(titledBorder1);
        panColProps.setDebugGraphicsOptions(0);
        //panColProps.setMaximumSize(new Dimension(32767, 32767));
        panColProps.setLayout(gridBagLayout2);
        lblHeaderText.setText(bundle.getMessage("hdrTxt") + " "); //NOI18N
        lblFooterText.setText(bundle.getMessage("ftrTxt") + " "); //NOI18N
        lblCompType.setText(bundle.getMessage("compType") + " "); //NOI18N
        lblValue.setText(bundle.getMessage("val") + " "); //NOI18N
        tfHeaderText.setText(""); //NOI18N
        tfHeaderText.getDocument().addDocumentListener(documentListener);
        tfFooterText.setText(""); //NOI18N
        tfFooterText.getDocument().addDocumentListener(documentListener);
        tfValue.setText(""); //NOI18N
        tfValue.getDocument().addDocumentListener(documentListener);
        listAvailable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listDisplayed.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollAvailable.setPreferredSize(new Dimension(200, 200));
        scrollDisplayed.setPreferredSize(new Dimension(200, 200));
        listAvailable.getSelectionModel().addListSelectionListener(listSelectionListener);
        listDisplayed.getSelectionModel().addListSelectionListener(listSelectionListener);
        scrollDisplayed.getViewport().add(listDisplayed, null);
        listDisplayed.setModel(modelDisplayed);
        comboCompType.setModel(modelCompType);
        comboCompType.addActionListener(actionListener);
        scrollAvailable.getViewport().add(listAvailable, null);
        listAvailable.setModel(modelAvailable);
        listAvailable.addMouseListener(mouseListener);
        listDisplayed.addMouseListener(listDisplayedMouseListener);
        
        this.add(sourceLabel, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
        GridBagConstraints.NONE, new Insets(8, 8, 4, 8), 0, 0));
        this.add(sourceCombo, new GridBagConstraints(0, 1, 3, 1, 1.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 8, 8, 0), 0, 0));
        this.add(lblAvailable, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL, new Insets(8, 8, 4, 8), 0, 0));
        this.add(scrollAvailable, new GridBagConstraints(0, 3, 1, 3, 1.0, 0.5,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 8, 8, 4), 0, 0));
        this.add(btnAdd, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0), 0, 0));
        this.add(lblDisplayed, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL, new Insets(8, 4, 4, 8), 0, 0));
        this.add(scrollDisplayed, new GridBagConstraints(2, 3, 1, 3, 1.0, 0.5,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 4, 8, 0), 0, 0));
        this.add(btnRemoveOne, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 8, 0), 0, 0));
        this.add(btnUp, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
        GridBagConstraints.HORIZONTAL, new Insets(0, 4, 4, 8), 0, 0));
        this.add(btnDown, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 4, 8, 8), 0, 0));
        this.add(btnRemoveAll, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
        GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 8, 0), 0,
        0));
        this.add(btnNew, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0,
        GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 4, 8, 8), 0,
        0));
        this.add(panColProps, new GridBagConstraints(0, 6, 4, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 8, 8, 8), 0, 0));
        panColProps.add(lblHeaderText, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 8, 0, 8), 0, 0));
        panColProps.add(lblFooterText, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 8, 8, 4), 0, 0));
        panColProps.add(lblCompType, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 8, 8, 4), 0, 0));
        panColProps.add(lblValue, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 8, 8, 4), 0, 0));
        panColProps.add(tfHeaderText, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(8, 0, 8, 8), 0, 0));
        panColProps.add(comboCompType, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 8, 8), 0, 0));
        panColProps.add(tfFooterText, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 8, 8), 0, 0));
        panColProps.add(tfValue, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 8, 8), 0, 0));
        panColProps.add(jPanel2, new GridBagConstraints(0, 5, 2, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        
        updateColumnProps();
        updating = false;
    }
    
    boolean initialized = false;
    void initState() {
        updating = true;
        DesignBean sourceBean = table.getSourceBean();
        sourceCombo.setSelectedItem(sourceBean); //perfectly fine to have sourceBean null here
        updating = false;
        initialized = true;
    }
    
    void saveState() {
    }
    
    private void validateIfJsfELSyntax(Result result, JTextField tf, int displayListColumnIndex,
    String dcText, String messageDisplayName, String messageDescription) {
        if (table.isJsfELSyntax(dcText)) {
            if (!table.validateJsfEL(dcText)) {
                if (result.getMessageCount() == 0) {
                    java.awt.Container container = this.getParent();
                    if (container instanceof JTabbedPane) {
                        ((JTabbedPane)container).setSelectedComponent(this);
                    }
                    listDisplayed.setSelectedIndex(displayListColumnIndex);
                    tf.requestFocus();
                    tf.setSelectionStart(0);
                    tf.setSelectionEnd(dcText.length());
                }
                result.setSuccess(false);
                result.addMessage(ResultMessage.create(ResultMessage.TYPE_CRITICAL,
                messageDisplayName, messageDescription));
            }
        }
    }
    
    void validate(Result result) {
        if (result == null) {
            result = new Result(true);
        }
        HtmlDataTableState.DisplayColumn[] dca = table.display.getColumns();
        for (int i = 0; i < dca.length; i++) {
            HtmlDataTableState.DisplayColumn dc = dca[i];
            validateIfJsfELSyntax(result, tfHeaderText, i, dc.headerText,
            bundle.getMessage("invalidEntry", "header text"), bundle.getMessage("tfInvalidJsf",  //NOI18N
            dc.itemText, "header")); //NOI18N
            validateIfJsfELSyntax(result, tfFooterText, i, dc.footerText,
            bundle.getMessage("invalidEntry", "footer text"), bundle.getMessage("tfInvalidJsf", //NOI18N
            dc.itemText, "footer")); //NOI18N
            validateIfJsfELSyntax(result, tfValue, i, dc.compValueRef,
            bundle.getMessage("invalidEntry", "value"), bundle.getMessage("tfInvalidJsf", //NOI18N
            dc.itemText, "value")); //NOI18N
        }
    }
    
    private void sourceCombo_actionPerformed(ActionEvent e) {
        if (updating) {
            return;
        }
        updating = true;
        DesignBean sourceBean = (DesignBean)sourceCombo.getSelectedItem();
        table.setSourceBean(sourceBean);
        if (sourceBean == table.getSavedSourceBean()) {
            table.loadColumnState();
        } else {
            table.refreshColumnInfo();
        }
        refreshResultSetColumns();
        refreshDisplayColumns();
        updateColumnProps();
        updating = false;
    }
    
    private void refreshResultSetColumns() {
        modelAvailable.fire();
        listAvailable.clearSelection();
        enableOrDisableListAvailableButtons();
    }
    
    private void refreshDisplayColumns() {
        modelDisplayed.fire();
        if (modelDisplayed.getSize() > 0) {
            listDisplayed.setSelectedIndex(0);
        } else {
            listDisplayed.clearSelection();
        }
        enableOrDisableListDisplayedButtons();
    }
    
    private void btnAdd_actionPerformed(ActionEvent e) {
        if (updating) {
            return;
        }
        updating = true;
        int[] selectedIndices = listAvailable.getSelectedIndices();
        if (selectedIndices.length == 0) {
            updating = false;
            return;
        }
        int modelDisplayedPreviousSize = modelDisplayed.getSize();
        for (int i = 0; i < selectedIndices.length; i++) {
            Object o = modelAvailable.getElementAt(selectedIndices[i]);
            if (o instanceof HtmlDataTableState.ResultSetColumn) {
                HtmlDataTableState.ResultSetColumn rsc = (HtmlDataTableState.ResultSetColumn)o;
                table.display.addColumn(rsc);
            }
        }
        modelDisplayed.fire();
        if (modelDisplayedPreviousSize == 0 && modelDisplayed.getSize() > 0) {
            listDisplayed.setSelectedIndex(0);
        }
        enableOrDisableListAvailableButtons();
        enableOrDisableListDisplayedButtons();
        updateColumnProps();
        updating = false;
    }
    
    private void btnNew_actionPerformed(ActionEvent e) {
        if (updating) {
            return;
        }
        updating = true;
        HtmlDataTableState.DisplayColumn dc = table.display.addNewColumn();
        modelDisplayed.fire();
        listDisplayed.setSelectedValue(dc, true);
        enableOrDisableListDisplayedButtons();
        updateColumnProps();
        updating = false;
    }
    
    private void btnRemoveOne_actionPerformed(ActionEvent e) {
        if (updating) {
            return;
        }
        updating = true;
        Object o = listDisplayed.getSelectedValue();
        if (!(o instanceof HtmlDataTableState.DisplayColumn)) {
            updating = false;
            return;
        }
        HtmlDataTableState.DisplayColumn dc = (HtmlDataTableState.DisplayColumn)o;
        table.display.removeColumn(dc);
        modelDisplayed.fire();
        int mdsize = modelDisplayed.getSize();
        if (mdsize == 0) {
            listDisplayed.clearSelection();
        } else if (listDisplayed.getSelectedIndex() >= mdsize) {
            listDisplayed.setSelectedIndex(mdsize - 1);
        }
        enableOrDisableListDisplayedButtons();
        updateColumnProps();
        updating = false;
    }
    
    private void btnRemoveAll_actionPerformed(ActionEvent e) {
        if (updating) {
            return;
        }
        updating = true;
        table.display.clearColumns();
        modelDisplayed.fire();
        listDisplayed.clearSelection();
        enableOrDisableListDisplayedButtons();
        updateColumnProps();
        updating = false;
    }
    
    private void btnUp_actionPerformed(ActionEvent e) {
        if (updating) {
            return;
        }
        updating = true;
        Object o = listDisplayed.getSelectedValue();
        if (!(o instanceof HtmlDataTableState.DisplayColumn)) {
            updating = false;
            return;
        }
        HtmlDataTableState.DisplayColumn dc = (HtmlDataTableState.DisplayColumn)o;
        table.display.moveColumnUp(dc);
        modelDisplayed.fire();
        listDisplayed.setSelectedValue(dc, true);
        enableOrDisableListDisplayedButtons();
        updateColumnProps();
        updating = false;
    }
    
    private void btnDown_actionPerformed(ActionEvent e) {
        if (updating) {
            return;
        }
        updating = true;
        Object o = listDisplayed.getSelectedValue();
        if (!(o instanceof HtmlDataTableState.DisplayColumn)) {
            updating = false;
            return;
        }
        HtmlDataTableState.DisplayColumn dc = (HtmlDataTableState.DisplayColumn)o;
        table.display.moveColumnDown(dc);
        modelDisplayed.fire();
        listDisplayed.setSelectedValue(dc, true);
        enableOrDisableListDisplayedButtons();
        updateColumnProps();
        updating = false;
    }
    
    private void comboCompType_actionPerformed(ActionEvent e) {
        if (updating) {
            return;
        }
        updating = true;
        updateColumnProps();
        updating = false;
    }
    
    private void listAvailable_valueChanged(ListSelectionEvent e) {
        if (updating) {
            return;
        }
        updating = true;
        enableOrDisableListAvailableButtons();
        updating = false;
    }
    
    private void listDisplayed_valueChanged(ListSelectionEvent e) {
        if (updating) {
            return;
        }
        updating = true;
        enableOrDisableListDisplayedButtons();
        updateColumnProps();
        updating = false;
    }
    
    private void enableOrDisableListAvailableButtons() {
        btnAdd.setEnabled(modelAvailable.getSize() > 0 &&
        listAvailable.getSelectedIndices().length > 0);
    }
    
    private void enableOrDisableListDisplayedButtons() {
        Object o = listDisplayed.getSelectedValue();
        if (o instanceof HtmlDataTableState.DisplayColumn) {
            HtmlDataTableState.DisplayColumn dc = (HtmlDataTableState.DisplayColumn)o;
            btnUp.setEnabled(table.display.canMoveUp(dc));
            btnDown.setEnabled(table.display.canMoveDown(dc));
            btnRemoveOne.setEnabled(true);
        } else {
            btnUp.setEnabled(false);
            btnDown.setEnabled(false);
            btnRemoveOne.setEnabled(false);
        }
        if (modelDisplayed.getSize() > 0) {
            btnRemoveAll.setEnabled(true);
        } else {
            btnRemoveAll.setEnabled(false);
        }
    }
    
    private boolean updating = false;
    private void updateColumnProps() {
        boolean valueNotApplicable = false;
        Object o = listDisplayed.getSelectedValue();
        HtmlDataTableState.DisplayColumn dc = null;
        if (o instanceof HtmlDataTableState.DisplayColumn) {
            dc = (HtmlDataTableState.DisplayColumn)o;
            titledBorder1.setTitle(" " + bundle.getMessage("dcProps", dc.toString()) + " "); //NOI18N
            tfHeaderText.setText(dc.headerText);
            tfFooterText.setText(dc.footerText);
            modelCompType.setDisplayColumn(dc);
            
            if (HtmlMessage.class.getName().equals(dc.compClassName) || HtmlPanelGroup.class.getName().equals(dc.compClassName) || HtmlPanelGrid.class.getName().equals(dc.compClassName)) {
                valueNotApplicable = true;
            }
            
            tfValue.setText(valueNotApplicable ? "" : dc.compValueRef); //NOI18N
        } else {
            titledBorder1.setTitle(" " + bundle.getMessage("colProps") + " "); //NOI18N
            tfHeaderText.setText(""); //NOI18N
            tfFooterText.setText(""); //NOI18N
            modelCompType.setDisplayColumn(null);
            tfValue.setText(""); //NOI18N
        }
        boolean enabled = true;
        boolean valueEnabled = true;
        //look for reasons to make enabled or valueEnabled false
        if (dc == null) {
            enabled = false;
            valueEnabled = false;
        } else if (dc.hasResultSetColumnPeer) {
            valueEnabled = false;
        } else if (valueNotApplicable) {
            valueEnabled = false;
        }
        tfHeaderText.setEnabled(enabled);
        tfFooterText.setEnabled(enabled);
        comboCompType.setEnabled(enabled);
        tfValue.setEnabled(valueEnabled);
        
        tfHeaderText.setBackground(enabled ? SystemColor.text : SystemColor.control);
        tfFooterText.setBackground(enabled ? SystemColor.text : SystemColor.control);
        tfValue.setBackground(valueEnabled ? SystemColor.text : SystemColor.control);
        
        validate();
        repaint(100);
    }
    
    private void tfHeaderText_changed(DocumentEvent e) {
        if (updating) {
            return;
        }
        updating = true;
        Object o = listDisplayed.getSelectedValue();
        if (o instanceof HtmlDataTableState.DisplayColumn) {
            HtmlDataTableState.DisplayColumn dc = (HtmlDataTableState.DisplayColumn)o;
            Document d = e.getDocument();
            try {
                dc.headerText = d.getText(d.getStartPosition().getOffset(),
                d.getEndPosition().getOffset()).trim();
            } catch (BadLocationException x) {}
        }
        updating = false;
    }
    
    private void tfFooterText_changed(DocumentEvent e) {
        if (updating) {
            return;
        }
        updating = true;
        Object o = listDisplayed.getSelectedValue();
        if (o instanceof HtmlDataTableState.DisplayColumn) {
            HtmlDataTableState.DisplayColumn dc = (HtmlDataTableState.DisplayColumn)o;
            Document d = e.getDocument();
            try {
                dc.footerText = d.getText(d.getStartPosition().getOffset(),
                d.getEndPosition().getOffset()).trim();
            } catch (BadLocationException x) {}
        }
        updating = false;
    }
    
    private void tfValue_changed(DocumentEvent e) {
        if (updating) {
            return;
        }
        updating = true;
        Object o = listDisplayed.getSelectedValue();
        if (o instanceof HtmlDataTableState.DisplayColumn) {
            HtmlDataTableState.DisplayColumn dc = (HtmlDataTableState.DisplayColumn)o;
            Document d = e.getDocument();
            try {
                dc.compValueRef = d.getText(d.getStartPosition().getOffset(),
                d.getEndPosition().getOffset()).trim();
            } catch (BadLocationException x) {}
        }
        updating = false;
    }
    
    private class ResultSetColumnListModel extends AbstractListModel {
        public int getSize() {
            return table.rsinfo == null ? 0 : table.rsinfo.getColumnCount();
        }
        
        public Object getElementAt(int index) {
            if (table.rsinfo != null && index >= 0 && index <= table.rsinfo.getColumnCount() - 1) {
                return table.rsinfo.getColumn(index);
            } else {
                return null;
            }
        }
        
        public void fire() {
            fireContentsChanged(this, -1, -1);
        }
    }
    
    private class DisplayColumnListModel extends AbstractListModel {
        public int getSize() {
            return table.display.getColumnCount();
        }
        
        public Object getElementAt(int index) {
            if (index >= 0 && index <= getSize() - 1) {
                return table.display.getColumn(index);
            } else {
                return null;
            }
        }
        
        public void fire() {
            fireContentsChanged(this, -1, -1);
        }
    }
    
    private class CompTypeComboModel extends AbstractListModel implements ComboBoxModel {
        private CompType[] types = new CompType[] {
            new CompType(bundle.getMessage("outputTextParenRO"), HtmlOutputText.class.getName(), "outputText"), //NOI18N
            new CompType(bundle.getMessage("inputTextParenE"), HtmlInputText.class.getName(), "textField"), //NOI18N
            new CompType(bundle.getMessage("secretTextParenE"), HtmlInputSecret.class.getName(), "secretField"), //NOI18N
            new CompType(bundle.getMessage("textAreaParenMulti"), HtmlInputTextarea.class.getName(), "textArea"), //NOI18N
            new CompType(bundle.getMessage("btnParenA"), HtmlCommandButton.class.getName(), "button"), //NOI18N
            new CompType(bundle.getMessage("linkParenA"), HtmlCommandLink.class.getName(), "linkAction"), //NOI18N
            new CompType(bundle.getMessage("chkParenTF"), HtmlSelectBooleanCheckbox.class.getName(), "checkbox"), //NOI18N
            new CompType(bundle.getMessage("dropdnParenLOI"), HtmlSelectOneMenu.class.getName(), "dropdown"), //NOI18N
            new CompType(bundle.getMessage("radioButtonList"), HtmlSelectOneRadio.class.getName(), "radioButtonList"), //NOI18N
            new CompType(bundle.getMessage("image"), HtmlGraphicImage.class.getName(), "image"), //NOI18N
            new CompType(bundle.getMessage("inlineMsg"), HtmlMessage.class.getName(), "inlineMessage"), //NOI18N
            new CompType(bundle.getMessage("gridPanel"), HtmlPanelGrid.class.getName(), "gridPanel"),   //NOI18N
            new CompType(bundle.getMessage("groupPanel"), HtmlPanelGroup.class.getName(), "groupPanel")   //NOI18N
        };
        
        private HashMap compTypeHash = new HashMap();
        private CompTypeComboModel() {
            for (int i = 0; i < types.length; i++) {
                compTypeHash.put(types[i].compClassName, types[i]);
            }
        }
        
        private HtmlDataTableState.DisplayColumn dc = null;
        private void setDisplayColumn(HtmlDataTableState.DisplayColumn dc) {
            this.dc = dc;
            fire();
        }
        
        public void setSelectedItem(Object anItem) {
            if (anItem instanceof CompType && dc != null) {
                CompType ct = (CompType)anItem;
                dc.compClassName = ct.compClassName;
                //dc.compInstanceName = ct.compInstanceName;
            }
        }
        
        public Object getSelectedItem() {
            if (dc != null) {
                return compTypeHash.get(dc.compClassName);
            }
            return null;
        }
        
        public int getSize() {
            return types.length;
        }
        
        public Object getElementAt(int index) {
            return types[index];
        }
        
        public void fire() {
            fireContentsChanged(this, -1, -1);
        }
    }
    
    private class CompType {
        public String display;
        public String compClassName;
        public String compInstanceName;
        public CompType(String display, String compClassName, String compInstanceName) {
            this.display = display;
            this.compClassName = compClassName;
            this.compInstanceName = compInstanceName;
        }
        
        public String toString() {
            return display;
        }
    }
    
    private class SourceComboRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                this.setText(bundle.getMessage("bracketsNone")); //NOI18N
            } else {
                DesignBean bean = (DesignBean)value;
                this.setText(bundle.getMessage("sourceComboRend", bean.getInstanceName(), //NOI18N
                bean.getDesignContext().getDisplayName())); //NOI18N
            }
            return this;
        }
    }
}
