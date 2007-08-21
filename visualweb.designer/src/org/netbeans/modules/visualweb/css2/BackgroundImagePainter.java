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
package org.netbeans.modules.visualweb.css2;

import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

import org.netbeans.modules.visualweb.api.designer.cssengine.CssListValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import org.openide.ErrorManager;
import org.w3c.dom.Element;



/**
 * Paints the background image. Most of this code was extracted from
 * Swing text's Css.java class.  <p>
 * @todo Update the code to use REPEAT, REPEAT_X etc. flags instead
 *  of constants
 */
public class BackgroundImagePainter {
    public static final short NO_REPEAT = 0;
    public static final short REPEAT_X = 1;
    public static final short REPEAT_Y = 2;
    public static final short REPEAT = REPEAT_X | REPEAT_Y;
    private static final short HORIZONTAL_RELATIVE = 4;
    private static final short VERTICAL_RELATIVE = 8;
    ImageIcon backgroundImage;
    float hPosition;
    float vPosition;

    // bit mask: 0 for repeat x, 1 for repeat y, 2 for horiz relative,
    // 3 for vert relative
    int flags;

    // These are used when painting, updatePaintCoordinates updates them.
    private int paintX;
    private int paintY;
    private int paintMaxX;
    private int paintMaxY;

    /**
     * Construct a new background painter with the given repeat and position
     */
//    public BackgroundImagePainter(ImageIcon bgImage, Value repeatValue, ListValue positionValue) {
    public BackgroundImagePainter(ImageIcon bgImage, CssValue cssRepeatValue, CssListValue cssPositionValue, Element element, int defaultFontSize) {
        this.backgroundImage = bgImage;

        int repeat = BackgroundImagePainter.REPEAT;

//        if (repeatValue == CssValueConstants.REPEAT_VALUE) {
        if (CssProvider.getValueService().isRepeatValue(cssRepeatValue)) {
            repeat = BackgroundImagePainter.REPEAT;
//        } else if (repeatValue == CssValueConstants.NO_REPEAT_VALUE) {
        } else if (CssProvider.getValueService().isNoRepeatValue(cssRepeatValue)) {
            repeat = BackgroundImagePainter.NO_REPEAT;
//        } else if (repeatValue == CssValueConstants.REPEAT_X_VALUE) {
        } else if (CssProvider.getValueService().isRepeatXValue(cssRepeatValue)) {
            repeat = BackgroundImagePainter.REPEAT_X;
//        } else if (repeatValue == CssValueConstants.REPEAT_Y_VALUE) {
        } else if (CssProvider.getValueService().isRepeatYValue(cssRepeatValue)) {
            repeat = BackgroundImagePainter.REPEAT_Y;
        }

        // XXX TODO - support background-attachment

        /*
         * If fractionX is greater than or
         * equal to zero, the horizontal position will be taken to be a
         * fraction of the padding rectangle width, and fractionX is that fraction.
         * In that case, left is ignored. Otherwise, left is the offset from
         * the top left padding rectangle corner where the image top left corner
         * will be located.
         * Similarly for the vertical dimension.
         */
        int left = 0;
        int top = 0;
        float fractionX = 0.0f;
        float fractionY = 0.0f;

//        if (positionValue != null) {
        if (cssPositionValue != null) {
//            assert positionValue.getLength() == 2;
            if (cssPositionValue.getLength() != 2) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("The lenght of the list value should be 2, but is not"
                            + "\nlistValue=" + cssPositionValue
                            + "\nlength=" + cssPositionValue.getLength()));
            } else {

//            Value horiz = positionValue.item(0);
//            Value vert = positionValue.item(1);
                CssValue cssHoriz = cssPositionValue.item(0);
                CssValue cssVert = cssPositionValue.item(1);

            if ((cssHoriz != null) && (cssVert != null)) {
//                if (horiz.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
                if (CssProvider.getValueService().isOfPrimitivePercentageType(cssHoriz)) {
                    fractionX = cssHoriz.getFloatValue() / 100.0f;
                } else if (CssProvider.getValueService().isOfPrimitiveEmsType(cssHoriz)) {
                    int fontSize = CssUtilities.getDesignerFontForElement(element, null, defaultFontSize).getSize();
                    left = (int)(cssHoriz.getFloatValue() * fontSize);
                    // XXX we should allow negative percentages too!
                    fractionX = -2.0f; // cause BackgroundImagePainter to ignore it
                } else {
                    left = (int)cssHoriz.getFloatValue();
                    // XXX we should allow negative percentages too!
                    fractionX = -2.0f; // cause BackgroundImagePainter to ignore it
                }

//                if (vert.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
                if (CssProvider.getValueService().isOfPrimitivePercentageType(cssVert)) {
                    fractionY = cssVert.getFloatValue() / 100.0f;
                } else if (CssProvider.getValueService().isOfPrimitiveEmsType(cssVert)) {
                    int fontSize = CssUtilities.getDesignerFontForElement(element, null, defaultFontSize).getSize();
                    top = (int)(cssVert.getFloatValue() * fontSize);
                    fractionY = -2.0f; // cause BackgroundImagePainter to ignore it
                } else {
                    top = (int)cssVert.getFloatValue();
                    fractionY = -2.0f; // cause BackgroundImagePainter to ignore it
                }
            }
            
            }
        }

        flags = repeat;

        if (fractionX >= -1.0) {
            flags |= HORIZONTAL_RELATIVE;
            hPosition = fractionX;
//            assert left == 0;
            if (left != 0) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("left is expected to be 0, left=" + left)); // NOI18N
            }
        } else {
            hPosition = left;
        }

        if (fractionY >= -1.0) {
            flags |= VERTICAL_RELATIVE;
            vPosition = fractionY;
//            assert top == 0;
            if (top != 0) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("top is expected to be 0, top=" + top)); // NOI18N
            }
        } else {
            vPosition = top;
        }

        // TODO - the old code premultiplied the font size here - WHY OH WHY?
        // Ah, they thought the percentages were in terms of em's ?
    }

