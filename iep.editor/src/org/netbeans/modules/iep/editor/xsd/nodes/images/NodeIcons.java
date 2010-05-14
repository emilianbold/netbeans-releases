/*
 * NodeIcons.java
 * 
 * Created on 28.09.2007, 23:25:55
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.iep.editor.xsd.nodes.images;

import java.awt.Image;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.Utilities;

/**
 *
 * 
 */
public enum NodeIcons {
    ATTRIBUTE, 
    ATTRIBUTE_OPTIONAL, 
    ELEMENT, 
    ELEMENT_OPTIONAL, 
    ELEMENT_OPTIONAL_REPEATING, 
    ELEMENT_REPEATING;
    
    private static final String IMAGE_FOLDER_NAME = 
            "org/netbeans/modules/iep/editor/xsd/nodes/images/"; // NOI18N

    public static final Icon UNKNOWN_IMAGE = new ImageIcon("UNKNOWN_IMAGE");
    
    private AtomicReference<Icon> mIcon = new AtomicReference<Icon>();

    public Icon getIcon() {
        if (mIcon.get() == null) {
            Icon icon;
            Image image = getImageImpl(this);
            if (image == null) {
                icon = UNKNOWN_IMAGE;
            } else {
                icon = new ImageIcon(image);
            }
            //
            mIcon.compareAndSet(null, icon);
        }
        return mIcon.get();
    }

    /**
     * Modificator allows having more then one icon associated with a Node Type
     */
    private static Image getImageImpl(Object name) {
        String fileName = IMAGE_FOLDER_NAME + name + ".png"; // NOI18N
        return Utilities.loadImage(fileName);
    }
            
}
