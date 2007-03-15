/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.api.screen.display;

import org.openide.util.Utilities;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Main class hodling bas device info for the device.
 *
 * TODO this class should be made abstract and the implementation
 * should be moved either to the MIDP module or to a completely
 * separate module defining L&F of the view
 *
 * @author breh
 *
 */
public class ScreenDeviceInfo {
    
    private static final Border NORMAL_BORDER = BorderFactory.createEmptyBorder(3, 3, 3, 3);
    private static final Border SELECTED_BORDER = BorderFactory.createLineBorder(Color.BLUE, 3);
    
    public enum Edge {
        TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT, TOP_LEFT
    }
    
    protected DeviceBorder[] deviceBorders;
    
    private static final String IMAGE_BASE = "org/netbeans/modules/vmd/screen/resources/display/"; // NOI18N
    private static final String[] IMAGE_NAMES = new String[] {
        "top.png", "top_right.png", "right.png", "bottom_right.png", "bottom.png", "bottom_left.png", "left.png", "top_left.png" // NOI18N
    };
    
    private Dimension currentScreenSize;
    private ArrayList<Dimension> customScreenSizes = new ArrayList<Dimension>();
    private DeviceTheme deviceTheme;
    
    public ScreenDeviceInfo() {
        loadImages();
        customScreenSizes.add(new Dimension(240,320));
        customScreenSizes.add(new Dimension(128,160));
        currentScreenSize = customScreenSizes.get(0);
    }
    
    /**
     * Gets device info ID. Used to identify the device info.
     * @return
     */
    public String getDeviceInfoID() {
        return "DefaultDevice"; // NOI18N
    }
    
    /**
     * Gets device view resource (e.g. colors, images, fonts  ...)
     * @return
     */
    public DeviceTheme getDeviceTheme() {
        if (deviceTheme == null) {
            deviceTheme = new DeviceTheme();
        }
        return deviceTheme;
    }
    
    /**
     * Gets current screen size
     * @return
     */
    public Dimension getCurrentScreenSize() {
        // TODO somehow obtain the screen size from the project
        return currentScreenSize;
    }
    
    /**
     * Checks whether this device support custom screen sizes
     * @return
     */
    public boolean supportsCustomScreenSize() {
        return true;
    }
    
    /**
     * Returns a collection of the arbitrary sceeen sizes
     * @return
     */
    public Collection<Dimension> getCustomScreenSizes() {
        return customScreenSizes;
    }
    
    /**
     * returns true if this device supports arbitrary screen size
     * @return
     */
    public boolean supportsArbitraryScreenSize() {
        return true;
    }
    
    /**
     * Sets the arbitrary screen size to be used
     * @param newScreenSize
     */
    public void setArbitraryScreenSize(Dimension newScreenSize) {
        currentScreenSize = newScreenSize;
    }
    
    /**
     * Gets image for the given edge of the device
     * @param edge
     * @return
     */
    public DeviceBorder getDeviceBorder(Edge edge) {
        return deviceBorders[edge.ordinal()];
    }
    
    protected void loadImages() {
        deviceBorders = new DeviceBorder[IMAGE_NAMES.length];
        for (int i=0; i < IMAGE_NAMES.length; i++) {
            deviceBorders[i] = new DeviceBorder(Utilities.loadImage(IMAGE_BASE + IMAGE_NAMES[i]));
        }
    }
    
    /**
     * Helper class implementing for showing one piece of device border
     * @author breh
     */
    public static class DeviceBorder extends JComponent {
        
        private Image borderImage;
        
        public DeviceBorder(Image borderImage) {
            this.setBorderImage(borderImage);
        }
        
        /**
         * @return Returns the borderImage.
         */
        public Image getBorderImage() {
            return borderImage;
        }
        
        /**
         * @param borderImage The borderImage to set.
         */
        public void setBorderImage(Image borderImage) {
            if (borderImage == null) throw new NullPointerException("borderImage parameter cannot be null"); // NOI18N
            this.borderImage = borderImage;
        }
        
        /**
         * @return Returns the borderSize.
         */
        public Dimension getBorderSize() {
            return new Dimension(borderImage.getWidth(null), borderImage.getHeight(null));
            
        }
        
