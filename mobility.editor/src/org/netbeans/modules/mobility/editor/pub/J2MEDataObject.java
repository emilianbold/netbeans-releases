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

/*
 * J2MEDataObject.java
 *
 * Created on February 20, 2004, 1:05 PM
 */
package org.netbeans.modules.mobility.editor.pub;

import org.netbeans.api.java.loaders.JavaDataSupport;
import org.netbeans.modules.mobility.editor.J2MENode;
import org.netbeans.modules.mobility.project.ApplicationDescriptorHandler;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;
import org.netbeans.mobility.antext.preprocessor.PreprocessorException;
import org.netbeans.modules.mobility.editor.J2MEKit;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.modules.mobility.project.TextSwitcher;
import org.netbeans.modules.mobility.snippets.SnippetsPaletteSupport;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.project.ProjectConfiguration;
import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node.Cookie;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.CloneableOpenSupport;

/**
 *
 * @author Adam Sotona
 */
public class J2MEDataObject extends MultiDataObject {
    static final long serialVersionUID = 8090017233591568305L;
    
    static final String ATTR_FILE_ENCODING = "Content-Encoding"; // NOI18N
    
    private J2MEEditorSupport jes;
    
    public J2MEDataObject(FileObject pf, MultiFileLoader loader)  throws DataObjectExistsException {
        super(pf,loader);
    }
    
    @Override
            public Node createNodeDelegate() {
        return new J2MENode(this, JavaDataSupport.createJavaNode(getPrimaryFile()));
    }
    
    protected FileObject handleRename(final String name) throws IOException {
        ApplicationDescriptorHandler.getDefault().handleRename(getPrimaryFile(), name);
        return super.handleRename(name);
    }
    
    protected FileObject handleMove(final DataFolder df) throws IOException {
        ApplicationDescriptorHandler.getDefault().handleMove(getPrimaryFile(), df.getPrimaryFile());
        return super.handleMove(df);
    }
    
    protected void handleDelete() throws java.io.IOException {
        ApplicationDescriptorHandler.getDefault().handleDelete(getPrimaryFile());
        super.handleDelete();
    }
    
    public @Override Cookie getCookie(Class type) {
        
        if (type.isAssignableFrom(J2MEEditorSupport.class)) {
            return createJavaEditorSupport();
        }
        return super.getCookie(type);
    }
    
    protected synchronized J2MEEditorSupport createJavaEditorSupport() {
        if (jes == null) {
            jes = new J2MEEditorSupport (this);
        }
        return jes;
    }

    public static class J2MEEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie, PrintCookie, EditorCookie.Observable {
        
        final private ProjectConfigurationsHelper pch;
        private static Method setAlreadyModified = null;
    
        private J2MEKit kit;
        
        static {
            try {
                setAlreadyModified = CloneableEditorSupport.class.getDeclaredMethod("setAlreadyModified", new Class[] {Boolean.TYPE}); //NOI18N
                setAlreadyModified.setAccessible(true);
            } catch (Exception e) {}
        }
        
        private static final class Environment extends DataEditorSupport.Env {
            
            private static final long serialVersionUID = -1;
            
            private transient SaveSupport saveCookie = null;
            
            private final class SaveSupport implements SaveCookie {
                public void save() throws java.io.IOException {
                    ((J2MEEditorSupport)findCloneableOpenSupport()).saveDocument();
                    getDataObject().setModified(false);
                }
            }
            
            public Environment(J2MEDataObject obj) {
                super(obj);
            }
            
            protected FileObject getFile() {
                return this.getDataObject().getPrimaryFile();
            }
            
            protected FileLock takeLock() throws java.io.IOException {
                return ((MultiDataObject)this.getDataObject()).getPrimaryEntry().takeLock();
            }
            
            public @Override CloneableOpenSupport findCloneableOpenSupport() {
                return (CloneableEditorSupport) ((J2MEDataObject)this.getDataObject()).getCookie(EditorCookie.class);
            }
            
            
            public void addSaveCookie() {
                J2MEDataObject javaData = (J2MEDataObject) this.getDataObject();
                if (javaData.getCookie(SaveCookie.class) == null) {
                    if (this.saveCookie == null)
                        this.saveCookie = new SaveSupport();
                    javaData.getCookieSet().add(this.saveCookie);
                    javaData.setModified(true);
                }
            }
            
            public void removeSaveCookie() {
                J2MEDataObject javaData = (J2MEDataObject) this.getDataObject();
                if (javaData.getCookie(SaveCookie.class) != null) {
                    javaData.getCookieSet().remove(this.saveCookie);
                    javaData.setModified(false);
                }
            }
        }
        
        public J2MEEditorSupport(J2MEDataObject dataObject) {
            super(dataObject, new Environment(dataObject));
            setMIMEType("text/x-java"); // NOI18N
            Project p = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
            pch = p == null ? null : (ProjectConfigurationsHelper)p.getLookup().lookup(ProjectConfigurationsHelper.class);
        }
        
        public static String getFileEncoding(FileObject someFile) {
            String enc = (String)someFile.getAttribute(ATTR_FILE_ENCODING);
            if (enc == null) {
//                enc = JavaSettings.getDefault().getDefaultEncoding();
                enc = System.getProperty("file.encoding"); //NOI18N
            }
            if ("".equals(enc))
                return null;
            else
                return enc;
        }
        
