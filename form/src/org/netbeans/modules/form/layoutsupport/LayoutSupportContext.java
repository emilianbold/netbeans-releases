/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutsupport;

import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import org.netbeans.modules.form.codestructure.*;

/**
 * Interface providing necessary context for LayoutSupportDelegate
 * implementations.
 *
 * @author Tomas Pavek
 */

public interface LayoutSupportContext {

    /** Returns the CodeStructure object to be used for reading/creating
     * the "code image" of the layout configuration. */
    public CodeStructure getCodeStructure();

    /** Returns the code expression of the primary container. */
    public CodeExpression getContainerCodeExpression();

    /** Returns the code expression of primary container delegate. */
    public CodeExpression getContainerDelegateCodeExpression();

    /** Returns the primary container. This is the referential instance used
     * in form metadata structures. */
    public Container getPrimaryContainer();

    /** Returns the primary container delegate. */
    public Container getPrimaryContainerDelegate();

    /** Returns the component of given index from primary container. */
    public Component getPrimaryComponent(int index);

    /** This method should be called by layout delegate if a change requires
     * to update the (primary) container layout completely (remove components,
     * set new layout, add components again). To be used probably only in case
     * the supported layout manager is not a bean (e.g. BoxLayout). */
    public void updatePrimaryContainer();

    /** This method should be called by layout delegate to notify about
     * changing a property of container layout. It calls back the delegate's
     * method acceptContainerLayoutChange which may throw PropertyVetoException
     * to require reverting the property. */
    public void containerLayoutChanged(PropertyChangeEvent evt)
        throws PropertyVetoException;

    /** This method should be called by layout delegate to notify about
      * changing a property of component layout constraint. It calls back
      * the delegate's method acceptComponentLayoutChange which may throw
      * PropertyVetoException to require reverting the property. */
    public void componentLayoutChanged(int index, PropertyChangeEvent evt)
        throws PropertyVetoException;
}
