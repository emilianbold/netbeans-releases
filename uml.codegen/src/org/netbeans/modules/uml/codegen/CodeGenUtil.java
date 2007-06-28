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

package org.netbeans.modules.uml.codegen;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.Family;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.TemplateFamilies;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.TemplateFamiliesHandler;
import org.netbeans.modules.uml.util.StringTokenizer2;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class CodeGenUtil 
{
    private CodeGenUtil() {}

    public static List<String> cleanProjectTemplatesList(
        ArrayList<String> projectTemplates)
    {
        List<String> cleanTemplates = new ArrayList<String>(projectTemplates.size());
        
        TemplateFamilies templateFamilies = 
            TemplateFamiliesHandler.getInstance().getTemplateFamilies();
        
        for (String famDomKey: projectTemplates)
        {
            String[] tokens = StringTokenizer2.tokenize(famDomKey, ":");
            String familyName = tokens[0];
            String domainName = tokens[1];
            
            Family family = templateFamilies.getFamilyByName(familyName);
            
            if (family != null && family.getDomainByName(domainName) != null)
                cleanTemplates.add(famDomKey);
        }
        
        return cleanTemplates;
    }
}
