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
package org.netbeans.modules.visualweb.propertyeditors;

import java.io.File;
import java.util.regex.Pattern;
import javax.swing.filechooser.FileFilter;

/**
 * A file filter designed specifically for filter files within a Creator
 * project, used by the URL property editors.
 *
 * @author gjmurphy
 * @see UrlPropertyEditor
 */
public class UrlFileFilter extends FileFilter{

    String description;
    Pattern pattern;

    /**
     * Create a new file filter for the URL property editors. The filter will
     * be used to determine which files are available for selection in the URL
     * custom property editor. The regular expression provided will be matched
     * against file names only (not directory names). If the pattern matcher's
     * <code>matches(String)</code> method returns true, the file is shown.
     */
    public UrlFileFilter(String description, String regex) {
        this.description = description;
        this.pattern = Pattern.compile(regex);
    }

    public boolean accept(File file) {
        if (file.isDirectory()) {
            if (!file.getName().equals("WEB-INF")) //NOI18N
                return true;
        } else if (pattern.matcher(file.getName()).matches()) {
            return true;
        }
        return false;
    }

    public String getDescription() {
        return description;
    }

    public java.io.FileFilter getIOFileFilter() {
        return new java.io.FileFilter() {
            public boolean accept(File file) {
                return UrlFileFilter.this.accept(file);
            }
        };
    }
}
