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

import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo.DeviceTheme.FontFace;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo.DeviceTheme.FontSize;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo.DeviceTheme.FontStyle;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo.DeviceTheme.FontType;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.ProjectResourceResolver;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.vmd.api.io.DataObjectContext;

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
        if (imageComponent == null)
            return null;
        String imagePath = MidpTypes.getString(imageComponent.readProperty(ImageCD.PROP_RESOURCE_PATH));
        if (imagePath == null)
            return null;
        DesignDocument document = imageComponent.getDocument();
        
        Set<FileObject> foldersToScan = getFoldersToScan(imageComponent);
        
        for (FileObject folder: foldersToScan) {
            Icon sourceIcon = resolveImageForRoot(folder, imagePath);
            if (sourceIcon != null)
                return sourceIcon;
        }
        
        Project project = MidpProjectSupport.getProjectForDocument(document);
        for (ProjectResourceResolver resolver : MidpProjectSupport.getAllResolvers()) {
            Collection<FileObject> collection = resolver.getResourceRoots(project, MidpDocumentSupport.PROJECT_TYPE_MIDP);
            if (collection != null)
                for (FileObject root : collection) {
                    Icon icon = resolveImageForRoot(root, imagePath);
                    if (icon != null)
                        return icon;
                }
        }
        Debug.warning("Resource path property in " + imageComponent + " contains incorrect value"); // NOI18N
        return null;
    }
    
    private static Icon resolveImageForRoot(FileObject root, String imagePath) {
        FileObject imageFile = root.getFileObject(imagePath);
        if (imageFile != null) {
            File input = FileUtil.toFile(imageFile);
            if (input != null) {
                try {
                    BufferedImage img = ImageIO.read(input);
                    if (img != null)
                        return new ImageIcon(img);
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
    
    public static int getFontHeight(Graphics g, Font f) {
        assert (g != null) && (f != null);
        FontMetrics fm = g.getFontMetrics(f);
        return fm.getHeight();
    }
    
    private static  Set<FileObject> getFoldersToScan(DesignComponent component) {
        DesignDocument document = component.getDocument();
        Set<FileObject> filesToScan = new HashSet<FileObject>();
        
        FileObject projectDirectory = ProjectUtils.getProject(document).getProjectDirectory();
        DataObjectContext dac = ProjectUtils.getDataObjectContextForDocument(document);
        //Resources
        for (FileObject f : ClassPath.getClassPath(projectDirectory, ClassPath.COMPILE).getRoots()) {
            filesToScan.add((f));
        }
        //Sources
        for (SourceGroup g : ProjectUtils.getSourceGroups(dac)) {
            filesToScan.add((g.getRootFolder()));
        }
        return filesToScan;
    }
    
}
