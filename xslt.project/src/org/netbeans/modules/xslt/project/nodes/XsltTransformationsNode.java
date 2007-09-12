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
package org.netbeans.modules.xslt.project.nodes;

import java.awt.Image;
import javax.swing.Action;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class XsltTransformationsNode extends FilterNode {
    
    private static Image ICON = Utilities.loadImage("org/netbeans/modules/xslt/project/resources/transformations.gif");
    
    public XsltTransformationsNode(DataObject dObj) {
        super(dObj.getNodeDelegate());
//            disableDelegation(DELEGATE_GET_DISPLAY_NAME|
//                    DELEGATE_SET_DISPLAY_NAME|DELEGATE_GET_SHORT_DESCRIPTION|
//                    DELEGATE_GET_ACTIONS);
            disableDelegation(DELEGATE_GET_DISPLAY_NAME|
                    DELEGATE_SET_DISPLAY_NAME|DELEGATE_GET_SHORT_DESCRIPTION);
    }

    public XsltTransformationsNode(DataObject dObj, org.openide.nodes.Children children) {
        super(dObj.getNodeDelegate(), children);
//            disableDelegation(DELEGATE_GET_DISPLAY_NAME|
//                    DELEGATE_SET_DISPLAY_NAME|DELEGATE_GET_SHORT_DESCRIPTION|
//                    DELEGATE_GET_ACTIONS);
//            disableDelegation(DELEGATE_GET_DISPLAY_NAME|
//                    DELEGATE_SET_DISPLAY_NAME|DELEGATE_GET_SHORT_DESCRIPTION);

//            disableDelegation(DELEGATE_GET_DISPLAY_NAME|
//                    DELEGATE_SET_DISPLAY_NAME|DELEGATE_GET_SHORT_DESCRIPTION);
    
    }

    // TODO m | r 
    public PropertySet[] getPropertySets() {
        return new PropertySet[0];
    }

//    public String getDisplayName() {
//        return NbBundle.getMessage(XsltTransformationsNode.class, "LBL_Transformations");
//    }

//    public Image getIcon(int type) {
//        return ICON;
//    }
//
//    public Image getOpenedIcon(int type) {
//        return ICON;
//    }
//
    public boolean canCopy() {
        return false;
    }

    public boolean canCut() {
        return false;
    }

    public boolean canDestroy() {
        return false;
    }

    public boolean canRename() {
        return false;
    }
    
//    @Override
//    public Action[] getActions(boolean context) {
//        return new Action[] {
//            org.openide.util.actions.SystemAction.get( org.openide.actions.EditAction.class ),
////            CommonProjectActions.newFileAction(),
////            null,
////            org.openide.util.actions.SystemAction.get( org.openide.actions.FileSystemAction.class ),
////            null,
////            org.openide.util.actions.SystemAction.get( org.openide.actions.FindAction.class ),
////            null,
////            org.openide.util.actions.SystemAction.get( org.openide.actions.PasteAction.class ),
////            null,
////            org.openide.util.actions.SystemAction.get( org.openide.actions.ToolsAction.class ),
//        };
//    }
}
