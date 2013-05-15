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
package org.netbeans.modules.team.ui.picker;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
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
    private final List<JComponent> columns;

    private ServersContainer( List<JComponent> columns ) {
        super( new GridBagLayout() );
        setOpaque( false );
        this.columns = new ArrayList<JComponent>( columns );

        int index = 0;
        for( int i=0; i<this.columns.size(); i++ ) {
            JComponent col = this.columns.get( i );
            add( col, new GridBagConstraints( index++, 0, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL, new Insets(5,5,5,5), 0,0 ) );
            if( i < this.columns.size()-1 ) {
                add( new JSeparator(JSeparator.VERTICAL), new GridBagConstraints( index++, 0, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL, new Insets(0,0,0,0), 0,0 ) );
            }
        }
    }

    static JComponent create( List<JComponent> serverComponents ) {
        ServersContainer container = new ServersContainer( serverComponents );

        if( serverComponents.size() <= MAX_VISIBLE_COLUMNS ) {
            container.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10) );
            return container;
        }
        ScrollingContainer res = new ScrollingContainer( container, true );
        res.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10));
        return res;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        Dimension res = super.getPreferredSize();
            int count = 0;
            res.width = 0;
            for( Component c : getComponents() ) {
                if( c == null )
                    continue;
                res.width += c.getPreferredSize().width;
                count++;
                if( count == 2*MAX_VISIBLE_COLUMNS-1 )
                    break;
            }
            res.width += MAX_VISIBLE_COLUMNS * 5 + 5;
        return res;
    }

    @Override
    public int getScrollableUnitIncrement( Rectangle visibleRect, int orientation, int direction ) {
        return 10; //TODO calculate the increment from a single server panel size
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
}
