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

package org.netbeans.modules.subversion;

import java.io.*;
import java.util.*;
import org.netbeans.modules.subversion.util.*;

/**
 * Encapsulates content of .svn/* files that is used to save to-be-deleted metadata.
 *
 * @author Petr Kuzel
 */
public class SvnMetadata  {

    /** Location on disk, a directory. */
    private File storage;

    private SvnMetadata() throws IOException {
        // XXX clean-up on IDE start, shutdown?
        storage = FileUtils.createTmpFolder("nb_SvnMetadata_");  // NOI18N
    }

    /**
     * Reads and stores .svn metadata.
     */ 
    public static SvnMetadata read(File dir) throws IOException {
        assert dir.isDirectory();

        SvnMetadata data = new SvnMetadata();
//        System.err.println("METADATA Saving: " + dir);
        data.load(dir);
        return data;
    }

    private void load(File dir) throws IOException {
        FileUtils.copyDirFiles(dir, storage, true);
    }

    /** Saves metadata in the given folder, typically named .svn.
     *
     * @param dir folder to save to
     * @throws java.io.IOException 
     */
    public void save(File dir) throws IOException {
//        System.err.println("METADATA Restoring: " + dir);
        dir.mkdirs();
        FileUtils.copyDirFiles(storage, dir, true);
    }

}
