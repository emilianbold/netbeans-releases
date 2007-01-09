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

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.netbeans.installer.product.*;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.helper.Text.ContentType;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardComponent;


/////////////////////////////////////////////////////////////////////////////////
// Inner Classes

public abstract class ProductConfigurationLogic {
    private Product productComponent;
                    
    public abstract void install(final Progress progress) throws InstallationException;
    
    public abstract void uninstall(final Progress progress) throws UninstallationException;
    
    public abstract List<WizardComponent> getWizardComponents();
    
    public Product getProduct() {
        return productComponent;
    }
    
    public void setProductComponent(final Product productComponent) {
        this.productComponent = productComponent;
    }
    
    public final String getProperty(String name) {
        return getProperty(name, true);
    }
    
    public final String getProperty(String name, boolean parse) {
        String value = productComponent.getProperty(name);
        
        if (parse) {
            return value != null ? parseString(value) : null;
        } else {
            return value;
        }
    }
    
    public void setProperty(final String name, final String value) {
        productComponent.setProperty(name, value);
    }
    
    // various documentation getters ////////////////////////////////////////////
    public Text getLicense() {
        String text = parseString("$R{" + getClass().getPackage().getName().replace('.', '/') + "/license.txt}");
        
        return new Text(text, ContentType.PLAIN_TEXT);
    }
    
    public Map<String, Text> getThirdPartyLicenses() {
        return null;
    }
    
    public Text getReleaseNotes() {
        return null;
    }
    
    public Text getReadme() {
        return null;
    }
    
    public Text getInstallationInstructions() {
        return null;
    }
    
    // helper methods for SystemUtils and ResourceUtils /////////////////////////
    public String parseString(String string) {
        return SystemUtils.parseString(string, productComponent.getClassLoader());
    }
    
    public File parsePath(String path) {
        return SystemUtils.parsePath(path, productComponent.getClassLoader());
    }
    
    public String getString(String key) {
        return ResourceUtils.getString(getClass(), key);
    }
    
    public String getString(String baseName, String key) {
        return ResourceUtils.getString(baseName, key, productComponent.getClassLoader());
    }
    
    public String getString(String baseName, String key, Object... arguments) {
        return ResourceUtils.getString(baseName, key, productComponent.getClassLoader(), arguments);
    }
    
    public InputStream getResource(String path) {
        return ResourceUtils.getResource(path, productComponent.getClassLoader());
    }
}