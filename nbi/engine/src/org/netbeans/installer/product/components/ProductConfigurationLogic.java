/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.product.components;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.helper.Text.ContentType;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardComponent;

public abstract class ProductConfigurationLogic {
    private Product product;
    
    // abstract /////////////////////////////////////////////////////////////////////
    public abstract void install(
            final Progress progress) throws InstallationException;
    
    public abstract void uninstall(
            final Progress progress) throws UninstallationException;
    
    public abstract List<WizardComponent> getWizardComponents();
    
    // product getter/setter ////////////////////////////////////////////////////////
    protected final Product getProduct() {
        return product;
    }
    
    final void setProduct(final Product product) {
        this.product = product;
    }
    
    // validation ///////////////////////////////////////////////////////////////////
    public String validateInstallation() {
        if (getProduct().getStatus() == Status.INSTALLED) {
            final File installLocation = getProduct().getInstallationLocation();
            if (installLocation == null) {
                return ResourceUtils.getString(
                        ProductConfigurationLogic.class,
                        "PCL.validation.directory.null");
            }
            if (!installLocation.exists()) {
                return ResourceUtils.getString(
                        ProductConfigurationLogic.class,
                        "PCL.validation.directory.missing",
                        installLocation);
            }
            if (!installLocation.isDirectory()) {
                return ResourceUtils.getString(
                        ProductConfigurationLogic.class,
                        "PCL.validation.directory.file",
                        installLocation);
            }
            if (FileUtils.isEmpty(installLocation)) {
                return ResourceUtils.getString(
                        ProductConfigurationLogic.class,
                        "PCL.validation.directory.empty",
                        installLocation);
            }
        }
        
        return null;
    }
    
    // product properties ///////////////////////////////////////////////////////////
    protected final String getProperty(String name) {
        return getProperty(name, true);
    }
    
    protected final String getProperty(String name, boolean parse) {
        final String value = product.getProperty(name);
        
        if (parse) {
            return value != null ? parseString(value) : null;
        } else {
            return value;
        }
    }
    
    protected final void setProperty(final String name, final String value) {
        product.setProperty(name, value);
    }
    
    // various documentation/legal getters //////////////////////////////////////////
    public Text getLicense() {
        final String text = parseString(
                "$R{" + StringUtils.asPath(getClass()) + "/license.txt}");
        
        return new Text(text, ContentType.PLAIN_TEXT);
    }
    
    public Map<String, Text> getThirdPartyLicenses() {
        return null;
    }
    
    public Text getThirdPartyLicense() {
        return null;
    }
    
    public Text getReleaseNotes() {
        return null;
    }
    
    public Text getReadme() {
        return null;
    }
    
    public Text getDistributionReadme() {
        return null;
    }
    
    public Text getInstallationInstructions() {
        return null;
    }
    
    // various informational probes /////////////////////////////////////////////////
    public boolean registerInSystem() {
        return true;
    }
    
    public String getSystemDisplayName() {
        return product.getDisplayName();
    }
    
    public boolean allowModifyMode() {
        return true;
    }
    
    /**
     * Specifies whether some special handling should be applied to the product's
     * files when placing them on disk. If this method returns <code>true</code>,
     * then the files of the product will be "wrapped" in the standard MacOS 
     * application directories structure. Also the {@link #getExecutable()} and
     * {@link #getIcon()} methods will be called and will be expected to return 
     * proper values for the product's executable and icon so that they are 
     * symlinked from appropriate locations.
     * 
     * @return Whether the product's files should be wrapped with the standard 
     *      Mac OS application directories structure.
     */
    public boolean wrapForMacOs() {
        return false;
    }
    
    /**
     * Specifies whether the the installation directory for the product should end
     * with Mac OS's specific extension - <code>.app</code>.
     * 
     * @return <code>true</code> - if the installation directory must end with 
     *      <code>.app</code>, <code>false</code> - otherwise.
     */
    public boolean requireDotAppForMacOs() {
        return false;
    }
    
    public boolean prohibitExclamation() {
        return true;
    }
    
    public String getExecutable() {
        return null;
    }
    
    public String getIcon() {
        return null;
    }
    
    public int getLogicPercentage() {
        return 10;
    }
    
    // installation behavior ////////////////////////////////////////////////////////
    public RemovalMode getRemovalMode() {
        return RemovalMode.ALL;
    }
    
    // helper methods for system utils and resource utils ///////////////////////////
    protected final String parseString(String string) {
        return SystemUtils.resolveString(string, product.getClassLoader());
    }
    
    protected final File parsePath(String path) {
        return SystemUtils.resolvePath(path, product.getClassLoader());
    }
    
    protected final String getString(String key) {
        return ResourceUtils.getString(getClass(), key);
    }
    
    protected final String getString(String key, Object... arguments) {
        return ResourceUtils.getString(getClass(), key, arguments);
    }
    
    protected final InputStream getResource(String path) {
        return ResourceUtils.getResource(path, product.getClassLoader());
    }
}
