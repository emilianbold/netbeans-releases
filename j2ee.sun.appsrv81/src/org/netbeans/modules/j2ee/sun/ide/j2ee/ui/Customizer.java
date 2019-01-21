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

package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.sun.ide.j2ee.DeploymentManagerProperties;

/**
 * AppServer instance customizer which is accessible from server manager.
 *
 * 
 */
public class Customizer extends JTabbedPane {

    private final DeploymentManager dmp;
    private ConnectionTabVisualPanel ctvp;
    private OptionTabVisualPanel otvp;

    public Customizer(DeploymentManager dmp) {
        
        this.dmp = dmp;
        initComponents ();
    }
    public void removeNotify(){
        super.removeNotify();
        ctvp.syncUpWithModel();
    }
    private void initComponents() {
        getAccessibleContext().setAccessibleName (NbBundle.getMessage(Customizer.class,"ACS_Customizer")); // NOI18N
        getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(Customizer.class,"ACS_Customizer")); // NOI18N
        // set help ID according to selected tab
        addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                String helpID = null;
                switch (getSelectedIndex()) {
                    case 0 : helpID = "appsrv81_customizer_connection"; // NOI18N
                             break;
                    case 1 : helpID = "appsrv81_customizer_classes";    // NOI18N
                             break;
                    case 2 : helpID = "appsrv81_customizer_sources";    // NOI18N
                             break;
                    case 3 : helpID = "appsrv81_customizer_javadoc";    // NOI18N
                             break;
                    case 4 : helpID = "appsrv81_customizer_options";    // NOI18N
                             break;
                    default: helpID = "appsrv81_customizer_connection"; // NOI18N
                             break;
                }
                putClientProperty("HelpID", helpID); // NOI18N
            }
        });
        DeploymentManagerProperties dmpr = new DeploymentManagerProperties(dmp);

        CustomizerDataSupport custData = new CustomizerDataSupport(dmpr);
        ctvp = new ConnectionTabVisualPanel(dmp);
        otvp = new OptionTabVisualPanel(dmp);
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Connection"), ctvp);
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Tab_Classes"), 
               CustomizerSupport.createClassesCustomizer(custData.getClassModel()));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Tab_Sources"), 
                CustomizerSupport.createSourcesCustomizer(custData.getSourceModel(), null));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Tab_Javadoc"), 
                CustomizerSupport.createJavadocCustomizer(custData.getJavadocsModel(), null));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Options"), otvp);
    }
}
