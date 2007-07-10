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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mobility.project.queries;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Adam Sotona
 */
public class FileEncodingQueryImpl extends FileEncodingQueryImplementation implements AntProjectListener {
    
    
    private final AntProjectHelper hlp;
    private Charset ch;
    
    public FileEncodingQueryImpl(final AntProjectHelper hlp) {
        this.hlp = hlp;
        hlp.addAntProjectListener(this);
    }
    
    public Charset getEncoding(FileObject file) {
        Charset c = ch;
        if (c == null) try {
            String enc = J2MEProjectUtils.evaluateProperty(hlp, DefaultPropertiesDescriptor.JAVAC_ENCODING);
            ch = c = enc == null ? Charset.defaultCharset() : Charset.forName(enc);
        } catch (IllegalCharsetNameException exception) {
            return null;
        }
        return c;
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
    }

    public void propertiesChanged(AntProjectEvent ev) {
        ch = null;
    }
    
}
