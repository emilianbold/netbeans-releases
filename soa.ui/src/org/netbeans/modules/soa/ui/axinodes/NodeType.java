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

package org.netbeans.modules.soa.ui.axinodes;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.ImageUtilities;
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
        return ImageUtilities.loadImage(fileName);
    }
}
