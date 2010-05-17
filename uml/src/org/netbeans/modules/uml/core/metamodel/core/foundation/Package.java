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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.dom4j.Document;
import org.dom4j.Node;

import java.io.File;
import java.util.List; 

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.FileManip;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;

/**
 * @author sumitabhk
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Package extends Namespace implements IPackage, IAutonomousElement
{
    IAutonomousElement m_AutonomousAggregate = new AutonomousElement();
    
    /**
     *
     */
    public Package()
    {
        super();
    }
    
    @Override
    public void setNode(Node n)
    {
        super.setNode(n);
        m_AutonomousAggregate.setNode(n);
    }
    
    /**
     *
     * Adds the passed-in element import to this Package's collection of
     * ElementImports.
     *
     * @param import[in] The import to add
     *
     * @return HRESULT
     *
     */
    public void addElementImport(IElementImport elem, INamespace owner)
    {
        EventDispatchRetriever ret = EventDispatchRetriever.instance();
        IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher)ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
        boolean proceed = true;
        if (disp != null)
        {
            IEventPayload payload = disp.createPayload("PreElementImport");
            IAutonomousElement element = elem.getImportedElement();
            proceed = disp.firePreElementImport(this, element, owner, payload);
        }
        
        if (proceed)
        {
            final IElementImport elemImport = elem;
            addChildAndConnect(
                    false, "UML:Package.elementImport",
                    "UML:Package.elementImport/*", elemImport,
                    new IBackPointer<IPackage>()
            {
                public void execute(IPackage obj)
                {
                    elemImport.setImportingPackage(obj);
                }
            }
            );
            
            if (disp != null)
            {
                IEventPayload payload = disp.createPayload("ElementImported");
                disp.fireElementImported(elem, payload);
            }
        }
        else
        {
            //cancel the event
        }
    }
    
    /**
     * 
     * Removes the passed-in element from this package import list
     *
     * @param import[in] The import to remove
     *
     * @return HRESULT
     *
     */
    public void removeElementImport(IElement elem)
    {
//        removeElement( elem, "UML:Package.elementImport/*");
        
        if (elem instanceof IPackage)
        {
            ETList<IPackageImport> imports = getPackageImports();
            for (IPackageImport im: imports)
            {
                IPackage element = im.getImportedPackage();
                if (elem.getXMIID().equals(element.getXMIID()))
                    UMLXMLManip.removeChild(this.getNode(), im);
            }
        }
        else
        {
            ETList<IElementImport> imports = getElementImports();
            for (IElementImport im: imports)
            {
                IElement element = im.getImportedElement();
                if (elem.getXMIID().equals(element.getXMIID()))
                    UMLXMLManip.removeChild(this.getNode(), im);
            }
        }
    }
    
    /**
     *
     * Retrieves the collection of ElementImports this package maintains.
     *
     * @param imports[out] The collection of ElementImports
     *
     * @return HRESULT
     *
     */
    public ETList<IElementImport> getElementImports()
    {
        IElementImport dummy = null;
        return retrieveElementCollection(dummy, "UML:Package.elementImport/*", IElementImport.class);
    }
    
    /**
     *
     * Adds the passed-in import to this Package.
     *
     * @param import[in] The PackageImport to add to this Package
     *
     * @return HRESULT
     *
     */
    public void addPackageImport(IPackageImport pack, INamespace owner)
    {
        EventDispatchRetriever ret = EventDispatchRetriever.instance();
        IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher)ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
        boolean proceed = true;
        if (disp != null)
        {
            IEventPayload payload = disp.createPayload("PrePackageImport");
            IPackage packEle = pack.getNode().getDocument() != null?
                pack.getImportedPackage() : null;
            proceed = disp.firePrePackageImport(this, packEle, owner, payload);
        }
        
        if (proceed)
        {
            final IPackageImport packImport = pack;
            addChildAndConnect(
                    false, "UML:Package.packageImport",
                    "UML:Package.packageImport/*", packImport,
                    new IBackPointer<IPackage>()
            {
                public void execute(IPackage obj)
                {
                    packImport.setImportingPackage(obj);
                }
            }
            );
            
            if (disp != null)
            {
                IEventPayload payload = disp.createPayload("PackageImported");
                disp.firePackageImported(pack, payload);
            }
        }
        else
        {
            //cancel the event
        }
    }
    
    /**
     *
     * Removes the passed-in import from this package's collection of PackageImports.
     *
     * @param import[in] The import to remove
     *
     * @return HRESULT
     *
     */
    public void removePackageImport(IPackageImport elem)
    {
        IElement remEle = removeElement( elem, "UML:Package.packageImport/*");
        //elem.setImportingPackage(null);
//         ETList<IPackageImport> imports = getPackageImports();
//        
//        for (IPackageImport im: imports)
//        {
//            IPackage element = im.getImportedPackage();
//            if (elem.getXMIID().equals(element.getXMIID()))
//                UMLXMLManip.removeChild(this.getNode(), im);
//        } 
    }
    
    /**
     *
     * Retrieves the collection if PackageImports this Package maintains.
     *
     * @param imports[out] The collection
     *
     * @return HRESULT
     *
     */
    public ETList<IPackageImport> getPackageImports()
    {
        IPackageImport dummy = null;
        return retrieveElementCollection(dummy, "UML:Package.packageImport/*", IPackageImport.class);
    }
    
    /**
     *
     * Searches all the imported element, both package a element imports, for an element whose name
     * matches typeName.
     *
     * @param typeName[in] The name to match against
     * @param foundElements[out] Collection of found elements
     *
     * @return HRESULT
     *
     */
    public ETList<INamedElement> findTypeByNameInImports(String name)
    {
        ETList<INamedElement> retElems = null;
        
        // First attempt to find the element in the ElementImports. If that fails, search
        // The package imports
        retElems = findByNameInElementImports(name);
        
        if (retElems == null || retElems.size() == 0)
        {
            retElems = findByNameInPackageImports(name);
        }
        
        return retElems;
    }
    
    /**
     *
     * Instructs this package to import another. The proper PackageImport object is constructed,
     * initialized, added to this package, and then returned.
     *
     * @param pack[in] The IPackage to import
     * @param href[in] The absolute location of the IPackage. This is generally an XPointer
     *                 instruction.
     * @param versionedElement[in] True if the Package being imported has been versioned, else false.
     *                             The default package imports should always set this parameter to a
     *                             "false" status.
     * @param pVal[out] The resultant IPackageImport that describes the imported package.
     *
     * @return HRESULT
     *
     */
    public IPackageImport importPackage(IPackage pack, String href, boolean versioned)
    {
        IPackageImport retImp = null;
        
        // Cannot allow a Package to import an actual Project element. This will surely
        // wreak havoc on the type system!
        if (pack != null && !(pack instanceof IProject))
        {
            IVersionableElement clone = null;
            ETPairT<Boolean, IVersionableElement> preparedImport = prepareImport(pack, true, href, versioned);
            boolean prepared = ((Boolean)preparedImport.getParamOne()).booleanValue();
            clone = preparedImport.getParamTwo();
            if (prepared)
            {
                // Now create the IPackageImport and put it on this Package
                FactoryRetriever fact = FactoryRetriever.instance();
                Object obj = fact.createType("PackageImport", null);
                if (obj != null && obj instanceof IPackageImport)
                {
                    IPackageImport packImport = (IPackageImport)obj;
                    
                    // This needs to be enclosed in outer braces
                    // to force the destructor to fire before the AddPackageImport
                    // call
                    {
                        OwnershipManager oMan = new OwnershipManager(this, packImport);
                        if (clone != null && clone instanceof IPackage)
                        {
                            IPackage clonedPack = (IPackage)clone;
                            packImport.setImportedPackage(clonedPack);
                        }
                        
                        //ownership manager needs to be finalized before we can call addPackageImport
                        oMan.stopManagingElement();
                    }
                    addPackageImport(packImport, (INamespace)pack.getOwner());
                    retImp = packImport;
                }
            }
        }
        return retImp;
    }
    
    /**
     *
     * Instructs this package to import an element that is contained within an external package.
     * The proper ElementImport object is constructed, initialized, added to this Package,
     * and then returned.
     *
     * @param pack[in] The PackageableElement to import
     * @param href[in] The absolute location of the element. This is generally an XPointer
     *                 instruction.
     * @param versionedElement[in] True if the element being imported has been versioned, else false.
     *                             The default element imports should always set this parameter to a
     *                             "false" status.
     * @param pVal[out] The resultant IElementImport that describes the imported element.
     *
     * @return HRESULT
     *
     */
    public IElementImport importElement(IAutonomousElement elem, String href, boolean versioned)
    {
        IElementImport retImp = null;
        IVersionableElement clone = null;
        ETPairT<Boolean, IVersionableElement> preparedImport = prepareImport(elem, false, href, versioned);
        boolean prepared = ((Boolean)preparedImport.getParamOne()).booleanValue();
        clone = preparedImport.getParamTwo();
        if (prepared)
        {
            // Now create the IElementImport and put it on this Package
            FactoryRetriever fact = FactoryRetriever.instance();
            Object obj = fact.createType("ElementImport", null);
            if (obj != null && obj instanceof IElementImport)
            {
                IElementImport elemImport = (IElementImport)obj;
                
                // We need to create temporary ownership in order
                // to properly resolve project ownership when queried from
                // the elImport element inside put_ImportedElement. This
                // is required as the ElementImport element has not
                // been added to the DOM yet, which causes problems
                // during the put_ImportedElement
                
                // This needs to be enclosed in outer braces
                // to force the destructor to fire before the AddElementImport
                // call
                {
                    OwnershipManager oMan = new OwnershipManager(this, elemImport);
                    if (clone != null && clone instanceof IAutonomousElement)
                    {
                        IAutonomousElement clonedElem = (IAutonomousElement)clone;
                        elemImport.setImportedElement(clonedElem);
                    }
                    oMan.stopManagingElement();
                }
                addElementImport(elemImport, elem.getOwningPackage());
                retImp = elemImport;
            }
        }
        return retImp;
    }
    
    public long getElementImportCount()
    {
        return UMLXMLManip.queryCount(m_Node, "UML:Package.elementImport/*", false);
    }
    
    public long getPackageImportCount()
    {
        return UMLXMLManip.queryCount(m_Node, "UML:Package.packageImport/*", false);
    }
    
    /**
     *
     * Retrieves the imported Packages currently imported on this Package
     *
     * @param pVal[out] The collection of imported packages
     *
     * @return HRESULT
     *
     */
    public ETList<INamespace> getImportedPackages()
    {
        ETList<INamespace> retSpaces = null;
        ETList<IPackageImport> imps = getPackageImports();
        if (imps != null)
        {
            int count = imps.size();
            if (count > 0)
            {
                retSpaces = new ETArrayList<INamespace>();
                for (int i=0; i<count; i++)
                {
                    IPackageImport pImp = imps.get(i);
                    IPackage pack = pImp.getImportedPackage();
                    
                    // No ASSERT here because some systems do not have imported packages,
                    // for example systems created via system upgrade.
                    if (pack != null)
                    {
                        retSpaces.add((INamespace)pack);
                    }
                }
            }
        }
        return retSpaces;
    }
    
    /**
     *
     * Retrieves the collection of imported elements
     *
     * @param *pVal[out] The collection
     *
     * @return HRESULT
     *
     */
    public ETList<IElement> getImportedElements()
    {
        ETList<IElement> retElems = null;
        ETList<IElementImport> imps = getElementImports();
        if (imps != null)
        {
            int count = imps.size();
            if (count > 0)
            {
                retElems = new ETArrayList<IElement>();
                for (int i=0; i<count; i++)
                {
                    IElementImport eImp = imps.get(i);
                    IAutonomousElement elem = eImp.getImportedElement();
                    
                    if (elem != null)
                    {
                        retElems.add(elem);
                    }
                }
            }
        }
        return retElems;
    }
    
    /**
     *
     * Establishes the appropriate XML elements for this UML type.
     *
     * @param doc[in] The document where this element will reside
     * @param parent[in] The element's parent node.
     *
     * @return HRESULT
     *
     */
    @Override
    public void establishNodePresence( Document doc, Node parent )
    {
        buildNodePresence( "UML:Package", doc, parent );
    }
    
    /**
     *
     * Searches through all the ElementImport objects for the specified element.
     *
     * @param typeName[in] The name of the element to find
     * @param foundElements[out] The collection of elements if found, else 0.
     *
     * @return HRESULT
     *
     */
    protected ETList<INamedElement> findByNameInElementImports(String typeName)
    {
        ETList<INamedElement> retElems = new ETArrayList<INamedElement>();
        ETList<IElementImport> imps = getElementImports();
        if (imps != null)
        {
            int count = imps.size();
            for (int i=0; i<count; i++)
            {
                IElementImport imp = imps.get(i);
                IAutonomousElement elem = imp.getImportedElement();
                if (elem != null)
                {
                    String name = elem.getName();
                    String fullyQualifiedName = elem.getFullyQualifiedName(false);
                    
                    if ((name.equals(typeName) == true) || 
                        (fullyQualifiedName.equals(typeName) == true))
                    {
                        retElems = new ETArrayList<INamedElement>();
                        retElems.add(elem);
                        break;
                    }
                }
            }
        }
        return retElems;
    }
    
    /**
     *
     * Searchs the package imports for a package whose name matches typeName
     *
     * @param typeName[in] The name to match against
     * @param foundElements[out] Collection of found elements
     *
     * @return HRESULT
     *
     */
    protected ETList<INamedElement> findByNameInPackageImports(String typeName)
    {
        ETList<INamedElement> retElems = null;
        ETList<IPackageImport> imps = getPackageImports();
        if (imps != null)
        {
            int count = imps.size();
            for (int i=0; i<count; i++)
            {
                IPackageImport imp = imps.get(i);
                retElems = imp.findByName(typeName);
                if (retElems != null && retElems.size() > 0)
                {
                    break;
                }
            }
        }
        return retElems;
    }
    
    /**
     *
     * Prepares the importElement. Makes sure the element is unique, and sets the loadedVersion attribute
     * accordingly.
     *
     * @param importElement[in] The element being imported.
     * @param isPackImport[in] - true to search the package import collection, else
     *                         - false to search the element import collection
     * @param href[in] The absolute location of the IPackage. This is generally an XPointer
     *                 instruction.
     * @param versionedElement[in] true if the Package being imported has been versioned, else false.
     *                             The default package imports should always set this parameter to a
     *                             "false" status.
     * @param clonedElement[out]   The clone of importElement.
     *
     * @return true if the element was prepared, else false
     *
     */
    protected ETPairT<Boolean, IVersionableElement> prepareImport( IVersionableElement importElement,
            boolean isPackImport,
            String href,
            boolean versionedElement)
    {
        boolean prepared = false;
        IVersionableElement clonedElement = null;
        ETPairT<Boolean, IVersionableElement> retVal = new ETPairT<Boolean, IVersionableElement>();
        
        // First make sure that the element being asked to be imported is not already one of our imports. If it is,
        // refuse to do the import.
        String xmiid = importElement.getXMIID();
        if (xmiid.length() > 0)
        {
            prepared = verifyUniqueImportID( xmiid, isPackImport );
        }
        if (prepared)
        {
            // Clone the importElement. We don't want to modify the original element due
            // to the import of that element into another.
            TypedFactoryRetriever < IVersionableElement > ret = new TypedFactoryRetriever < IVersionableElement >();
            IVersionableElement clone = ret.clone(importElement);
            if (clone != null)
            {
                Node node = clone.getNode();
                if (node != null)
                {
                    String val = versionedElement ? "true" : "false";
                    XMLManip.setAttributeValue(node, "loadedVersion", val);
                    
                    if (href != null && href.length() > 0)
                    {
                        // Retrieve this Package's project in order to get the file name so that we can make this href relative
                        IProject proj = getProject();
                        String refHref = href;
                        if (proj != null)
                        {
                            String fileName = proj.getFileName();
                            if (fileName.length() > 0)
                            {
                                int pos = href.indexOf('#');
                                if(pos >= 0)
                                {
                                    refHref = FileSysManip.retrieveRelativePath(href.substring(0, pos), fileName);
                                    refHref += href.substring(pos);
                                }
                                else
                                {
                                    refHref = FileSysManip.retrieveRelativePath(refHref, fileName);
                                }
                            }
                        }
                        XMLManip.setAttributeValue(node, "href", refHref);
                    }
                    clonedElement = clone;
                }
            }
        }
        retVal.setParamOne(Boolean.valueOf(prepared));
        retVal.setParamTwo(clonedElement);
        
        return retVal;
    }
    
    /**
     *
     * Verifies the the passed in XMI id is unique in either the imported element collection
     * on this package or the imported package collection, dependent on the value of
     * isPackImport.
     *
     * @param id[in] The XMI id to query against.
     * @param isPackImport[in] - true to search the package import collection, else
     *                         - false to search the element import collection
     *
     * @return true if the id is unique, else false.
     *
     */
    protected boolean verifyUniqueImportID( String id, boolean isPackImport )
    {
        boolean isUnique = true;
        
        String query = "UML:Package.";
        if( isPackImport )
        {
            query += "packageImport/UML:PackageImport/UML:PackageImport.importedPackage/*[@xmi.id='";
        }
        else
        {
            query += "elementImport/UML:ElementImport/UML:ElementImport.importedElement/*[@xmi.id='";
        }
        
        query += id;
        query += "']";
        List list = XMLManip.selectNodeList(m_Node, query);
        if (list != null)
        {
            int count = list.size();
            if (count > 0)
            {
                isUnique = false;
            }
        }
        
        return isUnique;
    }
    
    @Override
    public IVersionableElement performDuplication()
    {
        IVersionableElement dup = super.performDuplication();
        
        IPackage dupPack = (IPackage)dup;
        duplicateElementImports(dupPack);
        duplicatePackageImports(dupPack);
        
        return dup;
    }
    
    /**
     *
     * Duplicates all the ElementImport objects owned by the duplicated
     * Package
     *
     * @param dupPack[in] The duplicated Package
     *
     * @return HRESULT
     *
     */
    private void duplicateElementImports(IPackage dupPack)
    {
        ETList<IElementImport> imps = dupPack.getElementImports();
        if (imps != null)
        {
            int count = imps.size();
            for (int i=0; i<count; i++)
            {
                IElementImport imp = imps.get(i);
                IVersionableElement ver = imp.duplicate();
                if (ver instanceof IElementImport)
                {
                    IElementImport dupImp = (IElementImport)ver;
                    replaceIds(dupPack, dupImp);
                }
            }
        }
    }
    
    /**
     *
     * Duplicates all the PackageImport objects owned by the duplicated
     * Package
     *
     * @param dupPack[in] The duplicated Package
     *
     * @return HRESULT
     *
     */
    private void duplicatePackageImports(IPackage dupPack)
    {
        ETList<IPackageImport> imps = dupPack.getPackageImports();
        if (imps != null)
        {
            int count = imps.size();
            for (int i=0; i<count; i++)
            {
                IPackageImport imp = imps.get(i);
                IVersionableElement ver = imp.duplicate();
                if (ver instanceof IPackageImport)
                {
                    IPackageImport dupImp = (IPackageImport)ver;
                    replaceIds(dupPack, dupImp);
                }
            }
        }
    }
    
    protected void performDependentElementCleanUp( IVersionableElement thisElement )
    {
        super.performDependentElementCleanup( thisElement );
    }
    
    @Override
    public void establishNodeAttributes( org.dom4j.Element node )
    {
        super.establishNodeAttributes( node );
    }
    
    /**
     *
     * Retrieves the absolute directory where code owned by elements of this package
     * should be generated
     *
     * @param pVal[out]  The path
     *
     * @return HRESULT
     *
     */
    public String getSourceDir()
    {
        String srcDir = "";
        if (getIsTopLevelPackage(true))
        {
            String name = getName();
            String sourceDir = XMLManip.getAttributeValue(m_Node, "sourceDir");
            if (sourceDir == null || sourceDir.length() == 0)
            {
                // If this package is actually the Project and the sourceDir
                // attribute has not been set, then use the base directory of the
                // project
                if (this instanceof IProject)
                {
                    IProject proj = (IProject)this;
                    sourceDir = proj.getBaseDirectory();
                }
            }
            else
            {
                IProject proj = null;
                if (this instanceof IProject)
                {
                    proj = (IProject)this;
                }
                if (proj == null)
                {
                    // Only append the name of the Package if it is NOT the Project
                    // itself
                    sourceDir = makePath(sourceDir, name);
                }
            }
            if (sourceDir != null && sourceDir.length() > 0)
            {
                srcDir = sourceDir;
            }
        }
        else
        {
            IPackage owner = OwnerRetriever.getOwnerByType(this, IPackage.class);
            if (owner != null)
            {
                String parentSrcDir = owner.getSourceDir();
                String curName = getName();
                String fulSrcDir = makePath(parentSrcDir, curName);
                if (fulSrcDir != null && fulSrcDir.length() > 0)
                {
                    srcDir = fulSrcDir;
                }
            }
        }
        return srcDir;
    }
    
    /**
     *
     * Sets the directory where code belonging to elements owned
     * by this package is generated to. This will only be set if this
     * package is a top level package.
     *
     * @param pVal[in]   The new value
     *
     * @return HRESULT
     *
     */
    public void setSourceDir(String value)
    {
        if (getIsTopLevelPackage(false))
        {
            // Check to see if the user is passing in a path that includes the name
            // of this package. If it does, remove the name. This prevents the situation
            // where the user set this source dir property:
            //
            // c:\temp\C
            //
            // on a package called 'C' and immediately call get_SourceDir and gets:
            //
            // C:\temp\C\C
            String newSrcDir = value;
            if ((newSrcDir = validateSourceDir(newSrcDir)) != null)
            {
                // Let's check to see if the value coming in is the same as we currently have. If
                // so, no need to set again and fire events.
                String curVal = getAttributeValue("sourceDir");
                if (!newSrcDir.equals(curVal))
                {
                    EventDispatchRetriever ret = EventDispatchRetriever.instance();
                    IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher) ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
                    boolean proceed = true;
                    if (disp != null)
                    {
                        IEventPayload payload = disp.createPayload("PreSourceDirModified");
                        proceed = disp.firePreSourceDirModified(this, value, payload);
                    }
                    if (proceed)
                    {
                        setAttributeValue("sourceDir", newSrcDir);
                        if (disp != null)
                        {

                            IEventPayload payload = disp.createPayload("SourceDirModified");
                            disp.fireSourceDirModified(this, payload);
                        }
                    }
                }
            }
        }
    }
    
    /**
     *
     * Determines whether or not this package is a top level package.
     * A top level package is any package directly owned by the Project
     * ( that is, it's owning Namespace is the Project ), or that this
     * Package is the Project itself
     *
     * @param checkLength[in] true to include the check of the current source dir
     *                        property as an indication of whether or not a Package
     *                        is top level. This should be set to true during a get_SourceDir
     *                        and set to false during a put_SourceDir.
     *
     * @return true if toplevel, else false
     *
     */
    public boolean getIsTopLevelPackage(boolean checkLength)
    {
        boolean topLevel = false;
        INamespace owner = getNamespace();
        if (owner == null)
        {
            if (this instanceof IProject)
            {
                topLevel = true;
            }
        }
        else
        {
            if (owner instanceof IProject)
            {
                if (checkLength)
                {
                    // One last check is to see if the property is actually
                    // set on this Package. It won't be set by default. That is,
                    // when the user creates a new Project the only package that will
                    // have the actual xml attribute set is the project itself
                    String srcDir = XMLManip.getAttributeValue(m_Node, "sourceDir");
                    if (srcDir != null && srcDir.length() > 0)
                    {
                        topLevel = true;
                    }
                }
                else
                {
                    topLevel = true;
                }
            }
        }
        return topLevel;
    }
    
    /**
     *
     * Retrieves the top level package that curPack is owned by.
     *
     * @param curPack[in]   The package to retrieve from
     * @param topLevel[out] The owning top level package
     *
     * @return HRESULT
     *
     */
    protected IPackage getTopLevelPackage( IPackage curPack)
    {
        IPackage topLevel = null;
        boolean isTopLevel = curPack.getIsTopLevelPackage(false);
        if (isTopLevel)
        {
            topLevel = curPack;
        }
        else
        {
            IPackage testPack = curPack;
            IPackage owner = null;
            do
            {
                owner = OwnerRetriever.getOwnerByType(testPack, IPackage.class);
                if (owner != null)
                {
                    isTopLevel = owner.getIsTopLevelPackage(false);
                    testPack = owner;
                }
            }while (owner != null && !isTopLevel);
        }
        return topLevel;
    }
    
    /**
     *
     * Ensures that an appropriate path is constructed given the source directory and the
     * name of this package
     *
     * @param sourceDir[in] The current source directory
     * @param name[in]      Name of this package
     *
     * @return The final source directory
     *
     */
    private String makePath( String sourceDir, String name )
    {
        String path = sourceDir;
        if (sourceDir.length() >0 && name.length() > 0)
        {
            String str = FileSysManip.addBackslash(sourceDir);
            str += name;
            
            // Now we want to support the ability for specific machines to handle
            // expansion variables for path expansion specific to that machine
            path = FileManip.resolveVariableExpansion(str);
        }
        return path;
    }
    
    /**
     *
     * Checks and makes sure the source dir entry passes a couple of tests, such
     * as stripping off the name of the Package off the end of the entry, converting
     * backslashes, etc.
     *
     * @param newSourceDir[in, out]  The user entered directory on the in, and the validated
     *                               entry on the out
     *
     * @return true if validated
     *
     */
    private String validateSourceDir( String newSourceDir )
    {
        if (newSourceDir != null && newSourceDir.length() > 0)
        {
            String name = getName();
            if (name != null && name.length() > 0)
            {
                String srcDir = newSourceDir;
                if (srcDir.endsWith(File.separator))
                    srcDir = srcDir.substring(0,
                            srcDir.length() - File.separator.length());
                
                File srcDirFile = new File(srcDir);
                if (name.equals(srcDirFile.getName()))
                    srcDir = srcDirFile.getParent();
                
                newSourceDir = srcDir;
            }
        }
        return newSourceDir;
    }
    
    /**
     * Validate the passed in values according to the Describe business rules.
     * See method for the rules.
     *
     * @param pDisp[in]			The dispatch that needs validating
     * @param fieldName[in]		The name of the field to validate
     * @param fieldValue[in]	The string to validate
     * @param outStr[out]		The string changed to be valid (if necessary)
     * @param bValid[out]		Whether the string is valid as passed in
     *
     * @return HRESULT
     *
     */
    public boolean validate( Object disp, String fieldName, String fieldValue, String outVal)
    {
        boolean valid = true;
        
        // Using this mechanism to determine whether or not a package's source directory field
        // should be read-only or not in the property editor.  This was normally used
        // to validate data before it was saved, but the same mechanism could
        // be used for this.
        // Right now the only time we want the user to be able to edit the source directory field
        // is when it is a top-level - meaning that it is either the project or a package whose
        // owner is the project
        INamespace owner = getNamespace();
        if (owner != null)
        {
            if (owner instanceof IProject)
            {}
            else
            {
                valid = false;
            }
        }
        
        return valid;
    }
    
    public void whenValid(Object obj)
    {
    }
    
    public void whenInvalid(Object obj)
    {
    }
    
    //overrides the method in Namespace
    @Override
    public boolean addOwnedElement(INamedElement elem)
    {
        IProject elemProj = elem.getProject();
        if (elemProj != null)
        {
            IProject curProj = getProject();
            if (curProj != null)
            {
                boolean isSame = true;
                isSame = curProj.isSame(elemProj);
                if (!isSame)
                {
                    // Remove the element from the elements' type file
                    ITypeManager elemTypeMan = elemProj.getTypeManager();
                    if (elemTypeMan != null)
                    {
                        elemTypeMan.removeFromTypeLookup(elem);
                        
                        // Make sure the element is not an element import or package import
                        // in this package. If it is, the element import or package
                        // import must be deleted
                        String xmiid = elem.getXMIID();
                        if (m_Node != null && xmiid.length() > 0)
                        {
                            // Check element imports
                            String query = "UML:Package.elementImport/UML:ElementImport/UML:ElementImport.importedElement/*[@xmi.id=\"";
                            query += xmiid;
                            query += "\"]/ancestor::UML:ElementImport[1]";
                            org.dom4j.Node importNode = XMLManip.selectSingleNode(m_Node, query);
                            if (importNode != null)
                            {
                                // Now make sure to remove that type from the .ettm file
                                ITypeManager curTypeMan = curProj.getTypeManager();
                                if (curTypeMan != null)
                                {
                                    curTypeMan.removeFromTypeLookup(elem);
                                    TypedFactoryRetriever < IElementImport > ret = new TypedFactoryRetriever < IElementImport >();
                                    IElementImport imp = ret.createTypeAndFill(importNode);
                                    if (imp != null)
                                    {
                                        imp.delete();
                                    }
                                }
                            }
                            else
                            {
                                // Check to see if it is a package import that needs removing
                                String query2 = "UML:Package.packageImport/UML:PackageImport/UML:PackageImport.importedPackage/*[@xmi.id=\"";
                                query2 += xmiid;
                                query2 += "\"]/ancestor::UML:PackageImport[1]";
                                org.dom4j.Node importNode2 = XMLManip.selectSingleNode(m_Node, query2);
                                if (importNode2 != null)
                                {
                                    // Now make sure to remove that type from the .ettm file
                                    ITypeManager curTypeMan = curProj.getTypeManager();
                                    if (curTypeMan != null)
                                    {
                                        curTypeMan.removeFromTypeLookup(elem);
                                        TypedFactoryRetriever < IPackageImport > ret = new TypedFactoryRetriever < IPackageImport >();
                                        IPackageImport imp = ret.createTypeAndFill(importNode2);
                                        if (imp != null)
                                        {
                                            imp.delete();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return super.addOwnedElement(elem);
        
    }
    
    ///////////////////////////////////////////////////////////////////////////
    //IAutonomousElement methods
    
    public boolean isExpanded()
    {
        if (m_AutonomousAggregate == null)
        {
            m_AutonomousAggregate = new AutonomousElement();
        }
        return m_AutonomousAggregate.isExpanded();
    }
    
    public void setIsExpanded(boolean newVal )
    {
        if (m_AutonomousAggregate == null)
        {
            m_AutonomousAggregate = new AutonomousElement();
        }
        m_AutonomousAggregate.setIsExpanded(newVal);
    }
}
