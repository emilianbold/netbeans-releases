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

package org.netbeans.modules.gsf;

import java.io.IOException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.windows.CloneableOpenSupport;

public class GsfDataObject extends MultiDataObject {
    
    /** Used temporarily during file creation */
    private static Language templateLanguage;

    private GenericEditorSupport jes;
    private Language language;
    
    public GsfDataObject(FileObject pf, MultiFileLoader loader, Language language) throws DataObjectExistsException {
        super(pf, loader);

        // If the user creates a file with a filename where we can't figure out the language
        // (e.g. the PHP New File wizard doesn't enforce a file extension, so if you create
        // a file named "pie.class" (issue 124044) the data loader doesn't know which language
        // to associate this with since it isn't a GSF file extension or mimetype). However
        // during template creation we know the language anyway so we can use it. On subsequent
        // IDE restarts the file won't be recognized so the user will have to rename or
        // add a new file extension to file type mapping.
        if (language == null) {
            language = templateLanguage;
        }
        this.language = language;
        getCookieSet().assign( SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs( FileObject folder, String fileName ) throws IOException {
                createEditorSupport().saveAs( folder, fileName );
            }
        });
    }
    
    public @Override Node createNodeDelegate() {
        return new GsfDataNode(this, language);
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    public @Override <T extends Cookie> T getCookie(Class<T> type) {
        if (type.isAssignableFrom(GenericEditorSupport.class) && language != null) {
            return type.cast(createEditorSupport ());
        }
        return super.getCookie(type);
    }

    @Override
    protected DataObject handleCopyRename(DataFolder df, String name, String ext) throws IOException {
        FileObject fo = getPrimaryEntry ().copyRename (df.getPrimaryFile (), name, ext);
        DataObject dob = DataObject.find( fo );
        //TODO invoke refactoring here (if needed)
        return dob;
    }

    protected @Override DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
        if (name == null && language != null && language.getGsfLanguage().getPreferredExtension() != null) {
            // special case: name is null (unspecified or from one-parameter createFromTemplate)
            name = FileUtil.findFreeFileName(df.getPrimaryFile(),
                getPrimaryFile().getName(), language.getGsfLanguage().getPreferredExtension());
        } 
//        else if (!language.getGsfLanguage().isIdentifierChar(c) Utilities.isJavaIdentifier(name)) {
//            throw new IOException (NbBundle.getMessage(GsfDataObject.class, "FMT_Not_Valid_FileName", language.getDisplayName(), name));
//        }
        //IndentFileEntry entry = (IndentFileEntry)getPrimaryEntry();
        //entry.initializeIndentEngine();
        try {
            templateLanguage = language;
            DataObject retValue = super.handleCreateFromTemplate(df, name);
            FileObject fo = retValue.getPrimaryFile ();
            assert fo != null;
//        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//        String pkgName;
//        if (cp != null) {
//            pkgName = cp.getResourceName(fo.getParent(),'.',false);
//        }
//        else {
//            pkgName = "";   //NOI18N
//        }
//        renameJDO (retValue, pkgName, name, this.getPrimaryFile().getName());
            return retValue;
        } finally {
            templateLanguage = null;
        }
    }            
    
    
    private synchronized GenericEditorSupport createEditorSupport () {
        if (jes == null) {
            jes = new GenericEditorSupport(this, language);
        }
        return jes;
    }            
    
    public static final class GenericEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie, PrintCookie, EditorCookie.Observable {
        
        private static class Environment extends DataEditorSupport.Env {
            
            private static final long serialVersionUID = -1;
            
            private transient SaveSupport saveCookie = null;
            
            private class SaveSupport implements SaveCookie {
                public void save() throws java.io.IOException {
                    ((GenericEditorSupport)findCloneableOpenSupport()).saveDocument();
                    getDataObject().setModified(false);
                }
            }
            
            public Environment(GsfDataObject obj) {
                super(obj);
            }
            
            protected FileObject getFile() {
                return this.getDataObject().getPrimaryFile();
            }
            
            protected FileLock takeLock() throws java.io.IOException {
                return ((MultiDataObject)this.getDataObject()).getPrimaryEntry().takeLock();
            }
            
            public @Override CloneableOpenSupport findCloneableOpenSupport() {
                return (CloneableEditorSupport) ((GsfDataObject)this.getDataObject()).getCookie(EditorCookie.class);
            }
            
            
            public void addSaveCookie() {
                GsfDataObject javaData = (GsfDataObject) this.getDataObject();
                if (javaData.getCookie(SaveCookie.class) == null) {
                    if (this.saveCookie == null)
                        this.saveCookie = new SaveSupport();
                    javaData.getCookieSet().add(this.saveCookie);
                    javaData.setModified(true);
                }
            }
            
            public void removeSaveCookie() {
                GsfDataObject javaData = (GsfDataObject) this.getDataObject();
                if (javaData.getCookie(SaveCookie.class) != null) {
                    javaData.getCookieSet().remove(this.saveCookie);
                    javaData.setModified(false);
                }
            }
        }

        private Language language;

        public GenericEditorSupport(GsfDataObject dataObject, Language language) {
            super(dataObject, new Environment(dataObject));
            setMIMEType(language.getMimeType());
            this.language = language;
        }
        
        
        protected @Override boolean notifyModified() {
            if (!super.notifyModified())
                return false;
            ((Environment)this.env).addSaveCookie();
            return true;
        }
        
        
        protected @Override void notifyUnmodified() {
            super.notifyUnmodified();
            ((Environment)this.env).removeSaveCookie();
        }

//        protected @Override CloneableEditor createCloneableEditor() {
//            return new GsfEditor(this);
//        }
        
        public @Override boolean close(boolean ask) {
            return super.close(ask);
        }

        @Override
        protected StyledDocument createStyledDocument (EditorKit kit) {
            StyledDocument doc = super.createStyledDocument(kit);
            // Enter the file object in to InputAtrributes. It can be used by lexer.
            InputAttributes attributes = new InputAttributes();
            FileObject fileObject = NbEditorUtilities.getFileObject(doc);
            attributes.setValue(language.getGsfLanguage().getLexerLanguage(), FileObject.class, fileObject, false);
            doc.putProperty(InputAttributes.class, attributes);
            return doc;
        }

    }
}
