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

    static final Type TYPE_OUTPUT = new Type("output"); // NOI18N
    static final Type TYPE_LIBRARY = new Type("lib"); // NOI18N
    static final Type TYPE_EXTERNAL_LIBRARY = new Type("lib-ext"); // NOI18N
    static final Type TYPE_CONTAINER = new Type("con"); // NOI18N
    static final Type TYPE_VARIABLE= new Type("var"); // NOI18N
    static final Type TYPE_SOURCE = new Type("src"); // NOI18N
    static final Type TYPE_PROJECT = new Type("src-prj"); // NOI18N
    static final Type TYPE_LINK = new Type("src-link"); // NOI18N
    static final Type TYPE_UNKNOWN = new Type("unkown"); // NOI18N
    
    private Type type;
    private String rawPath;
    private String absolutePath;
    
    ClassPathEntry(String type, String rawPath) {
        this.rawPath = rawPath;
        setTypeFromRawtype(type);
    }

    void setType(ClassPathEntry.Type type) {
        this.type = type;
    }
    
    private void setTypeFromRawtype(String rawType) {
        if ("output".equals(rawType)) { // NOI18N
            this.type = TYPE_OUTPUT;
        } else if ("src".equals(rawType)) { // NOI18N
            // raw path for project entries starts with slash (on all platforms)
            if (rawPath.startsWith("/")) { // NOI18N
                this.type = TYPE_PROJECT;
            } else {
                this.type = TYPE_SOURCE;
            }
        } else if ("lib".equals(rawType)) { // NOI18N
            if (isRawPathRelative()) {
                this.type = TYPE_LIBRARY;
            } else {
                this.type = TYPE_EXTERNAL_LIBRARY;
            }
        } else if ("con".equals(rawType)) { // NOI18N
            this.type = TYPE_CONTAINER;
        } else if ("var".equals(rawType)) { // NOI18N
            this.type = TYPE_VARIABLE;
        } else {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Unkown type encountered in " + // NOI18N
                    "ClassPathEntry.setTypeFromRawtype(): " + rawType); // NOI18N
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
        return type + " = \"" + rawPath + "\"" + // NOI18N
                " (absolutePath: " + absolutePath + ")"; // NOI18N
    }
}

