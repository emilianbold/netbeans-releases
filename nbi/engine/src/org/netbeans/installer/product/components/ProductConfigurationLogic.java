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
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
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
    private Product product;
    
    // abstract /////////////////////////////////////////////////////////////////////
    public abstract void install(final Progress progress) throws InstallationException;
    
    public abstract void uninstall(final Progress progress) throws UninstallationException;
    
    public abstract List<WizardComponent> getWizardComponents();
    
    // product getter/setter ////////////////////////////////////////////////////////
    public final Product getProduct() {
        return product;
    }
    
    public final void setProduct(final Product product) {
        this.product = product;
    }
    
    // product properties ///////////////////////////////////////////////////////////
    public final String getProperty(String name) {
        return getProperty(name, true);
    }
    
    public final String getProperty(String name, boolean parse) {
        final String value = product.getProperty(name);
        
        if (parse) {
            return value != null ? parseString(value) : null;
        } else {
            return value;
        }
    }
    
    public final void setProperty(final String name, final String value) {
        product.setProperty(name, value);
    }
    
    // various documentation getters - to be overriden //////////////////////////////
    public Text getLicense() {
        final String text = parseString(
                "$R{" + StringUtils.asPath(getClass()) + "/license.txt}");
        
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
    
    // system integration probes ////////////////////////////////////////////////////
    public boolean registerInSystem() {
        return true;
    }
    
    // helper methods for SystemUtils and ResourceUtils /////////////////////////////
    public final String parseString(String string) {
        return SystemUtils.parseString(string, product.getClassLoader());
    }
    
    public final File parsePath(String path) {
        return SystemUtils.parsePath(path, product.getClassLoader());
    }
    
    public final String getString(String key) {
        return ResourceUtils.getString(getClass(), key);
    }
    
    public final String getString(String key, Object... arguments) {
        return ResourceUtils.getString(getClass(), key, arguments);
    }
    
    public final InputStream getResource(String path) {
        return ResourceUtils.getResource(path, product.getClassLoader());
    }
}