/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.core.client.wizard;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Collections;
import java.util.Set;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.ProgressMonitor;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.UnresolvedClass;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
        
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileAlreadyLockedException;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.client.ClientStubDescriptor;
import org.netbeans.modules.websvc.core.Utilities;

/** Wizard for adding web service clients to an application
 */
public class WebServiceClientWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private int index = 0;
    private WizardDescriptor.Panel [] panels;

    private WizardDescriptor wiz;
    // !PW FIXME How to handle freeform???
    private Project project;

    /** Entry point specified in layer
     */
    public static WebServiceClientWizardIterator create() {
        return new WebServiceClientWizardIterator();
    }

    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new WebServiceClientWizardDescriptor()
        };
    }

    public void initialize(WizardDescriptor wizard) {
        wiz = wizard;
        project = Templates.getProject(wiz);

        index = 0;
        panels = createPanels();

        Object prop = wiz.getProperty("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = Utilities.createSteps (beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }

            assert c instanceof JComponent;
            JComponent jc = (JComponent)c;
            // Step #.
            jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
            // Step name (actually the whole list for reference).
            jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
        }
    }

    public void uninitialize(WizardDescriptor wizard) {
        wiz.putProperty(WizardProperties.WSDL_DOWNLOAD_URL, null);
        wiz.putProperty(WizardProperties.WSDL_DOWNLOAD_FILE, null);
        wiz.putProperty(WizardProperties.WSDL_FILE_PATH,null);
        wiz.putProperty(WizardProperties.WSDL_PACKAGE_NAME,null);
        wiz.putProperty(WizardProperties.CLIENT_STUB_TYPE, null);

        wiz = null;
        panels = null;
    }

    /**
     * Returns Java source groups for all source packages in given project.<br>
     * Doesn't include test packages.
     *
     * @param project Project to search
     * @return Array of SourceGroup. It is empty if any probelm occurs.
     */
    static SourceGroup[] getJavaSourceGroups(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                                    JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set testGroups = getTestSourceGroups(project, sourceGroups);
        List result = new ArrayList();
        for (int i = 0; i < sourceGroups.length; i++) {
            if (!testGroups.contains(sourceGroups[i])) {
                result.add(sourceGroups[i]);
            }
        }
        return (SourceGroup[]) result.toArray(new SourceGroup[result.size()]);
    }
    
    private static Set/*<SourceGroup>*/ getTestSourceGroups(Project project, SourceGroup[] sourceGroups) {
        Map foldersToSourceGroupsMap = createFoldersToSourceGroupsMap(sourceGroups);
        Set testGroups = new HashSet();
        for (int i = 0; i < sourceGroups.length; i++) {
            testGroups.addAll(getTestTargets(sourceGroups[i], foldersToSourceGroupsMap));
        }
        return testGroups;
    }
    
    private static List/*<SourceGroup>*/ getTestTargets(SourceGroup sourceGroup, Map foldersToSourceGroupsMap) {
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        if (rootURLs.length == 0) {
            return new ArrayList();
        }
        List result = new ArrayList();
        List sourceRoots = getFileObjects(rootURLs);
        for (int i = 0; i < sourceRoots.size(); i++) {
            FileObject sourceRoot = (FileObject) sourceRoots.get(i);
            SourceGroup srcGroup = (SourceGroup) foldersToSourceGroupsMap.get(sourceRoot);
            if (srcGroup != null) {
                result.add(srcGroup);
            }
        }
        return result;
    }
    
    private static Map createFoldersToSourceGroupsMap(final SourceGroup[] sourceGroups) {
        Map result;
        if (sourceGroups.length == 0) {
            result = Collections.EMPTY_MAP;
        } else {
            result = new HashMap(2 * sourceGroups.length, .5f);
            for (int i = 0; i < sourceGroups.length; i++) {
                SourceGroup sourceGroup = sourceGroups[i];
                result.put(sourceGroup.getRootFolder(), sourceGroup);
            }
        }
        return result;
    }
    
    private static List/*<FileObject>*/ getFileObjects(URL[] urls) {
        List result = new ArrayList();
        for (int i = 0; i < urls.length; i++) {
            FileObject sourceRoot = URLMapper.findFileObject(urls[i]);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else {
                int severity = ErrorManager.INFORMATIONAL;
                if (ErrorManager.getDefault().isNotifiable(severity)) {
                    ErrorManager.getDefault().notify(severity, new IllegalStateException(
                       "No FileObject found for the following URL: " + urls[i])); //NOI18N
                }
            }
        }
        return result;
    }
    
    public Set/*FileObject*/ instantiate() throws IOException {

        Set result = Collections.EMPTY_SET;

        // Steps:
        // 1. invoke wizard to select which service to add a reference to.
        //    How to interpret node input set --
        //    + empty: wizard forces project selection, then service selection
        //    + client node: determine project and start on service page
        //    + wsdl node: would select project, but not service.  would also
        //      have to verify that WSDL is fully formed.

        WebServicesClientSupport clientSupport = null;
        project = Templates.getProject(wiz);

        // !PW Get client support from project (from first page of wizard)
        if(project != null) {
            clientSupport = WebServicesClientSupport.getWebServicesClientSupport(project.getProjectDirectory());
        }

        if(clientSupport == null) {
            // notify no client support
//			String mes = MessageFormat.format (
//				NbBundle.getMessage (WebServiceClientWizardIterator.class, "ERR_WebServiceClientSupportNotFound"),
//				new Object [] {"Servlet Listener"}); //NOI18N
            String mes = NbBundle.getMessage(WebServiceClientWizardIterator.class, "ERR_NoWebServiceClientSupport"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return result;
        }

        
        final byte [] sourceWsdlDownload = (byte []) wiz.getProperty(WizardProperties.WSDL_DOWNLOAD_FILE);
        String wsdlFilePath = (String) wiz.getProperty(WizardProperties.WSDL_FILE_PATH);
        String packageName = (String) wiz.getProperty(WizardProperties.WSDL_PACKAGE_NAME);
        ClientStubDescriptor stubDescriptor = (ClientStubDescriptor) wiz.getProperty(WizardProperties.CLIENT_STUB_TYPE);

        String sourceUrl;
        FileObject sourceWsdlFile = null;
        
        if(sourceWsdlDownload == null) {
            // Verify the existence of the source WSDL file and that we can get a file object for it.
            File normalizedWsdlFilePath = FileUtil.normalizeFile(new File(wsdlFilePath));
            sourceUrl = normalizedWsdlFilePath.toString();
            sourceWsdlFile = FileUtil.toFileObject(normalizedWsdlFilePath);
            if(sourceWsdlFile == null) {
                String mes = NbBundle.getMessage(WebServiceClientWizardIterator.class, "ERR_WsdlFileNotFound", normalizedWsdlFilePath); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
                return result;
            }
        } else {
            // 
            File wsdlFile = new File(System.getProperty("java.io.tmpdir"), wsdlFilePath);
            if(!wsdlFile.exists()) {
                try {
                    wsdlFile.createNewFile();
                } catch(IOException ex) {
                    String mes = NbBundle.getMessage(WebServiceClientWizardIterator.class, "ERR_UnableToCreateTempFile", wsdlFile.getPath()); // NOI18N
                    NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                    return result;
                }
            }
            
            sourceUrl = (String) wiz.getProperty(WizardProperties.WSDL_DOWNLOAD_URL);
            sourceWsdlFile = FileUtil.toFileObject(FileUtil.normalizeFile(wsdlFile));
        
            if(sourceWsdlFile != null) {
                FileLock wsdlLock = sourceWsdlFile.lock();

                try {
                    OutputStream out = sourceWsdlFile.getOutputStream(wsdlLock);
                    try {
                        out.write(sourceWsdlDownload);
                        out.flush();
                    } finally {
                        if(out != null) {
                            out.close();
                        }
                    }
                } finally {
                    wsdlLock.releaseLock();
                }
            } else {
                String mes = NbBundle.getMessage(WebServiceClientWizardIterator.class, "ERR_UnableToCreateTempFile", wsdlFile.getPath()); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
                return result;
            }
        }

        // 2. add jax-rpc library if wscompile isnt present
        SourceGroup[] sgs = getJavaSourceGroups(project);
        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);

        FileObject wscompileFO = classPath.findResource("com/sun/xml/rpc/tools/ant/Wscompile.class");  
        if (wscompileFO==null) {
            // add jax-rpc16 if webservice is not on classpath
            ProjectClassPathExtender pce = (ProjectClassPathExtender)project.getLookup().lookup(ProjectClassPathExtender.class);
            Library jaxrpclib = LibraryManager.getDefault().getLibrary("jaxrpc16"); //NOI18N
            if ((pce!=null) && (jaxrpclib != null)) {
                pce.addLibrary(jaxrpclib);
            }
        }
        
        // 3. add the service client to the project.
        // " " for note parameter ensures monitor panel has proper room for our notes.
        final ProgressMonitor monitor = new ProgressMonitor(WindowManager.getDefault().getMainWindow(), 
            NbBundle.getMessage(WebServiceClientWizardIterator.class, "MSG_WizCreateClient"), " ", 0, 100); // NOI18N
        monitor.setMillisToPopup(0);
        monitor.setMillisToDecideToPopup(0);
        monitor.setProgress(1);

        final ClientBuilder builder = new ClientBuilder(project, clientSupport, sourceWsdlFile, packageName, sourceUrl, stubDescriptor);
        final FileObject sourceWsdlFileTmp = sourceWsdlFile;
        
        org.openide.util.RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    builder.generate(monitor);

                    if(sourceWsdlDownload != null) {
                        // we used a temp file, delete it now.
                        try {
                            sourceWsdlFileTmp.delete();
                        } catch(FileAlreadyLockedException ex) {
                            String mes = NbBundle.getMessage(WebServiceClientWizardIterator.class, "ERR_TempFileLocked", sourceWsdlFileTmp.getNameExt()); // NOI18N
                            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
                        } catch(IOException ex) {
                            String mes = NbBundle.getMessage(WebServiceClientWizardIterator.class, "ERR_TempFileNotDeleted", sourceWsdlFileTmp.getNameExt()); // NOI18N
                            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
                        }
                    }
                    
                    builder.updateMonitor(monitor, NbBundle.getMessage(WebServiceClientWizardIterator.class, "MSG_WizDone"), 99); // NOI18N
                } finally {
                    builder.updateMonitor(monitor, null, 100);
                }
            }
        },500);
        
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

    public void addChangeListener(ChangeListener l) {
        // nothing to do yet
    }

    public void removeChangeListener(ChangeListener l) {
        // nothing to do yet
    }
}
