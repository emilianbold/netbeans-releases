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
 * File         : ISourceNavigable.java
 * Version      : 1.0
 * Description  : Interface for classes that can generate source navigation
 *                events.
 * Author       : Darshan
 */
package org.netbeans.modules.uml.integration.ide;

/**
 *  Interface implemented by classes that can generate source code navigation
 * events.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-05-22  Darshan     Created.
 *
 * @author Darshan
 */
public interface ISourceNavigable {
    public void setSourceNavigator(SourceNavigator nav);
    public SourceNavigator getSourceNavigator();
}
