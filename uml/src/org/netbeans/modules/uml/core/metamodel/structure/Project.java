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

package org.netbeans.modules.uml.core.metamodel.structure;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.support.IAssociatedProjectSourceRoots;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.Node;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.PreferenceManager;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.IQueryManager;
import org.netbeans.modules.uml.core.configstringframework.ConfigStringHelper;
import org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductEventDispatcher;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.EventState;
import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementChangeDispatchHelper;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementDisposal;
import org.netbeans.modules.uml.core.metamodel.core.foundation.EventContextManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ExternalFileManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeDispatchHelper;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementDisposal;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITransitionElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.StructureConstants;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.support.UMLLogger;
import org.netbeans.modules.uml.core.support.umlmessagingcore.UMLMessagingHelper;
import org.netbeans.modules.uml.core.support.umlsupport.FileManip;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.PathManip;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.Validator;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.core.typemanagement.TypeManager;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class Project extends Model implements IProject, IElementModifiedEventsSink, IDocumentationModifiedEventsSink, IElementLifeTimeEventsSink, ICoreProductInitEventsSink
{
    private Document m_Doc;
    private boolean m_IsDirty = false;
    private boolean m_ChildrenDirty; // Only possibly true when elements are versioned
    private boolean m_ProjectNeedsCommit;
    private String m_OrigFileName;
    private ExternalFileManager m_ExtManager;
    private ETList<IElement> m_DefaultImports;
    private long m_LifeCookie;
    private IElementDisposal m_Disposal;
    private ITypeManager m_TypeManager;
    private ILanguage m_DefaultLanguage;
    private boolean m_InLibraryState;
    private boolean m_OldImports;
    private IAssociatedProjectSourceRoots mSourceRoots = null;
    public static String PROJ_BASE_DIR = "";
    /** property change listener support */
    private PropertyChangeSupport changeSupport;
    public static String PROP_DIRTY = "dirty";
    private ETList<IElementImport> m_ElementImports = new ETArrayList<IElementImport>();
    private ETList<IPackageImport> m_PackageImports = new ETArrayList<IPackageImport>();
    private boolean init = false;
    /** The synchronization lock used only for methods creating listeners
     * objects. It is static and shared among all DataObjects.
     */
    private static final Object listenersMethodLock = new Object();


	/**
	 * 
	 */
	public Project() 
	{
		super();
		m_IsDirty = false;
		m_ChildrenDirty = false;
		m_ProjectNeedsCommit = false;
		m_InLibraryState = false;
		m_OldImports = false;
		m_ExtManager = null;		
	}
	
	/**
	 * Retrieves the document that holds the data of the project.
	*/
	public Document getDocument()
	{
		return m_Doc;
	}
	
	public void setDocument( Document doc )
	{
		this.m_Doc = doc;
	}
	
	public void prepareNode()
	{
		establishXMIHeaderInfo();
					
		// Find the content node. This will be the projects parent node
		org.dom4j.Node content = this.m_Doc.selectSingleNode("XMI/XMI.content");
		super.prepareNode(content);
		establishElementDisposal();
	}
	
   /**
	*	 This call will establish the "topLevel" stereotype on this project. Here
	*	 is the meaning of the "topLevel" stereotype, pulled from the UML1.4 spec:
	*
	*	 "TopLevel is a stereotyped package that denotes the highest level package
	*	 in a containment hierarchy. The topLevel stereotype defines the outer limit
	*	 for looking up names, as namespaces �see� outwards. A topLevel
	*	 subsystem is the top of a subsystem containment hierarchy, i.e., it is the
	*	 model element that represents the boundary of the entire physical system
	*	 being modeled."
	*
	*	 NOTE: As of 4/8/02, this is no longer the case. The Project element used to be
	*		   implemented in the .etd file as a UML:Model, but is now a UML:Project.
	*
	*	 INPUT:
	*		doc      - the document this project resides in.
	*		parent   - parent of this node.
	*/
   public void establishNodePresence(Document doc,org.dom4j.Node parent )
   {
	   buildNodePresence("UML:Project",doc,parent);
	   // Now establish any implicit Package imports. These are generally
	   // imports such as DataTypes and stereotype usages that we want
	   establishDefaults();
      
      establishVersionNumber();
      
	   // Now set the dirty flag
//	   m_IsDirty = true;
      setDirty(true);
   }
   
   /**
	* Puts in the XML attributes specific to Element into the passed in node.
	*
	* @param node[in] the node to inject the XML attributes into.
	*/
    @Override
   public void establishNodeAttributes(Element node)
   {
   	    super.establishNodeAttributes(node);
		// Add an attribute to our node that says we have never been saved.
		// We do this so that we can put all the contents of this project into
		// a valid XMI header with the DTD specification included. We can't do this
		// now as we don't know where the user is saving the project
		XMLManip.setAttributeValue(node,"neverSavedBefore","true");
   }
   
   /**
    *
    * Saves this project to the specified file.
    *
    * @param fileName[in] Points to a zero-terminated string containing the absolute path 
    *                of the file to which the object should be saved. If fileName
    *                is NULL, the object should save its data to the current file, if 
    *                there is one. 
    * @param remember[in] Indicates whether the fileName parameter is to be used as the 
    *                current working file. If TRUE, fileName becomes the current 
    *                file and the object should clear its dirty flag after the save. 
    *                If FALSE, this save operation is a "Save A Copy As ..." 
    *                operation. In this case, the current file is unchanged and the 
    *                object should not clear its dirty flag. If fileName is NULL, 
    *                the implementation should ignore the remember flag
    */
   synchronized public void save( String fileName, boolean remember )
   {
        // m_ChildrenDirty will only be set during the EstablishDirtyState()
        // call. It will only be true when dealing with a Project that contains
        // elements that have been versioned. And in that case, only when one of those
        // versioned elements has been modified.
        if( m_IsDirty || m_ChildrenDirty )
        {
                String curFileName = getCurFile();  
                String newFileName = fileName;     
                if( (curFileName == null || curFileName.length() == 0)  && (newFileName != null && newFileName.length() != 0) )
                {
                   // If we don't have a filename put on us yet and one is coming in,
                   // establish the file name
                   setFileName( newFileName );
                }

                EventDispatchRetriever ret = EventDispatchRetriever.instance();
                IStructureEventDispatcher disp = (IStructureEventDispatcher) 
                                                  ret.getDispatcher(EventDispatchNameKeeper.structure());
                boolean proceed = true; 
                if( disp != null )
                {
                   IEventPayload payload = disp.createPayload("ProjectPreSave" );
                   proceed = disp.fireProjectPreSave(this,payload);			 
                }

                if( proceed )
                {
                   internalCommit();
//                   if (m_IsDirty == false && ( m_ChildrenDirty == false ))
//                   {
//                            int retryMax = 5;
//                                while( ( ( m_IsDirty || m_ChildrenDirty ) && retryMax >0) )
//                                {
//                                   internalCommit();
//                                   retryMax--;
//                                }
//                                internalCommit();
//                   }
                   if( disp != null )
                   {
                          IEventPayload payload = disp.createPayload("ProjectSaved");
                          disp.fireProjectSaved(this,payload);
                   }
                }
        }		
   }
   
	/**
	 * Injects all the necessary XMI nodes into the document.
	 */
	public void establishXMIHeaderInfo()
	{
		final String dtdLoc = retrieveDTDLocation();
        try
        {
            String fragment = UMLXMLManip.retrieveXMLFragmentFromResource(
                    StructureConstants.IDR_XMI_HEADER);

            // Replace the "%1" in fragment with the location of the DTD. I am
            // putting an absolute reference to the DTD in the installation
            // directory. This will not be persisted once the user saves the
            // file. This is done solely to make sure the loadXML call does not
            // fail.
            
            String toReplace = "%1";
            if (fragment.indexOf(toReplace) != -1 )
            {
                fragment = StringUtilities.replaceSubString(fragment, toReplace,
                    dtdLoc);
                    
                // AZTEC: TODO: Fix this so that XML fragments that reference
                // DTDs parse without validation. Currently we have to comment
                // out DTD references/declarations.
                m_Doc = XMLManip.loadXML(fragment, false);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
	}
	
	/**
	 *
	 * Returns whether or not this project needs to be saved or not. This will
	 * return a true value if the Project itself is dirty or whether or not
	 * one of its versioned "children" are dirty.
	 */
	public boolean getDirty()
	{
		return (m_IsDirty || m_ChildrenDirty);
	}
	
	/**
	 * Sets the dirty flag on the project.  If the dirty flag is true, then
	 * this project needs to be saved.
	 *
	 * @param dirty [in] True to dirty the project, else False.
	 */
	public void setDirty(boolean isDirty)
        {
            if (m_IsDirty == isDirty)
                return;
            
            firePropertyChange(PROP_DIRTY, m_IsDirty ? Boolean.TRUE : Boolean.FALSE,
                    isDirty ? Boolean.TRUE : Boolean.FALSE);
            
            m_IsDirty = isDirty;
        }

	/**
	 * Gets the directory where this project will be saved to
	 */
	public String getBaseDirectory()
	{
            String baseDir = null;
            String curFile = getCurFile();
            if (curFile != null)
            {
                File file = new File(curFile);
                try
                {
                    file = file.getCanonicalFile();
                }
                catch (IOException ignored)
                { 
                }
                baseDir = file.getParent();
            }
            return baseDir;
        }
	
	/**
	 *	Retrieves the file name that this project will be save to if save
	 *	was called without specifying a path.
	 */
	public String getCurFile()
	{
		Element element = getElementNode();
		String curFile = null;
		if (element != null)
		{
			curFile = XMLManip.getAttributeValue(element,"fileName");
		}
		return curFile;
	}
	
	/**
	 *	Sets the file name where this project will be saved to.
	 *  @param fileName -  the name of the file.
	 */
	public void setFileName(String fileName)
	{
		Element element = getElementNode();
		if (element != null)
		{
			String curFileName = getCurFile();
			boolean proceed = true;
			if (curFileName != null && fileName != null)
			{
				if (curFileName.equals(fileName))
				{
					proceed = false;
				}
			}			
			XMLManip.setAttributeValue(element,"fileName",fileName);
			establishTypeManager();
			if (proceed)
			{
				establishDefaultPackageImports(null);
			}
		}
	}
	
	/**
	 *
	 * Queries the PreferenceManager to determine what packages that should
	 * be imported into this Project.
	 *
	 */
	public void establishDefaults()
	{
		establishTypeManager();
		if (requiresOldImports())
		{
			establishOldDefaultPackageImports(); 
		}
		IPreferenceAccessor pref = PreferenceAccessor.instance();
                //kris richards - "DefaultMode" pref expunged. Set to "PSK_IMPLEMENTATION".
		String modeName = "PSK_IMPLEMENTATION";
//                String modeName = "Implementation";
		XMLManip.setAttributeValue(m_Node,"mode",modeName);
                String str = "Implementation";

		String lang = pref.getDefaultLanguage(str);		
		if (lang != null)
		{
			setDefaultLanguage(lang);
		}
		else
		{
			setDefaultLanguage("UML");
		}
	}
	
	/**
	 *
	 * Pulls in the references to any default packages that the user has configured
	 * the Project to use.
	 *
	 * @param prefMan[in] A pointer to a PreferenceManager interface
	 */
	public void establishDefaultPackageImports( IPreferenceManager2 pref )
	{
		Element element = getElementNode();
		IPreferenceManager2 prefManager = pref;
		if (element != null)
		{
			String curMode = getMode();
			if (prefManager == null)
			{
				ICoreProduct product = ProductRetriever.retrieveProduct();
				if (product != null)
				{
					prefManager = product.getPreferenceManager();
				}
			}
			if (curMode != null && prefManager != null)
			{
				String mode = translateString(curMode);
				String prefLoc = "Modes|" + mode;
				IPropertyElement libElement = prefManager.getPreferenceElement
														 (prefLoc,"Libraries");
				if (libElement != null)
				{
					// Get the sub elements to see what libraries we need to 
					//inject into the Project
					Vector<IPropertyElement> subElems=libElement.getSubElements();
					if (subElems != null)
					{
						int num = subElems.size();
						Map refLibs = new HashMap();
						for( int x = 0; x < num; x++ )
						{
							IPropertyElement sub =(IPropertyElement)subElems.get(x);
							if (sub != null)
							{
								String value = sub.getValue();
								if (value != null)
								{
									String location = 
													FileManip.resolveVariableExpansion(value);
									refLibs.put(location,new Boolean(true));				
								}
							}
						}
						
						String fileName = getCurFile();
						// We don't want to add ref libs to actual reference libraries
						// specified in the preferences file. If we did, it could result
						// in an infinite loop
						if (!refLibs.containsKey(fileName)) 
						{
							Iterator iter = refLibs.keySet().iterator();
							while (iter.hasNext()) 
							{
							  addReferencedLibrary(iter.next().toString());
							}
						}
					}														 
				}
			}
			// Now establish the type manager. Had to do this here as we are guarenteed that this 
			// project is now completely built in both the create and open states.
			establishTypeManager();
		}		
	}
	
	/**
	 *
	 * Handles all the necessary file handling associated with nodes that
	 * have already been versioned or need to be versioned.
	 *
	 * @param element[in] parent element whose children need to be handled
	 * @param fileName[in] name of the file where element resides
	 */
	public void handleElementVersioning(Element element, String fileName)
	{
		ExternalFileManager external = m_ExtManager;
		if (external == null)
		{
			external = new ExternalFileManager();
		}
		fileName = ExternalFileManager.getRootFileName();
		String xmiid = getXMIID();
		external.setProjectXMIID(xmiid);
		external.handleElementVersioning(element,fileName,
            UMLXMLManip.retrieveXMLFragmentFromResource(
                StructureConstants.IDR_XML_VER_ELEMENT));
	}
	
	/**
	* Removes any nodes in the DOM tree that have the "__discard_ " attribute set to "true". 
	* Currently, the only mechanism that utilizes this attribute is the default package
	* import mechanism. The default packages, such as native C++ or Java types, are read
	* into the Project at open or creation time, with their respective packages injected into
	* the DOM. At save time, those elements are removed, only referencing the external file.
	* In this way, if those files are updated, the next time the Project is opened the new
	* changes will be ingested.
	*
	* @param doc[in] The document to search through
	*/
	public List removeDiscardNodes(Document doc)
	{
		List nodeList = doc.selectNodes("//*[@__discard_=\"true\"]");
		List cloned = null;
		if (nodeList != null)
		{
			ArrayList arrayList = new ArrayList(nodeList);
			if (arrayList != null)
			{
				cloned = (List)arrayList.clone();
			}
			
			if (cloned != null)
			{
				// Found all the nodes, so loop through them, clone them, and
				// remove their children
				for( int x = 0; x < arrayList.size(); x++ )
				{
					Node node = (Node)arrayList.get(x);
					XMLManip.removeAllChildNodes((Element)node);
				}
			}
		}
		return nodeList;
	}
	
	
	/**
	 * This method makes sure that the DTD file used for validation purposes has been created,
	 * and that the current content of the project is properly marked with the DTD information.
	 *
	 * @param fileName[in] The absolute path to where the project is being saved.
	 */
	public void validateDTDSettings(String fileName)
	{
		if( verifyDTDExistence( fileName ))
		{
		   // Now check to see if the "neverSavedBefore" attribute is on this node.
		   // If it is, then the contents of this node need to be appended to an 
		   // XMI header fragment that has the DTD specification in it.
		   String value = UMLXMLManip.getAttributeValue(m_Node,"neverSavedBefore");
		   if (value != null && value.equals("true"))
		   {
				establishDTDHeader( fileName );
				Element nodeElement = (Element)m_Node;
				if (nodeElement != null)
				{	
					nodeElement.remove(nodeElement.attribute("neverSavedBefore"));			
				}
		   }
		}         
	}
	
	/**
	 * Responsible for making sure that the current project contents is wrapped
	 * in an XMI header that has the DTD specification in it.
	 */
	public void establishDTDHeader(String fileName)
	{
		// this is different than in c++ because we already have the project and the m_Doc established
		// but the m_Doc has a DOCTYPE with the full path to the dtd file, which we don't want when
		// we go to save the etd file, so we need to replace the full path with just the file
		try
		{
			if (m_Doc != null)
			{
          	DocumentType dt = m_Doc.getDocType();
          	if (dt != null)
          	{
					dt.setSystemID(StructureConstants.IDR_XMI_DTD);
          	}
         }
		}
		catch(Exception e)
		{
		}
	}
	
	/**
	 * Makes sure that the required DTD file exists in the location of the 
	 * project file. If the DTD file does not exist at that location, it will
	 * be created.
	 *
	 * @param fileName[in] The absolute file name of the project file.
	 *
	 * @return true if the DTD file exists, else false if the file could
	 *         not be created for some reason.
	 */
	public boolean verifyDTDExistence(String fileName)
	{
		return UMLXMLManip.verifyDTDExistence( fileName );
	}
	
	/**
	 * Loads the default import elements assigned to every new project. 
	 * This is generally called right after a Save operation has been done.
	 * We need to remove any PackageImport element that was used to import
	 * the default package imports, and then load them again.a
	 */
	public boolean loadDefaultImports()
	{
		boolean flag = false;
		establishTypeManager();
		if( requiresOldImports() )
		{
			EventContextManager manager = new EventContextManager();
			// See the comment in ElementImpl::AddPresentationElement for background here.
			// The same concept applies when we are adding default imports to the Project.
			// Although the physical XML element of the project is being modified, logically
			// we want to treat it as a non-modify, no side-effect operation
//			IEventDispatcher disp = new EventDispatcher();
//			IEventContext context = manager.getNoEffectContext
//									(this,EventDispatchNameKeeper.modifiedName(),
//									"DefaultImports",disp);
         
         ETPairT < IEventContext, IEventDispatcher > contextInfo = manager.getNoEffectContext(this,
                                                                                             EventDispatchNameKeeper.modifiedName(),
                                                                                             "DefaultImports");
            
         IEventDispatcher disp = contextInfo.getParamTwo();
         IEventContext context = contextInfo.getParamOne();
         
			EventState state = new EventState(disp,context);
         try
         {
            IPreferenceManager pref = new PreferenceManager();
            ETList<String> names = pref.retrieveDefaultModelLibraryNames();
            if (names != null)
            {
               // Block all events while we load the project 
               boolean origBlock = EventBlocker.startBlocking();
               try
               {
                  int count = names.size();
                  for( int x = 0; x < count; x++ )
                  {
                     String name = names.get(x);
                     if (name != null)
                     {
                        removePackageImportByName( name );
                     }
                  }
                  // Now that all the previous package imports have been
                  // removed that matched the default library imports, 
                  // establish the default libraries again...
                  establishOldDefaultPackageImports();
                  ETList<IPackageImport> packImports = getPackageImports();				
                  if (packImports.size() == 2)
                  {
                     flag = true;					
                  }
               }
               finally
               {
                  EventBlocker.stopBlocking(origBlock);
               }
            }
         }
         finally
         {
            state.existState();
         }
		}
		return flag;
	}
	
	/**
	 *
	 * Removes a PackageImport object from this Project that matches the
	 * passed in name of the Package being imported.
	 *
	 * @param name[in] The name of the Package that will indicate what 
	 *                 PackageImport element to remove.
	 */
	public void removePackageImportByName(String name)
	{
		String query = "./UML:Package.packageImport/UML:PackageImport/UML:PackageImport.importedPackage/*[@name=\"";
		query += name + "\"]/ancestor::*[2]";
		if (m_Node != null)
		{
			Node node = m_Node.selectSingleNode(query);
			if (node != null)
			{
				TypedFactoryRetriever<IPackageImport> retriever = 
							new TypedFactoryRetriever<IPackageImport>();
				IPackageImport packImport = retriever.createTypeAndFill(node);
				if (packImport != null)
				{
					removePackageImport(packImport);
				}
					
			}
		}
	}
	
	/**
	 *
	 * Retrieves the location of the DTD file to be used, even if temporary.
	 *
	 * @return The absolute path to the DTD
	 * 
	 */
	public String retrieveDTDLocation()
	{
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		IConfigManager config = null;
		String dtdLoc = null;
		if (prod != null)
		{
			config = prod.getConfigManager();						
		}
		else
		{
			config = new ConfigManager();
		}
		if (config != null)
		{
			dtdLoc = config.getDTDLocation();			
		}
		return dtdLoc;
	}
	
	/**
	 *
	 * Called to prepare all the files associated with this Project
	 * for a save. Essentially saves everything to temporary files.
	 * The Commit() call should follow, which removes all those files
	 * after first copying them back to their original file names.
	 */
	 public void preCommit()
	 {
	 	if ( (m_Doc != null) && ( m_IsDirty || m_ChildrenDirty ))
	 	{
	 		save(null,true);
	 	}
	 }
	
	/**
	 *
	 * Commits this Project and all dependent files
	 */
	public void commit()
	{		
	}
	
	/**
	 *
	 * Retrieves a flag that indicates whether or not this Project has been
	 * modified or not.
	 *
	 * @return isDirty[out] The flag. true indicates that the Project
	 *                     has been modified since the last save process.
	 */
	public boolean isDirty()
	{
		return getDirty();
	}
	
    ETArrayList < IElement > mRemoveList = new ETArrayList < IElement >();
    
    /**
     * Add an element that must be removed before project is saved.
     */
    public void addRemoveOnSave(IElement element)
    {
       mRemoveList.add(element);
    }
    
	/**
	 * Removes all elements that contain the "removeOnSave" xml attribute.
	 * To date, this will be any element that represents a presentation element.
	 *
	 * @return elements[out] The elements removed
	 */
	public Map< Node, ETList<Node> > processRemoveOnSaves()
	{
//       Map< Node, ETList<Node> > nodeMap = new HashMap< Node, ETList<Node> >();
//       
//       Iterator < IElement > iter = mRemoveList.iterator();
//       while(iter.hasNext() == true)
//       {
//          IElement curElement = iter.next();
//          Node curNode = curElement.getNode();
//          
//          if(curNode != null)
//          {
//             Node parent = curNode.getParent();
//             if (parent != null)
//             {
//                 ETList<Node> subNodes = nodeMap.get(parent);
//                 if (subNodes == null)
//                 {
//                     subNodes = new ETArrayList<Node>();
//                     subNodes.add(curNode);
//                     nodeMap.put(parent, subNodes);
//                 }
//                 else
//                 {
//                     subNodes.add(curNode);
//                     //now put it in the nodeMap
//                     nodeMap.put(parent, subNodes);
//                 }
//
//                 if (curNode.getNodeType() == Node.ELEMENT_NODE)
//                 {
//                     XMLManip.removeChild(parent, ((Element)curNode).getQualifiedName());
//                 }
//                 else
//                 {
//                     curNode.detach();
//                 }
//              }
//          }
//       }
//       
//       return nodeMap;
       
       // TODO: If we speed up //* search then uncomment the below code.
		List nodes = m_Doc.selectNodes( "//*[@removeOnSave]" );
		Map< Node, ETList<Node> > nodeMap = new HashMap< Node, ETList<Node> >();
		if (nodes != null)
		{
			int numNodes = nodes.size();
            
			for( int x = 0; x < numNodes; x++ )
			{
				Node node = (Node)nodes.get(x);
				
				if (node != null)
				{
					Node parent = node.getParent();
                    
                    if (parent != null)
                    {
						ETList<Node> subNodes = nodeMap.get(parent);
						if (subNodes == null)
						{
							subNodes = new ETArrayList<Node>();
							subNodes.add(node);
							nodeMap.put(parent, subNodes);
						}
						else
						{
							subNodes.add(node);
							//now put it in the nodeMap
							nodeMap.put(parent, subNodes);
						}
                    
						if (node.getNodeType() == Node.ELEMENT_NODE)
						{
							XMLManip.removeChild(parent, ((Element)node).getQualifiedName());
						}
						else
						{
							node.detach();
						}
                    }
				}
			}			
		}
		return nodeMap;
	}
	
	private void injectRemoveOnSaves(Map< Node, ETList<Node> > elements)
	{
      if(elements != null)
      {
        Set<Node> nodeSet = elements.keySet();
        if (nodeSet != null)
        {
            Iterator<Node> iter = nodeSet.iterator();
            while (iter.hasNext())
            {
                Node key = iter.next();
                ETList<Node> subNodes = elements.get(key);
                if (subNodes == null) continue;
                for (int i = 0; i < subNodes.size(); ++i)
                    ((Element)key).add(subNodes.get(i));
            }
        }
      }
	}
	
	/**
	 *
	 * Retrieves the name of the mode that this Project is in.
	 *
	 * @return modeName[out] The name of the mode.
	 */
	public String getMode()
	{
		return XMLManip.getAttributeValue(m_Node, "mode" );
	}
	
	/**
	 *
	 * Sets the name of the mode that this Project is in.
	 *
	 * @param modeName[in] The mode to put the Project into
	 */
	public void setMode(String modeName)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IStructureEventDispatcher disp = (IStructureEventDispatcher) 
						  ret.getDispatcher(EventDispatchNameKeeper.structure());
		boolean proceed = true; 
		if( disp != null )
		{
		   IEventPayload payload = disp.createPayload("PreModeModified" );
		   proceed = disp.firePreModeModified(this,modeName,payload);			 
		}
			
		if( proceed )
		{
			UMLXMLManip.setAttributeValue(this,"mode",modeName);
			if (disp != null)
			{
				IEventPayload payload = disp.createPayload("ModeModified" );
				disp.fireModeModified(this,payload);
			}
			establishDefaultPackageImports(null);
			
			// mode name string is "Analysis", "Design", or "Implementation"
			// ideally, these should be defined as enum rather than string
			// get the language defined in PreferenceProperties.etcd for
			// initializing processor on which the language rule should be applied
            
			String mode = "Design";
			
			if (modeName != null) 
			{
			    if(modeName.equals(StructureConstants.PSK_IMPLEMENTATION) == true)
			    {
				mode = "Implementation";
			    }
			    else if(modeName.equals(StructureConstants.PSK_ANALYSIS) == true)
			    {
				mode = "Analysis";
			    }
			} 
            
			IPreferenceAccessor pref = PreferenceAccessor.instance();
			String lang = pref.getDefaultLanguage(mode);
            
            if((lang == null) || (lang.length() == 0))
            {
                lang = "UML";
            }
			setDefaultLanguage(lang);
                        setDirty(true);
//			m_IsDirty = true;
		}		
	}
	
	/**
	 *
	 * Retrieves the name of the language that is being used by default during
	 * Implementation mode.
	 *
	 * @return lang[out] The current value. Can be null.
	 */
	public String getDefaultLanguage()
	{
		return XMLManip.getAttributeValue(m_Node, "defaultLanguage");
	}
	
	/**
	 *
	 * Retrieves of the language that is being used by default during
	 * Implementation mode.
	 *
	 * @return lang[out] The current value. Can be null.
	 */
	public ILanguage getDefaultLanguage2()
	{
		ILanguage retLang = null;		
		if ( m_DefaultLanguage == null )
		{
			String myLang = getDefaultLanguage();			
			ICoreProduct pProduct = ProductRetriever.retrieveProduct();
			if (pProduct != null)
			{
				ILanguageManager manager = pProduct.getLanguageManager();				
				if (manager != null)
				{
					retLang = manager.getLanguage(myLang);
				}
			}
		}
		return retLang;
	}
	

	/**
	 *
	 * Sets the language to be used during implementation mode.
	 *
	 * @param lang[in] The new value
	 */
	public void setDefaultLanguage(String lang)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IStructureEventDispatcher disp = (IStructureEventDispatcher) 
						  ret.getDispatcher(EventDispatchNameKeeper.structure());
		boolean proceed = true; 
		if( disp != null )
		{
		   IEventPayload payload = disp.createPayload("PreDefaultLanguageModified" );
		   proceed = disp.firePreDefaultLanguageModified(this,lang,payload);			 
		}
			
		if( proceed )
		{
			UMLXMLManip.setAttributeValue(this,"defaultLanguage",lang);
			if (disp != null)
			{
				IEventPayload payload = disp.createPayload("DefaultLanguageModified" );
				disp.fireDefaultLanguageModified(this,payload);
			}
//			m_IsDirty = true;
                        setDirty(true);
		}	
	}
	
	/**
	 * Listens for element modify events on elements in this Project.
	 */
	public void initialize()
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IElementChangeEventDispatcher changeDisp = (IElementChangeEventDispatcher) 
						  ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
		if (changeDisp != null)
		{
            changeDisp.registerForElementModifiedEvents(this);
            changeDisp.registerForDocumentationModifiedEvents(this);
		}
        
        IElementLifeTimeEventDispatcher lifeDisp = 
            (IElementLifeTimeEventDispatcher)
                ret.getDispatcher(EventDispatchNameKeeper.lifeTime());
        if (lifeDisp != null)
        {
            lifeDisp.registerForLifeTimeEvents(this);
        }
        
        ICoreProductEventDispatcher coreDisp =
            (ICoreProductEventDispatcher)
                ret.getDispatcher(EventDispatchNameKeeper.coreProduct());
        if (coreDisp != null)
        {
            coreDisp.registerForInitEvents(this);
        }
  
        }
	
	/**
	 *
	 * Revokes the element change sink from the dispatcher
	 */
	public void deInitialize()
	{
        EventDispatchRetriever ret = EventDispatchRetriever.instance();
        IElementChangeEventDispatcher changeDisp = (IElementChangeEventDispatcher) 
                          ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
        if (changeDisp != null)
        {
            changeDisp.revokeElementModifiedSink(this);
            changeDisp.revokeDocumentationModifiedSink(this);
        }
        
        IElementLifeTimeEventDispatcher lifeDisp = 
            (IElementLifeTimeEventDispatcher)
                ret.getDispatcher(EventDispatchNameKeeper.lifeTime());
        if (lifeDisp != null)
        {
            lifeDisp.revokeLifeTimeSink(this);
        }
        
        ICoreProductEventDispatcher coreDisp =
            (ICoreProductEventDispatcher)
                ret.getDispatcher(EventDispatchNameKeeper.coreProduct());
        if (coreDisp != null)
        {
            coreDisp.revokeInitSink(this);
        }
	}
	
	/**
	 *
	 * This Project is listening for any element modified's coming through on elements that it owns.
	 * If the element is owned by this project, the dirty flag is set to true.
	 *
	 * @param element[in] Element being modified	 
	 */
	public void onElementModified(IVersionableElement element)
	{
		establishDirtyState( element );
	}
	
	
	/**
	 *
	 * Checks to see if the passed in element is part of this Project.
	 * If is is, the dirty flag of this Project is set to true.
	 *
	 * @param element[in] The element to check
	 */
	public void establishDirtyState( IVersionableElement element )
	{
		// If m_IsDirty is already true, no need to perform the 
		// calculation
		if( m_IsDirty == false )
		{
			if (element != null )
			{				
				// We don't want to have a modification of a transition element
				// affect the dirty state of the project 
				if(!(element instanceof ITransitionElement))
				{
               String xmiID = getXMIID();
					// Make sure that the element is actually a part of this Project
					IElement el = (IElement)element;
					String topID = el.getTopLevelId();
					if (topID != null && topID.equals(xmiID) )
					{
						// Now check to see if the element is versioned or
						// part of a versioned element
						String elementXMIID = getXMIID();
						String elementURI = getVersionedURI();
						if( elementXMIID != null && elementURI != null &&
							( elementXMIID.equals(elementURI) ))
						{
							EventContextManager man = new EventContextManager();
							if (!man.isNoEffectModification())
							{
								// The element is NOT versioned and is part of this
								// Project, so make the Project dirty
//								m_IsDirty = true;
                                                            setDirty(true);
							}													
						}
						else
						{
							m_ChildrenDirty = true;
						}
					}
				}
			}
		}
		if (element != null)
		{
			element.setDirty(true);
		}
	}
	
	/**
	 *
	 * Sets the dirty flag of this project when the documentation of one of its element's is modified.
	 *
	 * @param element[in] The element
	 * @param cell[in] The cell
	 */
	public void onDocumentationModified(IElement element)
	{
		establishDirtyState( element );
	}
	

	/**
	 *
	 * Retrieves the ElementDisposal object.
	 *
	 * @return pDisposal[out] The object
	 */
	public IElementDisposal getElementDisposal()
	{
		return m_Disposal;
	}
	
	/**
	 *
	 * Sets the ElementDisposal object on this Project
	 *
	 * @param pDisposal[in] The new object
	 */
	public void setElementDisposal(IElementDisposal disposal)
	{
		m_Disposal = disposal;
	}
	
	/**
	 *
	 * Creates and initializes the ElementDisposal object
	 */
	public void establishElementDisposal()
	{
		if (m_Disposal == null)
		{
			m_Disposal = new ElementDisposal();
		}
	}
	
	/**
	 *
	 * Retrieves a string used to identify this Project in the users Source
	 * Control Management tool
	 *
	 * @return pVal[out] The id
	 */
	public String getSourceControlID()
	{
		return XMLManip.getAttributeValue( m_Node, "scmID" );
	}
	
	/**
	 *
	 * Sets the id that identifies this Project in the user's SCM tool.
	 *
	 * @param newVal[in] The new ID
	 */
	public void setSourceControlID(String newVal)
	{
		IWSProject wsProject = getWSProject();
		if (wsProject != null)
		{
			// Be sure to mark our corresponding workspace project element
			// with the SCM ID so that proper SCM status can be determined
			// while this Project is closed
			wsProject.setSourceControlID(newVal);
		}
		XMLManip.setAttributeValue(m_Node,"isVersioned","true");
		XMLManip.setAttributeValue(m_Node,"scmID",newVal);
      setDirty(true);
	}
	
	public String getVersionedFileName()
	{
		String retVal = null;
		if (isVersioned())
		{
			retVal = getCurFile();
		}
		return retVal;
	}
	
	public void setVersionedElement(String newVal)
	{
		setFileName(newVal);
	}
	
	/**
	 * Extracts the passed in element, allowing for version control.
	 *
	 * @param element[in] The element to extract
	 */
	public void extractElement(IElement element)
	{
		boolean isVersioned = false;
		isVersioned = element.isVersioned();
		if (!isVersioned)
		{
			element.setMarkForExtraction(true);
			Node node = element.getNode();
			Element elementNode = (Element)node;
			if (elementNode != null)
			{
				// Make sure to tell the element that it is dirty. It is 
				// important to do this here so that the element's parent is actually 
				// dirtied. If we set this after the element has been extracted, the
				// element itself is handling it's dirty flag. Upon initial extraction,
				// the owning element should be set to dirty anyway, so this is good.
				element.setDirty(true);
				String fileName = getCurFile();
				String xmiID = getXMIID();
            
				ExternalFileManager man = new ExternalFileManager();
				man.setProjectXMIID(xmiID);
            man.setRootFileName(fileName);
				man.extractMarkedElements(elementNode,
										       fileName,
                                     UMLXMLManip.retrieveXMLFragmentFromResource(StructureConstants.IDR_XML_VER_ELEMENT)
										       );
				m_ChildrenDirty = true;												
			}
		}
	}
	
	/**
	 *
	 * Retrieves the TypeManager associated with this Project.
	 */
	public ITypeManager getTypeManager()
	{
		return m_TypeManager;
	}
	
	/**
	 *
	 * Makes sure that this Project has an associated TypeManager that is
	 * ready.
	 */
	public void establishTypeManager()
	{
		if (m_LifeCookie == 0)
		{
		   // We only get into this situation if we've leaked the project and revoked
		   // from our dispatchers.  If we're not leaking the Initialize gets called in
		   // the FinalConstruct of the COM coclass.
		   initialize();
		}
		if (m_TypeManager == null)
		{
			m_TypeManager = new TypeManager();
    		m_TypeManager.setProject(this);
		}
		if (m_TypeManager != null)
		{
			// Verify that we have a Project. This may not be the case 
			// in a situation where the Project was closed but leaked...
			IProject proj = m_TypeManager.getProject();
			if (proj == null)
			{
				IProject curObj = this;
				m_TypeManager.setProject(curObj);
			}
		}
	}
	
	/**
	 *
	 * Sets the name of this Project.
	 *
	 * @param newName[in] The name of the Project
	 */
	public void setName(String newName)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IStructureEventDispatcher disp = (IStructureEventDispatcher) 
						  ret.getDispatcher(EventDispatchNameKeeper.structure());
		boolean proceed = true; 
		String proposedName = newName;
		boolean fireRenameEvent = false;
		ETTripleT<Boolean, String, Boolean> result = doNamesDiffer(proposedName);
		proposedName = result.getParamTwo();
		fireRenameEvent = ((Boolean)result.getParamThree()).booleanValue();
		if( disp != null && fireRenameEvent)
		{
		   IEventPayload payload = disp.createPayload("ProjectPreRename" );
		   proceed = disp.fireProjectPreRename(this,newName,payload);			 
		}

                // check for invalid name bug fix 5104815
                if (proceed)
		{
			IElementChangeDispatchHelper helper = new ElementChangeDispatchHelper();
			proceed = helper.dispatchPreNameModified(this, newName);
                }

		if( proceed )
		{
			String oldName = getName();
			IWSProject wsProject = getWSProject();
			super.setName(newName);
			establishTypeManager();
			// Rename the WSProject that corresponds with this IProject
			if( wsProject != null)
			{
			   wsProject.setName( newName );
			}
			if (disp != null && fireRenameEvent)
			{
				IEventPayload payload = disp.createPayload("ProjectRenamed" );
				disp.fireProjectRenamed(this,oldName,payload);
			}			
		}			
	}
	
	/**
	 *
	 * Retrieves the corresponding IWSProject. This gives easier 
	 * access to the save mechanism
	 *
	 * @return wsProject[out] The corresponding IWSproject
	 */
	public IWSProject getWSProject()
	{
		IWSProject wsProj = null;
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null)
		{
			IWorkspace workspace = prod.getCurrentWorkspace();
			if (workspace != null)
			{
				String name = getName();
                if (name != null)
				    wsProj = workspace.getWSProjectByName(name);
			}
		}
		return wsProj;
	}
	
	/**
	 *
	 * Sends an urgent message to the Message service alerting the user that we
	 * were unable to save the Project
	 *
	 * @param error[in] The error number.
	 *
	 */
	public void sendSaveError()
	{
        String name = getName();
        String s = StructureMessages.getString("IDS_UNABLE_TO_SAVE", 
            new Object[] { name });
        UMLMessagingHelper mes = new UMLMessagingHelper("Project");
        mes.sendCriticalMessage(s);
    }

	/**
	 *
	 * Performs the delete of the passed in file. If the file does not exist,
	 * this method returns a successfull status.
	 *
	 * @param fileName[in]     The location of the file to delete
	 * @param errorCode[out]   The error code if one happens
	 *    
	 * @return  - true if the file specified was deleted or never existed, else
	 *          - false if the delete failed. In this case, the errorCode parameter
	 *            will contain the error status.
	 *
	 */
	public boolean deleteFile(String fileName)
	{
		boolean status = false;
		if (fileName != null)
		{
			File file = new File(fileName);
			if (!file.exists())
			{
				status = true;
			}
			else
			{
				status = file.delete();
			}				
		}
		return status;
	}
	
	/**
	 *
	 * Commits the temporary file that was created during the PreCommit
	 * to the original file. 
	 *
	 * @param tempName[in] The name of the temporary file.
	 *
	 * @return true if the commit succeeded, else false
	 *
	 */
