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

package org.netbeans.modules.ant.freeform.spi;

/**
 * Miscellaneous constants.
 * @author David Konecny
 */
public class ProjectConstants {

    private ProjectConstants() {}

    /**
     * This property is stored in project.xml iff Ant script is not in
     * default location, that is not in parent folder of nbproject directory.
     */
    public static final String PROP_ANT_SCRIPT = "ant.script"; // NOI18N

    /**
     * Location of original project. This property exist only when NB
     * project metadata are stored in different folder.
     */
    public static final String PROP_PROJECT_LOCATION = "project.dir"; // NOI18N
    
    /** 
     * Prefix used in paths to refer to project location.
     */
    public static final String PROJECT_LOCATION_PREFIX = "${" + PROP_PROJECT_LOCATION + "}/"; // NOI18N

}
