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

package org.netbeans.modules.xslt.tmap.nodes;

import java.awt.Image;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public enum NodeType {
    UNKNOWN_TYPE, // Special element which means that the value isn't known.
    TRANSFORMMAP(TransformMap.class),
    SERVICE(Service.class),
    OPERATION(Operation.class),
    INVOKE(Invoke.class),
    TRANSFORM(Transform.class),
    PARAM(Param.class);

    private AtomicReference<String> myDisplayName = new AtomicReference<String>();
    private AtomicReference<Image> myDefaultImage = new AtomicReference<Image>();
    private Class<? extends TMapComponent> myComponentType;
    private static final String IMAGE_FOLDER_PATH =
            "org/netbeans/modules/xslt/tmap/resources/images/"; // NOI18N
    public static final Image UNKNOWN_IMAGE = getImageImpl(UNKNOWN_TYPE, null);

    private NodeType() {
    }

    private NodeType(Class<? extends TMapComponent> tMapComponentType) {
        myComponentType = tMapComponentType;
    }
    
    public static NodeType getNodeType(TMapComponent entity) {
        if (entity == null) {
            return null;
        }
        return getNodeType(entity.getComponentType());
    }
    
    public static NodeType getNodeType(Class<? extends TMapComponent> tMapComponentType) {
        NodeType type = UNKNOWN_TYPE;
        NodeType[] types =  values();
        for (NodeType typeEl : types) {
            if (tMapComponentType == typeEl.getComponentType()) {
                type = typeEl;
                break;
            } 
        }
        return type;
    }
    
    public Class<? extends TMapComponent> getComponentType() {
        return myComponentType;
    }
    
    public String getDisplayName() {
        if (myDisplayName.get() == null) {
            try {
                myDisplayName.compareAndSet(null,
                        NbBundle.getMessage(NodeType.class, this.toString()));
            } catch(Exception ex) {
                myDisplayName.compareAndSet(null, name());
            }
        }
        return myDisplayName.get();
    }
    
    public String getDisplayName(Object modificator) {
        String displayName = null;
        try {
            String key = this.toString() + "_" + modificator.toString();
            displayName = NbBundle.getMessage(NodeType.class, key);
        } catch (Exception ex) {
            // do nothing
        }
        // can return null intentionally!
        return displayName;
    }

    public Icon getIcon() {
        return new ImageIcon(getImage());
    }    
    
    public Image getImage() {
        if (myDefaultImage.get() == null) {
            Image image = getImageImpl(this, null);
            if (image == null) {
                image = UNKNOWN_IMAGE;
            }
            //
            myDefaultImage.compareAndSet(null, image);
        }
        return myDefaultImage.get();
    }
    
    private static Image getImageImpl(Object name, Object modificator) {
        String fileName = null;
        if (modificator == null) {
            fileName = IMAGE_FOLDER_PATH + name + ".png"; // NOI18N
        } else {
            fileName = IMAGE_FOLDER_PATH + name + "_" + modificator + ".png"; // NOI18N
        }
        return Utilities.loadImage(fileName);
    }
}
