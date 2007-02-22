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
 * NodeFillColorAction.java
 *
 * Created on March 4, 2006, 7:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.nbprefuse;

import java.awt.Color;
import prefuse.Visualization;
import prefuse.action.assignment.ColorAction;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;

/**
 *
 * @author Jeri Lockhart
 */
public class NodeFillColorAction extends ColorAction {
    
    public NodeFillColorAction() {
        super(AnalysisConstants.GRAPH_GROUP_NODES, VisualItem.FILLCOLOR);
    }
    public int getColor(VisualItem item) {
        if (item.isHighlighted()){
            return AnalysisConstants.COLOR_HIGHLIGHT;
        }
        String fileName = null;
        if (item.canGetString(AnalysisConstants.XML_FILENAME)){
            fileName = item.getString(AnalysisConstants.XML_FILENAME);
        }
        TupleSet selected = 
                item.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS);
        boolean mouseover = false;
        if (item.canGetBoolean(AnalysisConstants.MOUSEOVER) &&
                item.getBoolean(AnalysisConstants.MOUSEOVER)){
                mouseover = true;
        }
        if (selected.containsTuple(item) || mouseover){
            return AnalysisConstants.COLOR_SELECTED_NODE;
//            return Color.BLACK.getRGB();
        }
        else if (fileName != null && fileName.length()>0) {
            return AnalysisConstants.COLOR_FILE_NODE;
        } else if (item.canGetBoolean(AnalysisConstants.IS_QUERY_NODE) &&
                item.getBoolean(AnalysisConstants.IS_QUERY_NODE)){
            return AnalysisConstants.COLOR_QUERY_NODE;
        } else if (item.canGetBoolean(AnalysisConstants.IS_USAGE_NODE) &&
                item.getBoolean(AnalysisConstants.IS_USAGE_NODE)){
            return AnalysisConstants.COLOR_USAGE_NODE;
        } else if (item.canGetBoolean(AnalysisConstants.IS_HEAD_ELEMENT) &&
                item.getBoolean(AnalysisConstants.IS_HEAD_ELEMENT)){
            return AnalysisConstants.COLOR_QUERY_NODE;
        }
        return Color.WHITE.getRGB();
    }
    
}
