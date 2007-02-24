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
 * File       : IClause.java
 * Created on : Sep 17, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public interface IClause extends IElement
{
    public ETList<IClause> getSuccessors();
    public void removeSuccessor(IClause pClause);
    public void addSuccessor(IClause pClause);
    public ETList<IClause> getPredecessors();
    public void removePredecessor(IClause pClause);
    public void addPredecessor(IClause pClause);
    public IValueSpecification getTestOutput();
    public void setTestOutput(IValueSpecification pValSpec);
    public ETList<IAction> getBody();
    public void removeFromBody(IAction pAction);
    public void addToBody(IAction pAction);
    public ETList<IAction> getTest();
    public void removeFromTest(IAction pAction);
    public void addToTest(IAction pAction);
}
