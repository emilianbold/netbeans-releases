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
package org.netbeans.modules.j2ee.jboss4.ide.ui;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.impl.ui.wizard.AddServerInstanceWizard;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentFactory;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;


/**
 *
 * @author Ivan Sidorkin
 */
public class JBInstantiatingIterator implements WizardDescriptor.InstantiatingIterator, ChangeListener {
    
    
    /**
     * skipServerLocationStep allow to skip Select Location step in New Instance Wizard
     * if this step allready was passed
     */
    public final boolean skipServerLocationStep = false;
    
    private transient AddServerLocationPanel locationPanel = null;
    private transient AddServerPropertiesPanel propertiesPanel = null;
    
    private WizardDescriptor wizard;
    private transient int index = 0;
    private transient WizardDescriptor.Panel[] panels = null;
    
    
    // private InstallPanel panel;
    private transient Set listeners = new HashSet(1);
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void uninitialize(WizardDescriptor wizard) {
    }
    
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }
    
    public void previousPanel() {
        index--;
    }
    
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    
    public String name() {
        return "JBoss Server AddInstanceIterator";  // NOI18N
    }
    
    public static void showInformation(final String msg,  final String title){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                d.setTitle(title);
                DialogDisplayer.getDefault().notify(d);
            }
        });
        
    }
    
    public Set instantiate() throws IOException {
        Set result = new HashSet();
        
        String displayName =  (String)wizard.getProperty(org.netbeans.modules.j2ee.deployment.impl.ui.wizard.AddServerInstanceWizard.PROP_DISPLAY_NAME);
        
        String url = JBDeploymentFactory.URI_PREFIX + host + ":" + port;    // NOI18N
        if (server != null && !server.equals(""))                           // NOI18N
            url += "#" + server;                                            // NOI18N
        url += "&"+ installLocation;                                        // NOI18N
      
        try {
            InstanceProperties ip = InstanceProperties.createInstanceProperties(url, userName, password, displayName);
            ip.setProperty(JBPluginProperties.PROPERTY_SERVER, server);
            ip.setProperty(JBPluginProperties.PROPERTY_DEPLOY_DIR, deployDir);
            ip.setProperty(JBPluginProperties.PROPERTY_SERVER_DIR, serverPath);
            ip.setProperty(JBPluginProperties.PROPERTY_ROOT_DIR, installLocation);
            
            ip.setProperty(JBPluginProperties.PROPERTY_HOST, host);
            ip.setProperty(JBPluginProperties.PROPERTY_PORT, port);
            
            result.add(ip);
        } catch (InstanceCreationException e){
            showInformation(e.getLocalizedMessage(), NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "MSG_INSTANCE_REGISTRATION_FAILED")); //NOI18N
            ErrorManager.getDefault().log(ErrorManager.EXCEPTION, e.getMessage());
        }
        
        return result;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }
    
    protected String[] createSteps() {
        if(!skipServerLocationStep){
            return new String[] { NbBundle.getMessage(JBInstantiatingIterator.class, "STEP_ServerLocation"),  // NOI18N
                    NbBundle.getMessage(JBInstantiatingIterator.class, "STEP_Properties") };    // NOI18N
        }else{
            if (!JBPluginProperties.getInstance().isCurrentServerLocationValid()){
                return new String[] { NbBundle.getMessage(JBInstantiatingIterator.class, "STEP_ServerLocation"),  // NOI18N
                        NbBundle.getMessage(JBInstantiatingIterator.class, "STEP_Properties") };    // NOI18N
            } else {
                return new String[] { NbBundle.getMessage(JBInstantiatingIterator.class, "STEP_Properties") };    // NOI18N
            }
        }
    }
    
    protected final String[] getSteps() {
        if (steps == null) {
            steps = createSteps();
        }
        return steps;
    }
    
    protected final WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = createPanels();
        }
        return panels;
    }
    
    protected WizardDescriptor.Panel[] createPanels() {
        if (locationPanel == null) {
            locationPanel = new AddServerLocationPanel(this);
            
            locationPanel.addChangeListener(this);
        }
        if (propertiesPanel == null) {
            propertiesPanel = new AddServerPropertiesPanel(this);
            
            propertiesPanel.addChangeListener(this);
        }
        
        if (skipServerLocationStep){
            if (!JBPluginProperties.getInstance().isCurrentServerLocationValid()){
                return new WizardDescriptor.Panel[] {
                    (WizardDescriptor.Panel)locationPanel,
                            (WizardDescriptor.Panel)propertiesPanel
                };
            } else {
                return new WizardDescriptor.Panel[] {
                    (WizardDescriptor.Panel)propertiesPanel
                };
            }
        }else{
            return new WizardDescriptor.Panel[] {
                (WizardDescriptor.Panel)locationPanel,
                        (WizardDescriptor.Panel)propertiesPanel
            };
        }
    }
    
    private transient String[] steps = null;
    
    protected final int getIndex() {
        return index;
    }
    
    public WizardDescriptor.Panel current() {
        WizardDescriptor.Panel result = getPanels()[index];
        JComponent component = (JComponent)result.getComponent();
        component.putClientProperty("WizardPanel_contentData", getSteps());  // NOI18N
        component.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(getIndex()));// NOI18N
        return result;
    }
    
    public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
        fireChangeEvent();
    }
    
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }
    
    private String host;
    private String port;
    private String userName="";
    private String password="";
    private String server;
    private String installLocation;
    private String deployDir;
    private String serverPath;
    
    
    public void setHost(String host){
        this.host = host.trim();
    }
    
    public void setPort(String port){
        this.port = port.trim();
    }
    
    public void setServer(String server){
        this.server = server;
    }
    
    public void setServerPath(String serverPath){
        this.serverPath = serverPath;
    }
    
    public void setDeployDir(String deployDir){
        this.deployDir = deployDir;
    }
    
    public void setInstallLocation(String installLocation){
        this.installLocation = installLocation;
        propertiesPanel.installLocationChanged();
    }
    
}
