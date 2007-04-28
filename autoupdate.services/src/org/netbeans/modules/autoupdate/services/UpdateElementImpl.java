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

package org.netbeans.modules.autoupdate.services;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.Union2;
/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateElementImpl extends Object {
    private String codeName;
    private String displayName;
    private SpecificationVersion specVersion;
    private String description;
    private String source;
    private String author;
    private String homepage;
    private int downloadSize;
    private String category;
    private InstallInfo installInfo;
    private final Union2<? extends UpdateItemImpl,ModuleInfo> itemOrInfo;
    private Boolean isModule;
    private String providerName;
    private Logger log = null;
    
    /** Creates a new instance of ElementImpl */
    public UpdateElementImpl (ModuleInfo info) {
        this.providerName = NbBundle.getMessage(UpdateElementImpl.class, "UpdateElementImpl_ProviderName_ForModuleInfo");//NOI18N
        codeName = info.getCodeNameBase();
        displayName = info.getDisplayName ();
        specVersion = info.getSpecificationVersion ();
        description = (String) info.getLocalizedAttribute("OpenIDE-Module-Long-Description");
        source = Utils.getProductVersion (); // XXX: #102330 - need to distinguish module from original distribution and modules from UC
        category = (String) info.getLocalizedAttribute ("OpenIDE-Module-Display-Category");
        isModule = true;
        itemOrInfo = Union2.createSecond(info);
    }
        
    public UpdateElementImpl (ModuleItem impl, String providerName) {
        this.providerName = providerName;
        codeName = impl.getCodeName ();
        specVersion = new SpecificationVersion (impl.getSpecificationVersion ());
        source = providerName;
        installInfo = new InstallInfo (impl);
        String dn = impl.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-Name");
        if (dn == null) {
            getLogger ().log (Level.WARNING, "Module " + codeName + " doesn't provider display name. Value of \"OpenIDE-Module-Name\" cannot be null.");
        }
        displayName = dn == null ? codeName : dn;
        description = impl.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-Long-Description");
        category = impl.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-Display-Category");
        author = impl.getAuthor ();
        downloadSize = impl.getDownloadSize ();
        homepage = impl.getHomepage ();
        this.isModule = true;
        itemOrInfo = Union2.createFirst(impl);
    }
    
    public UpdateElementImpl (LocalizationItem impl, String providerName) {
        this.providerName = providerName;        
        codeName = impl.getCodeName ();
        specVersion = new SpecificationVersion (impl.getSpecificationVersion ());
        source = providerName;
        installInfo = new InstallInfo (impl);
        displayName = impl.getLocalizedModuleName ();
        description = impl.getLocalizedModuleDescription ();
        itemOrInfo = Union2.createFirst(impl);
    }
    
    public UpdateElementImpl (FeatureItem impl, String providerName) {
        this.providerName = providerName;                
        codeName = impl.getCodeName ();
        specVersion = new SpecificationVersion (impl.getSpecificationVersion ());
        source = providerName;
        installInfo = new InstallInfo (impl);
        displayName = impl.getDisplayName ();
        description = impl.getDescription ();
        isModule = false;
        itemOrInfo = Union2.createFirst(impl);
    }
    
    public UpdateElementImpl (NativeComponentItem impl, String providerName) {
        this.providerName = providerName;                
        codeName = impl.getCodeName ();
        specVersion = new SpecificationVersion (impl.getSpecificationVersion ());
        source = providerName;
        installInfo = new InstallInfo (impl);
        displayName = impl.getDisplayName ();
        description = impl.getDescription ();
        isModule = false;
        itemOrInfo = Union2.createFirst(impl);
    }
    
    public String getCodeName () {
        return codeName;
    }
    
    public String getDisplayName () {
        return displayName;
    }
    
    public SpecificationVersion getSpecificationVersion () {
        return specVersion;
    }
    
    public String getDescription () {
        return description;
    }
    
    public String getAuthor () {
        return author;
    }
    
    public String getHomepage () {
        return homepage;
    }
    
    public int getDownloadSize () {
        return downloadSize;
    }
    
    public String getSource () {
        return source;
    }
    public String getProviderName() {
        return providerName;
    }
    
    public String getCategory () {
        return category;
    }
    
    public InstallInfo getInstallInfo () {
        return installInfo;
    }
    
    public UpdateItemImpl getUpdateItemImpl () {
        return itemOrInfo.hasFirst() ? itemOrInfo.first() : null;
    }
    
    public ModuleInfo getModuleInfo () {
        if (itemOrInfo.hasFirst()) {
            UpdateItemImpl uImpl = itemOrInfo.first();
            if (uImpl instanceof ModuleItem) {
                return ((ModuleItem)uImpl).getModuleInfo();
            }
        } else if (itemOrInfo.hasSecond()) {
            return itemOrInfo.second();
        }
        return null;
    }
    
    
    public boolean isModule () {
        assert isModule != null;
        return isModule;
    }

    public boolean isEnabled() {
        ModuleInfo mInfo = getModuleInfo ();
        return mInfo != null ? mInfo.isEnabled() : false;
    }            
    
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final UpdateElementImpl other = (UpdateElementImpl) obj;

        if (this.specVersion != other.specVersion &&
            (this.specVersion == null ||
             !this.specVersion.equals(other.specVersion)))
            return false;
        if (this.codeName != other.codeName &&
            (this.codeName == null || !this.codeName.equals(other.codeName)))
            return false;
        return true;
    }

    public int hashCode() {
        int hash = 5;

        hash = 61 * hash + (this.codeName != null ? this.codeName.hashCode()
                                                  : 0);
        hash = 61 * hash +
               (this.specVersion != null ? this.specVersion.hashCode()
                                         : 0);
        return hash;
    }
    
    private Logger getLogger () {
        if (log == null) {
            log = Logger.getLogger (UpdateElementImpl.class.getName ());
        }
        return log;
    }


    
    /*public boolean isAutomaticallyEnabled() {
        return Utils.isAutomaticallyEnabled(codeName);
    }*/
}
