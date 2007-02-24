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


package org.netbeans.modules.uml.ui.products.ad.ADDrawEngines;

import java.awt.Point;

import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETTransform;

/**
 * @author brettb
 *
 *  The shape is defined inside a canonical box,
 *  which has size 100 x 100, with its lower-left corner at the location (0, 0).
 */
public class ConvertRectToPercent
{

   /**
    *
    */
   public ConvertRectToPercent( final ETTransform transform )
   {
      super();

      IETRect rectWinScaledOwner = transform.getWinScaledOwnerRect();
      if( (rectWinScaledOwner != null) &&
          !rectWinScaledOwner.isZero() )
      {
         m_iTotalHeight = rectWinScaledOwner.getIntHeight();
         
         // Set up the coordinate conversion parameters
         m_dWidthMult  = 100.0 / rectWinScaledOwner.getWidth();
         m_dHeightMult = 100.0 / rectWinScaledOwner.getHeight();
      }
   }

   public Point ConvertToPercent( final Point point )
   {
      int x = (int)Math.round(point.x * m_dWidthMult);
      int y = (int)Math.round((m_iTotalHeight - point.y) * m_dHeightMult);
      
      return new Point( x, y );
   }
   
   
   private int    m_iTotalHeight = 0;
   private double m_dHeightMult = 0;
   private double m_dWidthMult = 0;

}
