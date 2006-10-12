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
 * @author Stepan Herold
 */
public class Customizer extends JTabbedPane {

    private final DeploymentManager dmp;
    private ConnectionTabVisualPanel ctvp;

    public Customizer(DeploymentManager dmp) {
        
        this.dmp = dmp;
        initComponents ();
    }
    public void removeNotify(){
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
                }
                putClientProperty("HelpID", helpID); // NOI18N
            }
        });
        DeploymentManagerProperties dmpr = new DeploymentManagerProperties(dmp);

        CustomizerDataSupport custData = new CustomizerDataSupport(dmpr);
        ctvp = new ConnectionTabVisualPanel(dmp);
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Connection"), ctvp);
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Tab_Classes"), 
               CustomizerSupport.createClassesCustomizer(custData.getClassModel()));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Tab_Sources"), 
                CustomizerSupport.createSourcesCustomizer(custData.getSourceModel(), null));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Tab_Javadoc"), 
                CustomizerSupport.createJavadocCustomizer(custData.getJavadocsModel(), null));
    }
}
