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

package org.netbeans.modules.testtools;

/*
 * XTestDataObject.java
 *
 * Created on May 2, 2002, 4:10 PM
 */

import java.io.IOException;

import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.nodes.Node.Cookie;
import org.openide.text.DataEditorSupport;
import org.openide.windows.CloneableOpenSupport;

/** Editor Support class for XTest Workspace Build Script
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public final class XTestEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie.Observable, PrintCookie, CloseCookie {

    /** SaveCookie for this support instance. The cookie is adding/removing 
     * data object's cookie set depending on if modification flag was set/unset. */
    private final SaveCookie saveCookie = new SaveCookie() {
        /** Implements <code>SaveCookie</code> interface. */
        public void save() throws IOException {
            XTestEditorSupport.this.saveDocument();
            XTestEditorSupport.this.getDataObject().setModified(false);
        }
    };
    
    
    /** Constructor. */
    XTestEditorSupport(XTestDataObject obj) {
        super(obj, new Environment(obj));
        
        setMIMEType("text/xml"); // NOI18N
    }
    
    /** Overrides superclass method. Adds adding of save cookie if the document has been marked modified.
     * @return true if the environment accepted being marked as modified
     *    or false if it has refused and the document should remain unmodified */
    protected boolean notifyModified () {
        if (!super.notifyModified()) 
            return false;

         ((XTestDataObject)getDataObject()).addSaveCookie(saveCookie);

        return true;
    }

    /** Overrides superclass method. Adds removing of save cookie. */
    protected void notifyUnmodified () {
        super.notifyUnmodified();

         ((XTestDataObject)getDataObject()).removeSaveCookie(saveCookie);
    }
    
    /** Nested class. Environment for this support. Extends <code>DataEditorSupport.Env</code> abstract class. */
    private static class Environment extends DataEditorSupport.Env {

        /** Constructor.
         * @param obj XTestDataObject */
        public Environment(XTestDataObject obj) {
            super(obj);
        }

        
        /** Implements abstract superclass method.
         * @return FileObject */
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        /** Implements abstract superclass method.
         * @throws IOException IOException
         * @return FileLock */
        protected FileLock takeLock() throws IOException {
            return ((XTestDataObject)getDataObject()).getPrimaryEntry().takeLock();
        }

        /** Overrides superclass method.
         * @return CloneableOpenSupport instance of enclosing class */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (XTestEditorSupport)((XTestDataObject)getDataObject()).getCookie(XTestEditorSupport.class);
        }
    } // End of nested Environment class.

}

