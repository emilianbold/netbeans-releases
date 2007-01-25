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
package org.netbeans.modules.xslt.project.nodes;

import java.awt.Image;
import org.netbeans.modules.xslt.core.xsltmap.TransformationDesc;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TransformationDescNode extends AbstractNode {
    private TransformationDesc tDesc;
    private static Image ICON = Utilities.loadImage("org/netbeans/modules/xslt/project/resources/transformationDesc.png");
    
    public TransformationDescNode(TransformationDesc tDesc) {
        super(Children.LEAF);
        this.tDesc = tDesc;
        // set nodeDescription property which is shown in property sheet help region
        setValue("nodeDescription", "");
    }

    public TransformationDescNode(Lookup lookup) {
        super(Children.LEAF, lookup);
    }

    public String getDisplayName() {
        return getName();
    }
    
    public String getName() {
        String roleName = tDesc.getRoleName();
        String fileName = tDesc.getFile();
        fileName = fileName == null ? " [empty file name] " : fileName;
        roleName = roleName == null ? "" : roleName;
        
        return roleName + " {"+ fileName+"} ";
    }

    public String getShortDescription() {
        String roleName = tDesc.getRoleName();
        String fileName = tDesc.getFile();
        fileName = fileName == null ? " [empty file name] " : fileName;
        roleName = roleName == null ? "" : roleName;
        return "<html> <b>role</b> = "+roleName + "<br> <b>file</b> ="+ fileName;
        
    }

    public Image getIcon(int type) {
        return ICON;
    }

    public Image getOpenedIcon(int type) {
        return ICON;
    }

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
    
}
