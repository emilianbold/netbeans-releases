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

package org.netbeans.modules.web.webmodule;

import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.api.webmodule.RequestParametersQuery;
import org.netbeans.modules.web.spi.webmodule.RequestParametersQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Pavel Buzek, Andrei Badea
 */
public class RequestParametersQueryTest extends NbTestCase {

    private static final String PARAMS = "MyJsp?foo=1&bar=0";

    public RequestParametersQueryTest(String name) {
        super(name);
    }

    private FileObject datadir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(RequestParametersQueryImpl.class);
        datadir = FileUtil.toFileObject(getDataDir());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        MockServices.setServices();
    }

    public void testGetParams() throws Exception {
        FileObject foo = datadir.getFileObject("a.foo");
        FileObject bar = datadir.getFileObject("b.bar");
        String params1 = RequestParametersQuery.getFileAndParameters(foo);
        assertNotNull("found params", params1);
        String params2 = RequestParametersQuery.getFileAndParameters(bar);
        assertEquals("different parameters expected", PARAMS, params1);
        assertNull("no params expected", params2);
    }

    public static final class RequestParametersQueryImpl implements RequestParametersQueryImplementation {

        public RequestParametersQueryImpl() {}

        public String getFileAndParameters(FileObject f) {
            if (f.getNameExt().equals("a.foo")) {
                return PARAMS;
            }
            return null;
        }
    }
}
