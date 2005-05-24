/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * TestConstants.java
 *
 * Created on March 11, 2004, 12:40 PM
 */

package org.netbeans.modules.j2ee.sun.share;

/**
 *
 * @author  vkraemer
 */
public class TestConstants {

    private TestConstants() { }; 
    
    public static final String validPlan2 = "<deployment-plan><file-entry><name>foobar</name><content>a b c d</content></file-entry></deployment-plan>";
    
    public static final String validPlan3 = "<sun-web-app><context-root>fooBar</context-root></sun-web-app>";

    public static final String invalidPlan1 = "<deployment-plan><name>foobar<content>a b c d</content></file-entry></deployment-plan>";
    
    public static final String invalidPlan2 = "<sun-ejb-jar></sun-ejb-jar>";
    
}
