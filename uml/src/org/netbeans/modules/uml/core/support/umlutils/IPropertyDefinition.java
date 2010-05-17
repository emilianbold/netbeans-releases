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

package org.netbeans.modules.uml.core.support.umlutils;

import java.util.Vector;
import java.util.HashMap;

import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface IPropertyDefinition
{

	//  Name of the property definition
	// HRESULT Name([out, retval] BSTR *pVal);
	public String getName();

	// Name of the property definition
	// HRESULT Name([in] BSTR newVal);
	public void setName(String name);

	//, Determines whether or not property is deletable
	// HRESULT Required([out, retval] VARIANT_BOOL *pVal);
	public boolean isRequired();

	// Determines whether or not property is deletable
	// HRESULT Required([in] VARIANT_BOOL newVal);
	public void setRequired(boolean val);
    
    /**
     * Sets the force refresh property.  The force refersh property is used to 
     * determine if the properties need to be refreshed when ever the value
     * changes. 
     */
    public void setForceRefersh(boolean val);
  
    /**
     * Determines if the properties need to be refreshed whenever the property
     * is modified.
     */
    public boolean isForceRefresh();

	// Specifies the number of these properties that a parent property can have
	// HRESULT Multiplicity([out, retval] long *pVal);
	public long getMultiplicity();

	// Specifies the number of these properties that a parent property can have
	//HRESULT Multiplicity([in] long newVal);
	public void setMultiplicity(long val);

	// Tooltip for property
	// HRESULT HelpDescription([out, retval] BSTR *pVal);
	public String getHelpDescription();

	// Tooltip for property
	// HRESULT HelpDescription([in] BSTR newVal);
	public void setHelpDescription(String val);

	// Child properties of this property
	// HRESULT SubDefinitions([out, retval] IPropertyDefinitions* *pVal);
	public Vector getSubDefinitions();
	public HashMap getHashedSubDefinitions();

	// Child properties of this property
	// HRESULT SubDefinitions([in] IPropertyDefinitions* newVal);
	public void setSubDefinitions(Vector val);

	// Add a new property definition to the property
	// HRESULT AddSubDefinition(IPropertyDefinition* definition);
	public void addSubDefinition(IPropertyDefinition def);

	// Gets a specific sub property by index
	// HRESULT GetSubDefinition(VARIANT index, IPropertyDefinition** definition);
	public IPropertyDefinition getSubDefinition(int index);
	public IPropertyDefinition getSubDefinition(String name);

	// Display Name of the property definition
	// HRESULT DisplayName([out, retval] BSTR *pVal);
	public String getDisplayName();

	public String getPropertyEditorShowName();
	public void setPropertyEditorShowName(String str);

	// Display Name of the property definition
	// HRESULT DisplayName([in] BSTR newVal);
	public void setDisplayName(String val);

	// Parent definition of the property definition
	// HRESULT Parent([out, retval] IPropertyDefinition* *pVal);
	public IPropertyDefinition getParent();

	// Parent definition of the property definition
	// HRESULT Parent([in] IPropertyDefinition* newVal);
	public void setParent(IPropertyDefinition def);

	// Gets the type of control that will be displayed for this definition - edit, listbox, combobox, etc
	// HRESULT ControlType([out, retval] BSTR *pVal);
	public String getControlType();

	// Sets the type of control that will be displayed for this definition - edit, listbox, combobox, etc
	// HRESULT ControlType([in] BSTR newVal);
	public void setControlType(String str);

	// Gets the method that will be invoked to obtain the information for this definition
	// HRESULT GetMethod([out, retval] BSTR *pVal);
	public String getGetMethod();

	// Sets the method that will be invoked to obtain the information for this definition
	// HRESULT GetMethod([in] BSTR newVal);
	public void setGetMethod(String val);

	// Gets the method that will be invoked to set the information for this definition
	// HRESULT SetMethod([out, retval] BSTR *pVal);
	public String getSetMethod();

	// Sets the method that will be invoked to set the information for this definition
	// HRESULT SetMethod([in] BSTR newVal);
	public void setSetMethod(String val);

	// ID of the property definition HRESULT ID([out, retval] BSTR *pVal);
	public String getID();

	// ID of the property definition HRESULT ID([in] BSTR newVal);
	public void setID(String val);

	// Gets the method that will be invoked to perform an insert for this definition
	// HRESULT InsertMethod([out, retval] BSTR *pVal);
	public String getInsertMethod();
	public void setInsertMethod(String val);

	// Sets the method that will be invoked to perform an insert for this definition
	// HRESULT InsertMethod([in] BSTR newVal);

	// Gets the method that will be invoked to perform a delete for this definition
	// HRESULT DeleteMethod([out, retval] BSTR *pVal);
	// Sets the method that will be invoked to perform a delete for this definition
	// HRESULT DeleteMethod([in] BSTR newVal);
	public String getDeleteMethod();
	public void setDeleteMethod(String val);

	// Gets a string that defines the data to be inserted into the control for this definition
	// HRESULT ValidValues([out, retval] BSTR *pVal);
	// Sets a string that defines the data to be inserted into the control for this definition
	// HRESULT ValidValues([in] BSTR newVal);
	public String getValidValues();
	public void setValidValues(String val);

	// Gets a string that represents the data to be inserted into the control for this definition
	// HRESULT ValidValues2([out, retval] BSTR *pVal);
	// Sets a string that represents the data to be inserted into the control for this definition
	// HRESULT ValidValues2([in] BSTR newVal);
	public String getValidValues2();
	public void setValidValues2(String val);

        /**
         * Some list are actually enumeration list.  The enum values are used to
         * determine the correct value that should be set and retreived.
         */
        public void setEnumValues(String values);
        
        /**
         * Some list are actually enumeration list.  The enum values are used to
         * determine the correct value that should be set and retreived.
         *
         * @return The string will be the list of enumeration values seperated
         *         by "|" characters.
         */
        public String getEnumValues();
        
        /**
         * Some list are actually enumeration list.  The enum values are used to
         * determine the correct value that should be set and retreived.
         *
         * @return The list of enumeration values.
         */
        public String[] getEnumValueList();
        
	// Catch all routine that builds a map of a xml nodes attributes and values
	// HRESULT AddToAttrMap(BSTR name, BSTR value);
	public void addToAttrMap(String name, String value);

	// Retrieves a particular xml attribute and value from the already built map
	// HRESULT GetFromAttrMap(BSTR name, BSTR* value);
	public String getFromAttrMap(String name);

	// Gets whether or not this definition needs to be built immediately
	// HRESULT OnDemand([out, retval] BOOL *pVal);
	// Sets whether or not this definition needs to be built immediately
	// HRESULT OnDemand([in] BOOL newVal);
	public boolean isOnDemand();
	public void setOnDemand(boolean val);

	// Gets the method that will be invoked to perform a create for this definition
	// HRESULT CreateMethod([out, retval] BSTR *pVal);
	//21), Sets the method that will be invoked to perform a create for this definition
	// HRESULT CreateMethod([in] BSTR newVal);
	public String getCreateMethod();
	public void setCreateMethod(String val);

	// Path of the image for this definition
	// HRESULT Image([out, retval] BSTR *pVal);
	// Path of the image method for this definition
	// HRESULT Image([in] BSTR newVal);
	public String getImage();
	public void setImage(String val);

	// Gets a ProgID to be used for this definition
	// HRESULT ProgID([out, retval] BSTR *pVal);
	// Sets a ProgID to be used for this definition
	// HRESULT ProgID([in] BSTR newVal);
	public String getProgID();
	public void setProgID(String val);

	// Tells a property definition to store itself
	// HRESULT Save();
	public void save();

	/* Determines whether or not this element has been modified
	 HRESULT Modified([out, retval] VARIANT_BOOL *pVal);
	 Determines whether or not this element has been modified
	 HRESULT Modified([in] VARIANT_BOOL newVal);*/
	public boolean isModified();
	public void setModified(boolean val);

	// Tells a property definition to remove itself
	// HRESULT Remove();
	public void remove();

	// Returns the path to this definition by getting each of the parent names and building a | delimited string
	// HRESULT GetPath(BSTR* path);
	public String getPath();

	/* Gets the default value of this definition
	 HRESULT DefaultValue([out, retval] BSTR *pVal);
	 Sets the default value of this definition
	 HRESULT DefaultValue([in] BSTR newVal); */
	public String getDefaultValue();
	public void setDefaultValue(String val);

	/* Does this definition have a default value
	 HRESULT DefaultExist([out, retval] VARIANT_BOOL *pVal);
	 Does this definition have a default value
	 HRESULT DefaultExist([in] VARIANT_BOOL newVal); */
	public boolean isDefaultExisting();
	public void setDefaultExists(boolean val);

	/* Gets the method that will be invoked to determine if the value for this definition is valid
	 HRESULT ValidateMethod([out, retval] BSTR *pVal);
	 Sets the method that will be invoked to determine if the value for this definition is valid
	 HRESULT ValidateMethod([in] BSTR newVal); */
	public String getValidateMethod();
	public void setValidateMethod(String val);

	// Retrieves the number of xml attribute in the map
	// HRESULT GetAttrMapCount(long* count);
	public long getAttrMapCount();

	// Retrieves a particular xml attribute and value from the already built map based on position
	// HRESULT GetFromAttrMap2(long pos, BSTR* name, BSTR* value);
	public void getFromAttrMap(long pos, String name, String value);

	// Returns a string collection that represents the data to be inserted into the control for this definition
	// HRESULT ValidValues3([in] IPropertyElement* pElement, [out, retval] IStrings* *pValues);
	public IStrings getValidValue(IPropertyElement elem);

}
