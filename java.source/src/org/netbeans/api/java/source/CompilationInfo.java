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

package org.netbeans.api.java.source;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.swing.text.Document;
import javax.tools.Diagnostic;
import org.netbeans.api.lexer.TokenHierarchy;
import org.openide.filesystems.FileObject;

/** Asorted information about the JavaSource.
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public class CompilationInfo {
    
    
    //INV: never null
    final JavaParserResult impl;
    
    
    
    CompilationInfo (final JavaParserResult impl)  {
        assert impl != null;
        this.impl = impl;
    }
             
    // API of the class --------------------------------------------------------
    
    /**
     * Returns the current phase of the {@link JavaSource}.
     * @return {@link JavaSource.Phase} the state which was reached by the {@link JavaSource}.
     */
    public JavaSource.Phase getPhase() {
        return this.impl.getPhase();
    }
    
    /**
     * Returns tree which was reparsed by an incremental reparse.
     * When the source file wasn't parsed yet or the parse was a full parse
     * this method returns null.
     * <p class="nonnormative">
     * Currently the leaf tree is a MethodTree but this may change in the future.
     * Client of this method is responsible to check the corresponding TreeKind
     * to find out if it may perform on the changed subtree or it needs to
     * reprocess the whole tree.
     * </p>
     * @return {@link TreePath} or null
     * @since 0.31
     */
    public TreePath getChangedTree () {
        return this.impl.getChangedTree();
    }       
    
    /**
     * Returns the javac tree representing the source file.
     * @return {@link CompilationUnitTree} the compilation unit cantaining the top level classes contained in the,
     * java source file. 
     * @throws java.lang.IllegalStateException  when the phase is less than {@link JavaSource.Phase#PARSED}
     */
    public CompilationUnitTree getCompilationUnit() {        
        return this.impl.getCompilationUnit();
    }
    
    /**
     * Returns the content of the file represented by the {@link JavaSource}.
     * @return String the java source
     */
    public String getText() {
        return this.impl.getText();
    }
    
    /**
     * Returns the {@link TokenHierarchy} for the file represented by the {@link JavaSource}.
     * @return lexer TokenHierarchy
     */
    public TokenHierarchy<?> getTokenHierarchy() {
        return this.impl.getTokenHierarchy();
    }
    
    /**
     * Returns the errors in the file represented by the {@link JavaSource}.
     * @return an list of {@link Diagnostic} 
     */
    public List<Diagnostic> getDiagnostics() {
        return this.impl.getDiagnostics();
    }
    
    /**
     * Returns all top level elements defined in file for which the {@link CompilationInfo}
     * was created. The {@link CompilationInfo} has to be in phase {@link JavaSource#Phase#ELEMENTS_RESOLVED}.
     * @return list of top level elements, it may return null when this {@link CompilationInfo} is not
     * in phase {@link JavaSource#Phase#ELEMENTS_RESOLVED} or higher.
     * @throws IllegalStateException is thrown when the {@link JavaSource} was created with no files
     * @since 0.14
     */
    public List<? extends TypeElement> getTopLevelElements () throws IllegalStateException {
        return this.impl.getTopLevelElements();
    }
        
    
    /**
     * Return the {@link Trees} service of the javac represented by this {@link CompilationInfo}.
     * @return javac Trees service
     */
    public Trees getTrees() {
        return this.impl.getTrees();
    }
    
    /**
     * Return the {@link Types} service of the javac represented by this {@link CompilationInfo}.
     * @return javac Types service
     */
    public Types getTypes() {
        return this.impl.getTypes();
    }
    
    /**
     * Return the {@link Elements} service of the javac represented by this {@link CompilationInfo}.
     * @return javac Elements service
     */
    public Elements getElements() {
        return this.impl.getElements();
    }
        
    /**
     * Returns {@link JavaSource} for which this {@link CompilationInfo} was created.
     * @return JavaSource
     */
    public JavaSource getJavaSource() {
        //tzezula: todo
        throw new UnsupportedOperationException ("Not yet supported");
    }
    
    /**
     * Returns {@link ClasspathInfo} for which this {@link CompilationInfo} was created.
     * @return ClasspathInfo
     */
    public ClasspathInfo getClasspathInfo() {
	return this.impl.getClasspathInfo();
    }
    
    /**
     * Returns the {@link FileObject} represented by this {@link CompilationInfo}.
     * @return FileObject
     */
    public FileObject getFileObject() {
        return impl.getFileObject();
    }
    
    /**Return {@link PositionConverter} binding virtual Java source and the real source.
     * Please note that this method is needed only for clients that need to work
     * in non-Java files (eg. JSP files) or in dialogs, like code completion.
     * Most clients do not need to use {@link PositionConverter}.
     * 
     * @return PositionConverter binding the virtual Java source and the real source.
     * @since 0.21
     */
    public PositionConverter getPositionConverter() {
        //tzezula: todo
        throw new UnsupportedOperationException("Not yet supported");
    }
            
    /**
     * Returns {@link Document} of this {@link CompilationInfoImpl}
     * @return Document or null when the {@link DataObject} doesn't
     * exist or has no {@link EditorCookie}.
     * @throws java.io.IOException
     */
    public Document getDocument() throws IOException { //XXX cleanup: IOException is no longer required? Used by PositionEstimator, DiffFacility
        return this.impl.getDocument();
    }
    
    
    /**
     * Returns {@link TreeUtilities}.
     * @return TreeUtilities
     */
    public synchronized TreeUtilities getTreeUtilities() {
        return this.impl.getTreeUtilities();
    }
    
    /**
     * Returns {@link ElementUtilities}.
     * @return ElementUtilities
     */
    public synchronized ElementUtilities getElementUtilities() {
        return this.impl.getElementUtilities();
    }
    
    /**Get the TypeUtilities.
     * @return an instance of TypeUtilities
     */
    public synchronized TypeUtilities getTypeUtilities() {
        return this.impl.getTypeUtilities();
    }        
}
