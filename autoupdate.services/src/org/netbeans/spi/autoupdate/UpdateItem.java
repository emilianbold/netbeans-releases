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

package org.netbeans.spi.autoupdate;

import java.net.URL;
import java.util.Locale;
import java.util.Set;
import java.util.jar.Manifest;
import org.netbeans.modules.autoupdate.services.LocalizationItem;
import org.netbeans.modules.autoupdate.services.FeatureItem;
import org.netbeans.modules.autoupdate.services.ModuleItem;
import org.netbeans.modules.autoupdate.services.NativeComponentItem;
import org.netbeans.modules.autoupdate.services.Trampoline;
import org.netbeans.modules.autoupdate.services.UpdateItemImpl;

/** Represents a item of content provider by <code>UpdateProvider</code>. These items are exposed to 
 * Autoupdate infrastructure what works on them.
 *
 * @author Jiri Rechtacek
 */
public final class UpdateItem {
    static {
        Trampoline.SPI = new TrampolineSPI ();
    }
    
    UpdateItemImpl impl;
    UpdateItem original;
    
    /** Creates a new instance of UpdateItem */
    UpdateItem (UpdateItemImpl item) {
        impl = item;
        item.setUpdateItem (this);
    }
    
    /** Creates <code>UpdateItem/code> which represents NetBeans Module in Autoupdate infrastructure.
     * UpdateItem is identify by <code>codeName</code> and <code>specificationVersion<code>.
     * 
     * @param codeName code name of module
     * @param specificationVersion specification version of module
     * @param distribution URL to NBM file
     * @param author name of module author or null
     * @param downloadSize size of NBM file in bytes
     * @param homepage homepage of module or null
     * @param manifest <code>java.util.jar.Manifest</code> describes the module in NetBeans module system
     * @param needsRestart if true then IDE must be restarted after module installation
     * @param isGlobal control if the module will be installed into the installation directory or into user's dir
     * @param targetCluster name of cluster where new module will be installed if installation isGlobal
     * @param license <code>UpdateLicense</code> represents license name and text of license agreement
     * @return UpdateItem
     */
    public static final UpdateItem createModule (
                                    String codeName,
                                    String specificationVersion,
                                    URL distribution,
                                    String author,
                                    String downloadSize,
                                    String homepage,
                                    Manifest manifest,
                                    Boolean needsRestart,
                                    Boolean isGlobal,
                                    String targetCluster,
                                    UpdateLicense license) {
        ModuleItem item = new ModuleItem (codeName, specificationVersion, distribution, 
                author, downloadSize, homepage,
                manifest, needsRestart, isGlobal, targetCluster, license.impl);
        return new UpdateItem (item);
    }
    
    /** Creates <code>UpdateItem</code> which represents <code>Feature</code>, it's means group
     * of NetBeans Modules. This <code>Feature</code> is handled in UI as atomic item.
     * UpdateItem is identify by <code>codeName</code> and <code>specificationVersion<code>.
     * 
     * 
     * @param codeName code name of feature
     * @param specificationVersion specification version of feature
     * @param dependencies dependencies to NetBeans modules on which is the feature based
     * @param displayName display name
     * @param description description
     * @return UpdateItem
     */
    public static final UpdateItem createFeature (
                                    String codeName,
                                    String specificationVersion,
                                    Set<String> dependencies,
                                    String displayName,
                                    String description) {
        FeatureItem item = new FeatureItem (codeName, specificationVersion, dependencies, displayName, description);
        return new UpdateItem (item);
    }
    
    /** Creates <code>UpdateItem</code> which represents Native Component with own installer. This component
     * can be visualized in UI as common item, when an user wants to install this component then
     * own <code>CustomInstaller</code> is call back.
     * 
     * @param codeName code name of the native component
     * @param specificationVersion specification version of component
     * @param dependencies dependencies to other <code>UpdateItem</code>
     * @param distribution URL to file for installation
     * @param downloadSize size of installation file in bytes
     * @param displayName display name
     * @param description description
     * @param needsRestart if true then IDE must be restarted after component installation
     * @param isGlobal control if the control will be installed into the installation directory or into user's dir
     * @param targetCluster name of cluster where new module will be installed if installation isGlobal
     * @param installer <code>CustomInstaller</code> call-back interface
     * @param license <code>UpdateLicense</code> represents license name and text of license agreement
     * @return <code>UpdateItem</code>
     */
    public static final UpdateItem createNativeComponent (
                                    String codeName,
                                    String specificationVersion,
                                    URL distribution,
                                    String downloadSize,
                                    Set<String> dependencies,
                                    String displayName,
                                    String description,
                                    Boolean needsRestart,
                                    Boolean isGlobal,
                                    String targetCluster,
                                    CustomInstaller installer,
                                    UpdateLicense license
                                    ) {
        NativeComponentItem item = new NativeComponentItem (codeName, specificationVersion, distribution, downloadSize, dependencies,
                displayName, description, needsRestart, isGlobal, targetCluster, installer, license.impl);
        return new UpdateItem (item);
    }
    
    /** Creates <code>UpdateItem</code> which can localized NetBeans Module in given <code>Locale</code>.
     * 
     * @param codeName code name of the module for localization
     * @param specificationVersion specification version of localization
     * @param moduleSpecificationVersion specification version of the module for localization
     * @param locale locale
     * @param branding branding
     * @param localizedName localized name of module
     * @param localizedDescription localized descripton of module
     * @param distribution URL to NBM file
     * @param needsRestart if true then IDE must be restarted after module installation
     * @param isGlobal control if the module will be installed into the installation directory or into user's dir
     * @param targetCluster name of cluster where new module will be installed if installation isGlobal
     * @param license <code>UpdateLicense</code> represents license name and text of license agreement
     * @return <code>UpdateItem</code>
     */
    public static final UpdateItem createLocalization (
                                    String codeName,
                                    String specificationVersion,
                                    String moduleSpecificationVersion,
                                    Locale locale,
                                    String branding,
                                    String localizedName,
                                    String localizedDescription,
                                    URL distribution,
                                    Boolean needsRestart,
                                    Boolean isGlobal,
                                    String targetCluster,
                                    UpdateLicense license) {
        LocalizationItem item = new LocalizationItem (codeName, specificationVersion, distribution,
                locale, branding, moduleSpecificationVersion, localizedName, localizedDescription,
                needsRestart, isGlobal, targetCluster, license.impl);
        return new UpdateItem (item);
    }
    
}
