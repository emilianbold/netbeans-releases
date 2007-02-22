/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ComplexTypeDerivationsReader.java
 *
 * Created on October 26, 2005, 11:27 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring.query.readers;

import java.awt.Color;
import java.awt.Image;
import java.beans.BeanInfo;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.xml.nbprefuse.AnalysisConstants;
import org.netbeans.modules.xml.refactoring.FindUsageResult;
import org.netbeans.modules.xml.refactoring.RefactoringManager;
import org.netbeans.modules.xml.refactoring.Usage;
import org.netbeans.modules.xml.refactoring.UsageGroup;
import org.netbeans.modules.xml.refactoring.UsageSet;
import org.netbeans.modules.xml.refactoring.spi.UIHelper;
import org.netbeans.modules.xml.refactoring.ui.util.AnalysisUtilities;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.refactoring.ui.DisplayInfoVisitor;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;


/**
 *
 * @author Jeri Lockhart
 *
 * Reader for Where Used query and Complex Type Derivations query
 */
public class ComplexTypeDerivationsReader {
    
    
    //Derivations view
    private static final DisplayInfoVisitor div = new DisplayInfoVisitor();
    
    
    public static final prefuse.data.Schema FIND_USAGES_NODES_SCHEMA =
            new prefuse.data.Schema();   // prefuse graph schema
    static {
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.LABEL,
                String.class, AnalysisConstants.EMPTY_STRING);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.ID,
                String.class, AnalysisConstants.EMPTY_STRING);
        FIND_USAGES_NODES_SCHEMA.addColumn(
                AnalysisConstants.COMPONENT_TYPE_NAME,
                String.class, AnalysisConstants.EMPTY_STRING);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.ELEMENT_TYPE,
                String.class, AnalysisConstants.EMPTY_STRING);
        // type of a GE, LE, GA, or LA
        
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.XML_FILENAME,
                String.class, AnalysisConstants.EMPTY_STRING);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.FILE_OBJECT,
                FileObject.class);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.FILE_TYPE,
                String.class, AnalysisConstants.EMPTY_STRING);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.TOOLTIP,
                String.class, AnalysisConstants.EMPTY_STRING); // name
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.XAM_COMPONENT,
                Component.class);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.OPENIDE_NODE,
                org.openide.nodes.Node.class);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.JAVA_AWT_IMAGE,
                Image.class);
        
        // used to set node visible or not visible, depending on whether the
        // file node is expanded or collapsed
        //  -1 = always visible, any other value matches a
        // FILE_NODE_FILE_GROUP
        // the file node itself has a FILE_GROUP of -1 because it is always
        // visible
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.FILE_GROUP,
                int.class, -1);
        // a File Node has a positive value for this column, other nodes
        //   have a -1 value
        FIND_USAGES_NODES_SCHEMA.addColumn(
                AnalysisConstants.FILE_NODE_FILE_GROUP, int.class, -1);
        // assigned number of schema file group
        
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.IS_PRIMITIVE,
                boolean.class, false);  // is builtin type
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.IS_QUERY_NODE,
                boolean.class, false);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.IS_USAGE_NODE,
                boolean.class, false);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.IS_FILE_NODE,
                boolean.class, false);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.IS_EXPANDED,
                boolean.class, false);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.MOUSEOVER,
                boolean.class, false);
    }
    
    
    
    public static final prefuse.data.Schema FIND_USAGES_EDGES_SCHEMA =
            new prefuse.data.Schema();   // prefuse graph schema
    static {
        FIND_USAGES_EDGES_SCHEMA.addColumn(AnalysisConstants.LABEL,
                String.class, AnalysisConstants.EMPTY_STRING);
        FIND_USAGES_EDGES_SCHEMA.addColumn(AnalysisConstants.EDGE_TYPE,
                String.class, AnalysisConstants.EMPTY_STRING);
        //  "file-edge-type" "generalization", "reference" "composition"
        
        FIND_USAGES_EDGES_SCHEMA.addColumn(AnalysisConstants.TOOLTIP,
                String.class, AnalysisConstants.EMPTY_STRING);
        // used to set edge visible or not visible, depending on whether the
        // file node is expanded or collapsed
        //  -1 = always visible, any other value matches a
        // FILE_NODE_FILE_GROUP
        // the file node itself has a FILE_GROUP of -1 because it is
        // always visible
        FIND_USAGES_EDGES_SCHEMA.addColumn(AnalysisConstants.FILE_GROUP,
                int.class, -1);
    }
    
    /**
     * Creates a new instance of ComplexTypeDerivationsReader
     */
    public ComplexTypeDerivationsReader() {
        
    }
    
    
    /**
     * graph for CT Derivations view
     *
     */
    
    @SuppressWarnings("unchecked")
    public Graph loadComplexTypeDerivationsGraph(
            GlobalComplexType baseCT) {
        // TODO implement interrupt when implementing project scope scanning
        if (baseCT == null){
            StatusDisplayer.getDefault().setStatusText(""); //NOI18N
            ErrorManager.getDefault().log(
                    "WhereUsedReader.loadComplexTypeDerivationsGraph()" +
                    " found null query base CT");   //NOI18N
            return null;
        }
        // not used yet
        int fileGroupNumber = 0;
        // tally restrictions and extensions for the status message
        List<Integer> derivationsCount = new ArrayList<Integer>();
        derivationsCount.add(Integer.valueOf(0));       // extensions
        derivationsCount.add(Integer.valueOf(0));       // restrictions
        Graph graph = new Graph(true);        // isDirected
        graph.getNodeTable().addColumns(FIND_USAGES_NODES_SCHEMA);
        graph.getEdgeTable().addColumns(FIND_USAGES_EDGES_SCHEMA);
        
        List<Component> componentsInGraph = new ArrayList<Component>();
        
        org.openide.nodes.Node displayNode = null;
        
        Named ref = baseCT;
        FindUsageResult result = RefactoringManager.getInstance().findUsages(baseCT,
                baseCT.getModel().getSchema());
        UsageSet usageSet = null;
        try {
            usageSet = result.get();
        } catch (InterruptedException ex) {
            ErrorManager.getDefault().notify(ex);
        } catch (ExecutionException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
        UIHelper queryUIHelper = getUIHelper(baseCT);
        Node queryNode  = createQueryNode(graph,
                baseCT,
                queryUIHelper,
                false,
                componentsInGraph,
                false,
                fileGroupNumber);
        if (queryUIHelper != null){
            displayNode = queryUIHelper.getDisplayNode(baseCT);
        }
        queryNode.set(AnalysisConstants.COMPONENT_TYPE_NAME,
                displayNode.getShortDescription()); // comp type
        queryNode.set(AnalysisConstants.XAM_COMPONENT,
                baseCT);
        queryNode.setString(AnalysisConstants.LABEL, displayNode.getName());
        queryNode.setString(AnalysisConstants.ELEMENT_TYPE,
                displayNode.getDisplayName());  // element type
        
        //  Map of SourceGroups and their packages
        Map<SourceGroup, Map<FileObject,Set<UsageGroup>>> sortedUses =
                usageSet.getSortedUsages();
        Set<Entry<SourceGroup,Map<FileObject,Set<UsageGroup>>>> sgUses =
                sortedUses.entrySet();
        int usagesCount = 0;
        for (Entry<SourceGroup, Map<FileObject, Set<UsageGroup>>> sgUse:sgUses){
            Set<Entry<FileObject,Set<UsageGroup>>> pkgUses = sgUse.getValue().entrySet();
            for (Entry<FileObject, Set<UsageGroup>> pkgUse:pkgUses){
                Set<UsageGroup> usages = pkgUse.getValue();
                for (UsageGroup usage:usages){
                    int count =  usage.getItems().size();
                    if (count < 1){
                        continue;
                    }
                    usagesCount += count;
                    fileGroupNumber++;
                    addUsagesToGraph(true,  // derivations of CTs only
                            usage,
                            graph,
                            false,  // is primitive
                            componentsInGraph,
                            baseCT.getModel(),
                            baseCT,
                            queryNode,
                            fileGroupNumber,
                            derivationsCount);
                    
                }
            }
        }
        
        
        writeDerivationsFoundStatusMessage(derivationsCount.get(0).intValue(),
                derivationsCount.get(1).intValue(), baseCT);
        return graph;
    }
    
    private void writeDerivationsFoundStatusMessage(final int extensionCount,
            final int restrictionCount, GlobalComplexType baseCT)
            throws MissingResourceException {
        if (baseCT == null){
            ErrorManager.getDefault().log(
                    "WhereUsedReader.loadComplexTypeDerivationsGraph()" +
                    " found null query base CT");   //NOI18N
        }
        
        String extensionMsg = null;
        if (extensionCount == 1){
            extensionMsg = NbBundle.getMessage(ComplexTypeDerivationsReader.class,
                    "LBL_Found_1_Extension");
        } else {
            extensionMsg = MessageFormat.format(NbBundle.getMessage(
                    ComplexTypeDerivationsReader.class,
                    "LBL_Found_Extensions"),
                    new Object[] {
                extensionCount
            }
            ) ;
        }
        
        String restrictionMsg = null;
        if (restrictionCount == 1){
            restrictionMsg = MessageFormat.format(NbBundle.getMessage(
                    ComplexTypeDerivationsReader.class,
                    "LBL_1_Restriction_On_Complex_Type"),
                    new Object[] {
                baseCT.getName()
            }
            ) ;
        } else {
            restrictionMsg = MessageFormat.format(NbBundle.getMessage(
                    ComplexTypeDerivationsReader.class,
                    "LBL_Restrictions_On_Complex_Type"),
                    new Object[] {
                restrictionCount,
                baseCT.getName()
            }
            ) ;
        }
        if (!(extensionMsg == null || restrictionMsg == null)){
            StatusDisplayer.getDefault().setStatusText(
                    extensionMsg + restrictionMsg);
            
        }
    }
    
    
    
    
    private UIHelper getUIHelper(Referenceable ref) {
        return RefactoringManager.getInstance().getTargetComponentUIHelper(ref);
    }

    /**
     *
     *  Get usages in one schema file
     *   For CT Derivations query and Primitive Usages query
     * @param  usage collection of Items found in one model
     * @param  graph the prefuse Graph
     * @param  schema the schema being scanned, not necessarily the MV schema file
     * @param  ref the query component
     * @param  isPrimitive  is the query component a primitive type
     * @param  componentsInGraph the list of SchemaComponents that are already in the graph
     * @param  model  the Model of this Multiview's schema file
     *
     */
    private void addUsagesToGraph(boolean ctDerivationsOnly,
            UsageGroup usage,
            Graph graph,
            boolean isPrimitive,
            List<Component> componentsInGraph,
            Model model,
            Component queryComponent,
            Node queryNode,
            int fileGroupNumber,
            List<Integer> derivationsCount) {
        
//        Map<Component, List<Component>> um =
        Collection<Usage> items = usage.getItems();
        UIHelper uiHelper = usage.getEngine().getUIHelper();
        
        // *****************************
        // *** FILE NODE
        // *****************************
        
        // create a special edge from the file node to the query node
        //  that will be visible when the file node is collapsed
        //  This edge uses the default prefuse renderer, e.g.,
        //    a small solid arrow head
        //  When the file node is expanded, this edge will be
        //  hidden.  See FindUsagesFocusControl (double click)
//        Edge fileEdge = graph.addEdge(fileNode,queryNode);
//        fileEdge.setString(AnalysisConstants.EDGE_TYPE,
//                AnalysisConstants.FILE_EDGE_TYPE);
//        fileEdge.setInt(AnalysisConstants.FILE_GROUP,
//                fileGroupNumber);
        Node fileNode = null;
        for (Usage item:items){
            if (ctDerivationsOnly){
                Component usageComponent = item.getComponent();
                if (usageComponent instanceof ComplexContentRestriction){
                    derivationsCount.set(1,derivationsCount.get(1)+1);
                } else if (usageComponent instanceof ComplexExtension){
                    derivationsCount.set(0,derivationsCount.get(0)+1);
                } else{
                    continue;   // continue to next Item
                }
            }
        // create file node and attach it to the query node
            if (fileNode == null){
                fileNode = createFileNode(graph,
                        queryComponent,
                        (FileObject)usage.getModel().getModelSource().getLookup().lookup(FileObject.class),
                        queryNode,
                        fileGroupNumber);
            }
            List<Component> aPath = uiHelper.getRelevantPathFromRoot(item);
            Node parent = null;
            for (int i = 0; i < aPath.size();i++){
                Component sc = aPath.get(i);
                Node pn = null;
                AnalysisUtilities.ToolTipLine topLine = null;
                
                if (componentsInGraph.contains(sc)){
                    // there's already a Node for this Component,
                    //    find it in Graph
                    pn = findDup(graph, sc);
                    assert pn != null:"Cannot find Node for Component "
                            +
                            sc; //NOI18N
                }
                
                if (pn == null){
                    pn = createNode(graph,
                            sc,
                            uiHelper,
                            false,
                            componentsInGraph,
                            queryComponent,
                            fileGroupNumber);
                }
                
                if (i == 0){
                    // connect top node to file node
                    AnalysisUtilities.ToolTipLine typeLine =
                            new AnalysisUtilities.ToolTipLine(pn.getString(AnalysisConstants.COMPONENT_TYPE_NAME),
                            100,
                            Color.BLACK.getRGB(),
                            AnalysisUtilities.ToolTipLine.
                            HorizontalAlignment.CENTER);
                    String toolTip = AnalysisUtilities.createHTMLToolTip(
                            new AnalysisUtilities.ToolTipLine[] {topLine, typeLine});
                    pn.setString(AnalysisConstants.TOOLTIP, toolTip);
                    
                    // connect the node to the File Node with compositon edge
                    Edge fileCompositionEdge = graph.addEdge(pn,fileNode);
                    fileCompositionEdge.setString(AnalysisConstants.EDGE_TYPE,
                            AnalysisConstants.COMPOSITION);
                    // it's part of the group of nodes and edges that are
                    // visible or hidden, depending on whether the file node
                    // is expanded or collapsed
                    fileCompositionEdge.setInt(AnalysisConstants.FILE_GROUP,
                            fileGroupNumber);
                    
                    // set the new parent node
                    parent = pn;
                } else {
                    
                    AnalysisUtilities.ToolTipLine typeLine =
                            new AnalysisUtilities.ToolTipLine(getCompTypeDisplayName(pn),
                            100,
                            Color.BLACK.getRGB(),
                            AnalysisUtilities.ToolTipLine.
                            HorizontalAlignment.CENTER);
                    String toolTip = AnalysisUtilities.createHTMLToolTip(
                            new AnalysisUtilities.ToolTipLine[] {topLine, typeLine});
                    pn.setString(AnalysisConstants.TOOLTIP, toolTip);
                    
                    
                    // connect it to its parent (parent should not be null)
                    if (parent != null){
                        addCompositionEdge(graph,
                                pn,
                                parent,
                                fileGroupNumber);
                    }
                    parent = pn;
                }
                
                
                // Usage node is last
                if (i == aPath.size()-1){
                    // this is the usage node
                    pn.setBoolean(
                            AnalysisConstants.IS_USAGE_NODE, true);
                    
                    topLine = new AnalysisUtilities.ToolTipLine((
                            MessageFormat.format(NbBundle.getMessage(
                            ComplexTypeDerivationsReader.class, "LBL_Uses_Component"),
                            new Object[] {queryNode.getString(
                                    AnalysisConstants.LABEL) })),
                            100,
                            Color.BLACK.getRGB(),
                            AnalysisUtilities.ToolTipLine.
                            HorizontalAlignment.CENTER);
                    // Connect this usage node to the Query Node
                    // with the appropriate edge (composition or reference)
                    addApppropriateEdge(graph,
                            pn,
                            queryNode,
                            fileGroupNumber,
                            item.getType());
                    
                }// END if (i == aPath.size()-1)
                
            }
        }
    }// end addUsagesToGraph()
    
    
    
    private static String getCompTypeDisplayName(final Node pn) throws MissingResourceException {
        String compType = null;
        if (pn.canGetBoolean(AnalysisConstants.IS_PRIMITIVE) &&
                pn.getBoolean(AnalysisConstants.IS_PRIMITIVE)){
            compType = NbBundle.getMessage(ComplexTypeDerivationsReader.class,
                    "LBL_Primitive_Type");
        } else {
            compType = pn.getString(AnalysisConstants.COMPONENT_TYPE_NAME);
        }
        return compType;
    }
    
    private void addCompositionEdge(Graph graph, Node part, Node whole,
            int fileGroupNumber){
        Edge edge = graph.addEdge(part, whole);
        //edge.setBoolean(AnalysisConstants.SHOW, isVisible);
        edge.setString(AnalysisConstants.EDGE_TYPE,
                AnalysisConstants.COMPOSITION);
        edge.setInt(AnalysisConstants.FILE_GROUP, Integer.valueOf(fileGroupNumber));
        
    }
    
    /**
     * Adds a Reference edge or Generalization edge from
     * "from" node to the queryNode
     *
     *
     */
    private void addApppropriateEdge(Graph graph, Node from, Node queryNode,
            int fileGroupNumber, Usage.Type edgeType){
        Edge edge = graph.addEdge(from, queryNode);
//        edge.setBoolean(AnalysisConstants.SHOW, isVisible);
        
        edge.setInt(AnalysisConstants.FILE_GROUP, Integer.valueOf(fileGroupNumber));
        if ( edgeType == Usage.Type.GENERALIZATION){
            edge.setString(AnalysisConstants.EDGE_TYPE,
                    AnalysisConstants.GENERALIZATION);
        } else if ( edgeType == Usage.Type.REFERENCE){
            edge.setString(AnalysisConstants.EDGE_TYPE,
                    AnalysisConstants.REFERENCE);
            from.setString(AnalysisConstants.LABEL,
                    MessageFormat.format(NbBundle.getMessage(
                    ComplexTypeDerivationsReader.class,
                    "LBL_References_Ref"),
                    new Object[]
            {queryNode.getString(AnalysisConstants.LABEL)}));
        }
    }
    
    private Node findDup(Graph graph, Component sc){
        Iterator it = graph.nodes();
        while (it.hasNext()){
            Node n= Node.class.cast(it.next());
            Object nodeSC = n.get(AnalysisConstants.XAM_COMPONENT);
            if ((n.canGetBoolean(AnalysisConstants.IS_FILE_NODE) &&
                    n.getBoolean(AnalysisConstants.IS_FILE_NODE) == false)
                    && nodeSC != null){
                if (nodeSC == sc){
                    return n;
                }
            }
        }
        return null;
    }
    
    public static Node createQueryNode(Graph graph,
            Component query,
            UIHelper uiHelper,
            boolean isPrimitive,
            List<Component> componentsInGraph,
            boolean showOnlyDerivations,
            int fileGroupNumber
            ) {
        //  TODO remove this temporary hack when UIHelper for query Component is available
        Node queryNode = null;
        if (uiHelper == null){
            String name = "";   //NOI18N
            if (query instanceof Named){
                name = ((Named)Named.class.cast(query)).getName();
            }
            queryNode = graph.addNode();
            componentsInGraph.add(query);
            queryNode.setBoolean(AnalysisConstants.IS_PRIMITIVE, isPrimitive);
            queryNode.setString(AnalysisConstants.LABEL, name);
            queryNode.setString(AnalysisConstants.COMPONENT_TYPE_NAME, "" ); //NOI18N
            queryNode.set(AnalysisConstants.XAM_COMPONENT, query  );
            queryNode.setInt(AnalysisConstants.FILE_GROUP, fileGroupNumber);
            
        } else {
            queryNode = createNode(graph, query, uiHelper, isPrimitive, componentsInGraph, query, fileGroupNumber);
        }
        
        queryNode.setBoolean(AnalysisConstants.IS_QUERY_NODE, true);
        // unset the FILE_GROUP because this node is always visible
        queryNode.setInt(AnalysisConstants.FILE_GROUP,-1);
        // reset IS_PRIMITIVE in case it is
        queryNode.setBoolean(AnalysisConstants.IS_PRIMITIVE, isPrimitive);
        
        AnalysisUtilities.ToolTipLine topLine = new AnalysisUtilities.ToolTipLine((
                showOnlyDerivations?
                    NbBundle.getMessage(ComplexTypeDerivationsReader.class,
                "LBL_Base_Complex_Type"):
                    NbBundle.getMessage(ComplexTypeDerivationsReader.class,
                "LBL_Query_Component")),
                100,
                Color.BLACK.getRGB(),
                AnalysisUtilities.ToolTipLine.HorizontalAlignment.CENTER);
        String compType = getCompTypeDisplayName(queryNode);
        AnalysisUtilities.ToolTipLine typeLine = new AnalysisUtilities.ToolTipLine(
                compType,
                100,
                Color.BLACK.getRGB(),
                AnalysisUtilities.ToolTipLine.HorizontalAlignment.CENTER);
        String toolTip = AnalysisUtilities.createHTMLToolTip(new AnalysisUtilities.ToolTipLine[] {
            topLine, typeLine});
        
        queryNode.setString(AnalysisConstants.TOOLTIP, toolTip);
        return queryNode;
    }
    
    
    private Node createFileNode(Graph graph, Component queryComp,
            FileObject fobj, Node queryNode, int fileGroupNumber){
        if (queryComp == null || fobj == null){
            return null;
        }
        String fileType = AnalysisUtilities.getXmlFileType(fobj);
        Node n = graph.addNode();
        n.setString(AnalysisConstants.FILE_TYPE,fileType);
        n.setInt(AnalysisConstants.FILE_NODE_FILE_GROUP, fileGroupNumber);
        n.setBoolean(AnalysisConstants.IS_EXPANDED, false);
        n.setBoolean(AnalysisConstants.IS_FILE_NODE, true);
        n.set(AnalysisConstants.JAVA_AWT_IMAGE, AnalysisUtilities.getImage(fobj));
        n.setString(AnalysisConstants.LABEL, fobj.getNameExt());
        n.setString(AnalysisConstants.XML_FILENAME, fobj.getNameExt());
        n.set(AnalysisConstants.FILE_OBJECT, fobj);

        // "Schema file containing usages of XYZ"
        AnalysisUtilities.ToolTipLine topLine = new AnalysisUtilities.ToolTipLine(
                MessageFormat.format(
                NbBundle.getMessage(ComplexTypeDerivationsReader.class,
                "LBL_Xml_File_With_Usages"),new Object[]{
            AnalysisUtilities.getXmlFileTypeDisplayName(fileType),
            queryNode.getString(AnalysisConstants.LABEL)}),
                100,
                Color.BLACK.getRGB(),
                AnalysisUtilities.ToolTipLine.HorizontalAlignment.CENTER);
        AnalysisUtilities.ToolTipLine typeLine =
                new AnalysisUtilities.ToolTipLine(
                FileUtil.getFileDisplayName(fobj),
                100,
                Color.BLACK.getRGB(),
                AnalysisUtilities.ToolTipLine.HorizontalAlignment.CENTER);
        String toolTip = AnalysisUtilities.createHTMLToolTip(
                new AnalysisUtilities.ToolTipLine[] {topLine, typeLine});

        n.setString(AnalysisConstants.TOOLTIP,
                toolTip);
        return n;
    }
    /**
     *
     *
     */
    public static Node createNode(Graph graph,
            Component comp,
            UIHelper uiHelper,
            boolean isPrimitive,
            List<Component> componentsInGraph,
            Component queryComponent,
            int fileGroupNumber
            ){
        Node n = graph.addNode();
        if (componentsInGraph != null){
            componentsInGraph.add(comp);
        }
//        DisplayInfo dInfo = div.getDisplayInfo(comp);
        org.openide.nodes.Node displayNode = uiHelper.getDisplayNode(comp);
        
        // if the queryComponent node is a primitive,
        // check if the usage node is also a primitive, i.e.,
        // from the same model (the W3c Schema model)
        if (isPrimitive){
            if (comp.getModel() ==
                    queryComponent.getModel()) {
                n.setBoolean(AnalysisConstants.IS_PRIMITIVE, true);
            }
        }
        n.setBoolean(AnalysisConstants.IS_PRIMITIVE, false);
        n.setString(AnalysisConstants.LABEL, displayNode.getName());
        n.setString(AnalysisConstants.COMPONENT_TYPE_NAME, displayNode.getShortDescription() );
        n.set(AnalysisConstants.XAM_COMPONENT, comp  );
        n.set(AnalysisConstants.OPENIDE_NODE, displayNode  );
        n.setInt(AnalysisConstants.FILE_GROUP, fileGroupNumber);
        n.set(AnalysisConstants.JAVA_AWT_IMAGE, displayNode.getIcon(BeanInfo.ICON_COLOR_16x16));
        
        return n;
    }
    
    
}
