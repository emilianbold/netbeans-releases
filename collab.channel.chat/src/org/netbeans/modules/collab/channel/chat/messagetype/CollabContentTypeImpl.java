/*
 * CollabContentTypeImpl.java
 *
 * Created on 20. říjen 2005, 22:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.collab.channel.chat.messagetype;

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.*;

import org.openide.filesystems.*;
import org.openide.util.ImageUtilities;

/**
 *
 * @author nenik
 */
public final class CollabContentTypeImpl extends CollabContentType {
    private Image icon;
    private String displayName;
    private String contentType;
    
    public Image getIcon() {
        return icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getContentType() {
        return contentType;
    }

    
    /** Creates a new instance of CollabContentTypeImpl */
    public CollabContentTypeImpl(String contentType, String displayName, Image icon) {
        this.contentType = contentType;
        this.displayName = displayName;
        this.icon = icon;
    }

    private static Object create(org.openide.filesystems.FileObject fo) {
        String displayName = fo.getName();
        Image icon = ImageUtilities.loadImage("org/netbeans/modules/collab/channel/chat/resources/chat_bubble.png");

        try {
            Set s = Collections.singleton(fo);
            displayName = fo.getFileSystem().getDecorator().annotateName (displayName, s);
            icon = FileUIUtils.getImageDecorator(fo.getFileSystem()).annotateIcon (icon, BeanInfo.ICON_COLOR_16x16, s);
        } catch (FileStateInvalidException ex) {
            // not fatal, report and continue
            org.openide.ErrorManager.getDefault().notify(ex);
        }
        
    	String contentType = (String)fo.getAttribute("contentType");
	return new CollabContentTypeImpl(contentType, displayName, icon);
    }    
}
