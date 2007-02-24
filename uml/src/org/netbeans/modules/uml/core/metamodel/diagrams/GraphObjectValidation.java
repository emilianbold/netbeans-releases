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


public class GraphObjectValidation implements IGraphObjectValidation
{
    boolean[] validationKinds = null;
    int[] validationResults = null;

    public GraphObjectValidation()
    {
        validationKinds = new boolean[7];
        validationResults = new int[7];
    }

    /* (non-Javadoc)
     * @see com.embarcadero.describe.diagrams.IGraphObjectValidation#addValidationKind(int)
     */
    public void addValidationKind(int nKind)
    {
        if (nKind == IDiagramValidateKind.DVK_VALIDATE_ALL)
       {
           validationKinds[IDiagramValidateKind.DVK_VALIDATE_ALL] = true;
           validationKinds[IDiagramValidateKind.DVK_VALIDATE_DRAWENGINE] = true;
           validationKinds[IDiagramValidateKind.DVK_VALIDATE_LINKENDS] = true;
           validationKinds[IDiagramValidateKind.DVK_VALIDATE_CONNECTIONTOELEMENT] = true;
           validationKinds[IDiagramValidateKind.DVK_VALIDATE_BRIDGES] = true;
           validationKinds[IDiagramValidateKind.DVK_VALIDATE_RESYNC_DEEP] = true;
       }
       else
       {
           validationKinds[nKind] = true;
       }
    }

    /* (non-Javadoc)
     * @see com.embarcadero.describe.diagrams.IGraphObjectValidation#areAnyValidationResultsInvalid()
     */
    public boolean areAnyValidationResultsInvalid()
    {
        
      int nResult = getValidationResult(IDiagramValidateKind.DVK_VALIDATE_DRAWENGINE);
      if (nResult != IDiagramValidateResult.DVR_INVALID)
      {
          nResult = getValidationResult(IDiagramValidateKind.DVK_VALIDATE_LINKENDS);
      }
      if (nResult != IDiagramValidateResult.DVR_INVALID)
      {
          nResult = getValidationResult(IDiagramValidateKind.DVK_VALIDATE_CONNECTIONTOELEMENT);
      }
      if (nResult != IDiagramValidateResult.DVR_INVALID)
      {
          nResult = getValidationResult(IDiagramValidateKind.DVK_VALIDATE_BRIDGES);
      }
      if (nResult != IDiagramValidateResult.DVR_INVALID)
      {
          nResult = getValidationResult(IDiagramValidateKind.DVK_VALIDATE_RESYNC_DEEP);
      }

      if (nResult == IDiagramValidateResult.DVR_INVALID)
      {
         return true;
      }
      
      return false;
    }

    /* (non-Javadoc)
     * @see com.embarcadero.describe.diagrams.IGraphObjectValidation#getValidationKind(int)
     */
    public boolean getValidationKind(int nKind)
    {
        if(nKind < validationKinds.length)
        {
            return validationKinds[nKind];
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.embarcadero.describe.diagrams.IGraphObjectValidation#getValidationResult(int)
     */
    public int getValidationResult(int nKind)
    {
        if(nKind < validationKinds.length)
        {
            return validationResults[nKind];
        }
        return -1;
    }

    /* (non-Javadoc)
     * @see com.embarcadero.describe.diagrams.IGraphObjectValidation#putValidationResult(int, int)
     */
    public void setValidationResult(int nKind, int nResult)
    {
        if(nKind < validationKinds.length)
        {
            validationResults[nKind] = nResult;
        }        
    }

    /* (non-Javadoc)
     * @see com.embarcadero.describe.diagrams.IGraphObjectValidation#removeValidationKind(int)
     */
    public void removeValidationKind(int nKind)
    {
        if(nKind < validationKinds.length)
        {
            validationKinds[nKind] = false;
        }        
    }

}
