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
import javax.swing.ListSelectionModel;
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
// Lots of duplication between BindOptionsToDataProviderPanel & BindValueToDataProviderPanel
// XXX Needs clean up

/*
 * @authors
 *            Winston Prakash (cleaned up lots of mess)
 */

public class BindOptionsToDataProviderPanel extends DataBindingPanel implements DesignContextListener{

    private static final Bundle bundle = Bundle.getBundle(BindOptionsToDataProviderPanel.class);

    protected JLabel dpLabel = new JLabel();
    protected JComboBox dpCombo = new JComboBox();

    protected HashMap valListModelHash = new HashMap();
    protected HashMap dispListModelHash = new HashMap();

    protected HashMap valSelectedItemHash = new HashMap();
    protected HashMap dispSelectedItemHash = new HashMap();

    protected JLabel valueListLabel = new JLabel();
    protected JList valueList = new JList();
    protected JScrollPane valueListScroll = new JScrollPane(valueList);

    protected JLabel displayListLabel = new JLabel();
    protected JList displayList = new JList();
    protected JScrollPane displayListScroll = new JScrollPane(displayList);

    protected GridBagLayout gridbag = new GridBagLayout();
    protected String parentName = null;

    protected JTextPane noneText = new JTextPane();

    private JButton addDataProviderButton = new JButton();

    DesignContext[] contexts;

    private List brokenDataProvider = new ArrayList();

    DesignProperty designProperty;
    DesignBean selectedBean = null;

    protected ListItem val_none = new ListItem();
    protected ListItem disp_none = new ListItem();

    private String newExpression = null;

    protected class ListItem {
        public String display;
        public FieldKey field;
        public String type;
        public String toString() {
            return field != null
                    ? "<html><body><b>" + field.getDisplayName() + "</b> &nbsp; <i>" + type + "</i></body></html>"
                    : display;
        }
    }
    
    boolean stopDataProviderThread = false;
    
