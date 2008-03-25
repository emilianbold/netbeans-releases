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

package org.netbeans.modules.websvc.axis2.options;

import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.modules.websvc.axis2.AxisUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Milan Kuchtiak
 */
public final class Axis2PanelController extends OptionsPanelController {


    public void update () {
//        getAdvancedPanel ().update ();
    }

    public void applyChanges () {
        if (advancedPanel.isChanged()) {
            System.out.println("apply changes");
            Preferences preferences = AxisUtils.getPreferences();
//            String oldAxisHome = preferences.get("AXIS_HOME",""); //NOI18N
            String oldAxisDeploy = preferences.get("AXIS_DEPLOY",""); //NOI18N
            
//            String axisHome = advancedPanel.getAxisHome();
//            if (!axisHome.equals(oldAxisHome)) {
//                preferences.put("AXIS_HOME", axisHome);
//            }
            String axisDeploy = advancedPanel.getAxisDeploy();
            System.out.println("changing AXIS_DEPLOY "+oldAxisDeploy+":"+axisDeploy);
            if (!axisDeploy.equals(oldAxisDeploy)) {
                preferences.put("AXIS_DEPLOY", axisDeploy);
            }

            String axisUrl = advancedPanel.getAxisUrl();
            String oldAxisUrl = preferences.get("AXIS_URL","");
            if (axisUrl.length() > 0 && !axisUrl.equals(oldAxisUrl)) {
                preferences.put("AXIS_URL", axisUrl);
            } else if (axisUrl.length() == 0) {
                preferences.remove("AXIS_URL");
            }

            String tomcatUser = advancedPanel.getTomcatManagerUsername();
            String oldTomcatUser = preferences.get("TOMCAT_USER","");
            if (tomcatUser != null && tomcatUser.length() > 0 && !tomcatUser.equals(oldTomcatUser)) {
                preferences.put("TOMCAT_MANAGER_USER", tomcatUser);
                preferences.put("TOMCAT_MANAGER_PASSWORD", advancedPanel.getTomcatManagerPassword());
            } else if (tomcatUser == null || tomcatUser.length() == 0) {
                preferences.remove("TOMCAT_MANAGER_USER");
                preferences.remove("TOMCAT_MANAGER_PASSWORD");
            }
            advancedPanel.setChanged(false);
        }
    }
    
    public void cancel () {
//        getAdvancedPanel ().cancel ();
    }
    
    public boolean isValid () {
//        return getAdvancedPanel ().dataValid ();
        return true;
    }
    
    public boolean isChanged () {
        return getAdvancedPanel ().isChanged ();
    }
    
    public JComponent getComponent (Lookup masterLookup) {
//        getAdvancedPanel ().init (masterLookup);
        return getAdvancedPanel ();
    }
    
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public void addPropertyChangeListener (PropertyChangeListener l) {
//        getAdvancedPanel ().addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
//        getAdvancedPanel ().removePropertyChangeListener (l);
    }

    private Axis2ConfigurationPanel advancedPanel;
    
    private Axis2ConfigurationPanel getAdvancedPanel () {
//        if (advancedPanel == null) {
            Preferences preferences = AxisUtils.getPreferences();
            String oldAxisDeploy = preferences.get("AXIS_DEPLOY",""); //NOI18N
            advancedPanel = new Axis2ConfigurationPanel(oldAxisDeploy);
            return advancedPanel;
//        }
//        return advancedPanel;
    }
}
