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

package org.netbeans.modules.cnd.discovery.wizard.tree;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Alexander Simon
 */
public class FileConfigurationNode extends DefaultMutableTreeNode {
    private FileConfigurationImpl file;
    
    public FileConfigurationNode(FileConfigurationImpl file) {
        super(file);
        this.file = file;
    }
    
    @Override
    public String toString() {
        String name = file.getFileName();
        //if (name.lastIndexOf('/')>0) {
        //    name = name.substring(name.lastIndexOf('/')+1);
        //}
        return name;
    }
    
    public FileConfigurationImpl getFile() {
        return file;
    }
    
    public boolean isCheckedInclude() {
        return !file.overrideIncludes();
    }
    
    public void setCheckedInclude(boolean checkedInclude) {
        file.setOverrideIncludes(!checkedInclude);
    }
    
    public boolean isCheckedMacro() {
        return !file.overrideMacros();
    }
    
    public void setCheckedMacro(boolean checkedMacro) {
        file.setOverrideMacros(!checkedMacro);
    }
}
