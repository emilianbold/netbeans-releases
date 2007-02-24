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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;

/**
 *
 * @author Trey Spiva
 */
public class StretchContext implements IStretchContext, StretchContextType
{
   private int          mType           = SCT_START;
   private IETRect      mRestrictedArea = null;
   private IETPoint     mStartingPoint  = null;
   private IETPoint     mFinishingPoint = null;
   private IETSize      mSizeStretched  = null;
   private ICompartment mCompartment    = null;
   private boolean      m_SizeNeedsToBeCalculated = true;
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext#getType()
    */
   public int getType()
   {
      return mType;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext#setType(int)
    */
   public void setType(int value)
   {
      mType = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext#getRestrictedArea()
    */
   public IETRect getRestrictedArea()
   {
      return mRestrictedArea;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext#setRestrictedArea(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
    */
   public void setRestrictedArea(IETRect value)
   {
      mRestrictedArea = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext#getStartPoint()
    */
   public IETPoint getStartPoint()
   {
      return mStartingPoint;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext#setStartPoint(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
    */
   public void setStartPoint(IETPoint value)
   {
      mStartingPoint = value;
      m_SizeNeedsToBeCalculated = true;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext#getFinishPoint()
    */
   public IETPoint getFinishPoint()
   {
      return mFinishingPoint;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext#setFinishPoint(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
    */
   public void setFinishPoint(IETPoint value)
   {
      mFinishingPoint = value;
      m_SizeNeedsToBeCalculated = true;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext#getStretchSize()
    */
   public IETSize getStretchSize()
   {
      if( m_SizeNeedsToBeCalculated == true )
      {
         int width = mFinishingPoint.getX() - mStartingPoint.getX();
         int height = mFinishingPoint.getY() - mStartingPoint.getY();
         
         mSizeStretched = new ETSize(width, height);
         m_SizeNeedsToBeCalculated = false;
      }
   
      return mSizeStretched;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext#getCompartment()
    */
   public ICompartment getCompartment()
   {
      return mCompartment;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext#setCompartment(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment)
    */
   public void setCompartment(ICompartment value)
   {
      mCompartment = value;
   }
}
