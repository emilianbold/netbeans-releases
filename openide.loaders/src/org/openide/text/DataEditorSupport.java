/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.text;

import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.lang.ref.Reference;

import javax.swing.text.*;
import org.openide.DialogDisplayer;

import org.openide.actions.*;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeListener;
import org.openide.loaders.*;
import org.openide.util.Mutex;
import org.openide.windows.*;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.lookup.*;

/** Support for associating an editor and a Swing {@link Document} to a data object.
* 
*
* @author Jaroslav Tulach
*/
public class DataEditorSupport extends CloneableEditorSupport {
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
        super (env, createLookup(obj));
        this.obj = obj;
    }
    
    /** Factory method to create simple CloneableEditorSupport for a given
     * entry of a given DataObject. The common use inside DataObject looks like
     * this:
     * <pre>
     *  Node.Cookie cookie = (Node.Cookie)DataEditorSupport.create(this, getPrimaryEntry(), getCookieSet ());
     *  getCookieSet ().add (cookie);
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

        String name;
        if(DataNode.getShowFileExtensions()) {
            name = obj.getPrimaryFile().getNameExt();
        } else {
            name = obj.getPrimaryFile().getName();
        }
        return addFlagsToName(name);
    }
        
    /** Helper only. */
    private String addFlagsToName(String name) {
        int version = 3;
        if (isModified ()) {
            if (obj.getPrimaryFile ().isReadOnly ()) {
                version = 2;
            } else {
                version = 1;
            }
        } else
        if (obj.getPrimaryFile ().isReadOnly ()) {
            version = 0;
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
        if(env().isModified() && isEnvReadOnly()) {
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

    /** Saves document. Overrides superclass method, adds checking
     * for read-only property of saving file and warns user in that case. */
    public void saveDocument() throws IOException {
        if(env().isModified() && isEnvReadOnly()) {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(
                    NbBundle.getMessage(DataObject.class,
                        "MSG_FileReadOnlySaving", 
                        new Object[] {((Env)env).getFileImpl().getNameExt()}),
                NotifyDescriptor.WARNING_MESSAGE
            ));
            return;
        }
        super.saveDocument();
    }

    /** Indicates whether the <code>Env</code> is read only. */
    boolean isEnvReadOnly() {
        CloneableEditorSupport.Env env = env();
        return env instanceof Env && ((Env)env).getFileImpl().isReadOnly();
    }
    
    /** Needed for EditorSupport */
    final DataObject getDataObjectHack2 () {
        return obj;
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
    public static abstract class Env extends OpenSupport.Env 
    implements CloneableEditorSupport.Env, java.io.Serializable,
    PropertyChangeListener, VetoableChangeListener {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -2945098431098324441L;

        /** The file object this environment is associated to.
        * This file object can be changed by a call to refresh file.
        */
        private transient FileObject fileObject;

        /** Lock acquired after the first modification and used in save.
        * Transient => is not serialized.
        */
        private transient FileLock fileLock;
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
                fileLock.releaseLock ();
                lockAgain = true;
            } else {
                lockAgain = false;
            }

            fileObject = newFile;
            fileObject.addFileChangeListener (new EnvListener (this));

            if (lockAgain) { // refresh lock
                try {
                    fileLock = takeLock ();
                } catch (IOException e) {
                    ErrorManager.getDefault ().notify (
                	    ErrorManager.INFORMATIONAL, e);
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
            if (fileLock == null || !fileLock.isValid()) {
                fileLock = takeLock ();
            }
            try {
                return getFileImpl ().getOutputStream (fileLock);
            } catch (IOException fse) {
	        // [pnejedly] just retry once.
		// Ugly workaround for #40552
                if (fileLock == null || !fileLock.isValid()) {
                    fileLock = takeLock ();
                }
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
            
            if(getFileImpl().isReadOnly()) {
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
            if (fileLock != null && fileLock.isValid()) {
                fileLock.releaseLock();
            }
            
            this.getDataObject ().setModified (false);
        }
        
        /** Called from the EnvListener
        * @param expected is the change expected
        * @param time of the change
        */
        final void fileChanged (boolean expected, long time) {
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
        private void fileRemoved(boolean canBeVetoed) {
            if (canBeVetoed) {
                try {
                    // Causes the 'Save' dialog to show if necessary.
                    fireVetoableChange(Env.PROP_VALID, Boolean.TRUE, Boolean.FALSE);
                } catch(PropertyVetoException pve) {
                    // Ignore it and close anyway. File doesn't exist anymore.
                }
            }
            
            // Closes the components.
            firePropertyChange(Env.PROP_VALID, Boolean.TRUE, Boolean.FALSE);            
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
        private Reference env;
        
        /** @param env environement to use
        */
        public EnvListener (Env env) {
            this.env = new java.lang.ref.WeakReference (env);
        }


        /** Handles <code>FileObject</code> deletion event. */
        public void fileDeleted(FileEvent fe) {
            Env env = (Env)this.env.get();
            FileObject fo = fe.getFile();
            if(env == null || env.getFileImpl() != fo) {
                // the Env change its file and we are not used
                // listener anymore => remove itself from the list of listeners
                fo.removeFileChangeListener(this);
                return;
            }
            
            fo.removeFileChangeListener(this);
            
            // #30210 - when edited file was deleted the "Do you want to save changes"
            // dialog should not be shown 
            env.fileRemoved(false);
            fo.addFileChangeListener(this);
        }
        
        /** Fired when a file is changed.
        * @param fe the event describing context where action has taken place
        */
        public void fileChanged(FileEvent fe) {
            Env env = (Env)this.env.get ();
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
                updateTitles();
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
    
    /* Create a special lookup implementation that contains a DataObject and its
     * primary fileobject. If the file is moved, the FileObject is replaced,
     * while the DataObject keeps the identity.
     */
    private static Lookup createLookup(final DataObject dobj) {
	final InstanceContent ic = new InstanceContent();
	Lookup l = new AbstractLookup(ic);
	dobj.addPropertyChangeListener(new PropertyChangeListener() {
	    public void propertyChange(PropertyChangeEvent ev) {
		String propName = ev.getPropertyName();
		if (propName == null || propName == DataObject.PROP_PRIMARY_FILE) {
		    updateLookup(dobj, ic);
		}
	    }
	});
	updateLookup(dobj,ic);
	return l;
    }
    
    private static void updateLookup(DataObject d, InstanceContent ic) {
	ic.set(Arrays.asList(new Object[] { d, d.getPrimaryFile() }), null);
    }
    
}
