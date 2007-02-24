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
 * File         : SourceNavigator.java
 * Version      : 2.0
 * Description  : Interface for classes that will navigate to selected source
 *                code.
 * Author       : Trey Spiva
 */
package org.netbeans.modules.uml.integration.ide;

import org.netbeans.modules.uml.integration.ide.events.ClassInfo;
import org.netbeans.modules.uml.integration.ide.events.MemberInfo;
import org.netbeans.modules.uml.integration.ide.events.MethodInfo;

/**
 *  The SourceNavigator interface will be implemented by IDE integrations to
 * navigate to the appropriate source code when the user selects a Describe
 * model element.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-05-22  Darshan     Adapted for Wolverine.
 */
public interface SourceNavigator  {
    /**
     *  Navigates to the given class.
     * @param clazz
     */
    public void navigateTo(ClassInfo clazz);

    /**
     *  Navigates to the given operation
     * @param minf
     */
    public void navigateTo(MethodInfo minf);

    /**
     *  Navigates to the given attribute.
     * @param minf
     */
    public void navigateTo(MemberInfo minf);
}
