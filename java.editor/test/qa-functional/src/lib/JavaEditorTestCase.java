/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package lib;

import lib.EditorTestCase;

/**
 *
 * @author  mroskanin
 */
public class JavaEditorTestCase extends EditorTestCase {
    
    public static final String PROJECT_NAME = "java_editor_test"; //NOI18N;
    
    public JavaEditorTestCase(String testMethodName) {
        super(testMethodName);
    }    
    
    protected String getDefaultProjectName() {
        return PROJECT_NAME;
    }
}
