/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * PropertyDisplayer.java
 *
 * Created on 17 October 2003, 15:31
 */
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node.*;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.event.ChangeListener;


/** A set of interfaces which define the contract for different types of
 * components that can display or edit properties.  There is the base interface
 * for any component that can display a property, and sub interfaces describing
 * aspects such as editability.  Eventually this interfaces and a factory
 * should become public as a replacement for PropertyPanel - they are much
 * more straightforward in terms of setting expectations correctly about
 * the behavior of the underlying component.
 * <p>
 * Note that to avoid making them public, the subinterfaces have been factored
 * out for the time being.
 *
 * @author  Tim Boudreau */
interface PropertyDisplayer {
    /**Update policy constant - update whenever an ActionEvent is received from
     * an editor component */
    public static final int UPDATE_ON_CONFIRMATION = 0;

    /**Update policy constant - update if the user tabs out of the editor
     * component or it otherwise loses focus */
    public static final int UPDATE_ON_FOCUS_LOST = 1;

    /**Update policy constant - fire an action event but do not actually
     * update the property */
    public static final int UPDATE_ON_EXPLICIT_REQUEST = 2;

    public Property getProperty();

    public void refresh();

    public Component getComponent();
}
