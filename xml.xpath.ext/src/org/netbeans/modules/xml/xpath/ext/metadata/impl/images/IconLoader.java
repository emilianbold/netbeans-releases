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

package org.netbeans.modules.xml.xpath.ext.metadata.impl.images;

import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author nk160297
 */
public final class IconLoader {

    private static final String IMAGE_FOLDER_NAME =
            "org/netbeans/modules/xml/xpath/ext/metadata/impl/images/"; // NOI18N
    
    public static final Icon UNKNOWN_ICON = getIcon("unknown_image"); // NOI18N
    
    public static Icon getIcon(Object name) {
        return getIcon(name, IMAGE_FOLDER_NAME);
    }
    
    public static Icon getIcon(Object name, String locationFolder) {
        String fileName = locationFolder + name + ".png"; // NOI18N
        Image img = ImageUtilities.loadImage(fileName);
        if (img == null) {
            return UNKNOWN_ICON;
        } else {
            return new ImageIcon(img);
        }
    }
    
}
