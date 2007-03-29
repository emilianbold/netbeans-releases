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

package org.netbeans.modules.mobility.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;

/**
 *
 * @author Adam Sotona
 */
public class UserConfigurationTemplatesProvider implements ProjectConfigurationFactory, ProjectConfigurationFactory.CategoryDescriptor {
        
    public static final String PRIVATE_PREFIX = "private."; //NOI18N
    public static final String CFG_TEMPLATE_SUFFIX = NbBundle.getMessage(UserConfigurationTemplatesProvider.class, "LBL_Cfg_TemplateSuffix"); //NOI18N
    public static final String CFG_EXT = "cfg"; //NOI18N
    public static final String CFG_TEMPLATES_PATH = "Templates/J2MEProjectConfigurations"; //NOI18N
    

    /** Creates a new instance of UserConfigurationTemplatesProvider */
    public UserConfigurationTemplatesProvider() {
    }
    
    public CategoryDescriptor getRootCategory() {
        return this;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UserConfigurationTemplatesProvider.class, "UserConfigurationTemplatesProvider");//NOI18N
    }

    public List<Descriptor> getChildren() {
        ArrayList<Descriptor> a = new ArrayList();
        FileObject root = Repository.getDefault().getDefaultFileSystem().findResource(CFG_TEMPLATES_PATH);
        if (root != null) {
            for (final FileObject fo : root.getChildren()) {
                if (CFG_EXT.equals(fo.getExt())) {
                    a.add(new ConfigurationTemplateDescriptor() {
                        Map<String, String> pcp, pgp, pp;
                        String name = fo.getName();
                        {if (name.toLowerCase().endsWith(CFG_TEMPLATE_SUFFIX.toLowerCase())) name = name.substring(0, name.length() - CFG_TEMPLATE_SUFFIX.length());}
                        public String getCfgName() {
                            return name;
                        }
                        public String getDisplayName() {
                            return name;
                        }
                        public Map<String, String> getProjectConfigurationProperties() {
                            synchronized(this) {
                                if (pcp == null) loadProperties();
                            }
                            return pcp;
                        }
                        public Map<String, String> getProjectGlobalProperties() {
                            synchronized(this) {
                                if (pgp == null) loadProperties();
                            }
                            return pgp;
                        }
                        public Map<String, String> getPrivateProperties() {
                            synchronized(this) {
                                if (pp == null) loadProperties();
                            }
                            return pp;
                        }
                        private void loadProperties() {
                            Properties props = new Properties();
                            InputStream in = null;
                            pcp = new HashMap();
                            pgp = new HashMap();
                            pp = new HashMap();
                            try {
                                in = fo.getInputStream();
                                props.load(in);
                            } catch (IOException ioe) {
                                ErrorManager.getDefault().notify(ioe);
                                return;
                            } finally {
                                if (in != null) try {in.close();} catch (IOException ioe) {}
                            }
                            int privPrefixL = PRIVATE_PREFIX.length() ;
                            String tmpPrefix = J2MEProjectProperties.CONFIG_PREFIX + fo.getName() + '.';
                            int tmpPrefixL = tmpPrefix.length();
                            for ( final Map.Entry en : props.entrySet() ) {
                                String key = (String)en.getKey();
                                if (key.startsWith(PRIVATE_PREFIX)) {
                                    key = key.substring(privPrefixL);
                                    pp.put(key, (String)en.getValue());
                                } else if (key.startsWith(tmpPrefix)) {
                                    key = key.substring(tmpPrefixL);
                                    pcp.put(key, (String)en.getValue());
                                } else {
                                    pgp.put(key, (String)en.getValue());
                                }
                            }
                        }
                    });
                }
            }
        }
        return a;
    }
}
