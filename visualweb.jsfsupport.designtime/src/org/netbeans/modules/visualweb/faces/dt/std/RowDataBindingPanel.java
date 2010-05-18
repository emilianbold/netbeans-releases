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
package org.netbeans.modules.visualweb.faces.dt.std;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import javax.sql.RowSet;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.faces.FacesDesignProject;

public class RowDataBindingPanel extends JPanel {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(RowDataBindingPanel.class);

    protected JTextPane noneText = new JTextPane();

    protected DesignProperty prop;
    protected ValueBindingPanel vbp;

    protected JLabel rsLabel = new JLabel();
    protected JComboBox rsCombo = new JComboBox();

    protected HashMap listModelHash = new HashMap();

    protected JLabel valueListLabel = new JLabel();
    protected JList valueList = new JList();
    protected JScrollPane valueListScroll = new JScrollPane(valueList);

    protected GridBagLayout gridbag = new GridBagLayout();
    protected String parentName = null;

    protected int rsIndex = -1;
    protected int valueIndex = 0;

    protected boolean initializing = true;

    protected class ListItem {
        public String display;
        public String table;
        public String column;
        public String toString() {
            return display;
        }
    }

//    // scans parent hierarchy for a specific bean type
//    protected DesignBean scanForParent(Class beanClass, DesignBean child) {
//        DesignBean parent = child.getBeanParent();
//        if (parent != null && parent.getInstance() != null) {
//            if (beanClass.isAssignableFrom(parent.getInstance().getClass())) {
//                return parent;
//            }
//            return scanForParent(beanClass, parent);
//        }
//        return null;
//    }

