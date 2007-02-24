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
 * File       : ImplementationChangeRequest.java
 * Created on : Oct 28, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;

/**
 * @author Aztec
 */
public class ImplementationChangeRequest
    extends ChangeRequest
    implements IImplementationChangeRequest
{
    private boolean m_ImplementEffected = false;
    private boolean m_ArtifactIsFrom = false;

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getAfterConnection()
     */
    public IElement getAfterConnection()
    {
        return getAfter();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getAfterImplementing()
     */
    public IClassifier getAfterImplementing()
    {
        IImplementation imp = afterImplementation();
        if(imp != null)
            return imp.getImplementingClassifier();
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getAfterInterface()
     */
    public IClassifier getAfterInterface()
    {
        IImplementation imp = afterImplementation();
        if(imp != null)
            return imp.getContract();
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getArtifactIsFrom()
     */
    public boolean getArtifactIsFrom()
    {
        return m_ArtifactIsFrom;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getBeforeConnection()
     */
    public IElement getBeforeConnection()
    {
        return getBefore();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getBeforeImplementing()
     */
    public IClassifier getBeforeImplementing()
    {
        int isCreate = getState();
        if (isCreate != ChangeKind.CT_CREATE)
        {
            boolean impEffected = getImplementationEffected();

            if (impEffected == false)
            {
                // The implementing class is not effected. So, it should be the same, so we
                // can use the after connection and not worry about the clone problem.

                IImplementation imp = afterImplementation();
                if(imp != null)
                    return imp.getImplementingClassifier();
            }
            else
            {
                // We have to worry about the clone problem.  The clone problem is 
                // that a node has been cloned, and so might not be able to able
                // retrieve an item out of the model based on an ID.
                
                IImplementation imp = beforeImplementation();
                if(imp != null)
                    return imp.getImplementingClassifier();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getBeforeInterface()
     */
    public IClassifier getBeforeInterface()
    {
        int isCreate = getState();
        if (isCreate != ChangeKind.CT_CREATE)
        {
            boolean impEffected = getImplementationEffected();

            if (impEffected)
            {
                // The implementing class is not effected. So, it should be the same, so we
                // can use the after connection and not worry about the clone problem.

                IImplementation imp = afterImplementation();
                if(imp != null)
                    return imp.getContract();
            }
            else
            {
                // We have to worry about the clone problem.  The clone problem is 
                // that a node has been cloned, and so might not be able to able
                // retrieve an item out of the model based on an ID.
                
                IImplementation imp = beforeImplementation();
                if(imp != null)
                    return imp.getContract();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getImplementationEffected()
     */
    public boolean getImplementationEffected()
    {
        return m_ImplementEffected;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#setArtifactIsFrom(boolean)
     */
    public void setArtifactIsFrom(boolean artIsFrom)
    {
        m_ArtifactIsFrom = artIsFrom;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#setImplementationEffected(boolean)
     */
    public void setImplementationEffected(boolean implEffected)
    {
        m_ImplementEffected = implEffected;
    }
    
    protected IImplementation beforeImplementation()
    {
        IElement retVal = getBefore();
        if(retVal != null && retVal instanceof IImplementation)
            return (IImplementation)retVal;
        return null;
    }
    
    protected IImplementation afterImplementation()
    {
        IElement retVal = getAfter();
        if(retVal != null && retVal instanceof IImplementation)
            return (IImplementation)retVal;
        return null;
    }

}
