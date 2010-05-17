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
package com.sun.rave.web.ui.theme;

/**
 * <p> This class contains javascript related theme constants.</p>
 * TODO: Eventually these need to move to a theme-based
 * resource file.
 */

public class ThemeJavascript {

    /**
     * A Javascript file that contains functions for manipulating
     * the AddRemove component.
     */
    public static final String ADD_REMOVE = "addRemove";
    /**
     * A Javascript file that contains general functions used by
     * simple components.
     */
    public static final String BASIC = "basic";
    /**
     * A Javascript file that contains functions for obtaining
     * the browser version.
     */
    public static final String BROWSER_VERSION = "browserVersion";
    /**
     * A javascript file that contains functions for manipulating
     * the Calendar component.
     */
    public static final String CALENDAR = "calendar";
    /**
     * A Javascript file that contains functions for manipulating
     * cookies.
     */
    public static final String COOKIE = "cookie";
    /**
     * A Javascript file that contains functions for manipulating
     * the EditableList component.
     */
    public static final String EDITABLE_LIST = "editableList";
    
    /**
     * A Javascript file that contains functions for manipulating
     * the FileChooser component.
     */
    public static final String FILE_CHOOSER = "fileChooser";
    
    /**
     * A Javascript file that contains functions for maintaining
     * the focus within the page.
     */
    public static final String FOCUS_COOKIE = "focusCookie";
    /**
     * A properties file key whose value is a space separated list of
     * keys identifying javascript files that are included in every page.
     */
    public static final String GLOBAL = "global";
    /**
     * A Javascript file that contains functions for manipulating
     * the OrderableList component.
     */
    public static final String ORDERABLE_LIST = "orderableList";
    /**
     * A Javascript file that contains functions for maintaining
     * the scroll position within a page.
     */
    public static final String SCROLL_COOKIE = "scrollCookie";
    
     /**
     * A javascript file that contains functions for manipulating
     * the Scheduler component.
     */
    public static final String SCHEDULER = "scheduler";
    
    /**
     * A Javascript file that contains functions for manipulating
     * component styles.
     */
    public static final String STYLESHEET = "stylesheet";
    /**
     * A Javascript file that contains functions for manipulating
     * the Table component.
     */
    public static final String TABLE = "table";
    /**
     * A Javascript file that contains functions for manipulating
     * the Tree component.
     */
    public static final String TREE = "tree";
    /**
     * A Javascript file that contains functions for manipulating
     * the Wizard component.
     */
    public static final String WIZARD = "wizard";

    /* Obsolete ?
    public static final String DYNAMIC = "dynamic";
    public static final String POPUP = "popup";
    public static final String TOPOLOGY = "topology";
    */

    /**
     * This private constructor prevents this class from being instantiated
     * directly as its only purpose is to provide image constants.
     */
    private ThemeJavascript() {
	// do nothing
    }
}


