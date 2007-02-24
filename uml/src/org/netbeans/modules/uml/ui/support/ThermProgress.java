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



package org.netbeans.modules.uml.ui.support;

import java.awt.Component;

import javax.swing.ProgressMonitor;

import org.netbeans.modules.uml.ui.support.applicationmanager.IProgressCtrl;

/**
 * @author sumitabhk
 *
 *
 */
public class ThermProgress
{
	/// The progress ctrl
	private IProgressCtrl m_ProgressCtrl = null;

	/// The current message
	private String m_Message = "";

   private Component m_parent = null;
   private ProgressMonitor m_progressMonitor = null;

   /**
    *
    */
   public ThermProgress()
   {
      this( ProductHelper.getWindowHandle() );
   }
   
   /**
    * 
    */
   public ThermProgress( Component parent )
   {
      super();
      m_parent = parent;
   }

	/// Begins the progress control
   public void beginProgress( String message, int nLower, int nUpper, int nInitialPos )
   {
      // Fix J2739:  Disable the progress monitor, since it is not working as desired
      // UPDATE m_progressMonitor = new ProgressMonitor( m_parent, message, "", nLower, nUpper );
      setPos( nInitialPos );
   }
   
	/// Sets the current position - leaves the text the same. 
   public void setPos( int nPos )
   {
      if( m_progressMonitor != null )
      {
         m_progressMonitor.setProgress( nPos );
      }
   }

	/// Sets the current position and text 
   public void setPos( String message, int nPos )
   {
      if( m_progressMonitor != null )
      {
         m_progressMonitor.setNote( message );
         m_progressMonitor.setProgress( nPos );
      }
   }

   /// initializes and updates the progress control
   public void updateProgressControl( IProgressCtrl hInstance, int nID, int nCurPos, int nTotalPos )
   {
      // TODO
   }

   /// initializes and updates the progress control
   public void updateProgressControl( String sMessage, int nCurPos, int nTotalPos )
   {
      if( m_progressMonitor != null )
      {
         m_progressMonitor.setNote( sMessage );
         m_progressMonitor.setProgress( nCurPos );
         m_progressMonitor.setMaximum( nTotalPos );
      }
   }

   /// Ends the progress
   public void endProgress()
   {
      if( m_progressMonitor != null )
      {
         m_progressMonitor.close();
      }
   }
}


