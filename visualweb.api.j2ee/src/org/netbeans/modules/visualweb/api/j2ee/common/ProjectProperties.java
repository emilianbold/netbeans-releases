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
// RAVE only!!

package org.netbeans.modules.visualweb.api.j2ee.common;

/**
 *
 * @author David Botterill
 */
public class ProjectProperties {

   public static final String PROJECT_PORTLET_NAME="project.portlet_name";
   public static final String PROJECT_WEB_CONTEXT="project.web_context";
   public static final String PROJECT_IS_PORTLET="project.is_portlet";

   // TODO FIXME - Sun Portal Server specific project properties that
   // don't have a home.  No UI for setting them.
   public static final String PROJECT_SUNPS_DEPLOY_DN="project.sunps_deploy_dn";
   public static final String PROJECT_SUNPS_DISPLAYPROFILE_DN="project.sunps_displayprofile_dn";
   public static final String PROJECT_SUNPS_CONTAINER="project.sunps_container";

}
