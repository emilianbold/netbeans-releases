/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview;

import org.netbeans.modules.xml.api.EncodingUtil;

import javax.swing.text.StyledDocument;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 * @author pfiala
 */
public class EncodingHelper {
    public static final String DEFAULT_ENCODING = "UTF-8"; // NOI18N;

    private String encoding = DEFAULT_ENCODING;

    public boolean isValidEncoding(String encoding) {
        //test encoding on dummy stream
        try {
            new java.io.OutputStreamWriter(new java.io.ByteArrayOutputStream(1), encoding);
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    public String setDefaultEncoding(String s) {
        // update prolog to new valid encoding
        if (s.startsWith("<?xml")) {
            int i = s.indexOf("?>");
            if (i > 0) {
                s = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + s.substring(i + 2);
            }
        }
        return s;
    }

    public String getEncoding() {
        return encoding;
    }

    public void resetEncoding() {
        encoding = DEFAULT_ENCODING;
    }

    public String detectEncoding(StyledDocument document) throws IOException {
        return setEncoding(EncodingUtil.detectEncoding(document));
    }

    public String detectEncoding(InputStream inputStream) throws IOException {
        return setEncoding(EncodingUtil.detectEncoding(inputStream));
    }

    public String detectEncoding(byte[] data) throws IOException {
        return detectEncoding(new ByteArrayInputStream(data));
    }

    public String setEncoding(String encoding) {
        if (encoding == null) {
            return this.encoding;
        }
        if (isValidEncoding(encoding)) {
            this.encoding = encoding;
        }
        return encoding;
    }
}
