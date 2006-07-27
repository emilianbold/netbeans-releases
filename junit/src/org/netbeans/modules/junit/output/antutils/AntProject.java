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

package org.netbeans.modules.junit.output.antutils;

import java.io.File;
import org.apache.tools.ant.module.spi.AntEvent;
import org.netbeans.modules.junit.output.antutils.FileUtils;

/**
 *
 * @author  Marian Petras
 */
final class AntProject {

    /** {@code AntEvent} which serves for evaluation of Ant properties */
    private final AntEvent event;
    /** project's base directory. */
    private final File baseDir;
    
    /**
     * Constructor used only in tests.
     */
    AntProject() {
        event = null;
        baseDir = null;
    }

    /**
     */
    public AntProject(AntEvent event) {
        this.event = event;
        String baseDirName = getProperty("basedir");                    //NOI18N
        if (baseDirName == null) {
            baseDirName = ".";                                          //NOI18N
        }
        baseDir = FileUtils.normalizePath(
                        new File(baseDirName).getAbsolutePath());
    }

    /**
     */
    public String getProperty(String propertyName) {
        return event.getProperty(propertyName);
    }

    /**
     */
    public String replaceProperties(String value) {
        return event.evaluate(value);
    }

    /**
     */
    public File resolveFile(String fileName) {
        return FileUtils.resolveFile(baseDir, fileName);
    }

    /**
     * Return the boolean equivalent of a string, which is considered
     * {@code true} if either {@code "on"}, {@code "true"},
     * or {@code "yes"} is found, ignoring case.
     *
     * @param  s  string to convert to a boolean value
     *
     * @return  {@code true} if the given string is {@code "on"}, {@code "true"}
     *          or {@code "yes"}; or {@ code false} otherwise.
     */
    public static boolean toBoolean(String s) {
        return ("on".equalsIgnoreCase(s)                                //NOI18N
                || "true".equalsIgnoreCase(s)                           //NOI18N
                || "yes".equalsIgnoreCase(s));                          //NOI18N
    }

}
