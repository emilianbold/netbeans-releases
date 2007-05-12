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

package com.sun.rave.designtime;

import java.beans.PropertyChangeListener;
import java.awt.Component;

/**
 * <p>The Customizer2 interface describes a context-aware customizer for a JavaBean.  A component
 * author may wish to supply a customizer for their JavaBean, which is a dialog that pops up and
 * provides a rich set of UI controls to manipulate the configuration of the entire JavaBean.  This
 * type of Customizer has significantly more access to the context that the JavaBean is being
 * designed in, and thus allows for much greater functionality.</p>
 *
 * <p>The dialog title and icon will use the values from 'getDisplayName()' and 'getSmallIcon()'
 * respectively.</p>
 *
 * <p>If a Customizer2 is apply capable (isApplyCapable() returns true), the host dialog will
 * have three buttons: "OK", "Apply", and "Cancel" (and possibly "Help" if there is a helpKey).
 * The 'isModified' method will be called each time a PropertyChangeEvent is fired to check if the
 * "Apply" button should be enabled.  When the user clicks "OK" or "Apply", the 'applyChanges'
 * method is called.  This implies that manipulations in the dialog are not directly affecting the
 * DesignBean.  The DesignBean should not be touched until 'applyChanges' has been called.</p>
 *
 * <p>If a Customizer2 is NOT apply capable (isApplyCapable() returns false), the host dialog
 * will only have one button: "Done" (and possibly "Help" if there is a helpKey).  The DesignBean
 * may be manipulated at will in this dialog, as it is considered to be non-stateful.  When the user
 * clicks "Done", the 'applyChanges' method will be called.</p>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see java.beans.Customizer
 */
public interface Customizer2 extends DisplayItem {

    /**
     * Returns a UI panel (should be lightweight) to be displayed to the user.  The passed in
     * DesignBean is the design-time proxy representing the JavaBean being customized.  This method
     * will be called *every* time the Customizer2 is invoked.
     *
     * @param designBean the DesignBean to be customized
     * @return a lightweight panel to display to the user
     */
    public Component getCustomizerPanel(DesignBean designBean);

    /**
     * <p>If a Customizer2 is apply capable (isApplyCapable() returns true), the host dialog will
     * have three buttons: "OK", "Apply", and "Cancel" (and possibly "Help" if there is a helpKey).
     * The 'isModified' method will be called each time a PropertyChangeEvent is fired to check if
     * the "Apply" button should be enabled.  When the user clicks "OK" or "Apply", the
     * 'applyChanges' method is called.  This implies that manipulations in the dialog are not
     * directly affecting the DesignBean.  The DesignBean should not be touched until 'applyChanges'
     * has been called.</p>
     *
     * <p>If a Customizer2 is NOT apply capable (isApplyCapable() returns false), the host dialog
     * will only have one button: "Done" (and possibly "Help" if there is a helpKey).  The DesignBean
     * may be manipulated at will in this dialog, as it is considered to be non-stateful.  When the
     * user clicks "Done", the 'applyChanges' method will be called.</p>
     *
     * @return returns <code>true</code> if the customizer is stateful and is capable of handling
     *         an apply operation
     * @see isModified()
     * @see applyChanges()
     */
    public boolean isApplyCapable();

    /**
     * Returns <code>true</code> if the customizer is in an edited state - to notify the customizer
     * dialog that the "Apply" button should be activated.
     *
     * @return returns <code>true</code> if the customizer is in an edited state, <code>false</code>
     *         if not
     */
    public boolean isModified();

    /**
     * Notifies the customizer that the user has clicked "OK" or "Apply" and the customizer should
     * commit it's changes to the DesignBean.
     *
     * @return A Result object, indicating success or failure, and optionally including messages
     *         for the user
     */
    public Result applyChanges();

    /**
     * Standard propertyChange events - 'null' property name indicates that the bean changed in
     * some other way than just a property.  An apply capable Customizer2 can use this as a hook
     * to notify the host dialog that the 'modified' state has changed.
     *
     * @param listener The PropertyChangeListener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Standard propertyChange events - 'null' property name indicates that the bean changed in
     * some other way than just a property.  An apply capable Customizer2 can use this as a hook
     * to notify the host dialog that the 'modified' state has changed.
     *
     * @param listener The PropertyChangeListener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Standard propertyChange events - 'null' property name indicates that the bean changed in
     * some other way than just a property.  An apply capable Customizer2 can use this as a hook
     * to notify the host dialog that the 'modified' state has changed.
     *
     * @return An array of PropertyChangeListener representing all the current listeners
     */
    public PropertyChangeListener[] getPropertyChangeListeners();
}
