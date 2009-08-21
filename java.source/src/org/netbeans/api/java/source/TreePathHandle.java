/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
                                                                                                                                                                                                                               
package org.netbeans.api.java.source;                                                                                                                                                                                          
                                                                                                                                                                                                                               
import com.sun.source.tree.Tree;                                                                                                                                                                                               
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;                                                                                                                                                                                           
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;                                                                                                                                                                                        
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.lang.model.element.Element;                                                                                                                                                                                       
import javax.lang.model.element.ElementKind;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;                                                                                                                                                                                         
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.Index;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;                                                                                                                                                                                     
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;                                                                                                                                                                                         
import org.openide.loaders.DataObjectNotFoundException;                                                                                                                                                                        
import org.openide.text.CloneableEditorSupport;                                                                                                                                                                                
import org.openide.text.EditorSupport;
import org.openide.text.PositionRef;                                                                                                                                                                                           
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
                                                                                                                                                                                                                               
/**                                                                                                                                                                                                                            
 * Represents a handle for {@link TreePath} which can be kept and later resolved                                                                                                                                               
 * by another javac. The Javac {@link Element}s are valid only in the single                                                                                                                                                   
 * {@link javax.tools.CompilationTask} or single run of the                                                                                                                                                                    
 * {@link org.netbeans.api.java.source.CancellableTask}. If the client needs to                                                                                                                                                
 * keep a reference to the {@link TreePath} and use it in the other CancellableTask                                                                                                                                            
 * he has to serialize it into the {@link TreePathHandle}.                                                                                                                                                                     
 * <div class="nonnormative">                                                                                                                                                                                                  
 * <p>                                                                                                                                                                                                                         
 * Typical usage of TreePathHandle enclElIsCorrespondingEl:                                                                                                                                                                    
 * </p>                                                                                                                                                                                                                        
 * <pre>                                                                                                                                                                                                                       
 * final TreePathHandle[] tpHandle = new TreePathHandle[1];                                                                                                                                                                    
 * javaSource.runCompileControlTask(new CancellableTask<CompilationController>() {                                                                                                                                             
 *     public void run(CompilationController compilationController) {                                                                                                                                                          
 *         parameter.toPhase(Phase.RESOLVED);                                                                                                                                                                                  
 *         CompilationUnitTree cu = compilationController.getTree ();                                                                                                                                                          
 *         TreePath treePath = getInterestingTreePath (cu);                                                                                                                                                                    
 *         treePathHandle[0] = TreePathHandle.create (element, compilationController);                                                                                                                                         
 *    }                                                                                                                                                                                                                        
 * },priority);                                                                                                                                                                                                                
 *                                                                                                                                                                                                                             
 * otherJavaSource.runCompileControlTask(new CancellableTask<CompilationController>() {                                                                                                                                        
 *     public void run(CompilationController compilationController) {                                                                                                                                                          
 *         parameter.toPhase(Phase.RESOLVED);                                                                                                                                                                                  
 *         TreePath treePath = treePathHanlde[0].resolve (compilationController);                                                                                                                                              
 *         ....                                                                                                                                                                                                                
 *    }                                                                                                                                                                                                                        
 * },priority);                                                                                                                                                                                                                
 * </pre>                                                                                                                                                                                                                      
 * </div>                                                                                                                                                                                                                      
 *                                                                                                                                                                                                                             
 *                                                                                                                                                                                                                             
 * @author Jan Becicka                                                                                                                                                                                                         
 */                                                                                                                                                                                                                            
public final class TreePathHandle {
    private static Logger log = Logger.getLogger(TreePathHandle.class.getName());

    private final Delegate delegate;
    
    private TreePathHandle(Delegate d) {
        if (d == null) {
            throw new IllegalArgumentException();
        }
        
        this.delegate = d;
    }
    
    /**                                                                                                                                                                                                                        
     * getter for FileObject from give TreePathHandle                                                                                                                                                                          
     * @return FileObject for which was this handle created                                                                                                                                                                    
     */                                                                                                                                                                                                                        
    public FileObject getFileObject() {
        return this.delegate.getFileObject();
    }                                                                                                                                                                                                                          
                                                                                                                                                                                                                               
