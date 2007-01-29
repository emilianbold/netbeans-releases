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
package org.netbeans.modules.visualweb.web.ui.dt.component.customizers;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

/* ImageFilter.java is a 1.4 example used by FileChooserDemo2.java. */
public class ImageFilter extends FileFilter {

    //Accept all directories and all gif, jpg, or png files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = Utils.getExtension(f);
        if (extension != null) {
            if (extension.equals(Utils.gif) ||
                    extension.equals(Utils.jpg) ||
                    extension.equals(Utils.jpe) ||
                    extension.equals(Utils.png) ||
                    extension.equals(Utils.jpeg)) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    //The description of this filter
    public String getDescription() {
        //"All Image Files (.gif, .jpg, .png, .jpe, .jpeg)";
        return java.util.ResourceBundle.getBundle("com/sun/rave/web/ui/dt/component/customizers/Bundle-DT").getString("imageFilterLabel"); // NOI18N

    }
}

