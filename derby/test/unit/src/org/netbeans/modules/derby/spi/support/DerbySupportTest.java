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

package org.netbeans.modules.derby.spi.support;

import junit.framework.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.RegisterDerby;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class DerbySupportTest extends TestCase {

    public DerbySupportTest(String testName) {
        super(testName);
    }

    public void testDefaultSystemHomeWhenNDSHPropertySetIssue76908() {
        // returning .netbeans-derby when netbeans.derby.system.home is not set...
        String defaultSystemHome = new File(System.getProperty("user.home"), ".netbeans-derby").getAbsolutePath();
        assertEquals(defaultSystemHome, DerbySupport.getDefaultSystemHome());

        // ... but returning it when it is
        System.setProperty(DerbyOptions.NETBEANS_DERBY_SYSTEM_HOME, "foo");
        assertEquals("foo", DerbySupport.getDefaultSystemHome());
    }
}
