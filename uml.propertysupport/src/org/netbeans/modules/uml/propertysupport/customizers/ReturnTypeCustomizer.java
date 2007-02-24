/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ReturnTypeCustomizer.java
 *
 * Created on April 14, 2005, 10:57 AM
 */

package org.netbeans.modules.uml.propertysupport.customizers;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.core.typemanagement.IPickListManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder;
import org.netbeans.modules.uml.propertysupport.nodes.CustomPropertyEditor;

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import java.util.Vector;
import java.util.HashMap;
import org.netbeans.modules.uml.core.support.Debug;

/**
 *
 * @author  khu
 */
public class ReturnTypeCustomizer extends javax.swing.JPanel implements EnhancedCustomPropertyEditor {
    private final static int TYPE = 1;
    private final static int LOWER = 5;
    private final static int RANGEPARENTS = 3;
    private final static int RANGEPARENT = 4;
    private final static int UPPER = 6;
    private final static int ORDERED = 7;
    private final static int ORDEREDPARENT = 2;
    private final static String EMPTY = "";  // NOI18N
    
    private IPropertyElement mElement = null;
    private IPropertyDefinition mDefinition = null;
    private HashMap <String, IPropertyElement> mElementHashed = new HashMap <String, IPropertyElement> ();
    private HashMap <String, IPropertyDefinition> mDefinitionHashed = new HashMap <String, IPropertyDefinition> ();
    private Vector <String> mElementNames = new Vector <String> ();
    private CustomPropertyEditor mEditor = null;
    
    boolean isMultiple = false;
    
    /** Creates new form ReturnTypeCustomizer */
    public ReturnTypeCustomizer() {
        initComponents();
        enableComponents (isMultiple);
    }
    public void setElement (IPropertyElement element, IPropertyDefinition def){
        mElement = element;
        mDefinition = def;
        /*
        mElementHashed.clear ();
        mElementNames.clear();
        mDefinitionHashed.clear ();
        retriveDefinitions (mDefinition);
        retriveProperties (mElement);
        printHashed ();
        **/
        resetTables ();
        initializeType ();
        initializeMulti ();
        // debugPrint ();
    }
    
    public void setPropertySupport(CustomPropertyEditor editor)
    {
        mEditor = editor;
    }
    
    protected void notifyChanged()
    {
        if(mEditor != null)
        {
            mEditor.firePropertyChange();
        }
    }
    
    protected void initializeType () {
        Vector < IPropertyElement > elems = mElement.getSubElements ();
        
        IStrings typeNames;
        IPropertyElement typeEl = null;
        IPropertyDefinition typeDef = null;
        if (elems != null && elems.size() > 0) {
            typeEl = elems.get(0);
            typeDef = typeEl.getPropertyDefinition();
        }
        if (typeDef != null && typeEl != null) {
            typeNames = typeDef.getValidValue(typeEl);
        } else {
            typeNames = searchAllTypes();
        }
        
        if (typeNames != null) {
            ComboBoxModel model = new DefaultComboBoxModel(typeNames.toArray());
            returnTypeCombo.setModel(model);
        }
        
        if (typeEl != null){
            returnTypeCombo.setSelectedItem(typeEl.getValue());
        }
    }
    
