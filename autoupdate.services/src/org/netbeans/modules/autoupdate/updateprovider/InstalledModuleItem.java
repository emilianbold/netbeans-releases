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

package org.netbeans.modules.autoupdate.updateprovider;

import org.netbeans.Module;
import org.netbeans.modules.autoupdate.services.UpdateItemDeploymentImpl;
import org.netbeans.modules.autoupdate.services.Utilities;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstalledModuleItem extends ModuleItem {
    
    private String codeName;
    private String specificationVersion;
    private ModuleInfo info;
    private Module m;
    private String author;
    private String source;
    private String installCluster;
    private String installDate;
    
    public InstalledModuleItem (
            String codeName,
            String specificationVersion,
            ModuleInfo info,
            String source,
            String author,
            String installCluster,
            String installTime) {
        this.codeName = codeName;
        this.specificationVersion = specificationVersion;
        this.info = info;
        this.author = author;
        this.installCluster = installCluster;
        this.source = source;
        this.installDate = installTime;
    }
    
    @Override
    public final String getCodeName () {
        return codeName;
    }
    
    @Override
    public final String getSpecificationVersion () {
        return specificationVersion;
    }
    
    public String getSource () {
        return source;
    }
    
    @Override
    public String getAuthor () {
        return author;
    }
    
    @Override
    public ModuleInfo getModuleInfo () {        
        return info;
    }
    
    @Override
    public String getAgreement () {
        assert false : "Don't call getAgreement() on InstalledModuleItem " + info;
        return null;
    }

    @Override
    public int getDownloadSize () {
        return 0;
    }
    
    @Override
    public String getDate () {
        return installDate;
    }
    
    @Override
    public UpdateItemDeploymentImpl getUpdateItemDeploymentImpl () {
        assert false : "Don't call getUpdateItemDeploymentImpl () on InstalledModuleItem.";
        return null;
    }
    
    @Override
    public boolean isAutoload () {
        return getModule () != null && getModule ().isAutoload ();
    }

    @Override
    public boolean isEager () {
        return getModule () != null && getModule ().isEager ();
    }
    
    private Module getModule () {
        if (m == null) {
            m = Utilities.toModule (info);
        }
        return m;
    }
}
