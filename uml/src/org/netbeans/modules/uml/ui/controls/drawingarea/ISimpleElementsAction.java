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


package org.netbeans.modules.uml.ui.controls.drawingarea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author brettb
 *
 */
public interface ISimpleElementsAction extends IDelayedAction, IExecutableAction
{
   public static class SEAK
   {
      // Handles the deep sync broadcast action, which can happen while presentation elements are being created...a bad spot
      public final static int DEEPSYNC_BROADCAST =                0;
      // Handles the deep sync broadcast action, it also resizes them to contents.
      public final static int DEEPSYNC_AND_RESIZE_BROADCAST =     DEEPSYNC_BROADCAST + 1;
      // Performs relationship discovery on these elements
      public final static int DISCOVER_RELATIONSHIPS =            DEEPSYNC_AND_RESIZE_BROADCAST + 1;
      // Reconnects presentation elements to their model elements
      public final static int RECONNECT_PRESENTATION_ELEMENTS =   DISCOVER_RELATIONSHIPS + 1;
      // When processed the diagram will CDFS these elements.
      public final static int DELAYED_CDFS =                      RECONNECT_PRESENTATION_ELEMENTS + 1;

      public static String getDescription( int kind )
      {
         String strDescription;
         
         switch (kind)
         {
         case DEEPSYNC_BROADCAST :
            strDescription = "SEAK_DEEPSYNC_BROADCAST";
            break;
         case DEEPSYNC_AND_RESIZE_BROADCAST :
            strDescription = "SEAK_DEEPSYNC_AND_RESIZE_BROADCAST";
            break;
         case DISCOVER_RELATIONSHIPS :
            strDescription = "SEAK_DISCOVER_RELATIONSHIPS";
            break;
         case RECONNECT_PRESENTATION_ELEMENTS :
            strDescription = "SEAK_RECONNECT_PRESENTATION_ELEMENTS";
            break;
         case DELAYED_CDFS :
            strDescription = "SEAK_DELAYED_CDFS";
            break;
         default :
            strDescription = "Unknown";
            assert( false );
            break;
         }
         
         return strDescription;
      }
   }
   
   /**
    * The kind of the action
   */
   public int getKind();

   /**
    * The kind of the action
   */
   public void setKind( /* _SimpleElementsActionKind */ int value );

   /*
    * The elements to be acted upon
    */
   public ETList< IElement > getElements();
   
   /*
    * The elements to be acted upon
    */
   public void setElements( ETList< IElement > newVal );

   /*
    * Adds an Element to this action
    */
   public void add( IElement newVal );
}
