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


