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

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;

/**
 * @author Tomas Pavek
 */

public class ScrollPaneSupport extends AbstractLayoutSupport {

    private static java.lang.reflect.Method addMethod;

    public Class getSupportedClass() {
        return ScrollPane.class;
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

        if (container instanceof ScrollPane) {
            // [do not allow adding a component when some already added??]
            ScrollPane scroll = (ScrollPane) container;
            Component removedComp = null;
            if (scroll.getComponentCount() > 0)
                removedComp = scroll.getComponent(0);
            scroll.add(components[0]);
            // hack for AWT components - we must attach the fake peer again
            // to the component that was removed by adding new component
            ensureFakePeerAttached(removedComp);
        }
    }

    public boolean removeComponentFromContainer(Container container,
                                                Container containerDelegate,
                                                Component component,
                                                int index)
    {
        return false;
    }

    static private void ensureFakePeerAttached(Component comp) {
        boolean attached = FakePeerSupport.attachFakePeer(comp);
        if (attached && comp instanceof Container)
            FakePeerSupport.attachFakePeerRecursively((Container)comp);
    }
}
