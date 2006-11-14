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
package org.netbeans.api.visual.animator;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.animator.PreferredBoundsAnimator;
import org.netbeans.modules.visual.animator.PreferredLocationAnimator;
import org.netbeans.modules.visual.animator.ZoomAnimator;
import org.netbeans.modules.visual.animator.ColorAnimator;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.awt.*;

/**
 * Manages all animations on a scene. An animation can be registered and started by calling Animator.start method.
 * The class contains a few built-in animators: preferredLocation, preferredBounds, background, foreground, zoomFactor.
 * 
 * @author David Kaspar
 */
public final class SceneAnimator {

    private static final long TIME_PERIOD = 500;
    private static final long SLEEP = 10;

    private Scene scene;

    private final HashMap<Animator, Long> animators = new HashMap<Animator, Long> ();
    private HashMap<Animator, Double> cache;
    private final Runnable task = new UpdateTask ();
    private volatile boolean taskAlive;

    private PreferredLocationAnimator preferredLocationAnimator = new PreferredLocationAnimator (this);
    private PreferredBoundsAnimator preferredBoundsAnimator = new PreferredBoundsAnimator (this);
    private ZoomAnimator zoomAnimator = new ZoomAnimator (this);
    private ColorAnimator colorAnimator = new ColorAnimator (this);

    /**
     * Creates a scene animator.
     * @param scene
     */
    public SceneAnimator (Scene scene) {
        this.scene = scene;
    }

    /**
     * Returns an assigned scene.
     * @return the scene
     */
    public Scene getScene () {
        return scene;
    }
    
    void start (Animator animator) {
        synchronized (animators) {
            animators.put (animator, System.currentTimeMillis ());
            animator.reset ();
            if (! taskAlive) {
                taskAlive = true;
                RequestProcessor.getDefault ().post (task);
            }
        }
    }

    boolean isRunning (Animator animator) {
        synchronized (animators) {
            if (animators.containsKey (animator))
                return true;
            if (cache != null  &&  cache.containsKey (animator))
                return true;
        }
        return false;
    }

    /**
     * Returns whether a preferredLocation animator for a specified widget is running.
     * @param widget the widget
     * @return true if running
     */
    public boolean isAnimatingPreferredLocation (Widget widget) {
        return isRunning (preferredLocationAnimator);
    }

    /**
     * Starts preferredLocation animation for a specified widget.
     * @param widget the widget
     * @param targetPreferredLocation the target preferred location
     */
    public void animatePreferredLocation (Widget widget, Point targetPreferredLocation) {
        preferredLocationAnimator.setPreferredLocation (widget, targetPreferredLocation);
    }

    /**
     * Returns whether a preferredBounds animator for a specified widget is running.
     * @param widget the widget
     * @return true if running
     */
    public boolean isAnimatingPreferredBounds (Widget widget) {
        return isRunning (preferredBoundsAnimator);
    }

    /**
     * Starts preferredBounds animation for a specified widget.
     * @param widget the widget
     * @param targetPreferredBounds the target preferred bounds
     */
    public void animatePreferredBounds (Widget widget, Rectangle targetPreferredBounds) {
        preferredBoundsAnimator.setPreferredBounds (widget, targetPreferredBounds);
    }

    /**
     * Returns whether a zoomFactor animator is running.
     * @return true if running
     */
    public boolean isAnimatingZoomFactor () {
        return isRunning (zoomAnimator);
    }

    /**
     * Returns a target zoom factor.
     * @return the target zoom factor
     */
    public double getTargetZoomFactor () {
        return zoomAnimator.getTargetZoom ();
    }

    /**
     * Starts zoomFactor animation.
     * @param targetZoomFactor the target zoom factor
     */
    public void animateZoomFactor (double targetZoomFactor) {
        zoomAnimator.setZoomFactor (targetZoomFactor);
    }

    /**
     * Returns whether a backgroundColor animator for a specified widget is running.
     * @param widget the widget
     * @return true if running
     */
    public boolean isAnimatingBackgroundColor (Widget widget) {
        return isRunning (colorAnimator);
    }

    /**
     * Starts backgroundColor animation for a specified widget.
     * @param widget the widget
     * @param targetBackgroundColor the target background color
     */
    public void animateBackgroundColor (Widget widget, Color targetBackgroundColor) {
        colorAnimator.setBackgroundColor (widget, targetBackgroundColor);
    }

    /**
     * Returns whether a foregroundColor animator for a specified widget is running.
     * @param widget the widget
     * @return true if running
     */
    public boolean isAnimatingForegroundColor (Widget widget) {
        return isRunning (colorAnimator);
    }

    /**
     * Starts foregroundColor animation for a specified widget.
     * @param widget the widget
     * @param targetForegroundColor the target foreground color
     */
    public void animateForegroundColor (Widget widget, Color targetForegroundColor) {
        colorAnimator.setForegroundColor (widget, targetForegroundColor);
    }

    private class UpdateTask implements Runnable {

        public void run () {
            synchronized (animators) {
                long currentTime = System.currentTimeMillis ();
                Set<Map.Entry<Animator, Long>> entries = animators.entrySet ();
                cache = new HashMap<Animator, Double> ();

                for (Iterator<Map.Entry<Animator, Long>> iterator = entries.iterator (); iterator.hasNext ();) {
                    Map.Entry<Animator, Long> entry = iterator.next ();
                    long diff = currentTime - entry.getValue ();
                    double progress;
                    if (diff < 0  ||  diff > TIME_PERIOD) {
                        iterator.remove ();
                        progress = 1.0;
                    } else
                        progress = (double) diff / (double) TIME_PERIOD;
                    cache.put (entry.getKey (), progress);
                }
            }

            try {
                SwingUtilities.invokeAndWait (new Runnable () {
                    public void run () {
                        for (final Map.Entry<Animator, Double> entry : cache.entrySet ())
                            entry.getKey ().performTick (entry.getValue ());
                        scene.validate ();
                    }
                });
            } catch (InterruptedException e) {
                ErrorManager.getDefault ().notify (e);
            } catch (InvocationTargetException e) {
                ErrorManager.getDefault ().notify (e);
            }

            try {
                Thread.sleep (SLEEP);
            } catch (InterruptedException e) {
                ErrorManager.getDefault ().notify (e);
            }

            synchronized (animators) {
                cache = null;
                taskAlive = animators.size () > 0;
                if (taskAlive)
                    RequestProcessor.getDefault ().post (task);
            }
        }

    }

}
