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


package org.netbeans.modules.mobility.cldcplatform.cfgfactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.cldcplatform.PlatformConvertor;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author Adam Sotona
 */
public class CLDCProjectConfigurationFactory implements ProjectConfigurationFactory, ProjectConfigurationFactory.CategoryDescriptor {
    
    /** Creates a new instance of CLDCProjectConfigurationsProvider */
    public CLDCProjectConfigurationFactory() {
    }

    public CategoryDescriptor getRootCategory() {
        return this;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(CLDCProjectConfigurationFactory.class, "CLDCProjectConfigurationFactory");//NOI18N 
    }

    public List<Descriptor> getChildren() {
        ArrayList<Descriptor> ps = new ArrayList();
        for (final JavaPlatform p : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            if (p instanceof J2MEPlatform) ps.add(new CategoryDescriptor() {
                public String getDisplayName() {
                    return p.getDisplayName();
                }
                public List<Descriptor> getChildren() {
                    ArrayList<Descriptor> ds = new ArrayList();
                    for (final J2MEPlatform.Device d : ((J2MEPlatform)p).getDevices()) {
                        ds.add(new ConfigurationTemplateDescriptor() {
                            public String getDisplayName() {
                                return d.getName();
                            }
                            public String getCfgName() {
                                return d.getName();
                            }
                            public Map<String, String> getProjectConfigurationProperties() {
                                return PlatformConvertor.extractPlatformProperties("", (J2MEPlatform)p, d, null, null); //NOI18N
                            }
                            public Map<String, String> getProjectGlobalProperties() {
                                return null;
                            }
                            public Map<String, String> getPrivateProperties() {
                                return null;
                            }
                        });
                    }
                    return ds;
                }
            });
        }
        return ps;
    }
}
