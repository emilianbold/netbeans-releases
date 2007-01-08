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

package org.netbeans.modules.mobility.jsr172.wizard;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.mobility.end2end.classdata.WSDLService;
import org.netbeans.modules.mobility.end2end.client.config.ClassDescriptor;
import org.netbeans.modules.mobility.end2end.client.config.ClientConfiguration;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.end2end.classdata.AbstractService;
import org.netbeans.spi.project.ui.templates.support.Templates;


/** Wizard for adding web service clients to an application
 */
public class WebServiceClientWizardIterator implements TemplateWizard.Iterator {
    
    public static final String WSDL_SOURCE = "wsdlSource"; // NOI18N
    public static final String WSDL_FILE_PATH = "wsdlFilePath"; // NOI18N
    public static final String WSDL_DOWNLOAD_URL = "wsdlDownloadUrl"; // NOI18N
    public static final String WSDL_DOWNLOAD_FILE = "wsdlDownloadedPath"; // NOI18N
    public static final String WSDL_PACKAGE_NAME = "wsdlPackageName"; // NOI18N
    public static final String JSR172_CLIENT_NAME = "clientName"; // NOI18N
    static String PROP_CREATE_MIDLET = "createMIDlet"; // NOI18N
    
    private int index = 0;
    private WizardDescriptor.Panel [] panels;
    
    // !PW FIXME How to handle freeform???
    private Project project;
    
    private static WebServiceClientWizardIterator instance;
    
    /** Entry point specified in layer
     */
    public static synchronized WebServiceClientWizardIterator singleton() {
        if( instance == null ) {
            instance = new WebServiceClientWizardIterator();
        }
        return instance;
    }
    
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new WebServiceClientWizardDescriptor()
        };
    }
    
    public void initialize(final TemplateWizard wiz) {
        project = Templates.getProject(wiz);
        
        index = 0;
        panels = createPanels();
        
        final Object prop = wiz.getProperty("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps(beforeSteps, panels);
        
        for (int i = 0; i < panels.length; i++) {
            final Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            
            assert c instanceof JComponent;
            final JComponent jc = (JComponent)c;
            // Step #.
            jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
            // Step name (actually the whole list for reference).
            jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
        }
    }
    
    public void uninitialize(TemplateWizard wiz) {
        wiz.putProperty(WSDL_DOWNLOAD_URL, null);
        wiz.putProperty(WSDL_DOWNLOAD_FILE, null);
        wiz.putProperty(WSDL_FILE_PATH,null);
        wiz.putProperty(WSDL_PACKAGE_NAME,null);
        
        wiz = null;
        panels = null;
    }
    
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        // Steps:
        // 1. invoke wizard to select which service to add a reference to.
        //    How to interpret node input set --
        //    + empty: wizard forces project selection, then service selection
        //    + client node: determine project and start on service page
        //    + wsdl node: would select project, but not service.  would also
        //      have to verify that WSDL is fully formed.
        
        final Configuration configuration = new Configuration();
        configuration.setServiceType(Configuration.JSR172_TYPE);
        
        project = Templates.getProject(wiz);
        final String packageName = (String) wiz.getProperty(WebServiceClientWizardIterator.WSDL_PACKAGE_NAME);
        final Sources sources = project.getLookup().lookup(Sources.class);
        final SourceGroup sg = sources.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA )[0]; //only one source root for mobile project
        FileObject targetFolder = sg.getRootFolder().getFileObject(packageName.replace('.','/'));
        if (targetFolder == null){
            targetFolder = FileUtil.createFolder(sg.getRootFolder(), packageName.replace('.','/'));
        }
        
        final String clientName = (String)wiz.getProperty(JSR172_CLIENT_NAME);
        
        final FileObject tempFO = Templates.getTemplate(wiz); // NOI18N
        final DataObject template = DataObject.find(tempFO);
        final E2EDataObject e2eDO = (E2EDataObject) template.createFromTemplate(
                (DataFolder)DataObject.find(targetFolder),
                clientName);//configuration.getClientConfiguration().getClassDescriptor().getLeafClassName());
        
        String filePath = null;
        final WSDLService service = new WSDLService();
        if(wiz.getProperty(WebServiceClientWizardIterator.WSDL_DOWNLOAD_URL) != null) {
            filePath = (String) wiz.getProperty(WebServiceClientWizardIterator.WSDL_FILE_PATH);
            FileObject fo = null;
            if ((fo = targetFolder.getFileObject(filePath)) != null){
                fo.delete();
            }
            fo = targetFolder.createData(filePath);
            FileLock fclk = null;
            try {
                fclk = fo.lock();
                final BufferedOutputStream bos = new BufferedOutputStream(fo.getOutputStream(fclk));
                bos.write((byte[])wiz.getProperty(WebServiceClientWizardIterator.WSDL_DOWNLOAD_FILE));
                bos.close();
            } catch (IOException e ){
                ErrorManager.getDefault().notify(e);
            } finally {
                if (fclk != null){
                    fclk.releaseLock();
                }
            }
            service.setUrl((String) wiz.getProperty(WebServiceClientWizardIterator.WSDL_DOWNLOAD_URL));
            service.setFile(filePath);
        } else {
            filePath = (String) wiz.getProperty(WebServiceClientWizardIterator.WSDL_FILE_PATH);
            final File file = new File(filePath);
            FileObject foFile = FileUtil.toFileObject(FileUtil.normalizeFile(file));
            final String name = foFile.getName();
            if( !foFile.getURL().toString().equals( targetFolder.getURL().toString() + foFile.getNameExt())) {
                foFile = FileUtil.copyFile(foFile, targetFolder, name);
            }
            service.setUrl((filePath.startsWith("/") ? "file:":"file:/") + filePath); //NOI18N
            service.setFile(foFile.getNameExt());
        }
        
        final ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setProjectName(ProjectUtils.getInformation(project).getName());
        final Properties prop = new Properties();
        prop.put("cldc11", "true");
        clientConfiguration.setProperties(prop);
        
        final ClassDescriptor classDescriptor = new ClassDescriptor(packageName + "." + clientName, sg.getName());
        clientConfiguration.setClassDescriptor(classDescriptor);
        configuration.setClientConfiguration(clientConfiguration);
        
        
        service.setName(clientName);
        
        final List<AbstractService> list = new ArrayList<AbstractService>();
        list.add(service);
        configuration.setServices(list);
        e2eDO.setConfiguration(configuration);
        
        final Boolean b = (Boolean)wiz.getProperty(PROP_CREATE_MIDLET);
        e2eDO.generate(b != null && b.booleanValue());
        Set<DataObject> result;
        result = new HashSet();
        result.add(e2eDO);
        
        return result;
    }
    
    public String name() {
        return NbBundle.getMessage(WebServiceClientWizardIterator.class, "LBL_WebServiceClient"); // NOI18N
    }
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    
    public void nextPanel() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }
        
        index++;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void previousPanel() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }
        
        index--;
    }
    
    public void addChangeListener(@SuppressWarnings("unused")
	final ChangeListener l) {
        // nothing to do yet
    }
    
    public void removeChangeListener(@SuppressWarnings("unused")
	final ChangeListener l) {
        // nothing to do yet
    }
    
    private static String[] createSteps(String[] before, final WizardDescriptor.Panel[] panels) {
        //assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }    
}
