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
package org.netbeans.modules.collab.ui;

import java.awt.*;
import java.io.Serializable;
import javax.swing.JComponent;


/**
 * A layout manager that accepts a delegate layout manager for actual layout,
 * in addition to a "glass pane" component that is layed out above all other
 * components and can be used to capture mouse events or overdraw the other
 * components.<p>
 *
 * Note, the glass pane component <em>must</em> be added first to the container
 * associated with this layout manager.
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class GlassLayout extends Object implements LayoutManager2, Serializable {
    ////////////////////////////////////////////////////////////////////////////
    // Class fields
    ////////////////////////////////////////////////////////////////////////////
    public static final String GLASS = "glass";

    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    private Container container;
    private LayoutManager delegate;
    private JComponent glassComponent;

    /**
     *
     *
     */
    public GlassLayout(Container container, LayoutManager delegate) {
        super();

        if (container == null) {
            throw new IllegalArgumentException("Parameter \"container\" cannot be null"); // NOI18N
        }

        if (delegate == null) {
            throw new IllegalArgumentException("Parameter \"delegate\" cannot be null"); // NOI18N
        }

        this.delegate = delegate;
        this.container = container;
    }

    /**
     *
     *
     */
    public Container getContainer() {
        return container;
    }

    /**
     *
     *
     */
    public LayoutManager getDelegate() {
        return delegate;
    }

    /**
     *
     *
     */
    public JComponent getGlassComponent() {
        return glassComponent;
    }

    /**
     *
     *
     */
    protected void setGlassComponent(JComponent value) {
        glassComponent = value;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Delegate methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void addLayoutComponent(Component comp, Object constraints) {
        if (GLASS.equals(constraints) && comp instanceof JComponent) {
            setGlassComponent((JComponent) comp);
        } else {
            ((LayoutManager2) getDelegate()).addLayoutComponent(comp, constraints);
        }
    }

    /**
     *
     *
     */
    public void addLayoutComponent(String name, Component comp) {
        if (GLASS.equals(name) && comp instanceof JComponent) {
            setGlassComponent((JComponent) comp);
        } else {
            getDelegate().addLayoutComponent(name, comp);
        }
    }

    /**
     *
     *
     */
    public float getLayoutAlignmentX(Container target) {
        return ((LayoutManager2) getDelegate()).getLayoutAlignmentX(target);
    }

    /**
     *
     *
     */
    public float getLayoutAlignmentY(Container target) {
        return ((LayoutManager2) getDelegate()).getLayoutAlignmentY(target);
    }

    /**
     *
     *
     */
    public void invalidateLayout(Container target) {
        ((LayoutManager2) getDelegate()).invalidateLayout(target);
    }

    /**
     *
     *
     */
    public void layoutContainer(Container parent) {
        JComponent component = getGlassComponent();

        // TAF: This doesn't work here, or in addLayoutComponent()
        //		if (component!=null && getContainer().getComponent(0)!=component)
        //			getContainer().add(component,0);
        getDelegate().layoutContainer(parent);

        // Layout the glass component on top of all the others
        if (component != null) {
            Rectangle bounds = parent.getBounds();
            Insets insets = parent.getInsets();
            int width = bounds.width - insets.right - insets.left;
            int height = bounds.height - insets.top - insets.bottom;

            component.setBounds(insets.left, insets.top, width, height);
        }
    }

    /**
     *
     *
     */
    public Dimension maximumLayoutSize(Container target) {
        return ((LayoutManager2) getDelegate()).maximumLayoutSize(target);
    }

    /**
     *
     *
     */
    public Dimension minimumLayoutSize(Container parent) {
        return getDelegate().minimumLayoutSize(parent);
    }

    /**
     *
     *
     */
    public Dimension preferredLayoutSize(Container parent) {
        return getDelegate().preferredLayoutSize(parent);
    }

    /**
     *
     *
     */
    public void removeLayoutComponent(Component comp) {
        if (comp == getGlassComponent()) {
            setGlassComponent(null);
        } else {
            getDelegate().removeLayoutComponent(comp);
        }
    }
}
