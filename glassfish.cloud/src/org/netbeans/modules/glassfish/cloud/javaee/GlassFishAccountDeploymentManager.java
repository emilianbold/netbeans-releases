/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.cloud.javaee;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;

/**
 * Deployment manager for GlassFish remote or local GlassFish server with
 * user account on cloud.
 * <p/>
 * Provides the core set of functions a Java EE platform must provide for
 * Java EE application deployment. It provides server related information,
 * such as list of deployment targets and GlassFish cloud unique runtime
 * configuration information.
 * <p/>
 * Based on API that will be made optional in JavaEE 7 platform.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishAccountDeploymentManager
        extends GlassFishDeploymentManager {

    /**
     * The redeploy method provides a means for updating currently deployed
     * Java EE applications.
     * <p/>
     * This is an optional method for GlassFish cloud implementation.
     * <p/>
     * @param targetList  A list of server targets the user is specifying
     *                    this application be deployed to.
     * @param deployment  Context describing everything necessary for a module
     *                    deployment.
     * @return An object that tracks and reports the status of the distribution
     *         process. 
     */
    @Override
    public ProgressObject redeploy(TargetModuleID[] targetList,
            DeploymentContext deployment) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Distribute method performs three tasks: it validates the deployment
     * configuration data, generates all container specific classes and
     * interfaces, and moves the fully baked archive to the designated
     * deployment targets. 
     * <p/>
     * @param targetList  A list of server targets the user is specifying
     *                    this application be deployed to.
     * @param deployment  Context describing everything necessary for a module
     *                    deployment.
     * @return An object that tracks and reports the status of the distribution
     *         process. 
     */
    @Override
    public ProgressObject distribute(Target[] targetList,
            DeploymentContext deployment) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Retrieve the list of Java EE application modules distributed to
     * the identified targets and that are currently running on the
     * associated server or servers.
     * <p/>
     * @param moduleType Predefined designator for a Java EE module type.
     * @param targetList List of deployment Target designators the user wants
     *                   checked for module running status.
     * @return An array of TargetModuleID objects representing the running
     *         modules or <code>null</code> if there are none. 
     * @throws TargetException Is thrown when the method is called when running
     *         in disconnected mode. 
     * @throws IllegalStateException An invalid Target designator encountered.
     */
    @Override
    public TargetModuleID[] getRunningModules(ModuleType mt, Target[] targets)
            throws TargetException, IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Retrieve the list of Java EE application modules distributed to
     * the identified targets and that are currently not running on the
     * associated server or servers.
     * <p/>
     * @param moduleType Predefined designator for a Java EE module type.
     * @param targetList List of deployment Target designators the user wants
     *                   checked for module not running status.
     * @return An array of TargetModuleID objects representing the non running
     *         modules or <code>null</code> if there are none. 
     * @throws TargetException Is thrown when the method is called when running
     *         in disconnected mode. 
     * @throws IllegalStateException An invalid Target designator encountered.
     */
    @Override
    public TargetModuleID[] getNonRunningModules(ModuleType moduleType,
            Target[] targetList) throws TargetException, IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Retrieve the list of all Java EE application modules running or not
     * running on the identified targets.
     * <p/>
     * @param moduleType Predefined designator for a Java EE module type.
     * @param targetList List of deployment Target designators the user wants
     *                   checked for module not running status. 
     * @return An array of TargetModuleID objects representing all deployed
     *         modules running or not or <code>null</code> if there are no
     *         deployed modules. 
     * @throws TargetException An invalid Target designator encountered.
     * @throws IllegalStateException Is thrown when the method is called when
     *         running in disconnected mode. 
     */
    @Override
    public TargetModuleID[] getAvailableModules(ModuleType moduleType,
            Target[] targetList) throws TargetException, IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Retrieve an object that provides server-specific deployment
     * configuration information for the Java EE deployable component.
     * <p/>
     * @param dObj An object representing a Java EE deployable component.
     * @return An object that provides server-specific deployment
     *         configuration information for the Java EE deployable component.
     * @throws InvalidModuleException DeployableObject is an unknown or
     *         unsupported component for this configuration tool.
     */
    @Override
    public DeploymentConfiguration createConfiguration(DeployableObject dObj)
            throws InvalidModuleException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Distribute method performs three tasks: it validates the deployment
     * configuration data, generates all container specific classes and
     * interfaces, and moves the fully baked archive to the designated
     * deployment targets. 
     * <p/>
     * @param targetList     A list of server targets the user is specifying
     *                       this application be deployed to.
     * @param moduleArchive  The file name of the application archive to be
     *                       disTributed.
     * @param deploymentPlan The XML file containing the runtime configuration
     *                       information associated with this application
     *                       archive.
     * @return An object that tracks and reports the status of the distribution
     *         process. 
     * @throws IllegalStateException Is thrown when the method is called when
     *         running in disconnected mode.
     */
    @Override
    public ProgressObject distribute(Target[] targetList, File moduleArchive,
            File deploymentPlan) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Distribute method performs three tasks: it validates the deployment
     * configuration data, generates all container specific classes and
     * interfaces, and moves the fully baked archive to the designated
     * deployment targets. 
     * <p/>
     * This method is no more supported and will always throw
     * <code>UnsupportedOperationException</code>
     * <p/>
     * @param targetList     A list of server targets the user is specifying
     *                       this application be deployed to.
     * @param moduleArchive  Input stream containing the application archive
     *                       to be distributed.
     * @param deploymentPlan Input stream containing the deployment
     *                       configuration information associated with this
     *                       application archive.
     * @return An object that tracks and reports the status of the distribution
     *         process.
     * @throws IllegalStateException Is thrown when the method is called when
     *         running in disconnected mode.
     * @deprecated as of Java EE 5, replaced with {@link #distribute(
     *             javax.enterprise.deploy.spi.Target[],
     *             javax.enterprise.deploy.shared.ModuleType,
     *             java.io.InputStream, java.io.InputStream)}
     */
    @Deprecated
    @Override
    public ProgressObject distribute(Target[] targetList,
            InputStream moduleArchive, InputStream deploymentPlan)
            throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Distribute method performs three tasks: it validates the deployment
     * configuration data, generates all container specific classes and
     * interfaces, and moves the fully baked archive to the designated
     * deployment targets. 
     * <p/>
     * @param targetList     A list of server targets the user is specifying
     *                       this application be deployed to.
     * @param type           Module type of this application archive.
     * @param moduleArchive  Input stream containing the application archive
     *                       to be distributed.
     * @param deploymentPlan Input stream containing the deployment
     *                       configuration information associated with this
     *                       application archive.
     * @return An object that tracks and reports the status of the distribution
     *         process.
     * @throws IllegalStateException Is thrown when the method is called when
     *         running in disconnected mode.
     */
    @Override
    public ProgressObject distribute(Target[] targetList, ModuleType type,
            InputStream moduleArchive, InputStream deploymentPlan)
            throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Start the application running.
     * <p/>
     * Only the TargetModuleIDs which represent a root module are valid for
     * being started. A root TargetModuleID has no parent. A TargetModuleID
     * with a parent can not be individually started. A root TargetModuleID
     * module and all its child modules will be started.
     * @param moduleIDList An array of TargetModuleID objects representing
     *                     the modules to be started. 
     * @return An object that tracks and reports the status of the start
     *         operation. 
     * @throws IllegalStateException Is thrown when the method is called when
     *                               running in disconnected mode.
     */
    @Override
    public ProgressObject start(TargetModuleID[] moduleIDList) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Stop the application running.
     * <p/>
     * @param moduleIDList An array of TargetModuleID objects representing
     *                     the modules to be stopped.
     * @return An object that tracks and reports the status of the stop
     *         operation. 
     * @throws IllegalStateException Is thrown when the method is called when
     *                               running in disconnected mode.
     */
    @Override
    public ProgressObject stop(TargetModuleID[] moduleIDList)
            throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Remove the application from the target server.
     * <p/>
     * Only the TargetModuleIDs which represent a root module are valid for
     * undeployment. A root TargetModuleID has no parent. A TargetModuleID with
     * a parent can not be undeployed. A root TargetModuleID module and all its
     * child modules will be undeployed. The root TargetModuleID module and all
     * its child modules must stopped before they can be undeployed.
     * <p/>
     * @param moduleIDList An array of TargetModuleID objects representing
     *                     the root modules to be stopped. 
     * @return Object that tracks and reports the status of the stop operation. 
     * @throws IllegalStateException Is thrown when the method is called when
     *                               running in disconnected mode.
     */
    @Override
    public ProgressObject undeploy(TargetModuleID[] moduleIDList)
            throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * This method designates whether this platform vendor provides application
     * redeployment functionality.
     * <p/>
     * @return Value of <true>true</true> means redeployment is supported
     *         by this vendor's DeploymentManager. Value of <code>false</code>
     *         means it is not.
     */
    @Override
    public boolean isRedeploySupported() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * The redeploy method provides a means for updating currently deployed
     * Java EE applications.
     * <p/>
     * This is an optional method for GlassFish cloud implementation.
     * <p/>
     * @param moduleIDList   An array of designators of the applications to
     *                       be updated.
     * @param moduleArchive  The file name of the application archive to
     *                       be disrtibuted.
     * @param deploymentPlan The deployment configuration information associated
     *                       with this application archive.
     * @returns An object that tracks and reports the status of the redeploy
     *          operation.
     * @throws IllegalStateException         Is thrown when the method is called
     *                                       when running in disconnected mode.
     * @throws UnsupportedOperationException This optional command is not
     *                                       supported by this implementation.
     */    
    @Override
    public ProgressObject redeploy(TargetModuleID[] moduleIDList,
            File moduleArchive, File file1)
            throws UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * The redeploy method provides a means for updating currently deployed
     * Java EE applications.
     * <p/>
     * This is an optional method for GlassFish cloud implementation.
     * <p/>
     * @param moduleIDList   An array of designators of the applications
     *                       to be updated.
     * @param moduleArchive  The input stream containing the application archive
     *                       to be distributed.
     * @param deploymentPlan The input stream containing the runtime
     *                       configuration information associated with this
     *                       application archive. 
     * @returns An object that tracks and reports the status of the redeploy
     *          operation.
     * @throws IllegalStateException         Is thrown when the method is called
     *                                       when running in disconnected mode.
     * @throws UnsupportedOperationException This optional command is not
     *                                       supported by this implementation.
     */
    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, InputStream in, InputStream in1) throws UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    /**
     * The release method is the mechanism by which the tool signals to the
     * DeploymentManager that the tool does not need it to continue running
     * connected to the platform.
     * <p/>
     * The tool may be signaling it wants to run in a disconnected mode
     * or it is planning to shutdown. When release is called
     * the DeploymentManager may close any Java EE resource connections
     * it had for deployment configuration and perform other related
     * resource cleanup. It should not accept any new operation requests
     * (i.e., distribute, start stop, undeploy, redeploy. It should finish
     * any operations that are currently in process. Each ProgressObject
     * associated with a running operation should be marked as released.
     */
    @Override
    public void release() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns the default locale supported by this implementation of
     * <code>javax.enterprise.deploy.spi</code> sub packages. 
     * <p/>
     * @return Default locale for this implementation.
     */
    @Override
    public Locale getDefaultLocale() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns the active locale this implementation of
     * <code>javax.enterprise.deploy.spi<code> sub packages is running.
     * <p/>
     * @return Active locale of this implementation.
     */
    @Override
    public Locale getCurrentLocale() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Set the active locale for this implementation of
     * <code>javax.enterprise.deploy.spi</code> sub packages to run.
     * <p/>
     * @param locale Locale to set for this implementation.
     * @throws UnsupportedOperationException Provided locale is not supported.
     */
    @Override
    public void setLocale(Locale locale) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns an array of supported locales for this implementation.
     * <p/>
     * @return List of supported locales.
     */
    @Override
    public Locale[] getSupportedLocales() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Reports if this implementation supports the designated locale.
     * <p/>
     * @param locale
     * @return Value of <code>true</code> means it is supported and
     *         <code>false</code> it is not.
     */
    @Override
    public boolean isLocaleSupported(Locale locale) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns the Java EE platform version number for which the configuration
     * beans are provided.
     * <p/>
     * The beans must have been compiled with the J2SE version required by the
     * Java EE platform.
     * <p/>
     * @return DConfigBeanVersionType object representing the platform version
     *         number for which these beans are provided.
     */
    @Override
    public DConfigBeanVersionType getDConfigBeanVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns if the configuration beans support the Java EE platform
     * version specified.
     * <p/>
     * @param version DConfigBeanVersionType object representing the Java EE
     *                platform version for which support is requested.
     * @return Value of <code>true</code> if the version is supported
     *         and <code>false</code> if is not.
     */
    @Override
    public boolean isDConfigBeanVersionSupported(
            DConfigBeanVersionType version) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Set the configuration beans to be used to the Java EE platform version
     * specified.
     * <p/>
     * @param version DConfigBeanVersionType object representing the Java EE
     *                platform version for which support is requested. 
     * @throws DConfigBeanVersionUnsupportedException When the requested bean
     *                                                version is not supported.
     */
    @Override
    public void setDConfigBeanVersion(DConfigBeanVersionType version)
            throws DConfigBeanVersionUnsupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
