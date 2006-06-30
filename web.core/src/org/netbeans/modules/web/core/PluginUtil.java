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

package org.netbeans.modules.web.core;

import java.io.File;

/** This class contains static utility methods, which are anticipated
 * to be used by most server plugins, as well as the core web module.
 * This is the only class from the web module that server plugins are
 * allowed to import. Plugins should not use any org.netbeans.* code except
 * for the integration APIs and this class, and likewise, they should not use
 * any org.openide.* classes.
 *
 * @author  pjiricka
 * @version
 */
public class PluginUtil {

    /** Replacement of java.io.File.mkdirs(), as that may fail 
     *  on Solaris mounted disks when invoked from NT. 
     *  Returns true if the dir exists when we finish.
     */
    public static boolean myMkdirs(File f) {
        if (f.exists()) return true;
        if (!f.isAbsolute())
            f = f.getAbsoluteFile();
        String par = f.getParent();
        if (par == null) return false;
        if (!myMkdirs(new File(par))) return false;
        f.mkdir();
        return f.exists();
    }


}
