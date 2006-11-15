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

package org.netbeans.modules.j2ee.common.source;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Copied from java/source TestUtilities. To be removed when SourceUtils
 * is moved to java/source.
 *
 * @author Andrei Badea
 */
public class TestUtilities {

    private TestUtilities() {
    }

    public static final FileObject copyStringToFileObject(FileObject fo, String content) throws IOException {
        OutputStream os = fo.getOutputStream();
        try {
            InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
            FileUtil.copy(is, os);
            return fo;
        } finally {
            os.close();
        }
    }

    public static final String copyFileObjectToString (FileObject fo) throws java.io.IOException {
        int s = (int)FileUtil.toFile(fo).length();
        byte[] data = new byte[s];
        InputStream stream = fo.getInputStream();
        try {
            int len = stream.read(data);
            if (len != s) {
                throw new EOFException("truncated file");
            }
            return new String (data);
        } finally {
            stream.close();
        }
    }
}
