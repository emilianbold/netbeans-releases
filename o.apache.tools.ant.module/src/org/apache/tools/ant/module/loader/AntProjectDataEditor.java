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
 *
 * Contributor(s): Jesse Glick.
 */

package org.apache.tools.ant.module.loader;

import java.io.IOException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
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

    protected boolean notifyModified () {
        if (!super.notifyModified ()) {
            return false;
        } else {
            AntEnv e = (AntEnv) env;
            e.getAntProjectDataObject ().addSaveCookie (e);
            return true;
        }
    }

    protected void notifyUnmodified () {
        super.notifyUnmodified ();
        AntEnv e = (AntEnv) env;
        e.getAntProjectDataObject ().removeSaveCookie (e);
    }
    
    protected String messageName() {
        String name = super.messageName();
        AntProjectDataObject d = ((AntEnv)env).getAntProjectDataObject();
        if (d.getPrimaryFile().getNameExt().equals("build.xml")) { // NOI18N
            // #25793: show project name in case the script name does not suffice
            AntProjectCookie cookie = (AntProjectCookie)d.getCookie(AntProjectCookie.class);
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

        protected FileObject getFile () {
            return getDataObject ().getPrimaryFile ();
        }

        protected FileLock takeLock () throws IOException {
            return ((AntProjectDataObject) getDataObject ()).getPrimaryEntry ().takeLock ();
        }

        public void save () throws IOException {
            ((AntProjectDataEditor) findCloneableOpenSupport ()).saveDocument ();
            getDataObject ().setModified (false);
        }

        public CloneableOpenSupport findCloneableOpenSupport () {
            return (CloneableOpenSupport) getDataObject ().getCookie (EditCookie.class);
        }

    }

}
