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

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.*;
import javax.swing.*;
import java.lang.reflect.Method;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;

/**
 * @author Tomas Pavek
 */

public class JScrollPaneSupport extends AbstractLayoutSupport {

    private static Method setViewportViewMethod;

    public Class getSupportedClass() {
        return JScrollPane.class;
    }

    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        if (container.getComponentCount() > 1) // [or containerDelegate??]
            return -1;
        return 0;
    }

    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        Dimension sz = container.getSize();
        Insets insets = container.getInsets();
        sz.width -= insets.left + insets.right;
        sz.height -= insets.top + insets.bottom;
        
        g.drawRect(0, 0, sz.width, sz.height);
        return true;
    }

    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        if (components.length == 0)
            return;

        if (container instanceof JScrollPane)
            ((JScrollPane)container).setViewportView(components[0]);
    }

    public boolean removeComponentFromContainer(Container container,
                                                Container containerDelegate,
                                                Component component,
                                                int index)
    {
        return false;
    }

    protected CodeElement getActiveContainerCodeElement() {
        return getLayoutContext().getContainerCodeElement();
    }

    protected CodeElement readComponentCode(CodeConnection connection,
                                            CodeConnectionGroup componentCode)
    {
        if (getSetViewportViewMethod().equals(connection.getConnectingObject())
            || getSimpleAddMethod().equals(connection.getConnectingObject()))
        {
            componentCode.addConnection(connection);
            getConstraintsList().add(null); // no constraints
            return connection.getConnectionParameters()[0];
        }

        return null;
    }

    protected void createComponentCode(CodeConnectionGroup componentCode,
                                       CodeElement componentElement,
                                       int index)
    {
        CodeConnection addConnection = CodeStructure.createConnection(
                         getLayoutContext().getContainerCodeElement(),
                         getSetViewportViewMethod(),
                         new CodeElement[] { componentElement });
        componentCode.addConnection(addConnection);
    }

    private static Method getSetViewportViewMethod() {
        if (setViewportViewMethod == null) {
            try {
                setViewportViewMethod = JScrollPane.class.getMethod(
                                            "setViewportView", // NOI18N
                                            new Class[] { Component.class });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return setViewportViewMethod;
    }
}
