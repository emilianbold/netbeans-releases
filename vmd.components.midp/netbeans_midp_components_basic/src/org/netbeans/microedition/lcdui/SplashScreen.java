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
 * SplashScreen.java
 *
 * Created on August 26, 2005, 10:19 AM
 */

package org.netbeans.microedition.lcdui;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;

/**
 * This component represents a splash screen, which is usually being displayed
 * when the application starts. It waits for a specified amount of time (by default
 * 5000 milliseconds) and then calls specified command listener commandAction method
 * with DISMISS_COMMAND as command parameter.
 * <p/>
 * This version is using CommandListener and static Command pattern, but is still
 * compatible with older version. So if there is no command listener specified,
 * it still can use setNextDisplayable() method to specify the dismiss screen and
 * automatically switch to it.
 * @author breh
 */
public class SplashScreen extends AbstractInfoScreen {

        
    /**
     * Command fired when the screen is about to be dismissed
     */
    public static final Command DISMISS_COMMAND = new Command("Dismiss",Command.OK,0);
    
    /**
     * Timeout value which wait forever. Value is "0".
     */
    public static final int FOREVER = 0;
    
    private static final int DEFAULT_TIMEOUT = 5000;
    
    private int timeout = DEFAULT_TIMEOUT;
    private boolean allowTimeoutInterrupt = true;
    
    private long currentDisplayTimestamp;
    
    /**
     * Creates a new instance of SplashScreen
     * @param display display - cannot be null
     * @throws java.lang.IllegalArgumentException when the display parameter is null
     */
    public SplashScreen(Display display) throws IllegalArgumentException  {
        super(display);
    }
    
    // properties
    
    
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
     *
     * @return timeout value
     */
    public int getTimeout() {
        return timeout;
    }
    
    /**
     * When set to true, the splashscreen timeout can be interrupted 
     * (and thus dismissed) by pressing a key.
     *
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
    
    
    // canvas methods
    
    /**
     * keyPressed callback
     * @param keyCode
     */
    protected void keyPressed(int keyCode) {
        if (allowTimeoutInterrupt) {
            doDismiss();
        }
    }
    
    
    /**
     * pointerPressed callback
     * @param x
     * @param y
     */
    protected void pointerPressed(int x, int y) {
        if (allowTimeoutInterrupt) {
            doDismiss();
        }
    }
    
    /**
     * starts the coundown of the timeout
     */
    protected void showNotify() {
        super.showNotify();
        // start watchdog task - only when applicable
        currentDisplayTimestamp = System.currentTimeMillis();
        if (timeout > 0) {
            Watchdog w = new Watchdog(timeout, currentDisplayTimestamp);
            w.start();
        }
    }
    
    
    protected void hideNotify() {
        super.hideNotify();
        currentDisplayTimestamp = System.currentTimeMillis();
    }
    
    
    
    // private stuff
    
    private void doDismiss() {
        CommandListener commandListener = getCommandListener();
        if (commandListener == null) {
            switchToNextDisplayable(); // @deprecated - works only if 
                                       // appropriate setters were called and no command listener 
                                       // was assigned to this component
        } else {
            commandListener.commandAction(DISMISS_COMMAND,this);
        }
    }
    
    
    
    private class Watchdog extends Thread {
        
        private int timeout;
        private long currentDisplayTimestamp;
        
        private Watchdog(int timeout, long currentDisplayTimestamp) {
            this.timeout = timeout;
            this.currentDisplayTimestamp = currentDisplayTimestamp;
        }
        
        public void run() {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ie) {
            }
            // doDismiss (only if current display timout matches) - this means this
            // splash screen is still being shown on the display
            if (this.currentDisplayTimestamp == SplashScreen.this.currentDisplayTimestamp) {
                doDismiss();
            }
        }
        
        
    }
    

}
