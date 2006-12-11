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

package org.netbeans.installer;

/**
 * Used for bean ID and property name constants.
 */
public class Names {
    //Product bean IDs
    public static final String PRODUCT_ROOT_ID = "beanProduct";
    public static final String CORE_IDE_ID = "beanCoreIDE";
    public static final String UNPACK_JARS_ID = "beanUnpackJars";
    public static final String APP_SERVER_ID = "beanAppServer";
    public static final String J2SE_ID = "beanJ2SE";
    
    //Used to distinguish installers
    public static final String INSTALLER_TYPE = "InstallerType";
    public static final String INSTALLER_NB = "NetBeansInstaller";
    public static final String INSTALLER_AS_BUNDLE = "ASBundleInstaller";
    public static final String INSTALLER_JBOSS_BUNDLE = "JBossBundleInstaller";
    public static final String INSTALLER_JDK_BUNDLE = "JDKBundleInstaller";
            
    /** Creates a new instance of Names */
    private Names() {
    }
    
}
