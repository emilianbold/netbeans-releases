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


package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.metamodel.core.foundation.DirectedRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.PreventElementReEntrance;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationshipEventsHelper;
import org.netbeans.modules.uml.core.support.umlsupport.URILocator;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

import org.dom4j.Node;
import org.dom4j.Document;


public class Generalization extends DirectedRelationship implements IGeneralization {
	public Generalization() {
		super();
	}

	/**
	 * property Specific
	 */
	public IClassifier getSpecific() {
		ElementCollector < IClassifier > collector = new ElementCollector < IClassifier > ();
		return collector.retrieveSingleElementWithAttrID(this, "specific", IClassifier.class);
	}

	/**
	 * property Specific
	*/
	public void setSpecific(IClassifier specific) {
		PreventElementReEntrance reEnt = new PreventElementReEntrance(this);
		try {
			if (!reEnt.isBlocking()) {
				RelationshipEventsHelper help = new RelationshipEventsHelper(this);
				if (help.firePreEndModified("specific", specific, null)) {
					final IClassifier classifier = specific;
					new ElementConnector < IGeneralization > ().setSingleElementAndConnect(this, classifier, "specific", new IBackPointer < IClassifier > () {
						public void execute(IClassifier obj) {
							obj.addGeneralization(Generalization.this);
						}
					}, new IBackPointer < IClassifier > () {
						public void execute(IClassifier obj) {
							obj.removeGeneralization(Generalization.this);
						}
					});
					help.fireEndModified();
				} else {
					//throw exception
				}
			}
		} finally {
			reEnt.releaseBlock();
		}
	}

	/**
	 * property General
	*/
	public IClassifier getGeneral() {
		ElementCollector < IClassifier > collector = new ElementCollector < IClassifier > ();
		return collector.retrieveSingleElementWithAttrID(this, "general", IClassifier.class);
	}

	/**
	 * property General
	*/
	public void setGeneral(IClassifier general) {
		PreventElementReEntrance reEnt = new PreventElementReEntrance(this);
		try {
			if (!reEnt.isBlocking()) {
				RelationshipEventsHelper help = new RelationshipEventsHelper(this);
				if (help.firePreEndModified("general", null, general)) {
					final IClassifier genClassifier = general;
					new ElementConnector < IGeneralization > ().setSingleElementAndConnect(this, genClassifier, "general", new IBackPointer < IClassifier > () {
						public void execute(IClassifier obj) {
							obj.addSpecialization(Generalization.this);
						}
					}, new IBackPointer < IClassifier > () {
						public void execute(IClassifier obj) {
							obj.removeSpecialization(Generalization.this);
						}
					});
					help.fireEndModified();
				} else {
					//throw exception
				}
			}
		} finally {
			reEnt.releaseBlock();
		}
	}

	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */
	public void establishNodePresence(Document doc, Node parent) {
		buildNodePresence("UML:Generalization", doc, parent);
	}

	/**	
	 * Handles the firing of the delete event. First makes sure that all elements
	 * in this relationship are set to a dirty state.
	 *	 
	 */
	public void fireDelete(IVersionableElement ver) {
		IClassifier superr = getGeneral();
		IClassifier sub = getSpecific();
		if (sub != null)
			sub.setDirty(true);
		if (superr != null)
			superr.setDirty(true);

		super.fireDelete(ver);
	}

	// Pass through methods for INamedElement

	//  Sets / Gets the name of this element.
	//    HRESULT Name([out, retval] BSTR* curVal );
	public String getName() {
		return getNamedElement().getName();
	}

	//  Sets / Gets the name of this element.
	//    HRESULT Name([in] BSTR newName );
	public void setName(String str) {
		getNamedElement().setName(str);
	}

	//  Sets / Gets the visibility of this element.
	//    HRESULT Visibility([out, retval] VisibilityKind* vis );
	public int getVisibility() {
		return getNamedElement().getVisibility();
	}

	//  Sets / Gets the visibility of this element.
	//    HRESULT Visibility([in] VisibilityKind vis );
	public void setVisibility(int vis) {
		getNamedElement().setVisibility(vis);
	}

	//  Sets / Gets the Namespace of this element.
	//    HRESULT Namespace([out, retval] INamespace** curSpace );
	public INamespace getNamespace() {
		return getNamedElement().getNamespace();
	}

	//  Sets / Gets the Namespace of this element.
	//    HRESULT Namespace([in] INamespace* space );
	public void setNamespace(INamespace space) {
		getNamedElement().setNamespace(space);
	}

	//  Adds a supplier dependency relationship to this element.
	//    HRESULT AddSupplierDependency([in] IDependency* dep );
	public void addSupplierDependency(IDependency dep) {
		getNamedElement().addSupplierDependency(dep);
	}

	//            Removes a supplier dependency relationship from this element.
	//    HRESULT RemoveSupplierDependency([in] IDependency* dep );
	public void removeSupplierDependency(IDependency dep) {
		getNamedElement().removeSupplierDependency(dep);
	}

