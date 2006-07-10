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
package org.netbeans.modules.subversion.client.parser;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import org.netbeans.modules.subversion.client.*;

/**
 *
 * @author Ed Hillmann
 */
public class SvnWcUtils {

    public static final String[] ADMIN_DIR_NAMES = new String[] {".svn", "_svn" };

    private static final String PROPS = "props";
    private static final String PROPS_BASE = "prop-base";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
        
    public static File getSvnFile(File file, String svnFileName) throws IOException {
        for (int i = 0; i < ADMIN_DIR_NAMES.length; i++) {            
            File svnFile = new File(file, ADMIN_DIR_NAMES[i] + "/" + svnFileName);
            if(svnFile.canRead()) {
                return svnFile;
            };
        }
        return null;                
    }
    
    public static File getPropertiesFile(File file, boolean base) throws IOException {

        if(file.isFile()) {            
            if (base) {
                return getSvnFile(file.getParentFile(), PROPS_BASE + "/" + file.getName() + getPropFileNameSuffix(base));
            } else {
                return getSvnFile(file.getParentFile(), PROPS + "/" + file.getName() + getPropFileNameSuffix(base));
            }            
        } else {            
            return getSvnFile(file, base ? "/dir-props-base" : "/dir-prop");
        }        
    }

    private static String getPropFileNameSuffix(boolean base) {
        if (base) {
            return ".svn-base";
        } else {
            return ".svn-work";
        }        
    }
    
    public static File getTextBaseFile(File file) throws IOException {
        return getSvnFile(file.getParentFile(), "text-base/" + file.getName() + ".svn-base");
    }

    public static Date parseSvnDate(String inputValue) throws ParseException {
        Date returnValue = null;
        if (inputValue != null) {
            returnValue = dateFormat.parse(inputValue);
        }
        return returnValue;        
    }
    
}
