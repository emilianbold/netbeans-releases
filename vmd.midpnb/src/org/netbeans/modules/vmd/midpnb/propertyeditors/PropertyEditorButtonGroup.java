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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.midpnb.propertyeditors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGButtonGroupCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGRadioButtonCD;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;



/**
 * @author ads
 *
 */
public class PropertyEditorButtonGroup extends PropertyEditorUserCode 
    implements PropertyEditorElement 
{

    private PropertyEditorButtonGroup(  ){
        super( NbBundle.getMessage( 
                PropertyEditorButtonGroup.class, "LBL_ButtonGroup") );
    }
    
    public static PropertyEditorButtonGroup createInstance( ){
        return new PropertyEditorButtonGroup( );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode#cleanUp(org.netbeans.modules.vmd.api.model.DesignComponent)
     */
    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (myCustomEditor != null) {
            myCustomEditor.cleanUp();
            myCustomEditor = null;
        }
        myRadioButton = null;
    }
    
    @Override
    public Boolean canEditAsText() {
        return false;
    }
    
    @Override
    public String getAsText() {
        return NbBundle.getMessage( PropertyEditorSpinnerModel.class, 
                "TXT_ButtonGroup");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode#getCustomEditor()
     */
    @Override
    public Component getCustomEditor() {
        if (myCustomEditor == null) {
            initComponents();
            initElements(Collections.<PropertyEditorElement>singleton(this));
        }
        return super.getCustomEditor();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement#getCustomEditorComponent()
     */
    public JComponent getCustomEditorComponent() {
        return myCustomEditor;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement#getRadioButton()
     */
    public JRadioButton getRadioButton() {
        return myRadioButton;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement#getTextForPropertyValue()
     */
    public String getTextForPropertyValue() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement#isInitiallySelected()
     */
    public boolean isInitiallySelected() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement#isVerticallyResizable()
     */
    public boolean isVerticallyResizable() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement#setTextForPropertyValue(java.lang.String)
     */
    public void setTextForPropertyValue( String text ) {
        saveValue(text);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement#updateState(org.netbeans.modules.vmd.api.model.PropertyValue)
     */
    public void updateState( PropertyValue value ) {
        if ( value != null ){
            myCustomEditor.setValue(value);
        }
        myRadioButton.setSelected(!isCurrentValueAUserCodeType());
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode#customEditorOKButtonPressed()
     */
    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        if ( myRadioButton.isSelected()) {
            saveValue( myCustomEditor.getValue() );
        }
    }
    
    private void initComponents() {
        myRadioButton = new JRadioButton();
        Mnemonics.setLocalizedText(myRadioButton, NbBundle.getMessage(
                PropertyEditorButtonGroup.class, "LBL_ButtonGroupList")); // NOI18N

        myRadioButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(
                        PropertyEditorButtonGroup.class, "ACSN_ButtonGroupList")); // NOI18N
        myRadioButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(
                        PropertyEditorButtonGroup.class, "ACSD_ButtonGroupList"));

        myCustomEditor = new CustomEditor();
        myCustomEditor.updateModel();
    }
    
    private void saveValue(final String text) {
        if (component == null || component.get() == null) {
            return;
        }
        
        final DesignComponent desComponent = component.get();
        final PropertyValue oldValue[] = new PropertyValue[1];
        desComponent.getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                  oldValue[0] = desComponent.readProperty(SVGRadioButtonCD.PROP_BUTTON_GROUP);
            }
        });
        if ( text!=null &&  text.equals(oldValue[0].getPrimitiveValue())) {
            return;
        }
        
        desComponent.getDocument().getTransactionManager().writeAccess(new Runnable() {

            public void run() {
                List<PropertyValue> list = desComponent.getParentComponent().
                        readProperty( SVGFormCD.PROP_COMPONENTS).getArray();
                for (PropertyValue propertyValue : list) {
                    if ( propertyValue.getComponent().getType().equals( 
                        SVGButtonGroupCD.TYPEID ))
                    {
                        DesignComponent buttonGroup = propertyValue.getComponent();
                        if ( buttonGroup.readProperty( ClassCD.PROP_INSTANCE_NAME).
                                getPrimitiveValue().toString().equals( text))
                        {
                            PropertyEditorButtonGroup.super.setValue( 
                                    PropertyValue.createComponentReference( buttonGroup));
                            return;
                        }
                    }
                }
                PropertyEditorButtonGroup.super.setValue( 
                        PropertyValue.createNull());
                /*desComponent.writeProperty( SVGRadioButtonCD.PROP_BUTTON_GROUP, 
                        PropertyValue.createComponentReference(text));*/
            }
        });
    }
    
    private class CustomEditor extends JPanel implements ActionListener {

        private static final long serialVersionUID = 7734641168584954536L;

        CustomEditor() {
            initComponents();
        }
        
        public void actionPerformed(ActionEvent evt) {
            myRadioButton.setSelected(true);
        }
        
        void cleanUp(){
            if (myCombobox != null) {
                myCombobox.removeActionListener(this);
                myCombobox = null;
            }
            removeAll();
        }
        
        void updateModel() {
            final DefaultComboBoxModel model = (DefaultComboBoxModel) myCombobox.getModel();
            model.removeAllElements();
            
            final DesignComponent desComponent = component.get();
            desComponent.getDocument().getTransactionManager().readAccess(new Runnable() {

                public void run() {
                      List<PropertyValue> list = desComponent.getParentComponent().
                          readProperty( SVGFormCD.PROP_COMPONENTS).getArray();
                      for (PropertyValue propertyValue : list) {
                          if ( propertyValue.getComponent().getType().equals( 
                                  SVGButtonGroupCD.TYPEID )){
                              model.addElement( propertyValue.getComponent().
                                      readProperty( ClassCD.PROP_INSTANCE_NAME).
                                      getPrimitiveValue().toString());
                          }
                      }
                }
            });
            /*for (String tag : tags) {
                model.addElement(tag);
            }*/
        }
        
        void setValue(final PropertyValue value) {
            if ( value.getComponent() == null ){
                return;
            }
            value.getComponent().getDocument().getTransactionManager().readAccess(
                    new Runnable() {
                        public void run() {
                            String group = value.getComponent().readProperty( ClassCD.PROP_INSTANCE_NAME).toString();
                            myCombobox.setSelectedItem( group );
                        }
                    });
        }
        
        String getValue(){
            return myCombobox.getSelectedItem().toString();
        }
        
        private void initComponents() {
            setLayout(new BorderLayout());
            myCombobox = new JComboBox();
            myCombobox.setModel(new DefaultComboBoxModel());
            myCombobox.addActionListener(this);

            myCombobox.getAccessibleContext().setAccessibleName(
                    myRadioButton.getAccessibleContext().getAccessibleName());
            myCombobox.getAccessibleContext().setAccessibleDescription(
                    myRadioButton.getAccessibleContext().getAccessibleDescription());

            add(myCombobox, BorderLayout.CENTER);
        }
        
        private JComboBox myCombobox;
    }
    
    private CustomEditor myCustomEditor;
    private JRadioButton myRadioButton;
}
