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
 *
 * $Id$
 */
package org.netbeans.installer.product.components;

import org.netbeans.installer.utils.applications.NetBeansUtils;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.actions.SetInstallationLocationAction;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class NbClusterConfigurationLogic extends ProductConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String BASE_IDE_UID =
            "nb-base"; // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private String[] clusterNames;
    private String productId;
    private String sourceUid;
    
    private List<WizardComponent> wizardComponents =
            new LinkedList<WizardComponent>();
    
    protected NbClusterConfigurationLogic(
            final String clusterName,
            final String productId,
            final String sourceUid) throws InitializationException {
        this(new String[]{clusterName}, productId, sourceUid);
    }
    
    protected NbClusterConfigurationLogic(
            final String[] clusterNames,
            final String productId,
            final String sourceUid) throws InitializationException {
        this.clusterNames = clusterNames;
        this.productId = productId;
        this.sourceUid = sourceUid;
        
        WizardAction action;
        
        action = new SetInstallationLocationAction();
        action.setProperty(
                SetInstallationLocationAction.SOURCE_UID_PROPERTY,
                sourceUid);
        wizardComponents.add(action);
    }
    
    protected NbClusterConfigurationLogic(
            final String clusterName,
            final String productId) throws InitializationException {
        this(new String[]{clusterName}, productId);
    }
    
    protected NbClusterConfigurationLogic(
            final String[] clusterNames,
            final String productId) throws InitializationException {
        this(clusterNames, productId, BASE_IDE_UID);
    }
    
    public void install(
            final Progress progress) throws InstallationException {
        final File installLocation = getProduct().getInstallationLocation();
        
        // get the list of suitable netbeans ide installations
        final List<Dependency> dependencies =
                getProduct().getDependencyByUid(sourceUid);
        final List<Product> sources =
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final File nbLocation = sources.get(0).getInstallationLocation();
        
        // add the cluster to the active clusters list //////////////////////////////
        for (String clusterName: clusterNames) {
            try {
                progress.setDetail(ResourceUtils.getString(
                        NbClusterConfigurationLogic.class,
                        "NCCL.install.netbeans.clusters", // NOI18N
                        clusterName));
                
                NetBeansUtils.addCluster(nbLocation, clusterName);
            } catch (IOException e) {
                throw new InstallationException(ResourceUtils.getString(
                        NbClusterConfigurationLogic.class,
                        "NCCL.install.error.netbeans.clusters", // NOI18N
                        clusterName),
                        e);
            }
        }
        
        // add the product id to the productid file /////////////////////////////////
        try {
            progress.setDetail(ResourceUtils.getString(
                    NbClusterConfigurationLogic.class,
                    "NCCL.install.productid",  // NOI18N
                    productId));
            
            NetBeansUtils.addPackId(nbLocation, productId);
        } catch (IOException e) {
            throw new InstallationException(ResourceUtils.getString(
                    NbClusterConfigurationLogic.class,
                    "NCCL.install.error.productid",  // NOI18N
                    productId),
                    e);
        }
        
        // remove files that are not suited for the current platform ////////////////
        for (String clusterName: clusterNames) {
            final File cluster = new File(installLocation, clusterName);
            
            try {
                progress.setDetail(ResourceUtils.getString(
                        NbClusterConfigurationLogic.class,
                        "NCCL.install.irrelevant.files")); // NOI18N
                
                SystemUtils.removeIrrelevantFiles(cluster);
            } catch (IOException e) {
                throw new InstallationException(ResourceUtils.getString(
                        NbClusterConfigurationLogic.class,
                        "NCCL.install.error.irrelevant.files"), // NOI18N
                        e);
            }
        }
        
        // corrent permisions on executable files ///////////////////////////////////
        for (String clusterName: clusterNames) {
            final File cluster = new File(installLocation, clusterName);
            
            try {
                progress.setDetail(ResourceUtils.getString(
                        NbClusterConfigurationLogic.class,
                        "NCCL.install.files.permissions")); // NOI18N
                
                SystemUtils.correctFilesPermissions(cluster);
            } catch (IOException e) {
                throw new InstallationException(ResourceUtils.getString(
                        NbClusterConfigurationLogic.class,
                        "NCCL.install.error.files.permissions"), // NOI18N
                        e);
            }
        }
        
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public void uninstall(
            final Progress progress) throws UninstallationException {
        final File installLocation = getProduct().getInstallationLocation();
        
        // get the list of suitable netbeans ide installations
        final List<Dependency> dependencies =
                getProduct().getDependencyByUid(sourceUid);
        final List<Product> sources =
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and assume that we're integrated with it
        final File nbLocation = sources.get(0).getInstallationLocation();
        
        // remove the cluster from the active clusters list /////////////////////////
        try {
            progress.setDetail(ResourceUtils.getString(
                    NbClusterConfigurationLogic.class,
                    "NCCL.uninstall.productid", // NOI18N
                    productId));
            
            NetBeansUtils.removePackId(nbLocation, productId);
        } catch (IOException e) {
            throw new UninstallationException(ResourceUtils.getString(
                    NbClusterConfigurationLogic.class,
                    "NCCL.uninstall.error.productid",  // NOI18N
                    productId),
                    e);
        }
        
        // remove the product id from the productid file ////////////////////////////
        for (String clusterName: clusterNames) {
            try {
                progress.setDetail(ResourceUtils.getString(
                        NbClusterConfigurationLogic.class,
                        "NCCL.uninstall.netbeans.clusters",  // NOI18N
                        clusterName));
                
                NetBeansUtils.removeCluster(nbLocation, clusterName);
            } catch (IOException e) {
                throw new UninstallationException(ResourceUtils.getString(
                        NbClusterConfigurationLogic.class,
                        "NCCL.uninstall.error.netbeans.clusters",  // NOI18N
                        clusterName),
                        e);
            }
        }
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }
    
    @Override
    public boolean registerInSystem() {
        return false;
    }

    @Override
    public RemovalMode getRemovalMode() {
        return RemovalMode.LIST;
    }
}
