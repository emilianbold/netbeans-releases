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

package org.netbeans.modules.javawebstart.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.JComponent;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Milan Kubec
 */
public class JWSCompositeCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    private static final String CAT_WEBSTART = "WebStart";
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
            category.setOkButtonListener(new SavePropsListener(jwsProps, context.lookup(Project.class)));
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
            boolean enabled = Boolean.valueOf(jwsProps.getProperty(JWSProjectProperties.JNLP_ENABLED)).booleanValue();
            final ProjectConfigurationProvider configProvider = 
                    j2seProject.getLookup().lookup(ProjectConfigurationProvider.class);
            try {
                if (enabled) {
                    // XXX logging
                    // test if the file already exists, if so do not generate, just set as active
                    J2SEProjectConfigurations.createConfigurationFiles(j2seProject, "JWS_generated",
                            prepareSharedProps(), null /*or new Properties()*/); // NOI18N
                    setActiveConfig(configProvider, "Web Start"); // XXX from bundle
                    copyTemplate(j2seProject);
                    modifyBuildXml(j2seProject);
                } else {
                    setActiveConfig(configProvider, "<default>"); // XXX from bundle?
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
            // XXX test if it's already modified - then do not save!
            Element docElem = xmlDoc.getDocumentElement();
            NodeList nl = docElem.getElementsByTagName("import"); // NOI18N
            if (nl.getLength() == 1) {
                Element importElem = xmlDoc.createElement("import"); // NOI18N
                importElem.setAttribute("file", "nbproject/jnlp-impl.xml");  // NOI18N
                Node n = nl.item(0).getNextSibling();
                docElem.insertBefore(importElem, n);
                Element targetElem = xmlDoc.createElement("target"); // NOI18N
                targetElem.setAttribute("name", "-post-jar"); // NOI18N
                targetElem.setAttribute("depends", "jnlp"); // NOI18N
                n = importElem.getNextSibling();
                docElem.insertBefore(targetElem, n);
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
            props.setProperty("$label", "Web Start"); // XXX get from bundle
            props.setProperty("$target.run", "jws-run"); // NOI18N
            props.setProperty("$target.debug", "jws-debug"); // NOI18N
            return props;
        }
        
    }
    
}
