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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.support.umlmessagingcore.UMLMessagingHelper;
import org.netbeans.modules.uml.core.support.umlsupport.PathManip;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.URILocator;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.dom4j.Attribute;

//import com.embarcadero.describe.foundation.IConfigManager;
/**
 * @author sumitabhk
 *
 */
public class ExternalFileManager 
{
	public static int EF_PRECOMMIT = 0;
	public static int EF_COMMIT = 1;
	
	public static String m_rootFileName = null;
	public static String m_ConfigLocation = null;
	private String m_ProjectXMIID = null;
   //contains expansion variable and the string it expands to.
	public static Hashtable m_Vars = new Hashtable();
	public Hashtable m_Temps = new Hashtable();
	private String m_FragmentXML = null;
	private int m_Mode = EF_PRECOMMIT;

	/**
	 * 
	 */
	public ExternalFileManager() {
		super();
	}

	public void setProjectXMIID(String project)
	{
		m_ProjectXMIID = project;
	}
	
	public String getProjectXMIID()
	{
		return m_ProjectXMIID;
	}

	/**
	 *
	 * This method is called to resolve in references to external files. If there
	 * is a reference, then the node found in that file will replace the node
	 * referencing it.
	 *
	 * @param nodeList[in] the list of nodes to validate.
	 * @param modified[out] a node in the list has been resolved.
	 *
	 * @return HRESULTs
	 *
	 */
//	public static boolean resolveExternalNodes(List list) {
//		boolean modified = false;
//		if (list != null)
//		{
//			int count = list.getLength();
//			for (int i=0; i<count; i++)
//			{
//				Node n = list.item(i);
//				org.dom4j.Node resolved = resolveExternalNode(n);
//				if (resolved != null)
//				{
//					modified = true;
//				}
//			}
//		}
//		return modified;
//	}

	public static boolean resolveExternalNodes(List list) 
	{
		boolean modified = false;
		if (list != null)
		{
			int count = list.size();
			for (int i=0; i<count; i++)
			{
				org.dom4j.Node n = (org.dom4j.Node)list.get(i);
				org.dom4j.Node resolved = resolveExternalNode(n);
				if (resolved != null)
				{
					modified = true;
				}
			}
		}
		return modified;
	}

