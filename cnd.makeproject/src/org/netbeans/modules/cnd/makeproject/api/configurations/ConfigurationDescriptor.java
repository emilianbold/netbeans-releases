/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import javax.swing.Icon;

public abstract class ConfigurationDescriptor {
    private Configurations confs;
    int version = -1;

    public ConfigurationDescriptor() {
    }

    public void init(Configuration[] confs, int defaultConf) {
        //jlahoda: in order to support listeners on Configurations:
        if (this.confs == null) {
            this.confs = new Configurations();
        }
        if (defaultConf < 0)
            defaultConf = 0;
        this.confs.init(confs, defaultConf);
    }

    public Configurations getConfs() {
	return confs;
    }

    public void setConfs(Configurations confs) {
        if (this.confs == null) {
            this.confs = confs;
        } else {
            //jlahoda:added in order to support listeners on Configurations:
            this.confs.init(confs.getConfs(), confs.getActiveAsIndex());
        }
    }
    
    public int getVersion() {
        return version;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }

    public abstract Icon getIcon();

    public abstract String getBaseDir();
    
    public abstract Configuration defaultConf(String name, int type);

    public abstract void copyFromProjectDescriptor(ConfigurationDescriptor projectDescriptor);

    public abstract ConfigurationDescriptor cloneProjectDescriptor();
    
    public abstract void assign(ConfigurationDescriptor configurationDescriptor);

    public void cloneProjectDescriptor(ConfigurationDescriptor clone) {
	// projectType is already cloned
	clone.setConfs(confs.cloneConfs());
        clone.setVersion(getVersion());
    }

    public abstract boolean save();
    public abstract boolean save(String extraMessage);
    public abstract void setModified();
}
