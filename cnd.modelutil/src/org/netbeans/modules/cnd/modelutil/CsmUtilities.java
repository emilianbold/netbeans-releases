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
package org.netbeans.modules.cnd.modelutil;

import java.awt.Container;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.*;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.JumpList;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.services.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.modelutil.spi.FileObjectRedirector;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditorCookie.Observable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.UserQuestionException;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmUtilities {

    /* ------------------ MODIFIERS ---------------------- */
    /**
     * The <code>int</code> value representing the <code>public</code>
     * modifier.
     */
    public static final int PUBLIC = 0x00000001;
    /**
     * The <code>int</code> value representing the <code>private</code>
     * modifier.
     */
    public static final int PRIVATE = 0x00000002;
    /**
     * The <code>int</code> value representing the <code>protected</code>
     * modifier.
     */
    public static final int PROTECTED = 0x00000004;
    /**
     * The <code>int</code> value representing the <code>static</code>
     * modifier.
     */
    public static final int STATIC = 0x00000008;
    // the bit for local member. the modificator is not saved within this bit.
    public static final int LOCAL_MEMBER_BIT = 0x00000100;

    // the bit for local member. the modificator is not saved within this bit.
    public static final int PUBLIC_LEVEL = 2;
    public static final int PROTECTED_LEVEL = 1;
    public static final int PRIVATE_LEVEL = 0;
    public static final int CONST_MEMBER_BIT= 0x00000200;
    public static final int ENUMERATOR      = 0x00000400;
    public static final int CONSTRUCTOR     = 0x00000800;
    public static final int GLOBAL          = 0x00001000;
    public static final int LOCAL           = 0x00002000;
    public static final int FILE_LOCAL      = 0x00004000;
    public static final int MEMBER          = 0x00008000;
    public static final int MACRO           = 0x00010000;
    public static final int DESTRUCTOR      = 0x00020000;
    public static final int OPERATOR        = 0x00040000;
    public static final int EXTERN          = 0x00080000;
    public static final int FORWARD         = 0x00100000;
    public static final boolean DEBUG = Boolean.getBoolean("csm.utilities.trace.summary") ||
            Boolean.getBoolean("csm.utilities.trace");
    private static final RequestProcessor RP = new RequestProcessor(CsmUtilities.class.getName(), 1);

    public static int getModifiers(CsmObject obj) {
        CndUtils.assertNonUiThread();
        int mod = 0;
        if (CsmKindUtilities.isClassMember(obj)) {
            mod |= CsmUtilities.getMemberModifiers((CsmMember) obj);
        } else if (CsmKindUtilities.isFunctionDefinition(obj)) {
            CsmFunctionDefinition fun = (CsmFunctionDefinition) obj;
            CsmFunction decl = fun.getDeclaration();
            if (CsmKindUtilities.isClassMember(decl)) {
                mod |= CsmUtilities.getMemberModifiers((CsmMember) decl);
            } else {
                if (decl == null) {
                    decl = fun;
                }
                if (CsmKindUtilities.isGlobalFunction(obj)) {
                    mod |= GLOBAL;
                }
                if (CsmKindUtilities.isFileLocalFunction(decl)){
                    mod |= FILE_LOCAL;
                }
            }
        } else {
            if (CsmKindUtilities.isGlobalVariable(obj) || CsmKindUtilities.isGlobalFunction(obj)) {
                mod |= GLOBAL;
            }
            if (CsmKindUtilities.isFileLocalVariable(obj) || CsmKindUtilities.isFileLocalFunction(obj)) {
                mod |= FILE_LOCAL;
            }
            if (CsmKindUtilities.isEnumerator(obj)) {
                mod |= ENUMERATOR;
            }
        }
        if (CsmClassifierResolver.getDefault().isForwardClass(obj)) {
            mod |= FORWARD;
        }
        if (CsmKindUtilities.isOperator(obj)) {
            mod |= OPERATOR;
        }
        // add contst info for variables
        if (CsmKindUtilities.isVariable(obj)) {
            CsmVariable var = (CsmVariable) obj;
            // parameters could be with null type if it's varagrs "..."
            mod |= (var.getType() != null && var.getType().isConst()) ? CONST_MEMBER_BIT : 0;
            if (var.isExtern()) {
                mod |= EXTERN;
            }
        }
        return mod;
    }

    public static int getMemberModifiers(CsmMember member) {
        int mod = 0;
        CsmVisibility visibility = member.getVisibility();
        if (CsmVisibility.PRIVATE == visibility) {
            mod = PRIVATE;
        } else if (CsmVisibility.PROTECTED == visibility) {
            mod = PROTECTED;
        } else if (CsmVisibility.PUBLIC == visibility) {
            mod = PUBLIC;
        }
        if (member.isStatic()) {
            mod |= STATIC;
        }
        mod |= MEMBER;
        if (CsmKindUtilities.isConstructor(member)) {
            mod |= CONSTRUCTOR;
        } else if (CsmKindUtilities.isDestructor(member)) {
            mod |= DESTRUCTOR;
        }
        return mod;
    }

    /** Get level from modifiers.
     * @param modifiers
     * @return one of correspond constant (PUBLIC_LEVEL, PROTECTED_LEVEL, PRIVATE_LEVEL)
     */
    public static int getLevel(int modifiers) {
        if ((modifiers & PUBLIC) != 0) {
            return PUBLIC_LEVEL;
        } else if ((modifiers & PROTECTED) != 0) {
            return PROTECTED_LEVEL;
        } else {
            return PRIVATE_LEVEL;
        }
    }

    public static boolean isPrimitiveClass(CsmClassifier c) {
        return c.getKind() == CsmDeclaration.Kind.BUILT_IN;
    }
    //====================

    public static CsmFile getCsmFile(Node node, boolean waitParsing) {
        return getCsmFile(node.getLookup().lookup(DataObject.class), waitParsing, false);
    }

    public static DataObject getDataObject(JTextComponent component) {
        if (component == null) {
            return null;
        }
        Document doc = component.getDocument();
        return (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
    }
    
    public static JTextComponent findOpenedEditor(DataObject dob) {
        if (dob == null) {
            return null;
        }
        List<? extends JTextComponent> componentList = EditorRegistry.componentList();
        for (JTextComponent comp : componentList) {
            if (comp instanceof JEditorPane) {
                DataObject dobj = getDataObject(comp);
                if (dob.equals(dobj)) {
                    return (JEditorPane) comp;
                }
            }
        }
        return null;
    }
    
    /*
     * redirected into EDT => can block
     * if interested in opened editor => try findRecentEditor method
     */
    public static JEditorPane findRecentEditorPaneInEQ(final EditorCookie ec) {
        assert ec != null;
        final JEditorPane[] panes = {null};
        if (SwingUtilities.isEventDispatchThread()) {
            panes[0] = NbDocument.findRecentEditorPane(ec);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        panes[0] = NbDocument.findRecentEditorPane(ec);
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return panes[0];
    }

    public static TopComponent getTopComponentInEQ(final String tcID) {
        assert tcID != null;
        final TopComponent tc[] = {null};
        if (SwingUtilities.isEventDispatchThread()) {
            tc[0] = WindowManager.getDefault().findTopComponent(tcID);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        tc[0] = WindowManager.getDefault().findTopComponent(tcID);
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return tc[0];
    }

    public static File getFile(Document bDoc) {
        DataObject dobj = NbEditorUtilities.getDataObject(bDoc);
        if (dobj != null && dobj.isValid()) {
            FileObject fo = dobj.getPrimaryFile();
            if (fo != null) {
                File file = FileUtil.toFile(fo);
                return file;
            }
        }
        return null;
    }

    public static CsmFile getCsmFile(JTextComponent comp, boolean waitParsing, boolean snapShot) {
        return comp == null ? null : getCsmFile(comp.getDocument(), waitParsing, snapShot);
    }

    public static CsmFile getCsmFile(Document bDoc, boolean waitParsing, boolean snapShot) {
        CsmFile csmFile = null;
        if (bDoc != null) {
            try {
                csmFile = (CsmFile) bDoc.getProperty(CsmFile.class);
                if (csmFile == null) {
                    csmFile = getCsmFile(NbEditorUtilities.getDataObject(bDoc), waitParsing, snapShot);
                }
                if (csmFile == null) {
                    String mimeType = DocumentUtilities.getMimeType(bDoc);
                    if ("text/x-dialog-binding".equals(mimeType)) { // NOI18N
                        // this is context from dialog
                        InputAttributes inputAttributes = (InputAttributes) bDoc.getProperty(InputAttributes.class);
                        if (inputAttributes != null) {
                            LanguagePath path = LanguagePath.get(MimeLookup.getLookup(mimeType).lookup(Language.class));
                            FileObject fileObject = (FileObject) inputAttributes.getValue(path, "dialogBinding.fileObject"); //NOI18N
                            csmFile = CsmUtilities.getCsmFile(fileObject, waitParsing, snapShot);
                            if (csmFile == null) {
                                Document d = (Document) inputAttributes.getValue(path, "dialogBinding.document"); //NOI18N
                                csmFile = d == null ? null : CsmUtilities.getCsmFile(d, waitParsing, snapShot);
                            }
                        }
                    }
                }
            } catch (NullPointerException exc) {
                exc.printStackTrace(System.err);
            }
        }
        return csmFile;
    }

    public static CsmProject getCsmProject(Document bDoc) {
        CsmProject csmProject = null;
        try {
            csmProject = getCsmFile(bDoc, false, false).getProject();
        } catch (NullPointerException exc) {
            exc.printStackTrace(System.err);
        }
        return csmProject;
    }

    /**
     * Tries to find project that contains given file under its source roots directories.
     * File doesn't have to be included into project or code model.
     * This is somewhat similar to default FileOwnerQueryImplementation,
     * but only for CsmProjects.
     *
     * @param fo  file to look up
     * @return project that contains file under its root directory,
     *      or <code>null</code> if there is no such project
     */
    public static Collection<CsmProject> getOwnerCsmProjects(FileObject fo) {
        Collection<CsmProject> out = new ArrayList<CsmProject>();
        if (fo != null && fo.isValid()) {
            String path = fo.getPath();
            FileSystem fileSystem;
            try {
                fileSystem = fo.getFileSystem();
            } catch (FileStateInvalidException ex) {
                return null;
            }
            for (CsmProject csmProject : CsmModelAccessor.getModel().projects()) {
                Object platformProject = csmProject.getPlatformProject();
                if (platformProject instanceof NativeProject) {
                    NativeProject nativeProject = (NativeProject)platformProject;
                    if (nativeProject.getFileSystem().equals(fileSystem)) {
                        NativeFileItem item = nativeProject.findFileItem(fo);
                        if (item != null) {
                            out.add(csmProject);
                        } else {
                            final List<String> sourceRoots = new ArrayList<String>();
                            sourceRoots.add(nativeProject.getProjectRoot());
                            sourceRoots.addAll(nativeProject.getSourceRoots());
                            for (String src : sourceRoots) {
                                if (path.startsWith(src)) {
                                    final int length = src.length();
                                    if (path.length() == length || path.charAt(length) == '\\' || path.charAt(length) == '/') {
                                        out.add(csmProject);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return out;
    }

    public static boolean isAnyNativeProjectOpened() {
        return !NativeProjectRegistry.getDefault().getOpenProjects().isEmpty();
    }

    private static final Pattern VCS_TMP_FILE = Pattern.compile("[\\\\/]vcs-[0-9]*[\\\\/]vcs-[0-9]*"); // NOI18N
    // /var/tmp/vcs-123123/vcs-123123/file.cpp
    public static boolean isTemporaryVCSFile(FileObject fo) {
        return VCS_TMP_FILE.matcher(fo.getPath()).find();
    }

    public static boolean isCsmSuitable(FileObject fo) {
        // workaround for #194431 - Path should be absolute: Templates/cFiles/CSimpleTest.c
        // fo.isVirtual returns false, FileUtil.toFile() return non-null for such files
        return CndPathUtilities.isPathAbsolute(fo.getPath());
    }
    
    public static Collection<NativeProject> getNativeProjects(DataObject dobj) {
        Collection<NativeProject> out = new ArrayList<>();
        if (dobj != null && dobj.isValid()) {
            NativeFileItemSet set = dobj.getLookup().lookup(NativeFileItemSet.class);
            if (set != null && !set.isEmpty()) {
                for (NativeFileItem item : set.getItems()) {
                    out.add(item.getNativeProject());
                }
            }
        }
        return out;
    }

    public static CsmFile[] getCsmFiles(DataObject dobj, boolean waitParsing, boolean snapShot) {
        if (waitParsing) { 
            CndUtils.assertNonUiThread();
        }
        if (dobj != null && dobj.isValid()) {
            try {
                List<CsmFile> files = new ArrayList<CsmFile>();
                // put standalone files into separate collection
                List<CsmFile> saFiles = new ArrayList<CsmFile>();
                NativeFileItemSet set = dobj.getLookup().lookup(NativeFileItemSet.class);
                boolean hasNormalFiles = false;
                if (set != null && !set.isEmpty()) {
                    for (NativeFileItem item : set.getItems()) {
                        CsmProject csmProject = CsmModelAccessor.getModel().getProject(item.getNativeProject());
                        if (csmProject != null) {
                            CsmFile file = csmProject.findFile(item, waitParsing, snapShot);
                            if (file != null) {
                                if (item.getClass().getName().contains("StandaloneFileProvider")) { // NOI18N
                                    saFiles.add(file);
                                } else {
                                    hasNormalFiles = true;
                                    files.add(file);
                                }
                            }
                        }
                    }
                    // append stand alone files always at the end of collection
                    files.addAll(saFiles);
                }
                if (files.isEmpty()) {
                    FileObject fo = dobj.getPrimaryFile();
                    if (fo != null && fo.isValid() && CsmUtilities.isCsmSuitable(fo)) {
                        CsmFile csmFile = CsmModelAccessor.getModel().findFile(FSPath.toFSPath(fo), waitParsing, snapShot);
                        if (csmFile != null) {
                            files.add(csmFile);
                        }
                    }
                }
                if (CndUtils.isDebugMode()) {
                    for (int i = 0; i < files.size(); i++) {
                        CsmFile csmFile = files.get(i);
                        CsmProject csmProject = csmFile.getProject();
                        if (csmProject != null) {
                            Object platformProject = csmProject.getPlatformProject();
                            if (platformProject == null) {
                                CndUtils.assertTrueInConsole(false, "null platform project for FILE " + csmFile + " from PROJECT " + csmProject); // NOI18N
                            } else if (!csmProject.isValid()) {
                                if (CsmModelAccessor.getModelState() == CsmModelState.ON) {
                                    CndUtils.assertTrueInConsole(false, "FILE " + csmFile + " from invalid PROJECT " + csmProject); // NOI18N
                                }
                            } else if (hasNormalFiles && platformProject.getClass().getName().contains("StandaloneFileProvider")) { // NOI18N
                                if (i == 0 && files.size() > 1) {
                                    if (CsmModelAccessor.getModelState() == CsmModelState.ON) {
                                        CndUtils.assertTrue(false, "!!! STANDALONE FILE " + csmFile + "\nTOOK PRIORITY OVER OTHER FILES " + files); // NOI18N
                                    }
                                } else {
//                                    System.err.printf("STANDALONE FILE TO BE USED %s\n", csmFile); // NOI18N
                                }
                            }
                        } else {
                           CndUtils.assertTrue(false, "FILE WITHOUT PROJECT" + csmFile); // NOI18N
                        }
                    }
                }
                return files.toArray(new CsmFile[files.size()]);
            } catch (BufferUnderflowException ex) {
                // FIXUP: IZ#148840
            } catch (AssertionError ex) {
                ex.printStackTrace(System.err);
            } catch (IllegalStateException ex) {
                // dobj can be invalid
            }
        }
        return new CsmFile[0];
    }
    
    public static CsmFile getCsmFile(DataObject dobj, boolean waitParsing, boolean snapShot) {
        CsmFile[] files = getCsmFiles(dobj, waitParsing, snapShot);
        if (files == null || files.length == 0) {
            return null;
        } else {
            if (waitParsing) {
                try {
                    files[0].scheduleParsing(true);
                } catch (InterruptedException ex) {                       // ignore

                }
            }
            return files[0];
        }
    }

    public static CsmFile getCsmFile(FileObject fo, boolean waitParsing, boolean snapShot) {
        if (fo == null || ! fo.isValid() || ! isCsmSuitable(fo)) { // #194431 Path should be absolute: Templates/cFiles/CSimpleTest.c
            return null;
        } else {
            try {
                return getCsmFile(DataObject.find(fo), waitParsing, snapShot);
            } catch (DataObjectNotFoundException ex) {
                return null;
            }
        }
    }
    
    public static FileObject getFileObject(CsmFile csmFile) {
        return (csmFile == null) ? null : csmFile.getFileObject();
//        FileObject fo = null;
//        if (csmFile != null) {
//            
//            try {
//                try {                    
//                    fo = CndFileUtils.toFileObject(csmFile.getAbsolutePath());
//                    if (fo == null /*paranoia*/ || !fo.isValid()) {
//                        File file = new File(csmFile.getAbsolutePath().toString()); // XXX:FileObject conversion
//                        fo = CndFileUtils.toFileObject(file.getCanonicalFile());
//                    }
//                } catch (IOException e) {
//                    fo = CndFileUtils.toFileObject(CndFileUtils.normalizeAbsolutePath(csmFile.getAbsolutePath().toString()));
//                }
//            } catch (IllegalArgumentException ex) {
//                ex.printStackTrace(System.err);
//            }
//        }
//        return fo;
    }

    public static FileObject getFileObject(Document doc) {
        FileObject fo = (FileObject)doc.getProperty(FileObject.class);
        if(fo == null) {
            DataObject dobj = NbEditorUtilities.getDataObject(doc);
            if (dobj != null) {
                fo = dobj.getPrimaryFile();
            } else {
                CsmFile csmFile = getCsmFile(doc, false, false);
                if (csmFile != null) {
                    fo = getFileObject(csmFile);
                }
            }            
        }
        return fo;
    }

    public static DataObject getDataObject(CsmFile csmFile) {
        return getDataObject(getFileObject(csmFile));
    }

    public static DataObject getDataObject(FileObject fo) {
        DataObject dob = null;
        if (fo != null) {
            try {
                dob = DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
            }
        }
        return dob;
    }

    public static Document getDocument(FileObject fo) {
        if (fo != null && fo.isValid()) {
            try {
                DataObject dob = DataObject.find(fo);
                if (dob != null && dob.isValid()) {
                    EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
                    if (ec != null) {
                        return ec.getDocument();
                    }
                }
            } catch (IOException ex) {
                // file can be removed or became invalid
                // we catch IOException, because FileStateInvalidException is IOException
                // but is not declared to be thrown from DataObject.find
                if (fo.isValid() && !fo.isVirtual()) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return null;
    }

    public static Document getDocument(CsmFile file) {
        FileObject fo = getFileObject(file);
        if(fo != null) {
            return getDocument(fo);
        }
        return null;
    }

    public static PositionBounds createPositionBounds(CsmOffsetable csmObj) {
        if (csmObj == null) {
            return null;
        }
        CloneableEditorSupport ces = findCloneableEditorSupport(csmObj.getContainingFile());
        if (ces != null) {
            PositionRef beg = ces.createPositionRef(csmObj.getStartOffset(), Position.Bias.Forward);
            PositionRef end = ces.createPositionRef(csmObj.getEndOffset(), Position.Bias.Backward);
            return new PositionBounds(beg, end);
        }
        return null;
    }

    public static CloneableEditorSupport findCloneableEditorSupport(CsmFile csmFile) {
        DataObject dob = getDataObject(csmFile);
        return findCloneableEditorSupport(dob);
    }

    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        if (dob == null) {
            return null;
        }
        Object obj = dob.getLookup().lookup(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport) obj;
        }
        obj = dob.getLookup().lookup(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport) obj;
        }
        return null;
    }
    //==================== open elemen's definition/declaration ================

    private static class Point {

        public final int line;
        public final int column;

        public Point(int line, int column) {
            this.line = line;
            this.column = column;
        }
    }

    private static interface Offsetable {
        int getOffset();
    }

    private static class PointOrOffsetable {

        private final Object content;

        public PointOrOffsetable(Point point) {
            content = point;
        }

        public PointOrOffsetable(final CsmOffsetable offsetable) {
            content = new Offsetable() {
                @Override
                public int getOffset() {
                    return offsetable.getStartOffset();
                }
            };
        }

        public PointOrOffsetable(final int offset) {
            content = new Offsetable() {
                @Override
                public int getOffset() {
                    return offset;
                }
            };
        }

        public Point getPoint() {
            return (content instanceof Point) ? (Point) content : null;
        }

        public Offsetable getOffsetable() {
            return (content instanceof Offsetable) ? (Offsetable) content : null;
        }

        @Override
        public String toString() {
            if (content instanceof Point) {
                Point point = (Point) content;
                return String.format("[%d:%d]", point.line, point.column); // NOI18N
            } else {
                return String.format("[%d]", ((Offsetable) content).getOffset()); // NOI18N
            }
        }
    }

    /**
     * opens document even if it is very big by silently confirming open
     * @param cookie
     * @return
     */
    public static StyledDocument openDocument(EditorCookie cookie) {
        if (cookie == null) {
            return null;
        }
        StyledDocument document = null;
        try {
            try {
                document = cookie.openDocument();
            } catch (UserQuestionException e) {
                e.confirmed();
                document = cookie.openDocument();
            }
        } catch(UserQuestionException e) {
            // no need to report
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return document;
    }
    
    public static StyledDocument openDocument(CloneableEditorSupport ces) {
        if (ces == null) {
            return null;
        }
        StyledDocument document = null;
        try {
            try {
                document = ces.openDocument();
            } catch (UserQuestionException e) {
                e.confirmed();
                document = ces.openDocument();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return document;
    } 
    
    /*
     * opens source file correspond to input object and set caret on
     * start offset position
     */
    public static boolean openSource(CsmObject element) {
        if (CsmKindUtilities.isOffsetable(element)) {
            return openAtElement((CsmOffsetable) element);
        } else if (CsmKindUtilities.isFile(element)) {
            final CsmFile file = (CsmFile) element;
            CsmOffsetable fileTarget = new FileTarget(file);
            return openAtElement(fileTarget);
        }
        return false;
    }

    public static boolean openSource(CsmFile file, int line, int column) {
        return openAtElement(getDataObject(file), new PointOrOffsetable(new Point(line, column)));
    }

    public static boolean openSource(FileObject fo, int line, int column) {
        DataObject dob;
        try {
            dob = DataObject.find(fo);
            return openAtElement(dob, new PointOrOffsetable(new Point(line, column)));
        } catch (DataObjectNotFoundException ex) {
            return false;
        }
    }

    public static boolean openSource(FileObject fo, int offset) {
        DataObject dob;
        try {
            dob = DataObject.find(fo);
            return openAtElement(dob, new PointOrOffsetable(offset));
        } catch (DataObjectNotFoundException ex) {
            return false;
        }
    }

    public static boolean openSource(PositionBounds position) {
        CloneableEditorSupport editorSupport = position.getBegin().getCloneableEditorSupport();
        editorSupport.edit();
        JEditorPane[] panes = editorSupport.getOpenedPanes();
        if (panes != null) {
            JumpList.checkAddEntry();
            JEditorPane pane = panes[0];
            pane.setCaretPosition(position.getBegin().getOffset());
            Container container = pane;
            while (container != null && !(container instanceof TopComponent)) {
                container = container.getParent();
            }
            if (container != null) {
                ((TopComponent) container).requestActive();
            }
        }
        return false;
    }

    private static boolean openAtElement(final CsmOffsetable element) {
        return openAtElement(getDataObject(element.getContainingFile()), new PointOrOffsetable(element));
    }

    private static boolean openAtElement(final DataObject orig, final PointOrOffsetable element) {
        final DataObject dob = redirect(orig);
        if (dob != null) {
            final EditorCookie.Observable ec = dob.getLookup().lookup(EditorCookie.Observable.class);
            if (ec != null) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        JumpList.checkAddEntry();
                        JEditorPane pane = findRecentEditorPaneInEQ(ec);
                        if (pane != null) {
                            //editor already opened, so just select
                            selectElementInPane(pane, element, false);
                        } else {
                            // editor not yet opened, attach listener and open from there
                            ec.addPropertyChangeListener(new PropertyChangeListenerImpl(ec, element));
                            ec.open();
                        }
                    }

                    class PropertyChangeListenerImpl implements PropertyChangeListener, Runnable {

                        private final Observable ec;
                        private final PointOrOffsetable element;
                        private boolean detach = false;

                        public PropertyChangeListenerImpl(Observable ec, PointOrOffsetable element) {
                            this.ec = ec;
                            this.element = element;
                        }

                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            if (EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
                                // IZ#199820 - 10 sec freezing while hyperlink to non UTF-8 include files
                                // we need event with already opened pane, unfortunately evt always have old/new params as null
                                // so we check if pane is opened ourselves
                                JEditorPane pane = findRecentEditorPaneInEQ(ec);
                                // we use detach, because user can answer "No" and we'd like to detach listener anyway
                                if (pane != null || detach) {
                                    ec.removePropertyChangeListener(this);
                                }
                                if (pane != null) {
                                    // redirect to jump on position after showing document content
                                    SwingUtilities.invokeLater(this);
                                } else if (detach) {
                                    // last try hack due to bug with PROP_OPENED_PANES (IZ#202242)
                                    SwingUtilities.invokeLater(this);
                                }
                                detach = true;
                            }
                        }

                        @Override
                        public void run() {
                            // do not use getOpenedPanes, because it holds AWT lock which
                            // prevents to show UserQuestionException based dialogs
                            // use non-blocking findRecentEditorPane instead
                            JEditorPane pane = findRecentEditorPaneInEQ(ec);
                            if (pane != null) {
                                selectElementInPane(pane, element, false);
                            }
                        }
                    }
                });
            }
            return true;
        }
        return false;
    }

    /** Jumps to element in given editor pane. When delayProcessing is
     * specified, waits for real visible open before jump
     */
    private static void selectElementInPane(final JEditorPane pane, final PointOrOffsetable element, boolean delayProcessing) {
        //final Cursor editCursor = pane.getCursor();
        //pane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (false && delayProcessing) {
            // [AS] Comment branch because it does not work when
            // method is called from action in undock view.
            // See IZ#135610:*CallGraph*: GoTo Caller works incorrectly in undock window
            // [dafe] I don't know why, but editor guys are waiting for focus
            // in delay processing, so I will do the same
            pane.addFocusListener(new FocusAdapter() {

                @Override
                public void focusGained(FocusEvent e) {
                    RP.post(new Runnable() {

                        @Override
                        public void run() {
                            jumpToElement(pane, element);
                        }
                    });
                    pane.removeFocusListener(this);
                }
            });
        } else {
            // immediate processing
            RP.post(new Runnable() {

                @Override
                public void run() {
                    jumpToElement(pane, element);
                }
            });
            // try to activate outer TopComponent
            Container temp = pane;
            while (temp != null && !(temp instanceof TopComponent)) {
                temp = temp.getParent();
            }
            if (temp instanceof TopComponent) {
                ((TopComponent) temp).open();
                ((TopComponent) temp).requestActive();
                ((TopComponent) temp).requestVisible();
            }
        }
    }
    //    /** Jumps to element on given editor pane. Call only outside AWT thread!
//     */
//    private static void jumpToElement(JEditorPane pane, CsmOffsetable element) {
//        jumpToElement(pane, element, false);
//    }

    private static void jumpToElement(JEditorPane pane, PointOrOffsetable pointOrOffsetable) {
        //start = jumpLineStart ? lineToPosition(pane, element.getStartPosition().getLine()-1) : element.getStartOffset();
        int start;
        Offsetable element = pointOrOffsetable.getOffsetable();
        Point point = pointOrOffsetable.getPoint();
        if (element == null) {
            start = Utilities.getRowStartFromLineOffset((BaseDocument) pane.getDocument(), point.line - 1);
            start += point.column;
        } else {
            start = element.getOffset();
        }
        if (pane.getDocument() != null && start >= 0 && start < pane.getDocument().getLength()) {
            pane.setCaretPosition(start);
            if (DEBUG) {
                String traceName;
                System.err.println("I'm going to " + start + " for element " + pointOrOffsetable);
            }
        }
        StatusDisplayer.getDefault().setStatusText(""); // NOI18N

    }
    // NB document independent method

    private static int lineToPosition(JEditorPane pane, int docLine) {
        Document doc = pane.getDocument();
        int lineSt = 0;
        if (doc instanceof BaseDocument) {
            // use NB utilities for NB documents
            lineSt = Utilities.getRowStartFromLineOffset((BaseDocument) doc, docLine);
        } else {
            // not NB document, count lines
            int len = doc.getLength();
            try {
                String text = doc.getText(0, len);
                boolean afterEOL = false;
                for (int i = 0; i < len; i++) {
                    char c = text.charAt(i);
                    if (c == '\n') {
                        docLine--;
                        if (docLine == 0) {
                            return lineSt;
                        }
                        afterEOL = true;
                    } else if (afterEOL) {
                        lineSt = i;
                        afterEOL = false;
                    }
                }
            } catch (BadLocationException e) {
            }
        }
        return lineSt;
    }

    public static String getElementJumpName(CsmObject element) {
        String text = "";
        if (element != null) {
            if (CsmKindUtilities.isNamedElement(element)) {
                text = ((CsmNamedElement) element).getName().toString();
            } else if (CsmKindUtilities.isStatement(element)) {
                text = ((CsmStatement) element).getText().toString();
            } else if (CsmKindUtilities.isOffsetable(element)) {
                text = ((CsmOffsetable) element).getText().toString();
            }
            if (text.length() > 0) {
                text = "\"" + text + "\""; // NOI18N

            }
        }
        return text;
    }

    public static <T> Collection<T> merge(Collection<T> orig, Collection<T> newList) {
        orig = orig != null ? orig : new ArrayList<T>();
        if (newList != null && newList.size() > 0) {
            orig.addAll(newList);
        }
        return orig;
    }

    public static <T> boolean removeAll(Collection<T> dest, Collection<T> removeItems) {
        if (dest != null && removeItems != null) {
            return dest.removeAll(removeItems);
        }
        return false;
    }

    public static String getCsmName(CsmObject obj) {
        StringBuilder buf = new StringBuilder();
        if (CsmKindUtilities.isNamedElement(obj)) {
            CsmNamedElement named = (CsmNamedElement) obj;
            buf.append(" [name] ").append(named.getName()); // NOI18N

        } else {
            String simpleName = obj.getClass().getName();
            simpleName = simpleName.substring(simpleName.lastIndexOf('.') + 1); // NOI18N

            buf.append(" [class] ").append(simpleName); // NOI18N

        }
        if (CsmKindUtilities.isDeclaration(obj)) {
            CsmDeclaration decl = (CsmDeclaration) obj;
            buf.append(" [kind] ").append(decl.getKind()); // NOI18N

        }
        return buf.toString();
    }          //-------------------------------------------------------------------------


    /**
     * Gets function signature in the form that is shown to client
     * @param fun function, which signature should be returned
     * @return signature of the function
     */
    public static String getSignature(CsmFunction fun) {
        return getSignature(fun, true);
    }

    /**
     * Gets function signature in the form that is shown to client
     * @param fun function, which signature should be returned
     * @param showParamNames determines whether to include parameter names in signature
     * @return signature of the function
     */
    public static String getSignature(CsmFunction fun, boolean showParamNames) {
        StringBuilder sb = new StringBuilder(CsmKindUtilities.isTemplate(fun) ? ((CsmTemplate) fun).getDisplayName() : fun.getName());
        if (!CsmKindUtilities.isProgram(fun)) {
            sb.append('(');
            boolean addComma = false;
            for (Iterator<CsmParameter> iter = fun.getParameters().iterator(); iter.hasNext();) {
                CsmParameter par = iter.next();
                if (addComma) {
                    sb.append(", "); // NOI18N

                } else {
                    addComma = true;
                }
                if (showParamNames) {
                    sb.append(par.getDisplayText());
                } else {
                    CsmType type = par.getType();
                    if (type != null) {
                        sb.append(type.getText());
                        //sb.append(' ');
                    } else if (par.isVarArgs()) {
                        sb.append("..."); // NOI18N

                    }
                }
            }
            sb.append(')');
            if (CsmKindUtilities.isMethodDeclaration(fun)) {
                if (((CsmMethod) fun).isConst()) {
                    sb.append(" const"); // NOI18N

                }
            }
        }
        // TODO: as soon as we extract APTStringManager into a separate module,
        // use string manager here.
        // For now it's client responsibility to do this
        //return NameCache.getString(sb.toString());
        return sb.toString();
    }
    
    private static DataObject redirect(DataObject dob) {
        if (dob == null) {
            return null;
        }
        Collection<? extends FileObjectRedirector> redirectors = Lookup.getDefault().lookupAll(FileObjectRedirector.class);
        for (FileObjectRedirector redirector : redirectors) {
            DataObject newDO = redirector.redirect(dob);
            if(newDO != null) {
                dob = newDO;
            }
        }
        return dob;
    }
    
    //-----------------------------------------------------------------

    private static final class FileTarget implements CsmOffsetable {

        private CsmFile file;

        public FileTarget(CsmFile file) {
            this.file = file;
        }

        @Override
        public CsmFile getContainingFile() {
            return file;
        }

        @Override
        public int getStartOffset() {
            // start of the file
            return DUMMY_POSITION.getOffset();
        }

        @Override
        public int getEndOffset() {
            // start of the file
            return DUMMY_POSITION.getOffset();
        }

        @Override
        public CsmOffsetable.Position getStartPosition() {
            return DUMMY_POSITION;
        }

        @Override
        public CsmOffsetable.Position getEndPosition() {
            return DUMMY_POSITION;
        }

        @Override
        public String getText() {
            return "";
        }
    }
    private static final CsmOffsetable.Position DUMMY_POSITION = new CsmOffsetable.Position() {

        @Override
        public int getOffset() {
            return -1;
        }

        @Override
        public int getLine() {
            return -1;
        }

        @Override
        public int getColumn() {
            return -1;
        }
    };

    private CsmUtilities() {
    }
}
