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

import java.net.URL;
import org.netbeans.spi.autoupdate.CustomInstaller;

/**
 *
 * @author Jiri Rechtacek
 */
public final class InstallInfo {
    
    private UpdateItemImpl item;
    
    /** Creates a new instance of InstallInfo */
    public InstallInfo (UpdateItemImpl item) {
        this.item = item;
    }
    
    public String getTargetCluster () {
        String res = null;
        if (item instanceof ModuleItem) {
            res = ((ModuleItem) item).getUpdateItemDeploymentImpl().getTargetCluster ();
        } else if (item instanceof LocalizationItem) {
            res = ((LocalizationItem) item).getUpdateItemDeploymentImpl ().getTargetCluster ();
        } else if (item instanceof FeatureItem) {
            assert false : "Feature not supported yet.";
        } else {
            assert false : "Unkown type of UpdateItem " + item;
        }
        return res;
    }
    
    public Boolean needsRestart () {
        Boolean res = null;
        if (item instanceof ModuleItem) {
            res = ((ModuleItem) item).getUpdateItemDeploymentImpl ().needsRestart ();
        } else if (item instanceof LocalizationItem) {
            res = ((LocalizationItem) item).getUpdateItemDeploymentImpl ().needsRestart ();
        } else if (item instanceof FeatureItem) {
            assert false : "Feature not supported yet.";
        } else {
            assert false : "Unkown type of UpdateItem " + item;
        }
        return res;
    }
    
    public Boolean isGlobal () {
        Boolean res = null;
        if (item instanceof ModuleItem) {
            res = ((ModuleItem) item).getUpdateItemDeploymentImpl ().isGlobal ();
        } else if (item instanceof LocalizationItem) {
            res = ((LocalizationItem) item).getUpdateItemDeploymentImpl ().isGlobal ();
        } else if (item instanceof FeatureItem) {
            assert false : "Feature not supported yet.";
        } else {
            assert false : "Unkown type of UpdateItem " + item;
        }
        return res;
    }
    
    public URL getDistribution () {
        URL res = null;
        if (item instanceof ModuleItem) {
            res = ((ModuleItem) item).getDistribution ();
        } else if (item instanceof LocalizationItem) {
            res = ((LocalizationItem) item).getDistribution ();
        } else if (item instanceof FeatureItem) {
            assert false : "Feature not supported yet.";
        } else {
            assert false : "Unkown type of UpdateItem " + item;
        }
        return res;
    }
    
    public CustomInstaller getCustomInstaller () {
        CustomInstaller res = null;
        if (item instanceof NativeComponentItem) {
            res = ((NativeComponentItem) item).getUpdateItemDeploymentImpl ().getCustomInstaller ();
        }
        return res;
    }
    
    public UpdateItemImpl getUpdateItemImpl () {
        return item;
    }
}
