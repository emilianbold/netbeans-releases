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

import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
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
import java.util.List;
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
import javax.swing.ListSelectionModel;
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

public class ColumnDataBindingPanel extends JPanel {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(ColumnDataBindingPanel.class);

    protected DesignProperty prop;
    protected ValueBindingPanel vbp;
    protected ColumnDataBindingCustomizer customizer;

    protected JLabel rsLabel = new JLabel();
    protected JComboBox rsCombo = new JComboBox();

    protected HashMap valListModelHash = new HashMap();
    protected HashMap dispListModelHash = new HashMap();

    protected JLabel valueListLabel = new JLabel();
    protected JList valueList = new JList();
    protected JScrollPane valueListScroll = new JScrollPane(valueList);

    protected JLabel displayListLabel = new JLabel();
    protected JList displayList = new JList();
    protected JScrollPane displayListScroll = new JScrollPane(displayList);

//    protected JLabel descrListLabel = new JLabel();
//    protected JList descrList = new JList();
//    protected JScrollPane descrListScroll = new JScrollPane(descrList);

    protected GridBagLayout gridbag = new GridBagLayout();
    protected String parentName = null;

    protected JTextPane noneText = new JTextPane();

    protected int modelIndex = -1;
    protected int valueIndex = 0;
    protected int displayIndex = 0;
//    protected int descrIndex = 0;

    protected int valueType = 0;

    protected boolean initializing = true;

    protected class ListItem {
        public String display;
        public String table;
        public String column;
        public int sqlType = 0;
        public boolean isNone = false;
        public String toString() {
            return display;
        }
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

