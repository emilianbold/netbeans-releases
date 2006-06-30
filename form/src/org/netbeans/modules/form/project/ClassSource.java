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

package org.netbeans.modules.form.project;

/**
 * Describes a source (i.e. classpath) of a component class to be used in form
 * editor.
 *
 * @author Tomas Pavek
 */

public class ClassSource {

    // classpath source types
    public static final String JAR_SOURCE = "jar"; // NOI18N
    public static final String LIBRARY_SOURCE = "library"; // NOI18N
    public static final String PROJECT_SOURCE = "project"; // NOI18N

    private String className;
    private String[] cpTypes;
    private String[] cpRoots;

    /**
     * @param className name of the class, can be null
     * @param cpTypes types of classpath entries in cpRoots (see constants above)
     * @param cpRoots names of classpath roots
     */
    public ClassSource(String className, String[] cpTypes, String[] cpRoots) {
        this.className = className;
        this.cpTypes = cpTypes;
        this.cpRoots = cpRoots;
    }

    public String getClassName() {
        return className;
    }

    public int getCPRootCount() {
        return cpTypes != null ? cpTypes.length : 0;
    }

    public String getCPRootType(int index) {
        return cpTypes[index];
    }

    public String getCPRootName(int index) {
        return cpRoots[index];
    }

}
