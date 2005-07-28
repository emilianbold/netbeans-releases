/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui;

import org.openide.*;
import org.openide.awt.*;
import org.openide.explorer.*;
import org.openide.explorer.view.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.util.io.*;
import org.openide.windows.*;

import java.applet.*;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.net.URL;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.ui.actions.*;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class SessionsTreeView extends BeanTreeView {
    /**
     *
     *
     */
    public SessionsTreeView() {
        super();
        getJTree().setDoubleBuffered(true);

        //		try
        //		{
        //			icon=Utilities.loadImage(
        //				"org/netbeans/modules/collab/core/resources/account_png.gif"); // NOI18N
        //		}
        //		catch (Exception e)
        //		{
        //			// Ignore
        //			Debug.debugNotify(e);
        //		}
        //		try
        //		{
        //			backgroundImage=Utilities.loadImage(
        //				"org/netbeans/modules/collab/ui/resources/login_bg.jpg"); // NOI18N
        //		}
        //		catch (Exception e)
        //		{
        //			// Ignore
        //			Debug.debugNotify(e);
        //		}
        //		try
        //		{
        //			URL url=DefaultUserInterface.class.getResource(
        //				"/org/netbeans/modules/collab/ui/sound/pop2.wav");
        //			pop=Applet.newAudioClip(url);
        //		}
        //		catch (Exception e)
        //		{
        //			// Ignore
        //			Debug.debugNotify(e);
        //		}
    }

    //	/**
    //	 *
    //	 *
    //	 */
    //	public boolean isOptimizedDrawingEnabled()
    //	{
    //		return false;
    //	}

    /**
     * Upgrade to public access
     *
     */
    public JTree getJTree() {
        return tree;
    }

    //	/**
    //	 *
    //	 *
    //	 */
    //	protected Thread getNotificationThread()
    //	{
    //		return notificationThread;
    //	}
    //	/**
    //	 *
    //	 *
    //	 */
    //	public void showContactNotification(CollabPrincipal contact, int type)
    //	{
    //		// Show a notification only for certain status changes
    //		switch (type)
    //		{
    //			case UserInterface.NOTIFY_USER_STATUS_AWAY:
    //			case UserInterface.NOTIFY_USER_STATUS_BUSY:
    //			case UserInterface.NOTIFY_USER_STATUS_IDLE:
    //			case UserInterface.NOTIFY_USER_STATUS_OFFLINE:
    //			case UserInterface.NOTIFY_USER_STATUS_ONLINE:
    //				synchronized (LOCK)
    //				{
    //					// Push a notification onto the queue
    //					notifications.add(new Notification(contact,type));
    //					dispatchNextNotification();
    //				}
    //				break;
    //			default:
    //				// Do nothing
    //				break;
    //		}
    //	}
    //	/**
    //	 *
    //	 *
    //	 */
    //	protected void dispatchNextNotification()
    //	{
    //		synchronized (LOCK)
    //		{
    //			if (notification==null && notifications.size()>0)
    //			{
    //				notification=(Notification)notifications.remove(0);
    //				if (notificationThread==null)
    //				{
    //					notificationThread=new NotificationThread();
    //					notificationThread.start();
    //				}
    //				else
    //				{
    //					LOCK.notify();
    //				}
    //			}
    //		}
    //	}
    //	/**
    //	 *
    //	 *
    //	 */
    //	public void paint(Graphics g)
    //	{
    //		super.paint(g);
    //
    //		synchronized (LOCK)
    //		{
    //			if (notification!=null)
    //			{
    //				if (!notification.paintNextFrame(g))
    //				{
    //					notification=null;
    //					dispatchNextNotification();
    //				}
    //			}
    //		}
    //	}
    ////////////////////////////////////////////////////////////////////////////
    // Helper methods
    ////////////////////////////////////////////////////////////////////////////
    //	/**
    //	 *
    //	 *
    //	 */
    //    public static boolean isXPTheme()
    //	{
    //        Boolean isXP=(Boolean)Toolkit.getDefaultToolkit().
    //			getDesktopProperty("win.xpstyle.themeActive");
    //        return isXP==null ? false : isXP.booleanValue();
    //    }
    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////
    //	/**
    //	 *
    //	 *
    //	 */
    //	protected class NotificationThread extends Thread
    //	{
    //		/**
    //		 *
    //		 *
    //		 */
    //		public NotificationThread()
    //		{
    //			super("Collaboration Status Notification");
    //			setPriority(3);
    //			setDaemon(true);
    //		}
    //
    //
    //		/**
    //		 *
    //		 *
    //		 */
    //		public void run()
    //		{
    //			try
    //			{
    //				while (!stopped)
    //				{
    //					synchronized (LOCK)
    //					{
    //						Thread.currentThread().sleep(50);
    //
    //						repaint();
    //						LOCK.wait();
    //					}
    //				}
    //			}
    //			catch (Throwable e)
    //			{
    //				notificationThread=null;
    //				notification=null;
    //				Debug.debugNotify(e);
    //			}
    //		}
    //
    //
    //		/**
    //		 *
    //		 *
    //		 */
    //		public void exit()
    //		{
    //			stopped=true;
    //			synchronized (LOCK)
    //			{
    //				LOCK.notify();
    //			}
    //		}
    //
    //		private boolean stopped=false;
    //	}
    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////
    //	/**
    //	 *
    //	 *
    //	 */
    //	private class Notification extends Object
    //	{
    //		/**
    //		 *
    //		 *
    //		 */
    //		public Notification(CollabPrincipal contact, int notification)
    //		{
    //			super();
    //			this.contact=contact;
    //			this.notification=notification;
    //		}
    //
    //
    //		/**
    //		 *
    //		 *
    //		 */
    //		public boolean paintNextFrame(Graphics g)
    //		{
    //			if (g==null)
    //				return false;
    //
    //			boolean result=true;
    //			if (!initialized)
    //			{
    //				// Calculate the message and extents of the notification box
    //				messageWidth=WIDTH-2*HORIZONTAL_PADDING;
    //
    //				// Construct the message
    //				message=new String[3];
    //				message[0]=contact.getCollabSession()
    //					.getUserPrincipal().getDisplayName();
    //				message[1]=contact.getDisplayName();
    //
    //				String keyName="LBL_SessionsTreeView_StatusUnknown";
    //				switch (notification)
    //				{
    //					case UserInterface.NOTIFY_USER_STATUS_AWAY:
    //						keyName="LBL_SessionsTreeView_StatusAway"; // NOI18N
    //						break;
    //					case UserInterface.NOTIFY_USER_STATUS_BUSY:
    //						keyName="LBL_SessionsTreeView_StatusBusy"; // NOI18N
    //						break;
    //					case UserInterface.NOTIFY_USER_STATUS_IDLE:
    //						keyName="LBL_SessionsTreeView_StatusIdle"; // NOI18N
    //						break;
    //					case UserInterface.NOTIFY_USER_STATUS_OFFLINE:
    //						keyName="LBL_SessionsTreeView_StatusOffline"; // NOI18N
    //						break;
    //					case UserInterface.NOTIFY_USER_STATUS_ONLINE:
    //						keyName="LBL_SessionsTreeView_StatusOnline"; // NOI18N
    //						break;
    //				}
    //
    //				message[2]=NbBundle.getMessage(
    //					SessionsTreeView.class,keyName); // NOI18N
    //
    //				// Check to make sure the width of the message box is 
    //				// sufficient
    //				for (int i=0; i<message.length; i++)
    //				{
    //					int stringWidth=g.getFontMetrics().stringWidth(message[i]);
    //					if (stringWidth>(messageWidth))
    //						messageWidth=stringWidth;
    //				}
    //
    //				startTime=System.currentTimeMillis();
    //				initialized=true;
    //			}
    //
    //			// Render the message using the opacity curve specified by the 
    //			// three time intervals: fade in, delay, and fade out
    //			long elapsedTime=System.currentTimeMillis()-startTime;
    //			if (elapsedTime<=FADE_IN_TIME)
    //			{
    //				float percentComplete=((float)elapsedTime/(float)FADE_IN_TIME);
    //
    //				// Fade in the message
    //				currentAlpha=MAX_ALPHA*percentComplete;
    //				paint(g,percentComplete);
    //			}
    //			else
    //			if (elapsedTime<=FADE_IN_TIME+DELAY_TIME)
    //			{
    //				// Render the message
    //				currentAlpha=MAX_ALPHA;
    //				paint(g,1.0f);
    //			}
    //			else
    //			if (elapsedTime<=FADE_IN_TIME+DELAY_TIME+FADE_OUT_TIME)
    //			{
    //				// Fade out the message
    //				currentAlpha=MAX_ALPHA-MAX_ALPHA*
    //					((float)(elapsedTime-FADE_IN_TIME-DELAY_TIME)/
    //						(float)FADE_OUT_TIME);
    //				paint(g,1.0f);
    //
    ////				if (pop!=null && !played)
    ////				{
    ////					pop.play();
    ////					played=true;
    ////				}
    //			}
    //			else
    //			{
    //				// Done
    //				result=false;
    //			}
    //
    //			// Notify the waiting thread
    //			synchronized (LOCK)
    //			{
    //				LOCK.notify();
    //			}
    //
    //			return result;
    //		}
    //
    //
    //		/**
    //		 *
    //		 *
    //		 */
    //		public void paint(final Graphics g, float percentShown)
    //		{
    //			try
    //			{
    //				// Set transparency for message
    //				Graphics2D g2d=null;
    //				if (g instanceof Graphics2D)
    //				{
    //					g2d=(Graphics2D)g;
    //
    //					// Ensure alpha is within bounds
    //					if (currentAlpha<0.0f)
    //						currentAlpha=0.0f;
    //					if (currentAlpha>1.0f)
    //						currentAlpha=1.0f;
    //
    //					g2d.setComposite(AlphaComposite.getInstance(
    //						AlphaComposite.SRC_OVER,currentAlpha));
    //				}
    //
    //				final int width=messageWidth+2*HORIZONTAL_PADDING;
    //				final int height=HEIGHT;
    //				final int lineHeight=g.getFontMetrics().getHeight();
    //				final int linePadding=g.getFontMetrics().getMaxDescent()+2;
    //
    //				// Determine the x coordinate.  The scrollbar may be showing,
    //				// so we want to avoid painting over it.  Note that this
    //				// arises because this class ultimately derives from 
    //				// JScrollPane.
    //				final int effectiveWidth=Math.min(
    //					getWidth(),getJTree().getWidth());
    //				final int left=(effectiveWidth-width)/2;
    //
    //				// Determine the y coordinate.  The scrollbar may be showing,
    //				// so we want to avoid painting over it.  Note that this
    //				// arises because this class ultimately derives from 
    //				// JScrollPane.
    //				final int effectiveHeight=Math.min(
    //					getHeight(),getJTree().getHeight());
    //				final int top=(effectiveHeight-height)+
    //					Math.round(height*(1.0f-percentShown));
    //
    //				// Draw the message box
    //				if (backgroundImage!=null)
    //				{
    //					final int XOFFSET=50;
    //					final int YOFFSET=90;
    //
    //					// Draw a nice watermark image
    //					int imageInset=lineHeight+linePadding;
    //					int imageHeight=height-imageInset;
    //
    //					// Draw a simple white background
    //					g.setColor(Color.white);
    //					g.fillRect(left,top+imageInset,width,imageHeight);
    //
    //					// Set transparency for message
    //					if (g2d!=null)
    //					{
    //						g2d.setComposite(AlphaComposite.getInstance(
    //							AlphaComposite.SRC_OVER,0.7f*currentAlpha));
    //					}
    //
    //					g.drawImage(backgroundImage,
    //						left,top+imageInset,left+width,top+height,
    //						XOFFSET,YOFFSET,XOFFSET+width,YOFFSET+imageHeight,
    //						Color.white,null);
    //
    //					// Set transparency for message
    //					if (g2d!=null)
    //					{
    //						g2d.setComposite(AlphaComposite.getInstance(
    //							AlphaComposite.SRC_OVER,currentAlpha));
    //					}
    //				}
    //				else
    //				{
    //					// Draw a simple white background
    //					g.setColor(Color.white);
    //					g.fillRect(left,top,width,height);
    //				}
    //
    //				// Draw the outline border
    //				g.setColor(BORDER_COLOR);
    //				g.drawRect(left,top,width,height);
    //
    //				if (g2d!=null && currentAlpha==MAX_ALPHA)
    //				{
    //					g2d.setComposite(AlphaComposite.getInstance(
    //						AlphaComposite.SRC_OVER,1.0f));
    //				}
    //
    //				// Draw the XP tab highlight if using XP L&F
    //				if (isXPTheme())
    //				{
    //					// Body of highlight
    //					g.setColor(HIGHLIGHT_COLOR);
    //					g.drawLine(left,top,left+width,top);
    //					g.drawLine(left+2,top-1,left+width-2,top-1);
    //
    //					// Top highlighted edge
    //					g.setColor(HIGHLIGHT_COLOR_2);
    //					g.drawLine(left+2,top-2,left+width-2,top-2); // line
    //
    //					// Endcaps
    //					g.setColor(HIGHLIGHT_COLOR_2);
    //					g.drawLine(left,top,left,top); // left dot
    //					g.drawLine(left+1,top-1,left+1,top-1); // left dot
    //					g.drawLine(left+width,top,left+width,top); // right dot
    //					g.drawLine(left+width-1,top-1,left+width-1,top-1); // right
    //				}
    //
    //				// Draw header
    //				g.setColor(Color.white);
    //				g.fillRect(left+1,top+1,width-2,lineHeight+linePadding);
    //
    //				// Draw the account name
    //				int lineX=left+width/2-
    //					g.getFontMetrics().stringWidth(message[0])/2;
    //				int lineY=top+lineHeight;
    //				g.setColor(Color.black);
    //				g.drawString(message[0],lineX,lineY);
    //
    //				// Draw the icon
    //				if (icon!=null)
    //				{
    //					final int ICON_WIDTH=16;
    //					g.drawImage(icon,left+2,
    //						top+(lineY+linePadding-top)/2-ICON_WIDTH/2,
    //						Color.white,null);
    //				}
    //
    //				if (g2d!=null)
    //				{
    //					g2d.setComposite(AlphaComposite.getInstance(
    //						AlphaComposite.SRC_OVER,currentAlpha));
    //				}
    //
    //				// Draw the separator
    //				g.setColor(BORDER_COLOR);
    //				lineY+=linePadding;
    //				g.drawLine(left,lineY,left+width,lineY);
    //				
    //				// Draw the rest of the message
    //				lineX=left+width/2-g.getFontMetrics().stringWidth(message[1])/2;
    //				lineY+=(height-(lineY-top))/2-linePadding;
    //				g.setColor(Color.black);
    //				g.drawString(message[1],lineX,lineY);
    //
    //				// Draw the rest of the message, next line
    //				lineX=left+width/2-g.getFontMetrics().stringWidth(message[2])/2;
    //				lineY+=lineHeight;
    //				g.setColor(Color.black);
    //				g.drawString(message[2],lineX,lineY);
    //			}
    //			catch (Exception e)
    //			{
    //				// Ignore
    //				Debug.debugNotify(e);
    //			}
    //		}
    //
    //		private final int WIDTH=150;
    //		private final int HEIGHT=125;
    //		private final int HORIZONTAL_PADDING=20; // 16+2+2, icon + 2 padding
    //		private final float MAX_ALPHA=0.85f;
    //		private final int FADE_IN_TIME=1000; // in milliseconds
    //		private final int FADE_OUT_TIME=2000; // in milliseconds
    //		private final int DELAY_TIME=4000; // in milliseconds
    //		private final Color BORDER_COLOR=
    //			UIManager.getColor("standard_border")!=null ?
    //			UIManager.getColor("standard_border") :
    //			new Color(127,157,185);
    //		private final Color HIGHLIGHT_COLOR=
    //			UIManager.getColor("tab_highlight_header_fill")!=null ?
    //			UIManager.getColor("tab_highlight_header_fill") :
    //			new Color(255,199,60);
    //		private final Color HIGHLIGHT_COLOR_2=
    //			UIManager.getColor("tab_highlight_header")!=null ?
    //			UIManager.getColor("tab_highlight_header") :
    //			new Color(230,139,44);
    //
    //		private CollabPrincipal contact;
    //		private int notification;
    //		private boolean initialized;
    //		private float currentAlpha;
    //		private String[] message;
    //		private int messageWidth;
    //		private long startTime;
    ////		private boolean played=false;
    //	}
    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    //	private Notification notification;
    //	private java.util.List notifications=new LinkedList();
    //	private Object LOCK=new Object();
    //	private Thread notificationThread;
    //	private Image icon;
    //	private Image backgroundImage;
    //	private AudioClip pop;
}
