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
import java.util.List;
import org.dom4j.Text;

import org.netbeans.modules.uml.core.support.umlutils.ETList;


/**
* TaggedValueImpl is the implementation of the UML
* TaggedValue meta type.
*
* A tagged value allows information to be attached to any model element in conformance with its
* tag definition. Although a tagged value, being an instance of a kind of NamedElement,
* automatically inherits the name attribute, the name that is actually used in the tagged value is
* the name of the associated tag definition. The interpretation of tagged values is intentionally
* beyond the scope of UML semantics. It must be determined by user or tool conventions that
* may be specified in a profile in which the tagged value is defined. It is expected that various
* model analysis tools will define tag definitions to supply information needed for their
* operations beyond the basis semantics of UML. Such information could include code
* generation options, model management information, or user-specified semantics.
* Any tagged value must have one or more reference value links or one or more data values, but
* not both.
*/ 
public class TaggedValue extends NamedElement implements ITaggedValue{

	/**
	 * 
	 */
	public TaggedValue() {
		super();
	}

	/**
	 * Retrieves the collection of String objects that make up the datavalues
	 * of this TaggedValue.
	 *
	 * Specifies the set of values that are part of the tagged value. The
	 * type of this value must conform to the type specified in the
	 * tagType attribute of the associated tag definition. The number of
	 * values that can be specified is defined by the multiplicity attribute
	 * of the associated tag definition.
	 *
	 * @param value[out] The value that this tag is comprised of.
	 *
	 * @return S_OK
	 *			  - E_INVALIDARG
	 *			  - TVR_E_INCOMPATIBLE_VALUES: The values of this tag are
	 *					not string values. The are actually refs to other
	 *					NamedElements. Call get_ReferenceValue() instead.
	 * @warning Is is assumed that the values associated with this tag are string values. 
	 *			If that is not the case, then the
	 *			get_TypedDataValue() must be called intead.
	 * 
	 */
	public String getDataValue() {
		String value = "";
		Node node = UMLXMLManip.selectSingleNode(m_Node, "UML:TaggedValue.dataValue");
		if (node != null && node instanceof org.dom4j.Element)
		{
			org.dom4j.Element ele = (org.dom4j.Element)node;
			value = ele.getText();
//			List list = ele.elements();
//			if (list != null)
//			{
//				// There should only be one child, which should
//				// be a text node
//				int count = list.size();
//				if (count == 1)
//				{
//					Node child = (Node)list.get(0);
//					if (child instanceof org.dom4j.Text)
//					{
//						Text textNode =(Text)child;
//						value = textNode.getText(); 
//					}
//				}
//			}
		}
		return value;
	}

	/**
	 *
	 * Sets the value of this tagged value.
	 *
	 * @param pVal[in] The new value
	 *
	 * @return HRESULTs
	 */
	public void setDataValue(String val) {
		if (val != null && val.length() > 0)
		{
			UMLXMLManip.setNodeTextValue(this, "UML:TaggedValue.dataValue", val, false);
		}
		else
		{
			UMLXMLManip.setNodeTextValue(this, "UML:TaggedValue.dataValue", "", false);
		}
	}

	/**
	 * Retrieves the NamedElement that this TaggedValue is
	 * association with.
	 *
	 * @param element[out] The element this tag is assocatied with
	 *
	 * @return HRESULTs
	 * 
	 */
	public INamedElement getNamedElement() 
   {
      INamedElement dummy = null;
		return retrieveSingleElementWithAttrID( "NamedElement", dummy, INamedElement.class );
	}

	/**
	 * Sets the model element that this TaggedValue is associated with.
	 *
	 * @param owner[in] The element this tag is associated with
	 *
	 * @return S_OK
	 *			  - GR_E_VERSIONABLEELEMENT_NOT_SUPPORTED: owner doesn't support
	 *							the IVersionableElement interface.
	 * 
	 */
	public void setNamedElement(INamedElement elem) {
		UMLXMLManip.addElementByID(this, elem, "NamedElement");
		
	}

	/**
	 *
	 * Sets the owner of the tag based on the ID passed in.
	 *
	 * @param ownerID[in] The XMI id of the owning model element
	 *
	 * @return HRESULTs
	 */
	public void setNamedElementID(String ownerID) {
		setAttributeValue( "NamedElement", ownerID );
	}

	/**
	 * Retrieves the collection of NamedElements that make up the datavalues
	 * of this TaggedValue.
	 *
	 * Specifies the set of values that are part of the tagged value. The
	 * type of this value must conform to the type specified in the
	 * tagType attribute of the associated tag definition. The number of
	 * values that can be specified is defined by the multiplicity attribute
	 * of the associated tag definition.
	 *
	 * @param collection[out] The collection of elements that this tag is comprised
	 *                    of.
	 *
	 * @return HRESULTs
	 * @see get_DataValue()
	 */
	public ETList<INamedElement> getReferenceValue() 
    {
      INamedElement dummy = null;
      return retrieveElementCollection(dummy, "UML:TaggedValue.dataValue/*", INamedElement.class);
	}

	/**
	 *
	 * Populates this TaggedValue with a simple name and one dataValue. The resultant
	 * XMI fragment looks like this when done, assuming "hello" and "Here's the value."
	 * are passed in as parameters:
	 *
	 *    <UML:TaggedValue name="hello" >
	 *			<UML:TaggedValue.dataValue>
	 *				Here's the value.
	 *			</UML:TaggedValue.dataValue>
	 *		</UML:TaggedValue>
	 *
	 * @param tagName[in] The name of the tag
	 * @param dataValue[in] The value of the tag
	 *
	 * @return HRESULTs
	 *
	 */
	public void populate(String tagName, String dataVal) {
		org.dom4j.Element element = getElementNode();
		if (element != null) 
		{
			if (tagName.length() > 0)
			{
				setAttributeValue("name", tagName);
				setDataValue(dataVal);
			}
			else
			{
				// cannot create a stereotype if there is no name
				// tried returning S_FALSE here but somehow through
				// the invoke, we lost its meaning
			}
		}
	}

	/**
	 *
	 * Determines whether or not the TaggedValue is displayed in the GUI.
	 *
	 * @param newVal[in] - true will hide the TaggedValue from the GUI, else
	 *                   - false will show the TaggedValue
	 *
	 * @return HRESULT
	 *
	 */
	public void setHidden(boolean newVal) {
		setBooleanAttributeValue("hidden", newVal);
	}

	public boolean isHidden() {
		return getBooleanAttributeValue("hidden", false);
	}

	/**
	 *
	 * Creates the actual TaggedValue node in the DOM tree.
	 *
	 * @param doc[in] The document owner of the new node
	 * @param parent[in] The parent of the new node
	 *
	 * @return HRESULTs
	 *
	 */
	public void establishNodePresence(Document doc, Node parent )
	{
	   buildNodePresence("UML:TaggedValue", doc, parent );
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
	protected boolean validate(Object pDisp, String fieldName, String fieldValue, String outVal)
	{
		boolean valid = true;
		
		// Using this mechanism to determine whether or not a tagged value
		// should be read-only or not in the property editor.  This was normally used
		// to validate data before it was saved, but the same mechanism could
		// be used for this.
		// Right now the only tagged value that we want to be read-only, but not hidden
		// is the ER studio tagged values
		String name = getName();
		if (name.equals("ERSDiagram"))
		{
			valid = false;
		}
		return valid;
	}
	
	protected void whenValid(Object obj)
	{
		
	}
	protected void whenInvalid(Object obj)
	{
		
	}
}

