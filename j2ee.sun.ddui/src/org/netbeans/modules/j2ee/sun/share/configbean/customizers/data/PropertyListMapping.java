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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
/*
 * PropertyListMapping.java
 *
 * Created on January 29, 2004, 2:06 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.data;

import java.io.InputStream;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

import java.net.URL;
import java.net.URLClassLoader;

import org.netbeans.modules.j2ee.sun.share.configbean.Utils;

/**
 *
 * @author Peter Williams
 */
public class PropertyListMapping implements Comparable {
	
	/** constants that define the expected property lists from propertydata.xml
	 */
	public static final String WEBAPP_JSPCONFIG_PROPERTIES = "WebAppJspConfigProperties";	// NOI18N
	public static final String WEBAPP_PROPERTIES = "WebAppProperties";	// NOI18N
	public static final String WEBAPP_CLASSLOADER_PROPERTIES = "WebAppClassloaderProperties";	// NOI18N
	
	public static final String CACHE_PROPERTIES = "CacheProperties";	// NOI18N
	public static final String CACHE_DEFAULT_HELPER_PROPERTIES = "CacheDefaultHelperProperties";	// NOI18N
	public static final String CACHE_HELPER_PROPERTIES = "CacheHelperProperties";		// NOI18N
	
	public static final String CONFIG_MANAGER_PROPERTIES = "ConfigManagerProperties";	// NOI18N
	public static final String CONFIG_STORE_PROPERTIES = "ConfigStoreProperties";		// NOI18N
	public static final String CONFIG_SESSION_PROPERTIES = "ConfigSessionProperties";	// NOI18N
	public static final String CONFIG_COOKIE_PROPERTIES = "ConfigCookieProperties";		// NOI18N

	public static final String SERVICE_REF_CALL_PROPERTIES = "ServiceRefCallProperties";		// NOI18N
	public static final String SERVICE_REF_STUB_PROPERTIES = "ServiceRefStubProperties";		// NOI18N

	public static final String EJBJAR_CMP_PROPERTIES = "EjbJarCmpProperties";	// NOI18N
	public static final String EJBJAR_CMP_SCHEMA_PROPERTIES = "EjbJarCmpSchemaProperties";	// NOI18N
    
    
	private final PropertyList propList;
//	private String displayText;
	
	/** Creates a new instance of PropertyListMapping
	 *  This object does NOT handle a null PropertyList
	 */
	private PropertyListMapping(final PropertyList l) {
		propList = l;
	}

	/** equals() maps to PropertyList.equals()
	 *
	 * @return true/false based on whether the embedded property list objects
	 *  compare as equal.
	 */
	public boolean equals(Object obj) {
		boolean result = false;
		
		if(obj instanceof PropertyListMapping) {
			if(this == obj) {
				result = true;
			} else {
				PropertyListMapping targetMapping = (PropertyListMapping) obj;
				PropertyList targetList = targetMapping.getPropertyList();
				result = propList.getPropertyName().equals(targetList.getPropertyName());
			}
		}
		return result;
	}
	
	/** hashCode() maps to PropertyList.hashCode()
	 *
	 * @return the hashcode
	 */
	public int hashCode() {
		return propList.getPropertyName().hashCode();
	}
	
	/** A more readable display string
	 *
	 * @return A descriptive string
	 */
	public String toString() {
		return propList.getPropertyName();
	}

	/** The property list
	 *
	 * @return the property list this is a mapping for
	 */
	public PropertyList getPropertyList() {
		return propList;
	}
	
	/** For sorted collections.  We compare the string representations of the 
	 *  embedded property list.
	 *
	 * @param obj the PropertyListMapping to compare to
	 * @return result of comparison (negative, 0, or positive depending on match)
	 */
	public int compareTo(Object obj) {
		int result = -1;
		
		if(obj instanceof PropertyListMapping) {
			if(this == obj) {
				result = 0;
			} else {
				PropertyListMapping targetMapping = (PropertyListMapping) obj;
				PropertyList targetList = targetMapping.getPropertyList();
				result = propList.getPropertyName().compareTo(targetList.getPropertyName());
			}
		}
		
		return result;
	}
	
	/** -----------------------------------------------------------------------
	 *  Loader for property list data and validators
	 */
	/** Retrieve the specified property list, if it exists.
	 * 
	 * @param propertyListName The name of the property list to be found.  This
	 *  corresponds to the value of the property-name field in the xml file.
	 * @return The property list specified, or null if it was not found.
	 */
	public static PropertyList getPropertyList(String propertyListName) {
		return (PropertyList) propertyLists.get(propertyListName);
	}

	/** Retrieve the Pattern associated with the validator specified.  This allows
	 *  us to cache the patterns that match an reuse them (which we don't currently
	 *  do.)
	 *
	 * !PW This doesn't really belong in this object, but it's a convenient
	 *     location to put it since the validators and the property lists are
	 *     read from the same XML file.
	 * 
	 * @param validatorName The name of the validator to be found.  This
	 *  corresponds to the value of the validator-name field in the xml file.
	 * @return A Pattern built from the regular expression associated with the
	 *  name passed in or null if the name was not found.
	 */
	public static Pattern getValidator(String validatorName) {
		return (Pattern) validatorList.get(validatorName);
	}
	
	private static final String PROPERTYDATA_FILENAME =
		"org/netbeans/modules/j2ee/sun/share/configbean/customizers/data/propertydata.xml";	// NOI18N
	
	private static Map propertyLists;
	private static Map validatorList;
	
	static {
		loadPropertyLists();
	}
	
	private static void loadPropertyLists() {
		propertyLists = new HashMap(37);
		validatorList = new HashMap(19);
		
		try {
			URL propertyListURL = Utils.getResourceURL(PROPERTYDATA_FILENAME, PropertyListMapping.class);
			InputStream inputStream = propertyListURL.openStream();
			
			DynamicProperties props = DynamicProperties.read(inputStream);
			for(Iterator iter = props.fetchPropertyListList().iterator(); iter.hasNext(); ) {
				PropertyList propList = (PropertyList) iter.next();
				String propertyListName = propList.getPropertyName();
				propertyLists.put(propertyListName, propList);
			}
			
			for(Iterator iter = props.fetchValidatorList().iterator(); iter.hasNext(); ) {
				Validator validator = (Validator) iter.next();
				Pattern pattern = Pattern.compile(validator.getValidatorPattern());
				validatorList.put(validator.getValidatorName(), pattern);
			}
		} catch(Exception ex) {
			// FIXME issue severe error message if this happens. 
			// (and it's a bug in our dtd or data file)
			ex.printStackTrace();
		}
	}
}