	//            Retrieves the collection of Dependencies where this element plays the supplier role.
	//    HRESULT SupplierDependencies([out, retval] IDependencies** deps);
	public ETList < IDependency > getSupplierDependencies() {
		return getNamedElement().getSupplierDependencies();
	}

	//            Retrieves the collection of Dependencies where this element plays the supplier role.  The Dependencies are of type sElementType.
	//    HRESULT SupplierDependenciesByType([in]BSTR sElementType, [out, retval] IDependencies** deps);
	public ETList < IDependency > getSupplierDependenciesByType(String type) {
		return getNamedElement().getSupplierDependenciesByType(type);
	}

	//            Adds a client dependency relationship to this element.
	//    HRESULT AddClientDependency([in] IDependency* dep );
	public void addClientDependency(IDependency dep) {
		getNamedElement().addClientDependency(dep);
	}

	//            Removes a client dependency relationship from this element.
	//    HRESULT RemoveClientDependency([in] IDependency* dep );
	public void removeClientDependency(IDependency dep) {
		getNamedElement().removeClientDependency(dep);
	}

	//            Retrieves the collection of Dependencies where this element plays the client role.
	//    HRESULT ClientDependencies([out, retval] IDependencies** deps );
	public ETList < IDependency > getClientDependencies() {
		return getNamedElement().getClientDependencies();
	}

	//            Retrieves the collection of Dependencies where this element plays the client role.  The Dependencies are of type sElementType.
	//    HRESULT ClientDependenciesByType([in]BSTR sElementType, [out, retval] IDependencies** deps);
	public ETList < IDependency > getClientDependenciesByType(String type) {
		return getNamedElement().getClientDependenciesByType(type);
	}

	//  Retrieves the fully qualified name of the element. Project name is included based on the user preference.  This will be in the form '[ProjectName::]A::B::C'.
	//    HRESULT QualifiedName([out, retval] BSTR* name );
	public String getQualifiedName() {
		return getNamedElement().getQualifiedName();
	}

	//  Retrieves the fully qualified name of the element. This will be in the form '[ProjectName::]A::B::C'.
	//    HRESULT FullyQualifiedName([in] VARIANT_BOOL useProjectName, [out, retval] BSTR* name );
	public String getFullyQualifiedName(boolean useProjName) {
		return getNamedElement().getFullyQualifiedName(useProjName);
	}

	//  Used to establish a different name for this element.
	//    HRESULT Alias([out, retval] BSTR* curVal );
	public String getAlias() {
		return getNamedElement().getAlias();
	}

	//  Used to establish a different name for this element.
	//    HRESULT Alias([in] BSTR newName );
	public void setAlias(String str) {
		getNamedElement().setAlias(str);
	}

	//  Does this element have an aliased name?
	//    HRESULT IsAliased([out, retval] VARIANT_BOOL* bIsAliased );
	public boolean isAliased() {
		return getNamedElement().isAliased();
	}

	//  .
	//    HRESULT SupplierDependencyCount([out, retval] long* pVal);
	public long getSupplierDependencyCount() {
		return getNamedElement().getSupplierDependencyCount();
	}

	//  .
	//    HRESULT ClientDependencyCount([out, retval] long* pVal);
	public long getClientDependencyCount() {
		return getNamedElement().getClientDependencyCount();
	}

	//  Retrieves the fully qualified name of the element. Project name is never included.  This will be in the form 'A::B::C'.
	//    HRESULT QualifiedName2([out, retval] BSTR* name );
	public String getQualifiedName2() {
		return getNamedElement().getQualifiedName2();
	}

	public boolean isNameSame(IBehavioralFeature feature) {
		return getNamedElement().isNameSame(feature);
	}

	public String getNameWithAlias() {
		return getNamedElement().getNameWithAlias();
	}

	public void setNameWithAlias(String newVal) {
		getNamedElement().setNameWithAlias(newVal);
	}

	// Helper methods for INamedElement

	//  Sets the XML node associated with this element.
	// HRESULT Node([in] IXMLDOMNode* newVal);
	public void setNode(Node n) {
		super.setNode(n);

		if (m_namedElement != null) {
			m_namedElement.setNode(getNode());
		}
	}

	protected INamedElement getNamedElement() {
		if (null == m_namedElement) {
			m_namedElement = new NamedElement();
			m_namedElement.setNode(getNode());
		}

		return m_namedElement;
	}

	private INamedElement m_namedElement;

	private String getInternalXMIID(final String specificOrGeneral)
	{
		String sXMIID = null;
		try {
			Node node = getNode();
			if (node != null) {
				String sID = XMLManip.getAttributeValue(node, specificOrGeneral);
				if (sID != null && sID.length() > 0) {
					// Remove the __uri in case it's been versioned

					sXMIID = URILocator.retrieveRawID(sID);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sXMIID;		
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization#getGeneralXMIID()
	 */
	public String getGeneralXMIID() {
		return getInternalXMIID("general");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization#getSpecificXMIID()
	 */
	public String getSpecificXMIID() {
		return getInternalXMIID("specific");
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
        if (!getName().equals(other.getName()) || !(other instanceof IGeneralization))
            return false;

        return true;
    }
}
