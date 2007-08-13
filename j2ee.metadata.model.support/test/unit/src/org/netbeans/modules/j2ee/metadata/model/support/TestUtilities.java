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

package org.netbeans.modules.j2ee.metadata.model.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * 
 * @author Andrei Badea, Erno Mononen
 */
public class TestUtilities {

    private TestUtilities() {
    }

    public static final FileObject copyStringToFileObject(FileObject parent, String path, String contents) throws IOException {
        FileObject fo = FileUtil.createData(parent, path);
        copyStringToFileObject(fo, contents);
        return fo;
    }

    /**
     * Copies the given <code>content</code> to the given <code>fo</code>.
     * 
     * @param file the file object to which the given content is copied.
     * @param content the contents to copy.
     */ 
    public static final void copyStringToFileObject(FileObject fo, String contents) throws IOException {
        OutputStream os = fo.getOutputStream();
        try {
            InputStream is = new ByteArrayInputStream(contents.getBytes("UTF-8"));
            FileUtil.copy(is, os);
        } finally {
            os.close();
        }
    }

    /**
     * Copies the given <code>content</code> to the given <code>file</code>.
     * 
     * @param file the file to which the given content is copied.
     * @param content the contents to copy.
     */ 
    public static final void copyStringToFile(File file, String content) throws IOException {
        copyStringToFileObject(FileUtil.toFileObject(file), content);
    }

    /**
     * Copies the given stream to a String.
     * 
     * @param input the stream to copy.
     * @return string representing the contents of the given stream.
     */ 
    public static final String copyStreamToString(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FileUtil.copy(input, output);
        return Charset.forName("UTF-8").newDecoder().decode(ByteBuffer.wrap(output.toByteArray())).toString();
    }

    /**
     * Copies the given <code>fo</code> to a String.
     * 
     * @param fo the file objects to copy.
     * @return string representing the contents of the given <code>fo</code>.
     */ 
    public static final String copyFileObjectToString(FileObject fo) throws IOException {
        InputStream stream = fo.getInputStream();
        try {
            return copyStreamToString(stream);
        } finally {
            stream.close();
        }
    }

    /**
     * Copies the given <code>file</code> to a String.
     * 
     * @param fo the file to copy.
     * @return string representing the contents of the given <code>file</code>.
     */ 
    public static final String copyFileToString(File file) throws IOException {
        return copyFileObjectToString(FileUtil.toFileObject(file));
    }

}
