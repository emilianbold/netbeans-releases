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
package org.netbeans.modules.uml.drawingarea.widgets;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.drawingarea.actions.SceneAcceptProvider;
import org.netbeans.modules.uml.drawingarea.actions.WidgetAcceptAction;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.MoveWidgetTransferable;
import org.openide.util.Lookup;

/**
 *
 * @author Jyothi
 */
public class ContainerWidget extends Widget
{
    public final static String CHILDREN_CHANGED = "children-changed";
    private ArrayList < PropertyChangeListener > listeners 
            = new ArrayList < PropertyChangeListener >();
    
    public ContainerWidget(Scene scene)
    {
        super(scene);
        this.setOpaque(false);
        
        setLayout(LayoutFactory.createAbsoluteLayout());
        
        initActions();
    }
    
    public INamespace getContainerNamespace()
    {
        INamespace retVal=null;
        
        IElement element = getContainerElement();
        if(element instanceof INamespace)
        {
            retVal = (INamespace)element;
        }
        
        return retVal;
    }
    public IElement getContainerElement()
    {
        IElement retVal = null;
        
        ObjectScene scene = (ObjectScene) getScene();
        IPresentationElement presentation = (IPresentationElement) scene.findObject(this);

        if(presentation != null)
        {
            retVal = presentation.getFirstSubject();
        }
        
        return retVal;
    }

    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        listeners.add(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        listeners.remove(l);
    }
    
    public void firePropertyChange(String propName, Object before, Object after)
    {
        PropertyChangeEvent event = new PropertyChangeEvent(this, propName, before, after);
        for(PropertyChangeListener l : listeners)
        {
            l.propertyChange(event);
        }
    }
    
    public void calculateChildren(boolean processChildren)
    {
        GraphScene scene = (GraphScene) getScene();
        Rectangle mySceneBounds = convertLocalToScene(getBounds());
        
        boolean changed = false;
        if(processChildren == true)
        {
            changed = removeChildrenOutsideBounds(scene, mySceneBounds);
        }
        
        if(addChildrenInsideBounds(scene, mySceneBounds) == true)
        {
            changed = true;
        }
        
        if(changed == true)
        {
            firePropertyChange(CHILDREN_CHANGED, null, null);
        }
        
    }

    protected void initActions()
    {
        ContainerAcceptProvider provider = new ContainerAcceptProvider();
        WidgetAction acceptAction = ActionFactory.createAcceptAction(provider);
        
        createActions(DesignerTools.SELECT).addAction(acceptAction);
        createActions(DesignerTools.PALETTE).addAction(new WidgetAcceptAction(provider));
    }
    
    private boolean addChildNode(INamespace namespace, Object nodeData, Widget node)
    {
        Widget parent = node.getParentWidget();
        Point sceneLocation = node.getPreferredLocation();
        if(parent != null)
        {
            sceneLocation = parent.convertLocalToScene(node.getLocation());
            parent.removeChild(node);
        }
        
        addChild(node);
        node.setPreferredLocation(convertSceneToLocal(sceneLocation));

        INamedElement element = (INamedElement) ((IPresentationElement)nodeData).getFirstSubject();
        if(namespace!=null) namespace.addOwnedElement(element);//combined fragment isn't a namespace but support graphical containment
        //TBD is it necessary to add element to an interaction?
        return true;
    }