    protected RowDataBindingCustomizer customizer;
    public RowDataBindingPanel(ValueBindingPanel vbp, RowDataBindingCustomizer customizer,
        DesignProperty prop) {
        this.vbp = vbp;
        this.customizer = customizer;
        this.prop = prop;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (prop != null) {
            //DesignContext[] contexts = prop.getDesignBean().getDesignContext().getProject().getDesignContexts();
            DesignContext[] contexts = getDesignContexts(prop.getDesignBean());

            rsLabel.setText(bundle.getMessage("chooseRsToBind", prop.getDesignBean().getInstanceName())); //NOI18N

//            // first check to see if the comp is in a UIData bound to a RowSet
//            DesignBean tableBean = scanForParent(UIData.class, prop.getDesignBean());
//            if (tableBean != null) {
//                //!JOE do this later...
//            }

            // find the current value
            String currentRef = prop.getValueSource();
            newRef = currentRef;
            String contextName = null;
            String rsName = null;
            String valueField = null;
            if (currentRef != null && currentRef.startsWith("#{") && //NOI18N
                currentRef.endsWith("}")) { //NOI18N

                ArrayList parts = new ArrayList();
                StringTokenizer st = new StringTokenizer(currentRef.substring(2,
                    currentRef.length() - 1), ".");
                while (st.hasMoreElements()) {
                    parts.add(st.nextElement());
                }

                if (parts.size() > 0) {
                    contextName = "" + parts.get(0);
                }
                if (parts.size() > 1) {
                    rsName = "" + parts.get(1);
                }
                if (parts.size() > 2) {
                    String val = String.valueOf(parts.get(2));
                    if (val.startsWith("currentRow['") && val.endsWith("']")) { //NOI18N
                        valueField = val.substring("currentRow['".length(), val.length() - 2); //NOI18N
                    }
                }
            }

            // then scan for all rowsets
            ArrayList rowsetBeans = new ArrayList();
            for (int i = 0; i < contexts.length; i++) {
                String scope = (String)contexts[i].getContextData(Constants.ContextData.SCOPE);
                if ("request".equals(scope) && contexts[i] != prop.getDesignBean().getDesignContext()) { //NOI18N
                    continue;
                }
                DesignBean[] rsbs = contexts[i].getBeansOfType(RowSet.class);
                for (int j = 0; j < rsbs.length; j++) {
                    rowsetBeans.add(rsbs[j]);
                }
            }

            if (rowsetBeans.size() > 0) {
                for (int i = 0; i < rowsetBeans.size(); i++) {
                    DesignBean rsBean = (DesignBean)rowsetBeans.get(i);
                    try {
                        RowSet rs = (RowSet)rsBean.getInstance();
                        if (rs != null) {
                            if (rsIndex < 0) {
                                DesignContext rsc = rsBean.getDesignContext();
                                if (rsc instanceof FacesDesignContext) {
                                    if (((FacesDesignContext)rsc).getReferenceName().equals(
                                        contextName) &&
                                        rsBean.getInstanceName().equals(rsName)) {
                                        rsIndex = i;
                                    }
                                } else {
                                    if (rsc.getDisplayName().equals(contextName) &&
                                        rsBean.getInstanceName().equals(rsName)) {
                                        rsIndex = i;
                                    }
                                }
                            }

                            ResultSetMetaData rsmd = rs.getMetaData();

                            DefaultListModel dlm = new DefaultListModel();
                            ListItem none = new ListItem();
                            none.display = bundle.getMessage("noneBrackets"); //NOI18N
                            dlm.addElement(none);

                            rsCombo.addItem(rsBean);
                            listModelHash.put(rsBean, dlm);

                            int cols = rsmd.getColumnCount();
                            for (int c = 1; c <= cols; c++) {
                                ListItem li = new ListItem();
                                li.table = rsmd.getTableName(c);
                                li.column = rsmd.getColumnName(c);
                                //li.display = li.table + "." + li.column; //NOI18N
                                li.display = (li.table == null || li.table.length() == 0) ? li.column : li.table + "." + li.column; //NOI18N
                                dlm.addElement(li);
                                if (rsIndex > -1) {
                                    if (valueIndex <= 0 && li.column.equals(valueField)) {
                                        valueIndex = c;
                                    }
                                }
                            }
                        }
                    } catch (Exception x) {
                        System.err.println(bundle.getMessage("getMetaDataException")); //NOI18N
                        x.printStackTrace();
                    }
                }
            } else {
                this.removeAll();
                this.add(rsLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST,
                    GridBagConstraints.NONE, new Insets(8, 8, 2, 8), 0, 0));
                this.add(noneText, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER,
                    GridBagConstraints.BOTH, new Insets(0, 8, 8, 8), 0, 0));
            }
        }
        updateLists();
        if (rsIndex > -1) {
            rsCombo.setSelectedIndex(rsIndex);
        }
        if (valueIndex > -1) {
            valueList.setSelectedIndex(valueIndex);
        }
        initializing = false;
        repaint(100);
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

    private void jbInit() throws Exception {
        noneText.setEditable(false);
        noneText.setFont(rsLabel.getFont());
        noneText.setBorder(UIManager.getBorder("TextField.border")); //NOI18N
        noneText.setText(bundle.getMessage("noRss")); //NOI18N

        rsLabel.setText(bundle.getMessage("chooseRsToBindSimple")); //NOI18N
        valueListLabel.setText(bundle.getMessage("dataField")); //NOI18N
        valueListScroll.setPreferredSize(new Dimension(200, 200));

        rsCombo.setRenderer(new RSComboRenderer());

        this.setPreferredSize(new Dimension(400, 200));
        this.setLayout(gridbag);

        this.add(rsLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.NONE, new Insets(8, 8, 2, 8), 0, 0));
        this.add(rsCombo, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, new Insets(0, 8, 8, 8), 0, 0));

        this.add(valueListLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST,
            GridBagConstraints.NONE, new Insets(0, 8, 2, 8), 0, 0));
        this.add(valueListScroll, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0,
            GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 8, 8, 8), 0, 0));

        rsCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                updateLists();
            }
        });
        valueList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                updateSelection();
            }
        });
    }

    protected void updateLists() {
        Object o = listModelHash.get(rsCombo.getSelectedItem());
        if (o instanceof ListModel) {
            ListModel lm = (ListModel)o;
            valueList.setModel(lm);
        }
        updateSelection();
    }

    protected void updateSelection() {
        if (initializing) {
            return;
        }
        DesignBean rsBean = null;
        String valName = null;
        Object o = rsCombo.getSelectedItem();
        if (o instanceof DesignBean) {
            rsBean = (DesignBean)o;
        }
        o = valueList.getSelectedValue();
        if (o instanceof ListItem) {
            valName = ((ListItem)o).column;
        }
        String ref = calcValueRef(rsBean, valName);
        if (vbp != null) {
            vbp.setValueBinding(ref);
        } else if (customizer != null) {
            newRef = ref;
            customizer.firePropertyChange();
        } else {
            prop.setValue(ref);
        }
    }

    public boolean isModified() {
        String curRef = prop.getValueSource();
        return!((curRef == null && newRef == null) || (curRef != null && curRef.equals(newRef)));
        //return!(((curRef == null || curRef.length() == 0) && (newRef == null || newRef.length() == 0)) || (curRef != null && curRef.equals(newRef)));
    }

    String newRef = null;
    public void customizerApply() {
        prop.setValueSource(newRef);
        //newRef = null;
    }

    protected String calcValueRef(DesignBean rsBean, String valueColumn) {
        if (rsBean == null || valueColumn == null || valueColumn.length() == 0) {
            //return ""; //NOI18N
        	return null;
        }
        DesignContext c = rsBean.getDesignContext();
        if (c instanceof FacesDesignContext) {
            return "#{" + ((FacesDesignContext)c).getReferenceName() + "." + rsBean.getInstanceName() +
                ".currentRow['" + valueColumn + "']}"; //NOI18N
        }
        return "#{" + c.getDisplayName() + "." + rsBean.getInstanceName() + ".currentRow['" +
            valueColumn + "']}"; //NOI18N
    }

    class RSComboRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            DesignBean rsBean = (DesignBean)value;
            this.setText(bundle.getMessage("rsDisplayTextPattern", rsBean.getInstanceName(),
                rsBean.getDesignContext().getDisplayName())); //NOI18N
            return this;
        }
    }
}
