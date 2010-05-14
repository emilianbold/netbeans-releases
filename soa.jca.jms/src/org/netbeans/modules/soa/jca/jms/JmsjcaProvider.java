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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.jca.jms;

import org.netbeans.modules.soa.jca.jms.ui.JMSActivationPanel;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.netbeans.modules.soa.jca.base.spi.GlobalRarProvider;
import org.netbeans.modules.soa.jca.base.spi.InboundConfigCustomPanel;
import java.util.Collections;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.HelpCtx;

/**
 * JMSJCA implmentation of GlobalRarProvider
 *
 * @author echou
 */
public class JmsjcaProvider extends GlobalRarProvider {

    public static final String NAME = "sun-jms-adapter"; // NOI18N
    public static final String DISPLAY_NAME = java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/jms/Bundle").getString("JMSJCA_Global_Rar");

    private static final String TEMPLATE_FILE_NAME = "/org/netbeans/modules/soa/jca/jms/JmsjcaTemplate.xml" ; // NOI18N
    private static final String INBOUND_CFG_FILE_NAME = "/org/netbeans/modules/soa/jca/jms/JmsjcaInboundConfig.xml" ; // NOI18N
    private static final String INBOUND_MDB_TEMPLATE_FILE_NAME = "Templates/J2EE/GlobalRars/JmsjcaMDB.java" ; // NOI18N
    private static final String ICON_FILE_NAME = "/org/netbeans/modules/soa/jca/jms/session_16.png"; // NOI18N

    private List<String> inboundStaticOtdTypes = new ArrayList<String>();
    private List<String> otdTypes = new ArrayList<String>();
    private List<String> listenerInterfaces = new ArrayList<String>();
    private Properties addtionalProps = new Properties();

    public JmsjcaProvider(){
        this.inboundStaticOtdTypes.add("javax.jms.Message"); // NOI18N
        this.otdTypes.add("javax.jms.Session"); // NOI18N
        this.listenerInterfaces.add("javax.jms.MessageListener"); // NOI18N
    }

    public String getName() {
        return NAME;
    }

    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    public String getShortName() {
        return "jms"; // NOI18N
    }

    public List<String> getLibraryNames() {
        return Collections.EMPTY_LIST;
    }

    public InputStream getTemplate() {
        return JmsjcaProvider.class.getResourceAsStream(TEMPLATE_FILE_NAME);
    }

    public boolean supportsInboundTx() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.soa.jca.jms.about"); // NOI18N
    }

    public InboundConfigCustomPanel getInboundConfigCustomPanel(Project project, String contextName) {
        return new JMSActivationPanel(project, contextName);
    }

    public InputStream getInboundConfig() {
        return JmsjcaProvider.class.getResourceAsStream(INBOUND_CFG_FILE_NAME);
    }

    public FileObject getInboundMDBTemplate() {
        FileSystem defaultFS = Repository.getDefault().getDefaultFileSystem();
        FileObject templateFO = defaultFS.findResource(INBOUND_MDB_TEMPLATE_FILE_NAME);
        return templateFO;
    }

    public List<String> getInboundStaticOTDTypes() {
        return Collections.unmodifiableList(this.inboundStaticOtdTypes);
    }

    public List<String> getOTDTypes() {
        return Collections.unmodifiableList(this.otdTypes);
    }

    public List<String> getSupportedDynamicOTDTypes() {
        return Collections.EMPTY_LIST;
    }

    public List<String> getListenerInterfaces() {
        return Collections.unmodifiableList(this.listenerInterfaces);
    }

    public Properties getAdditionalConfig() {
        return this.addtionalProps;
    }

    public ImageIcon getIcon() {
        return new ImageIcon(JmsjcaProvider.class.getResource(ICON_FILE_NAME));
    }
}
