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


/**
 * in progress of converting from element types
 * it seems most expanded types are the same as types exept role types
 */
package org.netbeans.test.umllib;
/**
 * @author psb
 */
public enum ExpandedElementTypes {
    /*
          string passed to constructor is {elementGraphObject}.getPresentationElement().getFirstSubject().getExpandedElementType() string
     */
   COMPONENT("Component"),
        CLASS("Class"),
        TEMPLATE_CLASS("Class"),
        DERIVATION_CLASSIFIER("DerivationClassifier"),
        INTERFACE("Interface"),
        PACKAGE("Package"),
        //DEPLOYMENT_SPECIFICATION("DeploymentSpecification"),
        BOUNDARY_CLASS("Class"),
        CONTROL_CLASS("Class"),
        ENTITY_CLASS("Class"),
        ENUMERATION("Enumeration"),
        ASSEMBLY_CONNECTOR("Interface"),
        NODE("Node"),
        DATATYPE("DataType"),
        ARTIFACT("Artifact"),
        UTILITY_CLASS("Class"),
        USE_CASE("UseCase"),
        // represents Actor lifeline symbol on interaction diagrams (COD, SQD)
        ACTOR_LIFELINE("Lifeline"),
        // represents lifeline symbol on interaction diagrams (COD, SQD)
        LIFELINE("Lifeline"),
        // represents Actor symbol on all non-interaction diagrams (CLD, USD, comp)
        ACTOR("Actor"),
        // represents Collaboration Lifiline symbol on CLD
        COLLABORATION_LIFELINE("Lifeline"), 
        COMMENT("Comment"),
        LINK_COMMENT("Comment"),
        //INVOCATION("InvocationNode"),
        //DECISION("DecisionMergeNode"),
        //VERTICAL_FORK("JoinForkNode"),
        //HORIZONTAL_FORK("JoinForkNode"),
        ACTIVITY_GROUP("ComplexActivityGroup"),
        //INITIAL_NODE("InitialNode"),
        //ACTIVITY_FINAL_NODE("ActivityFinalNode"),
        //FLOW_FINAL("FlowFinalNode"),
        //PARAMETER_USAGE("ParameterUsageNode"),
        //DATA_STORE("DataStoreNode"),
        //SIGNAL("SignalNode"),
        //PARTITION("ActivityPartition"),
        //CREATE_MESSAGE("CreateMessage"),
        //COMBINED_FRAGMENT("CombinedFragment"),
        DESIGN_PATTERN("Collaboration"),
        ROLE("PartFacade_Class"),
        //component-specific
        PORT("Port"),
        // class diagram Palette specific
        ALIASED("AliasedType") ,
        INTERFACE_ROLE("PartFacade_Interface") ,
        ACTOR_ROLE("PartFacade_Actor"),
        CLASS_ROLE("PartFacade_Class"),
        USE_CASE_ROLE("PartFacade_UseCase"),
        ROLE_BINDING("PartFacade") ,
        

        //state diagram specific
        //SIMPLE_STATE("State"),
        //INITIAL_STATE("PseudoState"),
        //FINAL_STATE("FinalState"),
        //ABORTED_FINAL_STATE("FinalState"),
        //VERTICAL_JOIN_MERGE("PseudoState"),
        //HORIZONTAL_JOIN_MERGE("PseudoState"),
        //COMPOSITE_STATE("State"),
        //CHOICE_PSEUDO_STATE("PseudoState"),
        //SHALLOW_HISTORY_STATE("PseudoState"),
        //DEEP_HISTORY_STATE("PseudoState"),
        //ENTRY_POINT_STATE("PseudoState"),
        //JUNCTION_POINT_STATE("PseudoState"),
        //SUBMACHINE_STATE("State"),
        
        ANY("Any")
        ;
    
    private String val = "";
    
    private ExpandedElementTypes(String val){
        this.val = val;
    }
    
    /**
     * 
     * @return 
     */
    public String toString(){
        return val;
    }
    
}
