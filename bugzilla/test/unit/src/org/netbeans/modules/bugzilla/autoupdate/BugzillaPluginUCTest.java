/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.autoupdate;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Calendar;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaVersion;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author tomas
 */
public class BugzillaPluginUCTest extends BugzillaPluginUCTestCase {

    String CATALOG_CONTENTS_FORMAT =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.6//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_6.dtd\">" +
            "<module_updates timestamp=\"20/02/13/01/07/2009\">" +
            "<module autoload=\"false\" " +
                    "codenamebase=\"org.netbeans.libs.bugzilla\" " +
                    "distribution=\"modules/extra/org-netbeans-libs-bugzilla.nbm\" " +
                    "downloadsize=\"2546568\" " +
                    "eager=\"false\" " +
                    "homepage=\"http://www.netbeans.org/\" " +
                    "license=\"BE94B573\" " +
                    "moduleauthor=\"\" " +
                    "needsrestart=\"false\" " +
                    "releasedate=\"2009/05/27\">" +
                "<manifest AutoUpdate-Show-In-Client=\"true\" " +
                          "OpenIDE-Module=\"org.netbeans.libs.bugzilla\" " +
                          "OpenIDE-Module-Display-Category=\"Libraries\" " +
                          "OpenIDE-Module-Implementation-Version=\"090527\" " +
                          "OpenIDE-Module-Java-Dependencies=\"Java > 1.5\" " +
                          "OpenIDE-Module-Long-Description=\"This module bundles the Bugzilla connector implementation\" " +
                          "OpenIDE-Module-Module-Dependencies=\"org.jdesktop.layout/1 > 1.6, " +
                                                               "org.netbeans.libs.bugtracking > 1.0, " +
                                                               "org.netbeans.libs.commons_logging/1 > 1.7, " +
                                                               "org.openide.awt > 7.3, " +
                                                               "org.openide.dialogs > 7.8, " +
                                                               "org.openide.modules > 6.0, " +
                                                               "org.openide.nodes > 7.7, " +
                                                               "org.openide.util > 7.18, " +
                                                               "org.openide.windows > 6.24\" " +
                          "OpenIDE-Module-Name=\"Bugzilla Libraries\" " +
                          "OpenIDE-Module-Requires=\"org.openide.modules.ModuleFormat1\" " +
                          "OpenIDE-Module-Short-Description=\"Bundles Bugzilla Libraries\" " +
                          "OpenIDE-Module-Specification-Version=\"1.0.0\"/>" +
            "</module>" +
            "<module autoload=\"false\" " +
                    "codenamebase=\"{0}\" " +
                    "distribution=\"modules/extra/org-netbeans-modules-bugzilla.nbm\" " +
                    "downloadsize=\"192657\" " +
                    "eager=\"false\" " +
                    "homepage=\"http://www.netbeans.org/\" " +
                    "license=\"8B813426\" " +
                    "moduleauthor=\"\" " +
                    "needsrestart=\"true\" " +
                    "releasedate=\"2009/05/27\">" +
                "<manifest AutoUpdate-Show-In-Client=\"true\" " +
                          "OpenIDE-Module=\"{0}\" " +
                          "OpenIDE-Module-Implementation-Version=\"090527\" " +
                          "OpenIDE-Module-Java-Dependencies=\"Java > 1.5\" " +
                          "OpenIDE-Module-Long-Description=\"Bugzilla Support to version 3.2.3 \" " +
                          "OpenIDE-Module-Module-Dependencies=\"org.jdesktop.layout/1 > 1.6, " +
                                                               "org.netbeans.api.progress/1 > 1.13, " +
                                                               "org.netbeans.libs.bugtracking > 1.0, " +
                                                               "org.netbeans.libs.commons_logging/1 > 1.7, " +
                                                               "org.netbeans.libs.bugzilla > 1.0, " +
                                                               "org.netbeans.modules.bugtracking > 1.0, " +
                                                               "org.netbeans.modules.kenai > 0.1, " +
                                                               "org.openide.awt > 7.3, " +
                                                               "org.openide.dialogs > 7.8, " +
                                                               "org.openide.filesystems > 7.21, " +
                                                               "org.openide.loaders > 7.5, " +
                                                               "org.openide.modules > 6.0, " +
                                                               "org.openide.nodes > 7.7, " +
                                                               "org.openide.util > 7.18, " +
                                                               "org.openide.windows > 6.24\" " +
                           "OpenIDE-Module-Name=\"Bugzilla\" " +
                           "OpenIDE-Module-Requires=\"org.openide.modules.ModuleFormat1\" " +
                           "OpenIDE-Module-Short-Description=\"Bugzilla\" " +
                           "OpenIDE-Module-Specification-Version=\"{1}\"/>" +
            "</module>" +
            "</module_updates>";

    public BugzillaPluginUCTest(String testName) {
        super(testName);
    }

    public void testNewBugzillavailable() throws Throwable {
        String contents = MessageFormat.format(CATALOG_CONTENTS_FORMAT, BugzillaAutoupdate.BUGZILLA_MODULE_CODE_NAME, "9.9.9");
        populateCatalog(contents);

        BugzillaAutoupdate jau = new BugzillaAutoupdate();
        assertTrue(jau.checkNewBugzillaPluginAvailable());
    }

    public void testNewBugzillaNotAvailable() throws Throwable {
        String contents = MessageFormat.format(CATALOG_CONTENTS_FORMAT, BugzillaAutoupdate.BUGZILLA_MODULE_CODE_NAME, "0.0.0");
        populateCatalog(contents);

        BugzillaAutoupdate jau = new BugzillaAutoupdate();
        assertFalse(jau.checkNewBugzillaPluginAvailable());
    }

