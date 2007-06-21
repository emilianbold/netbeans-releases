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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    
    public FileOutputStream getTemplateFileOutputStream()
        throws FileNotFoundException 
    {
        return new FileOutputStream(getTemplateFile());
    }
    
    public void setTemplateFilename(String val)
    {
        templateFilename = val;
    }

    
}
