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


package org.netbeans.modules.uml.ui.products.ad.diagramengines;

import java.util.Hashtable;
import java.util.Iterator;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author brettb
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MapElementToDrawEngine extends Hashtable< IElement, IPresentationElement >
{
   protected final String m_strElementType;
   
   
   MapElementToDrawEngine( final String strElementType )
   {
      m_strElementType = strElementType;
   }

   /// Load the map with the draw engines, only if there are no elements
   void testInitialize( IDrawingAreaControl ctrl )
   {
      if( size() == 0 )
      {
         initialize( ctrl );
      }
   }

   /// Retrieve a draw engine by element
   IDrawEngine getDrawEngine( IElement element )
   {
      IDrawEngine engine = null;
      
      IPresentationElement pe = (IPresentationElement)get( element );
      if( pe != null )
      {
         engine = TypeConversions.getDrawEngine( pe );
      }
      
      return engine;
   }

   /// Load the map with the draw engines
   protected void initialize( IDrawingAreaControl ctrl )
   {
      clear();

      if( ctrl != null )
      {
         ETList< IPresentationElement > lifelinePEs = ctrl.getAllByType( m_strElementType );
         if( lifelinePEs != null )
         {
            for (Iterator iter = lifelinePEs.iterator(); iter.hasNext();)
            {
               IPresentationElement pe = (IPresentationElement)iter.next();

               IElement firstElement = pe.getFirstSubject();
               if( firstElement != null )
               {
                  put( firstElement, pe );
               }
            }
         }
      }
   }
}
