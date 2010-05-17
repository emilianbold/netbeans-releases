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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.websphere6;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;
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
 * This class is a wrapper to make deployment manager thread safe. Underlying
 * Weblogic deployment manager is not thread safe (as this is not required by
 * specification). However it seems that J2EE Server sometimes invoke other
 * operation while previous is running (for example release() while running
 * getRunningModules()). This cause troubles like #85737.
 */
public final class SafeDeploymentManager implements DeploymentManager {

    private final DeploymentManager delegate;

    public SafeDeploymentManager(DeploymentManager delegate) {
        this.delegate = delegate;
    }

    public synchronized ProgressObject undeploy(TargetModuleID[] arg0) throws IllegalStateException {
        return delegate.undeploy(arg0);
    }

    public synchronized ProgressObject stop(TargetModuleID[] arg0) throws IllegalStateException {
        return delegate.stop(arg0);
    }

    public synchronized ProgressObject start(TargetModuleID[] arg0) throws IllegalStateException {
        return delegate.start(arg0);
    }

    public synchronized void setLocale(Locale arg0) throws UnsupportedOperationException {
        delegate.setLocale(arg0);
    }

    public synchronized void setDConfigBeanVersion(DConfigBeanVersionType arg0) throws DConfigBeanVersionUnsupportedException {
        delegate.setDConfigBeanVersion(arg0);
    }

    public synchronized void release() {
        delegate.release();
    }

    public synchronized ProgressObject redeploy(TargetModuleID[] arg0, InputStream arg1, InputStream arg2) throws UnsupportedOperationException, IllegalStateException {
        return delegate.redeploy(arg0, arg1, arg2);
    }

    public synchronized ProgressObject redeploy(TargetModuleID[] arg0, File arg1, File arg2) throws UnsupportedOperationException, IllegalStateException {
        return delegate.redeploy(arg0, arg1, arg2);
    }

    public synchronized boolean isRedeploySupported() {
        return delegate.isRedeploySupported();
    }

    public synchronized boolean isLocaleSupported(Locale arg0) {
        return delegate.isLocaleSupported(arg0);
    }

    public synchronized boolean isDConfigBeanVersionSupported(DConfigBeanVersionType arg0) {
        return delegate.isDConfigBeanVersionSupported(arg0);
    }

    public synchronized Target[] getTargets() throws IllegalStateException {
        return delegate.getTargets();
    }

    public synchronized Locale[] getSupportedLocales() {
        return delegate.getSupportedLocales();
    }

    public synchronized TargetModuleID[] getRunningModules(ModuleType arg0, Target[] arg1) throws TargetException, IllegalStateException {
        return delegate.getRunningModules(arg0, arg1);
    }

    public synchronized TargetModuleID[] getNonRunningModules(ModuleType arg0, Target[] arg1) throws TargetException, IllegalStateException {
        return delegate.getNonRunningModules(arg0, arg1);
    }

    public synchronized Locale getDefaultLocale() {
        return delegate.getDefaultLocale();
    }

    public synchronized DConfigBeanVersionType getDConfigBeanVersion() {
        return delegate.getDConfigBeanVersion();
    }

    public synchronized Locale getCurrentLocale() {
        return delegate.getCurrentLocale();
    }

    public synchronized TargetModuleID[] getAvailableModules(ModuleType arg0, Target[] arg1) throws TargetException, IllegalStateException {
        return delegate.getAvailableModules(arg0, arg1);
    }

    public synchronized ProgressObject distribute(Target[] arg0, ModuleType arg1, InputStream arg2, InputStream arg3) throws IllegalStateException {
        return delegate.distribute(arg0, arg1, arg2, arg3);
    }

    public synchronized ProgressObject distribute(Target[] arg0, InputStream arg1, InputStream arg2) throws IllegalStateException {
        return delegate.distribute(arg0, arg1, arg2);
    }

    public synchronized ProgressObject distribute(Target[] arg0, File arg1, File arg2) throws IllegalStateException {
        return delegate.distribute(arg0, arg1, arg2);
    }

    public synchronized DeploymentConfiguration createConfiguration(DeployableObject arg0) throws InvalidModuleException {
        return delegate.createConfiguration(arg0);
    }

}