    /**                                                                                                                                                                                                                        
     * Resolves an {@link TreePath} from the {@link TreePathHandle}.                                                                                                                                                           
     * @param compilationInfo representing the {@link javax.tools.CompilationTask}                                                                                                                                             
     * @return resolved subclass of {@link Element} or null if the element does not exist on                                                                                                                                    
     * the classpath/sourcepath of {@link javax.tools.CompilationTask}.
     * @throws IllegalArgumentException when this {@link TreePathHandle} is not created for a source
     * represented by the compilationInfo.
     */                                                                                                                                                                                                                        
    public TreePath resolve (final CompilationInfo compilationInfo) throws IllegalArgumentException {
        final TreePath result = this.delegate.resolve(compilationInfo);
        if (result == null) {
            Logger.getLogger(TreePathHandle.class.getName()).info("Cannot resolve: "+toString());
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TreePathHandle)) {
            return false;
        }
        
        if (delegate.getClass() != ((TreePathHandle) obj).delegate.getClass()) {
            return false;
        }
        
        return delegate.equalsHandle(((TreePathHandle) obj).delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
                                                                                                                                                                                                                               
    /**                                                                                                                                                                                                                        
     * Resolves an {@link Element} from the {@link TreePathHandle}.                                                                                                                                                            
     * @param compilationInfo representing the {@link javax.tools.CompilationTask}                                                                                                                                             
     * @return resolved subclass of {@link Element} or null if the element does not exist on                                                                                                                                    
     * the classpath/sourcepath of {@link javax.tools.CompilationTask}.                                                                                                                                                        
     */                                                                                                                                                                                                                        
    public Element resolveElement(final CompilationInfo info) {
        Parameters.notNull("info", info);
        
        final Element result = this.delegate.resolveElement(info);
        if (result == null) {
            Logger.getLogger(TreePathHandle.class.getName()).info("Cannot resolve: "+toString());
        }
        return result;
    }                                                                                                                                                                                                                          
                                                                                                                                                                                                                               
    /**                                                                                                                                                                                                                        
     * Returns the {@link Tree.Kind} of this TreePathHandle,                                                                                                                                                                   
     * it returns the kind of the {@link Tree} from which the handle                                                                                                                                           
     * was created.                                                                                                                                                                                                            
     *                                                                                                                                                                                                                         
     * @return {@link Tree.Kind}                                                                                                                                                                                               
     */                                                                                                                                                                                                                        
    public Tree.Kind getKind() {
        return this.delegate.getKind();
    }                                                                                                                                                                                                                          
                                                                                                                                                                                                                               
    /**                                                                                                                                                                                                                        
     * Factory method for creating {@link TreePathHandle}.                                                                                                                                                                     
     *                                                                                                                                                                                                                         
     * @param treePath for which the {@link TrePathHandle} should be created.                                                                                                                                                  
     * @param info 
     * @return a new {@link TreePathHandle}                                                                                                                                                                                    
     * @throws java.lang.IllegalArgumentException if arguments are not supported
     */
    public static TreePathHandle create(final TreePath treePath, CompilationInfo info) throws IllegalArgumentException {
        Parameters.notNull("treePath", treePath);
        Parameters.notNull("info", info);
        
        FileObject file;
        try {
            URL url = treePath.getCompilationUnit().getSourceFile().toUri().toURL();
            file = URLMapper.findFileObject(url);
            if (file == null) {
                //#155161:
                throw new IllegalStateException("Cannot find FileObject for: " + url);
            }
        } catch (MalformedURLException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
        int position = ((JCTree) treePath.getLeaf()).pos;
        PositionRef pos = createPositionRef(file, position, Bias.Forward);
        TreePath current = treePath;
        Element element;
        boolean enclElIsCorrespondingEl = true;
        do {
            element = info.getTrees().getElement(current);
            current = current.getParentPath();
            if (element != null && !isSupported(element)) {
                enclElIsCorrespondingEl = false;
            }
        } while ((element == null || !isSupported(element)) && current != null);
        return new TreePathHandle(new TreeDelegate(pos, new TreeDelegate.KindPath(treePath), file, ElementHandle.create(element), enclElIsCorrespondingEl));
    }

    /**                                                                                                                                                                                                                        
     * Factory method for creating {@link TreePathHandle}.                                                                                                                                                                     
     *                                                                                                                                                                                                                         
     * @param element for which the {@link TrePathHandle} should be created.                                                                                                                                                  
     * @param info 
     * @return a new {@link TreePathHandle}                                                                                                                                                                                    
     * @throws java.lang.IllegalArgumentException if arguments are not supported
     */
    public static TreePathHandle create(Element element, CompilationInfo info) throws IllegalArgumentException {
        URL u = null;
        String qualName = null;
        Symbol.ClassSymbol clsSym;
        if (element instanceof Symbol.ClassSymbol) {
            clsSym = (Symbol.ClassSymbol) element;
        } else {
            clsSym = (Symbol.ClassSymbol) SourceUtils.getEnclosingTypeElement(element);
        }
        if (clsSym != null && clsSym.classfile != null) {
            try {
                u = clsSym.classfile.toUri().toURL();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            qualName = clsSym.getEnclosingElement().getQualifiedName().toString();
        }
            
        return new TreePathHandle(new ElementDelegate(ElementHandle.create(element), u, qualName, info.getClasspathInfo()));
    }

    private static boolean isSupported(Element el) {
        switch (el.getKind()) {
            case PACKAGE:
            case CLASS:
            case INTERFACE:
            case ENUM:
            case METHOD:
            case CONSTRUCTOR:
            case INSTANCE_INIT:
            case STATIC_INIT:
            case FIELD:
            case ANNOTATION_TYPE:
            case ENUM_CONSTANT:
                return true;
            default:
                return false;
        }
    }

    private static PositionRef createPositionRef(FileObject file, int position, Position.Bias bias) {
        try {
            CloneableEditorSupport ces;
            DataObject dob = DataObject.find(file);
            Object obj = dob.getCookie(org.openide.cookies.OpenCookie.class);
            if (obj instanceof CloneableEditorSupport) {
                return ((CloneableEditorSupport) obj).createPositionRef(position, bias);
            }
            obj = dob.getCookie(org.openide.cookies.EditorCookie.class);
            if (obj instanceof CloneableEditorSupport) {
                return ((CloneableEditorSupport) obj).createPositionRef(position, bias);
            }
            @SuppressWarnings("deprecation")
            EditorSupport es = dob.getCookie(EditorSupport.class);
            if (es != null) {
                return es.createPositionRef(position, bias);
            }
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        }
        throw new IllegalStateException("Cannot create PositionRef for file " + file.getPath() + ". CloneableEditorSupport not found");
    }
    
    @Override
    public String toString() {
        return "TreePathHandle[delegate:"+delegate+"]";
    }

    static interface Delegate {
        public FileObject getFileObject();

        public TreePath resolve(final CompilationInfo compilationInfo) throws IllegalArgumentException;

        public boolean equalsHandle(Delegate obj);

        public int hashCode();

        public Element resolveElement(final CompilationInfo info);

        public Tree.Kind getKind();
    }

    private static final class TreeDelegate implements Delegate {
        
        private final PositionRef position;
        private final KindPath kindPath;
        private final FileObject file;
        private final ElementHandle enclosingElement;
        private final boolean enclElIsCorrespondingEl;
        private final Tree.Kind kind;

        private TreeDelegate(PositionRef position, KindPath kindPath, FileObject file, ElementHandle element, boolean enclElIsCorrespondingEl) {
            this.kindPath = kindPath;
            this.position = position;
            this.file = file;
            this.enclosingElement = element;
            this.enclElIsCorrespondingEl = enclElIsCorrespondingEl;
            if (kindPath != null) {
                this.kind = kindPath.kindPath.get(0);
            } else {
                if (enclElIsCorrespondingEl) {
                    ElementKind k = element.getKind();
                    if (k.isClass() || k.isInterface()) {
                        kind = Tree.Kind.CLASS;
                    } else if (k.isField()) {
                        kind = Tree.Kind.VARIABLE;
                    } else if (k == ElementKind.METHOD || k == ElementKind.CONSTRUCTOR) {
                        kind = Tree.Kind.METHOD;
                    } else {
                        kind = null;
                    }
                } else {
                    kind = null;
                }
            }
        }

        /**                                                                                                                                                                                                                        
         * getter for FileObject from give TreePathHandle                                                                                                                                                                          
         * @return FileObject for which was this handle created                                                                                                                                                                    
         */
        public FileObject getFileObject() {
            return file;
        }

        /**                                                                                                                                                                                                                        
         * Resolves an {@link TreePath} from the {@link TreePathHandle}.                                                                                                                                                           
         * @param compilationInfo representing the {@link javax.tools.CompilationTask}                                                                                                                                             
         * @return resolved subclass of {@link Element} or null if the element does not exist on                                                                                                                                    
         * the classpath/sourcepath of {@link javax.tools.CompilationTask}.
         * @throws IllegalArgumentException when this {@link TreePathHandle} is not created for a source
         * represented by the compilationInfo.
         */
        public TreePath resolve(final CompilationInfo compilationInfo) throws IllegalArgumentException {
            assert compilationInfo != null;
            if (!compilationInfo.getFileObject().equals(getFileObject())) {
                StringBuilder debug  = new StringBuilder();
                FileObject    mine   = getFileObject();
                FileObject    remote = compilationInfo.getFileObject();
                
                debug.append("TreePathHandle [" + FileUtil.getFileDisplayName(mine) + "] was not created from " + FileUtil.getFileDisplayName(remote));
                debug.append("\n");

                try {
                    debug.append("mine: id=" + System.identityHashCode(mine) + ", valid=" + mine.isValid() + ", url=");
                    debug.append(mine.getURL().toExternalForm());
                } catch (FileStateInvalidException ex) {
                    debug.append(ex.getMessage());
                }

                debug.append("\n");
                
                try {
                    debug.append("remote: id=" + System.identityHashCode(remote) + ", valid=" + remote.isValid() + ", url=");
                    debug.append(remote.getURL().toExternalForm());
                } catch (FileStateInvalidException ex) {
                    debug.append(ex.getMessage());
                }

                throw new IllegalArgumentException(debug.toString());
            }
            Element element = enclosingElement.resolve(compilationInfo);
            TreePath tp = null;
            if (element != null) {
                TreePath startPath = compilationInfo.getTrees().getPath(element);
                if (startPath == null) {
                    Logger.getLogger(TreePathHandle.class.getName()).fine("compilationInfo.getTrees().getPath(element) returned null for element %s " + element + "(" + file.getPath() + ")");    //NOI18N
                } else {
                    tp = compilationInfo.getTreeUtilities().pathFor(startPath, position.getOffset() + 1);
                }
            }
            if (tp != null && new KindPath(tp).equals(kindPath)) {
                return tp;
            }
            tp = compilationInfo.getTreeUtilities().pathFor(position.getOffset() + 1);
            while (tp != null) {
                KindPath kindPath1 = new KindPath(tp);
                kindPath.getList().remove(Tree.Kind.ERRONEOUS);
                if (kindPath1.equals(kindPath)) {
                    return tp;
                }
                tp = tp.getParentPath();
            }
            return null;
        }

        public boolean equalsHandle(Delegate obj) {
            TreeDelegate other = (TreeDelegate) obj;
            
            try {
                if (this.position == null && other.position == null) {
                    assert this.enclElIsCorrespondingEl;
                    assert other.enclElIsCorrespondingEl;
                    return this.enclosingElement.equals(other.enclosingElement);
                }
                if (this.position.getPosition().getOffset() != this.position.getPosition().getOffset()) {
                    return false;
                }
                if (this.file != other.file && (this.file == null || !this.file.equals(other.file))) {
                    return false;
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            if (this.position == null) {
                return 553 + enclosingElement.hashCode();
            }
            int hash = 7;
            hash = 79 * hash + this.position.getOffset();
            hash = 79 * hash + (this.file != null ? this.file.hashCode() : 0);
            return hash;
        }

        /**                                                                                                                                                                                                                        
         * Resolves an {@link Element} from the {@link TreePathHandle}.                                                                                                                                                            
         * @param compilationInfo representing the {@link javax.tools.CompilationTask}                                                                                                                                             
         * @return resolved subclass of {@link Element} or null if the element does not exist on                                                                                                                                    
         * the classpath/sourcepath of {@link javax.tools.CompilationTask}.                                                                                                                                                        
         */
        public Element resolveElement(final CompilationInfo info) {
            TreePath tp = null;
            IllegalStateException ise = null;
            try {
                if ((this.file != null && info.getFileObject() != null) && info.getFileObject().equals(this.file) && this.position != null) {
                    tp = this.resolve(info);
                }
            } catch (IllegalStateException i) {
                ise = i;
            }
            if (tp == null) {
                if (enclElIsCorrespondingEl) {
                    Element e = enclosingElement.resolve(info);
                    if (e == null) {
                        Logger.getLogger(TreePathHandle.class.getName()).severe("Cannot resolve" + enclosingElement + " in " + info.getClasspathInfo());    //NOI18N
                    }
                    return e;
                } else {
                    if (ise == null) {
                        return null;
                    }
                    throw ise;
                }
            }
            Element el = info.getTrees().getElement(tp);
            if (el == null) {
                Logger.getLogger(TreePathHandle.class.toString()).fine("info.getTrees().getElement(tp) returned null for " + tp);
                if (enclElIsCorrespondingEl) {
                    Element e = enclosingElement.resolve(info);
                    if (e == null) {
                        Logger.getLogger(TreePathHandle.class.getName()).fine("Cannot resolve" + enclosingElement + " in " + info.getClasspathInfo());    //NOI18N
                    }
                    return e;
                } else {
                    return null;
                }
            } else {
                return el;
            }
        }

        /**                                                                                                                                                                                                                        
         * Returns the {@link Tree.Kind} of this TreePathHandle,                                                                                                                                                                   
         * it returns the kind of the {@link Tree} from which the handle                                                                                                                                           
         * was created.                                                                                                                                                                                                            
         *                                                                                                                                                                                                                         
         * @return {@link Tree.Kind}                                                                                                                                                                                               
         */
        public Tree.Kind getKind() {
            return kind;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName()+"[kind:" + kind + ", enclosingElement:" + enclosingElement +", file:" + file + "]";
        }

        static class KindPath {
            private ArrayList<Tree.Kind> kindPath = new ArrayList();

            KindPath(TreePath treePath) {
                while (treePath != null) {
                    kindPath.add(treePath.getLeaf().getKind());
                    treePath = treePath.getParentPath();
                }
            }

            public int hashCode() {
                return kindPath.hashCode();
            }

            public boolean equals(Object object) {
                if (object instanceof KindPath) {
                    return kindPath.equals(((KindPath) object).kindPath);
                }
                return false;
            }

            public ArrayList<Tree.Kind> getList() {
                return kindPath;
            }
        }

    }
    
    private static final class ElementDelegate implements Delegate {

        private final ElementHandle<? extends Element> el;
        private final URL source;
        private final String qualName;
        private final ClasspathInfo cpInfo;

        public ElementDelegate(ElementHandle<? extends Element> el, URL source, String qualName, ClasspathInfo cpInfo) {
            this.el = el;
            this.source = source;
            this.qualName = qualName;
            this.cpInfo = cpInfo;
        }

        public FileObject getFileObject() {
            //source does not exist
            FileObject file = SourceUtils.getFile(el, cpInfo);
            //tzezula: Very strange and probably useless
            if (file == null && source != null) {
                FileObject fo = URLMapper.findFileObject(source);
                if (fo == null) {
                    log.log(Level.INFO, "There is no fileobject for source: " +source + ". Was this file removed?");
                    return file;
                }
                file = fo;
                if (fo.getNameExt().endsWith(FileObjects.SIG)) {
                    //NOI18N
                    //conversion sig -> class
                    String pkgName = FileObjects.convertPackage2Folder(qualName);
                    StringTokenizer tk = new StringTokenizer(pkgName, "/"); //NOI18N
                    for (int i = 0; fo != null && i <= tk.countTokens(); i++) {
                        fo = fo.getParent();
                    }
                    if (fo != null) {
                        try {
                            URL url = fo.getURL();
                            URL sourceRoot = null;//XXX: Index.getSourceRootForClassFolder(url);
                            if (sourceRoot != null) {
                                FileObject root = URLMapper.findFileObject(sourceRoot);
                                String resourceName = FileUtil.getRelativePath(fo, URLMapper.findFileObject(source));
                                file = root.getFileObject(resourceName.replace('.'+FileObjects.SIG, '.'+FileObjects.CLASS)); //NOI18N
                            } else {
                                Logger.getLogger(TreePathHandle.class.getName()).fine("Index.getSourceRootForClassFolder(url) returned null for url=" + url); //NOI18N
                            }
                        } catch (FileStateInvalidException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
            
            return file;
        }

        public TreePath resolve(CompilationInfo compilationInfo) throws IllegalArgumentException {
            Element e = resolveElement(compilationInfo);
            
            if (e == null) {
                return null;
            }
            return compilationInfo.getTrees().getPath(e);
        }

        public Element resolveElement(CompilationInfo info) {
            return el.resolve(info);
        }

        public Kind getKind() {
            switch (el.getKind()) {
                case PACKAGE:
                    return Kind.COMPILATION_UNIT;
                    
                case ENUM:
                case CLASS:
                case ANNOTATION_TYPE:
                case INTERFACE:
                    return Kind.CLASS;
                    
                case ENUM_CONSTANT:
                case FIELD:
                case PARAMETER:
                case LOCAL_VARIABLE:
                case EXCEPTION_PARAMETER:
                    return Kind.VARIABLE;
                    
                case METHOD:
                case CONSTRUCTOR:
                    return Kind.METHOD;
                    
                case STATIC_INIT:
                case INSTANCE_INIT:
                    return Kind.BLOCK;
                    
                case TYPE_PARAMETER:
                    return Kind.TYPE_PARAMETER;
                    
                case OTHER:
                default:
                    return Kind.OTHER;
            }
        }

        public boolean equalsHandle(Delegate obj) {
            ElementDelegate other = (ElementDelegate) obj;
            
            return el.signatureEquals(other.el) && cpInfo.equals(other.cpInfo);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(el.getSignature());
        }
        
        @Override
        public String toString() {
            return this.getClass().getSimpleName()+"[elementHandle:"+el+", url:"+source+"]";
        }
    }
    
}                                                                                                                                                                                                                              
