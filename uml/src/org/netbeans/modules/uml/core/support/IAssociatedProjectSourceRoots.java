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

/*
 * ISource.java
 *
 * Created on March 29, 2005, 11:24 AM
 */

package org.netbeans.modules.uml.core.support;

import java.io.File;


/**
 * NetBeans projects are able to have multiple source roots.  The
 * IAssociatedProjectSourceRoots aides in converting a file path that is
 * relative to one of the NetBean project source roots to an absolute
 * file path.  IAssociatedProjectSourceRoots can also convert an absolute
 * file path into a path relative to one of the source roots.
 *
 * Each project source root has a identifier.  To convert an absolute path
 * to a relative path a source root identifier in placed in the relative path.
 *
 * @author Trey Spiva
 */
public interface IAssociatedProjectSourceRoots
{  
   /**
    * Retrieves the source root identifier that matches the specified
    * file name.
    *
    * @param file The file used to retrieve the source root identifier.
    * @return  The source root identifier if one can be mapped to the file name
    *          otherwise an empty string.
    */
   public String getSourceRootId(String file);
   
   /**
    * Creates a path that is relative to one of the projects source roots.  If 
    * the file name is not descendent of one of the source roots an empty string
    * will be returned.
    *
    * @param file The name of the file to convert.
    * @return The relative path unless the file name is not a descendent of one 
    *         of the source roots.
    */
   public String createRelativePath(String file);
   
   /**
    * Creates an absolute file path name.  The file name is only converted if the 
    * file name start with a source root identifier.  If the source file name does not
    * start with a source root identifier then an empty string
    * will be returned.
    *
    * @param file The name of the file to convert.
    * @return The converted path unless the file name does not start with a source
    *         root identifier
    */
   public String createAbsolutePath(String file);
   
   /**
    * Retrieves the source roots that are need to locate all source files that
    * are needed to compile the project.
    */
   public File[] getCompileDependencies();
}
