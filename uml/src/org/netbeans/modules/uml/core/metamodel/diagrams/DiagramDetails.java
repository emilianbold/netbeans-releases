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

import java.util.ArrayList;
import java.util.Date;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.ui.support.diagramsupport.IDiagramTypesManager; 


/**
 *
 * @author Trey Spiva
 */
public class DiagramDetails implements IDiagramKind
{
    private String mFileName = null;
    private String mDiagramName = "";
    private int mDiagramType = 0;
    private String mDiagramTypeName = "";
    private String mNamespaceXMIID = "";
    //private String mDiagramAlias = "";
    private String mDiagramXMIID = "";
    private String mProjectXMIID = "";
    private String mToplevelXMIID = "";
    private String mZoom = null;
    private String documentation="";
    private INamespace mNamespace = null;
    private ArrayList<String> mAssociatedDiagrams = null;
    private ArrayList<ModelElementXMIIDPair> mAssociatedElements = null;
    private Date mDateModified = null;


    /**
     * Gets the name of the diagram.
     *
     * @return The name of the diagrm
     */
    public String getDiagramName() 
    {
        return mDiagramName;
    }

    
    /**
     * Sets the name of the diagram.
     *
     * @parm value The name of the diagrm
     */
    public void setDiagramName(String diagramName) 
    {
        mDiagramName = diagramName;
    }

    /**
     * Returns the type of the daigram.  The valid values for
     * the daigram type is specified in the interface IDiagramKind.
     *
     * @return The diagram type.
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind
     */
    public int getDiagramType()
    {
        return mDiagramType;
    }

    /**
     * Sets the type of the daigram.  The valid values for
     * the daigram type is specified in the interface IDiagramKind.
     *
     * @param vlue The diagram type.
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind
     */
    public void setDiagramType(int value)
    {
        mDiagramType = value;
    }


    /**
     * Retrieves the XMI ID for of the namespace that contains the diagram.
     *
     * @return The id.
     */
    public String getDiagramNamespaceXMIID()
    {
        String retVal = mNamespaceXMIID;

        if ((retVal.length() <= 0) && (mNamespace != null))
        {
            retVal = mNamespace.getXMIID();
        }

        return retVal;
    }

    /**
     * Sets the XMI ID for of the namespace that contains the diagram.
     *
     * @param value
     */
    public void setDiagramNamespaceXMIID(String value)
    {
        mNamespaceXMIID = value;
    }

    /**
     * Retrieves the diagrams XMI ID.
     *
     * @return the id.
     */
    public String getDiagramXMIID()
    {
        return mDiagramXMIID;
    }

    /**
     * Sets the diagrams XMIID.
     *
     * @param value the id.
     */
    public void setDiagramXMIID(String value)
    {
        mDiagramXMIID = value;
    }

    /**
     * Retrieves the XMI ID of the project that contains the diagram.
     *
     * @return the ID.
     */
    public String getDiagramProjectXMIID()
    {
        return mProjectXMIID;
    }

    /**
     * Set the XMI ID of the project that contains the diagram.
     * 
     * @param valuethe ID.
     */
    public void setDiagramProjectXMIID(String value)
    {
        mProjectXMIID = value;
    }
    
    /**
     * Retrieves the XMI ID of the top level component.
     *
     * @return the ID.
     */
    public String getToplevelXMIID()
    {
        return mToplevelXMIID;
    }

    /**
     * Sets the XMI ID of the top level component
     * @param valuethe ID.
     */
    public void setToplevelXMIID(String value)
    {
        mToplevelXMIID = value;
    }

    /**
     * Retrieves the type name of the diagram.
     *
     * @return The diagram type.
     */
    public String getDiagramTypeName()
    {
        return mDiagramTypeName;
    }

    /**
     * Sets the type name of the diagram.
     *
     * @param value The diagram type.
     */
    public void setDiagramTypeName(String value)
    {
        mDiagramTypeName = value;

        IDiagramTypesManager manager = DiagramTypesManager.instance();
        setDiagramType(manager.getDiagramKind(value));
    }

    /**
     * @param space
     */
    public void setNamespace(INamespace space)
    {
        mNamespace = space;
    }

    /**
     * @param space
     */
    public INamespace getNamespace()
    {
        return mNamespace;
    }

    /**
     * @param object
     */
    public void setAssociatedElements(ArrayList<ModelElementXMIIDPair> elements)
    {
        mAssociatedElements = elements;
    }

    /**
     * @param object
     */
    public ArrayList<ModelElementXMIIDPair> getAssociatedElements()
    {
        return mAssociatedElements;
    }

    /**
     * Retrieves the diagram zoom level
     *
     * @return The last date modified.
     */
    public String getZoom() {
        return mZoom;
    }

    /**
     * Sets the diagram zoom level
     *
     * @param value The file date.
     */
    public void setZoom(String zoom) {
        mZoom = zoom;
    }
    
    /**
     * Retrieves the diagram file name that was used to retrieve the diagram details.
     *
     * @return The last date modified.
     */
    public String getDiagramFileName() {
        return mFileName;
    }

    /**
     * Sets the file name that was used to retrieve the diagram details.
     *
     * @param value The file date.
     */
    public void setDiagramFileName(String fileName) {
        mFileName = fileName;
    }
    
    /**
     * Retrieves the file date that was used to retrieve the diagram details.
     *
     * @return The last date modified.
     */
    public Date getDateModified()
    {
        return mDateModified;
    }

    /**
     * Sets the file date that was used to retrieve the diagram details.
     *
     * @param value The file date.
     */
    public void setDateModified(Date value)
    {
        mDateModified = value;
    }
    
    /**
     * @param object
     */
    public void setAssociatedDiagrams(ArrayList<String> diagrams)
    {
        mAssociatedDiagrams = diagrams;
    }

    /**
     * @param object
     */
    public ArrayList<String> getAssociatedDiagrams()
    {
        return mAssociatedDiagrams;
    }

    /**
     * @return the documentation
     */
    public String getDocumentation() {
        return documentation;
    }

    /**
     * @param documentation the documentation to set
     */
    public void setDocumentation(String documentation) {
        this.documentation = documentation!=null ? documentation :"";
    }
}
