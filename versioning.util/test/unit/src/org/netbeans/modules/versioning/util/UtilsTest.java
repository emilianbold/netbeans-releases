/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.versioning.util;

import java.io.File;
import java.lang.reflect.Field;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.spi.VersioningSupport;

/**
 *
 * @author ondra
 */
public class UtilsTest extends NbTestCase {

    public UtilsTest (String name) {
        super(name);
    }
    
    public void testUnversionedFolders () throws Exception {
        File a = new File(getWorkDir(), "a");
        File b = new File(getWorkDir(), "b");
        System.setProperty("versioning.unversionedFolders", a.getAbsolutePath() + ";" + b.getAbsolutePath() + ";");
        File c = new File(getWorkDir(), "c");
        VersioningSupport.getPreferences().put("unversionedFolders", c.getAbsolutePath()); //NOI18N
        File userdir = new File(getWorkDir(), "userdir");
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        assertTrue(Utils.isScanForbidden(a));
        assertTrue(Utils.isScanForbidden(b));
        assertTrue(Utils.isScanForbidden(c));
        assertTrue(Utils.isScanForbidden(userdir));
        assertTrue(Utils.isScanForbidden(new File(userdir, "ffff")));
        assertFalse(Utils.isScanForbidden(userdir.getParentFile()));

        Field f = Utils.class.getDeclaredField("unversionedFolders");
        f.setAccessible(true);
        assertEquals(4, ((File[]) f.get(Utils.class)).length);

        System.setProperty("versioning.netbeans.user.versioned", "true");
        
        f.set(Utils.class, (File[]) null);
        
        assertTrue(Utils.isScanForbidden(a));
        assertTrue(Utils.isScanForbidden(b));
        assertTrue(Utils.isScanForbidden(c));
        assertFalse(Utils.isScanForbidden(userdir));
        assertFalse(Utils.isScanForbidden(new File(userdir, "ffff")));
        assertFalse(Utils.isScanForbidden(userdir.getParentFile()));

        f.setAccessible(true);
        assertEquals(3, ((File[]) f.get(Utils.class)).length);
    }
}
