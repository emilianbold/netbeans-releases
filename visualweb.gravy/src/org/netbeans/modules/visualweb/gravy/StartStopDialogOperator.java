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

import java.awt.Component;
import java.awt.Container;
import javax.swing.JFrame;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JButtonOperator;

/**
 * This class is used in automated tests, developed for Creator 2.0/2.1, for launching
 * and shutting down of an application server, included into these products.
 */
public class StartStopDialogOperator extends JFrameOperator {
    JCheckBoxOperator autoHide;
    
    /** 
     * Creates a new instance of this class.
     */
    public StartStopDialogOperator() {
        super(new StartStopDialogFinder());
        copyEnvironment(Util.getMainWindow());
        getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 600000);
        getTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout", 600000);
    }
    
    /**
     * Initializes and returns a checkbox "Autohide"
     * @return an object JCheckBoxOperator
     */   
    public JCheckBoxOperator chbAutoHide() {
        if(autoHide == null) {
            autoHide = new JCheckBoxOperator(this, "Automatically close this window when finished");
        }
        return(autoHide);
    }
    
    /**
     * Marks or unmarks a checkbox "Auto Hide".
     * @param isAutoHide boolean value, which defines if a checkbox "Auto Hide" 
     * is marked or not.
     */
    public void setAutoHide(boolean isAutoHide) {
        chbAutoHide().changeSelection(isAutoHide);
    }
    
    /**
     * Waits until the required string appears in the dialog.
     * @param status string, which is waited in the dialog
     * @return an object JLabelOperator
     */
    public JLabelOperator waitStatus(String status) {
        return(new JLabelOperator(this, status));
    }
    
    /**
     * Waits until the string "Completed" appears in the dialog.
     * @return an object JLabelOperator
     */
    public JLabelOperator waitCompleted() {
        return(waitStatus("Completed"));
    }

    /**
     * Launches an application server.
     */
    public void start(String action) {
	new JButtonOperator(this, "Start Server").push();
    }

    /**
     * Shuts an application server down.
     */
    public void stop(String action) {
	new JButtonOperator(this, "Stop Server").push();
    }

    /**
     * Inner class, which is used for searching of a "Start/Stop-dialog" in 
     * Creator's IDE.
     */
    public static class StartStopDialogFinder implements ComponentChooser {
        /**
         * Searches a component "Start/Stop-dialog" in Creator's IDE
         * @return true if dialog is found, otherwise - false.
         */
        public boolean checkComponent(Component comp) {
            ComponentSearcher searcher = new ComponentSearcher((Container)comp);
            searcher.setOutput(JemmyProperties.getCurrentOutput().createErrorOutput());
            return(comp instanceof JFrame &&
            searcher.findComponent(new ComponentChooser() {
                public boolean checkComponent(Component comp) {
                    return(comp.getClass().getName().startsWith("Server Status"));
                }
                public String getDescription() {
                    return("ProgressUI");
                }
            }) != null);
        }
        public String getDescription() {
            return("StartStop dialog");
        }
    }
}
