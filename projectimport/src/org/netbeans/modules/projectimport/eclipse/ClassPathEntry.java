/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.eclipse;

import java.io.File;
import org.openide.ErrorManager;

/**
 * Represents one classpath entry of an Eclipse project's .classpath file.
 *
 * @author mkrauskopf
 */
public final class ClassPathEntry {
    
    /** Serves for type-safe enumeration of types */
    static class Type {
        String desc;
        private Type(String desc) {
            this.desc = desc;
        }
        public String toString() {
            return desc;
        }
    }
    
    static final Type TYPE_OUTPUT = new Type("output");
    static final Type TYPE_LIBRARY = new Type("lib");
    static final Type TYPE_EXTERNAL_LIBRARY = new Type("lib-ext");
    static final Type TYPE_CONTAINER = new Type("con");
    static final Type TYPE_VARIABLE= new Type("var");
    static final Type TYPE_SOURCE = new Type("src");
    static final Type TYPE_PROJECT = new Type("src-prj");
    static final Type TYPE_LINK = new Type("src-link");
    static final Type TYPE_UNKNOWN = new Type("unkown");
    
    private Type type;
    private String rawPath;
    private String absolutePath;
    
    static ClassPathEntry create(String type, String rawPath) {
        ClassPathEntry cpe = new ClassPathEntry();
        cpe.rawPath = rawPath;
        cpe.setTypeFromRawtype(type);
        return cpe;
    }
    
    void setType(ClassPathEntry.Type type) {
        this.type = type;
    }
    
    private void setTypeFromRawtype(String rawType) {
        if ("output".equals(rawType)) {
            this.type = TYPE_OUTPUT;
        } else if ("src".equals(rawType)) {
            // raw path for project entries starts with slash (on all platforms)
            if (rawPath.startsWith("/")) {
                this.type = TYPE_PROJECT;
            } else {
                this.type = TYPE_SOURCE;
            }
        } else if ("lib".equals(rawType)) {
            if (isRawPathRelative()) {
                this.type = TYPE_LIBRARY;
            } else {
                this.type = TYPE_EXTERNAL_LIBRARY;
            }
        } else if ("con".equals(rawType)) {
            this.type = TYPE_CONTAINER;
        } else if ("var".equals(rawType)) {
            this.type = TYPE_VARIABLE;
        } else {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Unkown type encountered in " +
                    "ClassPathEntry.setTypeFromRawtype(): " + rawType);
            this.type = TYPE_UNKNOWN;
        }
    }
    
    Type getType() {
        return type;
    }
    
    public String getRawPath() {
        return rawPath;
    }
    
    public String getAbsolutePath() {
        return absolutePath;
    }
    
    void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }
    
    boolean isRawPathRelative() {
        return !(new File(rawPath).isAbsolute());
    }
    
    public String toString() {
        return type + " = \"" + rawPath + "\"" +
                " (absolutePath: " + absolutePath + ")";
    }
}

