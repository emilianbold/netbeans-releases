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


package org.netbeans.core.windows.view.ui;


import org.openide.windows.TopComponent;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;


/**
 * Interface describing component which is used inside <code>SimpleContainer</code>.
 * There will be at two implementations one for view and second one for editor type.
 *
 * @author  Peter Zavadsky
 */
public interface Tabbed {

    public void requestAttention(TopComponent tc);

    public void cancelRequestAttention(TopComponent tc);
    
    public void addTopComponent(String name, Icon icon, TopComponent tc, String toolTip);

    public void insertComponent(String name, Icon icon, Component comp, String toolTip, int position);
    
    public void setTopComponents(TopComponent[] tcs, TopComponent selected);
    
    public int getTabCount();
    
    public TopComponent[] getTopComponents();
    
    public TopComponent getTopComponentAt(int index);
    
    public int indexOf(Component tc);
    
    public void removeComponent(Component comp);
    
    public void setTitleAt(int index, String title);
    
    public void setIconAt(int index, Icon icon);
    
    public void setToolTipTextAt(int index, String toolTip);
    
    public void setSelectedComponent(Component comp);
    
    public TopComponent getSelectedTopComponent();

    public void addChangeListener(ChangeListener listener);
    
    public void removeChangeListener(ChangeListener listener);

    public void addActionListener (ActionListener al);

    public void removeActionListener (ActionListener al);

    public void setActive(boolean active);
    
    public int tabForCoordinate(Point p);
   
    public Shape getIndicationForLocation(Point location, TopComponent startingTransfer,
            Point startingPoint, boolean attachingPossible);
    
    public Object getConstraintForLocation(Point location, boolean attachingPossible);
    
    public Image createImageOfTab (int tabIndex);
    
    /** Accessor for visual component holding components */
    public Component getComponent();
    
    /** Allows tabbed implementors to speficy content of popup menu on tab
     * with given index. Incoming actions are default set by winsys
     */
    public Action[] getPopupActions(Action[] defaultActions, int tabIndex);
    
    /** Returns bounds of tab with given index */
    public Rectangle getTabBounds(int tabIndex);
    
    public boolean isTransparent();
    
    public void setTransparent( boolean transparent );
    
    /** Interface for simple accessing of Tabbed instance */
    public interface Accessor {

        public Tabbed getTabbed ();

    } // end of Accessor

}

