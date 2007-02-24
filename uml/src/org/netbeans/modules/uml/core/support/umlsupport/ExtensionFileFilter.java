/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.core.support.umlsupport;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 *
 * @author Trey Spiva
 */
public class ExtensionFileFilter implements FilenameFilter, FileFilter
{
   private String m_Extension = "";

   public ExtensionFileFilter(String extension)
   {
      setExtension(extension);
   }

   /* (non-Javadoc)
    * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
    */
   public boolean accept(File dir, String name)
   {
      return StringUtilities.hasExtension(name, m_Extension);
   }

   /* (non-Javadoc)
    * @see java.io.FileFilter#accept(java.io.File)
    */
   public boolean accept(File pathname)
   {
      return StringUtilities.hasExtension(pathname.getAbsolutePath(), 
                                          m_Extension);
   }

   /**
    * @return
    */
   public String getExtension()
   {
      return m_Extension;
   }

   /**
    * @param string
    */
   public void setExtension(String string)
   {
      m_Extension = string;
   }
}
