/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.javacard.card;

import java.awt.EventQueue;
import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import org.netbeans.modules.javacard.Utils;
import org.netbeans.modules.javacard.api.CardInstanceFactory;
import org.netbeans.modules.javacard.api.JavacardPlatform;
import org.netbeans.modules.javacard.api.PlatformKind;
import org.netbeans.modules.javacard.constants.JavacardDeviceKeyNames;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of CardInstanceFactory for the Reference Implementation
 *
 * @author Tim Boudreau
 */
@ServiceProvider(service = CardInstanceFactory.class)
@PlatformKind(kind = "RI") //NOI18N
public final class RIFactory implements CardInstanceFactory<ReferenceImplementation> {

    public ReferenceImplementation create(JavacardPlatform platform, Properties properties) {
        String id = properties.getProperty(JavacardDeviceKeyNames.DEVICE_DISPLAY_NAME);
        String displayName = properties.getProperty(JavacardDeviceKeyNames.DEVICE_DISPLAY_NAME);
        String username = ""; //PENDING - not supported yet
        String password = ""; //PENDING - not supported yet
        //XXX turn this off for release builds - its basically diagnostics
        //for an incomplete card file
        StringBuilder problems = new StringBuilder("<html><b>Problems " + 
                "encountered with the definition of device '" + displayName +
                "' on JavaCard&trade; Platform '" + platform.getDisplayName() +
                "':</b><p><ul>");
        int len = problems.length();
        try {
            String prop = null;
            prop = properties.getProperty(JavacardDeviceKeyNames.DEVICE_CONTACTLESSPORT);
            int contactlessPort;
            if (prop == null) {
                contactlessPort = 9025;
                problem("Contactless port not set - using default of " + contactlessPort, problems);
            } else {
                try {
                    contactlessPort = Integer.parseInt(prop);
                } catch (NumberFormatException nfe) {
                    problem("Contactless port set to '" + prop + "' which is not a legal integer", problems);
                    contactlessPort = 9025;
                }
            }
            int contactedport;
            prop = properties.getProperty(JavacardDeviceKeyNames.DEVICE_CONTACTEDPORT);
            if (prop == null) {
                problem("Contacted port not set.  Defaulting value to 9026", problems);
                contactedport = 9026;
            } else {
                try {
                    contactedport = Integer.parseInt(prop);
                } catch (NumberFormatException e) {
                    problem("Contacted port value '" + prop + "' is not an integer.  Defaulting value to 9026", problems);
                    contactedport = 9026;
                }
            }
            String ramSize = properties.getProperty(JavacardDeviceKeyNames.DEVICE_RAMSIZE);
            if (ramSize == null) {
                ramSize = "24K";
                problem("Ram size not set.  Defaulting value to 24K", problems);
            }
            boolean secureMode = false; //XXX

            prop = properties.getProperty(JavacardDeviceKeyNames.DEVICE_HTTPPORT);
            int httpPort;
            if (prop == null) {
                httpPort = 8019;
                problem("Http port not set - using default of " + httpPort, problems);
            } else {
                try {
                    httpPort = Integer.parseInt(prop);
                } catch (NumberFormatException nfe) {
                    problem("Http port set to '" + prop + "' which is not a legal integer", problems);
                    httpPort = 8019;
                }
            }
            prop = properties.getProperty(JavacardDeviceKeyNames.DEVICE_E2PSIZE);
            String e2pprop;
            if (prop == null) {
                e2pprop = "512K";
                problem("EEPROM size not set - using default of " + e2pprop, problems);
            } else {
                e2pprop = prop;
            }

            prop = properties.getProperty(JavacardDeviceKeyNames.DEVICE_CORSIZE);
            String corSize = "4K";
            if (prop == null) {
                problem("Cor size not set - using default of " + corSize, problems);
            } else {
                try {
                    String val = prop;
                    boolean k = val.endsWith("K") || val.endsWith("k");
                    if (k) {
                        val = val.substring(0, val.length() -1);
                    }
                    Integer.parseInt(val);
                    corSize = prop;
                } catch (NumberFormatException nfe) {
                    problem("COR size set to '" + prop + "' which is not a legal integer", problems);
                    corSize = "4K";
                }
            }

            int proxy2cjcreport;
            prop = properties.getProperty(JavacardDeviceKeyNames.DEVICE_PROXY2CJCREPORT);
            if (prop == null) {
                proxy2cjcreport = 7019;
                problem("Debug Proxy <-> CJCRE Port not set.  Using default value of " + proxy2cjcreport, problems);
            } else {
                try {
                    proxy2cjcreport = Integer.parseInt(prop);
                } catch (NumberFormatException e) {
                    proxy2cjcreport = 7019;
                    problem("Debug Proxy <-> CJCRE Port set to '" + prop + " which is not a legal integer.  Using default value of " + proxy2cjcreport, problems);
                }
            }

            int proxy2ideport;
            prop = properties.getProperty(JavacardDeviceKeyNames.DEVICE_PROXY2IDEPORT);
            if (prop == null) {
                proxy2ideport = 7019;
                problem("Debug Proxy <-> CJCRE Port not set.  Using default value of " + proxy2ideport, problems);
            } else {
                try {
                    proxy2ideport = Integer.parseInt(prop);
                } catch (NumberFormatException e) {
                    proxy2ideport = 7019;
                    problem("Debug Proxy <-> CJCRE Port set to '" + prop + " which is not a legal integer.  Using default value of " + proxy2ideport, problems);
                }
            }

            String loggerLevel;
            prop = properties.getProperty(JavacardDeviceKeyNames.DEVICE_LOGGERLEVEL);
            if (prop == null) {
                loggerLevel = "debug";
                problem("Logger level not set.  Using default of 'debug'", problems);
            } else {
                loggerLevel = prop;
            }
            String contactedProtocol;
            prop = properties.getProperty(JavacardDeviceKeyNames.DEVICE_CONTACTEDPROTOCOL);
            if (prop == null) {
                contactedProtocol = "T=1";
                problem("Contacted Protocol not set.  Using default of 'T=1'", problems);
            } else {
                contactedProtocol = prop;
            }
            prop = properties.getProperty(JavacardDeviceKeyNames.DEVICE_DONT_SUSPEND_THREADS_ON_STARTUP);
            boolean noSuspend;
            if (prop == null) {
                noSuspend = false;
            } else {
                noSuspend = Boolean.valueOf(prop);
            }

            prop = properties.getProperty(JavacardDeviceKeyNames.DEVICE_IS_REMOTE);
            boolean remote;
            if (prop == null) {
                remote = false;
            } else {
                remote = Boolean.valueOf(prop);
            }

            File eeprom = Utils.eepromFileForDevice(platform, displayName, true);
            ReferenceImplementation result = new ReferenceImplementation(platform, id,
                    displayName, 
                    username, password, ramSize, e2pprop, "" + corSize, //NOI18N
                    loggerLevel, "" + httpPort,
                    "" + contactedport, contactedProtocol, //NOI18N
                    contactlessPort + "", secureMode, //NOI18N
                    proxy2cjcreport + "", proxy2ideport + "", remote); //NOI18N
            result.setEepromFile(eeprom);
            result.setNoSuspend(noSuspend);
            return result;
        } finally {
            if (problems.length() != len) {
                showProblemsDialog(problems, properties, platform);
            }
        }
    }

    private static void problem(String problem, StringBuilder sb) {
        sb.append("<li>");
        sb.append(problem);
        sb.append("</li>\n");
    }

    private static void showProblemsDialog(final StringBuilder problems, final Properties properties, final JavacardPlatform platform) {
        problems.append("</ul>\n<p><b>Full diagnostic info:</b><p>\\n<blockquote>\n");
        for (String key : NbCollections.checkedSetByFilter(properties.keySet(), String.class, false)) {
            problems.append(key);
            problems.append('=');
            problems.append(properties.getProperty(key));
            problems.append("<br>\n");
        }

        EventQueue.invokeLater(new Runnable() {

            public void run() {
                JEditorPane ed = new JEditorPane();
                ed.setContentType("text/html"); //NOI18N
                ed.setText(problems.toString());
                JScrollPane pane = new JScrollPane(ed);
                String title = NbBundle.getMessage(SunJavaCardServer.class,
                        "TITLE_PROBLEMS_DIALOG"); //NOI18N
                DialogDisplayer.getDefault().notify(new DialogDescriptor(pane, title));
            }
        });
        Logger.getLogger(RIFactory.class.getName()).log(Level.WARNING, problems.toString());
    }
}
