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
package org.netbeans.modules.mercurial;

import java.io.File;

/**
 * Holds detailed information about status of a mercurial-managed file, ie repository URL, remote path, branch, etc.
 * 
 * @author Maros Sandor
 */
public class FileStatus {
    private File mFile;
    private boolean mbCopied;
    
    public FileStatus(File file, boolean isCopied){
        mFile = file;
        mbCopied = isCopied;
    }
    public File getFile(){
        return mFile;
    }
    public boolean isCopied(){
        return mbCopied;
    }
}