    public BindOptionsToDataProviderPanel(BindingTargetCallback callback, DesignProperty prop) {
        super(callback, prop);
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // For Shortfin we removed the Server Navigator window.
        // Add Data provider dialogs depends on it. So hide it for Shortfin - Winston
        addDataProviderButton.setVisible(false);
        
        designProperty = prop;
        val_none.display = bundle.getMessage("noneBrackets"); //NOI18N
        disp_none.display = bundle.getMessage("useValueBrackets"); //NOI18N
        dpCombo.addItem(bundle.getMessage("dpRetrievingMessage"));
        Thread dataProviderNodeThread = new Thread(new Runnable() {
            //SwingUtilities.invokeLater(new Runnable() {
            public void run(){
                updateDataProvider(designProperty);
            }
        });
        dataProviderNodeThread.setPriority(Thread.MIN_PRIORITY);
        dataProviderNodeThread.start();
        
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
        Mnemonics.setLocalizedText(dpLabel, bundle.getMessage("chooseDpToBind", prop.getDesignBean().getInstanceName()));
        
        if (prop != null) {
            //contexts = prop.getDesignBean().getDesignContext().getProject().getDesignContexts();
            contexts = getDesignContexts(prop.getDesignBean());
            
            // find the current value
            String currentRef = prop.getValueSource();
            String contextName = null;
            String modelName = null;
            String valueField = null;
            String displayField = null;
            if (currentRef != null && currentRef.startsWith("#{") && //NOI18N
                    currentRef.endsWith("}")) { //NOI18N
                
                String optionsKey = getPropertyResolverKey();
                List parts = new ArrayList();
                
                //MBOHM fix 5086833
                //there could be internal dots within, say, the options['travel.person.personid,travel.person.name']
                String strippedRef = currentRef.substring(2, currentRef.length() - 1);
                int bracesOpen = 0, currStart = 0;
                String part;
                for (int i = 0; i < strippedRef.length(); i++) {
                    char c = strippedRef.charAt(i);
                    if (c == '[') {
                        bracesOpen++;
                    } else if (c == ']') {
                        bracesOpen--;
                    } else if (c == '.' && bracesOpen < 1) {
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
                if (parts.size() > 2 && ((String)parts.get(2)).startsWith(optionsKey + "['")) { //NOI18N
                    String siText = "" + parts.get(2);
                    String fieldList = siText.substring(siText.indexOf(
                            optionsKey + "['") + (optionsKey + "['").length()); //NOI18N
                    fieldList = fieldList.substring(0, fieldList.indexOf("']")); //NOI18N
                    
                    ArrayList fields = new ArrayList();
                    
                    //MBOHM fix 5086833
                    //could have internal commas, in say, selectItems['employee.employeeid, employee.firstname || \' , \' || employee.lastname']
                    boolean quoteOpen = false;
                    currStart = 0;
                    String field;
                    for (int i = 0; i < fieldList.length(); i++) {
                        char c = fieldList.charAt(i);
                        if (c == '\'') {
                            quoteOpen = !quoteOpen;
                        } else if (c == ',' && !quoteOpen) {
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
                }
            }
            
            // then scan for all data providers
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
                    if(stopDataProviderThread) return;
                    DesignBean dpb = (DesignBean)dpBeans.get(i);
                    try {
                        if (selectedBean == null) {
                            DesignContext dpc = dpb.getDesignContext();
                            if (dpc instanceof FacesDesignContext) {
                                if (((FacesDesignContext)dpc).getReferenceName().equals(contextName) &&
                                        dpb.getInstanceName().equals(modelName)) {
                                    selectedBean = dpb;
                                }
                            } else {
                                if (dpc.getDisplayName().equals(contextName) &&
                                        dpb.getInstanceName().equals(modelName)) {
                                    selectedBean = dpb;
                                }
                            }
                        }
                        DataProvider dp = (DataProvider)dpb.getInstance();
                        if (dp != null) {
                            DefaultListModel val_dlm = new DefaultListModel();
                            val_dlm.addElement(val_none);
                            
                            DefaultListModel disp_dlm = new DefaultListModel();
                            disp_dlm.addElement(disp_none);
                            
                            try{
                                FieldKey[] fkeys = dp.getFieldKeys();
                                for (int f = 0; f < fkeys.length; f++) {
                                    ListItem li = new ListItem();
                                    li.field = fkeys[f];
                                    Class t = dp.getType(fkeys[f]);
                                    li.type = PropertyBindingHelper.getPrettyTypeName(t.getName());
                                    val_dlm.addElement(li);
                                    disp_dlm.addElement(li);
                                    if(dpb == selectedBean){
                                        String liColumnEscapeApos = li.field.getFieldId().replaceAll("\\'", "\\\\'");   //NOI18N
                                        if (!valSelectedItemHash.containsKey(dpb) && liColumnEscapeApos.equals(valueField)) {
                                            valSelectedItemHash.put(dpb, li);
                                        }
                                        if (!dispSelectedItemHash.containsKey(dpb) && liColumnEscapeApos.equals(displayField)) {
                                            dispSelectedItemHash.put(dpb, li);
                                        }
                                    }else{
                                        if (!valSelectedItemHash.containsKey(dpb) && isTypeOf(Integer.class, "int", dp.getType(fkeys[f]))){
                                            valSelectedItemHash.put(dpb, li);
                                        }
                                        if (!dispSelectedItemHash.containsKey(dpb) && dp.getType(fkeys[f]).isAssignableFrom(String.class)){
                                            dispSelectedItemHash.put(dpb, li);
                                        }
                                    }
                                }
                            }catch(Exception exc){
                                ErrorManager.getDefault().notify(exc);
                                brokenDataProvider.add(dpb);
                            }
                            if (!valSelectedItemHash.containsKey(dpb)){
                                valSelectedItemHash.put(dpb, val_none);
                            }
                            if (!dispSelectedItemHash.containsKey(dpb)){
                                valSelectedItemHash.put(dpb, disp_none);
                            }
                            
                            valListModelHash.put(dpb, val_dlm);
                            dispListModelHash.put(dpb, disp_dlm);
                        }
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
            }
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run(){
                dpCombo.removeAllItems();
                Iterator iter = valListModelHash.keySet().iterator();
                while (iter.hasNext()){
                    dpCombo.addItem(iter.next());
                }
                attachListeners();
                if (dpCombo.getItemCount() > 0){
                    if (selectedBean != null){
                        dpCombo.setSelectedItem(selectedBean);
                    }else{
                        dpCombo.setSelectedIndex(0);
                    }
                    updateLists();
                }
                repaint(100);
            }
        });
    }
    
    private static boolean isTypeOf( Class ofType, String primitiveType, Class tobecheckType ) {
        if( tobecheckType.isAssignableFrom( ofType ) ||
                (tobecheckType.isPrimitive() && tobecheckType.getName().equals( primitiveType ) ) )
            return true;
        else
            return false;
    }
    
    public void addNotify(){
        super.addNotify();
        contexts = designProperty.getDesignBean().getDesignContext().getProject().getDesignContexts();
        for (int i = 0; i < contexts.length; i++) {
            //System.out.println("Adding context Listeners - " + contexts[i].getDisplayName());
            contexts[i].addDesignContextListener(BindOptionsToDataProviderPanel.this);
        }
    }
    
    public void removeNotify(){
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
        
        valueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        valueList.getAccessibleContext().setAccessibleName(bundle.getMessage("VALUE_FIELD_LIST_ACCESS_NAME"));
        valueList.getAccessibleContext().setAccessibleDescription(bundle.getMessage("VALUE_FIELD_LIST_ACCESS_DESC"));
        displayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        displayList.getAccessibleContext().setAccessibleName(bundle.getMessage("DISPLAY_FIELD_LIST_ACCESS_NAME"));
        displayList.getAccessibleContext().setAccessibleDescription(bundle.getMessage("DISPLAY_FIELD_LIST_ACCESS_DESC"));
        noneText.setEditable(false);
        noneText.setFont(dpLabel.getFont());
        noneText.setBorder(UIManager.getBorder("TextField.border")); //NOI18N
        noneText.setText(bundle.getMessage("noDps")); //NOI18N
        
        Mnemonics.setLocalizedText(valueListLabel, bundle.getMessage("valField"));
        valueListLabel.setLabelFor(valueList);        
        Mnemonics.setLocalizedText(displayListLabel, bundle.getMessage("displayField"));
        displayListLabel.setLabelFor(displayList);
        valueListScroll.setPreferredSize(new Dimension(200, 200));
        
        dpLabel.setLabelFor(dpCombo);
        
        dpCombo.getAccessibleContext().setAccessibleName(bundle.getMessage("DP_COMBO_ACCESS_NAME"));
        dpCombo.getAccessibleContext().setAccessibleDescription(bundle.getMessage("DP_COMBO_ACCESS_DESC"));
        dpCombo.setRenderer(new DPComboRenderer());
        
        this.setPreferredSize(new Dimension(400, 200));
        this.setLayout(gridbag);
        
        addDataProviderButton.setText(bundle.getMessage("ADD_DP_BUTTON_LBL"));
        addDataProviderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                new AddDataProviderDialog().showDialog();
            }
        });
        
        this.add(dpLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(8, 8, 2, 8), 0, 0));
        this.add(dpCombo, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 8, 8, 8), 0, 0));
        this.add(addDataProviderButton, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
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
    }
    
    /**
     * Private method to attach listeners to Data provider combobox and
     * value list items and display list items after the data providers
     */
    private void attachListeners(){
        
        dpCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.SELECTED){
                    updateLists();
                }
            }
        });
        valueList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()){
                    updateSelection();
                }
            }
        });
        displayList.addListSelectionListener(new ListSelectionListener() {
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
        ListModel vlm = (ListModel) valListModelHash.get(dpCombo.getSelectedItem());
        valueList.setModel(vlm);
        ListModel dlm = (ListModel) dispListModelHash.get(dpCombo.getSelectedItem());
        displayList.setModel(dlm);
        valueList.setSelectedValue(valSelectedItemHash.get(dpCombo.getSelectedItem()), true);
        displayList.setSelectedValue(dispSelectedItemHash.get(dpCombo.getSelectedItem()), true);
    }
    
    protected void updateSelection() {
        FieldKey valField = null;
        FieldKey dispField = null;
        ListItem valItem = (ListItem)valueList.getSelectedValue();
        ListItem dispItem = (ListItem)displayList.getSelectedValue();
        if(valItem != null){
            valField = valItem.field;
        }
        if(dispItem != null){
            dispField = dispItem.field;
        }
        
        newExpression = calcValueRef((DesignBean)dpCombo.getSelectedItem(), valField, dispField);
        
        if((newExpression != null) && isShowing()){
            bindingCallback.setNewExpressionText(newExpression);
        }
    }
    
    protected String calcValueRef(DesignBean dpBean, FieldKey valueField, FieldKey labelField) {
        if (dpBean == null || valueField == null) {
            return null;
        }
        String optionsKey = getPropertyResolverKey();
        String ref = valueField.getFieldId() + (labelField != null ? "," + labelField.getFieldId() : "");
        //MBOHM fix 5086833
        ref = ref.replaceAll("\\'", "\\\\'");   //NOI18N
        DesignContext c = dpBean.getDesignContext();
        if (c instanceof FacesDesignContext) {
            return "#{" + ((FacesDesignContext)c).getReferenceName() + "." + dpBean.getInstanceName() +
                    "." + optionsKey + "['" + ref + "']}"; //NOI18N
        } else {
            return "#{" + c.getDisplayName() + "." + dpBean.getInstanceName() + "." + optionsKey + "['" +
                    ref + "']}"; //NOI18N
        }
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
    
    protected String getPropertyResolverKey() {
        return "options"; // NOI18N
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
        DesignBean dpb = prop.getDesignBean();
        if ((dpb.getInstance() instanceof DataProvider)){
            //System.out.println("Bean property Changed - "  + prop.getDesignBean().getInstanceName());
            //System.out.println("Property Name - "  + prop.getPropertyDescriptor().getDisplayName());
            if (prop.getPropertyDescriptor().getName().equals("CachedRowSet")){
                DataProvider dp = (DataProvider)dpb.getInstance();
                if (dp != null) {
                    DefaultListModel val_dlm = new DefaultListModel();
                    val_dlm.addElement(val_none);
                    
                    DefaultListModel disp_dlm = new DefaultListModel();
                    disp_dlm.addElement(disp_none);
                    
                    valListModelHash.put(prop.getDesignBean(), val_dlm);
                    dispListModelHash.put(prop.getDesignBean(), disp_dlm);
                    
                    try{
                        FieldKey[] fkeys = dp.getFieldKeys();
                        for (int f = 0; f < fkeys.length; f++) {
                            ListItem li = new ListItem();
                            li.field = fkeys[f];
                            Class t = dp.getType(fkeys[f]);
                            li.type = PropertyBindingHelper.getPrettyTypeName(t.getName());
                            val_dlm.addElement(li);
                            disp_dlm.addElement(li);
                            if (!valSelectedItemHash.containsKey(dpb) && isTypeOf(Integer.class, "int", dp.getType(fkeys[f]))){
                                valSelectedItemHash.put(dpb, li);
                            }
                            if (!dispSelectedItemHash.containsKey(dpb) && dp.getType(fkeys[f]).isAssignableFrom(String.class)){
                                dispSelectedItemHash.put(dpb, li);
                            }
                        }
                    }catch(Exception exc){
                        ErrorManager.getDefault().notify(exc);
                    }
                    dpCombo.addItem(prop.getDesignBean());
                    dpCombo.setSelectedItem(dpb);
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
