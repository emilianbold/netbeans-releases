/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.uml.diagrams.actions;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JMenu;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.nodes.activity.ControlNodeWidget;
import org.netbeans.modules.uml.diagrams.nodes.activity.DecisionNodeWidget;
import org.netbeans.modules.uml.drawingarea.actions.SceneNodeAction;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author jyothi
 */
public class ResizeToDimensionAction extends SceneNodeAction
{

    private DesignerScene scene;
    private JMenu popupMenu;
    private Node[] activatedNodes;
    private Rectangle currentBounds;

    @Override
    public Action createContextAwareInstance(Lookup actionContext)
    {        
        scene = actionContext.lookup(DesignerScene.class);
        return this;        
    }

    @Override
    protected void performAction(Node[] activatedNodes)
    {
        this.activatedNodes = activatedNodes;
        if (scene== null || activatedNodes == null)
            return;
        if(activatedNodes.length < 1)
            return;
        
        IPresentationElement[] pElt = getSelectedElements();
        if (pElt != null && pElt.length == 1) {

            Widget w = scene.findWidget(pElt[0]);
            if (w instanceof UMLNodeWidget) 
            {
                currentBounds = w.getPreferredBounds();
            }            
            //Display the dialog
            ResizeElementsPanel panel = new ResizeElementsPanel(currentBounds.width, currentBounds.height);
            DialogDescriptor descriptor = new DialogDescriptor(
                    panel, 
                    NbBundle.getMessage(ResizeToDimensionAction.class, "CTL_SET_DIMENSION"),
                    true, 
                    null);
            Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ResizeToDimensionAction.class, "CTL_SET_DIMENSION"));
            dialog.setVisible(true);
            if (descriptor.getValue() == DialogDescriptor.OK_OPTION) 
            {
                int height = panel.getResizeHeight();
                int width = panel.getResizeWidth();
                if (height > 5 && width > 5)
                {
                    w.setPreferredBounds(null);
                    w.setPreferredSize(new Dimension(width, height));
                    w.revalidate();
                }                
            }
            if (descriptor.getValue() == DialogDescriptor.CANCEL_OPTION) 
            {
                // don't do anything. Just exit
            }
        }
    }

    @Override
    protected boolean enable(org.openide.nodes.Node[] activatedNodes)
    {
        boolean retVal = false;

        if (super.enable(activatedNodes) == true) 
        {
            if (!(activatedNodes.length == 1)) 
            {
                retVal = false;
            }
            else 
            {
                Set selectedObjs = scene.getSelectedObjects();
                if (selectedObjs != null && selectedObjs.size() == 1) 
                {
                    Object obj = selectedObjs.toArray()[0];
                    if (obj != null && obj instanceof IPresentationElement) 
                    {
                        Widget widget = scene.findWidget((IPresentationElement) obj);
                        if (widget instanceof UMLNodeWidget)
                        {
                            retVal = true;
                            if (widget instanceof ControlNodeWidget) 
                            {
                                retVal = false;
                            }
                            //now an exception is Decision widget which is a controlnodewidget but still resizable
                            if (widget instanceof DecisionNodeWidget)
                            {
                                retVal = true;
                            }
                        }
                    }
                }
            }
        }
        return retVal;
    }

    @Override
    public String getName()
    {
        if (scene == null) {
            return "";
        }

        return NbBundle.getMessage(ResizeToDimensionAction.class, "CTL_SET_DIMENSION");
    }

    @Override
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }
    
    private IPresentationElement[] getSelectedElements()
    {
        Set<IPresentationElement> selected = (Set<IPresentationElement>) scene.getSelectedObjects();

        IPresentationElement[] elements = new IPresentationElement[selected.size()];
        selected.toArray(elements);
        return elements;
    }

}
