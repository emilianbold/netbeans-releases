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

package org.netbeans.api.gsf;

import org.netbeans.api.gsf.annotations.CheckForNull;
import org.netbeans.api.gsf.annotations.NonNull;
import org.openide.filesystems.FileObject;

/**
 * A file passed in to the parser
 *  
 * @author Tor Norbye
 */
public interface ParserFile {

    /** Return the file object corresponding to the file */
    @NonNull
    public FileObject getFileObject();

    /** Return the relative path of the file (relative to its root, such as
     * a class path root, or a project source root, etc.*/
    @CheckForNull
    public String getRelativePath();
    
    public String getNameExt();
    
    public String getExtension();
    
    /**
     * Return true if this file is part of the "platform" (e.g. Ruby builtins
     * or gems rather than user source)
     */
    public boolean isPlatform();
}
