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
 * AnalysisConstants.java
 *
 * Created on January 17, 2006, 12:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.nbprefuse;

import java.awt.BasicStroke;
import java.awt.Color;
import javax.swing.ImageIcon;
import org.netbeans.modules.xml.nbprefuse.render.NbLabelRenderer;
/**
 *
 * @author Jeri Lockhart
 */
public interface AnalysisConstants {
    // node and edge attributes
    public static final String ID = "id";                           // NOI18N
    public static final String LABEL = "label";                     // NOI18N
    public static final String TOOLTIP = "tooltip";                 // NOI18N
    public static final String COMPONENT_TYPE_NAME = "component-type-name";   // NOI18N
    public static final String SCHEMA_COMPONENT = "schema-component";   // NOI18N
    public static final String OPENIDE_NODE = "openide-node";       // NOI18N
    public static final String XAM_COMPONENT = "xam-component";     // NOI18N
    public static final String FILE_GROUP = "file-group";           // NOI18N
    public static final String ELEMENT_TYPE = "element-type";       // NOI18N
    public static final String EDGE_TYPE = "edge-type";             // NOI18N
    public static final String GENERALIZATION = "generalization";   // NOI18N
    public static final String COMPOSITION = "composition";         // NOI18N
    public static final String REFERENCE = "reference";             // NOI18N
    public static final String XML_FILENAME = "xml-filename";       // NOI18N
    public static final String FILE_OBJECT = "file-object";         // NOI18N
    public static final String FILE_TYPE = "file-type    ";         // NOI18N
    public static final String SHOW = "show";                       // NOI18N
    public static final String FILE_EDGE_TYPE = "file-edge-type";   // NOI18N
    public static final String IS_EXPANDED = "is-expanded";         // NOI18N
    public static final String IS_PRIMITIVE = "is-primitive";       // NOI18N
    public static final String IS_QUERY_NODE = "is-query-node";     // NOI18N
    public static final String IS_USAGE_NODE = "is-usage-node";     // NOI18N
    public static final String IS_FILE_NODE = "is-file-node";       // NOI18N
    public static final String IS_FILE_GROUP_AGGREGATE
                                 = "is-file-group-aggregate";       // NOI18N
    public static final String IS_HEAD_ELEMENT = "is-head-element"; // NOI18N
    public static final String EMPTY_STRING = "";                   // NOI18N
    public static final String FILE_NODE_FILE_GROUP = "file-node-file-group"; // NOI18N
    public static final String MOUSEOVER = "mouseover";             // NOI18N
    public static final String JAVA_AWT_IMAGE = "java-awt-image";   // NOI18N



    public static final String GRAPH_GROUP = "graph";
    public static final String GRAPH_GROUP_NODES = "graph.nodes";  // prefuse appends subgroup name "nodes"
    public static final String GRAPH_GROUP_EDGES = "graph.edges";  // prefuse appends subgroup name "edges""
    public static final String GRAPH_GROUP_AGGR = "aggregates";

    public static final int DISPLAY_PREFERRED_WIDTH = 300;
    public static final int DISPLAY_PREFERRED_HEIGHT =200;

    // named Visualization ActionLists and Actions
    public static final String ACTION_UPDATE = "action-update";      //NOI18N
    public static final String ACTION_UPDATE_REPAINT =
            "action-update-repaint";      //NOI18N
    public static final String ACTION_UPDATE_AGGREGATE_LAYOUT_REPAINT =
            "action-update-aggregate-layout-repaint";      //NOI18N
    public static final String ACTION_DRAW = "action-draw";           //NOI18N
    public static final String ACTION_LAYOUT = "action-layout";      //NOI18N
    public static final String ACTION_LAYOUT_REPAINT
                                    = "action-layout-repaint";      //NOI18N
    public static final String ACTION_ANIMATE = "action-animate";    //NOI18N
    public static final String ACTION_REPAINT = "action-repaint";    //NOI18N
    public static final String ACTION_AGGREGATE_LAYOUT =
            "action-aggregate-layout";    //NOI18N


    public static final String SCHEMA_FILE_EXTENSION = "xsd";  //NOI18N
    public static final String BPEL_FILE_EXTENSION = "bpel";  //NOI18N

    public static enum GlobalTypes {COMPLEX_TYPE, SIMPLE_TYPE, ELEMENT,
            GROUP, ATTRIBUTE, ATTRIBUTE_GROUP, PRIMITIVE, BASE_COMPLEX_TYPES};

    public static final BasicStroke SELECTED_STROKE = new BasicStroke(2f);
    public static final BasicStroke UNSELECTED_STROKE = new BasicStroke(1f);


    public static final int COLOR_HIGHLIGHT= new Color(245, 217, 86).getRGB();   //F5D956   (255, 226, 90) FFE25A -Leos , (204, 102, 0)-Original;
    public static final int COLOR_QUERY_NODE = new Color(255, 84, 0).getRGB();           // orange, lt blue (169, 205, 255)
    public static final int COLOR_FILE_NODE = new Color(205, 216, 220).getRGB();     // pale brown grey(245, 240, 225)
    public static final int COLOR_USAGE_NODE = new Color(255,197,145).getRGB();          // light orange, pale blue (209,245,255)
    public static final int COLOR_SELECTED_NODE = new Color(255,251,155).getRGB();//F5FB9B  (255, 248, 106) FFF86A- Leos;     // light orange, was black reverse

//        private GradientPaint usesColor =
//                new GradientPaint(
//                0, 0, new Color(200, 227, 255), 30, 30,
//                new Color(255, 255, 255), true); // true means to repeat pattern  0xA9CDE8, 0xDDEBF6



        public static enum HSBHues {
            RED(0.0f,0.086f),
            ORANGE(0.086f,0.115f),
            GREEN(0.115f, 0.392f),
            TURQUOISE(0.392f, 0.529f),
            BLUE(0.529f, 0.667f),
            PURPLE(0.667f, 0.784f),
            VIOLET(0.784f,0.823f),
            PINK(0.823f, 1.0f);

            float low;  // inclusive HSB hue 0.0 to 1.0
            float high; // inclusive HSB hue 0.0 to 1.0
            HSBHues(float low, float high) {
                this.low = low;
                this.high = high;
            }
            public float low()   { return low; }
            public float high() {return high;}
        }
        
    public final static String SCHEMA_FILE_TYPE = "schema-file-type";   //NOI18N
    public final static String WSDL_FILE_TYPE = "wsdl-file-type";   //NOI18N
    public final static String BPEL_FILE_TYPE = "bpel-file-type";   //NOI18N
        
    // java.util.prefs.Preferences keys
    public static final String PREFERENCES_XML_REFACTORING_PREVIEW_ALL =  
            "preferences-xml-refactoring-preview-all";   //NOI18N   boolean
    
    public static final String USER_OBJECT = "user-obj";
    public static final String REFACTORING_ELEMENT = "refactor-element";

}
