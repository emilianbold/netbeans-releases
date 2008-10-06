/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.welcome.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.Scrollable;
import org.netbeans.modules.welcome.content.BackgroundPanel;
import org.netbeans.modules.welcome.content.Constants;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 * Base class for inner tabs in the Welcome Page
 * 
 * @author S. Aubrecht
 */
abstract class AbstractTab extends BackgroundPanel implements Scrollable, Constants {

    private boolean initialized = false;
    
    private Image bottomGradient;

    public AbstractTab() {
        super( new BorderLayout() );
        
        this.bottomGradient = ImageUtilities.loadImage( IMAGE_BOTTOM_GRADIENT );
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if( !initialized ) {
            buildContent();
            initialized = true;
        }
    }

    protected abstract void buildContent();
    
    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if( null != getParent() && getParent().getHeight() > 0 && getParent().getHeight() > d.height )
            d.height = getParent().getHeight();
        if( null != getParent() && getParent().getWidth() > 0 ) {
            if( d.width > getParent().getWidth() ) {
                d.width = Math.max(getParent().getWidth(), START_PAGE_MIN_WIDTH+(int)(((FONT_SIZE-11)/11.0)*START_PAGE_MIN_WIDTH));
            } else if( d.width < getParent().getWidth() ) {
                d.width = getParent().getWidth();
            }
        }
        return d;
    }
    
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Constants.FONT_SIZE;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 30*Constants.FONT_SIZE;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getBottomStripeOrigin();
        
        //bottom gradient
        int gradHeigt = bottomGradient.getHeight(null);
        int gradWidth = bottomGradient.getWidth(null);
        for( int i=0; i<=width/gradWidth; i++ ) {
            g.drawImage(bottomGradient, i*gradWidth, height-gradHeigt, null);
        }
    }
    
    protected abstract int getBottomStripeOrigin();
}
