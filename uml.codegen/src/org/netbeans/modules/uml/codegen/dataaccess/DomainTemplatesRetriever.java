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
package org.netbeans.modules.uml.codegen.dataaccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.DomainObject;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.Family;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.Template;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.TemplateFamilies;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.TemplateFamiliesHandler;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.project.ProjectUtil;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.util.StringTokenizer2;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class DomainTemplatesRetriever 
{
    public final static String TEMPLATES_BASE_FOLDER = "Templates/UML/Code Generation"; // NOI18N
    
    private static HashMap<String, List<DomainTemplate>> codeGenTemplates = null;
    
    public static void clear()
    {
        codeGenTemplates = null;
    }
    
    public static void load(UMLProject project)
    {
        // get the enabled templates (domain objects) 
        // from project properties (from Project Customizer UI)
        List<String> projectTemplates = 
            project.getUMLProjectProperties().getCodeGenTemplatesArray();        

        // initialize to the size of the enabled templates
        codeGenTemplates = 
            new HashMap<String, List<DomainTemplate>>(projectTemplates.size());

        // get all of the available templates (from UML Options UI)
        TemplateFamiliesHandler dataHandler = 
            TemplateFamiliesHandler.getInstance(true);
        
        // TemplateFamilies templateFamilies = dataHandler.getTemplateFamilies();
        TemplateFamilies templateFamilies = dataHandler.getTemplateFamilies();
        
        if (templateFamilies == null)
            throw new IllegalStateException("No Template Family Data Found"); // NOI18N
        
        // Load the codeGenTemplates HashMap with the enabled templates which
        // are represented as CodeGenTemplate instances.
        // This outer loop iterates over the enabled templates 
        // (from Project Customizer UI) which is a subset
        // of the total number of available teamplate (from UML Options UI)
        for (String projectTemplate: projectTemplates)
        {
            // each enabled template is stored in the project properties as a
            // pipe (|) delimited list of familyName:domainName pairs. The pipes
            // are tokenized into a List for this loop to use
            String[] tokens = StringTokenizer2.tokenize(projectTemplate, ":");

            Family family = templateFamilies.getFamilyByName(tokens[0]);
            
            if (family == null)
                continue;
            
            DomainObject domainObject = family.getDomainByName(tokens[1]);
            
            if (domainObject == null)
                continue;
            
            String elementType = domainObject.getModelElement();
            String stereotype = domainObject.getStereotype();
            
            Template[] templateList = domainObject.getTemplate();
            List<DomainTemplate> templates = new ArrayList<DomainTemplate>();
            
            for (Template template: templateList)
            {
                String format = template.getFilenameFormat();
                String ext = template.getFileExtension();
                String path = template.getFolderPath();
                String file = template.getTemplateFile();

                // Each domain has a list of template file(s) (usually just 
                // one) that are needed to generate code for the particular 
                // model element; i.e. - a "Class" element with a stereotype 
                // of "form" might have a template file for its source code, 
                // and another template file for its .form (metadata) file 
                // (think Matisse GUI editor).
                // We need to create a CodeGenTemplate instance for each 
                // of these template files 
                // (see the CodeGenTemplate for more details)

                DomainTemplate cgt = new DomainTemplate(
                    "", format, ext, path, file);

                templates.add(cgt);
            }
            
            // the key to the hashmap will either be an element type or a
            // combination of element type and stereotype
            // i.e. - Class, or Class:foo
            
            String key = elementType;
            if (stereotype != null && stereotype.length() > 0)
                key += ":" + stereotype;
            
            // all templates, no matter which family they are in 
            // (Java, C++, etc.) will be added under this key
            if (key != null && templates != null & templates.size() > 0)
            {
                // if there are already templates added from another 
                // domain template, then add to the end of those
                if (codeGenTemplates.containsKey(key))
                {
                    List<DomainTemplate> curTemplates = codeGenTemplates.get(key);
                    curTemplates.addAll(templates);
                    codeGenTemplates.put(key, curTemplates);
                }

                else
                    codeGenTemplates.put(key, templates);
            }
        }
    }

    public static List<DomainTemplate> retrieveTemplates(INamedElement element)
    {
        if (codeGenTemplates == null || codeGenTemplates.size() == 0)
            load((UMLProject)ProjectUtil.findReferencingProject(element));
        
        String eleType = element.getElementType();
        ETList<String> stereotypes = element.getAppliedStereotypesAsString();
        
        // Interface elements are also marked with the stereotype <<interface>>
        // but we don't need the extra demarcation so remove it from the list
        // of stereotypes it may have.
        if (eleType.equals("Interface") && 
            stereotypes != null && stereotypes.size() > 0)
        {
            stereotypes.remove("interface");
        }
        
        // if the model element has no stereotypes, get enabled templates 
        // that do not have stereotypes
        // TODO: but is this the right thing to do? See stereotype loop below
        if (stereotypes == null || stereotypes.size() == 0)
        {
            return insertElementName(
                element.getName(), codeGenTemplates.get(eleType));
        }
        
        List<DomainTemplate> validTemplates = new ArrayList<DomainTemplate>();
        
        // an element can have multiple stereotypes, so we need to get all
        // templates that match all combinations of elementType:stereotype 
        // i.e. - if a model element is of type Class and has two stereotypes
        // foo and bar, then we need to find any enbaled templates with 
        // a match for Class:foo and Class:bar
        // TODO: if there is no match for Class:foo or Class:bar, do we return
        //       a match for Class with no stereotype? Currently, we are not
        //       doing that.
        for (String stereotype: stereotypes)
        {
            List<DomainTemplate> templates = 
                codeGenTemplates.get(eleType += ":" + stereotype);
            
            if (templates != null && templates.size() > 0)
                validTemplates.addAll(templates);
        }
        
        // the CodeGenTemplate instances don't have any element name and it
        // would be convenient for the client of this class to have it preset
        return insertElementName(element.getName(), validTemplates);
    }
    
    
    private static List<DomainTemplate> insertElementName(
        String elementName,
        List<DomainTemplate> templates)
    {
        if (templates == null || templates.size() == 0)
            return null;
        
        for (DomainTemplate cgt: templates)
            cgt.setElementName(elementName);
        
        return templates;
    }
    

    public static List<String> retrieveProjectEnabledModelElements()
    {
        Set<String> keys = codeGenTemplates.keySet();
        List<String> elementTypes = new ArrayList<String>(keys.size());
        
        for (String key: keys)
        {
            int index = key.indexOf(':') + 1;
            String eleType = "";
            
            if (index > 0)
                eleType = key.substring(index);
            
            else
                eleType = key;
            
            if (!elementTypes.contains(eleType))
                elementTypes.add(eleType);
        }
        
        return elementTypes;
    }
    
}
