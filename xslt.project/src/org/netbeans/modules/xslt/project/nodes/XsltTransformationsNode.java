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
