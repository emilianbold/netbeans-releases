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
 * WizardIterator.java
 *
 * Created on August 3, 2005, 2:00 PM
 *
 */
package org.netbeans.modules.mobility.end2end.ui.wizard;

import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;


/**
 *
 * @author Michal Skvor
 */
public class GenericServiceIterator implements TemplateWizard.Iterator, ChangeListener {
    
    public static final String PROP_CONFIGURATION = "e2e-configuration"; // NOI18N
    public static final String PROP_SERVER_PROJECT = "server-project"; // NOI18N
    static String PROP_CLIENT_ROOT = "client-root"; // NOI18N
    static String PROP_DATABINDING = "DataBinding"; // NOI18N
    
    protected static GenericServiceIterator instance;
    
    private static final int STEPS_COUNT = 3;
    private int currentStep;
    private boolean isWebService = false;
    
    private WebApplicationPanel.WebApplicationPanelWizard webapplicationPanel;
    private ServiceSelectionPanel serviceSelectionPanel;
    private ClientOptionsPanel clientOptionsPanel;
    
    static String firstStep =
            NbBundle.getMessage( GenericServiceIterator.class, "TITLE_firstStep" ); // NOI18N
    static String clientTypeStep =
            NbBundle.getMessage( GenericServiceIterator.class, "TITLE_clientTypeStep" ); // NOI18N
    static String serviceSelectionStep =
            NbBundle.getMessage( GenericServiceIterator.class, "TITLE_serviceSelectionStep" ); // NOI18N
    static String clientOptionsStep =
            NbBundle.getMessage( GenericServiceIterator.class, "TITLE_clientOptionsStep" ); // NOI18N
    static String operationSelectionStep =
            NbBundle.getMessage( GenericServiceIterator.class, "TITLE_operationSelectionStep" ); // NOI18N
    
    final private String[] stepsMethodCall = new String[]{
        firstStep,
        clientTypeStep,
        serviceSelectionStep,
        clientOptionsStep
    };
    
    final private String[] stepsWebService = new String[]{
        firstStep,
        clientTypeStep,
        operationSelectionStep,
        clientOptionsStep
    };
    
    private static final int INDEX_SERVICES = 2;
    
    private String[] getSteps(){
        return !isWebService ? stepsMethodCall : stepsWebService;
    }
    
    static Object create() {
        return new GenericServiceIterator();
    }
        
    public void initialize( final TemplateWizard templateWizard ) {
        final Configuration configuration = new Configuration();
        templateWizard.putProperty( PROP_CONFIGURATION, configuration );
        
        webapplicationPanel = WebApplicationPanel.create();
        webapplicationPanel.addChangeListener(this);
        serviceSelectionPanel = new ServiceSelectionPanel();
        serviceSelectionPanel.getComponent().setName(getSteps()[INDEX_SERVICES]);
        clientOptionsPanel = new ClientOptionsPanel();
        
        currentStep = 0;
    }
    
    public void uninitialize( final TemplateWizard templateWizard ) {
        webapplicationPanel.removeChangeListener(this);
        webapplicationPanel = null;
        serviceSelectionPanel = null;
        clientOptionsPanel = null;
        currentStep = -1;
    }
    
    public Set<DataObject> instantiate( final TemplateWizard templateWizard ) throws IOException {
        //System.err.println(" instantiate ");
        
        final Configuration configuration = (Configuration)templateWizard.getProperty( PROP_CONFIGURATION );
        
        String packageName = configuration.getClientConfiguration().getClassDescriptor().getPackageName();
        packageName = packageName.replace('.','/');
        //only one SG in Mobile project
        FileObject targetFolder = (FileObject)templateWizard.getProperty(PROP_CLIENT_ROOT);
        if (packageName.length() != 0){
            targetFolder = FileUtil.createFolder(targetFolder, packageName);
        }
        
        final FileObject tempFO = FileUtil.getConfigFile("Templates/MIDP/E2EWebApplication.wsclient"); // NOI18N
        final DataObject template = DataObject.find(tempFO);
        final E2EDataObject e2eDO = (E2EDataObject) template.createFromTemplate(
                (DataFolder)DataObject.find(targetFolder),
                configuration.getClientConfiguration().getClassDescriptor().getLeafClassName());
        
        final FileObject setting = e2eDO.getPrimaryFile();
        final Project serverProject = (Project) templateWizard.getProperty(GenericServiceIterator.PROP_SERVER_PROJECT);
        final FileObject server = serverProject.getProjectDirectory();
        String path;
        if (CollocationQuery.areCollocated(
                FileUtil.normalizeFile(FileUtil.toFile(setting)),
                FileUtil.normalizeFile(FileUtil.toFile(server)))){
            
            path = FileUtil.getRelativePath(setting, server);
        } else {
            path = FileUtil.normalizeFile(FileUtil.toFile(server)).getAbsolutePath();
        }
        
        configuration.getServerConfigutation().setProjectPath(path);
        e2eDO.setConfiguration(configuration);
        
        e2eDO.generate();
        
        final Set<DataObject> set = new HashSet<DataObject>();
        set.add(e2eDO);
        return set;
    }
    
    public void previousPanel() {
        if( !hasPrevious())
            throw new NoSuchElementException();
        currentStep--;
    }
    
    public void nextPanel() {
        if( !hasNext())
            throw new NoSuchElementException();
        currentStep++;
    }
    
    public String name() {
        return current().getComponent().getName();
    }
    
    public boolean hasPrevious() {
        return currentStep > 0;
    }
    
    public boolean hasNext() {
        return currentStep <  STEPS_COUNT - 1;
    }
    
    public Panel current() {
        Panel component = null;
        
        switch( currentStep ) {
            case 0: component = webapplicationPanel;
            break;
            case 1: component = serviceSelectionPanel;
            break;
            case 2: component = clientOptionsPanel;
            break;
        }
        
        JComponent jc = null;
        if ((jc = (JPanel)component.getComponent()) == null)
            throw new IllegalStateException();
        
        
        jc.putClientProperty( WizardDescriptor.PROP_CONTENT_DATA, getSteps() );
        jc.putClientProperty( WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer( currentStep ));
        return component;
    }
    
    final private transient Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // Set<ChangeListener>
    public final void addChangeListener(final ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(final ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Set<ChangeListener> clSet;
        synchronized (listeners) {
            clSet = new HashSet<ChangeListener>(listeners);
        }
        final ChangeEvent ev = new ChangeEvent(this);
        for ( ChangeListener cl : clSet ) {
            cl.stateChanged(ev);
        }
    }
    
    public void stateChanged(final javax.swing.event.ChangeEvent changeEvent) {
        if (changeEvent.getSource().getClass() == WebApplicationPanel.WebApplicationPanelWizard.class && webapplicationPanel != null) {
            isWebService = ((WebApplicationPanel)webapplicationPanel.getComponent()).isWsdl();
            if (serviceSelectionPanel != null){
                serviceSelectionPanel.getComponent().setName(getSteps()[INDEX_SERVICES]);
            }
        }
        
        fireChangeEvent();
        
    }
    
    /**
     * Returns FileWizardIterator singleton instance. This method is used for constructing the instance from filesystem.attributes.
     */
    public static synchronized GenericServiceIterator singleton() {
        if( instance == null ) {
            instance = new GenericServiceIterator();
        }
        return instance;
    }
}
