/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui;

import java.awt.*;

import java.io.*;

import javax.swing.*;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class GlassPanel extends JPanel {
    /**
     *
     *
     */
    public GlassPanel() {
        super();
        setLayout(new BorderLayout());
    }

    /**
     * Overridden to enforce the position of the glass component as
     * the zero child.
     *
     * @param comp the component to be enhanced
     * @param constraints the constraints to be respected
     * @param index the index
     */
    protected void addImpl(Component comp, Object constraints, int index) {
        super.addImpl(comp, constraints, index);

        // TAF: Adapted from JRootPane line 777
        /// We are making sure the glassPane is on top. 
        JComponent glassPane = getGlassComponent();

        if ((glassPane != null) && (glassPane.getParent() == this) && (getComponent(0) != glassPane)) {
            add(glassPane, GlassLayout.GLASS, 0);
        }
    }

    /**
     * Sets a specified <code>Component</code> to be the glass pane for this
     * panel.  The glass pane should normally be a lightweight,
     * transparent component, because it will be made visible when
     * ever the root pane needs to grab input events.
     *
     * @param glass the <code>Component</code> to use as the glass pane
     *              for this <code>GlassPanel</code>
     * @exception NullPointerException if the <code>glass</code> parameter is
     *                <code>null</code>
     */
    public JComponent getGlassComponent() {
        return ((GlassLayout) getLayout()).getGlassComponent();
    }

    /**
     * Sets a specified <code>Component</code> to be the glass pane for this
     * panel.  The glass pane should normally be a lightweight,
     * transparent component, because it will be made visible when
     * ever the root pane needs to grab input events.
     *
     * @param glass the <code>Component</code> to use as the glass pane
     *              for this <code>GlassPanel</code>
     * @exception NullPointerException if the <code>glass</code> parameter is
     *                <code>null</code>
     */
    public void setGlassComponent(JComponent glassComponent) {
        // TAF: Adapted from JRootPane line 610
        if (glassComponent == null) {
            throw new IllegalArgumentException("Paramter \"glassComponent\" cannot be null");
        }

        JComponent glassPane = getGlassComponent();

        Boolean visible = null;

        if ((glassPane != null) && (glassPane.getParent() == this)) {
            this.remove(glassPane);
            visible = new Boolean(glassPane.isVisible());
        }

        if (visible != null) {
            glassComponent.setVisible(visible.booleanValue());
        }

        this.add(glassComponent, GlassLayout.GLASS, 0);

        if ((visible != null) && visible.booleanValue()) {
            repaint();
        }
    }

    /**
     *
     *
     */
    public GlassLayout getGlassLayout() {
        LayoutManager result = super.getLayout();

        if (super.getLayout() instanceof GlassLayout) {
            return (GlassLayout) super.getLayout();
        } else {
            return null;
        }
    }

    /**
     * Note, this method should always return an instance of
     * <code>GlassLayout</code>
     *
     */
    public LayoutManager getLayout() {
        LayoutManager result = super.getLayout();

        return result;
    }

    /**
     * Automatically wraps the supplied layout manager with a
     * <code>GlassLayout</code> instance
     *
     */
    public void setLayout(LayoutManager manager) {
        if (!(manager instanceof GlassLayout)) {
            manager = new GlassLayout(this, manager);
        }

        super.setLayout(manager);
    }

    /**
     * Overridden to account for the presence of the glass pane.  If the
     * glass pane is visible, then optimized drawing must be turned off
     * in order to force proper repainting of the component.
     *
     */
    public boolean isOptimizedDrawingEnabled() {
        if (getGlassComponent() != null) {
            return !getGlassComponent().isVisible();
        } else {
            return true;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    //	private JComponent glassPane;
}