        protected void saveFromKitToStream(final StyledDocument doc, final EditorKit kit, final OutputStream stream) throws IOException, BadLocationException {
            // super.saveFromKitToStream called to handled guarded sections -- store the results in memory
            final ByteArrayOutputStream myStream = new ByteArrayOutputStream();
            saveFromKitToStreamHook (doc,kit,myStream);
            final String encoding = getEncoding ();
            final CommentingPreProcessor.Source ppSource = new CommentingPreProcessor.Source() {
                public Reader createReader() throws IOException {
                    return new StringReader(myStream.toString(encoding));
                }
            };
            final CommentingPreProcessor.Destination ppDestination = new CommentingPreProcessor.Destination() {
                public void doInsert( int line,  String s) {}
                
                public void doRemove( int line,  int column,  int length) {}
                
                public Writer createWriter( boolean validOutput) throws IOException {
                    return new OutputStreamWriter(stream, encoding);
                }

            };
            final ProjectConfiguration defCfg = pch == null ? null : pch.getDefaultConfiguration();
            final HashMap<String,String> identifiers = new HashMap<String,String>();
            if (defCfg !=null) {
                identifiers.putAll(pch.getAbilitiesFor(defCfg));
                identifiers.put(defCfg.getDisplayName(),null);
            }
            final CommentingPreProcessor cpp = new CommentingPreProcessor(ppSource, ppDestination, identifiers);
            try {
                cpp.run();
            } catch (PreprocessorException pe) {
                ErrorManager.getDefault().notify(pe);
            }
        }

        protected final String getEncoding () {
            final String enc = getFileEncoding(getDataObject().getPrimaryFile());
            final String encoding = enc == null ? System.getProperty("file.encoding") : enc; //NOI18N
            return encoding;
        }

        protected void saveFromKitToStreamHook (StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
            super.saveFromKitToStream (doc, kit, stream);
        }

        protected void loadFromStreamToKit(final StyledDocument doc, final InputStream stream, final EditorKit kit) throws IOException, BadLocationException {
            final String encoding = getEncoding ();

            final CommentingPreProcessor.Source ppSource = new CommentingPreProcessor.Source() {
                public Reader createReader() throws IOException {
                    return new InputStreamReader(stream, encoding);
                }
            };
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final OutputStreamWriter osw = new OutputStreamWriter(out, encoding);
            final CommentingPreProcessor.Destination ppDestination = new CommentingPreProcessor.Destination() {
                public void doInsert( int line,  String s)  {}

                public void doRemove( int line,  int column,  int length)  {}

                public Writer createWriter( boolean validOutput) {
                    return osw;
                }

            };
            final ProjectConfiguration conf = pch == null ? null : pch.getActiveConfiguration();
            final HashMap<String,String> identifiers=new HashMap<String,String>();
            if (conf != null) {
                identifiers.putAll(pch.getAbilitiesFor(conf));
                identifiers.put(conf.getDisplayName(),null);
            }
            final CommentingPreProcessor cpp =new CommentingPreProcessor(ppSource, ppDestination, identifiers);
            cpp.run();
            loadFromStreamToKitHook (doc, new ByteArrayInputStream(out.toByteArray()), kit);
        }

        protected void loadFromStreamToKitHook (StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
            super.loadFromStreamToKit (doc, stream, kit);
        }

            /** override to return j2me editor kit
            * @return editor kit
            */
        protected EditorKit createEditorKit() {
            if (kit == null) kit = new J2MEKit();
            return kit;
        }
        
        protected boolean notifyModified() {
            final Document document = getDocument();
            if ( document != null && document.getProperty(TextSwitcher.SKIP_DUCUMENT_CHANGES) != null && !isModified() && setAlreadyModified != null) try {
                setAlreadyModified.invoke(this, new Object[] {Boolean.FALSE});
                return true;
            } catch (Exception e) {}
            if (!super.notifyModified())
                return false;
            ((Environment)this.env).addSaveCookie();
            return true;
        }
        
        
        protected @Override void notifyUnmodified() {
            super.notifyUnmodified();
            ((Environment)this.env).removeSaveCookie();
        }
        
        protected @Override CloneableEditor createCloneableEditor() {
            return new J2MEEditor(this);
        }
        
        public @Override boolean close(boolean ask) {
            return super.close(ask);
        }
        
    }
    
    public static class J2MEEditor extends CloneableEditor {
        
        private static final long serialVersionUID = -1;
        
        public J2MEEditor() {
        }
        
        public J2MEEditor(J2MEEditorSupport sup) {
            super(sup);
            initialize();
        }
        
        void associatePalette(J2MEEditorSupport s) {
            try {
                DataObject dataObject = s.getDataObject();
                if (dataObject instanceof J2MEDataObject) {
                    PaletteController mController;
                    mController = SnippetsPaletteSupport.getPaletteController();
                    Lookup pcl = Lookups.singleton(mController);
                    Lookup anl = getActivatedNodes()[0].getLookup();
                    Lookup actionMap = Lookups.singleton(getActionMap());
                    ProxyLookup l = new ProxyLookup(new Lookup[] { anl, actionMap, pcl });
                    associateLookup(l);
                }
            }catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        private void initialize() {
            Node nodes[] = {((DataEditorSupport)cloneableEditorSupport()).getDataObject().getNodeDelegate()};
            setActivatedNodes(nodes);
            associatePalette((J2MEEditorSupport)cloneableEditorSupport());
        }
        
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            initialize();
        }
        
    }
}
