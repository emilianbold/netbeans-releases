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
