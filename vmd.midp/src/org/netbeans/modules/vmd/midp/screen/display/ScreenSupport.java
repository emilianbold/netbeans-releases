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

import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo.DeviceTheme.FontFace;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo.DeviceTheme.FontSize;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo.DeviceTheme.FontStyle;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo.DeviceTheme.FontType;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.netbeans.modules.vmd.api.model.Debug;

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
     * Wraps given text with html tags to be displayed in swing component,
     * removes all exising tags in the text
     *
     * @param text to be wraped
     * @return text
     */
    public static String wrapWithHtml(String text) {
        if (text == null) {
            return text;
        }
        text = text.replaceAll("<.*>", ""); // NOI18N
        
        StringBuffer str = new StringBuffer();
        str.append("<html>"); // NOI18N
        str.append(text);
        str.append("</html>"); // NOI18N
        return str.toString();
    }
    
    /**
     * Wraps given text with html tags and make a link to be displayed in swing component,
     * removes all exising tags in the text
     *
     * @param text to be wraped
     * @return text
     */
    public static String wrapLinkWithHtml(String text) {
        if (text == null) {
            return text;
        }
        text = text.replaceAll("<.*>", ""); // NOI18N
        
        StringBuffer str = new StringBuffer();
        str.append("<html>"); // NOI18N
        str.append("<a href='null'>"); // NOI18N
        str.append(text);
        str.append("</a"); // NOI18N
        str.append("</html>"); // NOI18N
        return str.toString();
    }
    
    /**
     * Loads icon using resourcePath property from given image design component
     *
     * @param imageComponent image design component
     * @return icon
     */
    public static Icon getIconFromImageComponent(DesignComponent imageComponent) {
        Icon icon = null;
        if (imageComponent != null) {
            String iconPath = MidpTypes.getString(imageComponent.readProperty(ImageCD.PROP_RESOURCE_PATH));
            if (iconPath != null) {
                DesignDocument document = ActiveDocumentSupport.getDefault().getActiveDocument();
                DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(document);
                if (context != null) { // document is loading
                    SourceGroup sourceGroup = ProjectUtils.getSourceGroups(context).get(0); // CLDC project has always only one source root
                    String srcPath = sourceGroup.getRootFolder().getPath();
                    icon = new ImageIcon(); //NOI18N
                    try {
                        BufferedImage img = null;
                        img = ImageIO.read(new File("/"+srcPath + iconPath));
                        icon = new ImageIcon(img);
                    } catch (IOException e) {
                        icon = null;
                    }
                    if (icon == null) {
                        Debug.warning("Resource path property in " + imageComponent + " contains incorrect value"); //NOI18N
                    }
                }
            }
        }
        return icon;
    }
    
    public static int getFontHeight(Graphics g, Font f) {
        assert (g != null) && (f != null);
        FontMetrics fm = g.getFontMetrics(f);
        return fm.getHeight();
    }
    
}
