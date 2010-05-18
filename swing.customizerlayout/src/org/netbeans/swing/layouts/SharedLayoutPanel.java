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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.swing.layouts;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * A panel which, if it has an ancestor that implements SharedLayoutData, will
 * automatically align its so that all children of that parent have their
 * columns aligned.  Use for a set of components (such as a label for something
 * followed by a control that affects that setting) which will appear in an
 * ancestor component, but may be children of each other, yet should nonetheless
 * have their columns aligned.
 * <p/>
 * Note that the layout manager may not be set at runtime on instances of
 * SharedLayoutPanel.
 * <p/>
 * SharedLayoutPanels also have an &quot;expanded&quot; property - a component
 * may hide some data until a button is clicked, at which point it grows and
 * displays a detail view.  By default any SharedLayoutPanel shares its 
 * expanded setting with all others owned by the same SharedLayoutData ancestor,
 * so if any one is expanded, all others are set to unexpanded - so only one is
 * expanded at a time.
 * Borrowed from http://imagine.dev.java.net
 * @author Tim Boudreau
 */
public class SharedLayoutPanel extends JPanel implements LayoutDataProvider {
    private boolean initialized;
    public SharedLayoutPanel() {
        this (new LDPLayout());
    }

    public SharedLayoutPanel(LDPLayout layout) {
        super (layout);
        initialized = true;
    }
    
    public SharedLayoutPanel(Component c) {
        this();
        add (c);
    }

    @Override
    public final void setBorder (Border b) {
        //do nothing
    }
    
    /**
     * Overridden to throw an exception if called at runtime.
     * @param mgr The layout manager
     */
    @Override
    public final void setLayout (LayoutManager mgr) {
        if (initialized) {
            throw new UnsupportedOperationException ("Cannot set layout on a SharedLayoutPanel");
        }
        super.setLayout (mgr);
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        SharedLayoutData p = (SharedLayoutData) SwingUtilities.getAncestorOfClass(SharedLayoutData.class, this);
        if (p != null) {
            p.register(this);
        }
    }
    
    @Override
    public void removeNotify() {
        SharedLayoutData p = (SharedLayoutData) SwingUtilities.getAncestorOfClass(SharedLayoutData.class, this);
        if (p != null) {
            p.unregister(this);
        }
        super.removeNotify();
    }

    /**
     * Gets the column position for this column from the layout manager.
     * @param col The column
     * @return A pixel position in the x axis
     */
    public final int getColumnPosition(int col) {
        LDPLayout layout = (LDPLayout) getLayout();
        return layout.getColumnPosition(this, col);
    }

    /**
     * Returns false by default.  Override if you want to support expansion.
     * @return
     */
    public boolean isExpanded() {
        return false;
    }

    /**
     * Does nothing by default;  override to handle the case that you actually
     * want to change the set of child components when expanded.  You are 
     * responsible for providing a control that actually sets the expanded 
     * state.
     * 
     * @param val To expand or not
     */
    public void doSetExpanded(boolean val) {
        //do nothing by default
    }
}
