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
