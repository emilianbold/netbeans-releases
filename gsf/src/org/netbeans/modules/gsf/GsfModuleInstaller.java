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
package org.netbeans.modules.gsf;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.netbeans.api.retouche.source.SourceTaskFactoryManager;
import org.netbeans.editor.Settings;
import org.netbeans.modules.retouche.source.ActivatedDocumentListener;
import org.netbeans.modules.retouche.source.usages.ClassIndexManager;
import org.netbeans.modules.retouche.source.usages.NBLockFactory;
import org.netbeans.modules.retouche.source.usages.RepositoryUpdater;
import org.netbeans.modules.retouche.source.util.LowMemoryNotifierMBean;
import org.netbeans.modules.retouche.source.util.LowMemoryNotifierMBeanImpl;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;


public class GsfModuleInstaller extends ModuleInstall {
    private static final boolean ENABLE_MBEANS =
        Boolean.getBoolean("org.netbeans.modules.gsf.enableMBeans"); //NOI18N

    @Override
    public void restored() {
        // add editor support for our registered editor types
        Settings.addInitializer(new GsfEditorSettings());
        Settings.reset();

        NBLockFactory.clearLocks();
        SourceTaskFactoryManager.register();

        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                public void run() {
                    RepositoryUpdater.getDefault();
                    ActivatedDocumentListener.register();
                }
            });

        if (ENABLE_MBEANS) {
            registerMBeans();
        }
    }
    
    public @Override boolean closing () {
        final boolean ret = super.closing();
        RepositoryUpdater.getDefault().close();
        try {
            ClassIndexManager.getDefault().writeLock(new ClassIndexManager.ExceptionAction<Void>() {
                 public Void run() throws IOException {
                     ClassIndexManager.getDefault().close();
                     return null;
                 }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        };            
        if (ENABLE_MBEANS) {
            unregisterMBeans();
        }
        return ret;
    }
    
    private static void registerMBeans() {
        try {
            MBeanServer mgs = ManagementFactory.getPlatformMBeanServer();
            mgs.registerMBean (new LowMemoryNotifierMBeanImpl(), new ObjectName (LowMemoryNotifierMBean.OBJECT_NAME));
            //mgs.registerMBean( LuceneIndexMBeanImpl.getDefault(), new ObjectName (LuceneIndexMBean.OBJECT_NAME));
        } catch (NotCompliantMBeanException e) {
            ErrorManager.getDefault ().notify (e);
        }
        catch (MalformedObjectNameException e) {
            ErrorManager.getDefault ().notify (e);
        }
        catch (InstanceAlreadyExistsException e) {
            ErrorManager.getDefault ().notify (e);
        }
        catch (MBeanRegistrationException e) {
            ErrorManager.getDefault ().notify (e);
        }
    }
    
    private static void unregisterMBeans() {
        try {
            MBeanServer mgs = ManagementFactory.getPlatformMBeanServer();
            mgs.unregisterMBean (new ObjectName (LowMemoryNotifierMBean.OBJECT_NAME));
            //mgs.unregisterMBean (new ObjectName (LuceneIndexMBean.OBJECT_NAME));
        } catch (MalformedObjectNameException e) {
            ErrorManager.getDefault ().notify (e);
        }
        catch (InstanceNotFoundException e) {
            ErrorManager.getDefault ().notify (e);
        }
        catch (MBeanRegistrationException e) {
            ErrorManager.getDefault ().notify (e);
        }
    }
}
