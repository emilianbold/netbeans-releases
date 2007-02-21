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

package org.netbeans.modules.soa.ui;

import java.awt.Color;
import java.awt.Image;

/**
 *
 * @author nk160297
 */
public class SoaUiUtil {
    
    public static Color MISTAKE_RED = new Color(204, 0, 0);
    public static Color INACTIVE_BLUE = new Color(0, 102, 153);
    public static Color HTML_GRAY = new Color(153, 153, 153);
    
    private static String GRAY_COLOR = "#999999";
    
    public static String getGrayString(String message) {
        return getGrayString("", message);
    }
    
    public static String getGrayString(String nonGrayPrefix, String message) {
        return message == null ? nonGrayPrefix : "<html>"+getCorrectedHtmlRenderedString(nonGrayPrefix) // NOI18N
        +"<font color='"+GRAY_COLOR+"'>"+getCorrectedHtmlRenderedString(message)+"</font></html>";// NOI18N
    }
    
    public static String getGrayString(String nonGrayPrefix, String message
            , String nonGraySuffix) {
        return getGrayString(nonGrayPrefix,message,nonGraySuffix, true);
    }
    
    public static String getGrayString(String nonGrayPrefix, String message
            , String nonGraySuffix, boolean isSetHtmlHeader) {
        String htmlHeader = isSetHtmlHeader ? "<html>" : ""; // NOI18N
        String htmlFooter = isSetHtmlHeader ? "</html>" : ""; // NOI18N
        return message == null ? nonGrayPrefix : htmlHeader
                +getCorrectedHtmlRenderedString(nonGrayPrefix)
                +"<font color='"+GRAY_COLOR+"'>" // NOI18N
                +getCorrectedHtmlRenderedString(message)
                +"</font>" // NOI18N
                +(nonGraySuffix == null ? ""
                : getCorrectedHtmlRenderedString(nonGraySuffix))
                +htmlFooter;// NOI18N
    }
    
    public static String getFormattedHtmlString(
            boolean isSetHtmlHeader, TextChunk... chunkArr) {
        String htmlHeader = isSetHtmlHeader ? "<html>" : ""; // NOI18N
        String htmlFooter = isSetHtmlHeader ? "</html>" : ""; // NOI18N
        //
        StringBuffer sb = new StringBuffer();
        boolean isFirst = true;
        TextChunk prevTextChunk = null;
        boolean isPrevChunkStyled = false;
        for (TextChunk chunk : chunkArr) {
            if (chunk.myText == null || chunk.myText.length() == 0) {
                continue;
            }
            //
            boolean hasSameTextAttributes = prevTextChunk == null ? false :
                prevTextChunk.hasSameTextAttributes(chunk);
            if (isPrevChunkStyled && !hasSameTextAttributes) {
                sb.append("</font>");  // NOI18N
            }
            //
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(" "); // NOI18N
            }
            //
            if (chunk.myColor == null) {
                sb.append(getCorrectedHtmlRenderedString(chunk.myText));
                isPrevChunkStyled = false;
            } else {
                if (!isPrevChunkStyled || 
                        (isPrevChunkStyled && !hasSameTextAttributes)) {
                    sb.append("<font color='" + getHtmlColor(chunk.myColor) + "'>"); // NOI18N
                }
                sb.append(getCorrectedHtmlRenderedString(chunk.myText));
                isPrevChunkStyled = true;
            }
            //
            prevTextChunk = chunk;
        }
        //
        if (isPrevChunkStyled) {
            sb.append("</font>");  // NOI18N
        }
        //
        return htmlHeader + sb.toString() + htmlFooter;
    }
    
    public static class TextChunk {
        String myText = null;
        Color myColor = null;
        
        public TextChunk(String text) {
            myText = text;
        }
        
        public TextChunk(String text, Color color) {
            myText = text;
            myColor = color;
        }
        
        public boolean hasSameTextAttributes(TextChunk anotherChunk) {
            if (anotherChunk == null) {
                return false;
            }
            //
            return anotherChunk.myColor == this.myColor;
        }
    }
    
    public static String getHtmlColor(Color color) {
        int redValue = color.getRed();
        String red = redValue == 0 ? "00" : Integer.toHexString(redValue);
        //
        int greenValue = color.getGreen();
        String green = greenValue == 0 ? "00" : Integer.toHexString(greenValue);
        //
        int blueValue = color.getBlue();
        String blue = blueValue == 0 ? "00" : Integer.toHexString(blueValue);
        //
        return "#" + red + green + blue; // NOI18N
    }
    
    public static Image getBadgedIcon(Image originalImage, Image badgeImage) {
        return getBadgedIcon(originalImage, badgeImage, 9, 0);
    }
    
    public static Image getBadgedIcon(Image originalImage, Image badgeImage, int x, int y) {
        if (originalImage == null) {
            return null;
        }
        if (badgeImage == null) {
            return originalImage;
        }
        Image image = org.openide.util.Utilities.mergeImages(originalImage, badgeImage, x, y );
        return image;
    }
    
    public static final String getCorrectedHtmlRenderedString(String htmlString) {
        if (htmlString == null) {
            return null;
        }
        htmlString = htmlString.replaceAll("&amp;","&"); // NOI18n
        htmlString = htmlString.replaceAll("&gt;",">;"); // NOI18n
        htmlString = htmlString.replaceAll("&lt;","<"); // NOI18n
        
        htmlString = htmlString.replaceAll("&","&amp;"); // NOI18n
        htmlString = htmlString.replaceAll(">","&gt;"); // NOI18n
        htmlString = htmlString.replaceAll("<","&lt;"); // NOI18n
        return htmlString;
    }
    
}
