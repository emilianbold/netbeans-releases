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

package org.netbeans.modules.websvc.rest.wadl.model;

import java.util.Collection;
import org.netbeans.modules.xml.xam.Nameable;

public interface Application extends Nameable<WadlComponent>, ReferenceableWadlComponent, WadlComponent {
    
    public static String TARGET_NAMESPACE_PROPERTY = "targetNamespace";
    public static String SCHEMA_NAMESPACE_PROPERTY = "schemaNamespace";
    public static String GRAMMARS_PROPERTY = "grammars";
    public static String RESOURCES_PROPERTY = "resources";
    public static String TYPES_PROPERTY = "types";
    public static String RESOURCE_TYPE_PROPERTY = "resource_type";
    public static String REPRESENTATION_PROPERTY = "representation";
    public static String FAULT_PROPERTY = "fault";
    public static String METHOD_PROPERTY = "method";

    public String getSchemaNamespacePrefix();

    public String getTargetNamespace();
    public void setTargetNamespace(String tns);
    
    public Collection<Grammars> getGrammars();
    public void addGrammars(Grammars grammars);
    void removeGrammars(Grammars importDefinition);

    public Collection<Resources> getResources();
    public void addResources(Resources resources);
    public void removeResources(Resources resources);
    
    public Collection<ResourceType> getResourceType();
    public void addResourceType(ResourceType r);
    public void removeResourceType(ResourceType r);
    
    public Collection<Method> getMethod();
    public void addMethod(Method m);
    public void removeMethod(Method m);

    public Collection<RepresentationType> getRepresentationType();
    public Collection<Representation> getRepresentation();
    public void addRepresentation(Representation rep);
    void removeRepresentation(Representation rep);
    
    public Collection<Fault> getFault();
    public void addFault(Fault rep);
    void removeFault(Fault rep);
}
