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
package org.netbeans.api.visual.animator;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.awt.*;

/**
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

    public SceneAnimator (Scene scene) {
        this.scene = scene;
    }

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

    public boolean isAnimatingPreferredLocation (Widget widget) {
        return isRunning (preferredLocationAnimator);
    }

    public void animatePreferredLocation (Widget widget, Point targetPreferredLocation) {
        preferredLocationAnimator.setPreferredLocation (widget, targetPreferredLocation);
    }

    public boolean isAnimatingPreferredBounds (Widget widget) {
        return isRunning (preferredBoundsAnimator);
    }

    public void animatePreferredBounds (Widget widget, Rectangle targetPreferredBounds) {
        preferredBoundsAnimator.setPreferredBounds (widget, targetPreferredBounds);
    }

    public boolean isAnimatingZoomFactor () {
        return isRunning (zoomAnimator);
    }

    public double getTargetZoomFactor () {
        return zoomAnimator.getTargetZoom ();
    }

    public void animateZoomFactor (double targetZoomFactor) {
        zoomAnimator.setZoomFactor (targetZoomFactor);
    }

    @Deprecated
    public PreferredLocationAnimator getPreferredLocationAnimator () {
        return preferredLocationAnimator;
    }

    @Deprecated
    public PreferredBoundsAnimator getPreferredBoundsAnimator () {
        return preferredBoundsAnimator;
    }

    @Deprecated
    public ZoomAnimator getZoomAnimator () {
        return zoomAnimator;
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
