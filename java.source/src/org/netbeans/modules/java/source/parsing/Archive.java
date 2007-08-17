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

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
/** Methods for accessing an archive. Archive represents zip file or
 * folder.
 *
 * @author Petr Hrebejk
 */
public interface Archive {
       
    // New implementation Archive Interface ------------------------------------


    /** Gets all files in given folder
     *  @param folderName name of folder to list, path elements separated by / char
     *  @param entry owning ClassPath.Entry to check the excludes or null if everything should be included
     *  @param kinds to list, may be null => all types
     *  @param filter to filter the file content
     *  @return the listend files
     */
    public Iterable<JavaFileObject> getFiles( String folderName, ClassPath.Entry entry, Set<JavaFileObject.Kind> kinds, JavaFileFilterImplementation filter) throws IOException;    
    
    
    /**
     * Cleans cached data
     */
    public void clear ();

}
