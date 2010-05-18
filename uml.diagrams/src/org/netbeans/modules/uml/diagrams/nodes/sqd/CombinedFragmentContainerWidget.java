/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.uml.diagrams.nodes.sqd;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.util.Set;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.drawingarea.actions.SceneAcceptProvider;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.MoveWidgetTransferable;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;

/**
 *
 * @author sp153251
 */
public class CombinedFragmentContainerWidget extends ContainerWidget {
    public CombinedFragmentContainerWidget(Scene scene)
    {
        super(scene);
    }
    
//    /**
//     * combined fragment may contain messages, but not directly
//     * all nmessages belong to operands
//     * also may contain coveredLifelines
//     * other element containment like comments isn't handled
//     */
//    @Override
//    public void calculateChildren(boolean processChildren)
//    {
//        if(getCFElement()!=null)
//        {
//            GraphScene scene = (GraphScene) getScene();
//            Rectangle mySceneBounds = convertLocalToScene(getBounds());
//
//            boolean changed = false;
//            if(processChildren == true)
//            {
//                changed = removeChildrenOutsideBounds(scene, mySceneBounds);
//            }
//
//            if(addChildrenInsideBounds(scene, mySceneBounds) == true)
//            {
//                changed = true;
//            }
//
//            if(changed == true)
//            {
//                firePropertyChange(CHILDREN_CHANGED, null, null);
//            }
//        }
//        else
//        {
//            super.calculateChildren(processChildren);
//        }
//    }
//    public ICombinedFragment getCFElement()
//    {
//        ICombinedFragment retVal = null;
//        
//        ObjectScene scene = (ObjectScene) getScene();
//        IPresentationElement presentation = (IPresentationElement) scene.findObject(this);
//
//        if(presentation != null)
//        {
//            IElement element = presentation.getFirstSubject();
//            if(element instanceof ICombinedFragment)
//            {
//                retVal = (ICombinedFragment)element;
//            }
//        }
//        
//        return retVal;
//    }
//
//    @Override
//    protected void initActions()
//    {
//        if(getCFElement()!=null)//not necessary child elements need cf action, so this container is compartible with default for all except cf
//        {
//            createActions(DesignerTools.SELECT).addAction(ActionFactory.createAcceptAction(new ContainerAcceptProvider()) );
//        }
//        else 
//        {
//            super.initActions();
//        }
//    }
//    
//    
//    private void addChildNode(INamespace namespace, Object nodeData, Widget node)
//    {
//        Widget parent = node.getParentWidget();
//        Point sceneLocation = node.getPreferredLocation();
//        if(parent != null)
//        {
//            sceneLocation = parent.convertLocalToScene(node.getLocation());
//            parent.removeChild(node);
//        }
//        
//        addChild(node);
//        node.setPreferredLocation(convertSceneToLocal(sceneLocation));
//
//        INamedElement element = (INamedElement) ((IPresentationElement)nodeData).getFirstSubject();
//        if(namespace!=null)namespace.addOwnedElement(element);//combined fragment isn't a namespace but support graphical containment
//        //TBD is it necessary to add element to an interaction?
//    }
//
//    @Override
//    protected boolean addChildrenInsideBounds(GraphScene scene, 
//                                            Rectangle mySceneBounds)
//    {
//        boolean changed = false;
//
//        INamespace namespace = getContainerNamespace();
//        
//        // Second see if any nodes need to be added to the container.
//        for (Object nodeData : scene.getNodes())
//        {
//            Widget node = scene.findWidget(nodeData);
//            if (node != null)
//            {
//                Rectangle sceneBounds = node.convertLocalToScene(node.getBounds());
//                if (mySceneBounds.contains(sceneBounds) == true)
//                {
//                    // If a node is alreay contained by a container, the entire
//                    // container needs to be added, not the child node.
//                    Widget parent = node.getParentWidget();
//                    if (!(parent instanceof ContainerWidget))
//                    {
//                        addChildNode(namespace, nodeData, node);
//                        changed = true;
//                    }
//                }
//            }
//        }
//
//        return changed;
//    }
//    
//    public class ContainerAcceptProvider extends SceneAcceptProvider
//    {
//        public ContainerAcceptProvider()
//        {
//            super(null);
//        }
//
//        @Override
//        public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable)
//        {
//            ConnectorState retVal = super.isAcceptable(widget, point, transferable);
//            
//            if(retVal != ConnectorState.ACCEPT)
//            {
//                if(isWidgetMove(transferable) == true)
//                {
//                    retVal = ConnectorState.ACCEPT;
//                }
//            }
//            
//            return retVal;
//        }
//
//        
//        @Override
//        public void accept(Widget widget, Point point, Transferable transferable)
//        {
//            super.accept(widget, point, transferable);
//
//            Widget[] target = null;
//            ObjectScene scene = (ObjectScene) widget.getScene();
//            boolean convertLocation = false;
//            
//            if(isWidgetMove(transferable) == true)
//            {
//                try
//                {
//                    MoveWidgetTransferable data = 
//                            (MoveWidgetTransferable) transferable.getTransferData(MoveWidgetTransferable.FLAVOR);
//                    target = new Widget[] { data.getWidget() };
//                    convertLocation = true;
//                }
//                catch(Exception e)
//                {
//                    target = new Widget[0];
//                }
//            }
//            else
//            {
//                // Now the new Nodes should be selected.  So get the widgets and 
//                // add them to the container.
//                    
//                target = new Widget[scene.getSelectedObjects().size()];
//                Set <Object> selected = (Set<Object>) scene.getSelectedObjects();
//                Object[] selectedArray = new Object[selected.size()];
//                selected.toArray(selectedArray);
//
//                for(int i = 0; i < selected.size(); i++)
//                {
//                    Object curObj = selectedArray[i];
//                    Widget curWidget = scene.findWidget(curObj);
//                    target[i] = curWidget;
//                }
//            }
//            
//            for(Widget curWidget : target)
//            {
//                if(curWidget.getParentWidget() != null)
//                {
//                    curWidget.getParentWidget().removeChild(curWidget);
//                }
//                
//                Point curPt = curWidget.getLocation();
//                addChild(curWidget);
//                
//                if(convertLocation == true)
//                {
//                    curWidget.setPreferredLocation(convertSceneToLocal(curPt));
//                }
//                
//                Object data = scene.findObject(curWidget);
//                INamedElement element = (INamedElement) ((IPresentationElement)data).getFirstSubject();
//                INamespace ns=getContainerNamespace();
//                if(ns!=null)ns.addOwnedElement(element);//some elements(like combined fragment) are not namespace but can contain other element graphically
//                //TBD find out if it is necessary to handle addition to interactions etc
//            }
//            
//            if(target.length > 0)
//            {
//                firePropertyChange(CHILDREN_CHANGED, null, null);
//            }
//            
//            revalidate();
//
//        }
//        
//        @Override
//        protected INamespace getNamespace()
//        {
//            return getContainerNamespace();
//        }
//        
//        protected boolean isWidgetMove(Transferable transferable)
//        {
//            return transferable.isDataFlavorSupported(MoveWidgetTransferable.FLAVOR);
//        }
//    }
}
