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

package org.openide.text;


import java.beans.*;
import java.io.*;
import java.lang.ref.Reference;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.*;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.openide.loaders.UIException;
import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.lookup.*;
import org.openide.windows.CloneableOpenSupport;

/**
 * Support for associating an editor and a Swing {@link Document} to a data object.
 * @author Jaroslav Tulach
 */
public class DataEditorSupport extends CloneableEditorSupport {
    /** error manager for CloneableEditorSupport logging and error reporting */
    static final Logger ERR = Logger.getLogger("org.openide.text.DataEditorSupport"); // NOI18N

    /** Which data object we are associated with */
    private final DataObject obj;
    /** listener to asociated node's events */
    private NodeListener nodeL;
    
    /** Editor support for a given data object. The file is taken from the
    * data object and is updated if the object moves or renames itself.
    * @param obj object to work with
    * @param env environment to pass to 
    */
    public DataEditorSupport (DataObject obj, CloneableEditorSupport.Env env) {
        super (env, new DOEnvLookup (obj));
        this.obj = obj;
    }
    
    /** Getter for the environment that was provided in the constructor.
    * @return the environment
    */
    final CloneableEditorSupport.Env desEnv() {
        return (CloneableEditorSupport.Env) env;
    }
    
    /** Factory method to create simple CloneableEditorSupport for a given
     * entry of a given DataObject. The common use inside DataObject looks like
     * this:
     * <pre>
     *  getCookieSet().add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), getCookieSet()));
     * </pre>
     *
     * @param obj the data object
     * @param entry the entry to read and write from
     * @param set cookie set to add remove additional cookies (currently only {@link org.openide.cookies.SaveCookie})
     * @return a subclass of DataEditorSupport that implements at least
     *   {@link org.openide.cookies.OpenCookie}, 
     *   {@link org.openide.cookies.EditCookie}, 
     *   {@link org.openide.cookies.EditorCookie.Observable}, 
     *   {@link org.openide.cookies.PrintCookie}, 
     *   {@link org.openide.cookies.CloseCookie}
     * @since 5.2
     */
    public static CloneableEditorSupport create (DataObject obj, MultiDataObject.Entry entry, org.openide.nodes.CookieSet set) {
        return new SimpleES (obj, entry, set);
    }
    
    /** Getter of the data object that this support is associated with.
    * @return data object passed in constructor
    */
    public final DataObject getDataObject () {
        return obj;
    }

    /** Message to display when an object is being opened.
    * @return the message or null if nothing should be displayed
    */
    protected String messageOpening () {
        return NbBundle.getMessage (DataObject.class , "CTL_ObjectOpen", // NOI18N
            obj.getPrimaryFile().getNameExt(),
            FileUtil.getFileDisplayName(obj.getPrimaryFile())
        );
    }
    

    /** Message to display when an object has been opened.
    * @return the message or null if nothing should be displayed
    */
    protected String messageOpened () {
        return NbBundle.getMessage (DataObject.class, "CTL_ObjectOpened", // NOI18N
            obj.getPrimaryFile().getNameExt(),
            FileUtil.getFileDisplayName(obj.getPrimaryFile())
        );
    }

    /** Constructs message that should be displayed when the data object
    * is modified and is being closed.
    *
    * @return text to show to the user
    */
    protected String messageSave () {
        return NbBundle.getMessage (
            DataObject.class,
            "MSG_SaveFile", // NOI18N
            obj.getPrimaryFile().getNameExt()
        );
    }
    
    /** Constructs message that should be used to name the editor component.
    *
    * @return name of the editor
    */
    protected String messageName () {
        if (! obj.isValid()) return ""; // NOI18N

        return addFlagsToName(obj.getNodeDelegate().getDisplayName());
    }
    
    protected String messageHtmlName() {
        if (! obj.isValid()) return null;

        String name = obj.getNodeDelegate().getHtmlDisplayName();
        if (name != null) {
            if (!name.startsWith("<html>")) {
                name = "<html>" + name;
            }
            name = addFlagsToName(name);
        }
        return name;
    }
        
    /** Helper only. */
    private String addFlagsToName(String name) {
        int version = 3;
        if (isModified ()) {
            if (!obj.getPrimaryFile().canWrite()) {
                version = 2;
            } else {
                version = 1;
            }
        } else {
            if (!obj.getPrimaryFile().canWrite()) {
                version = 0;
            }
        }

        return NbBundle.getMessage (DataObject.class, "LAB_EditorName",
		new Integer (version), name );
    }
    
