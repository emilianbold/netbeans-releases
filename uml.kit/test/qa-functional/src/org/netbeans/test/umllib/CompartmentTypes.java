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



package org.netbeans.test.umllib;


/**
 * Compartment classes and types are connected here
 */
public enum CompartmentTypes {

    NAME_LIST_COMPARTMENT("org.netbeans.modules.uml.diagrams.nodes.?INameListCompartment"),
    NAME_COMPARTMENT("org.netbeans.modules.uml.diagrams.nodes.UMLNameWidget"),
    ATTRIBUTE_LIST_COMPARTMENT("org.netbeans.modules.uml.diagrams.nodes.ElementListWidget"),
    ATTRIBUTE_COMPARTMENT("org.netbeans.modules.uml.diagrams.nodes.AttributeWidget"),
    EXTENSION_POINTS_LIST_COMPARTMENT("org.netbeans.modules.uml.diagrams.nodes.?IADExtensionPointListCompartment"),
    EXTENSION_POINT_COMPARTMENT("org.netbeans.modules.uml.diagrams.nodes.?ETExtensionPointCompartment"),
    OPERATION_LIST_COMPARTMENT("org.netbeans.modules.uml.diagrams.nodes.ElementListWidget"),
    OPERATION_COMPARTMENT("org.netbeans.modules.uml.diagrams.nodes.OperationWidget"),
    SEQUENCE_LIFELINE_COMPARTMENT("org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineLineWidget"),
    SEQUENCE_LIFELINE_NAME_COMPARTMENT("org.netbeans.modules.uml.diagrams.nodes.sqd.LifelinBoxWidget"),
    PACKAGE_IMPORT_COMPARTMENT("org.netbeans.modules.uml.diagrams.nodes.PackageCompartment"),
    TEMPLATE_PARAMETERS_COMPARTMENT("org.netbeans.modules.uml.diagrams.nodes.?ITemplateParametersCompartment"),
    ACTIVITY_PARTITIONS_COMPARTMENT("org.netbeans.modules.uml.diagrams.nodes.?ETActivityPartitionsCompartment"),
    STEREOTYPE_COMPARTMENT("org.netbeans.modules.uml.diagrams.nodes.?IADStereotypeCompartment");
    
    private String val = "";
    
    private CompartmentTypes(String val){
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
