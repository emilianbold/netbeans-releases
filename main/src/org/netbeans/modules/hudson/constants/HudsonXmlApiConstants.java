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

package org.netbeans.modules.hudson.constants;

/**
 * Constants provider for Hudson XML_API
 * 
 * @author Michal Mocnak
 */
public interface HudsonXmlApiConstants {
    
    // XML API Suffix
    public static final String XML_API_URL ="/api/xml";
    
    // Hudson Instance Element
    public static final String XML_API_VIEW_ELEMENT = "view";
    public static final String XML_API_JOB_ELEMENT = "job";
    public static final String XML_API_NAME_ELEMENT = "name";
    public static final String XML_API_URL_ELEMENT = "url";
    public static final String XML_API_COLOR_ELEMENT = "color";
    
    // Hudson Job Elements
    public static final String XML_API_DESCRIPTION_ELEMENT = "description";
    public static final String XML_API_DISPLAY_NAME_ELEMENT = "displayName";
    public static final String XML_API_BUILDABLE_ELEMENT = "buildable";
    public static final String XML_API_INQUEUE_ELEMENT = "inQueue";
    public static final String XML_API_LAST_BUILD_ELEMENT = "lastBuild";
    public static final String XML_API_LAST_STABLE_BUILD_ELEMENT = "lastStableBuild";
    public static final String XML_API_LAST_SUCCESSFUL_BUILD_ELEMENT = "lastSuccessfulBuild";
    public static final String XML_API_LAST_FAILED_BUILD_ELEMENT = "lastFailedBuild";
    
    // Hudson Job Build Elements
    public static final String XML_API_BUILDING_ELEMENT = "building";
    public static final String XML_API_DURATION_ELEMENT = "duration";
    public static final String XML_API_TIMESTAMP_ELEMENT = "timestamp";
    public static final String XML_API_RESULT_ELEMENT = "result";
    public static final String XML_API_ITEM_ELEMENT = "item";
    public static final String XML_API_FILE_ELEMENT = "file";
    public static final String XML_API_EDIT_TYPE_ELEMENT = "editType";
    public static final String XML_API_REVISION_ELEMENT = "revision";
    public static final String XML_API_PREV_REVISION_ELEMENT = "prevrevision";
    public static final String XML_API_MSG_ELEMENT = "msg";
    public static final String XML_API_USER_ELEMENT = "user";
    
    // Start Hudson Job Command
    public static final String XML_API_BUILD_URL = "build";
}