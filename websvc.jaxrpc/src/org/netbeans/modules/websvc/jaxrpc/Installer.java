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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.jaxrpc;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;

/**
 * @author Peter Williams
 */
public class Installer extends ModuleInstall {

    public Installer() {
        super();
    }

    public  void restored() {
        ProjectManager.mutex().postWriteRequest(new Runnable(){
            public  void run() {
                try {
                    EditableProperties ep = PropertyUtils.getGlobalProperties();
                    boolean changed = false;
                    File wsclient_update = InstalledFileLocator.getDefault().locate("ant/extra/wsclientuptodate.jar", null, false);
                    if (wsclient_update == null) {
                        String msg = NbBundle.getMessage(Installer.class, "MSG_WSClientUpdateMissing");
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                    } else  {
                        String wsclient_update_old = ep.getProperty(WebServicesClientSupport.WSCLIENTUPTODATE_CLASSPATH);
                        if (wsclient_update_old == null || !wsclient_update_old.equals(wsclient_update.toString())) {
                            ep.setProperty(WebServicesClientSupport.WSCLIENTUPTODATE_CLASSPATH, wsclient_update.toString());
                            changed = true;
                        }
                    }
                    if (changed) {
                        PropertyUtils.putGlobalProperties(ep);
                    }
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
        });
    }
}
