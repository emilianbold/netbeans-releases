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

/*
 * TrivialLayout.java
 *
 * Created on 20. srpen 2003, 18:33
 */

package org.netbeans.modules.mobility.svgcore.navigator;

import java.awt.*;

/**
 * Trivial layout manager class used by the panels for selecting look and filter.  Simply uses the preferred size of the
 * first compnent and fills the rest of the space with the second, to the height of the tallest.
 *
 * 
 */
final class TrivialLayout implements LayoutManager {
    public void addLayoutComponent (String name, Component comp) {
        //do nothing
    }

    public void removeLayoutComponent (Component comp) {
        //do nothing
    }

    public void layoutContainer (Container parent) {
        if ( parent instanceof TapPanel ) {
            layoutTapPanel ( (TapPanel) parent );
        } else {
            layoutComp ( parent );
        }
    }

    /**
     * Standard layout for any container
     */
    private void layoutComp (Container parent) {
        Component[] c = parent.getComponents ();
        if ( c.length > 1 ) {
            Dimension d1 = c[ 0 ].getPreferredSize ();
            Dimension d2 = c[ 1 ].getPreferredSize ();
            int labely = 0;
            d1.width += 10; //Aqua displays elipsis
            if ( d2.height > d1.height ) {
                labely = ( d2.height / 2 ) - ( d1.height / 2 );
            }
            if ( parent.getWidth () - d1.width < d2.width ) {
                c[ 0 ].setBounds ( 0, 0, 0, 0 );
                c[ 1 ].setBounds ( 0, 0, parent.getWidth (), parent.getHeight () );
            } else {
                c[ 0 ].setBounds ( 0, labely, d1.width, d1.height );
                c[ 1 ].setBounds ( d1.width + 1, 0, parent.getWidth () - d1.width,
                        Math.min ( parent.getHeight (), d2.height ) );
            }
        }
    }

    /**
     * Layout for TapPanel, taking into account its minimumHeight
     */
    private void layoutTapPanel (TapPanel tp) {
        Component[] c = tp.getComponents ();
        if ( c.length > 1 ) {
            Dimension d1 = c[ 0 ].getPreferredSize ();
            Dimension d2 = c[ 1 ].getPreferredSize ();
            int labely = 0;
            if ( d2.height > d1.height ) {
                labely = ( ( d2.height / 2 ) - ( d1.height / 2 ) ) + 2; //+2 fudge factor for font baseline
            }

            if ( tp.isExpanded () ) {
                int top = tp.getOrientation () == tp.UP ? 0 : tp.getMinimumHeight ();
                int height = Math.min ( tp.getHeight () - tp.getMinimumHeight (), d2.height );
                if ( tp.getWidth () - d1.width < d2.width ) {
                    c[ 0 ].setBounds ( 0, 0, 0, 0 );
                    c[ 1 ].setBounds ( 0, top, tp.getWidth (), height );
                } else {
                    c[ 0 ].setBounds ( 0, top + labely, d1.width, d1.height );
                    c[ 1 ].setBounds ( d1.width + 1, top, tp.getWidth () - d1.width,
                            height );
                }
            } else {
                c[ 0 ].setBounds ( 0, 0, 0, 0 );
                c[ 1 ].setBounds ( 0, 0, 0, 0 );
            }
        }
    }


    public Dimension minimumLayoutSize (Container parent) {
        Dimension result = new Dimension ( 20, 10 );
        Component[] c = parent.getComponents ();
        TapPanel tp = (TapPanel) parent;
        if ( c.length > 1 ) {
            Dimension d1 = c[ 0 ].getPreferredSize ();
            Dimension d2 = c[ 1 ].getPreferredSize ();
            result.width = d1.width + d2.width;
            result.height = tp.isExpanded () ? Math.max ( d1.height, d2.height ) + tp.getMinimumHeight () : tp.getMinimumHeight ();
        }
        return result;
    }

    public Dimension preferredLayoutSize (Container parent) {
        return minimumLayoutSize ( parent );
    }
}

