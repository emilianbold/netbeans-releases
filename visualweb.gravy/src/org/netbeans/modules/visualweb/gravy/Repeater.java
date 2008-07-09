/*
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
* Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.gravy;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;

/**
 * This class is intended to be used as a base for anonymous classes
 * aimed on several repeating of some action which is possible to failed
 * from first (second, third..) attempt
 */
public class Repeater {
    
    private int repeatCount = 3;
    private long repeatTimeout = 2000;
    
    /** Creates a new instance of Repeater 
     *  Uses default of 3 attempts with 2 sec timeout
     */
    public Repeater() {
    }
    
    /** Creates a new instance of Repeater
     *  Uses default 2 sec timeout
     * @param repeatCount Number of attempts to try
     */
    public Repeater(int repeatCount) {
        this.repeatCount = repeatCount;
    }
    
    /** Creates a new instance of Repeater
     * @param repeatCount Number of attempts to try
     * @param repeatTimout Timeout between attempts
     */
    public Repeater(int repeatCount, long repeatTimeout) {
        this.repeatCount = repeatCount;
        this.repeatTimeout = repeatTimeout;
    }
    
    /**
     * Trys to perform action() several attempts, if any errors are
     * thrown, tryis to repeat it until specified repeat number of
     * attempts is reached
     */
    public void tryAction() {
        boolean success = false;
        for(int i = 0; !success && i < repeatCount; i++) {
            try {
                action();
                success = true;
            } catch (Error e) {
                JemmyProperties.getCurrentOutput().printTrace("Attempt = " + (i + 1) + " failed with the following error:");
                e.printStackTrace(JemmyProperties.getCurrentOutput().getOutput());
                // We re-throw exception if this is the last attempt
                if (i == repeatCount - 1) throw e;
                new Timeout("repeatTimeout", repeatTimeout).sleep();
            } catch (RuntimeException re) {
                JemmyProperties.getCurrentOutput().printTrace("Attempt = " + (i + 1) + " failed with the following exception:");
                re.printStackTrace(JemmyProperties.getCurrentOutput().getOutput());
                // We re-throw exception if this is the last attempt
                if (i == repeatCount - 1) throw re;
                new Timeout("repeatTimeout", repeatTimeout).sleep();
            }
        }
    }

    /**
     * Trys to perform action() several attempts, if any errors are
     * thrown, tryis to repeat it until specified repeatCount number of
     * attempts is reached
     * @param repeatCount Number of attempts to perform
     */
    public void tryAction(int repeatCount) {
        this.repeatCount = repeatCount;
        tryAction();
    }
    
    /**
     * Trys to perform action() several attempts, if any errors are
     * thrown, tryis to repeat it until specified repeatCount number of
     * attempts is reached. Uses specified repeatTimeout between attempts
     * @param repeatCount Number of attempts to perform
     * @param repeatTimeout Timeout between attempts
     */
    public void tryAction(int repeatCount, long repeatTimeout) {
        this.repeatCount = repeatCount;
        this.repeatTimeout = repeatTimeout;
        tryAction();
    }
    
    /**
     * Action to perform. Implement it in your anonymous class
     */
    public void action() {
        JemmyProperties.getCurrentOutput().printTrace("Repeater: Nothing set to do!");
    }
}
