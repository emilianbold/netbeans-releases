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

package org.netbeans.modules.autoupdate.ui;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationSupport;

/**
 *
 * @author Radek Matous
 */
public class Containers {
    private static Reference<OperationContainer<InstallSupport>> INSTALL;
    private static Reference<OperationContainer<InstallSupport>> UPDATE;
    private static Reference<OperationContainer<InstallSupport>> INSTALL_FOR_NBMS;   
    private static Reference<OperationContainer<InstallSupport>> UPDATE_FOR_NBMS;
    private static Reference<OperationContainer<OperationSupport>> UNINSTALL;
    private static Reference<OperationContainer<OperationSupport>> ENABLE;
    private static Reference<OperationContainer<OperationSupport>> DISABLE;
    private static Reference<OperationContainer<OperationSupport>> CUSTOM_INSTALL;
    private static Reference<OperationContainer<OperationSupport>> CUSTOM_UNINSTALL;
    
    private Containers(){}
    public static void initNotify() {
        forAvailableNbms().removeAll();
        forUpdateNbms().removeAll();
        forAvailable().removeAll();
        forUninstall().removeAll();
        forUpdate().removeAll();
        forEnable().removeAll();
        forDisable().removeAll();
    }

    public static OperationContainer<InstallSupport> forAvailableNbms() {
        synchronized(Containers.class) {
            if (INSTALL_FOR_NBMS == null || INSTALL_FOR_NBMS.get() == null) {
                INSTALL_FOR_NBMS = new WeakReference<OperationContainer<InstallSupport>>(OperationContainer.createForInstall());
            }
            return INSTALL_FOR_NBMS.get();
        }        
    }
    public static OperationContainer<InstallSupport> forUpdateNbms() {
        synchronized(Containers.class) {
            if (UPDATE_FOR_NBMS == null || UPDATE_FOR_NBMS.get() == null) {
                UPDATE_FOR_NBMS = new WeakReference<OperationContainer<InstallSupport>>(OperationContainer.createForUpdate());
            }
            return UPDATE_FOR_NBMS.get();
        }        
    }
    
    public static OperationContainer<InstallSupport> forAvailable() {
        synchronized(Containers.class) {
            if (INSTALL == null || INSTALL.get() == null) {
                INSTALL = new WeakReference<OperationContainer<InstallSupport>>(OperationContainer.createForInstall());
            }
            return INSTALL.get();
        }        
    }
    public static OperationContainer<InstallSupport> forUpdate() {
        synchronized(Containers.class) {
            if (UPDATE == null || UPDATE.get() == null) {
                UPDATE = new WeakReference<OperationContainer<InstallSupport>>(OperationContainer.createForUpdate());
            }
            return UPDATE.get();
        }        
    }
    public static OperationContainer<OperationSupport> forUninstall() {
        synchronized(Containers.class) {
            if (UNINSTALL == null || UNINSTALL.get() == null) {
                UNINSTALL = new WeakReference<OperationContainer<OperationSupport>>(OperationContainer.createForUninstall());
            }
            return UNINSTALL.get();
        }        
    }
    public static OperationContainer<OperationSupport> forEnable() {
        synchronized(Containers.class) {
            if (ENABLE == null || ENABLE.get() == null) {
                ENABLE = new WeakReference<OperationContainer<OperationSupport>>(OperationContainer.createForEnable());
            }
            return ENABLE.get();
        }        
    }
    public static OperationContainer<OperationSupport> forDisable() {
        synchronized(Containers.class) {
            if (DISABLE == null || DISABLE.get() == null) {
                DISABLE = new WeakReference<OperationContainer<OperationSupport>>(OperationContainer.createForDisable());
            }
            return DISABLE.get();
        }        
    }
    public static OperationContainer<OperationSupport> forCustomInstall () {
        synchronized (Containers.class) {
            if (CUSTOM_INSTALL == null || CUSTOM_INSTALL.get () == null) {
                CUSTOM_INSTALL = new WeakReference<OperationContainer<OperationSupport>> (OperationContainer.createForCustomInstallComponent ());
            }
            return CUSTOM_INSTALL.get();
        }        
    }
    public static OperationContainer<OperationSupport> forCustomUninstall () {
        synchronized (Containers.class) {
            if (CUSTOM_UNINSTALL == null || CUSTOM_UNINSTALL.get () == null) {
                CUSTOM_UNINSTALL = new WeakReference<OperationContainer<OperationSupport>> (OperationContainer.createForCustomUninstallComponent ());
            }
            return CUSTOM_UNINSTALL.get();
        }        
    }
}
