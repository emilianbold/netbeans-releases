/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.team.server.ui.picker;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.Scrollable;

/**
 * Top-level container holding column-like panels for each TeamServer.
 *
 * @author S. Aubrecht
 */
class ServersContainer extends JPanel implements Scrollable {

    private static final int MAX_VISIBLE_COLUMNS = 3;
    private final JComponent[] servers;
    private final JComponent[] separators;

    private ServersContainer( List<JComponent> columns ) {
        this.servers = columns.toArray( new JComponent[columns.size()] );
        separators = new JComponent[Math.max(servers.length-1,0)];
        for( int i=0; i<separators.length; i++ ) {
            separators[i] = new JSeparator( JSeparator.VERTICAL );
        }
        setLayout( new SimpleyLayout() );
        setOpaque( false );
        
        for( JComponent c : servers ) {
            add( c );
        }
        
        for( JComponent c : separators ) {
            add( c );
        }
    }

    static JComponent create( List<JComponent> serverComponents ) {
        ServersContainer container = new ServersContainer( serverComponents );

        ScrollingContainer res = new ScrollingContainer( container, true );
        res.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10));
        return res;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        Dimension res = super.getPreferredSize();
        if( servers.length <= MAX_VISIBLE_COLUMNS )
            return res;
        res.width = 0;
        for( int i=0; i<servers.length && i<MAX_VISIBLE_COLUMNS; i++ ) {
            JComponent c = servers[i];
            Dimension size = c.getPreferredSize();
            res.width += HORIZONTAL_INSET;
            res.width += Math.min( size.width, MAX_COLUMN_WIDTH );

            if( i < separators.length && i < MAX_VISIBLE_COLUMNS-1 ) {
                c = separators[i];
                size = c.getPreferredSize();
                res.width += size.width;
            }
        }
        res.width += 2*HORIZONTAL_INSET;
        return res;
    }

    @Override
    public int getScrollableUnitIncrement( Rectangle visibleRect, int orientation, int direction ) {
        int res = MAX_COLUMN_WIDTH;
        if( separators.length > 0 )
            res += separators[0].getSize().width + 3*HORIZONTAL_INSET; 
        return res;
    }

    @Override
    public int getScrollableBlockIncrement( Rectangle visibleRect, int orientation, int direction ) {
        return getScrollableUnitIncrement( visibleRect, orientation, direction );
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return true;
    }
    
    private final static int HORIZONTAL_INSET = 10;
    private final static int VERTICAL_INSET = 5;
    private static final int MAX_COLUMN_WIDTH = 300;
 
    private class SimpleyLayout implements LayoutManager {

        @Override
        public void addLayoutComponent( String name, Component comp ) {
        }

        @Override
        public void removeLayoutComponent( Component comp ) {
        }

        @Override
        public Dimension preferredLayoutSize( Container parent ) {
            int width = 0;
            int height = 0;
            
            for( int i=0; i<servers.length; i++ ) {
                JComponent c = servers[i];
                Dimension size = c.getPreferredSize();
                width += HORIZONTAL_INSET;
                width += Math.min( size.width, MAX_COLUMN_WIDTH );
                height = Math.max( height, size.height );
                
                if( i < separators.length ) {
                    c = separators[i];
                    size = c.getPreferredSize();
                    width += size.width;
                    width += HORIZONTAL_INSET;
                }
            }
            width += HORIZONTAL_INSET;
            height += 2*VERTICAL_INSET;
            return new Dimension(width, height);
        }

        @Override
        public Dimension minimumLayoutSize( Container parent ) {
            return preferredLayoutSize( parent );
        }

        @Override
        public void layoutContainer( Container parent ) {
            int x = 0;
            int y = VERTICAL_INSET;
            int height = parent.getHeight();
            for( int i=0; i<servers.length; i++ ) {
                x += HORIZONTAL_INSET;
                JComponent server = servers[i];
                Dimension size = server.getPreferredSize();
                int width = Math.min( MAX_COLUMN_WIDTH, size.width );
                server.setBounds( x, y, width, height - 2*VERTICAL_INSET );
                
                x += width;
                
                if( i < separators.length ) {
                    x += HORIZONTAL_INSET;
                    JComponent separator = separators[i];
                    separator.setBounds( x, y, separator.getPreferredSize().width, height-2*VERTICAL_INSET );
                }
            }
        }
        
    }
}
