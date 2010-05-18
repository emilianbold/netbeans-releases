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
 * LibProperties.java
 *
 */

package org.netbeans.test.umllib.util;

import java.io.PrintStream;
import java.lang.Enum;
import java.util.HashMap;
import java.util.Iterator;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.ExpandedElementTypes;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.SetName;
import org.netbeans.test.umllib.namers.LifelineNamer;

/**
 *
 * @author Alexei Mokeev
 */
public class LibProperties {
    private static LibProperties properties = null;;
    private HashMap<Enum,String> tools = null;
    private HashMap<Enum, SetName> namers = null;
    private HashMap<Enum,String> defNames = null;
    public SetName DEFAULT_NAMER = null;
    public SetName LABELS_NAMER = null;
    public SetName LIFELINE_NAMER = null;
    
    //
    public final String COMMON_NEW_ELEMENT_NAME="Unnamed";
   
    
    /** Creates a new instance of LibProperties */
    protected LibProperties() {
        tools  = new HashMap<Enum, String>();
        namers  = new HashMap<Enum, SetName>();
        defNames = new HashMap<Enum, String>();
        DEFAULT_NAMER = new DiagramElementOperator.DefaultNamer();
        LABELS_NAMER  = new DiagramElementOperator.LabelsNamer();
        LIFELINE_NAMER = new LifelineNamer();
        
        initDefaultToolNames();
        initDefaultNamers();
        initDefaultNames();
    }
    
    /**
     * 
     * @return 
     */
    public static LibProperties getProperties() {
        if(properties == null) {
            properties = new LibProperties();
        }
        return properties;
    }
    
    /**
     * 
     * @param element 
     * @return 
     */
    public static String getCurrentToolName(Enum element) {
        return getProperties().getToolName(element);
    }
    
    /**
     * 
     * @param element 
     * @return 
     */
    public static SetName getCurrentNamer(Enum element) {
        return getProperties().getNamer(element);
    }
    
