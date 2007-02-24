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
 * File       : NameSpaceModifyPreRequest.java
 * Created on : Nov 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;

/**
 * @author Aztec
 */
public class NameSpaceModifyPreRequest
    extends PreRequest
    implements INameSpaceModifyPreRequest
{
    protected String m_OldNamespaceName;
    protected String m_NewNamespaceName;
    protected boolean m_NameIsDefault;

    protected INamespace m_ModifiedNamespace;

    protected String m_OldModifiedNamespaceQualifiedName;
    protected String m_NewModifiedNamespaceQualifiedName;
    protected String m_OldSourceDir;
    protected String m_NewSourceDir;
    
    public NameSpaceModifyPreRequest()
    {
        super();
    }
    
    public NameSpaceModifyPreRequest(IElement preElement, 
                        IElement pClone,
                        IElement elementWithArtifact,
                        IRequestProcessor proc, 
                        int detail,
                        IEventPayload payload,
                        IElement clonedOwner)
    {
        super(preElement, 
                pClone, 
                elementWithArtifact, 
                proc, 
                detail, 
                payload, 
                clonedOwner);
                
        m_NameIsDefault = false;

        if ( preElement != null )
        {
           String fsn = null;

           if ( getDetail() == RequestDetailKind.RDT_NAMESPACE_MODIFIED ||
                getDetail() == RequestDetailKind.RDT_CHANGED_NAMESPACE ||
                getDetail() == RequestDetailKind.RDT_NAMESPACE_MOVED )
           {
                IElement pParent = preElement.getOwner();
                if ( pParent != null )
                {
                    IProject pProject = (pParent instanceof IProject)
                                        ? (IProject)pParent : null;
                    if ( pProject == null )
                    {
                        INamespace pSpace = (pParent instanceof INamespace)
                                            ? (INamespace)pParent : null;
                        if ( pSpace != null )
                        {
                            fsn = pSpace.getQualifiedName2();
                        }
                    }
                }
            }
            else
            {
                INamespace pSpace = (preElement instanceof INamespace)
                                    ? (INamespace)preElement : null;
    
                if ( pSpace != null )
                {
                    fsn = pSpace.getQualifiedName2();
                }
    
                if ( getDetail() == RequestDetailKind.RDT_NAME_MODIFIED )
                {
                    INamedElement pElement = (preElement instanceof INamedElement)
                                        ? (INamedElement)preElement : null;
                    if ( pElement != null )
                    {
                        m_NameIsDefault = isDefaultName ( pElement );
                    }
                }
            }

            
            IPackage pPackage = (preElement instanceof IPackage)
                                        ? (IPackage)preElement : null;
            if( pPackage != null)
            {
                String sourceDir = pPackage.getSourceDir();
                setOldSourceDir( sourceDir );
            }    
    
            setOldNamespaceName ( fsn );
        }                
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INameSpaceModifyPreRequest#getNewModifiedNamespaceQualifiedName()
     */
    public String getNewModifiedNamespaceQualifiedName()
    {
        if ( m_ModifiedNamespace != null )
        {
           setNewModifiedNamespaceQualifiedName(m_ModifiedNamespace
                                                    .getQualifiedName2());
        }

        return m_NewModifiedNamespaceQualifiedName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INameSpaceModifyPreRequest#getNewNamespaceName()
     */
    public String getNewNamespaceName()
    {
        return m_NewNamespaceName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INameSpaceModifyPreRequest#getNewSourceDir()
     */
    public String getNewSourceDir()
    {
        return m_NewSourceDir;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INameSpaceModifyPreRequest#getOldModifiedNamespaceQualifiedName()
     */
    public String getOldModifiedNamespaceQualifiedName()
    {
        return m_OldModifiedNamespaceQualifiedName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INameSpaceModifyPreRequest#getOldNamespaceName()
     */
    public String getOldNamespaceName()
    {
        return m_OldNamespaceName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INameSpaceModifyPreRequest#getOldSourceDir()
     */
    public String getOldSourceDir()
    {
        return m_OldSourceDir;
    }
    
    public void setNewModifiedNamespace(INamespace newVal)
    {
        m_ModifiedNamespace = newVal;

        if ( newVal != null )
        {
            setOldModifiedNamespaceQualifiedName(newVal.getQualifiedName2());
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INameSpaceModifyPreRequest#setNewModifiedNamespaceQualifiedName(java.lang.String)
     */
    public void setNewModifiedNamespaceQualifiedName(String qualName)
    {
        m_NewModifiedNamespaceQualifiedName = qualName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INameSpaceModifyPreRequest#setNewNamespaceName(java.lang.String)
     */
    public void setNewNamespaceName(String nsName)
    {
        m_NewNamespaceName = nsName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INameSpaceModifyPreRequest#setNewSourceDir(java.lang.String)
     */
    public void setNewSourceDir(String srcDir)
    {
        m_NewSourceDir = srcDir;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INameSpaceModifyPreRequest#setOldModifiedNamespaceQualifiedName(java.lang.String)
     */
    public void setOldModifiedNamespaceQualifiedName(String qualName)
    {
        m_OldModifiedNamespaceQualifiedName = qualName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INameSpaceModifyPreRequest#setOldNamespaceName(java.lang.String)
     */
    public void setOldNamespaceName(String nsName)
    {
        m_OldNamespaceName = nsName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INameSpaceModifyPreRequest#setOldSourceDir(java.lang.String)
     */
    public void setOldSourceDir(String srcDir)
    {
        m_OldSourceDir = srcDir;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#getModifiedNamespace()
     */
    public INamespace getModifiedNamespace()
    {
        return m_ModifiedNamespace;
    }
   
    public IChangeRequest createChangeRequest(IElement pElement, 
                                                /*ChangeKind*/int type)
    {
        if( pElement == null ) return null;
        
        IChangeRequest pReq = new NamespaceChangeRequest();

        populateChangeRequest ( pReq );

        pReq.setAfter(pElement);
        pReq.setState(type);

        // Now allow the PreRequest object make sure this ChangeRequest is absolutely
        // ready to go...

        preProcessRequest( pReq );
        return pReq;
    }
    
    public boolean inCreateState(IElement element)
    {
       return m_NameIsDefault;
    }
    
    public void populateChangeRequest (IChangeRequest req)
    {
        if( req == null ) return;
        super.populateChangeRequest(req);
       
        INamespaceChangeRequest pNSreq 
                        = (req instanceof INamespaceChangeRequest)
                            ? (INamespaceChangeRequest)req : null;
        if ( pNSreq != null )
        {
            pNSreq.setOldNamespaceName(getOldNamespaceName());
            pNSreq.setNewNamespaceName (getNewNamespaceName());
            pNSreq.setModifiedNamespace(getModifiedNamespace());
            pNSreq.setOldModifiedNamespaceQualifiedName(
                        getOldModifiedNamespaceQualifiedName());
            pNSreq.setNewModifiedNamespaceQualifiedName(
                        getNewModifiedNamespaceQualifiedName());
            pNSreq.setOldSourceDir(getOldSourceDir());
            pNSreq.setNewSourceDir(getNewSourceDir());

        }
    }
    
    public boolean postEvent(IElement pElement)
    {
        boolean retval = super.postEvent(pElement);
        if (retval)
        {
            if ( pElement != null )
            {
                String fsn = null;

                if ( getDetail() == RequestDetailKind.RDT_NAMESPACE_MODIFIED ||
                     getDetail() == RequestDetailKind.RDT_CHANGED_NAMESPACE ||
                     getDetail() == RequestDetailKind.RDT_NAMESPACE_MOVED )
                {
                    IElement pParent = pElement.getOwner();
                    if ( pParent != null )
                    {
                        IProject pProject = (pParent instanceof IProject)
                                             ? (IProject)pParent : null;
                        if ( pProject == null )
                        {
                            INamespace pSpace = (pParent instanceof INamespace)
                                                 ? (INamespace)pParent : null;
                            if ( pSpace != null )
                            {
                                fsn = pSpace.getQualifiedName2();
                            }
                        }
                    }
                }
                else
                {
                    INamespace pSpace = (pElement instanceof INamespace)
                                        ? (INamespace)pElement : null;
    
                    if ( pSpace != null )
                    {
                        fsn = pSpace.getQualifiedName2();
                    }
                }
                IPackage pPackage = (pElement instanceof IPackage)
                                            ? (IPackage)pElement : null;
                if( pPackage != null)
                {
                    String sourceDir = pPackage.getSourceDir();
                    setNewSourceDir( sourceDir );
                }    
                setNewNamespaceName ( fsn );
            }            
        }
        return retval;
    }
    
    public boolean postEvent(IRelationProxy pRel)
    {
       return false;
    }
}