    public void testBugzillaIsNotAtUCAvailable() throws Throwable {
        String contents = MessageFormat.format(CATALOG_CONTENTS_FORMAT, "org.netbeans.modules.ketchup", "1.0.0");
        populateCatalog(contents);

        BugzillaAutoupdate jau = new BugzillaAutoupdate();
        assertFalse(jau.checkNewBugzillaPluginAvailable());
    }

    public void testIsSupported() {
        BugzillaAutoupdate jau = new BugzillaAutoupdate();
        assertTrue(jau.isSupportedVersion(BugzillaVersion.MIN_VERSION));
        assertTrue(jau.isSupportedVersion(BugzillaVersion.BUGZILLA_3_2));
        assertTrue(jau.isSupportedVersion(new BugzillaVersion("3.2.1")));
        assertTrue(jau.isSupportedVersion(getLower(BugzillaAutoupdate.SUPPORTED_BUGZILLA_VERSION.toString())));
    }

    public void testIsNotSupported() {
        BugzillaAutoupdate jau = new BugzillaAutoupdate();
        assertFalse(jau.isSupportedVersion(getHigherMicro(BugzillaAutoupdate.SUPPORTED_BUGZILLA_VERSION.toString())));
        assertFalse(jau.isSupportedVersion(getHigherMinor(BugzillaAutoupdate.SUPPORTED_BUGZILLA_VERSION.toString())));
        assertFalse(jau.isSupportedVersion(getHigherMajor(BugzillaAutoupdate.SUPPORTED_BUGZILLA_VERSION.toString())));
    }

    public void testCheckedToday() {

        BugzillaAutoupdate jau = new BugzillaAutoupdate();

        assertFalse(jau.wasCheckedToday(-1));                           // never

        assertFalse(jau.wasCheckedToday(1L));                           // a long long time ago

        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, -24);                                      // yesterday
        assertFalse(jau.wasCheckedToday(c.getTime().getTime()));

        assertTrue(jau.wasCheckedToday(System.currentTimeMillis()));    // now
    }

    public void testGetVersion() {
        BugzillaAutoupdate jau = new BugzillaAutoupdate();

        assertEquals(new BugzillaVersion("1.1.1").toString(), jau.getVersion("test version 1.1.1 test").toString());
        assertEquals(new BugzillaVersion("1.1.1").toString(), jau.getVersion("test version 1.1.1 test").toString());
        assertEquals(new BugzillaVersion("1.1.1").toString(), jau.getVersion("test version 1.1.1").toString());
        assertEquals(new BugzillaVersion("1.1.1").toString(), jau.getVersion("version 1.1.1").toString());
        assertEquals(new BugzillaVersion("1.1").toString(), jau.getVersion("version 1.1").toString());
    }
    
    public void testGotVersion() {
        BugzillaAutoupdate jau = new BugzillaAutoupdate();
        String desc = NbBundle.getBundle("org/netbeans/modules/bugzilla/Bundle").getString("OpenIDE-Module-Long-Description");
        BugzillaVersion version = jau.getVersion(desc);
        assertNotNull(version);
        assertEquals(BugzillaAutoupdate.SUPPORTED_BUGZILLA_VERSION.toString(), version.toString());
    }

    private void populateCatalog(String contents) throws FileNotFoundException, IOException {
        OutputStream os = new FileOutputStream(catalogFile);
        try {
            os.write(contents.getBytes());
        } finally {
            os.close();
        }
        UpdateUnitProviderFactory.getDefault().refreshProviders (null, true);
    }

    private BugzillaVersion getHigherMicro(String version) {
        String[] segments = version == null ? new String[0] : version.split("\\."); //$NON-NLS-1$
        int major = segments.length > 0 ? toInt(segments[0]) : 0;
        int minor = segments.length > 1 ? toInt(segments[1]) : 0;
        int micro = segments.length > 2 ? toInt(segments[2]) : 0;
        return new BugzillaVersion(new String("" + major + "." + minor + "." + ++micro));
    }

    private BugzillaVersion getHigherMinor(String version) {
        String[] segments = version == null ? new String[0] : version.split("\\."); //$NON-NLS-1$
        int major = segments.length > 0 ? toInt(segments[0]) : 0;
        int minor = segments.length > 1 ? toInt(segments[1]) : 0;
        int micro = segments.length > 2 ? toInt(segments[2]) : 0;
        return new BugzillaVersion(new String("" + major + "." + ++minor + "." + micro));
    }

    private BugzillaVersion getHigherMajor(String version) {
        String[] segments = version == null ? new String[0] : version.split("\\."); //$NON-NLS-1$
        int major = segments.length > 0 ? toInt(segments[0]) : 0;
        int minor = segments.length > 1 ? toInt(segments[1]) : 0;
        int micro = segments.length > 2 ? toInt(segments[2]) : 0;
        return new BugzillaVersion(new String("" + ++major + "." + minor + "." + micro));
    }

    private BugzillaVersion getLower(String version) {
        String[] segments = version == null ? new String[0] : version.split("\\."); //$NON-NLS-1$
        int major = segments.length > 0 ? toInt(segments[0]) : 0;
        int minor = segments.length > 1 ? toInt(segments[1]) : 0;
        int micro = segments.length > 2 ? toInt(segments[2]) : 0;
        if(micro > 0) {
            micro--;
        } else {
            if(minor > 0) {
                minor--;
            } else {
                major--;
            }
        }
        return new BugzillaVersion(new String("" + major + "." + minor + "." + ++micro));
    }

    private int toInt(String segment) {
        try {
            return segment.length() == 0 ? 0 : Integer.parseInt(getVersion(segment));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getVersion(String segment) {
        int n = segment.indexOf('-');
        return n == -1 ? segment : segment.substring(0, n);
    }

}
