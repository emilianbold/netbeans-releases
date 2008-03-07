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

package org.netbeans.modules.xml.schema.refactoring.ui;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.xml.nbprefuse.AnalysisConstants;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.visitor.FindUsageVisitor;
import org.netbeans.modules.xml.schema.model.visitor.Preview;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.util.ColorLib;

/**
 *
 * @author Jeri Lockhart
 */
public abstract class QueryUtilities {
    
    /**
     *  Get all the files in the project for the SchemaModel
     *  @param  model The SchemaModel that is contained in the project
     *  @param  sourceGroup The source group, such as JavaProjectConstants.SOURCES_TYPE_JAVA
     *     or Sources.TYPE_GENERIC
     *  @return  a List of  SourceGroups
     *
     */
    
    public static List<SourceGroup> getProjectSourceGroups(final SchemaModel model,
            final String sourceGroupName){
        if (model == null || sourceGroupName == null){
            return null;
        }
        List<SourceGroup>result = new ArrayList<SourceGroup>();
        Project proj = getProject(model);
        //fix for issue 128660
        if(proj == null)
            return null;
        
        List<String>sourceGroupTypeList = new ArrayList<String>();
        sourceGroupTypeList.add(sourceGroupName);
        
        for(String type: sourceGroupTypeList){
            SourceGroup[] srcGrps = ProjectUtils.getSources(proj).getSourceGroups(type);
            if(srcGrps != null){
                for(SourceGroup srcGrp : srcGrps) {
                    result.add(srcGrp);
                }
            }
        }
        return result;
    }
    
    /**
     *
     *
     */
    private static Project getProject(final Model model){
        if (model == null ){
            return null;
        }
        FileObject fileObj = (FileObject) model.getModelSource().getLookup().lookup(FileObject.class);
        return FileOwnerQuery.getOwner(fileObj);
        
    }
    
    
    
    
    /**
     *  Use FindUsageVisitor to get the usages Preview for a Named
     *
     */
    
    @SuppressWarnings("unchecked")
    public static Preview getUsagesPreview(Schema schema, NamedReferenceable ref){
        FindUsageVisitor usage = new FindUsageVisitor();
        return usage.findUsages(Collections.singletonList
                (schema), ref);
        
    }
    
    /**
     *  Creates an AbstractNode with the given label
     *  with Children.SortedArray and a String comparator
     *  Used by Analysis modules to create category container
     *  nodes for Global Complex Types, Global Elements, etc
     *
     *
     */
    public static AbstractNode createCategoryNode(AnalysisConstants.GlobalTypes type) {
        Children.SortedArray children = new Children.SortedArray();
        children.setComparator(new NodeComparator());
        AbstractNode catNode = new AbstractNode(children) {
            org.openide.nodes.Node node = null;
            private org.openide.nodes.Node getDelegateNode() {
                if(node==null) {
                    try {
                        node =  DataObject.find(Repository.getDefault().
                                getDefaultFileSystem().getRoot()).getNodeDelegate();
                    } catch (DataObjectNotFoundException ex) {
                    }
                }
                return node;
            };
            public Image getIcon(int type) {
                org.openide.nodes.Node n = getDelegateNode();
                return n!=null?n.getIcon(type):super.getIcon(type);
            }
            public Image getOpenedIcon(int type) {
                org.openide.nodes.Node n = getDelegateNode();
                return n!=null?n.getOpenedIcon(type):super.getOpenedIcon(type);
            }
        };
        String label = null;
        switch(type){
            case COMPLEX_TYPE:
                label =   NbBundle.getMessage(QueryUtilities.class,"LBL_GlobalComplexTypes");
                break;
            case SIMPLE_TYPE:
                label =   NbBundle.getMessage(QueryUtilities.class,"LBL_GlobalSimpleTypes");
                break;
            case PRIMITIVE:
                label =   NbBundle.getMessage(QueryUtilities.class,"LBL_Primitives");
                break;
            case ELEMENT:
                label =   NbBundle.getMessage(QueryUtilities.class,"LBL_GlobalElements");
                break;
            case GROUP:
                label =   NbBundle.getMessage(QueryUtilities.class,"LBL_GlobalGroups");
                break;
            case ATTRIBUTE:
                label =   NbBundle.getMessage(QueryUtilities.class,"LBL_GlobalAttributes");
                break;
            case ATTRIBUTE_GROUP:
                label =   NbBundle.getMessage(QueryUtilities.class,"LBL_GlobalAttributeGroups");
                break;
            case BASE_COMPLEX_TYPES:
                label =   NbBundle.getMessage(QueryUtilities.class,"LBL_Global_ComplexTypes_With_Derivations");
                break;
        }
        
        catNode.setName(label);
        return catNode;
    }
    
    
    /**
     * Returns a color palette of given size that cycles through
     * the hues of the HSB (Hue/Saturation/Brightness) color space.
     * @param size the size of the color palette
     * @param s the saturation value to use
     * @param b the brightness value to use
     * @param huesToExclude null or an array of hues to exclude from palette
     * @return the color palette
     */
    public static int[] getHSBPalette(int size, float s, float b,
            AnalysisConstants.HSBHues[] huesToExclude) {
        int[] cm = new int[size];
        int igen = 0;
        for ( int i=0; i<size; i++ ) {
            float h = 0;
            boolean goodHue = false;
            while(goodHue == false){
                h = ((float)igen++)/(size);
                if (huesToExclude != null) {
                    for (AnalysisConstants.HSBHues exH:huesToExclude){
                        goodHue = !isInColorFamily(h, exH);
                        if (goodHue == false){
                            break;
                        }
                    }
                } else {
                    goodHue = true;
                }
            }
            cm[i] = ColorLib.hsb(h,s,b);
        }
        return cm;
    }
    
