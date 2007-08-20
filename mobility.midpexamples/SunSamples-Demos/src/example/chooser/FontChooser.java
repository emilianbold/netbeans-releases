/*
 *
 * Copyright (c) 2007, Sun Microsystems, Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package example.chooser;

import javax.microedition.lcdui.*;


/**
 * A Font chooser.  This screen can be used to
 * choose fonts.  A form is used to select from
 * the various choices for size, style, and face.
 */
public class FontChooser extends Form implements ItemStateListener {
    int face;
    int style;
    int size;
    ChoiceGroup faceChoice;
    ChoiceGroup styleChoice;
    ChoiceGroup sizeChoice;

    /**
     * Create a new font chooser form.
     * Create each of the form entries
     */
    public FontChooser() {
        super("Choose Attributes");
        faceChoice = new ChoiceGroup("Face", Choice.EXCLUSIVE);
        faceChoice.append("System", null);
        faceChoice.append("Monospace", null);
        faceChoice.append("Proportional", null);
        styleChoice = new ChoiceGroup("Style", Choice.MULTIPLE);
        styleChoice.append("Bold", null);
        styleChoice.append("Italic", null);
        styleChoice.append("Underlined", null);
        sizeChoice = new ChoiceGroup("Size", Choice.EXCLUSIVE);
        sizeChoice.append("Small", null);
        sizeChoice.append("Medium", null);
        sizeChoice.append("Large", null);

        append("Face");
        append(faceChoice);
        append("Style");
        append(styleChoice);
        append("Size");
        append(sizeChoice);

        setItemStateListener(this);
    }

    /**
     * Set the Style of font to display.
     * @param style the style to select
     * @see Font#getStyle
     */
    public void setStyle(int style) {
        this.style = style;
    }

    /**
     * Get the style of font currently being displayed.
     * @return the current style being used for text
     * @see Font#getStyle
     */
    public int getStyle() {
        return style;
    }

    /**
     * Set the Face of font to display.
     * @param face the face to select
     * @see Font#getFace
     */
    public void setFace(int face) {
        this.face = face;
    }

    /**
     * Get the face of font currently being displayed.
     * @return the current face of the font
     * @see Font#getFace
     */
    public int getFace() {
        return face;
    }

    /**
     * Set the Size of font to display.
     * @param size of the font to set
     * @see Font#getSize
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Get the size of font currently being displayed.
     * @return the current size of the font
     * @see Font#getSize
     */
    public int getSize() {
        return size;
    }

    /**
     * Reflect changes in the item states into the states.
     * @param item that to which some change occurred
     */
    public void itemStateChanged(Item item) {
        if (item == faceChoice) {
            int f = faceChoice.getSelectedIndex();

            switch (f) {
            case 0:
                face = Font.FACE_SYSTEM;

                break;

            case 1:
                face = Font.FACE_MONOSPACE;

                break;

            case 2:
                face = Font.FACE_PROPORTIONAL;

                break;
            }
        } else if (item == styleChoice) {
            style = 0;

            if (styleChoice.isSelected(0)) {
                style += Font.STYLE_BOLD;
            }

            if (styleChoice.isSelected(1)) {
                style |= Font.STYLE_ITALIC;
            }

            if (styleChoice.isSelected(2)) {
                style |= Font.STYLE_UNDERLINED;
            }
        } else if (item == sizeChoice) {
            int s = sizeChoice.getSelectedIndex();

            switch (s) {
            case 0:
                size = Font.SIZE_SMALL;

                break;

            case 1:
                size = Font.SIZE_MEDIUM;

                break;

            case 2:
                size = Font.SIZE_LARGE;

                break;
            }
        }
    }
}
