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
package org.netbeans.modules.tomcat5.customizer;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.tomcat5.TomcatManager;

/**
 * Tomcat instance customizer which is accessible from server manager.
 *
 * @author Stepan Herold
 */
public class Customizer extends JTabbedPane {

    private static final String CLASSPATH = J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH;
    private static final String SOURCES = J2eeLibraryTypeProvider.VOLUME_TYPE_SRC;
    private static final String JAVADOC = J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC;

    private TomcatManager manager;
    private J2eePlatformImpl platform;

    public Customizer(TomcatManager aManager) {
        manager = aManager;
        platform = manager.getTomcatPlatform();
        initComponents ();
    }

    private void initComponents() {
        getAccessibleContext().setAccessibleName (NbBundle.getMessage(Customizer.class,"ACS_Customizer")); // NOI18N
        getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(Customizer.class,"ACS_Customizer")); // NOI18N
        CustomizerDataSupport custData = new CustomizerDataSupport(manager);
        // set help ID according to selected tab
        addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                String helpID = null;
                switch (getSelectedIndex()) {
                    case 0 : helpID = "tomcat_customizer_general";    // NOI18N
                             break;
                    case 1 : helpID = "tomcat_customizer_startup";    // NOI18N
                             break;
                    case 2 : helpID = "tomcat_customizer_platform";    // NOI18N
                             break;
                    case 3 : helpID = "tomcat_customizer_deployment";   // NOI18N
                             break;
                    case 4 : helpID = "tomcat_customizer_classes";    // NOI18N
                             break;
                    case 5 : helpID = "tomcat_customizer_sources";    // NOI18N
                             break;
                    case 6 : helpID = "tomcat_customizer_javadoc";    // NOI18N
                             break;
                }
                putClientProperty("HelpID", helpID); // NOI18N
            }
        });
        addTab(NbBundle.getMessage(Customizer.class,"TXT_General"),
                new CustomizerGeneral(custData));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Startup"),
                new CustomizerStartup(custData, manager.getTomcatProperties().getCatalinaHome()));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Platform"),
                new CustomizerJVM(custData));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Deployment"),
                new CustomizerDeployment(custData));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Tab_Classes"), 
               CustomizerSupport.createClassesCustomizer(custData.getClassModel()));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Tab_Sources"), 
                CustomizerSupport.createSourcesCustomizer(custData.getSourceModel(), null));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Tab_Javadoc"), 
                CustomizerSupport.createJavadocCustomizer(custData.getJavadocsModel(), null));
    }
}
