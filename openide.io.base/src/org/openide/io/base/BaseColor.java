/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.openide.io.base;

/**
 * Color for use in base I/O APIs. Extrected from java.awt.Color.
 *
 * @author jhavlin
 */
public final class BaseColor {

    private int value;

    /**
     * Creates an opaque sRGB color with the specified combined RGB value
     * consisting of the red component in bits 16-23, the green component in
     * bits 8-15, and the blue component in bits 0-7. The actual color used in
     * rendering depends on finding the best match given the color space
     * available for a particular output device. Alpha is defaulted to 255.
     *
     * @param rgb the combined RGB components
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @see #getRGB
     */
    public BaseColor(int rgb) {
        value = 0xff000000 | rgb;
    }

    /**
     * Creates an sRGB color with the specified combined RGBA value consisting
     * of the alpha component in bits 24-31, the red component in bits 16-23,
     * the green component in bits 8-15, and the blue component in bits 0-7. If
     * the <code>hasalpha</code> argument is <code>false</code>, alpha is
     * defaulted to 255.
     *
     * @param rgba the combined RGBA components
     * @param hasalpha <code>true</code> if the alpha bits are valid;
     * <code>false</code> otherwise
     * @see java.awt.image.ColorModel#getRGBdefault
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @see #getAlpha
     * @see #getRGB
     */
    public BaseColor(int rgba, boolean hasalpha) {
        if (hasalpha) {
            value = rgba;
        } else {
            value = 0xff000000 | rgba;
        }
    }

    /**
     * Creates an opaque sRGB color with the specified red, green, and blue
     * values in the range (0 - 255). The actual color used in rendering depends
     * on finding the best match given the color space available for a given
     * output device. Alpha is defaulted to 255.
     *
     * @throws IllegalArgumentException if <code>r</code>, <code>g</code> or
     * <code>b</code> are outside of the range 0 to 255, inclusive
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @see #getRGB
     */
    public BaseColor(int r, int g, int b) {
        this(r, g, b, 255);
    }

    /**
     * Creates an sRGB color with the specified red, green, blue, and alpha
     * values in the range (0 - 255).
     *
     * @throws IllegalArgumentException if <code>r</code>, <code>g</code>,
     * <code>b</code> or <code>a</code> are outside of the range 0 to 255,
     * inclusive
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @param a the alpha component
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @see #getAlpha
     * @see #getRGB
     */
    public BaseColor(int r, int g, int b, int a) {
        value = ((a & 0xFF) << 24)
                | ((r & 0xFF) << 16)
                | ((g & 0xFF) << 8)
                | ((b & 0xFF));
    }

    /**
     * Returns the RGB value representing the color in the default sRGB
     * color model. (Bits 24-31 are alpha, 16-23 are red, 8-15 are green,
     * 0-7 are blue).
     *
     * @return the RGB value of the color in the default sRGB
     * <code>ColorModel</code>.
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     */
    public int getRGB() {
        return value;
    }

    /**
     * Returns the red component in the range 0-255 in the default sRGB space.
     *
     * @return the red component.
     * @see #getRGB
     */
    public int getRed() {
        return (getRGB() >> 16) & 0xFF;
    }

    /**
     * Returns the green component in the range 0-255 in the default sRGB space.
     *
     * @return the green component.
     * @see #getRGB
     */
    public int getGreen() {
        return (getRGB() >> 8) & 0xFF;
    }

    /**
     * Returns the blue component in the range 0-255 in the default sRGB space.
     *
     * @return the blue component.
     * @see #getRGB
     */
    public int getBlue() {
        return (getRGB()) & 0xFF;
    }

    /**
     * Returns the alpha component in the range 0-255.
     *
     * @return the alpha component.
     * @see #getRGB
     */
    public int getAlpha() {
        return (getRGB() >> 24) & 0xff;
    }
}
