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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.netbeans.lib.uihandler;

import java.util.logging.LogRecord;

/** Represents an operation on the list of opened projects.
 *
 * @author Jaroslav Tulach
 * @since 1.7
 */
public final class ProjectOp {
    private final String name;
    private final String type;
    private final int number;

    private ProjectOp(String name, String type, int number) {
        this.name = name;
        this.type = type;
        this.number = number;
    }
    
    /** Human readable name of the project the operation happened on
     */
    public String getProjectDisplayName() {
        return name;
    }

    /** Fully qualified class name of the project.
     */
    public String getProjectType() {
        return type;
    }
    
    /** Number of projects of this type that has been added.
     * @return positive value if some projects were open, negative if some were closed
     */
    public int getDelta() {
        return number;
    }
    
    /** Finds whether the record was an operation on projects.
     * @param rec the record to test
     * @return null if the record is of unknown format or data about the project operation
     */
    public static ProjectOp valueOf(LogRecord rec) {
        if ("UI_CLOSED_PROJECTS".equals(rec.getMessage())) {
            String type = getStringParam(rec, 0, "unknown"); // NOI18N
            String name = getStringParam(rec, 1, "unknown"); // NOI18N
            int cnt = Integer.parseInt(getStringParam(rec, 2, "0"));
            return new ProjectOp(name, type, -cnt);
        }
        if ("UI_OPEN_PROJECTS".equals(rec.getMessage())) {
            String type = getStringParam(rec, 0, "unknown"); // NOI18N
            String name = getStringParam(rec, 1, "unknown"); // NOI18N
            int cnt = Integer.parseInt(getStringParam(rec, 2, "0"));
            return new ProjectOp(name, type, cnt);
        }
        return null;
    }
    
    private static String getStringParam(LogRecord rec, int index, String def) {
        if (rec == null) {
            return def;
        }
        Object[] params = rec.getParameters();
        if (params == null || params.length <= index) {
            return def;
        }
        if (params[index] instanceof String) {
            return (String)params[index];
        }
        return def;
    }
}
