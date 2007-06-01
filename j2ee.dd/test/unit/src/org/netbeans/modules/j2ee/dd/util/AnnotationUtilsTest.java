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

package org.netbeans.modules.j2ee.dd.util;

import org.netbeans.junit.NbTestCase;

/**
 * Test for {@link AnnotationUtils}.
 * @author Tomas Mysik
 */
public class AnnotationUtilsTest extends NbTestCase {
    
    public AnnotationUtilsTest(String name) {
        super(name);
    }
    
    public void testSetterNameToPropertyName() throws Exception {
        // correct setters
        assertEquals("property name should be ok", "time", AnnotationUtils.setterNameToPropertyName("setTime"));
        assertEquals("property name should be ok", "longTermPlan", AnnotationUtils.setterNameToPropertyName("setLongTermPlan"));
        assertEquals("property name should be ok", "a", AnnotationUtils.setterNameToPropertyName("setA"));
        
        // incorrect setters
        assertNull("property name should be null", AnnotationUtils.setterNameToPropertyName("isTest"));
        assertNull("property name should be null", AnnotationUtils.setterNameToPropertyName("testMe"));
        assertNull("property name should be null", AnnotationUtils.setterNameToPropertyName("getShortTermPlan"));
        assertNull("property name should be null", AnnotationUtils.setterNameToPropertyName("is"));
        assertNull("property name should be null", AnnotationUtils.setterNameToPropertyName("se"));
        assertNull("property name should be null", AnnotationUtils.setterNameToPropertyName("get"));
        assertNull("property name should be null", AnnotationUtils.setterNameToPropertyName("set"));
    }
}
