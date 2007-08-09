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

package org.netbeans.modules.uml.core.support.umlutils;

import org.netbeans.modules.uml.core.metamodel.core.constructs.PartFacade;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Node;
import java.util.List;
import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IUMLBinding;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PropertyDefinitionFactory implements IPropertyDefinitionFactory{

  private String m_definitionFile = null;
  private Document m_Doc = null;
  private Object m_ModelElement = null;
  private Hashtable<String, IPropertyDefinition> m_DefinitionMap = new Hashtable<String, IPropertyDefinition>();
  private Hashtable<String, Node> m_NodeMap = new Hashtable<String, Node>();
  private Hashtable<String, Node> m_NamedNodeMap = new Hashtable<String, Node>();

  //typedef std::map < UINT, CComPtr< IFunction > > IDMap;
  private Hashtable m_IDMap = new Hashtable();

  //typedef std::map < CComBSTR, IDMap > FunctionMap;
  private Hashtable m_FunctionMap = new Hashtable();


  public PropertyDefinitionFactory() {
  }

  /**
   * Retrieve or create the proper property definition for the passed-in element type.
   *
   * @param[in] eleStr The element type
   * @param[in] pDisp  The model element (used to build the function maps for the IDispatch for faster
   *                   lookup when doing get/set/insert/deletes.  Also used to get the help description)
   * @param[out] pVal  The property definition that has been created or found
   *
   * @return HRESULT
   *
   */
  public IPropertyDefinition getPropertyDefinitionForElement( String eleName, Object pDisp)
  {
    IPropertyDefinition propDef = null;
    if (eleName == null || eleName.length() == 0)
    {
		// have to special case part facade
		if (pDisp instanceof IPartFacade)
		{
			IPartFacade pPart = (IPartFacade)pDisp;

			// actually only need to special case a part facade representing a use case
			// unfortunately this information is on a parameterable element, not the part
			// facade
			if (pPart instanceof IParameterableElement)
			{
				IParameterableElement pParamEle = (IParameterableElement)pPart;
				eleName = "PartFacade";
				String type = pParamEle.getTypeConstraint();
				if (type != null && type.length() > 0)
				{
					eleName += type;
				}
			}
		}
		else if (pDisp instanceof IUMLBinding)
		{
			eleName = "UMLBinding";
		}

		// haven't found it yet, so now use element type
		if (eleName == null || eleName.length() == 0)
		{
			if (pDisp instanceof IElement)
			{
				IElement pUMLEle = (IElement)pDisp;
				eleName = pUMLEle.getElementType();
			}
		}
    }
    propDef = getFromDefinitionMap(eleName);

    if (propDef == null)
    {
      //buildFunctionMap(pDisp);
      
      //This member variable creates a leak.  For instance
      // Open a workspace and then close it right away.  View
      // the memory detector addin and you'll have 2 workspaces
      // still in memory (one is design center).  Then set a break in
      // FinalRelease of Workspace you'll see that this
      // being released does a final release of the IWorkspace.
      //factory 
      //m_ModelElement = pDisp;
      
      propDef = createDefinition(eleName);
      if (propDef != null)
      {
        addToDefinitionMap(eleName, propDef);
      }
    }
    setAppropriateDisplayName(propDef, pDisp);
    return propDef;
  }

    private void setAppropriateDisplayName(IPropertyDefinition propDef, Object pDisp) {
        String expandedType = null;
        if (pDisp instanceof IAssociation) {
             expandedType = ((IElement) pDisp).getExpandedElementType();
             if (expandedType != null && expandedType.trim().length() > 0)
             {
                 propDef.setDisplayName(expandedType.replace('_', ' '));
             }
        } 
        else if (pDisp instanceof PartFacade ) {
            expandedType = ((IElement) pDisp).getExpandedElementType();
            if (expandedType != null) {
                expandedType = expandedType.toLowerCase();
                if  (expandedType.equals("partfacade_class")) {
                    propDef.setDisplayName("PSK_PE_CLASSPARTICIPANT");
                } else if (expandedType.equals("partfacade_interface")) {
                    propDef.setDisplayName("PSK_PE_INTERFACEPARTICIPANT");
                } else if (expandedType.equals("partfacade_actor")) {
                    propDef.setDisplayName("PSK_PE_ACTORPARTICIPANT");
                } else if (expandedType.equals("partfacade_usecase")) {
                    propDef.setDisplayName("PSK_PE_USECASEPARTICIPANT");
                }
            }
        }
    }
  /**
   * Transfer the information from the xml dom node to the property definition.  This handles the special case
   * processing for when the get method returns an IDispatch.  The DispatchInvokes tell us what to do with the
   * IDispatch that was returned.
   *
   * @param[in] pEle   The xml dom node to obtain the information from
   * @param[in] pDef   The property definition to add the information to
   *
   * @return HRESULT
   *
   */
  private void processOtherInvokes(Node pNode, IPropertyDefinition pDef)
  {
    // Some of the get routines return an IDispatch which may or may not need more things done
    // to it in order to display it in the property editor
    // The section in the xml file looks like:
    // 			<DispatchInvoke name="{123-456}" get="Type"/>
    // and tells us if the dispatch can be cast to an object represented by the name,
    // then invoke the get="" method on the IDispatch
    String pattern = "DispatchInvoke";
    try {
    	if (pNode instanceof org.dom4j.Element)
    	{
    		org.dom4j.Element ele = (org.dom4j.Element)pNode;
			List list = ele.elements();
			if (list != null && list.size() > 0) {
			  for (int i = 0; i < list.size(); i++) {
				Node node = (Node)list.get(i);
				if (node.getName().equals(pattern)) {
				  Node nameNode = XMLManip.getAttribute(node, "name");
				  Node getNode = XMLManip.getAttribute(node, "get");
				  //Node nameNode = XPathAPI.selectSingleNode(node, "@name");
				  //Node getNode = XPathAPI.selectSingleNode(node, "@get");
				  String name = null;
				  String get = null;
				  if (nameNode != null)
					name = nameNode.getStringValue();
				  if (getNode != null)
					get = getNode.getStringValue();

				  pDef.addToAttrMap("castID", name);
				  pDef.addToAttrMap("castIDGet", get);
				}
			  }
			}
    	}
    } catch (Exception e)
    {}
  }

  /**
   * Retrieve the property definition for the passed in value
   *
   * @param[in] name    The element type of the definition to retrieve
   * @param[out] pVal   The retrieved property definition
   *
   * @return HRESULT
   *
   */
  private IPropertyDefinition getFromDefinitionMap(String eName)
  {
    IPropertyDefinition pDef = null;
    try {
      pDef = m_DefinitionMap.get(eName);
    } catch (Exception e)
    {}
    return pDef;
  }

  private Node getFromNodeMap(String eName)
  {
	Node pNode = null;
	try {
	  pNode = m_NodeMap.get(eName);
	} catch (Exception e)
	{}
	return pNode;
  }

  private Node getFromNamedNodeMap(String eName)
  {
	Node pNode = null;
	try {
	  pNode = m_NamedNodeMap.get(eName);
	} catch (Exception e)
	{}
	return pNode;
  }

  /**
   * Add the passed in property definition to the factory map
   *
   * @param[in] name   The element type
   * @param[in] pVal   The property definition to add to the map
   *
   * @return HRESULT
   *
   */
  private void addToDefinitionMap(String eName, IPropertyDefinition pDef)
  {
    if (eName.length() > 0)
    {
      m_DefinitionMap.put(eName, pDef);
    }
  }

  //this method is added because sub definitions references FontDefinition 
  //multiple times and each time the XPathAPI was used to get to the node,
  //now we are catching the node and using it.
  //Keeps the Node and corresponding id in the map.
  private void addToNodeMap(Node node, String eName)
  {
	if (eName != null && eName.length() > 0)
	{
	  m_NodeMap.put(eName, node);
	}
  }

  //Keeps the Node and corresponding name in the map.
  private void addToNamedNodeMap(Node node, String eName)
  {
	if (eName.length() > 0)
	{
	  m_NamedNodeMap.put(eName, node);
	}
  }

  /**
   * Process the sub definitions of the xml node.  These are predefined in the xml file.
   *
   * @param[in] parentNode   The xml node to process
   * @param[in] parentDef    The owning property definition to which any subs will be added
   *
   * @return HRESULT
   *
   */
  private void processSubDefinitions(Node pNode, IPropertyDefinition pDef)
  {
  	if (pNode.getNodeType() == Node.ELEMENT_NODE)
  	{
  		org.dom4j.Element elem = (org.dom4j.Element)pNode;
  		List list = elem.selectNodes("aDefinition");
  		if (list != null)
  		{
  			int count = list.size();
  			for (int i=0; i<count; i++)
  			{
				Node n = (Node)list.get(i);
				testmyProcessSubDefinitions(n, pDef);  				
  			}
  		}
  	}
  }
  private void testmyProcessSubDefinitions(Node item, IPropertyDefinition pDef)
  {

	Node refNode = XMLManip.getAttribute(item, "pdref");
	//Node refNode = XPathAPI.selectSingleNode(item, "@pdref");
	if (refNode != null)
	{
	  // if the sub definition references another definition, we need to build that one
	  String refVal = refNode.getStringValue();
	  if (refVal.length() > 0)
	  {
		// if the sub definition is an on demand definition, we actually don't build
		// it at this time
		Node onDemNode = XMLManip.getAttribute(item, "onDemand");
		//Node onDemNode = XPathAPI.selectSingleNode(item, "@onDemand");
		if (onDemNode != null)
		{
		  String onDemand = onDemNode.getStringValue();
		  if (onDemand.equals("true"))
		  {
			// build part of it because it is on demand
			buildOnDemandDefinition(item, pDef);
		  }
		  else
		  {
			// not on demand, so build it
			Node n = findPropertyDefinitionDOMNodeForSubDef(refVal);
			if (n == null)
			{
			  n = findPropertyDefinitionDOMNode(refVal);
			}
			if (n != null)
			  processSubDefinitions(n, pDef);
		  }
		}
		else
		{
		  // not on demand, so build it
		  Node n = findPropertyDefinitionDOMNodeForSubDef(refVal);
		  if (n == null)
		  {
		  	n = findPropertyDefinitionDOMNode(refVal);
		  }
		  if (n != null)
			processSubDefinitions(n, pDef);
		}
	  }
	  else
	  {
		// not referencing anything else, so build straight up
		buildDefinition(item, pDef);
	  }
	}
	else
	{
	  // Need to special case this - the edit control which is using the definitions and
	  // factory needs the structure to be like this:
	  // <Params
	  //   <Param <-- No on demand, but a pdref
	  //     <Name
	  // The property editor needs it to look like this:
	  // <Params
	  //   <Param <-- On demand, but a pdref
	  // so then when the user clicks on the on demand, the property editor builds the right
	  // stuff and replaces the definition
	  // If it is not on demand, but pdref'ed, the param node is not built because then in
	  // the property editor, we would get
	  // <Class
	  //    <Classifier
	  //		  <NamedElement
	  // which is why the edit control now has a pdref2 not a pdref
	  Node refNode2 = XMLManip.getAttribute(item, "pdref2");
	  //Node refNode2 = XPathAPI.selectSingleNode(item, "@pdref2");
	  if (refNode2 != null)
	  {
		String refVal2 = refNode2.getStringValue();
		Node n = findPropertyDefinitionDOMNodeForSubDef(refVal2);
		if (n != null)
		  buildDefinition(n, pDef);
	  }
	  else
	  {
		buildDefinition(item, pDef);
	  }
	}
  }  	
  

/*  private void processSubDefinitions(Node pNode, IPropertyDefinition pDef)
  {
    try {
      String pattern = "aDefinition";
      //Sumitabh check if this is XPath
      List list = XMLManip.selectNodeListNS(pNode, pattern);
	  //List list = XPathAPI.selectNodeList(pNode, pattern);
      if (list != null && list.getLength() > 0)
      {
        for (int i=0; i<list.getLength(); i++)
        {
          Node item = list.item(i);
          Node refNode = XMLManip.getAttribute(item, "pdref");
		  //Node refNode = XPathAPI.selectSingleNode(item, "@pdref");
          if (refNode != null)
          {
            // if the sub definition references another definition, we need to build that one
            String refVal = refNode.getNodeValue();
            if (refVal.length() > 0)
            {
              // if the sub definition is an on demand definition, we actually don't build
              // it at this time
              Node onDemNode = XMLManip.getAttribute(item, "onDemand");
			  //Node onDemNode = XPathAPI.selectSingleNode(item, "@onDemand");
              if (onDemNode != null)
              {
                String onDemand = onDemNode.getNodeValue();
                if (onDemand.equals("true"))
                {
                  // build part of it because it is on demand
                  buildOnDemandDefinition(item, pDef);
                }
                else
                {
                  // not on demand, so build it
                  Node n = findPropertyDefinitionDOMNodeForSubDef(refVal);
                  if (n != null)
                    processSubDefinitions(n, pDef);
                }
              }
              else
              {
                // not on demand, so build it
                Node n = findPropertyDefinitionDOMNodeForSubDef(refVal);
                if (n != null)
                  processSubDefinitions(n, pDef);
              }
            }
            else
            {
              // not referencing anything else, so build straight up
              buildDefinition(item, pDef);
            }
          }
          else
          {
            // Need to special case this - the edit control which is using the definitions and
            // factory needs the structure to be like this:
            // <Params
            //   <Param <-- No on demand, but a pdref
            //     <Name
            // The property editor needs it to look like this:
            // <Params
            //   <Param <-- On demand, but a pdref
            // so then when the user clicks on the on demand, the property editor builds the right
            // stuff and replaces the definition
            // If it is not on demand, but pdref'ed, the param node is not built because then in
            // the property editor, we would get
            // <Class
            //    <Classifier
            //		  <NamedElement
            // which is why the edit control now has a pdref2 not a pdref
            Node refNode2 = XMLManip.getAttribute(item, "pdref2");
			//Node refNode2 = XPathAPI.selectSingleNode(item, "@pdref2");
            if (refNode2 != null)
            {
              String refVal2 = refNode2.getNodeValue();
              Node n = findPropertyDefinitionDOMNodeForSubDef(refVal2);
              if (n != null)
                buildDefinition(n, pDef);
            }
            else
            {
              buildDefinition(item, pDef);
            }
          }
        }
      }
    } catch (Exception e)
    {

    }
  }*/

  /**
   * Transfer the information from the xml dom node to the property definition
   *
   * @param[in] pEle         The xml dom node to obtain the information from
   * @param[in] parentDef    The owning property definition to which to add the newly built definition to
   *
   * @return HRESULT
   *
   */
  private void buildDefinition(Node pNode, IPropertyDefinition pDef)
  {
    try {
      // we have the potential for there to be duplicate property definitions within a single "element"
      // because of multiple inheritance
      // IClass -> IClassifier -> INamespace -> INamedElement
      //					 -> IRedefinableElement	-> INamedElement
      //					 -> IPackageableElement	-> INamedElement
      // which was then giving us multiple name definitions
      //
      // this now checks its sub definitions for one matching the name, and only builds it if there is
      // not one present
      if (pNode != null) {
        Node pNamedNode = XMLManip.getAttribute(pNode, "name");
		//Node pNamedNode = XPathAPI.selectSingleNode(pNode, "@name");
        if (pNamedNode != null) {
          String name = pNamedNode.getStringValue();
//          Vector defs = pDef.getSubDefinitions();
//          if (defs != null) {
            IPropertyDefinition def = pDef.getSubDefinition(name);
/*            Enumeration enum = defs.elements();
            while (enum.hasMoreElements()) {
              Object obj = enum.nextElement();
              if (obj instanceof IPropertyDefinition) {
                IPropertyDefinition def1 = (IPropertyDefinition) obj;
                if (def1.getName().equals(name))
                {
                	def = def1;
					break;
                }
              }
            }*/
            if (def == null) {
              def = new PropertyDefinition();
              setAttributes(pNode, def);
              pDef.addSubDefinition(def);
              processSubDefinitions(pNode, def);
            }
//          }
        }
      }
    } catch (Exception e)
    {}
  }

  /**
   * Transfer the information from the xml dom node to the property definition for a definition that
   * has been marked as on demand.  This information is different from a node that is built right away.
   *
   * @param[in] pEle         The xml node to obtain the information from
   * @param[in] parentDef    The owning property definition to which to add the newly built definition to
   *
   * @return HRESULT
   *
   */
  private void buildOnDemandDefinition(Node pNode, IPropertyDefinition pDef)
  {
    IPropertyDefinition def = new PropertyDefinition();
    setRefAttributes(pNode, def);
    pDef.addSubDefinition(def);
  }

  /**
   * Catch all routine to build a map of attributes for this xml dom node.  This will catch any
   * attributes that do not have get/set methods for them
   *
   * @param[in] pEle   The xml node to obtain the information from
   * @param[in] pDef   The property definition to add the information to
   *
   * @return HRESULT
   *
   */
  private void processAttributes(Node pNode, IPropertyDefinition pDef)
  {
    if (pNode != null && pDef != null)
    {
    	try {
			if (pNode instanceof org.dom4j.Element)
			{            
				org.dom4j.Element ele = (org.dom4j.Element)pNode;
            int count = ele.attributeCount();
            for (int i=0; i<count; i++)
            {
               Attribute attr = ele.attribute(i);
               String name = attr.getName();
               String value = attr.getValue();
               pDef.addToAttrMap(name, value);
            }
            
//				List list = ele.elements();//attributes();
//				if (list != null)
//				{
//					int count = list.size();
//					for (int i=0; i<count; i++)
//					{
//						Attribute attr = (Attribute)list.get(i);
//						String name = attr.getName();
//						String value = attr.getValue();
//						pDef.addToAttrMap(name, value);
//					}
//				}
			}
    	}
    	catch (Exception e)
    	{
    		//e.printStackTrace();
    	}
    }
  }

 /**
 * Transfer the information from the xml dom node to the property definition
 *
 * @param[in] pEle   The xml dom node to obtain the information from
 * @param[in] pDef   The property defintion to add the information to
 *
 * @return HRESULT
 *
 */
 private void setAttributes(Node pNode, IPropertyDefinition pDef)
 {
   try {
   	if (pNode instanceof org.dom4j.Element)
   	{
   		org.dom4j.Element ele = (org.dom4j.Element)pNode;
   		
		//update the ID
   		Attribute idAttr = ele.attribute("id");
		if (idAttr != null) {
		  String idVal = idAttr.getValue();
		  pDef.setID(idVal);
		}
		
		//update the name
		Attribute nameAttr = ele.attribute("name");
		if (nameAttr != null) {
		  String nameVal = nameAttr.getValue();
		  pDef.setName(nameVal);
		}
		
		//update the display name
		Attribute dispAttr = ele.attribute("displayName");
		if (dispAttr != null) {
		  String dispVal = dispAttr.getValue();
		  pDef.setDisplayName(dispVal);
		}

		//update the multiplicity
		long mult = 1;
		Attribute multAttr = ele.attribute("multiplicity");
		if (multAttr != null) {
		  String multVal = multAttr.getValue();
		  if (multVal.equals("*"))
			mult = 2;
		}
		pDef.setMultiplicity(mult);

		//update the control type
		Attribute contAttr = ele.attribute("controlType");
		if (contAttr != null) {
		  String contVal = contAttr.getValue();
		  pDef.setControlType(contVal);
		}

		//update valid values
		Attribute valAttr = ele.attribute("values");
		if (valAttr != null)
                {
                    String val = valAttr.getValue();
                    pDef.setValidValues(val);
                    processValues(val, pDef);
                    
                    //update valid values
                    Attribute enumAttr = ele.attribute("enumValues");
                    if (enumAttr != null)
                    {
                        String enumVals = enumAttr.getValue();
                        pDef.setEnumValues(enumVals);
                    }
                }

		//update get method name
		String getMethodName = null;
		Attribute getAttr = ele.attribute("get");
		if (getAttr != null) {
		  String getVal = getAttr.getValue();
		  pDef.setGetMethod(getVal);
		  getMethodName = getVal;
		}

		//update set method name
		Attribute setAttr = ele.attribute("set");
		if (setAttr != null) {
		  String setVal = setAttr.getValue();
		  pDef.setSetMethod(setVal);
		}

		//update insert method name
		Attribute insAttr = ele.attribute("insert");
		if (insAttr != null) {
		  String insVal = insAttr.getValue();
		  pDef.setInsertMethod(insVal);
		}

		//update delete method name
		Attribute delAttr = ele.attribute("delete");
		if (delAttr != null) {
		  String delVal = delAttr.getValue();
		  pDef.setDeleteMethod(delVal);
		}

		//update create method name
		Attribute crtAttr = ele.attribute("create");
		if (crtAttr != null) {
		  String crtVal = crtAttr.getValue();
		  pDef.setCreateMethod(crtVal);
		}

		//update validate method name
		Attribute validAttr = ele.attribute("validate");
		if (validAttr != null) {
		  String validVal = validAttr.getValue();
		  pDef.setValidateMethod(validVal);
		}

		//update image name
		Attribute imgAttr = ele.attribute("image");
		if (imgAttr != null) {
		  String imgVal = imgAttr.getValue();
		  pDef.setImage(imgVal);
		}

		//update progID
		Attribute progAttr = ele.attribute("progID");
		if (progAttr != null) {
		  String progVal = progAttr.getValue();
		  pDef.setProgID(progVal);
		}

		//update help text
		Attribute helpAttr = ele.attribute("helpText");
		if (helpAttr != null) {
		  String helpVal = helpAttr.getValue();
		  pDef.setHelpDescription(helpVal);
		}
//		else {
//		  if (m_ModelElement != null) {
//			buildHelpDocumentation(pDef, m_ModelElement, getMethodName);
//		  }
//		}

		//update onDemand
		pDef.setOnDemand(false);
		Attribute demAttr = ele.attribute("onDemand");
		if (demAttr != null) {
		  String demVal = demAttr.getValue();
		  if (demVal.equals("true"))
			pDef.setOnDemand(true);
		}

		//update required
		pDef.setRequired(false);
		Attribute reqAttr = ele.attribute("required");
		if (reqAttr != null) {
		  String reqVal = reqAttr.getValue();
		  if (reqVal.equals("true"))
			pDef.setRequired(true);
		}
        
        //update force refresh
		pDef.setForceRefersh(false);
		Attribute refreshAttr = ele.attribute("forceRefresh");
		if (refreshAttr != null) {
		  String refreshVal = refreshAttr.getValue();
		  if (refreshVal.equals("true"))
			pDef.setForceRefersh(true);
		}

		//update defaultValue
		pDef.setDefaultExists(false);
		Attribute defAttr = ele.attribute("defaultValue");
		if (defAttr != null) {
		  String defVal = defAttr.getValue();
		  pDef.setDefaultValue(defVal);
		  pDef.setDefaultExists(true);
		}
   	}

     // call to build any other attributes that may not be listed here
     processAttributes(pNode, pDef);

     // new section in the xml file that states what to do with the information
     // we receive back from a "get" call, when that information is a IDispatch
     processOtherInvokes(pNode, pDef);
   } catch (Exception e)
   {
   	//e.printStackTrace();
   }
 }

 /**
  * Transfer the information from the xml dom node to the property definition.  This is a special
  * case if the property definition is referencing some other definition.  Only certain things need
  * to be stored.
  *
  * @param[in] pEle   The xml dom node to transfer the information from
  * @param[in] pDef   The property definition to add the information to
  *
  * @return HRESULT
  *
  */
 private void setRefAttributes(Node pNode, IPropertyDefinition pDef)
 {
   try {
   	if (pNode instanceof org.dom4j.Element)
   	{
   		org.dom4j.Element ele = (org.dom4j.Element)pNode;
   		
		//update the ID
		Attribute idNode = ele.attribute("id");
		if (idNode != null) {
		  String idVal = idNode.getValue();
		  pDef.setID(idVal);
		}

		//update the name
		Attribute nameNode = ele.attribute("pdref");
		if (nameNode != null) {
		  String nameVal = nameNode.getValue();
		  //This is different from C++ version, as our pdref is ID
		  org.dom4j.Element domEle = m_Doc.elementByID(nameVal);
		  if (domEle != null)
		  {
		  	String actualName = domEle.attributeValue("name");
			pDef.setName(actualName);
			pDef.setOnDemand(true);
		  }
		}

		//update the control type
		Attribute contNode = ele.attribute("controlType");
		if (contNode != null) {
		  String contVal = contNode.getValue();
		  pDef.setControlType(contVal);
		}

		//update progID
		Attribute progNode = ele.attribute("progID");
		if (progNode != null) {
		  String progVal = progNode.getValue();
		  pDef.setProgID(progVal);
		}
   	}
   	
     // catch all to process any other attributes not listed here
     processAttributes(pNode, pDef);
   } catch (Exception e)
   {}
 }

 /**
  * Build the help string for the property definition based on the "get" function
  *
  * @param[in] pDef      The property definition to add the help information to
  * @param[in] pDisp     The IDispatch to obtain the help information from
  * @param[in] funcName  The function whose help documentation we are getting
  *
  * @return HRESULT
  *
  */
 private void buildHelpDocumentation(IPropertyDefinition pDef, Object pDisp, String funcName)
 {
   if (funcName != null)
   {
     pDef.setHelpDescription(funcName);
   }
   else
   {
     // just set it to the class name.
     pDef.setHelpDescription(pDisp.getClass().getName());
   }
   //Sumitabh might have to get it from getElementTypeDocMap?
 }



  /**
   * Initialize the factory
  */
  public void initialize()
  {

  }

  /**
   * Get the Method that matches the passed-in method name and parmType list in passed pDisp.
   *
   * @param[in] pDisp  The IDispatch to obtain the function information from
   * @param[in] memID  The ID of the function that we are looking for
   * @param[out] pVal  The IDispatch of the found function
   *
   * @return HRESULT
   *
   */
  public Object getFromFunctionMap( Object pDisp, String methName, String[] parmTypes )
  {
    Method func = null;
    try {
      Class clazz = pDisp.getClass();
      Class[] parms = null;
      if (parmTypes != null && parmTypes.length > 0) {
        parms = new Class[parmTypes.length];
        for (int i = 0; i < parmTypes.length; i++) {
          parms[i] = Class.forName(parmTypes[i]);
        }
      }
      func = clazz.getMethod(methName, parms);
    } catch(Exception e)
    {}
    return func;
  }


  /**
   * Retrieve documentation information from an already built map based on Object type
  */
  public void getFromElementTypeDocMap( Object pDisp, String pVal )
  {

  }

  /**
   * Gets the xml file that defines the property definitions
  */
  public String getDefinitionFile()
  {
    return m_definitionFile;
  }

  /**
   * Gets the xml file that defines the property definitions
  */
  public void setDefinitionFile( String value )
  {
    m_definitionFile = value;
  }

  /**
   * There were some cases where the property definitions are contained in a file, and we want them
   * all to be built at once.  The property editor builds them as it needs to, the preference manager
   * just builds them all at once.
   *
   * The file needs to have already been set before calling this function.
   *
   * @param pVal[out]	The property definitions that were built
   *
   * @return HRESULT
   *
   */
  public Vector<IPropertyDefinition> buildDefinitionsUsingFile()
  {
    Vector<IPropertyDefinition> vec = new Vector<IPropertyDefinition>();
    try {
      if (m_definitionFile != null && m_definitionFile.length() > 0) {
      	ETSystem.out.println(m_definitionFile);
        Document doc = XMLManip.getDOMDocument(m_definitionFile);
        if (doc != null) {
          // loop through all property definitions and build them
          // wanted to use existing "create" code, so we just get the name
          // of the property definition and then ask the same mechanism that
          // was used for the property editor
          String pattern = "//PropertyDefinition";
		  List list = doc.selectNodes(pattern);
          //List list = XPathAPI.selectNodeList(doc, pattern);
          if (list != null && list.size() > 0) {
          	
          	//just go through all PropertyDefinition nodes and store the name
          	// and id in a hashtable.
			for (int i = 0; i < list.size(); i++) {
				Node node = (Node)list.get(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					String name = ((org.dom4j.Element)node).attributeValue("name");
					String id = ((org.dom4j.Element)node).attributeValue("id");
					addToNodeMap(node, id);
					addToNamedNodeMap(node, name);
				}
			}
          	
            for (int i = 0; i < list.size(); i++) {
              Node node = (Node)list.get(i);
              Node nameNode = XMLManip.getAttribute(node, "name");
			  //Node nameNode = XPathAPI.selectSingleNode(node, "@name");
              if (nameNode != null) {
                IPropertyDefinition def = getPropertyDefinitionForElement(
                    nameNode.getStringValue(), null);
                if (def != null) {
                  vec.add(def);
                }
              }
            }
          }
        }
      }
    } catch (Exception e)
    {
    	e.printStackTrace();
    }

    return vec;
  }

  /**
   * The property definition can have several values for its "values" field: it can be
   * a hardcoded string ("public|private"), a string ("FormatString"), a xpath query(#//UML:Class/@name),
   * or an xpath query plus file (Languages.etc#//Language/@type).
   *
   * This method interprets the value and fills in the appropriate information
   *
   *
   * @param pVal[in]	String stored in definition attribute "values"
   * @param pDef[in]	property definition to process
   *
   * @return
   *
   */
  private void processValues(String pVal, IPropertyDefinition pDef)
  {
    //only do something if pVal has some value
    if (pVal != null && pVal.length() > 0)
    {
      // if it has a "#" in it, then this is an xpath query
      // it could either be a xpath query on a given file
      // or an empty file means perform the query on the current project
      int pos = pVal.indexOf("#");
      if (pos >=0)
      {
        // have found a #, but now need to check if it is a protected value in a list
        int pos2 = pVal.indexOf("|#|");
        if (pos2 >= 0)
        {
          pDef.setValidValues2(pVal);
        }
        else
        {
          // this is a # in a string that should represent a file and an xpath
          String[] strs = pVal.split("#");
          if (strs != null && strs.length > 1)
          {
            String fileName = strs[0];
            String xpath = strs[1];
            if (fileName.length() > 0)
            {
              //Sumitabh use IConfigManager to get the document using home etc.
              
			  String configLoc = ProductRetriever.retrieveProduct().getConfigManager().getDefaultConfigLocation();
			  //m_ConMan.getDefaultConfigLocation();
			  configLoc += fileName;
              Document doc = XMLManip.getDOMDocument(configLoc);
              if (doc != null) {
                try {
                  List list = XMLManip.selectNodeList(doc, xpath);
                  if (list != null && list.size() > 0) {
                    String val = "";
                    for (int i = 0; i < list.size(); i++) {
                      Node lNode = (Node)list.get(i);
                      if (val.length() > 0)
                        val += "|";
                      val += lNode.getStringValue();
                    }
                    pDef.setValidValues2(val);
                  }
                } catch(Exception e)
                {}
              }
            }
            else
            {
                    // we do not have a file, so our requirements are to search the current project
                    // unfortunately at this time, we do not have that information
                    // the values for the property definition will have to be built at a later time
            }
          }
        }
      }
      else
      {
        // the values string did not contain any xpath stuff
        // so check for other known values
        pos = pVal.indexOf("FormatString");
        if( pos >= 0)
        {
                // the definition has been marked to contain a format string
                // unfortunately at this time, we do not have that information
                // the values for the property definition will have to be built at a later time
        }
        else
        {
          pos = pVal.indexOf("::");
          if (pos >= 0)
          {
            // the definition has an object and a method stored in it that needs to be
            // created and then invoked in order to have the value
            String[] strs = pVal.split("::");
            if (strs != null && strs.length > 1)
            {
              //first element will be object name and second will be method name
              String objName = strs[0];
              String methName = strs[1];
              if (objName.length() >0 && methName.length()>0)
              {
                 Object objInstance = null;
                 if(objName.equals("org.netbeans.modules.uml.core.metamodel.core.foundation.ConfigManager") == true)
                 {
                    objInstance = retrieveConfigManagerAsObject();   
                 }
                 else if(objName.equals("org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.LanguageManager") == true)
                 {
                    objInstance = retrieveLanguageManagerAsObject(); 
                 }
                 
                //now invoke this method on this object and get the result -
                //these are values
                try 
                {
                   objInstance = retrieveObject(objName);
//                  Class clazz = Class.forName(objName);
//                  objInstance = clazz.newInstance();
                  
                   if(objInstance != null)
                   { 
                      //Sumitabh need to figure out the parameters - may be we need third
                      //parameter in pVal :: separated.
                      Method meth = objInstance.getClass()
                        .getMethod(methName, (Class[])null);
                      
                      Object obj = meth.invoke(objInstance, (Object[])null);
                      if (obj != null)
                      {
                         //if (obj.getClass().getName().equals("java.lang.String"))
						pDef.setValidValues(obj.toString());
                         pDef.setValidValues2(obj.toString());
                      }
                   }
                }catch (Exception e)
                {
                   //e.printStackTrace();
                }
              }
            }
          }
          else
            pDef.setValidValues2(pVal);
        }
      }
    }
  }


   protected Object retrieveConfigManagerAsObject()
   {
      Object retVal = null;
      
      ICoreProduct product = ProductRetriever.retrieveProduct();
      if(product != null)
      {
         retVal = product.getConfigManager();
      }
      
      return retVal;
   }
   
   protected Object retrieveLanguageManagerAsObject()
   {
      Object retVal = null;
      
      ICoreProduct product = ProductRetriever.retrieveProduct();
      if(product != null)
      {
         retVal = product.getLanguageManager();
      }
      
      return retVal;
   }

   protected Object retrieveObject(String objName)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      Object retVal = null;
      
      ICoreProduct product = ProductRetriever.retrieveProduct();
      if(product != null)
      {
         Class clazz = Class.forName(objName);         
         retVal = clazz.newInstance();
      }
      
      return retVal;
   }
   
  /**
   * Gets the xml document that defines the property definitions
  */
  public Document getXMLDocument()
  {
    return m_Doc;
  }

  /**
   * Gets the xml document that defines the property definitions
  */
  public void setXMLDocument( Document value )
  {
    m_Doc = value;
  }

  /**
   * Retrieve or create the proper property definition for the passed-in name.
   *
   * @param[in] eleStr The name of the property definition
   * @param[out] pVal  The property definition that has been created or found
   *
   * @return HRESULT
   *
   */
  public IPropertyDefinition getPropertyDefinitionByName( String name)
  {
    IPropertyDefinition def = null;
    if (name != null && name.length() > 0)
    {
      def = getFromDefinitionMap(name);
      if (def == null)
      {
        def = createDefinition(name);
      }
      if (def != null)
      {
        addToDefinitionMap(name, def);
      }
    }
    return def;
  }

  /**
   *	Given a xml node, build the corresponding property definition
   *
   *
   * @param pNode[in]				The node for which we want to build a definition
   * @param pDef[out]				The newly created definition
   *
   * @return HRESULT
   *
   */
  public IPropertyDefinition buildDefinitionFromNode( Node pNode )
  {
    IPropertyDefinition def = null;
    if (pNode != null)
    {
      Document doc = pNode.getDocument();
      setXMLDocument(doc);
      def = new PropertyDefinition();
      setAttributes(pNode, def);
      processSubDefinitions(pNode, def);
    }
    return def;
  }

  /**
   *	Given a xml node, build the corresponding property definition
   *
   *
   * @param pNode[in]				The node for which we want to build a definition
   * @param pDef[out]				The newly created definition
   *
   * @return HRESULT
   *
   */
  public IPropertyDefinition buildDefinitionFromString( String str )
  {
    IPropertyDefinition def = null;
    try {
      if (str != null && str.length() > 0) {
        Document doc = XMLManip.getDOMDocumentFromString(str);
        if (doc != null) {
          String pattern = "PropertyDefinition";
          List list = doc.selectNodes(pattern);
          if (list != null && list.size() > 0)
          {
			Node n = (Node)list.get(0);
			//Node n = XPathAPI.selectSingleNode(doc, pattern);
			if (n != null) {
			  def = new PropertyDefinition();
			  setAttributes(n, def);
			  processSubDefinitions(n, def);
			}
          }
        }
      }
    } catch (Exception e)
    {}
    return def;
  }

  /**
   * Find the xml dom node that defines the passed in value.  The definition file is created based on element
   * type.
   *
   * @param[in] name   The element type
   * @param[out] pVal  The dom node in the definition file matching the element type
   *
   * @return HRESULT
   *
   */
  private Node findPropertyDefinitionDOMNode(String name)
  {
    Node node = null;
    try {
      if (name != null && name.length() > 0) 
      {
        if (m_NamedNodeMap != null)
        {
        	node = getFromNamedNodeMap(name);
        }
        if (node == null)
        {
          if (m_Doc != null) {
          String pattern = "//PropertyDefinition[@name=\'";
          pattern += name;
          pattern += "\']";
          node = m_Doc.selectSingleNode(pattern);
          }
        }
      }
    } catch (Exception e)
    {}
    return node;
  }

  //used for building sub definitions.
  private Node findPropertyDefinitionDOMNodeForSubDef(String name)
  {
	Node retNode = getFromNodeMap(name);
  	if (retNode == null)
  	{
		retNode = m_Doc.elementByID(name);
  		if (retNode != null)
  		{
  			addToNodeMap(retNode, name);
  		}
  	}
	return retNode;
  }
  /**
   * Build a property definition
   *
   * @param[in] name   The element type
   * @param[out] pVal  The property definition that was just built
   *
   * @return HRESULT
   *
   */
  private IPropertyDefinition createDefinition(String name)
  {
    IPropertyDefinition def = null;
    if (name != null && name.length()>0)
    {
      try {
        if (m_definitionFile != null && m_definitionFile.length() > 0) {
          if (m_Doc == null) {
            m_Doc = XMLManip.getDOMDocument(m_definitionFile);
          }
          if (m_Doc != null) {
            Node domNode = findPropertyDefinitionDOMNode(name);
            if (domNode != null) {
              def = new PropertyDefinition();
              setAttributes(domNode, def);
              processSubDefinitions(domNode, def);
              addToNodeMap(domNode, name);
            }
          }
        }
      } catch (Exception e)
      {}
    }
    return def;
  }
}