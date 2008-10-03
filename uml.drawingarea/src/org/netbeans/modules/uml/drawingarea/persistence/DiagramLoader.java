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
package org.netbeans.modules.uml.drawingarea.persistence;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.drawingarea.UIDiagram;
import org.netbeans.modules.uml.drawingarea.UMLDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.actions.ActionProvider;
import org.netbeans.modules.uml.drawingarea.actions.AfterValidationExecutor;
import org.netbeans.modules.uml.drawingarea.actions.SQDMessageConnectProvider;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngine;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramEdgeReader;
import org.netbeans.modules.uml.drawingarea.persistence.api.GraphNodeReader;
import org.netbeans.modules.uml.drawingarea.persistence.data.ConnectorInfo;
import org.netbeans.modules.uml.drawingarea.persistence.data.EdgeInfo;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.persistence.readers.GraphNodeReaderFactory;
import org.netbeans.modules.uml.drawingarea.support.ProxyPresentationElement;
import org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator.SQDDiagramEngineExtension;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget.UMLWidgetIDString;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author jyothi
 */
class DiagramLoader
{

    private String fileName;
    private XMLInputFactory factory;
    private XMLStreamReader reader = null;
    private DiagramDetails diagInfo = new DiagramDetails();
    private DesignerScene scene;
    private IProject project;
    private IElementLocator locator = new ElementLocator();
    private UMLDiagramTopComponent topComp;
    // list of all presentation elements created
    private List<IPresentationElement> presEltList = new ArrayList<IPresentationElement>();
    //list of all connectors for this graph
    private List<ConnectorInfo> connectorList = new ArrayList<ConnectorInfo>();
    //model element which has connectors eg, class , interface, etc..
    private Stack<String> prevModelElt = new Stack();
    //this is the PE of the graph node which has connectors
    private Stack<String> prevGraphNodePEID = new Stack();
    
    private Stack edgeContainedStack = new Stack();
    private final String CONTAINED = "CONTAINED";
    private EdgeInfo.EndDetails mostRecentEnd = null;
    private Point edgePosition = null;
    private Hashtable<String, String> nodeProperties = new Hashtable();
    private Hashtable<String, String> diagProperties = new Hashtable();
    private List<EdgeInfo> edgeInfoList = new ArrayList<EdgeInfo>();
    private boolean groupEdges = false;
    private Stack<GraphNodeReader> graphNodeReaderStack = new Stack();

    public DiagramLoader(String fileName, UMLDiagramTopComponent tc,
                         boolean groupEdges)
    {
        this.fileName = fileName;
        this.topComp = tc;
        this.groupEdges = groupEdges;
    }

    public DesignerScene openDiagram()
    {
        boolean success = initialize();
        if (success)
        {
            readXML();
        }        
        return scene;
    }

