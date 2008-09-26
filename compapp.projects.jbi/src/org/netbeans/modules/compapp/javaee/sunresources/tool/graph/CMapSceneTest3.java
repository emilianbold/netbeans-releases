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

package org.netbeans.modules.compapp.javaee.sunresources.tool.graph;


import org.netbeans.api.visual.vmd.VMDGraphScene;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

import java.awt.*;

/**
 * @author echou
 */
public class CMapSceneTest3 extends VMDGraphScene {

    /*
    private static final Image IMAGE_LIST = Utilities.loadImage ("test/resources/list_32.png"); // NOI18N
    private static final Image IMAGE_CANVAS = Utilities.loadImage ("test/resources/custom_displayable_32.png"); // NOI18N
    private static final Image IMAGE_COMMAND = Utilities.loadImage ("test/resources/command_16.png"); // NOI18N
    private static final Image IMAGE_ITEM = Utilities.loadImage ("test/resources/item_16.png"); // NOI18N
    private static final Image GLYPH_PRE_CODE = Utilities.loadImage ("test/resources/preCodeGlyph.png"); // NOI18N
    private static final Image GLYPH_POST_CODE = Utilities.loadImage ("test/resources/postCodeGlyph.png"); // NOI18N
    private static final Image GLYPH_CANCEL = Utilities.loadImage ("test/resources/cancelGlyph.png"); // NOI18N
    */
    

    /*
    public static void main (String[] args) {
        VMDGraphScene scene = new VMDGraphScene ();

        String mobile = createNode (scene, 100, 100, IMAGE_LIST, "menu", "List", null);
        createPin (scene, mobile, "start", IMAGE_ITEM, "Start", "Element");
        createPin (scene, mobile, "resume", IMAGE_ITEM, "Resume", "Element");

        String menu = createNode (scene, 400, 400, IMAGE_LIST, "menu", "List", null);
        createPin (scene, menu, "game", IMAGE_ITEM, "New Game", "Element");
        createPin (scene, menu, "options", IMAGE_ITEM, "Options", "Element");
        createPin (scene, menu, "help", IMAGE_ITEM, "Help", "Element");
        createPin (scene, menu, "exit", IMAGE_ITEM, "Exit", "Element");

        String game = createNode (scene, 600, 100, IMAGE_CANVAS, "gameCanvas", "MyCanvas", Arrays.asList (GLYPH_PRE_CODE, GLYPH_POST_CODE, GLYPH_CANCEL));
        createPin (scene, game, "ok", IMAGE_COMMAND, "okCommand1", "Command");
        createPin (scene, game, "cancel", IMAGE_COMMAND, "cancelCommand1", "Command");

        createEdge (scene, "start", menu);
        createEdge (scene, "resume", menu);

        createEdge (scene, "game", game);
        createEdge (scene, "exit", mobile);

        createEdge (scene, "ok", menu);
        createEdge (scene, "cancel", menu);

        SceneSupport.show (scene);
    }
    */
    
    public static final Image IMAGE_CANVAS = ImageUtilities.loadImage ("custom_displayable_32.png"); // NOI18N
    
    public CMapSceneTest3(String title) {
        addChild(new LabelWidget(this, title));
        
    }

    public String addCMapNode (String nodeName, int x, int y, Image image, String type) {
        VMDNodeWidget widget = (VMDNodeWidget) addNode (nodeName);
        widget.setPreferredLocation (new Point (x, y));
        widget.setNodeProperties (image, nodeName, type, null);
        String pinId = nodeName + "pin"; // NOI18N
        VMDPinWidget pin = (VMDPinWidget) addPin(nodeName, pinId);
        System.out.println("pin=" + pin); // NOI18N
        pin.setProperties(nodeName, null);
        return pinId;
    }

    private static void createPin (VMDGraphScene scene, String nodeID, String pinID, Image image, String name, String type) {
        ((VMDPinWidget) scene.addPin (nodeID, pinID)).setProperties (name, null);
    }

    public void addCMapEdge (String edgeName, String sourcePinId, 
            String targetPinId) {

        addEdge (edgeName);
        setEdgeSource (edgeName, sourcePinId);
        setEdgeTarget (edgeName, targetPinId);

    }

}

