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
package org.netbeans.modules.visualweb.propertyeditors.binding.data;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.sun.data.provider.DataProvider;
import com.sun.data.provider.FieldKey;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.event.DesignContextListener;
import com.sun.rave.designtime.Position;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.faces.FacesDesignProject;
import org.netbeans.modules.visualweb.propertyeditors.binding.BindingTargetCallback;
import org.netbeans.modules.visualweb.propertyeditors.binding.PropertyBindingHelper;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;

// Modified to support Add Data Provider from this panel- Winston
// XXX Lots of duplication between BindOptionsToDataProviderPanel & BindValueToDataProviderPanel
// Needs clean up

/*
 * @authors
 *            Winston Prakash (cleaned up lots of mess)
 */

public class BindValueToDataProviderPanel extends DataBindingPanel implements DesignContextListener {

    private static final Bundle bundle = Bundle.getBundle(BindValueToDataProviderPanel.class);

    protected JTextPane noneText = new JTextPane();

    protected JLabel dpLabel = new JLabel();
    protected JComboBox dpCombo = new JComboBox();

    protected HashMap listModelHash = new HashMap();
    protected HashMap valSelectedItemHash = new HashMap();

    protected JLabel valueListLabel = new JLabel();
    protected JList fieldList = new JList();
    protected JScrollPane valueListScroll = new JScrollPane(fieldList);

    protected GridBagLayout gridbag = new GridBagLayout();
    protected String parentName = null;

    protected boolean initializing = true;

    private JButton addDataProviderButton = new JButton();

    DesignContext[] contexts;

    private List brokenDataProvider = new ArrayList();

    DesignProperty designProperty;

    DesignBean selectedBean = null;

    protected ListItem val_none = new ListItem();

    private String newExpression = null;

