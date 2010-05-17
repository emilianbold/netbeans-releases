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
package org.netbeans.modules.visualweb.insync.java;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;

import javax.swing.event.DocumentEvent;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;

import org.openide.filesystems.FileObject;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;

import org.netbeans.modules.visualweb.insync.ParserAnnotation;
import org.netbeans.modules.visualweb.insync.SourceUnit;
import org.netbeans.modules.visualweb.insync.UndoManager;
import org.netbeans.modules.visualweb.insync.Unit.State;
import org.netbeans.modules.visualweb.insync.Util;

/**
 * <p>A SourceUnit that manages a Java source file.</p>
 * @author Carl Quinn
 * @version 1.0
 */
public class JavaUnit extends SourceUnit {

    URLClassLoader classLoader;
    private boolean markSourceDirty = true;
    private enum ImportStatus {
        needed, not_needed, exists, not_allowed
    }
    ParserAnnotation[] errors = ParserAnnotation.EMPTY_ARRAY;

    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct a JavaUnit from an existing source Document
     */
    public JavaUnit(FileObject fobj, URLClassLoader cl, UndoManager undoManager) {
        super(fobj, undoManager);
        //Trace.enableTraceCategory("insync.java");
    }

    /*
     * Clean up all of our big resources.
     * @see org.netbeans.modules.visualweb.insync.Unit#destroy()
     */
    public void destroy() {
        classLoader = null;
        super.destroy();
    }

    //---------------------------------------------------------------------------------------- Input

    protected void read(char[] cbuf, int len) {
        
    }
    
