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

package org.netbeans.modules.uml.codegen.dataaccess.xmlbeans;

import java.io.File;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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
        File file=new File(configFolder + "TemplateFamilies.etc");// NOI18N
        FileObject fo=FileUtil.toFileObject(file);
        InputStream in=null;
        try
        {
            //templateFamilies = TemplateFamilies.createGraph(
            //    new FileInputStream(configFolder + "TemplateFamilies.etc")); // NOI18N
            in=fo.getInputStream();
            templateFamilies = TemplateFamilies.createGraph(in); // NOI18N
        }
        
        catch (Schema2BeansException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        
        catch (IOException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        finally
        {
            if(in!=null)
            {
                try {
                    in.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    public void reset()
    {
        templateFamilies = null;
        self = null;
    }

    public void save()
    {
        File file=new File(configFolder + "TemplateFamilies.etc");// NOI18N
        FileObject fo=FileUtil.toFileObject(file);
        OutputStream out=null;
        try
        {
//            templateFamilies.write(new FileOutputStream(
//                configFolder + "TemplateFamilies.etc")); // NOI18N
            out=fo.getOutputStream();
            templateFamilies.write(out); // NOI18N
        }
        
        catch (IOException ex) 
        {
            Exceptions.printStackTrace(ex);
        } 
        finally
        {
            if(out!=null)
            {
                try {
                    out.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    public TemplateFamilies getTemplateFamilies()
    {
        return templateFamilies;
    }
}
