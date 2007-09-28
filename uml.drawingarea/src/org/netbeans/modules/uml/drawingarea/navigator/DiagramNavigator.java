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

/*
 * DiagramNavigator.java
 *
 * Created on December 6, 2005, 8:57 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.drawingarea.navigator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.uml.drawingarea.DiagramTopComponent;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
//import com.tomsawyer.editor.overview.TSEOverviewComponent;
import javax.swing.JComponent;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author TreySpiva
 */
public class DiagramNavigator implements NavigatorPanel, PropertyChangeListener
{
   private javax.swing.JLabel component = null;
////   private TSEOverviewComponent overview = null;
//   private IDrawingAreaControl drawingArea = null;
//   private JComponent navigatorView = null;
   private DiagramNavigatorPanel navigator = null;
   
   /** Creates a new instance of DiagramNavigator */
   public DiagramNavigator()
   {
   }

   public String getDisplayName()
   {
      return NbBundle.getMessage(DiagramNavigator.class, 
                                 "Navigator_DisplayName");
   }

   public String getDisplayHint()
   {
      return NbBundle.getMessage(DiagramNavigator.class, "Navigator_Hint");
   }

   public JComponent getComponent()
   {
      DiagramNavigatorPanel panel = getNavigatorPanel();
      panel.refresh();
      return panel;
   }

   public void panelActivated(Lookup lookup)
   {      
      IUIDiagram diagram = (IUIDiagram)lookup.lookup(IUIDiagram.class);
      if(diagram != null)
      {
          IDrawingAreaControl ctrl = diagram.getDrawingArea();
          if(ctrl != null)
          {         
             DiagramNavigatorPanel panel = getNavigatorPanel();
             panel.setDrawingArea(ctrl);
             TopComponent.getRegistry().addPropertyChangeListener(this);
          }
      }
   }

   public void panelDeactivated()
   {
       //Jyothi: remove navigator listeners..
       TopComponent.getRegistry().removePropertyChangeListener(this);
   }

   public Lookup getLookup()
   {
      return null;
   }
   
   protected DiagramNavigatorPanel getNavigatorPanel()
   {
      if(navigator == null)
      {
         navigator = new DiagramNavigatorPanel();
      }
      return navigator;
   }
   
   public void propertyChange(PropertyChangeEvent evt) {
       String property = evt.getPropertyName();
       if(property.equals(TopComponent.Registry.PROP_ACTIVATED)) {
           DiagramNavigatorPanel panel = getNavigatorPanel();
           TopComponent view = (TopComponent) evt.getNewValue();
           Object ctrl = view.getLookup().lookup(IDrawingAreaControl.class);
           if(ctrl instanceof IDrawingAreaControl) {
               panel.setDrawingArea((IDrawingAreaControl)ctrl);
               panel.refresh();
           }
       }
   }
}