    protected class ListItem {
        public String display;
        public FieldKey field;
        public String type;
        public String toString() {
            return field != null
                    ? "<html><b>" + field.getDisplayName() + "</b> &nbsp; <i>" + type + "</i></html>"
//                ? field.getDisplayName() + "  (" + type + ")"
                    : display;
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
    
    boolean stopDataProviderThread = false;
    
    public BindValueToDataProviderPanel(BindingTargetCallback callback, DesignProperty prop) {
        super(callback, prop);
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // For Shortfin we removed the Server Navigator window.
        // Add Data provider dialogs depends on it. So hide it for Shortfin - Winston
        addDataProviderButton.setVisible(false);
        
        dpLabel.setLabelFor(dpCombo);
        dpCombo.getAccessibleContext().setAccessibleName(bundle.getMessage("DP_COMBO_ACCESS_NAME"));
        dpCombo.getAccessibleContext().setAccessibleDescription(bundle.getMessage("DP_COMBO_ACCESS_DESC"));
        addDataProviderButton.getAccessibleContext().setAccessibleName(bundle.getMessage("ADD_DP_BUTTON_ACCESS_NAME"));
        valueListLabel.setLabelFor(fieldList);
        fieldList.getAccessibleContext().setAccessibleName(bundle.getMessage("FIELD_LIST_ACCESS_NAME"));
        fieldList.getAccessibleContext().setAccessibleDescription(bundle.getMessage("FIELD_LIST_ACCESS_DESC"));
        designProperty = prop;
        dpCombo.addItem(bundle.getMessage("dpRetrievingMessage"));
        val_none.display = bundle.getMessage("noneBrackets"); //NOI18N
        Thread dataProvideNodeThread = new Thread(new Runnable() {
            public void run(){
                updateDataProvider(designProperty);
            }
        });
        dataProvideNodeThread.setPriority(Thread.MIN_PRIORITY);
        dataProvideNodeThread.start();
        addComponentListener(new ComponentAdapter(){
            
            public void componentShown(ComponentEvent e){
                if (newExpression != null){
                    if((newExpression != null) && isShowing()){
                        bindingCallback.setNewExpressionText(newExpression);
                    }
                }
            }
        });
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
             
    protected void updateDataProvider(DesignProperty prop){
        if (prop != null) {
            //contexts = prop.getDesignBean().getDesignContext().getProject().getDesignContexts();
            contexts = getDesignContexts(prop.getDesignBean());
            
            dpLabel.setText(bundle.getMessage("chooseDpToBind", prop.getDesignBean().getInstanceName())); //NOI18N
            
//            // first check to see if the comp is in a UIData bound to a RowSet
//            DesignBean tableBean = scanForParent(UIData.class, prop.getDesignBean());
//            if (tableBean != null) {
//                //!JOE do this later...
//            }
            
            // find the current value
            String currentRef = prop.getValueSource();
            String contextName = null;
            String dpName = null;
            String valueField = null;
            // "#{Page1.personDataProvider['NAME']}"
            if (currentRef != null && currentRef.startsWith("#{") && //NOI18N
                    currentRef.endsWith("}")) { //NOI18N
                
                ArrayList parts = new ArrayList();
                StringTokenizer st = new StringTokenizer(currentRef.substring(2,
                        currentRef.length() - 1), ".[");
                while (st.hasMoreElements()) {
                    parts.add(st.nextElement());
                }
                
                if (parts.size() > 0) {
                    contextName = "" + parts.get(0);
                }
                if (parts.size() > 1) {
                    dpName = "" + parts.get(1);
                }
                /*if (parts.size() > 2) {
                    String val = String.valueOf(parts.get(2));
                    if (val.startsWith("'") && val.endsWith("']")) { //NOI18N
                        valueField = val.substring(1, val.length() - 2); //NOI18N
                    }
                }*/
                
                //XXX - revisit after EA - WInston
                int startIndex = currentRef.indexOf('[');
                int endIndex = currentRef.indexOf(']');
                if ((startIndex != -1) && (endIndex != -1)){
                    valueField =  currentRef.substring(startIndex + 2, endIndex - 1);
                }
                /*System.out.println("-----------------------------------");
                System.out.println("-- " + contextName);
                System.out.println("-- " + dpName);
                System.out.println("-- " + valueField);
                System.out.println("-----------------------------------");*/
            }
            
            // then scan for all dataproviders
            ArrayList dpBeans = new ArrayList();
            for (int i = 0; i < contexts.length; i++) {
                String scope = (String)contexts[i].getContextData(Constants.ContextData.SCOPE);
                if ("request".equals(scope) && contexts[i] != prop.getDesignBean().getDesignContext()) { //NOI18N
                    continue;
                }
                DesignBean[] dpbs = contexts[i].getBeansOfType(DataProvider.class);
                for (int j = 0; j < dpbs.length; j++) {
                    dpBeans.add(dpbs[j]);
                }
            }
            
            if (dpBeans.size() > 0) {
                for (int i = 0; i < dpBeans.size(); i++) {
                    if (stopDataProviderThread) return;
                    DesignBean dpBean = (DesignBean)dpBeans.get(i);
                    DataProvider dp = (DataProvider)dpBean.getInstance();
                    if (dp != null) {
                        if (selectedBean == null) {
                            DesignContext dpc = dpBean.getDesignContext();
                            if (dpc instanceof FacesDesignContext) {
                                if (((FacesDesignContext)dpc).getReferenceName().equals(
                                        contextName) &&
                                        dpBean.getInstanceName().equals(dpName)) {
                                    selectedBean = dpBean;
                                }
                            } else {
                                if (dpc.getDisplayName().equals(contextName) &&
                                        dpBean.getInstanceName().equals(dpName)) {
                                    selectedBean = dpBean;
                                }
                            }
                        }
                        
                        DefaultListModel dlm = new DefaultListModel();
                        dlm.addElement(val_none);
                        
                        try{
                            FieldKey[] fields = dp.getFieldKeys();
                            for (int f = 0; f < fields.length; f++) {
                                ListItem li = new ListItem();
                                li.field = fields[f];
                                if((dp.getType(fields[f]) != null) && dp.getType(fields[f]).toString().indexOf("java.lang.Class") == -1){
                                    li.type = PropertyBindingHelper.getPrettyTypeName(dp.getType(fields[f]).getName());
                                    dlm.addElement(li);
                                    if(dpBean == selectedBean){
                                        String liColumnEscapeApos = li.field.getFieldId().replaceAll("\\'", "\\\\'");   //NOI18N
                                        if (!valSelectedItemHash.containsKey(dpBean) && liColumnEscapeApos.equals(valueField)) {
                                            valSelectedItemHash.put(dpBean, li);
                                        }
                                    }
                                }
                            }
                        }catch(Exception exc){
                            ErrorManager.getDefault().notify(exc);
                            brokenDataProvider.add(dpBean);
                        }
                        if (!valSelectedItemHash.containsKey(dpBean)){
                            valSelectedItemHash.put(dpBean, val_none);
                        }
                        listModelHash.put(dpBean, dlm);
                    }
                }
            }
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run(){
                dpCombo.removeAllItems();
                Iterator iter = listModelHash.keySet().iterator();
                while (iter.hasNext()){
                    dpCombo.addItem(iter.next());
                }
                attachListeners();
                if (dpCombo.getItemCount() > 0){
                    if (selectedBean != null){
                        dpCombo.setSelectedItem(selectedBean);
                        updateLists();
                    }else{
                        dpCombo.setSelectedIndex(0);
                        fieldList.setModel((ListModel) listModelHash.get(dpCombo.getSelectedItem()));
                    }
                }
                repaint(100);
            }
        });
    }
    
    public void addNotify(){
        super.addNotify();
        //contexts = designProperty.getDesignBean().getDesignContext().getProject().getDesignContexts();
        contexts = getDesignContexts(designProperty.getDesignBean());
        for (int i = 0; i < contexts.length; i++) {
            //System.out.println("Adding context Listeners - " + contexts[i].getDisplayName());
            contexts[i].addDesignContextListener(BindValueToDataProviderPanel.this);
        }
    }
    
    public void removeNotify(){
        //contexts = designProperty.getDesignBean().getDesignContext().getProject().getDesignContexts();
        contexts = getDesignContexts(designProperty.getDesignBean());
        // Make sure the added listeners to contexts are removed
        // Sigh! if only design time provide a neater way to clean up.
        for (int i = 0; i < contexts.length; i++) {
            //System.out.println("Removing context Listeners - " + contexts[i].getDisplayName());
            contexts[i].removeDesignContextListener(this);
        }
        stopDataProviderThread = true;
        super.removeNotify();
    }
    
    private void jbInit() throws Exception {
        noneText.setEditable(false);
        noneText.setFont(dpLabel.getFont());
        noneText.setBorder(UIManager.getBorder("TextField.border")); //NOI18N
        noneText.setText(bundle.getMessage("noDps")); //NOI18N
        
        dpLabel.setText(bundle.getMessage("chooseDpToBindSimple")); //NOI18N
        dpLabel.setDisplayedMnemonic(bundle.getMessage("chooseDpToBindSimpleDisplayedMnemonic").charAt(0)); //NOI18N
        Mnemonics.setLocalizedText(valueListLabel, bundle.getMessage("dataField"));
        valueListScroll.setPreferredSize(new Dimension(200, 200));
        
        dpCombo.setRenderer(new DPComboRenderer());
        
        this.setPreferredSize(new Dimension(400, 200));
        this.setLayout(gridbag);
        
        Mnemonics.setLocalizedText(addDataProviderButton, bundle.getMessage("ADD_DP_BUTTON_LBL"));
        addDataProviderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                new AddDataProviderDialog().showDialog();
            }
        });
        