//	public boolean commitProjectFile(String tempName)
//	{
//		boolean isSuccess = false;
//
//		if( m_ProjectNeedsCommit )
//		{
//			if ( deleteFile(m_OrigFileName) )
//			{
//				isSuccess = moveFile(tempName,m_OrigFileName);
//			}
//			else
//			{
//				sendSaveError();
////				m_IsDirty = true;
//                                setDirty(true);
//			}
//		}
//		else
//		{
//			// Just clean up the file used for the temporary name
//			deleteFile(tempName);
//		}
//		return isSuccess;
//	}
	
	/**
	 *
	 * Performs the necessary clean up of the Project after a save has just occurred.
	 *
	 * @param projSaveResult[in]  The HRESULT returned from the actual save of the XMLDOMDocument
	 *                            that this Project represents. If anything other than S_OK, that
	 *                            HRESULT will be returned by this method.
	 * @param curFileName[in]     The name of the file the Project is currently being saved to.
	 * @param removed[in]         The collection of presentation elements removed during the save process.
	 * @param nodeElement[in]     The actual element representing this Project
	 *
	 * @return If projSaveResult comes in with any value other than S_OK, that result is returned. Otherwise,
	 *         status of the after save process is returned.
	 *
	 */
	public void performAfterSaveCleanUp(String curFileName,
                                   Map< Node, ETList<Node> > removed,
								   Element nodeElement)
	{
		// Use XMLManip here so that we don't cause an element modified
		XMLManip.setAttributeValue(nodeElement,"fileName",curFileName);
		// Now convert the relative paths back into absolute paths
		UMLXMLManip.convertRelativeHrefs(curFileName,new VersionableElement());
		
		// Now put the removeOnSaved elements back into the DOM tree
		injectRemoveOnSaves(removed);
		
		// Now set the fileName attribute to the value of the passed
		// in fileName. This is how we keep track of the filename
		// that this project is being saved to.
		// Retrieve the default imports again
		if (loadDefaultImports())
		{
//			m_IsDirty = false;
                    setDirty(false);
		}
		m_ChildrenDirty = false;
	}								   
	
	/**
	 *
	 * Cleans up all the version information associated with this Project 
	 * node.
	 */
	public void removeVersionInformation()
	{
		super.removeVersionInformation();
		Element element = (Element) m_Node;
		if (element != null)
		{
         Attribute attr = element.attribute("scmID");
         if(attr != null)
         {
            element.remove(attr);

            IWSProject wsProj = getWSProject();
            if (wsProj != null)
            {
               wsProj.setSourceControlID("");
            }
//            m_IsDirty = true;
            setDirty(true);
         }
		}
	}
	
	public void onElementDeleted(IVersionableElement ver)
	{
		establishDirtyState(ver);
	}
	
	/**
	 * Validate the passed in values according to the Describe business rules.
	 * See method for the rules.
	 *	 
	 * @param fieldName[in]		The name of the field to validate
	 * @param fieldValue[in]	The string to validate
	 * @return outStr[out]		The string changed to be valid (if necessary)
	 * @return bValid[out]		Whether the string is valid as passed in
	 */
	public boolean validate(String fieldName)
	{
		boolean isValid = true;
		// Using this mechanism to determine whether or not a project's mode
		// should be read-only or not in the property editor.  This was normally used
		// to validate data before it was saved, but the same mechanism could
		// be used for this.
		// We are not allowing the user to go back to analysis or design mode
		// once in implementation mode, so this will tell the property editor
		// to make the field read-only
		String name = fieldName;
		if (name.equals("Mode"))
		{
			String mode = getMode();
			if (mode.equals("Implementation") || 
				mode == StructureConstants.PSK_IMPLEMENTATION)
			{
				isValid = false;
			}
		}
		return isValid;
	}
	
	/**
	 *
	 * Called during Save processing. Save directly to the model files (*.etd and *.ettm)
	 */
	public void internalCommit()
	{
		String fileName = getCurFile();   //<project folder>\*.etd file
		if (fileName != null)
		{
			m_OrigFileName = fileName;
			m_ExtManager = new ExternalFileManager();
			if (m_ExtManager != null)
			{
                                if (m_IsDirty)
                                {
                                    internalSave(fileName);
                                    // Now save the TypeManager
                                    if (m_TypeManager != null)
                                    {
                                            String typeManFile = StringUtilities.ensureExtension(fileName,".ettm");
                                            m_TypeManager.save(typeManFile);															
                                    }
    //					
                                    m_ExtManager.commit();
                                    setDirty(false);
                                    m_ChildrenDirty   = false;
                                    m_ExtManager = null;
                                    m_OrigFileName = "";	
                                    //m_ProjectNeedsCommit = false;
                                }
			}
		}
	}
		
	/**
	 *
	 * Called during the Save() processing
	 */
	public void internalSave(String fileName)
	{	      	
		Element nodeElement = getElementNode();
		if (nodeElement != null)
		{
                    String curFileName = fileName;    
                    Map< Node, ETList<Node> > removed = null;
                    try
                    {
   			if (curFileName == null || curFileName.length() < 0)
   			{
   				// Let's get the current name of the project and save to that
   				curFileName = getAttributeValue("fileName");
   			}
   			// We don't want to hard-code the the file name in the XML
   			// file, so we need to remove the attribute if it is there			
   			nodeElement.remove(nodeElement.attribute("fileName"));	
   			
   			validateDTDSettings(curFileName);
   			// Remove any nodes that are marked with the removeOnSave attribute.
   			// Be sure to do this before handling versioned elements to be sure
   			// not to output presentation element information to the .etx
   			// files
   			removed = processRemoveOnSaves();
            
   			// Handle versioned elements and elements that need
   			// to be versioned
   			handleElementVersioning( nodeElement, curFileName );
            
   			// Remove the absolute path references in any child
   			// elements
   			UMLXMLManip.convertAbsoluteHrefs(nodeElement, curFileName);
   			//Tell the ElementDisposal object to do 
   			if (m_Disposal != null)
   			{
   				m_Disposal.disposeElements();
   			}

   			if (m_IsDirty)
   			{
                                XMLManip.save(m_Doc,curFileName);
   			}
                     }
                     finally
                     {
                        performAfterSaveCleanUp(curFileName,removed,nodeElement);
                     }
                }
	}
    
	public void close()
	{
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null)
		{
			IApplication app = prod.getApplication();
			if (m_TypeManager != null)
			{
				// This MUST come before the CloseCache() call
				// later in this routine. That's 'cause the TypeManager
				// is calling the Deinitialize() on the IPickListManager,
				// which is controlled by the QueryManager. Not ideal, but
				// we'll live with this for now.
				m_TypeManager.setProject(null); 
			}
			if (app != null)
			{
				IQueryManager queryManager = app.getQueryManager();
				if (queryManager != null)
				{
					queryManager.closeCache(this);
				}
			}
		}
		deInitialize();
	}
	
	/**
	 *
	 * Adds a location that should point at a .etd file that this project will
	 * now reference. This new etd file will be used for type resolutions
	 *
	 * @param libLocation[in]  The absolute location to the library
	 */
	public void addReferencedLibrary( String libLocation )
	{
		if (libLocation != null && libLocation.length() > 0)
		{
			String fileName = getCurFile();
			// Make sure the passed in file exists
			String expandedLoc = FileManip.resolveVariableExpansion(libLocation);
			// __uuidof( IProject ) is the second parameter.
			Validator.verifyFileExists(expandedLoc);
			// Make sure that a library reference with the given location is 
			//not already present
			Node foundNode = getRefLibNode(libLocation);
			if ( foundNode == null && fileName.length() > 0 )
			{
				// Only add the import if the files aren't the same!
				if (fileName.equals(expandedLoc))
				{					
					IElementChangeDispatchHelper helper = new ElementChangeDispatchHelper();
					EventDispatchRetriever ret = EventDispatchRetriever.instance();
					IStructureEventDispatcher disp = (IStructureEventDispatcher) 
									  ret.getDispatcher(EventDispatchNameKeeper.structure());
					boolean proceed = true;
					if( disp != null)
					{
					   IEventPayload payload = disp.createPayload("PreReferencedLibraryAdded" );
					   proceed = disp.firePreReferencedLibraryAdded(this,libLocation,payload);			 
					}			
					if( proceed )
					{
						boolean process = UMLXMLManip.fireElementPreModified(this,helper);
						if (process)
						{
							Node node = ensureElementExists("UML:Project.referencedLibraries",
										  		"UML:Project.referencedLibraries" );
							if (node != null)
							{								
								Document doc = m_Node.getDocument();
								DocumentFactory fact = DocumentFactory.getInstance();
								Element newEle = fact.createElement("UML:ReferencedLibrary");
								doc.add(newEle);
								Node refLibNode = null;
								if (refLibNode != null)
								{
									UMLXMLManip.appendChild(node,refLibNode);
									if (fileName != null)
									{
										String relativeLibLocation = PathManip.retrieveRelativePath(libLocation,fileName);
										XMLManip.setAttributeValue(refLibNode,"location",relativeLibLocation);
										injectRefLibIntoWorkspace(expandedLoc);															 										  		
									}
								}
							}
							if (helper != null)
							{
								helper.dispatchElementModified(this);
							}
							if (disp != null)
							{
								IEventPayload payload = disp.createPayload("ReferencedLibraryAdded" );
								disp.fireReferencedLibraryAdded(this,libLocation,payload);			
							}
							
						}
					}					
				}
			}	
		}
	}
	
	/**
	 *
	 * Removes the node reference at the location specified
	 *
	 * @param libLocation[in] The location of the library to remove from
	 *                        this project
	 */
	public void removeReferencedLibrary(String libLocation)
	{
		if (libLocation != null && m_Node != null)
		{
			Node node = getRefLibNode(libLocation);
			if (node != null)
			{
				Node parent = node.getParent();
				if (parent != null)
				{
					IElementChangeDispatchHelper helper = new ElementChangeDispatchHelper();
					EventDispatchRetriever ret = EventDispatchRetriever.instance();
					IStructureEventDispatcher disp = (IStructureEventDispatcher) 
									  ret.getDispatcher(EventDispatchNameKeeper.structure());
					boolean proceed = true;
					if( disp != null)
					{
					   IEventPayload payload = disp.createPayload("PreReferencedLibraryAdded" );
					   proceed = disp.firePreReferencedLibraryRemoved(this,libLocation,payload);			 
					}			
					if( proceed )
					{
						boolean process = UMLXMLManip.fireElementPreModified(this,helper);
						if (process)
						{
							XMLManip.removeChild(parent,node.getName());
							if (helper != null)
							{
								helper.dispatchElementModified(this);							
							}
							if (disp != null)
							{
								IEventPayload payload = disp.createPayload("ReferencedLibraryRemoved" );
								disp.fireReferencedLibraryRemoved(this,libLocation,payload);			
							}
								
						}
					}
				} 					
			}
		}
	}
		
	/**
	 *
	 * Retrieves the collection of absolute locations to the libraries
	 * this Project references
	 *
	 * @return pVal[out] The collection
	 */
	public ETList<String> getReferencedLibraries()
	{
		ETList<String> libs = new ETArrayList<String>();
		if (m_Node != null)
		{
			List nodes = m_Node.selectNodes("UML:Project.referencedLibraries/UML:ReferencedLibrary");
			if (nodes != null)
			{
				int num = nodes.size();
				if (num > 0)
				{
					String projFile = getCurFile();					
					for (int i=0;i<num;i++)
					{
						Node node = (Node)nodes.get(i);
						if (node != null)
						{
							String location = XMLManip.getAttributeValue(node,"location");
							if (location != null)
							{
								String expandedLoc = FileManip.resolveVariableExpansion(location);
								String absolutePath = PathManip.retrieveRelativePath(expandedLoc,projFile);
								libs.add(absolutePath);	
							}
						}
					}
				}
			}
		}
		return libs;
	}
	
	/**
	 *
	 * Retrieves the node of an existing reference library node
	 *
	 * @param refLoc[in] The absolute location of the library
	 * @return node[out]  The found node, else 0
	 */	
	public Node getRefLibNode(String refLoc)
	{
		String fileName = getCurFile();
		Node node = null;
		if (fileName != null && refLoc != null)
		{
			String relativeLibLocation = PathManip.retrieveRelativePath(refLoc,fileName);
			if (relativeLibLocation != null)
			{
				XMLManip.checkForIllegals(relativeLibLocation);
				String query = "UML:Project.referencedLibraries/UML:ReferencedLibrary[@location=\"";
				query += relativeLibLocation+"\"";
				node = m_Node.selectSingleNode(query);
			}
		}
		return node;
	}

	/**
	 *
	 * Determines whether or not the Project is in "Library" mode, which essentially just 
	 * turns off Roundtrip temporarily.
	 *
	 * @return pVal[out] The current value
	 */
	public boolean getLibraryState()
	{ 
		return m_InLibraryState;
	}
	
	public void setLibraryState(boolean pVal)
	{
		m_InLibraryState = pVal;		
	}
	
	public IProject getReferencedLibraryProjectByLocation(String refLibLoc)
	{
		IProject pProj = null;
		if (m_TypeManager != null)
		{
			 pProj = 
                m_TypeManager.getReferencedLibraryProjectByLocation(refLibLoc);
		}
		return pProj;
	}
	
	public ETList<IProject> getReferencedLibraryProjects()
	{
        ETList<IProject> pProjects = null;
		if (m_TypeManager != null)
		{
			pProjects = m_TypeManager.getReferencedLibraryProjects();
		}
		return pProjects;
	}
	
	public String translateString(String sPSK)
	{
		IConfigStringTranslator translator = null;
		ConfigStringHelper csHelp = ConfigStringHelper.instance();
		String retStr = sPSK;
		if (csHelp != null)
		{
			translator = csHelp.getTranslator();
			if (translator != null)
			{
				retStr = translator.translate(null,sPSK);
			}
		}
		return retStr;		
	}
	
	/**
	 *
	 * Inserts the project at the passed in location into the namespace the project is
	 * in.
	 *
	 * @param locationOfRefLib[in] The location of the project to insert
	 */
	public void injectRefLibIntoWorkspace(String locationOfRefLib)
	{
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null)
		{
			IWorkspace workspace = prod.getCurrentWorkspace();
			if (workspace != null)
			{
				IProject libProj = getReferencedLibraryProjectByLocation(locationOfRefLib);
				if (libProj != null)
				{
					IApplication app = prod.getApplication();
					if (app != null)
					{
						IWSProject wsProj = app.importProject(workspace,libProj);
					}
				}
			}
		}
	}
	
	/**
	 *
	 * Called by the framework whenever the user clicks the "Save all" button. 
	 * Project handles this event by unloading external elements that were loaded.
	 *
	 * @param pProd[in]
	 */
	public void onCoreProductSaved(String pProd)
	{
		boolean isDirty = false;
		isDirty = isDirty();
		if (isDirty)
		{
			// We should only process the external elements if the
			// Project is dirty. We don't want to call the methods
			// below if the Project is clean for some reason,
			// as the user will not be notified that elements could
			// be saved. The whole purpose of this event handler
			// is to simply dump external elements that were previously
			// loaded.
			if (!isDirty())
			{
				Element nodeElement = getElementNode();
				if (nodeElement != null)
				{
					String curFileName = getCurFile();
					handleElementVersioning(nodeElement,curFileName);
				}
			}
		}
	}
	
	/**
	 *
	 * Checks to see if an old default import is in the project.
	 *
	 * @return true if the import exists, else false
	 *
	 */
	public boolean requiresOldImports()
	{
		boolean required = m_OldImports;			
		if (m_Node != null && m_OldImports == false)
		{
			String query = "UML:Package.packageImport/UML:PackageImport/UML:PackageImport.importedPackage/UML:Package[@name=\"Java Data Types\" and @xmi.id=\"J.0\"]";
			Node node = m_Node.selectSingleNode(query);
			if (node != null)
			{
				required = true;
				m_OldImports = true;
			}			
		}
		return required;
	}
	
	/**
	 *
	 * This is here to support 6.03 Describe Developer projects. In Describe
	 * Enterprise 6.0, we no longer inject these default package imports.
	 *
	 */
	public void establishOldDefaultPackageImports()
	{
		IPreferenceManager pref = new PreferenceManager();
		// Grab the Preference Manager and see what the default
		// package imports for new projects is
		Node xmlLibs = null;
		if (m_DefaultImports != null)
		{
			m_DefaultImports = null;
		}
		
		IPackage thisPack = null;
		if (this instanceof IPackage)
		{
			thisPack = (IPackage)this;
		}
		boolean origBlock = EventBlocker.startBlocking();
		try
		{
			pref.installDefaultModelLibraries(thisPack, m_DefaultImports);
		}
		finally
		{
			EventBlocker.stopBlocking(origBlock);
		}
	}

	public boolean getChildrenDirty()
	{
		return m_ChildrenDirty;		
	}
	public void setChildrenDirty( boolean value )
	{
		m_ChildrenDirty = value;
	}
	