    public ColumnDataBindingPanel(ValueBindingPanel vbp, ColumnDataBindingCustomizer customizer,
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

//            // first check to see if the comp is in a UIData bound to a RowSet
//            DesignBean tableBean = scanForParent(UIData.class, prop.getDesignBean());
//            if (tableBean != null) {
//                //!JOE do this later...
//            }

            // find the current value
            String currentRef = prop.getValueSource();
            newRef = currentRef;
            String contextName = null;
            String modelName = null;
            String valueField = null;
            String displayField = null;
//            String descrField = null;
            if (currentRef != null && currentRef.startsWith("#{") && //NOI18N
                currentRef.endsWith("}")) { //NOI18N

                List parts = new ArrayList();
                /*
                StringTokenizer st = new StringTokenizer(currentRef.substring(2,
                        currentRef.length() - 1), ".");
                    while (st.hasMoreElements()) {
                        parts.add(st.nextElement());
                    }
                */

                //MBOHM fix 5086833
                //there could be internal dots within, say, the selectItems['travel.person.personid,travel.person.name']
                String strippedRef = currentRef.substring(2, currentRef.length() - 1);
                int bracesOpen = 0, currStart = 0;
                String part;
                for (int i = 0; i < strippedRef.length(); i++) {
                	char c = strippedRef.charAt(i);
                	if (c == '[') {
                		bracesOpen++;
                	}
                	else if (c == ']') {
                		bracesOpen--;
                	}
                	else if (c == '.' && bracesOpen < 1) {
            			part = strippedRef.substring(currStart, i);
            			if (part.length() > 0) {
            				parts.add(part);
            			}
            			currStart = i + 1;
                	}
                }
                //get the remaining stuff after the last period
                if (currStart < strippedRef.length()) {
                	part = strippedRef.substring(currStart);
                	parts.add(part);
                }

                if (parts.size() > 0) {
                    contextName = "" + parts.get(0);
                }
                if (parts.size() > 1) {
                    modelName = "" + parts.get(1);
                }
                if (parts.size() > 2 && ((String)parts.get(2)).startsWith("selectItems['")) { //NOI18N
                    String siText = "" + parts.get(2);
                    String fieldList = siText.substring(siText.indexOf(
                        "selectItems['") + "selectItems['".length()); //NOI18N
                    fieldList = fieldList.substring(0, fieldList.indexOf("']")); //NOI18N
                    
                    ArrayList fields = new ArrayList();
                    /*
                    StringTokenizer st2 = new StringTokenizer(fieldList, ","); //NOI18N
                    while (st2.hasMoreTokens()) {
                        fields.add(st2.nextToken());
                    }
                    */
                    
                    //MBOHM fix 5086833
                    //could have internal commas, in say, selectItems['employee.employeeid, employee.firstname || \' , \' || employee.lastname']
                    boolean quoteOpen = false;
                    currStart = 0;
                    String field;
                    for (int i = 0; i < fieldList.length(); i++) {
                    	char c = fieldList.charAt(i);
                    	if (c == '\'') {
                    		quoteOpen = !quoteOpen;
                    	}
                    	else if (c == ',' && !quoteOpen) {
                			field = fieldList.substring(currStart, i);
                			if (field.length() > 0) {
                				fields.add(field);
                			}
                			currStart = i + 1;
                    	}
                    }
                    //get the remaining stuff after the last period
		            if (currStart < fieldList.length()) {
		            	field = fieldList.substring(currStart);
		            	fields.add(field);
		            }
                    

                    valueField = fields.size() > 0 ? "" + fields.get(0) : null; //NOI18N
                    displayField = fields.size() > 1 ? "" + fields.get(1) : null; //NOI18N
//                    descrField = fields.size() > 2 ? "" + fields.get(2) : null;
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
                    DesignBean rsb = (DesignBean)rowsetBeans.get(i);
                    try {
                        if (modelIndex < 0) {
                            DesignContext rsc = rsb.getDesignContext();
                            if (rsc instanceof FacesDesignContext) {
                                if (((FacesDesignContext)rsc).getReferenceName().equals(contextName) &&
                                    rsb.getInstanceName().equals(modelName)) {
                                    modelIndex = i;
                                }
                            } else {
                                if (rsc.getDisplayName().equals(contextName) &&
                                    rsb.getInstanceName().equals(modelName)) {
                                    modelIndex = i;
                                }
                            }
                        }
                        RowSet rs = (RowSet)rsb.getInstance();
                        if (rs != null) {
                            ResultSetMetaData rsmd = rs.getMetaData();

                            DefaultListModel val_dlm = new DefaultListModel();
                            ListItem val_none = new ListItem();
                            val_none.display = bundle.getMessage("noneBrackets"); //NOI18N
                            val_none.isNone = true;
                            val_dlm.addElement(val_none);

                            DefaultListModel disp_dlm = new DefaultListModel();
                            ListItem disp_none = new ListItem();
                            disp_none.display = bundle.getMessage("useValueBrackets"); //NOI18N
                            disp_dlm.addElement(disp_none);

                            rsCombo.addItem(rsb);
                            valListModelHash.put(rsb, val_dlm);
                            dispListModelHash.put(rsb, disp_dlm);

                            int cols = rsmd.getColumnCount();
                            for (int c = 1; c <= cols; c++) {
                                ListItem li = new ListItem();
                                li.table = rsmd.getTableName(c);
                                li.column = rsmd.getColumnName(c);
                                li.sqlType = rsmd.getColumnType(c);
                                li.display = (li.table == null || li.table.length() == 0) ? li.column : li.table + "." + li.column; //NOI18N
                                val_dlm.addElement(li);
                                disp_dlm.addElement(li);
                                if (modelIndex > -1) {
                                	//MBOHM fix 5086833
                                	String liColumnEscapeApos = li.column.replaceAll("\\'", "\\\\'");	//NOI18N
                                    if (valueIndex <= 0 && liColumnEscapeApos.equals(valueField)) {
                                        valueIndex = c;
                                    }
                                    if (displayIndex <= 0 && liColumnEscapeApos.equals(displayField)) {
                                        displayIndex = c;
                                    }
//                                    if (descrIndex <= 0 && liColumnEscapeApos.equals(descrField)) {
//                                        descrIndex = c;
//                                    }
                                }
                            }
                        }
                    } catch (Exception x) {
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
        if (modelIndex > -1) {
            rsCombo.setSelectedIndex(modelIndex);
        }
        if (valueIndex > -1) {
        	//MBOHM fix 5073036
        	ListModel lm = valueList.getModel();
        	//if <none> is selected and the list has more than one element in it, select the second one
        	if (valueIndex == 0 && lm != null && lm.getSize() > 1) {
        		valueIndex = 1;
        	}
            valueList.setSelectedIndex(valueIndex);
        }
        if (displayIndex > -1) {
            displayList.setSelectedIndex(displayIndex);
        }
//        if (descrIndex > -1) {
//            descrList.setSelectedIndex(descrIndex);
//        }
        initializing = false;
        updateSelection();
        repaint(100);
    }

    private void jbInit() throws Exception {

    	valueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	displayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        noneText.setEditable(false);
        noneText.setFont(rsLabel.getFont());
        noneText.setBorder(UIManager.getBorder("TextField.border")); //NOI18N
        noneText.setText(bundle.getMessage("noRss")); //NOI18N

        rsLabel.setText(bundle.getMessage("chooseFieldsForItems")); //NOI18N

        valueListLabel.setText(bundle.getMessage("valField")); //NOI18N
        displayListLabel.setText(bundle.getMessage("displayField")); //NOI18N
//        descrListLabel.setText("Description field:");
        valueListScroll.setPreferredSize(new Dimension(200, 200));

        rsCombo.setRenderer(new RSComboRenderer());

        this.setPreferredSize(new Dimension(400, 200));
        this.setLayout(gridbag);

        this.add(rsLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.NONE, new Insets(8, 8, 2, 8), 0, 0));
        this.add(rsCombo, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, new Insets(0, 8, 8, 8), 0, 0));

        this.add(valueListLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST,
            GridBagConstraints.NONE, new Insets(0, 8, 2, 8), 0, 0));
        this.add(valueListScroll, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0,
            GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 8, 8, 8), 0, 0));

