/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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

/** 
 * A flashing icon to provide visual feedback for the user when something 
 * not-so-important happened in the system.
 * The icon is flashing for a few seconds and then remains visible for some more
 * time. 
 * Overriding classes should provide implementation for methods onMouseClick() 
 * and timeout().
 *
 * @author saubrecht
 */
class FlashingIcon extends JComponent implements MouseListener {
    
    public static final long STOP_FLASHING_DELAY = 5*1000;
    public static final long DISAPPER_DELAY_MILLIS = STOP_FLASHING_DELAY + 30*1000;
    
    private Icon icon;
    
    private boolean keepRunning = false;
    private Timer timer;
    private boolean isIconVisible = false;
    private boolean keepFlashing = true;
    private long startTime = 0;
    
    /** 
     * Creates a new instance of FlashingIcon 
     *
     * @param icon The icon that will be flashing (blinking)
     */
    public FlashingIcon( Icon icon ) {
        this.icon = icon;
        Dimension d = new Dimension( icon.getIconWidth(), icon.getIconHeight() );
        setMinimumSize( d );
        setMaximumSize( d );
        setPreferredSize( d );
        
        addMouseListener( this );
    }

    /**
     * Start flashing of the icon. If the icon is already flashing, the timer
     * is reset.
     * If the icon is visible but not flashing, it starts flashing again
     * and the disapper timer is reset.
     */
    public void startFlashing() {
        synchronized( this ) {
            startTime = System.currentTimeMillis();
            isIconVisible = !isIconVisible;
            keepRunning = true;
            keepFlashing = true;
            if( null == timer ) {
                timer = new Timer();
                timer.start();
            }
        }
        repaint();
    }
    
    /**
     * Stop the flashing and hide the icon.
     */
    public void disapper() {
        synchronized( this ) {
            keepRunning = false;
            isIconVisible = false;
            keepFlashing = false;
            timer = null;
            setToolTipText( null );
        }
        repaint();
    }
    
    /**
     * Stop flashing of the icon. The icon remains visible and active (listens 
     * for mouse clicks and displays tooltip) until the disapper timer expires.
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

    public void mouseReleased(java.awt.event.MouseEvent e) {
    }

    public void mousePressed(java.awt.event.MouseEvent e) {
        stopFlashing();
    }

    public void mouseExited(java.awt.event.MouseEvent e) {
        stopFlashing();
    }

    public void mouseEntered(java.awt.event.MouseEvent e) {
        stopFlashing();
    }

    public void mouseClicked(java.awt.event.MouseEvent e) {
        if( isIconVisible ) {
            disapper();
            onMouseClick();
        }
    }
    
    /**
     * Invoked when the user clicks the icon.
     */
    protected void onMouseClick() {
    }

    /**
     * Invoked when the disappear timer expired.
     */
    protected void timeout() {
    }

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
        
        
        Point retValue = new Point( getWidth()-d.width, -d.height );
        return retValue;
    }
    
    private class Timer extends Thread {
        public void run() {
            while( keepRunning ) {
                synchronized( FlashingIcon.this ) {
                    long currentTime = System.currentTimeMillis();
                    if( keepFlashing ) {
                        if( currentTime - startTime < STOP_FLASHING_DELAY ) {
                            flashIcon();
                        } else {
                            stopFlashing();
                        }
                    }
                    if( currentTime - startTime >= DISAPPER_DELAY_MILLIS ) {
                        disapper();
                        timeout();
                        break;
                    }
                }
                try {
                    sleep( 500 );
                } catch( InterruptedException iE ) {
                    //ignore
                }
            }
        }
    }
}
