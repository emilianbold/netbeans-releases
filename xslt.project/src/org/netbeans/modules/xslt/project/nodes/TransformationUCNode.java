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
import org.netbeans.modules.xslt.tmap.model.xsltmap.TransformationType;
import org.netbeans.modules.xslt.tmap.model.xsltmap.TransformationUC;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TransformationUCNode extends AbstractNode {
    private String useCase;
    private TransformationUC tUC;
    private static String ICON = "org/netbeans/modules/xslt/project/resources/requestReplyUC.png"; // NOI18N
    private static String PROXY_UC_ICON = "org/netbeans/modules/xslt/project/resources/proxyUC.png"; // NOI18N
    
    public TransformationUCNode(TransformationUC tUC, Children children) {
        super(children);
        this.tUC = tUC;
        TransformationType ucType = tUC.getTransformationType();
        this.useCase = ucType != null ? ucType.getTagName() : "";
        
    }
    
    public String getDisplayName() {
        return getName();
    }
    
    public String getName() {
        return useCase;
    }

    public String getShortDescription() {
        return "<html> <b>"+useCase+"</b> </html>";
        
    }

    public Image getIcon(int type) {
        return Utilities.loadImage(
                    TransformationType.REQUEST_REPLY_SERVICE.
                                            equals(tUC.getTransformationType()) 
                    ? ICON 
                    : PROXY_UC_ICON
                );
    }

    public Image getOpenedIcon(int type) {
        return getIcon(type);
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
