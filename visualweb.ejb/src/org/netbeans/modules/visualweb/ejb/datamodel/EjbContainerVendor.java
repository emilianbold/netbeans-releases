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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

/*
 * EjbContainerVendor.java
 */

package org.netbeans.modules.visualweb.ejb.datamodel;

import org.openide.util.NbBundle;

/**
 * This class lists all the EJB container vendors Rave can support 
 *
 * @author  cao
 */
public class EjbContainerVendor
{
    // Supported application servers
    public static final String SUN_APP_SERVER_9_1 = NbBundle.getMessage(EjbContainerVendor.class,
            "SUN_APP_SERVER_9_1");

    public static final String SUN_APP_SERVER_9 = NbBundle.getMessage(EjbContainerVendor.class,
            "SUN_APP_SERVER_9");
    public static final String SUN_APP_SERVER_8_1 = NbBundle.getMessage( EjbContainerVendor.class, "SUN_APP_SERVER_8_1" );
    public static final String SUN_APP_SERVER_8 = NbBundle.getMessage( EjbContainerVendor.class, "SUN_APP_SERVER_8" );
    public static final String SUN_APP_SERVER_7 = NbBundle.getMessage( EjbContainerVendor.class, "SUN_APP_SERVER_7" );
    public static final String WEBLOGIC_8_1 = NbBundle.getMessage( EjbContainerVendor.class, "BEA_WEBLOGIC_8_1" );
    public static final String WEBSPHERE_5_1 = NbBundle.getMessage( EjbContainerVendor.class, "IBM_WEBSPHERE_5_1" );
    
    // Default IIOP Port for different application servers
    public static final int SUN_APP_SERVER_PORT = 3700;
    public static final int WEBLOGIC_PORT = 7001;
    public static final int WEBSPHERE_PORT = 2809;
    public static final int UNKNOWN_PORT = 0;
    
    // EJB deployment descriptors
    public static final String STANDARD_DEPLOYMENT_DESCRIPTOR = "ejb-jar.xml";
    public static final String SUN_DEPLOYMENT_DESCRIPTOR = "sun-ejb-jar.xml";
    public static final String WEBLOGIC_DEPLOYMENT_DESCRIPTOR = "weblogic-ejb-jar.xml";
    public static final String WEBSPHERE_DEPLOYMENT_DESCRIPTOR = "ibm-ejb-jar-bnd.xmi";
    
    public static String[] getContainerTypeNames()
    {
        String[] containerTypes = { SUN_APP_SERVER_9_1, SUN_APP_SERVER_9, SUN_APP_SERVER_8_1,
                SUN_APP_SERVER_8, SUN_APP_SERVER_7, WEBLOGIC_8_1, WEBSPHERE_5_1 };
        return containerTypes;
    }
    
    public static int getDefaultPort( String server )
    {
        if (isSunAppServer(server))
            return SUN_APP_SERVER_PORT;
        else if( server.equals( WEBLOGIC_8_1 ) )
            return WEBLOGIC_PORT;
        else if( server.equals( WEBSPHERE_5_1 ) )
            return WEBSPHERE_PORT;
        else
            return UNKNOWN_PORT;
    }

    public static String getVendorDDFileName( String server )
    {
        if (isSunAppServer(server))
            return SUN_DEPLOYMENT_DESCRIPTOR;
        else if( server.equals( WEBLOGIC_8_1 ) )
            return WEBLOGIC_DEPLOYMENT_DESCRIPTOR;
        else if( server.equals( WEBSPHERE_5_1 ) )
            return WEBSPHERE_DEPLOYMENT_DESCRIPTOR;
        else
            throw new java.lang.IllegalArgumentException( server );
    }

    static boolean isSunAppServer(String server) {
        return server.equals(SUN_APP_SERVER_9_1) || server.equals(SUN_APP_SERVER_9)
                || server.equals(SUN_APP_SERVER_8_1) || server.equals(SUN_APP_SERVER_8)
                || server.equals(SUN_APP_SERVER_7);
    }    
}
