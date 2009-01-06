/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JRadioButton;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.openide.util.NbBundle;
import org.openide.awt.Mnemonics;


/**
 * @author ads
 *
 */
public class PropertyEditorListModel extends PropertyEditorUserCode 
    implements PropertyEditorElement 
{

    private PropertyEditorListModel( String userCodeLabel , String modelText) {
        super(userCodeLabel);
        myModelText = modelText;
    }
    
    public static PropertyEditorListModel createInstance( String label, 
            String modelText){
        return new PropertyEditorListModel( label , modelText );
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
        return myModelText;
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
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement#setTextForPropertyValue(java.lang.String)
     */
    public void setTextForPropertyValue( String arg0 ) {
        // should not be called becuase it is not editable as text
        assert false;
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
    
    private void saveValue(List<String> modelItems ) {
        List<PropertyValue> list = new ArrayList<PropertyValue>( modelItems.size());
        for (String string : modelItems) {
            PropertyValue value = MidpTypes.createStringValue( string );
            list.add( value );
        }
        PropertyValue model = PropertyValue.createArray( 
                MidpTypes.TYPEID_JAVA_LANG_STRING, list);
        super.setValue( model );
    }
    
    private void initComponents() {
        myRadioButton = new JRadioButton();
        Mnemonics.setLocalizedText(myRadioButton, NbBundle.getMessage(
                PropertyEditorButtonGroup.class, "LBL_DefaultModel")); // NOI18N

        myRadioButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(
                        PropertyEditorButtonGroup.class, "ACSN_DefaultModel")); // NOI18N
        myRadioButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(
                        PropertyEditorButtonGroup.class, "ACSD_DefaultModel"));

        myCustomEditor = new SVGListPropertyCustomEditor();
    }
    
    private SVGListPropertyCustomEditor myCustomEditor;
    private JRadioButton myRadioButton;
    
    private String myModelText; 

}
