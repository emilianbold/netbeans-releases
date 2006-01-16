/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.javahelp;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.CreatedModifiedFilesFactory;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.WizardDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 * Wizard for creating JavaHelp
 *
 * @author Radek Matous
 */
public class NewJavaHelpIterator extends BasicWizardIterator {
    
    private static final long serialVersionUID = 1L;
    private NewJavaHelpIterator.DataModel data;
    
    public static NewJavaHelpIterator createIterator() {
        return new NewJavaHelpIterator();
    }
    
    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        return getCreatedFiles(cmf, data.getProject());
    }
    
    protected BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz) {
        data = new NewJavaHelpIterator.DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new JavaHelpPanel(wiz, data)
        };
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
    
    static final class DataModel extends BasicWizardIterator.BasicDataModel {
        private static String[] TEMPLATES_SUFFIXES = new String[] {
            "-about.html",//NOI18N
            ".hs",//NOI18N
            "-idx.xml",//NOI18N
            "-map.jhm",//NOI18N
            "-toc.xml"//NOI18N
        };
        
        private static String[] TOKENS = new String[] {
            "@@MyPlugin@@",//NOI18N
            "@@MyPlugin_DisplayName@@",//NOI18N
            "@@MyPlugin_PATH@@"//NOI18N
        };
        
        private static String TEMPLATE_NAME_PREFIX = "template_myplugin";//NOIN18N
        private static String HELPSETREF_SUFFIX = "-helpset.xml";//NOIN18N
        
        private CreatedModifiedFiles files;
        private String codeNameBase;
        
        DataModel(WizardDescriptor wiz) {
            super(wiz);
        }
        
        public CreatedModifiedFiles getCreatedModifiedFiles() {
            if (files == null) {
                files = new CreatedModifiedFiles(getProject());
                Map tokens = new HashMap();
                for (int i = 0; i < TOKENS.length; i++) {
                    tokens.put(TOKENS[i],replaceToken(TOKENS[i]));
                }
                files.add(createLayerEntryOperation(tokens));
                for (int i = 0; i < TEMPLATES_SUFFIXES.length; i++) {
                    files.add(createFileOperation(TEMPLATES_SUFFIXES[i], tokens));
                }
                Properties props = new Properties();
                props.setProperty("javahelp.base", getCodeNameBase().replace('.','/'));//NOIN18N
                //props.setProperty("jhall.jar","${harness.dir}/lib/jhall.jar");//NOI18N
                files.add(new AddProperties(getProject(), "nbproject/project.properties", props));//NOIN18N
                Properties attribs = new Properties();
                attribs.setProperty("OpenIDE-Module-Requires", "org.netbeans.api.javahelp.Help");//NOIN18N
                files.add(new AddAttributesIntoManifestSection(getProject(), null, attribs));                
            }
            return files;
        }
        
        private CreatedModifiedFiles.Operation createLayerEntryOperation(final Map tokens) {
            URL template = NewJavaHelpIterator.class.getResource(TEMPLATE_NAME_PREFIX+HELPSETREF_SUFFIX);
            return files.createLayerEntry("Services/JavaHelp/"+getFileNamePrefix()+HELPSETREF_SUFFIX,//NOIN18N
                    template,
                    tokens,
                    NbBundle.getMessage(NewJavaHelpIterator.class, "LBL_HelSet"),//NOI18N
                    null);
        }
        
        private CreatedModifiedFiles.Operation createFileOperation(final String templateSuffix, final Map tokens) {
            URL template = NewJavaHelpIterator.class.getResource(TEMPLATE_NAME_PREFIX+templateSuffix);
            String filePath = "javahelp/"+ getCodeNameBase().replace('.','/')+"/";//NOI18N
            filePath = filePath + getFileNamePrefix()+templateSuffix;
            return files.createFileWithSubstitutions(filePath, template, tokens);
        }
        
        private String getFilePathForTemplateFile(final String templateSuffix) {
            StringBuffer sb = new StringBuffer();
            sb.append(getCodeNameBase().replace('.','/'));
            sb.append("/").append(getFileNamePrefix()+templateSuffix);//NOI18N
            return sb.toString();
        }
        
        
        private String replaceToken(final String token) {
            String replacement = null;
            if (/*"@@MyPlugin@@"*/TOKENS[0].equals(token)) {
                assert "@@MyPlugin@@".equals(token);//NOI18N
                replacement = getFileNamePrefix();
            } else if (/*"@@MyPlugin_DisplayName@@"*/TOKENS[1].equals(token)) {
                assert "@@MyPlugin_DisplayName@@".equals(token);//NOI18N
                replacement = ProjectUtils.getInformation(getProject()).getDisplayName();
            } else if (/*"@@MyPlugin_PATH@@"*/TOKENS[2].equals(token)) {
                assert "@@MyPlugin_PATH@@".equals(token);//NOI18N
                replacement = getFilePathForTemplateFile(/*".hs"*/TEMPLATES_SUFFIXES[1]);
            }
            
            assert replacement != null;
            return replacement;
        }
        
        private String getFileNamePrefix() {
            String replacement;
            String codeNameBase = getCodeNameBase();
            replacement = codeNameBase.substring(codeNameBase.lastIndexOf(".")+1);
            return replacement;
        }
        
        private String getCodeNameBase() {
            if (codeNameBase == null) {
                ManifestManager mm = ManifestManager.getInstance(getProject().getManifest(), false);
                codeNameBase = mm.getCodeNameBase();
            }
            return codeNameBase;
        }
    }
    
    private static final class AddAttributesIntoManifestSection extends CreatedModifiedFilesFactory.OperationBase {
        private FileObject mfFO;
        private Properties attrs2Include;
        private String sectionName;
        
        public AddAttributesIntoManifestSection(final NbModuleProject project, final  String sectionName, final Properties attrs2Include) {
            super(project);
            this.attrs2Include = attrs2Include;
            this.sectionName = sectionName;
            this.mfFO = getProject().getManifestFile();
            addModifiedFileObject(mfFO);
        }
        
        public void run() throws IOException {
            //#65420 it can happen the manifest is currently being edited. save it
            // and cross fingers because it can be in inconsistent state
            try {
                DataObject dobj = DataObject.find(mfFO);
                SaveCookie safe = (SaveCookie)dobj.getCookie(SaveCookie.class);
                if (safe != null) {
                    safe.save();
                }
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            }
            
            EditableManifest em = Util.loadManifest(mfFO);
            if (sectionName != null) {
                em.addSection(sectionName);
            }
            Set keys = attrs2Include.keySet();
            for (Iterator it = keys.iterator(); it.hasNext();) {
                String key = (String) it.next();
                String value = attrs2Include.getProperty(key);
                em.setAttribute(key, value, sectionName);
            }            
            Util.storeManifest(mfFO, em);
        }
    }
    
    private static class AddProperties extends CreatedModifiedFilesFactory.OperationBase {
        private Properties props2Include;
        private String propertyPath;
        
        AddProperties(final NbModuleProject project, final String propertyPath, final Properties props2Include) {
            super(project);
            this.props2Include = props2Include;
            this.propertyPath= propertyPath;
            addCreatedOrModifiedPath(propertyPath,true);
        }
        
        public void run() throws IOException {
            FileObject propsFileFO = FileUtil.createData(getProject().getProjectDirectory(), propertyPath);
            
            EditableProperties ep = Util.loadProperties(propsFileFO);
            ep.putAll(props2Include);
            Util.storeProperties(propsFileFO,ep);
        }
    }
}

