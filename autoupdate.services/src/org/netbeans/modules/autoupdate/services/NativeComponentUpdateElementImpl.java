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

import org.netbeans.modules.autoupdate.updateprovider.InstallInfo;
import org.netbeans.modules.autoupdate.updateprovider.NativeComponentItem;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.autoupdate.UpdateManager;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Jiri Rechtacek
 */
public class NativeComponentUpdateElementImpl extends UpdateElementImpl {
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
    private NativeComponentItem nativeItem;

    public NativeComponentUpdateElementImpl (NativeComponentItem item, String providerName) {
        super (item, providerName);
        codeName = item.getCodeName ();
        specVersion = new SpecificationVersion (item.getSpecificationVersion ());
        source = providerName;
        installInfo = new InstallInfo (item);
        displayName = item.getDisplayName ();
        description = item.getDescription ();
        downloadSize = item.getDownloadSize ();
        this.nativeItem = item;
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
    
    public String getCategory () {
        if (category != null) {
            category = UpdateUnitFactory.UNSORTED_CATEGORY;
        }
        return category;
    }
    
    public String getDate () {
        return null;
    }
    
    public String getLicence () {
        return nativeItem.getAgreement ();
    }

    public InstallInfo getInstallInfo () {
        return installInfo;
    }
    
    public List<ModuleInfo> getModuleInfos () {
        return Collections.emptyList ();
    }

    public UpdateManager.TYPE getType () {
        return UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT;
    }

    public boolean isEnabled () {
        // XXX: how to detect if NativeComponent is enbaled or not
        return false;
    }            
    
    public boolean isAutoload () {
        return false;
    }

    public boolean isEager () {
        return false;
    }
    
    public boolean isFixed () {
        return false;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final NativeComponentUpdateElementImpl other = (NativeComponentUpdateElementImpl) obj;

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

}
