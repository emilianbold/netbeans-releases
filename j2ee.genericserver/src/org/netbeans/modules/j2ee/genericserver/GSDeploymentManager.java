/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.genericserver;

import java.io.File;
import java.io.InputStream;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;


/**
 *
 * @author Martin Adamek
 */
public class GSDeploymentManager implements DeploymentManager {
    
    public ProgressObject distribute(Target[] target, File file, File file2) throws IllegalStateException {
        return null;
    }

    public DeploymentConfiguration createConfiguration(DeployableObject deployableObject) throws InvalidModuleException {
        return null;
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleID, InputStream inputStream, InputStream inputStream2) throws UnsupportedOperationException, IllegalStateException {
        return null;
    }

    public ProgressObject distribute(Target[] target, InputStream inputStream, InputStream inputStream2) throws IllegalStateException {
        return null;
    }

    public ProgressObject undeploy(TargetModuleID[] targetModuleID) throws IllegalStateException {
        return null;
    }

    public ProgressObject stop(TargetModuleID[] targetModuleID) throws IllegalStateException {
        return null;
    }

    public ProgressObject start(TargetModuleID[] targetModuleID) throws IllegalStateException {
        return null;
    }

    public void setLocale(java.util.Locale locale) throws UnsupportedOperationException {
    }

    public boolean isLocaleSupported(java.util.Locale locale) {
        return false;
    }

    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        return null;
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        return null;
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        return null;
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleID, File file, File file2) throws UnsupportedOperationException, IllegalStateException {
        return null;
    }

    public void setDConfigBeanVersion(DConfigBeanVersionType dConfigBeanVersionType) throws DConfigBeanVersionUnsupportedException {
    }

    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType dConfigBeanVersionType) {
        return false;
    }

    public void release() {
    }

    public boolean isRedeploySupported() {
        return false;
    }

    public java.util.Locale getCurrentLocale() {
        return null;
    }

    public DConfigBeanVersionType getDConfigBeanVersion() {
        return null;
    }

    public java.util.Locale getDefaultLocale() {
        return null;
    }

    public java.util.Locale[] getSupportedLocales() {
        return null;
    }

    public Target[] getTargets() throws IllegalStateException {
        return null;
    }
    
}