    protected String documentID() {
        if (! obj.isValid()) return ""; // NOI18N
        return obj.getPrimaryFile().getName();
    }

    /** Text to use as tooltip for component.
    *
    * @return text to show to the user
    */
    protected String messageToolTip () {
        // update tooltip
        return FileUtil.getFileDisplayName(obj.getPrimaryFile());
    }
    
    /** Computes display name for a line based on the 
     * name of the associated DataObject and the line number.
     *
     * @param line the line object to compute display name for
     * @return display name for the line like "MyFile.java:243"
     *
     * @since 4.3
     */
    protected String messageLine (Line line) {
        return NbBundle.getMessage(DataObject.class, "FMT_LineDisplayName2",
            obj.getPrimaryFile().getNameExt(),
            FileUtil.getFileDisplayName(obj.getPrimaryFile()),
            new Integer(line.getLineNumber() + 1));
    }
    
    
    /** Annotates the editor with icon from the data object and also sets 
     * appropriate selected node. But only in the case the data object is valid.
     * This implementation also listen to display name and icon chamges of the
     * node and keeps editor top component up-to-date. If you override this
     * method and not call super, please note that you will have to keep things
     * synchronized yourself. 
     *
     * @param editor the editor that has been created and should be annotated
     */
    protected void initializeCloneableEditor (CloneableEditor editor) {
        // Prevention to bug similar to #17134. Don't call getNodeDelegate
        // on invalid data object. Top component should be discarded later.
        if(obj.isValid()) {
            Node ourNode = obj.getNodeDelegate();
            editor.setActivatedNodes (new Node[] { ourNode });
            editor.setIcon(ourNode.getIcon (java.beans.BeanInfo.ICON_COLOR_16x16));
            NodeListener nl = new DataNodeListener(editor);
            ourNode.addNodeListener(org.openide.nodes.NodeOp.weakNodeListener (nl, ourNode));
            nodeL = nl;
        }
    }

    /** Called when closed all components. Overrides superclass method,
     * also unregisters listening on node delegate. */
    protected void notifyClosed() {
        // #27645 All components were closed, unregister weak listener on node.
        nodeL = null;
        
        super.notifyClosed();
    }
    
    /** Let's the super method create the document and also annotates it
    * with Title and StreamDescription properities.
    *
    * @param kit kit to user to create the document
    * @return the document annotated by the properties
    */
    protected StyledDocument createStyledDocument (EditorKit kit) {
        StyledDocument doc = super.createStyledDocument (kit);
            
        // set document name property
        doc.putProperty(javax.swing.text.Document.TitleProperty,
            FileUtil.getFileDisplayName(obj.getPrimaryFile())
        );
        // set dataobject to stream desc property
        doc.putProperty(javax.swing.text.Document.StreamDescriptionProperty,
            obj
        );
        
        return doc;
    }

    /** Checks whether is possible to close support components.
     * Overrides superclass method, adds checking
     * for read-only property of saving file and warns user in that case. */
    protected boolean canClose() {
        if(desEnv().isModified() && isEnvReadOnly()) {
            Object result = DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(DataObject.class,
                        "MSG_FileReadOnlyClosing", 
                        new Object[] {((Env)env).getFileImpl().getNameExt()}),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE
            ));

