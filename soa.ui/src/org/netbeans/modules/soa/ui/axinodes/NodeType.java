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
    
    private static final String IMAGE_FOLDER_NAME = 
            "org/netbeans/modules/soa/ui/axinodes/"; // NOI18N
    
    private String myDisplayName;
    private String myHelpId;
    private Image myDefaultImage;
    
    /**
     * This image is used as the default for types which hasn't icon provided.
     * It is public to be able to check if the image is provided. 
     */
    public static final Image UNKNOWN_IMAGE = getImageImpl(UNKNOWN_TYPE);
    
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
    
    public synchronized Image getImage() {
        if (myDefaultImage == null) {
            myDefaultImage = getImageImpl(this);
            if (myDefaultImage == null) {
                myDefaultImage = UNKNOWN_IMAGE;
            }
        }
        return myDefaultImage;
    }
    
    /**
     * Modificator allows having more then one icon associated with a Node Type
     */
    private static Image getImageImpl(Object name) {
        String fileName = null;
        fileName = IMAGE_FOLDER_NAME + name + ".png"; // NOI18N
        return Utilities.loadImage(fileName);
    }
    
}