	/**
	 *
	 * This method is called to resolve any references to external files. If there
	 * is a reference, then the node found in that file will replace the node
	 * referencing it.
	 *
	 * @param node[in]            The node that is being resolved.
	 * @param resolvedNode[out]   If the node passed in needed to be resolved, the results of that
	 *                            resolution are returned in resolvedNode. This is the node that 
	 *                            should now be used
	 *
	 * @return HRESULTs
	 *
	 */
	public static org.dom4j.Node resolveExternalNode(org.dom4j.Node n) 
	{
		org.dom4j.Node retNode = null;
		if (n != null && n.getNodeType() == org.dom4j.Node.ELEMENT_NODE)
		{
			org.dom4j.Element element = (org.dom4j.Element)n;

			// The href attribute holds the location of the actual node
			// that we need
			String value = element.attributeValue("href");
			if (value != null && value.length() > 0)
			{
				// Check to see if this element has already been resolved
				String loaded = element.attributeValue("loadedVersion");
				if (loaded == null || loaded.length() == 0)
				{
					boolean proceed = true;
					String actualHref = retrieveDocLocation(element);
					EventDispatchRetriever ret = EventDispatchRetriever.instance();
					IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher) ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
					proceed = firePreExternalLoadEvent(actualHref, disp);
					
					if (proceed)
					{
						// This node has not been resolved, so let's go get it.
						org.dom4j.Node exNode = URILocator.retrieveNode(actualHref);
						if (exNode != null)
						{
							retNode = loadExternalNode(disp, element, exNode, actualHref);
						}
					}
				}
			}
		}
		return retNode;
	}
        
        
        public static org.dom4j.Node getExternalNode(org.dom4j.Node n)
        {
            org.dom4j.Node retNode = null;
            if (n != null && n.getNodeType() == org.dom4j.Node.ELEMENT_NODE)
            {
                org.dom4j.Element element = (org.dom4j.Element)n;
                
                // The href attribute holds the location of the actual node
                // that we need
                String value = element.attributeValue("href");
                if (value != null && value.length() > 0)
                {
                    String actualHref = retrieveDocLocation(element);   
                    retNode = URILocator.retrieveNode(actualHref);
                }
            }
            return retNode;
        }
        
        
        
        /**
         *
         * Fires the pre event before loading an external element into the current DOM tree.
	 *
	 * @param href[in] The location of the element to load
	 * @param dispatcher[in] The retrieved dispatcher
	 * @param proceed[out] true to continue the event processing, else false to bail.
	 *
	 * @return HRESULT
	 *
	 */
	private static boolean firePreExternalLoadEvent(String href, IElementChangeEventDispatcher disp) {
		boolean proceed = true;
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("ExternalElementPreLoaded");
			proceed = disp.fireExternalElementPreLoaded(href, payload);
		}
		return proceed;
	}

	/**
	 *
	 * Retrieves the document location of the passed in element.
	 *
	 * @param element[in] The element
	 * @param actualHREF[out] The actual HREF of the passed in element, after
	 *                        variable expansion.
	 * @param loc[in] The URILocator to perform much of the work
	 *
	 * @return HRESULT
	 *
	 */
	private static String retrieveDocLocation(org.dom4j.Element element) {
		String actualhref = null;
		String docLoc = null;
		if (element != null)
		{
			String href = element.attributeValue("href");
			if (href != null && href.length() > 0)
			{
				// If the raw HREF attribute has a variable that needs to be
				// expanded, perform that expansion now. These variables
				// are in the form %variable%
				actualhref = resolveVariableExpansion(href);
				ETPairT<String, String> obj = URILocator.uriparts(actualhref);
				docLoc = obj.getParamOne();
				String nodeLoc = obj.getParamTwo();
				if (isPathRelative(docLoc))
				{
					String rootfile = getRootFileName();
					if (rootfile == null || rootfile.length() == 0)
					{
						org.dom4j.Document doc = element.getDocument();
						if (doc != null)
						{
							INamespace space = UMLXMLManip.getProject(doc);
							if (space instanceof IProject)
							{
								IProject proj = (IProject)space;
								String projFileName = proj.getFileName();
								if (projFileName.length() > 0)
								{
									rootfile = projFileName;
								}
							}
						}
					}
					
					if ((rootfile != null) && (rootfile.length() > 0))
					{
						docLoc = PathManip.retrieveAbsolutePath(docLoc, rootfile);
						actualhref = docLoc + "#" + nodeLoc;
					}
				}
			}
		}
		return actualhref;
	}

	/**
	 *
	 * Takes the incoming string and checks to see if any variables exist
	 * in the string that need to be expanded.
	 *
	 * @param href[in] The string to check
	 *
	 * @return The expaned string, else the string that was passed in.
	 *
	 */
	private static String resolveVariableExpansion(String href) {
		establishExpansionVars();
		String result = href;
		int pos = href.indexOf("%");
		if (pos >= 0)
		{
			String subStr = href.substring(pos);
			int endPos = subStr.indexOf("%");
			if (endPos >= 0)
			{
				String possibleVar = subStr.substring(0, endPos);
				if (possibleVar.length() > 0)
				{
					IPreferenceAccessor pPref = PreferenceAccessor.instance();
					if (pPref != null)
					{
						String value = "";
						String theValue = pPref.getExpansionVariable(possibleVar);
						if (theValue == null || theValue.length() == 0)
						{
							// This is probably a "special" var, such as CONFIG_LOCATION
							if( possibleVar.equals("CONFIG_LOCATION" ))
							{
								value += m_ConfigLocation;
							}
						}
						else
						{
							value += theValue;
						}
						
						String var = "%" + possibleVar + "%";
						result = StringUtilities.replaceSubString(href, var, value);
					}
				}
			}
		}
		return result;
	}

	/**
	 *
	 * Reads the PreferenceManagers list of expansion variables.
	 *
	 *
	 * @return HRESULT
	 *
	 */
	private static void establishExpansionVars() {
		if (m_Vars.size() == 0)
		{
			// get the default config location
			ICoreProduct prod = ProductRetriever.retrieveProduct();
			if (prod != null)
			{
				IConfigManager conMan = prod.getConfigManager();
				if (conMan != null)
				{
					String loc = conMan.getDefaultConfigLocation();
					if (loc.length() > 0)
					{
						m_ConfigLocation = loc;
					}
				}
			}
		}
	}

	/**
	 *
	 * Performs the low level load of an external element
	 *
	 * @param dispatcher[in]   The event dispatcher to fire the Post load event on.
	 * @param element[in]      The element that is being resolved. This is the element
	 *                         who is getting replaced in the main DOM document
	 * @param exNode[in]       The actual node in the external document
	 * @param actualHREF[in]   The full href to the external doc and node
	 * @param resolvedNode[out]Set if the node is successfully replaced. 
	 *
	 * @return HRESULT
	 *
	 */
	private static org.dom4j.Node loadExternalNode(IElementChangeEventDispatcher dispatcher, 
                                                  org.dom4j.Element element, 
                                                  org.dom4j.Node exNode,
                                                  String actualHref) 
   {
      org.dom4j.Node retNode = null;
		if (exNode != null && exNode.getNodeType() == org.dom4j.Node.ELEMENT_NODE)
		{
			org.dom4j.Element exEle = (org.dom4j.Element)exNode;
			org.dom4j.Element parent = element.getParent();
			if (parent != null)
			{
				// Replace the current skeleton node in the Project
				// tree with the node we have just retrieved. We also
				// won't worry about cloning exNode as we are not
				// going to save that external file. If we did
				// save that file, we would need to clone.
				//Node out = parent.replaceChild(element, exEle);
				Document doc = parent.getDocument();
				if (doc != null)
				{
					doc.remove(element);
				}
				element.detach();
				Element cloneEle = exEle.createCopy();
				
				Element exEleParent = exEle.getParent();
				cloneEle.detach();
				parent.add(cloneEle);
				cloneEle.setParent(parent);
				//exEle.setParent(parent);
				
				// Make sure the href value still points at the href that
				// brought the element in.
				cloneEle.addAttribute("href", actualHref);
				
				// Now set the "loadedVersion" attribute to true
				// so we don't drop in here again
				cloneEle.addAttribute("loadedVersion", "true");
				
				fireExternalLoadEvent(dispatcher, cloneEle);
				exEle.setParent(exEleParent);
				retNode = cloneEle;
				
			}
		}
		return retNode;
	}

	/**
	 *
	 * Fires the event that signals that an external element has been loaded successfully into the
	 * current Project
	 *
	 * @param disp[in] The dispatcher
	 * @param element[in] The element that was loaded
	 *
	 * @return HRESULT
	 *
	 */
	private static void fireExternalLoadEvent(IElementChangeEventDispatcher disp, org.dom4j.Element exEle) {
		if (disp != null)
		{
			TypedFactoryRetriever <IVersionableElement> ret = new TypedFactoryRetriever<IVersionableElement>();
			IVersionableElement elem = ret.createTypeAndFill(exEle);
			if (elem != null && disp != null)
			{
				IEventPayload payload = disp.createPayload("ExternalElementLoaded");
				disp.fireExternalElementLoaded(elem, payload);
			}
		}
	}

	/**
	 *
	 * Finds out where the external file is that node is referring to and then
	 * deletes it. Removes node from parent after that is done.
	 *
	 * @param parent[in] the parent node, trying to remove the child node.
	 *	@param node[in] the child about to be destroyed.
	 *
	 * @return HRESULTs
	 *
	 */
