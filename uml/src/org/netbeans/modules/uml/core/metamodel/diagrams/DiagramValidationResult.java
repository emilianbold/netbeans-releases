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


package org.netbeans.modules.uml.core.metamodel.diagrams;


public class DiagramValidationResult implements IDiagramValidationResult
{
    private long[] numValidateKind = null;

    public DiagramValidationResult()
    {
        numValidateKind = new long[7];
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidationResult#getNumInvalidItems(int)
     */
    public long getNumInvalidItems(int nKind)
    {
        if(nKind < numValidateKind.length)
        {
            return numValidateKind[nKind];
        }
        return -1;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidationResult#incrementNumInvalidItems(int)
     */
    public void incrementNumInvalidItems(int nKind)
    {
        if(nKind < numValidateKind.length)
        {
            numValidateKind[nKind] += 1;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidationResult#reset()
     */
    public void reset()
    {
        numValidateKind = new long[7];
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidationResult#setNumInvalidItems(int, int)
     */
    public void setNumInvalidItems(int nKind, int nCount)
    {
        if(nKind < numValidateKind.length)
        {
            numValidateKind[nKind] = nCount;
        }
    }

}
