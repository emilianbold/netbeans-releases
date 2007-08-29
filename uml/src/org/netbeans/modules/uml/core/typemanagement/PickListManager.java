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


package org.netbeans.modules.uml.core.typemanagement;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.IQueryUpdater;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespaceModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IImportEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NameResolver;
import org.netbeans.modules.uml.core.metamodel.core.foundation.PreventElementReEntrance;
import org.netbeans.modules.uml.core.metamodel.core.foundation.PreventReEntranceByValue;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.PathManip;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class PickListManager implements IPickListManager, IQueryUpdater,
        INamedElementEventsSink,
        IClassifierTransformEventsSink,
        INamespaceModifiedEventsSink,
        IElementLifeTimeEventsSink,
        IImportEventsSink
{
    public static int TS_NOT_MODIFIED = 0;
    public static int TS_NEW = TS_NOT_MODIFIED + 1;
    public static int TS_MODIFIED = TS_NEW + 1;
    public static int TS_DELETED = TS_MODIFIED + 1;
    
    private static int s_NextIndex = 0;
    private ITypeManager m_rawTypeManager = null;
    private String m_ProjectID = "";
    private String m_ProjectLocation = "";
    
    //Hashtable<int, String> m _ElementTypeMap = null;
    private Hashtable m_ElementTypeMap = new Hashtable();
    
    //Hashtable<String, NamedType> m_ElementMap = null;
    private Hashtable m_ElementMap = new Hashtable();
    
    //Hashtable<String, Vector<NamedType>> m_NameTypeCache = null;
    private Hashtable m_NameTypeCache = new Hashtable();
    
    //Hashtable<String, Vector<NamedType>> m_TypeMap = null;
    private Hashtable m_TypeMap = new Hashtable();
    
    //Hashtable<String, int> m_TypesCached = null;
    private Hashtable m_TypesCached = new Hashtable();
    /**
     *
     */
    public PickListManager()
    {
        super();
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.typemanagement.IPickListManager#getTypesWithFilter(org.netbeans.modules.uml.core.support.umlsupport.IStrings)
         */
    public IElement getTypesWithFilter(IStrings typeFilter)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.typemanagement.IPickListManager#getTypesOfType(java.lang.String)
         */
    public ETList<IElement> getTypesOfType(String type)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.typemanagement.IPickListManager#getTypesWithStringFilter(java.lang.String)
         */
    public ETList<IElement> getTypesWithStringFilter(String filter)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     *
     * Retrieves all the elements in the type cache that match the types found in
     * the typeFilter collection
     *
     * @param typeFilter[in]   The collection of type names used to filter the name by
     * @param typeNames[out]   The collection of found names
     *
     * @return HRESULT
     *
     */
    public IStrings getTypeNamesWithFilter(IStrings typeFilter)
    {
        return getTypeNamesWithFilter(typeFilter, false);
    }
    
    /**
     *
     * Retrieves the names of a given type of element
     *
     * @param type[in]         The type of element that should be retrieved
     * @param typeNames[out]   The name of elements of type type. If the collection coming
     *                         in has a value, it will be added to.
     *
     * @return HRESULT
     *
     */
    public IStrings getTypeNamesOfType(String type)
    {
        return getTypeNamesOfType(type, false, null);
    }
    
    /**
     *
     * Retrieves the types that match the type names passed in via the filter parameter.
     *
     * @param filter[in]       A space delimited list of type names
     * @param typeNames[out]   The found types
     *
     * @return HRESULT
     *
     */
    public IStrings getTypeNamesWithStringFilter(String filter)
    {
        return getTypeNamesWithStringFilter(filter, false);
    }
    
    public IStrings getFullyQualifiedTypeNamesWithFilter(IStrings typeFilter)
    {
        return getTypeNamesWithFilter(typeFilter, true);
    }
    
    public IStrings getFullyQualifiedTypeNamesOfType(String type)
    {
        return getTypeNamesOfType(type, true, null);
    }
    
    public IStrings getFullyQualifiedTypeNamesWithStringFilter(String filter)
    {
        return getTypeNamesWithStringFilter(filter, true);
    }
    
    /**
     *
     * Builds up the internal maps of types
     *
     * @param context[in]      The IProject coming in that this PickListManager is associated with
     * @param queryCache[in]   The actual .QueryCache document
     *
     * @return HRESULT
     *
     */
    public void initialize(Object context, Document queryCache)
    {
        registerListeners();
        if (context instanceof IProject)
        {
            IProject project = (IProject)context;
            attachToTypeManager(project);
            
            // Build the type cache
            List nameNodes = queryCache.selectNodes("//Name");
            if (nameNodes != null)
            {
                int count = nameNodes.size();
                for (int i=0; i<count; i++)
                {
                    Node node = (Node)nameNodes.get(i);
                    String name = XMLManip.getAttributeValue(node, "name");
                    String alias = XMLManip.getAttributeValue(node, "alias");
                    String nodeType = XMLManip.getAttributeValue(node, "nodeType_");
                    String id = XMLManip.getAttributeValue(node, "id");
                    String fullName = XMLManip.getAttributeValue(node, "fullName");
                    
                    if (id == null) continue;
                    NamedType newType = new NamedType(name, alias, id, fullName, addTypeIndex(nodeType));
                    
                    Object obj1 = m_TypeMap.get(nodeType);
                    if (obj1 != null)
                    {
                        Vector col1 = (Vector)obj1;
                        col1.add(newType);
                    }
                    else
                    {
                        Vector<NamedType> col1 = new Vector<NamedType>();
                        col1.add(newType);
                        m_TypeMap.put(nodeType, col1);
                    }
                    
                    m_ElementMap.put(id, newType);
                    
                    Object obj2 = m_NameTypeCache.get(name);
                    if (obj2 != null)
                    {
                        Vector col2 = (Vector)obj2;
                        col2.add(newType);
                    }
                    else
                    {
                        Vector<NamedType> col2 = new Vector<NamedType>();
                        col2.add(newType);
                        m_NameTypeCache.put(name, col2);
                    }
                }
                addDefaultTypes();
            }
        }
    }
    
    /**
     *
     * This is called by the QueryManager whenever the .QueryCache is about to be
     * closed. This is our chance to update the cache. This should only be called once
     * in this PickListManager's lifetime. By this time, the PickListManager is no
     * longer listening to event sinks and is no longer attached to a TypeManager.
     *
     * @param queryCache[in] The doc that represents the .QueryCache file
     *
     * @return HRESULT
     *
     */
    public void updateCache(Document queryCache)
    {
        Enumeration iter = m_ElementMap.elements();
        Enumeration iter2 = m_ElementMap.keys();
        while (iter.hasMoreElements())
        {
            NamedType type = (NamedType)iter.nextElement();
            String id = (String)iter2.nextElement();
            if (type.getState() == TS_NEW)
            {
                createNewResult(queryCache, type);
            }
            else if (type.getState() == TS_MODIFIED)
            {
                updateResult(queryCache, type, id);
            }
            else if (type.getState() == TS_DELETED)
            {
                deleteResult(queryCache, id);
            }
        }
        
        // Clean up memory allocations
        m_ElementMap.clear();
        m_NameTypeCache.clear();
    }
    
    /**
     *
     * Cleans up the cached type information
     *
     * @return HRESULT
     *
     */
    public void deinitialize()
    {
        revokeListeners();
        m_rawTypeManager = null;
    }
    
    /**
     *
     * Retrieves the TypeManager associated with this manager
     *
     * @param pVal[out] The manager
     *
     * @return HRESULT
     *
     */
    public ITypeManager getTypeManager()
    {
        return m_rawTypeManager;
    }
    
    /**
     *
     * Associates the passed in TypeManager with this manager
     *
     * @param newVal[in] The type manager
     *
     * @return HRESULT
     *
     */
    public void setTypeManager(ITypeManager value)
    {
        if (m_rawTypeManager != null)
        {
            m_rawTypeManager = null;
        }
        
        m_rawTypeManager = value;
        m_ProjectID = "";
        m_ProjectLocation = "";
        
        if (m_rawTypeManager != null)
        {
            IProject proj = m_rawTypeManager.getProject();
            if (proj != null)
            {
                m_ProjectID = proj.getXMIID();
                m_ProjectLocation = proj.getFileName();
            }
        }
    }
    
    /**
     *
     * Retrieves the id of the first element found with elementName as its name.
     *
     * @param elementName[in]  The name to match against
     * @param idOfElement[out] The id of the found element, else 0
     *
     * @return HRESULT
     *
     */
    public String getIDByName(String elementName)
    {
        String retStr = "";
        if (elementName != null && elementName.length() > 0)
        {
            IStrings ids = getIDsByName(elementName);
            if (ids != null)
            {
                long count = ids.getCount();
                if (count > 0)
                {
                    retStr = ids.item(0);
                }
            }
        }
        return retStr;
    }
    
    /**
     *
     * Retrieves the ids of the elements found with elementName as its name. If the type
     * is not found in the immediate cache, reference libraries will be checked.
     *
     * @param elementName[in]  The name to match against
     * @param idsOfElement[out] The ids of the found elements, else 0
     *
     * @return PL_S_TYPES_FROM_EXTERNAL_PROJECT if the types were retrieved from an
     *         external project, else S_OK
     *
     */
    public IStrings getIDsByName(String elementName)
    {
        return getIDsByName(elementName, true);
    }
    
    /**
     *
     * Retrieves the ids of the elements found with elementName as its name. The query
     * will be local to the Project this manager is associated with. No searches
     * in reference libraries will be made
     *
     * @param elementName[in]  The name to match against
     * @param idsOfElement[out] The ids of the found elements, else 0
     *
     * @return HRESULT
     *
     */
    public IStrings getLocalIDsByName(String elementName)
    {
        return getIDsByName( elementName, false );
    }
    
    /**
     *
     * Adds the passed in type to this PickListManager's cache of known types
     *
     * @param element[in]   The element to add. If the element is already a
     *                      part of the cache, that element's information in the
     *                      cache is simply updated
     *
     * @return HRESULT
     *
     */
    public void addExternalNamedType(INamedElement pNamedElement)
    {
        addNamedType(pNamedElement, true);
    }
    public void addNamedType(INamedElement pNamedElement)
    {
        addNamedType(pNamedElement, false);
    }
    private void addNamedType(INamedElement pNamedElement, boolean external)
    {
        if (processElement(pNamedElement, external))
        {
            String id = pNamedElement.getXMIID();
            NamedType type = (NamedType)m_ElementMap.get(id);
            if (type != null)
            {
                String name = pNamedElement.getName();
                type.setName(name);
                String fullName = pNamedElement.getQualifiedName2();
                type.setFullName(fullName);
                
                Object obj2 = m_NameTypeCache.get(name);
                if (obj2 != null)
                {
                    Vector col2 = (Vector)obj2;
                    col2.add(type);
                }
                else
                {
                    Vector<NamedType> col2 = new Vector<NamedType>();
                    col2.add(type);
                    m_NameTypeCache.put(name, col2);
                }
            }
            else
            {
                addNewType(pNamedElement);
            }
        }
    }
    
    /**
     *
     * Removes the type from the cache that has the passed in name and the ID of the passed
     * in element
     *
     * @param element[in]   The element to match against
     * @param curName[in]   The name to remove
     *
     * @return HRESULT
     *
     */
    public void removeNamedType(INamedElement element, String curName)
    {
        if (processElement(element))
        {
            Object obj = m_NameTypeCache.get(curName);
            if (obj != null)
            {
                String curElemId = element.getXMIID();
                if (curElemId != null && curElemId.length() > 0)
                {
                    Vector types = (Vector)obj;
                    for (int i=0; i<types.size(); i++)
                    {
                        NamedType type = (NamedType)types.get(i);
                        String id = type.getId();
                        
                        // Remove the entry from the vector, as this name is changing
                        if (id != null && id.length() > 0)
                        {
                            if (id.equals(curElemId))
                            {
                                types.remove(type);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     *
     * Retrieves the first type that matches the passed in name and type specified in the space delimited name of meta types.
     *
     * @param elementName[in]     The name of the element to match
     * @param filter[in]          A space delimited list of meta types, such as 'Class Interface DataType'
     * @param foundElements[out]  The collection of found elements
     *
     * @return HRESULT
     * @warning If multiple types are found by a specific name, the first element found to match the first meta type will be returned
     *
     */
    public IElement getElementByNameAndStringFilter(String elementName, String filter)
    {
        IElement retEle = null;
        ETList<IElement> elements = getElementsByNameAndStringFilter(elementName, filter);
        if (elements != null)
        {
            int count = elements.size();
            if (count > 0)
            {
                retEle = elements.get(0);
            }
        }
        return retEle;
    }
    
    /**
     *
     * Retrieves the collection of types that match the passed in name of space delimited name of meta types.
     *
     * @param elementName[in]     The name of the element to match
     * @param filter[in]          A space delimited list of meta types, such as 'Class Interface DataType'
     * @param foundElements[out]  The collection of found elements
     *
     * @return HRESULT
     *
     */
    public ETList<IElement> getElementsByNameAndStringFilter(String elementName, String filter)
    {
        ETList<IElement> retObj = null;
        if (filter != null && filter.length() > 0)
        {
            StringTokenizer tokenizer = new StringTokenizer(filter, " ");
            while (tokenizer.hasMoreTokens())
            {
                String typeName = tokenizer.nextToken();
                IElement foundElement = getElementByNameAndType(elementName, typeName);
                if (foundElement != null)
                {
                    if (retObj == null)
                    {
                        retObj = new ETArrayList<IElement>();
                    }
                    retObj.add(foundElement);
                }
            }
        }
        return retObj;
    }
    
    /**
     *
     * Retrieves a single type based on its name and its type
     *
     * @param elementName[in]  The name of the element to retrieve
     * @param sType[in]        The type to filter by
     * @param foundElement[out] The found element
     *
     * @return HRESULT
     *
     */
    public IElement getElementByNameAndType(String elementName, String sType)
    {
        IElement retEle = null;
        if (sType != null && sType.length() > 0 && m_rawTypeManager != null)
        {
            Vector<NamedType> types = getTypesByName(elementName);
            if (types != null)
            {
                for (int i=0; i<types.size(); i++)
                {
                    NamedType type = types.get(i);
                    String elementType = type.getElemType();
                    if (elementType != null && elementType.length() > 0)
                    {
                        if (elementType.equals(sType))
                        {
                            IVersionableElement ver = m_rawTypeManager.getElementByID(type.getId());
                            if (ver != null && ver instanceof IElement)
                            {
                                retEle = (IElement)ver;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return retEle;
    }
    
    public void onPreNameModified(INamedElement element, String proposedName, IResultCell cell)
    {
        try
        {
            // This was commented out, as NamedElementImpl is now calling AddNamedType() directly
            // to make sure that named types get added to the PickListManager regardless of whether
            // or not events are firing or not.
            //
            // Also, AddNamedType() is smart in that if element is cached already, we find
            // it by ID and updated the name information
            
            //hr = RemoveNamedType( element, proposedName );
        }
        catch( Exception e )
        {
        }
        
    }
    
    /**
     *
     * Handles the name change of elements in the cache
     *
     * @param element[in]      The element whose name has just changed
     *
     * @return HRESULT
     *
     */
    public void onNameModified(INamedElement element, IResultCell cell)
    {
        try
        {
            // This was commented out, as NamedElementImpl is now calling AddNamedType() directly
            // to make sure that named types get added to the PickListManager regardless of whether
            // or not events are firing or not.
            
            //hr = AddNamedType( element );
        }
        catch( Exception e )
        {
        }
    }
    
    public void onPreVisibilityModified(INamedElement element, int proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onVisibilityModified(INamedElement element, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPreAliasNameModified(INamedElement element, String proposedName, IResultCell cell)
    {
        //nothing to do
    }
    
    /**
     *
     * Handles the alias name change of elements in the cache
     *
     * @param element[in]      The element whose alias has just changed
     *
     * @return HRESULT
     *
     */
    public void onAliasNameModified(INamedElement element, IResultCell cell)
    {
        if (processElement(element))
        {
            String id = element.getXMIID();
            NamedType type = (NamedType)m_ElementMap.get(id);
            if (type != null)
            {
                String alias = element.getAlias();
                type.setAlias(alias);
            }
        }
    }
    
    public void onPreNameCollision(INamedElement element, String proposedName, ETList<INamedElement> collidingElements, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onNameCollision(INamedElement element, ETList<INamedElement> collidingElements, IResultCell cell)
    {
        //nothing to do
    }
    
    /**
     *
     * Handles element creates
     *
     * @param element[in]
     * @param IResultCell*[in]
     *
     * @return
     *
     */
    public void onElementCreated(IVersionableElement element, IResultCell cell)
    {
        if (processElement(element))
        {
            addNewType(element);
        }
    }
    
    public void onElementPreCreate( String ElementType, IResultCell cell )
    {
        //nothing to do
    }
    
    public void onElementPreDelete(IVersionableElement element, IResultCell cell)
    {
        //nothing to do
    }
    
    /**
     *
     * Handles the delete of elements from the cache
     *
     * @param element[in]
     * @param IResultCell*[in]
     *
     * @return
     *
     */
    public void onElementDeleted(IVersionableElement element, IResultCell cell)
    {
        if (processElement(element))
        {
            if (element instanceof INamedElement)
                removeTypeFromCache((INamedElement)element);
        }
    }
    
    private void removeTypeFromCache(INamedElement element)
    {
        if (element instanceof INamespace)
        {
            ETList<INamedElement> list = ((INamespace)element).getOwnedElements();
            for (INamedElement e: list)
            {
                removeTypeFromCache(e);
            }
        }
        remove(element);
    }
    
    
    private void remove(IElement e)
    {
        String id = e.getXMIID();
        NamedType type = (NamedType)m_ElementMap.get(id);
        if (type != null)
        {
            type.setState(TS_DELETED);
            m_ElementMap.remove(type);
            Object obj2 = m_NameTypeCache.get(type.getName());
            if (obj2 != null)
            {
                Vector col2 = (Vector)obj2;
                col2.remove(type);
            }
        }
    }
    
    public void onElementPreDuplicated(IVersionableElement element, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onElementDuplicated(IVersionableElement element, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPreTransform(IClassifier classifier, String newForm, IResultCell cell)
    {
        if (processElement(classifier))
        {
            String id = classifier.getXMIID();
            
            // We need to make sure to remove the element in the m_TypeMap
            // before we lose the type that the classifier currently is.
            // This is because in the post transform, we will be deleting
            // the pointer
            NamedType type = (NamedType)m_ElementMap.get(id);
            if (type != null)
            {
                String curType = classifier.getElementType();
                Object obj = m_TypeMap.get(curType);
                if (obj != null)
                {
                    Vector col1 = (Vector)obj;
                    Enumeration iter = col1.elements();
                    while(iter.hasMoreElements())
                    {
                        Object item = iter.nextElement();
                        if (item.equals(type))
                        {
                            col1.remove(item);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    /**
     *
     * Handles the transform of one type to another
     *
     * @param classifier[in]
     * @param cell[in]
     *
     * @return
     *
     */
    public void onTransformed(IClassifier classifier, IResultCell cell)
    {
        if (processElement(classifier))
        {
            if (isTypeCached(classifier))
            {
                String id = classifier.getXMIID();
                NamedType type = (NamedType)m_ElementMap.get(id);
                if (type != null)
                {
                    m_ElementMap.remove(id);
                    TypedNamedType newType = new TypedNamedType(classifier);
                    m_ElementMap.put(id, newType);
                    
                    Object obj = m_TypeMap.get(newType.getElemType());
                    if (obj != null)
                    {
                        Vector col1 = (Vector)obj;
                        col1.add(newType);
                    }
                    else
                    {
                        Vector<NamedType> col1 = new Vector<NamedType>();
                        col1.add(newType);
                        m_TypeMap.put(newType.getElemType(), col1);
                    }
                }
            }
        }
    }
    
    public void onPreElementAddedToNamespace(INamespace space, INamedElement elementToAdd, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onElementAddedToNamespace(INamespace space, INamedElement elementAdded, IResultCell cell)
    {
        if (elementAdded != null)
        {
            String id = elementAdded.getXMIID();
            NamedType type = (NamedType)m_ElementMap.get(id);
            if (type != null)
            {
                String fullName = elementAdded.getQualifiedName();
                type.setFullName(fullName);
            }
        }
    }
    
    // IImportEventsSink interface implementation ...........................
    
    public void onElementImported(IElementImport elImport, IResultCell cell)
    {
        if (processElement(elImport, true))
        {
            addNewType(elImport);
        }
    }
    
    public void onPackageImported(IPackageImport packImport, IResultCell cell)
    {
        
    }
    
    public void onPreElementImport(IPackage importingPackage,
            IElement element,
            INamespace owner,
            IResultCell cell)
    {
        
    }
    
    public void onPrePackageImport(IPackage importingPackage,
            IPackage importedPackage,
            INamespace owner,
            IResultCell cell)
    {
        
    }
    
    
    
    
    /**
     *
     * Revokes from the connected listeners
     *
     * @return HRESULT
     *
     */
    private void revokeListeners()
    {
        EventDispatchRetriever nameRet = EventDispatchRetriever.instance();
        IElementChangeEventDispatcher nameDisp = null;
        Object obj = nameRet.getDispatcher(EventDispatchNameKeeper.modifiedName());
        if (obj != null)
        {
            nameDisp = (IElementChangeEventDispatcher)obj;
        }
        
        if (nameDisp != null)
        {
            nameDisp.revokeNamedElementSink(this);
            nameDisp.revokeNamespaceModifiedSink(this);
            nameDisp.revokeImportEventsSink(this);
        }
        IEventDispatchController controller = nameRet.getController();
        
        IElementLifeTimeEventDispatcher lifeDisp = null;
        Object lifeObj = nameRet.getDispatcher(EventDispatchNameKeeper.lifeTime());
        if (lifeObj != null)
        {
            lifeDisp = (IElementLifeTimeEventDispatcher)lifeObj;
        }
        
        if (lifeDisp != null)
        {
            lifeDisp.revokeLifeTimeSink(this);
        }
        
        IClassifierEventDispatcher classDisp = null;
        Object classObj = nameRet.getDispatcher(EventDispatchNameKeeper.classifier());
        if (classObj != null)
        {
            classDisp = (IClassifierEventDispatcher)classObj;
        }
        if (classDisp != null)
        {
            classDisp.revokeTransformSink(this);
        }
    }
    
    /**
     *
     * Registers as a listener to a number of event dispatchers
     *
     * @return HRESULT
     *
     */
    private void registerListeners()
    {
        EventDispatchRetriever nameRet = EventDispatchRetriever.instance();
        IElementChangeEventDispatcher nameDisp = null;
        Object obj = nameRet.getDispatcher(EventDispatchNameKeeper.modifiedName());
        if (obj != null)
        {
            nameDisp = (IElementChangeEventDispatcher)obj;
        }
        
        if (nameDisp != null)
        {
            nameDisp.registerForNamedElementEvents(this);
            nameDisp.registerForNamespaceModifiedEvents(this);
            nameDisp.registerForImportEventsSink(this);
        }
        IEventDispatchController controller = nameRet.getController();
        
        IElementLifeTimeEventDispatcher lifeDisp = null;
        Object lifeObj = nameRet.getDispatcher(EventDispatchNameKeeper.lifeTime());
        if (lifeObj != null)
        {
            lifeDisp = (IElementLifeTimeEventDispatcher)lifeObj;
        }
        
        if (lifeDisp != null)
        {
            lifeDisp.registerForLifeTimeEvents(this);
        }
        
        IClassifierEventDispatcher classDisp = null;
        Object classObj = nameRet.getDispatcher(EventDispatchNameKeeper.classifier());
        if (classObj != null)
        {
            classDisp = (IClassifierEventDispatcher)classObj;
        }
        if (classDisp != null)
        {
            classDisp.registerForTransformEvents(this);
        }
    }
    
    /**
     *
     * Sets the PickListManager on the TypeManager of the Project
     *
     * @param project[in]   The Project
     *
     * @return HRESULT
     *
     */
    private void attachToTypeManager(IProject project)
    {
        ITypeManager man = project.getTypeManager();
        if (man != null)
        {
            man.setPickListManager(this);
        }
    }
    
    /**
     *
     * Creates a new element that is inserted into the cache document
     *
     * @param cache[in]  The document to update
     * @param type[in]   The type that holds the information that need to be created and
     *                   placed into the document
     *
     * @return HRESULT
     *
     */
    private void createNewResult(Document cache, NamedType type )
    {
        if (type instanceof NewNamedType)
        {
            NewNamedType newType = (NewNamedType)type;
            String fileName = newType.getFileName();
            fileName = getRelativePathToProject(fileName);
            //XMLManip.translateIllegals(fileName);
            String query = "//CachedQueries";
            if (fileName != null && fileName.length() > 0)
            {
                query += "[@fileName=\"";
                query += fileName;
                query += "\"]";
            }
            else
            {
                // If the new element doesn't have a filename, that is
                // probably due to the fact that the element was just created
                // but not part of the Project yet, so we weren't able to
                // object a file name. In this case, we will assume that the
                // type should be placed in the Project cache element
                
                query += "[1]";
            }
            
            Node queryElem = cache.selectSingleNode(query);
            
            // This should only fail in the case where we are updating a versioned element
            // file for the first time. The next time the user opens the project, the
            // .QueryCache file will update with the new information
            if (queryElem != null)
            {
                Node newNode = XMLManip.createElement((Element)queryElem, "Name");
                if (newNode != null)
                {
                    XMLManip.setAttributeValue(newNode, "nodeType_", newType.getElemType());
                    XMLManip.setAttributeValue(newNode, "name", newType.getName());
                    XMLManip.setAttributeValue(newNode, "id", newType.getId());
                    XMLManip.setAttributeValue(newNode, "alias", newType.getAlias());
                    XMLManip.setAttributeValue(newNode, "fullName", newType.getFullName());
                }
            }
        }
    }
    
    /**
     *
     * Updates a node in the cache with new information
     *
     * @param cache[in]  The cache doc
     * @param type[in]   The type to update
     * @param id[in]     ID of the type being updated
     *
     * @return HRESULT
     *
     */
    private void updateResult(Document cache, NamedType type, String id )
    {
        Node node = getNameElementByID(cache, id);
        if (node != null)
        {
            // If a type was transformed...
            if (type instanceof TypedNamedType)
            {
                XMLManip.setAttributeValue(node, "nodeType_", ((TypedNamedType)type).getElemType());
            }
            XMLManip.setAttributeValue(node, "name", type.getName());
            XMLManip.setAttributeValue(node, "alias", type.getAlias());
            XMLManip.setAttributeValue(node, "fullName", type.getFullName());
        }
        else
        {
            // Check to see if we actually have a new type here
            if (type instanceof NewNamedType)
            {
                createNewResult(cache, type);
            }
        }
    }
    
    /**
     *
     * Deletes a node from the cache
     *
     * @param cache[in]  The cache doc
     * @param id[in]     The id of the node to delete
     *
     * @return HRESULT
     *
     */
    private void deleteResult(Document cache, String id )
    {
        Node node = getNameElementByID(cache, id);
        if (node != null)
        {
            Node parent = node.getParent();
            if (parent != null)
            {
                node.detach();
            }
        }
    }
    
    /**
     *
     * Retrieves a Name element from the Cache file by the id passed in
     *
     * @param cache[in]        The cache doc
     * @param id[in]           The id to find
     * @param nameNode[out]    The found node, else 0
     *
     * @return HRESULT
     *
     */
    private Node getNameElementByID(Document cache, String id)
    {
        String query =  "//Name[@id='" ;
        query += id;
        query += "']";
        Node node = cache.selectSingleNode(query);
        return node;
    }
    
    /**
     *
     * Determines whether or not the type of the element passed in is a type
     * that is currently being cached
     *
     * @param namedElement[in] The element to check.
     *
     * @return true if the type is cached, else false
     *
     */
    private boolean isTypeCached( INamedElement namedElement )
    {
        boolean isCached = false;
        
        String nodeType = namedElement.getElementType();
        Object obj = m_TypesCached.get(nodeType);
        if (obj != null)
        {
            isCached = true;
        }
        return isCached;
    }
    
    /**
     *
     * Makes sure that the types that we certainly want to upkeep are added to our list
     * of cacheable types.
     *
     */
    private void addDefaultTypes()
    {
        addTypeIndex( "Class" );
        addTypeIndex( "Interface" );
        addTypeIndex( "Package" );
        addTypeIndex( "DataType" );
        addTypeIndex( "Enumeration" );
        addTypeIndex( "Collaboration" );
        addTypeIndex( "Component" );
        addTypeIndex( "State" );
        addTypeIndex( "Project" );
        addTypeIndex( "ComponentAssembly" );
        addTypeIndex( "Signal" );
        addTypeIndex( "InvocationNode" );
        addTypeIndex( "Actor" );
        addTypeIndex( "UseCase" );
        addTypeIndex( "Stereotype" );
    }
    
    /**
     *
     * Appends the types from any reference projects to the current list of types in the
     * Project this PickListManager manages
     *
     * @param type[in]            The name of the meta type to retrieve
     * @param curCollection[in]   The current collection of found types
     *
     * @return HRESULT
     *
     */
    private IStrings addReferencedTypes( String type, IStrings curCollection )
    {
        if (m_rawTypeManager != null)
        {
            ETList<IProject> libProjs = m_rawTypeManager.getReferencedLibraryProjects();
            if (libProjs != null)
            {
                int count = libProjs.size();
                for (int i=0; i<count; i++)
                {
                    IProject proj = (IProject)libProjs.get(i);
                    ITypeManager typeMan = proj.getTypeManager();
                    if (typeMan != null)
                    {
                        IPickListManager pickMan = typeMan.getPickListManager();
                        if (pickMan != null)
                        {
                            if (isFullyQualified())
                            {
                                curCollection = pickMan.getFullyQualifiedTypeNamesOfType(type);
                            }
                            else
                            {
                                curCollection = pickMan.getTypeNamesOfType(type);
                            }
                        }
                    }
                }
            }
        }
        return curCollection;
    }
    
    /**
     *
     * Determines whether or not processing should occur on the passed in element
     *
     * @param element[in]   The element to check
     *
     * @return true if ok to process, else false
     *
     */
    private boolean processElement( IVersionableElement verElement )
    {
        return processElement(verElement, false);
    }
    
    private boolean processElement( IVersionableElement verElement, boolean external )
    {
        boolean proceed = false;
        
        if (verElement instanceof IElement)
        {
            IElement element = (IElement)verElement;
            if (verElement instanceof IPresentationElement)
            {
            }
            else
            {
                if (m_rawTypeManager != null)
                {
                    IProject proj = m_rawTypeManager.getProject();
                    
                    if (proj != null)
                    {
                        IProject elProj = element.getProject();
                        if (elProj != null)
                        {
                            if (external || elProj.isSame(proj))
                            {
                                proceed = true;
                            }
                        }
                    }
                }
            }
        }
        return proceed;
    }
    
    /**
     *
     * Adds the new type to this cache
     *
     * @param element[in]   The element to add
     *
     * @return HRESULT
     *
     */
    private void addNewType( IVersionableElement element )
    {
        if (element instanceof INamedElement)
        {
            INamedElement namedEle = (INamedElement)element;
            if (isTypeCached(namedEle))
            {
                NewNamedType newType = new NewNamedType(namedEle);
                
                Object obj = m_TypeMap.get(newType.getElemType());
                if (obj != null)
                {
                    Vector col1 = (Vector)obj;
                    col1.add(newType);
                }
                else
                {
                    Vector<NamedType> col1 = new Vector<NamedType>();
                    col1.add(newType);
                    m_TypeMap.put(newType.getElemType(), col1);
                }
                
                m_ElementMap.put(newType.getId(), newType);
                
                String name = namedEle.getName();
                Object obj2 = m_NameTypeCache.get(name);
                if (obj2 != null)
                {
                    Vector col2 = (Vector)obj2;
                    col2.add(newType);
                }
                else
                {
                    Vector<NamedType> col2 = new Vector<NamedType>();
                    col2.add(newType);
                    m_NameTypeCache.put(name, col2);
                }
            }
        }
    }
    
    /**
     *
     * Retrieves the relative path from the Project that this PickListManager
     * is managing and the file passed in.
     *
     * @param fileName[in]  The file location to make relative
     *
     * @return The relative location
     *
     */
    private String getRelativePathToProject(String fileName)
    {
        String retStr = "";
        if (m_ProjectLocation != null && m_ProjectLocation.length() > 0)
        {
            String projPath = StringUtilities.getPath(m_ProjectLocation);
            retStr = PathManip.retrieveRelativePath(fileName, projPath);
        }
        return retStr;
    }
    
    /**
     *
     * Retrieves the types that match the type names passed in via the filter parameter.
     *
     * @param filter[in]       A space delimited list of type names
     * @param fullNames[in]    true for fully qualified names
     * @param typeNames[out]   The found types
     *
     * @return HRESULT
     *
     */
    private IStrings getTypeNamesWithStringFilter(String filter, boolean fullNames)
    {
        IStrings foundNames = null;
        if (filter != null && filter.length() > 0)
        {
            foundNames = new Strings();
            StringTokenizer tokenizer = new StringTokenizer(filter, " ");
            while (tokenizer.hasMoreTokens())
            {
                String token = tokenizer.nextToken().trim();
                foundNames = getTypeNamesOfType(token, fullNames, foundNames);
            }
        }
        return foundNames;
    }
    
    /**
     *
     * Retrieves the names of a given type of element
     *
     * @param type[in]         The type of element that should be retrieved
     * @param fullNames[in]    true for fully qualified names, else false
     * @param typeNames[out]   The name of elements of type type. If the collection coming
     *                         in has a value, it will be added to.
     *
     * @return HRESULT
     *
     */
    private IStrings getTypeNamesOfType(String type, boolean fullNames,
            IStrings typeNames)
    {
        if (type != null && type.length() > 0)
        {
            PreventReEntranceByValue blocker = null;
            try
            {
                blocker = new PreventReEntranceByValue(type, m_ProjectID, 0);
                if (!blocker.isBlocking())
                {
                    boolean created = (typeNames != null) ? false : true;
                    Object obj = m_TypeMap.get(type);
                    if (obj != null)
                    {
                        Vector<NamedType> types = new Vector<NamedType>((Collection)obj);
                        if (types.size() > 0)
                        {
                            if (typeNames == null)
                            {
                                typeNames = new Strings();
                            }
                            
                            for (int i=0; i<types.size(); i++)
                            {
                                // Only add the type if it has not been deleted
                                NamedType foundType = types.get(i);
                                if (foundType.getState() != TS_DELETED)
                                {
                                    if (fullNames)
                                    {
                                        typeNames.add(foundType.getFullName());
                                    }
                                    else
                                    {
                                        typeNames.add(foundType.getName());
                                    }
                                }
                            }
                        }
                    }
                    
                    if (typeNames != null)
                    {
                        typeNames = addReferencedTypes(type, typeNames);
                    }
                }
            }
            catch (Exception e)
            {}
            finally
            {
                if (blocker != null)
                {
                    blocker.releaseBlock();
                }
            }
        }
        return typeNames;
    }
    
    /**
     *
     * Retrieves all the elements in the type cache that match the types found in
     * the typeFilter collection
     *
     * @param typeFilter[in]   The collection of type names used to filter the name by
     * @param fullNames[in]    true for fully qualified names
     * @param typeNames[out]   The collection of found names
     *
     * @return HRESULT
     *
     */
    private IStrings getTypeNamesWithFilter(IStrings typeFilter, boolean fullNames)
    {
        IStrings foundNames = new Strings();
        if (typeFilter != null)
        {
            int num = typeFilter.getCount();
            for (int i=0; i<num; i++)
            {
                String typeName = typeFilter.item(i);
                foundNames = getTypeNamesOfType(typeName, fullNames, foundNames);
            }
        }
        return foundNames;
    }
    
    /**
     *
     * Determines whether or not pick lists should be shown using fully qualified names
     *
     * @return HRESULT
     *
     */
    private boolean isFullyQualified()
    {
        //kris richards - "DisplayTypeFSN" pref removed. Set to true
        return true;
    }
    
    /**
     *
     * Searches reference libraries for the passed in name
     *
     * @param elementName[in]     The name to find
     * @param idsOfElement[out]   The list of IDs that match the name
     *
     * @return HRESULT
     *
     */
    private IStrings checkRefLibsForTypes(String elementName)
    {
        IStrings retObj = null;
        if (m_rawTypeManager != null)
        {
            ETList<IProject> libProjs = m_rawTypeManager.getReferencedLibraryProjects();
            if (libProjs != null)
            {
                int count = libProjs.size();
                for (int i=0; i<count; i++)
                {
                    IProject proj = libProjs.get(i);
                    ITypeManager typeMan = proj.getTypeManager();
                    if (typeMan != null)
                    {
                        IPickListManager pickMan = typeMan.getPickListManager();
                        if (pickMan != null)
                        {
                            retObj = pickMan.getIDsByName(elementName);
                            if (retObj != null)
                            {
                                long numElems = retObj.getCount();
                                if (numElems > 0)
                                {
                                    break;
                                }
                                else
                                {
                                    retObj = null;
                                }
                            }
                        }
                    }
                }
            }
        }
        return retObj;
    }
    
    /**
     *
     * Adds a new entry to our index of known types
     *
     * @param elementType[in]
     *
     * @return
     *
     */
    private int addTypeIndex(String elementType )
    {
        int index = -1;
        if (elementType != null && elementType.length() > 0)
        {
            Object obj = m_TypesCached.get(elementType);
            if (obj == null)
            {
                index = s_NextIndex++;
                m_TypesCached.put(elementType, new Integer(index));
                m_ElementTypeMap.put(new Integer(index), elementType);
            }
            else
            {
                index = ((Integer)obj).intValue();
            }
        }
        return index;
    }
    
    public String getType(int index)
    {
        String retType = null;
        Object obj = m_ElementTypeMap.get(new Integer(index));
        if (obj != null)
        {
            retType = (String)obj;
        }
        return retType;
    }
    
    private Vector<NamedType> getTypesByName(String elementName)
    {
        Vector<NamedType> retTypes = new Vector<NamedType>();
        if (elementName != null && elementName.length() > 0)
        {
            // Check to see if the name is fully qualified
            String fullyQualified = elementName;
            String nameToMatch = fullyQualified;
            boolean fullName = false;
            int pos = elementName.indexOf("::");
            if (pos >= 0)
            {
                nameToMatch = NameResolver.getSimpleName(elementName);
                fullName = true;
            }
            
            Object obj = m_NameTypeCache.get(nameToMatch);
            if (obj != null)
            {
                Vector<NamedType> types = new Vector<NamedType>((Collection)obj);
                for (int i=0; i<types.size(); i++)
                {
                    boolean add = true;
                    NamedType type = types.get(i);
                    if (fullName)
                    {
                        String fullQual = type.getFullName();
                        if (fullQual != null && fullQual.length()>0 &&
                                !fullQual.equals(fullyQualified))
                        {
                            add = false;
                        }
                    }
                    if (add && (type.getState() != TS_DELETED))
                    {
                        retTypes.add(type);
                    }
                }
            }
        }
        return retTypes;
    }
    
    /**
     *
     * Searches the immediate cache for elements with the passed in name.
     *
     * @param elementName[in]     The name to match against
     * @param crossProject[in]    true to include reference libraries in the search, else false to restrict
     *                            the search to the local project.
     * @param idsOfElement[out]   The found IDs, else 0
     *
     * @return HRESULT
     *
     */
    private IStrings getIDsByName(String elementName, boolean crossProject)
    {
        IStrings retObj = null;
        if (elementName != null && elementName.length() > 0)
        {
            PreventReEntranceByValue blocker = null;
            try
            {
                blocker = new PreventReEntranceByValue(elementName, m_ProjectID, 0);
                if (!blocker.isBlocking())
                {
                    Vector<NamedType> types = getTypesByName(elementName);
                    if (types != null)
                    {
                        retObj = new Strings();
                        for (int i=0; i<types.size(); i++)
                        {
                            NamedType type = types.get(i);
                            String id = type.getId();
                            if (id != null && id.length() > 0)
                            {
                                retObj.add(id);
                            }
                        }
                    }
                    else if (crossProject)
                    {
                        retObj = checkRefLibsForTypes(elementName);
                    }
                }
            }
            catch (Exception e)
            {
            }
            finally
            {
                if (blocker != null)
                {
                    blocker.releaseBlock();
                }
            }
        }
        return retObj;
    }
    
    /**
     * NamedType is a small utility object that encapsulates the type
     * information that is used for pick lists
     */
    
    private class NamedType
    {
        protected String m_Name = "";
        protected String m_Alias = "";
        protected String m_ID = "";
        protected String m_FullName = "";
        protected int m_TypeIndex = 0;
        protected int m_State = 0;
        
        public NamedType(String name, String alias, String id, String fullName, int index)
        {
            m_Name = name;
            m_Alias = alias;
            m_ID = id;
            m_FullName = fullName;
            m_State = TS_NOT_MODIFIED;
            m_TypeIndex = index;
        }
        
        public NamedType(INamedElement namedEle)
        {
            setState(TS_NOT_MODIFIED);
            if (namedEle != null)
            {
                m_Name = namedEle.getName();
                m_Alias = namedEle.getAlias();
                m_FullName = namedEle.getQualifiedName();
                String nodeType = namedEle.getElementType();
                m_TypeIndex = addTypeIndex(nodeType);
            }
        }
        
        public String getName()
        {
            return m_Name;
        }
        public void setName(String name)
        {
            m_Name = name;
            setState(TS_MODIFIED);
        }
        
        public String getAlias()
        {
            return m_Alias;
        }
        public void setAlias(String alias)
        {
            m_Alias = alias;
            setState(TS_MODIFIED);
        }
        
        public int getState()
        {
            return m_State;
        }
        public void setState(int state)
        {
            m_State = state;
        }
        
        public String getId()
        {
            return m_ID;
        }
        public void setId(String id)
        {
            m_ID = id;
        }
        
        public String getFullName()
        {
            return m_FullName;
        }
        public void setFullName(String name)
        {
            m_FullName = name;
            setState(TS_MODIFIED);
        }
        
        public int getTypeIndex()
        {
            return m_TypeIndex;
        }
        public void setTypeIndex(int index)
        {
            m_TypeIndex = index;
        }
        
        public String getElemType()
        {
            return getType(m_TypeIndex);
        }
    }
    
    /**
     * TypedNamedType is used when handling type transforms
     */
    private class TypedNamedType extends NamedType
    {
        public TypedNamedType(String name, String alias, String id, String fullName, int index)
        {
            super(name, alias, id, fullName, index);
            setState(TS_MODIFIED);
        }
        
        public TypedNamedType(INamedElement namedEle)
        {
            super(namedEle);
            if (namedEle != null)
            {
                setState(TS_MODIFIED);
            }
        }
    }
    
    /**
     * NewNamedType is used when a new element has been created.
     */
    private class NewNamedType extends TypedNamedType
    {
        private String m_FileName = "";
        public NewNamedType(String name, String alias, String id, String fullName, int index)
        {
            super(name, alias, id, fullName, index);
            setState(TS_MODIFIED);
        }
        
        public NewNamedType(INamedElement namedEle)
        {
            super(namedEle);
            if (namedEle != null)
            {
                setState(TS_NEW);
                m_ID = namedEle.getXMIID();
                m_FileName = namedEle.getVersionedFileName();
                if (m_FileName == null || m_FileName.length() == 0)
                {
                    IProject proj = namedEle.getProject();
                    if (proj != null)
                    {
                        m_FileName = proj.getFileName();
                    }
                }
            }
        }
        
        public String getFileName()
        {
            return m_FileName;
        }
    }
    
}



