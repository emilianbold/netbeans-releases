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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.vmd.midp.screen.display;

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.DeviceTheme.FontFace;
import org.netbeans.modules.vmd.api.screen.display.DeviceTheme.FontSize;
import org.netbeans.modules.vmd.api.screen.display.DeviceTheme.FontStyle;
import org.netbeans.modules.vmd.api.screen.display.DeviceTheme.FontType;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfoPresenter;
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
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.modules.vmd.api.model.PropertyValue;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public final class ScreenSupport {

    private ScreenSupport() {
    }

    public static Font getFont(DesignComponent fontComponent) {
        if (fontComponent == null) {
            return null;
        }
        return getFont(getDeviceInfo(fontComponent.getDocument()), fontComponent);
    }

    public static Font getFont(DesignDocument document, int kindCode, int faceCode, int styleCode, int sizeCode) {
        ScreenDeviceInfo deviceInfo = getDeviceInfo(document);

        if (kindCode == FontCD.VALUE_KIND_DEFAULT) {
            return deviceInfo.getDeviceTheme().getFont(FontType.DEFAULT);
        } else if (kindCode == FontCD.VALUE_KIND_STATIC) {
            return deviceInfo.getDeviceTheme().getFont(FontType.STATIC_TEXT);
        } else if (kindCode == FontCD.VALUE_KIND_INPUT) {
            return deviceInfo.getDeviceTheme().getFont(FontType.INPUT_TEXT);
        }

        FontFace face = FontFace.SYSTEM;
        if (faceCode == FontCD.VALUE_FACE_MONOSPACE) {
            face = FontFace.MONOSPACE;
        } else if (faceCode == FontCD.VALUE_FACE_PROPORTIONAL) {
            face = FontFace.PROPORTIONAL;
        }

        EnumSet<FontStyle> style = EnumSet.of(FontStyle.PLAIN);
        if ((styleCode & FontCD.VALUE_STYLE_BOLD) != 0) {
            style.add(FontStyle.BOLD);
        }
        if ((styleCode & FontCD.VALUE_STYLE_ITALIC) != 0) {
            style.add(FontStyle.ITALIC);
        }
        if ((styleCode & FontCD.VALUE_STYLE_UNDERLINED) != 0) {
            style.add(FontStyle.UNDERLINED);
        }

        FontSize size = FontSize.MEDIUM;
        if (sizeCode == FontCD.VALUE_SIZE_SMALL) {
            size = FontSize.SMALL;
        } else if (sizeCode == FontCD.VALUE_SIZE_LARGE) {
            size = FontSize.LARGE;
        }

        return deviceInfo.getDeviceTheme().getFont(face, style, size);
    }

    // TODO Should this method be in VMD Screen Designer module?
    public static ScreenDeviceInfo getDeviceInfo(final DesignDocument document) {
        final ScreenDeviceInfo[] screenDevice = new ScreenDeviceInfo[1];
        if (document == null) {
            return null;
        }
        document.getTransactionManager().readAccess(new Runnable() {

            public void run() {
                DesignComponent rootComponent = document.getRootComponent();
                ScreenDeviceInfoPresenter presenter = rootComponent.getPresenter(ScreenDeviceInfoPresenter.class);
                if (presenter == null) {
                    throw Debug.error("No ScreenDevice attached to the root component"); //NOI18N
                }
                screenDevice[0] = presenter.getScreenDeviceInfo();
            }
        });
        return screenDevice[0];
    }

    /**
     * Returns AWT font according to kind, face, style and size
     *
     * @param deviceInfo
     * @param fontComponent
     * @return font
     */
    public static Font getFont(ScreenDeviceInfo deviceInfo, DesignComponent fontComponent) {
        if (fontComponent == null) {
            return deviceInfo.getDeviceTheme().getFont(FontType.DEFAULT);
        }

        PropertyValue value = fontComponent.readProperty(FontCD.PROP_FONT_KIND);
        int kindCode;
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            kindCode = MidpTypes.getInteger(value);
        } else {
            kindCode = FontCD.VALUE_KIND_DEFAULT;
        }

        value = fontComponent.readProperty(FontCD.PROP_FACE);
        int faceCode;
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            faceCode = MidpTypes.getInteger(value);
        } else {
            faceCode = FontCD.VALUE_FACE_SYSTEM;
        }

        value = fontComponent.readProperty(FontCD.PROP_STYLE);
        int styleCode;
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            styleCode = MidpTypes.getInteger(value);
        } else {
            styleCode = FontCD.VALUE_STYLE_PLAIN;
        }

        value = fontComponent.readProperty(FontCD.PROP_SIZE);
        int sizeCode;
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            sizeCode = MidpTypes.getInteger(value);
        } else {
            sizeCode = FontCD.VALUE_SIZE_MEDIUM;
        }

        return getFont(fontComponent.getDocument(), kindCode, faceCode, styleCode, sizeCode);
    }

    /**
     * Loads icon using resourcePath property from given image design component
     *
     * @param imageComponent image design component
     * @return icon
     */
    public static Icon getIconFromImageComponent(DesignComponent imageComponent) {
        if (imageComponent == null) {
            return null;
        }
        PropertyValue value = imageComponent.readProperty(ImageCD.PROP_RESOURCE_PATH);
        String imagePath = null;
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            imagePath = MidpTypes.getString(imageComponent.readProperty(ImageCD.PROP_RESOURCE_PATH));
        }

        if (imagePath == null) {
            return null;
        }
        DesignDocument document = imageComponent.getDocument();

        Map<FileObject, FileObject> fileMap = MidpProjectSupport.getFileObjectsForRelativeResourcePath(document, imagePath);
        if (fileMap == null || fileMap.keySet().iterator().hasNext() == false) {
            return null;
        }
        FileObject imageFileObject = fileMap.keySet().iterator().next();
        if (imageFileObject != null) {
            return resolveImageForRoot(imageFileObject, imagePath);
        }
        Debug.warning("Resource path property in", imageComponent, "contains incorrect value"); // NOI18N
        return null;
    }

    public static FileObject getFileObjectFromImageComponent(DesignComponent imageComponent) {
        if (imageComponent == null) {
            return null;
        }
        PropertyValue value = imageComponent.readProperty(ImageCD.PROP_RESOURCE_PATH);
        String imagePath = null;
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            imagePath = MidpTypes.getString(imageComponent.readProperty(ImageCD.PROP_RESOURCE_PATH));
        }

        if (imagePath == null) {
            return null;
        }
        DesignDocument document = imageComponent.getDocument();

        Map<FileObject, FileObject> fileMap = MidpProjectSupport.getFileObjectsForRelativeResourcePath(document, imagePath);
        if (fileMap == null || !fileMap.keySet().iterator().hasNext()) {
            return null;
        }
        FileObject imageFileObject = fileMap.keySet().iterator().next();
        if (imageFileObject != null) {
            return imageFileObject;
        }
        Debug.warning("Resource path property in", imageComponent, "contains incorrect value"); // NOI18N
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
            if (img != null) {
                return new ImageIcon(img);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}