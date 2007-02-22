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
package org.netbeans.modules.xml.refactoring;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.openide.filesystems.FileObject;


public class ErrorItem {

    public enum Level { WARNING, FATAL };

    private Object source;
    private String message;
    private Level level = Level.WARNING;

    public ErrorItem(Object source, String errorMessage) {
        this(source, errorMessage, Level.WARNING);
    }

    public ErrorItem(Object source, String errorMessage, Level level) {
        this.source = source;
        this.message = errorMessage;
        this.level = level;
    }
    
    /**
     * Returns the component on which the error happen during usage search, 
     * or null if the error does not associated with any particular component.
     */
    public Component getComponent() { 
        if (source instanceof Component) {
            return (Component) source;
        }
        return null;
    }
    
    public Model getModel() {
        if (source instanceof Model) {
            return (Model) source;
        }
        return null;
    }

    public FileObject getFile() {
        if (source instanceof FileObject) {
            return (FileObject) source;
        }
        return null;
    }
    
    public String getMessage() { 
        return message; 
    }
    
    public Level getLevel(){
        return level;
    }
    
    public void setLevel(Level level){
        this.level= level;
    }
}