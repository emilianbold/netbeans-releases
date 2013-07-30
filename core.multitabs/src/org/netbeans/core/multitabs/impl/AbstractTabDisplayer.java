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

package org.netbeans.core.multitabs.impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.multitabs.ButtonFactory;
import org.netbeans.core.multitabs.Controller;
import org.netbeans.core.multitabs.Settings;
import org.netbeans.core.multitabs.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;

/**
 *
 * @author S. Aubrecht
 */
abstract class AbstractTabDisplayer extends TabDisplayer implements MouseWheelListener {

    protected Controller controller;
    protected final JScrollPane scrollPane;
    protected final int orientation;

    private final JToolBar controls;

    private final JLabel lblFullPath = new JLabel();

    private final ScrollAction scrollLeft;
    private final ScrollAction scrollRight;

    private final ChangeListener fullPathListener;

    public AbstractTabDisplayer( final TabDataModel tabModel, int tabsLocation ) {
        super( tabModel );
        setLayout( new BorderLayout( 3, 3 ) );
        this.orientation = tabsLocation == JTabbedPane.TOP || tabsLocation == JTabbedPane.BOTTOM ? JTabbedPane.HORIZONTAL : JTabbedPane.VERTICAL;
        scrollPane = new JScrollPane();
        controls = new ControlsToolbar();
        lblFullPath.setBorder( BorderFactory.createEmptyBorder( 0, 3, 2, 3) );
        Font defaultFont = lblFullPath.getFont();
        lblFullPath.setFont( defaultFont.deriveFont( defaultFont.getSize2D()-2 ) );
        JPanel controlsPanel = new JPanel( new BorderLayout() );
        controlsPanel.setOpaque( false );
        if( TabTableUI.IS_AQUA ) {
            Color backColor = UIManager.getColor( "NbSplitPane.background" ); //NOI18N
            if( null != backColor ) {
                setBackground( backColor );
                setOpaque( true );
            }
        }
        switch( tabsLocation ) {
            case JTabbedPane.TOP:
            case JTabbedPane.BOTTOM:
                add( scrollPane, BorderLayout.CENTER );
                controlsPanel.add( controls, BorderLayout.NORTH );
                add( controlsPanel, BorderLayout.EAST );
                if( Settings.getDefault().isShowFullPath() )
                    add( lblFullPath, BorderLayout.SOUTH );
                break;
            case JTabbedPane.LEFT:
            case JTabbedPane.RIGHT:
                add( scrollPane, BorderLayout.CENTER );
                controlsPanel.add( controls, BorderLayout.EAST );
                add( controlsPanel, BorderLayout.NORTH );
                break;
            default:
                throw new IllegalArgumentException( "Invalid orientation: " + tabsLocation );
        }
        configureScrollPane( scrollPane );
        scrollLeft = new ScrollAction( scrollPane, tabsLocation, true );
        scrollRight = new ScrollAction( scrollPane, tabsLocation, false );
        controls.add( ButtonFactory.createScrollLeftButton( tabModel, scrollLeft ) );
        controls.add( ButtonFactory.createScrollRightButton( tabModel, scrollRight ) );
        addMouseWheelListener( this );

        ProjectSupport.getDefault().addPropertyChangeListener( new PropertyChangeListener() {
            @Override
            public void propertyChange( PropertyChangeEvent evt ) {
                repaint();
                ProjectSupport.getDefault().removePropertyChangeListener( this );
            }
        });

        fullPathListener = new ChangeListener() {
            @Override
            public void stateChanged( ChangeEvent e ) {
                updateFullPath();
            }
        };
    }

    private void configureScrollPane( JScrollPane scrollPane ) {
        scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_NEVER );
        scrollPane.setBorder( BorderFactory.createEmptyBorder() );
        scrollPane.setOpaque( false );
        scrollPane.getViewport().setOpaque( false );
        scrollPane.setFocusable( false );
        scrollPane.setWheelScrollingEnabled( false );
        scrollPane.setViewportBorder( BorderFactory.createEmptyBorder() );

        scrollPane.addComponentListener( new ComponentListener() {

            @Override
            public void componentResized( ComponentEvent e ) {
                showSelectedTab();
            }

            @Override
            public void componentMoved( ComponentEvent e ) {
            }

            @Override
            public void componentShown( ComponentEvent e ) {
            }

            @Override
            public void componentHidden( ComponentEvent e ) {
            }
        });
    }

    @Override
    public void attach( final Controller controller ) {
        this.controller = controller;
        controls.add( ButtonFactory.createDropDownButton( controller ) );
        controls.add( ButtonFactory.createMaximizeButton( controller ) );
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if( null != controller )
            controller.addSelectionChangeListener( fullPathListener );
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if( null != controller )
            controller.removeSelectionChangeListener( fullPathListener );
    }

    private void updateFullPath() {
        if( !lblFullPath.isVisible() || null == controller )
            return;
        String text = null;
        int selIndex = controller.getSelectedIndex();
        if( selIndex >= 0 ) {
            TabData tab = tabModel.getTab( selIndex );
            if( null != tab ) {
                text = tab.getTooltip();
            }
        }
        lblFullPath.setText( text );
    }

    @Override
    public final void mouseWheelMoved( MouseWheelEvent e ) {
        scrollLeft.mouseWheelMoved( e );
        if( e.isConsumed() )
            return;
        scrollRight.mouseWheelMoved( e );
    }

    private void showSelectedTab() {
        if( null == controller )
            return;
        int selIndex = controller.getSelectedIndex();
        if( selIndex < 0 )
            return;
        Rectangle rect = getTabBounds( selIndex );
        if( null == rect )
            return;
        scrollPane.getViewport().scrollRectToVisible( rect );
    }
}
