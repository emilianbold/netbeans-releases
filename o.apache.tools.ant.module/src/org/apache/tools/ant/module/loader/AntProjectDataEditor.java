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

package org.apache.tools.ant.module.loader;

import java.io.IOException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.CloneableOpenSupport;
import org.w3c.dom.Element;

final class AntProjectDataEditor extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie.Observable, PrintCookie, ChangeListener {
    
    private boolean addedChangeListener = false;

    public AntProjectDataEditor (AntProjectDataObject obj) {
        super (obj, new AntEnv (obj));
        setMIMEType(AntProjectDataLoader.REQUIRED_MIME);
    }

    @Override
    protected boolean notifyModified () {
        if (!super.notifyModified ()) {
            return false;
        } else {
            AntEnv e = (AntEnv) env;
            e.getAntProjectDataObject ().addSaveCookie (e);
            return true;
        }
    }

    @Override
    protected void notifyUnmodified () {
        super.notifyUnmodified ();
        AntEnv e = (AntEnv) env;
        e.getAntProjectDataObject ().removeSaveCookie (e);
    }
    
    @Override
    protected String messageName() {
        String name = super.messageName();
        return annotateWithProjectName(name);
    }
    
    @Override
    protected String messageHtmlName () {
        String name = super.messageHtmlName();
        return name != null ? annotateWithProjectName(name) : null;
    }
    
    /** #25793 fix - adds project name to given ant script name if needed.
     * @return ant script name annotated with project name or ant script name unchanged
     */
    private String annotateWithProjectName (String name) {
        DataObject d = getDataObject();
        if (d.getPrimaryFile().getNameExt().equals("build.xml")) { // NOI18N
            // #25793: show project name in case the script name does not suffice
            AntProjectCookie cookie = d.getCookie(AntProjectCookie.class);
            Element pel = cookie.getProjectElement();
            if (pel != null) {
                String projectName = pel.getAttribute("name"); // NOI18N
                if (!projectName.equals("")) { // NOI18N
                    name = NbBundle.getMessage(AntProjectDataEditor.class,
                        "LBL_editor_tab", name, projectName);
                }
            }
            if (!addedChangeListener) {
                cookie.addChangeListener(WeakListeners.change(this, cookie));
                addedChangeListener = true;
            }
        }
        return name;
    }
    
    
    /**
     * Overridden to ensure that the displayName of the node in the editor has
     * the right annotation for build.xml files, so that the Navigator will display it.
     */
    @Override
    protected void initializeCloneableEditor(CloneableEditor editor) {
        super.initializeCloneableEditor(editor);
        editor.setActivatedNodes(new Node[] {
            new FilterNode(getDataObject().getNodeDelegate()) {
                @Override
                public String getDisplayName() {
                    return messageName();
                }
            }
        });
    }

    public void stateChanged(ChangeEvent e) {
        // Project name might have changed. See messageName().
        updateTitles();
    }
    
    private static class AntEnv extends DataEditorSupport.Env implements SaveCookie {

        private static final long serialVersionUID = 6610627377311504616L;
        
        public AntEnv (AntProjectDataObject obj) {
            super (obj);
        }
        
        AntProjectDataObject getAntProjectDataObject () {
            return (AntProjectDataObject) getDataObject ();
        }

        @Override
        protected FileObject getFile () {
            return getDataObject ().getPrimaryFile ();
        }

        @Override
        protected FileLock takeLock () throws IOException {
            return ((AntProjectDataObject) getDataObject ()).getPrimaryEntry ().takeLock ();
        }

        public void save () throws IOException {
            ((AntProjectDataEditor) findCloneableOpenSupport ()).saveDocument ();
            getDataObject ().setModified (false);
        }

        @Override
        public CloneableOpenSupport findCloneableOpenSupport () {
            return (CloneableOpenSupport) getDataObject ().getCookie (EditCookie.class);
        }

    }

}