        public Dimension getPreferredSize() {
            return getBorderSize();
        }
        
        protected void paintComponent(Graphics g) {
            if (borderImage != null) {
                Dimension componentSize = getSize();
                g.drawImage(borderImage,0,0,componentSize.width,componentSize.height,null);
            }
        }
    }
    
    /**
     * Describes basic device resource. This class should be redesigned
     * to allow more independence on the actual implementation. Current
     * version is too MIDP2 specific.
     *
     * TODO this should be redesigned so it is not MIDP2 specific here !!!
     *
     * @author breh
     * @author Anton Chechel
     */
    public static class DeviceTheme {
        
        public enum FontFace {MONOSPACE, PROPORTIONAL, SYSTEM}
        public enum FontSize {SMALL, MEDIUM, LARGE}
        public enum FontStyle {PLAIN, BOLD, ITALIC, UNDERLINED}
        public enum FontType {DEFAULT, INPUT_TEXT, STATIC_TEXT, CUSTOM}
        
        private static final int FONT_SIZE_SMALL = 10;
        private static final int FONT_SIZE_MEDIUM = 12;
        private static final int FONT_SIZE_LARGE = 16;
        
        private static final String FONT_FACE_MONOSPACE = "Monospaced"; // NOI18N
        private static final String FONT_FACE_PROPORTIONAL = "Dialog"; // NOI18N
        private static final String FONT_FACE_SYSTEM = "Dialog"; // NOI18N
        
        public static final int COLOR_BACKGROUND = 1;
        public static final int COLOR_FOREGROUND = 2;
        public static final int COLOR_HIGHLIGHTED = 3;
        
        private static final Font FONT_DEFAULT = new Font(FONT_FACE_PROPORTIONAL, Font.PLAIN, FONT_SIZE_MEDIUM);
        private static final Font FONT_INPUT_TEXT = new Font(FONT_FACE_PROPORTIONAL, Font.PLAIN, FONT_SIZE_MEDIUM);
        private static final Font FONT_STATIC_TEXT = new Font(FONT_FACE_PROPORTIONAL, Font.PLAIN, FONT_SIZE_MEDIUM);
        
        private static final Map ATTR_MAP = new HashMap(1);
        
        static {
            ATTR_MAP.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        }
        
        public Font getFont(FontType type) {
            switch (type) {
                case INPUT_TEXT: return FONT_INPUT_TEXT;
                case STATIC_TEXT: return FONT_STATIC_TEXT;
                default: return FONT_DEFAULT;
            }
        }
        
        public Font getFont(FontFace face, FontStyle style, FontSize size) {
            String name = FONT_FACE_SYSTEM;
            if (face == FontFace.MONOSPACE) {
                name = FONT_FACE_MONOSPACE;
            } else if (face == FontFace.PROPORTIONAL) {
                name = FONT_FACE_PROPORTIONAL;
            }
            
            int styleInt = Font.PLAIN;
            if (style == FontStyle.BOLD) {
                styleInt |= Font.BOLD;
            }
            if (style == FontStyle.ITALIC) {
                styleInt |= Font.ITALIC;
            }
            
            int sizeInt = FONT_SIZE_MEDIUM;
            if (size == FontSize.LARGE) {
                sizeInt = FONT_SIZE_LARGE;
            } else if (size == FontSize.SMALL) {
                sizeInt = FONT_SIZE_SMALL;
            }
            
            Font font = new Font(name, styleInt, sizeInt);
            if (style == FontStyle.UNDERLINED) {
                font = font.deriveFont(ATTR_MAP);
            }
            
            return font;
        }
        
        public Color getColor(int colorType) {
            switch (colorType) {
            case COLOR_BACKGROUND : return Color.WHITE;
            case COLOR_FOREGROUND : return Color.BLACK;
            case COLOR_HIGHLIGHTED : return Color.RED;
            default: return null;
            }
        }
        
        public Border getBorder(boolean selected) {
            return selected ? SELECTED_BORDER : NORMAL_BORDER;
        }
        
    }
    
}
