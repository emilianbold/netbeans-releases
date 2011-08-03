/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

/*
 * J2MEDataObject.java
 *
 * Created on February 20, 2004, 1:05 PM
 */
package org.netbeans.modules.mobility.editor.pub;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;

import org.netbeans.api.java.loaders.JavaDataSupport;
import org.netbeans.modules.mobility.editor.J2MENode;
import org.netbeans.modules.mobility.project.ApplicationDescriptorHandler;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;
import org.netbeans.mobility.antext.preprocessor.PreprocessorException;
import org.netbeans.modules.mobility.editor.DocumentPreprocessor;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.modules.mobility.project.TextSwitcher;
import org.netbeans.spi.project.ProjectConfiguration;
import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.Node.Cookie;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.TopComponent;

/**
 *
 * @author Adam Sotona
 */
public class J2MEDataObject extends MultiDataObject {
    static final long serialVersionUID = 8090017233591568305L;

    private static final String MIME_TYPE = "text/x-java"; // NOI18N
    static final String ATTR_FILE_ENCODING = "Content-Encoding"; // NOI18N
    
    private J2MEEditorSupport jes;

    public J2MEDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        getCookieSet().assign(SaveAsCapable.class, new SaveAsCapable() {
            @Override
            public void saveAs(FileObject folder, String fileName) throws IOException {
                createJavaEditorSupport().saveAs(folder, fileName);
            }
        });
//        registerEditor(MIME_TYPE, true);
    }

    @Override
    public Node createNodeDelegate() {
        return new J2MENode(this, JavaDataSupport.createJavaNode(getPrimaryFile()));
    }

    @Override
    protected FileObject handleRename(final String name) throws IOException {
        ApplicationDescriptorHandler.getDefault().handleRename(getPrimaryFile(), name);
        return super.handleRename(name);
    }

    @Override
    protected FileObject handleMove(final DataFolder df) throws IOException {
        ApplicationDescriptorHandler.getDefault().handleMove(getPrimaryFile(), df.getPrimaryFile());
        return super.handleMove(df);
    }

    @Override
    protected void handleDelete() throws java.io.IOException {
        ApplicationDescriptorHandler.getDefault().handleDelete(getPrimaryFile());
        super.handleDelete();
    }

//    @Override
//    protected int associateLookup() {
//        return 1;
//    }

    @Override
    public Lookup getLookup() {
        return new ProxyLookup((isValid() ? getNodeDelegate() : createNodeDelegate()).getLookup(),
                new Lookup() {
                    @Override
                    public <T> T lookup(Class<T> clazz) {
                        return (T) getCookie(clazz);
                    }

                    @Override
                    public <T> Result<T> lookup(final Template<T> template) {
                        return new Lookup.Result<T>() {
                            @Override
                            public void addLookupListener(LookupListener l) {}
                            
                            @Override
                            public void removeLookupListener(LookupListener l) {}
                            
                            @Override
                            public Collection<? extends T> allInstances() {
                                Cookie c = getCookie(template.getType());
                                return c == null ? Collections.EMPTY_SET : Collections.singleton((T) c);
                            }
                        };
                    }
                });
    }

    @Override
    public Cookie getCookie(Class type) {
        if (type.isAssignableFrom(J2MEEditorSupport.class)) {
            return createJavaEditorSupport();
        }
        return super.getCookie(type);
    }

