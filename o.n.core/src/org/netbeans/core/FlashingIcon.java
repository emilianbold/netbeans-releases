/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * A flashing icon to provide visual feedback for the user when something
 * not very important happens in the system.
 * The icon is flashed for a few seconds and then remains visible for a while longer. 
 *
 * @author saubrecht
 */
abstract class FlashingIcon extends JComponent implements MouseListener {
    
    private static final long STOP_FLASHING_DELAY = 5 * 1000;
    private static final long DISAPPEAR_DELAY_MILLIS = STOP_FLASHING_DELAY + 30 * 1000;
    
    private Icon icon;
    
    private boolean keepRunning = false;
    private boolean isIconVisible = false;
    private boolean keepFlashing = true;
    private long startTime = 0;
    private RequestProcessor rp;
    private Task timerTask;
    
    /** 
     * Creates a new instance of FlashingIcon 
     *
     * @param icon The icon that will be flashing (blinking)
     */
    protected FlashingIcon( Icon icon ) {
        this.icon = icon;
        Dimension d = new Dimension( icon.getIconWidth(), icon.getIconHeight() );
        setMinimumSize( d );
        setMaximumSize( d );
        setPreferredSize( d );
        
        addMouseListener( this );
        rp = new RequestProcessor( "Exception Notification Icon" ); //NOI18N
    }

    /**
     * Start flashing of the icon. If the icon is already flashing, the timer
     * is reset.
     * If the icon is visible but not flashing, it starts flashing again
     * and the disappear timer is reset.
     */
    public void startFlashing() {
        synchronized( this ) {
            startTime = System.currentTimeMillis();
            isIconVisible = !isIconVisible;
            keepRunning = true;
            keepFlashing = true;
            if( null == timerTask ) {
                timerTask = rp.create( new Timer() );
            }
            timerTask.run();
        }
        repaint();
    }
    
    /**
     * Stop the flashing and hide the icon.
     */
    public void disappear() {
        synchronized( this ) {
            keepRunning = false;
            isIconVisible = false;
            keepFlashing = false;
            if( null != timerTask )
                timerTask.cancel();
            timerTask = null;
            setToolTipText( null );
        }
        repaint();
    }
    
    /**
     * Stop flashing of the icon. The icon remains visible and active (listens 
     * for mouse clicks and displays tooltip) until the disappear timer expires.
     */
    public void stopFlashing() {
        synchronized( this ) {
            if( keepRunning && !isIconVisible ) {
                isIconVisible = true;
                repaint();
            }
        }
        keepFlashing = false;
    }
    
    /**
     * Switch the current image and repaint
     */
    protected void flashIcon() {
        isIconVisible = !isIconVisible;
        
        repaint();
    }

    public void paint(java.awt.Graphics g) {
        if( isIconVisible ) {
            icon.paintIcon( this, g, 0, 0 );
        }
    }

    public void mouseReleased(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        stopFlashing();
    }

    public void mouseExited(MouseEvent e) {
        stopFlashing();
    }

    public void mouseEntered(MouseEvent e) {
        stopFlashing();
    }

    public void mouseClicked(MouseEvent e) {
        if( isIconVisible ) {
            disappear();
            onMouseClick();
        }
    }
    
    /**
     * Invoked when the user clicks the icon.
     */
    protected abstract void onMouseClick();

    /**
     * Invoked when the disappear timer expired.
     */
    protected abstract void timeout();

    public Cursor getCursor() {

        if( isIconVisible ) {
            return Cursor.getPredefinedCursor( Cursor.HAND_CURSOR );
        }
        return Cursor.getDefaultCursor();
    }

    public Point getToolTipLocation( MouseEvent event ) {

        JToolTip tip = createToolTip();
        tip.setTipText( getToolTipText() );
        Dimension d = tip.getPreferredSize();
        
        
        return new Point( getWidth()-d.width, -d.height );
    }
    
    private class Timer implements Runnable {
        public void run() {
            synchronized( FlashingIcon.this ) {
                try {
                    long currentTime = System.currentTimeMillis();
                    if( keepFlashing ) {
                        if( currentTime - startTime < STOP_FLASHING_DELAY ) {
                            flashIcon();
                        } else {
                            stopFlashing();
                        }
                    }
                    if( currentTime - startTime >= DISAPPEAR_DELAY_MILLIS ) {
                        disappear();
                        timeout();
                    } else {
                        if( null != timerTask )
                            timerTask.schedule( 500 );
                    }
                } catch( Throwable e ) {
                    //swallow all exceptions to avoid endless exception <-> notification loop
                    e.printStackTrace();
                }
            }
        }
    }
}
