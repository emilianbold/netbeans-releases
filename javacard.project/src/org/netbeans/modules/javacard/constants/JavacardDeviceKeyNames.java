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
package org.netbeans.modules.javacard.constants;

public final class JavacardDeviceKeyNames {
    //These are keys which should appear in .jcard files, which are properties
    //files imported by the build script that specify information about a
    //specific device
    public static final String DEVICE_CONTACTEDPORT = "javacard.device.contactedPort"; //NOI18N
    public static final String DEVICE_CONTACTEDPROTOCOL = "javacard.device.contactedProtocol"; //NOI18N
    public static final String DEVICE_APDUTOOL_CONTACTEDPROTOCOL = "javacard.device.apdutool.contactedProtocol"; //NOI18N
    public static final String DEVICE_CONTACTLESSPORT = "javacard.device.contactlessPort"; //NOI18N
    public static final String DEVICE_CORSIZE = "javacard.device.corSize"; //NOI18N
    public static final String DEVICE_E2PSIZE = "javacard.device.e2pSize"; //NOI18N
    public static final String DEVICE_HTTPPORT = "javacard.device.httpPort"; //NOI18N
    public static final String DEVICE_LOGGERLEVEL = "javacard.device.loggerLevel"; //NOI18N
    public static final String DEVICE_PROXY2CJCREPORT = "javacard.device.proxy2cjcrePort"; //NOI18N
    public static final String DEVICE_PROXY2IDEPORT = "javacard.device.proxy2idePort"; //NOI18N
    public static final String DEVICE_RAMSIZE = "javacard.device.ramSize"; //NOI18N
    public static final String DEVICE_SECUREMODE = "javacard.device.secureMode"; //NOI18N
    public static final String DEVICE_SERVERURL = "javacard.device.serverurl"; //NOI18N
    public static final String DEVICE_CARDMANAGERURL = "javacard.device.cardmanagerurl"; //NOI18N
    //The device id / display name.  It must always be a valid file name.
    public static final String DEVICE_DISPLAY_NAME = "javacard.device.name";  //NOI18N
    //Display name and device ID
    public static final String DEVICE_DONT_SUSPEND_THREADS_ON_STARTUP = "javacard.device.nosuspend";  //NOI18N
    //If true, pass -nosuspend
    public static final String DEVICE_IS_REMOTE = "javacard.device.is.remotehost"; //NOI18N

    private JavacardDeviceKeyNames(){}
}
