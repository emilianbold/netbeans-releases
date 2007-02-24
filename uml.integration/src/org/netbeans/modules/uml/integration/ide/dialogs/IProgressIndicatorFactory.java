/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
 * File         : IProgressIndicatorFactory.java
 * Version      : 1.00
 * Description  : A factory for progress indicators.
 * Author       : Darshan
 */
package org.netbeans.modules.uml.integration.ide.dialogs;

/**
 *  A factory for progress indicators.
 * @author  Darshan
 * @version 1.0
 */
public interface IProgressIndicatorFactory {
    /**
     *  Creates and returns a new <code>IProgressIndicator</code>.
     * @return An <code>IProgressIndicator</code>. Note that <code>null</code>
     *         is an acceptable return value, if the progress indicator must be
     *         suppressed.
     */
    public IProgressIndicator getProgressIndicator();
}
