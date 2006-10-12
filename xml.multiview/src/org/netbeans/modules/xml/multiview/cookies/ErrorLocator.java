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

package org.netbeans.modules.xml.multiview.cookies;

/** 
 * This interface should be implemented by classes
 * that need to be able to associate errors with individual
 * components.
 *
 * Created on November 22, 2004, 8:03 PM
 * @author mkuchtiak
 */
public interface ErrorLocator {
    
    /**
     * Gets the component that is associated with the given 
     * <code>errorId></code>.
     * @param errorId the id of the error.
     */ 
    public javax.swing.JComponent getErrorComponent(String errorId);
}