    private boolean initialize()
    {
        boolean success = false;
        try
        {
            factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
            FileObject fobj = FileUtil.toFileObject(new File(fileName));
            if (fobj != null && fobj.getSize() > 0)
            {
                InputStream is = fobj.getInputStream();
                reader = factory.createXMLStreamReader(is,"UTF-8");
                success = true;
            }
            else
            {
                success = false;
                System.err.println(" Corrupted diagram file. Cannot open the diagram."+fileName);
            }            
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        catch (FileNotFoundException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch (Exception ex)
            {
                Exceptions.printStackTrace(ex);
            }

        }
        return success;
    }

    private void readXML()
    {
        try
        {
            int event = reader.getEventType();
            while (true)
            {
                switch (event)
                {
                    case XMLStreamConstants.START_DOCUMENT:
                        break;
                    case XMLStreamConstants.START_ELEMENT:
                        handleStartElement();
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        if (reader.isWhiteSpace())
                        {
                            break;
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        handleEndElement();
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
//                        printData();                      
                        reader.close();
                        break;
                    }

                if (!reader.hasNext())
                {
                    break;
                }
                event = reader.next();
            }
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    private void printData()
    {
//        System.out.println(" connector list =  " + connectorList);
//        System.out.println(" pres elt list =  " + presEltList);

    }

    private void handleStartElement()
    {
        if (reader.getName().getLocalPart().equalsIgnoreCase("Diagram"))
        {
            handleDiagram();
            return;
        }
        if (reader.getName().getLocalPart().equalsIgnoreCase("GraphNode"))
        {
            //Push an obj as soon as you begin a graph node
            graphNodeReaderStack.push(null);
            prevModelElt.push(null);
            prevGraphNodePEID.push(null);
            processGraphNode();
            return;
        }
        if (reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.dependencies"))
        {
            processDependencies();
            return;
        }

        if (reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.anchorage"))
        {
            processAnchorage();
            return;
        }

        if (reader.getName().getLocalPart().equalsIgnoreCase("GraphEdge"))
        {
            if ((scene.getDiagram()).getDiagramKind() == IDiagramKind.DK_SEQUENCE_DIAGRAM) {
                processSQDEdge();
            }
            else {
                processGraphEdge();
            }
            
            return;
        }
    }

    //ignore position and size elements for now
    private void ignorePositionSize()
    {

        if (reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.position"))
        {
            //            System.out.println("Ignoring POSITION.. ");
            jumpToClosingTag("GraphElement.position");
            return; //This is needed for the reader to continue reading
        }
        if (reader.getName().getLocalPart().equalsIgnoreCase("GraphNode.size"))
        {
            //            System.out.println("Ignoring SIZE.. ");
            jumpToClosingTag("GraphNode.size");
            return; //This is needed for the reader to continue reading
        }
    }

    private Point getPosition(String endElement)
    {
        Point pt = new Point();
        String values[] = getXMIFieldValues(endElement);
        if (values.length > 0)
        {
            pt.x = Integer.parseInt(values[0]);
            pt.y = Integer.parseInt(values[1]);
        }
        return pt;
    }

    private Dimension getSize()
    {
        Dimension d = new Dimension();
        String values[] = getXMIFieldValues("GraphNode.size");
        if (values.length > 0)
        {
            d.width = Integer.parseInt(values[0]);
            d.height = Integer.parseInt(values[1]);
        }
        return d;
    }

    private void handleDiagram()
    {
        try
        {
            if (reader.getAttributeCount() > 0)
            {
                diagInfo.setDiagramXMIID(reader.getAttributeValue(null, "xmi.id"));
                diagInfo.setDiagramName(reader.getAttributeValue(null, "name"));
                diagInfo.setZoom(reader.getAttributeValue(null, "zoom"));
            }
            while (reader.hasNext())
            {
                if (XMLStreamConstants.START_ELEMENT == reader.next())
                { //we are only intersted in data of particular start elements
                    //ignore position and size elements for now
                    ignorePositionSize();
                    if (reader.getName().getLocalPart().equalsIgnoreCase("DiagramElement.property"))
                    {
                        processDiagProperties(diagInfo);
                    }
                    if (reader.getName().getLocalPart().equalsIgnoreCase("SimpleSemanticModelElement"))
                    {
                        diagInfo.setDiagramTypeName(reader.getAttributeValue(null, "typeinfo"));
                    }
                    //if we encounter contained.. we should exit this method and let others handle the rest
                    if (reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.contained"))
                    {
                        createScene(diagInfo);
                        return;
                    }
                }
            }
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    private Hashtable<String, String> processProperties()
    {
       Hashtable<String, String> tempProps = new Hashtable<String, String>();
        try
        {
            while (reader.hasNext())
            {
                if (XMLStreamConstants.START_ELEMENT == reader.next() && reader.getName().getLocalPart().equalsIgnoreCase("Property"))
                {
                    if (reader.getAttributeCount() > 0)
                    {
                        String key = reader.getAttributeValue(null, "key");
                        String value = reader.getAttributeValue(null, "value");
                        tempProps.put(key, value);
                    }
                }
                else
                {
                    if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase("DiagramElement.property"))
                    {
//                        System.out.println(" hash table = " + tempProps.toString());
                        return tempProps;
                    }
                }
            }            
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        return tempProps;
    }

    private void processDiagProperties(DiagramDetails diagInfo)
    {
        diagProperties = processProperties();
        //process diag properties
        if (diagProperties != null && diagProperties.size() > 0)
        {
            //get projectID
            if (diagProperties.containsKey("netbeans-diagram-projectID"))
            {
                String prjID = diagProperties.get("netbeans-diagram-projectID");
                if (prjID != null && prjID.length() > 0)
                {
                    diagInfo.setDiagramProjectXMIID(prjID);
                }
            }
            //get namespace
            if (diagProperties.containsKey("netbeans-diagram-namespace"))
            {
                String namespace = diagProperties.get("netbeans-diagram-namespace");
                if (namespace != null && namespace.length() > 0)
                {
                    diagInfo.setDiagramNamespaceXMIID(namespace);
                }
            }            
        }
    }

    private void createScene(DiagramDetails diagInfo)
    {
        String name = diagInfo.getDiagramName();
        if (name == null)
        {
            name = "";
        }
        String diaType = diagInfo.getDiagramTypeName();
        if (diaType == null)
        {
            diaType = "";
        }

        String namespace = diagInfo.getDiagramNamespaceXMIID();
        if (namespace == null)
        {
            namespace = "";
        }

        String projectID = diagInfo.getDiagramProjectXMIID();
        if (projectID == null)
        {
            projectID = "";
        }

        project = getProject(projectID);
        IElement element = getElement(project, namespace);

        if (diaType.length() > 0)
        {
            UIDiagram diagram = (UIDiagram) FactoryRetriever.instance().createType("Diagram", null);
            String diagramXmiId = diagInfo.getDiagramXMIID();
            if (diagramXmiId != null && diagramXmiId.trim().length() > 0)
            {
                diagram.setXMIID(diagramXmiId);
            }
            diagram.setName(name);
            if (element instanceof INamespace)
            {
                diagram.setNamespace((INamespace) element);
            } //should get the namespace instance
            diagram.setDiagramKind(diagInfo.getDiagramType());
            scene = new DesignerScene(diagram, this.topComp);
            scene.setEdgesGrouped(groupEdges);

            if (scene.getView() == null)
            {
                scene.createView();
            }
            scene.setZoomFactor(Double.parseDouble(diagInfo.getZoom()));
        }
    }

    public IProject getProject(String xmiID)
    {
        IApplication app = ProductHelper.getApplication();
        project = (app != null) ? app.getProjectByID(xmiID) : null;
        return project;
    }

    private IElement getElement(IProject project, String sModelElementID)
    {
        IElement element = null;
        if (project != null)
        {
            element = locator.findElementByID(project, sModelElementID);
        }
        return element;
    }

    private void handleEndElement()
    {
        if (reader.getName().getLocalPart().equalsIgnoreCase("GraphNode"))
        {
            endGraphNode();
        }
        else if (reader.getName().getLocalPart().equalsIgnoreCase("Diagram"))
        {
            //FOR SQD: we have read thru all the edges, now arrange them and create them
            if ((scene.getDiagram()).getDiagramKind() == IDiagramKind.DK_SEQUENCE_DIAGRAM) {

                new AfterValidationExecutor(new ActionProvider() {
                    public void perfomeAction() {
                        PersistenceUtil.setDiagramLoading(true);//main thread already consider loading is done, so reset flag here
                        try
                        {
                            createSQDMessages();
                        }
                        finally
                        {
                            PersistenceUtil.setDiagramLoading(false);
                        }
                   }
                },scene);
                scene.validate();
            }
        }
    }

    private void jumpToClosingTag(String tag)
    {
        try
        {
            while (reader.hasNext())
            {
                if (XMLStreamConstants.END_ELEMENT == reader.next() && reader.getName().getLocalPart().equalsIgnoreCase(tag))
                {
                    return;
                }
            }
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    private void processGraphNode()
    {
        try
        {
            NodeInfo nodeInfo = new NodeInfo();
            GraphNodeReader gnReader = null;
            nodeInfo.setProject(project);
            nodeInfo.setPEID(reader.getAttributeValue(null, "xmi.id"));
            while (reader.hasNext())
            {
                if (XMLStreamConstants.START_ELEMENT == reader.next())
                {
                    //we are only intersted in data of particular start elements
                    if (reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.position"))
                    {
                        //nodePosition = getPosition("GraphElement.position");
                        nodeInfo.setPosition(getPosition("GraphElement.position"));
                    }
                    else if (reader.getName().getLocalPart().equalsIgnoreCase("GraphNode.size"))
                    {
                        //size = getSize();
                        nodeInfo.setSize(getSize());
                    }
                    else if (reader.getName().getLocalPart().equalsIgnoreCase("DiagramElement.property"))
                    {
                        nodeProperties = processProperties();
                        nodeInfo.setProperties(nodeProperties);
                    }
                    else if (reader.getName().getLocalPart().equalsIgnoreCase("Uml2SemanticModelBridge.element"))
                    {
                        reader.nextTag();
                        //get the  xmi.idref
                        nodeInfo.setMEID(reader.getAttributeValue(null, "xmi.idref"));
                        //Based on the meid, decide which reader should be initialized..
                        gnReader = GraphNodeReaderFactory.getReader(nodeInfo);    
                        if (gnReader != null)
                        {
                            gnReader.initializeReader(scene,nodeInfo);
                        }

                    } //if we encounter contained.. we should exit this method and let others handle the rest
                    else if (reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.contained"))
                    {
                        graphNodeReaderStack.pop(); //remove null        
                        if (graphNodeReaderStack != null)
                        {
                            if (graphNodeReaderStack.size() == 0 && gnReader != null)
                            {
                                gnReader.processGraphNode(null, nodeInfo);
                            }
                            else if (graphNodeReaderStack.size() > 0 && gnReader != null)
                            {
                                gnReader.processGraphNode(graphNodeReaderStack.peek(), nodeInfo);
                            }                            
                        }                                             
                        graphNodeReaderStack.push(gnReader); 
                        if (nodeInfo.getPresentationElement() != null)
                        {
                            //add this PE to the presLIst
                            presEltList.add(nodeInfo.getPresentationElement());
                            //if there is a widget attached to this nodeInfo,
                            //store it until you come across anchorage
                            if (scene.findWidget(nodeInfo.getPresentationElement()) != null)
                            {
                                prevModelElt.pop(); //remove null
                                prevGraphNodePEID.pop(); //remove null
                                prevModelElt.push(nodeInfo.getMEID()); //stored for anchorage ref
                                prevGraphNodePEID.push(nodeInfo.getPEID()); // stored for anchorage ref
                            }
                        }
                    
//                        addNodeToScene(nodeInfo);
                        //clear the nodeProperties hashtable
                        nodeProperties.clear();
                        return;
                    }
//                    else if (reader.getName().getLocalPart().equalsIgnoreCase("SimpleSemanticModelElement"))
//                    {
//                        String typeInfo = reader.getAttributeValue(null, "typeinfo");
//                        if (typeInfo.length() > 0)
//                        {
//                            if (gnReader == null)
//                                gnReader = graphNodeReaderStack.peek();
//                            if(gnReader != null)
//                            {
//                                gnReader.processGraphNode(graphNodeReaderStack.peek(), nodeInfo);
//                            }                            
//                        }
//                    }           
                }
                else if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase("GraphNode"))
                {
                    endGraphNode();
                    //we have reached the end of anchors list                    
                    return;
                }
            }
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    private void endGraphNode()
    {
        prevModelElt.pop();
        prevGraphNodePEID.pop();
        
        GraphNodeReader gnR = graphNodeReaderStack.pop();
        if (gnR != null)
            gnR.finalizeReader();
    }

    private String[] getXMIFieldValues(String endElement)
    {
        String values[] = new String[2];
        int i = 0;
        try
        {
            while (reader.hasNext())
            {
                if (XMLStreamConstants.START_ELEMENT == reader.next() && reader.getName().getLocalPart().equalsIgnoreCase("XMI.field"))
                {
                    values[i++] = reader.getElementText();
                }
                else
                {
                    if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase(endElement))
                    {
                        return values;
                    }
                }
            }
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        return values;
    }

    private void processAnchorage()
    {
        try
        {
            ConnectorInfo connDet = null;
            while (reader.hasNext())
            {
                if (XMLStreamConstants.START_ELEMENT == reader.next())
                {
                    if (reader.getName().getLocalPart().equalsIgnoreCase("GraphConnector")) {
                        connDet = new ConnectorInfo(); //create new conn det object
                        connDet.setConnectorID(reader.getAttributeValue(null, "xmi.id"));
                        
                        if (!prevModelElt.empty()) {
                            connDet.setNodeMEID(prevModelElt.peek());
                        }
                        if (!prevGraphNodePEID.empty()) {
                            connDet.setNodePEID(prevGraphNodePEID.peek());
                        }
                    }

                    if (reader.getName().getLocalPart().equalsIgnoreCase("GraphConnector.position")) {
                        connDet.setPosition(getPosition("GraphConnector.position"));
                    }

                    if (reader.getName().getLocalPart().equalsIgnoreCase("GraphEdge")) {
                        connDet.setEdgeID(reader.getAttributeValue(null, "xmi.idref"));
                    }
                }
                else
                {
                    if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase("GraphConnector"))
                    {
                        //add the connectorDetails object to the connectorList
                        connectorList.add(connDet);
                    }
                    else
                    {
                        if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.anchorage"))
                        {
                            //we have reached the end of anchors list     
                            //pop both stacks if they have been used above
//                            if (connDet != null && connDet.getNodeMEID().trim().length() > 0) {
//                                prevModelElt.pop();
//                                prevGraphNodePEID.pop();
//                            }
                            return;
                        }
                    }
                }
            }

        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
        
    private void processDependencies()
    {
        try
        {
            Point position = null;
            Dimension size = null;
            String peid = reader.getAttributeValue(null, "xmi.id");
            
            GraphNodeReader nodeReader = graphNodeReaderStack.peek();
            NodeInfo parentNodeInfo = null;
            if(nodeReader != null)
            {
                parentNodeInfo = nodeReader.getNodeInfo();
            }
            
            if (parentNodeInfo == null)
                return;
            while (reader.hasNext())
            {
                if (XMLStreamConstants.START_ELEMENT == reader.next())
                {
                    //we are only intersted in data of particular start elements
                    if (reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.position"))
                    {
                        position = getPosition("GraphElement.position");
                    } 
                    else if (reader.getName().getLocalPart().equalsIgnoreCase("GraphNode.size"))
                    {
                        size = getSize();
                    } 
                    else if (reader.getName().getLocalPart().equalsIgnoreCase("DiagramElement.property"))
                    {
                    } 
                    else if (reader.getName().getLocalPart().equalsIgnoreCase("SimpleSemanticModelElement"))
                    {
                        String typeInfo = reader.getAttributeValue(null, "typeinfo");
                        if (typeInfo.length() > 0)
                        {
                            NodeInfo.NodeLabel nLabel = new NodeInfo.NodeLabel();
                            nLabel.setLabel(typeInfo);
                            nLabel.setPosition(position);
                            nLabel.setSize(size);    
                            nLabel.setPEID(peid);
                            if (parentNodeInfo != null)
                            {                                
                                parentNodeInfo.addNodeLabel(nLabel);
                            }
                        }
                    }                     
                }                   
                else if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase("GraphNode"))
                {
                    position = null; size = null; peid = null;
                }
                else if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.dependencies"))
                {
                    //we are done with nodelabels..
                    graphNodeReaderStack.peek().processDependencies();
                    return;
                }
                else if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.contained"))
                {
                    //this should never happen.. if it does.. just be safe
                    return;
                }
            }
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    

    //Graph Edge handling..
    private void processGraphEdge()
    {
        try
        {
            EdgeInfo edgeReader = new EdgeInfo();
            Widget connWidget = null;
            Hashtable<String, String> props = new Hashtable();

            edgeReader.setPEID(reader.getAttributeValue(null, "xmi.id"));
            while (reader.hasNext())
            {
                if (XMLStreamConstants.START_ELEMENT == reader.next())
                {
                    //we are only intersted in data of particular start elements
                    if (reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.position"))
                    {
                        edgePosition = getPosition("GraphElement.position");
                    }
                    if (reader.getName().getLocalPart().equalsIgnoreCase("GraphEdge.waypoints"))
                    {
                        edgeReader.setWayPoints(processWayPoints());
                    }
                    if (reader.getName().getLocalPart().equalsIgnoreCase("Uml2SemanticModelBridge"))
                    {
                        edgeReader.setSemanticModelBridgePresentation(reader.getAttributeValue(null, "presentation"));
                    }
                    if (reader.getName().getLocalPart().equalsIgnoreCase("DiagramElement.property"))
                    {
                        props = processProperties();
                        edgeReader.setProperties(props);
                    }                    
                    if (reader.getName().getLocalPart().equalsIgnoreCase("Uml2SemanticModelBridge.element"))
                    {
                        reader.nextTag();
                        //get the  xmi.idref
                        edgeReader.setMEID(reader.getAttributeValue(null, "xmi.idref"));
                        connWidget = addEdgeToScene(edgeReader);

                    }
                    if (reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.contained"))
                    {
                        edgeContainedStack.push(CONTAINED);
                        handleGraphEdgeContainedElements(edgeReader);
                    }
                    if (reader.getName().getLocalPart().equalsIgnoreCase("GraphEdge.anchor"))
                    {
                        String[] edgeConnectors = processGraphEdgeAnchor();
                        if (edgeConnectors.length == 2)
                        {
                            // we have two ids - src connector & target connector
                            edgeReader.setSourcePE(findNode(edgeConnectors[0])); //We know that the first conn is src and the second is target
                            edgeReader.setTargetPE(findNode(edgeConnectors[1])); //target
                        }
                    }
                }
                else
                {
                    if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase("GraphEdge"))
                    {
                        //we have reached the end of graph edge 
                        if (connWidget != null && connWidget instanceof UMLEdgeWidget)
                        {
                            ((UMLEdgeWidget) connWidget).load(edgeReader);
                        }
                        return;
                    }
                }
            }
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    private Widget addEdgeToScene(EdgeInfo edgeReader)
    {
        Widget connWidget = null;
        IPresentationElement pE = null;
        IPresentationElement proxyPE = null;

        IElement elt = getElement(project, edgeReader.getMEID());
        if (elt == null)
        {
            //there is nothing to add.. so return..
            return null;
        }
        pE = elt.getPresentationElementById(edgeReader.getPEID());
        if (pE == null)
        {
            pE = Util.createNodePresentationElement();
        } 
//            pE.setXMIID(PEID);
        pE.addSubject(elt);
        
        Hashtable edgeProps = edgeReader.getProperties();
        if (edgeProps != null && edgeProps.size() > 0)
        {
            if (edgeProps.containsKey(UMLEdgeWidget.PROXY_PRESENTATION_ELEMENT)) 
            {
                String proxyType = edgeReader.getSemanticModelBridgePresentation();
                if (proxyType.trim().length() > 0 && !proxyType.equalsIgnoreCase("")) 
                {
                    proxyPE = new ProxyPresentationElement(pE, proxyType);
                }
            }
        }
        if (proxyPE != null)
        {
            connWidget = scene.addEdge(proxyPE);
        } 
        else
        {
            connWidget = scene.addEdge(pE);
        }

        return connWidget;
    }

    private List processWayPoints()
    {
        List wayPoints = new ArrayList<Point>();
        try
        {
            //iterate thru all way points here..
            while (reader.hasNext())
            {
                if (XMLStreamConstants.START_ELEMENT == reader.next() && reader.getName().getLocalPart().equalsIgnoreCase("XMI.field"))
                {
                    Point wayPt = new Point();
                    String[] values = getXMIFieldValues("XMI.field");
                    if (values.length > 0)
                    {
                        wayPt.x = Integer.parseInt(values[0]);
                        wayPt.y = Integer.parseInt(values[1]);
                    }
                    wayPoints.add(wayPt);
                }
                if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase("GraphEdge.waypoints"))
                {
                    //we have reached the end of graph edge
                    return wayPoints;
                }
            }
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        return wayPoints;
    }

    private String[] processGraphEdgeAnchor()
    {
        String values[] = new String[2];
        int i = 0;
        try
        {
            while (reader.hasNext())
            {
                if (XMLStreamConstants.START_ELEMENT == reader.next() && reader.getName().getLocalPart().equalsIgnoreCase("GraphConnector"))
                {
                    values[i++] = reader.getAttributeValue(null, "xmi.idref");
                }
                if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase("GraphEdge.anchor"))
                {
                    return values;
                }
            }
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        return values;
    }

    private IPresentationElement findNode(String connID)
    {
        String nodeID = "";
        for (ConnectorInfo conn : connectorList)
        {
            if ((connID != null) && (connID.equalsIgnoreCase(conn.getConnectorID())))
            {
                nodeID = conn.getNodePEID();
                break;
            }
        }
        
        if (connID != null && nodeID != null && !nodeID.equals(""))
        {
            for (IPresentationElement pE : presEltList)
            {
                if (nodeID.equalsIgnoreCase(pE.getXMIID()))
                {
                    return pE;
                }
            }
        }
        return null;
    }

    //We know we come here only from handleGraphEdge when we have contained elements in a graphedge
    private void handleGraphEdgeContainedElements(EdgeInfo edgeReader)
    {
        try
        {
            //handle contained graph nodes of the edge
            while (reader.hasNext())
            {
                if (XMLStreamConstants.START_ELEMENT == reader.next())
                {
                    //we are only intersted in data of particular start elements
                    if (reader.getName().getLocalPart().equalsIgnoreCase("GraphNode"))
                    {
                        edgeReader.setHasContainedElements(true);
                        NodeInfo nodeInfo = new NodeInfo();
                        nodeInfo.setPEID(reader.getAttributeValue(null, "xmi.id"));
                        processGraphNodeInsideEdge(edgeReader);
                    }
                }
                else if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.contained"))
                {
                    edgeContainedStack.pop();
                    mostRecentEnd = null;
                    if (edgeContainedStack.isEmpty())
                    {
                        return;
                    }
                }
            }
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
     private void processGraphNodeInsideEdge(EdgeInfo edgeReader)
    {
        try
        {           
            NodeInfo nodeInfo = new NodeInfo();
            Hashtable<String, String> props = new Hashtable();
            nodeInfo.setPEID(reader.getAttributeValue(null, "xmi.id"));
            while (reader.hasNext())
            {
                if (XMLStreamConstants.START_ELEMENT == reader.next())
                {
                    //we are only intersted in data of particular start elements
                    if (reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.position"))
                    {
                        //nodePosition = getPosition("GraphElement.position");
                        nodeInfo.setPosition(getPosition("GraphElement.position"));
                    }
                    else if (reader.getName().getLocalPart().equalsIgnoreCase("GraphNode.size"))
                    {
                        //size = getSize();
                        nodeInfo.setSize(getSize());
                    }
                    else if (reader.getName().getLocalPart().equalsIgnoreCase("DiagramElement.property"))
                    {
                        props = processProperties();
                        nodeInfo.setProperties(props);
                    }
                    else if (reader.getName().getLocalPart().equalsIgnoreCase("SimpleSemanticModelElement"))
                    {
                        String typeInfo = reader.getAttributeValue(null, "typeinfo");
                        if (typeInfo.length() > 0)
                        {
                            EdgeInfo.EdgeLabel eLabel = edgeReader.new EdgeLabel();
                            eLabel.setLabel(typeInfo);
                            eLabel.setPosition(nodeInfo.getPosition());
                            eLabel.setSize(nodeInfo.getSize());
                            eLabel.setLabelProperties(nodeInfo.getProperties());
                            if (mostRecentEnd == null)
                            {
                                edgeReader.getLabels().add(eLabel);
                            }
                            else
                            {
                                mostRecentEnd.getEndEdgeLabels().add(eLabel);
                            }
                        }
                    }
                    else if (reader.getName().getLocalPart().equalsIgnoreCase("Uml2SemanticModelBridge.element"))
                    {
                        reader.nextTag();
                        EdgeInfo.EndDetails assocEnd = edgeReader.new EndDetails();
                        //get the  xmi.idref
                        assocEnd.setID(reader.getAttributeValue(null, "xmi.idref"));
                        edgeReader.getEnds().add(assocEnd);
                        mostRecentEnd = assocEnd;
                    }
                    else if (reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.contained"))
                    {
                        edgeContainedStack.push(CONTAINED);
                        mostRecentEnd = mostRecentEnd;
                    }
                }
                else if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.contained"))
                {
                    edgeContainedStack.pop();
                    mostRecentEnd = null;
                }
                else if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase("GraphNode"))
                {
                    return;
                }
            }
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
     
     private void processSQDEdge() {
         try
        {
            EdgeInfo edgeReader = new EdgeInfo();
            edgeReader.setPEID(reader.getAttributeValue(null, "xmi.id"));
            while (reader.hasNext())
            {
                if (XMLStreamConstants.START_ELEMENT == reader.next())
                {
                    //we are only intersted in data of particular start elements
                    if (reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.position"))
                    {
                        edgePosition = getPosition("GraphElement.position");
                    }
                    else if (reader.getName().getLocalPart().equalsIgnoreCase("GraphEdge.waypoints"))
                    {
                        edgeReader.setWayPoints(processWayPoints());
                    }
                    else if (reader.getName().getLocalPart().equalsIgnoreCase("Uml2SemanticModelBridge.element"))
                    {
                        reader.nextTag();
                        //get the  xmi.idref
                        edgeReader.setMEID(reader.getAttributeValue(null, "xmi.idref"));
                        addEdgeToList(edgeReader);

                    }
                    else if (reader.getName().getLocalPart().equalsIgnoreCase("GraphElement.contained"))
                    {
                        edgeContainedStack.push(CONTAINED);
                        handleGraphEdgeContainedElements(edgeReader);
                    }
                    else if (reader.getName().getLocalPart().equalsIgnoreCase("GraphEdge.anchor"))
                    {
                        String[] edgeConnectors = processGraphEdgeAnchor();
                        if (edgeConnectors.length == 2)
                        {
                            // we have two ids - src connector & target connector
                            if(edgeConnectors[0]!=null)edgeReader.setSourcePE(findNode(edgeConnectors[0])); //We know that the first conn is src and the second is target
                            else
                            {
//                                System.out.println("WARNING, EDGE WITTH NULL CONNECTOR");
                                edgeInfoList.remove(edgeReader);
                                return;
                            }
                            if(edgeConnectors[1]!=null)edgeReader.setTargetPE(findNode(edgeConnectors[1])); //target
                            else
                            {
//                                System.out.println("WARNING, EDGE WITTH NULL CONNECTOR");
                                edgeInfoList.remove(edgeReader);
                                return;
                            }
                        }
                    }
                }
                else
                {
                    if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase("GraphEdge"))
                    {
//                        System.out.println(" End Graph Edge !!");                        
                        return;
                    }
                }
            }
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    private void addEdgeToList(EdgeInfo edgeReader) {
        edgeInfoList.add(edgeReader);
    }

    private void createSQDMessages() {
        DiagramEngine engine=scene.getEngine();
        SQDDiagramEngineExtension sqdengine=(SQDDiagramEngineExtension) engine;
        SQDMessageConnectProvider provider = null;
        
        //get/set all the properties for sqd
        setSQDProperties(engine);       
        
        //First get an ordered list of all messages in this interaction
        List<IMessage> messageList = null;
        INamespace namespace = scene.getDiagram().getNamespace();
        if (namespace != null) {
            IInteraction interaction = (IInteraction) namespace;
            messageList = interaction.getMessages();
        }
        //now get the list of all edgeReaders and sort them based on y-axis
        Collections.sort(edgeInfoList, Y_AXIS_COMPARATOR);
        //System.out.println("  AFTER SORT !!!!!"+edgeInfoList);
        
        //now for each message in the edgeInfoList, find it in the messageList,
        // and create it.. since sequence of creation is very important.

        
        for (Iterator<EdgeInfo> it = edgeInfoList.iterator(); it.hasNext();) {
            EdgeInfo edgeInfo = it.next();
//            System.out.println("  !!!  edgeInfo = "+edgeInfo);
            Widget sourceWidget = scene.findWidget(edgeInfo.getSourcePE());
            Widget targetWidget = scene.findWidget(edgeInfo.getTargetPE());
            Point startingPoint = (Point) edgeInfo.getWayPoints().get(0);
            Point endingPoint = (Point) edgeInfo.getWayPoints().get(edgeInfo.getWayPoints().size() - 1);
            String edgeMEID = edgeInfo.getMEID();
            //now get the matching IMessage from messageList
            IMessage message = findMessage(messageList, edgeMEID);
            if (message != null)
            {
                List retVal = new ArrayList();
                EdgeInfo returnMsgInfo = null;
                if (message.getKind() == IMessageKind.MK_SYNCHRONOUS)
                {
                    Point resultStartingPoint, resultEndingPoint;
//                    System.out.println(" SYNCHRONOUS message... ");
                    //now find the result message for this call message
                    IMessage returnMsg = findReturnMessage(messageList, message);
                    if (returnMsg != null)
                    {
                        //create the sync msg and delete it from the edgeInfoList
                        provider = sqdengine.getConnectProvider(message, returnMsg);
                        //get the returnMsg info from the edgeInfoList
                        returnMsgInfo = findEdgeInfoOfReturnMsg(returnMsg);
                        if (returnMsgInfo != null) {
                            resultStartingPoint = (Point) returnMsgInfo.getWayPoints().get(0);
                            resultEndingPoint = (Point) returnMsgInfo.getWayPoints().get(returnMsgInfo.getWayPoints().size() - 1);
                            
                            retVal = (List)provider.createSynchConnection(sourceWidget, targetWidget, startingPoint, endingPoint, resultStartingPoint, resultEndingPoint);
//                            System.out.println("syncMsg"+retVal);
                        }
                    }
                } 
                else if ((message.getKind() == IMessageKind.MK_ASYNCHRONOUS) 
                        || (message.getKind() == IMessageKind.MK_CREATE))
                {
//                    System.out.println(" ASYNC message ..");
                    provider = sqdengine.getConnectProvider(message, null);
                    retVal = (List)provider.createConnection(sourceWidget, targetWidget, startingPoint, endingPoint);                    
//                    System.out.println("Async "+retVal);
                } 
                else if (message.getKind() == IMessageKind.MK_RESULT) {
                    //do nothing for now.. TODO delete it.
//                    System.out.println(" Result Message ! ");
                }
               else
                {
//                    System.out.println(" short method..");
                    provider = sqdengine.getConnectProvider(message, null);
                    provider.createConnection(sourceWidget, targetWidget);
                }
                scene.validate();
                for (int i = 0; i < retVal.size(); i++)
                {
                    Object object = retVal.get(i);
                    if (object instanceof DiagramEdgeReader)
                    {
                        if (object instanceof UMLEdgeWidget && 
                                (((UMLEdgeWidget)object).getWidgetID()).equalsIgnoreCase(UMLWidgetIDString.RESULTMESSAGECONNECTIONWIDGET.toString())) {
                            ((DiagramEdgeReader)object).load(returnMsgInfo);
                        }
                        else {
                            ((DiagramEdgeReader)object).load(edgeInfo);
                        }                        
                    }
                    
                }
            }
            else if (message == null && edgeInfo != null)  // we may be a comment edge..
            {
//                System.out.println("comment edge .. I cannot think of any other edge type here..");
                Widget connWidget = addEdgeToScene(edgeInfo);
                if (connWidget != null && connWidget instanceof UMLEdgeWidget)
                {
                    ((UMLEdgeWidget) connWidget).load(edgeInfo);
                }
            }
        }
    }
    
    private IMessage findMessage(List<IMessage> messageList, String edgeMEID) {
        IMessage message = null;
        for (Iterator<IMessage> it1 = messageList.iterator(); it1.hasNext();) {
                IMessage iMessage = it1.next();
                if (iMessage.getXMIID().equalsIgnoreCase(edgeMEID)) {
                    message = iMessage;
                }
            }
        return message;
    }
   
    private IMessage findReturnMessage(List<IMessage> messageList, IMessage callMessage)
    {
        int callMsgIndex = messageList.indexOf(callMessage);
        for (int i = callMsgIndex; i < messageList.size(); i++)
        {
            IMessage iMessage = (IMessage)messageList.get(i);
            if (iMessage.getKind() == IMessageKind.MK_RESULT && iMessage.getSendingMessage().equals(callMessage)) {
                return iMessage;
            }
        }
        return null;
    }

    private EdgeInfo findEdgeInfoOfReturnMsg(IMessage returnMsg)
    {
        EdgeInfo retValue = null;
        
        for (Iterator<EdgeInfo> it = edgeInfoList.iterator(); it.hasNext();)
        {
            EdgeInfo eInfo = it.next();
            if (returnMsg != null && returnMsg.getXMIID().equalsIgnoreCase(eInfo.getMEID())) {
                retValue = eInfo;
                //now delete it from the edgeInfoList
//                edgeInfoList.remove(it);
                break;
            }
        }        
        return retValue;           
    }
    
    private void setSQDProperties(DiagramEngine engine) {
        if (engine == null)
            return;

        if (engine != null) 
        {
            if (diagProperties != null && diagProperties.size() > 0)
            {
                //show message numbers?
                String msgNumKey = ((SQDDiagramEngineExtension)engine).SHOW_MESSAGE_NUMBERS;
                if (diagProperties.containsKey(msgNumKey))
                {
                    String showMsgStr = diagProperties.get(msgNumKey);
                    if (showMsgStr != null && showMsgStr.length() > 0)
                    {
                        Boolean showMsg = Boolean.valueOf(showMsgStr);
                        engine.setSettingValue(msgNumKey, showMsg);
                    }
                }
                //show return messages?
                String retMsgKey = ((SQDDiagramEngineExtension)engine).SHOW_RETURN_MESSAGES;
                if (diagProperties.containsKey(retMsgKey))
                {
                    String showRetMsgStr = diagProperties.get(retMsgKey);
                    if (showRetMsgStr != null && showRetMsgStr.length() > 0)
                    {
                        Boolean showRetMsg = Boolean.valueOf(showRetMsgStr);
                        engine.setSettingValue(retMsgKey, showRetMsg);
                    }
                }              
            }
        }
    }

    static final Comparator<EdgeInfo> Y_AXIS_COMPARATOR =
            new Comparator<EdgeInfo>() {

                public int compare(EdgeInfo e1, EdgeInfo e2) {
                    int y1 = ((Point) (e1.getWayPoints().get(0))).y;
                    int y2 = ((Point) (e2.getWayPoints().get(0))).y;
                    return (y1 < y2 ? -1 : (y1 == y2 ? 0 : 1));
                }
            };
}
