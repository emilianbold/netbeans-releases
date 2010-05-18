/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
