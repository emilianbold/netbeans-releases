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
package org.netbeans.modules.bpel.nodes;

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
