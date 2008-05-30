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



package org.netbeans.modules.uml.ui.swing.propertyeditor;

//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author KevinM
 *
 */
public class PropertyEditorEventBlocker implements IPropertyEditorEventBlocker
{
   protected PropertyEditorBlocker m_blocker = null;
//TODO
//   public PropertyEditorEventBlocker(IDrawingAreaControl drawingArea)
//   {
//      //m_blocker = new PropertyEditorBlocker();
////      disableEvents();
//
//   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditorEventBlocker#disableEvents()
    */
   public boolean disableEvents()
   {
      PropertyEditorBlocker.block();
      return true;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditorEventBlocker#enableEvents()
    */
   public boolean enableEvents()
   {
      PropertyEditorBlocker.unblock();
      return true;
   }

}
