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
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.drawingarea.UMLDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.keymap.DiagramInputkeyMapper;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.openide.awt.Toolbar;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl Su
 */
public class DiagramSelectToolAction extends AbstractAction
{

    private Scene scene;
    private Cursor cursor;
    private String tool;

    public DiagramSelectToolAction(Scene scene)
    {
        this(scene, DesignerTools.SELECT, ImageUtil.instance().getIcon("selection-arrow.png"), 
                NbBundle.getMessage(DiagramSelectToolAction.class, "LBL_SelectToolAction"),
                Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR),null, null);
    }

    public DiagramSelectToolAction(Scene scene, 
                                   String tool, 
                                   Icon icon, 
                                   String tooltip, 
                                   Cursor cursor, 
                                   KeyStroke accelerator,
                                   KeyStroke macAccelerator)
    {
        this.scene = scene;
        putValue(Action.SMALL_ICON, icon); 
        putValue(Action.SHORT_DESCRIPTION, tooltip);
        this.cursor = cursor; 
        this.tool = tool;
        
        putValue(Action.ACCELERATOR_KEY, accelerator);
        putValue(DiagramInputkeyMapper.MAC_ACCELERATOR, macAccelerator);
    }

    public void actionPerformed(ActionEvent evt)
    {
        Object eventSource = evt.getSource();
        IDiagram diagram = scene.getLookup().lookup(IDiagram.class);
        
        if (eventSource instanceof JToggleButton)
        {
            JToggleButton button = (JToggleButton) eventSource;
            if (button.isSelected())
            {
                // If the diagram is in read-only mode then we do not want to 
                // set the scene in select, but READ_ONLY mode instead.
                if((tool.equals(DesignerTools.SELECT) == true) && 
                    ((diagram != null) && diagram.getReadOnly() == true))
                {
                    tool = DesignerTools.READ_ONLY;
                }
                
                scene.setActiveTool(tool);
                scene.setCursor(cursor);
            } 
            else
            {
                // If the diagram is in read-only mode then we do not want to 
                // set the scene in select, but READ_ONLY mode instead.
                if((tool.equals(DesignerTools.SELECT) == true) && 
                    ((diagram != null) && diagram.getReadOnly() == true))
                {
                    scene.setActiveTool(DesignerTools.READ_ONLY);
                }
                else
                {
                    scene.setActiveTool(DesignerTools.SELECT);
                }
                
                scene.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
        // The action is invoked by key strokes
        else if (eventSource instanceof UMLDiagramTopComponent)
        {
            UMLDiagramTopComponent umlTopComp = (UMLDiagramTopComponent) eventSource;
            // If the diagram is in read-only mode then we do not want to 
            // set the scene in select, but READ_ONLY mode instead.
            if ((tool.equals(DesignerTools.SELECT) == true) &&
                    ((diagram != null) && diagram.getReadOnly() == true))
            {
                tool = DesignerTools.READ_ONLY;
            } else
            {
                JToggleButton toolButton = this.getToolButton(umlTopComp);
                toolButton.setSelected(true);
            }
            scene.setActiveTool(tool);
            scene.setCursor(cursor);
        }
    }
    
    private JToggleButton getToolButton(UMLDiagramTopComponent umlTopComp)
    {
        JToggleButton retTool = null;

        Toolbar editorToolbar = umlTopComp.getLookup().lookup(Toolbar.class);
        if (editorToolbar != null)
        {
            Component[] toolbarComps = editorToolbar.getComponents();
            for (Component comp : toolbarComps)
            {
                if (comp instanceof JToggleButton)
                {
                    JToggleButton jtButton = (JToggleButton) comp;
                    if (jtButton.getName().equals(this.tool))
                    {
                        retTool = jtButton;
                        break;
                    }
                }
            }
        }
        return retTool;
    }
}
