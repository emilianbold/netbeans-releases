/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.editor.lib2.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;

/**
 * Various view utilities.
 * 
 * @author Miloslav Metelka
 */

public final class ViewUtils {

    // -J-Dorg.netbeans.modules.editor.lib2.view.ViewUtils.level=FINE
    private static final Logger LOG = Logger.getLogger(ViewUtils.class.getName());

    private ViewUtils() { // No instances
    }

    public static Rectangle2D.Double shape2Bounds(Shape s) {
        Rectangle2D r;
        if (s instanceof Rectangle2D) {
            r = (Rectangle2D) s;
        } else {
            r = s.getBounds2D();
        }
        return new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    public static Rectangle2D shapeAsRect(Shape s) {
        Rectangle2D r;
        if (s instanceof Rectangle2D) {
            r = (Rectangle2D) s;
        } else {
            r = s.getBounds2D();
        }
        return r;
    }

    public static Rectangle toRect(Rectangle2D r2d) {
        Rectangle r = new Rectangle();
        r.setRect(r2d);
        return r;
    }

    public static void fillRect(Graphics2D g, Rectangle2D r) {
        g.fillRect(
                (int) r.getX(),
                (int) r.getY(),
                (int) r.getWidth(),
                (int) r.getHeight()
        );
    }

    public static void applyBackgroundAttributes(AttributeSet attributes,
            Color defaultBackground, Graphics2D graphics)
    {
        if (attributes != null) {
            Color c = (Color) attributes.getAttribute(StyleConstants.Background);
            if (c != null) {
                defaultBackground = c;
            }
        }
        graphics.setColor(defaultBackground);
    }

    public static void applyForegroundAttributes(AttributeSet attributes, Font defaultFont,
            Color defaultForeground, Graphics2D graphics)
    {
        if (attributes != null) {
            Color c = (Color) attributes.getAttribute(StyleConstants.Foreground);
            if (c != null) {
                defaultForeground = c;
            }
            defaultFont = getFont(attributes, defaultFont);
        }
        graphics.setColor(defaultForeground);
        graphics.setFont(defaultFont);
    }

    public static Font getFont(AttributeSet attributes, Font defaultFont) {
        if (attributes != null) {
            String fontName = (String) attributes.getAttribute(StyleConstants.FontFamily);
            Boolean bold = (Boolean) attributes.getAttribute(StyleConstants.Bold);
            Boolean italic = (Boolean) attributes.getAttribute(StyleConstants.Italic);
            Integer fontSizeInteger = (Integer) attributes.getAttribute(StyleConstants.FontSize);
            if (fontName != null || bold != null || italic != null || fontSizeInteger != null) {
                if (fontName == null) {
                    fontName = defaultFont.getFontName();
                }
                int fontStyle = defaultFont.getStyle();
                if (bold != null) {
                    fontStyle &= ~Font.BOLD;
                    if (bold) {
                        fontStyle |= Font.BOLD;
                    }
                }
                if (italic != null) {
                    fontStyle &= ~Font.ITALIC;
                    if (italic) {
                        fontStyle |= Font.ITALIC;
                    }
                }
                int fontSize = (fontSizeInteger != null) ? fontSizeInteger : defaultFont.getSize();
                defaultFont = new Font(fontName, fontStyle, fontSize);
            }
        }
        return defaultFont;
    }

    public static int getOtherAxis(int axis) {
        return (axis == View.X_AXIS) ? View.Y_AXIS : View.X_AXIS;
    }

    public static void repaint(JComponent component, Rectangle2D r) {
        component.repaint((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());
    }

    public static String toString(Color c) {
        return "RGB[" + c.getRed() + ';' + c.getGreen() + ';' + c.getBlue() + ']'; // NOI18N
    }

    public static String toString(Rectangle2D r) {
        return "XYWH[" + r.getX() + ';' + r.getY() + ';' + r.getWidth() + ';' + r.getHeight() + ']'; // NOI18N
    }

    public static String toStringHex8(int i) {
        String s = Integer.toHexString(i);
        while (s.length() < 8) {
            s = "0" + s;
        }
        return s;
    }

    public static String toStringId(Object o) {
        return (o != null)
                ? toStringHex8(System.identityHashCode(o))
                : "<NULL>"; // NOI18N
    }

    public static String toStringNameId(Object o) {
        if (o == null) {
            return "<NULL>"; // NOI18N
        }
        // Use last part (after '.') of class name
        String className = o.getClass().getName();
        className = className.substring(className.lastIndexOf('.') + 1);
        return className + "@" + toStringId(o);
    }

    public static String toStringPrec1(double d) {
        String s = Double.toString(d);
        int dotIndex = s.indexOf('.');
        if (dotIndex >= 0 && dotIndex < s.length() - 2) {
            s = s.substring(0, dotIndex + 2);
        }
        return s;
    }

    public static String toString(AttributeSet attributes) {
        boolean nonFirst = false;
        StringBuilder sb = new StringBuilder(200);
        String fontName = (String) attributes.getAttribute(StyleConstants.FontFamily);
        Boolean bold = (Boolean) attributes.getAttribute(StyleConstants.Bold);
        Boolean italic = (Boolean) attributes.getAttribute(StyleConstants.Italic);
        Integer fontSizeInteger = (Integer) attributes.getAttribute(StyleConstants.FontSize);
        if (fontName != null || bold != null || italic != null || fontSizeInteger != null) {
            sb.append("Font[");
            sb.append((fontName != null) ? '"' + fontName + '"' : '?').append(',');
            if (bold != null || italic != null) {
                if (bold != null) {
                    sb.append('B');
                }
                if (italic != null) {
                    sb.append('I');
                }
            } else {
                sb.append('?');
            }
            sb.append(',');
            sb.append((fontSizeInteger != null) ? fontSizeInteger : '?');
            sb.append("], ");
            nonFirst = true;
        }
        Color foreColor = (Color) attributes.getAttribute(StyleConstants.Foreground);
        if (foreColor != null) {
            if (nonFirst) {
                sb.append(", ");
            }
            sb.append("fg=").append(toString(foreColor));
            nonFirst = true;
        }
        Color backColor = (Color) attributes.getAttribute(StyleConstants.Background);
        if (backColor != null) {
            if (nonFirst) {
                sb.append(", ");
            }
            sb.append("bg=").append(toString(backColor));
            nonFirst = true;
        }
        return sb.toString();
    }

    public static String toString(JComponent component) {
        if (component == null) {
            return "<NULL>"; // NOI18N
        }
        StringBuilder sb = new StringBuilder(100);
        sb.append(toStringNameId(component));
        if (component instanceof JTextComponent) {
            JTextComponent textComponent = (JTextComponent) component;
            sb.append(" doc: ").append(toString(textComponent.getDocument()));
        }
        return sb.toString();
    }

    public static String toString(Document doc) {
        if (doc == null) {
            return "<NULL>"; // NOI18N
        }
        StringBuilder sb = new StringBuilder(100);
        sb.append(toStringNameId(doc));
        sb.append(", StreamDesc:");
        Object streamDesc = doc.getProperty(Document.StreamDescriptionProperty);
        if (streamDesc != null) {
            sb.append(streamDesc);
        } else {
            sb.append("<NULL>"); // NOI18N
        }
        return sb.toString();
    }

    public static void checkFragmentBounds(int p0, int p1, int startOffset, int length) {
        if (p0 < startOffset || p0 > p1 || p1 > startOffset + length) {
            throw new IllegalArgumentException("Illegal bounds: <" + p0 + "," + p1 + // NOI18N
                    "> outside of <" + startOffset + "," + (startOffset+length) + ">"); // NOI18N
        }
    }

    /**
     * Round given float number to maximum of 1/8 of fractional parts for
     * bound-related operations to partly eliminate rounding errors.
     *
     * @param f
     * @return
     */
    public static float cutFractions(float f) {
        return (float) (Math.ceil(f * 8f) / 8f);
    }

    /**
     * Round given double number to maximum of 1/8 of fractional parts for
     * bound-related operations to partly eliminate rounding errors.
     *
     * @param d
     * @return
     */
    public static float cutFractions(double d) {
        return (float) (Math.ceil(d * 8d) / 8d);
    }

}
