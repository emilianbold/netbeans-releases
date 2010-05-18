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