    private static boolean isInColorFamily(float h, AnalysisConstants.HSBHues hue){
        if (hue == null){
            return false;
        }
        return (h >= hue.low() && h <= hue.high());
        
    }

    /**
     * Print out of prefuse graph for debugging
     *
     *
     */
    public static void dumpGraph(Graph graph){
//        if (graph == null){
//            return;
//        }
//        
//        Iterator nodeIt = graph.nodes();
//        Iterator edgeIt = graph.edges();
//        System.out.println("NODES:");
//        while (nodeIt.hasNext()){
//            Node node = Node.class.cast(nodeIt.next());
//            System.out.println(" ");
//            System.out.println(node.toString());
//            System.out.println("Node Attributes:");
//            Map map = node.getAttributes();
//            Set entries = map.entrySet();
//            for (Iterator i = entries.iterator(); i.hasNext();){
//                Entry entry = Entry.class.cast(i.next());
//                System.out.println(entry.getKey() + ": " + entry.getValue());
//            }
//            Iterator nodeEdgesIt = node.edges();
//            while(nodeEdgesIt.hasNext()){
//                Edge edge = Edge.class.cast(nodeEdgesIt.next());
//                System.out.println("First node: " + edge.getSourceNode().toString()
//                + " Second node: " + edge.getTargetNode().toString()
//                + " isDirected: "  + edge.isDirected()
//                );
//            }
//        }
//        System.out.println("EDGES:");
//        while (edgeIt.hasNext()){
//            Edge edge = Edge.class.cast(edgeIt.next());
//            System.out.println(" ");
//            System.out.println(edge.toString());
//            System.out.println("Edge Attributes:");
//            Map map = edge.getAttributes();
//            Set entries = map.entrySet();
//            for (Iterator i = entries.iterator(); i.hasNext();){
//                Entry entry = Entry.class.cast(i.next());
//                System.out.println(entry.getKey() + ": " + entry.getValue());
//            }
//            System.out.println("First Node: " + edge.getSourceNode().toString());
//            System.out.println("Second Node: " + edge.getTargetNode().toString());
//        }
//        
    }
    
