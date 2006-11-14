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
package org.netbeans.modules.visual.animator;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.animator.Animator;
import org.netbeans.api.visual.animator.SceneAnimator;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David Kaspar
 */
public final class ColorAnimator extends Animator {

    private HashMap<Widget, Color> sourceBackgroundColors = new HashMap<Widget, Color> ();
    private HashMap<Widget, Color> targetBackgroundColors = new HashMap<Widget, Color> ();
    private HashMap<Widget, Color> sourceForegroundColors = new HashMap<Widget, Color> ();
    private HashMap<Widget, Color> targetForegroundColors = new HashMap<Widget, Color> ();

    public ColorAnimator (SceneAnimator sceneAnimator) {
        super (sceneAnimator);
    }

    public void setBackgroundColor (Widget widget, Color backgroundColor) {
        assert widget != null;
        assert backgroundColor != null;
        sourceBackgroundColors.clear ();
        targetBackgroundColors.put (widget, backgroundColor);
        start ();
    }

    public void setForegroundColor (Widget widget, Color foregroundColor) {
        assert widget != null;
        assert foregroundColor != null;
        sourceForegroundColors.clear ();
        targetForegroundColors.put (widget, foregroundColor);
        start ();
    }

    protected void tick (double progress) {
        for (Map.Entry<Widget, Color> entry : targetBackgroundColors.entrySet ()) {
            Widget widget = entry.getKey ();
            Color sourceColor = sourceBackgroundColors.get (widget);
            if (sourceColor == null) {
                Paint background = widget.getBackground ();
                sourceColor = background instanceof Color ? (Color) background : Color.WHITE;
                sourceBackgroundColors.put (widget, sourceColor);
            }
            Color targetColor = entry.getValue ();

            Color color;
            if (progress >= 1.0)
                color = targetColor;
            else
                color = new Color (
                        (int) (sourceColor.getRed () + progress * (targetColor.getRed () - sourceColor.getRed ())),
                        (int) (sourceColor.getGreen () + progress * (targetColor.getGreen () - sourceColor.getGreen ())),
                        (int) (sourceColor.getBlue () + progress * (targetColor.getBlue () - sourceColor.getBlue ()))
                );
            widget.setBackground (color);
        }

        for (Map.Entry<Widget, Color> entry : targetForegroundColors.entrySet ()) {
            Widget widget = entry.getKey ();
            Color sourceColor = sourceForegroundColors.get (widget);
            if (sourceColor == null) {
                sourceColor = widget.getForeground ();
                sourceForegroundColors.put (widget, sourceColor);
            }
            Color targetColor = entry.getValue ();

            Color color;
            if (progress >= 1.0)
                color = targetColor;
            else
                color = new Color (
                        (int) (sourceColor.getRed () + progress * (targetColor.getRed () - sourceColor.getRed ())),
                        (int) (sourceColor.getGreen () + progress * (targetColor.getGreen () - sourceColor.getGreen ())),
                        (int) (sourceColor.getBlue () + progress * (targetColor.getBlue () - sourceColor.getBlue ()))
                );
            widget.setForeground (color);
        }

        if (progress >= 1.0) {
            sourceBackgroundColors.clear ();
            targetBackgroundColors.clear ();
            sourceForegroundColors.clear ();
            targetForegroundColors.clear ();
        }
    }

}
