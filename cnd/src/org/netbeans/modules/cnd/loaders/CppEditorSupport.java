/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.loaders;

// This file was initially based on org.netbeans.modules.java.JavaEditor
// (Rev 61)
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;
import javax.swing.text.*;

import org.openide.ErrorManager;
import org.openide.text.*;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;

import org.netbeans.modules.cnd.editor.parser.CppMetaModel;
import org.netbeans.modules.cnd.editor.parser.ParsingEvent;
import org.netbeans.modules.cnd.editor.parser.ParsingListener;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node.Cookie;
import org.openide.windows.CloneableOpenSupport;

/**
 *  C/C++/Fortran source-file extension for handling the Editor.
 *  If we plan to use guarded sections, we'd need to implement that
 *  here. For now, this is used to get toggle-breakpoint behavior.
 */
public class CppEditorSupport extends DataEditorSupport implements EditorCookie, EditorCookie.Observable, OpenCookie, CloseCookie, PrintCookie {

    private long lastModified = 0;
    private ParsingListener wParsingL;
    private boolean parsingAttached;
    private static final ErrorManager log =
            ErrorManager.getDefault().getInstance("CppFoldTracer"); // NOI18N
    private final ParsingListener listener = new ParsingListener() {

        public void objectParsed(final ParsingEvent evt) {
            log.log("CES$ParserListener.objectParsed: " + evt); // NOI18N
        }
    };
    /** SaveCookie for this support instance. The cookie is adding/removing 
     * data object's cookie set depending on if modification flag was set/unset. */
    private final SaveCookie saveCookie = new SaveCookie() {

        /** Implements <code>SaveCookie</code> interface. */
        public void save() throws IOException {
            CppEditorSupport.this.saveDocument();
            CppEditorSupport.this.getDataObject().setModified(false);
        }
    };

