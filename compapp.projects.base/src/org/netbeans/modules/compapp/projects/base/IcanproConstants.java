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


package org.netbeans.modules.compapp.projects.base;

/**
 * Used by sub-type project
 */

public interface IcanproConstants {
    String WIZARD_BUNDLE="org/netbeans/modules/compapp/projects/base/ui/wizards/Bundle";

    String COMMAND_REDEPLOY = "redeploy";
    String COMMAND_DEPLOY = "deploy";
    String ARTIFACT_TYPE_JAR = "jar";
    String ARTIFACT_TYPE_EJB_WS="j2ee_archive";
    String SOURCES_TYPE_JAVA = "java";
    String VALIDATION_FLAG = "allow.build.with.error"; //NOI18N

}
