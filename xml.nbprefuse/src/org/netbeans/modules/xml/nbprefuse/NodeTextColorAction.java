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
 * NodeTextColorAction.java
 *
 * Created on March 4, 2006, 7:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.nbprefuse;

import java.awt.Color;
import java.awt.Font;
import prefuse.Visualization;
import prefuse.action.assignment.ColorAction;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;

/**
 *
 * @author Jeri Lockhart
 */
public class NodeTextColorAction  extends ColorAction {
        public NodeTextColorAction() {
            super(AnalysisConstants.GRAPH_GROUP_NODES, VisualItem.TEXTCOLOR);
        }
        public int getColor(VisualItem item) {
            boolean mouseover = false;
            if (item.canGetBoolean(AnalysisConstants.MOUSEOVER) &&
                    item.getBoolean(AnalysisConstants.MOUSEOVER)){
                    mouseover = true;
            }
            TupleSet selected = 
                    item.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS);
            if (selected.containsTuple(item) || mouseover){
//                return Color.WHITE.getRGB();
                return Color.BLACK.getRGB();
            }
            if (item.canGetBoolean(AnalysisConstants.IS_QUERY_NODE) &&
                    item.getBoolean(AnalysisConstants.IS_QUERY_NODE)){
                item.setFont(item.getFont().deriveFont(Font.BOLD));
                if (item.canGetBoolean(AnalysisConstants.MOUSEOVER) &&
                        item.getBoolean(AnalysisConstants.MOUSEOVER) == false &&
                        item.isHighlighted() == false){
                    return Color.WHITE.getRGB();
                }
            }
            return Color.BLACK.getRGB();
        }
        
    }
