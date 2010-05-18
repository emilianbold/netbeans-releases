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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class DomainTemplate 
{
    public final static String ELEMENT_NAME_TOKEN = "{name}"; // NO18N
    
    private String elementName;
    private String filenameFormat;
    private String extension;
    private String folderPath;
    private String templateFilename;
    
    public DomainTemplate()
    {
    }

    public DomainTemplate(
        String anElementName,
        String aFilenameFormat,
        String anExtension,
        String aFolderPath,
        String aTemplateFile)
    {
        elementName = anElementName;
        filenameFormat = aFilenameFormat;
        extension = anExtension;
        folderPath = aFolderPath;
        templateFilename = aTemplateFile;
    }
    
    
    public String getElementName()
    {
        return elementName;
    }

    public void setElementName(String val)
    {
        elementName = val;
    }

    public String getFilenameFormat()
    {
        return filenameFormat;
    }
    
    public String getFormattedFilename()
    {
        return String.format(getFilenameFormat(), elementName);
    }
    
    public void setFilenameFormat(String val)
    {
        filenameFormat = val;
    }

    public String getExtension()
    {
        return extension;
    }
    
    public void setExtension(String val)
    {
        extension = val;
    }
    
    public String getFolderPath()
    {
        return folderPath;
    }
    
    public void setFolderPath(String val)
    {
        folderPath = val;
    }

    public String getTemplateFilename()
    {
        return templateFilename;
    }
    
    public File getTemplateFile()
    {
        return new File(DomainTemplatesRetriever.TEMPLATES_BASE_FOLDER +
            File.separatorChar + getTemplateFilename());
    }

    public FileObject getTemplateFileObject()
    {
        return FileUtil.toFileObject(getTemplateFile());
    }
    
    public OutputStream getTemplateFileOutputStream() throws FileAlreadyLockedException, IOException
    {
        return getTemplateFileObject().getOutputStream();
    }
    
    public void setTemplateFilename(String val)
    {
        templateFilename = val;
    }

    
}
