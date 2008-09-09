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
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.actions.SceneAcceptProvider;
import org.netbeans.modules.uml.drawingarea.actions.WidgetAcceptAction;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.MoveWidgetTransferable;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget.UMLWidgetIDString;
import org.openide.util.Exceptions;
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
    private ArrayList <IPresentationElement> droppedNodes = null;
    
    public ContainerWidget(Scene scene)
    {
        super(scene);
        this.setOpaque(false);
        
        setLayout(LayoutFactory.createAbsoluteLayout());
        
        initActions();
        //all should be as for scene initially
        if(scene instanceof DesignerScene)
        {
            DesignerScene sc=(DesignerScene) scene;
            // Inherit the foreground and background properties from the parent
            // node.
            setForeground(null);
            setBackground(null);
            setFont(sc.getMainLayer().getFont());
            //need to add font/colors listener to scene to get updates, but currently inheritance from scene
        }
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
        //ContainerAcceptProvider provider = new ContainerAcceptProvider();
        ContainerAcceptProvider provider = new ContainerAcceptProvider(this);
        WidgetAction acceptAction = ActionFactory.createAcceptAction(provider);
        
        createActions(DesignerTools.SELECT).addAction(acceptAction);
        createActions(DesignerTools.PALETTE).addAction(new WidgetAcceptAction(provider));
        createActions(DesignerTools.CONTEXT_PALETTE).addAction(acceptAction);
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

    private boolean addChildToCombinedFragment(ICombinedFragment namespace, Object nodeData, Widget node)
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

        if(((IPresentationElement)nodeData).getFirstSubject() instanceof ILifeline)
        {
            ILifeline element = (ILifeline) ((IPresentationElement)nodeData).getFirstSubject();
            if(namespace!=null)namespace.addCoveredLifeline(element);//combined fragment isn't a namespace but support graphical containment
            //TBD is it necessary to add element to an interaction?
        }
        return true;
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
            if(!(((IPresentationElement)nodeData).getFirstSubject() instanceof INamedElement ))continue;
            Widget node = scene.findWidget(nodeData);
            if (node != null)
            {
                Rectangle sceneBounds = node.convertLocalToScene(node.getBounds());
                if (mySceneBounds.contains(sceneBounds) == true)
                {
                    // If a node is alreay contained by a container, the entire
                    // container needs to be added, not the child node.
                    //
                    // In the case of nested containers, I need to check if the
                    // parent of the node is also the parent of 
                    // contianer node that owns "this" container.
                    Widget parent = node.getParentWidget();
                    
                    boolean performContainment = true;
                    if (parent instanceof ContainerWidget)
                    {
                        performContainment = false;
                        
                        // Check to see if both are contained by the same parent.
                        // If the are not both contained by the same parent
                        // then do not allow the containment.  The parent container
                        // must be added, as described in the previous comment.
                        
                        Object myNodeData = scene.findObject(this);
                        Widget myParentNode = scene.findWidget(myNodeData);
                        
                        if(myParentNode.getParentWidget() == parent)
                        {
                            performContainment = true;
                        }
                    }
                    
                    if(performContainment == true)
                    {
                        if(namespace!=null)
                        {
                            //works for most elements
                            changed =addChildNode(namespace, nodeData, node);
                        }
                        else if(contElement instanceof ICombinedFragment) {
                            //combined fragment can contain lifelines for example (cover) and is not a namespace
                            changed =addChildToCombinedFragment((ICombinedFragment) contElement,nodeData, node);
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
                if(!(((IPresentationElement)nodeData).getFirstSubject() instanceof INamedElement ))continue;
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
    
    protected boolean allowed(IElement... elements)
    {
        return true;
    }
    
    /**
     * Test if a widget is fully with in the bounds of the container widget.
     * 
     * @param widget The widget to test
     * @return True if the widget is in the containers bounds.
     */
    protected boolean isFullyContained(Widget widget)
    {
        // Calling getPreferredBounds forces the bounds to be calculated if it
        // has not already been calculated.  For example when the Widget was 
        // just created and therefore has not had a chance to be displayed.
        Rectangle area = widget.getClientArea();
        
        boolean retVal = false;
        if(area != null)
        {
            Rectangle sceneArea = widget.convertLocalToScene(area);

            Rectangle localArea = convertSceneToLocal(sceneArea);
            Rectangle myArea = getClientArea();
            retVal = myArea.contains(localArea);
        }
        
        return retVal;
    }
    

    public ArrayList<IPresentationElement> getDroppedNodes()
    {
        if (droppedNodes == null)
        {
            droppedNodes = new ArrayList<IPresentationElement>();
        }
        return droppedNodes;
    }

    public void setDroppedNodes(ArrayList<IPresentationElement> droppedNodes)
    {
        this.droppedNodes = droppedNodes;
    }
    
    
    public class ContainerAcceptProvider extends SceneAcceptProvider
    {
        private Widget containerWidget = null;
        
        public ContainerAcceptProvider()
        {
            super(null);
        }
        
        public ContainerAcceptProvider(ContainerWidget containerW)
        {
            this();
            containerWidget = containerW;
        }
        
        @Override
        protected boolean elementsAllowed(IElement... elements)
        {
            return allowed(elements);
        }

        @Override
        public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable)
        {
            ConnectorState retVal = super.isAcceptable(widget, point, transferable);
            
            if(isWidgetMove(transferable) == true)
            {
                try
                {
                    MoveWidgetTransferable data = (MoveWidgetTransferable) transferable.getTransferData(MoveWidgetTransferable.FLAVOR);
                    Widget[] target = new Widget[]{data.getWidget()};
                    for (Widget curWidget : target)
                    {
                        if (isFullyContained(curWidget) == false)
                        {
                            retVal = ConnectorState.REJECT;
                            break;
                        }
                    }
                }
                catch (UnsupportedFlavorException ex)
                {
                    // Since we first test if the datafalvor is supported, 
                    // it is an error if we get this exception.
                    Exceptions.printStackTrace(ex);
                }
                catch (IOException ex)
                {
                    Exceptions.printStackTrace(ex);
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
            boolean isMovingWidget = false;
            Set <Object> selected = null;
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
                
                isMovingWidget = true;
            }
            else
            {
                // Now the new Nodes should be selected.  So get the widgets and 
                // add them to the container.
                target = new Widget[scene.getSelectedObjects().size()];
                selected = (Set<Object>) scene.getSelectedObjects();
                Object[] selectedArray = new Object[selected.size()];
                selected.toArray(selectedArray);

                for(int i = 0; i < selected.size(); i++)
                {
                    Object curObj = selectedArray[i];
                    Widget curWidget = scene.findWidget(curObj);
                    target[i] = curWidget;
                }
            }
            final Set finalselected=selected!=null ? new HashSet(selected):null;
            boolean reselect=false;
            for(Widget curWidget : target)
            {
                // Only add the node to the container if it is fully contained
                // by the container.
                if((isMovingWidget == true) && (isFullyContained(curWidget) == false))
                {
                    break;
                }
                
                if(curWidget.getParentWidget() != null)
                {
                    curWidget.getParentWidget().removeChild(curWidget);
                }
                
                Point curPt = curWidget.getPreferredLocation();
                if(curPt==null)curPt = curWidget.getLocation();
                addChild(curWidget);
                Object data = scene.findObject(curWidget);
                INamedElement element = (INamedElement) ((IPresentationElement)data).getFirstSubject();
                
                if(convertLocation == true)
                {
                    curWidget.setPreferredLocation(convertSceneToLocal(curPt));
                }
                else if(element instanceof ILifeline)//lifeline need correction because need to be on certain level, not on dropped position
                {
                    curPt.y=convertSceneToLocal(curPt).y;
                    curWidget.setPreferredLocation(curPt);
                }
                
                INamespace ns = getContainerNamespace();
                IElement containerElem = getContainerElement();
                
                if(ns != null) 
                {
//                    ns.addOwnedElement(element);
                   
                     if (element instanceof  IActivityNode && ns instanceof IActivityGroup) 
                    {   
                        IActivityGroup group = (IActivityGroup) ns;
                        IActivityNode activityElem = (IActivityNode)element;
                        
                        // Remove an activity node from its previous containers, i.e., activity groups.
                        // This is the case when the activity node is moved from one container to the other.
                        ETList<IActivityGroup> previousGroups = activityElem.getGroups();
                        for (IActivityGroup aGroup : previousGroups)
                        {
                            aGroup.removeNodeContent(activityElem);
                            activityElem.removeGroup(aGroup);
                        }
                        // add the activity node to the new container
                        group.addOwnedElement(element);
                        activityElem.addGroup(group);
                       
                        // An activity node that is contained in an activity group is still under 
                        // an Activity namespace; hence adding the activity node
                        // to its Activity namespace.
                        INamespace activityNamespace = ((DesignerScene) scene).getDiagram().getNamespace();
                        activityNamespace.addOwnedElement(element);
                    }
                }
                //some elements(like combined fragment) are not namespace but can contain other element graphically
                else if(containerElem instanceof ICombinedFragment)
                {
                    ICombinedFragment cf=(ICombinedFragment) containerElem;
                    INamespace cfNs=cf.getNamespace();
                    if(element instanceof ILifeline)
                    {
                        ILifeline ll=(ILifeline) element;
                        cf.addCoveredLifeline(ll);
                    }
                    if(element.getNamespace()!=cfNs)
                    {
                        if(element.getNamespace()!=null)
                        {
                            element.getNamespace().removeOwnedElement(element);
                        }
                        cfNs.addOwnedElement(element);
                    }
                    reselect=true;
               }
            }
            
            if(target.length > 0)
            {
                firePropertyChange(CHILDREN_CHANGED, null, null);
            }
            revalidate();
            scene.validate();
           
            // after accept the dropped nodes, clear the list
           if (getContainerWidget() != null)
           {
               ((ContainerWidget)getContainerWidget()).setDroppedNodes(null);
           }
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

        public Widget getContainerWidget()
        {
            return containerWidget;
        }

        public void setContainerWidget(Widget containerWidget)
        {
            this.containerWidget = containerWidget;
        }
        
    }
    
    
    public void duplicate(boolean setBounds, Widget target)
    {
        assert target instanceof ContainerWidget;
        assert target.getScene() instanceof DesignerScene;

        DesignerScene targetScene = (DesignerScene) target.getScene();
        DesignerScene sourceScene = (DesignerScene) getScene();

        target.setFont(getFont());
        target.setForeground(getForeground());
        target.setBackground(getBackground());
        
        // some nodes may have logic to populate contained elements during initialization,
        // clear the container and only create the ones exist in original container
        List<Widget> children = new ArrayList<Widget> (target.getChildren());
        for (Widget c: children)
        {
            Object o = targetScene.findObject(c);
            if (o instanceof IPresentationElement)
                targetScene.removeNodeWithEdges((IPresentationElement)o);
        }

        // 1. clone contained inner nodes
        List<Widget> list = new ArrayList<Widget> (getChildren());
        for (Widget child : list)
        {
            if (!(child instanceof UMLNodeWidget))
            {
                continue;
            }
            IPresentationElement presentation = Util.createNodePresentationElement();
            presentation.addSubject(((UMLNodeWidget) child).getObject().getFirstSubject());
            Widget copy = targetScene.getEngine().addWidget(presentation, child.getPreferredLocation());
            ((UMLNodeWidget) child).duplicate(setBounds, copy);
            copy.setPreferredLocation(child.getPreferredLocation());

            copy.removeFromParent();
            target.addChild(copy);
        }
        targetScene.validate();

        // 2. clone connections among contained inner nodes

        for (ConnectionWidget cw : Util.getAllContainedEdges(this))
        {
            if (cw instanceof UMLEdgeWidget)
            {
                UMLEdgeWidget originalCW = (UMLEdgeWidget) cw;
                IPresentationElement sourcePE = sourceScene.getEdgeSource(originalCW.getObject());
                IPresentationElement targetPE = sourceScene.getEdgeTarget(originalCW.getObject());

                IPresentationElement newSourcePE = null;
                IPresentationElement newTargetPE = null;

                for (Object obj : Util.getAllNodeChildren(target))
                {
                    if (((IPresentationElement) obj).getFirstSubject().getXMIID().equals(sourcePE.getFirstSubject().getXMIID()))
                    {
                        newSourcePE = (IPresentationElement) obj;
                        break;
                    }
                }
                for (Object obj : Util.getAllNodeChildren(target))
                {
                    if (((IPresentationElement) obj).getFirstSubject().getXMIID().equals(targetPE.getFirstSubject().getXMIID()))
                    {
                        newTargetPE = (IPresentationElement) obj;
                        break;
                    }
                }

                IPresentationElement clonedEdgePE = Util.createNodePresentationElement();
                // Workaround for nested link. Unlike other relationships, it does not
                // have its own designated IElement, the IPresentationElement.getFirstSubject
                // returns an element at one end. Use this mechanism (multiple subjects) for 
                // DefaultDiagramEngine.createConnectionWidget() to identify the connector type
                if (((UMLEdgeWidget) cw).getWidgetID().
                        equals(UMLWidgetIDString.NESTEDLINKCONNECTIONWIDGET.toString()))
                {
                    clonedEdgePE.addSubject(sourcePE.getFirstSubject());
                    clonedEdgePE.addSubject(targetPE.getFirstSubject());
                } else
                {
                    clonedEdgePE.addSubject(originalCW.getObject().getFirstSubject());
                }

                Widget clonedEdge = targetScene.addEdge(clonedEdgePE);

                targetScene.setEdgeSource(clonedEdgePE, newSourcePE);
                targetScene.setEdgeTarget(clonedEdgePE, newTargetPE);
                Lookup lookup = clonedEdge.getLookup();
                if (lookup != null)
                {
                    LabelManager manager = lookup.lookup(LabelManager.class);
                    if (manager != null)
                    {
                        manager.createInitialLabels();
                    }
                }
                ((UMLEdgeWidget) originalCW).duplicate(clonedEdge);
            }
        }   
        targetScene.validate();
    }
}
