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
 * EdgeStrokeColorAction.java
 *
 * Created on March 4, 2006, 7:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.nbprefuse;

import prefuse.action.assignment.ColorAction;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

/**
 *
 * @author Jeri Lockhart
 */
public class EdgeStrokeColorAction  extends ColorAction {

        public EdgeStrokeColorAction() {
            super(AnalysisConstants.GRAPH_GROUP_EDGES, VisualItem.STROKECOLOR);
        }
        public int getColor(VisualItem item) {
            if (item.isHighlighted()){
                return AnalysisConstants.COLOR_HIGHLIGHT;
            }
            return ColorLib.gray(200);
        }
        
    }
