/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.openide.awt;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.UIManager;
import org.openide.util.ImageUtilities;

/**
 * Factory class for Close Buttons.
 *
 * @author M. Kristofic
 * @since 7.38
 */
public final class CloseButtonFactory{

    private static Image closeTabImage;
    private static Image closeTabPressedImage;
    private static Image closeTabMouseOverImage;
    private static Image bigCloseTabImage;
    private static Image bigCloseTabPressedImage;
    private static Image bigCloseTabMouseOverImage;

    private CloseButtonFactory() {
    }
    
    /**
     * Creates a small 'close' JButton with close icon, rollover icon and pressed icon according to Look and Feel
     * 
     * @return JButton with close icons.
     */
    public static JButton createCloseButton() {
        JButton closeButton = new JButton();
        int size = 16;
        closeButton.setPreferredSize(new Dimension(size, size));
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusable(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setBorderPainted(false);
        closeButton.setRolloverEnabled(true);
        closeButton.setIcon(ImageUtilities.image2Icon(getCloseTabImage()));
        closeButton.setRolloverIcon(ImageUtilities.image2Icon(getCloseTabRolloverImage()));
        closeButton.setPressedIcon(ImageUtilities.image2Icon(getCloseTabPressedImage()));
        return closeButton;
    }
    
    /**
     * Creates a big 'close' JButton with close icon, rollover icon and pressed icon according to Look and Feel
     * 
     * @return JButton with close icons.
     */
    public static JButton createBigCloseButton() {
        JButton closeButton = new JButton();
        int size = 19;
        closeButton.setPreferredSize(new Dimension(size, size));
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusable(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setBorderPainted(false);
        closeButton.setRolloverEnabled(true);
        closeButton.setIcon(ImageUtilities.image2Icon(getBigCloseTabImage()));
        closeButton.setRolloverIcon(ImageUtilities.image2Icon(getBigCloseTabRolloverImage()));
        closeButton.setPressedIcon(ImageUtilities.image2Icon(getBigCloseTabPressedImage()));
        return closeButton;
    }

    private static boolean isWindowsVistaLaF() {
        String osName = System.getProperty("os.name");
        return osName.indexOf("Vista") >= 0
                || (osName.equals("Windows NT (unknown)") && "6.0".equals(System.getProperty("os.version")));
    }

    private static boolean isWindowsXPLaF() {
        Boolean isXP = (Boolean) Toolkit.getDefaultToolkit().
                getDesktopProperty("win.xpstyle.themeActive"); //NOI18N
        return isWindowsLaF() && (isXP == null ? false : isXP.booleanValue());
    }

    private static boolean isWindowsLaF() {
        String lfID = UIManager.getLookAndFeel().getID();
        return lfID.endsWith("Windows"); //NOI18N
    }

    private static boolean isAquaLaF() {
        return "Aqua".equals(UIManager.getLookAndFeel().getID());
    }
    
    private static boolean isGTKLaF () {
        return "GTK".equals( UIManager.getLookAndFeel().getID() ); //NOI18N
    }

    private static Image getCloseTabImage() {
        if( null == closeTabImage ) {
            if( isWindowsVistaLaF() ) {
                closeTabImage = ImageUtilities.loadImage("org/openide/awt/resources/vista_close_enabled.png"); // NOI18N
            } else if( isWindowsXPLaF() ) {
                closeTabImage = ImageUtilities.loadImage("org/openide/awt/resources/xp_close_enabled.png"); // NOI18N
            } else if( isWindowsLaF() ) {
                closeTabImage = ImageUtilities.loadImage("org/openide/awt/resources/win_close_enabled.png"); // NOI18N
            } else if( isAquaLaF() ) {
                closeTabImage = ImageUtilities.loadImage("org/openide/awt/resources/mac_close_enabled.png"); // NOI18N
            } else if( isGTKLaF() ) {
                closeTabImage = ImageUtilities.loadImage("org/openide/awt/resources/gtk_close_enabled.png"); // NOI18N
            } else {
                closeTabImage = ImageUtilities.loadImage("org/openide/awt/resources/metal_close_enabled.png"); // NOI18N
            }
        }
        return closeTabImage;
    }
    
    private static Image getCloseTabPressedImage() {
        if( null == closeTabPressedImage ) {
            if( isWindowsVistaLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/vista_close_pressed.png"); // NOI18N
            } else if( isWindowsXPLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/xp_close_pressed.png"); // NOI18N
            } else if( isWindowsLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/win_close_pressed.png"); // NOI18N
            } else if( isAquaLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/mac_close_pressed.png"); // NOI18N
            } else if( isGTKLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/gtk_close_pressed.png"); // NOI18N
            } else {
                closeTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/metal_close_pressed.png"); // NOI18N
            }
        }
        return closeTabPressedImage;
    }
    
    private static Image getCloseTabRolloverImage() {
        if( null == closeTabMouseOverImage ) {
            if( isWindowsVistaLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/vista_close_rollover.png"); // NOI18N
            } else if( isWindowsXPLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/xp_close_rollover.png"); // NOI18N
            } else if( isWindowsLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/win_close_rollover.png"); // NOI18N
            } else if( isAquaLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/mac_close_rollover.png"); // NOI18N
            } else if( isGTKLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/gtk_close_rollover.png"); // NOI18N
            } else {
                closeTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/metal_close_rollover.png"); // NOI18N
            }
        }
        return closeTabMouseOverImage;
    }
    
    
    private static Image getBigCloseTabImage() {
        if( null == bigCloseTabImage ) {
            if( isWindowsVistaLaF() ) {
                bigCloseTabImage = ImageUtilities.loadImage("org/openide/awt/resources/vista_bigclose_enabled.png"); // NOI18N
            } else if( isWindowsXPLaF() ) {
                bigCloseTabImage = ImageUtilities.loadImage("org/openide/awt/resources/xp_bigclose_enabled.png"); // NOI18N
            } else if( isWindowsLaF() ) {
                bigCloseTabImage = ImageUtilities.loadImage("org/openide/awt/resources/win_bigclose_enabled.png"); // NOI18N
            } else if( isAquaLaF() ) {
                bigCloseTabImage = ImageUtilities.loadImage("org/openide/awt/resources/mac_bigclose_enabled.png"); // NOI18N
            } else if( isGTKLaF() ) {
                bigCloseTabImage = ImageUtilities.loadImage("org/openide/awt/resources/gtk_bigclose_enabled.png"); // NOI18N
            } else {
                bigCloseTabImage = ImageUtilities.loadImage("org/openide/awt/resources/metal_bigclose_enabled.png"); // NOI18N
            }
        }
        return bigCloseTabImage;
    }
    
    private static  Image getBigCloseTabPressedImage() {
        if( null == bigCloseTabPressedImage ) {
            if( isWindowsVistaLaF() ) {
                bigCloseTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/vista_bigclose_pressed.png"); // NOI18N
            } else if( isWindowsXPLaF() ) {
                bigCloseTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/xp_bigclose_pressed.png"); // NOI18N
            } else if( isWindowsLaF() ) {
                bigCloseTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/win_bigclose_pressed.png"); // NOI18N
            } else if( isAquaLaF() ) {
                bigCloseTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/mac_bigclose_pressed.png"); // NOI18N
            } else if( isGTKLaF() ) {
                bigCloseTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/gtk_bigclose_pressed.png"); // NOI18N
            } else {
                bigCloseTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/metal_bigclose_pressed.png"); // NOI18N
            }
        }
        return bigCloseTabPressedImage;
    }
    
    private static Image getBigCloseTabRolloverImage() {
        if( null == bigCloseTabMouseOverImage ) {
            if( isWindowsVistaLaF() ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/vista_bigclose_rollover.png"); // NOI18N
            } else if( isWindowsXPLaF() ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/xp_bigclose_rollover.png"); // NOI18N
            } else if( isWindowsLaF() ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/win_bigclose_rollover.png"); // NOI18N
            } else if( isAquaLaF() ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/mac_bigclose_rollover.png"); // NOI18N
            } else if( isGTKLaF() ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/gtk_bigclose_rollover.png"); // NOI18N
            } else {
                bigCloseTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/metal_bigclose_rollover.png"); // NOI18N
            }
        }
        return bigCloseTabMouseOverImage;
    }
}
