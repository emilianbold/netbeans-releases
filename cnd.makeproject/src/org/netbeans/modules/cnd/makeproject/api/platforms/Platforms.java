/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.api.platforms;

import java.util.Enumeration;
import java.util.Vector;
import org.openide.util.Utilities;

public class Platforms {
    private static Vector platforms;

    public static void checkInitialized() {
        if (platforms == null) {
            platforms = new Vector();
            platforms.add(new PlatformSolarisSparc());
            platforms.add(new PlatformSolarisIntel());
            platforms.add(new PlatformLinux());
            platforms.add(new PlatformWindows());
            platforms.add(new PlatformGeneric());
        }
    }
    
    public static Platform getPlatform(String name) {
        checkInitialized();
        Enumeration e = platforms.elements();
        for (; e.hasMoreElements(); ) {
            Platform pl = (Platform)e.nextElement();
            if (pl.getName().equals(name))
                return pl;
        }
        return null;
    }
    
    public static Platform getPlatform(int id) {
        checkInitialized();
        Enumeration e = platforms.elements();
        for (; e.hasMoreElements(); ) {
            Platform pl = (Platform)e.nextElement();
            if (pl.getId() == id)
                return pl;
        }
        return null;
    }
    
    public static String[] getPlatformDisplayNames() {
        checkInitialized();
        String[] ret = new String[platforms.size()];
        Enumeration e = platforms.elements();
        int index = 0;
        for (; e.hasMoreElements(); ) {
            Platform cs = (Platform)e.nextElement();
            ret[index++] = cs.getDisplayName();
        }
        return ret;
    }
}
