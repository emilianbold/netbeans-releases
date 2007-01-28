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
package org.netbeans.modules.visualweb.faces.dt.std;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Iterator;
import javax.sql.RowSet;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.visualweb.faces.dt.std.table.HtmlDataTableState;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.faces.FacesDesignProject;

public class DataModelBindingPanel extends JPanel {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(DataModelBindingPanel.class);

    protected DesignProperty prop;
    protected ValueBindingPanel vbp;
    protected JScrollPane listScroll = new JScrollPane();
    protected JList list = new JList();
    protected JLabel listLabel = new JLabel();
    protected GridBagLayout gridbag = new GridBagLayout();
    protected JTextPane noneText = new JTextPane();
    protected JCheckBox chkGenColumns = new JCheckBox();
    private HtmlDataTableState tableState;

    protected int modelIndex = -1;

    protected boolean initializing = true;

    protected DataModelBindingCustomizer customizer;
    public DataModelBindingPanel(ValueBindingPanel vbp, DataModelBindingCustomizer dmbc,
        DesignProperty prop) {
        this.vbp = vbp;
        this.customizer = dmbc;
        this.prop = prop;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        DefaultListModel dlm = new DefaultListModel();
        if (prop != null) {
            listLabel.setText(bundle.getMessage("chooseRs", prop.getDesignBean().getInstanceName())); //NOI18N

            tableState = new HtmlDataTableState(prop.getDesignBean());
            tableState.varName = HtmlDataTableState.DEFAULT_VAR_NAME;

            /*
                         // find the current value
                         String currentRef = prop.getValueSource();
                         newRef = currentRef;
                         String contextName = null;
                         String rsName = null;
                         if (currentRef != null && currentRef.startsWith("#{") && //NOI18N
                currentRef.endsWith("}")) { //NOI18N

                ArrayList parts = new ArrayList();
             StringTokenizer st = new StringTokenizer(currentRef.substring(2, currentRef.length() - 1), ".");
                while (st.hasMoreElements()) {
                    parts.add(st.nextElement());
                }
                if (parts.size() > 0) {
                    contextName = "" + parts.get(0);
                }
                if (parts.size() > 1) {
                    rsName = "" + parts.get(1);
                }
                         }
             */

            DesignContext thisContext = prop.getDesignBean().getDesignContext();

            //DesignContext[] contexts = thisContext.getProject().getDesignContexts();
            DesignContext[] contexts = getDesignContexts(prop.getDesignBean());

            java.util.List sortedContexts = new ArrayList();
            sortedContexts.add(thisContext);
            java.util.List sessionContexts = new ArrayList();
            java.util.List applicationContexts = new ArrayList();
            for (int i = 0; i < contexts.length; i++) {
                if (contexts[i] == thisContext) {
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

            // get rowset live beans
            java.util.List rowsetBeans = new ArrayList();
            for (Iterator iter = sortedContexts.iterator(); iter.hasNext(); ) {
                DesignContext aContext = (DesignContext)iter.next();
                DesignBean[] rsbs = aContext.getBeansOfType(RowSet.class);
                for (int j = 0; j < rsbs.length; j++) {
                    rowsetBeans.add(rsbs[j]);
                }
            }

            dlm.addElement(null);
            if (rowsetBeans.size() > 0) {
                for (int i = 0; i < rowsetBeans.size(); i++) {
                    DesignBean rsBean = (DesignBean)rowsetBeans.get(i);

                    dlm.addElement(rsBean);
                    if (modelIndex < 0) {
                        if (tableState.getSourceBean() == rsBean) {
                            modelIndex = i + 1;
                        }
                    }
                }
            } else {
                this.remove(listScroll);
                this.remove(chkGenColumns);
                this.add(noneText, listScrollConstraints);
            }
        }
        list.setModel(dlm);
        repaint(100);
        if (modelIndex > -1) {
            list.setSelectedIndex(modelIndex);
            chkGenColumns.setEnabled(true);
        } else {
            list.setSelectedIndex(0);
            chkGenColumns.setEnabled(false);
            tableState.setSourceBean(null); //if table is not bound to a rowset in "scope," then make it bound to nothing
            if (vbp != null) {
                vbp.setValueBinding(""); //if none is selected, remove the value binding on this table. NOTE: currently this does not work!
            }
        }
        initializing = false;
    }
    
    // For performance improvement. No need to get all the contexts in the project
    private DesignContext[] getDesignContexts(DesignBean designBean){
        DesignProject designProject = designBean.getDesignContext().getProject();
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
        designContexts[0] = designBean.getDesignContext();
        System.arraycopy(contexts, 0, designContexts, 1, contexts.length);
        return designContexts;
    }

    GridBagConstraints listScrollConstraints = new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER,
        GridBagConstraints.BOTH, new Insets(0, 8, 8, 8), 0, 0);

    private void jbInit() throws Exception {
        noneText.setEditable(false);
        noneText.setFont(listLabel.getFont());
        noneText.setBorder(UIManager.getBorder("TextField.border")); //NOI18N
        noneText.setText(bundle.getMessage("noRss")); //NOI18N

        chkGenColumns.setText(bundle.getMessage("autoGenCols")); //NOI18N
        chkGenColumns.setSelected(true);

        list.setCellRenderer(new RSListRenderer());

        this.setPreferredSize(new Dimension(400, 200));
        this.setLayout(gridbag);

        listLabel.setText(bundle.getMessage("chooseRsSimple")); //NOI18N
        this.add(listLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, new Insets(8, 8, 2, 8), 0, 0));
        this.add(listScroll, listScrollConstraints);
        if (customizer != null) {
            this.add(chkGenColumns, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 8, 8, 8), 0, 0));
        }
        listScroll.setPreferredSize(new Dimension(200, 200));
        listScroll.getViewport().add(list, null);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (initializing) {
                    return;
                }
                Object o = list.getSelectedValue();
                if (o == null || o instanceof DesignBean) {
                    DesignBean b = (DesignBean)o;
                    String calcRef = calcValueRef(b);
                    if (vbp != null) {
                        vbp.setValueBinding(calcRef == null ? "" : calcRef);
                    } else if (customizer != null) {
                        customizer.setModified(true);
                        chkGenColumns.setEnabled(o == null ? false : true);
                    } else {
                        prop.setValueSource(calcRef);
                    }

                }
            }
        });
    }

    public void customizerApply() {
        Object selectedObj = list.getSelectedValue();
        tableState.setSourceBean((DesignBean)selectedObj);
        if (chkGenColumns.isSelected()) {
            tableState.refreshColumnInfo();
        }
        tableState.saveState();
        //newRef = null;
    }

    protected String calcValueRef(DesignBean rowsetBean) {
        if (rowsetBean == null) {
            return null;
        }
        DesignContext c = rowsetBean.getDesignContext();
        if (c instanceof FacesDesignContext) {
            return "#{" + ((FacesDesignContext)c).getReferenceName() + "." +
                rowsetBean.getInstanceName() + "}"; //NOI18N
        }
        return "#{" + c.getDisplayName() + "." + rowsetBean.getInstanceName() + "}"; //NOI18N
    }

    class RSListRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof DesignBean) {
                DesignBean rsBean = (DesignBean)value;
                this.setText(bundle.getMessage("rsDisplayTextPattern", rsBean.getInstanceName(), //NOI18N
                    rsBean.getDesignContext().getDisplayName())); //NOI18N
            } else if (value == null) {
                this.setText(bundle.getMessage("noneBrackets")); //NOI18N
            }
            return this;
        }
    }
}