    /**
     * @return a trimmed snippet of XML from the 1st line of the Schema Component
     * @param sc  a Schema Component
     *
     */
    public static String getTextForSchemaComponent(final Component comp){
        SchemaComponent sc = null;
        if (comp instanceof SchemaComponent){
            sc = SchemaComponent.class.cast(comp);
        }
        if (sc == null){
            return "";  //NOI18N
        }
        // TODO - if the line doesn't contain the target (query component name) string, keep searcing subsequent lines
        DataObject dobj = null;
        try {
            dobj = DataObject.find((FileObject) sc.getModel().getModelSource().getLookup().lookup(FileObject.class));
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        CloneableEditorSupport editor = (CloneableEditorSupport)dobj.getCookie(org.openide.cookies.EditorCookie.class);
        Line.Set s =editor.getLineSet();
        StyledDocument doc = editor.getDocument();
        
        int position = (int)sc.findPosition();
        int line = NbDocument.findLineNumber(doc, position);
        int col = NbDocument.findLineColumn(doc, position);
        Line xmlLine = s.getCurrent(line);
        String nodeLabel =   xmlLine.getText().trim();
        // substitute xml angle brackets <> for &lt; and &gt;
        Pattern lt = Pattern.compile("<"); //NOI18N
        Matcher mlt = lt.matcher(nodeLabel);
        nodeLabel = mlt.replaceAll("&lt;");  //NOI18N
        Pattern gt = Pattern.compile(">"); //NOI18N
        Matcher mgt = gt.matcher(nodeLabel);
        nodeLabel = mgt.replaceAll("&gt;");  //NOI18N
        return boldenRefOrType(nodeLabel);
    }
    
    // TODO get xml snippet for line that contains the
    //  query component name
    /**
     * If the label contains ref= or type=
     * the substring containing the named portion of the attribute
     * will be surrounded with html bold tags
     * e.g.,
     * input param <xsd:element ref="comment" minOccurs="0"/>
     * return <xsd:element ref="<b>comment</b>" minOccurs="0"/>
     *
     *
     */
    private static String boldenRefOrType(String label){
        // find index of type or ref
        // find 1st occurence of " from index
        // find 1st occurence of : after ", if any
        // insert <b>
        // find closing "
        // insert </b>
        int it = label.indexOf(" type"); //NOI18N
        if (it < 0){
            it = label.indexOf(" ref"); //NOI18N
        }
        if (it < 0){
            // no type or ref found
            return label;
        }
        int iq1 = label.indexOf('"',it);
        if (iq1 < it){
            // no begin quote
            return label;
        }
        int ic = label.indexOf(':',iq1);
//        if (ic < iq1){
//            // no colon
//        }
        int iq2 = label.indexOf('"', iq1+1);
        if (iq2 < iq1 || ic > iq2){
            // couldn't find closing quote for tag
            return label;
        }
        int ib1 = -1;
        if (ic > -1){
            ib1 = ic+1;
        } else {
            ib1 = iq1+1;
        }
        StringBuffer l = new StringBuffer(label);
        l.insert(ib1,"<b>");
        // the close quote has now been pushed right 3 spaces
        l.insert(iq2+3,"</b>");
        return l.toString();
        
    }
    
    
    /**
     * An array of ToolTipLine instances can be passed to
     * GraphUtilities.createHTMLToolTip()
     * ToolTipLine contains the text, the font size as a percentage of the default size,
     * the horizontal alignment (center, left, or right), and the font color.
     *
     *
     */
    public static class ToolTipLine {
        public enum HorizontalAlignment {
            CENTER("center"),   // NOI18N
            LEFT("left"),       // NOI18N
            RIGHT("right");     // NOI18N
            
            private final String name;
            HorizontalAlignment(String name) {
                this.name = name;
            }
            public String toString()   { return name; }
        }
        private String text ="";   //NOI18N
        private int fontSizePercentage = 100;
        private int rgbColor = Color.BLACK.getRGB();
        private HorizontalAlignment hAlign = HorizontalAlignment.CENTER;
        public ToolTipLine(String text, int fontSizePercentage, int rgbColor, HorizontalAlignment hAlign){
            
            this.text = text;
            this.fontSizePercentage = fontSizePercentage;
            this.rgbColor = rgbColor;
            this.hAlign = hAlign;
        }
        
        
        public ToolTipLine(String text,  int rgbColor){
            this.text = text;
            this.rgbColor = rgbColor;
        }
        
        
        public ToolTipLine(String text){
            this.text = text;
        }
        
        public String getHorizontalAlignmentAsString() {
            return this.hAlign.toString();
        }
        
        public String getText() {
            return " " + this.text + " "; //NOI18N
        }
        
        public String getFontSizePercentageAsString() {
            return String.valueOf(fontSizePercentage);
        }
        
        
        // example return value  "(145,123,000)"
        public String getColorAsRGBString() {
            Color color = new Color(rgbColor);
            StringBuffer str = new StringBuffer();
            str.append("(");    //NOI18N
            str.append(String.valueOf(color.getRed()));
            str.append(",");   //NOI18N
            str.append(String.valueOf(color.getGreen()));
            str.append(",");   //NOI18N
            str.append(String.valueOf(color.getBlue()));
            str.append(")");   //NOI18N
            return str.toString();
        }
        
        
    }
}

