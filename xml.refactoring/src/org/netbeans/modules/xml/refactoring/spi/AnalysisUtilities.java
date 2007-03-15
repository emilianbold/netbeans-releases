/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.refactoring.spi;

import java.awt.Color;
import java.awt.Image;
import java.beans.BeanInfo;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.tree.TreeModel;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.xml.nbprefuse.AnalysisConstants;
import org.netbeans.modules.xml.refactoring.CannotRefactorException;
import org.netbeans.modules.xml.refactoring.ui.ModelProvider;
import org.netbeans.modules.xml.refactoring.ui.RenameRefactoringUI;
import org.netbeans.modules.xml.refactoring.ui.ReferenceableProvider;
import org.netbeans.modules.xml.refactoring.ui.DeleteRefactoringUI;
import org.netbeans.modules.xml.refactoring.ui.FileRenameRefactoringUI;
import org.netbeans.modules.xml.refactoring.ui.views.WhereUsedView;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.util.ColorLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;

/**
 *
 * @author Jeri Lockhart
 */
public abstract class AnalysisUtilities {
    

    /**
     * Find the first CheckNode in the tree model that has the passed user object
     * @param model the TreeModel
     * @param object the user object
     * @returns CheckNode the check node that contains the user object, or null
     *    if none is found
     *
     */
  /*  public static CheckNode findCheckNode(TreeModel model, Object userObject){
        if (model == null || userObject == null){
            return null;
        }
        CheckNode root = (CheckNode)model.getRoot();
        if (root.getUserObject() == userObject){
            return root;
        }
        return processChildren(root, userObject);       
        
    }*/
    
  /*  private static CheckNode processChildren(CheckNode node, Object userObject){
        Enumeration en = node.children();
        while (en.hasMoreElements()){
            CheckNode child = (CheckNode)en.nextElement();
            Object currUserObject = child.getUserObject();
            if (currUserObject == userObject){
                return child;
            }
            return processChildren(child, userObject);
        }
        return null;
    }*/
    
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
     *  If there is only one file node, expand it
     *   otherwise, collapse them
     *  When a file node is expanded, all the schema component nodes
     *  in the file are shown.  The edge from the file node to the
     *  query node is hidden.
     *
     *  When a file node is collapsed, all the schema component nodes
     *  in the file are hidden.  The edge from the file node to the query
     *  node is shown.
     *
     *
     */
    public static void expandCollapseFileNodes(List<NodeItem> fileNodes){
        Predicate p = null;
        if (fileNodes == null ||  fileNodes.size() < 1 || fileNodes.get(0) == null){
            return;
        }
        Visualization vis = fileNodes.get(0).getVisualization();
        
        if (fileNodes.size() == 1){
            NodeItem n = fileNodes.get(0);
            n.setBoolean(AnalysisConstants.IS_EXPANDED, true);
            p = (Predicate)
            ExpressionParser.parse("["+AnalysisConstants.FILE_GROUP+"] = " +      //NOI18N
                    n.getInt(AnalysisConstants.FILE_NODE_FILE_GROUP));
            vis.setVisible(AnalysisConstants.GRAPH_GROUP, p, true);
            setFileEdgeVisible(n, false);
            
        } else {
            for(NodeItem n:fileNodes){
                n.setBoolean(AnalysisConstants.IS_EXPANDED, false);
                p = (Predicate)
                ExpressionParser.parse("["+AnalysisConstants.FILE_GROUP+"] = " +      //NOI18N
                        n.getInt(AnalysisConstants.FILE_NODE_FILE_GROUP));
                vis.setVisible(AnalysisConstants.GRAPH_GROUP, p, false);
                setFileEdgeVisible(n, true);
            }
        }
    }
    
    /**
     * Set the edge between the file node and query node
     * visible or not visible
     *
     *
     */
    private static void setFileEdgeVisible(final NodeItem fileNode, boolean visible){
        Iterator outEdges = fileNode.outEdges();
        while(outEdges.hasNext()){
            EdgeItem edge = EdgeItem.class.cast(outEdges.next());
            if (edge.getString(AnalysisConstants.EDGE_TYPE).equals(
                    AnalysisConstants.FILE_EDGE_TYPE)){
                edge.setVisible(visible);
            }
        }
    }
    
    /**
     * create String using HTML and CSS style tag.  Example:
     *"<html>
     *    <head>
     *       </head>
     *          <body>
     *             <p style='color: rgb(169,205,255);
     *                       text-align:center;
     *                       font-size:130%'>
     *   This is the top line of the tooltip
     *            </p>
     *             <p style='color: rgb(169,169,169);
     *                       text-align:center;
     *                       font-size:130%'>
     *   This is the second line of the tooltip.
     *            </p>
     *        </body>
     * </html>"
     */
    
    public static String createHTMLToolTip(ToolTipLine[] lines){
        if (lines == null){
            return null;
        }
        StringBuffer tooltip = new StringBuffer();
        tooltip.append("<html><head></head><body>");
        for (ToolTipLine l:lines) {
            if (l == null){
                continue;
            }
            tooltip.append("<p style='color: rgb");
            tooltip.append(l.getColorAsRGBString());
            tooltip.append(";text-align:");
            tooltip.append(l.getHorizontalAlignmentAsString());
            tooltip.append(";font-size:");
            tooltip.append(l.getFontSizePercentageAsString());
            tooltip.append("%'>");
            tooltip.append(l.getText());
            tooltip.append("</p>");
        }
        tooltip.append("</body></html>");
        return tooltip.toString();
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
            StringBuilder str = new StringBuilder();
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


