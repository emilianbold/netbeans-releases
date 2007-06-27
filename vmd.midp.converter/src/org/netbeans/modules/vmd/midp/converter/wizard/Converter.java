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
 *
 */
package org.netbeans.modules.vmd.midp.converter.wizard;

import org.netbeans.modules.vmd.api.io.providers.DocumentSerializer;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.w3c.dom.Node;

/**
 * @author David Kaspar
 */
public class Converter {

    public static void convert (final FileObject inputJavaFile, final FileObject inputDesignFile, String outputFileName) {
        try {
            DataFolder folder = DataFolder.findFolder (inputJavaFile.getParent ());
            final Node rootNode = XMLUtil.getRootNode (inputDesignFile);
            DataObject template = DataObject.find (Repository.getDefault ().getDefaultFileSystem ().findResource ("Templates/MIDP/VisualMIDlet.java")); // NOI18N
            DataObject outputDesign = template.createFromTemplate (folder, outputFileName);
            DocumentSerializer serializer = IOSupport.getDocumentSerializer (outputDesign);
            serializer.waitDocumentLoaded ();
            final DesignDocument document = serializer.getDocument ();
            document.getTransactionManager ().writeAccess (new Runnable() {
                public void run () {
                    convert (inputJavaFile, rootNode, document);
                }
            });
            IOSupport.forceUpdateCode (outputDesign);
            outputDesign.getLookup ().lookup (CloneableEditorSupport.class).saveDocument ();
        } catch (Exception e) {
            Exceptions.printStackTrace (e);
        }
    }

    private static void convert (FileObject inputJavaFile, Node rootNode, DesignDocument document) {
        // TODO
    }

}
