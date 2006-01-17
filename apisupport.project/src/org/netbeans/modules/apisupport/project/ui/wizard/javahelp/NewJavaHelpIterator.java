/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.javahelp;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.CreatedModifiedFilesFactory;
import org.netbeans.modules.apisupport.project.CreatedModifiedFilesFactory.ModifyManifest;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
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
                
                //layer registration
                files.add(createLayerEntryOperation(tokens));
                
                //copying templates
                for (int i = 0; i < TEMPLATES_SUFFIXES.length; i++) {
                    files.add(createFileOperation(TEMPLATES_SUFFIXES[i], tokens));
                }
                
                //put javahelp.base into nbproject/project.properties
                Map props = new HashMap();
                props.put("javahelp.base", getCodeNameBase().replace('.','/'));//NOIN18N
                //props.put("jhall.jar","${harness.dir}/lib/jhall.jar");//NOI18N
                files.add(files.propertiesModification("nbproject/project.properties",props));//NOIN18N
                
                //put OpenIDE-Module-Requires into manifest
                ModifyManifest attribs = new CreatedModifiedFilesFactory.ModifyManifest(getProject()) {
                    protected void performModification(final EditableManifest em,final String name,final String value,
                            final String section) throws IllegalArgumentException {
                        String originalValue = em.getAttribute(name, section);
                        if (originalValue != null) {
                            em.setAttribute(name, originalValue+","+value, section);
                        } else {
                            super.performModification(em, name, value, section);
                        }
                    }
                    
                };
                attribs.setAttribute("OpenIDE-Module-Requires", "org.netbeans.api.javahelp.Help", null);//NOIN18N
                files.add(attribs);
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
}

