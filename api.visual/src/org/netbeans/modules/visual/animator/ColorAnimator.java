/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