            return result == NotifyDescriptor.OK_OPTION;
        }
        
        return super.canClose();
    }
    
    /**
     * @inheritDoc
     */
    @Override
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        final Charset c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
        final Reader r = new InputStreamReader (stream, c);
        try {
            kit.read(r, doc, 0);
        } finally {
            r.close();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        final Charset c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
        final Writer w = new OutputStreamWriter (stream, c);
        try {
            kit.write(w, doc, 0, doc.getLength());
        } finally {
            w.close();
        }
    }        

    /** Saves document. Overrides superclass method, adds checking
     * for read-only property of saving file and warns user in that case. */
    public void saveDocument() throws IOException {
        if(desEnv().isModified() && isEnvReadOnly()) {
            IOException e = new IOException("File is read-only: " + ((Env)env).getFileImpl()); // NOI18N
            UIException.annotateUser(e, null,
                                     org.openide.util.NbBundle.getMessage(org.openide.loaders.DataObject.class,
                                                                          "MSG_FileReadOnlySaving",
                                                                          new java.lang.Object[]{((org.openide.text.DataEditorSupport.Env) env).getFileImpl().getNameExt()}),
                                     null, null);
            throw e;
        }
        super.saveDocument();
    }

    /** Indicates whether the <code>Env</code> is read only. */
    boolean isEnvReadOnly() {
        CloneableEditorSupport.Env env = desEnv();
        return env instanceof Env && !((Env) env).getFileImpl().canWrite();
    }
    
    /** Needed for EditorSupport */
    final DataObject getDataObjectHack2 () {
        return obj;
    }
    
    /** Accessor for updateTitles.
     */
    final void callUpdateTitles () {
        updateTitles ();
    }
    
    /** Support method that extracts a DataObject from a Line. If the 
     * line is created by a DataEditorSupport then associated DataObject
     * can be accessed by this method.
     *
     * @param l line object 
     * @return data object or null
     *
     * @since 4.3
     */
    public static DataObject findDataObject (Line l) {
        if (l == null) throw new NullPointerException();
        return (DataObject)l.getLookup ().lookup (DataObject.class);
    }
    
    /** Environment that connects the data object and the CloneableEditorSupport.
    */
    public static abstract class Env extends OpenSupport.Env implements CloneableEditorSupport.Env {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -2945098431098324441L;

        /** The file object this environment is associated to.
        * This file object can be changed by a call to refresh file.
        */
        private transient FileObject fileObject;

        /** Lock acquired after the first modification and used in save.
        * Transient => is not serialized.
        * Not private for tests.
        */
        transient FileLock fileLock;
        /** did we warned about the size of the file?
         */
        private transient boolean warned;

        /** Constructor.
        * @param obj this support should be associated with
        */
        public Env (DataObject obj) {
            super (obj);
        }
        
        /** Getter for the file to work on.
        * @return the file
        */
        private FileObject getFileImpl () {
            // updates the file if there was a change
	    changeFile();
            return fileObject;
        }
        
        /** Getter for file associated with this environment.
        * @return the file input/output operation should be performed on
        */
        protected abstract FileObject getFile ();

        /** Locks the file.
        * @return the lock on the file getFile ()
        * @exception IOException if the file cannot be locked
        */
        protected abstract FileLock takeLock () throws IOException;
                
        /** Method that allows subclasses to notify this environment that
        * the file associated with this support has changed and that 
        * the environment should listen on modifications of different 
        * file object.
        */
        protected final void changeFile () {

            FileObject newFile = getFile ();
            
            if (newFile.equals (fileObject)) {
                // the file has not been updated
                return;
            }
            
            boolean lockAgain;
            if (fileLock != null) {
// <> NB #61818 In case the lock was not active (isValid() == false), the new lock was taken,
// which seems to be incorrect. There is taken a lock on new file, while it there wasn't on the old one.
//                fileLock.releaseLock ();
//                lockAgain = true;
// =====
                if(fileLock.isValid()) {
                    ERR.fine("changeFile releaseLock: " + fileLock + " for " + fileObject); // NOI18N
                    fileLock.releaseLock ();
                    lockAgain = true;
                } else {
                    fileLock = null;
                    lockAgain = false;
                }
// </>
            } else {
                lockAgain = false;
            }

            fileObject = newFile;
            ERR.fine("changeFile: " + newFile + " for " + fileObject); // NOI18N
            fileObject.addFileChangeListener (new EnvListener (this));

            if (lockAgain) { // refresh lock
                try {
                    fileLock = takeLock ();
                    ERR.fine("changeFile takeLock: " + fileLock + " for " + fileObject); // NOI18N
                } catch (IOException e) {
                    Logger.getLogger(DataEditorSupport.class.getName()).log(Level.WARNING, null, e);
                }
            }
            
        }
        
        
        /** Obtains the input stream.
        * @exception IOException if an I/O error occures
        */
        public InputStream inputStream() throws IOException {
            final FileObject fo = getFileImpl ();
            if (!warned && fo.getSize () > 1024 * 1024) {
                class ME extends org.openide.util.UserQuestionException {
                    private long size;
                    
                    public ME (long size) {
                        super ("The file is too big. " + size + " bytes.");
                        this.size = size;
                    }
                    
                    public String getLocalizedMessage () {
                        Object[] arr = {
                            fo.getPath (),
                            fo.getNameExt (),
                            new Long (size), // bytes
                            new Long (size / 1024 + 1), // kilobytes
                            new Long (size / (1024 * 1024)), // megabytes
                            new Long (size / (1024 * 1024 * 1024)), // gigabytes
                        };
                        return NbBundle.getMessage(DataObject.class, "MSG_ObjectIsTooBig", arr);
                    }
                    
                    public void confirmed () {
                        warned = true;
                    }
                }
                throw new ME (fo.getSize ());
            }
            InputStream is = getFileImpl ().getInputStream ();
            return is;
        }
        
        /** Obtains the output stream.
        * @exception IOException if an I/O error occures
        */
        public OutputStream outputStream() throws IOException {
            ERR.fine("outputStream: " + fileLock + " for " + fileObject); // NOI18N
            if (fileLock == null || !fileLock.isValid()) {
                fileLock = takeLock ();
            }
            ERR.fine("outputStream after takeLock: " + fileLock + " for " + fileObject); // NOI18N
            try {
                return getFileImpl ().getOutputStream (fileLock);
            } catch (IOException fse) {
	        // [pnejedly] just retry once.
		// Ugly workaround for #40552
                if (fileLock == null || !fileLock.isValid()) {
                    fileLock = takeLock ();
                }
                ERR.fine("ugly workaround for #40552: " + fileLock + " for " + fileObject); // NOI18N
                return getFileImpl ().getOutputStream (fileLock);
            }	    
        }
        
        /** The time when the data has been modified
        */
        public Date getTime() {
            // #32777 - refresh file object and return always the actual time
            getFileImpl().refresh(false);
            return getFileImpl ().lastModified ();
        }
        
        /** Mime type of the document.
        * @return the mime type to use for the document
        */
        public String getMimeType() {
            return getFileImpl ().getMIMEType ();
        }
        
        /** First of all tries to lock the primary file and
        * if it succeeds it marks the data object modified.
         * <p><b>Note: There is a contract (better saying a curse)
         * that this method has to call {@link #takeLock} method
         * in order to keep working some special filesystem's feature.
         * See <a href="http://www.netbeans.org/issues/show_bug.cgi?id=28212">issue #28212</a></b>.
        *
        * @exception IOException if the environment cannot be marked modified
        *   (for example when the file is readonly), when such exception
        *   is the support should discard all previous changes
         * @see  org.openide.filesystems.FileObject#isReadOnly
        */
        public void markModified() throws java.io.IOException {
            // XXX This shouldn't be here. But it is due to the 'contract',
            // see javadoc to this method.
            if (fileLock == null || !fileLock.isValid()) {
                fileLock = takeLock ();
            }
            ERR.fine("markModified: " + fileLock + " for " + fileObject); // NOI18N
            
            if (!getFileImpl().canWrite()) {
                if(fileLock != null && fileLock.isValid()) {
                    fileLock.releaseLock();
                }
                throw new IOException("File " // NOI18N
                    + getFileImpl().getNameExt() + " is read-only!"); // NOI18N
            }

            this.getDataObject ().setModified (true);
        }
        
        /** Reverse method that can be called to make the environment 
        * unmodified.
        */
        public void unmarkModified() {
            ERR.fine("unmarkModified: " + fileLock + " for " + fileObject); // NOI18N
            if (fileLock != null && fileLock.isValid()) {
                fileLock.releaseLock();
                ERR.fine("releaseLock: " + fileLock + " for " + fileObject); // NOI18N
            }
            
            this.getDataObject ().setModified (false);
        }
        
        /** Called from the EnvListener
        * @param expected is the change expected
        * @param time of the change
        */
        final void fileChanged (boolean expected, long time) {
            ERR.fine("fileChanged: " + expected + " for " + fileObject); // NOI18N
            if (expected) {
                // newValue = null means do not ask user whether to reload
                firePropertyChange (PROP_TIME, null, null);
            } else {
                firePropertyChange (PROP_TIME, null, new Date (time));
            }
        }

        /** Called from the <code>EnvListener</code>.
         * The components are going to be closed anyway and in case of
         * modified document its asked before if to save the change. */
        final void fileRemoved(boolean canBeVetoed) {
            /* JST: Do not do anything here, as there will be new call from
               the DataObject.markInvalid0
             
            if (canBeVetoed) {
                try {
                    // Causes the 'Save' dialog to show if necessary.
                    fireVetoableChange(Env.PROP_VALID, Boolean.TRUE, Boolean.FALSE);
                } catch(PropertyVetoException pve) {
                    // ok vetoed, keep the window open, but continue to veto for ever
                    // any subsequent veto messages from the data object
                }
            }
            
            // Closes the components.
            firePropertyChange(Env.PROP_VALID, Boolean.TRUE, Boolean.FALSE);            
             */
        }
        
        public CloneableOpenSupport findCloneableOpenSupport() {
            CloneableOpenSupport cos = super.findCloneableOpenSupport ();
            if (cos instanceof DataEditorSupport) {
                Object o = ((DataEditorSupport)cos).env;
                if (o != this && o instanceof Env) {
                   ((Env)o).warned = this.warned;
                }
            }
            return cos;
        }
        
        private void readObject (ObjectInputStream ois) throws ClassNotFoundException, IOException {
            ois.defaultReadObject ();
            warned = true;
        }
    } // end of Env
    
    /** Listener on file object that notifies the Env object
    * that a file has been modified.
    */
    private static final class EnvListener extends FileChangeAdapter {
        /** Reference (Env) */
        private Reference<Env> env;
        
        /** @param env environement to use
        */
        public EnvListener (Env env) {
            this.env = new java.lang.ref.WeakReference<Env> (env);
        }


        /** Handles <code>FileObject</code> deletion event. */
        public void fileDeleted(FileEvent fe) {
            Env env = this.env.get();
            FileObject fo = fe.getFile();
            if(env == null || env.getFileImpl() != fo) {
                // the Env change its file and we are not used
                // listener anymore => remove itself from the list of listeners
                fo.removeFileChangeListener(this);
                return;
            }
            
            fo.removeFileChangeListener(this);
            
            env.fileRemoved(true);
            fo.addFileChangeListener(this);
        }
        
        /** Fired when a file is changed.
        * @param fe the event describing context where action has taken place
        */
        public void fileChanged(FileEvent fe) {
            Env env = this.env.get ();
            if (env == null || env.getFileImpl () != fe.getFile ()) {
                // the Env change its file and we are not used
                // listener anymore => remove itself from the list of listeners
                fe.getFile ().removeFileChangeListener (this);
                return;
            }

            // #16403. Added handling for virtual property of the file.
            if(fe.getFile().isVirtual()) {
                // Remove file event coming as consequence of this change.
                fe.getFile().removeFileChangeListener(this);
                // File doesn't exist on disk -> simulate env is invalid,
                // even the fileObject could be valid, see VCS FS.
                env.fileRemoved(true);
                fe.getFile().addFileChangeListener(this);
            } else {
                env.fileChanged (fe.isExpected (), fe.getTime ());
            }
        }
                
    }
    
    /** Listener on node representing asociated data object, listens to the
     * property changes of the node and updates state properly
     */
    private final class DataNodeListener extends NodeAdapter {
        /** Asociated editor */
        CloneableEditor editor;
        
        DataNodeListener (CloneableEditor editor) {
            this.editor = editor;
        }
        
        public void propertyChange(final PropertyChangeEvent ev) {
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
            if (Node.PROP_DISPLAY_NAME.equals(ev.getPropertyName())) {
                callUpdateTitles();
            }
            if (Node.PROP_ICON.equals(ev.getPropertyName())) {
                if (obj.isValid()) {
                    editor.setIcon(obj.getNodeDelegate().getIcon (java.beans.BeanInfo.ICON_COLOR_16x16));
                }
            }
                }
            });
        }
        
    } // end of DataNodeListener

    /** Lookup that holds DataObject, its primary file and updates if that
     * changes.
     */
    private static class DOEnvLookup extends AbstractLookup 
    implements PropertyChangeListener {
        private DataObject dobj;
        private InstanceContent ic;
        
        public DOEnvLookup (DataObject dobj) {
            this (dobj, new InstanceContent ());
        }
        
        private DOEnvLookup (DataObject dobj, InstanceContent ic) {
            super (ic);
            this.ic = ic;
            this.dobj = dobj;
        	dobj.addPropertyChangeListener(WeakListeners.propertyChange(this, dobj));
     
            updateLookup ();
        }
        
        private void updateLookup() {
            ic.set(Arrays.asList(new Object[] { dobj, dobj.getPrimaryFile() }), null);
        }
        
        public void propertyChange(PropertyChangeEvent ev) {
            String propName = ev.getPropertyName();
            if (propName == null || propName == DataObject.PROP_PRIMARY_FILE) {
                updateLookup();
            }
        }
    }
    
}
