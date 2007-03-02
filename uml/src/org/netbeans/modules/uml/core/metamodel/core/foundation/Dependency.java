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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.dom4j.Document;
import org.dom4j.Node;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class Dependency extends DirectedRelationship 
    implements IDependency, IPackageableElement
{

	private IPackageableElement m_Pack = new PackageableElement();

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#setNode(org.dom4j.Node)
     */
    public void setNode(Node n)
    {
        super.setNode(n);
        m_Pack.setNode(n);
    }

	/**
	 * The supplier of the Dependency.
	 * 
	 * @param newVal[in] 
	 *
	 * @return S_OK
	 */
    public void setSupplier(INamedElement elem)
    {
        PreventElementReEntrance reEnt = new PreventElementReEntrance(this);
        try
        {
            if (!reEnt.isBlocking())
            {    
              	RelationshipEventsHelper help = new RelationshipEventsHelper(this);
              	if (help.firePreEndModified("supplier", null, elem))
              	{
              		setSingleElementAndConnect(elem, "supplier",
        												new IBackPointer<INamedElement>() 
        												{
        													public void execute(INamedElement obj) 
        													{
        													    obj.addSupplierDependency(Dependency.this);
        													}
        												},
        												new IBackPointer<INamedElement>() 
        												{
        													public void execute(INamedElement obj) 
        													{
        														obj.removeSupplierDependency(Dependency.this);
        													}
        												}
        	  								);
        
              		help.fireEndModified();
              	}
            }
        }
        finally
        {
            reEnt.releaseBlock();
        }
    }

  /**
   * The supplier of the Dependency.
   * 
   * @param pval[out] 
   *
   * @return S_OK
   */
  public INamedElement getSupplier()
  {
     INamedElement dummy = null;
	  return retrieveSingleElementWithAttrID( "supplier", dummy, INamedElement.class );
  }

    /**
     * Sets the client of this Dependency.
     * 
     * @param elem The client INamedElement of this Dependency.
     */
    public void setClient(INamedElement elem)
    {
        PreventElementReEntrance reEnt = new PreventElementReEntrance(this);
        try
        {
            if (!reEnt.isBlocking())
            {
                RelationshipEventsHelper help = 
                    new RelationshipEventsHelper(this);
                if (help.firePreEndModified("client", elem, null))
                {
                    setSingleElementAndConnect(elem, "client",
                        new IBackPointer<INamedElement>()
                        {
                            public void execute(INamedElement el)
                            {
                                el.addClientDependency(Dependency.this);
                            }
                        },
                        
                        new IBackPointer<INamedElement>()
                        {
                            public void execute(INamedElement el)
                            {
                                el.removeClientDependency(Dependency.this);
                            }
                        });
                    help.fireEndModified();
                }
            }
        }
        finally
        {
            reEnt.releaseBlock();
        }
    }

  /**
   * The client of the Dependency.
   * 
   * @param pval[out] 
   *
   * @return S_OK
   */
  public INamedElement getClient()
  {   
     INamedElement dummy = null;
	  return retrieveSingleElementWithAttrID( "client", dummy, INamedElement.class );
  }

  /**
   * Establishes the appropriate XML elements for this UML type.
   *
   * [in] The document where this element will reside
   * [in] The element's parent node.
   *
   * @return HRESULT
   */

  public void establishNodePresence( Document doc, Node parent )
  {
	 buildNodePresence( "UML:Dependency", doc, parent );
  }

  /**
   *
   * Called when this Dependency has been deleted. Simply makes sure that both ends of this dependency
   * are marked as dirty if those elements have been versioned.
   *
   * @param ver[in] The Dependency link being deleted
   *
   * @return HRESULT
   *
   */
	public void fireDelete(IVersionableElement elem)
	{
		INamedElement client = getClient();
		INamedElement supplier = getSupplier();
		if (client != null)
		{
			client.setDirty(true);
		}
		if (supplier != null)
		{
			supplier.setDirty(true);
		}
		super.fireDelete(elem);
	}

	/**
	 * @param dep
	 */
	public void addClientDependency(IDependency dep)
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		m_Pack.addClientDependency(dep);
	}

	/**
	 * @param dep
	 */
	public void addSupplierDependency(IDependency dep)
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		m_Pack.addSupplierDependency(dep);
	}

	/**
	 * @return
	 */
	public String getAlias()
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		return m_Pack.getAlias();
	}

	/**
	 * @return
	 */
	public ETList<IDependency> getClientDependencies()
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		return m_Pack.getClientDependencies();
	}

	/**
	 * @param type
	 * @return
	 */
	public ETList<IDependency> getClientDependenciesByType(String type)
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		return m_Pack.getClientDependenciesByType(type);
	}

	/**
	 * @return
	 */
	public long getClientDependencyCount()
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		return m_Pack.getClientDependencyCount();
	}

	/**
	 * @param useProjName
	 * @return
	 */
	public String getFullyQualifiedName(boolean useProjName)
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		return m_Pack.getFullyQualifiedName(useProjName);
	}

	/**
	 * @return
	 */
	public String getName()
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		return m_Pack.getName();
	}

	/**
	 * @return
	 */
	public INamespace getNamespace()
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		return m_Pack.getNamespace();
	}

	/**
	 * @return
	 */
	public String getQualifiedName()
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		return m_Pack.getQualifiedName();
	}

	/**
	 * @return
	 */
	public String getQualifiedName2()
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		return m_Pack.getQualifiedName2();
	}

	/**
	 * @return
	 */
	public ETList<IDependency> getSupplierDependencies()
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		return m_Pack.getSupplierDependencies();
	}

	/**
	 * @param type
	 * @return
	 */
	public ETList<IDependency> getSupplierDependenciesByType(String type)
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		return m_Pack.getSupplierDependenciesByType(type);
	}

	/**
	 * @return
	 */
	public long getSupplierDependencyCount()
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		return m_Pack.getSupplierDependencyCount();
	}

	/**
	 * @return
	 */
	public int getVisibility()
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		return m_Pack.getVisibility();
	}

	/**
	 * @return
	 */
	public boolean isAliased()
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		return m_Pack.isAliased();
	}

	/**
	 * @param dep
	 */
	public void removeClientDependency(IDependency dep)
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		m_Pack.removeClientDependency(dep);
	}

	/**
	 * @param dep
	 */
	public void removeSupplierDependency(IDependency dep)
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		m_Pack.removeSupplierDependency(dep);
	}

	/**
	 * @param str
	 */
	public void setAlias(String str)
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		m_Pack.setAlias(str);
	}

	/**
	 * @param str
	 */
	public void setName(String str)
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		m_Pack.setName(str);
	}

	/**
	 * @param space
	 */
	public void setNamespace(INamespace space)
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		m_Pack.setNamespace(space);
	}

	/**
	 * @param vis
	 */
	public void setVisibility(int vis)
	{
		if (m_Pack == null)
		{
			m_Pack = new PackageableElement();
		}
		m_Pack.setVisibility(vis);
	}
	public boolean isNameSame(IBehavioralFeature feature)
	{
		return true;
	}
	
	public String getNameWithAlias()
	{
		return m_Pack.getNameWithAlias();
	}
	public void setNameWithAlias(String newVal)
	{
		m_Pack.setNameWithAlias(newVal);
	}

    /**
     * The default behavior to this method is to return true if the names of the
     * two elements being compared are same. Subclasses should override to 
     * implement class specific <em>isSimilar</em> behavior.
     *
     * @param other The other named element to compare this named element to.
     * @return true, if the names are the same, otherwise, false.
     */
    public boolean isSimilar(INamedElement other)
    {
        if (!getName().equals(other.getName()) || !(other instanceof IDependency))
            return false;
        
        return true;
    }
}


