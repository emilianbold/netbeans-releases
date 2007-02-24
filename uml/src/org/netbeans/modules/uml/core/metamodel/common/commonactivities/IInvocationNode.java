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
 * File       : IInvocationNode.java
 * Created on : Sep 17, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public interface IInvocationNode extends IActivityNode
{
    public ETList<IConstraint> getLocalPostConditions();
    public void removeLocalPostcondition(IConstraint pConstraint);
    public void addLocalPostCondition(IConstraint pConstraint);
    public ETList<IConstraint> getLocalPreconditions();
    public void removeLocalPrecondition(IConstraint pConstraint);
    public void addLocalPrecondition(IConstraint pConstraint);
    public IMultiplicity getMultiplicity();
    public void setMultiplicity(IMultiplicity mul);
    public boolean getIsMultipleInvocation();
    public void setIsMultipleInvocation(boolean isMulInvoc);
    public boolean getIsSynchronous();
    public void setIsSynchronous(boolean sync);
    public IConstraint createCondition(String condition);
    public String getRangeAsString();
}
