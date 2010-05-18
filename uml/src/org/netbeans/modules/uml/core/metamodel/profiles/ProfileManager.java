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


package org.netbeans.modules.uml.core.metamodel.profiles;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.StructureConstants;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.XMLFragmentLoader;
import org.netbeans.modules.uml.core.support.umlsupport.PathManip;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author sumitabhk
 *
 */
public class ProfileManager 
{

	/**
	 * 
	 */
	public ProfileManager() 
	{
		super();	
	}

	/**
	 *
	 * Creates a new Stereotype with the given name. Dependent on preference settings, that stereotype
	 * will be placed in a Profile that is then imported by the Project that owns context.
	 *
	 * @param context[in]         Used to retrieve the IProject that will have the potentially new Profile injected 
	 *                            into it
	 * @param stereotypeName[in]  The name of the new stereotype
	 * @param newStereotype[out]  The newly created Stereotype
	 *
	 * @return HRESULT
	 *
	 */
	public Object establishNewStereotype(IElement context, String name) 
	{
		IStereotype newStereo = null;
		IProject proj = context.getProject();
		if (proj != null)
		{
			String href = null;			
			//Here proceed is passed by ref. so any modification inside the method,
			//would reflect here as well.
			ETTripleT<String,IProfile,Boolean> retValue = retrieveAutoCreatedProfile(proj);
			href = retValue.getParamOne();
			IProfile prof = retValue.getParamTwo();
			Boolean proceed = retValue.getParamThree();			
			if (proceed != null && proceed.booleanValue() && prof != null)			
			{
				IPackageImport packImp = proj.importPackage(prof, href, true);
				if (packImp != null)
				{
					// Now mark the PackageImport with a tagged value for
					// easy retrieval later 
					packImp.addTaggedValue("autoCreated", "true");

					prof = null;
					// Need to retrieve the package import in this case, in order to make sure that
					// the Profile is reimported, ensuring that the element in memory is loaded properly
					IPackage pack = packImp.getImportedPackage();
					if (pack instanceof IProfile)
					{
						prof = (IProfile)pack;
					}
				}
			}
			
			if (prof != null)
			{
				// Now, create the new Stereotype
				TypedFactoryRetriever < IStereotype > ret = new TypedFactoryRetriever < IStereotype >();
				newStereo = ret.createType("Stereotype");
				if (newStereo != null)
				{
					newStereo.setName(name);
					prof.addOwnedElement(newStereo);
				}
			}
		}
		return newStereo;
	}

	/**
	 *
	 * Retrieves the imported Profile from the Project, else creates one if it is not there.a
	 *
	 * @param proj[in]   The IProject we are querying against
	 * @return ETTripleT returns an object of href, profile and the success of this method
	 */
	protected ETTripleT<String,IProfile,Boolean> retrieveAutoCreatedProfile(IProject proj) 
	{		
		boolean wasCreated = false;
		String href = null;
		ETTripleT<String, IProfile, Boolean> retTriple = new ETTripleT<String, IProfile, Boolean>();
		if (proj != null)
		{
			IProfile prof = null;
			String query = "./UML:Package.packageImport/UML:PackageImport/UML:Element.ownedElement/UML:TaggedValue[@name=\"autoCreated\"]/ancestor::*[2]";
			Node node = proj.getNode();
			if (node != null)
			{
				Node importNode = node.selectSingleNode(query);
				if (importNode != null)
				{
					String nodeName = importNode.getName();
					TypedFactoryRetriever<IPackageImport> ret = new TypedFactoryRetriever<IPackageImport>();
					IPackageImport packImport = ret.createTypeAndFill(importNode);
					if (packImport != null)
					{
						IPackage pack = packImport.getImportedPackage();						
						if (pack instanceof IProfile)
						{
							prof = (IProfile)pack;
						}
						if (prof != null)
						{
							Node profNode = prof.getNode();
							if (profNode != null)
							{
								href = XMLManip.getAttributeValue(profNode, "href");
							}
						}
					}
					retTriple.setParamOne(href);
					retTriple.setParamTwo(prof);					
				}
				else
				{
					//Create the Profile, and add to Project as a PackageImport
					ETPairT<String, IProfile> retPair = createStandAloneProfile(proj);
					retTriple.setParamOne( retPair.getParamOne() );
					retTriple.setParamTwo( retPair.getParamTwo() );
					
					wasCreated = true;
				}
			}
		}
		retTriple.setParamThree(Boolean.valueOf(wasCreated));
		return retTriple;
	}
	
