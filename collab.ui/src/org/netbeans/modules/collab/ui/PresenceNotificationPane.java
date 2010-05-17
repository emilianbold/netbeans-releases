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
package org.netbeans.modules.collab.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;

import org.openide.util.*;

import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.UserInterface;
import org.netbeans.modules.collab.core.Debug;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class PresenceNotificationPane extends JPanel implements MouseListener {
    ////////////////////////////////////////////////////////////////////////////
    // Class fields
    ////////////////////////////////////////////////////////////////////////////
    private static final int WIDTH = 150;
    private static final int HEIGHT = 125;
    private static final int HORIZONTAL_PADDING = 20; // 16+2+2, icon + 2 padding
    private static final float MAX_ALPHA = 0.95f;
    private static final int FADE_IN_TIME = 750; // in milliseconds
    private static final int FADE_OUT_TIME = 2000; // in milliseconds
    private static final int QUICK_FADE_OUT_TIME = 300; // in milliseconds
    private static final int DELAY_TIME = 4000; // in milliseconds
    private static final Color BORDER_COLOR = (UIManager.getColor("standard_border") != null)
        ? UIManager.getColor("standard_border") : new Color(127, 157, 185);
    private static final Color HIGHLIGHT_COLOR = (UIManager.getColor("tab_highlight_header_fill") != null)
        ? UIManager.getColor("tab_highlight_header_fill") : new Color(255, 199, 60);
    private static final Color HIGHLIGHT_COLOR_2 = (UIManager.getColor("tab_highlight_header") != null)
        ? UIManager.getColor("tab_highlight_header") : new Color(230, 139, 44);
    private static final Font FONT = new JLabel().getFont();
    private static Image icon;
    private static Image backgroundImage;

    ////////////////////////////////////////////////////////////////////////////
    // Static initializer
    ////////////////////////////////////////////////////////////////////////////
    static {
        try {
            icon = Utilities.loadImage("org/netbeans/modules/collab/core/resources/account_png.gif"); // NOI18N
        } catch (Exception e) {
            // Ignore
            Debug.debugNotify(e);
        }

        try {
            backgroundImage = Utilities.loadImage("org/netbeans/modules/collab/ui/resources/login_bg.jpg"); // NOI18N
        } catch (Exception e) {
            // Ignore
            Debug.debugNotify(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    private Notification notification;
    private java.util.List notifications = new LinkedList();
    private Object LOCK = new Object();
    private NotificationThread notificationThread;
    private Component dispatchComponent;

    /**
     *
     *
     */
    public PresenceNotificationPane() {
        this(null);
    }

    /**
     *
     *
     */
    public PresenceNotificationPane(Component dispatchComponent) {
        super();
        setOpaque(false);
        setVisible(true);
        this.dispatchComponent = dispatchComponent;
    }

    /**
     *
     *
     */
    public void removeNotify() {
        super.removeNotify();

        if (getNotificationThread() != null) {
            ((NotificationThread) getNotificationThread()).stopThread();
        }
    }

    /**
     *
     *
     */
    protected Thread getNotificationThread() {
        return notificationThread;
    }

    /**
     *
     *
     */
    public void showContactNotification(CollabPrincipal contact, int type) {
        // Show a notification only for certain status changes		
        switch (type) {
        case UserInterface.NOTIFY_USER_STATUS_AWAY:
        case UserInterface.NOTIFY_USER_STATUS_BUSY:
        case UserInterface.NOTIFY_USER_STATUS_IDLE:
        case UserInterface.NOTIFY_USER_STATUS_OFFLINE:
        case UserInterface.NOTIFY_USER_STATUS_ONLINE:

            synchronized (LOCK) {
                // Push a notification onto the queue
                notifications.add(new Notification(contact, type));

                //Fix for bug # 6267322
                if (
                    (contact.getStatus() == CollabPrincipal.STATUS_IDLE) ||
                        (contact.getStatus() == CollabPrincipal.STATUS_AWAY)
                ) {
                    notifications.clear();
                }

                dispatchNextNotification();
            }

            break;

        default:

            // Do nothing
            break;
        }
    }

    /**
     *
     *
     */
    protected void dispatchNextNotification() {
        synchronized (LOCK) {
            // Proceed only if there is no current notification but there are
            // notifications waiting
            if ((notification == null) && (notifications.size() > 0)) {
                // Dequeue the next notification
                notification = (Notification) notifications.remove(0);

                // Either create a new thread for showing the notification,
                // or wake up the current thread
                if (notificationThread == null) {
                    notificationThread = new NotificationThread();
                    notificationThread.start();
                } else {
                    LOCK.notify();
                }
            }
        }
    }

    /**
     *
     *
     */
    public void paintComponent(Graphics g) {
        long time1 = System.currentTimeMillis();

        synchronized (LOCK) {
            if (notification != null) {
                long time2 = System.currentTimeMillis();

                // Paint the notification using its current visual state
                notification.paint(g);

                //Debug.out.println("Time to paint: "+(System.currentTimeMillis()-time2)+" ms");
                // If the notification is complete, then remove it and do the
                // the next one in the queue (if present)
                if (notification.isComplete()) {
                    notification = null;
                    removeMouseListener(PresenceNotificationPane.this);
                    dispatchNextNotification();
                } else {
                    // Notify the thread waiting on the repaint to finish
                    // Note, this is key to making the animation of the
                    // notification smooth.
                    synchronized (LOCK) {
                        LOCK.notify();
                    }
                }
            }
        }

        //Debug.out.println("Time to paint (all): "+(System.currentTimeMillis()-time1)+" ms");
    }

    ////////////////////////////////////////////////////////////////////////////
    // Helper methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public static boolean isXPTheme() {
        Boolean isXP = (Boolean) Toolkit.getDefaultToolkit().getDesktopProperty("win.xpstyle.themeActive");

        return (isXP == null) ? false : isXP.booleanValue();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Mouse listener methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void mouseClicked(MouseEvent event) {
        //		redispatchMouseEvent(event);
        // Note, when the mouse listener is attached, all events are captured
        // by this glass pane instead of the underlying components. We would
        // need to redispatch the event in order for them to receive it.
        synchronized (LOCK) {
            if (notification != null) {
                notification.close();
                LOCK.notify();
            }
        }
    }

    /**
     *
     *
     */
    public void mouseEntered(MouseEvent event) {
    }

    /**
     *
     *
     */
    public void mouseExited(MouseEvent event) {
    }

    /**
     *
     *
     */
    public void mousePressed(MouseEvent event) {
    }

    /**
     *
     *
     */
    public void mouseReleased(MouseEvent event) {
    }

    // TAF: Note, this method doesn't actually work.  It's just for reference at 
    // the moment.
    //	//A more finished version of this method would
    //	//handle mouse-dragged events specially.
    //	private void redispatchMouseEvent(MouseEvent event)
    //	{
    //		Point glassPanePoint=event.getPoint();
    //
    //        Point containerPoint=SwingUtilities.convertPoint(
    //			this,glassPanePoint,getParent());
    //
    //		//Find out exactly which component it's over
    //		Component component=SwingUtilities.getDeepestComponentAt(
    //			getParent(),containerPoint.x,containerPoint.y);
    //
    //		if (component!=null)
    //		{
    //			// Forward events over the check box
    //			Point componentPoint=SwingUtilities.convertPoint(
    //				this,glassPanePoint,component);
    //			component.dispatchEvent(new MouseEvent(component,
    //												 event.getID(),
    //												 event.getWhen(),
    //												 event.getModifiers(),
    //												 componentPoint.x,
    //												 componentPoint.y,
    //												 event.getClickCount(),
    //												 event.isPopupTrigger()));
    //		}
    //	}
    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected class NotificationThread extends Thread {
        private boolean stopped = false;

        /**
         *
         *
         */
        public NotificationThread() {
            super("Collaboration Presence Notification");
            setPriority(3);
            setDaemon(true);
        }

        /**
         *
         *
         */
        public void run() {
            try {
                while (!stopped) {
                    synchronized (LOCK) {
                        // Wait a short amount of time (target = 30 fps)
                        LOCK.wait(Math.round(1000 / 30));

                        if (notification != null) {
                            // Update the state of the notification object.
                            // If the state was modified, repaint the component
                            // and wait until the repaint is done.  Note that
                            // it is possible for us to be notified not because
                            // the paint has completed but because another
                            // notification has been placed on the queue.  This
                            // is fine; it just means an extra repaint in the
                            // latter case.
                            if (notification.update()) {
                                repaint();
                                LOCK.wait();
                            }

                            if (notification.isComplete()) {
                                repaint();
                                LOCK.wait();
                            }
                        } else {
                            // Wait for the next notification to be pushed 
                            // onto the queue
                            LOCK.wait();
                        }
                    }
                }
            } catch (Throwable e) {
                Debug.debugNotify(e);
            } finally {
                synchronized (LOCK) {
                    if (notificationThread == this) {
                        notificationThread = null;
                    }

                    notification = null;

                    removeMouseListener(PresenceNotificationPane.this);
                }
            }
        }

        /**
         *
         *
         */
        public void stopThread() {
            stopped = true;

            synchronized (LOCK) {
                LOCK.notify();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    private class Notification extends Object {
        //		/**
        //		 *
        //		 *
        //		 */
        //		private final int[] unpackPixel(int pixel)
        //		{
        //			return new int[] {
        ////				((pixel & 0xff000000) >> 24) & 0xff,	// a
        //				(pixel & 0x00ff0000) >> 16,				// r
        //				(pixel & 0x0000ff00) >> 8,				// g
        //				(pixel & 0x000000ff) >> 0 };			// b
        //		}
        //
        //
        //		/**
        //		 *
        //		 *
        //		 */
        //		private final int packPixel(int[] pixel)
        //		{
        //			int result=0xff000000;
        ////			result+=(pixel[0] & 0x000000ff) << 24; 
        //			result+=(pixel[0] & 0x000000ff) << 16; 
        //			result+=(pixel[1] & 0x000000ff) << 8; 
        //			result+=(pixel[2] & 0x000000ff) << 0; 
        //			return result;
        //		}
        //
        //
        //		/**
        //		 *
        //		 *
        //		 */
        //		private final void addUnpackedPixels(int[] dest, int[] source)
        //		{
        //			for (int i=0; i<dest.length; i++)
        //				dest[i]+=source[i];
        //		}
        //
        //
        //		/**
        //		 *
        //		 *
        //		 */
        //		private final void addUnpackedPixels(int[] dest, int[] source, float bias)
        //		{
        //			for (int i=0; i<dest.length; i++)
        //				dest[i]=Math.round(dest[i]*(1f-bias)+source[i]*(bias));
        //		}
        private boolean initialized;
        private boolean complete;
        private float currentAlpha;
        private long startTime;
        private float percentShown;
        private boolean renderedSteadyState;
        private BufferedImage bufferImage;
        private boolean explicitlyClosed;

        /**
         *
         *
         */
        public Notification(CollabPrincipal contact, int notification) {
            super();
            prepareBuffer(contact, notification);
        }

        /**
         *
         *
         */
        public synchronized boolean isComplete() {
            return complete;
        }

        /**
         *
         *
         */
        public void close() {
            synchronized (LOCK) {
                explicitlyClosed = true;
                startTime = System.currentTimeMillis() - FADE_IN_TIME - DELAY_TIME;

                // Close quickly
                startTime -= (FADE_OUT_TIME - QUICK_FADE_OUT_TIME);
            }
        }

        /**
         * Implements a simpe time-based state machine that drives the
         * rendering parameters for this notification.
         *
         */
        public synchronized boolean update() {
            if (!initialized) {
                startTime = System.currentTimeMillis();
                initialized = true;

                // State not modified
                return false;
            }

            // Render the message using the opacity curve specified by the 
            // three time intervals: fade in, delay, and fade out
            long elapsedTime = System.currentTimeMillis() - startTime;

            if (elapsedTime <= FADE_IN_TIME) {
                float percentComplete = ((float) elapsedTime / (float) FADE_IN_TIME);

                // Fade in the message
                currentAlpha = Math.min(MAX_ALPHA, (MAX_ALPHA * percentComplete) + 0.1f);
                percentShown = percentComplete;

                // State modified
                return true;
            } else if (elapsedTime <= (FADE_IN_TIME + DELAY_TIME)) {
                if (!renderedSteadyState) {
                    renderedSteadyState = true;

                    // Render the message
                    currentAlpha = MAX_ALPHA;
                    percentShown = 1.0f;

                    // Note, calling this method results in all mouse events being
                    // captured by the glass pane.
                    addMouseListener(PresenceNotificationPane.this);

                    // State mofified
                    return true;
                } else {
                    // State not modified
                    return false;
                }
            } else if (elapsedTime <= (FADE_IN_TIME + DELAY_TIME + FADE_OUT_TIME)) {
                removeMouseListener(PresenceNotificationPane.this);

                if (explicitlyClosed) {
                    int timeIntoNormalFade = (int) (elapsedTime - FADE_IN_TIME - DELAY_TIME);
                    currentAlpha = (float) (FADE_OUT_TIME - timeIntoNormalFade) / ((float) (QUICK_FADE_OUT_TIME));

                    // Fade out if closed
                    percentShown = 1.0f;
                } else {
                    currentAlpha = MAX_ALPHA -
                        (MAX_ALPHA * ((float) (elapsedTime - FADE_IN_TIME - DELAY_TIME) / (float) FADE_OUT_TIME));

                    // Otherwise, flip down
                    float percentComplete = ((float) (elapsedTime - FADE_IN_TIME - DELAY_TIME) / (float) FADE_OUT_TIME);
                    percentShown = -(1.0f - percentComplete);
                }

                // State modified
                return true;
            } else {
                complete = true;
                currentAlpha = 0f;
                removeMouseListener(PresenceNotificationPane.this);

                // State not modified
                return false;
            }
        }

        /**
         *
         *
         */
        private synchronized void prepareBuffer(CollabPrincipal contact, int notification) {
            try {
                // Calculate the message and extents of the notification box
                int messageWidth = WIDTH - (2 * HORIZONTAL_PADDING);

                // Construct the message
                String[] message = new String[3];
                message[0] = contact.getCollabSession().getUserPrincipal().getDisplayName();
                message[1] = contact.getDisplayName();

                String keyName = "LBL_SessionsTreeView_StatusUnknown";

                switch (notification) {
                case UserInterface.NOTIFY_USER_STATUS_AWAY:
                    keyName = "LBL_SessionsTreeView_StatusAway"; // NOI18N

                    break;

                case UserInterface.NOTIFY_USER_STATUS_BUSY:
                    keyName = "LBL_SessionsTreeView_StatusBusy"; // NOI18N

                    break;

                case UserInterface.NOTIFY_USER_STATUS_IDLE:
                    keyName = "LBL_SessionsTreeView_StatusIdle"; // NOI18N

                    break;

                case UserInterface.NOTIFY_USER_STATUS_OFFLINE:
                    keyName = "LBL_SessionsTreeView_StatusOffline"; // NOI18N

                    break;

                case UserInterface.NOTIFY_USER_STATUS_ONLINE:
                    keyName = "LBL_SessionsTreeView_StatusOnline"; // NOI18N

                    break;
                }

                message[2] = NbBundle.getMessage(SessionsTreeView.class, keyName); // NOI18N

                // Create a temp buffered image in order to measure font metrics
                bufferImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

                Graphics g = bufferImage.getGraphics();

                // Set the font to the standard
                g.setFont(FONT);

                // Check to make sure the width of the message box is 
                // sufficient
                for (int i = 0; i < message.length; i++) {
                    int stringWidth = g.getFontMetrics().stringWidth(message[i]);

                    if (stringWidth > (messageWidth)) {
                        messageWidth = stringWidth;
                    }
                }

                final int width = messageWidth + (2 * HORIZONTAL_PADDING);
                final int height = HEIGHT;

                // Create the real buffer image now that we know the 
                // appropriate width to use
                bufferImage = new BufferedImage(width + 1, height, BufferedImage.TYPE_INT_ARGB);
                g = bufferImage.getGraphics();

                // Set the font to the standard
                g.setFont(FONT);

                // Set transparency for message
                Graphics2D g2d = null;

                if (g instanceof Graphics2D) {
                    g2d = (Graphics2D) g;

                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }

                final float percentShown = 1.0f;

                final int lineHeight = g.getFontMetrics().getHeight();
                final int linePadding = g.getFontMetrics().getMaxDescent() + 2;

                // Determine the x coordinate.  The scrollbar may be showing,
                // so we want to avoid painting over it.  Note that this
                // arises because this class ultimately derives from 
                // JScrollPane.
                final int left = 0;

                // Determine the y coordinate.  The scrollbar may be showing,
                // so we want to avoid painting over it.  Note that this
                // arises because this class ultimately derives from 
                // JScrollPane.
                final int top = 2;

                // Draw the message box
                if (backgroundImage != null) {
                    final int XOFFSET = 50;
                    final int YOFFSET = 90;

                    // Draw a nice watermark image
                    int imageInset = lineHeight + linePadding;
                    int imageHeight = height - imageInset;

                    // Draw a simple white background
                    g.setColor(Color.white);
                    g.fillRect(left, top + imageInset, width, imageHeight);

                    // Set transparency for background image
                    if (g2d != null) {
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f * MAX_ALPHA));
                    }

                    g.drawImage(
                        backgroundImage, left, top + imageInset, left + width, top + height, XOFFSET, YOFFSET,
                        XOFFSET + width, YOFFSET + imageHeight, Color.white, null
                    );

                    // Set transparency for background image
                    if (g2d != null) {
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    }
                } else {
                    // Draw a simple white background
                    g.setColor(Color.white);
                    g.fillRect(left, top, width, height);
                }

                // Draw the outline border
                g.setColor(BORDER_COLOR);
                g.drawRect(left, top, width, height);

                // Draw the XP tab highlight if using XP L&F
                if (isXPTheme()) {
                    // Body of highlight
                    g.setColor(HIGHLIGHT_COLOR);
                    g.drawLine(left, top, left + width, top);
                    g.drawLine(left + 2, top - 1, (left + width) - 2, top - 1);

                    // Top highlighted edge
                    g.setColor(HIGHLIGHT_COLOR_2);
                    g.drawLine(left + 2, top - 2, (left + width) - 2, top - 2); // line

                    // Endcaps
                    g.setColor(HIGHLIGHT_COLOR_2);
                    g.drawLine(left, top, left, top); // left dot
                    g.drawLine(left + 1, top - 1, left + 1, top - 1); // left dot
                    g.drawLine(left + width, top, left + width, top); // right dot
                    g.drawLine((left + width) - 1, top - 1, (left + width) - 1, top - 1); // right
                }

                // Draw header
                g.setColor(Color.white);
                g.fillRect(left + 1, top + 1, width - 2, lineHeight + linePadding);

                // Draw the account name
                int lineX = (left + (width / 2)) - (g.getFontMetrics().stringWidth(message[0]) / 2);
                int lineY = top + lineHeight;
                g.setColor(Color.black);
                g.drawString(message[0], lineX, lineY);

                // Draw the icon
                if (icon != null) {
                    final int ICON_WIDTH = 16;
                    g.drawImage(
                        icon, left + 2, (top + (((lineY + linePadding) - top) / 2)) - (ICON_WIDTH / 2), Color.white,
                        null
                    );
                }

                // Draw the separator
                g.setColor(BORDER_COLOR);
                lineY += linePadding;
                g.drawLine(left, lineY, left + width, lineY);

                // Draw the rest of the message
                lineX = (left + (width / 2)) - (g.getFontMetrics().stringWidth(message[1]) / 2);
                lineY += (((height - (lineY - top)) / 2) - linePadding);
                g.setColor(Color.black);
                g.drawString(message[1], lineX, lineY);

                // Draw the rest of the message, next line
                lineX = (left + (width / 2)) - (g.getFontMetrics().stringWidth(message[2]) / 2);
                lineY += lineHeight;
                g.setColor(Color.black);
                g.drawString(message[2], lineX, lineY);
            } catch (Exception e) {
                // Ignore
                Debug.debugNotify(e);
                complete = true;
            }
        }

        /**
         * Renders this notification using the current rendering parameter
         * state.  This version renders the message as a 2D card rising in
         * the Y axis.
         *
         */

        //		public synchronized void paint(final Graphics g)
        //		{
        //			if (bufferImage==null)
        //			{
        //				complete=true;
        //				return;
        //			}
        //
        //			final int width=bufferImage.getWidth(null);
        //			final int height=bufferImage.getHeight(null);
        //
        //			// Determine the x coordinate.  The scrollbar may be showing,
        //			// so we want to avoid painting over it.  Note that this
        //			// arises because this class ultimately derives from 
        //			// JScrollPane.
        //			final int effectiveWidth=getWidth();
        //			final int left=(effectiveWidth-width)/2;
        //
        //			// Determine the y coordinate.  The scrollbar may be showing,
        //			// so we want to avoid painting over it.  Note that this
        //			// arises because this class ultimately derives from 
        //			// JScrollPane.
        //			final int effectiveHeight=getHeight();
        //			final int top=(effectiveHeight-height)+
        //				Math.round(height*(1.0f-percentShown));
        //
        //			// Set transparency for message
        //			Graphics2D g2d=null;
        //			if (g instanceof Graphics2D)
        //			{
        //				g2d=(Graphics2D)g;
        //
        //				// Ensure alpha is within bounds
        //				if (currentAlpha<0.0f)
        //					currentAlpha=0.0f;
        //				if (currentAlpha>1.0f)
        //					currentAlpha=1.0f;
        //
        //				g2d.setComposite(AlphaComposite.getInstance(
        //					AlphaComposite.SRC_OVER,currentAlpha));
        //			}
        //
        //			g.drawImage(bufferImage,left,top,width,height,null);
        //		}

        /**
         * Renders this notification using the current rendering parameter
         * state.  This version renders the message as a 3D card being flipped
         * up by being rotated around the X axis.
         *
         */
        public synchronized void paint(final Graphics g) {
            if (bufferImage == null) {
                complete = true;

                return;
            }

            // Set transparency for message
            Graphics2D g2d = null;

            if (g instanceof Graphics2D) {
                g2d = (Graphics2D) g;

                // Ensure alpha is within bounds
                if (currentAlpha < 0.0f) {
                    currentAlpha = 0.0f;
                }

                if (currentAlpha > 1.0f) {
                    currentAlpha = 1.0f;
                }

                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha));
            }

            BufferedImage image = getRenderableImage();
            int x = (getWidth() / 2) - (image.getWidth() / 2);
            int y = getHeight() - image.getHeight();
            g.drawImage(image, x, y, image.getWidth(), image.getHeight(), null);
        }

        /**
         *
         *
         */
        private BufferedImage getRenderableImage() {
            // Just return the texture image directly
            if (percentShown == 1.0) {
                return bufferImage;
            }

            // Pitch increments from 1.0 to 0.0
            final double pitch = Math.toRadians(90d * Math.abs(percentShown));
            final double percent = Math.cos(pitch);

            final int PERSPECTIVE_INSET = 25;
            final int MIN = bufferImage.getWidth();
            final int MAX = MIN + (PERSPECTIVE_INSET * 2);

            //			final double DIR=percentShown>0 ? 1 : -1;
            final double DIR = -1;
            int w = MAX;
            int h = Math.max(1, (int) Math.round(bufferImage.getHeight() * Math.sin(pitch)));

            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) image.getGraphics();

            // Calculate a polygon that simulates a 3D card being rotated
            // around the X axis
            Polygon poly = new Polygon();
            poly.addPoint((int) (PERSPECTIVE_INSET - (DIR * PERSPECTIVE_INSET * percent)), 0);
            poly.addPoint((int) (PERSPECTIVE_INSET + MIN + (DIR * PERSPECTIVE_INSET * percent)), 0);
            poly.addPoint(PERSPECTIVE_INSET + MIN, h);
            poly.addPoint(PERSPECTIVE_INSET, h);

            // Draw a solid color card
            //			g2d.setColor(Color.red);
            //			g2d.fill(poly);
            //			if (1==1)
            //				return image;
            // Texture map the message image onto the polygon.  We cheat a bit 
            // here and grab incremental chunks (of constant width but varying 
            // height) from the texture and use Java2D to average each chunk 
            // over a single scan line width/height as we draw the chunk into 
            // the final position.  This alleviates the need to do such 
            // averaging manually.  As a byproduct, doing it this way should
            // be faster if hardware acceleration is possible for the transform.
            // Note, if only the JDK included a warp transform, all this work
            // wouldn't be necessary; we could then simply warp the buffer 
            // image to our polygon shape directly.
            float ty = 0;
            final int tw = bufferImage.getWidth();
            final int th = bufferImage.getHeight();
            final float dy = (float) th / (float) h;

            Map hints = new HashMap();
            hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.addRenderingHints(hints);

            // For each scanline of the image, grab a texture "chunk" from the
            // texture and BLT it into place, scaling it as we do to fit the
            // polygon.
            for (int y = 0; y < h; y++) {
                // Track the distance form the left edge to the first pixel
                // that falls within the polygon
                int xoffset = 0;

                for (int x = 0; x < w; x++) {
                    // Find the first opaque pixel on the scanline; this is the
                    // only way to determine the width and starting x coord
                    // of this line, as Shape has no way of being manipulated
                    // as a raster image
                    if (poly.contains(x, y)) {
                        int scanHeight = Math.round(dy);
                        BufferedImage scanline = bufferImage.getSubimage(
                                0, Math.min(th - scanHeight, Math.round(ty)), tw, scanHeight
                            );
                        g2d.drawImage(scanline, xoffset, y, w - (2 * xoffset), 1, null);

                        // Next scanline
                        ty += dy;

                        break;
                    } else {
                        // Found a clear pixel (a pixel outside the polygon)
                        xoffset++;
                    }
                }
            }

            // Manual texture mapping.  This works, but looks a ugly
            // because there is no interpolation of texture pixels.
            //			for (int y=0; y<h; y++)
            //			{
            //				int xoffset=0;
            //	 			float dx=-1;
            //
            //				for (int x=0; x<w; x++)
            //				{
            //					if (poly.contains(x,y))
            //					{
            //						if (dx==-1)
            //							dx=(float)tw/(float)(w-2*xoffset);
            //
            //						float txf=(x-xoffset)*dx;
            //						float tyf=y*dy;
            //
            //						tx=Math.min(Math.round(txf),tw-1);
            //						ty=Math.min(Math.round(tyf),th-1);
            //
            //						int[] targetPixel=unpackPixel(
            //							bufferImage.getRGB(tx,ty));
            //
            //						image.setRGB(x,y,packPixel(targetPixel));
            //					}
            //					else
            //					{
            //						xoffset++;
            //					}
            //				}
            //			}
            return image;
        }
    }
}
