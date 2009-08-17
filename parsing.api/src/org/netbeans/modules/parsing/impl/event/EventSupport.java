/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.event;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.SourceFlags;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;


/**
 *
 * @author Tomas Zezula
 */
public final class EventSupport {
    
    private static final Logger LOGGER = Logger.getLogger(EventSupport.class.getName());

    // make sure that RP is initialized before anything else happens (#163056)
    private static final RequestProcessor RP = new RequestProcessor ("parsing-event-collector",1);       //NOI18N

    private final Source source;
    private volatile boolean initialized;
    private volatile boolean k24;
    private DocListener docListener;
    private FileChangeListener fileChangeListener;
    private DataObjectListener dobjListener;
    private ChangeListener parserListener;
    
    private final RequestProcessor.Task resetTask = RP.create(new Runnable() {
        public void run() {
            resetStateImpl();
        }
    });

    private static final EditorRegistryListener editorRegistryListener  = new EditorRegistryListener();
    
    public EventSupport (final Source source) {
        assert source != null;
        this.source = source;
    }
    
    public void init () {
        if (initialized) {
            return;
        }
        final Parser parser = SourceAccessor.getINSTANCE ().getCache (source).getParser ();
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            if (!initialized) {
                final FileObject fo = source.getFileObject();
                if (fo != null) {
                    try {
                        fileChangeListener = new FileChangeListenerImpl();
                        fo.addFileChangeListener(FileUtil.weakFileChangeListener(this.fileChangeListener,fo));
                        DataObject dObj = DataObject.find(fo);
                        assignDocumentListener (dObj);
                        dobjListener = new DataObjectListener(dObj);
                        parserListener = new ParserListener();                        
                        if (parser != null) {
                            parser.addChangeListener(parserListener);
                        }
                    } catch (DataObjectNotFoundException e) {
                        LOGGER.warning("Ignoring events non existent file: " + FileUtil.getFileDisplayName(fo));     //NOI18N
                    }
                }
                initialized = true;
            }
        }
    }   
    
    public void resetState (
        final boolean           invalidate,
        final int               startOffset,
        final int               endOffset
    ) {
        final Set<SourceFlags> flags = EnumSet.of(SourceFlags.CHANGE_EXPECTED);
        if (invalidate) {
            flags.add(SourceFlags.INVALID);
            flags.add(SourceFlags.RESCHEDULE_FINISHED_TASKS);
            SourceAccessor.getINSTANCE().setSourceModification (source, startOffset, endOffset);
        }
        SourceAccessor.getINSTANCE().setFlags(this.source, flags);
        TaskProcessor.resetState (this.source,invalidate,true);
        
        if (!k24) {
            resetTask.schedule(TaskProcessor.reparseDelay);
        }
    }
    
    /**
     * Not synchronized, only sets the atomic state and clears the listeners
     *
     */
    private void resetStateImpl() {
        if (!k24) {
            //todo: threading flags cleaned in the TaskProcessor.resetStateImpl
            final boolean reschedule = SourceAccessor.getINSTANCE().testFlag (source, SourceFlags.RESCHEDULE_FINISHED_TASKS);
            if (reschedule) {
                //S ystem.out.println("reschedule");
                SourceAccessor.getINSTANCE ().getCache (source).sourceModified ();
                //S ystem.out.println("reschedule end");
            }
            TaskProcessor.resetStateImpl (this.source);
        }
    }
    
    private void assignDocumentListener(final DataObject od) {
        EditorCookie.Observable ec = od.getCookie(EditorCookie.Observable.class);
        if (ec != null) {
            docListener = new DocListener (ec);
        }
    }    
    
    private class DocListener implements PropertyChangeListener, TokenHierarchyListener {
        
        private EditorCookie.Observable ec;
        private TokenHierarchyListener lexListener;
        
        public DocListener (final EditorCookie.Observable ec) {
            assert ec != null;
            this.ec = ec;
            this.ec.addPropertyChangeListener(WeakListeners.propertyChange(this, this.ec));
            Document doc = source.getDocument(false);
            if (doc != null) {
                TokenHierarchy th = TokenHierarchy.get(doc);
                th.addTokenHierarchyListener(lexListener = WeakListeners.create(TokenHierarchyListener.class, this,th));
            }
        }

        public void propertyChange(final PropertyChangeEvent evt) {
            if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
                Object old = evt.getOldValue();                
                if (old instanceof Document && lexListener != null) {
                    TokenHierarchy th = TokenHierarchy.get((Document) old);
                    th.removeTokenHierarchyListener(lexListener);
                    lexListener = null;
                }                
                Document doc = source.getDocument(false);
                if (doc != null) {
                    TokenHierarchy th = TokenHierarchy.get(doc);
                    th.addTokenHierarchyListener(lexListener = WeakListeners.create(TokenHierarchyListener.class, this,th));
                    resetState(true, -1, -1);
                }                
            }
        }
        
        public void tokenHierarchyChanged(final TokenHierarchyEvent evt) {
            resetState (true, evt.affectedStartOffset (), evt.affectedEndOffset ());
        }
    }
    
    private class ParserListener implements ChangeListener {
        
        public void stateChanged(final ChangeEvent e) {
            resetState(true, -1, -1);
        }
    }
    
    private class FileChangeListenerImpl extends FileChangeAdapter {                
        
        @Override
        public void fileChanged(final FileEvent fe) {
            resetState(true, -1, -1);
        }        

        @Override
        public void fileRenamed(final FileRenameEvent fe) {
            resetState(true, -1, -1);
        }
    }
    
    private final class DataObjectListener implements PropertyChangeListener {
                     
        private DataObject dobj;
        private final FileObject fobj;
        private PropertyChangeListener wlistener;
        
        public DataObjectListener(final DataObject dobj) {            
            this.dobj = dobj;
            this.fobj = dobj.getPrimaryFile();
            wlistener = WeakListeners.propertyChange(this, dobj);
            this.dobj.addPropertyChangeListener(wlistener);
        }
        
        public void propertyChange(PropertyChangeEvent pce) {
            DataObject invalidDO = (DataObject) pce.getSource();
            if (invalidDO != dobj)
                return;
            final String propName = pce.getPropertyName();
            if (DataObject.PROP_VALID.equals(propName)) {
                handleInvalidDataObject(invalidDO);
            } else if (pce.getPropertyName() == null && !dobj.isValid()) {
                handleInvalidDataObject(invalidDO);
            }            
        }
        
        private void handleInvalidDataObject(final DataObject invalidDO) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    handleInvalidDataObjectImpl(invalidDO);
                }
            });
        }
        
        private void handleInvalidDataObjectImpl(DataObject invalidDO) {
            invalidDO.removePropertyChangeListener(wlistener);
            if (fobj.isValid()) {
                // file object still exists try to find new data object
                try {
                    DataObject dobjNew = DataObject.find(fobj);
                    synchronized (DataObjectListener.this) {
                        if (dobjNew == dobj) {
                            return;
                        }
                        dobj = dobjNew;
                        dobj.addPropertyChangeListener(wlistener);
                    }
                    assignDocumentListener(dobjNew);
                    resetState(true, -1, -1);
                } catch (DataObjectNotFoundException e) {
                    //Ignore - invalidated after fobj.isValid () was called
                } catch (IOException ex) {
                    // should not occur
                    Exceptions.printStackTrace(ex);
                }
            }
        }        
    }
    
    private static class EditorRegistryListener implements CaretListener, PropertyChangeListener {
                        
        private TaskProcessor.Request request;
        private Reference<JTextComponent> lastEditorRef;
        
        public EditorRegistryListener () {
            EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    editorRegistryChanged();
                }
            });
            editorRegistryChanged();
        }
                
        public void editorRegistryChanged() {
            final JTextComponent editor = EditorRegistry.lastFocusedComponent();
            final JTextComponent lastEditor = lastEditorRef == null ? null : lastEditorRef.get();
            if (lastEditor != editor) {
                if (lastEditor != null) {
                    lastEditor.removeCaretListener(this);
                    lastEditor.removePropertyChangeListener(this);
                    final Document document = lastEditor.getDocument();
                    Source source = null;
                    if (document != null) {
                        String mimeType = NbEditorUtilities.getMimeType (document);
                        if (mimeType != null)
                            source = Source.create (document);
                    }
                    if (source != null) {
                        TaskProcessor.Request _request = request;
                        request = null;
                        EventSupport support = SourceAccessor.getINSTANCE ().getEventSupport (source);
                        support.k24 = false;
                        if (_request != null) {
                            support.resetTask.schedule (TaskProcessor.reparseDelay);
                            TaskProcessor.resetStateImplAsync (_request);
                        }
                    }
                }
                lastEditorRef = new WeakReference<JTextComponent>(editor);
                if (editor != null) {
                    editor.addCaretListener(this);
                    editor.addPropertyChangeListener(this);
                }
                final JTextComponent focused = EditorRegistry.focusedComponent();
                if (focused != null) {
                    final Document doc = editor.getDocument();
                    final Source source = doc == null ? null : Source.create(doc);
                    if (source != null) {
                        SourceAccessor.getINSTANCE().getEventSupport(source).resetState(true, -1, -1);
                    }
                }
            }
        }
        
        public void caretUpdate(final CaretEvent event) {
            final JTextComponent lastEditor = lastEditorRef == null ? null : lastEditorRef.get();
            if (lastEditor != null) {
                Document doc = lastEditor.getDocument ();
                String mimeType = NbEditorUtilities.getMimeType (doc);
                if (doc != null && mimeType != null) {
                    Source source = Source.create(doc);
                    if (source != null) {
                        SourceAccessor.getINSTANCE().getEventSupport(source).resetState(false, -1, -1);
                    }
                }
            }
        }

        public void propertyChange(final PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if ("completion-active".equals(propName)) { //NOI18N
                Source source = null;
                final JTextComponent lastEditor = lastEditorRef == null ? null : lastEditorRef.get();
                final Document doc = lastEditor == null ? null : lastEditor.getDocument();
                if (doc != null) {
                    source = Source.create(doc);
                }
                if (source != null) {
                    Object rawValue = evt.getNewValue();
                    if (rawValue instanceof Boolean && ((Boolean) rawValue).booleanValue()) {
                        assert this.request == null;
                        this.request = TaskProcessor.resetState(source, false, false);
                        SourceAccessor.getINSTANCE().getEventSupport(source).k24 = true;
                    } else {
                        TaskProcessor.Request _request = this.request;
                        this.request = null;
                        final EventSupport support = SourceAccessor.getINSTANCE().getEventSupport(source);
                        support.k24 = false;
                        support.resetTask.schedule(TaskProcessor.reparseDelay);
                        TaskProcessor.resetStateImplAsync(_request);
                    }
                }
            }
        }        
    }

}
