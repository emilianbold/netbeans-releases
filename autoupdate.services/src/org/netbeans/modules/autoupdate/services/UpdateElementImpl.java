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
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import java.util.List;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Jiri Rechtacek
 */
public abstract class UpdateElementImpl extends Object {
    private UpdateUnit unit;
    private UpdateElement element;
    
    public UpdateElementImpl (UpdateItemImpl item, String providerName) {}
    
    public UpdateUnit getUpdateUnit () {
        return unit;
    }
    
    public void setUpdateUnit (UpdateUnit unit) {
        assert unit != null : "UpdateUnit cannot for " + this + " cannot be null.";
        this.unit = unit;
    }
    
    public UpdateElement getUpdateElement () {
        return element;
    }
    
    public void setUpdateElement (UpdateElement element) {
        assert element != null : "UpdateElement cannot for " + this + " cannot be null.";
        this.element = element;
    }
    
    public abstract String getCodeName ();
    
    public abstract String getDisplayName ();
    
    public abstract SpecificationVersion getSpecificationVersion ();
    
    public abstract String getDescription ();
    
    public abstract String getAuthor ();
    
    public abstract String getHomepage ();
    
    public abstract int getDownloadSize ();
    
    public abstract String getSource ();
    
    public abstract String getDate ();
    
    public abstract String getCategory ();
    
    public abstract boolean isEnabled ();
    
    public abstract String getLicence ();
    
    public abstract UpdateManager.TYPE getType ();
    
    public abstract boolean isAutoload ();
    public abstract boolean isEager ();
    public abstract boolean isFixed ();
    
    // XXX: try to rid of this
    public abstract List<ModuleInfo> getModuleInfos ();
    
    // XXX: try to rid of this
    public abstract InstallInfo getInstallInfo ();
    
}
