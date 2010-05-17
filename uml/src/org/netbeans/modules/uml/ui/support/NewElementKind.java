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


/*
 *
 * Created on Jul 1, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support;

/**
 *
 * @author Trey Spiva
 */
public interface NewElementKind
{
	public final static int NEK_NONE = 0;
	public final static int NEK_ACTOR = 1;
	public final static int NEK_ATTRIBUTE = 2;
	public final static int NEK_CLASS = 3;
	public final static int NEK_INTERFACE = 4;
	public final static int NEK_OPERATION = 5;
	public final static int NEK_USE_CASE = 6;
	public final static int NEK_DATATYPE = 7;
	public final static int NEK_ALIASED_TYPE = 8;
	public final static int NEK_ARTIFACT = 9;
	
	public final static int NEK_COLLAB_LIFELINE = 10;
	public final static int NEK_ENUMERATION = 11;
	public final static int NEK_NODE = 12;
	public final static int NEK_UTILITY_CLASS = 13;
	public final static int NEK_INVOCATION_NODE = 14;
	public final static int NEK_ACTIVITY_GROUP = 15;
	public final static int NEK_INITIAL_NODE = 16;
	public final static int NEK_ACTIVITY_FINAL_NODE = 17;
	public final static int NEK_ACTIVITY_FLOW_FINAL_NODE = 18;
	public final static int NEK_DECISION_MERGE_NODE = 19;
	
	public final static int NEK_ABORTED_FINAL_STATE = 20;
	public final static int NEK_COMPOSITE_STATE = 21;
	public final static int NEK_COMPONENT = 22;
	public final static int NEK_DATA_STORE_NODE = 23;
	public final static int NEK_DERIVATION_CLASSIFIER = 24;
	public final static int NEK_ENUMERATION_LITERAL = 25;
	public final static int NEK_FINAL_STATE = 26;
	public final static int NEK_FORK_STATE = 27;
	public final static int NEK_INITIAL_STATE = 28;
	public final static int NEK_JOIN_FORK_NODE = 29;
	
	public final static int NEK_JOIN_STATE = 30;
	public final static int NEK_JUNCTION_STATE = 31;
	public final static int NEK_LIFELINE = 32;
	public final static int NEK_PARAMETER_USAGE_NODE = 33;
	public final static int NEK_SIGNAL_NODE = 34;
	public final static int NEK_SIMPLE_STATE = 35;
	public final static int NEK_STATE = 36;
	public final static int NEK_STOP_STATE = 37;
	public final static int NEK_USE_CASE_DETAIL = 38;
	public final static int NEK_COLLABORATION = 39;
	
	
	// CR#6263225 cvc - added arrays to make the maintenance of 
	//  adding/changing/removing elements much easier

	
	public final static Integer[] ELEMENT_NUMBERS = 
	{
		new Integer(NEK_NONE),
		new Integer(NEK_ACTOR),
		new Integer(NEK_ATTRIBUTE),
		new Integer(NEK_CLASS),
		new Integer(NEK_INTERFACE),
		new Integer(NEK_OPERATION),
		new Integer(NEK_USE_CASE),
		new Integer(NEK_DATATYPE),
		new Integer(NEK_ALIASED_TYPE),
		new Integer(NEK_ARTIFACT),
		
		new Integer(NEK_COLLAB_LIFELINE),
		new Integer(NEK_ENUMERATION),
		new Integer(NEK_NODE),
		new Integer(NEK_UTILITY_CLASS),
		new Integer(NEK_INVOCATION_NODE),
		new Integer(NEK_ACTIVITY_GROUP),
		new Integer(NEK_INITIAL_NODE),
		new Integer(NEK_ACTIVITY_FINAL_NODE),
		new Integer(NEK_ACTIVITY_FLOW_FINAL_NODE),
		new Integer(NEK_DECISION_MERGE_NODE),
		
		new Integer(NEK_ABORTED_FINAL_STATE),
		new Integer(NEK_COMPOSITE_STATE),
		new Integer(NEK_COMPONENT),
		new Integer(NEK_DATA_STORE_NODE),
		new Integer(NEK_DERIVATION_CLASSIFIER),
		new Integer(NEK_ENUMERATION_LITERAL),
		new Integer(NEK_FINAL_STATE),
		new Integer(NEK_FORK_STATE),
		new Integer(NEK_INITIAL_STATE),
		new Integer(NEK_JOIN_FORK_NODE),
		
		new Integer(NEK_JOIN_STATE),
		new Integer(NEK_JUNCTION_STATE),
		new Integer(NEK_LIFELINE),
		new Integer(NEK_PARAMETER_USAGE_NODE),
		new Integer(NEK_SIGNAL_NODE),
		new Integer(NEK_SIMPLE_STATE),
		new Integer(NEK_STATE),
		new Integer(NEK_STOP_STATE),
		new Integer(NEK_USE_CASE_DETAIL),	
		new Integer(NEK_COLLABORATION)
	};
	
	
	// these "no spaces" names need to match their 
	//  display names that have spaces
	// example: "Abc Xyz" to "AbcXyz"
	
	public final static String[] ELEMENT_NAMES =
	{
		"None",
		"Actor",
		"Attribute",
		"Class",
		"Interface",
		"Operation",
		"UseCase",
		"DataType",
		"AliasedType",
		"Artifact",
		
		"CollaborationLifeline",
		"Enumeration",
		"Node",
		"UtilityClass",
		"InvocationNode",
		"ActivityPartition",
		"InitialNode",
		"ActivityFinalNode",
		"FlowFinalNode",
		"DecisionMergeNode",
		
		"AbortedFinalState",
		"CompositeState",
		"Component",
		"DataStoreNode",
		"DerivationClassifier",
		"EnumerationLiteral",
		"FinalState",
		"ForkState",
		"InitialState",
		"JoinForkNode",
		
		"JoinState",
		"JunctionState",
		"Lifeline",
		"ParameterUsageNode",
		"SignalNode",
		"SimpleState",
		"State",
		"StopState",
		"UseCaseDetail",
		"Collaboration"
	};
}
