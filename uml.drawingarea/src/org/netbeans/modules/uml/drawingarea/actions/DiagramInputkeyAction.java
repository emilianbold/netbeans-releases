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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Set;
import javax.swing.JToggleButton;
import org.netbeans.modules.uml.drawingarea.UMLDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.keymap.DiagramKeyMapConstants;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteManager;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.spi.palette.PaletteController;
import org.openide.awt.Toolbar;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
/**
 *
 * @author thuy
 */
public class DiagramInputkeyAction extends javax.swing.AbstractAction 
{
    private TopComponent component;
    public DiagramInputkeyAction(TopComponent component, String actionName)
    {
        this.component = component;
        putValue(NAME, actionName);
    }

    public void actionPerformed(ActionEvent event)     
    {          
        String command = (String) getValue(NAME);
        if (DiagramKeyMapConstants.CANCEL_ACTION.equals(command))
        {
            onCancelAction();
        }
    }

    private void onCancelAction()
    {
        if (component instanceof UMLDiagramTopComponent)
        {
            UMLDiagramTopComponent umlTopComp = (UMLDiagramTopComponent)component;
            DesignerScene scene = umlTopComp.getScene();
            
            // cancel all selected objects on scene if any
            Set<Object> selectedObjs = (Set<Object>) scene.getSelectedObjects();
            if ( selectedObjs != null && selectedObjs.size() > 0)
            {
                scene.setSelectedObjects(Collections.EMPTY_SET);
                scene.clearLockedSelected();
                
                // cancel context palette
                ContextPaletteManager contextManager = scene.getContextPaletteManager();
                if(contextManager != null)
                {
                    contextManager.cancelPalette();
                }
            }
            
            // cancel palette selection if any
            PaletteController controller = umlTopComp.getLookup().lookup(PaletteController.class);
            if (controller != null) 
            { 
                Lookup paletteSelection  = controller.getSelectedItem();
                if (paletteSelection != null && paletteSelection != Lookup.EMPTY )
                {
                    controller.setSelectedItem(Lookup.EMPTY, Lookup.EMPTY);
                    scene.removeBackgroundWidget();
                }
            }
            
            // cancel toolbar's JToggleButton selection &&
            // select the default button toolbar "Select"
            Toolbar editorToolbar = umlTopComp.getLookup().lookup(Toolbar.class);
            if (editorToolbar != null)
            {
                Component[] toolbarComps = editorToolbar.getComponents();
                for (Component comp : toolbarComps) 
                {
                    if (comp instanceof JToggleButton)
                    {
                        JToggleButton jtButton = (JToggleButton)comp;
                        if (DesignerTools.SELECT.equals(jtButton.getName()) && !jtButton.isSelected())
                        {
                             jtButton.setSelected(true);
                             
                             String tool = DesignerTools.SELECT;
                             if(scene.isReadOnly() == true)
                             {
                                 tool = DesignerTools.READ_ONLY;
                             }
                             
                             scene.setActiveTool(tool);
                             scene.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        } else
                        {
                            jtButton.setSelected( false);
                        }
                    }
                }
            }
        }
    }  // end onCancelAction
    
}

