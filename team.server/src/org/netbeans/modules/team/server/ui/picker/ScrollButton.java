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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.UIManager;
import org.openide.util.ImageUtilities;

/**
 * Button for scrolling, keeps sending scrolling the container until mouse is released.
 * 
 * @author S. Aubrecht
 */
final class ScrollButton extends JButton implements ActionListener {

    private final static int INSET = 2;
    private Timer timer = null;
    private int count = 0;

    public ScrollButton( Action action, int orientation ) {
        super( action );
        
        setBorder( BorderFactory.createEmptyBorder( INSET, INSET, INSET, INSET));
        setBorderPainted( false );
        setFocusable( false );
        setFocusPainted( false );
        setContentAreaFilled( false );
        setOpaque( false );
        setDefaultCapable( false );
        setRolloverEnabled( true );

        String imgName = null;
        switch( orientation ) {
            case JTabbedPane.NORTH:
                imgName = "up"; //NOI18N
                break;
            case JTabbedPane.SOUTH:
                imgName = "down"; //NOI18N
                break;
            case JTabbedPane.WEST:
                imgName = "left"; //NOI18N
                break;
            case JTabbedPane.EAST:
                imgName = "right"; //NOI18N
                break;
            default:
                throw new IllegalArgumentException();
        }
        Icon icon = ImageUtilities.loadImageIcon( "org/netbeans/modules/team/server/resources/" + imgName+ ".png", true); //NOI18N
        setIcon( icon );
        icon = ImageUtilities.createDisabledIcon(icon);
        setDisabledIcon(icon );
    }

    @Override
    public void paint( Graphics g ) {
        if( isEnabled() ) {
            Color color = null;
            if( getModel().isPressed()) {
                color = UIManager.getColor( "Button.shadow"); //NOI18N
            } else if( getModel().isRollover() ) {
                color = UIManager.getColor( "Button.light"); //NOI18N
            }
            if( null != color ) {
                g.setColor( color );
                g.fillRect( 0, 0, getWidth(), getHeight() );
            }
            super.paint( g );
        }
    }

    @Override
    public Icon getIcon() {
        if( isEnabled() )
            return super.getIcon();
        return EMPTY_ICON;
    }
    
    private Timer getTimer() {
        if( timer == null ) {
            timer = new Timer( 400, this );
            timer.setRepeats( true );
        }
        return timer;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        count++;
        if( count > 2 ) {
            if( count > 5 ) {
                timer.setDelay( 75 );
            } else {
                timer.setDelay( 200 );
            }
        }
        performAction();
    }

    private void performAction() {
        if( !isEnabled() ) {
            stopTimer();
            return;
        }
        getAction().actionPerformed( new ActionEvent( this,
                ActionEvent.ACTION_PERFORMED,
                getActionCommand() ) );
    }

    private void startTimer() {
        Timer t = getTimer();
        if( t.isRunning() ) {
            return;
        }
        repaint();
        t.setDelay( 400 );
        t.start();
    }

    private void stopTimer() {
        if( timer != null ) {
            timer.stop();
        }
        repaint();
        count = 0;
    }

    @Override
    protected void processMouseEvent( MouseEvent me ) {
        if( isEnabled() && me.getID() == MouseEvent.MOUSE_PRESSED ) {
            startTimer();
        } else if( me.getID() == MouseEvent.MOUSE_RELEASED ) {
            stopTimer();
        }
        super.processMouseEvent( me );
    }

    @Override
    protected void processFocusEvent( FocusEvent fe ) {
        super.processFocusEvent( fe );
        if( fe.getID() == FocusEvent.FOCUS_LOST ) {
            stopTimer();
        }
    }
    
    private static final Icon EMPTY_ICON = new Icon() {

        @Override
        public void paintIcon( Component c, Graphics g, int x, int y ) {
        }

        @Override
        public int getIconWidth() {
            return 10;
        }

        @Override
        public int getIconHeight() {
            return 10;
        }
        
    };
}