//	private boolean moveFile(String source_file1, String destination_file1)
//	{
//		boolean isSuccess = false;
//		try
//		{	
//			File source_file = new File(source_file1);
//			if(source_file.exists())	
//			{		
//				File dest_file = new File(destination_file1);
//				if(!dest_file.exists())		
//				{			
//					isSuccess = source_file.renameTo(dest_file);					
//				}	
//			}
//		}
//		catch(Exception e)
//		{		 
//			e.printStackTrace();
//		}
//		return isSuccess;
//	}
	
	/**
	 * Sets / Gets the name of the file this project will be saved to.
	*/
	public String getFileName()
	{		
		Element element = getElementNode();
		String curFile = null;
		if (element != null)
		{
			curFile = XMLManip.getAttributeValue(element,"fileName");
		}
		return curFile;			
	}

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink#onElementPreModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementPreModified(IVersionableElement element, IResultCell cell)
    {
        //nothing to do
    }

	/**
	 *
	 * This Project is listening for any element modified's coming through on elements that it owns.
	 * If the element is owned by this project, the dirty flag is set to true.
	 *
	 * @param element[in] Element being modified
	 * @param cell[in] The cell with the event
	 *
	 * @return HRESULT
	 *
	 */
    public void onElementModified(IVersionableElement element, IResultCell cell)
    {
		establishDirtyState( element );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink#onDocumentationPreModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDocumentationPreModified(IElement element, String doc, IResultCell cell)
    {
    	//nothing to do
    }

	/**
	 *
	 * Sets the dirty flag of this project when the documentation of one of its element's is modified.
	 *
	 * @param element[in] The element
	 * @param cell[in] The cell
	 *
	 * @return HRESULT
	 *
	 */
    public void onDocumentationModified(IElement element, IResultCell cell)
    {
		establishDirtyState( element );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreCreate(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementPreCreate(String ElementType, IResultCell cell)
    {
        //nothing to do
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementCreated(IVersionableElement element, IResultCell cell)
    {
        //nothing to do
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDelete(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementPreDelete(IVersionableElement element, IResultCell cell)
    {
        //nothing to do
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementDeleted(IVersionableElement element, IResultCell cell)
    {
		establishDirtyState( element );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementPreDuplicated(IVersionableElement element, IResultCell cell)
    {
        //nothing to do
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementDuplicated(IVersionableElement element, IResultCell cell)
    {
        //nothing to do
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreInit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onCoreProductPreInit(ICoreProduct pVal, IResultCell cell)
    {
        //nothing to do
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductInitialized(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onCoreProductInitialized(ICoreProduct newVal, IResultCell cell)
    {
        //nothing to do
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreQuit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onCoreProductPreQuit(ICoreProduct pVal, IResultCell cell)
    {
        //nothing to do
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onCoreProductPreSaved(ICoreProduct pVal, IResultCell cell)
    {
        //nothing to do
    }

	/**
	 *
	 * Called by the framework whenever the user clicks the "Save all" button. 
	 * Project handles this event by unloading external elements that were loaded.
	 *
	 * @param pProd[in]
	 * @param IResultCell*[in]
	 *
	 * @return 
	 *
	 */
    public void onCoreProductSaved(ICoreProduct newVal, IResultCell cell)
    {
        boolean isDirty = isDirty();

		// We should only process the external elements if the
		// Project is dirty. We don't want to call the methods
		// below if the Project is clean for some reason,
		// as the user will not be notified that elements could
		// be saved. The whole purpose of this event handler
		// is to simply dump external elements that were previously
		// loaded.
        if (!isDirty)
        {
        	Element nodeEle = getElementNode();
        	if (nodeEle != null)
        	{
        		String fileName = getCurFile();
        		handleElementVersioning(nodeEle, fileName);
        	}
        }
    }

   /**
    * Sets the version number on this project, retrieved from calling the Application's
    * get_ApplicationVersion() method.
    * 
    * @return HRESULT 
    */

   private void establishVersionNumber()
   {
      ICoreProduct prod = ProductRetriever.retrieveProduct();
      if (prod != null)
      {
         IApplication app = prod.getApplication();
         if (app != null)
         {
            String version = app.getApplicationVersion();
            if (version != null && version.length() > 0)
            {
               UMLXMLManip.setAttributeValue(this, "projVersion", version);
            }
         }
      }
   }   
   
   /**
     * Sets the source roots that is associated with the project.
     */
    public void setAssociatedProjectSourceRoots(IAssociatedProjectSourceRoots sourceRoots)
    {
      mSourceRoots = sourceRoots;
    }
    
    /**
     * Retrieve the source roots associated with the project.
     */
    public IAssociatedProjectSourceRoots getAssociatedProjectSourceRoots()
    {
       return mSourceRoots;
    }
    
    //
    // Property change support
    //

    /** Add a property change listener.
     * @param l the listener to add
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        synchronized (listenersMethodLock) {
            if (changeSupport == null)
                changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(l);
    }

    /** Remove a property change listener.
     * @param l the listener to remove
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        if (changeSupport != null)
            changeSupport.removePropertyChangeListener(l);
    }

    /** Fires property change notification to all listeners registered via
    * {@link #addPropertyChangeListener}.
    *
    * @param name of property
    * @param oldValue old value
    * @param newValue new value
    */
    protected final void firePropertyChange (String name, Object oldValue, Object newValue) {
        if (changeSupport != null)
            changeSupport.firePropertyChange(name, oldValue, newValue);
    }
    
    
    private synchronized void loadImports()
    {
        if (init)
            return;
        
        m_ElementImports = retrieveElementCollection(null,
                "//UML:Package.elementImport/*",
                IElementImport.class);

        m_PackageImports = retrieveElementCollection(null,
                "//UML:Package.packageImport/*",
                IPackageImport.class);

        init = true;
    }
    
    
    public void addElementImport(IElementImport elem, INamespace owner)
    {
        super.addElementImport(elem, owner);
        if (!init)
        {
            loadImports();
        }
        m_ElementImports.add(elem);
    }
    
    
    public void addPackageImport(IPackageImport pack, INamespace owner)
    {
        super.addPackageImport(pack, owner);
        if (!init)
        {
            loadImports();
        }
        m_PackageImports.add(pack);
    }
    
    public long getElementImportCount()
    {
//        return UMLXMLManip.queryCount(m_Node, "//UML:Package.elementImport/*", false);
        if (!init)
            loadImports();
        return m_ElementImports.size();
    }
    
    public long getPackageImportCount()
    {
//        return UMLXMLManip.queryCount(m_Node, "//UML:Package.packageImport/*", false);
        if (!init)
            loadImports();
        return m_PackageImports.size();
    }
    
    public ETList<IPackageImport> getPackageImports()
    {
        if (!init)
        {
            loadImports();
        }
        return m_PackageImports;
        
    }
    
    public ETList<IElementImport> getElementImports()
    {
        if (!init)
        {
            loadImports();
        }
        return m_ElementImports;
    }
    
    public void removePackageImport(IPackageImport elem)
    {
//        IElement remEle = removeElement( elem, "//UML:Package.packageImport/*");
        removeElementImport(elem);
    }
    
    
    public void removeElementImport(IElement elem)
    {
        if (elem instanceof IPackage || elem instanceof IPackageImport)
        {
            ETList<IPackageImport> imports = getPackageImports();
            // avoid concurrent modification to the imported list
            ArrayList<IPackageImport> list = new ArrayList<IPackageImport>();
            list.addAll(imports);
            for (IPackageImport im: list)
            {
                IPackage element = im.getImportedPackage();
                if (element == null)
                    return;
                if ( im == elem || elem.getXMIID().equals(element.getXMIID()))
                {      
                    // im.delete() basically fires delete event, it's important to 
                    // call im.delete() before removeChild
                    setDirty(true);
                    im.delete();
                    UMLXMLManip.removeChild(this.getNode(), im);
                    m_PackageImports.remove(im);
                }
            }
        }
        else
        {
            ETList<IElementImport> imports = getElementImports();
            ArrayList<IElementImport> list = new ArrayList<IElementImport>();
            list.addAll(imports);
            for (IElementImport im: list)
            {
                IElement element = im.getImportedElement();
                if (element == null)
                    return;
                if (im == elem || elem.getXMIID().equals(element.getXMIID()))
                {
                    setDirty(true);
                    im.delete();
                    UMLXMLManip.removeChild(this.getNode(), im);
                    m_ElementImports.remove(im);
                }
            }
        }
    }

}
