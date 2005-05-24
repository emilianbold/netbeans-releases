/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ParamMapping.java
 *
 * Created on January 29, 2004, 2:06 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.data;

/**
 *
 * @author Peter Williams
 */
public class ParamMapping implements Comparable {
	
	private PropertyParam param;
//	private String displayText;
	
	/** Creates a new instance of ParamMapping
	 *  This object does handle a null PropertyParam
	 */
	public ParamMapping(final PropertyParam pp) {
		param = pp;
	}

	/** equals() maps to PropertyParam.equals()
	 *
	 * @return true/false based on whether the embedded property param objects
	 *  compare as equal.
	 */
	public boolean equals(Object obj) {
		boolean result = false;
		
		// This implementation is made more difficult due to the allowing of the
		// param member to be null (to represent a null entry in the combobox).
		// 
		if(obj instanceof ParamMapping) {
			if(this == obj) {
				result = true;
			} else {
				ParamMapping targetMapping = (ParamMapping) obj;
				PropertyParam targetParam = targetMapping.getParam();
				if(param != null) {
					if(targetParam != null) {
						result = param.getParamName().equals(targetParam.getParamName());
					}
				} else if(targetParam == null) {
					result = true;
				}
			}
		}
		return result;
	}
	
	/** hashCode() maps to PropertyParam.hashCode()
	 *
	 * @return the hashcode
	 */
	public int hashCode() {
		int hashcode = 509; // use this prime for nulls.
		if(param != null) {
			hashcode = param.getParamName().hashCode();
		}
		return hashcode;
	}
	
	/** A more readable display string
	 *
	 * @return A descriptive string
	 */
	public String toString() {
		String result = "";
		if(param != null) {
			result = param.getParamName();
		}
		return result;
	}

	/** The property param
	 *
	 * @return the property param this is a mapping for
	 */
	public PropertyParam getParam() {
		return param;
	}
	
	/** For sorted collections.  We compare the string representations of the 
	 *  embedded property param.
	 *
	 * @param obj the ParamMapping to compare to
	 * @return result of comparison (negative, 0, or positive depending on match)
	 */
	public int compareTo(Object obj) {
		int result = -1;
		
		// This implementation is made more difficult due to the allowing of the
		// param member to be null (to represent a null entry in the combobox).
		// 
		// If param is null, that entry is considered less than any other param
		// type so that it's always at the top of the list.
		//
		if(obj instanceof ParamMapping) {
			if(this == obj) {
				result = 0;
			} else {
				ParamMapping targetMapping = (ParamMapping) obj;
				PropertyParam targetParam = targetMapping.getParam();
				if(param != null) {
					if(targetParam != null) {
						result = param.getParamName().compareTo(targetParam.getParamName());
					} else {
						result = 1;
					}
				} else if(targetParam == null) {
					result = 0;
				}
			}
		}
		
		return result;
	}	
}
