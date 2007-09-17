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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Pavel Buzek, Andrei Badea
 */
public class WebModuleTest extends NbTestCase {

    private FileObject datadir;

    public WebModuleTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(WebModuleProviderImpl.class);
        datadir = FileUtil.toFileObject(getDataDir());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        MockServices.setServices();
    }

    public void testProviders() throws Exception {
        Collection<? extends WebModuleProvider> providers = Lookup.getDefault().lookupAll(WebModuleProvider.class);
        assertEquals("there should be 2 instances - one from web/webapi and one from tests", 2, providers.size());
    }

    public void testGetWebModule() throws Exception {
        FileObject foo = datadir.getFileObject("a.foo");
        FileObject bar = datadir.getFileObject("b.bar");
        WebModule wm1 = WebModule.getWebModule(foo);
        assertNotNull("found web module", wm1);
        WebModule wm2 = WebModule.getWebModule(bar);
        assertNull("no web module", wm2);
    }

    public static final class WebModuleProviderImpl implements WebModuleProvider {

        private final Map<FileObject, WebModule> cache = new HashMap<FileObject, WebModule>();

        public WebModuleProviderImpl() {}

        public WebModule findWebModule(FileObject file) {
            if (file.getExt().equals("foo")) {
                WebModule wm = cache.get(file.getParent());
                if (wm == null) {
                    wm = WebModuleFactory.createWebModule(new SimpleWebModuleImpl());
                    cache.put(file.getParent(), wm);
                }
                return wm;
            }
            return null;
        }
    }
}
