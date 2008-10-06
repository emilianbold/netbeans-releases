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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.actions.SceneNodeAction;
import org.netbeans.modules.uml.drawingarea.view.CollapsibleWidgetManager;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author jyothi
 */
public class ShowHideListCompartmentAction extends SceneNodeAction
{

    private DesignerScene scene;
    private JMenu popupMenu;
    private Node[] activatedNodes;
    public static final int ATTRIBUTES_COMPARTMENT = 0;
    public static final int OPERATIONS_COMPARTMENT = 1;
    public static final int REDEFINED_ATTR_COMPARTMENT = 2;
    public static final int REDEFINED_OPER_COMPARTMENT = 3;
    public static final int LITERALS_COMPARTMENT = 4;
    public static final int EXPAND_ALL = 5;
    public static final int COLLAPSE_ALL = 6;
    
    
    
    @Override
    public Action createContextAwareInstance(Lookup actionContext)
    {
        scene = actionContext.lookup(DesignerScene.class);        
        return this;
    }

    @Override
    protected void performAction(org.openide.nodes.Node[] activatedNodes)
    {
        this.activatedNodes = activatedNodes;
    }

    @Override
    protected boolean enable(org.openide.nodes.Node[] activatedNodes)
    {
        boolean retVal = false;
        
        if(super.enable(activatedNodes) == true)
        {
            retVal = activatedNodes.length >= 1;
        }
        
        return retVal;
    }

    @Override
    public String getName()
    {
        if (scene == null)
        {
            return "";
        }

        return NbBundle.getMessage(ShowHideListCompartmentAction.class, "CTL_SHOW_HIDE_LIST_COMPARTMENTS");
    }

    @Override
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public JMenuItem getPopupPresenter()
    {
        popupMenu = new JMenu(NbBundle.getMessage(ShowHideListCompartmentAction.class, "CTL_SHOW_HIDE_LIST_COMPARTMENTS")); // NOI18N
        popupMenu.setEnabled((scene != null) && (scene.isReadOnly() == false));

        ResourceBundle bundle = NbBundle.getBundle(ShowHideListCompartmentAction.class);
        JMenuItem attributeComp = new ShowHideMenuItem(bundle.getString("CTL_AttributesCompartment"), ATTRIBUTES_COMPARTMENT); // NOI18N
        JMenuItem operationComp = new ShowHideMenuItem(bundle.getString("CTL_OperationsCompartment"), OPERATIONS_COMPARTMENT); // NOI18N
        JMenuItem redefinedAttrComp = new ShowHideMenuItem(bundle.getString("CTL_RedefinedAttrCompartment"), REDEFINED_ATTR_COMPARTMENT); // NOI18N
        JMenuItem redefinedOperComp = new ShowHideMenuItem(bundle.getString("CTL_RedefinedOperCompartment"), REDEFINED_OPER_COMPARTMENT); // NOI18N
        JMenuItem literalsComp = new ShowHideMenuItem(bundle.getString("CTL_LiteralsCompartment"), LITERALS_COMPARTMENT); // NOI18N
        JMenuItem expandAll = new ShowHideMenuItem(bundle.getString("CTL_ExpandAllCompartments"), EXPAND_ALL); // NOI18N
        JMenuItem collapseAll = new ShowHideMenuItem(bundle.getString("CTL_CollapseAllCompartments"), COLLAPSE_ALL); // NOI18N
        
        popupMenu.add(expandAll);
        popupMenu.add(collapseAll);
        
        //show only those submenu items which have collapsible compartments
        for (IPresentationElement p : getSelectedElements())
        {
            Widget w = scene.findWidget(p);

            if (w instanceof UMLNodeWidget)
            {
                Collection<? extends CollapsibleWidgetManager> mgrList = w.getLookup().lookupAll(CollapsibleWidgetManager.class);
                for (CollapsibleWidgetManager mgr : mgrList)
                {
                    if (mgr != null  &&  mgr.getCollapsibleCompartmentWidget().isVisible())
                    {                        
                        String compName = mgr.getCollapsibleCompartmentName();
                        if (compName.equalsIgnoreCase(UMLNodeWidget.ATTRIBUTES_COMPARTMENT))
                        {
                            popupMenu.add(attributeComp);
                        }
                        if (compName.equalsIgnoreCase(UMLNodeWidget.OPERATIONS_COMPARTMENT))
                        {
                            popupMenu.add(operationComp);
                        }
                        if (compName.equalsIgnoreCase(UMLNodeWidget.REDEFINED_ATTR_COMPARTMENT))
                        {
                            popupMenu.add(redefinedAttrComp);
                        }
                        if (compName.equalsIgnoreCase(UMLNodeWidget.REDEFINED_OPER_COMPARTMENT))
                        {
                            popupMenu.add(redefinedOperComp);
                        }
                        if (compName.equalsIgnoreCase(UMLNodeWidget.LITERALS_COMPARTMENT))
                        {
                            popupMenu.add(literalsComp);
                        }
                        
                    }                    
                }
            }
        }
        
        for (int i = 0; i < popupMenu.getItemCount(); i++)
        {
            popupMenu.getItem(i).addActionListener(new ShowHideMenuItemListener());
            popupMenu.getItem(i).setEnabled(scene != null);
        }
        return popupMenu;
    }
    
