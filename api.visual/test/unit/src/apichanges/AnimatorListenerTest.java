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
package apichanges;

import framework.VisualTestCase;
import org.netbeans.api.visual.animator.AnimatorEvent;
import org.netbeans.api.visual.animator.AnimatorListener;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Test for #99048 - Animator listener is needed
 * @author David Kaspar
 */
public class AnimatorListenerTest extends VisualTestCase {

    public AnimatorListenerTest (String name) {
        super (name);
    }

    public void testAnimatorListener () {
        final Scene scene = new Scene ();
        Widget widget = new Widget (scene);
        scene.addChild (widget);

        AnimatorListener listener = new AnimatorListener() {
            public void animatorStarted (AnimatorEvent event) {
                getRef ().println ("Animator started");
            }
            public void animatorReset (AnimatorEvent event) {
                getRef ().println ("Animator reset");
            }
            public void animatorFinished (AnimatorEvent event) {
                getRef ().println ("Animator finished");
            }
            public void animatorPreTick (AnimatorEvent event) {
                if (event.getProgress () >= 1.0)
                    getRef ().println ("Animator pre-tick: " + event.getProgress ());
            }
            public void animatorPostTick (AnimatorEvent event) {
                if (event.getProgress () >= 1.0)
                getRef ().println ("Animator post-tick: " + event.getProgress ());
            }
        };
        scene.getSceneAnimator ().getPreferredLocationAnimator ().addAnimatorListener (listener);
        widget.setPreferredLocation (new Point (0, 0));
        scene.getSceneAnimator ().animatePreferredLocation (widget, new Point (100, 100));

        final JFrame[] frame = new JFrame[1];
        try {
            SwingUtilities.invokeAndWait (new Runnable() {
                public void run () {
                    frame[0] = showFrame (scene);
                }
            });

            Thread.sleep (2000);

            SwingUtilities.invokeAndWait (new Runnable() {
                public void run () {
                    frame[0].setVisible (false);
                    frame[0].dispose ();
                }
            });
        } catch (InterruptedException e) {
            throw new AssertionError (e);
        } catch (InvocationTargetException e) {
            throw new AssertionError (e);
        }

        compareReferenceFiles ();
    }

}
