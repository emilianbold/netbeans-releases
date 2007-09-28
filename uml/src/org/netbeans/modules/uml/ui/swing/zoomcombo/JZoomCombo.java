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
 * JZoomCombo.java
 *
 * Created on May 19, 2004, 1:43 PM
 */

package org.netbeans.modules.uml.ui.swing.zoomcombo;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JComboBox;

/**
 *
 * @author  Trey Spiva
 */
public class JZoomCombo extends JComboBox implements ActionListener
{
   private boolean m_PlugUpdate = false;
   private ResourceBundle m_Bundle = ResourceBundle.getBundle("org/netbeans/modules/uml/ui/swing/zoomcombo/Bundle");
   
   /** Creates a new instance of JZoomCombo */
   public JZoomCombo()
   {
      this.addItem(m_Bundle.getString("ZOOM.FOUR_HUNDRED"));
      this.addItem(m_Bundle.getString("ZOOM.TWO_HUNDRED"));
      this.addItem(m_Bundle.getString("ZOOM.ONE_HUNDRED"));
      this.addItem(m_Bundle.getString("ZOOM.SEVENTYFIVE"));
      this.addItem(m_Bundle.getString("ZOOM.FIFTY"));
      this.addItem(m_Bundle.getString("ZOOM.TWENTYFIVE"));
      this.addItem(m_Bundle.getString("DIAGRAM.ZOOM_TO_FIT"));
      
      setSelectedIndex(2);
      setEditable(true);
      addActionListener(this);
   }
   
   public synchronized void blockUpdate(boolean value)
   {
      m_PlugUpdate = value;
   }
   
   public void actionPerformed(ActionEvent e)
   {
      if (m_PlugUpdate == false)
      {
         String selectedValue = (String) getSelectedItem();
         String cbVal = selectedValue.trim();
         
         String cmd = e.getActionCommand();
         if(cmd.equals("comboBoxChanged") == false)
         {
            cbVal = cmd;
         }
         
         IProductDiagramManager manager = ProductHelper.getProductDiagramManager();
         if(manager != null)
         {
            IDiagram curDiagram = manager.getCurrentDiagram();
            if(curDiagram != null)
            {
               if(cbVal.equals(m_Bundle.getString("DIAGRAM.ZOOM_TO_FIT")) == true)
               {
                  curDiagram.fitInWindow();
               }
					else if (cbVal.equals(m_Bundle.getString("ZOOM.FOUR_HUNDRED")))
					{
						cbVal = "400%";
					}
					else if (cbVal.equals(m_Bundle.getString("ZOOM.TWO_HUNDRED")))
					{
						cbVal = "200%";
					}
					else if (cbVal.equals(m_Bundle.getString("ZOOM.ONE_HUNDRED")))
					{
						cbVal = "100%";
					}
					else if (cbVal.equals(m_Bundle.getString("ZOOM.SEVENTYFIVE")))
					{
						cbVal = "75%";
					}
					else if (cbVal.equals(m_Bundle.getString("ZOOM.FIFTY")))
					{
						cbVal = "50%";
					}
					else if (cbVal.equals(m_Bundle.getString("ZOOM.TWENTYFIVE")))
					{
						cbVal = "25%";
					}
               
               // remove the precent sign before converting the string to a double.
               if(cbVal.endsWith("%") == true)
               {
                  cbVal = cbVal.substring(0, cbVal.length() - 1);
               }
               
               try
               {
                  Double zoomVal = new Double(cbVal);
                  double zoom =  zoomVal.doubleValue() / 100;
                  curDiagram.zoom(zoom);
               }
               catch (NumberFormatException nfe)
               {
                  Log.out("zoom value entered is not a number");
               }
            }
         }
      }
   }
}
