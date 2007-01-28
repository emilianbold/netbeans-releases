/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