    /**
     * Read the errors in source
     */
    protected void readErrors() {
        ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                List<ParserAnnotation> parserAnnotationsList = new ArrayList<ParserAnnotation>();
                for(Diagnostic diagnostic : cinfo.getDiagnostics()) {
                    if(diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                        ParserAnnotation annotation = new ParserAnnotation(diagnostic.getMessage(Locale.getDefault()),
                                fobj, (int)diagnostic.getLineNumber(), (int)diagnostic.getColumnNumber());
                        parserAnnotationsList.add(annotation);
                    }
                }
                errors = parserAnnotationsList.toArray(ParserAnnotation.EMPTY_ARRAY);
                return null;
            }
        }, fobj, false);
        

        //Java source may not be parsed successfully if the project has dependency on libraries 
        //or source roots that are not yet scanned. Therefore wait for the scan to complete and 
        //check for errors again
        if (errors.length > 0 && SourceUtils.isScanInProgress()) {
            try {
                Future future = JavaSource.forFileObject(fobj).runWhenScanFinished(new Task<CompilationController>() {
                    public void run(CompilationController cc) throws Exception {
                    }
                }, true);
                //Wait till the scan is done
                future.get();
                //Try reading errors again
                readErrors();
            }catch(Exception e){
                //Ignore exception and continue to show the errors to user
            }
        }
    }

    /**
     * @param out
     * @throws java.io.IOException
     */
    public void writeTo(OutputStream out) throws java.io.IOException {
    }
    
    /**
     * Implicit read. Read the document supplied during construction into this model.
     * 
     * @return whether or not the read affected the model.
     */
    public boolean sync() {
        //Grab the current document from our underlying editor. 
        //The property change listener from editor cookie (see SourceUnit)
        //will add UndoableEditListener to the document.
        
        //XXX We need to revisit this as the document is unnecessarily grabbed 
        //It should be probably opened in the JavaClassAdapter before write transaction 
        //to restrict being opened only during flush - Winston
        
        Util.retrieveDocument(fobj, true);
        
        // make sure it is necessary & ok to read.
        State state = getState();
        assert Trace.trace("insync", "SU.sync of " + getName() + " state:" + state );
        if (state == State.CLEAN)
            return false;
        if (state == State.MODELDIRTY) {
            assert Trace.trace("insync", "SU.sync attempt to read source into a dirty model");
            //Trace.printStackTrace();
            return false;
        }

        readErrors();
        
        if(errors.length > 0) {
            setBusted();
        } else {
            setClean();
        }
        
        return true;
    }
    

    //------------------------------------------------------------------------------------ Accessors

    /*
     * Returns the public class in the file JavaUnit represents
     */
    public JavaClass getJavaClass() {
        return JavaClass.getJavaClass(fobj);
    }

    private String getPackageName(CompilationInfo cinfo) {
        Element e = cinfo.getTrees().getElement(new TreePath(cinfo.getCompilationUnit()));
        if (e != null && e.getKind() == ElementKind.PACKAGE) {
            return ((PackageElement) e).getQualifiedName().toString();
        }
        return null;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#getErrors()
     */
    public ParserAnnotation[] getErrors() {
        if(errors == null)
            readErrors();
        return errors;
    }
    
    //------------------------------------------------------------------------------------- Mutators

    /**
     * Ensure that a given import is in the list, if not try to add it
     * @param 
     * @return true if the import was added if required or if the import is not
     * required to be added
     */
    public boolean ensureImport(final String fqn) {
        ImportStatus result = (ImportStatus)WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                ImportStatus status = checkImportStatus(wc, fqn);
                if(status.equals(ImportStatus.needed)) {
                    addImport(wc, fqn);
                }
                return status;
            }
        }, getFileObject()); 
        //Check if the import is successfully added
        if(result.equals(ImportStatus.needed)) {
            return isImported(fqn);
        }else if(result.equals(ImportStatus.not_allowed)){
            return false;
        }else {
            return true;
        }
    }
 
   /**
     * Add a new import 
     *
     */
    private void addImport(WorkingCopy wc, String fqn) {
        ImportTree imprt = TreeMakerUtils.createImport(wc, fqn);
        CompilationUnitTree cunit = wc.getTreeMaker().addCompUnitImport(wc.getCompilationUnit(), imprt);
        wc.rewrite(wc.getCompilationUnit(), cunit);
    }
    
    /**
     * @return true if the class has been imported
     * 
     */
    private boolean isImported(final String fqn) {
        return (Boolean)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {        
                for (ImportTree importTree : cinfo.getCompilationUnit().getImports()) {
                    if(importTree.getQualifiedIdentifier().getKind() == Tree.Kind.MEMBER_SELECT) {
                        MemberSelectTree memSelectTree = (MemberSelectTree)importTree.getQualifiedIdentifier();
                        if(getFQN(cinfo, memSelectTree).equals(fqn)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }, getFileObject());
    }
    
    /**
     * @return computes the fqn for ExpressionTree taking multi byte character into account
     * toString() on trees do not support multi-byte characters, therefore this method is added
     */
    private String getFQN(CompilationInfo cinfo, ExpressionTree tree) {
        if(tree.getKind() == Tree.Kind.MEMBER_SELECT) {
            MemberSelectTree memSelectTree = (MemberSelectTree)tree;
            return getFQN(cinfo, memSelectTree.getExpression()) + "." + memSelectTree.getIdentifier().toString();
        }else if(tree.getKind() == Tree.Kind.IDENTIFIER) {
            return ((IdentifierTree)tree).getName().toString();
        }
        return tree.toString();
    }
    
    
    /**
     * @return needed if the import is required for the class
     *         not_needed if the import is not required
     *         exists if the import exists
     *         not_allowed if the import is not allowed
     * Checks if the class is in same package, or if the class is already imported,
     * or if the wild card imports exists for class's package and if a different
     * class by same name but in different package has been imported
     */
    private ImportStatus checkImportStatus(CompilationInfo cinfo, String fqn) {
        int index = fqn.lastIndexOf('.');
        String pkgName = null;
        String className = null;
        if(index != -1) {
            pkgName = fqn.substring(0, index);
            className = fqn.substring(index+1);
        }
        
        //check if they belong to same package
        String currentPkgName = getPackageName(cinfo);
        if(pkgName == null || pkgName.equals(currentPkgName)) {
            return ImportStatus.not_needed;
        }
        
        //check if the class by same name exists in the package
        if(cinfo.getElements().getTypeElement(currentPkgName + "." + className) != null) {
            return ImportStatus.not_allowed;
        }
        
        for (ImportTree importTree : cinfo.getCompilationUnit().getImports()) {
            if(importTree.getQualifiedIdentifier().getKind() == Tree.Kind.MEMBER_SELECT) {
                MemberSelectTree memSelectTree = (MemberSelectTree)importTree.getQualifiedIdentifier();
                String importClassName = memSelectTree.getIdentifier().toString();
                //FQN match
                if(getFQN(cinfo, memSelectTree).equals(fqn)) {
                    return ImportStatus.exists;
                }
                //Check for wild card imports
                if(importClassName.equals("*")) {
                    if(getFQN(cinfo, memSelectTree.getExpression()).equals(pkgName)) {
                        return ImportStatus.exists;
                    }
                }else {
                    //Check if the import is not allowed because of name clash
                    if(importClassName.equals(className)){
                        return ImportStatus.not_allowed;
                    }
                }
            }
        }
        return ImportStatus.needed;
    }    
    
    /**
     *
     */
    public JavaClass addClass(String name) {
        return null;
    }

    /**
     *
     */
    public void removeImport(String ident) {
    }

    /**
     *
     */
    public void removeClass(JavaClass cls) {
    }

    //-------------------------------------------------------------------------------------- 
    public boolean flush() {    
        return true;
    }

    public void writeTo(Writer w) throws IOException {
    }

    public void dumpTo(PrintWriter w) {
    }
    
    protected synchronized void firstWriteLock() {
        markSourceDirty = false;
    }

    protected synchronized void lastWriteUnlock() {
        markSourceDirty = true;
    }    
    //----------------------------------------------------------------------------- DocumentListener

    /*
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) {
        assert Trace.trace("insync-listener", "SU.insertUpdate");
        undoManager.notifyBufferEdited(this);
        if(markSourceDirty) {
            setSourceDirty();
        }
    }

    /*
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    public void removeUpdate(DocumentEvent e) {
        assert Trace.trace("insync-listener", "SU.removeUpdate");
        undoManager.notifyBufferEdited(this);
        if(markSourceDirty) {
            setSourceDirty();
        }
    }
    
}
