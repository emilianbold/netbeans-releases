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

import java.util.Map;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.Parser;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.api.gsf.ColoringAttributes;
import java.io.IOException;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.gsf.Error;
import org.netbeans.api.gsf.Index;
import org.netbeans.api.gsf.PositionManager;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.gsf.Language;
import org.openide.filesystems.FileObject;
import static org.netbeans.napi.gsfret.source.Phase.*;

/** 
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *  Class for explicit invocation of compilation phases on a source file.
 *  The implementation delegates to the {@link CompilationInfo} to get the data,
 *  the access to {@link CompilationInfo} is not synchronized, so the class isn't
 *  reentrant.
 *  
 *  XXX: make toPhase automatic in getTrees(), Trees.getElement, etc....
 * @author Petr Hrebejk, Tomas Zezula
 */
public class CompilationController extends CompilationInfo {
    
    //Not private for unit tests
    /*private*/final CompilationInfo delegate;
    
    CompilationController(final CompilationInfo delegate) throws IOException {        
        super();
        assert delegate != null;
        this.delegate = delegate;
    }
        
    // API of the class --------------------------------------------------------
    
    /**
     * Moves the state to required phase. If given state was already reached 
     * the state is not changed. The method will throw exception if a state is 
     * illegal required. Acceptable parameters for thid method are <BR>
     * <LI>{@link org.netbeans.napi.gsfret.source.Source.Phase.PARSED}
     * <LI>{@link org.netbeans.napi.gsfret.source.Source.Phase.ELEMENTS_RESOLVED}
     * <LI>{@link org.netbeans.napi.gsfret.source.Source.Phase.RESOLVED}
     * <LI>{@link org.netbeans.napi.gsfret.source.Source.Phase.UP_TO_DATE}   
     * 
     * @param phase The required phase
     * @return the reached state
     * @throws IllegalArgumentException in case that given state can not be 
     *         reached using this method
     * @throws IOException when the file cannot be red
     */    
    public Phase toPhase(Phase phase ) throws IOException {
        if (phase == MODIFIED) {
            throw new IllegalArgumentException( "Wrong phase" + phase );
        }
        if (delegate.jfo == null) {
            Phase currentPhase = delegate.getPhase();
            if (currentPhase.compareTo(phase)<0) {
                delegate.setPhase(phase);
            }
            return delegate.getPhase();
        }
        else {
            Phase currentPhase = Source.moveToPhase(phase, this.delegate,false);
            return currentPhase.compareTo (phase) < 0 ? currentPhase : phase;
        }
    }            

    /**
     * Returns the current phase of the {@link Source}.
     * 
     * 
     * @return {@link Source.Phase} the state which was reached by the {@link Source}.
     */
    @Override
    public Phase getPhase() {        
        return this.delegate.getPhase();
    }
        
    /**
     * Returns the javac tree representing the source file.
     *
     * @return {@link CompilationUnitTree} the compilation unit cantaining the top level classes contained in the
     * java source file. It may return null when the {@link CompilationController#getPhase} is lower than 
     * {@link JavaSource.Phase#PARSED}. Before calling this method the client has to call {@link CompilationController#toPhase}
     * with required {@link JavaSource.Phase}.
     */
    @Override
    public CompilationUnitTree getCompilationUnit() {
        return this.delegate.getCompilationUnit();
    }

    @Override
    public ParserResult getParserResult() {
        return this.delegate.getParserResult();
    }
    /**
     * Returns the content of the file represented by the {@link Source}.
     * 
     * 
     * @return String the java source
     */
    @Override
    public String getText() {
        return this.delegate.getText();
    }

    @Override
    public TokenHierarchy<?> getTokenHierarchy() {
        return this.delegate.getTokenHierarchy();
    }

    /**
     * Returns the errors in the file represented by the {@link Source}.
     * 
     * 
     * @return an list of {@link Diagnostic}
     */
    @Override
    public List<Error/*Diagnostic*/> getDiagnostics() {
        return this.delegate.getDiagnostics();
    }

//    @Override
//    public Trees getTrees() {
//        return this.delegate.getTrees();
//    }
//
//    @Override
//    public Types getTypes() {
//        return this.delegate.getTypes();
//    }
//    
//    @Override
//    public Elements getElements() {
//        return this.delegate.getElements();
//    }
    
    @Override 
    public Source getSource() {
        return this.delegate.getSource();
    }

    @Override 
    public ClasspathInfo getClasspathInfo() {
        return this.delegate.getClasspathInfo();
    }

    @Override
    public FileObject getFileObject() {
        return this.delegate.getFileObject();
    }

    @Override
    public PositionManager getPositionManager() {
        return this.delegate.getPositionManager();
    }

    @Override
    public Parser getParser() {
        return this.delegate.getParser();
    }

    @Override
    public Document getDocument() throws IOException {
        return this.delegate.getDocument();
    }
        
    // Package private methods -------------------------------------------------
    
    @Override 
    void setPhase(final Phase phase) {
        throw new UnsupportedOperationException ("CompilationController supports only read interface");          //NOI18N
    }   

    @Override 
    void setCompilationUnit(final CompilationUnitTree compilationUnit) {
        throw new UnsupportedOperationException ("CompilationController supports only read interface");          //NOI18N
    }

    @Override 
    public void setParserResult(final ParserResult parserResult) {
        throw new UnsupportedOperationException ("CompilationController supports only read interface");          //NOI18N
    }

    @Override 
    public void setPositionManager(final PositionManager positions) {
        throw new UnsupportedOperationException ("CompilationController supports only read interface");          //NOI18N
    }

    @Override 
    public void setParser(final Parser parser) {
        throw new UnsupportedOperationException ("CompilationController supports only read interface");          //NOI18N
    }

    @Override 
    public void setLanguage(final Language language) {
        throw new UnsupportedOperationException ("CompilationController supports only read interface");          //NOI18N
    }

    @Override
    public Language getLanguage() {
        return this.delegate.getLanguage();
    }

    @Override 
    ParserTaskImpl getParserTask() {        
        return this.delegate.getParserTask();
    }
    
    @Override
    public Index getIndex() {
        return this.delegate.getIndex();
    }
}
