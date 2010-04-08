/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

/*
 * GraphHelper.java
 *
 * Created on January 29, 2007, 4:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.refactoring.ui;

import java.awt.Color;
import java.awt.Image;
import java.beans.BeanInfo;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.xml.nbprefuse.AnalysisConstants;
import org.netbeans.modules.xml.refactoring.FauxRefactoringElement;
import org.netbeans.modules.xml.refactoring.spi.AnalysisUtilities;
import org.netbeans.modules.xml.refactoring.spi.UIHelper;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 *
 * @author Sonali
 */
public class GraphHelper {
    
        private static final ImageIcon FIND_USAGES_ICON = ImageUtilities.loadImageIcon(
             "org/netbeans/modules/refactoring/api/resources/" + 
             "findusages.png", false);
    
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
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.USER_OBJECT,
                Object.class);
     //   FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.OPENIDE_NODE,
              //  org.openide.nodes.Node.class);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.JAVA_AWT_IMAGE,
                Image.class);
        FIND_USAGES_NODES_SCHEMA.addColumn(AnalysisConstants.REFACTORING_ELEMENT,
                RefactoringElement.class);
        
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
    
    Graph graph;
    Referenceable queryComponent;
    boolean isPrimitive;
    
    /** Creates a new instance of GraphHelper */
    public GraphHelper(Referenceable query) {
        this.queryComponent = query;
    }
    
    public Graph loadGraph(ArrayList<TreeElement> elements) {
        graph = new Graph(true);        // isDirected
        graph.getNodeTable().addColumns(FIND_USAGES_NODES_SCHEMA);
        graph.getEdgeTable().addColumns(FIND_USAGES_EDGES_SCHEMA);
        
        List componentsInGraph = new ArrayList();
        List<FileObject> files = new ArrayList<FileObject>();
        Map<FileObject, Node> fileNodes = new Hashtable<FileObject, Node>();
        int fileGroupNumber = 0;
        int usagesCount = 0;
        
        Node queryNode =  createQueryNode(graph, queryComponent, false,componentsInGraph, false, fileGroupNumber);
        
   
         // *****************************
        // *** FILE NODE
        // *****************************
        // create file node and attach it to the query node
        for(int i =0; i < elements.size(); i++ ){
      
            TreeElement usageNode = elements.get(i);
            
            //get the RefactoringElement
            RefactoringElement usageElement =(RefactoringElement)usageNode.getUserObject();
            
             //For the 0 usages case, we get a FauxTreeElement. Dont graph this map 
            //the only way of finding its a faux element is by looking at its composite object
           
            if (usageElement.getLookup().lookup(SimpleRefactoringElementImplementation.class)!=null){
                break;
            }
               
            
            //First lets create a file node
            //one file node per file object
            
            FileObject fo = usageElement.getParentFile();
            Node parent = null;
            if( !(files.contains(fo)) ) {
                Node fileNode = createFileNode(graph, queryComponent, fo, queryNode, ++fileGroupNumber);

              // create a special edge from the file node to the query node
              //  that will be visible when the file node is collapsed
              //  This edge uses the default prefuse renderer, e.g.,
              //    a small solid arrow head
              //  When the file node is expanded, this edge will be
              //  hidden.  See FindUsagesFocusControl (double click)
                  Edge fileEdge = graph.addEdge(fileNode,queryNode);
                  fileEdge.setString(AnalysisConstants.EDGE_TYPE, AnalysisConstants.FILE_EDGE_TYPE);
                  fileEdge.setInt(AnalysisConstants.FILE_GROUP, fileGroupNumber);

                  files.add(fo);
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
                
                //we dont want to draw grap beyond the file unlike the Jtree that goes all the way to project
                if( (userObject instanceof FileObject) ){
                    break;
                }
                if(componentsInGraph.contains(userObject)){
                    //there's already a node for this
                    pn= findDup(graph,userObject);
                    assert pn !=null:"Cannot find node for User Object" + userObject;
                }
                
                if(pn == null){
                    pn = createNode(leaf, graph, componentsInGraph, queryComponent, fileGroupNumber);
                }
                
                //// To find out if this tree element is a usage node, we would need to check if there's a corresponding
                //// refactoring element. only leaf nodes have corresponding refactoring elements
                AnalysisUtilities.ToolTipLine topLine = null;
                if(userObject instanceof RefactoringElement){
                    pn.setBoolean(AnalysisConstants.IS_USAGE_NODE, true);
                    pn.set(AnalysisConstants.REFACTORING_ELEMENT, (RefactoringElement)userObject);
                    
                    topLine = new AnalysisUtilities.ToolTipLine((MessageFormat.format(NbBundle.getMessage(GraphHelper.class, "LBL_Uses_Component"),
                            new Object[] {queryNode.getString(AnalysisConstants.LABEL) })),
                            100,
                            Color.BLACK.getRGB(),
                            AnalysisUtilities.ToolTipLine.
                            HorizontalAlignment.CENTER);
                    // Connect this usage node to the Query Node
                    // with the appropriate edge (composition or reference)
                     Component obj = ((RefactoringElement)userObject).getLookup().lookup(Component.class);
                     if(obj!=null)
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
                addApppropriateEdge(graph, child, pn, fileGroupNumber, null);
                child = pn;
                leaf = leaf.getParent(true);
            }
            
            //Connect last node to file node
            addApppropriateEdge(graph, child, parent, fileGroupNumber, null);
        }
        
        return graph;
        
    }
    
      
   
    
    private static Node createQueryNode(Graph graph, Referenceable query, boolean isPrimitive, List<Component> componentsInGraph,
            boolean showOnlyDerivations, int fileGroupNumber  ) {
        
        Node queryNode = null;
        Component queryComponent = null;
        if (query instanceof Component) {
            queryComponent = (Component) query;
        } else {
            if (query instanceof DocumentModel) {
                queryComponent = ((DocumentModel) query).getRootComponent();
            }
        }
        String name = "";
        if (query instanceof Named){
                name = ((Named)Named.class.cast(query)).getName();
        } else {
            //TEMP hack :: Since I dont have access to UI helper, the only way to get the display node is to call
            //the tree element factory
            TreeElement elem = TreeElementFactory.getTreeElement(queryComponent);
            name =elem.getText(true);
        }
        
        
        queryNode = graph.addNode();
        queryNode.setBoolean(AnalysisConstants.IS_PRIMITIVE, isPrimitive);
        queryNode.setString(AnalysisConstants.LABEL, name);
        queryNode.setString(AnalysisConstants.COMPONENT_TYPE_NAME, "" ); //NOI18N
        queryNode.set(AnalysisConstants.USER_OBJECT, query  );
        queryNode.setInt(AnalysisConstants.FILE_GROUP, fileGroupNumber);
       
        
        queryNode.setBoolean(AnalysisConstants.IS_QUERY_NODE, true);
        // unset the FILE_GROUP because this node is always visible
        queryNode.setInt(AnalysisConstants.FILE_GROUP,-1);
        // reset IS_PRIMITIVE in case it is
        queryNode.setBoolean(AnalysisConstants.IS_PRIMITIVE, isPrimitive);
        
        AnalysisUtilities.ToolTipLine topLine = new AnalysisUtilities.ToolTipLine((
                showOnlyDerivations?
                    NbBundle.getMessage(GraphHelper.class,
                "LBL_Base_Complex_Type"):
                    NbBundle.getMessage(GraphHelper.class,
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
        String toolTip =AnalysisUtilities.createHTMLToolTip(new AnalysisUtilities.ToolTipLine[] {
            topLine, typeLine});
        
        queryNode.setString(AnalysisConstants.TOOLTIP, toolTip);
        return queryNode;
    }
    
     private static Node createNode(TreeElement displayNode, Graph graph, List componentsInGraph, Referenceable queryComponent, int fileGroupNumber){
        Node n = graph.addNode();
        
        if(componentsInGraph !=null) {
            componentsInGraph.add(displayNode.getUserObject());
        }
     
        n.setBoolean(AnalysisConstants.IS_PRIMITIVE, false);
        n.setString(AnalysisConstants.LABEL, displayNode.getText(true));
        n.set(AnalysisConstants.USER_OBJECT,displayNode.getUserObject()  );
        n.setInt(AnalysisConstants.FILE_GROUP, fileGroupNumber);

        if (displayNode.getIcon() != null) {
          n.set(AnalysisConstants.JAVA_AWT_IMAGE,( (ImageIcon)displayNode.getIcon()).getImage());
        }
        
        return n;
    }
    
        
    private static String getCompTypeDisplayName(final Node pn) {
        String compType = null;
        if (pn.canGetBoolean(AnalysisConstants.IS_PRIMITIVE) && pn.getBoolean(AnalysisConstants.IS_PRIMITIVE)){
            compType = NbBundle.getMessage(GraphHelper.class, "LBL_Primitive_Type");
        } else {
            compType = pn.getString(AnalysisConstants.COMPONENT_TYPE_NAME);
        }
        return compType;
    }
    
    /**
     * Adds a Reference edge or Generalization edge from
     * "from" node to the queryNode
     * or adds composition edge if edgeType is null
     *
     */
    private static void addApppropriateEdge(Graph graph, Node from, Node queryNode, int fileGroupNumber, Type edgeType){
        Edge edge = graph.addEdge(from, queryNode);
//        edge.setBoolean(AnalysisConstants.SHOW, isVisible);
        edge.setInt(AnalysisConstants.FILE_GROUP, Integer.valueOf(fileGroupNumber));
        if ( edgeType == Type.GENERALIZATION){
            edge.setString(AnalysisConstants.EDGE_TYPE, AnalysisConstants.GENERALIZATION);
        } else if ( edgeType == Type.REFERENCE){
            edge.setString(AnalysisConstants.EDGE_TYPE,
                    AnalysisConstants.REFERENCE);
            from.setString(AnalysisConstants.LABEL,
                    MessageFormat.format(NbBundle.getMessage(
                    GraphHelper.class,
                    "LBL_References_Ref"),
                    new Object[]
            {queryNode.getString(AnalysisConstants.LABEL)}));
        } else {
            edge.setString(AnalysisConstants.EDGE_TYPE, AnalysisConstants.COMPOSITION);
        }
    }
    
    private static Node createFileNode(Graph graph, Referenceable queryComp,
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
        if (fobj != null){
            n.setString(
                    AnalysisConstants.LABEL, fobj.getName()+"."+fobj.getExt());  // NOI18N
            n.setString(
                    AnalysisConstants.XML_FILENAME,
                    fobj.getName()+"."+fobj.getExt());  // NOI18N
            n.set(AnalysisConstants.FILE_OBJECT, fobj);
            
            // "Schema file containing usages of XYZ"
            AnalysisUtilities.ToolTipLine topLine = new AnalysisUtilities.ToolTipLine(
                    MessageFormat.format(
                    NbBundle.getMessage(GraphHelper.class,
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
        }
        return n;
    }
    
    
     private static Node findDup(Graph graph, Object sc){
        Iterator it = graph.nodes();
        while (it.hasNext()){
            Node n= Node.class.cast(it.next());
            Object nodeSC = n.get(AnalysisConstants.USER_OBJECT);
            if ((n.canGetBoolean(AnalysisConstants.IS_FILE_NODE) && n.getBoolean(AnalysisConstants.IS_FILE_NODE) == false)&& nodeSC != null){
                if (nodeSC == sc){
                    return n;
                }
            }
        }
        return null;
    }
    
}
