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
package org.netbeans.test.codegen;

/**
 * Tested class in which annotation attribute will will be changed to
 * detect correctness of its generating.
 *
 * @author Pavel Flaska
 */
public class AnnotationAttributeValueTest {

    /** Creates a new instance of AnnotationAttributeValueTest */
    public @AnnotationType(id = 666, engineer = "MaM", synopsis = "unknown", date = "2004-06-03") AnnotationAttributeValueTest() {
    }

}
