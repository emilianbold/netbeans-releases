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

import org.netbeans.modules.form.codestructure.*;

/**
 * @author Tomas Pavek
 */

public interface LayoutSupportContext {

    // possible component resizing directions (bit flag constants)
    public final int RESIZE_UP = 1;
    public final int RESIZE_DOWN = 2;
    public final int RESIZE_LEFT = 4;
    public final int RESIZE_RIGHT = 8;

    public CodeStructure getCodeStructure();

    public CodeExpression getContainerCodeExpression();
    public CodeExpression getContainerDelegateCodeExpression();

    // getters for instances of container, container delegate, and subcomponents
    public Container getPrimaryContainer();
    public Container getPrimaryContainerDelegate();
    public Component getPrimaryComponent(int index);

    public void containerLayoutChanged(PropertyChangeEvent evt);
    public void componentLayoutChanged(int index, PropertyChangeEvent evt);
}
