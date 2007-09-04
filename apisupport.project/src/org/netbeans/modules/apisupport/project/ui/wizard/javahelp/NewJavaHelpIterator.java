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

package org.netbeans.modules.apisupport.project.ui.wizard.javahelp;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.CreatedModifiedFilesFactory;
import org.netbeans.modules.apisupport.project.CreatedModifiedFilesFactory.ModifyManifest;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;

/**
 * Wizard for creating JavaHelp
 *
 * @author Radek Matous, Jesse Glick
 */
public class NewJavaHelpIterator extends BasicWizardIterator {
    
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
        
        private static final String TEMPLATE_SUFFIX_HS = "-hs.xml"; // NOI18N
        private static final String[] TEMPLATE_SUFFIXES = {
            TEMPLATE_SUFFIX_HS,
            "-idx.xml", // NOI18N
            "-map.xml", // NOI18N
            "-toc.xml", // NOI18N
            "-about.html", // NOI18N
        };
        private static final String[] TEMPLATE_RESOURCES = {
            // Historical names in CVS, do not match actual extensions when created:
            "template_myplugin.hs", // NOI18N
            "template_myplugin-idx.xml", // NOI18N
            "template_myplugin-map.jhm", // NOI18N
            "template_myplugin-toc.xml", // NOI18N
            "template_myplugin-about.html", // NOI18N
        };
        
        private CreatedModifiedFiles files;
        
        DataModel(WizardDescriptor wiz) {
            super(wiz);
        }
        
        public CreatedModifiedFiles getCreatedModifiedFiles() {
            if (files == null) {
                // org.netbeans.modules.foo
                String codeNameBase = getModuleInfo().getCodeNameBase();
                // foo
                String basename = codeNameBase.substring(codeNameBase.lastIndexOf('.') + 1);
                // org/netbeans/modules/foo/docs/
                String path = codeNameBase.replace('.','/') + "/docs/"; // NOI18N
                
                files = new CreatedModifiedFiles(getProject());
                Map<String,String> tokens = new HashMap<String,String>();
                tokens.put("@@CODE_NAME@@", basename); // NOI18N
                tokens.put("@@FULL_CODE_NAME@@", codeNameBase); // NOI18N
                tokens.put("@@DISPLAY_NAME@@", ProjectUtils.getInformation(getProject()).getDisplayName()); // NOI18N
                tokens.put("@@HELPSET_PATH@@", path + basename + TEMPLATE_SUFFIX_HS); // NOI18N
                
                //layer registration
                files.add(files.createLayerEntry("Services/JavaHelp/" + basename + "-helpset.xml", // NOI18N
                        NewJavaHelpIterator.class.getResource("template_myplugin-helpset.xml"), // NOI18N
                        tokens,
                        null,
                        // Pick an arbitrary place to put it. Can always be moved elsewhere if anyone cares:
                        Collections.<String,Object>singletonMap("position", Integer.toString(3000 + new Random().nextInt(1000))))); // NOI18N
                
                //copying templates
                for (int i = 0; i < TEMPLATE_SUFFIXES.length; i++) {
                    URL template = NewJavaHelpIterator.class.getResource(TEMPLATE_RESOURCES[i]);
                    String filePath = "javahelp/" + path + basename + TEMPLATE_SUFFIXES[i]; // NOI18N
                    files.add(files.createFileWithSubstitutions(filePath, template, tokens));
                }
                
                // edit some properties
                Map<String,String> props = new HashMap<String,String>();
                // Default for javahelp.base (org/netbeans/modules/foo/docs) is correct.
                // For <checkhelpset> (currently nb.org modules only, but may be bundled in harness some day):
                props.put("javahelp.hs", basename + TEMPLATE_SUFFIX_HS); // NOI18N
                // XXX 71527: props.put("jhall.jar", "${harness.dir}/lib/jhall.jar"); // NOI18N
                files.add(files.propertiesModification("nbproject/project.properties", props)); // NOI18N
                
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
                attribs.setAttribute("OpenIDE-Module-Requires", "org.netbeans.api.javahelp.Help", null); // NOI18N
                files.add(attribs);
            }
            return files;
        }
        
    }
    
}
