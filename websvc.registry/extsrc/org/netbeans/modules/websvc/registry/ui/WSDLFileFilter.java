/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.ui;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * Filter file selection so only files with extention WSDL are shown
 */
public class WSDLFileFilter extends FileFilter {

        public final static String wsdl = "wsdl"; // NOI18N

        /** Allow directories
         */
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                if (extension.equals(wsdl)) {
                    return true;
                } else {
                    return false;
                }
            }

            return false;
        }

        public String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }
            return ext;
        }

        /** The description of this filter */
        public String getDescription() {
            return "Web Services Descriptor File (.wsdl)";
        }
    }