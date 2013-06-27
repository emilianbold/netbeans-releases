/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.SourceFlags;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.netbeans.modules.parsing.impl.indexing.IndexingManagerAccessor;
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
    private static final RequestProcessor RP = new RequestProcessor ("parsing-event-collector",1, false, false);       //NOI18N
    /** Default reparse - sliding window for editor events*/
    private static final int DEFAULT_REPARSE_DELAY = 500;
    /** Default reparse - sliding window for focus events*/
    private static final int IMMEDIATE_REPARSE_DELAY = 10;

    private static int reparseDelay = DEFAULT_REPARSE_DELAY;
    private static int immediateReparseDelay = IMMEDIATE_REPARSE_DELAY;

    private final Source source;
    private volatile boolean initialized;
    private DocListener docListener;
    private FileChangeListener fileChangeListener;
    private DataObjectListener dobjListener;
    private ChangeListener parserListener;
    
    private final RequestProcessor.Task resetTask = RP.create(new Runnable() {
        @Override
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
                Document doc;
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
                        LOGGER.log(Level.WARNING, "Ignoring events non existent file: {0}", FileUtil.getFileDisplayName(fo));     //NOI18N
                    }
                } else if ((doc=source.getDocument(false)) != null) {
                    docListener = new DocListener (doc);
                    parserListener = new ParserListener();                        
                    if (parser != null) {
                        parser.addChangeListener(parserListener);
                    }
                }
                initialized = true;
            }
        }
    }

    public void resetState (
        final boolean           invalidate,
        final boolean           mimeChanged,
        final int               startOffset,
        final int               endOffset,
        final boolean           fast) {
        final Set<SourceFlags> flags = EnumSet.of(SourceFlags.CHANGE_EXPECTED);
        if (invalidate) {
            flags.add(SourceFlags.INVALID);
            flags.add(SourceFlags.RESCHEDULE_FINISHED_TASKS);            
        }
        SourceAccessor.getINSTANCE().setSourceModification (source, invalidate, startOffset, endOffset);
        SourceAccessor.getINSTANCE().setFlags(this.source, flags);
        if (mimeChanged) {
            SourceAccessor.getINSTANCE().mimeTypeMayChanged(source);
        }
        TaskProcessor.resetState (this.source,invalidate,true);

        if (!EditorRegistryListener.k24.get()) {
            resetTask.schedule(getReparseDelay(fast));
        }
    }

    /**
     * Expert: Called by {@link IndexingManager#refreshIndexAndWait} to prevent
     * AWT deadlock. Never call this method in other cases.
     */
    public static void releaseCompletionCondition() {
        if (!IndexingManagerAccessor.getInstance().requiresReleaseOfCompletionLock() ||
            !IndexingManagerAccessor.getInstance().isCalledFromRefreshIndexAndWait()) {
            throw new IllegalStateException();
        }
        final boolean wask24 = EditorRegistryListener.k24.getAndSet(false);
        if (wask24) {
            TaskProcessor.resetStateImpl(null);
        }
    }

    /**
     * Sets the reparse delays.
     * Used by unit tests.
     */
    public static void setReparseDelays(
        final int standardReparseDelay,
        final int fastReparseDelay) throws IllegalArgumentException {
        if (standardReparseDelay < fastReparseDelay) {
            throw new IllegalArgumentException(
                    String.format(
                        "Fast reparse delay %d > standatd reparse delay %d",    //NOI18N
                        fastReparseDelay,
                        standardReparseDelay));
        }
        immediateReparseDelay = fastReparseDelay;
        reparseDelay = standardReparseDelay;
    }

    public static int getReparseDelay(final boolean fast) {
        return fast ? immediateReparseDelay : reparseDelay;
    }
    // <editor-fold defaultstate="collapsed" desc="Private implementation">

    /**
     * Not synchronized, only sets the atomic state and clears the listeners
     *
     */
    private void resetStateImpl() {
        if (!EditorRegistryListener.k24.get()) {
            //todo: threading flags cleaned in the TaskProcessor.resetStateImpl
            final boolean reschedule = SourceAccessor.getINSTANCE().testFlag (source, SourceFlags.RESCHEDULE_FINISHED_TASKS);
            if (reschedule) {
                //S ystem.out.println("reschedule");
                SourceAccessor.getINSTANCE ().getCache (source).sourceModified ();
                //S ystem.out.println("reschedule end");
            }
            assert this.source != null;
            TaskProcessor.resetStateImpl (this.source);
        }
    }
    
    private void assignDocumentListener(final DataObject od) {
        EditorCookie.Observable ec = od.getCookie(EditorCookie.Observable.class);
        if (ec != null) {
            docListener = new DocListener (ec);
        }
    }    
    
    private class DocListener implements PropertyChangeListener, DocumentListener, TokenHierarchyListener {
        
        private final EditorCookie.Observable ec;
        private DocumentListener docListener;
        private TokenHierarchyListener thListener;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public DocListener (final EditorCookie.Observable ec) {
            assert ec != null;
            this.ec = ec;
            this.ec.addPropertyChangeListener(WeakListeners.propertyChange(this, this.ec));
            final Document doc = source.getDocument(false);
            if (doc != null) {
                assignDocumentListener(doc);
            }
        }
        
        public DocListener(final Document doc) {
            assert doc != null;
            this.ec = null;
            assignDocumentListener(doc);
        }                

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
                Object old = evt.getOldValue();                
                if (old instanceof Document && docListener != null) {
                    Document doc = (Document) old;
                    TokenHierarchy th = TokenHierarchy.get(doc);
                    th.removeTokenHierarchyListener(thListener);
                    doc.removeDocumentListener(docListener);
                    thListener = null;
                    docListener = null;
                }                
                Document doc = source.getDocument(false);
                if (doc != null) {
                    assignDocumentListener(doc);
                    resetState(true, false, -1, -1, false);
                }                
            }
        }
        
        private void assignDocumentListener(final Document doc) {
            TokenHierarchy th = TokenHierarchy.get(doc);
            th.addTokenHierarchyListener(thListener = WeakListeners.create(TokenHierarchyListener.class, this,th));
            doc.addDocumentListener(docListener = WeakListeners.create(DocumentListener.class, this, doc));
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            TokenHierarchy th = TokenHierarchy.get(e.getDocument());
            if (th.isActive()) return ;//handled by the lexer based listener
            resetState (true, false, e.getOffset(), e.getOffset() + e.getLength(), false);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            TokenHierarchy th = TokenHierarchy.get(e.getDocument());
            if (th.isActive()) return;//handled by the lexer based listener
            resetState (true, false, e.getOffset(), e.getOffset(), false);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {}

        @Override
        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            resetState (true, false, evt.affectedStartOffset(), evt.affectedEndOffset(), false);
        }
    }
    
    private class ParserListener implements ChangeListener {
        
        @Override
        public void stateChanged(final ChangeEvent e) {
            resetState(true, false, -1, -1, false);
        }
    }
    
    private class FileChangeListenerImpl extends FileChangeAdapter {                
        
        @Override
        public void fileChanged(final FileEvent fe) {
            resetState(true, false, -1, -1, false);
        }        

        @Override
        public void fileRenamed(final FileRenameEvent fe) {
            final String oldExt = fe.getExt();
            final String newExt = fe.getFile().getExt();
            resetState(true, !Objects.equals(oldExt, newExt), -1, -1, false);
        }
    }
    
    private final class DataObjectListener implements PropertyChangeListener {
                     
        private DataObject dobj;
        private final FileObject fobj;
        private PropertyChangeListener wlistener;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public DataObjectListener(final DataObject dobj) {            
            this.dobj = dobj;
            this.fobj = dobj.getPrimaryFile();
            wlistener = WeakListeners.propertyChange(this, dobj);
            this.dobj.addPropertyChangeListener(wlistener);
        }
        
        @Override
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
            RP.post(new Runnable() {
                @Override
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
                    resetState(true, false, -1, -1, false);
                } catch (DataObjectNotFoundException e) {
                    //Ignore - invalidated after fobj.isValid () was called
                } catch (IOException ex) {
                    // should not occur
                    Exceptions.printStackTrace(ex);
                }
            }
        }        
    }

    //Public because of test
    public static class EditorRegistryListener implements CaretListener, PropertyChangeListener {

        private static final AtomicBoolean k24 = new AtomicBoolean();
                        
        private Reference<JTextComponent> lastEditorRef;
        
        private EditorRegistryListener () {
            EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    editorRegistryChanged();
                }
            });
            editorRegistryChanged();
        }
                
        private void editorRegistryChanged() {
            final JTextComponent editor = EditorRegistry.lastFocusedComponent();
            final JTextComponent lastEditor = lastEditorRef == null ? null : lastEditorRef.get();
            if (lastEditor != editor) {
                if (lastEditor != null) {
                    lastEditor.removeCaretListener(this);
                    lastEditor.removePropertyChangeListener(this);
                    k24.set(false);
                }
                lastEditorRef = new WeakReference<JTextComponent>(editor);
                if (editor != null) {
                    editor.addCaretListener(this);
                    editor.addPropertyChangeListener(this);
                }
                final JTextComponent focused = EditorRegistry.focusedComponent();
                if (focused != null) {
                    final Document doc = editor.getDocument();
                    final String mimeType = DocumentUtilities.getMimeType (doc);
                    if (doc != null && mimeType != null) {
                        final Source source = Source.create (doc);
                        if (source != null)
                            SourceAccessor.getINSTANCE().getEventSupport(source).resetState(true, false, -1, -1, true);
                    }
                }
            }
        }
        
        @Override
        public void caretUpdate(final CaretEvent event) {
            final JTextComponent lastEditor = lastEditorRef == null ? null : lastEditorRef.get();
            if (lastEditor != null) {
                Document doc = lastEditor.getDocument ();
                String mimeType = DocumentUtilities.getMimeType (doc);
                if (doc != null && mimeType != null) {
                    Source source = Source.create(doc);
                    if (source != null) {
                        SourceAccessor.getINSTANCE().getEventSupport(source).resetState(false, false, -1, -1, false);
                    }
                }
            }
        }

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if ("completion-active".equals(propName)) { //NOI18N
                Source source = null;
                final JTextComponent lastEditor = lastEditorRef == null ? null : lastEditorRef.get();
                final Document doc = lastEditor == null ? null : lastEditor.getDocument();
                if (doc != null) {
                    String mimeType = DocumentUtilities.getMimeType (doc);
                    if (mimeType != null) {
                        source = Source.create(doc);
                    }
                }
                if (source != null) {
                    handleCompletionActive(source, evt.getNewValue());
                }
            }
        }

        private void handleCompletionActive(
                final @NonNull Source source,
                final @NullAllowed Object rawValue) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "completion-active={0} for {1}", new Object [] { rawValue, source }); //NOI18N
            }
            if (rawValue instanceof Boolean && ((Boolean) rawValue).booleanValue()) {
                TaskProcessor.resetState(source, false, true);
                k24.set(true);
            } else {
                final EventSupport support = SourceAccessor.getINSTANCE().getEventSupport(source);
                k24.set(false);
                support.resetTask.schedule(0);
            }
        }
    }

    // </editor-fold>
}
