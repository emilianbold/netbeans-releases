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

package org.netbeans.modules.autoupdate.services;

import org.netbeans.api.autoupdate.UpdateManager.TYPE;
import org.netbeans.modules.autoupdate.updateprovider.InstallInfo;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleItem;
import org.netbeans.modules.autoupdate.updateprovider.ModuleItem;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateManager;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Jiri Rechtacek
 */
public class ModuleUpdateElementImpl extends UpdateElementImpl {
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
    private Logger log = null;
    private ModuleInfo moduleInfo;
    private ModuleItem item;
    private String date;
    private boolean isEager;
    private boolean isAutoload;
    
    public ModuleUpdateElementImpl (ModuleItem item, String providerName) {
        super (item, providerName);
        this.moduleInfo = item.getModuleInfo ();
        this.item = item;
        codeName = item.getCodeName ();
        specVersion = new SpecificationVersion (item.getSpecificationVersion ());
        source = item instanceof InstalledModuleItem ? ((InstalledModuleItem) item).getSource () : providerName;
        if (source == null) {
            source = Utilities.getProductVersion ();
        }
        installInfo = new InstallInfo (item);
        String dn = moduleInfo.getDisplayName ();
        assert dn != null : "Module " + codeName + " doesn't provider display name. Value of \"OpenIDE-Module-Name\" cannot be null.";
        if (dn == null) {
            getLogger ().log (Level.WARNING, "Module " + codeName + " doesn't provider display name. Value of \"OpenIDE-Module-Name\" cannot be null.");
        }
        displayName = dn == null ? codeName : dn;
        description = (String) moduleInfo.getLocalizedAttribute ("OpenIDE-Module-Long-Description");
        category = item.getCategory ();
        if (category == null) {
            category = (String) moduleInfo.getLocalizedAttribute ("OpenIDE-Module-Display-Category");
        }
        author = item.getAuthor ();
        downloadSize = item.getDownloadSize ();
        homepage = item.getHomepage ();
        date = item.getDate ();
        isEager = item.isEager ();
        isAutoload = item.isAutoload ();
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
    
    public String getDate () {
        return date;
    }
    
    public String getCategory () {
        if (isAutoload () || isFixed ()) {
            category = UpdateUnitFactory.LIBRARIES_CATEGORY;
        } else if (isEager ()) {
            category = UpdateUnitFactory.BRIDGES_CATEGORY;
        } else if (category == null || category.length () == 0) {
            category = UpdateUnitFactory.UNSORTED_CATEGORY;
        }
        return category;
    }
    
    public String getLicence () {
        return item.getAgreement ();
    }

    public InstallInfo getInstallInfo () {
        return installInfo;
    }
    
    public List<ModuleInfo> getModuleInfos () {
        return Collections.singletonList (getModuleInfo ());
    }
    
    public ModuleInfo getModuleInfo () {
        assert moduleInfo != null : "Each ModuleUpdateElementImpl has ModuleInfo, but " + this;
        
        // find really module info if present
        ModuleInfo info = Utilities.toModule (this.moduleInfo);
        if (info != null) {
            this.moduleInfo = info;
        } else {
            this.moduleInfo = item.getModuleInfo ();
        }
        
        return this.moduleInfo;
    }
    
    public TYPE getType () {
        return UpdateManager.TYPE.MODULE;
    }

    public boolean isEnabled () {
        return getModuleInfo ().isEnabled ();
    }            
    
    public boolean isAutoload () {
        return isAutoload;
    }

    public boolean isEager () {
        return isEager;
    }
    
    public boolean isFixed () {
        return Utilities.toModule(getCodeName (), null) == null ? false : Utilities.toModule(getCodeName (), null).isFixed ();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ModuleUpdateElementImpl other = (ModuleUpdateElementImpl) obj;

        if (this.specVersion != other.specVersion &&
            (this.specVersion == null ||
             !this.specVersion.equals(other.specVersion)))
            return false;
        if (this.codeName != other.codeName &&
            (this.codeName == null || !this.codeName.equals(other.codeName)))
            return false;
        return true;
    }

    @Override
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
            log = Logger.getLogger (ModuleUpdateElementImpl.class.getName ());
        }
        return log;
    }
    
}
