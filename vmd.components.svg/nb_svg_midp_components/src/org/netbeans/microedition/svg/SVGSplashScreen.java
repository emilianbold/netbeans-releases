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
 * SVGSplashScreen.java
 *
 * Created on June 19, 2006, 2:50 PM
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

/**
 * This component represents a splash screen, which is usually being displayed
 * when the application starts. It waits for a specified amount of time (by default
 * 5000 milliseconds) and then calls specified command listener commandAction method
 * with DISMISS_COMMAND as command parameter. It displays an animation of SVG image.
 * @author breh
 */
public class SVGSplashScreen extends SVGAnimatorWrapper {
    
     /**
     * Command fired when the screen is about to be dismissed
     */
    public static final Command DISMISS_COMMAND = new Command("Dismiss",Command.OK,0);
    
    /**
     * Timeout value which wait forever. Value is "0".
     */
    public static final int FOREVER = 0;
    
    // timeout for the splashscreen
    private int timeout = 5000;
    // allow interrupt
    private boolean allowTimeoutInterrupt = true;
    // show timestamp
    private static long currentDisplayTimestamp;
    
    /**
     * Creates a new instance of SVGSplashScreen 
     * 
     * <p/> Please note, supplied SVGImage shouldn't be reused in other SVGAnimator.
     */
    public SVGSplashScreen(SVGImage svgImage, Display display) throws IllegalArgumentException {
        super(svgImage, display);
        setFullScreenMode(true);
        setSVGEventListener(new SplashScreenSvgEventListener());
    }
    
    /**
     * Sets the timeout of the splash screen - i.e. the time in milliseconds for
     * how long the splash screen is going to be shown on the display.
     * <p/>
     * If the supplied timeout is 0, then the splashscreen waits forever (it needs to
     * be dismissed by pressing a key)
     *
     * @param timeout in milliseconds
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }    
    
    
    /**
     * Gets current timeout of the splash screen
     * @return timeout value
     */
    public int getTimeout() {
        return timeout;
    }
    
    /**
     * Set to true, when the timeout with a specified timeout interval can
     * be interrupted by pressing a key.
     * @param allow true if the user can interrupt the screen, false if the user need to wait
     * until timeout.
     */
    public void setAllowTimeoutInterrupt(boolean allow) {
        this.allowTimeoutInterrupt = allow;
    }
    
    /**
     * Can be the splashscreen interrupted (dismissed) by the user pressing a key?
     * @return true if user can interrupt it, false otherwise
     */
    public boolean isAllowTimeoutInterrupt() {
        return allowTimeoutInterrupt;
    }
    
    
    // user dismiss method (calls commandAction with DISMISS_COMMAND as argument)
    private void doDismiss() {
        //System.err.println("doDismiss");
        CommandListener cl = getCommandListener();
        if (cl != null) {
            cl.commandAction(DISMISS_COMMAND,this);
        }
    }
    
    
    // timer
    private class Watchdog extends Thread {
        
        private int timeout;
        private long currentDisplayTimestamp;
        
        private Watchdog(int timeout, long currentDisplayTimestamp) {
            this.timeout = timeout;
            this.currentDisplayTimestamp = currentDisplayTimestamp;
        }
        
        public void run() {
            try {
                //System.err.println("Watchdog running");
                Thread.sleep(timeout);
            } catch (InterruptedException ie) {
            }
            // timeout (only if current display timout matches) - this means this
            // splash screen is still being shown on the display
            if (this.currentDisplayTimestamp == SVGSplashScreen.this.currentDisplayTimestamp) {
                doDismiss();
            }
        }
        
        
    }   
    
    
    
    
    // svg event listener listening on key presses
    private class SplashScreenSvgEventListener implements SVGEventListener {

        public void keyPressed(int i) {
            //System.out.println("keyPressed");
            if (allowTimeoutInterrupt) {
                doDismiss();
            }
        }

        public void keyReleased(int i) {
        }

        public void pointerPressed(int i, int i0) {
            if (allowTimeoutInterrupt) {
                doDismiss();
            }            
        }

        public void pointerReleased(int i, int i0) {
        }

        public void hideNotify() {
            currentDisplayTimestamp = System.currentTimeMillis();
        }

        public void showNotify() {
            //System.out.println("showNotify");
            // start watchdog task - only when applicable
            currentDisplayTimestamp = System.currentTimeMillis();
            if (timeout > 0) {
                Watchdog w = new Watchdog(timeout, currentDisplayTimestamp);
                w.start();
            }
        }

        public void sizeChanged(int i, int i0) {
        }
        
    }
    
    
}
