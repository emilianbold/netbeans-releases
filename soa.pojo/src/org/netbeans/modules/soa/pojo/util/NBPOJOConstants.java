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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.soa.pojo.util;

import java.math.BigDecimal;

/**
 *
 * @author gpatil
 */
public class NBPOJOConstants {
    public static final int PROJECT_TYPE_J2SE = 0;
    public static final int PROJECT_TYPE_EJB = 1;
    public static final int PROJECT_TYPE_WEB = 2;

    public static final String NBPROJECT_DIR = "nbproject"; //NOI18N
    public static final String POJOS_CONFIG_FILE_NAME = "pojo-cfg.xml"; //NOI18N
    public static final String POJOS_OLD_CONFIG_FILE_NAME = "pojos-cfg.xml"; //NOI18N
    public static final String POJO_BUILD_FILE_NAME = "pojo-bld.xml"; //NOI18N
    public static final String POJO_OLD_BUILD_FILE_NAME = "pojobuild.xml"; //NOI18N
    public static final String POJO_BUILD_RESOURCE = "org/netbeans/modules/soa/pojo/resources/" + POJO_BUILD_FILE_NAME;//NOI18N
    public static final String POJO_OLD_BUILD_RESOURCE = "org/netbeans/modules/soa/pojo/resources/" + POJO_BUILD_FILE_NAME;//NOI18N
    public static final String FILE_OBJECT_SEPARATOR = "/"; // NOI18N

    public static final String POJO_ANT_XTN_NAME = "pojoproject";
    public static final String POJO_COMPILE_TARGET_DEPENDS = "-post-jar" ;//NOI18N
    public static final String POJO_COMPILE_TARGET = "pojo-annotation-processor";

    public static final String POJO_LIB_NAME = "PojoSELib"; //NOI18N
    public static final BigDecimal LATEST_CFG_VERSION = new BigDecimal("0.41"); //NOI18N
    public static final BigDecimal LATEST_POJO_PRJ_VERSION = new BigDecimal("0.41"); //NOI18N
    
    public static final String PROP_BUILD_DIR = "build.dir"; //NOI18N
    public static final String PROP_POJO_ENABLED = "pojo.enabled"; //NOI18N
    public static final String PROP_POJO_PROJECT_VERSION = "pojo.project.version"; //NOI18N
    public static final String PROP_POJO_PACKAGE_ALL = "pojo.packageall"; //NOI18N
    public static final String PROP_POJO_JAR_PACKAGE_EXCLUDES = "pojo.package.excludes"; //NOI18N
    public static final String PROP_POJO_JAR_PACKAGE_EXCLUDES_VAL = "**/jbi.jar,**/sun-pojo-engine-api*.jar,*.properties,*.txt"; //NOI18N
    public static final String PROP_SRC_DIR = "src.dir"; //NOI18N
    public static final String PROP_SRC_ROOT = "source.root"; //NOI18N
}
