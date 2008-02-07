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

package org.netbeans.modules.javawebstart.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.java.j2seproject.api.J2SEProjectConfigurations;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Milan Kubec
 */
public class JWSCompositeCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    private static final String CAT_WEBSTART = "WebStart"; // NOI18N
    private String catName = null;
    
    private static JWSProjectProperties jwsProps = null;
    
    public JWSCompositeCategoryProvider(String name) {
        catName = name;
    }
    
    public ProjectCustomizer.Category createCategory(Lookup context) {
        ResourceBundle bundle = NbBundle.getBundle(JWSCompositeCategoryProvider.class);
        ProjectCustomizer.Category category = null;
        if (CAT_WEBSTART.equals(catName)) {
            category = ProjectCustomizer.Category.create(CAT_WEBSTART,
                    bundle.getString("LBL_Category_WebStart"), null, null);
        }
        return category;
    }
    
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String name = category.getName();
        JComponent component = null;
        if (CAT_WEBSTART.equals(name)) {
            jwsProps = new JWSProjectProperties(context);
            category.setStoreListener(new SavePropsListener(jwsProps, context.lookup(Project.class)));
            component = new JWSCustomizerPanel(jwsProps);
        }
        return component;
    }
    
    // ----------
    
    public static JWSCompositeCategoryProvider createWebStart() {
        return new JWSCompositeCategoryProvider(CAT_WEBSTART);
    }
    
    // ----------
    
    private static class SavePropsListener implements ActionListener {
        
        private JWSProjectProperties jwsProps;
        private Project j2seProject;
        
        public SavePropsListener(JWSProjectProperties props, Project proj) {
            jwsProps = props;
            j2seProject = proj;
        }
        
        public void actionPerformed(ActionEvent e) {
            // log("Saving Properties " + jwsProps + " ...");
            try {
                jwsProps.store();
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
            final ProjectConfigurationProvider configProvider = 
                    j2seProject.getLookup().lookup(ProjectConfigurationProvider.class);
            try {
                if (jwsProps.isJWSEnabled()) {
                    // XXX logging
                    // test if the file already exists, if so do not generate, just set as active
                    J2SEProjectConfigurations.createConfigurationFiles(j2seProject, "JWS_generated",
                            prepareSharedProps(), null /*or new Properties()*/); // NOI18N
                    setActiveConfig(configProvider, NbBundle.getBundle(JWSCompositeCategoryProvider.class).getString("LBL_Category_WebStart"));
                    copyTemplate(j2seProject);
                    modifyBuildXml(j2seProject);
                } else {
                    setActiveConfig(configProvider, NbBundle.getBundle(JWSCompositeCategoryProvider.class).getString("LBL_Category_Default"));
                }
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
        
        private void setActiveConfig(final ProjectConfigurationProvider provider, String displayName) throws IOException {
            Collection<ProjectConfiguration> configs = provider.getConfigurations();
            for (final ProjectConfiguration c : configs) {
                if (displayName.equals(c.getDisplayName())) {
                    try {
                        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                            public Void run() throws Exception {
                                provider.setActiveConfiguration(c);
                                return null;
                            }
                        });
                    } catch (MutexException mex) {
                        throw (IOException) mex.getException();
                    }
                }
            }
        }
        
        private void copyTemplate(Project proj) throws IOException {
            FileObject projDir = proj.getProjectDirectory();
            FileObject jnlpBuildFile = projDir.getFileObject("nbproject/jnlp-impl.xml"); // NOI18N
            if (jnlpBuildFile == null) {
                FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
                FileObject templateFO = sfs.findResource("Templates/JWS/jnlp-impl.xml"); // NOI18N
                if (templateFO != null) {
                    FileUtil.copyFile(templateFO, projDir.getFileObject("nbproject"), "jnlp-impl"); // NOI18N
                }
            }
        }
        
        private void modifyBuildXml(Project proj) throws IOException {
            FileObject projDir = proj.getProjectDirectory();
            final FileObject buildXmlFO = projDir.getFileObject("build.xml"); // NOI18N
            File buildXmlFile = FileUtil.toFile(buildXmlFO);
            Document xmlDoc = null;
            try {
                xmlDoc = XMLUtil.parse(new InputSource(buildXmlFile.toURI().toString()), false, true, null, null);
            } catch (SAXException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            FileObject jnlpBuildFile = projDir.getFileObject("nbproject/jnlp-impl.xml"); // NOI18N
            AntBuildExtender extender = proj.getLookup().lookup(AntBuildExtender.class);
            if (extender != null) {
                assert jnlpBuildFile != null;
                if (extender.getExtension("jws") == null) { // NOI18N
                    AntBuildExtender.Extension ext = extender.addExtension("jws", jnlpBuildFile); // NOI18N
                    ext.addDependency("jar", "jnlp"); // NOI18N
                }
                ProjectManager.getDefault().saveProject(proj);
            } else {
                Logger.getLogger(JWSCompositeCategoryProvider.class.getName()).log(Level.INFO, 
                        "Trying to include JWS build snippet in project type that doesn't support AntBuildExtender API contract."); // NOI18N
            }
            
            //TODO this piece shall not proceed when the upgrade to j2se-project/4 was cancelled.
            //how to figure..
            Element docElem = xmlDoc.getDocumentElement();
            NodeList nl = docElem.getElementsByTagName("target"); // NOI18N
            Element target = null;
            for (int i = 0; i < nl.getLength(); i++) {
                Element e = (Element) nl.item(i);
                if (e.getAttribute("name") != null && "-post-jar".equals(e.getAttribute("name"))) { // NOI18N
                    target = e;
                    break;
                }
            }
            boolean changed = false;
            if (target != null) {
                if ((target.getAttribute("depends") != null && target.getAttribute("depends").contains("jnlp"))) { // NOI18N
                    String old = target.getAttribute("depends"); // NOI18N
                    old = old.replaceAll("jnlp", ""); // NOI18N
                    old = old.replaceAll(",[\\s]*$", ""); // NOI18N
                    old = old.replaceAll("^[\\s]*,", ""); // NOI18N
                    old = old.replaceAll(",[\\s]*,", ","); // NOI18N
                    old = old.trim();
                    if (old.length() == 0) {
                        target.removeAttribute("depends"); // NOI18N
                    } else {
                        target.setAttribute("depends", old); // NOI18N
                    }
                    changed = true;
                }
            }
            nl = docElem.getElementsByTagName("import"); // NOI18N
            for (int i = 0; i < nl.getLength(); i++) {
                Element e = (Element) nl.item(i);
                if (e.getAttribute("file") != null && "nbproject/jnlp-impl.xml".equals(e.getAttribute("file"))) { // NOI18N
                    e.getParentNode().removeChild(e);
                    changed = true;
                    break;
                }
            }
            
            if (changed) {
                final Document fdoc = xmlDoc;
                try {
                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                        public Void run() throws Exception {
                            FileLock lock = buildXmlFO.lock();
                            try {
                                OutputStream os = buildXmlFO.getOutputStream(lock);
                                try {
                                    XMLUtil.write(fdoc, os, "UTF-8"); // NOI18N
                                } finally {
                                    os.close();
                                }
                            } finally {
                                lock.releaseLock();
                            }
                            return null;
                        }
                    });
                } catch (MutexException mex) {
                    throw (IOException) mex.getException();
                }
            }
        }
        
        private Properties prepareSharedProps() {
            Properties props = new Properties();
            props.setProperty("$label", NbBundle.getBundle(JWSCompositeCategoryProvider.class).getString("LBL_Category_WebStart"));
            props.setProperty("$target.run", "jws-run"); // NOI18N
            props.setProperty("$target.debug", "jws-debug"); // NOI18N
            return props;
        }
        
    }
    
}
