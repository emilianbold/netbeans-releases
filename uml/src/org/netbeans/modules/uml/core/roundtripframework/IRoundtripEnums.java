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


package org.netbeans.modules.uml.core.roundtripframework;

/**
 * @author sumitabhk
 *
 */
public interface IRoundtripEnums
{
	//change kinds
	public static int CT_MODIFY	= 0;
	public static int CT_DELETE	= CT_MODIFY + 1;
	public static int CT_CREATE	= CT_DELETE + 1;
	public static int CT_NONE	= CT_CREATE + 1;
	
	//Roundtrip Element kind
	public static int RCT_CLASS	= 0;
	public static int RCT_ATTRIBUTE	= RCT_CLASS + 1;
	public static int RCT_OPERATION	= RCT_ATTRIBUTE + 1;
	public static int RCT_PACKAGE	= RCT_OPERATION + 1;
	public static int RCT_RELATION	= RCT_PACKAGE + 1;
	public static int RCT_PARAMETER	= RCT_RELATION + 1;
	public static int RCT_INTERFACE	= RCT_PARAMETER + 1;
	public static int RCT_NONE	= RCT_INTERFACE + 1;
	public static int RCT_NAVIGABLE_END_ATTRIBUTE	= RCT_NONE + 1;
	
	//RequestDetailKind
	public static int RDT_NONE	= 0;
	public static int RDT_DOCUMENTATION_MODIFIED	= RDT_NONE + 1;
	public static int RDT_ELEMENT_DELETED	= RDT_DOCUMENTATION_MODIFIED + 1;
	public static int RDT_NAME_MODIFIED	= RDT_ELEMENT_DELETED + 1;
	public static int RDT_VISIBILITY_MODIFIED	= RDT_NAME_MODIFIED + 1;
	public static int RDT_ELEMENT_ADDED_TO_NAMESPACE	= RDT_VISIBILITY_MODIFIED + 1;
	public static int RDT_RELATION_VALIDATE	= RDT_ELEMENT_ADDED_TO_NAMESPACE + 1;
	public static int RDT_RELATION_MODIFIED	= RDT_RELATION_VALIDATE + 1;
	public static int RDT_RELATION_DELETED	= RDT_RELATION_MODIFIED + 1;
	public static int RDT_ATTRIBUTE_DEFAULT_MODIFIED	= RDT_RELATION_DELETED + 1;
	public static int RDT_ATTRIBUTE_DEFAULT_BODY_MODIFIED	= RDT_ATTRIBUTE_DEFAULT_MODIFIED + 1;
	public static int RDT_ATTRIBUTE_DEFAULT_LANGUAGE_MODIFIED	= RDT_ATTRIBUTE_DEFAULT_BODY_MODIFIED + 1;
	public static int RDT_CONCURRENCY_MODIFIED	= RDT_ATTRIBUTE_DEFAULT_LANGUAGE_MODIFIED + 1;
	public static int RDT_SIGNAL_ADDED	= RDT_CONCURRENCY_MODIFIED + 1;
	public static int RDT_SIGNAL_REMOVED	= RDT_SIGNAL_ADDED + 1;
	public static int RDT_PARAMETER_ADDED	= RDT_SIGNAL_REMOVED + 1;
	public static int RDT_PARAMETER_REMOVED	= RDT_PARAMETER_ADDED + 1;
	public static int RDT_ABSTRACT_MODIFIED	= RDT_PARAMETER_REMOVED + 1;
	public static int RDT_FEATURE_ADDED	= RDT_ABSTRACT_MODIFIED + 1;
	public static int RDT_FEATURE_REMOVED	= RDT_FEATURE_ADDED + 1;
	public static int RDT_STATIC_MODIFIED	= RDT_FEATURE_REMOVED + 1;
	public static int RDT_CONDITION_ADDED	= RDT_STATIC_MODIFIED + 1;
	public static int RDT_CONDITION_REMOVED	= RDT_CONDITION_ADDED + 1;
	public static int RDT_QUERY_MODIFIED	= RDT_CONDITION_REMOVED + 1;
	public static int RDT_PARAMETER_DEFAULT_MODIFIED	= RDT_QUERY_MODIFIED + 1;
	public static int RDT_PARAMETER_DEFAULT_BODY_MODIFIED	= RDT_PARAMETER_DEFAULT_MODIFIED + 1;
	public static int RDT_PARAMETER_DEFAULT_LANGUAGE_MODIFIED	= RDT_PARAMETER_DEFAULT_BODY_MODIFIED + 1;
	public static int RDT_PARAMETER_DIRECTION_MODIFIED	= RDT_PARAMETER_DEFAULT_LANGUAGE_MODIFIED + 1;
	public static int RDT_CHANGEABILITY_MODIFIED	= RDT_PARAMETER_DIRECTION_MODIFIED + 1;
	public static int RDT_MULTIPLICITY_MODIFIED	= RDT_CHANGEABILITY_MODIFIED + 1;
	public static int RDT_TYPE_MODIFIED	= RDT_MULTIPLICITY_MODIFIED + 1;
	public static int RDT_LOWER_MODIFIED	= RDT_TYPE_MODIFIED + 1;
	public static int RDT_UPPER_MODIFIED	= RDT_LOWER_MODIFIED + 1;
	public static int RDT_RANGE_ADDED	= RDT_UPPER_MODIFIED + 1;
	public static int RDT_RANGE_REMOVED	= RDT_RANGE_ADDED + 1;
	public static int RDT_ORDER_MODIFIED	= RDT_RANGE_REMOVED + 1;
	public static int RDT_PACKAGE_NAME_MODIFIED	= RDT_ORDER_MODIFIED + 1;
	public static int RDT_TRANSIENT_MODIFIED	= RDT_PACKAGE_NAME_MODIFIED + 1;
	public static int RDT_NATIVE_MODIFIED	= RDT_TRANSIENT_MODIFIED + 1;
	public static int RDT_VOLATILE_MODIFIED	= RDT_NATIVE_MODIFIED + 1;
	public static int RDT_LEAF_MODIFIED	= RDT_VOLATILE_MODIFIED + 1;
	public static int RDT_RELATION_END_MODIFIED	= RDT_LEAF_MODIFIED + 1;
	public static int RDT_RELATION_END_ADDED	= RDT_RELATION_END_MODIFIED + 1;
	public static int RDT_RELATION_END_REMOVED	= RDT_RELATION_END_ADDED + 1;
	public static int RDT_DEPENDENCY_ADDED	= RDT_RELATION_END_REMOVED + 1;
	public static int RDT_DEPENDENCY_REMOVED	= RDT_DEPENDENCY_ADDED + 1;
	public static int RDT_ASSOCIATION_END_MODIFIED	= RDT_DEPENDENCY_REMOVED + 1;
	public static int RDT_ASSOCIATION_END_ADDED	= RDT_ASSOCIATION_END_MODIFIED + 1;
	public static int RDT_ASSOCIATION_END_REMOVED	= RDT_ASSOCIATION_END_ADDED + 1;
	public static int RDT_RELATION_CREATED	= RDT_ASSOCIATION_END_REMOVED + 1;
	public static int RDT_FEATURE_MOVED	= RDT_RELATION_CREATED + 1;
	public static int RDT_FEATURE_DUPLICATED	= RDT_FEATURE_MOVED + 1;
	public static int RDT_NAMESPACE_MODIFIED	= RDT_FEATURE_DUPLICATED + 1;
	public static int RDT_CHANGED_NAMESPACE	= RDT_NAMESPACE_MODIFIED + 1;
	public static int RDT_NAMESPACE_MOVED	= RDT_CHANGED_NAMESPACE + 1;
	public static int RDT_FINAL_MODIFIED	= RDT_NAMESPACE_MOVED + 1;
	public static int RDT_STRICTFP_MODIFIED	= RDT_FINAL_MODIFIED + 1;
	public static int RDT_MULTIPLE_PARAMETER_TYPE_MODIFIED	= RDT_STRICTFP_MODIFIED + 1;
	public static int RDT_TRANSFORM	= RDT_MULTIPLE_PARAMETER_TYPE_MODIFIED + 1;
	public static int RDT_EXCEPTION_ADDED	= RDT_TRANSFORM + 1;
	public static int RDT_EXCEPTION_REMOVED	= RDT_EXCEPTION_ADDED + 1;
	public static int RDT_SIGNATURE_CHANGED	= RDT_EXCEPTION_REMOVED + 1;
	public static int RDT_SOURCE_DIR_CHANGED	= RDT_SIGNATURE_CHANGED + 1;
	public static int RDT_OPERATION_PROPERTY_CHANGED	= RDT_SOURCE_DIR_CHANGED + 1;
	
}


