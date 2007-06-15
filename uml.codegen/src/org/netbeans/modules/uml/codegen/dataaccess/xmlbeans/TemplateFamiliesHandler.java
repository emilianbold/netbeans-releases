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

package org.netbeans.modules.uml.codegen.dataaccess.xmlbeans;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.openide.util.Exceptions;


/**
 *
 * @author Craig Conover, craig.conover@sun.com
 * 
 * Responsible for reading and writing of the
 * XML data containing the UML code generation template preferences.
 * 
 */
public class TemplateFamiliesHandler 
{
    private static TemplateFamiliesHandler self = null;
    
    private TemplateFamilies templateFamilies = null;

    public final static String configFolder = 
        ProductHelper.getConfigManager().getDefaultConfigLocation();

    public static TemplateFamiliesHandler getInstance(boolean reset)
    {
        if (reset)
            self = null;
        
        if (self == null)
            self = new TemplateFamiliesHandler();
        
        return self;
    }
    
    public static TemplateFamiliesHandler getInstance()
    {
        return TemplateFamiliesHandler.getInstance(false);
    }
    
    private TemplateFamiliesHandler()
    {
        read();
    }
    
    public void read()
    {
        try
        {
            templateFamilies = TemplateFamilies.createGraph(
                new FileInputStream(configFolder + "TemplateFamilies.etc")); // NOI18N
        }
        
        catch (Schema2BeansException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        
        catch (IOException ex)
        {
            Exceptions.printStackTrace(ex);
        }

    }
    
    public void reset()
    {
        templateFamilies = null;
        self = null;
    }

    public void save()
    {
        try
        {
            templateFamilies.write(new FileOutputStream(
                configFolder + "TemplateFamilies.etc")); // NOI18N
        }
        
        catch (IOException ex) 
        {
            Exceptions.printStackTrace(ex);
        } 
    }
    
    public TemplateFamilies getTemplateFamilies()
    {
        return templateFamilies;
    }
}