    private void showHideCompartment(String compName)
    {
        IPresentationElement[] elementList;

        if ((getSelectedElements().length == 0) &&
                (compName.equalsIgnoreCase(UMLNodeWidget.EXPAND_ALL) || compName.equalsIgnoreCase(UMLNodeWidget.COLLAPSE_ALL)))
        {
            elementList = getAllNodes();
        } 
        else
        {
            elementList = getSelectedElements();
        }
        for (IPresentationElement p : elementList)
        {
            Widget w = scene.findWidget(p);

            if (w instanceof UMLNodeWidget)
            {
                Collection<? extends CollapsibleWidgetManager> mgrList = w.getLookup().lookupAll(CollapsibleWidgetManager.class);
                for (CollapsibleWidgetManager mgr : mgrList)
                {
                    if (mgr != null)
                    {
                        if (compName.equalsIgnoreCase(UMLNodeWidget.EXPAND_ALL) || compName.equalsIgnoreCase(UMLNodeWidget.COLLAPSE_ALL))
                        {
                            mgr.collapseWidget(compName);
                        } 
                        else if (mgr.getCollapsibleCompartmentName().equalsIgnoreCase(compName))
                        {
                            mgr.collapseWidget(compName);
                        } 
                        else
                        {
//                        System.out.println(" mgr compName = "+mgr.getCollapsibleCompartmentName());
                        }
                    }
                }
            }
        }
    }

    private IPresentationElement[] getSelectedElements()
    {
        Set<IPresentationElement> selected = (Set<IPresentationElement>) scene.getSelectedObjects();

        IPresentationElement[] elements = new IPresentationElement[selected.size()];
        selected.toArray(elements);
        return elements;
    }
    
    private IPresentationElement[] getAllNodes()
    {
        Set<IPresentationElement> nodesList = (Set<IPresentationElement>) scene.getNodes();

        IPresentationElement[] elements = new IPresentationElement[nodesList.size()];
        nodesList.toArray(elements);
        return elements;
    }

    private static class ShowHideMenuItem extends JMenuItem
    {

        int actionType;

        ShowHideMenuItem(String text, int action)
        {
            super(text);
            actionType = action;
        }

        int getActionType()
        {
            return actionType;
        }
    }

    private class ShowHideMenuItemListener implements ActionListener
    {

        public void actionPerformed(ActionEvent evt)
        {
            Object source = evt.getSource();
            if (!(source instanceof ShowHideMenuItem))
            {
                return;
            }
            ShowHideMenuItem mi = (ShowHideMenuItem) source;
            if (!mi.isEnabled())
            {
                return;
            }
            switch (mi.getActionType())
            {
                case EXPAND_ALL:
                    showHideCompartment(UMLNodeWidget.EXPAND_ALL);
                    break;
                case COLLAPSE_ALL:
                    showHideCompartment(UMLNodeWidget.COLLAPSE_ALL);
                    break;
                case ATTRIBUTES_COMPARTMENT:
                    showHideCompartment(UMLNodeWidget.ATTRIBUTES_COMPARTMENT);
                    break;
                case OPERATIONS_COMPARTMENT:
                     showHideCompartment(UMLNodeWidget.OPERATIONS_COMPARTMENT);
                    break;
                case REDEFINED_ATTR_COMPARTMENT:
                     showHideCompartment(UMLNodeWidget.REDEFINED_ATTR_COMPARTMENT);
                     break;
                case REDEFINED_OPER_COMPARTMENT:
                    showHideCompartment(UMLNodeWidget.REDEFINED_OPER_COMPARTMENT);
                    break;
                case LITERALS_COMPARTMENT:
                    showHideCompartment(UMLNodeWidget.LITERALS_COMPARTMENT);
                    break;
            }
            //mark the diagram dirty
            if (scene != null)
            {
                scene.getEngine().getTopComponent().setDiagramDirty(true);
            }
        }
    }

    
}

