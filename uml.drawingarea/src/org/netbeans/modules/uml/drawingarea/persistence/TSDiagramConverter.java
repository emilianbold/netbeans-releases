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

package org.netbeans.modules.uml.drawingarea.persistence;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.TSDiagramDetails;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.Lifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.Message;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.drawingarea.AbstractLabelManager;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.SQDDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.UMLDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.actions.ActionProvider;
import org.netbeans.modules.uml.drawingarea.actions.AfterValidationExecutor;
import org.netbeans.modules.uml.drawingarea.actions.SQDMessageConnectProvider;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngine;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramEdgeReader;
import org.netbeans.modules.uml.drawingarea.persistence.data.EdgeInfo;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo.NodeLabel;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;

import org.netbeans.modules.uml.drawingarea.support.ProxyPresentationElement;
import org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator.SQDDiagramEngineExtension;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget.UMLWidgetIDString;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerNode;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.diagramsupport.IDrawingAreaEventDispatcher;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class TSDiagramConverter
{
    public static final String ELEMENT = "ELEMENT";
    private static final String BENDSPROPERTY = "BENDS61";
    private static final String ORIENTATION = "Orientation";
    private static final String PRESENTATIONELEMENT = "PRESENTATION";
    private static final String SHOWMESSAGETYPE = "ShowMessageType";
    private static final String TSLABELTYPE = "TYPE";
    private static final String LIFELINESHIFTKEY = "LIFELINESHIFT";
    private static final String TSLABELPLACEMENT = "PLACEMENT";
    private final String GRAPHPROPERTY="graphicsType";
    private final String GRAPHICONIC="ETGenericNodeLabelUI";
    private final String GRAPHDEFAULT="ETGenericNodeUI";
    private final String ENGINE="engine";
    private final String PATTERNEDGEENGINE="PartFacadeEdgeDrawEngine";
    private final String PATTERNEDGEPRESENTATION="PartFacadeEdge";
    private final String NESTEDLINKENGINE="NestedLinkDrawEngine";
    private final String NESTEDLINKPRESENTATION="NestedLink";
    private final String NODECONNECTORS="NodeConnectors";
    private final String ASSOCIATIONCLASSCONNECTORENGINE="AssociationClassConnectorDrawEngine";
    private IProxyDiagram proxyDiagram;
    private TSDiagramDetails diagramDetails;
    
    private XMLInputFactory factory;
    private InputStream fisPres;
    private InputStream fisData;
    private XMLStreamReader readerPres;
    private XMLStreamReader readerData;
    
    // coordinate values for converting from TS graph to Meteora graph
    private int minX = Integer.MAX_VALUE;
    private int maxY = Integer.MIN_VALUE;
    private Integer sqdYShift=null;
    
    // data file node id (integer string) -> PEID
    private Map<Integer, String> dataNodeIdMap = new HashMap<Integer, String>(); 
    // data file edge id (integer string) -> PEID
    private Map<String, String> dataEdgeIdMap = new HashMap<String, String>(); 
    private Map<String, ETPairT<String, String>> dataConnIdMap = 
        new HashMap<String, ETPairT<String, String>>(); 
    
    private Map<String,NodeInfo.NodeLabel> peidToNodeLabels=new HashMap<String,NodeInfo.NodeLabel>();
    
    private HashMap<String,HashMap<String,Object>> peidToEdgeLabelMap=new HashMap<String,HashMap<String,Object>>();
    
    private HashMap<String,NodeInfo> associationClasses=new HashMap<String,NodeInfo>();//it's central elements only (circle in 6.1)
    
    // PEID -> NodeInfo
    private Map<String, NodeInfo> presIdNodeInfoMap = 
        new HashMap<String, NodeInfo>(); 

    // PEID -> EdgeInfo
    private Map<String, EdgeInfo> presIdEdgeInfoMap = 
        new HashMap<String, EdgeInfo>(); 
    private ArrayList<EdgeInfo> messagesInfo=new ArrayList<EdgeInfo>();

    private DesignerScene scene;
    private UMLDiagramTopComponent topComponent;

    private IProject project;
    private IElementLocator locator = new ElementLocator();
    
    // list of all presentation elements created
    private List<IPresentationElement> presEltList = 
        new ArrayList<IPresentationElement>();
    private ArrayList<Widget> widgetsList = 
        new ArrayList<Widget>();
    //
    private HashMap<String,ConnectorData> connectors=new HashMap<String,ConnectorData>();
    private Boolean ShowAllReturnMessages;
    private Boolean ShowMessageNumbers;

    private FileObject etldFO;

    private FileObject etlpFO;;
    
    public TSDiagramConverter(IProxyDiagram proxyDiagram)
    {
        this.proxyDiagram = proxyDiagram;
        diagramDetails = (TSDiagramDetails)proxyDiagram.getDiagramDetails();
    }

    public UMLDiagramTopComponent convertDiagram()
    {
        initialize();
        readXMLData();
        normilizeNodesForZero();
        readXMLPres();
        releaseResources();
        //
        createNodesPresentationElements();
        findEdgesElements();
        handleLabelsInfo(peidToEdgeLabelMap);
        handleBendPointsInfo();
        if(diagramDetails.getDiagramType() == IDiagramKind.DK_SEQUENCE_DIAGRAM)
        {
            normalizeSQDDiagram();
        }
        createDiagram();
        return topComponent;
    }

 





    private void initialize()
    {
        try
        {
            project = getProject(diagramDetails.getDiagramProjectXMIID());
            
            factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
            etldFO = FileUtil.toFileObject(new File(diagramDetails.getDiagramFileName()));
        
            etlpFO = FileUtil.findBrother(etldFO, FileExtensions.DIAGRAM_TS_PRESENTATION_EXT_NODOT);
            
            fisData = etldFO.getInputStream();
            readerData = factory.createXMLStreamReader(fisData, "UTF-8");

            fisPres = etlpFO.getInputStream();
            readerPres = factory.createXMLStreamReader(fisPres, "UTF-8");
        }
        
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        
        catch (FileNotFoundException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    
    private void createDiagram()
    {
        PersistenceUtil.setDiagramLoading(true);
        if(diagramDetails.getDiagramType() == IDiagramKind.DK_SEQUENCE_DIAGRAM)
        {
            topComponent = new SQDDiagramTopComponent(
                (INamespace) getElement(getProject(
                    diagramDetails.getDiagramProjectXMIID()), 
                    diagramDetails.getDiagramNamespaceXMIID()), 
                diagramDetails.getDiagramName(), 
                diagramDetails.getDiagramType());
            //
            
        }
        else
        {
            topComponent = new UMLDiagramTopComponent(
                (INamespace) getElement(getProject(
                    diagramDetails.getDiagramProjectXMIID()), 
                    diagramDetails.getDiagramNamespaceXMIID()), 
                diagramDetails.getDiagramName(), 
                diagramDetails.getDiagramType());
        }

        scene = topComponent.getScene();
        DiagramEngine engine = scene.getEngine();
        if(diagramDetails.getDiagramType() == IDiagramKind.DK_SEQUENCE_DIAGRAM)
        {
            engine.setSettingValue(SQDDiagramEngineExtension.SHOW_MESSAGE_NUMBERS, ShowMessageNumbers);
            engine.setSettingValue(SQDDiagramEngineExtension.SHOW_RETURN_MESSAGES, ShowAllReturnMessages);
        }
        //

        Collection<NodeInfo> ninfos = presIdNodeInfoMap.values();
        ArrayList<NodeInfo> ninfoSorted=new ArrayList<NodeInfo>(ninfos);
        Collections.sort(ninfoSorted,new Comparator<NodeInfo>() {
            public int compare(NodeInfo o1, NodeInfo o2) {
                return (Integer)o1.getProperty("NODEID61")-(Integer)o2.getProperty("NODEID61");
            }
        });
        for (NodeInfo ninfo : ninfoSorted)
        {
            addNodeToScene(ninfo);
        }
        addEdgestWithValidationWait();
        scene.revalidate();
        scene.validate();
   }
    
    private void addEdgestWithValidationWait()
    {
        ActionProviderImplOne actProvOne = new ActionProviderImplOne();
        new AfterValidationExecutor(actProvOne, scene);
    }
    
    private class ActionProviderImplOne implements ActionProvider
    {
        public ActionProviderImplOne()
        {
        }

        public void perfomeAction()
        {
            if (diagramDetails.getDiagramType() == IDiagramKind.DK_SEQUENCE_DIAGRAM)
            {
                addEdgesSequenceDiagram();
            }
            else
            {
                addEdgesGeneral();
            }
            processContainmentWithValidationWait();
            scene.revalidate();
            scene.validate();
        }
    }
    
    private void processContainmentWithValidationWait()
    {
        ActionProviderImplTwo actProvTwo = new ActionProviderImplTwo();
        new AfterValidationExecutor(actProvTwo, scene);
    }
    
    private class ActionProviderImplTwo implements ActionProvider
    {
        public ActionProviderImplTwo()
        {
        }

        public void perfomeAction()
        {
            for (Widget w : widgetsList)
            {
                if (w instanceof ContainerNode && w instanceof UMLNodeWidget)
                {
                    ObjectScene scene = (ObjectScene) w.getScene();
                    IPresentationElement pe = (IPresentationElement) scene.findObject(w);
                    if (pe.getFirstSubject() instanceof ICombinedFragment)
                    {
                        continue; //TBD need to solve concurrent modification issue
                    }
                    ContainerNode cont = (ContainerNode) w;
                    cont.getContainer().calculateChildren(false);
                }
            }
            scene.validate();
            scene.getDiagram().setDirty(true);
//                scene.getEngine().getTopComponent().setDiagramDirty(true);
            try
            {
                scene.getDiagram().save();
            }
            catch (IOException ex)
            {
                Exceptions.printStackTrace(ex);
            }
            processArchiveWithValidationWait();
            scene.revalidate();
            scene.validate();
            PersistenceUtil.setDiagramLoading(false);
        }
    }
    
    private void processArchiveWithValidationWait()
    {
        new AfterValidationExecutor(new ActionProvider() {

            public void perfomeAction() {
                archiveTSDiagram();
            }
        }, scene);
    }     
    
    
    private void addEdgesGeneral()
    {
        Collection<EdgeInfo> einfos = presIdEdgeInfoMap.values();
        for (EdgeInfo einfo : einfos)
        {
            Widget connWidget=addEdgeToScene(einfo);
            if (connWidget != null && connWidget instanceof UMLEdgeWidget)
            {
                ((UMLEdgeWidget) connWidget).load(einfo);
            }
        }
    }
    
    private void addEdgesSequenceDiagram()
    {
        Collection<EdgeInfo> einfos = presIdEdgeInfoMap.values();
        for (EdgeInfo einfo : einfos)
        {
            Widget connWidget=addEdgeToScene(einfo);
            if (connWidget != null && connWidget instanceof UMLEdgeWidget)
            {
                ((UMLEdgeWidget) connWidget).load(einfo);
            }
        }
        if(messagesInfo.size()>0)addMessagesToSQD(messagesInfo);
        scene.validate();
    }
    
    private void archiveTSDiagram()
    {
        FileObject projectFO = etldFO.getParent();
        
        try
        {
            FileObject backupFO = 
                FileUtil.createFolder(projectFO, "DiagramBackup");
            FileObject parent=etldFO.getParent();
            String path=etldFO.getPath();
            String path2=etlpFO.getPath();
            String parpath=parent.getPath();
            //if it's reconverting just override previous 6.1 backup
             FileObject destFO1 = backupFO.getFileObject(etldFO.getName(), etldFO.getExt());
             if (destFO1 != null)
             {
                destFO1.delete();
             }             
             FileObject destFO2 = backupFO.getFileObject(etlpFO.getName(), etlpFO.getExt());
             if (destFO2 != null)
             {
                destFO2.delete();
             }             
            //
            FileUtil.moveFile(etldFO, backupFO, etldFO.getName());
            FileUtil.moveFile(etlpFO, backupFO, etlpFO.getName());
            IDrawingAreaEventDispatcher dispatcher = getDispatcher();
            if (dispatcher != null) {
                IEventPayload payload = dispatcher.createPayload("DrawingAreaFileRemoved");
                dispatcher.fireDrawingAreaFileRemoved(diagramDetails.getDiagramFileName(), payload);
                dispatcher.fireDrawingAreaFileRemoved(path2, payload);
            }
            //FileUtil.refreshFor(new File(parpath));
        }
        
        catch (DataObjectNotFoundException ex)
        {
            Exceptions.printStackTrace(ex);
            ex.printStackTrace();
        }

        catch (IOException ex)
        {
            Exceptions.printStackTrace(ex);
            ex.printStackTrace();
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

    private void addNodeToScene(NodeInfo nodeInfo)
    {
        try
        {            
            IPresentationElement presEl = (IPresentationElement) nodeInfo.getProperty(PRESENTATIONELEMENT);
            if(presEl!=null)
            {
                nodeInfo.setModelElement(presEl.getFirstSubject());
                DiagramEngine engine = scene.getEngine();
                Widget widget = null;
                if(presEl!=null && presEl.getFirstSubject() instanceof INamedElement)widget=engine.addWidget(presEl, nodeInfo.getPosition());
                if(widget!=null)
                {
                    //
                    postProcessNode(widget,presEl);
                    //add this PE to the presLIst
                    widgetsList.add(widget);
                    if (widget!=null && widget instanceof UMLNodeWidget)
                    {
                        if(nodeInfo.getModelElement() instanceof IState)
                        {
                            if(nodeInfo.getProperty(ORIENTATION)==null)
                            {
                                //default (null) 
                                nodeInfo.setProperty(ORIENTATION,SeparatorWidget.Orientation.HORIZONTAL.toString());
                            }
                            if(nodeInfo.getProperty("ShowTransitions")==null)nodeInfo.setProperty("ShowTransitions", Boolean.FALSE);
                        }
                        else if(nodeInfo.getModelElement() instanceof IActivityPartition)
                        {
                            if(nodeInfo.getProperty(ORIENTATION)==null)nodeInfo.setProperty(ORIENTATION,SeparatorWidget.Orientation.VERTICAL.toString());
                        }
                        ((UMLNodeWidget) widget).load(nodeInfo);
                        if(nodeInfo.getModelElement() instanceof ICombinedFragment)
                        {
                            ICombinedFragment cfE=(ICombinedFragment) nodeInfo.getModelElement();
                            //new AfterValidationExecutor(new LoadInteractionOperandsProvider((UMLNodeWidget) widget,cfE, nodeInfo.getLabels(), nodeInfo.getDevidersOffests()), scene);
                            new LoadInteractionOperandsProvider((UMLNodeWidget) widget,cfE, nodeInfo.getLabels(), nodeInfo.getDevidersOffests()).perfomeAction();
                        }
                        else if(nodeInfo.getModelElement() instanceof IActivityPartition)
                        {
                            IActivityPartition ap=(IActivityPartition)nodeInfo.getModelElement();
                            {
                                new AfterValidationExecutor(new LoadSubPartitionsProvider((UMLNodeWidget) widget,ap, SeparatorWidget.Orientation.valueOf(nodeInfo.getProperty(ORIENTATION).toString()), nodeInfo.getDevidersOffests()), scene);
                            }
                        }
                        else if(nodeInfo.getModelElement() instanceof IState)
                        {
                            IState state=(IState) nodeInfo.getModelElement();
                            {
                                //composite state
                                new AfterValidationExecutor(new LoadRegionsProvider((UMLNodeWidget) widget,state, SeparatorWidget.Orientation.valueOf(nodeInfo.getProperty(ORIENTATION).toString()), nodeInfo.getDevidersOffests()), scene);
                            }
                        }
                    }
                }
                else
                {
                    //most likely unsupported widgets and it wasn't created
                    //or not named element (for example expression
                    if(scene.isNode(presEl))scene.removeNode(presEl);
                    presEl.getFirstSubject().removePresentationElement(presEl);
                    presEltList.remove(presEl);
                    nodeInfo.getProperties().remove(PRESENTATIONELEMENT);
                }
            }
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean createNodesPresentationElements()
    {
        Collection<NodeInfo> ninfos = presIdNodeInfoMap.values();
        boolean good=true;
        for (NodeInfo ninfo : ninfos)
        {
            good=createPE(ninfo)&&good;
        }        
        return good;
    }
    
    private boolean createPE(NodeInfo nodeInfo)
    {
        //special case, circle at center of assocciation connector isn't supported
        if(associationClasses.get(nodeInfo.getPEID())!=null)return false;
        //
        boolean good=true;
        try
        {            
             IPresentationElement presEl = createPresentationElement(
             nodeInfo.getMEID(), nodeInfo.getPEID());
             good=presEl!=null;
             if(good)
             {
                mapEngineToView(presEl,nodeInfo);
                nodeInfo.setProperty(PRESENTATIONELEMENT, presEl);
                presEltList.add(presEl);
             }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return good;
    }

    private Widget addEdgeToScene(EdgeInfo edgeReader)
    {
        Widget connWidget = null;
        IPresentationElement pE = null;
        IPresentationElement proxyPE = null;
        
        //
        IElement elt = (IElement) edgeReader.getProperty(ELEMENT);
        if (elt == null)
        {
            //there is nothing to add.. so return..
            return null;
        }
             //
        String sourceId=dataConnIdMap.get(edgeReader.getPEID()).getParamOne();
        String targetId=dataConnIdMap.get(edgeReader.getPEID()).getParamTwo();
        IPresentationElement sourcePE=findNode(sourceId);
        IPresentationElement targetPE=findNode(targetId);
            //it may be association class
            if(elt instanceof IAssociationClass)
            {
                //we need to collect info from all three parts of association link from 6.1 diagram
                NodeInfo circleInfo=null;
                if(associationClasses.get(sourceId)!=null)
                {
                    //this size circle
                    circleInfo=associationClasses.get(sourceId);
                }
                else if(associationClasses.get(targetId)!=null)
                {
                    //this side circle
                    circleInfo=associationClasses.get(targetId);
                }
                if(circleInfo!=null)
                {
                    if(circleInfo.getProperty("LINKENDS")==null)
                    {
                        circleInfo.setProperty("LINKENDS", new ArrayList<IPresentationElement>());
                    }
                    if(sourcePE!=null && targetPE==null)
                    {
                        ((ArrayList) circleInfo.getProperty("LINKENDS")).add(sourcePE);
                        if(!(sourcePE.getFirstSubject() instanceof IAssociationClass))
                        {
                            circleInfo.setProperty("ASSOCIATIONCLASSSOURCE",sourcePE);
                        }
                        else circleInfo.setProperty("ASSOCIATIONCLASSASSOCIATIONCLASST",sourcePE);
                    }
                    else if(sourcePE==null && targetPE!=null)
                    {
                        ((ArrayList) circleInfo.getProperty("LINKENDS")).add(targetPE);
                        if(!(targetPE.getFirstSubject() instanceof IAssociationClass))
                        {
                            circleInfo.setProperty("ASSOCIATIONCLASSTARGET",targetPE);
                        }
                        else circleInfo.setProperty("ASSOCIATIONCLASSASSOCIATIONCLASST",targetPE);
                    }
                    else if(sourcePE!=null && targetPE!=null)
                    {
                        //shouldn't happens
//                        System.out.println("****WARNING: both ends of association class link exist");
                    }
                    else
                    {
                        //both null, some element is missed, so association link will not be created
                    }
                    if(((ArrayList) circleInfo.getProperty("LINKENDS")).size()==3)
                    {
                        //collected all  3 parts of link from 6.1
                        sourcePE=(IPresentationElement) circleInfo.getProperty("ASSOCIATIONCLASSSOURCE");
                        targetPE=(IPresentationElement) circleInfo.getProperty("ASSOCIATIONCLASSTARGET");
                    }
                }
            }
        if(sourcePE==null || targetPE==null)
        {
            
            //
            return null;//target or source is missed from model
        }
        
        if(NESTEDLINKENGINE.equals(edgeReader.getProperty(ENGINE)))
        {//nested link need reverse order in new diagram
            IPresentationElement tmp=sourcePE;
            sourcePE=targetPE;
            targetPE=tmp;
        }
        //
        edgeReader.setSourcePE(sourcePE);
        edgeReader.setTargetPE(targetPE);
        if(elt instanceof Message)
        {
            messagesInfo.add(edgeReader);
            //messages will be handled in seprate method
            return null;
        }
        //
        processSemanricPresentation(edgeReader);

            pE = Util.createNodePresentationElement();
    //            pE.setXMIID(PEID);
            pE.addSubject(elt);
            String proxyType = edgeReader.getSemanticModelBridgePresentation();
            if (proxyType.trim().length() > 0 && !proxyType.equalsIgnoreCase(""))
            {
                proxyPE = new ProxyPresentationElement(pE, proxyType);
            }
            IPresentationElement peToUse=(proxyPE != null) ? proxyPE : pE;
            edgeReader.setProperty(PRESENTATIONELEMENT, peToUse);
            connWidget = scene.addEdge(peToUse);
            if(connWidget==null)
            {
                    if(scene.isEdge(peToUse))scene.removeEdge(peToUse);
                    peToUse.getFirstSubject().removePresentationElement(peToUse);
                    edgeReader.getProperties().remove(PRESENTATIONELEMENT);
                    edgeReader.getProperties().remove(ELEMENT);
            }
            else
            {

            }
        return connWidget;
    }


    /**
     * need to be called after general addEfge(where all source/target widgets are determined)
     * also all elements in collection should be messages
     * @return
     */
    private void addMessagesToSQD(ArrayList<EdgeInfo> einfos)
    {
        Collections.sort(einfos,new Comparator<EdgeInfo>()
        {
            public int compare(EdgeInfo o1, EdgeInfo o2) {
                //first check special null cases
                IPresentationElement sourcePE1=o1.getSourcePE();
                IPresentationElement sourcePE2=o2.getSourcePE();
                if(sourcePE1==null && sourcePE2==null)return 0;
                else if(sourcePE1==null)return -1;
                else if(sourcePE2==null)return 1;
                NodeInfo sourceInfo1=presIdNodeInfoMap.get(sourcePE1.getXMIID());
                ConnectorData sourceConnector1=(ConnectorData) o1.getProperty("SOURCECONNECTOR");
                NodeInfo sourceInfo2=presIdNodeInfoMap.get(sourcePE2.getXMIID());
                ConnectorData sourceConnector2=(ConnectorData) o2.getProperty("SOURCECONNECTOR");
                int y1=0,y2=0;
                if(o1.getSourcePE().getFirstSubject() instanceof Lifeline)
                {
                    boolean actor=((Lifeline) o1.getSourcePE().getFirstSubject()).getIsActorLifeline();
                    int y_level=actor ? SQDDiagramEngineExtension.DEFAULT_ACTORLIFELINE_Y : SQDDiagramEngineExtension.DEFAULT_LIFELINE_Y;
                    y1= y_level+sourceConnector1.getY() - (Integer) sourceInfo1.getProperty(LIFELINESHIFTKEY);//connector is relative to lifeline, also need to count on shift from initial different lvel locations
                }
                else
                {
                    //
                    Widget sourceWidget = scene.findWidget(o1.getSourcePE());
                    y1=sourceWidget.getPreferredLocation().y+sourceConnector1.getY();//consider it may be cf only/interaction and all entire area is used
                }
               if(o2.getSourcePE().getFirstSubject() instanceof Lifeline)
                {
                    boolean actor=((Lifeline) o2.getSourcePE().getFirstSubject()).getIsActorLifeline();
                    int y_level=actor ? SQDDiagramEngineExtension.DEFAULT_ACTORLIFELINE_Y : SQDDiagramEngineExtension.DEFAULT_LIFELINE_Y;
                    y2= y_level+sourceConnector2.getY() - (Integer) sourceInfo2.getProperty(LIFELINESHIFTKEY);//connector is relative to lifeline, also need to count on shift from initial different lvel locations
                }
                else
                {
                    //
                    Widget sourceWidget = scene.findWidget(o2.getSourcePE());
                    y2=sourceWidget.getPreferredLocation().y+sourceConnector2.getY();
                }
                
                
                return y1-y2;
            }
        });
        
        for(int index1=0;index1<einfos.size();index1++)
        {
            EdgeInfo edgeInfo=einfos.get(index1);
            IElement elt = (IElement) edgeInfo.getProperty(ELEMENT);
            if(elt==null)continue;//skip absent model element
            Message message=(Message) elt;
            if(message.getKind()==Message.MK_RESULT)
            {
                continue;//result is created with call message at the same time
            }
            IPresentationElement pE = Util.createNodePresentationElement();
    //            pE.setXMIID(PEID);
            IPresentationElement sourcePE=edgeInfo.getSourcePE();
            IPresentationElement targetPE=edgeInfo.getTargetPE();
            if(sourcePE==null || targetPE==null)continue;//skip if source or target is missed
            Widget sourceWidget = scene.findWidget(sourcePE);
            Widget targetWidget = scene.findWidget(targetPE);
            NodeInfo sourceInfo=presIdNodeInfoMap.get(edgeInfo.getSourcePE().getXMIID());
            NodeInfo targetInfo=presIdNodeInfoMap.get(edgeInfo.getTargetPE().getXMIID());
            pE.addSubject(elt);
            DiagramEngine engine=scene.getEngine();
            SQDDiagramEngineExtension sqdengine=(SQDDiagramEngineExtension) engine;
            SQDMessageConnectProvider provider = null;
            List retVal = new ArrayList();
            Message returnMsg = null;
            EdgeInfo resultInfo=null;
            Point startingPoint=null, endingPoint=null, resultStartingPoint=null, resultEndingPoint=null;
            ConnectorData sourceConnector=(ConnectorData) edgeInfo.getProperty("SOURCECONNECTOR");
            ConnectorData targetConnector=(ConnectorData) edgeInfo.getProperty("TARGETCONNECTOR");
            //
            //
            if(edgeInfo.getSourcePE().getFirstSubject() instanceof Lifeline)
            {
                boolean actor=((Lifeline) edgeInfo.getSourcePE().getFirstSubject()).getIsActorLifeline();
                int y_level=actor ? SQDDiagramEngineExtension.DEFAULT_ACTORLIFELINE_Y : SQDDiagramEngineExtension.DEFAULT_LIFELINE_Y;
                int x=sourceWidget.getPreferredLocation().x+sourceWidget.getMinimumSize().width/2;
                int y= y_level+sourceConnector.getY() - (Integer) sourceInfo.getProperty(LIFELINESHIFTKEY);//connector is relative to lifeline, also need to count on shift from initial different lvel locations
                startingPoint=new Point(x,y);
            }
            else
            {
                //yet unsupported, y may be from preferred location but x need to be archived from data
                int y=sourceWidget.getPreferredLocation().y+sourceConnector.getY();
                int x=sourceWidget.getPreferredLocation().x+(sourceConnector.getProportionalOffetX()>0 ? sourceWidget.getBounds().width : 0);
                startingPoint=new Point(x,y);
            }
            if(edgeInfo.getTargetPE().getFirstSubject() instanceof Lifeline)
            {
                int x=targetWidget.getPreferredLocation().x+targetWidget.getMinimumSize().width/2;
                //int y= targetConnector.getY() - (Integer) targetInfo.getProperty(LIFELINESHIFTKEY);
                int y=startingPoint.y;
                if(sourceWidget==targetWidget)y+=10;
                endingPoint=new Point(x,y);
            }
            else
            {
                //yet unsupported
                int y=startingPoint.y;
                int x=targetWidget.getPreferredLocation().x+(targetConnector.getProportionalOffetX()>0 ? targetWidget.getBounds().width : 0);
                endingPoint=new Point(x,y);
            }
            ArrayList<Point> wayPoints=new ArrayList<Point>();
            wayPoints.add(startingPoint);
            wayPoints.add(endingPoint);
            edgeInfo.setWayPoints(wayPoints);
            //
            if (message.getKind() == Message.MK_SYNCHRONOUS)
            {
                //now find the result message for this call message
                for(EdgeInfo edgeInfo2:einfos)
                {
                    Message resTmp= (Message) edgeInfo2.getProperty(ELEMENT);
                    if(resTmp.getKind()==Message.MK_RESULT)
                    {
                        if(resTmp.getSendingMessage()==message)
                        {
                            returnMsg=resTmp;
                            resultInfo=edgeInfo2;
                            break;
                        }
                    }
                }
                if (returnMsg != null)
                {
                    //create the sync msg and delete it from the edgeInfoList
                    provider = sqdengine.getConnectProvider(message, returnMsg);
                    //get the returnMsg info from the edgeInfoList
                    if (resultInfo != null) {
                        ConnectorData sourceResultConnector=(ConnectorData) resultInfo.getProperty("SOURCECONNECTOR");
                        ConnectorData targetResultConnector=(ConnectorData) resultInfo.getProperty("TARGETCONNECTOR");
                        
                        if(resultInfo.getSourcePE().getFirstSubject() instanceof Lifeline)
                        {
                            boolean actor=((Lifeline) resultInfo.getSourcePE().getFirstSubject()).getIsActorLifeline();
                            int y_level=actor ? SQDDiagramEngineExtension.DEFAULT_ACTORLIFELINE_Y : SQDDiagramEngineExtension.DEFAULT_LIFELINE_Y;
                            int x=targetWidget.getPreferredLocation().x+targetWidget.getMinimumSize().width/2;
                            int y= y_level+sourceResultConnector.getY() - (Integer) targetInfo.getProperty(LIFELINESHIFTKEY);
                            resultStartingPoint=new Point(x,y);
                        }
                        else
                        {
                            //yet unsupported
                            int y=targetWidget.getPreferredLocation().y+sourceResultConnector.getY();
                            int x=targetWidget.getPreferredLocation().x + (sourceResultConnector.getProportionalOffetX()>0 ? targetWidget.getBounds().width : 0);
                            resultStartingPoint=new Point(x,y);
                        }
                        if(resultInfo.getTargetPE().getFirstSubject() instanceof Lifeline)
                        {
                            int x=sourceWidget.getPreferredLocation().x+sourceWidget.getMinimumSize().width/2;
                            //int y= targetResultConnector.getY() - (Integer) targetInfo.getProperty(LIFELINESHIFTKEY);
                            int y=resultStartingPoint.y;
                            if(sourceWidget==targetWidget)y+=10;
                            resultEndingPoint=new Point(x,y);
                        }
                        else
                        {
                            //yet unsupported
                            int y=resultStartingPoint.y;
                            int x=sourceWidget.getPreferredLocation().x + (targetResultConnector.getProportionalOffetX()>0 ? sourceWidget.getBounds().width : 0);
                            resultEndingPoint=new Point(x,y);
                        }
                        
                        ArrayList<Point> resultWayPoints=new ArrayList<Point>();
                        resultWayPoints.add(resultStartingPoint);
                        resultWayPoints.add(resultEndingPoint);
                        resultInfo.setWayPoints(resultWayPoints);
                        
                        retVal = (List)provider.createSynchConnection(sourceWidget, targetWidget, startingPoint, endingPoint, resultStartingPoint, resultEndingPoint);
                    }
                }
            } 
            else if ((message.getKind() == Message.MK_ASYNCHRONOUS) 
                    || (message.getKind() == Message.MK_CREATE))
            {
                provider = sqdengine.getConnectProvider(message, null);
                retVal = provider.createConnection(sourceWidget, targetWidget, startingPoint, endingPoint);                    
            } 
            scene.validate();
            for (int i = 0; i < retVal.size(); i++)
            {
                Object object = retVal.get(i);
                if (object instanceof DiagramEdgeReader)
                {
                    //workaround for issue with number on message which do not have associated operation, will show name label instead of operation label
                    if(ShowMessageNumbers)
                    {
                        if(message.getOperationInvoked()==null)//no operation - no operation label in 6.5
                        {
                            if(edgeInfo.getLabels()!=null && edgeInfo.getLabels().size()>0 && edgeInfo.getLabels().get(0).getLabel().equalsIgnoreCase(AbstractLabelManager.OPERATION))//atempt to load operation
                            {
                                edgeInfo.getLabels().get(0).setLabel(AbstractLabelManager.NAME);//change o show name
                            }
                        }
                    }
                    //
                    if (object instanceof UMLEdgeWidget && 
                            (((UMLEdgeWidget)object).getWidgetID()).equalsIgnoreCase(UMLWidgetIDString.RESULTMESSAGECONNECTIONWIDGET.toString())) {
                        ((DiagramEdgeReader)object).load(resultInfo);
                    }
                    else {
                        try
                        {
                            ((DiagramEdgeReader)object).load(edgeInfo);
                        }
                        catch(java.lang.IllegalArgumentException ex)
                        {
//                            System.out.println("***WARNING: "+ex);
                        }
                    }                        
                }

            }
        }
    }
    
    private void findEdgesElements() {
        Collection<EdgeInfo> einfos = presIdEdgeInfoMap.values();
        for (EdgeInfo einfo : einfos)
        {
            IElement elt = getElement(project, einfo.getMEID());
            if(elt!=null)einfo.setProperty( ELEMENT,elt);
        }
    }
   
    /**
     * make lowest x equal to zero(with some shift), revert y and make lowest y equal to zero(with some shift)
     */
    private void normilizeNodesForZero() {
        Collection<NodeInfo> ninfos = presIdNodeInfoMap.values();
        int margin=60;
        for (NodeInfo ninfo : ninfos)
        {
            Point loc=ninfo.getPosition();
            loc.x-=minX-margin;
            loc.y=maxY+margin-loc.y;
            ninfo.setPosition(loc);
        }
    }
    
    private void handleBendPointsInfo()
    {
        Collection<EdgeInfo> einfos = presIdEdgeInfoMap.values();
        int margin=60;
        for (EdgeInfo einfo : einfos)
        {
                ArrayList<Point> points=new ArrayList<Point>();
                Point point0=new Point(0,0);
                if(einfo.getSourcePE()!=null && presIdNodeInfoMap.get(einfo.getSourcePE().getXMIID())!=null)
                {
                    point0=presIdNodeInfoMap.get(einfo.getSourcePE().getXMIID()).getPosition();
                }
                points.add(point0);
                if(einfo.getProperty(BENDSPROPERTY)!=null)
                {
                    ArrayList<Point> tmp=(ArrayList<Point>) einfo.getProperty(BENDSPROPERTY);
                    for(int i=0;i<tmp.size();i++)
                    {
                        Point p=tmp.get(i);
                        Point loc=new Point(p);
                        loc.x-=minX-margin;
                        loc.y=maxY+margin-loc.y;
                        points.add(loc);
                    }
                }
                
                Point pointN=new Point(0,0);
                if(einfo.getTargetPE()!=null && presIdNodeInfoMap.get(einfo.getTargetPE().getXMIID())!=null)
                {
                    pointN=presIdNodeInfoMap.get(einfo.getTargetPE().getXMIID()).getPosition();
                }
                points.add(pointN);
                //
                if(NESTEDLINKENGINE.equals(einfo.getProperty(ENGINE)))
                {//nested link need reverse order in new diagram
                    Collections.reverse(points);
                }
                //
                einfo.setWayPoints(points);
        }
    }
    
    /**
     * this method handle positioning of all lifelines on the same level
     */
    private void normalizeSQDDiagram()
    {
        Collection<NodeInfo> ninfos = presIdNodeInfoMap.values();
        int topLifelineY=Integer.MAX_VALUE;
        for (NodeInfo ninfo : ninfos)//determine top Y
        {
            IPresentationElement pres=(IPresentationElement) ninfo.getProperty(PRESENTATIONELEMENT);
            if(pres!=null && pres.getFirstSubject() instanceof Lifeline)
            {
                Lifeline ll=(Lifeline) pres.getFirstSubject();
                Point loc=new Point(ninfo.getPosition());
                boolean actor=ll.getIsActorLifeline();
                int y_level=actor ? SQDDiagramEngineExtension.DEFAULT_ACTORLIFELINE_Y : SQDDiagramEngineExtension.DEFAULT_LIFELINE_Y;
                int diff=y_level-loc.y;
                loc.y=y_level;
                ninfo.setPosition(loc);
                Dimension size=ninfo.getSize();
                size.height-=diff;//correct length
                ninfo.setSize(size);
                ninfo.setProperty( LIFELINESHIFTKEY,diff);
            }
        }
    }

    private void processSemanricPresentation(EdgeInfo edgeReader) {
        String  engine=(String) edgeReader.getProperty(ENGINE);
        if(PATTERNEDGEENGINE.equals(engine))
        {
            edgeReader.setSemanticModelBridgePresentation(PATTERNEDGEPRESENTATION);
        }
        else if(NESTEDLINKENGINE.equals(engine))
        {
            edgeReader.setSemanticModelBridgePresentation(NESTEDLINKPRESENTATION);
        }
    }

    private void shiftNodesByY(int shift)
    {
        Collection<NodeInfo> ninfos = presIdNodeInfoMap.values();
        for (NodeInfo ninfo : ninfos)
        {
            Point loc=ninfo.getPosition();
            loc.y+=shift;
            ninfo.setPosition(loc);
        }
    }
    
    private void releaseResources()
    {
        try
        {
            readerPres.close();
            fisPres.close();
            readerData.close();
            fisData.close();
        }
        
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        
        catch (IOException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    private void readXMLPres()
    {
        try
        {
            int event = readerPres.getEventType();
            while (true)
            {
                switch (event)
                {
                    case XMLStreamConstants.START_ELEMENT:
                        handlePresStartElement();
                        break;
                        
                    case XMLStreamConstants.END_DOCUMENT:
                        readerPres.close();
                        break;
                }

                if (!readerPres.hasNext())
                    break;
                
                event = readerPres.next();
            }
        }
        
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        
        catch (Exception ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void handlePresStartElement()
    {
        if (readerPres.getName().getLocalPart().startsWith("DCE."))
        {
            String PEID = readerPres.getAttributeValue(null, "PEID");
            NodeInfo ninfo = presIdNodeInfoMap.get(PEID);
            
            if (ninfo != null)
            {
                //node
                ninfo.setMEID(readerPres.getAttributeValue(null, "MEID"));
                //
                getEngineFromPres(readerPres,ninfo);
                //
                if(ASSOCIATIONCLASSCONNECTORENGINE.equals(ninfo.getProperty(ENGINE)))
                {
                    //this is circle on association class connector
                    //presIdNodeInfoMap.remove(PEID);
                    associationClasses.put(PEID,ninfo);
                    //return;
                }
                //
                //presIdNodeInfoMap.put(PEID, ninfo);
            }
            else
            {
                EdgeInfo einfo = presIdEdgeInfoMap.get(PEID);
                
                if (einfo != null)
                {
                    //edge
                    einfo.setMEID(readerPres.getAttributeValue(null, "MEID"));
                    getEngineFromPres(readerPres,einfo);
                    presIdEdgeInfoMap.put(PEID, einfo);
                }
                else if(peidToEdgeLabelMap.get(PEID)!=null)
                {
                    //label
                    getEdgeLabelInfoFromPres(readerPres, peidToEdgeLabelMap.get(PEID));
                }
                else if(peidToNodeLabels.get(PEID)!=null)
                {
                    getNodeLabalInfoFromPres(readerPres,peidToNodeLabels.get(PEID));
                }
            }
        }
        else if(readerPres.getName().getLocalPart().startsWith("diagramInfo"))
        {
            ShowAllReturnMessages=(Boolean) Boolean.parseBoolean(readerPres.getAttributeValue(null, "ShowAllReturnMessages"));
            ShowMessageNumbers=(Boolean) Boolean.parseBoolean(readerPres.getAttributeValue(null, "ShowMessageNumbers"));
        }
    }
    
    private void postProcessNode(Widget widget, IPresentationElement presEl) {

    }
    
    private ConnectorData handleConnectorInNode(XMLStreamReader readerData, HashMap<String,ConnectorData> connectors) {
        try {
            String startWith = readerData.getLocalName();
            ConnectorData connectorData=new ConnectorData();
            connectorData.setId( readerData.getAttributeValue(null, "id"));
            //we need to go deeper and exit on the same node, or can exit before on place we found necessary info
            while (readerData.hasNext() && !(readerData.isEndElement() && readerData.getLocalName().equals(startWith))) {
                if(readerData.isStartElement() && readerData.getLocalName().equals("constantOffset"))
                {
                    connectorData.setY((int) -Double.parseDouble(readerData.getAttributeValue(null, "y")));//invert Y because of coordinate system changes
                }
                else if(readerData.isStartElement() && readerData.getLocalName().equals("proportionalOffset"))
                {
                    connectorData.setProportionalOffetX(Double.parseDouble(readerData.getAttributeValue(null, "x")));
                }
                readerData.next();
            }
            connectors.put(connectorData.getId()+"",connectorData);
            return connectorData;
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    private void handleNodeLabels(XMLStreamReader readerData,ArrayList nodeLabels)
    {
        try {
            String startWith = readerData.getLocalName();
            //we need to go deeper and exit on the same node, or can exit before on place we found necessary info
            while (readerData.hasNext() && !(readerData.isEndElement() && readerData.getLocalName().equals(startWith))) {
                if(readerData.isStartElement() && readerData.getLocalName().equals("nodeLabel"))
                {
                    //node label here, need to handle and show later
                    NodeInfo.NodeLabel nL=new NodeInfo.NodeLabel();
                    handleNodeLabel(readerData,nL);
                    nodeLabels.add(nL);
                }
                readerData.next();
            }
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    private void handleNodeLabel(XMLStreamReader readerData,NodeInfo.NodeLabel nL)
    {
        try {
            String startWith = readerData.getLocalName();
            //we need to go deeper and exit on the same node, or can exit before on place we found necessary info
            while (readerData.hasNext() && !(readerData.isEndElement() && readerData.getLocalName().equals(startWith))) {
                if(readerData.isStartElement())
                {
                    if(readerData.getLocalName().equals("name"))
                    {
                        nL.setLabel(readerData.getAttributeValue(null, "value"));
                    }
                    else if(readerData.getLocalName().equals("proportionalOffset"))
                    {
                        //need to be converted to new coordinates if support will be required
                    }
                    else if(readerData.getLocalName().equals("PEID"))
                    {
                        nL.setPEID(readerData.getAttributeValue(null, "value"));
                        peidToNodeLabels.put(nL.getPEID(), nL);
                    }
                }
                readerData.next();
            }
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private String getEngineFromPres(XMLStreamReader readerPres,NodeInfo ninfo) {
        try {
            String startWith = readerPres.getLocalName();
            //we need to go deeper and exit on the same node, or can exit before on place we found necessary info
            while (readerPres.hasNext() && !(readerPres.isEndElement() && readerPres.getLocalName().equals(startWith))) {
                if(readerPres.isStartElement())
                {
                    if(readerPres.getLocalName().equals("engine"))
                    {
                        ninfo.setProperty(ENGINE, readerPres.getAttributeValue(null, "name"));
                    }
                    else if(readerPres.getLocalName().equals("Divider"))
                    {
                        ninfo.addDeviderOffset(readerPres.getAttributeValue(null, "offset"));
                    }
                    else if(readerPres.getLocalName().equals("compartment"))
                    {
                        String name=readerPres.getAttributeValue(null, "name");
                            String orientation=readerPres.getAttributeValue(null, "orientation");
                            String value=readerPres.getAttributeValue(null, "value");
                            if(orientation!=null && orientation.length()>0)
                            {
                                if("1".equals(orientation))
                                {
                                    //
                                    ninfo.setProperty(ORIENTATION, SeparatorWidget.Orientation.HORIZONTAL.toString());
                                }
                                else if("0".equals(orientation))
                                {
                                    //
                                     ninfo.setProperty(ORIENTATION, SeparatorWidget.Orientation.VERTICAL.toString());
                                }
                                else
                                {
//                                    System.out.println("***WARNING: UNKNOWN ORIENTATION: "+orientation);
                                }
                            }
                            if("Transitions".equals(value))
                            {
                                //it's in State widget
                                ninfo.setProperty("ShowTransitions", Boolean.TRUE);
                            }
                    }
                }
                readerPres.next();
            }
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        //
        return            (String) ninfo.getProperty(ENGINE);
    }
    private String getEngineFromPres(XMLStreamReader readerPres, EdgeInfo einfo) {
        try {
            String startWith = readerPres.getLocalName();
            //we need to go deeper and exit on the same node, or can exit before on place we found necessary info
            while (readerPres.hasNext() && !(readerPres.isEndElement() && readerPres.getLocalName().equals(startWith))) {
                if(readerPres.isStartElement() && readerPres.getLocalName().equals("engine"))
                {
                    einfo.setProperty(ENGINE, readerPres.getAttributeValue(null, "name"));
                    String messageShowType=readerPres.getAttributeValue(null,SHOWMESSAGETYPE);
                    if(messageShowType!=null)einfo.setProperty( SHOWMESSAGETYPE,messageShowType);
                    return            (String) einfo.getProperty(ENGINE);
                }
                readerPres.next();
            }
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        //
        return null;
    }
    
    private void getEdgeLabelInfoFromPres(XMLStreamReader readerPres,HashMap<String,Object> labelInfo)
    {
        String startWith = readerPres.getLocalName();
        //we need to go deeper and exit on the same node, or can exit before on place we found necessary info
        String type=readerPres.getAttributeValue(null, "TSLabelKind");
        String placement=readerPres.getAttributeValue(null, "TSLabelPlacementKind");
        labelInfo.put( TSLABELTYPE,type);
        labelInfo.put( TSLABELPLACEMENT,placement);
    }
    
    private void getNodeLabalInfoFromPres(XMLStreamReader readerPres, NodeLabel nL) {
        String meid=readerPres.getAttributeValue(null, "MEID");
        //find model
        IElement el=getElement(project, meid);
        if(el!=null)nL.setElement(el);
    }

    
    /**
     * should add all labels info to edges info
     * @param peidToLabelMap
     */
    private void handleLabelsInfo(HashMap<String, HashMap<String, Object>> peidToLabelMap) {
        for(String key:peidToLabelMap.keySet())
        {
            HashMap<String,Object> labelInfo=peidToLabelMap.get(key);
            int tsType=Integer.parseInt((String) labelInfo.get(TSLABELTYPE));
            Dimension size=(Dimension) labelInfo.get("SIZE");
            String typeInfo=null;
            EdgeInfo edgeInfo=(EdgeInfo) labelInfo.get("EDGE");
            boolean endLabel=false;
            LabelManager.LabelType type=null;
            switch(tsType)
            {
                case 11:
                    //it's name for activity edge with lightning, after addition of support in 6.5 need to add some info here
//                    System.out.println("***WARNING: Unsupported lightning on signal to invocation edge");
                case 1://for names of smth on all diagrams
                case 12://for names of smth on all diagrams
                    typeInfo=AbstractLabelManager.NAME;
                    break;
                case 7://for sqd message operations
                    String msgShowType=(String) edgeInfo.getProperty(SHOWMESSAGETYPE);
                    if("0".equals(msgShowType))
                        typeInfo=AbstractLabelManager.OPERATION;
                    else if("1".equals(msgShowType))
                        typeInfo=AbstractLabelManager.NAME;
                    break;
                case 6:
                    typeInfo=AbstractLabelManager.STEREOTYPE;
                    break;
                case 13:
                    //derivation specification
                    typeInfo=AbstractLabelManager.BINDING;
                    break;
                case 4:
                    //target association name
                    endLabel=true;
                    typeInfo=AbstractLabelManager.NAME;
                    type=LabelManager.LabelType.TARGET;
                    break;
                case 2:
                    //source association name
                    endLabel=true;
                    typeInfo=AbstractLabelManager.NAME;
                    type=LabelManager.LabelType.SOURCE;
                    break;
                case 5:
                    //some end multiplicity
                    endLabel=true;
                    typeInfo=AbstractLabelManager.MULTIPLICITY;
                    type=LabelManager.LabelType.TARGET;
                    break;
                case 3:
                    //other end multiplicity
                    endLabel=true;
                    typeInfo=AbstractLabelManager.MULTIPLICITY;
                    type=LabelManager.LabelType.SOURCE;
                    break;
                case 10:
                    //guard condition(activity)
                    typeInfo=AbstractLabelManager.GUARD_CONDITION;
                    break;
                case 16:
                    //pre consition(state)
                    //endLabel=true;
                    typeInfo=AbstractLabelManager.PRECONDITION;
                    type=LabelManager.LabelType.SOURCE;
                    break;
                case 17:
                    //post condition(state)
                    //endLabel=true;
                    typeInfo=AbstractLabelManager.POSTCONDITION;
                    type=LabelManager.LabelType.TARGET;
                    break;
                default:
                    throw new UnsupportedOperationException("Converter can't handle label kind: "+tsType);
            }
//            System.out.println("LABEL: "+typeInfo+":"+type);
            if(typeInfo==null)continue;//unsupported yet
            if(endLabel)
            {
                EdgeInfo.EndDetails endDet = null;
                //need source and target id for association
                IElement elt=(IElement) edgeInfo.getProperty(ELEMENT);
                String sourceID=null;
                String targetID=null;
                if(elt!=null && elt instanceof IAssociation)
                {
                    IAssociation ass=(IAssociation) elt;
                    sourceID=ass.getEndAtIndex(0).getXMIID();
                    targetID=ass.getEndAtIndex(1).getXMIID();
                }
                else if(elt!=null && elt instanceof ITransition)
                {
//                    System.out.println("***WARNING: unsupported pre/postcondition label was skipped");
                    continue;//pre-post transitions unsupported yet
                }
                else
                {
                    continue;//support only associations (now?)
                }
                //
                String requiredID=null;
                if(type.equals(LabelManager.LabelType.TARGET))
                {
                    requiredID=(targetID);
                }
                else if(type.equals(LabelManager.LabelType.SOURCE))
                {
                    requiredID=(sourceID);
                }
                //
                for(EdgeInfo.EndDetails tmp:edgeInfo.getEnds())
                {
                    if(tmp.getID().equals(requiredID))
                    {
                        endDet=tmp;
                        break;
                    }
                }
                if(endDet==null)
                {
                    endDet = edgeInfo.new EndDetails();
                    edgeInfo.getEnds().add(endDet);
                }
                endDet.setID(requiredID);
                edgeInfo.setHasContainedElements(true);
                EdgeInfo.EdgeLabel label=edgeInfo.new EdgeLabel();
                label.setLabel(typeInfo);
                label.setSize(size);
                endDet.getEndEdgeLabels().add(label);
            }
            else
            {
                edgeInfo.setHasContainedElements(true);
                EdgeInfo.EdgeLabel label=edgeInfo.new EdgeLabel();
                label.setLabel(typeInfo);
                label.setSize(size);
                if(type!=null)
                {
                    label.getLabelProperties().put(UMLEdgeWidget.LABEL_TYPE, typeInfo+"_"+type);
                }
                //label.setPosition(null);
                edgeInfo.getLabels().add(label);
            }
        }
    }
    
    private void mapEngineToView(IPresentationElement presEl,NodeInfo ninfo) {
        if(ninfo.getViewName()!=null)return;//only if view wasn't set from node before
        String engine=(String) ninfo.getProperty(ENGINE);
        if(engine!=null)//try from engine
        {
            if(engine.equals("ClassRobustnessDrawEngine"))//iconic viw for robustness classes, now need to find robustness kind
            {
                //let use first stereotype here(looks like approach in 6.1)
                if(presEl.getFirstSubject().getAppliedStereotypes().size()>0)
                {
                    ninfo.setViewName(presEl.getFirstSubject().getAppliedStereotypesAsString().get(0));
                }
            }
            else if(engine.equals("InterfaceDrawEngine"))
            {
                ninfo.setViewName("lollipop");
            }
        }
        if(ninfo.getViewName()!=null)return;//only if view wasn't set from node before
    }

    private IPresentationElement createPresentationElement(
        String meid, String peid)
    {
        IElement element = getElement(project, meid);
        if(element==null)return null;
        IPresentationElement presEl = Util.createNodePresentationElement();
        presEl.setXMIID(peid);
        presEl.addSubject(element);
        return presEl;
    }


    private void readXMLData()
    {
        try
        {
            int event = readerData.getEventType();
            while (true)
            {
                switch (event)
                {
                    case XMLStreamConstants.START_ELEMENT:
                        handleDataStartElement();
                        break;
                        
                    case XMLStreamConstants.END_DOCUMENT:
                        readerData.close();
                        break;
                }

                if (!readerData.hasNext())
                    break;
                
                event = readerData.next();
            }
        }
        
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    
    
    private void handleDataStartElement()
    {
        if (readerData.getName().getLocalPart().equalsIgnoreCase("node"))
        {
            handleNode();
        }

        if (readerData.getName().getLocalPart().equalsIgnoreCase("edge"))
        {
                handleEdge();
        }
    }
    
    
    private void handleNode()
    {
        String nodeId = null;
        Point position = null;
        Dimension size = null;
        String PEID = null;
        String graphicsType=null;
        ArrayList<NodeInfo.NodeLabel> nodeLabels=new ArrayList<NodeInfo.NodeLabel>();
        try
        {
            nodeId = readerData.getAttributeValue(null, "id");
            HashMap<String,ConnectorData> nodeConnectors=new HashMap<String,ConnectorData>();
            while (readerData.hasNext())
            {
                if (readerData.next() == XMLStreamConstants.START_ELEMENT)
                {
                    //we are only intersted in data of particular start elements
                    if(readerData.getName().getLocalPart()
                        .equalsIgnoreCase("connector"))
                    {
                        ConnectorData connector=handleConnectorInNode(readerData,connectors);
                        nodeConnectors.put(connector.getId()+"", connector);
                    }
                    else if(readerData.getName().getLocalPart()
                        .equalsIgnoreCase("nodeLabels"))
                    {
                        handleNodeLabels(readerData,nodeLabels);
                    }
                    else if (readerData.getName().getLocalPart()
                        .equalsIgnoreCase("center") && position==null)//second position may be corresponding label position
                    {
                        int x = Float.valueOf(readerData
                            .getAttributeValue(null, "x")).intValue();
                        
                        int y = Float.valueOf(readerData
                            .getAttributeValue(null, "y")).intValue();
                            
                        
                        // minX and maxY not final yet so keep use coords here;
                        // recalc all points as nodes are added to new diagram
                        position = new Point(x, y);
                    }
                    
                    else if (readerData.getName().getLocalPart()
                        .equalsIgnoreCase("size") && size==null)//second size may be corresponding label size
                    {
                        size = new Dimension(
                            Float.valueOf(readerData
                                .getAttributeValue(null, "width")).intValue(),
                            Float.valueOf(readerData
                                .getAttributeValue(null, "height")).intValue());
                    }

                    else if (readerData.getName().getLocalPart()
                        .equalsIgnoreCase("PEID") && PEID==null)
                    {
                        PEID = readerData.getAttributeValue(null, "value");
                    }
                    else if (readerData.getName().getLocalPart()
                        .equalsIgnoreCase("graphics") && graphicsType==null)
                    {
                        graphicsType = readerData.getAttributeValue(null, "type");
                    }
                }
                
                else if (readerData.isEndElement() && 
                    readerData.getName().getLocalPart().equalsIgnoreCase("node"))
                {
                    //we have reached the end of anchors list                    
                    break;
                }
            } // while
            
            if (nodeId != null)
            {
                if (PEID != null)
                {
                    NodeInfo nodeInfo=new NodeInfo(PEID, "");
                    if(size!=null)nodeInfo.setSize(size);
                    if(position!=null)
                    {
                        if(size!=null)
                        {
                            position.x-=size.width/2;
                            position.y+=size.height/2;
                        }
                        nodeInfo.setPosition(position);
                        minX = Math.min(position.x, minX);
                        maxY = Math.max(position.y, maxY);
                    }
                    
                    if(graphicsType!=null)
                    {
                        nodeInfo.setProperty(GRAPHPROPERTY, graphicsType);
                    }
                    
                    if(nodeConnectors.size()>0)
                    {
                        nodeInfo.setProperty(NODECONNECTORS, nodeConnectors);
                    }
                    
                    if(nodeLabels.size()>0)
                    {
                        nodeInfo.addNodeLabels(nodeLabels);
                    }
                    
                    dataNodeIdMap.put(Integer.parseInt(nodeId), PEID);
                    nodeInfo.setProperty("NODEID61",Integer.parseInt(nodeId));
                    presIdNodeInfoMap.put(
                        PEID, nodeInfo);
                }
            }
        }
        
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private IPresentationElement findNode(String PEID)
    {
        if (PEID!=null && !PEID.equals(""))
        {
            for (IPresentationElement pE : presEltList)
            {
                if (PEID.equalsIgnoreCase(pE.getXMIID()))
                {
                    return pE;
                }
            }
        }
        return null;
    }
    
    private void handleEdge()
    {
        String edgeId = null;
        String sourceId = null;
        String targetId = null;
        String PEID = null;
        
        try
        {
            edgeId = readerData.getAttributeValue(null, "id");
            sourceId = readerData.getAttributeValue(null, "source");
            targetId = readerData.getAttributeValue(null, "target");
            //
            EdgeInfo einfo = new EdgeInfo();
            //
            
            while (readerData.hasNext())
            {
                if (readerData.next() == XMLStreamConstants.START_ELEMENT)
                {
                    if (readerData.getName().getLocalPart()
                        .equalsIgnoreCase("PEID"))
                    {
                        PEID = readerData.getAttributeValue(null, "value");
                    }

                    else if (readerData.getName().getLocalPart()
                        .equalsIgnoreCase("edgeLabel"))
                    {
                        
                        handleEdgeLabel(einfo);
                    }
                    else if(readerData.getName().getLocalPart()
                        .equalsIgnoreCase("sourceConnector"))
                    {
                        //edges goes after nodes, so info should be availabel
                        einfo.setProperty("SOURCECONNECTOR", connectors.get(readerData.getAttributeValue(null, "value")));
                    }
                    else if(readerData.getName().getLocalPart()
                        .equalsIgnoreCase("targetConnector"))
                    {
                        //edges goes after nodes, so info should be availabel
                        einfo.setProperty("TARGETCONNECTOR", connectors.get(readerData.getAttributeValue(null, "value")));
                    }
                    else if(readerData.getName().getLocalPart()
                        .equalsIgnoreCase("bends"))
                    {
                        handleEdgeBends(einfo);
                    }
                }
                
                else if (readerData.isEndElement() && 
                    readerData.getName().getLocalPart().equalsIgnoreCase("edge"))
                {
                    //we have reached the end of anchors list                    
                    break;
                }
            } // while
            
            if (edgeId != null)
            {
                if (PEID != null)
                {
                    dataEdgeIdMap.put(edgeId, PEID);
                    einfo.setPEID(PEID);
                    
                    presIdEdgeInfoMap.put(PEID, einfo);

                    dataConnIdMap.put(PEID, new ETPairT(
                        dataNodeIdMap.get(Integer.parseInt(sourceId)),
                        dataNodeIdMap.get(Integer.parseInt(targetId))));
                    
                    presIdEdgeInfoMap.put(PEID, einfo);
                }
            }
        }
        
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void handleEdgeLabel(EdgeInfo edgeReader)
    {
        //starts with 'edgeLabel' tag
        //
        //TSLabelPlacementKind="2" - center?
        //TSLabelKind="1" - name for association?  --> typeindo="Name" && xmi.id="Name_EDGE_PEID"?
        //TSLabelKind="12" - name for generalization? --> typeinfo="Name" && xmi.id="Name_EDGE_PEID"?
        //  --> typeinfo="Multiplicity" && xmi.id="Multiplicity_TARGET_PEID" || xmi.id="Multiplicity_SOURCE_PEID"
        //  --> xmi.ixd="End Name_SOURCE_PEID" typeinfo="Name" 
        //
        try
        {
            Point pt = null;
            Dimension d = null;
            
            HashMap<String,Object> labelInfo=new HashMap<String,Object>();
            //peidToLabelMap;
            //
            labelInfo.put("EDGE", edgeReader);
            while (readerData.hasNext())
            {
                if (XMLStreamConstants.START_ELEMENT == readerData.next())
                {
                    //we are only intersted in data of particular start elements
                    if (readerData.getName().getLocalPart().equalsIgnoreCase("constantOffset"))
                    {
                        //about position 1
                        //nodePosition = getPosition("GraphElement.position");
                        //nodeInfo.setPosition(getPosition("GraphElement.position"));
                    }
                    else if(readerData.getName().getLocalPart().equalsIgnoreCase("distanceFromSource"))
                    {
                        //about position 2
                    }
                    else if(readerData.getName().getLocalPart().equalsIgnoreCase("name"))
                    {
                        labelInfo.put("NAME", readerData.getAttributeValue(null, "value"));
                    }
                    else if(readerData.getName().getLocalPart().equalsIgnoreCase("PEID"))
                    {
                        labelInfo.put("PEID", readerData.getAttributeValue(null, "value"));
                    }
                    else if (readerData.getName().getLocalPart().equalsIgnoreCase("size"))
                    {
                        int width=(int) Double.parseDouble(readerData.getAttributeValue(null, "width"));
                        int height=(int) Double.parseDouble(readerData.getAttributeValue(null, "height"));
                        labelInfo.put("SIZE",new Dimension(width,height));
                    }
                    else if (readerData.getName().getLocalPart().equalsIgnoreCase("DiagramElement.property"))
                    {
                    }
                    else if (readerData.getName().getLocalPart().equalsIgnoreCase("SimpleSemanticModelElement"))
                    {
                    }
                    else if (readerData.getName().getLocalPart().equalsIgnoreCase("Uml2SemanticModelBridge.element"))
                    {
                    }
                    else if (readerData.getName().getLocalPart().equalsIgnoreCase("GraphElement.contained"))
                    {
                    }
                }
                else if (readerData.isEndElement() && readerData.getName().getLocalPart().equalsIgnoreCase("GraphElement.contained"))
                {
                }
                else if (readerData.isEndElement() && readerData.getName().getLocalPart().equalsIgnoreCase("edgeLabel"))
                {
                    peidToEdgeLabelMap.put((String) labelInfo.get("PEID"),labelInfo);
                    return;
                }
            }
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    private void handleEdgeBends(EdgeInfo einfo)
    {
        try {
            while (readerData.hasNext()) {
                if (XMLStreamConstants.START_ELEMENT == readerData.next()) {
                    if (readerData.getName().getLocalPart().equals("point")) {
                        int x = (int) Double.parseDouble(readerData.getAttributeValue(null, "x"));
                        int y = (int) Double.parseDouble(readerData.getAttributeValue(null, "y"));
                        if (einfo.getProperty(BENDSPROPERTY) == null) {
                            einfo.setProperty(BENDSPROPERTY, new ArrayList());
                        }
                        ((ArrayList) einfo.getProperty(BENDSPROPERTY)).add(new Point(x, y));
                    }
                }
                else if (readerData.isEndElement() && readerData.getName().getLocalPart().equalsIgnoreCase("bends"))
                {
                   break; 
                }
            }
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }

    }
    
    private class ConnectorData
    {
        private int id;
        private int y;
        private double proportionalX;
        
        public void setId(int id)
        {
            this.id=id;
        }
        public void setId(String id)
        {
            this.id= Integer.parseInt(id);
        }
        public int getId()
        {
            return id;
        }

        public void setY(int y) {
            this.y=y;
        }
        public int getY()
        {
            return y;
        }

        public void setProportionalOffetX(double x) {
            proportionalX=x;
        }
        public double getProportionalOffetX() {
            return proportionalX;
        }
    }
    private IDrawingAreaEventDispatcher getDispatcher()
    {
        IDrawingAreaEventDispatcher retVal = null;
        
        DispatchHelper helper = new DispatchHelper();
        retVal = helper.getDrawingAreaDispatcher();
        
        return retVal;
    }
}
