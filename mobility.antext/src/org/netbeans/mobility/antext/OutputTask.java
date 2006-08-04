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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * OutputTask.java
 *
 * Created on September 8, 2005, 10:40 AM
 *
 */
package org.netbeans.mobility.antext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * OutputTask Task adds support for writing properly encoded text into a file.
 *
 * <p>Attributes:<ol>
 * <li>File - Required. Specifies file where the text will be written to.
 * <li>Encoding - Optional. Specifies encoding used (system default file encoding is used by default).
 * <li>Append - Optional. Specifies if the text should be appended to the file (false by default).
 * <li>Text - Optional. Text to be written to the file (empty by default). Nested CDATA section may contain multi-line text.
 * </ol>
 *
 * @author Adam Sotona
 */
public class OutputTask extends Task
{
    
    File file = null;
    String text = ""; //NOI18N
    String encoding = System.getProperty("file.encoding"); //NOI18N
    boolean append = false;
    
    public void execute() throws BuildException
    {
        if (file == null) throw new BuildException(Bundle.getMessage("ERR_MissingAttr", "file")); // NO I18N
        OutputStreamWriter out = null;
        try
        {
            out = new OutputStreamWriter(new FileOutputStream(file, append), encoding);
            out.write(text);
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (IOException ioex)
                {}
            }
        }
    }
    
    public void setFile(final File file)
    {
        this.file = file;
    }
    
    public void setEncoding(final String encoding)
    {
        this.encoding = encoding;
    }
    
    public void setAppend(final boolean append)
    {
        this.append = append;
    }
    
    public void setText(final String text)
    {
        this.text = text;
    }
    
    public void addText(final String text)
    {
        this.text += getProject().replaceProperties(text);
    }
}
