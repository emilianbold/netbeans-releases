/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * Contributor(s): Jesse Glick.
 */

package org.apache.tools.ant.module.loader;

import java.io.IOException;

import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.loaders.OpenSupport;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableOpenSupport;

class AntProjectDataEditor extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie, PrintCookie {

    public AntProjectDataEditor (AntProjectDataObject obj) {
        super (obj, new AntEnv (obj));
        setMIMEType ("text/xml"); // NOI18N
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
