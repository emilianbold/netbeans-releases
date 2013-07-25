/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.team.server.ui.picker;

import java.awt.BorderLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.Scrollable;

/**
 * Special scroll pane that supports mouse-wheel scrolling and has custom scroll buttons
 * (it doesn't show any scrollbars).
 *
 * @author S. Aubrecht
 */
final class ScrollingContainer extends JPanel implements MouseWheelListener  {

    private final JComponent content;
    private final Scrollable sc;
    private final boolean horizontalOrientation;
    private final ScrollButton btnScrollLeft;
    private final ScrollButton btnScrollRight;
    private final JScrollPane scrollPane;
    private final ScrollAction scrollLeft;
    private final ScrollAction scrollRight;

    public ScrollingContainer( JComponent content, boolean horizontalScrolling ) {
        super( new BorderLayout( 1, 1 ) );
        setOpaque( false );
        
        this.content = content;
        this.sc = ( Scrollable ) content;
        this.horizontalOrientation = horizontalScrolling;

        scrollPane = new JScrollPane( content );
        configureScrollPane( scrollPane );

        scrollLeft = new ScrollAction( scrollPane, horizontalScrolling, true );
        scrollRight = new ScrollAction( scrollPane, horizontalScrolling, false );

        btnScrollLeft = new ScrollButton( scrollLeft, horizontalScrolling ? JTabbedPane.WEST : JTabbedPane.NORTH );
        btnScrollRight = new ScrollButton( scrollRight, horizontalScrolling ? JTabbedPane.EAST : JTabbedPane.SOUTH );

        add( scrollPane, BorderLayout.CENTER );
        if( horizontalScrolling ) {
            add( btnScrollLeft, BorderLayout.WEST );
            add( btnScrollRight, BorderLayout.EAST );
        } else  {
            add( btnScrollLeft, BorderLayout.NORTH );
            add( btnScrollRight, BorderLayout.SOUTH );
        }

        addMouseWheelListener( this );
        content.addMouseWheelListener( this );
        btnScrollLeft.addMouseWheelListener( this );
        btnScrollRight.addMouseWheelListener( this );
    }

    @Override
    public final void mouseWheelMoved( MouseWheelEvent e ) {
        scrollLeft.mouseWheelMoved( e );
        if( e.isConsumed() )
            return;
        scrollRight.mouseWheelMoved( e );
    }

    private void configureScrollPane( JScrollPane scrollPane ) {
        scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_NEVER );
        scrollPane.setBorder( BorderFactory.createEmptyBorder() );
        scrollPane.setOpaque( false );
        scrollPane.getViewport().setOpaque( false );
        scrollPane.setFocusable( false );
        scrollPane.setWheelScrollingEnabled( false );
    }
}