//	public static void removeExternalNode(Node parentNode, Node node) {
//		if (node != null)
//		{
//			String href = UMLXMLManip.getAttributeValue(node, "href");
//			if (href != null && href.length() > 0)
//			{
//				String fileName = href;
//				int pos = href.indexOf("#");
//				if (pos >= 0)
//				{
//					fileName = href.substring(0, pos);
//				}
//				
//				// Now delete the external file
//				if (deleteFile(fileName))
//				{
//					parentNode.removeChild(node);
//				}
//			}
//		}
//	}

	public static void removeExternalNode(Node parentNode, Node node) 
	{
		if (node != null)
		{
			String href = XMLManip.getAttributeValue(node, "href");
			if (href != null && href.length() > 0)
			{
				String fileName = href;
				int pos = href.indexOf("#");
				if (pos >= 0)
				{
					fileName = href.substring(0, pos);
				}
				
				// Now delete the external file
				if (deleteFile(fileName))
				{
					node.detach();
					//parentNode.removeChild(node);
				}
			}
		}
	}

	/**
	 * Tries to delete the file specified by the name
	 * @param fileName
	 * @return
	 */
	private static boolean deleteFile(String fileName) {
		boolean deleted = false;
		try {
			File file = new File(fileName);
			deleted = file.delete();
		}catch(Exception e)
		{
		}
		return deleted;
	}

	private static boolean isPathRelative(String fileName) {
		boolean relative = false;
		try {
			File file = new File(fileName);
			relative = (!file.isAbsolute());
		}catch(Exception e)
		{
		}
		return relative;
	}

	/**
	 *
	 * Retrieves the root location.
	 *
	 * @return The location
	 * @see RootFileName()
	 *
	 */
	public static String getRootFileName()
	{
		return m_rootFileName;
	}
	
	/**
	 *
	 * Sets the location of the file that will be the root where all external documents are relative to.
	 *
	 * @param fileName[in] The new value
	 *
	 * @return HRESULT
	 *
	 */
	public static void setRootFileName(String val)
	{
		m_rootFileName = val;
	}

	/**
	 *
	 * Establishes the root file name from the element passed in.
	 *
	 * @param ver[in] The element. The element's Project will be retrieved, using
	 *                that Project's file name as the root.
	 *
	 * @return HRESULT
	 *
	 */
	public void setRootFileName(IVersionableElement ver)
	{
		if (ver != null)
		{
			if (ver instanceof IElement)
			{
				IElement element = (IElement)ver;
				IProject proj = element.getProject();
				if (proj != null)
				{
					m_rootFileName = proj.getFileName();
				}
			}
		}
	}

	/**
	 *
	 * We now need to take the updated data found in node and jam it back out into
	 * the external file from which it orininally came, and then remove all the 
	 *	children of the element( node ).
	 *
	 * @param node[in] the element from the Project tree that we are pushing out
	 *					to its original file.
	 *
	 * @return HRESULTs
	 *
	 */
	public void pushExternalNode(Node node)
	{
		if (node instanceof org.dom4j.Element)
		{
			org.dom4j.Element nodeElement = (org.dom4j.Element)node;
			String loaded = XMLManip.getAttributeValue(nodeElement, "loadedVersion");

			// Only need to replicate the node back out to the external file
			// if the loadedVersion attribute is "true", which indicates that
			// we have a noded that has been loaded from an external source
			// that is associated with version control. 
			if (loaded != null && loaded.equals("true"))
			{
				// Find out if this is an imported element. If it is, we don't want
				// to push the element out. We should never push an imported element
				// out to disk. Why? 'Cause in the case that the import is from one 
				// project to another, the original project handles the actual unloading 
				// of the modified data ( actually doing the save ). It would be potentially
				// bad if the original project saved, then the project that was importing
				// also pushed the imported data out. Better to be controlled by one
				// manager.
				if (!isImportedElement(nodeElement))
				{
					// Now, let's see if the "isDirty" attribute exists. If it does and
					// it is currently set to "false", we don't need to do anything except
					// remove the loaded element from the current DOM. If the attribute does
					// not exist, then run through the push code below anyway.
					String dirty = XMLManip.getAttributeValue(nodeElement, "isDirty");
					if (dirty == null || dirty.equals("true") || dirty.length() == 0)
					{
						pushExternalElement(nodeElement);
					}
					else
					{
						// Even though nodeElement may not be dirty, it may contain an externally
						// loaded element that IS dirty. Let's check...
						String docLoc = retrieveDocLocation(nodeElement);
						handleElementVersioning(nodeElement, docLoc, m_FragmentXML);
					}
				}
			}
			
			// OK, the node has been replace. Now let's remove
			// all the data out of the Project DOM tree. That
			// data is now in the external file, which can now
			// be versioned.
			XMLManip.removeAllChildNodes(nodeElement);
			
			// Remove the "loadedVersion" attribute. This means
			// the next time a caller requests this node, it will
			// need to be retrieved from the external file that
			// it href attribute points to.
			org.dom4j.Attribute attr = nodeElement.attribute("loadedVersion");
			if (attr != null)
			{
				nodeElement.remove(attr);
			}
		}
	}
	
	/**
	 *
	 * Saves the passed in element out to a file it references internally via the
	 * href xml attribute.
	 *
	 * @param nodeElement[in] The passed in element. This element is part of the
	 *                        current DOM tree. The contents of this element are
	 *                        going to be extracted to an external file indicated
	 *                        by the href attribute. If it does not have an href
	 *                        attribute, nothing happens.
	 *
	 * @return HRESULT
	 */
	private void pushExternalElement(org.dom4j.Element nodeElement)
	{
		// Let's find out exactly where this node originated by
		// looking into the href attribute
		String actualHref = retrieveDocLocation(nodeElement);
		if (actualHref.length() > 0)
		{
			// Now we go and retrieve the original node
			// from the external file. We are doing this in
			// order to replace that node with the node that
			// was passed into this call
			Document oldDoc = URILocator.retrieveDocument(actualHref);
			Node oldNode = URILocator.retrieveNode(actualHref, oldDoc);
			
			if (oldNode != null)
			{
				// Get the parent node so that replaceChild()
				// doesn't fail
				Node oldParent = oldNode.getParent();
				if (oldParent != null)
				{
					// We need to make sure we clone the element
					// we are extracting, otherwise it becomes
					// a member of the external file. We don't
					// want that as we want to top of the element,
					// just not the children nodes. That way
					// we can retrieve this node later

					// Be sure to set the "isDirty" xml attribute to "false"
					XMLManip.setAttributeValue(nodeElement, "isDirty", "false");

					// Remove the "loadedVersion" attribute. This means
					// the next time a caller requests this node, it will
					// need to be retrieved from the external file that
					// it href attribute points to.
					org.dom4j.Attribute attr = nodeElement.attribute("loadedVersion");
					if (attr != null)
					{
						nodeElement.remove(attr);
					}
					
					Node clone = (Node)nodeElement.clone();
					if (clone != null)
					{
//						String docLoc = retrieveExtractName(URILocator.docLocation());
                  String docLoc = URILocator.docLocation();
						if (docLoc != null && docLoc.length() > 0)
						{
							if (clone instanceof org.dom4j.Element)
							{
								org.dom4j.Element verElement = (org.dom4j.Element)clone;

								// Be sure to tell the element we are about to push to handle
								// any elements it may need to push that are internal to it.
								handleElementVersioning(verElement, docLoc, m_FragmentXML);
								
								oldNode.detach();
								((Element)oldParent).add(verElement);
								//verElement.setParent((org.dom4j.Element)oldParent);
								
								// Now save the old, i.e., external document, out
								saveExternalDoc(oldDoc, docLoc, verElement);
							}
						}
					}
				}
			}
		}
	}

	/**
	 *
	 * Retrieves an absolute path name for the passed in absolute path. If this manager
	 * is in COMMIT mode, the file returned will be the file passed in. If in PRECOMMIT
	 * mode, the file returned will be a temporary file.
	 *
	 * @param fileName[in] An absolute file spec. This includes directory and filename.
	 *
	 * @return The filename.
	 *
	 */
	private String retrieveExtractName(String fileName)
	{
		String newFile = fileName;
		//if (m_Mode == EF_PRECOMMIT)
		{
			try
			{
				File origFile = new File(fileName);
				File dir = origFile.getParentFile();
				File file = File.createTempFile("$Temp", "tmp", dir);
				newFile = file.getAbsolutePath();
				m_Temps.put(fileName, newFile);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return newFile;
	}

	/**
	 *
	 * Determines whether or not the passed in node element is imported
	 * into this DOM or not.
	 *
	 * @param node[in] The node to check
	 *
	 * @return HRESULT
	 *
	 */
	public boolean isImportedElement(Node node)
	{
		boolean isImported = false;
		
		// Retrieve this nodes immediate parent in the DOM. If it
		// is a UML:PackageImport or a UML:ElementImport node, then
		// this node is imported
		if (node != null)
		{
			Node grandParent = null;
			try {
				grandParent = node.selectSingleNode("ancestor::*[2]");
			}
			catch(Exception e)
         {
            if(node != null)
            {
               Node parent = node.getParent();
               if(parent != null)
               {
                  grandParent = parent.getParent();
               }
            }
			}
			
			if (grandParent != null && grandParent.getNodeType() == Node.ELEMENT_NODE)
			{
				String grandParentNodeName = ((Element)grandParent).getQualifiedName();
				if (grandParentNodeName.equals("UML:PackageImport") ||
					grandParentNodeName.equals("UML:ElementImport"))
				{
					// Make one last check to see if the PackageImport was autoCreated. If it
					// is, we want to replace the contents of it with what is in memory
					Node autoCreate = grandParent.selectSingleNode("UML:Element.ownedElement/UML:TaggedValue[@name='autoCreated']");
					if (autoCreate == null)
					{
						isImported = true;
					}
				}
			}
		}
		
		return isImported;
	}

	public boolean isImportedElement( IVersionableElement ver )
	{
	   boolean isImported = false;

	   if( ver != null)
	   {
		  Node node = ver.getNode();

		  isImported = isImportedElement( node );
	   }

	   return isImported;
	}

	/**
	 *
	 * Handles all the necessary file handling associated with nodes that
	 * have already been versioned or need to be versioned.
	 *
	 * @param element[in] parent element whose children need to be handled
	 * @param fileName[in] name of the file where element resides
	 *
	 */
	public void handleElementVersioning(org.dom4j.Element element, String fileName, String fragmentXML)
	{
		m_FragmentXML = fragmentXML;
		// Find all elements that have the "isVersioned" attribute set to "true".
		// These are elements that were previously versioned ( i.e., put into external
		// files ) that have been brought into this project. The contents of those
		// nodes now need to get placed back out into those files.
		pushExternalElements(element);
		
		// Now handle all of the elements that need to be extracted for the
		// first time.
		handleElementsToBeVersioned(element, fileName, fragmentXML);
	}

	/**
	 *
	 * Finds all child nodes of parentElement that have the "versionMe" XML
	 * attribute set to true and calls ExtractElementToFile() on them. This results
	 * in the contents of those nodes to be pulled out of this project and
	 * placed into an external file that will then be reference by
	 * the now skeleton node in the project.
	 *
	 * @param parentElement[in] the element whose children will be extracted.
	 * @param fileName[in] name of the file parentElement resides in.
	 *
	 */
	private void handleElementsToBeVersioned(org.dom4j.Element parentElement, 
											String fileName, String fragmentXML)
	{
		List nodes = parentElement.selectNodes(".//*[@versionMe=\"true\"]");
		if (nodes != null)
		{
			int count = nodes.size();
			for (int i=0; i<count; i++)
			{
				Node n = (Node)nodes.get(i);
				if (n instanceof org.dom4j.Element)
				{
					org.dom4j.Element element = (org.dom4j.Element)n;
					String newFile = extractMarkedElements(element, fileName, fragmentXML);
					
					// Now remove the current element's children
					XMLManip.removeAllChildNodes(element);
				}
			}
		}
	}
   
   protected Node findAcestorWithAttribute(Node curNode, String attrName)
   {
      Node retVal = null;
      
      if(curNode != null)
      {
         Node parent = curNode.getParent();
         if(parent instanceof Element)
         {
            Element parentElement = (Element)parent;
            Attribute attr = parentElement.attribute(attrName);
            if(attr == null)
            {
               retVal = findAcestorWithAttribute(parent, attrName);               
            }
            else
            {
               retVal = parent;
            }
         }
      }
      
      return retVal;
   }
   
	/**
	 *
	 * Handles the manipulation of elements that contain the "loadedVersion"
	 * attribute. These are elements that were previously extracted ( versioned )
	 * into external files and were subsequently brought in during this
	 * run of the application due to a caller requesting data in that external
	 * element. We now need to take the updated data, jam it back out into
	 * the external file, and remove all the children of the element.
	 *
	 * @param element[in] an element that is or contains elements that have been
	 *				  previously versioned.
	 *
	 * @return HRESULTs
	 *
	 */
	public void pushExternalElements( org.dom4j.Element element )
	{
		List nodes = getImmediateExternalElements(element);
		if (nodes != null)
		{
			int count = nodes.size();
			for (int i=0; i<count; i++)
			{
				Node n = (Node)nodes.get(i);
				pushExternalNode(n);
			}
		}
	}
	
	/**
	 *
	 * Retrieves the loadedElements for the passed in element. This includes any package or element
	 * imports, plus any immediate owned elements that are external.
	 *
	 * @param element[in] The element to search against
	 * @param nodes[out] The collection of found external elements. This will be null if no package or
	 *                   element imports were found, or any loaded external owned elements were found
	 *
	 * @return HRESULT
	 *
	 */
	private List getImmediateExternalElements(org.dom4j.Element element)
	{
		List retList = null;
		List loadedNodes = element.selectNodes(".//*[@loadedVersion]");
		
		// Now make sure that the nodes are part of this element. That is, we don't want to grab
		// elements that need to be handled the a versioned element that also happens to be loaded,
		// 'cause then we could be processing loaded elements twice.
		if (loadedNodes != null)
		{
			int count = loadedNodes.size();
			if (count > 0)
			{
				String elementId = XMLManip.getAttributeValue(element, "xmi.id");
				retList = new Vector();
				for (int i=0; i<count; i++)
				{
					Node node = (Node)loadedNodes.get(i);
					//Node parent = node.selectSingleNode("ancestor::*[@href][1]");
               Node parent = findAcestorWithAttribute(node, "href");
					
					if (parent == null)
					{
						//parent = node.selectSingleNode("ancestor::*[@isVersioned][1]");
                  parent = findAcestorWithAttribute(node, "isVersioned");
					}
					
					if (parent != null)
					{
						String parentId = XMLManip.getAttributeValue(parent, "xmi.id");
						if (parentId.length() > 0 && elementId.length() > 0)
						{
							if (parentId.equals(elementId))
							{
								retList.add(node);
							}
						}
					}
					else
					{
						// If it doesn't have any parent with the href or isVersioned atts, those
						// can be processed.
						retList.add(node);
					}
				}
			}
		}
		
		if (true)
		{
			List packageImports = element.selectNodes("./UML:Package.packageImport/UML:PackageImport/UML:PackageImport.importedPackage/*[@loadedVersion]");
			List elementImports = element.selectNodes("./UML:Package.elementImport/UML:ElementImport/UML:ElementImport.importedElement/*[@loadedVersion]");
			
			// We need to find the first child that is loaded. Once we do that, we only want that child's siblings. If
			// we blindly retrieved all the children, nodes would be processed twice. Because we could potentially be
			// dealing with numerous documents, we have to handle this carefully, i.e., we can't just mark the node as
			// handled already.
			Node loadedElement = element.selectSingleNode("./UML:Element.ownedElement//*[@loadedVersion][1]");
			
			int numPackImports = 0;
			int numEleImports = 0;
			if (packageImports != null)
			{
				numPackImports = packageImports.size();
			}
			if (elementImports != null)
			{
				numEleImports = elementImports.size();
			}
			
			if (numPackImports > 0 || numEleImports > 0 || loadedElement != null)
			{
				if (retList == null)
				{
					retList = new Vector();
				}
				
				if (numPackImports > 0)
				{
					populateFromList(retList, packageImports);
				}
				if (numEleImports > 0)
				{
					populateFromList(retList, elementImports);
				}
				if (loadedElement != null)
				{
					retList.add(loadedElement);
					
					// Now retrieve all the sibling nodes of loadedElement
					List siblings = loadedElement.selectNodes("following-sibling::*[@loadedVersion]");
					populateFromList(retList, siblings);
				}
			}
		}
		return retList;
	}

	/**
	 *
	 * Populates the passed in Nodes collection with the contents of the node list passed in.
	 *
	 * @param nodes[in] The XMLDOMNodes collection to populate
	 * @param nodeList[in] The IXMLDOMNodeList collection to populate from
	 *
	 * @return HRESULT
	 *
	 */
	private void populateFromList(List nodes, List nodeList)
	{
		if (nodeList != null)
		{
			int count = nodeList.size();
			for (int i=0; i<count; i++)
			{
				Node node = (Node)nodeList.get(i);
				nodes.add(node);
			}
		}
	}

	/**
	 * Creates a unique file name that will house the contents of element. Creates
	 * the file in the directory of the parent xml file.
	 *
	 * @param element[in] the element whose contents will be placed in the new file
	 * @param parentFileName[in] the name of the file that houses the parent xml
	 *								file. This method is assuming that this is a full
	 *								path to the file, and that it is not a directory
	 *								path, i.e., that name of the actual file ( "cameron.xml" )
	 *								is part of the string.
	 *	@param frag[in]        the xml fragment that defines the structure of an empty 
	 *							   version XML file.
	 * @return Name of the new file that was created.
	 *
	 */
	public String extractMarkedElements( org.dom4j.Element element, 
	         									 String parentFileName, 
										 String	frag )
	{
		String newFile = null;
		String parentStr = parentFileName;
		String dir = parentStr.substring(0, parentStr.lastIndexOf(File.separator));
      
		String physicalFileName=null;
      String xmiID = XMLManip.getAttributeValue(element, "xmi.id");
      if(xmiID.length() > 0)
      {
         String strID = xmiID;
         String prefix = "Ver" + strID.substring(4, 9);
         newFile = retrieveExtractName(dir, prefix, physicalFileName);
      }
      else
      {
         newFile = retrieveExtractName(dir, "Ver", physicalFileName);
      }
		
		// Now create the XML document that will house element
		boolean proceed = true;
		TypedFactoryRetriever ret = new TypedFactoryRetriever();
		IVersionableElement ver = (IVersionableElement)ret.createTypeAndFill(element);
		EventDispatchRetriever dispRet = EventDispatchRetriever.instance();
		IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher) dispRet.getDispatcher(EventDispatchNameKeeper.modifiedName());
		proceed = firePreExtractEvent(newFile, element, disp, ver);
		
		if (proceed)
		{
			Document doc = XMLManip.loadXML(frag);
			if (doc != null)
			{
				setProjectId(doc);

				// It is possible that the element coming in has already been orphaned either
				// during the FirePreExtractEvent or before. Re-get the xml node to be sure
				// we have the right data
				Node modNode = ver.getNode();
				if (modNode instanceof org.dom4j.Element)
				{
					org.dom4j.Element modElement = (org.dom4j.Element)modNode;

					// Add the href attribute and the isVersioned attributes.
					// Remove the "versionMe" attribute
					establishVersionedElement(newFile, modElement);
					
					// Must call FireExtractEvent() here. The TypeManager
					// resolves internal types, etc.
					fireExtractEvent(disp, ver);
					
					// The node behind ver could have changed during the FireExtractEvent if
					// we determined that the node was orphaned due to version control unloading
					// parent nodes. Re-get the xml node to be sure
					modNode = null;
					modNode = ver.getNode();
					
					if (modElement instanceof org.dom4j.Element)
					{
						modElement = (org.dom4j.Element)modNode;

						// Make sure that the "loadedVersion" attribute is not present when
						// saving out this element
						org.dom4j.Attribute attr = modElement.attribute("loadedVersion");
						modElement.remove(attr);

						// Clone element
						Node cloned = (Node)modElement.clone();
						
						saveExtractedElement(doc, cloned, newFile);
						
						// Now mark the original element as loaded, so that it is properly removed
						// upon save
						XMLManip.setAttributeValue(modElement, "loadedVersion", "true");
					}
				}
			}
		}
		
		return newFile;
	}

	/**
	 *
	 * Sets the xmi id of the owning project on the VersionedElement element
	 *
	 * @param doc[in] The document that contains the new VersionedElement
	 *
	 * @return HRESULT
	 *
	 */
	private void setProjectId(Document doc)
	{
		Node verNode = doc.selectSingleNode("VersionedElement");
		if (verNode != null)
		{
			XMLManip.setAttributeValue(verNode, "projectID", getProjectXMIID());
		}
	}

	/**
	 *
	 * Fires the event indicating that the passed in element has successfully been extracted.
	 *
	 * @param disp[in] The dispatcher
	 * @param ver[in] The element that was just extracted.
	 *
	 * @return HRESULT
	 *
	 */
	private void fireExtractEvent(IElementChangeEventDispatcher disp, 
									IVersionableElement ver)
	{
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("InitialExtraction");
			disp.fireInitialExtraction(ver, payload);
		}
	}

	/**
	 *
	 * Fires the event that indicates that the passed in element is about to be
	 * extracted to an external file.
	 *
	 * @param fileName[in] The file that the element will be housed in.
	 * @param node[in] The node being extracted.
	 * @param disp[out] The returned dispatcher
	 * @param proceed[out] true to continue with the event, else false
	 * @param ver[out] The COM object wrapping node.
	 *
	 * @return HRESULT
	 *
	 */
	private boolean firePreExtractEvent(String fileName,
										Node node, 
										IElementChangeEventDispatcher disp,
										IVersionableElement ver)
	{
		boolean proceed = true;
		if (ver != null)
		{
			if (disp != null)
			{
				IEventPayload payload = disp.createPayload("PreInitialExtraction");
				proceed = disp.firePreInitialExtraction(fileName, ver, payload);
				
				// Now make sure the node behind *ver is still ok
				if (ver instanceof IElement)
				{
					IElement verElement = (IElement)ver;
					IProject proj = verElement.getProject();
					if (proj != null)
					{
						ITypeManager man = proj.getTypeManager();
						if (man != null)
						{
							boolean modified = man.verifyInMemoryStatus(verElement);
						}
					}
				}
			}
		}
		return proceed;
	}

	/**
	 * Retrieves an absolute file name for an element to be extracted from the main document.
	 *
	 * @param dir[in] The directory the new file will reside
	 * @param prefix[in] The prefix to use for the new file
	 * @param physicalName[out] The actual file name. If the the mode of this
	 *                          manager is EF_COMMIT, then this will be the same
	 *                          value as the return value. However, if the mode
	 *                          is EF_PRECOMMIT, this will be a temporary filename,
	 *                          whereas the filename returned from this method will
	 *                          be the name of the file that should be used
	 *                          once a full commit has been performed.
	 *
	 * @return The file new file name.
    * @return 
	 *
	 */
	private String  retrieveExtractName(String dir, 
						                     String prefix, 
                                       String physicalName)
	{
		//ETPairT < String, String > retVal = new ETPairT < String, String >();
      
      String retVal = "";
      try
      {
         File tempFile = File.createTempFile(prefix, ".etx", new File(dir));
         if(m_Mode == EF_COMMIT)
         {

         }
         
         retVal = tempFile.getAbsolutePath();
      }
      catch(IOException e)
      {
         UMLMessagingHelper helper = new UMLMessagingHelper();
         helper.sendExceptionMessage(e);
      }
		return retVal;
	}

	/**
	 *
	 * Saves the extracted node to the indicated document.
	 *
	 * @param doc[in] the DOM document that will hold the new element
	 *	@param node[in] the extracted node element
	 *	@param parentFileName[in] name of the file where the node originally belonged
	 *	@param newFile[in] the name of the file that the node is being saved
	 *								to.
	 *
	 * @return HRESULTs
	 *
	 */
	private void saveExtractedElement(Document doc, Node node, String newFile)
	{
		// Retrieve the "VersionedElement" node
		Node verEl = doc.selectSingleNode("//VersionedElement");
		if (verEl != null)
		{
			// Add the cloned element to the new document and save
			if (verEl instanceof org.dom4j.Element)
			{
				org.dom4j.Element ele = (org.dom4j.Element)verEl;
				ele.add(node);
				
				if (node instanceof org.dom4j.Element)
				{
					org.dom4j.Element nodeEle = (org.dom4j.Element)node;
					nodeEle.setParent(ele);

					// Be sure to convert any absolute href values
					saveExternalDoc(doc, newFile, nodeEle);
				}
			}
		}
	}

	/**
	 *
	 * Saves the document passed in, converting absolute hrefs to relative hrefs, relative
	 * to m_RootFileName.
	 *
	 * @param doc[in]       The document to save
	 * @param docLoc[in]    The location the document will be saved
	 * @param element[in]   The node whose hrefs will be made relative
	 *
	 * @return HRESULT
	 *
	 */
	private void saveExternalDoc(Document doc, String docLoc,
                org.dom4j.Element element)
	{
		UMLXMLManip.convertAbsoluteHrefs(element, m_rootFileName);
		XMLManip.save(doc, docLoc);
		
	}

	/**
	 *
	 * Sets the href attribute and the isVersioned attribute, and removes
	 * the versionMe attribute.
	 *
	 * @param newFile[in] name of the file where the element is going
	 *	@param element[in] the element we are modifiying. This is the element that
	 *                    will remain in the parent document. It is the element that
	 *                    is being cloned in order to create a VersionElement out
	 *                    in an external document.
	 *
	 * @return HRESULTs
	 *
	 */
	private void establishVersionedElement(String newFile, Element element)
	{
		// Retrieve the xmi.id to use in the href value
		String id = XMLManip.getAttributeValue(element, "xmi.id");
		String newHref = newFile + "#//*[@xmi.id=\"" + id + "\"]";
		XMLManip.setAttributeValue(element, "href", newHref);
		XMLManip.setAttributeValue(element, "isDirty", "true");
		XMLManip.setAttributeValue(element, "isVersioned", "true");
		
		org.dom4j.Attribute attr = element.attribute("versionMe");
		element.remove(attr);
	}

	/**
	 *
	 * Retrieves all nodes that have the "loadedVersion" xml attribute
	 *
	 * @param searchNode[in] The node to search from.
	 * @param nodes[in] The retrieved nodes.
	 *
	 * @return HRESULT
	 *
	 */
	public List getExternalElements(Node searchNode)
	{
		return searchNode.selectNodes(".//*[@loadedVersion]");
	}

	/**
	 *
	 * If this manager contains any TempFileSpec objects, those specs will be used to 
	 * move from the temporary file name to the final. Also, this method will switch the
	 * internal state of this manager to EF_COMMIT.
	 *
	 * @return true if the commit succeeded else, false
	 *
	 */
	public boolean commit()
	{
		boolean success = true;
		m_Mode = EF_COMMIT;
		if (m_Temps != null)
		{
			Enumeration iter = m_Temps.elements();
			Enumeration iter2 = m_Temps.keys();
			while (iter.hasMoreElements())
			{
				String existingFileName = (String)iter.nextElement();
				String newFileName = (String)iter2.nextElement();
				
				File existingFile = new File(existingFileName);
				File newFile = new File(newFileName);
				
				newFile.delete();
				success = existingFile.renameTo(newFile);
				if (!success)
				{
					break;
				}
			}
		}
		return success;
	}
}



