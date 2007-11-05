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
package org.netbeans.napi.gsfret.source;

import java.io.IOException;
import org.netbeans.api.gsf.Index;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.gsfret.source.parsing.SourceFileObject;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.netbeans.api.gsf.annotations.CheckForNull;
import org.netbeans.api.lexer.TokenHierarchy;


/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 * Assorted information about the Source.
 *
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public class CompilationInfo extends org.netbeans.api.gsf.CompilationInfo {
    private Phase phase = Phase.MODIFIED;
    private CompilationUnitTree compilationUnit;
    private ParserTaskImpl javacTask;
    final SourceFileObject jfo;
    final Source javaSource;
    boolean needsRestart;
    private Language language;

    CompilationInfo() throws IOException {
        super(null);
        this.javaSource = null;
        this.jfo = null;
        this.javacTask = null;
    }

    CompilationInfo(final Source javaSource, final FileObject fo, final ParserTaskImpl javacTask)
        throws IOException {
        super(fo);
        assert javaSource != null;
        this.javaSource = javaSource;

        //this.jfo = fo != null ? javaSource.jfoProvider.createJavaFileObject(fo) : null;        
        if (fo.isValid() && !fo.isVirtual()) {
            this.jfo = new SourceFileObject(fo, true);
        } else {
            this.jfo = null;
        }

        this.javacTask = javacTask;
    }

    // API of the class --------------------------------------------------------

    /**
     * Returns the current phase of the {@link Source}.
     *
     *
     * @return {@link Source.Phase} the state which was reached by the {@link Source}.
     */
    public Phase getPhase() {
        return this.phase;
    }

    /**
     * Returns the javac tree representing the source file.
     * @return {@link CompilationUnitTree} the compilation unit cantaining the top level classes contained in the,
     * java source file. It may return null when the phase is less than {@link JavaSource.Phase#PARSED}
     */
    @CheckForNull
    public CompilationUnitTree getCompilationUnit() {
        if (this.jfo == null) {
            throw new IllegalStateException();
        }

        return this.compilationUnit;
    }

    /**
     * Returns the content of the file represented by the {@link Source}.
     *
     *
     * @return String the java source
     */
    public String getText() {
        if (this.jfo == null) {
            throw new IllegalStateException();
        }

        try {
            return this.jfo.getCharContent(false).toString();
        } catch (IOException ioe) {
            //Should never happen
            ErrorManager.getDefault().notify(ioe);

            return null;
        }
    }

    public TokenHierarchy<?> getTokenHierarchy() {
        if (this.jfo == null) {
            throw new IllegalStateException ();
        }
        try {
            return ((SourceFileObject) this.jfo).getTokenHierarchy();
        } catch (IOException ioe) {
            //Should never happen
            ErrorManager.getDefault().notify(ioe);
            return null;
        }
    }
    
    public Source getSource() {
        return javaSource;
    }

    public ClasspathInfo getClasspathInfo() {
        return javaSource.getClasspathInfo();
    }
    
    @Override
    public Index getIndex() {
        return getClasspathInfo().getClassIndex();
    }

    void setPhase(final Phase phase) {
        assert phase != null;
        this.phase = phase;
    }

    void setCompilationUnit(final CompilationUnitTree compilationUnit) {
        assert compilationUnit != null;
        this.compilationUnit = compilationUnit;
    }

    synchronized ParserTaskImpl getParserTask() {
        if (javacTask == null) {
            javacTask =
                javaSource.createParserTask( /*new DiagnosticListenerImpl(errors),*/
                    this);
        }

        return javacTask;
    }

    public Language getLanguage() {
        if (language == null) {
            FileObject fo = getFileObject();
            String mimeType = fo.getMIMEType();
            language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);            
        }
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }    
}
