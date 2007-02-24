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
 * File       : GeneralizationChangeRequest.java
 * Created on : Oct 28, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;

/**
 * @author Aztec
 */
public class GeneralizationChangeRequest
    extends ChangeRequest
    implements IGeneralizationChangeRequest
{
    private boolean m_SpecializeEffected = false;
    private boolean m_ArtifactIsFrom = false;
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getAfterConnection()
     */
    public IElement getAfterConnection()
    {
        return getAfter();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getAfterGeneralizing()
     */
    public IClassifier getAfterGeneralizing()
    {
        IGeneralization gen = afterGeneralization();
        if(gen != null)
            return gen.getGeneral();
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getAfterSpecializing()
     */
    public IClassifier getAfterSpecializing()
    {
        IGeneralization gen = afterGeneralization();
        if(gen != null)
            return gen.getSpecific();
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getArtifactIsFrom()
     */
    public boolean getArtifactIsFrom()
    {
        return m_ArtifactIsFrom;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getBeforeConnection()
     */
    public IElement getBeforeConnection()
    {
        return getBefore();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getBeforeGeneralizing()
     */
    public IClassifier getBeforeGeneralizing()
    {
        int isCreate = getState();
        if (isCreate != ChangeKind.CT_CREATE)
        {
            boolean splEffected = getSpecializationEffected();
        
            if (splEffected)
            {
                // The generalizing class is not effected. So, it should be the same, so we
                // can use the after connection and not worry about the clone problem.
        
                IGeneralization gen = afterGeneralization();
                if(gen != null)
                    return gen.getGeneral();
            }
            else
            {
                // We have to worry about the clone problem.  The clone problem is 
                // that a node has been cloned, and so might not be able to able
                // retrieve an item out of the model based on an ID.
                        
                IGeneralization gen = beforeGeneralization();
                if(gen != null)
                    return gen.getGeneral();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getBeforeSpecializing()
     */
    public IClassifier getBeforeSpecializing()
    {
        int isCreate = getState();
        if (isCreate != ChangeKind.CT_CREATE)
        {
            boolean splEffected = getSpecializationEffected();
        
            if (splEffected == false)
            {
                // The implementing class is not effected. So, it should be the same, so we
                // can use the after connection and not worry about the clone problem.
        
                IGeneralization gen = afterGeneralization();
                if(gen != null)
                    return gen.getSpecific();
            }
            else
            {
                // We have to worry about the clone problem.  The clone problem is 
                // that a node has been cloned, and so might not be able to able
                // retrieve an item out of the model based on an ID.
                        
                IGeneralization gen = beforeGeneralization();
                if(gen != null)
                    return gen.getSpecific();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getSpecializationEffected()
     */
    public boolean getSpecializationEffected()
    {
        return m_SpecializeEffected;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#setArtifactIsFrom(boolean)
     */
    public void setArtifactIsFrom(boolean artIsFrom)
    {
        m_ArtifactIsFrom = artIsFrom;        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#setSpecializationEffected(boolean)
     */
    public void setSpecializationEffected(boolean splEffected)
    {
        m_SpecializeEffected = splEffected;
    }
    
    protected IGeneralization beforeGeneralization()
    {
        IElement retVal = getBefore();
        if(retVal != null && retVal instanceof IGeneralization)
            return (IGeneralization)retVal;
        return null;
    }

    protected IGeneralization afterGeneralization()
    {
        IElement retVal = getAfter();
        if(retVal != null && retVal instanceof IGeneralization)
            return (IGeneralization)retVal;
        return null;
    }

}
