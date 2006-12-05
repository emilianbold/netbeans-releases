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

package org.netbeans.modules.html.editor.test;

import org.netbeans.api.html.lexer.HTMLTokenId;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.xml.sax.SAXException;

/**
 * Repository whose getDefaultFileSystem() returns a writeable FS containing
 * the layer of the html editor module. It is put in the default lookup,
 * thus it is returned by Repository.getDefault().
 *
 * @author Andrei Badea, Marek Fukala
 */
public class RepositoryImpl extends Repository {
    
    private XMLFileSystem system;
    
    public RepositoryImpl() {
        super(createDefFs());
    }
    
    private static FileSystem createDefFs() {
        try
        {
            FileSystem writeFs = FileUtil.createMemoryFileSystem();
            FileSystem layerFs = new XMLFileSystem(RepositoryImpl.class.getClassLoader().getResource("org/netbeans/modules/html/editor/resources/layer.xml"));
            FileSystem lexerLayerFS = new XMLFileSystem(HTMLTokenId.class.getClassLoader().getResource("org/netbeans/lib/html/lexer/layer.xml"));
            return new MultiFileSystem(new FileSystem[] { writeFs, lexerLayerFS, layerFs });
        } catch (SAXException e) {
            return null;
        }
    }
}
