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
package org.netbeans.modules.vmd.io.javame;

import org.netbeans.api.java.loaders.JavaDataSupport;
import org.netbeans.modules.mobility.editor.pub.J2MEDataObject;
import org.netbeans.modules.vmd.api.io.providers.DataObjectInterface;
import org.netbeans.modules.vmd.api.model.Debug;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

import javax.swing.text.StyledDocument;
import java.io.IOException;

/**
 * @author David Kaspar
 */
public final class MEDesignDataObject extends J2MEDataObject implements DataObjectInterface {

    private FileObject javaFile;
    private FileObject designFile;
    private MEDesignEditorSupport editorSupport;

    public MEDesignDataObject (FileObject javaFile, FileObject designFile, MultiFileLoader loader) throws DataObjectExistsException {
        super (javaFile, loader);
        ((MEDesignDataLoader) loader).createSecondaryEntry (this, designFile);
        this.javaFile = javaFile;
        this.designFile = designFile;

        editorSupport = new MEDesignEditorSupport (this);

        CookieSet cookies = getCookieSet ();
        cookies.add (editorSupport);
    }

    public Node createNodeDelegate () {
        return new MEDesignNode (JavaDataSupport.createJavaNode (javaFile));
    }

//    public void addSaveCookie (SaveCookie save) {
//        getCookieSet ().add (save);
//    }

//    public void removeSaveCookie (SaveCookie save) {
//        getCookieSet ().remove (save);
//    }

//    public FileObject getJavaFile () {
//        return javaFile;
//    }

    public FileObject getDesignFile () {
        return designFile;
    }

    public void discardAllEditorSupportEdits () {
        editorSupport.discardAllEdits ();
    }

    public void notifyEditorSupportModified () {
        editorSupport.notifyModified ();
    }

    public void setMVTC (TopComponent multiViewTopComponent) {
        editorSupport.setMVTC (multiViewTopComponent);
    }

    public StyledDocument getEditorDocument () {
        try {
            return editorSupport.openDocument ();
        } catch (IOException e) {
            throw Debug.error (e);
        }
    }

    public MEDesignEditorSupport getEditorSupport () {
        return editorSupport;
    }

    @Override
    protected synchronized J2MEEditorSupport createJavaEditorSupport() {
        return editorSupport;
    }

}
