/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.ImageIcon;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.xml.nbprefuse.AnalysisConstants;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.refactoring.spi.UIHelper;
import org.netbeans.modules.xml.refactoring.spi.AnalysisUtilities;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.refactoring.SchemaUIHelper;
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
    
     public enum Type {
        REFERENCE, GENERALIZATION;
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
   //     FindUsageResult result = RefactoringManager.getInstance().findUsages(baseCT,
  //              baseCT.getModel().getSchema());
        Collection<RefactoringElement> elements = SharedUtils.findUsages(baseCT, baseCT.getModel().getSchema());
                
       // UIHelper queryUIHelper = getUIHelper(baseCT);
        
        ArrayList<TreeElement> nodes = new ArrayList<TreeElement>();
        for (RefactoringElement element: elements) {
            TreeElement previewNode = TreeElementFactory.getTreeElement(element);
            if(previewNode != null)
                nodes.add(previewNode);
        }
        
        
        Node queryNode  = createQueryNode(graph,
                baseCT,
                false,
                componentsInGraph,
                false,
                fileGroupNumber);
        
        SchemaUIHelper uiHelper = new SchemaUIHelper();
        displayNode = uiHelper.getDisplayNode(baseCT);
        
        queryNode.set(AnalysisConstants.COMPONENT_TYPE_NAME,
                displayNode.getShortDescription()); // comp type
        queryNode.set(AnalysisConstants.XAM_COMPONENT,
                baseCT);
        queryNode.setString(AnalysisConstants.LABEL, displayNode.getName());
        queryNode.setString(AnalysisConstants.ELEMENT_TYPE,
                displayNode.getDisplayName());  // element type
        
        loadGraph(true, //derivations of CTs only
                graph, 
                queryNode, 
                baseCT, 
                nodes,
                false, //is primitive
                derivationsCount);
        //  Map of SourceGroups and their packages
     /*   Map<SourceGroup, Map<FileObject,Set<UsageGroup>>> sortedUses =
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
        }*/
        
        
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
    
    
    
    
  /*  private UIHelper getUIHelper(Referenceable ref) {
        return RefactoringManager.getInstance().getTargetComponentUIHelper(ref);
    }*/

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
    /*private void addUsagesToGraph(boolean ctDerivationsOnly,
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
    }// end addUsagesToGraph()*/
    
    
    
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
            int fileGroupNumber, Type edgeType){
        Edge edge = graph.addEdge(from, queryNode);
//        edge.setBoolean(AnalysisConstants.SHOW, isVisible);
        
        edge.setInt(AnalysisConstants.FILE_GROUP, Integer.valueOf(fileGroupNumber));
        if ( edgeType == Type.GENERALIZATION){
            edge.setString(AnalysisConstants.EDGE_TYPE,
                    AnalysisConstants.GENERALIZATION);
        } else if ( edgeType == Type.REFERENCE){
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
    
    private Node findDup(Graph graph, Object sc){
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
            boolean isPrimitive,
            List componentsInGraph,
            boolean showOnlyDerivations,
            int fileGroupNumber
            ) {
        //  TODO remove this temporary hack when UIHelper for query Component is available
        Node queryNode = null;
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
        String fileType = SharedUtils.getXmlFileType(fobj);
        Node n = graph.addNode();
        n.setString(AnalysisConstants.FILE_TYPE,fileType);
        n.setInt(AnalysisConstants.FILE_NODE_FILE_GROUP, fileGroupNumber);
        n.setBoolean(AnalysisConstants.IS_EXPANDED, false);
        n.setBoolean(AnalysisConstants.IS_FILE_NODE, true);
        n.set(AnalysisConstants.JAVA_AWT_IMAGE, SharedUtils.getImage(fobj));
        n.setString(AnalysisConstants.LABEL, fobj.getNameExt());
        n.setString(AnalysisConstants.XML_FILENAME, fobj.getNameExt());
        n.set(AnalysisConstants.FILE_OBJECT, fobj);

        // "Schema file containing usages of XYZ"
        AnalysisUtilities.ToolTipLine topLine = new AnalysisUtilities.ToolTipLine(
                MessageFormat.format(
                NbBundle.getMessage(ComplexTypeDerivationsReader.class,
                "LBL_Xml_File_With_Usages"),new Object[]{
            SharedUtils.getXmlFileTypeDisplayName(fileType),
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
            TreeElement displayNode,
            boolean isPrimitive,
            List componentsInGraph,
            Component queryComponent,
            int fileGroupNumber
            ){
        Node n = graph.addNode();
        
        if (componentsInGraph != null){
            componentsInGraph.add(displayNode.getUserObject());
        }
//        DisplayInfo dInfo = div.getDisplayInfo(comp);
       // org.openide.nodes.Node displayNode = uiHelper.getDisplayNode(comp);
        
        // if the queryComponent node is a primitive,
        // check if the usage node is also a primitive, i.e.,
        // from the same model (the W3c Schema model)
        if (isPrimitive){
            if(displayNode.getUserObject() instanceof Component ){
                Component comp = (Component)displayNode.getUserObject();
                if (comp.getModel() ==    queryComponent.getModel()) {
                    n.setBoolean(AnalysisConstants.IS_PRIMITIVE, true);
                }
            }
        }
        n.setBoolean(AnalysisConstants.IS_PRIMITIVE, false);
            n.setString(AnalysisConstants.LABEL, displayNode.getText(true));
        n.setString(AnalysisConstants.COMPONENT_TYPE_NAME, displayNode.getText(true) );
       
        if(displayNode.getUserObject() instanceof Component)
            n.set(AnalysisConstants.XAM_COMPONENT, (Component)displayNode.getUserObject()  );
        else if(displayNode.getUserObject() instanceof RefactoringElement ) {
            Component comp = (Component)( (RefactoringElement)displayNode.getUserObject()).getLookup().lookup(Component.class);
            n.set(AnalysisConstants.XAM_COMPONENT, comp);
        } else
            //no clue what kind of obj we got
            n.set(AnalysisConstants.XAM_COMPONENT, null);
        
        //We no longer have a ide Node to represent the usage component
        //n.set(AnalysisConstants.OPENIDE_NODE, displayNode  );
        n.setInt(AnalysisConstants.FILE_GROUP, fileGroupNumber);
        n.set(AnalysisConstants.JAVA_AWT_IMAGE,( (ImageIcon)displayNode.getIcon()).getImage());
        
        return n;
    }
    
    public void loadGraph(boolean ctDerivationsOnly, Graph graph, Node queryNode, Component queryComponent, ArrayList<TreeElement> elements, boolean isPrimitive, List<Integer> derivationsCount) {
        List componentsInGraph = new ArrayList();
        List<FileObject> files = new ArrayList<FileObject>();
        Map<FileObject, Node> fileNodes = new Hashtable<FileObject, Node>();
        int fileGroupNumber = 0;
        int usagesCount = 0;
       
        // *****************************
        // *** FILE NODE
        // *****************************
        // create file node and attach it to the query node
        for(int i =0; i < elements.size(); i++ ){
      
            TreeElement usageNode = elements.get(i);
            
            //get the RefactoringElement
            RefactoringElement usageElement =(RefactoringElement)usageNode.getUserObject();
            
            if (ctDerivationsOnly){
                Component usageComponent = usageElement.getLookup().lookup(Component.class);
                if (usageComponent instanceof ComplexContentRestriction){
                    derivationsCount.set(1,derivationsCount.get(1)+1);
                } else if (usageComponent instanceof ComplexExtension){
                    derivationsCount.set(0,derivationsCount.get(0)+1);
                } else{
                    continue;   // continue to next Item
                }
            }           
            //Next, lets create a file node and attach it to the query node
            //there should be one file node per file object
            
            FileObject fo = usageElement.getParentFile();
            Node parent = null;
            if( !(files.contains(fo)) ) {
                Node fileNode = createFileNode(graph, queryComponent, fo, queryNode, ++fileGroupNumber);
                parent = fileNode;
                fileNodes.put(fo, fileNode);
            } else {
                parent = fileNodes.get(fo);
            }
            
            Node child = null;
            TreeElement leaf = usageNode;
            
            while( (leaf.getParent(true)) instanceof TreeElement ) {
                Node pn = null;
                
                Object userObject = leaf.getUserObject();
                
                //we dont want to draw grap beyond the file
                if( (userObject instanceof FileObject) ){
                    break;
                }
                                
                if(componentsInGraph.contains(userObject)){
                    //there's already a node for this
                    pn= findDup(graph,userObject);
                    assert pn !=null:"Cannot find node for User Object" + userObject;
                }
                
                if(pn == null){
                    pn =  createNode(graph, leaf, isPrimitive, componentsInGraph, queryComponent, fileGroupNumber);
                    
                }
                
                //// To find out if this tree element is a usage node, we would need to check if there's a corresponding
                //// refactoring element. only leaf nodes have corresponding refactoring elements
                AnalysisUtilities.ToolTipLine topLine = null;
                if(userObject instanceof RefactoringElement){
                    pn.setBoolean(AnalysisConstants.IS_USAGE_NODE, true);
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
                     Component obj= ((RefactoringElement)userObject).getLookup().lookup(Component.class);
                     if(obj !=null)
                         addApppropriateEdge(graph, pn, queryNode, fileGroupNumber,Type.REFERENCE );
                     else
                         addApppropriateEdge(graph, pn, queryNode, fileGroupNumber, null);
                     
                    child=pn;
                    leaf = leaf.getParent(true);
                    continue;
                }
                                     
               AnalysisUtilities.ToolTipLine typeLine =
                        new AnalysisUtilities.ToolTipLine(getCompTypeDisplayName(pn),
                        100,
                        Color.BLACK.getRGB(),
                        AnalysisUtilities.ToolTipLine.
                        HorizontalAlignment.CENTER);
                String toolTip = AnalysisUtilities.createHTMLToolTip(
                        new AnalysisUtilities.ToolTipLine[] {topLine, typeLine});
                pn.setString(AnalysisConstants.TOOLTIP, toolTip);
                
                
                // connect it to its parent
                addCompositionEdge(graph, child, pn, fileGroupNumber);
                child = pn;
                leaf = leaf.getParent(true);
            }
            
            AnalysisUtilities.ToolTipLine topLine = null;
                  // connect last node to file node
            AnalysisUtilities.ToolTipLine typeLine =
                            new AnalysisUtilities.ToolTipLine(child.getString(AnalysisConstants.COMPONENT_TYPE_NAME),
                            100,
                            Color.BLACK.getRGB(),
                            AnalysisUtilities.ToolTipLine.
                            HorizontalAlignment.CENTER);
                    String toolTip = AnalysisUtilities.createHTMLToolTip(
                            new AnalysisUtilities.ToolTipLine[] {topLine, typeLine});
                    child.setString(AnalysisConstants.TOOLTIP, toolTip);
                    
                    // connect the node to the File Node with compositon edge
                    Edge fileCompositionEdge = graph.addEdge(child,parent);
                    fileCompositionEdge.setString(AnalysisConstants.EDGE_TYPE,
                            AnalysisConstants.COMPOSITION);
                    // it's part of the group of nodes and edges that are
                    // visible or hidden, depending on whether the file node
                    // is expanded or collapsed
                    fileCompositionEdge.setInt(AnalysisConstants.FILE_GROUP,
                            fileGroupNumber);
          
        }
        
    }
    
  
}