        this.add(dpLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(8, 8, 2, 8), 0, 0));
        this.add(dpCombo, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 8, 8, 8), 0, 0));
        this.add(addDataProviderButton, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 8, 8, 8), 0, 0));
        
        this.add(valueListLabel, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 8, 2, 8), 0, 0));
        this.add(valueListScroll, new GridBagConstraints(0, 3, 2, 1, 1.0, 1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(0, 8, 8, 8), 0, 0));
        
        
    }
    
    /**
     * Private method to attach listeners to Data provider combobox and
     * value list items  after initlaizing the data providers
     */
    private void attachListeners(){
        dpCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.SELECTED){
                    updateLists();
                }
            }
        });
        fieldList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()){
                    updateSelection();
                }
            }
        });
    }
    
    public String getDataBindingTitle() {
        return bundle.getMessage("bindToDp"); // NOI18N
    }
    
    protected void updateLists() {
        ListModel vlm = (ListModel) listModelHash.get(dpCombo.getSelectedItem());
        fieldList.setModel(vlm);
        fieldList.setSelectedValue(valSelectedItemHash.get(dpCombo.getSelectedItem()), true);
    }
    
    protected void updateSelection() {
        FieldKey valField = null;
        ListItem valItem = (ListItem)fieldList.getSelectedValue();
        if(valItem != null){
            valField = valItem.field;
        }
        if (valField != null){
            newExpression = calcValueRef((DesignBean)dpCombo.getSelectedItem(), valField.getFieldId());
            
            if((newExpression != null) && isShowing()){
                bindingCallback.setNewExpressionText(newExpression);
            }
        }
    }
    
    protected String calcValueRef(DesignBean dpBean, String fieldId) {
        if (dpBean == null || fieldId == null || fieldId.length() == 0) {
            //return ""; //NOI18N
            return null;
        }
        DesignContext c = dpBean.getDesignContext();
        if (c instanceof FacesDesignContext) {
            return "#{" + ((FacesDesignContext)c).getReferenceName() + "." + dpBean.getInstanceName() +
                    ".value['" + fieldId + "']}"; //NOI18N
        }
        return "#{" + c.getDisplayName() + "." + dpBean.getInstanceName() + ".value['" +
                fieldId + "']}"; //NOI18N
    }
    
    class DPComboRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if(value instanceof DesignBean){
                DesignBean dpBean = (DesignBean)value;
                if(dpBean != null){
                    if(brokenDataProvider.contains(dpBean)){
                        setText(bundle.getMessage("dpErrorDisplayTextPattern", dpBean.getInstanceName(),
                                dpBean.getDesignContext().getDisplayName())); //NOI18N
                    }else{
                        setText(bundle.getMessage("dpDisplayTextPattern", dpBean.getInstanceName(),
                                dpBean.getDesignContext().getDisplayName())); //NOI18N
                    }
                }
            }else if(value instanceof String){
                setText((String) value);
            }
            return this;
        }
    }
    
