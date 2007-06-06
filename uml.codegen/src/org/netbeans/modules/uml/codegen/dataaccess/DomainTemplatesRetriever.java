/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.uml.codegen.dataaccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.netbeans.modules.uml.codegen.dataaccess.xmlbinding.TemplateFamilies;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbinding.TemplateFamilies.Family;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbinding.TemplateFamilies.Family.DomainObject;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbinding.TemplateFamilies.Family.DomainObject.Template;

import org.netbeans.modules.uml.codegen.dataaccess.xmlbinding.TemplateFamiliesHandler;
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
    private static HashMap<String,List<DomainTemplate>> codeGenTemplates = null;
    
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
            new HashMap<String,List<DomainTemplate>>(projectTemplates.size());

        // get all of the available templates (from UML Options UI)
        TemplateFamiliesHandler dataHandler = 
            TemplateFamiliesHandler.getInstance();
        
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
            DomainObject domainObject = family.getDomainByName(tokens[1]);
            String elementType = domainObject.getModelElement();
            String stereotype = domainObject.getStereotype();
            
            List<Template> templateList = domainObject.getTemplate();
            List<DomainTemplate> templates = new ArrayList<DomainTemplate>();
            
            // for (int i=0; i < domainNodes.getLength(); i++)
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
                codeGenTemplates.put(key, templates);
        }
    }

    public static List<DomainTemplate> retrieveTemplates(INamedElement element)
    {
        if (codeGenTemplates == null || codeGenTemplates.size() == 0)
            load((UMLProject)ProjectUtil.findReferencingProject(element));
        
        String eleType = element.getElementType();
        ETList<String> stereotypes = 
            element.getAppliedStereotypesAsString();
        
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
    
}
