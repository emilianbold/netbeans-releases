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


public class DiagramValidation implements IDiagramValidation
{
    private boolean[] validationKinds = null;
    private boolean[] validationResponse = null;
    private boolean validateLinks = false;
    private boolean validateNodes = false;




    public DiagramValidation()
    {
        validationKinds = new boolean[7];
        validationResponse = new boolean[7];
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation#addValidationKind(int)
     */
    public void addValidationKind(int nKind)
    {
        if(nKind < validationKinds.length)
        {
            validationKinds[nKind] = true;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation#addValidationResponse(int)
     */
    public void addValidationResponse(int nResponse)
    {
        if(nResponse < validationResponse.length)
        {
            validationResponse[nResponse] = true;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation#createGraphObjectValidation()
     */
    public IGraphObjectValidation createGraphObjectValidation()
    {
       IGraphObjectValidation pLocal = new GraphObjectValidation();
 
       pLocal.addValidationKind(IDiagramValidateKind.DVK_VALIDATE_DRAWENGINE);
       pLocal.addValidationKind(IDiagramValidateKind.DVK_VALIDATE_LINKENDS);
       pLocal.addValidationKind(IDiagramValidateKind.DVK_VALIDATE_CONNECTIONTOELEMENT);
       pLocal.addValidationKind(IDiagramValidateKind.DVK_VALIDATE_BRIDGES);
       pLocal.addValidationKind(IDiagramValidateKind.DVK_VALIDATE_RESYNC_DEEP);
       pLocal.addValidationKind(IDiagramValidateKind.DVK_VALIDATE_ALL);
       pLocal.addValidationKind(IDiagramValidateKind.DVK_VALIDATE_NONE);
       
       return pLocal;
     }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation#getValidateLinks()
     */
    public boolean getValidateLinks()
    {
        return validateLinks;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation#getValidateNodes()
     */
    public boolean getValidateNodes()
    {
        return validateNodes;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation#getValidationKind(int)
     */
    public boolean getValidationKind(int nKind)
    {
        if(!validationKinds[IDiagramValidateKind.DVK_VALIDATE_ALL])
        {
            return validationKinds[nKind];
        }
        return true;        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation#getValidationResponse(int)
     */
    public boolean getValidationResponse(int nKind)
    {
        if(!validationResponse[IDiagramValidateResponse.DVRSP_VALIDATE_ALL])
        {
            return validationResponse[nKind];
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation#removeValidationKind(int)
     */
    public void removeValidationKind(int nKind)
    {
        if(nKind < validationKinds.length)
        {
            if (nKind == IDiagramValidateKind.DVK_VALIDATE_ALL)
            {
                validationKinds = new boolean[7];
            }
            else if (nKind != IDiagramValidateKind.DVK_VALIDATE_NONE)
            {
                validationKinds[IDiagramValidateKind.DVK_VALIDATE_ALL] = false;
                validationKinds[nKind] = false;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation#removeValidationResponse(int)
     */
    public void removeValidationResponse(int nKind)
    {
        if(nKind < validationKinds.length)
        {
            validationResponse[nKind] = false;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation#reset()
     */
    public void reset()
    {
        validationKinds = new boolean[7];
        validationResponse = new boolean[7];
        validateLinks = false;
        validateNodes = false;

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation#setValidateLinks(boolean)
     */
    public void setValidateLinks(boolean value)
    {
        validateLinks = value;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation#setValidateNodes(boolean)
     */
    public void setValidateNodes(boolean value)
    {
        validateNodes = value;
    }

}
