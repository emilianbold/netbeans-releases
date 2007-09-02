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
package org.netbeans.modules.xml.xdm.xam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess;
import org.netbeans.modules.xml.xam.spi.DocumentModelAccessProvider;
import org.openide.loaders.DataObject;

/**
 *
 * @author Nam Nguyen
 */
public class XDMAccessProvider implements DocumentModelAccessProvider {
    
    /** Creates a new instance of XDMAccessProvider */
    public XDMAccessProvider() {
    }

    public DocumentModelAccess createModelAccess(AbstractDocumentModel model) {
        return new XDMAccess(model);
    }
    
    public Document loadSwingDocument(InputStream in) throws IOException, BadLocationException {
        Document sd = new BaseDocument(XMLKit.class, false);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                sd.insertString(sd.getLength(), line+System.getProperty("line.separator"), null); // NOI18N
            }
        } finally {
            br.close();
        }
        return sd;
    }

    public Object getModelSourceKey(ModelSource source) {
        Object key = source.getLookup().lookup(DataObject.class);
        //Fix for IZ 112329: For referenced schemas in runtime catalog, there will be no DO,
        //hence we must return the Document as the key as an alternative.
        if(key != null)
            return key;
        return source.getLookup().lookup(Document.class);
    }
}
