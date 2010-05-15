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
package org.netbeans.modules.soa.ui.nodes;

/**
 * The interface contains the only one method which provide access to an object.
 * It's intended to get universal access to different providing methods.
 *
 * @author nk160297
 */
public interface InstanceRef<T> {
    T getReference();

    /**
     * This method is intended to be used for searching methods through reflection
     * If required method can't be found in the main reference, then it 
     * will be looking for in alternative reference. 
     * The result type doesn't matter here because of the reflection is intended to be used. 
     * <p> 
     * In cases where the alternative doesn't make sence the method should return null.
     */
    Object getAlternativeReference();
}
