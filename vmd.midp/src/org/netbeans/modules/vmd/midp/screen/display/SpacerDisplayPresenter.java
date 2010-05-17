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
package org.netbeans.modules.vmd.midp.screen.display;

import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsSupport;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.components.items.SpacerCD;


/**
 * @author David Kaspar
 */
public class SpacerDisplayPresenter extends ScreenDisplayPresenter {

    private JSeparator separator;
    private JPanel panel;
    private JPanel innerPanel;

    public SpacerDisplayPresenter() {
        panel = new JPanel( );
        innerPanel = new JPanel( );
        separator = new JSeparator(JSeparator.HORIZONTAL) {

            @Override
            public JPopupMenu getComponentPopupMenu() {
                return Utilities.actionsToPopup(ActionsSupport.createActionsArray(getRelatedComponent()), this);
            }
        };
        panel.setOpaque( false );
        innerPanel.setOpaque( false );



        panel.setLayout(//new FlowLayout() {
                new BoxLayout(panel, BoxLayout.LINE_AXIS ){

            @Override
            public void layoutContainer(Container parent) {
                super.layoutContainer(parent);
                innerPanel.setLocation(0,
                        (int) (parent.getHeight() -
                        separator.getPreferredSize().getHeight()) / 2);

            }
        });
        innerPanel.setLayout( new BoxLayout(innerPanel , BoxLayout.LINE_AXIS) );
        
        panel.add( innerPanel );
        
        innerPanel.add( separator );

        // Fix for #79636 - Screen designer tab traversal
        ScreenSupport.addKeyboardSupport(this);
    }

    public boolean isTopLevelDisplay () {
        return false;
    }

    public Collection<DesignComponent> getChildren () {
        return Collections.emptyList ();
    }

    public JComponent getView () {
        return panel;
    }

    public void reload (ScreenDeviceInfo deviceInfo) {
        int minHeight = Integer.parseInt(getComponent().readProperty(
                SpacerCD.PROP_MIN_HEIGHT).getPrimitiveValue().toString());
        int minWidth = Integer.parseInt(getComponent().readProperty(
                SpacerCD.PROP_MIN_WIDTH).getPrimitiveValue().toString());

        int panelWidth = (int)panel.getMinimumSize().getWidth();
        panel.setMinimumSize( new Dimension(panelWidth, minHeight));

        int sepHeight = (int)separator.getMinimumSize().getHeight();
        innerPanel.setMinimumSize( new Dimension( minWidth , sepHeight));

        int prefHeight = -1;
        int prefWidth = -1;
        PropertyValue val = getComponent().readProperty(ItemCD.PROP_PREFERRED_HEIGHT);
        if ( val!= null && val .getPrimitiveValue()!= null ) {
            prefHeight = Integer.valueOf(val.getPrimitiveValue().toString());
        }

        val = getComponent().readProperty(ItemCD.PROP_PREFERRED_WIDTH);
        if ( val!= null && val .getPrimitiveValue()!= null ) {
            prefWidth = Integer.valueOf(val.getPrimitiveValue().toString());
        }

        if ( prefHeight == -1 ){
            panel.setPreferredSize( null );
        }
        else {
            panel.setPreferredSize( new Dimension( (int)panel.getPreferredSize().
                    getWidth() ,prefHeight ));
        }

        if ( prefWidth ==-1 ){
            innerPanel.setPreferredSize( null );
            innerPanel.setMaximumSize( null );
        }
        else {
            innerPanel.setPreferredSize( new Dimension(prefWidth ,
                    (int)separator.getPreferredSize().getHeight()) );
            innerPanel.setMaximumSize( new Dimension( prefWidth ,
                    (int) separator.getMaximumSize().getHeight()) );
        }

        innerPanel.invalidate();
        innerPanel.revalidate();
        panel.revalidate();
    }

    public Shape getSelectionShape () {
        return new Rectangle (panel.getSize ());
    }

    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        return Collections.emptySet();
    }

}