    private boolean addChildLifeline(ICombinedFragment namespace, Object nodeData, Widget node)
    {
        if(((IPresentationElement)nodeData).getFirstSubject() instanceof ILifeline)
        {
            Widget parent = node.getParentWidget();
            Point sceneLocation = node.getPreferredLocation();
            if(parent != null)
            {
                sceneLocation = parent.convertLocalToScene(node.getLocation());
                parent.removeChild(node);
            }

            addChild(node);
            node.setPreferredLocation(convertSceneToLocal(sceneLocation));

            ILifeline element = (ILifeline) ((IPresentationElement)nodeData).getFirstSubject();
            if(namespace!=null)namespace.addCoveredLifeline(element);//combined fragment isn't a namespace but support graphical containment
            //TBD is it necessary to add element to an interaction?
            return true;
        }
        else
        {
            return false;
        }
    }
    
    
    protected boolean addChildrenInsideBounds(GraphScene scene, 
                                            Rectangle mySceneBounds)
    {
        boolean changed = false;

        INamespace namespace = getContainerNamespace();
        IElement contElement= getContainerElement();
        
        // Second see if any nodes need to be added to the container.
        for (Object nodeData : scene.getNodes())
        {
            Widget node = scene.findWidget(nodeData);
            if (node != null)
            {
                Rectangle sceneBounds = node.convertLocalToScene(node.getBounds());
                if (mySceneBounds.contains(sceneBounds) == true)
                {
                    // If a node is alreay contained by a container, the entire
                    // container needs to be added, not the child node.
                    Widget parent = node.getParentWidget();
                    if (!(parent instanceof ContainerWidget))
                    {
                        if(namespace!=null)
                        {
                            //works for most elements
                            changed =addChildNode(namespace, nodeData, node);
                        }
                        else if(contElement instanceof ICombinedFragment) {
                            //combined fragment can contain lifelines for example (cover) and is not a namespace
                            changed =addChildLifeline((ICombinedFragment) contElement,nodeData, node);
                        }
                    }
                }
            }
        }

        return changed;
    }

    public boolean removeAllChildren(GraphScene scene)
    {
        return removeChildrenOutsideBounds(scene, new Rectangle(0,0,0,0));
    }
    protected boolean removeChildrenOutsideBounds(GraphScene scene, 
                                                Rectangle mySceneBounds)
    {
        boolean changed = false;
        
        // Find the node that owns the container
        Widget nodeWidget = Util.getParentNodeWidget(this);
        INamespace namespace = getContainerNamespace();
        
        // Next see if any nodes need to be removed from the container.
        List<Widget> children = getChildren();
        for (int index = children.size() - 1; index >= 0; index--)
        {
            Widget child = children.get(index);

            Rectangle sceneBounds = child.convertLocalToScene(child.getBounds());
            if (mySceneBounds.contains(sceneBounds) == false)
            {
                // Found an orphaned node.
                //
                // Find the node, and see who owns the node.  If the owner will
                // contain the orphaned node and it is a container, then add the
                // orphaned node to the containers namespace.
                //
                // Otherwise add the orphaned node to the scenes namespace.
                //
                removeChild(child);

                Widget newParent = nodeWidget.getParentWidget();
                INamespace newSpace = null;
                if (newParent instanceof ContainerWidget)
                {
                    newSpace = ((ContainerWidget) newParent).getContainerNamespace();
                }
                else
                {
                    Lookup lookup = getScene().getLookup();
                    IDiagram diagram = lookup.lookup(IDiagram.class);
                    if (diagram != null)
                    {
                        newSpace = diagram.getNamespace();
                    }
                    else
                    {
                        newSpace = namespace.getOwningPackage();
                    }
                }

                newParent.addChild(child);
                child.setPreferredLocation(newParent.convertSceneToLocal(sceneBounds.getLocation()));

                Object nodeData = scene.findObject(child);
                INamedElement element = (INamedElement) ((IPresentationElement)nodeData).getFirstSubject();

                if(namespace!=null)
                {
                    namespace.removeOwnedElement(element);//handle cases lifele combined fragmets, may be nodes, components etc
                }
                else if(getContainerElement() instanceof ICombinedFragment && element instanceof ILifeline)
                {
                    //combined fragment can contain lifelines but is not a namespace
                    ((ICombinedFragment)getContainerElement()).removeCoveredLifeline((ILifeline) element);
                }
                
                if(newSpace!=null)
                {
                    newSpace.addOwnedElement(element);
                }
                else
                {
                    if (newParent instanceof ContainerWidget)
                    {
                        IElement newElement=((ContainerWidget) newParent).getContainerElement();
                        if(newElement instanceof ICombinedFragment && element instanceof ILifeline)
                        {
                            //combined fragment can contain lifelines but is not a namespace
                           ((ICombinedFragment)newElement).addCoveredLifeline((ILifeline) element);
                        }
                    }
                }

                changed = true;
            }
        }
        return changed;
    }
    
