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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.nodes.BpelNode;
import java.awt.Image;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.editors.api.nodes.images.FolderIcon;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * This node is intended to present variouse simple folder at a tree view.
 *
 * @author nk160297
 */
public class CategoryFolderNode extends BpelNode<NodeType> {
    
    private AtomicReference<Image> myIconRef = new AtomicReference<Image>();
    private AtomicReference<Image> myOpenedIconRef = new AtomicReference<Image>();
    
    public CategoryFolderNode(NodeType type, Children children, Lookup lookup) {
        super(type, children, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.FOLDER;
    }
    
    public synchronized Image getIcon(int type) {
        if (myIconRef.get() == null) {
            Image resultImage = null;
            boolean processed = false;
            //
            NodeType refNodeType = getReference();
            //
            // Look for badge icon first 
            Image badgeImage = refNodeType.getBadgeImage();
            if (NodeType.isValidImage(badgeImage)) {
                resultImage = FolderIcon.getIcon(type);
                if (resultImage != null) {
                    resultImage = Utilities.mergeImages(
                            resultImage, badgeImage, 8, 8);
                    processed = true;
                }
            }
            //
            if (!processed) {
                // If it is impossible to build the badged icon, 
                // Then try to load an icon associated to referenced node type. 
                resultImage = refNodeType.getImage();
            }
            //
            if (!NodeType.isValidImage(resultImage)) {
                // If there is not any valid icon in resources 
                // then show the default icon.
                resultImage = FolderIcon.getIcon(type);
            }
            //
            if (resultImage != null) {
                myIconRef.compareAndSet(null, resultImage);
            }
        }
        //
        return myIconRef.get();
    }
    
    public Image getOpenedIcon(int type) {
        if (myOpenedIconRef.get() == null) {
            Image resultImage = null;
            boolean processed = false;
            //
            NodeType refNodeType = getReference();
            //
            // Look for badge icon first 
            Image badgeImage = refNodeType.getBadgeImage();
            if (NodeType.isValidImage(badgeImage)) {
                resultImage = FolderIcon.getOpenedIcon(type);
                if (resultImage != null) {
                    resultImage = Utilities.mergeImages(
                            resultImage, badgeImage, 8, 8);
                    processed = true;
                }
            }
            //
            if (!processed) {
                // If it is impossible to build the badged icon, 
                // Then try to load an icon associated to referenced node type. 
                resultImage = refNodeType.getImage();
            }
            //
            if (!NodeType.isValidImage(resultImage)) {
                // If there is not any valid icon in resources 
                // then show the default icon.
                resultImage = FolderIcon.getIcon(type);
            }
            //
            if (resultImage != null) {
                myOpenedIconRef.compareAndSet(null, resultImage);
            }
        }
        //
        return myOpenedIconRef.get();
    }
    
    protected String getNameImpl() {
        String name = null;
        //
        // Tries to load the name with the FOLDER prefix first.
        NodeType refNodeType = getReference();
        name = getNodeType().getDisplayName(refNodeType);
        //
        if (name == null || name.length() == 0) {
            // If there is not the name with FOLDER prefix then tries to load 
            // the name associated to the referenced node type.
            name = refNodeType.getDisplayName();
        }
        return (name != null) ? name : "";
    }
    
}
