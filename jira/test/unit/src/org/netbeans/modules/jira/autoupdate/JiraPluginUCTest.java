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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.autoupdate;

import java.io.IOException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.bugtracking.commons.AutoupdatePluginUCTestCase;
import org.netbeans.modules.bugtracking.commons.AutoupdateSupport;
import org.netbeans.modules.jira.JiraTestUtil;
import org.netbeans.modules.jira.client.spi.JiraConnectorSupport;
import org.netbeans.modules.jira.client.spi.JiraVersion;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class JiraPluginUCTest extends AutoupdatePluginUCTestCase {

    String CATALOG_CONTENTS_FORMAT =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.6//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_6.dtd\">" +
            "<module_updates timestamp=\"20/02/13/01/07/2009\">" +
            "<module autoload=\"false\" " +
                    "codenamebase=\"org.netbeans.libs.jira\" " +
                    "distribution=\"modules/extra/org-netbeans-libs-jira.nbm\" " +
                    "downloadsize=\"2546568\" " +
                    "eager=\"false\" " +
                    "homepage=\"https://netbeans.apache.org/\" " +
                    "license=\"BE94B573\" " +
                    "moduleauthor=\"\" " +
                    "needsrestart=\"false\" " +
                    "releasedate=\"2009/05/27\">" +
                "<manifest AutoUpdate-Show-In-Client=\"true\" " +
                          "OpenIDE-Module=\"org.netbeans.libs.jira\" " +
                          "OpenIDE-Module-Display-Category=\"Libraries\" " +
                          "OpenIDE-Module-Implementation-Version=\"090527\" " +
                          "OpenIDE-Module-Java-Dependencies=\"Java > 1.5\" " +
                          "OpenIDE-Module-Long-Description=\"This module bundles the JIRA connector implementation\" " +
                          "OpenIDE-Module-Module-Dependencies=\"org.jdesktop.layout/1 > 1.6, " +
                                                               "org.netbeans.libs.bugtracking > 1.0, " +
                                                               "org.netbeans.libs.commons_logging/1 > 1.7, " +
                                                               "org.openide.awt > 7.3, " +
                                                               "org.openide.dialogs > 7.8, " +
                                                               "org.openide.modules > 6.0, " +
                                                               "org.openide.nodes > 7.7, " +
                                                               "org.openide.util > 7.18, " +
                                                               "org.openide.windows > 6.24\" " +
                          "OpenIDE-Module-Name=\"JIRA Libraries\" " +
                          "OpenIDE-Module-Requires=\"org.openide.modules.ModuleFormat1\" " +
                          "OpenIDE-Module-Short-Description=\"Bundles JIRA Libraries\" " +
                          "OpenIDE-Module-Specification-Version=\"1.0.0\"/>" +
            "</module>" +
            "<module autoload=\"false\" " +
                    "codenamebase=\"{0}\" " +
                    "distribution=\"modules/extra/org-netbeans-modules-jira.nbm\" " +
                    "downloadsize=\"192657\" " +
                    "eager=\"false\" " +
                    "homepage=\"https://netbeans.apache.org/\" " +
                    "license=\"8B813426\" " +
                    "moduleauthor=\"\" " +
                    "needsrestart=\"true\" " +
                    "releasedate=\"2009/05/27\">" +
                "<manifest AutoUpdate-Show-In-Client=\"true\" " +
                          "OpenIDE-Module=\"{0}\" " +
                          "OpenIDE-Module-Implementation-Version=\"090527\" " +
                          "OpenIDE-Module-Java-Dependencies=\"Java > 1.5\" " +
                          "OpenIDE-Module-Long-Description=\"Support for JIRA issue tracker from version 1.0 up to version {1}\" " +
                          "OpenIDE-Module-Module-Dependencies=\"org.jdesktop.layout/1 > 1.6, " +
                                                               "org.netbeans.api.progress/1 > 1.13, " +
                                                               "org.netbeans.libs.bugtracking > 1.0, " +
                                                               "org.netbeans.libs.commons_logging/1 > 1.7, " +
                                                               "org.netbeans.libs.jira > 1.0, " +
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
                           "OpenIDE-Module-Name=\"JIRA\" " +
                           "OpenIDE-Module-Requires=\"org.openide.modules.ModuleFormat1\" " +
                           "OpenIDE-Module-Short-Description=\"JIRA\" " +
                           "OpenIDE-Module-Specification-Version=\"{1}\"/>" +
            "</module>" +
            "</module_updates>";

    public JiraPluginUCTest(String testName) throws IOException {
        super(testName);
    }

    public static junit.framework.Test suite() {
        return NbModuleSuite.create(JiraPluginUCTest.class, null, null);
    }
        
    public void testIsSupported() {
        JiraAutoupdate jau = JiraAutoupdate.getInstance();
        assertTrue(jau.isSupportedVersion(JiraTestUtil.getJiraConstants().getMIN_VERSION()));
        assertTrue(jau.isSupportedVersion(createJiraVersion("3.3.0")));
        assertTrue(jau.isSupportedVersion(createJiraVersion("3.3.1")));
        assertTrue(jau.isSupportedVersion(createJiraVersion("3.8.0")));
        assertTrue(jau.isSupportedVersion(getLower(JiraAutoupdate.SUPPORTED_JIRA_VERSION.toString())));
    }

    public void testIsNotSupported() {
        JiraAutoupdate jau = JiraAutoupdate.getInstance();
        assertFalse(jau.isSupportedVersion(getHigherMicro(JiraAutoupdate.SUPPORTED_JIRA_VERSION.toString())));
        assertFalse(jau.isSupportedVersion(getHigherMinor(JiraAutoupdate.SUPPORTED_JIRA_VERSION.toString())));
        assertFalse(jau.isSupportedVersion(getHigherMajor(JiraAutoupdate.SUPPORTED_JIRA_VERSION.toString())));
    }

    public void testGetVersion() {
        JiraAutoupdate jau = JiraAutoupdate.getInstance();

        assertEquals(createJiraVersion("1.1.1").toString(), jau.getVersion("test version 1.1.1 test").toString());
        assertEquals(createJiraVersion("1.1.1").toString(), jau.getVersion("test version 1.1.1 test").toString());
        assertEquals(createJiraVersion("1.1.1").toString(), jau.getVersion("test version 1.1.1").toString());
        assertEquals(createJiraVersion("1.1.1").toString(), jau.getVersion("version 1.1.1").toString());
        assertEquals(createJiraVersion("1.1").toString(), jau.getVersion("version 1.1").toString());
    }

    public void testGotVersion() {
        JiraAutoupdate jau = JiraAutoupdate.getInstance();
        String desc = NbBundle.getBundle("org/netbeans/modules/jira/Bundle").getString("OpenIDE-Module-Long-Description");
        JiraVersion version = jau.getVersion(desc);
        assertNotNull(version);
        assertEquals(JiraAutoupdate.SUPPORTED_JIRA_VERSION.toString(), version.toString());
    }
    
    private JiraVersion getHigherMicro(String version) {
        String[] segments = version == null ? new String[0] : version.split("\\."); //$NON-NLS-1$
        int major = segments.length > 0 ? toInt(segments[0]) : 0;
        int minor = segments.length > 1 ? toInt(segments[1]) : 0;
        int micro = segments.length > 2 ? toInt(segments[2]) : 0;
        return createJiraVersion(new String("" + major + "." + minor + "." + ++micro));
    }

    private JiraVersion getHigherMinor(String version) {
        String[] segments = version == null ? new String[0] : version.split("\\."); //$NON-NLS-1$
        int major = segments.length > 0 ? toInt(segments[0]) : 0;
        int minor = segments.length > 1 ? toInt(segments[1]) : 0;
        int micro = segments.length > 2 ? toInt(segments[2]) : 0;
        return createJiraVersion(new String("" + major + "." + ++minor + "." + micro));
    }

    private JiraVersion getHigherMajor(String version) {
        String[] segments = version == null ? new String[0] : version.split("\\."); //$NON-NLS-1$
        int major = segments.length > 0 ? toInt(segments[0]) : 0;
        int minor = segments.length > 1 ? toInt(segments[1]) : 0;
        int micro = segments.length > 2 ? toInt(segments[2]) : 0;
        return createJiraVersion(new String("" + ++major + "." + minor + "." + micro));
    }

    private JiraVersion getLower(String version) {
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
        return createJiraVersion(new String("" + major + "." + minor + "." + ++micro));
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

    @Override
    protected AutoupdateSupport getAutoupdateSupport() {
        return JiraAutoupdate.getInstance().getAutoupdateSupport();
    }

    @Override
    protected String getContentFormat() {
        return CATALOG_CONTENTS_FORMAT;
    }

    @Override
    protected String getCNB() {
        return JiraAutoupdate.JIRA_MODULE_CODE_NAME;
    }
    
    private JiraVersion createJiraVersion(String string) {
        return JiraConnectorSupport.getInstance().getConnector().createJiraVersion(string);
    }
}
