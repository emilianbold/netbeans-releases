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
 * QuerySubstitutionGroupsReader.java
 *
 * Created on October 26, 2005, 11:27 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring.query.readers;


import java.awt.Color;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.xml.nbprefuse.AnalysisConstants;
import org.netbeans.modules.xml.refactoring.RefactoringManager;
import org.netbeans.modules.xml.refactoring.spi.UIHelper;
import org.netbeans.modules.xml.refactoring.ui.util.AnalysisUtilities;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Component;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 *
 * @author Jeri Lockhart
 */
public class QuerySubstitutionGroupsReader  {
    
    private SchemaModel model;
//    private SchemaComponent query;
    
//    private QuerySchemaComponentNode headElementNode;
    // Map  -  Key is HeadElement, Value is List of Global elements in Subst Grp
    private Map<GlobalElement,List<GlobalElement>> sGroups;
    
    
    public static final prefuse.data.Schema FIND_USAGES_NODES_SCHEMA = new prefuse.data.Schema();   // prefuse graph schema
    static {
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.LABEL, String.class, AnalysisConstants.EMPTY_STRING);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.ID, String.class, AnalysisConstants.EMPTY_STRING);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.COMPONENT_TYPE_NAME, String.class, AnalysisConstants.EMPTY_STRING);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.ELEMENT_TYPE, String.class, AnalysisConstants.EMPTY_STRING);    // type of a GE, LE, GA, or LA
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.XML_FILENAME, String.class, AnalysisConstants.EMPTY_STRING);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.TOOLTIP, String.class, AnalysisConstants.EMPTY_STRING); // name
//        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.SCHEMA_COMPONENT, SchemaComponent.class);  // Global Complex Type, etc
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.XAM_COMPONENT, Component.class);  // Global Complex Type, etc
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.IS_PRIMITIVE, boolean.class, false);  // is builtin type
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.IS_QUERY_NODE, boolean.class, false);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.IS_USAGE_NODE, boolean.class, false);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.IS_HEAD_ELEMENT, boolean.class, false);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.OPENIDE_NODE,
                org.openide.nodes.Node.class);
    }
    
    public static final prefuse.data.Schema FIND_USAGES_EDGES_SCHEMA = new prefuse.data.Schema();   // prefuse graph schema
    static {
        FIND_USAGES_EDGES_SCHEMA.addColumn(AnalysisConstants.LABEL, String.class, AnalysisConstants.EMPTY_STRING);
        FIND_USAGES_EDGES_SCHEMA.addColumn(AnalysisConstants.EDGE_TYPE, String.class, AnalysisConstants.EMPTY_STRING);    //  "file-edge-type" "generalization", "reference" "composition"
        FIND_USAGES_EDGES_SCHEMA.addColumn(AnalysisConstants.TOOLTIP, String.class, AnalysisConstants.EMPTY_STRING);
    }
    
    
    /**
     * Creates a new instance of QuerySubstitutionGroupsReader
     */
    public QuerySubstitutionGroupsReader(SchemaModel model) {
        this.model = model;
    }
    
    
    public Graph loadGraph() {
        Graph graph = new Graph(true);        // isDirected
        graph.getNodeTable().addColumns(FIND_USAGES_NODES_SCHEMA);
        graph.getEdgeTable().addColumns(FIND_USAGES_EDGES_SCHEMA);
        // get global elements
        //  if GE has SubstitutionGroup (SG)
        //  create new group entry in map
        //  add GE to list
        sGroups = new HashMap<GlobalElement,List<GlobalElement>>();
        Collection<GlobalElement> ges = model.getSchema().getElements();
        for (GlobalElement ge:ges){
            NamedComponentReference<GlobalElement> headElemRef = ge.getSubstitutionGroup();
            // TODO add stats to status area about broken refs
            //  ref can be broken
            // headElemRef.isBroken()
            // headElemRef.get() == null
            if (headElemRef != null){
                GlobalElement headElem = headElemRef.get();
                if (headElem != null){
                    List<GlobalElement> members = sGroups.get(headElem);
                    if (members == null){
                        members = new ArrayList<GlobalElement>();
                        sGroups.put(headElem, members);
                    }
                    members.add(ge);
                } else {
                    ErrorManager.getDefault().log(ErrorManager.ERROR,"QuerySubstitutionGroupsReader.loadGraph ignoring GE w/ invalid substGrp ref " + ge.getName());
                }
            }
        }
        int numGroups = sGroups.size();
        
        if (numGroups == 1) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(QuerySubstitutionGroupsReader.class,
                    "LBL_SubstitutionGroup_Found"));
        } else {
            StatusDisplayer.getDefault().setStatusText(
                    MessageFormat.format(NbBundle.getMessage(QuerySubstitutionGroupsReader.class,
                    "LBL_SubstitutionGroups_Found"),
                    new Object[] {numGroups,
            }
            ));
        }
        // build graph
        Set<GlobalElement> headElems = sGroups.keySet();
        for (GlobalElement head: headElems){
            List<GlobalElement> geList = sGroups.get(head);
            // this shouldn't happen
            assert geList != null:"Error is finding substitution group for "+ head;
            Node headNode = createHeadElementNode(graph, head);
            for (GlobalElement ge: geList){
                Node elemNode = createNode(graph, ge);
                
//                elemNode.setString(AnalysisConstants.TOOLTIP,
//                        MessageFormat.format(
//                        NbBundle.getMessage(QuerySubstitutionGroupsReader.class,
//                        "LBL_Substitutable_Element"),  // e.g., "Substituable for xxx"
//                        new Object[] {headNode.getString(AnalysisConstants.LABEL)}));
                
                AnalysisUtilities.ToolTipLine topLine = null;
                topLine = new AnalysisUtilities.ToolTipLine((
                        MessageFormat.format(
                        NbBundle.getMessage(QuerySubstitutionGroupsReader.class,
                        "LBL_Substitutable_Element"),  // e.g., "Substituable for xxx"
                        new Object[] {headNode.getString(AnalysisConstants.LABEL)})),
                        100,
                        Color.BLACK.getRGB(),
                        AnalysisUtilities.ToolTipLine.HorizontalAlignment.CENTER);
                String compType = elemNode.getString(AnalysisConstants.COMPONENT_TYPE_NAME);
                AnalysisUtilities.ToolTipLine typeLine = new AnalysisUtilities.ToolTipLine(compType,
                        100,
                        Color.BLACK.getRGB(),
                        AnalysisUtilities.ToolTipLine.HorizontalAlignment.CENTER);
                String toolTip = AnalysisUtilities.createHTMLToolTip(new AnalysisUtilities.ToolTipLine[] {topLine, typeLine});
                elemNode.setString(AnalysisConstants.TOOLTIP, toolTip);
                
                Edge edge = createEdge(graph, elemNode, headNode);
            }
        }
