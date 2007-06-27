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

package org.openide.awt;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

/**
 * Factory creating buttons with a small arrow icon that shows a popup menu when clicked. 
 * The default button behavior hasn't changed.
 * 
 * @author S. Aubrecht
 * @since 6.11
 */
public final class DropDownButtonFactory {
    
    /**
     * Use this property name to assign or remove popup menu to/from buttons created by this factory,
     * e.g. <code>dropDownButton.putClientProperty( PROP_DROP_DOWN_MENU, new JPopupMenu() )</code>
     * The property value must be <code>JPopupMenu</code>, removing this property removes the arrow from the button.
     */
    public static final String PROP_DROP_DOWN_MENU = "dropDownMenu";
    
    /** Creates a new instance of DropDownButtonFactory */
    private DropDownButtonFactory() {
    }
    
    /**
     * Creates JButton with a small arrow that shows the provided popup menu when clicked.
     * 
     * @param icon The default icon, cannot be null
     * @param dropDownMenu Popup menu to display when the arrow is clicked. If this parameter is null
     * then the button doesn't show any arrow and behaves like a regular JButton. It is possible to add
     * the popup menu later using PROP_DROP_DOWN_MENU client property.
     * @return A button that is capable of displaying an 'arrow' in its icon to open a popup menu.
     */
    public static JButton createDropDownButton( Icon icon, JPopupMenu dropDownMenu ) {
        return new DropDownButton( icon, dropDownMenu );
    }
    
    /**
     * Creates JToggleButton with a small arrow that shows the provided popup menu when clicked.
     * 
     * @param icon The default icon, cannot be null
     * @param dropDownMenu Popup menu to display when the arrow is clicked. If this parameter is null
     * then the button doesn't show any arrow and behaves like a regular JToggleButton. It is possible to add
     * the popup menu later using PROP_DROP_DOWN_MENU client property.
     * @return A toggle-button that is capable of displaying an 'arrow' in its icon to open a popup menu.
     */
    public static JToggleButton createDropDownToggleButton( Icon icon, JPopupMenu dropDownMenu ) {
        return new DropDownToggleButton( icon, dropDownMenu );
    }
}