    protected void initializeMulti () {
        // HACK relies on the order of the difinition 
        // and assume the definition name is the same as element name
        // if PropertyDefinition changed, need to change too
        IPropertyElement elem = mElementHashed.get ((String)mElementNames.get(LOWER));
        if (elem != null) {
            lowerTextField.setText (elem.getValue());
            if (elem.getValue ().trim ().length () > 0 ){
                isMultiple = true;
            }
        }
        elem = mElementHashed.get ((String)mElementNames.get(UPPER));
        if (elem != null) {
            upperTextField.setText (elem.getValue());
            if (elem.getValue ().trim ().length () > 0 ){
                isMultiple = true;
            }
        }
        elem = mElementHashed.get ((String)mElementNames.get(ORDERED));
        if (elem != null && isMultiple) {
            jComboBox2.setSelectedItem (elem.getValue());
        }
        enableComponents (isMultiple);
    }
    private void resetTables () {
        mElementHashed.clear ();
        mElementNames.clear();
        mDefinitionHashed.clear ();
        retriveDefinitions (mDefinition);
        retriveProperties (mElement);
    }
    private void retriveProperties (IPropertyElement elem){
        if (elem.getPropertyDefinition().isOnDemand()){
           DefinitionPropertyBuilder builder = DefinitionPropertyBuilder.instance();
           builder.loadOnDemandProperties (elem);
        }
        mElementHashed.put (elem.getName (), elem);
        // Debug.out.println ("##### elem.name="+elem.getName()+" value="+elem.getValue()+" orig="+elem.getOrigValue());
        Vector < IPropertyElement > subElem = elem.getSubElements ();
        for (IPropertyElement cur : subElem){
            retriveProperties (cur);
        }
    }
    private void retriveDefinitions (IPropertyDefinition def){
        if (def.isOnDemand ()== true){
             DefinitionPropertyBuilder builder = DefinitionPropertyBuilder.instance();
             def = builder.loadOnDemandDefintion(def);
        }
        mElementHashed.put (def.getName (), (IPropertyElement)null);
        mDefinitionHashed.put (def.getName (), def);
        mElementNames.add (def.getName());
        // Debug.out.println ("!! def = "+def.getName());
        Vector < IPropertyDefinition > subDef = def.getSubDefinitions();
        for (IPropertyDefinition cur : subDef){
            retriveDefinitions (cur);
        }
    }  
   
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        returnTypeLabel = new javax.swing.JLabel();
        returnTypeCombo = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        rangePanel = new javax.swing.JPanel();
        lowerLabel = new javax.swing.JLabel();
        lowerTextField = new javax.swing.JTextField();
        upperLabel = new javax.swing.JLabel();
        upperTextField = new javax.swing.JTextField();
        rangeLabel = new javax.swing.JLabel();
        isOrderedLabel = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        multiplicityCheckBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EtchedBorder());
        setPreferredSize(new java.awt.Dimension(350, 200));
        returnTypeLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/propertysupport/customizers/Bundle").getString("RETURN_TYPE_Mnemonic").charAt(0));
        returnTypeLabel.setLabelFor(returnTypeCombo);
        returnTypeLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/propertysupport/customizers/Bundle").getString("RETURN_TYPE"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 5, 0, 5);
        add(returnTypeLabel, gridBagConstraints);

        returnTypeCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(returnTypeCombo, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(new javax.swing.border.EtchedBorder());
        rangePanel.setLayout(new java.awt.GridBagLayout());

        rangePanel.setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/propertysupport/customizers/Bundle").getString("RANGES")));
        rangePanel.setMinimumSize(new java.awt.Dimension(300, 50));
        lowerLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/propertysupport/customizers/Bundle").getString("LOWER_Mnemonic").charAt(0));
        lowerLabel.setLabelFor(lowerTextField);
        lowerLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/propertysupport/customizers/Bundle").getString("LOWER"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        rangePanel.add(lowerLabel, gridBagConstraints);

        lowerTextField.setMinimumSize(new java.awt.Dimension(500, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 3.0;
        rangePanel.add(lowerTextField, gridBagConstraints);

        upperLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/propertysupport/customizers/Bundle").getString("UPPER_Mnemonic").charAt(0));
        upperLabel.setLabelFor(upperTextField);
        upperLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/propertysupport/customizers/Bundle").getString("UPPER"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.8;
        rangePanel.add(upperLabel, gridBagConstraints);

        upperTextField.setMinimumSize(new java.awt.Dimension(100, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 3.0;
        rangePanel.add(upperTextField, gridBagConstraints);

        rangeLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/propertysupport/customizers/Bundle").getString("RANGE"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        rangePanel.add(rangeLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 3, 5);
        jPanel1.add(rangePanel, gridBagConstraints);

        isOrderedLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/propertysupport/customizers/Bundle").getString("ORDERED_Mnemonic").charAt(0));
        isOrderedLabel.setLabelFor(jComboBox2);
        isOrderedLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/propertysupport/customizers/Bundle").getString("ORDERED"));
        isOrderedLabel.setName("isOrderedLabel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel1.add(isOrderedLabel, gridBagConstraints);

        jComboBox2.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel1.add(jComboBox2, gridBagConstraints);

        multiplicityCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/propertysupport/customizers/Bundle").getString("MULTIPLICITY_Mnemonic").charAt(0));
        multiplicityCheckBox.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/propertysupport/customizers/Bundle").getString("MULTIPLICITY"));
        multiplicityCheckBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                multiplicityCboxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(multiplicityCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jPanel1, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void multiplicityCboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multiplicityCboxActionPerformed
// TODO add your handling code here:
        isMultiple = multiplicityCheckBox.isSelected ();
        if (isMultiple) {
            initializeMulti ();
        } else {
            enableComponents (isMultiple);
        }
    }//GEN-LAST:event_multiplicityCboxActionPerformed
    
    private void enableComponents (boolean b){
        // rangePanel.setEnabled (b);
        multiplicityCheckBox.setSelected (b);
        lowerLabel.setEnabled (b);
        upperLabel.setEnabled (b);
        rangeLabel.setEnabled (b);
        isOrderedLabel.setEnabled (b);
        lowerTextField.setEnabled (b);
        upperTextField.setEnabled (b);
        jComboBox2.setEnabled (b);
        isMultiple = b;
    }
   ////////////////////////////////////////////////////////////////////////////
   // EnhancedCustomPropertyEditor Implementation
   
   /** 
    * Get the customized property value.  This implementation will 
    * return an array of property elements.  Basically when this method
    * gets called the user has pressed the OK button.
    *
    * @return the property value
    * @exception IllegalStateException when the custom property editor does not contain a valid property value
    *            (and thus it should not be set)
    */
    public Object getPropertyValue() throws IllegalStateException
    {
       Object retVal = null;
       // for element
       Vector < IPropertyElement > elems = mElement.getSubElements();
       IPropertyElement type = null;
       if (elems != null && elems.size () > 0){
           type = (IPropertyElement)elems.get (0);
       }
       if (type == null) {
           DefinitionPropertyBuilder builder = DefinitionPropertyBuilder.instance ();
           IPropertyDefinition pd = mDefinition.getSubDefinition (0);
           type = builder.retrievePropertyElement (pd, mElement);
           // mElement.addSubElement (type);
           // IPropertyElement parent = mElement;
       }
       type.setValue((String)returnTypeCombo.getSelectedItem ());
       // Debug.out.println ("el="+type.getName()+ "  "+ returnTypeCombo.getSelectedItem ());
       // save (mElement);
       
       // for Multiplicity
       if (isMultiple) {
           getMultiplicityValue ();
       }else { // reset everything
           IPropertyElement elem = mElementHashed.get ((String)mElementNames.get(LOWER));
        if (elem != null) {
            elem.setValue(EMPTY);
        }
        elem = mElementHashed.get ((String)mElementNames.get(UPPER));
        if (elem != null) {
            elem.setValue(EMPTY);
        }
       }
       save (mElement);       
       
       org.netbeans.modules.uml.core.support.umlutils.IPropertyElementManager manager = mElement.getPropertyElementManager();
       
       IPropertyElement parent = mElement.getParent();
       manager.reloadElement(mElement.getElement(), mElement.getPropertyDefinition(), mElement);
       notifyChanged();
       return retVal;
    }
    
    private void getMultiplicityValue (){
        IPropertyElement elem = mElementHashed.get ((String)mElementNames.get(RANGEPARENT));
        IPropertyElement ranges = mElementHashed.get ((String)mElementNames.get(RANGEPARENTS));
        IPropertyElement range = null;
        if (elem == null){
            elem = addElement (mDefinitionHashed.get ((String)mElementNames.get(RANGEPARENT)), 
                        mElementHashed.get ((String)mElementNames.get(RANGEPARENTS)));
            // elem.setParent (mElementHashed.get ((String)mElementNames.get(RANGEPARENTS)));
            // IPropertyElement parent = mElementHashed.get ((String)mElementNames.get(RANGEPARENTS));
            // parent.addSubElement (elem);
            mElementHashed.put ((String)mElementNames.get(RANGEPARENT), elem);
            save (elem);
        
        }
        range = elem;
        elem = mElementHashed.get ((String)mElementNames.get(LOWER));
        if (elem == null ) {
            elem = addElement (mDefinitionHashed.get ((String)mElementNames.get(LOWER)), 
                        mElementHashed.get ((String)mElementNames.get(RANGEPARENT)));
        }
        elem.setValue (lowerTextField.getText ());
        // IPropertyElement lower = elem;
        // elem.setParent (mElementHashed.get ((String)mElementNames.get(RANGEPARENT)));
        // save (elem);
        elem = mElementHashed.get ((String)mElementNames.get(UPPER));
        if (elem == null ) {
            elem = addElement (mDefinitionHashed.get ((String)mElementNames.get(UPPER)), 
                        mElementHashed.get ((String)mElementNames.get(RANGEPARENT)));
        }
        // elem.setParent (mElementHashed.get ((String)mElementNames.get(RANGEPARENT)));
        elem.setValue (upperTextField.getText ());
        IPropertyElement upper = elem;
        // save (elem);
        elem = mElementHashed.get ((String)mElementNames.get(ORDERED));
        if (elem == null ) {
            elem = addElement (mDefinitionHashed.get ((String)mElementNames.get(ORDERED)), 
                        mElementHashed.get ((String)mElementNames.get(ORDEREDPARENT)));
        }
        elem.setValue ((String)jComboBox2.getSelectedItem ());
        //ranges.addSubElement (range);
        Vector < IPropertyElement > rangeVec = new Vector < IPropertyElement > ();
        rangeVec.add (range);
        ranges.setSubElements (rangeVec) ;
        save (mElement);
        
        // upper.setValue (upperTextField.getText ());
        // save (mElement);
        // printProperty (range);
        // Debug.out.println ("******");
        // printProperty (mElement);
        /* this is a HACK, not sure why the new elements (i.e. MultiplicityRange)
         * is not "updated" (i.e. MultiplicityRanges does not have subElements of 
         * MultiplicityRange) after save (mElement). Need to manually set it as 
         * subElements. 
         */
        retriveProperties (mElement);
        elem = mElementHashed.get ((String)mElementNames.get(LOWER));
        elem.setValue (lowerTextField.getText ());
        elem = mElementHashed.get ((String)mElementNames.get(UPPER));
        elem.setValue (upperTextField.getText ());
        // save (mElement);
    }
    private IPropertyElement addElement (IPropertyDefinition def, 
                             IPropertyElement parent) {
        IPropertyElement retVal = null;
        DefinitionPropertyBuilder builder = DefinitionPropertyBuilder.instance ();
        retVal = builder.retrievePropertyElement (def, parent);
        return retVal;
    }
    protected void save (IPropertyElement element){
        if (element != null){
            element.save ();
            Vector < IPropertyElement > children = element.getSubElements ();
            for (IPropertyElement child : children){
                save (child);
            }
        }
    }
    
    public void setVisible(boolean aFlag)
    {      
       super.setVisible(aFlag);
    }
      
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel isOrderedLabel;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lowerLabel;
    private javax.swing.JTextField lowerTextField;
    private javax.swing.JCheckBox multiplicityCheckBox;
    private javax.swing.JLabel rangeLabel;
    private javax.swing.JPanel rangePanel;
    private javax.swing.JComboBox returnTypeCombo;
    private javax.swing.JLabel returnTypeLabel;
    private javax.swing.JLabel upperLabel;
    private javax.swing.JTextField upperTextField;
    // End of variables declaration//GEN-END:variables

    private void debugPrint () {
        printProperty (mElement);
        retriveDefinitions (mDefinition);
        printDefinition (mDefinition);
        Vector < IPropertyElement > subEl = mElement.getSubElements ();
        for (IPropertyElement curE : subEl) {
            Debug.out.println ("$ subelement.name="+curE.getName());
            Debug.out.println ("$ subelement.value="+curE.getValue());
        }
        Vector < IPropertyDefinition > subDe = mDefinition.getSubDefinitions ();
        for (IPropertyDefinition curD : subDe){
            Debug.out.println("@ subdefinition.name="+curD.getName());
            
        }
        IPropertyDefinition def = mDefinition.getSubDefinition(1);
        Debug.out.println ("! def="+def.getName());
        subDe = def.getSubDefinitions();
        for (IPropertyDefinition curD : subDe){            
            Debug.out.println("* name="+curD.getName());
        }
        def = subDe.get(0);
        subDe = def.getSubDefinitions ();
        Debug.out.println ("$ size="+subDe.size());
        def = subDe.get(0);
        // printDefinition (mDefinition);
        printDefinition (def);
    }
    private void printDefinition (IPropertyDefinition def){
        Debug.out.println ("!# def="+def.getName ());
        /*
        if (def.isOnDemand ()== true){
             DefinitionPropertyBuilder builder = DefinitionPropertyBuilder.instance();
             def = builder.loadOnDemandDefintion(def);
        }
         **/
        Vector < IPropertyDefinition >subDef = def.getSubDefinitions();
        for (IPropertyDefinition cur : subDef){
            printDefinition (cur);
        }
    }
    private void printProperty (IPropertyElement elem){
        if (elem.getPropertyDefinition().isOnDemand()){
           DefinitionPropertyBuilder builder = DefinitionPropertyBuilder.instance();
           builder.loadOnDemandProperties (elem);
        }
        Debug.out.println ("##### elem.name="+elem.getName()+" value="+elem.getValue()+ " orig="+elem.getOrigValue ());
        Vector < IPropertyElement > subElem = elem.getSubElements ();
        for (IPropertyElement cur : subElem){
            printProperty (cur);
        }
    }
    private void printHashed () {
        for (String cur : mElementNames){
            IPropertyElement elm = (IPropertyElement) mElementHashed.get (cur);
            Debug.out.println ("name="+cur+" elm="+(elm == null ? "null" : elm.getName ()));
            
        }
    }
    
    private IStrings searchAllTypes() {
        IStrings list = new Strings();
        IProduct prod = ProductHelper.getProduct();
        if (prod != null)
        {
           IProductProjectManager pMan = prod.getProjectManager();
           if (pMan != null)
           {
              IProject proj = pMan.getCurrentProject();
              if (proj != null)
              {
                 ITypeManager typeMan = proj.getTypeManager();
                 if (typeMan != null)
                 {
                    IPickListManager pickMan = typeMan.getPickListManager();
                    if (pickMan != null)
                    {
                       String filter = "DataType Class Interface";
                       list = pickMan.getTypeNamesWithStringFilter(filter);
                    }
                 }
              }
           }
        }
        return list;
    }
}