        this.add(displayListLabel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST,
            GridBagConstraints.NONE, new Insets(0, 0, 2, 8), 0, 0));
        this.add(displayListScroll, new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0,
            GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 0, 8, 8), 0, 0));

//        this.add(descrListLabel, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
//            GridBagConstraints.NONE, new Insets(0, 0, 2, 8), 0, 0));
//        this.add(descrListScroll, new GridBagConstraints(2, 3, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
//            GridBagConstraints.BOTH, new Insets(0, 0, 8, 8), 0, 0));

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
        displayList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                updateSelection();
            }
        });
//        descrList.addListSelectionListener(new ListSelectionListener() {
//            public void valueChanged(ListSelectionEvent e) {
//                updateSelection();
//            }
//        });
    }

    protected void updateLists() {
        updateSelection();
        Object o = valListModelHash.get(rsCombo.getSelectedItem());
        if (o instanceof ListModel) {
            ListModel lm = (ListModel)o;
            valueList.setModel(lm);
        }
        o = dispListModelHash.get(rsCombo.getSelectedItem());
        if (o instanceof ListModel) {
            ListModel lm = (ListModel)o;
            displayList.setModel(lm);
        }
        updateSelection();
    }

    protected void updateSelection() {
        if (initializing) {
            return;
        }

        DesignBean rsBean = null;
        String valName = null;
        String dispName = null;
        String descName = null;

        Object o = rsCombo.getSelectedItem();
        if (o instanceof DesignBean) {
            rsBean = (DesignBean)o;
        }
        o = valueList.getSelectedValue();
        if (o instanceof ListItem) {
            valName = ((ListItem)o).column;
            this.valueType = ((ListItem)o).sqlType;
        } else {
            this.valueType = 0;
        }
        if (valName != null) {	//MBOHM: use this to prevent a selection on display field list when value field list selection is null
	        o = displayList.getSelectedValue();
	        if (o instanceof ListItem) {
	            dispName = ((ListItem)o).column;
	        }
	        if (displayList.isSelectionEmpty()) {
	        	displayList.setSelectedIndex(0);
	        }
        } else {
        	displayList.clearSelection();
        }//MBOHM
//        o = descrList.getSelectedValue();
//        if (o instanceof ListItem) {
//            descName = ((ListItem)o).column;
//        }

        displayListLabel.setEnabled(valName != null);
        displayList.setEnabled(valName != null);

        String ref = calcValueRef(rsBean, valName, dispName); //, descName);

        if (vbp != null) {
            vbp.setSelectItemsValueType(valueType);
            vbp.setValueBinding(ref);
        } else if (customizer != null) {
            newRef = ref;
            customizer.firePropertyChange();
        } else {
            prop.setValueSource(ref);
        }
    }

    public boolean isModified() {
        String curRef = prop.getValueSource();
        return!((curRef == null && newRef == null) || (curRef != null && curRef.equals(newRef)));
    }

    String newRef = null;
    public void customizerApply() {
        // start with the UISelectItems bean
        //MBOHM for 6194849, no harm in calling this line here
        newRef = HtmlDesignInfoBase.maybeSetupDefaultSelectItems(prop.getDesignBean(), newRef);
        prop.setValueSource(newRef);
        //MBOHM 6194849 //HtmlDesignInfoBase.maybeSetupConverter(prop.getDesignBean(), valueType);
        //newRef = null;
    }

    protected String calcValueRef(DesignBean rsBean, String valueColumn, String labelColumn) { //, String descrColumn) {
        if (rsBean == null || valueColumn == null || valueColumn.length() == 0) {
            return null; //NOI18N
        }

        String ref = valueColumn + (labelColumn != null ? "," + labelColumn : ""); // + (descrColumn != null ? "_" + descrColumn : "");        //NOI18N
        //MBOHM fix 5086833
        ref = ref.replaceAll("\\'", "\\\\'");	//NOI18N
        DesignContext c = rsBean.getDesignContext();
        if (c instanceof FacesDesignContext) {
            return "#{" + ((FacesDesignContext)c).getReferenceName() + "." + rsBean.getInstanceName() +
                ".selectItems['" + ref + "']}"; //NOI18N
        } else {
            return "#{" + c.getDisplayName() + "." + rsBean.getInstanceName() + ".selectItems['" +
                ref + "']}"; //NOI18N
        }
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