	/**
	 *
	 * Creates a new Profile that exists in its own file. Where that file is
	 * placed is preference driven.
	 *
	 * @param proj[in]   The IProject we are querying against. Used to establish a location
	 */          
	protected ETPairT<String, IProfile> createStandAloneProfile(IProject proj) {
            ETPairT<String, IProfile> retPair = new ETPairT<String, IProfile>();
            IProfile prof = null;
            String href = "";
            try {
                if (proj != null) {
                    ICoreProduct prod = ProductRetriever.retrieveProduct();
                    if (prod != null) {
                        IPreferenceManager2 prefMan = prod.getPreferenceManager();
                        if (prefMan != null) {
                            //Need to determine where to create the new Profile
                            //kris richards - UnknownStereotypeCreate pref deleted. Set to PSK_IN_PROJECT_PROFILE
                            String prefValue = "PSK_IN_PROJECT_PROFILE";
                            String location = determineProfileLocation( proj, prod, prefValue );
                            if (location != null && location.length() > 0) {
                                XMLFragmentLoader loader = new XMLFragmentLoader();
                                String xmlFrag = loader.retrieveFragment(
                                        ProfileManager.class,
                                        StructureConstants.IDR_XMI_HEADER_WITHOUT_DTD);
                                
                                if (xmlFrag != null && xmlFrag.length() > 0) {
                                    ETPairT<Document, IProfile> sapPair = createStandAloneProfile(xmlFrag);
                                    Document doc = sapPair.getParamOne();
                                    prof = sapPair.getParamTwo();
                                    if (doc != null && prof != null) {
                                        String projName = proj.getName();
                                        if (projName != null && projName.length() > 0) {
                                            String projLoc = proj.getFileName();
                                            location += projName + ".etup";
                                            
                                            XMLManip.save(doc, location);
                                            
                                            href = PathManip.retrieveRelativePath(location, projLoc);
                                            String xmiID = prof.getXMIID();
                                            
                                            href += "#//*[@xmi.id='";
                                            href += xmiID;
                                            href += "']";
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch(Exception e) {
                //not doing anything for now.
            }
            
            retPair.setParamOne(href);
            retPair.setParamTwo(prof);
            return retPair;
        }

	/**
	 *
	 * Determines the location of a new Profile given the preference value passed in.
	 *
	 * @param proj[in]   Could be used to anchor the location with the Project
	 * @param prod[in]   Could be used to determine the default configuration location
	 * @param value[in]  The preference value. Should be "PSK_IN_PROJECT_PROFILE" or 
	 *                   "PSK_IN_CENTRAL_PROFILE", otherwise, no location will be retrieved
	 */
	protected String determineProfileLocation(IProject proj, ICoreProduct prod, String value)
	{
		String location = null;
		if (value != null)
		{
			if (proj != null && value.equals("PSK_IN_PROJECT_PROFILE"))
			{
				String fileName = proj.getFileName();
				if (fileName != null && fileName.length() > 0)
				{
					location = StringUtilities.getPath(fileName);
				}
			}
			else if (prod != null && value.equals("PSK_IN_CENTRAL_PROFILE"))
			{
				IConfigManager config = prod.getConfigManager();
				if (config != null)
				{
					location = config.getDefaultConfigLocation();
				}
			}
		}
		return location;
	}
	
	/**
	 *
	 * Creates a Profile object and places it in the XML document that will be created
	 * from the fragment passed in.
	 *
	 * @param frag[in]   The XML fragment that will house the Profile
	 */
	protected ETPairT<Document, IProfile> createStandAloneProfile(String frag)
	{
		ETPairT<Document, IProfile> retPair = new ETPairT<Document, IProfile>();		
		IProfile prof = null;
		Document doc = null;
		
		try
		{
			doc = XMLManip.loadXML(frag);
			if (doc != null)
			{
				TypedFactoryRetriever<IProfile> retr = new TypedFactoryRetriever<IProfile>();
				prof = retr.createType("Profile");
				if (prof != null)
				{
					Node node = prof.getNode();
					String query = "XMI/XMI.content";
					Node contentNode = doc.selectSingleNode(query);
					Element element = (Element)contentNode;
					if (element != null)
					{
						node.detach();
						element.add(node);
						//UMLXMLManip.appendChild(element,node);
					}
				}
			}
			else
			{
				//throw Exception.
			}
		}
		catch(Exception e)
		{
			//Not doing anything for now
		}
		retPair.setParamOne(doc);
		retPair.setParamTwo(prof);
		
		return retPair;
	} 
}


