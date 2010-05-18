/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

 /*
 * SVGWaitScreen.java
 *
 * Created on June 19, 2006, 4:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.microedition.svg;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.m2g.SVGEventListener;
import javax.microedition.m2g.SVGImage;
import org.netbeans.microedition.util.CancellableTask;

/**
 * This component suits as a wait screen, which let the user to execute a blocking
 * background task (e.g. a network communication) and waits for it until finished.
 * During the execution of the task, an SVG image/animation is being shown
 * on the screen.
 * <p/>
 * The background task is being started immediately prior the component is being
 * shown on the screen.
 * <p/>
 * When the background task is finished, this component calls commandAction method
 * on assigned CommandListener object. In the case of success, the commandAction method
 * is called with SUCCESS_COMMAND as parameter, in the case of failure, the commandAction
 * method is called with FAILURE_COMMAND as parameter.
 * <p/>
 * @author breh
 */
public class SVGWaitScreen extends SVGAnimatorWrapper {
     
    /**
     * Command fired when the background task was finished succesfully
     */
    public static final Command SUCCESS_COMMAND = new Command("Success",Command.OK,0);
    
    /**
     * Command fired when the background task failed (threw exception)
     */
    public static final Command FAILURE_COMMAND = new Command("Failure",Command.OK,0);

    
    // background task 
    private CancellableTask task;
    // background thread task executor
    private Thread backgroundExecutor;
    
    
    /**
     * 
     * Creates a new instance of SVGWaitScreen. It requires instance of SVGImage, which will
     * be used for animation and display. 
     * <p/> Please note, supplied SVGImage shouldn't be reused in other SVGAnimator.
     */
    public SVGWaitScreen(SVGImage svgImage, Display display) throws IllegalArgumentException {
        super(svgImage, display);
        setSVGEventListener(new WaitScreenSvgEventListener());
    }
    
    
    /**
     * Sets the task to be run on the background.
     * @param task task to be executed
     */
    public void setTask(CancellableTask task) {
        this.task = task;
    }
    
    
    /**
     * Gets the background task.
     * @return task being executed in background while this component is being shown
     * on the screen
     */
    public CancellableTask getTask() {
        return task;
    }
    
    
    // private stuff
    private void doAction() {
        CommandListener cl = getCommandListener();
        if (cl != null) {            
            if ((task != null) && (task.hasFailed())) {
                cl.commandAction(FAILURE_COMMAND,this);
            } else {
                // task didn't failed - success !!!
                cl.commandAction(SUCCESS_COMMAND,this);
            }
        }
        
    }
    
    
    
    /**
     * BackgroundExecutor task
     */
    private class BackgroundExecutor implements Runnable {
        
        private CancellableTask task;
        
        public BackgroundExecutor(CancellableTask task)	throws IllegalArgumentException {
            if (task == null) throw new IllegalArgumentException("Task parameter cannot be null");
            this.task = task;
        }
        
        public void run() {
            try {
                task.run();
            } finally {
                SVGWaitScreen.this.backgroundExecutor = null;
                doAction();
            }
        }
    }    
    
    
    // svg event listener listening on key presses
    private class WaitScreenSvgEventListener implements SVGEventListener {

        public void keyPressed(int i) {
        }

        public void keyReleased(int i) {
        }

        public void pointerPressed(int i, int i0) {
        }

        public void pointerReleased(int i, int i0) {
        }

        public void hideNotify() {
        }

        public void showNotify() {
            if (task != null) {
                if (backgroundExecutor == null) {
                    backgroundExecutor = new Thread(new BackgroundExecutor(task));
                    backgroundExecutor.start();
                }
            }  else {                
                // switch to next displayable immediatelly - no task was assigned
                // do it when the task is repainted - on some devices there are
                // some race-conditions
                getDisplay().callSerially(new Runnable() {
                    public void run() {
                        doAction();
                    }
                });
        }
        }

        public void sizeChanged(int i, int i0) {
        }
        
    }    
    
}
