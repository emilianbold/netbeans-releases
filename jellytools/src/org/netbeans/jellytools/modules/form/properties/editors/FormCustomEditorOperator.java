/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.modules.form.properties.editors;

/*
 * CustomEditorDialogOperator.java
 *
 * Created on 6/13/02 11:58 AM
 */

import javax.swing.JDialog;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Dialog opened after click on "..." button in Component Inspector
 * (or property sheet can be docked to a different window).<p>
 * Contains Default, OK and Cancel buttons,
 * combobox enabling to change editor (DimensionEditor/Form Connection) and
 * button Advanced to call "Advanced Initialization Code" dialog (it is
 * FormCustomEditorAdvancedOperator).<p>
 * Example:<p>
 * <pre>
 *  ...
 *  property.openEditor();
 *  FormCustomEditorOperator fceo = new FormCustomEditorOperator(property.getName());
 *  fceo.setMode("PointEditor");
 *  PointCustomEditorOperator pceo = new PointCustomEditorOperator(fceo);
 *  pceo.setPointValue(...);
 *  fceo.ok(); //or pceo.ok(); it does not matter
 * </pre>
 * @author as103278
 * @version 1.0 */
public class FormCustomEditorOperator extends NbDialogOperator {

    /** Search for FormCustomEditor with defined title
     * @throws TimeoutExpiredException when NbDialog not found
     * @param title title of FormCustomEditor (mostly property name) */
    public FormCustomEditorOperator(String title) {
        super(title);
    }

    private JButtonOperator _btAdvanced;
    private JButtonOperator _btDefault;
    private JComboBoxOperator _cboMode;

    /** Tries to find "Advanced..." JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btAdvanced() {
        if (_btAdvanced==null) {
            _btAdvanced = new JButtonOperator( this, "Advanced...", 0 );
        }
        return _btAdvanced;
    }

    /** Tries to find "Default" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btDefault() {
        if (_btDefault==null) {
            _btDefault = new JButtonOperator( this, "Default", 0 );
        }
        return _btDefault;
    }

    /** Tries to find JComboBox in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JComboBoxOperator */
    public JComboBoxOperator cboMode() {
        if (_cboMode==null) {
            _cboMode = new JComboBoxOperator( this, 0 );
        }
        return _cboMode;
    }

    /** clicks on "Advanced..." JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void advanced() {
        btAdvanced().pushNoBlock();
    }

    /** clicks on "Default" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void setDefault() {
        btDefault().push();
    }

    /** getter for currenlty selected mode
     * @return String mode name */    
    public String getMode() {
        return cboMode().getSelectedItem().toString();
    }
    
    /** tries to find cboMode and select item
     * @param mode String FormCustomEditor mode name */
    public void setMode(String mode) {
        cboMode().selectItem(mode);
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btAdvanced();
        btDefault();
        cboMode();
    }

}

