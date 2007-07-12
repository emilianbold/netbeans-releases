/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.utils.system.launchers;

import java.io.File;
import org.netbeans.installer.utils.StringUtils;

/**
 *
 * @author Dmitry Lipin
 */
public class LauncherResource {
    
    public static enum Type {
        BUNDLED,
        ABSOLUTE,
        RELATIVE_JAVAHOME,
        RELATIVE_USERHOME,
        RELATIVE_LAUNCHER_PARENT,
        RELATIVE_LAUNCHER_TMPDIR;
        
        public long toLong() {
            switch (this) {
                case BUNDLED : return 0L;
                case ABSOLUTE: return 1L;
                case RELATIVE_JAVAHOME: return 2L;
                case RELATIVE_USERHOME: return 3L;
                case RELATIVE_LAUNCHER_PARENT: return 4L;
                case RELATIVE_LAUNCHER_TMPDIR: return 5L;
            }
            return 1L;
        }

        public String toString() {
            switch(this) {
                case BUNDLED:
                    return "nbi.launcher.tmp.dir";
                case ABSOLUTE:
                    return StringUtils.EMPTY_STRING;
                case RELATIVE_JAVAHOME:
                    return "nbi.launcher.java.home";
                case RELATIVE_USERHOME:
                    return "nbi.launcher.user.home";
                case RELATIVE_LAUNCHER_PARENT :
                    return "nbi.launcher.parent.dir";
                case RELATIVE_LAUNCHER_TMPDIR:
                    return "nbi.launcher.tmp.dir";
                default:
                    return null;
                    
            }
        }

    };
    
    private Type  type;
    private String path;
    
    /**
     * Bundled launcher file
     */
    public LauncherResource(File file) {
        this(true,file);
    }
    public LauncherResource(boolean bundled, File file) {
        this.type= (bundled) ? Type.BUNDLED : Type.ABSOLUTE;
        this.path= file.getPath();
    }
    /**
     * External or bundled launcher file
     */
    public LauncherResource(Type type, String path) {
        this.type=type;
        this.path=path;
    }
    
    public Type getPathType() {
        return type;
    }
    public boolean isBundled() {
        return type.equals(Type.BUNDLED);
    }
    public String getPath() {
        return path;
    }
    
    
    public String getAbsolutePath() {
        if(type.equals(Type.ABSOLUTE)) {
            return path;
        } else if (type.equals(Type.BUNDLED)) {
            return "$L{" + Type.BUNDLED.toString()+ "}/" + 
                    new File(path).getName();
        } else {
            return "$L{" + type.toString() + "}/" + path;
        }
    }
}
