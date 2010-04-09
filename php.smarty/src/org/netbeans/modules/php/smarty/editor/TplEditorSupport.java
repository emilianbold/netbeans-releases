
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.php.smarty.editor;

import java.io.IOException;
import java.io.ObjectInput;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.nodes.Node.Cookie;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;
import org.openide.util.UserCancelException;
import org.openide.windows.CloneableOpenSupport;

/**
 * Editor support for TPL data objects.
 * @author Martin Fousek
 */
public final class TplEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie.Observable, PrintCookie {

    /** SaveCookie for this support instance. The cookie is adding/removing
     * data object's cookie set depending on if modification flag was set/unset.
     * It also invokes beforeSave() method on the TplDataObject to give it
     * a chance to eg. reflect changes in 'charset' attribute
     * */
    private final SaveCookie saveCookie = new SaveCookie() {

        /** Implements <code>SaveCookie</code> interface. */
        @Override
        public void save() throws IOException {
            try {
                saveDocument();
            } catch (UserCancelException uce) {
                //just ignore
            }
        }
    };

    /** Constructor. */
    TplEditorSupport(TplDataObject obj) {
        super(obj, new Environment(obj));
        setMIMEType(obj.getPrimaryFile().getMIMEType());
    }

    @Override
    protected boolean asynchronousOpen() {
	return true;
    }

    @Override
    public void saveDocument() throws IOException {
        super.saveDocument();
        TplEditorSupport.this.getDataObject().setModified(false);
    }

    @Override
    protected StyledDocument createStyledDocument(EditorKit kit) {
        StyledDocument doc = super.createStyledDocument(kit);

        // see TplKit.createDefaultDocument;
        Runnable postInitRunnable = (Runnable)doc.getProperty("postInitRunnable"); //NOI18N
        if(postInitRunnable != null) {
            postInitRunnable.run();
        }

        return doc;
    }

    /**
     * Overrides superclass method. Adds adding of save cookie if the document has been marked modified.
     * @return true if the environment accepted being marked as modified
     *    or false if it has refused and the document should remain unmodified
     */
    @Override
    protected boolean notifyModified() {
        if (!super.notifyModified()) {
            return false;
        }
        addSaveCookie();
        return true;
    }

    /** Overrides superclass method. Adds removing of save cookie. */
    @Override
    protected void notifyUnmodified() {
        super.notifyUnmodified();
        removeSaveCookie();
    }

    /** Helper method. Adds save cookie to the data object. */
    private void addSaveCookie() {
        TplDataObject obj = (TplDataObject) getDataObject();

        // Adds save cookie to the data object.
        if (obj.getCookie(SaveCookie.class) == null) {
            obj.getCookieSet0().add(saveCookie);
            obj.setModified(true);
        }
    }

    /** Helper method. Removes save cookie from the data object. */
    private void removeSaveCookie() {
        TplDataObject obj = (TplDataObject) getDataObject();

        // Remove save cookie from the data object.
        Cookie cookie = obj.getCookie(SaveCookie.class);

        if (cookie != null && cookie.equals(saveCookie)) {
            obj.getCookieSet0().remove(saveCookie);
            obj.setModified(false);
        }
    }

    /** Nested class. Environment for this support. Extends <code>DataEditorSupport.Env</code> abstract class. */
    private static class Environment extends DataEditorSupport.Env {

        private static final long serialVersionUID = 3035543158452715818L;

        /** Constructor. */
        public Environment(TplDataObject obj) {
            super(obj);
        }

        /** Implements abstract superclass method. */
        @Override
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        /** Implements abstract superclass method.*/
        @Override
        protected FileLock takeLock() throws IOException {
            return ((TplDataObject) getDataObject()).getPrimaryEntry().takeLock();
        }

        /**
         * Overrides superclass method.
         * @return text editor support (instance of enclosing class)
         */
        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (TplEditorSupport) getDataObject().getCookie(TplEditorSupport.class);
        }
    } // End of nested Environment class.

    /** A method to create a new component. Overridden in subclasses.
     * @return the {@link TplEditor} for this support
     */
    @Override
    protected CloneableEditor createCloneableEditor() {
        return new TplEditor(this);
    }

    public static class TplEditor extends CloneableEditor {

        public TplEditor() {
        }

        /** Creates new editor */
        public TplEditor(TplEditorSupport s) {
            super(s);
            initialize();
        }

        private void initialize() {
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            initialize();
        }
    }
}
