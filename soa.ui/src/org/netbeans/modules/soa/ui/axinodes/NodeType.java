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

package org.netbeans.modules.soa.ui.axinodes;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author nk160297
 */
public enum NodeType {
    UNKNOWN_TYPE, // Special element which means that the value isn't known.
    ELEMENT,
    ATTRIBUTE,
    COMPOSITOR
            ;
    
    public static enum BadgeModificator {
        SINGLE, OPTIONAL, REPEATING, OPTIONAL_REPEATING;
    }
    
    private static final String IMAGE_FOLDER_NAME =
            "org/netbeans/modules/soa/ui/axinodes/images/"; // NOI18N
    
    private String myDisplayName;
    private String myHelpId;
    private Map<BadgeModificator, Image> myImageMap;
    
    /**
     * This image is used as the default for types which hasn't icon provided.
     * It is public to be able to check if the image is provided.
     */
    public static final Image UNKNOWN_IMAGE = getImageImpl(UNKNOWN_TYPE, null);
    
    public synchronized String getDisplayName() {
        if (myDisplayName == null) {
            try {
                myDisplayName = NbBundle.getMessage(NodeType.class, this.toString());
            } catch(Exception ex) {
                myDisplayName = name();
            }
        }
        return myDisplayName;
    }
    
    public String getHelpId() {
        if (myHelpId == null) {
            myHelpId = getClass().getName()+"."+this.toString(); // NOI18N
        }
        return myHelpId;
    }
    
    public Image getImage(BadgeModificator mult) {
        if (mult == null) {
            mult = BadgeModificator.SINGLE;
        }
        synchronized (this) {
            Image image = getImageMap().get(mult);
            if (image == null) {
                image = getImageImpl(this, mult);
                if (image == null) {
                    image = UNKNOWN_IMAGE;
                }
                //
                getImageMap().put(mult, image);
            }
            return image;
        }
    }
    
    private Map<BadgeModificator, Image> getImageMap() {
        if (myImageMap == null) {
            myImageMap = new HashMap<BadgeModificator, Image>();
        }
        return myImageMap;
    }
    
    /**
     * Modificator allows having more then one icon associated with a Node Type
     */
    private static Image getImageImpl(Object name, BadgeModificator mult) {
        String fileName = null;
        if (mult == null || mult == BadgeModificator.SINGLE) {
            fileName = IMAGE_FOLDER_NAME + name + ".png"; // NOI18N
        } else {
            fileName = IMAGE_FOLDER_NAME + name + "_" + mult + ".png"; // NOI18N
        }
        return Utilities.loadImage(fileName);
    }
}