//        GraphUtilities.dumpGraph(graph);
        return graph;
    }
    
    
    
    
    
    private Node createHeadElementNode(Graph graph, GlobalElement head) {
        Node headElementNode = createNode(graph, head);
        headElementNode.setBoolean(AnalysisConstants.IS_HEAD_ELEMENT, true);
        
//        headElementNode.setAttribute(AnalysisConstants.TOOLTIP,
//                NbBundle.getMessage(QuerySubstitutionGroupsReader.class,
//                "LBL_Head_Element"));
        
        AnalysisUtilities.ToolTipLine topLine = new AnalysisUtilities.ToolTipLine((
                NbBundle.getMessage(QuerySubstitutionGroupsReader.class,
                "LBL_Head_Element")),
                100,
                Color.BLACK.getRGB(),
                AnalysisUtilities.ToolTipLine.HorizontalAlignment.CENTER);
        
        AnalysisUtilities.ToolTipLine typeLine = new AnalysisUtilities.ToolTipLine(
                NbBundle.getMessage(QuerySubstitutionGroupsReader.class,"LBL_Global_Element"),
                100,
                Color.BLACK.getRGB(),
                AnalysisUtilities.ToolTipLine.HorizontalAlignment.CENTER);
        
        String toolTip = AnalysisUtilities.createHTMLToolTip(new AnalysisUtilities.ToolTipLine[] {topLine, typeLine});
        headElementNode.setString(AnalysisConstants.TOOLTIP, toolTip);
        
        return headElementNode;
        
        
    }
    
    private Edge createEdge(Graph graph, Node elemNode, Node headNode){
//
//        Edge edge = graph.addEdge(queryNode, fileNode);
        Edge edge = graph.addEdge(elemNode, headNode);
        edge.setString(AnalysisConstants.EDGE_TYPE, AnalysisConstants.GENERALIZATION);
        return edge;
    }
    
    private Node createNode(Graph graph, GlobalElement ge){
        Node n = graph.addNode();
        UIHelper uiHleper = RefactoringManager.getInstance().getTargetComponentUIHelper(ge);
        org.openide.nodes.Node displayNode = uiHleper.getDisplayNode(ge);
        n.set(AnalysisConstants.OPENIDE_NODE, displayNode  );
        n.set(AnalysisConstants.XAM_COMPONENT, ge);
        n.setString(AnalysisConstants.COMPONENT_TYPE_NAME, displayNode.getShortDescription());
        n.setString(AnalysisConstants.LABEL, displayNode.getName());
        n.setString(AnalysisConstants.ELEMENT_TYPE, displayNode.getDisplayName());
        
        return n;
    }
    
    
    
}
