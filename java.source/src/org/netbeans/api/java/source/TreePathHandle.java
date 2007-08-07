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
                                                                                                                                                                                                                               
package org.netbeans.api.java.source;                                                                                                                                                                                          
                                                                                                                                                                                                                               
import com.sun.source.tree.Tree;                                                                                                                                                                                               
import com.sun.source.util.TreePath;                                                                                                                                                                                           
import com.sun.tools.javac.tree.JCTree;                                                                                                                                                                                        
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.logging.Logger;                                                                                                                                                                                                    
import javax.lang.model.element.Element;                                                                                                                                                                                       
import javax.lang.model.element.ElementKind;
import javax.swing.text.Position.Bias;                                                                                                                                                                                         
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;                                                                                                                                                                                     
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;                                                                                                                                                                                         
import org.openide.loaders.DataObjectNotFoundException;                                                                                                                                                                        
import org.openide.text.CloneableEditorSupport;                                                                                                                                                                                
import org.openide.text.PositionRef;                                                                                                                                                                                           
import org.openide.util.Exceptions;
                                                                                                                                                                                                                               
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
                                                                                                                                                                                                                               
    private PositionRef position;                                                                                                                                                                                              
    private KindPath kindPath;                                                                                                                                                                                                 
    private FileObject file;                                                                                                                                                                                                   
    private ElementHandle enclosingElement;                                                                                                                                                                                    
    private boolean enclElIsCorrespondingEl;
    private Tree.Kind kind;
                                                                                                                                                                                                                               
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
     * @return resolved subclass of {@link Element} or null if the elment does not exist on                                                                                                                                    
     * the classpath/sourcepath of {@link javax.tools.CompilationTask}.
     * @throws {@link IllegalArgumentException} when this {@link TreePathHandle} is not created for a source
     * represented by the compilationInfo.
     */                                                                                                                                                                                                                        
    public TreePath resolve (final CompilationInfo compilationInfo) throws IllegalArgumentException {
        assert compilationInfo != null;
        if (!compilationInfo.getFileObject().equals(getFileObject())) {
            throw new IllegalArgumentException ("TreePathHandle ["+FileUtil.getFileDisplayName(getFileObject())+"] was not created from " + FileUtil.getFileDisplayName(compilationInfo.getFileObject()));
        }                                                                                                                                                                                                                      
        Element element = enclosingElement.resolve(compilationInfo);                                                                                                                                                           
        TreePath tp = null;                                                                                                                                                                                                    
        if (element != null) {                                                                                                                                                                                                 
            TreePath startPath = compilationInfo.getTrees().getPath(element);                                                                                                                                                  
            if (startPath == null) {                                                                                                                                                                                           
                Logger.getLogger(TreePathHandle.class.getName()).fine("compilationInfo.getTrees().getPath(element) returned null for element %s " + element + "(" +file.getPath() +")");    //NOI18N
            } else {                                                                                                                                                                                                           
                tp = compilationInfo.getTreeUtilities().pathFor(startPath, position.getOffset()+1);
            }                                                                                                                                                                                                                  
        }                                                                                                                                                                                                                      
        if (tp!=null && new KindPath(tp).equals(kindPath))                                                                                                                                                                                 
            return tp;                                                                                                                                                                                                         
        tp = compilationInfo.getTreeUtilities().pathFor(position.getOffset()+1);
        while (tp!=null) {
            if (new KindPath(tp).equals(kindPath)) {
                return tp;
            }
            tp = tp.getParentPath();
        }
        return null;
    }

    public boolean equals(Object obj) {
        try {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }   
            final TreePathHandle other = (TreePathHandle) obj;
            if (this.position==null && other.position==null) {
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

    public int hashCode() {
        if (this.position==null) {
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
     * @return resolved subclass of {@link Element} or null if the elment does not exist on                                                                                                                                    
     * the classpath/sourcepath of {@link javax.tools.CompilationTask}.                                                                                                                                                        
     */                                                                                                                                                                                                                        
    public Element resolveElement(final CompilationInfo info) {                                                                                                                                                                
        TreePath tp = null;                                                                                                                                                                                                    
        IllegalStateException ise = null;
        try {
            if ((this.file!=null && info.getFileObject()!=null) && info.getFileObject().equals(this.file)) {
                tp = this.resolve(info);
            }
        } catch (IllegalStateException i) {
            ise=i;
        }
        if (tp==null) {                                                                                                                                                                                                        
            if (enclElIsCorrespondingEl) {
                Element e = enclosingElement.resolve(info);
                if (e==null) {
                    Logger.getLogger(TreePathHandle.class.getName()).severe("Cannot resolve" + enclosingElement + " in " + info.getClasspathInfo());    //NOI18N
                }
                return e;                                                                                                                                                                         
            } else {
                if (ise==null)
                    return null;
                throw ise;                                                                                                                                                                                                   
            }                                                                                                                                                                                                                  
        }                                                                                                                                                                                                                      
        Element el = info.getTrees().getElement(tp);
        if (el==null) {
            Logger.getLogger(TreePathHandle.class.toString()).fine("info.getTrees().getElement(tp) returned null for " + tp);
            if (enclElIsCorrespondingEl) {
                Element e = enclosingElement.resolve(info);
                if (e==null) {
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
                                                                                                                                                                                                                               
    private TreePathHandle(PositionRef position, KindPath kindPath, FileObject file, ElementHandle element, boolean enclElIsCorrespondingEl) {                                                                                 
        this.kindPath = kindPath;                                                                                                                                                                                              
        this.position = position;                                                                                                                                                                                              
        this.file = file;                                                                                                                                                                                                      
        this.enclosingElement = element;                                                                                                                                                                                       
        this.enclElIsCorrespondingEl = enclElIsCorrespondingEl;
        if (kindPath!=null)
            this.kind = kindPath.kindPath.get(0);
        else {
            if (enclElIsCorrespondingEl) {
                ElementKind k = element.getKind();
                if (k.isClass() || k.isInterface()) {
                    kind = Tree.Kind.CLASS;
                } else if (k.isField()) {
                    kind = Tree.Kind.VARIABLE;
                } else if (k==ElementKind.METHOD || k==ElementKind.CONSTRUCTOR) {
                    kind = Tree.Kind.METHOD;
                }
            }
        } 
    }
    
    /**                                                                                                                                                                                                                        
     * Factory method for creating {@link TreePathHandle}.                                                                                                                                                                     
     *                                                                                                                                                                                                                         
     * @param treePath for which the {@link TrePathHandle} should be created.                                                                                                                                                  
     * @param info 
     * @return a new {@link TreePathHandle}                                                                                                                                                                                    
     * @throws java.lang.IllegalArgumentException if arguments are not supported
     */                                                                                                                                                                                                                        
    public static TreePathHandle create (final TreePath treePath, CompilationInfo info) throws IllegalArgumentException {                                                                                                      
        FileObject file;
        try {
            file = URLMapper.findFileObject(treePath.getCompilationUnit().getSourceFile().toUri().toURL());
        } catch (MalformedURLException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
        int position = ((JCTree) treePath.getLeaf()).pos;               
        CloneableEditorSupport s = findCloneableEditorSupport(file);                                                                                                                                           
        PositionRef pos = s.createPositionRef(position, Bias.Forward);                                                                                                                                                         
        TreePath current = treePath;                                                                                                                                                                                           
        Element element;                                                                                                                                                                                                       
        boolean enclElIsCorrespondingEl = true;                                                                                                                                                                                
        do {                                                                                                                                                                                                                   
            element = info.getTrees().getElement(current);                                                                                                                                                                     
            current = current.getParentPath();                                                                                                                                                                                 
            if (element!=null && !isSupported(element)) {                                                                                                                                                                      
                enclElIsCorrespondingEl=false;                                                                                                                                                                                 
            }                                                                                                                                                                                                                  
        } while ((element == null || !isSupported(element)) && current !=null);                                                                                                                                                
        return new TreePathHandle(pos, new KindPath(treePath), file,ElementHandle.create(element), enclElIsCorrespondingEl);                                                                                   
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
        TreePath treePath = info.getTrees().getPath(element);
        if (treePath != null)
            return create(treePath, info);
        //source does not exist
        ElementHandle eh = ElementHandle.create(element);
        return new TreePathHandle(null, null, null, eh, true);
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
            case ENUM_CONSTANT: return true;                                                                                                                                                                                   
            default: return false;                                                                                                                                                                                             
        }                                                                                                                                                                                                                      
    }                                                                                                                                                                                                                          
                                                                                                                                                                                                                               
    private static CloneableEditorSupport findCloneableEditorSupport(FileObject file) {                                                                                                                                        
        try {                                                                                                                                                                                                                  
            DataObject dob = DataObject.find(file);                                                                                                                                                                            
            Object obj = dob.getCookie(org.openide.cookies.OpenCookie.class);                                                                                                                                                  
            if (obj instanceof CloneableEditorSupport) {                                                                                                                                                                       
                return (CloneableEditorSupport)obj;                                                                                                                                                                            
            }                                                                                                                                                                                                                  
            obj = dob.getCookie(org.openide.cookies.EditorCookie.class);                                                                                                                                                       
            if (obj instanceof CloneableEditorSupport) {                                                                                                                                                                       
                return (CloneableEditorSupport)obj;                                                                                                                                                                            
            }                                                                                                                                                                                                                  
        } catch (DataObjectNotFoundException ex) {                                                                                                                                                                             
            ex.printStackTrace();                                                                                                                                                                                              
        }                                                                                                                                                                                                                      
        return null;                                                                                                                                                                                                           
    }                                                                                                                                                                                                                          
                                                                                                                                                                                                                               
    private static class KindPath {                                                                                                                                                                                            
        private ArrayList<Tree.Kind> kindPath = new ArrayList();                                                                                                                                                               
                                                                                                                                                                                                                               
        private KindPath(TreePath treePath) {                                                                                                                                                                                  
            while (treePath!=null) {                                                                                                                                                                                           
                kindPath.add(treePath.getLeaf().getKind());                                                                                                                                                                    
                treePath = treePath.getParentPath();                                                                                                                                                                           
            }                                                                                                                                                                                                                  
        }                                                                                                                                                                                                                      
                                                                                                                                                                                                                               
        public int hashCode() {                                                                                                                                                                                                
            return kindPath.hashCode();                                                                                                                                                                                        
        }                                                                                                                                                                                                                      
                                                                                                                                                                                                                               
        public boolean equals(Object object) {                                                                                                                                                                                 
            if (object instanceof KindPath)                                                                                                                                                                                    
                return kindPath.equals(((KindPath)object).kindPath);                                                                                                                                                           
            return false;                                                                                                                                                                                                      
        }                                                                                                                                                                                                                      
    }                                                                                                                                                                                                                          
}                                                                                                                                                                                                                              