//    @Messages("Source=&Source") // NOI18N
//    @MultiViewElement.Registration(displayName = "#Source", // NOI18N
//            iconBase = "org/netbeans/modules/mobility/editor/resources/class.gif", // NOI18N
//            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
//            mimeType = MIME_TYPE,
//            preferredID = "javame.source", // NOI18N
//            position = 1
//    )
//    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
//        return new MultiViewEditorElement(context);
//    }

    protected synchronized J2MEEditorSupport createJavaEditorSupport() {
        if (jes == null) {
            jes = new J2MEEditorSupport(this);
        }
        return jes;
    }

    public static class J2MEEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie, PrintCookie, EditorCookie.Observable {

        private ProjectConfigurationsHelper pch;
        private static Method setAlreadyModified = null;
        private static DocumentPreprocessor documentPreprocessor;

        static {
            try {
                setAlreadyModified = CloneableEditorSupport.class.getDeclaredMethod("setAlreadyModified", new Class[]{Boolean.TYPE}); //NOI18N
                setAlreadyModified.setAccessible(true);
            } catch (Exception e) {
            }
        }

        protected static class Environment extends DataEditorSupport.Env {

            private static final long serialVersionUID = -1;
            private transient SaveSupport saveCookie = null;

            private final class SaveSupport implements SaveCookie {
                @Override
                public void save() throws java.io.IOException {
                    ((J2MEEditorSupport) findCloneableOpenSupport()).saveDocument();
                    getDataObject().setModified(false);
                }
            }

            public Environment(J2MEDataObject obj) {
                super(obj);
            }

            @Override
            protected FileObject getFile() {
                return this.getDataObject().getPrimaryFile();
            }

            @Override
            protected FileLock takeLock() throws java.io.IOException {
                return ((MultiDataObject) this.getDataObject()).getPrimaryEntry().takeLock();
            }

            @Override
            public CloneableOpenSupport findCloneableOpenSupport() {
                return (CloneableEditorSupport) ((J2MEDataObject) this.getDataObject()).getCookie(EditorCookie.class);
            }

            public void addSaveCookie() {
                J2MEDataObject javaData = (J2MEDataObject) this.getDataObject();
                if (javaData.getCookie(SaveCookie.class) == null) {
                    if (this.saveCookie == null) {
                        this.saveCookie = new SaveSupport();
                    }
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
            this(dataObject, new Environment(dataObject));
        }

        public J2MEEditorSupport(J2MEDataObject dataObject, CloneableEditorSupport.Env env) {
            super(dataObject, env);
            setMIMEType(MIME_TYPE);
        }

        @Override
        protected void saveFromKitToStream(final StyledDocument doc, final EditorKit kit, final OutputStream stream) throws IOException, BadLocationException {
            if (pch == null) {
                Project p = FileOwnerQuery.getOwner(getDataObject().getPrimaryFile());
                pch = p == null ? null : (ProjectConfigurationsHelper) p.getLookup().lookup(ProjectConfigurationsHelper.class);
            }
            if (pch == null || !pch.isPreprocessorOn()) {
                saveFromKitToStreamHook(doc, kit, stream);
                return;
            }
            // super.saveFromKitToStream called to handled guarded sections -- store the results in memory
            final ByteArrayOutputStream myStream = new ByteArrayOutputStream();
            saveFromKitToStreamHook(doc, kit, myStream);
            final String encoding = getEncoding().name();
            final CommentingPreProcessor.Source ppSource = new CommentingPreProcessor.Source() {
                @Override
                public Reader createReader() throws IOException {
                    return new StringReader(myStream.toString(encoding));
                }
            };
            final CommentingPreProcessor.Destination ppDestination = new CommentingPreProcessor.Destination() {
                @Override
                public void doInsert(int line, String s) {}

                @Override
                public void doRemove(int line, int column, int length) {}

                @Override
                public Writer createWriter(boolean validOutput) throws IOException {
                    return new OutputStreamWriter(stream, encoding);
                }
            };
            final ProjectConfiguration defCfg = pch == null ? null : pch.getDefaultConfiguration();
            final HashMap<String, String> identifiers = new HashMap<String, String>();
            if (defCfg != null) {
                identifiers.putAll(pch.getAbilitiesFor(defCfg));
                identifiers.put(defCfg.getDisplayName(), null);
            }
            final CommentingPreProcessor cpp = new CommentingPreProcessor(ppSource, ppDestination, identifiers);
            try {
                cpp.run();
            } catch (PreprocessorException pe) {
                ErrorManager.getDefault().notify(pe);
            }
        }

        protected final Charset getEncoding() {
            return FileEncodingQuery.getEncoding(getDataObject().getPrimaryFile());
        }

        protected void saveFromKitToStreamHook(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
            super.saveFromKitToStream(doc, kit, stream);
        }

        @Override
        protected void loadFromStreamToKit(final StyledDocument doc, final InputStream stream, final EditorKit kit) throws IOException, BadLocationException {
            if (pch == null) {
                Project p = FileOwnerQuery.getOwner(getDataObject().getPrimaryFile());
                pch = p == null ? null : (ProjectConfigurationsHelper) p.getLookup().lookup(ProjectConfigurationsHelper.class);
            }
            if (pch == null || !pch.isPreprocessorOn()) {
                loadFromStreamToKitHook(doc, stream, kit);
                return;
            }
            final Charset encoding = getEncoding();

            final CommentingPreProcessor.Source ppSource = new CommentingPreProcessor.Source() {
                @Override
                public Reader createReader() throws IOException {
                    return new InputStreamReader(stream, encoding);
                }
            };
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final OutputStreamWriter osw = new OutputStreamWriter(out, encoding);
            final CommentingPreProcessor.Destination ppDestination = new CommentingPreProcessor.Destination() {
                @Override
                public void doInsert(int line, String s) {}

                @Override
                public void doRemove(int line, int column, int length) {}

                @Override
                public Writer createWriter(boolean validOutput) {
                    return osw;
                }
            };
            final ProjectConfiguration conf = pch == null ? null : pch.getActiveConfiguration();
            final HashMap<String, String> identifiers = new HashMap<String, String>();
            if (conf != null) {
                identifiers.putAll(pch.getAbilitiesFor(conf));
                identifiers.put(conf.getDisplayName(), null);
            }
            final CommentingPreProcessor cpp = new CommentingPreProcessor(ppSource, ppDestination, identifiers);
            cpp.run();
            loadFromStreamToKitHook(doc, new ByteArrayInputStream(out.toByteArray()), kit);
        }

        protected void loadFromStreamToKitHook(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
            super.loadFromStreamToKit(doc, stream, kit);
        }

        /** override to return j2me editor kit
         * @return editor kit
         */
        @Override
        protected EditorKit createEditorKit() {
            if (documentPreprocessor == null) {
                documentPreprocessor = new DocumentPreprocessor();
                EditorRegistry.addPropertyChangeListener(documentPreprocessor);
            }
            return super.createEditorKit();
        }

        @Override
        protected boolean notifyModified() {
            final Document document = getDocument();
            if (document != null && document.getProperty(TextSwitcher.SKIP_DUCUMENT_CHANGES) != null && !isModified() && setAlreadyModified != null) {
                try {
                    setAlreadyModified.invoke(this, new Object[]{Boolean.FALSE});
                    return true;
                } catch (Exception e) {
                }
            }
            if (!super.notifyModified()) {
                return false;
            }
            ((Environment) this.env).addSaveCookie();
            return true;
        }

        protected @Override
        void notifyUnmodified() {
            super.notifyUnmodified();
            ((Environment) this.env).removeSaveCookie();
        }

        public @Override
        boolean close(boolean ask) {
            return super.close(ask);
        }
    }
}
