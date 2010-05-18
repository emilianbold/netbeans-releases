/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.jconsole;

/**
 * Class used to discover Which JVM is used in order to enable only supported features.
 * @author jfdenise
 */
public class FeaturesDeployer {
    private static Boolean enable;

    private static boolean runOnSun() {
        if(enable == null)  {
            try {
                Class.forName("com.sun.jmx.mbeanserver.JmxMBeanServer"); // NOI18N
                enable = Boolean.TRUE;
            }catch(Throwable e) {
                enable = Boolean.FALSE;
            }
        }
        return enable.booleanValue();
    }
        
    
    /**
     * Returns if the JVM supports local management features.
     * @return <CODE>boolean</CODE> true if feature is supported
     */
    public static String enableMM() {
        return runOnSun() ?
            "Actions/Management/org-netbeans-modules-jmx-runtime-EnableMMAction.instance" : // NOI18N
            null;
    }
    
    /**
     * Returns if the JVM supports local management debug features.
     * @return <CODE>boolean</CODE> true only if JVM supports local management debug features
     */
    public static String debugMM() {
        return runOnSun() ?
            "Actions/Management/org-netbeans-modules-jmx-runtime-DebugMMAction.instance" : // NOI18N
            null;
    }
    
     /**
      * Returns if the JVM contains Jconsole (SUN JVM).
      * @return <CODE>boolean</CODE> true only if JVM contains Jconsole (SUN JVM).
      */
     public static String launchJConsole() {
        return runOnSun() ?
            "Actions/Management/org-netbeans-modules-jmx-jconsole-LaunchAction.instance" : // NOI18N
            null;
    }
     
    /**
     * Returns if the JVM supports loading of management configuration files (Sun JVM only).
     * @return <CODE>boolean</CODE> true only if JVM supports loading of management configuration files.
     */
    public static String enableManagementProperties() {
        return runOnSun() ?
            "Management/Templates/management.properties" : // NOI18N
            null;
    }
}
