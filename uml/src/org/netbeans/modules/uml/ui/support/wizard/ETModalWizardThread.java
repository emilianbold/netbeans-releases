/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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



package org.netbeans.modules.uml.ui.support.wizard;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;


/**
 * @author KevinM
 *
 */
public abstract class ETModalWizardThread
{
   protected Thread m_pThread = null;
   protected Runnable m_pRunnable = null;
	protected boolean m_bLockFrameInput = true;
	
   /**
    * @param target
    */
   public ETModalWizardThread(Runnable target)
   {
      super();
      m_pRunnable = target;
   }

	/*
	 * Protected constructor this class is abstract, and you need a runnable to use the start method.
	 */
   protected ETModalWizardThread()
   {
      super();      
   }

   protected Thread getThread()
   {
      return m_pThread;
   }

   /*
    * 
    * @author KevinM
    *
    * This class watches for componenet to be moved and it repaints the 
    * parent frame windows, when the component is hidden it reenables the frame window so it will except input.
    */
   public class ProgreessMoveListener implements ComponentListener
   {
		public ProgreessMoveListener()
		{
			super();
		}
		
      /**
      	 * Invoked when the component's size changes.
      	 */
      public void componentResized(ComponentEvent e)
      {
			ETSystem.out.println("ETModalWizardThread componentResized");
			repaintFrame();
      }

      /**
      	* Invoked when the component's position changes.
      	*/
      public void componentMoved(ComponentEvent e)
      {
			ETSystem.out.println("ETModalWizardThread componentMoved");
			repaintFrame();
      }

      /**
      	* Invoked when the component has been made visible.
      	*/
      public void componentShown(ComponentEvent e)
      {
         ETSystem.out.println("ETModalWizardThread componentShown");
			repaintFrame();
      }

      /**
      	* Invoked when the component has been made invisible.
      	*/
      public void componentHidden(ComponentEvent e)
      {
			ETSystem.out.println("ETModalWizardThread componentHidden");
			
			if (m_bLockFrameInput && getOwnerFrame() != null)
			{
				getOwnerFrame().setEnabled(true);
				getOwnerFrame().setVisible(true);
				getOwnerFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
        
       	if (e != null)
         	e.getComponent().removeComponentListener(this);
      }
      
      /*
       * Redraws the frame window when the component moves or is resized to fix the Tracks.
       */
      protected void repaintFrame()
      {
			Frame frame = getOwnerFrame();
			 if (frame != null)
			 {
			 	// Fix the Tracks on the frame window.
				 frame.invalidate();
				 Thread currentThread = Thread.currentThread();
				 if (currentThread == ETModalWizardThread.this.getThread())
					 currentThread.yield();          
			 }      	
      }
   };

	/*
	 * Returns the main integration app frmae. 
	 */
   protected Frame getOwnerFrame()
   {
      IProxyUserInterface ui = ProductHelper.getProxyUserInterface();
      return ui != null ? ui.getWindowHandle() : null;
   }
   
	/*
	 * Start the thread and invoke the Runnable 
	 */
	public void start()
	{
		start(m_bLockFrameInput);
	}
	
	/*
	 * Strat the Runnable and disables the application frame until the thread is complete.
	 */
   public void start(boolean lockTheFrame)
   {  
	//	if (getRunnable() != null)
	//		getRunnable().run();
   	
		m_bLockFrameInput = lockTheFrame;
      if (getRunnable() != null)
      {
         Thread reportThread = new Thread(getRunnable());
			reportThread.setPriority(this.getDefaultPrioity());
			
			// disable the integration frame, so the dialog pumps msg's but it acts modal.
			if (m_bLockFrameInput && getOwnerFrame() != null)
			{
				getOwnerFrame().setEnabled(false); 
			}
				
         reportThread.start();
         Component pWizardComponent = getComponent();

         if (pWizardComponent != null)
         {
            pWizardComponent.addComponentListener(new ProgreessMoveListener());
         }
         else if (getOwnerFrame() != null)
         {
         	// Make sure the frame isn't locked up.
				getOwnerFrame().setEnabled(true);
				getOwnerFrame().setVisible(true);        
         }
      }
      
   }

	protected int getDefaultPrioity()
	{
		return java.lang.Thread.NORM_PRIORITY; 		
	}
	
   /*
    * Return your progress bar component.
    */
   protected abstract Component getComponent();
   
   /*
    * Retuns the action interface to invoke from the the start method.
    */
   protected Runnable getRunnable()
   {
      return m_pRunnable;
   }
}
