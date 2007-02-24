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
 * File       : IRelationPreRequest.java
 * Created on : Nov 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;

/**
 * @author Aztec
 */
public interface IRelationPreRequest extends IPreRequest
{
    public final int EAID_OLD_FROM = 0;

    public final int EAID_OLD_TO = 1;

    public final int EAID_NEW_FROM = 2;

    public final int EAID_NEW_TO = 3;

    public final int EAID_UNKNOWN = 4;

    public IRelationProxy getRelation();
}
