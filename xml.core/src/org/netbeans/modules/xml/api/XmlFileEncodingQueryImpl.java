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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.netbeans.modules.xml.core.lib.EncodingHelper;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * This implementation of the FileEncodingQueryImplementation can be used
 * by any XML file: WSDL, Schema, BPEL, ...
 *
 * @author nk160297
 */
public class XmlFileEncodingQueryImpl extends FileEncodingQueryImplementation {

    private static XmlFileEncodingQueryImpl singleton = new XmlFileEncodingQueryImpl();
    
    public static XmlFileEncodingQueryImpl singleton() {
        return singleton;
    }
    
    public synchronized Charset getEncoding(FileObject file) {
        assert file != null;
        //
        try {
            InputStream in = null;
            String encoding = null;
            try {
                in = new BufferedInputStream(file.getInputStream(), 
                        EncodingHelper.EXPECTED_PROLOG_LENGTH);
                encoding = EncodingHelper.detectEncoding(in);
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (encoding == null) {
                encoding = "UTF8"; //!!! // NOI18N
            }
            return Charset.forName(encoding);
        } catch (Exception ex) {
            // There isn't specific error processing for a while
            // Uncomment if necessary.
//        } catch (FileNotFoundException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (IllegalCharsetNameException ichse) {
//        } catch (UnsupportedCharsetException uchse) {
        }

        return null;
    }
}
