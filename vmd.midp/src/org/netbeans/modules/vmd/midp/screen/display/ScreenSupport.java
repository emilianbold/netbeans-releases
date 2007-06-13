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

package org.netbeans.modules.vmd.midp.screen.display;

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo.DeviceTheme.FontFace;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo.DeviceTheme.FontSize;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo.DeviceTheme.FontStyle;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo.DeviceTheme.FontType;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.openide.filesystems.FileObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public final class ScreenSupport {
    
    private ScreenSupport() {
    }
    
    /**
     * Returns AWT font according to kind, face, style and size
     *
     * @param deviceInfo
     * @param fontComponent
     * @return font
     */
    public static Font getFont(ScreenDeviceInfo deviceInfo, DesignComponent fontComponent) {
        if (fontComponent == null)
            return deviceInfo.getDeviceTheme().getFont(FontType.DEFAULT);
        int kindCode = MidpTypes.getInteger(fontComponent.readProperty(FontCD.PROP_FONT_KIND));
        if (kindCode == FontCD.VALUE_KIND_DEFAULT) {
            return deviceInfo.getDeviceTheme().getFont(FontType.DEFAULT);
        } else if (kindCode == FontCD.VALUE_KIND_STATIC) {
            return deviceInfo.getDeviceTheme().getFont(FontType.STATIC_TEXT);
        } else if (kindCode == FontCD.VALUE_KIND_INPUT) {
            return deviceInfo.getDeviceTheme().getFont(FontType.INPUT_TEXT);
        }
        
        int faceCode = MidpTypes.getInteger(fontComponent.readProperty(FontCD.PROP_FACE));
        FontFace face = FontFace.SYSTEM;
        if (faceCode == FontCD.VALUE_FACE_MONOSPACE) {
            face = FontFace.MONOSPACE;
        } else if (faceCode == FontCD.VALUE_FACE_PROPORTIONAL) {
            face = FontFace.PROPORTIONAL;
        }
        
        int styleCode = MidpTypes.getInteger(fontComponent.readProperty(FontCD.PROP_STYLE));
        FontStyle style = FontStyle.PLAIN;
        if (styleCode == FontCD.VALUE_STYLE_BOLD) {
            style = FontStyle.BOLD;
        } else if (styleCode == FontCD.VALUE_STYLE_ITALIC) {
            style = FontStyle.ITALIC;
        } else if (styleCode == FontCD.VALUE_STYLE_UNDERLINED) {
            style = FontStyle.UNDERLINED;
        }
        
        int sizeCode = MidpTypes.getInteger(fontComponent.readProperty(FontCD.PROP_SIZE));
        FontSize size = FontSize.MEDIUM;
        if (sizeCode == FontCD.VALUE_SIZE_SMALL) {
            size = FontSize.SMALL;
        } else if (sizeCode == FontCD.VALUE_SIZE_LARGE) {
            size = FontSize.LARGE;
        }
        
        return deviceInfo.getDeviceTheme().getFont(face, style, size);
    }
    
    /**
     * Loads icon using resourcePath property from given image design component
     *
     * @param imageComponent image design component
     * @return icon
     */
    public static Icon getIconFromImageComponent(DesignComponent imageComponent) {
        // System.out.println("imageComponent: " + imageComponent);
        if (imageComponent == null)
            return null;
        String imagePath = MidpTypes.getString(imageComponent.readProperty(ImageCD.PROP_RESOURCE_PATH));
        
        if (imagePath == null)
            return null;
        DesignDocument document = imageComponent.getDocument();
        
        Map<FileObject, FileObject> fileMap = MidpProjectSupport.getFileObjectsForRelativeResourcePath(document, imagePath);
        if (fileMap ==  null || fileMap.keySet().size() == 0)
            return null;
        FileObject imageFileObject = fileMap.keySet().iterator().next();
        if (imageFileObject != null) {
            return resolveImageForRoot(imageFileObject, imagePath);
        }
        Debug.warning("Resource path property in " + imageComponent + " contains incorrect value"); // NOI18N
        return null;
    }
    
    public static int getFontHeight(Graphics g, Font f) {
        assert (g != null) && (f != null);
        FontMetrics fm = g.getFontMetrics(f);
        return fm.getHeight();
    }
    
    private static Icon resolveImageForRoot(FileObject file, String relPath) {
        try {
            BufferedImage img = ImageIO.read(file.getInputStream());
            if (img != null)
                return new ImageIcon(img);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
