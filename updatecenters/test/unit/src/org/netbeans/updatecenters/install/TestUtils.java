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
package org.netbeans.updatecenters.install;

import java.io.File;
import org.netbeans.ModuleManager;
import org.netbeans.core.startup.Main;
import org.netbeans.spi.autoupdate.UpdateItem;

/**
 *
 * @author Jaromir Uhrik
 */
public class TestUtils {

    private static UpdateItem item = null;
    private static ModuleManager mgr = null;

    public static void setUserDir(String path) {
        System.setProperty("netbeans.user", path);
    }

    public static File getPlatformDir() {
        return new File(System.getProperty("netbeans.home")); // NOI18N
    }

    public static void setPlatformDir(String path) {
        System.setProperty("netbeans.home", path);
    }

    public static void testInit() {
        mgr = Main.getModuleSystem().getManager();
        assert mgr != null;
    }
}
