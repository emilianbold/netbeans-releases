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
