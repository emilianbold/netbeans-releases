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
package org.netbeans.modules.uml.diagrams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPort;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.diagrams.edges.AbstractUMLConnectionWidget;
import org.netbeans.modules.uml.diagrams.nodes.ContainerNode;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.RelationshipDiscovery;
import org.netbeans.modules.uml.drawingarea.support.ProxyPresentationElement;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;
import org.openide.util.Lookup;

/**
 *
 */
public class UMLRelationshipDiscovery implements RelationshipDiscovery
{
    private GraphScene scene = null;
    public UMLRelationshipDiscovery(GraphScene scene)
    {
        this.scene = scene;
    }

    public IPresentationElement createEdgePresentationElement(IElement element)
    {
        return createEdgePresentationElement(element, null);
    }
    
    public IPresentationElement createEdgePresentationElement(IElement element, 
                                                                     String proxyType)
    {
        IPresentationElement retVal = null;

        if ((element instanceof IPresentationElement) && 
            (proxyType != null) &&
            (proxyType.length() > 0))
        {
            IPresentationElement presentation = (IPresentationElement) element;
            retVal = new ProxyPresentationElement(presentation, proxyType);
        }
        else
        {
            ICreationFactory creationFactory = FactoryRetriever.instance().getCreationFactory();
            if (creationFactory != null)
            {
                Object presentationObj = creationFactory.retrieveMetaType("NodePresentation", null);
                if (presentationObj instanceof IPresentationElement)
                {
                    retVal = (IPresentationElement) presentationObj;
                    retVal.addSubject(element);
                    element.addPresentationElement(retVal);
                }
            }
        }

        return retVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery#discoverCommonRelations(boolean)
     */
    public List<IPresentationElement> discoverCommonRelations(Collection<IElement> pNewElementsBeingCreated, 
                                                              Collection<IElement> pElementsAlreadyOnTheDiagrams)
    {
        // Normal relationship discovery logic
        List<IPresentationElement> allDiscovered = new ArrayList<IPresentationElement>();

        List<IPresentationElement> newPresentationElements;
        newPresentationElements = createCommonRelations(pNewElementsBeingCreated, 
                                                        pElementsAlreadyOnTheDiagrams);


        // Discover nested links and update the busy state if it's active
        List<IPresentationElement> nestedLinks;
        nestedLinks = createdNestedLinks(pNewElementsBeingCreated,
                                          pElementsAlreadyOnTheDiagrams);

        // Discover comment links and update the busy state if it's active
        List<IPresentationElement> commentLinks;
        commentLinks = createCommentLinks(pNewElementsBeingCreated,
                                            pElementsAlreadyOnTheDiagrams);
//
        // Discover part facade links and update the busy state if it's active
        List<IPresentationElement> associationClassLinks;
        associationClassLinks = discoverAssociationClassLinks(pNewElementsBeingCreated,
                                                              pElementsAlreadyOnTheDiagrams);

        List<IPresentationElement> partFacadeLinks; 
        partFacadeLinks = discoverPartFacadeLinks(pNewElementsBeingCreated, 
                                                  pElementsAlreadyOnTheDiagrams);

        // Okay add them up.
        if (newPresentationElements != null)
        {
            allDiscovered.addAll(newPresentationElements);
        }

        if (nestedLinks != null)
        {
            allDiscovered.addAll(nestedLinks);
        }

        if (commentLinks != null)
        {
            allDiscovered.addAll(commentLinks);
        }

        if (associationClassLinks != null)
        {
            allDiscovered.addAll(associationClassLinks);
        }
        if (partFacadeLinks != null)
        {
            allDiscovered.addAll(partFacadeLinks);
        }

        scene.validate();
        return allDiscovered;

    }

    public List<IPresentationElement> discoverCommonRelations(Collection<IElement> pDiscoverOnTheseElements)
    {
        List<IPresentationElement> retVal = createCommonRelations(pDiscoverOnTheseElements);
            List<IPresentationElement> createdElements = createNestedLinks(pDiscoverOnTheseElements);

        if (createdElements != null && retVal != null)
        {
            retVal.addAll(createdElements);
        }

        // Discover comment links and update the busy state if it's active
        List<IPresentationElement> commentLinks;
        commentLinks = createCommentLinks(pDiscoverOnTheseElements);
        if (commentLinks != null)
        {
            retVal.addAll(commentLinks);
        }

        List<IPresentationElement> associationClasses = discoverAssociationClassLinks(pDiscoverOnTheseElements);
        if (associationClasses != null && retVal != null)
        {
            retVal.addAll(associationClasses);
        }
//        }
//        else
//        {
//            ETList<IPresentationElement> createdElements = discoverMessages(null);
//            if (createdElements != null && retVal != null)
//            {
//                retVal.addThese(createdElements);
//            }
//        }
//        {
//            ETList<IPresentationElement> createdElements = discoverCommentLinks(pDiscoverOnTheseElements);
//            if (createdElements != null && retVal != null)
//            {
//                retVal.addThese(createdElements);
//            }
//        }
        
        List<IPresentationElement> partFacadeLinks = discoverPartFacadeLinks(pDiscoverOnTheseElements);
        if (partFacadeLinks != null && retVal != null)
        {
            retVal.addAll(partFacadeLinks);
        }
        
//        {
//            ETList<IPresentationElement> createdElements = discoverAssociationClassLinks(pDiscoverOnTheseElements);
//            if (createdElements != null && retVal != null)
//            {
//                retVal.addThese(createdElements);
//            }
//        }

        scene.validate();
        return retVal;
    }
    
    public List<IPresentationElement> createCommonRelations(Collection<IElement> pNewElementsBeingCreated, 
                                                            Collection<IElement> pElementsAlreadyOnTheDiagrams)
    {
        
        List<IPresentationElement> retVal = new ArrayList<IPresentationElement>();

        int items = pNewElementsBeingCreated != null ? pNewElementsBeingCreated.size() : 0;
        if (items > 0)
        {
            // First I have to convert to the old collection type.  
            ETList created = new ETArrayList < IElement > (pNewElementsBeingCreated);
            ETList onDiagram = new ETArrayList < IElement > (pElementsAlreadyOnTheDiagrams);

            IRelationFactory factory = new RelationFactory();
            ETList<IRelationProxy> proxies = factory.determineCommonRelations3(created, onDiagram);

            if (proxies != null && proxies.size() > 0)
            {
                Iterator<IRelationProxy> proxyIter = proxies.iterator();
                while (proxyIter.hasNext())
                {
                    IRelationProxy proxy = proxyIter.next();

                    IPresentationElement newPresentation = addConnectToScene(proxy);
                    if(newPresentation != null)
                    {
                        retVal.add(newPresentation);
                    }
                }
            }
        }

        return retVal;
    }
    
    private IPresentationElement addConnectToScene(IRelationProxy proxy)
    {
        IElement from = proxy.getFrom();
        IElement to = proxy.getTo();

        IElement connection = proxy.getConnection();

        IPresentationElement sourceElement = null;
        IPresentationElement targetElement = null;

        Collection < IPresentationElement > nodes = scene.getNodes();
        for (IPresentationElement pe : nodes)
        {
            // If the subject is equal to NULL that means someone has removed
            // the subject from the presentation element, but did not remove
            // the node from the diagram.
            IElement subject = pe.getFirstSubject();
            if(subject != null)
            {
                if (pe.getFirstSubject().equals(from))
                {
                    sourceElement = pe;
                    break;
                }
            }
        }
        for (IPresentationElement pe : nodes)
        {
            IElement subject = pe.getFirstSubject();
            
            // If the subject is equal to NULL that means someone has removed
            // the subject from the presentation element, but did not remove
            // the node from the diagram.
            if(subject != null)
            {
                if (pe.getFirstSubject().equals(to))
                {
                    targetElement = pe;
                    break;
                }
            }
        }
        IPresentationElement edge = createConnection(connection, 
                                                     from, 
                                                     to,
                                                     connection.getElementType());
        
        return edge;
    }
    
    public List<IPresentationElement> createCommonRelations(Collection<IElement> pDiscoverOnTheseElements)
    {
        List<IPresentationElement> retVal = new ArrayList<IPresentationElement>();
        long numElements = pDiscoverOnTheseElements != null ? pDiscoverOnTheseElements.size() : 0;

        if (numElements > 0)
        {
            IRelationFactory factory = new RelationFactory();
            if (factory != null)
            {
                ETList onDiagram = new ETArrayList < IElement > (pDiscoverOnTheseElements);

                ETList<IRelationProxy> proxies = factory.determineCommonRelations(onDiagram);

                long numProxies = proxies != null ? proxies.size() : 0;

                if (numProxies > 0)
                {
                    Iterator<IRelationProxy> proxyIter = proxies.iterator();
                    while (proxyIter.hasNext())
                    {
                        IRelationProxy proxy = proxyIter.next();

                        IPresentationElement newPresentation = addConnectToScene(proxy);
                        if(newPresentation != null)
                        {
                            retVal.add(newPresentation);
                        }
                    }
                }
            }
        }

        return retVal;
    }
    
    public List<IPresentationElement> createNestedLinks(Collection<IElement> pDiscoverOnTheseElements)
    {
        Collection <IElement> pFoundModelElements = pDiscoverOnTheseElements;
        if((pFoundModelElements == null) || (pFoundModelElements.size() == 0))
        {
            pFoundModelElements = getAllNodeModelElements();
        }

        List<IPresentationElement> retVal = new ArrayList<IPresentationElement>();

        if ((pFoundModelElements != null) &&
            (pFoundModelElements.size() > 0))
        {
            List<INamedElement> pNamedElements = new ArrayList<INamedElement>();
            
            // Reduce this list to just namedelements
            for (IElement element : pFoundModelElements)
            {
                // Add all the named elements, except for a few that
                // should never have nested links
                if ((element instanceof INamedElement) &&
                        !(element instanceof IPort))
                {
                    pNamedElements.add((INamedElement) element);
                }
            }

            for (INamedElement pNamedElement : pNamedElements)
            {
                // Now start looking for children and parents among the list
                if (pNamedElement != null)
                {
                    INamespace pNamespace = pNamedElement.getNamespace();
                    if (pNamedElements.contains(pNamespace))
                    {
                        // Create the nested link
                        IPresentationElement pCreatedPE = createNestedLink(pNamedElement, pNamespace);
                        if (pCreatedPE != null)
                        {
                            retVal.add(pCreatedPE);
                        }
                    }
                }
            }
        }
        
        return retVal;
    }
    /* Discover nested links when the user drops onto the diagram
     *
     * @param bAutoRouteEdges [in] Should we autoroute the edges
     * @param pNewElementsBeingCreated [in] The elements being created dropped onto the diagram
     * @param pElementsAlreadyOnTheDiagrams [in] The elements already on the diagram
     * @param pPresentationElements [out,retval] The created presentation elements
     */
    public List<IPresentationElement> createdNestedLinks(Collection<IElement> pNewElementsBeingCreated, 
                                                            Collection<IElement> pElementsAlreadyOnTheDiagrams)
    {
        // Discover nested links.  This is complicated because we don't want to discover links between 
        // items already on the diagram.  Just between the new elements being dropped and elements already
        // on the diagram.

        List<IPresentationElement> retVal = new ArrayList<IPresentationElement>();
        List<IPresentationElement> createdElements = createNestedLinks(pNewElementsBeingCreated);

        // Discover nested links among items being created on the diagram
        if (createdElements != null && retVal != null)
        {
            retVal.addAll(createdElements);
        }

        if (createdElements != null)
        {
            createdElements.clear();
        }

        // Now see if any of the elements being created are related to elements 
        // already on the diagram			
        ETList<IElement> elements = new ETArrayList<IElement>();
        
        // See if parents to the dropped items exist on the diagram
        for (IElement pElement : pNewElementsBeingCreated)
        {
            INamedElement pNamedElement = null;
            if (pElement instanceof INamedElement)
            {
                pNamedElement = (INamedElement) pElement;
                INamespace pNamespace = pNamedElement.getNamespace();

                if (pNamespace != null)
                {
                    if (pElementsAlreadyOnTheDiagrams != null &&
                            pElementsAlreadyOnTheDiagrams.contains(pNamespace))
                    {
                        elements.clear();
                        elements.add(pNamespace);
                        elements.add(pElement);

                        createdElements = createNestedLinks(elements);
                        if (createdElements != null && retVal != null)
                        {
                            retVal.addAll(createdElements);
                        }

                        if (createdElements != null)
                        {
                            createdElements.clear();
                        }
                    }
                }
            }
        }

        // Now see if children to the dropped items exist on the diagram
        for (IElement pElement : pElementsAlreadyOnTheDiagrams)
        {
            if (pElement instanceof INamedElement )
            {
                INamedElement pNamedElement = (INamedElement) pElement;
                INamespace pNamespace = pNamedElement.getNamespace();

                if (pNamespace != null)
                {
                    if (pNewElementsBeingCreated != null && 
                        pNewElementsBeingCreated.contains(pNamespace))
                    {
                        elements.clear();
                        elements.add(pNamespace);
                        elements.add(pElement);

                        createdElements = createNestedLinks(elements);
                        if (createdElements != null && retVal != null)
                        {
                            retVal.addAll(createdElements);
                        }

                        if (createdElements != null)
                        {
                            createdElements.clear();
                        }
                    }
                }
            }
        }
        
        return retVal;
    }

    /**
     * Used to create a nested link between the argument elements
     *
     * @param fromElement [in] The child element that's owned by pNamespace
     * @param pNamespace [in]  The namespace owner of pNestedChild
     * @param pCreatedPE [out] The created presentation element for the link.  Note that it's not an error
     * for a NULL to be returned - the link may already exist.
     */
    public IPresentationElement createNestedLink(INamedElement childElement, 
                                                 INamespace namespace)
    {
        if (childElement == null || namespace == null)
        {
            return null;
        }
        
        IElement fromElement = namespace instanceof IElement ? (IElement) namespace : null;
        IElement linkElement = namespace;

        List<IPresentationElement> pStartPEs;
        List<IPresentationElement> pEndPEs;

        // See if they both have presentation elements on this diagram
        pEndPEs = getNodesOnScene(childElement, scene);
        pStartPEs = getNodesOnScene(fromElement, scene);

        // Now see how many presentation items we have
        long numStartPEs = pStartPEs != null ? pStartPEs.size() : 0;
        long numEndPEs = pEndPEs != null ? pEndPEs.size() : 0;

        IPresentationElement retVal = null;
        
        // We have presentation elements on both the general and specific
        if (numStartPEs > 0 && numEndPEs > 0 && numStartPEs == 1 && numEndPEs == 1)
        {
            IPresentationElement pFromPresentationElement = pStartPEs.get(0);
            IPresentationElement pToPresentationElement = pEndPEs.get(0);
            if (linkAlreadyExists(linkElement, pFromPresentationElement, pToPresentationElement) == false && 
                linkIsValid("NestedLink", pFromPresentationElement, pToPresentationElement) == true)
            {
                // In the case of nested links we do one more check not done for other links.  If the 
                // to element is sitting on the from element then don't do the link.  The namespace
                // containment is being represented by graphical containment - no need for the link.

                Widget fromWidget = scene.findWidget(pFromPresentationElement);
                Widget toWidget = scene.findWidget(pToPresentationElement);
                boolean okToCreateEdge = true;
                
                if (fromWidget instanceof ContainerNode)
                {
                    ContainerWidget containerWidget = ((ContainerNode) fromWidget).getContainer();
                    
                    if (toWidget.getParentWidget().equals(containerWidget)) 
                    {
                        okToCreateEdge = false;
                    }
                    else 
                    {
                        // if a node is dnd from the project tree to a container node on a digram,
                        // the node has not yet been added to the container yet at this point,
                        // so I can not determine if the node is a child of the container node by 
                        // calling fromWidget.getParentWidget()
                        ArrayList <IPresentationElement> nodesDroppedOnContainer = containerWidget.getDroppedNodes();
                        for (IPresentationElement nodePE : nodesDroppedOnContainer)
                        {
                            if (pToPresentationElement.equals(nodePE))
                            {
                                okToCreateEdge = false;
                                break;
                            }
                        }
                    }
                }
                
                if (okToCreateEdge)
                {            
                    retVal = createConnection(pFromPresentationElement, 
                                          pFromPresentationElement, 
                                          pToPresentationElement,
                                          "NestedLink");
                }
            }
        }
        return retVal;
    }
    
    protected List < IPresentationElement > getNodesOnScene(IElement element,
                                                                GraphScene scene)
    {
        ArrayList < IPresentationElement > retVal = new ArrayList < IPresentationElement >();
        
        // To check if the presentation is on the scene, simply call isNode.
        // Since the presentation element is the nodes data object, it should
        // return true the presentation element is representing a node.
        for(IPresentationElement presentation : element.getPresentationElements())
        {
            if(scene.isNode(presentation) == true)
            {
                retVal.add(presentation);
            }
        }
        
        return retVal;
    }
    
    /**
     * Basically check if the link is already displayed on the diagram.  If the
     * link is already displayed on the diagram, then do not show it again.
     * 
     * @param pLinkElement
     * @param pStartNode
     * @param pEndNode
     * @return
     */
    protected boolean linkAlreadyExists(IElement pLinkElement, 
                                               IPresentationElement pStartNode, 
                                               IPresentationElement pEndNode)
    {
        boolean retVal = false;
        
        if (pStartNode != null && pEndNode != null)
        {
            Collection<IPresentationElement> edges = scene.findNodeEdges(pStartNode, true, true);
            
            for(IPresentationElement curEdge : edges)
            {
                // First see if this edge is the connected to the end node.
                IPresentationElement source = (IPresentationElement)scene.getEdgeSource(curEdge);
                IPresentationElement target = (IPresentationElement)scene.getEdgeTarget(curEdge);
                
                if((source.isSame(pEndNode) == true) ||
                   (target.isSame(pEndNode) == true))
                {
                    // See if any of these links have pLinkElement as a model element
                    IElement pThisElement = curEdge.getFirstSubject();
                    if (pThisElement != null && pThisElement.isSame(pLinkElement))
                    {
                        return true;
                    }
                }
            }
        }

        return retVal;
    }
    
    /**
     * Checks to see if this link is allowed between these two types
     *
     * @param linkType [in] The type of the link
     * @param pLinkElement [in] The element that is attached to the new link
     * @param pStartNode [in] The start node of the link we're trying to find
     * @param pEndNode [in] The end node of the link we're trying to find
     */
    protected boolean linkIsValid(String linkType, 
                                  IPresentationElement pStartNode, 
                                  IPresentationElement pEndNode)
    {
        if (pStartNode == null || pEndNode == null || linkType == null)
        {
            return false;
        }
        
        boolean retVal = true;
        
        if (linkType.equals("NestedLink") == true)
        {
            IElement pStartME = pStartNode.getFirstSubject();
            IElement pEndME = pEndNode.getFirstSubject();

            String sStartType = pStartME != null ? pStartME.getElementType() : null;
            String sEndType = pEndME != null ? pEndME.getElementType() : null;

            if (sStartType != null && sEndType != null && 
               (sStartType.compareTo("PartFacade") == 0 || 
                sEndType.compareTo("Collaboration") == 0 || 
                sStartType.compareTo("Collaboration") == 0 || 
                sEndType.compareTo("PartFacade") == 0))
            {
                retVal = false;
            }
        }

        return retVal;
    }

    private IPresentationElement createConnection(IElement connection, 
                                                         IPresentationElement source, 
                                                         IPresentationElement target,
                                                         String proxyType)
    {

        IPresentationElement edge = createEdgePresentationElement(connection, proxyType);
        Widget w = scene.addEdge(edge);

        if(w!=null)
        {
            scene.setEdgeSource(edge, source);
            scene.setEdgeTarget(edge, target);

            Lookup lookup = w.getLookup();
            if (lookup != null)
            {
                LabelManager manager = lookup.lookup(LabelManager.class);
                if (manager != null)
                {
                    manager.createInitialLabels();
                }
            }
        }
        else
        {
            connection.removePresentationElement(edge);
            edge=null;
        }

        return edge;
    }
    
    
    private IPresentationElement createConnection(IElement connection, 
                                                         IElement fromElement, 
                                                         IElement toElement,
                                                         String proxyType)
    {
        if (fromElement == null || toElement == null)
        {
            return null;
        }
        
        List<IPresentationElement> pStartPEs;
        List<IPresentationElement> pEndPEs;

        // See if they both have presentation elements on this diagram
        pStartPEs = getNodesOnScene(fromElement, scene);
        pEndPEs = getNodesOnScene(toElement, scene);

        // Now see how many presentation items we have
        long numStartPEs = pStartPEs != null ? pStartPEs.size() : 0;
        long numEndPEs = pEndPEs != null ? pEndPEs.size() : 0;

        IPresentationElement retVal = null;
        
        // We have presentation elements on both the general and specific
        if (numStartPEs > 0 && numEndPEs > 0 && numStartPEs == 1 && numEndPEs == 1)
        {
            IPresentationElement pFromPresentationElement = pStartPEs.get(0);
            IPresentationElement pToPresentationElement = pEndPEs.get(0);
            if (linkAlreadyExists(connection, pFromPresentationElement, pToPresentationElement) == false && 
                linkIsValid(proxyType, pFromPresentationElement, pToPresentationElement) == true)
            {
                retVal = createConnection(connection, 
                                          pFromPresentationElement, 
                                          pToPresentationElement,
                                          proxyType);
            }
        }
        
        return retVal;
    }
    
    private List < IElement > getAllNodeModelElements()
    {
        List < IElement > retVal = new ArrayList < IElement >();
        
        for(Object obj : scene.getNodes())
        {
            IPresentationElement node = (IPresentationElement)obj;
            retVal.add(node.getFirstSubject());
        }
        
        return retVal;
    }
    
    /**
     * Discovers Comments
     *
     * @param pDiscoverOnTheseElements [in] The elements that should be used to search for nested link relations.
     * @param pPresentationElements [out,retval] The created presentation elements
     */
    public List<IPresentationElement> createCommentLinks(Collection<IElement> pDiscoverOnTheseElement)
    {
        Collection<IElement> foundElements = pDiscoverOnTheseElement;
        if((foundElements == null) && (foundElements.size() == 0))
        {
            foundElements = getAllNodeModelElements();
        }

        // Get a list of all the IComments

        List<IComment> pComments = new ArrayList<IComment>();

        // Create a list of just comments
        for (IElement curElement : foundElements)
        {
            if (curElement instanceof IComment)
            {
                IComment comment = (IComment)curElement;
                if (!pComments.contains(comment))
                {
                    pComments.add(comment);
                }
            }
        }

        // Now go over the list of elements and see if any of them are 
        // annotated by the comments
        List<IPresentationElement> retVal = new ArrayList<IPresentationElement>();
        
        for (IElement curElement : foundElements)
        {
            if (curElement instanceof INamedElement)
            {
                INamedElement pNamedElement = (INamedElement) curElement;
                
                // Go over the list of comments and see if any of them annotate
                // this guy
                for (IComment comment : pComments)
                {
                    if (comment != null)
                    {
                        if (comment.getIsAnnotatedElement(pNamedElement))
                        {
                            IPresentationElement pCreatedPE = 
                                    createConnection(comment, 
                                                     curElement, 
                                                     comment, 
                                                     "CommentLink");
                            
                            if (pCreatedPE != null)
                            {
                                retVal.add(pCreatedPE);
                            }
                        }
                    }
                }
            }
        }

        return retVal;
    }

    public List<IPresentationElement> createCommentLinks(Collection<IElement> newElementsBeingCreated, 
                                                             Collection <IElement> elementsAlreadyOnTheDiagrams)
    {
        List<IPresentationElement> retVal = new ArrayList<IPresentationElement>();

        // Now we need to discover comment links.  This is equally 
        // complicated for the same reasons as above. We need to find all 
        // comments being dropped and see if there are any annotated
        // elements already on the diagram then visa versa.
        List<IPresentationElement> createdElements = 
                createCommentLinks(newElementsBeingCreated);
        
        // Discover comment links among items being created on the diagram
        if (createdElements != null)
        {
            retVal.addAll(retVal);
            createdElements.clear();
        }


        // Now see if any of the elements being created are related to 
        // elements already on the diagram.  The easiest way to do this is 
        // get a list of all IComments being dropped and those already on 
        // the diagram.  Then discover using the two sets of elements
        ETList<IElement> commentsBeingCreated = new ETArrayList<IElement>();
        ETList<IElement> commentsAlreadyOnTheDiagram = new ETArrayList<IElement>();
        ETList<IElement> nonCommentsBeingCreated = new ETArrayList<IElement>();
        ETList<IElement> nonCommentsAlreadyOnTheDiagram = new ETArrayList<IElement>();

        // Divide the items being dropped into two groups - comments and 
        // non-comments
        for (IElement pElement : newElementsBeingCreated)
        {
            if (pElement instanceof IComment)
            {
                commentsBeingCreated.add(pElement);
            }
            else
            {
                nonCommentsBeingCreated.add(pElement);
            }
        }

        // Divide the items already on the diagram into two groups - comments 
        // and non-comments
        for (IElement pElement : elementsAlreadyOnTheDiagrams)
        {
            if (pElement instanceof IComment)
            {
                commentsAlreadyOnTheDiagram.add(pElement);
            }
            else
            {
                nonCommentsAlreadyOnTheDiagram.add(pElement);
            }
        }

        // Now do three calls to search for links
        // pCommentsBeingCreated + pNonCommentsAlreadyOnTheDiagram
        // pNonCommentsBeingCreated + pCommentsAlreadyOnTheDiagram
        // pCommentsBeingCreated + pCommentsAlreadyOnTheDiagram (comments can annotate comments)
        //
        // The only bug is that comment to comment links will reappear when both comments are already
        // on the diagram.  But the code is much simpler and this use case doesn't pop up.
        long commentsBeingCreatedCount = commentsBeingCreated.size();
        long nonCommentsBeingCreatedCount = nonCommentsBeingCreated.size();
        long commentsAlreadyOnTheDiagramCount = commentsAlreadyOnTheDiagram.size();
        long nonCommentsAlreadyOnTheDiagramCount = nonCommentsAlreadyOnTheDiagram.size();

        // 1. pCommentsBeingCreated + pNonCommentsAlreadyOnTheDiagram
        if (commentsBeingCreatedCount > 0 && nonCommentsAlreadyOnTheDiagramCount > 0)
        {
            ETList<IElement> pNewList = new ETArrayList<IElement>();
            pNewList.addAll(commentsBeingCreated);
            pNewList.addAll(nonCommentsAlreadyOnTheDiagram);

            createdElements = createCommentLinks(pNewList);
            if (createdElements != null && retVal != null)
            {
                retVal.addAll(createdElements);
            }

            if (createdElements != null)
            {
                createdElements.clear();
            }
        }

        // 2. pNonCommentsBeingCreated + pCommentsAlreadyOnTheDiagram
        if (nonCommentsBeingCreatedCount > 0 && commentsAlreadyOnTheDiagramCount > 0)
        {
            ETList<IElement> pNewList = new ETArrayList<IElement>();

            pNewList.addAll(nonCommentsBeingCreated);
            pNewList.addAll(commentsAlreadyOnTheDiagram);

            createdElements = createCommentLinks(pNewList);
            if (createdElements != null && retVal != null)
            {
                retVal.addAll(createdElements);
            }

            if (createdElements != null)
            {
                createdElements.clear();
            }
        }

        // 3. pCommentsBeingCreated + pCommentsAlreadyOnTheDiagram
        if (commentsBeingCreatedCount > 0 && commentsAlreadyOnTheDiagramCount > 0)
        {
            ETList<IElement> pNewList = new ETArrayList<IElement>();

            pNewList.addAll(commentsBeingCreated);
            pNewList.addAll(commentsAlreadyOnTheDiagram);

            createdElements = createCommentLinks(pNewList);
            if (createdElements != null && retVal != null)
            {
                retVal.addAll(createdElements);
            }

            if (createdElements != null)
            {
                createdElements.clear();
            }
        }
        return retVal;
    }
    
    /**
     * Discover associationclass links when the user drops onto the diagram
     *
     * @param pDiscoverOnTheseElements [in] The elements that should be used to search for associationclass relations.
     * @param pPresentationElements [out,retval] The created presentation elements
     */
    public List<IPresentationElement> discoverAssociationClassLinks(Collection<IElement> pDiscoverOnTheseElements)
    {
        Collection<IElement> foundElements = pDiscoverOnTheseElements;
        if((foundElements == null) && (foundElements.size() == 0))
        {
            foundElements = getAllNodeModelElements();
        }

        // Create a list of just collaborations
        List<IPresentationElement> retVal = new ArrayList < IPresentationElement >();
        for (IElement curElement : foundElements)
        {
            // Go through and discover on all association classes

            if (curElement instanceof IAssociationClass)
            {
                IAssociationClass pAssocClass = (IAssociationClass) curElement;

                List < IAssociationEnd > ends = pAssocClass.getEnds();

                if(ends.size() == 2)
                {
                    IAssociationEnd sourceEnd = ends.get(0);
                    IAssociationEnd targetEnd = ends.get(1);

                    IClassifier sourceClazz = sourceEnd.getFeaturingClassifier();
                    IClassifier targetClazz = targetEnd.getFeaturingClassifier();

                    IPresentationElement pCreatedPE = createConnection(pAssocClass, 
                                                 sourceClazz, 
                                                 targetClazz, 
                                                 "AssociationClass");

                    if (pCreatedPE != null)
                    {
                        retVal.add(pCreatedPE);

//                        Widget widget = scene.findWidget(pCreatedPE);
//                        if (widget instanceof AssociationClassConnector)
//                        {
//                            AssociationClassConnector connector = (AssociationClassConnector) widget;
//                            List < IPresentationElement > presentations = getNodesOnScene(pAssocClass, scene);
//                            if(presentations.size() > 0)
//                            {
//                                IPresentationElement node = presentations.get(0);
//                                connector.buildBridge(node);
//                            }
//                        }
                    }
                }  
            }
        }

        return retVal;
    }

    /**
     * Discover associationclass links when the user drops onto the diagram
     *
     * @param bAutoRouteEdges [in] Should we autoroute the edges
     * @param pNewElementsBeingCreated [in] The elements being created dropped onto the diagram
     * @param pElementsAlreadyOnTheDiagrams [in] The elements already on the diagram
     * @param pPresentationElements [out,retval] The created presentation elements
     */
    public ETList<IPresentationElement> discoverAssociationClassLinks(Collection<IElement> pNewElementsBeingCreated, 
                                                                             Collection<IElement> pElementsAlreadyOnTheDiagrams)
    {
        // Now we need to discover associationclass links.  This is equally complicated for the same 
        // reasons as above.  We need to find all associationclasses being dropped and see if 
        // there are any collaborations already on the diagram then visa versa.

        // Discover association class links among items being created on the diagram
        List<IPresentationElement> pCreatedElements = discoverAssociationClassLinks(pNewElementsBeingCreated);
        ETList<IPresentationElement> retVal = new ETArrayList<IPresentationElement>();
        if (pCreatedElements != null && retVal != null)
        {
            retVal.addAll(pCreatedElements);
        }

        if (pCreatedElements != null)
        {
            pCreatedElements.clear();

        // Now see if any of the elements being created are related to elements already on the diagram.
        }

        return retVal;
    }



    /**
     * Discovers PartFacade Links on the collaboration diagram
     *
     * @param pDiscoverOnTheseElements [in] The elements that should be used to search for partfacade link relations.
     * @param pPresentationElements [out,retval] The created presentation elements
     */
    public List<IPresentationElement> discoverPartFacadeLinks(Collection<IElement> pDiscoverOnTheseElements) {

        Collection<IElement> foundElements = pDiscoverOnTheseElements;
        if((foundElements == null) && (foundElements.size() == 0))
        {
            foundElements = getAllNodeModelElements();
        }

        // Get a list of all the IComments

        List<ICollaboration> pCollaborations = new ArrayList<ICollaboration>();

        // Create a list of just comments
        for (IElement curElement : foundElements)
        {
            if (curElement instanceof ICollaboration)
            {
                ICollaboration pattern = (ICollaboration)curElement;
                if (!pCollaborations.contains(pattern))
                {
                    pCollaborations.add(pattern);
                }
            }
        }

        // Now go over the list of elements and see if any of them are part facades that 
        // have a collaboration on the diagram
        List<IPresentationElement> retVal = new ArrayList<IPresentationElement>();
        
        for (IElement pElement : foundElements)
        {
            if (pElement instanceof IParameterableElement) {
                IParameterableElement pPartFacade = (IParameterableElement) pElement;
                
                // Go over the list of comments and see if any of them annotate
                // this guy
                for (ICollaboration pCollaboration : pCollaborations)
                {
                    if (pCollaboration != null)
                    {
                        if (pCollaboration.getIsTemplateParameter(pPartFacade)) {
                            IPresentationElement pCreatedPE = createPartFacadeLink(pPartFacade, pCollaboration);
                            
                            if (pCreatedPE != null)
                            {
                                retVal.add(pCreatedPE);
                            }
                        }
                    }
                }
            }
        }

        return retVal;
    }

    
    private IPresentationElement createPartFacadeLink(IParameterableElement pParameterableElement, 
                                                      ICollaboration pCollaboration)
    {
        if (pParameterableElement == null || pCollaboration == null)
        {
            return null;
        }
        
        List<IPresentationElement> pStartPEs;
        List<IPresentationElement> pEndPEs;

        // See if they both have presentation elements on this diagram
        pStartPEs = getNodesOnScene(pParameterableElement, scene);
        pEndPEs = getNodesOnScene(pCollaboration, scene);

        // Now see how many presentation items we have
        long numStartPEs = pStartPEs != null ? pStartPEs.size() : 0;
        long numEndPEs = pEndPEs != null ? pEndPEs.size() : 0;

        IPresentationElement retVal = null;
        
        // We have presentation elements on both the general and specific
        if (numStartPEs > 0 && numEndPEs > 0 && numStartPEs == 1 && numEndPEs == 1)
        {
            IPresentationElement pFromPresentationElement = pStartPEs.get(0);
            IPresentationElement pToPresentationElement = pEndPEs.get(0);
            if (linkAlreadyExists(pParameterableElement, pFromPresentationElement, pToPresentationElement) == false && 
                linkIsValid("PartFacadeEdge", pFromPresentationElement, pToPresentationElement) == true)
            {
                Widget nodeWidget = scene.findWidget(pFromPresentationElement);
                if(!(nodeWidget.getParentWidget() instanceof ContainerWidget))
                {            
                    retVal = createConnection(pFromPresentationElement, 
                                              pFromPresentationElement, 
                                              pToPresentationElement,
                                              "PartFacadeEdge");
                }
            }
        }
        
        return retVal;
    }



    /**
     * Discover part facade links when the user drops onto the diagram
     *
     * @param pNewElementsBeingCreated [in] The elements being created dropped onto the diagram
     * @param pElementsAlreadyOnTheDiagrams [in] The elements already on the diagram
     * @param pPresentationElements [out,retval] The created presentation elements
     */
    public List<IPresentationElement> discoverPartFacadeLinks(Collection<IElement> pNewElementsBeingCreated, 
                                                              Collection<IElement> pElementsAlreadyOnTheDiagrams) 
    {
        try {
            // Now we need to discover part facade links.  This is equally complicated for the same reasons as above.
            // We need to find all part facade being dropped and see if there are any collaborations already on 
            // the diagram then visa versa.
            List<IPresentationElement> pCreatedElements = discoverPartFacadeLinks(pNewElementsBeingCreated);
            List<IPresentationElement> pPresentationElements = new ArrayList<IPresentationElement>();

            // Discover partfacade links among items being created on the diagram
            if (pCreatedElements != null && pPresentationElements != null) {
                pPresentationElements.addAll(pCreatedElements);
            }
            pCreatedElements.clear();

            // Now see if any of the elements being created are related to elements already on the diagram.  The
            // easiest way to do this is get a list of all IPartFacade's being dropped and those already on the diagram.
            // Then discover using the two sets of elements
            List<IElement> pPartFacadesBeingCreated = new ArrayList<IElement>();
            List<IElement> pPartFacadesAlreadyOnTheDiagram = new ArrayList<IElement>();
            List<IElement> pCollaborationsBeingCreated = new ArrayList<IElement>();
            List<IElement> pCollaborationsAlreadyOnTheDiagram = new ArrayList<IElement>();

            //IteratorT < IElement > iter = new IteratorT < IElement > (pNewElementsBeingCreated);
            
            // Divide the items being dropped into two groups - collaborations and partfacades
            //while (iter.hasNext()) {
            for(IElement pElement : pNewElementsBeingCreated) 
            {
                if (pElement instanceof IPartFacade) {
                    pPartFacadesBeingCreated.add(pElement);
                } else if (pElement instanceof ICollaboration) {
                    pCollaborationsBeingCreated.add(pElement);
                }
            }

            // Divide the items already on the diagram into two groups - collaborations and partfacades
            //if (pElementsAlreadyOnTheDiagrams != null)
            //    iter.reset(pElementsAlreadyOnTheDiagrams);
            //while (iter.hasNext()) {
            for(IElement pElement : pElementsAlreadyOnTheDiagrams)
            {
                if (pElement instanceof IPartFacade) {
                    pPartFacadesAlreadyOnTheDiagram.add(pElement);
                } else if (pElement instanceof ICollaboration) {
                    pCollaborationsAlreadyOnTheDiagram.add(pElement);
                }
            }

            // Now do three calls to search for links
            // pPartFacadesBeingCreated + pNonPartFacadesAlreadyOnTheDiagram
            // pNonPartFacadesBeingCreated + pPartFacadesAlreadyOnTheDiagram
            //
            // The only bug is that partFacade to partFacade links will reappear when both partFacades are already
            // on the diagram.  But the code is much simpler and this use case doesn't pop up.
            long partFacadesBeingCreatedCount = pPartFacadesBeingCreated.size();
            long collaborationsBeingCreatedCount = pCollaborationsBeingCreated.size();
            long partFacadesAlreadyOnTheDiagramCount = pPartFacadesAlreadyOnTheDiagram.size();
            long collaborationsAlreadyOnTheDiagramCount = pCollaborationsAlreadyOnTheDiagram.size();

            pCreatedElements.clear();
            // 1. pPartFacadesBeingCreated + pNonPartFacadesAlreadyOnTheDiagram
            if (partFacadesBeingCreatedCount > 0 && collaborationsAlreadyOnTheDiagramCount > 0) {
                List<IElement> pNewList = new ArrayList<IElement>();
                
                pNewList.addAll(pPartFacadesBeingCreated);
                pNewList.addAll(pCollaborationsAlreadyOnTheDiagram);

                pCreatedElements = discoverPartFacadeLinks(pNewList);
                if (pCreatedElements != null && pPresentationElements != null) {
                    pPresentationElements.addAll(pCreatedElements);
                }
                pCreatedElements.clear();
                pNewList.clear();
            }

            // 2. pNonPartFacadesBeingCreated + pPartFacadesAlreadyOnTheDiagram
            if (collaborationsBeingCreatedCount > 0 && partFacadesAlreadyOnTheDiagramCount > 0) {
                List<IElement> pNewList = new ArrayList<IElement>();

                pNewList.addAll(pCollaborationsBeingCreated);
                pNewList.addAll(pPartFacadesAlreadyOnTheDiagram);

                pCreatedElements = discoverPartFacadeLinks(pNewList);
                if (pCreatedElements != null && pPresentationElements != null) {
                    pPresentationElements.addAll(pCreatedElements);
                }
                pCreatedElements.clear();
                pNewList.clear();
            }
            return pPresentationElements != null && pPresentationElements.size() > 0 ? pPresentationElements : null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