    public class ContainerAcceptProvider extends SceneAcceptProvider
    {
        public ContainerAcceptProvider()
        {
            super(null);
        }

        @Override
        public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable)
        {
            ConnectorState retVal = super.isAcceptable(widget, point, transferable);
            
            if(retVal != ConnectorState.ACCEPT)
            {
                if(isWidgetMove(transferable) == true)
                {
                    retVal = ConnectorState.ACCEPT;
                }
            }
            
            return retVal;
        }

        
        @Override
        public void accept(Widget widget, Point point, Transferable transferable)
        {
            super.accept(widget, point, transferable);

            Widget[] target = null;
            ObjectScene scene = (ObjectScene) widget.getScene();
            boolean convertLocation = false;
            
            if(isWidgetMove(transferable) == true)
            {
                try
                {
                    MoveWidgetTransferable data = 
                            (MoveWidgetTransferable) transferable.getTransferData(MoveWidgetTransferable.FLAVOR);
                    target = new Widget[] { data.getWidget() };
                    convertLocation = true;
                }
                catch(Exception e)
                {
                    target = new Widget[0];
                }
            }
            else
            {
                // Now the new Nodes should be selected.  So get the widgets and 
                // add them to the container.
                target = new Widget[scene.getSelectedObjects().size()];
                Set <Object> selected = (Set<Object>) scene.getSelectedObjects();
                Object[] selectedArray = new Object[selected.size()];
                selected.toArray(selectedArray);

                for(int i = 0; i < selected.size(); i++)
                {
                    Object curObj = selectedArray[i];
                    Widget curWidget = scene.findWidget(curObj);
                    target[i] = curWidget;
                }
            }
            
            for(Widget curWidget : target)
            {
                if(curWidget.getParentWidget() != null)
                {
                    curWidget.getParentWidget().removeChild(curWidget);
                }
                
                Point curPt = curWidget.getLocation();
                addChild(curWidget);
                
                if(convertLocation == true)
                {
                    curWidget.setPreferredLocation(convertSceneToLocal(curPt));
                }
                
                Object data = scene.findObject(curWidget);
                INamedElement element = (INamedElement) ((IPresentationElement)data).getFirstSubject();
                INamespace ns = getContainerNamespace();
                IElement containerElem = getContainerElement();
                
                if(ns != null) 
                {
                    ns.addOwnedElement(element);
                   
                    // An activiy node that is contained in an activity group still
                    // has  Activity as its name space; hence adding 
                    // the node to its activity namespace
                    if (element instanceof  IActivityNode) 
                    {   
                        INamespace activityNamespace = ((DesignerScene) scene).getDiagram().getNamespace();
                        activityNamespace.addOwnedElement(element);
                    }
                }
                //some elements(like combined fragment) are not namespace but can contain other element graphically
                else if(containerElem instanceof ICombinedFragment && element instanceof ILifeline)
                {
                    ((ICombinedFragment) containerElem).addCoveredLifeline((ILifeline) element);
                }
            }
            
            if(target.length > 0)
            {
                firePropertyChange(CHILDREN_CHANGED, null, null);
            }
            revalidate();
        }
        
        @Override
        protected INamespace getNamespace()
        {
            return getContainerNamespace();
        }
        
        protected boolean isWidgetMove(Transferable transferable)
        {
            return transferable.isDataFlavorSupported(MoveWidgetTransferable.FLAVOR);
        }
    }
}