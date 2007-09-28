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
