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

/*
 * J2MEProjectModuleLoader.java
 *
 * Created on 7. prosinec 2004, 15:58
 */
package org.netbeans.modules.mobility.project;

import java.io.File;
import java.util.Iterator;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mobility.project.deployment.MobilityDeploymentProperties;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Adam Sotona
 */
public class J2MEProjectModuleInstall extends ModuleInstall implements LookupListener {
    
    public void restored() {
        final Lookup.Result result = Lookup.getDefault().lookup(new Lookup.Template<DeploymentPlugin>(DeploymentPlugin.class));
        result.addLookupListener(this);
        resultChanged(new LookupEvent(result));
    }
    
    public void resultChanged(final LookupEvent e) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    ProjectManager.mutex().writeAccess(
                        new Mutex.ExceptionAction<Object>() {
                            public Object run() throws Exception{
                                MobilityDeploymentProperties mp = new MobilityDeploymentProperties();
                                final EditableProperties props = PropertyUtils.getGlobalProperties();
                                final Iterator it = ((Lookup.Result)e.getSource()).allInstances().iterator();
                                while (it.hasNext()) {
                                    final DeploymentPlugin plugin = (DeploymentPlugin)it.next();
                                    final String name = plugin.getDeploymentMethodName();
                                    final String loc = plugin.getAntScriptLocation();
                                    if (loc != null) {
                                        File f = new File(loc);
                                        if (!f.isFile()) f = InstalledFileLocator.getDefault().locate(loc, null, false);
                                        if (name != null && f != null && Utilities.isJavaIdentifier(name)) {
                                            props.setProperty("deployment." + name + ".scriptfile", f.getAbsolutePath()); //NOI18N
                                        }
                                    }
                                    if (!mp.getInstanceList(name).contains("default")) mp.createInstance(name, "default"); //NOI18N
                                }
                                PropertyUtils.putGlobalProperties(props);
                                return null;
                            }
                        }
                    );
                } catch (MutexException me) {
                    ErrorManager.getDefault().notify(me.getException());
                }
            }
        }, 200);
    }
}
