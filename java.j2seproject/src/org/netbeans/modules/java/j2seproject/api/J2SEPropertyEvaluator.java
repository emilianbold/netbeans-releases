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

package org.netbeans.modules.java.j2seproject.api;

import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 * Readonly access to project properties through PropertyEvaluator,
 * an instance will be in lookup of the j2seproject.
 * 
 * @author Milan Kubec
 * @since 1.10
 */
public interface J2SEPropertyEvaluator {
    /**
     * Gives PropertyEvaluator for resolving project properties
     *
     * @return PropertyEvaluator for given project
     */
    PropertyEvaluator evaluator();
}
