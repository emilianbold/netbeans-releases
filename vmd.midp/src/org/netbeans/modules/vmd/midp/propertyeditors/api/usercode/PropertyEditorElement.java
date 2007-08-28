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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.midp.propertyeditors.api.usercode;

import javax.swing.JComponent;
import javax.swing.JRadioButton;
import org.netbeans.modules.vmd.api.model.PropertyValue;

/**
 * This class represents an UI and logical element of PropertyEditorUserCode.
 *
 * @author Anton Chechel
 */
public interface PropertyEditorElement {

    /**
     * Updates state of the element by given property value
     * @param PropertyValue value which represents current state of model
     */
    void updateState(PropertyValue value);

    /**
     * Sets text from which element can set property value. Another way is to use
     * DesignPropertyEditor.customEditorOKButtonPressed()
     * 
     * @param PropertyValue value which represents current state of model
     */
    void setTextForPropertyValue(String text);

    /**
     * Returns text from element what represents property value's state
     * 
     * @return String text
     */
    String getTextForPropertyValue();

    /**
     * Returns custom editor component for element
     * 
     * @return JComponent custom editor
     */
    JComponent getCustomEditorComponent();

    /**
     * Returns radio button component for element
     * 
     * @return JRadioButton
     */
    JRadioButton getRadioButton();

    /**
     * Returns boolean value whether this element should be active by default
     * and radio button selected
     * 
     * @return booleans is initially selected
     */
    boolean isInitiallySelected();

    /**
     * Returns boolean value whether this element could be vertically resized.
     * For example for simple elements like JComboBox or JTextField it should return
     * false, but for complex panel which encapsulates a lot of components or for
     * big components like JEditorPane it should return true.
     * 
     * @return booleans is element vertically resizable
     */
    boolean isVerticallyResizable();
}
