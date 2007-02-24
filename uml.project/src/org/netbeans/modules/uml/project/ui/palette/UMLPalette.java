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

package org.netbeans.modules.uml.project.ui.palette;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;

import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public class UMLPalette extends TopComponent
{
   private static UMLPalette m_TopComponent = null;
   private PaletteComponent m_Control = null;

   public UMLPalette()
   {
      super();
      initComponents();
   }

   public UMLPalette(Lookup lookup)
   {
      super(lookup);
      initComponents();
   }
   
   public void initComponents()
   {
      setName("UMLPalette");
      setDisplayName("UMLPalette");
      
      setLayout(new BorderLayout());
      
      m_Control = new PaletteComponent();
      
      add(m_Control, BorderLayout.CENTER);
      
      setPreferredSize(new Dimension(200,70));
   }
   
   public static synchronized UMLPalette getDefault()
   {
      if (m_TopComponent == null)
      {
         m_TopComponent = new UMLPalette();
      }
      return m_TopComponent;
   }
   
   public static synchronized UMLPalette getInstance()
   {
      if(m_TopComponent == null)
      {
         TopComponent tc = WindowManager.getDefault().findTopComponent("UMLPalette");
         if (tc != null)
         {
            m_TopComponent = (UMLPalette)tc;
         }
         else
         {
            m_TopComponent = new UMLPalette();      
         }
      }
      
      return m_TopComponent;
   }
   
   /** Overriden to explicitely set persistence type of UmlPalette
    * to PERSISTENCE_ALWAYS */
   public int getPersistenceType() {
       return TopComponent.PERSISTENCE_ALWAYS;
   }
   
   public void addNotify()
   {
      super.addNotify();
   }
   
   public void removeNotify()
   {
      super.removeNotify();
   }

}