// Implementation of DesignContextListener
    
    public void beanCreated(DesignBean designBean){
        if (designBean.getInstance() instanceof DataProvider){
            //System.out.println("Bean Created - " + designBean.getInstanceName());
        }
    }
    
    public void instanceNameChanged(DesignBean designBean, String oldInstanceName){
        if (designBean.getInstance() instanceof DataProvider){
            //System.out.println("Instance Name changed - " + oldInstanceName + " to " + designBean.getInstanceName());
            dpCombo.repaint();
        }
    }
    
    public void propertyChanged(DesignProperty prop, Object oldValue){
        if ((prop.getDesignBean().getInstance() instanceof DataProvider)){
            //System.out.println("Bean property Changed - "  + prop.getDesignBean().getInstanceName());
            //System.out.println("Property Name - "  + prop.getPropertyDescriptor().getDisplayName());
            if (prop.getPropertyDescriptor().getName().equals("CachedRowSet")){
                DataProvider dp = (DataProvider)prop.getDesignBean().getInstance();
                if (dp != null) {
                    DefaultListModel dlm = new DefaultListModel();
                    dlm.addElement(val_none);
                    
                    listModelHash.put(prop.getDesignBean(), dlm);
                    try{
                        FieldKey[] fields = dp.getFieldKeys();
                        for (int f = 0; f < fields.length; f++) {
                            ListItem li = new ListItem();
                            li.field = fields[f];
                            li.type = PropertyBindingHelper.getPrettyTypeName(dp.getType(fields[f]).getName());
                            dlm.addElement(li);
                        }
                    }catch(Exception exc){
                        ErrorManager.getDefault().notify(exc);
                    }
                    dpCombo.addItem(prop.getDesignBean());
                    dpCombo.setSelectedItem(prop.getDesignBean());
                }
            }
        }
    }
    
    public void beanChanged(DesignBean designBean){
    }
    
    public void contextActivated(DesignContext context){}
    
    public void contextDeactivated(DesignContext context){}
    
    public void contextChanged(DesignContext context){}
    
    public void beanDeleted(DesignBean designBean){}
    
    public void beanMoved(DesignBean designBean, DesignBean oldParent, Position pos){}
    
    public void beanContextActivated(DesignBean designBean){}
    
    public void beanContextDeactivated(DesignBean designBean){}
    
    public void eventChanged(DesignEvent event){}
}
