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
package org.netbeans.modules.iep.samples;

import java.awt.Component;
import java.io.File;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

public abstract class SampleWizardIterator implements WizardDescriptor.InstantiatingIterator {
    private static final long serialVersionUID = 1L;
    
    public static final String PARENT_DIR = "parentDir"; // NOI18N
    public static final String NAME = "name"; // NOI18N
    
    public static final String PROJECT_TYPE = "type";
    public static final String PROJECT_TYPE_JBI = "http://www.netbeans.org/ns/j2ee-jbi/1";  // NOI18N
    public static final String PROJECT_TYPE_OTHER = "doc";
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;

    abstract protected WizardDescriptor.Panel[] createPanels();

    abstract protected String[] createSteps();

    public abstract String getBluePrintName();
    
    private static String getAppServer() { 
        Deployment deployment = Deployment.getDefault();
        String[] serverInstanceIDs = deployment.getServerInstanceIDs();
        ArrayList arr = new ArrayList();
        for (int i = 0; i < serverInstanceIDs.length; i++) {
            // This is slow if the server instance is remote!
            J2eePlatform platform = deployment.getJ2eePlatform(serverInstanceIDs[i]);
            if (platform == null) {
                continue;
            }
            if (isAppServer(platform)) {
                String serverId = serverInstanceIDs[0];
                int idx = serverId.indexOf("]");
                String serverRoot = serverId.substring(1, idx);  // [C:\\openesb\\glassfish]
                serverRoot = serverRoot.replace("\\", "/");
                return serverRoot;
            }
        }
        return null;
    }        
     
    private static boolean isAppServer(J2eePlatform platform) {
        File[] cps = platform.getClasspathEntries();
        for (int j = 0; j < cps.length; j++) {
            String path = cps[j].getPath();
            if ((path.indexOf("javaee.jar") > 0) || // NOI18N
                    (path.indexOf("j2ee.jar") > 0)) { // NOI18N
                return true;
            }
        }
        return false;
    }    

    protected FileObject createProject(FileObject parentDir, FileObject project, Properties prop) throws IOException {
        String name = (String) wiz.getProperty(NAME);
        String projectName = name + project.getAttribute("postfix");
        FileObject projectDir = parentDir.createFolder(projectName);

        String bluePrintSourcePath = "org-netbeans-modules-iep-samples-resources-zip/" + getBluePrintName() + "/" + project.getName();
        FileObject bluePrint = Repository.getDefault().getDefaultFileSystem().findResource(bluePrintSourcePath);      
        Util.unZipFile(bluePrint.getInputStream(), projectDir);
        String type = (String) project.getAttribute("type");  // NOI18N
        Util.setProjectName(projectDir, type, projectName, project.getName());
        
        Util.addProperties(projectDir, prop);
        return projectDir;
    }

    public Set<FileObject> instantiate() throws IOException {
        final Set<FileObject> resultSet = new LinkedHashSet<FileObject>();

        Repository.getDefault().getDefaultFileSystem().runAtomicAction(new org.openide.filesystems.FileSystem.AtomicAction() {

            public void run() throws IOException {
                File parentFile = FileUtil.normalizeFile((File) wiz.getProperty(PARENT_DIR));
                parentFile.mkdirs();
                FileObject parentDir = FileUtil.toFileObject(parentFile);
                String prefix = (String) wiz.getProperty(NAME);
                
                FileObject bluePrintRoot = 
                    Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-iep-samples-resources-zip/" + getBluePrintName()); // NOI18N
                
                FileObject[] project = bluePrintRoot.getChildren();
                List<FileObject> projList = new LinkedList<FileObject>();
                for (int i = 0; i < project.length; i++) {
                    FileObject p = project[i];
                    if (p.getName().equals("templateIEP")) {
                        projList.add(0, p);
                    } else {
                        projList.add(p);
                    }
                }
                FileObject jbi = null;
                List<FileObject> seList = new LinkedList<FileObject>();
        
                // get all the properties
                Properties prop = new Properties();
                String appServerRoot = getAppServer();
                if (appServerRoot != null) {
                    prop.setProperty("iep.j2ee.server", appServerRoot);
                }    
                
                for (int i = 0; i < projList.size(); i++) {
                    FileObject proj = projList.get(i);
                    FileObject p = createProject(parentDir, proj, prop);
                    resultSet.add(p);
                    String type = (String) proj.getAttribute("type");  // NOI18N
                    boolean serviceEngine = (Boolean)proj.getAttribute("serviceEngine");  // NOI18N
                    if (PROJECT_TYPE_JBI.equals(type)) {
                        jbi = p;
                    } else if (serviceEngine) {
                        seList.add(p);
                    }
                }
                if (jbi != null) {
                    for (int i = 0; i < seList.size(); i++) {
                        FileObject p = seList.get(i);
                        Util.addJbiModule(jbi, p);
                    }
                }
            }
        });
        return resultSet;
    }

    public void initialize(WizardDescriptor aWiz) {
        this.wiz = aWiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }

    public void uninitialize(WizardDescriptor aWiz) {
        this.wiz.putProperty(PARENT_DIR, null);
        this.wiz.putProperty(NAME, null);
        this.wiz = null;
        panels = null;
    }

    public String name() {
        return MessageFormat.format("{0} of {1}", new Object[]{new Integer(index + 1), new Integer(panels.length)}); // NOI18N
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }
}
