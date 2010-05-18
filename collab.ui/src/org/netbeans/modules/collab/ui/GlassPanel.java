/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.collab.ui;

import java.awt.*;
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
