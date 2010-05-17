/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
