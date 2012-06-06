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

package org.netbeans.modules.mobility.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Adam Sotona
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory.class, position=90)
public class UserConfigurationTemplatesProvider implements ProjectConfigurationFactory, ProjectConfigurationFactory.CategoryDescriptor {
        
    public static final String PRIVATE_PREFIX = "private."; //NOI18N
    public static final String CFG_TEMPLATE_SUFFIX = id("_template"); //NOI18N
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
        FileObject root = FileUtil.getConfigFile(CFG_TEMPLATES_PATH);
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
    
    private static String id(final String literal) {
        return literal;
    }
}