    /**
     * 
     * @param element 
     * @return 
     */
    public static String getCurrentDefaultName(Enum element) {
        return getProperties().getDefaultName(element);
    }
    
    
    protected void initDefaultToolNames()  {
        setToolName(ElementTypes.CLASS,"Class");
        setToolName(ElementTypes.INTERFACE,"Interface");
        setToolName(ElementTypes.ARTIFACT,"Artifact");
        setToolName(ElementTypes.ALIASED,"Aliased");
        setToolName(ElementTypes.COMPONENT,"Component");
        setToolName(ElementTypes.PACKAGE, "Package");
        setToolName(ElementTypes.DEPLOYMENT_SPECIFICATION, "Deployment Specification");
        setToolName(ElementTypes.BOUNDARY_CLASS, "Boundary Class");
        setToolName(ElementTypes.CONTROL_CLASS, "Control Class");
        setToolName(ElementTypes.ENTITY_CLASS, "Entity Class");
        setToolName(ElementTypes.ENUMERATION, "Enumeration");
        setToolName(ElementTypes.ASSEMBLY_CONNECTOR, "Assembly Connector");
        setToolName(ElementTypes.NODE,"Node");
        setToolName(ElementTypes.DATATYPE,"Datatype");
        setToolName(ElementTypes.UTILITY_CLASS, "Utility Class");
        setToolName(ElementTypes.ACTOR_ROLE, "Actor Role");
        setToolName(ElementTypes.USE_CASE, "Use Case");
        setToolName(ElementTypes.ACTOR_LIFELINE, "Actor");
        setToolName(ElementTypes.LIFELINE, "Lifeline");
        setToolName(ElementTypes.ACTOR, "Actor");
        setToolName(ElementTypes.COLLABORATION_LIFELINE, "Collaboration Lifeline");
        setToolName(ElementTypes.COMMENT, "Comment");
        setToolName(ElementTypes.LINK_COMMENT, "Link Comment");
        setToolName(ElementTypes.INVOCATION, "Invocation");
        setToolName(ElementTypes.DECISION, "Decision");
        setToolName(ElementTypes.VERTICAL_FORK, "Vertical Fork");
        setToolName(ElementTypes.HORIZONTAL_FORK, "Horizontal Fork");
        setToolName(ElementTypes.ACTIVITY_GROUP, "Activity Group");
        setToolName(ElementTypes.INITIAL_NODE, "Initial Node");
        setToolName(ElementTypes.ACTIVITY_FINAL_NODE, "Activity Final Node");
        setToolName(ElementTypes.FLOW_FINAL, "Flow Final");
        setToolName(ElementTypes.PARAMETER_USAGE, "Parameter Usage");
        setToolName(ElementTypes.DATA_STORE, "Data Store");
        setToolName(ElementTypes.SIGNAL, "Signal");
        setToolName(ElementTypes.PARTITION, "Partition");
        setToolName(ElementTypes.CREATE_MESSAGE, "Create Message");
        setToolName(ElementTypes.COMBINED_FRAGMENT, "Combined Fragment");
        setToolName(ElementTypes.TEMPLATE_CLASS, "Template Class");
        setToolName(ElementTypes.DERIVATION_CLASSIFIER, "Derivation Classifier");
        setToolName(ElementTypes.DESIGN_PATTERN, "Design Pattern");
        setToolName(ElementTypes.ROLE, "Role");
        setToolName(ElementTypes.INTERFACE_ROLE, "Interface Role");
        setToolName(ElementTypes.CLASS_ROLE, "Class Role");
        setToolName(ElementTypes.USE_CASE_ROLE, "Use Case Role"); 
        setToolName(ElementTypes.ROLE_BINDING, "Role Binding");   
        
        setToolName(ElementTypes.SUBMACHINE_STATE, "Submachine State");   
        setToolName(ElementTypes.JUNCTION_POINT_STATE, "Junction State");   
        setToolName(ElementTypes.ENTRY_POINT_STATE, "Entry Point State");   
        setToolName(ElementTypes.DEEP_HISTORY_STATE, "Deep History State");   
        setToolName(ElementTypes.SHALLOW_HISTORY_STATE, "Shallow History State");   
        setToolName(ElementTypes.CHOICE_PSEUDO_STATE, "Choice Pseudo State");   
        setToolName(ElementTypes.COMPOSITE_STATE, "Composite State");   
        setToolName(ElementTypes.HORIZONTAL_JOIN_MERGE, "Horizontal Join/Merge");   
        setToolName(ElementTypes.VERTICAL_JOIN_MERGE, "Vertical Join/Merge");   
        setToolName(ElementTypes.ABORTED_FINAL_STATE, "Aborted Final State");   
        setToolName(ElementTypes.FINAL_STATE, "Final State");   
        setToolName(ElementTypes.INITIAL_STATE, "Initial State");   
        setToolName(ElementTypes.SIMPLE_STATE, "Simple State");   

        //expanded
        setToolName(ExpandedElementTypes.CLASS,"Class");
        setToolName(ExpandedElementTypes.INTERFACE,"Interface");
        setToolName(ExpandedElementTypes.ARTIFACT,"Artifact");
        setToolName(ExpandedElementTypes.ALIASED,"Aliased");
        setToolName(ExpandedElementTypes.COMPONENT,"Component");
        setToolName(ExpandedElementTypes.PACKAGE, "Package");
        //setToolName(ExpandedElementTypes.DEPLOYMENT_SPECIFICATION, "Deployment Specification");
        setToolName(ExpandedElementTypes.BOUNDARY_CLASS, "Boundary Class");
        setToolName(ExpandedElementTypes.CONTROL_CLASS, "Control Class");
        setToolName(ExpandedElementTypes.ENTITY_CLASS, "Entity Class");
        setToolName(ExpandedElementTypes.ENUMERATION, "Enumeration");
        setToolName(ExpandedElementTypes.ASSEMBLY_CONNECTOR, "Assembly Connector");
        setToolName(ExpandedElementTypes.NODE,"Node");
        setToolName(ExpandedElementTypes.DATATYPE,"Datatype");
        setToolName(ExpandedElementTypes.UTILITY_CLASS, "Utility Class");
        setToolName(ExpandedElementTypes.ACTOR_ROLE, "Actor Role");
        setToolName(ExpandedElementTypes.USE_CASE, "Use Case");
        setToolName(ExpandedElementTypes.ACTOR_LIFELINE, "Actor");
        setToolName(ExpandedElementTypes.LIFELINE, "Lifeline");
        setToolName(ExpandedElementTypes.ACTOR, "Actor");
        setToolName(ExpandedElementTypes.COLLABORATION_LIFELINE, "Collaboration Lifeline");
        setToolName(ExpandedElementTypes.COMMENT, "Comment");
        setToolName(ExpandedElementTypes.LINK_COMMENT, "Link Comment");
        //setToolName(ExpandedElementTypes.INVOCATION, "Invocation");
        //setToolName(ExpandedElementTypes.DECISION, "Decision");
        //setToolName(ExpandedElementTypes.VERTICAL_FORK, "Vertical Fork");
        //setToolName(ExpandedElementTypes.HORIZONTAL_FORK, "Horizontal Fork");
        setToolName(ExpandedElementTypes.ACTIVITY_GROUP, "Activity Group");
        //setToolName(ExpandedElementTypes.INITIAL_NODE, "Initial Node");
        //setToolName(ExpandedElementTypes.ACTIVITY_FINAL_NODE, "Activity Final Node");
        //setToolName(ExpandedElementTypes.FLOW_FINAL, "Flow Final");
        //setToolName(ExpandedElementTypes.PARAMETER_USAGE, "Parameter Usage");
        //setToolName(ExpandedElementTypes.DATA_STORE, "Data Store");
        //setToolName(ExpandedElementTypes.SIGNAL, "Signal");
        //setToolName(ExpandedElementTypes.PARTITION, "Partition");
        //setToolName(ExpandedElementTypes.CREATE_MESSAGE, "Create Message");
        //setToolName(ExpandedElementTypes.COMBINED_FRAGMENT, "Combined Fragment");
        setToolName(ExpandedElementTypes.TEMPLATE_CLASS, "Template Class");
        setToolName(ExpandedElementTypes.DERIVATION_CLASSIFIER, "Derivation Classifier");
        setToolName(ExpandedElementTypes.DESIGN_PATTERN, "Design Pattern");
        setToolName(ExpandedElementTypes.ROLE, "Role");
        setToolName(ExpandedElementTypes.INTERFACE_ROLE, "Interface Role");
        setToolName(ExpandedElementTypes.CLASS_ROLE, "Class Role");
        setToolName(ExpandedElementTypes.USE_CASE_ROLE, "Use Case Role"); 
        setToolName(ExpandedElementTypes.ROLE_BINDING, "Role Binding");   
        
        //setToolName(ExpandedElementTypes.SUBMACHINE_STATE, "Submachine State");   
        //setToolName(ExpandedElementTypes.JUNCTION_POINT_STATE, "Junction State");   
        //setToolName(ExpandedElementTypes.ENTRY_POINT_STATE, "Entry Point State");   
        //setToolName(ExpandedElementTypes.DEEP_HISTORY_STATE, "Deep History State");   
        //setToolName(ExpandedElementTypes.SHALLOW_HISTORY_STATE, "Shallow History State");   
        //setToolName(ExpandedElementTypes.CHOICE_PSEUDO_STATE, "Choice Pseudo State");   
        //setToolName(ExpandedElementTypes.COMPOSITE_STATE, "Composite State");   
        //setToolName(ExpandedElementTypes.HORIZONTAL_JOIN_MERGE, "Horizontal Join/Merge");   
        //setToolName(ExpandedElementTypes.VERTICAL_JOIN_MERGE, "Vertical Join/Merge");   
        //setToolName(ExpandedElementTypes.ABORTED_FINAL_STATE, "Aborted Final State");   
        //setToolName(ExpandedElementTypes.FINAL_STATE, "Final State");   
        //setToolName(ExpandedElementTypes.INITIAL_STATE, "Initial State");   
        //setToolName(ExpandedElementTypes.SIMPLE_STATE, "Simple State");   
        //
        
        
        setToolName(LinkTypes.DEPENDENCY, "Dependency");
        setToolName(LinkTypes.PERMISSION, "Permission");
        setToolName(LinkTypes.ASSOCIATION, "Association");
        setToolName(LinkTypes.NAVIGABLE_ASSOCIATION, "Navigable Association");
        setToolName(LinkTypes.COMPOSITION, "Composition");
        setToolName(LinkTypes.NAVIGABLE_COMPOSITION, "Navigable Composition");
        setToolName(LinkTypes.AGGREGATION, "Aggregation");
        setToolName(LinkTypes.NAVIGABLE_AGGREGATION, "Navigable Aggregation");
        setToolName(LinkTypes.ASSOCIATION_CLASS, "Association Class");
        setToolName(LinkTypes.GENERALIZATION, "Generalization");
        setToolName(LinkTypes.IMPLEMENTATION, "Implementation");
        setToolName(LinkTypes.NESTED_LINK, "Nested Link");
        setToolName(LinkTypes.REALIZE, "Realize");
        setToolName(LinkTypes.USAGE, "Usage");
        setToolName(LinkTypes.ABSTRACTION, "Abstraction");
        setToolName(LinkTypes.PACKAGE, "Nested Link");
        setToolName(LinkTypes.DERIVATION_EDGE, "Derivation Edge");
        setToolName(LinkTypes.DELEGATE, "Delegate");
        setToolName(LinkTypes.ASSEMBLY, "Assembly Connector");
        setToolName(LinkTypes.ACTIVITY_EDGE, "Activity Edge");
        setToolName(LinkTypes.CONNECTOR, "Connector");
        setToolName(LinkTypes.SYNC_MESSAGE, "Synchronous Message");
        setToolName(LinkTypes.ASYNC_MESSAGE, "Asynchronous Message");
        setToolName(LinkTypes.MESSAGE_TO_SELF, "Message To Self");
        setToolName(LinkTypes.CREATE_MESSAGE, "Create Message");
        setToolName(LinkTypes.ROLE, "Role Binding"); 
        setToolName(LinkTypes.ROLE_BINDING, "Role Binding"); 
        setToolName(LinkTypes.INCLUDE, "Include");
        setToolName(LinkTypes.EXTEND, "Extend");
        setToolName(LinkTypes.COMMENT, "Link Comment");
    }
    
    
    protected void initDefaultNamers() {
        setNamer(ElementTypes.DECISION, LABELS_NAMER);
        setNamer(ElementTypes.VERTICAL_FORK, LABELS_NAMER);
        setNamer(ElementTypes.HORIZONTAL_FORK, LABELS_NAMER);
        setNamer(ElementTypes.INITIAL_NODE, LABELS_NAMER);
        setNamer(ElementTypes.ACTIVITY_FINAL_NODE, LABELS_NAMER);
        setNamer(ElementTypes.FLOW_FINAL, LABELS_NAMER);
        setNamer(ElementTypes.LIFELINE, LIFELINE_NAMER);
        setNamer(ElementTypes.COLLABORATION_LIFELINE, LIFELINE_NAMER);
        setNamer(ElementTypes.ACTOR_LIFELINE, LIFELINE_NAMER);
        
    }
    //
    protected void initDefaultNames() {
        setDefaultName(ElementTypes.COMMENT, "");
        setDefaultName(ElementTypes.LINK_COMMENT, "");
        setDefaultName(ElementTypes.PORT, "");

        setDefaultName(ExpandedElementTypes.COMMENT, "");
        setDefaultName(ExpandedElementTypes.LINK_COMMENT, "");
        setDefaultName(ExpandedElementTypes.PORT, "");
    }
    
    
    /**
     * 
     * @param element 
     * @param toolTip 
     * @return 
     */
    public String setToolName(Enum element, String toolTip) {
        return (String)tools.put(element, toolTip);
    }
    
    /**
     * 
     * @param element 
     * @return 
     */
    public String getToolName(Enum element) {
        return (String)tools.get(element);
    }
    
    /**
     * 
     * @param element 
     * @return 
     */
    public SetName getNamer(Enum element) {
        SetName namer = null;
        try {
            namer = namers.get(element);
            if(namer == null) {
                namer = DEFAULT_NAMER;
            }
        }catch(Exception e){};
        return namer;
    }
    
    /**
     * 
     * @param element 
     * @param obj 
     * @return 
     */
    public SetName setNamer(Enum element, SetName obj) {
        return (SetName)namers.put(element, obj);
    }
    
    private void setDefaultName(Enum element,String defName)
    {
        defNames.put(element, defName);
    }
    
    /**
     *  get Default or initial name with standart uml options
     * @param element 
     * @return 
     */
    public String getDefaultName(Enum element)
    {
        String ret=null;
        try {
            ret = defNames.get(element);
            if(ret == null) {
                if(element instanceof LinkTypes)ret="";
                else ret = COMMON_NEW_ELEMENT_NAME;
            }
        }catch(Exception e){};
        return ret;
        
    }
}
