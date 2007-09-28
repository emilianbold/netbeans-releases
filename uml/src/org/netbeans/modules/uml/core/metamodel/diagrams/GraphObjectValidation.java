/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
