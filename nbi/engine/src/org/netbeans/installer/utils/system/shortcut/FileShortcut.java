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
 */

package org.netbeans.installer.utils.system.shortcut;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dmitry Lipin
 */
public abstract class FileShortcut extends Shortcut {
    private File target;
    private boolean modifyPath;
    private List<String> arguments;
    
    public FileShortcut(String name, File target) {
        super(name);
        setTarget(target);    
        setArguments(new ArrayList<String>());
    }
    
    public boolean canModifyPath() {
        return modifyPath;
    }
    
    public void setModifyPath(final boolean modifyPath) {
        this.modifyPath = modifyPath;
    }
    
    public String getTargetPath() {
        return target.getPath();
    }    
    public File getTarget() {
        return target;
    }
    public void setTarget(File target) {
        this.target = target;
    }
    
    
    public List<String> getArguments() {
        return this.arguments;
    }
    
    public String getArgumentsString() {
        if (arguments.size() != 0) {
            StringBuilder builder = new StringBuilder();
            
            for (int i = 0; i < arguments.size(); i++) {
                builder.append(arguments.get(i));
                
                if (i != arguments.size() - 1) {
                    builder.append(" ");
                }
            }
            
            return builder.toString();
        }  else {
            return null;
        }
    }
    
    public void setArguments(final List<String> arguments) {
        this.arguments = arguments;
    }
    
    public void addArgument(final String argument) {
        arguments.add(argument);
    }
    
    public void removeArgument(final String argument) {
        arguments.remove(argument);
    }
    @Deprecated
    public File getExecutable() {
        return getTarget();
    }
    @Deprecated
    public String getExecutablePath() {
        return getTargetPath();
    }
    @Deprecated
    public void setExecutable(final File executable) {
        setTarget(executable);
    }
    @Deprecated
    public boolean canModifyExecutablePath() {
        return canModifyPath();
    }
    @Deprecated
    public void setModifyExecutablePath(final boolean modifyExecutablePath) {
        setModifyPath(modifyExecutablePath);
    }
}
