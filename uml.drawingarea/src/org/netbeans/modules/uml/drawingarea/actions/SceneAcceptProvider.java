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

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
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
import org.netbeans.modules.uml.core.metamodel.core.foundation.MetaLayerRelationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IAutonomousElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.RelationshipDiscovery;
import org.netbeans.modules.uml.drawingarea.dataobject.PaletteItem;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngine;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.MoveWidgetTransferable;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget.UMLWidgetIDString;
import org.netbeans.modules.uml.drawingarea.view.WidgetViewManager;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget.ContainerAcceptProvider;
import org.netbeans.modules.uml.ui.support.ADTransferable;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.MultiTransferObject;


public class SceneAcceptProvider implements AcceptProvider
{

    protected INamespace sceneNamespace = null;
    protected boolean handleMovingNodes = false;

    public SceneAcceptProvider(INamespace space)
    {
        this(space, true);
    }
    
    public SceneAcceptProvider(INamespace space, boolean handleMoving)
    {
        sceneNamespace = space;
        handleMovingNodes = handleMoving;
    }
    
    public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable)
    {
        if (transferable == null)
            return ConnectorState.REJECT;
        Transferable[] allTrans;
        if (transferable.isDataFlavorSupported(ExTransferable.multiFlavor))
        {
            try
            {
                MultiTransferObject transObj = (MultiTransferObject) transferable.getTransferData(ExTransferable.multiFlavor);
                allTrans = new Transferable[transObj.getCount()];
                for (int i = 0; i < allTrans.length; i++)
                {
                    allTrans[i] = transObj.getTransferableAt(i);
                }
            } catch (UnsupportedFlavorException ex)
            {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                return ConnectorState.REJECT;
            } catch (IOException ex)
            {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                return ConnectorState.REJECT;
            }
        } else
        {
            allTrans = new Transferable[]{transferable};
        }
        for (int i = 0; i < allTrans.length; i++)
        {
            Transferable t = allTrans[i];

            if (t.isDataFlavorSupported(PaletteItem.FLAVOR))
            {
                continue;
            } else if (t.isDataFlavorSupported(ADTransferable.ADDataFlavor))
            {
                try
                {
                    ADTransferable.ADTransferData transferData = (ADTransferable.ADTransferData) t.getTransferData(ADTransferable.ADDataFlavor);
                    if (transferData.getModelElements().size() == 0 &&
                            transferData.getPresentationElements().size() == 0)
                        return ConnectorState.REJECT;
                    else
                    {
                        IElement[] elements = new IElement[transferData.getModelElements().size()];
                        if (!elementsAllowed(transferData.getModelElements().toArray(elements)))
                            return ConnectorState.REJECT;
                        for (IPresentationElement pe: transferData.getPresentationElements())
                        {
                            if (!elementsAllowed(new IElement[]{pe.getFirstSubject()}))
                                return ConnectorState.REJECT;
                        }
                    }
                } catch (UnsupportedFlavorException ex)
                {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex)
                {
                    Exceptions.printStackTrace(ex);
                }
            }
            else if ((handleMovingNodes == true) && 
                     transferable.isDataFlavorSupported(MoveWidgetTransferable.FLAVOR))
            {
                try
                {
                    MoveWidgetTransferable tran = (MoveWidgetTransferable) transferable.getTransferData(MoveWidgetTransferable.FLAVOR);
                    Widget transferWidget = tran.getWidget();
                    if (transferWidget == null)
                    {
                        return ConnectorState.REJECT;
                    }
                    Object obj = ((ObjectScene) widget.getScene()).findObject(transferWidget);
                    if (!(obj instanceof IPresentationElement) || !elementsAllowed(new IElement[]{((IPresentationElement) obj).getFirstSubject()}))
                    {
                        return ConnectorState.REJECT;
                    }
                } catch  (UnsupportedFlavorException ex)
                {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex)
                {
                    Exceptions.printStackTrace(ex);
                }
            }
            else
            {
                return ConnectorState.REJECT;
            }
        }
        return ConnectorState.ACCEPT;
    }

    public void accept(Widget widget, Point point, Transferable transferable)
    {
        DesignerScene scene = (DesignerScene) widget.getScene();
        DiagramEngine engine = scene.getEngine();
        accept(engine, point, transferable);
    }

    public void accept(DiagramEngine engine, Point point, Transferable transferable)
    {  
        try
        {
            ArrayList<IPresentationElement> presentations = new ArrayList<IPresentationElement>();
            boolean discoverRleationships = false;
            
            // from palette
            if (transferable.isDataFlavorSupported(PaletteItem.FLAVOR))
            {
                INamedElement element = getElementFromPalette(transferable);
                element=engine.processDrop(element);
                INamespace space = getNamespace();
                if (space != null)
                {
                    space.addOwnedElement(element);
                }

                IPresentationElement presentation = Util.createNodePresentationElement();
                presentations.add(presentation);
                presentation.addSubject(element);
                engine.addWidget(presentation, point);
            }
            // single drop from project tree or single/multi drop from diagram
            // there is no need to discover and create links for the dropped nodes
            else if (transferable.isDataFlavorSupported(ADTransferable.ADDataFlavor))
            {
                ADTransferable.ADTransferData transferData = (ADTransferable.ADTransferData) transferable.getTransferData(ADTransferable.ADDataFlavor);
                double Xmin = Double.MAX_VALUE;
                double Ymin = Double.MAX_VALUE;

                Object sourceEngine = transferData.getDiagramEngine();
                if (sourceEngine instanceof DiagramEngine)
                {
                    ArrayList<IPresentationElement> elements = transferData.getPresentationElements();
                    ArrayList<IPresentationElement> list = new ArrayList<IPresentationElement>(elements);
                    
                    for (IPresentationElement pre : list)
                    {
                        Widget w = ((DiagramEngine) sourceEngine).getScene().findWidget(pre);
                        if (w == null)
                            continue;
                        // remove those contained elements from list; they will be recreated by their container widget
                        if (isContained(w, elements))
                            elements.remove(pre);
                        
                        if (w instanceof ConnectionWidget)
                        {
                            continue;
                        }
                        if (w != null)
                        {
                            Xmin = Math.min(Xmin, w.convertLocalToScene(new Point(0, 0)).getX());
                            Ymin = Math.min(Ymin, w.convertLocalToScene(new Point(0, 0)).getY());
                        }
                    }

                    Point startingPoint = new Point();
                    if (Xmin == Double.MAX_VALUE || Ymin == Double.MAX_VALUE)
                    {
                        startingPoint.setLocation(0, 0);
                    } else
                    {
                        startingPoint.setLocation(Xmin, Ymin);
                    }
                    HashMap<IPresentationElement, IPresentationElement> duplicates = new HashMap<IPresentationElement, IPresentationElement>();

                    // first pass to copy nodes
                    for (IPresentationElement pre : elements)
                    {
                        Widget original = ((DiagramEngine) sourceEngine).getScene().findWidget(pre);
                        
                        if(original instanceof UMLNodeWidget && !((UMLNodeWidget)original).isCopyCutDeletable())continue;
                        else if(original instanceof UMLEdgeWidget)continue;//we copy nodes in this section, see 2nd pass
                        
                        IPresentationElement presentation = Util.createNodePresentationElement();
                        presentation.addSubject(pre.getFirstSubject());
                        presentations.add(presentation);
                        Point setLocPoint=new Point(point);
                        if (original instanceof UMLNodeWidget)
                        {
                            setLocPoint=getNewLocation(startingPoint, point, original.getLocation());
                        }
                        Widget copy = engine.addWidget(presentation, setLocPoint);
                        if(copy != null)
                        {
                            duplicates.put(pre, presentation);
                            engine.getScene().validate();
                            if (original instanceof UMLNodeWidget)
                            {
                                ((UMLNodeWidget) original).duplicate(true, copy);
                            }
                        }
                        else
                        {
                            engine.getScene().removeNode(presentation);
                            
                            presentations.remove(presentation);
                            presentation.removeSubject(pre.getFirstSubject());
                            presentation.delete();
                        }
                    }

                    // second pass to copy edges
                    for (IPresentationElement pre : elements)
                    {
                        Widget original = ((DiagramEngine) sourceEngine).getScene().findWidget(pre);
                        if(original instanceof UMLNodeWidget && !((UMLNodeWidget)original).isCopyCutDeletable())continue;
                        IElement rel = pre.getFirstSubject();
                        if(rel instanceof IMessage)continue;//copying of messages is not supported
                        if (original instanceof UMLEdgeWidget)
                        {
                            IPresentationElement source = ((DiagramEngine) sourceEngine).getScene().getEdgeSource(pre);
                            IPresentationElement target = ((DiagramEngine) sourceEngine).getScene().getEdgeTarget(pre);
                            if (elements.contains(source) && elements.contains(target))
                            {
                                IPresentationElement newSource = duplicates.get(source);
                                IPresentationElement newTarget = duplicates.get(target);

                                IPresentationElement edge = Util.createNodePresentationElement();
                                // Workaround for nested link. Unlike other relationships, it does not
                                // have its own designated IElement, the IPresentationElement.getFirstSubject
                                // returns an element at one end. Use this mechanism (multiple subjects) for 
                                // DefaultDiagramEngine.createConnectionWidget() to identify the connector type
                                if (((UMLEdgeWidget)original).getWidgetID().
                                        equals(UMLWidgetIDString.NESTEDLINKCONNECTIONWIDGET.toString()))
                                {
                                    edge.addSubject(source.getFirstSubject());
                                    edge.addSubject(target.getFirstSubject());
                                }
                                else
                                {
                                    edge.addSubject(rel);
                                }
                                presentations.add(edge);
                                DesignerScene scene = engine.getScene();
                                Widget copy = scene.addEdge(edge);

                                scene.setEdgeSource(edge, newSource);
                                scene.setEdgeTarget(edge, newTarget);
                                Lookup lookup = copy.getLookup();
                                if (lookup != null)
                                {
                                    LabelManager manager = lookup.lookup(LabelManager.class);
                                    if (manager != null)
                                    {
                                        manager.createInitialLabels();
                                    }
                                }
                                engine.getScene().validate();
                                ((UMLEdgeWidget) original).duplicate(copy);                           
                            }
                        }
                    }
                    
                    // third pass to clean up some dangling edges
                    for (IPresentationElement pre : elements)
                    {
                        Widget original = ((DiagramEngine) sourceEngine).getScene().findWidget(pre);
                        if (transferData.getTransferType() == ADTransferable.CUT)
                        {
                            DesignerScene scene = ((DiagramEngine) sourceEngine).getScene();
                            // connect the edges to the new widget
                            if (original instanceof UMLNodeWidget)
                            {                                
                                IPresentationElement copy = duplicates.get(pre);
                                Collection<IPresentationElement> output = scene.findNodeEdges(pre, true, false);
                                Collection<IPresentationElement> input = scene.findNodeEdges(pre, false, true);
                                for (IPresentationElement edge : output)
                                {
                                    if (!elements.contains(edge))
                                        scene.setEdgeSource(edge, copy);
                                }
                                for (IPresentationElement edge : input)
                                {
                                    if (!elements.contains(edge))
                                        scene.setEdgeTarget(edge, copy);
                                }
                                ((UMLNodeWidget)original).remove();
                            }
                            else if (original instanceof ConnectionWidget)                           
                                scene.removeEdge(pre); 
                        }
                    }               
                }
                Point p = point;
                for (IElement modelElement : transferData.getModelElements())
                {
                    INamedElement element = (INamedElement) modelElement;

                    // check if drop is possible for this model element and if
                    // new element should be created
                    INamedElement toDrop = engine.processDrop(element);

                    if (toDrop != null && engine.isDropPossible(toDrop))
                    {
                        IPresentationElement presentation = Util.createNodePresentationElement();
                        presentations.add(presentation);

                        presentation.addSubject(toDrop);
                        engine.addWidget(presentation, p);
                        int x = (int) p.getX() + 50;
                        int y = (int) p.getY() + 50;
                        p = new Point(x, y);
                        
                        // import element if from different project
                        importElement(toDrop);
                        
                        // To track node if it dropped on a container for later use
                        keepTrackNodesIfDroppedOnContainer(presentation);
                    }
                }
                discoverRleationships = true;
                
            } 
            // multi drop from project tree, this is the case where we need to discover
            // the relationships among dropped nodes
            else if (transferable.isDataFlavorSupported(ExTransferable.multiFlavor))
            {
                Transferable[] allTrans = new Transferable[1];
                try
                {
                    MultiTransferObject transObj = (MultiTransferObject) transferable.getTransferData(ExTransferable.multiFlavor);
                    allTrans = new Transferable[transObj.getCount()];
                    for (int i = 0; i < allTrans.length; i++)
                    {
                        allTrans[i] = transObj.getTransferableAt(i);
                    }
                } catch (UnsupportedFlavorException ex)
                {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                } catch (IOException ex)
                {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }

                Point p = point;
                for (Transferable t : allTrans)
                {
                    ADTransferable.ADTransferData transferData = (ADTransferable.ADTransferData) t.getTransferData(ADTransferable.ADDataFlavor);
                    for (IElement modelElement : transferData.getModelElements())
                    {
                        INamedElement element = (INamedElement) modelElement;

                        // check if drop is possible for this model element and if
                        // new element should be created
                        INamedElement toDrop = engine.processDrop(element);

                        if (toDrop != null && engine.isDropPossible(toDrop))
                        {
                            IPresentationElement presentation = Util.createNodePresentationElement();
                            presentations.add(presentation);

                            presentation.addSubject(toDrop);
                            engine.addWidget(presentation, p);
                            int x = (int) p.getX() + 50;
                            int y = (int) p.getY() + 50;
                            p = new Point(x, y);
                            
                            importElement(toDrop);
                            
                            // To track node if it dropped on a container for later use
                            keepTrackNodesIfDroppedOnContainer(presentation);
                        }
                    }                            
                }
                discoverRleationships = true;
            }
            
            else if ((handleMovingNodes == true) && 
                    transferable.isDataFlavorSupported(MoveWidgetTransferable.FLAVOR))
            {
                try
                {
                    MoveWidgetTransferable tran = (MoveWidgetTransferable) transferable.getTransferData(MoveWidgetTransferable.FLAVOR);
                    Widget transferWidget = tran.getWidget();
                    INamespace ns = getNamespace();

                    IPresentationElement pe = (IPresentationElement) ((DesignerScene) engine.getScene()).findObject(transferWidget);
                    IElement element = pe.getFirstSubject();
                    
                    // do not change ownership if it's an imported element
                    if ( ns != null && ns.getProject() == element.getProject())
                    {
                        if (ns!=null && !ns.equals(element.getOwner()))
                        {
                            if (element instanceof INamedElement)
                            {
                                ns.addOwnedElement((INamedElement) element);
                            }
                        }
                        else 
                        {
                             if (element instanceof IActivityNode)
                            {   
                                IActivityNode activityElem = (IActivityNode) element;
                                ETList<IActivityGroup> groups = activityElem.getGroups();
                                // Remove an activity node from its container nodes, i.e., activity groups
                                for (IActivityGroup aGroup : groups)
                                {
                                    aGroup.removeNodeContent(activityElem);
                                    activityElem.removeGroup(aGroup);
                                }
                                ns.addOwnedElement(activityElem);
                            }
                        }
                    }
                    transferWidget.removeFromParent();
                    engine.getScene().getMainLayer().addChild(transferWidget);
                    transferWidget.setPreferredLocation(point);
                } catch (UnsupportedFlavorException ex)
                {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex)
                {
                    Exceptions.printStackTrace(ex);
                }
            }
            
            if(discoverRleationships == true)
            {
                createConnection(engine.getScene(), presentations);
            }
             
            for (IPresentationElement presentation : presentations)
            {                
                Widget newWidget = engine.getScene().findWidget(presentation);
                if(newWidget==null)
                {
                    if(presentation.getFirstSubject()!=null)presentation.getFirstSubject().removePresentationElement(presentation);
                    continue;
                    //may be connection from project tree, may be some issue but check for npe here, better realization is if isDropPossible above will return false, but it may be hard to have simple check if it's edge or node named element
                    //anyway if drop failed it may be better to do nothing rather then throw npe in this place
                    //also clear out such presentations in this loop
                }
                Lookup lookup = newWidget.getLookup();
                WidgetViewManager manager = lookup.lookup(WidgetViewManager.class);
                if (manager != null)
                {
                    String viewName = getDefaultViewName(transferable);
                    if((viewName != null) && (viewName.length() > 0))
                    {
                        manager.switchViewTo(viewName);
                    }
                }
            }

            if (!presentations.isEmpty())
            {
                engine.getScene().userSelectionSuggested(new HashSet<IPresentationElement>(presentations), false);
            }
        } catch (UnsupportedFlavorException ex)
        {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    // todo: should be overriden by each individual diagram scene 
    protected boolean elementsAllowed(IElement... element)
    {
        return true;
    }
    
    private void createConnection(DesignerScene scene, ArrayList<IPresentationElement> presentations)
    {
        
        ETList<IElement> elements = new ETArrayList<IElement> ();
        for (IPresentationElement pe: presentations)
        {
            elements.add(pe.getFirstSubject());
        }
        
        DiagramEngine engine = scene.getEngine();
        RelationshipDiscovery relDiscovery = engine.getRelationshipDiscovery();
        
        List < IElement > nodesOnScene = new ArrayList < IElement >();
        for(IPresentationElement element : scene.getNodes())
        {
            nodesOnScene.add(element.getFirstSubject());
        }
        
        relDiscovery.discoverCommonRelations(elements, nodesOnScene);
    }
    
    
    protected boolean isFromPalette(Transferable transferable)
    {
        return transferable.isDataFlavorSupported(PaletteItem.FLAVOR);
    }
    
    protected boolean isFromProjectTree(Transferable transferable)
    {
        return transferable.isDataFlavorSupported(ADTransferable.ADDataFlavor) || 
               transferable.isDataFlavorSupported(ExTransferable.multiFlavor);
    }
   
    protected INamedElement getElementFromPalette(Transferable transferable) throws UnsupportedFlavorException, IOException
    {
        PaletteItem item = (PaletteItem) transferable.getTransferData(PaletteItem.FLAVOR);
        return item.createModelElement(getNamespace());
    }

    protected String getDefaultViewName(Transferable transferable)
    {
        String retVal = "";
        try 
        {
            PaletteItem item = (PaletteItem) transferable.getTransferData(PaletteItem.FLAVOR);
        if (item != null)
        {
            retVal = item.getDefaultViewName();
        }
        }catch (UnsupportedFlavorException e)
        {
         // ignore
        }
        catch (IOException e)
        {
            //ignore
        }
        
        return retVal;
    }
    
    protected INamespace getNamespace()
    {
        return sceneNamespace;
    }

    private Point getNewLocation(Point startingPoint, Point dropPoint, Point original)
    {
        Point newPoint = new Point();
        double x = dropPoint.getX() - startingPoint.getX();
        double y = dropPoint.getY() - startingPoint.getY();
        newPoint.setLocation(original.getX() + x, original.getY() + y);
        return newPoint;
    }
    
    private void importElement(INamedElement element)
    {
        if (getNamespace() == null)
            return;
        
        if (element.getProject() != getNamespace().getProject())
        {
            // Only AutonomousElements can be imported across Projects
            if (element instanceof IAutonomousElement)
            {
                // create flat import element structure
                MetaLayerRelationFactory.instance().establishImportIfNeeded(getNamespace().getProject(), element);
            } 
        }
    }
    
    private void keepTrackNodesIfDroppedOnContainer (IPresentationElement targetPE)
    {
        if (this instanceof ContainerAcceptProvider && targetPE != null)    
        {
            Widget containerW = ((ContainerAcceptProvider)this).getContainerWidget();
            if (containerW instanceof ContainerWidget)
            {
                ArrayList <IPresentationElement> droppedNodes = ((ContainerWidget)containerW).getDroppedNodes();
                droppedNodes.add(targetPE);
            }
        }
    }
    
    private boolean isContained(Widget w, List<IPresentationElement> list)
    {
        assert w != null && w.getScene() instanceof ObjectScene;
        
        ObjectScene scene = (ObjectScene)w.getScene();
        Widget parent = w.getParentWidget();
        
        while (parent != null && parent != scene)
        {
            if (list.contains(scene.findObject(parent)))
                return true;
            parent = parent.getParentWidget();
        }
        return false;
    }
}