    /**
     *  Create a new Editor support for the given C/C++/Fortran source.
     *  @param entry The (primary) file entry representing the C/C++/f95 source file
     */
    public CppEditorSupport(DataObject obj) {
        super(obj, new Environment(obj));

        // Add change listener. Note: This should be "addPropertyChange"!
        addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                // XXX - Need to update parser information...
                if (!isDocumentLoaded()) {
                    notifyClose();
                }
            }
        });
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

        lastModified = System.currentTimeMillis();
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
        CndDataObject obj = (CndDataObject) getDataObject();

        // Adds save cookie to the data object.
        if (obj.getCookie(SaveCookie.class) == null) {
            obj.addSaveCookie(saveCookie);
        }
    }

    /** Helper method. Removes save cookie from the data object. */
    private void removeSaveCookie() {
        CndDataObject obj = (CndDataObject) getDataObject();

        // Remove save cookie from the data object.
        Cookie cookie = obj.getCookie(SaveCookie.class);

        if (cookie != null && cookie.equals(saveCookie)) {
            obj.removeSaveCookie(saveCookie);
        }
    }
    /** True, if there's a visible editor component flying around */
    private boolean componentsCreated = false;

    /** Notify about the editor closing */
    protected void notifyClose() {
        componentsCreated = false;
    }

    /** Nested class. Environment for this support. Extends <code>DataEditorSupport.Env</code> abstract class. */
    private static class Environment extends DataEditorSupport.Env {

        private static final long serialVersionUID = 3035543168452715818L;

        /** Constructor. */
        public Environment(DataObject obj) {
            super(obj);
        }

        /** Implements abstract superclass method. */
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        /** Implements abstract superclass method.*/
        protected FileLock takeLock() throws IOException {
            return ((CndDataObject) getDataObject()).getPrimaryEntry().takeLock();
        }

        /** 
         * Overrides superclass method.
         * @return text editor support (instance of enclosing class)
         */
        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return getDataObject().getCookie(CppEditorSupport.class);
        }
    } // End of nested Environment class.    

    // ==================== Misc not-public methods ========================
    /** A method to create a new component. Overridden in subclasses.
     * @return the {@link HtmlEditor} for this support
     */
    @Override
    protected CloneableEditor createCloneableEditor() {
        return new CppEditorComponent(this);
    }

    /**
     *  The real component of the C/C++/f77 editor.
     *  Subclasses should not attempt to work with this;
     *  if they require special editing support, separate windows
     *  should be created by overriding (e.g.) {@link EditorSupport#open}.
     */
    public static class CppEditorComponent extends CloneableEditor {

        /** The support, subclass of EditorSupport */
        CppEditorSupport support = null;

        //static final long serialVersionUID =6223349196427270209L;
        /** Only for externalization */
        public CppEditorComponent() {
            super();
            initialize();
        }

        /** Creates new editor */
        public CppEditorComponent(CloneableEditorSupport sup) {
            super(sup);
            initialize();
        }

        /** Return the support object */
        public CppEditorSupport getSupport() {
            return support;
        }

        /** Obtain a support for this component */
        private void initialize() {
            support = (CppEditorSupport) cloneableEditorSupport();
        }

        /**
         *  This method is called when parent window of this component has focus,
         *  and this component is preferred one in it. This implementation adds 
         *  performer to the ToggleBreakpointAction.
         */
        @Override
        protected void componentActivated() {
            if (support.getDocument() != null) {
                log.log("CES.componentActivated: Activating " + getShortName() + // NOI18N
                        " [" + Thread.currentThread().getName() + "]"); // NOI18N
                if (activationPerformers != null) {
                    int n = activationPerformers.size();
                    for (int i = 0; i < n; i++) {
                        CppEditorActivationPerformer a = activationPerformers.get(i);
                        a.performActivation(this);
                    }
                }
                super.componentActivated();
                CppMetaModel.getDefault().scheduleParsing(support.getDocument());
                support.attachParsingListener();
            } else {
                log.log("CES.componentActivated: Activating without document!!!" + // NOI18N
                        " [" + Thread.currentThread().getName() + "]"); // NOI18N
            }
        }

        /* XXX -Debug method. Remove later? */
        private String getShortName() {
            String longname = (String) support.getDocument().getProperty(Document.TitleProperty);
            longname = longname == null ? "" : longname;
            int slash = longname.lastIndexOf(File.separatorChar);

            if (slash != -1) {
                return longname.substring(slash + 1);
            } else {
                return longname;
            }
        }

        /** Return the current line number */
        public int getLineNumber() {
            int l = NbDocument.findLineNumber(support.getDocument(),
                    getEditorPane().getCaret().getDot());
            return l;
        }

        /**
         * This method is called when parent window of this component losts focus,
         * or when this component loses preferrence in the parent window.
         */
        @Override
        protected void componentDeactivated() {
            support.removeParsingListener();
        }

        /**
         *  When closing last view, also close the document.
         *  @return <code>true</code> if close succeeded
         */
        @Override
        protected boolean closeLast() {
            if (!super.closeLast()) {
                return false;
            }
            if (support != null) {
                support.componentsCreated = false;
            }
            return true;
        }

        /**
         *  Deserialize this top component.
         *  @param in the stream to deserialize from
         */
        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            initialize();
        }
    } // end of CppEditorComponent inner class
    static Vector<CppEditorActivationPerformer> activationPerformers = null;

    /**
     *  Add an activation performer. This actionperformer will be called whenever
     *  this component is activated.
     */
    public static void addActivationPerformer(
            CppEditorActivationPerformer a) {
        if (activationPerformers == null) {
            activationPerformers = new Vector<CppEditorActivationPerformer>(2);
        }
        activationPerformers.add(a);
    }

    /**
     * Returns last modification timestamp or since file was opened, or 0
     * if not modifed at all. Note the timestamp doesn't get reset if file saved.
     */
    public long getLastModified() {
        return lastModified;
    }

    private synchronized void attachParsingListener() {
        if (!parsingAttached) {
            if (wParsingL == null) {
                wParsingL = new WParsingListener(listener);
            }
            CppMetaModel.getDefault().addParsingListener(wParsingL);
            parsingAttached = true;
        }
    }

    private synchronized void removeParsingListener() {
        if (parsingAttached) {
            CppMetaModel.getDefault().removeParsingListener(wParsingL);
            parsingAttached = false;
        }
    }

    static class WParsingListener extends WeakReference<ParsingListener> implements ParsingListener, Runnable {

        WParsingListener(ParsingListener orig) {
            super(orig, Utilities.activeReferenceQueue());
        }

        public void run() {
            CppMetaModel.getDefault().removeParsingListener(this);
        }

        ParsingListener getListener() {
            Object o = get();
            if (o == null) {
                CppMetaModel.getDefault().removeParsingListener(this);
            }
            return (ParsingListener) o;
        }

        public void objectParsed(ParsingEvent evt) {
            ParsingListener l = getListener();
            if (l != null) {
                l.objectParsed(evt);
            }
        }
    }
}
