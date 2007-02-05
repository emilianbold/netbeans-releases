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
package org.netbeans.installer.utils.helper;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Kirill Sorokin
 */
public class ApplicationDescriptor {
    private String uid;
    
    private String displayName;
    private String icon;
    
    private String installPath;
    
    private String uninstallCommand;
    private String modifyCommand;
    
    private Map<String, Object> parameters;
    
    public ApplicationDescriptor(
            final String uid,
            final String displayName,
            final String icon,
            final String installPath,
            final String uninstallCommand,
            final String modifyCommand) {
        this.uid = uid;
        
        this.displayName = displayName;
        this.icon = icon;
        
        this.installPath = installPath;
        
        this.uninstallCommand = uninstallCommand;
        this.modifyCommand = modifyCommand;
        
        this.parameters = new HashMap<String, Object>();
    }
    
    public ApplicationDescriptor(
            final String uid,
            final String displayName,
            final String icon,
            final String installPath,
            final String uninstallCommand,
            final String modifyCommand,
            final Map<String, Object> parameters) {
        this(uid,
            displayName,
            icon,
            installPath,
            uninstallCommand,
            modifyCommand);
        
        this.parameters.putAll(parameters);
    }
    
    public String getUid() {
        return uid;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getInstallPath() {
        return installPath;
    }
    
    public String getUninstallCommand() {
        return uninstallCommand;
    }
    
    public String getModifyCommand() {
        return modifyCommand;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
}
