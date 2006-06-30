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
