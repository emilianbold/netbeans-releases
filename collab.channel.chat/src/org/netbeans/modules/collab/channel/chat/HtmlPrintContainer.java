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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.chat;

import java.awt.Color;
import java.awt.Font;

import org.netbeans.editor.PrintContainer;

import org.netbeans.modules.collab.*;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class HtmlPrintContainer extends Object implements PrintContainer {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final String PRE_START = "<pre>"; // NOI18N
    private static final String PRE_END = "</pre>"; // NOI18N
    private static final String EOL = "\n"; // NOI18N
    private static final char ZERO = '0'; // NOI18N
    private static final String ESC_LT = "&lt;"; // NOI18N
    private static final String ESC_GT = "&gt;"; // NOI18N
    private static final String ESC_AMP = "&amp;"; // NOI18N
    private static final String ESC_QUOT = "&quot;"; // NOI18N

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private StringBuffer buffer = new StringBuffer(PRE_START);
    private String string;

    /**
     *
     *
     */
    public HtmlPrintContainer() {
        super();
    }

    /**
     * @return true if the container needs to init empty line with
     * at least one character. Printing then adds one space
     * to each empty line.
     * False means that the container is able to accept
     * lines with no characters.
     */
    public boolean initEmptyLines() {
        return false;
    }

    /**
         * Add the attributed characters to the container.
     * @param chars characters being added.
     * @param font font of the added characters
     * @param foreColor foreground color of the added characters
     * @param backColor background color of the added characters
     */
    public void add(char[] chars, Font font, Color foreColor, Color backColor) {
        if (isWhitespace(chars)) {
            buffer.append(chars);
        } else {
            // If foreground color = black and background = white, just append
            // the characters directly
            if (foreColor.equals(Color.black) && backColor.equals(Color.white)) {
                escape(chars);
            } else {
                buffer.append("<font style=\"color: ");
                htmlColor(foreColor);

                if (!backColor.equals(Color.white)) {
                    buffer.append("; background-color: ");
                    htmlColor(backColor);
                }

                buffer.append("\">");
                escape(chars);
                buffer.append("</font>");
            }
        }

        string = null;
    }

    /**
         * End of line was found.
         *
         */
    public void eol() {
        buffer.append(EOL);
        string = null;
    }

    /**
     *
     *
     */
    public String toString() {
        if (string == null) {
            // The printing adds two EOLs at the end of the content; remove them
            if (Character.isWhitespace(buffer.charAt(buffer.length() - 1))) {
                buffer.deleteCharAt(buffer.length() - 1);
            }

            if (Character.isWhitespace(buffer.charAt(buffer.length() - 1))) {
                buffer.deleteCharAt(buffer.length() - 1);
            }

            string = buffer.append(PRE_END).toString();
        }

        return string;
    }

    /**
     *
     *
     */
    private boolean isWhitespace(char[] chars) {
        for (int i = 0; i < chars.length; i++) {
            if (!Character.isWhitespace(chars[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     *
     *
     */
    private void htmlColor(Color color) {
        buffer.append("#"); //NOI18N

        if (color.getRed() < 0x10) {
            buffer.append(ZERO);
        }

        buffer.append(Integer.toHexString(color.getRed()));

        if (color.getGreen() < 0x10) {
            buffer.append(ZERO);
        }

        buffer.append(Integer.toHexString(color.getGreen()));

        if (color.getBlue() < 0x10) {
            buffer.append(ZERO);
        }

        buffer.append(Integer.toHexString(color.getBlue()));
    }

    /**
     *
     *
     */
    private void escape(char[] chars) {
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '<') { // NOI18N
                buffer.append(ESC_LT);
            } else if (chars[i] == '>') { // NOI18N
                buffer.append(ESC_GT);
            } else if (chars[i] == '&') { // NOI18N
                buffer.append(ESC_AMP);
            } else if (chars[i] == '\'') { // NOI18N
                buffer.append(ESC_QUOT);
            } else if (Character.isWhitespace(chars[i])) {
                buffer.append(chars[i]);
            } else {
                buffer.append(chars[i]);
            }
        }
    }
}