//    /**
//     * Returns the ImageIcon to draw in the background for
//     * <code>attr</code>.
//     */
//    public static ImageIcon getBackgroundImage(WebForm webform, Element element) {
////        Value value = CssLookup.getValue(element, XhtmlCss.BACKGROUND_IMAGE_INDEX);
//        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.BACKGROUND_IMAGE_INDEX);
//
////        if (value == CssValueConstants.NONE_VALUE) {
//        if (CssProvider.getValueService().isNoneValue(cssValue)) {
//            return null;
//        }
//
////        String urlString = value.getStringValue();
//        String urlString = cssValue.getStringValue();
//
//        // XXX This is wrong. I should get the -stylesheet- URL.
//        // And what about linked style sheets?
//        URL reference = webform.getMarkup().getBase();
//        URL url = null;
//
//        try {
//            url = new URL(reference, urlString);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//
//            return null;
//        }
//
//        ImageCache cache = webform.getDocument().getImageCache();
//        ImageIcon ii = cache.get(url);
//
//        if (ii == null) {
//            ii = new ImageIcon(url);
//            cache.put(url, ii);
//        }
//
//        return ii;
//    }

//    /**
//     * Returns the ImageIcon to draw in the background for
//     * <code>attr</code>.
//     */
//    public static ImageIcon getBackgroundImage(URL reference, Element element) {
////        Value value = CssLookup.getValue(element, XhtmlCss.BACKGROUND_IMAGE_INDEX);
//        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.BACKGROUND_IMAGE_INDEX);
//
//
////        if (value == CssValueConstants.NONE_VALUE) {
//        if (CssProvider.getValueService().isNoneValue(cssValue)) {
//            return null;
//        }
//
////        String urlString = value.getStringValue();
//        String urlString = cssValue.getStringValue();
//
//        URL url = null;
//
//        try {
//            url = new URL(reference, urlString);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//
//            return null;
//        }
//
//        //        ImageCache cache = doc.getImageCache();
//        //        ImageIcon ii = cache.get(url);
//        //        if (ii == null) {
//        ImageIcon ii = new ImageIcon(url);
//
//        //            cache.put(url, ii);
//        //        }
//        return ii;
//    }

    public void paint(Graphics g, float x, float y, float w, float h) {
        Rectangle clip = g.getClipBounds();

        if (clip != null) {
            // Constrain the clip so that images don't draw outside the
            // legal bounds.
            g.clipRect((int)x, (int)y, (int)w, (int)h);
        }

        if ((flags & REPEAT) == 0) {
            // no repeating
            int width = backgroundImage.getIconWidth();
            int height = backgroundImage.getIconWidth();

            if ((flags & HORIZONTAL_RELATIVE) == HORIZONTAL_RELATIVE) {
                paintX = (int)((x + (w * hPosition)) - ((float)width * hPosition));
            } else {
                paintX = (int)x + (int)hPosition;
            }

            if ((flags & VERTICAL_RELATIVE) == VERTICAL_RELATIVE) {
                paintY = (int)((y + (h * vPosition)) - ((float)height * vPosition));
            } else {
                paintY = (int)y + (int)vPosition;
            }

            if ((clip == null) ||
                    !(((paintX + width) <= clip.x) || ((paintY + height) <= clip.y) ||
                    (paintX >= (clip.x + clip.width)) || (paintY >= (clip.y + clip.height)))) {
                backgroundImage.paintIcon(null, g, paintX, paintY);
            }
        } else {
            int width = backgroundImage.getIconWidth();
            int height = backgroundImage.getIconHeight();

            if ((width > 0) && (height > 0)) {
                paintX = (int)x;
                paintY = (int)y;
                paintMaxX = (int)(x + w);
                paintMaxY = (int)(y + h);

                // We also need to compute the position to begin painting at
                // even when repeating (Swing's StyleSheet.BackgroundImagePainter
                // wasn't doing this.)
                int offsetX = 0;

                if ((flags & HORIZONTAL_RELATIVE) == HORIZONTAL_RELATIVE) {
                    offsetX = (int)((w * hPosition) - ((float)width * hPosition));
                } else {
                    offsetX = (int)hPosition;
                }

                if ((flags & REPEAT_X) == REPEAT_X) {
                    if (offsetX > 0) {
                        // Move to a multiple before X
                        int mod = offsetX % width;

                        if (mod > 0) {
                            mod -= width;
                        }

                        offsetX = mod;
                    }
                }

                paintX += offsetX;

                int offsetY = 0;

                if ((flags & VERTICAL_RELATIVE) == VERTICAL_RELATIVE) {
                    offsetY = (int)((h * vPosition) - ((float)height * vPosition));
                } else {
                    offsetY = (int)vPosition;
                }

                if ((flags & REPEAT_Y) == REPEAT_Y) {
                    if (offsetY > 0) {
                        // Move to a multiple above y
                        int mod = offsetY % height;

                        if (mod > 0) {
                            mod -= height;
                        }

                        offsetY = mod;
                    }
                }

                paintY += offsetY;

                if (updatePaintCoordinates(clip, width, height)) {
                    while (paintX < paintMaxX) {
                        int ySpot = paintY;

                        while (ySpot < paintMaxY) {
                            backgroundImage.paintIcon(null, g, paintX, ySpot);
                            ySpot += height;
                        }

                        paintX += width;
                    }
                }
            }
        }

        if (clip != null) {
            // Reset clip.
            g.setClip(clip.x, clip.y, clip.width, clip.height);
        }
    }

    private boolean updatePaintCoordinates(Rectangle clip, int width, int height) {
        if ((flags & REPEAT) == REPEAT_X) {
            paintMaxY = paintY + 1;
        } else if ((flags & REPEAT) == REPEAT_Y) {
            paintMaxX = paintX + 1;
        }

        if (clip != null) {
            if (((flags & REPEAT) == REPEAT_X) &&
                    (((paintY + height) <= clip.y) || (paintY > (clip.y + clip.height)))) {
                // not visible.
                return false;
            }

            if (((flags & REPEAT) == REPEAT_Y) &&
                    (((paintX + width) <= clip.x) || (paintX > (clip.x + clip.width)))) {
                // not visible.
                return false;
            }

            if ((flags & REPEAT_X) == REPEAT_X) {
                if ((clip.x + clip.width) < paintMaxX) {
                    if ((((clip.x + clip.width) - paintX) % width) == 0) {
                        paintMaxX = clip.x + clip.width;
                    } else {
                        paintMaxX =
                            (((((clip.x + clip.width) - paintX) / width) + 1) * width) + paintX;
                    }
                }

                if (clip.x > paintX) {
                    paintX = ((clip.x - paintX) / width * width) + paintX;
                }
            }

            if ((flags & REPEAT_Y) == REPEAT_Y) {
                if ((clip.y + clip.height) < paintMaxY) {
                    if ((((clip.y + clip.height) - paintY) % height) == 0) {
                        paintMaxY = clip.y + clip.height;
                    } else {
                        paintMaxY =
                            (((((clip.y + clip.height) - paintY) / height) + 1) * height) + paintY;
                    }
                }

                if (clip.y > paintY) {
                    paintY = ((clip.y - paintY) / height * height) + paintY;
                }
            }
        }

        // Valid
        return true;
    }
}
