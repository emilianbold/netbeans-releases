/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License ("CDDL")(the "License"). You
 * may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://open-esb.dev.java.net/public/CDDLv1.0.html
 * or mural/license.txt. See the License for the specific language
 * governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL Header Notice
 * in each file and include the License file at mural/license.txt.
 * If applicable, add the following below the CDDL Header, with the
 * fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 */
package org.netbeans.modules.etl.project;

import net.java.hulp.i18n.Logger;
/**
 * Log utility functions customized for DataIntegrator
 * This class creates a mapping from packages to Component name
 * and gives a jdk logger instance given a class name
 * @author srengara@dev.java.net
 */
public class LogUtil {
    /** Logger prefix */
    public static final String DATAINTEGRATOR_LOG_PREFIX = "SUN.DM.DI";

    
    /**
     * This method returns an instance of <code>java.util.Logger</code> given a class name
     * @param className
     * @return logger instance
     **/
    public static Logger getLogger(String className) {
        return getLogger(LogUtil.DATAINTEGRATOR_LOG_PREFIX,className);
    }
    
    /** Get logger for given object and prefix
     * @param logPrefix
     * @param className class name
     * @return logger
     */
    public static Logger getLogger(String logPrefix, String className) {
        if(logPrefix == null || "".equalsIgnoreCase(logPrefix)) {
            logPrefix = LogUtil.DATAINTEGRATOR_LOG_PREFIX;
        }
       //String componentName = (String) sLogMapping.getObjectValue(className);
        return Logger.getLogger(logPrefix + "." + className);
    }
}
