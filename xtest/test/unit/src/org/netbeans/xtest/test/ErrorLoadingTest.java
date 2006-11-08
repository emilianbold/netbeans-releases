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
 package org.netbeans.xtest.testetbeans.xtest.test;

/** This test verifies reporting of invalid tests (see http://www.netbeans.org/issues/show_bug.cgi?id=88180).
 * Because this class doesn't implement Test interface it is not loaded and ClassCastException
 * is thrown and reported by XTest.
 */
public class ErrorLoadingTest {
    
    public ErrorLoadingTest() {
    }
}