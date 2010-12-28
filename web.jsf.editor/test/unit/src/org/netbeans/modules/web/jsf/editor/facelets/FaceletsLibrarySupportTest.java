/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.editor.facelets;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.web.jsf.editor.JsfSupportImpl;
import org.netbeans.modules.web.jsf.editor.TestBaseForTestProject;
import org.netbeans.modules.web.jsf.editor.index.JsfCustomIndexer;
import org.netbeans.modules.web.jsf.editor.index.JsfIndexer;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.JsfSupport;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.LibraryType;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.netbeans.modules.web.jsfapi.spi.LibraryUtils;

/**
 *
 * @author marekfukala
 */
public class FaceletsLibrarySupportTest extends TestBaseForTestProject {

    public FaceletsLibrarySupportTest(String name) {
        super(name);
    }

    public static Test xsuite() {
        TestSuite suite = new TestSuite();
//        suite.addTest(new FaceletsLibrarySupportTest("testCompositeComponentLibraryWithoutDescriptorFromLibraryProject"));
//        suite.addTest(new FaceletsLibrarySupportTest("testCompositeComponentLibraryWithDescriptorFromLibraryProject"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        JsfCustomIndexer.LOGGER.setLevel(Level.FINE);
        JsfIndexer.LOG.setLevel(Level.FINE);

        Handler h = new ConsoleHandler();
        h.setLevel(Level.FINE);
        JsfIndexer.LOG.addHandler(h);
        JsfCustomIndexer.LOGGER.addHandler(h);
        super.setUp();
    }

    public void testCompositeComponentLibraryWithoutDescriptor() {
        JsfSupportImpl instance = getJsfSupportImpl();

        String ezCompLibraryNS = LibraryUtils.getCompositeLibraryURL("ezcomp");

        Library ezcompLib = instance.getLibrary(ezCompLibraryNS);
        assertNotNull(String.format("Library %s not found!", ezCompLibraryNS), ezcompLib);

        assertNotNull(ezcompLib.getLibraryDescriptor());
        assertEquals("ezcomp", ezcompLib.getDefaultPrefix());
        assertSame(LibraryType.COMPOSITE, ezcompLib.getType());

        assertEquals(ezCompLibraryNS, ezcompLib.getDefaultNamespace());
        assertEquals(ezCompLibraryNS, ezcompLib.getNamespace());
        Tag t = ezcompLib.getTag("test");
        assertNotNull(t);

        assertEquals("test", t.getName());
        Attribute a = t.getAttribute("testAttr");
        assertNotNull(a);
        assertEquals("testAttr", a.getName());

    }

    public void testCompositeComponentLibraryWithDescriptor() {
        JsfSupportImpl instance = getJsfSupportImpl();

        String ezCompLibraryNS = "http://ezcomp.com/jsflib";

        Library ezcompLib = instance.getLibrary(ezCompLibraryNS);
        assertNotNull(String.format("Library %s not found!", ezCompLibraryNS), ezcompLib);

        assertNotNull(ezcompLib.getLibraryDescriptor());
        assertEquals("ezcomp2", ezcompLib.getDefaultPrefix());
        assertSame(LibraryType.COMPOSITE, ezcompLib.getType());

        String ezCompLibraryDefaultNS = LibraryUtils.getCompositeLibraryURL("ezcomp2");
        assertEquals(ezCompLibraryDefaultNS, ezcompLib.getDefaultNamespace());
        assertEquals(ezCompLibraryNS, ezcompLib.getNamespace());
        Tag t = ezcompLib.getTag("test");
        assertNotNull(t);

        assertEquals("test", t.getName());
        Attribute a = t.getAttribute("testAttr");
        assertNotNull(a);
        assertEquals("testAttr", a.getName());

    }

    public void testClassBaseLibraryWithinCurrentProject() {
        JsfSupportImpl instance = getJsfSupportImpl();

        String libNs = "http://mysite.org/classTaglib";

        Library lib = instance.getLibrary(libNs);
        assertNotNull(String.format("Library %s not found!", libNs), lib);

        assertNotNull(lib.getLibraryDescriptor());
        assertEquals("moc", lib.getDefaultPrefix());
        assertSame(LibraryType.CLASS, lib.getType());

        assertEquals(libNs, lib.getNamespace());
        Tag t = lib.getTag("mytag");
        assertNotNull(t);

        assertEquals("mytag", t.getName());
        assertNotNull(t.getDescription());

        Attribute a = t.getAttribute("myattr");
        assertNotNull(a);
        assertEquals("myattr", a.getName());
        assertNotNull(a.getDescription());

    }

    public void testClassBaseLibraryFromLibraryProject() {
        JsfSupportImpl instance = getJsfSupportImpl();

        String libNs = "http://mysite.org/classTaglibIJL";

        Library lib = instance.getLibrary(libNs);
        assertNotNull(String.format("Library %s not found!", libNs), lib);

        assertNotNull(lib.getLibraryDescriptor());
        assertEquals("moc", lib.getDefaultPrefix());
        assertSame(LibraryType.CLASS, lib.getType());

        assertEquals(libNs, lib.getNamespace());
        Tag t = lib.getTag("mytag");
        assertNotNull(t);

        assertEquals("mytag", t.getName());
        assertNotNull(t.getDescription());

        Attribute a = t.getAttribute("myattr");
        assertNotNull(a);
        assertEquals("myattr", a.getName());
        assertNotNull(a.getDescription());

    }

    public void testCompositeComponentLibraryWithoutDescriptorFromLibraryProject() {
        JsfSupportImpl instance = getJsfSupportImpl();

//        debugLibraries(instance);

        String libNs = LibraryUtils.getCompositeLibraryURL("cclib");

        Library lib = instance.getLibrary(libNs);
        assertNotNull(String.format("Library %s not found!", libNs), lib);

        assertNotNull(lib.getLibraryDescriptor());
        assertEquals("cclib", lib.getDefaultPrefix());
        assertSame(LibraryType.COMPOSITE, lib.getType());

        assertEquals(libNs, lib.getDefaultNamespace());
        assertEquals(libNs, lib.getNamespace());
        Tag t = lib.getTag("cc");
        assertNotNull(t);

        assertEquals("cc", t.getName());
        Attribute a = t.getAttribute("ccattr");
        assertNotNull(a);
        assertEquals("ccattr", a.getName());

    }

    public void testCompositeComponentLibraryWithDescriptorFromLibraryProject() {
        JsfSupportImpl instance = getJsfSupportImpl();

        String libNs = "http://mysite.org/cclib2";

        Library lib = instance.getLibrary(libNs);
        assertNotNull(String.format("Library %s not found!", libNs), lib);

        assertNotNull(lib.getLibraryDescriptor());
        assertEquals("cclib2", lib.getDefaultPrefix());
        assertSame(LibraryType.COMPOSITE, lib.getType());

        String ezCompLibraryDefaultNS = LibraryUtils.getCompositeLibraryURL("cclib2");
        assertEquals(ezCompLibraryDefaultNS, lib.getDefaultNamespace());
        assertEquals(libNs, lib.getNamespace());
        Tag t = lib.getTag("cc2");
        assertNotNull(t);

        assertEquals("cc2", t.getName());
        Attribute a = t.getAttribute("ccattr2");
        assertNotNull(a);
        assertEquals("ccattr2", a.getName());

    }

    private void debugLibraries(JsfSupport jsfs) {
        System.out.println("Found libraries:");
        for(Library lib : jsfs.getLibraries().values()) {
            System.out.println(lib.getNamespace());
        }
        System.out.println("-------------------");
    }


}