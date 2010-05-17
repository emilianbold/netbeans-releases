/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.vmd.io.javame;

import org.netbeans.modules.mobility.editor.pub.J2MEDataObject;
import org.netbeans.modules.vmd.api.io.providers.DataObjectInterface;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.modules.vmd.api.io.providers.DocumentSerializer;
import org.netbeans.modules.vmd.api.model.Debug;
import org.openide.filesystems.*;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.text.StyledDocument;
import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author David Kaspar
 */
public final class MEDesignDataObject extends J2MEDataObject implements DataObjectInterface, FileChangeListener {

//    private FileObject javaFile;
//    private FileObject designFile;
    private MEDesignEditorSupport editorSupport;
    private AtomicBoolean dirty = new AtomicBoolean (false);

    public MEDesignDataObject (FileObject javaFile, FileObject designFile, MultiFileLoader loader) throws DataObjectExistsException {
        super (javaFile, loader);
        ((MEDesignDataLoader) loader).createSecondaryEntry (this, designFile);
        //this.javaFile = javaFile;
        //this.designFile = designFile;

        editorSupport = new MEDesignEditorSupport (this);

        CookieSet cookies = getCookieSet ();
        cookies.add (editorSupport);

        designFile.addFileChangeListener (WeakListeners.create (FileChangeListener.class, this, designFile));
//        javaFile.addFileChangeListener (WeakListeners.create (FileChangeListener.class, this, javaFile)); // handled by the CloneableEditorSupport
    }

    public Node createNodeDelegate () {
        return new MEDesignNode (this);
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
        return FileUtil.findBrother(getPrimaryFile(), "vmd"); //NOI18N
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

    public void fileFolderCreated (FileEvent fe) {
        // do nothing
    }

    public void fileDataCreated (FileEvent fe) {
        // do nothing
    }

    public void fileChanged (FileEvent fe) {
        if (! fe.firedFrom (editorSupport.getAtomicSaveAction ()))
            reloadDesign ();
    }

    public void fileDeleted (FileEvent fe) {
        // do nothing
    }

    public void fileRenamed (FileRenameEvent fe) {
        getEditorSupport().updateDisplayName();
    }

    public void fileAttributeChanged (FileAttributeEvent fe) {
        // do nothing
    }

    private void reloadDesign () {
        if (dirty.compareAndSet (true, true))
            return;
        SwingUtilities.invokeLater (new Runnable() {
            public void run () {
                dirty.set (false);
                NotifyDescriptor.Confirmation confirmation = new NotifyDescriptor.Confirmation (
                        NbBundle.getMessage (MEDesignDataObject.class, "MSG_ConfirmReload", getDesignFile().getPath ()), // NOI18N
                        NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
                if (DialogDisplayer.getDefault ().notify (confirmation) != NotifyDescriptor.YES_OPTION)
                    return;
                DocumentSerializer documentSerializer = IOSupport.getDocumentSerializer (MEDesignDataObject.this);
                if (documentSerializer.isLoadingOrLoaded ())
                    documentSerializer.restartLoadingDocument ();
            }
        });
    }

}
