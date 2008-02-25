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
package org.netbeans.modules.languages.php;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.swing.text.Document;

import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Element;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.OccurrencesFinder;
import org.netbeans.modules.gsf.api.ParseEvent;
import org.netbeans.modules.gsf.api.ParseListener;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.PositionManager;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;
import org.netbeans.modules.gsf.api.SourceFileReader;
import org.netbeans.modules.php.model.ModelAccess;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.SourceElement;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;


/**
 * @author ads
 *
 */
public class PhpParser implements Parser {
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.Parser#createHandle(org.netbeans.modules.gsf.api.CompilationInfo, org.netbeans.modules.gsf.api.Element)
     */
    public <T extends Element> ElementHandle<T> createHandle(
            CompilationInfo info, T element )
    {
        return new PhpElementHandle(info, element);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.Parser#getMarkOccurrencesTask(int)
     */
    public OccurrencesFinder getMarkOccurrencesTask( int caretPosition ) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.Parser#getPositionManager()
     */
    public PositionManager getPositionManager() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.Parser#getSemanticAnalysisTask()
     */
    public SemanticAnalyzer getSemanticAnalysisTask() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.Parser#parseFiles(java.util.List, org.netbeans.modules.gsf.api.ParseListener, org.netbeans.modules.gsf.api.SourceFileReader)
     */
    public void parseFiles( List<ParserFile> files, ParseListener listener,
            SourceFileReader reader )
    {
        for (ParserFile file : files) {
            ParseEvent beginEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, null);
            listener.started(beginEvent);

            ParserResult result = null;
            Document doc = null;
            try {
                DataObject dataObject = DataObject.find( file.getFileObject() );
                EditorCookie editorCookie = 
                    dataObject.getCookie(EditorCookie.class);
                doc = editorCookie.openDocument();
            }
            catch (DataObjectNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            if ( doc == null ){
                return;
            }
            PhpModel model = ModelAccess.getAccess().getModel( 
                    ModelAccess.getModelOrigin( file.getFileObject() ));
            model.sync();
            
            try {
                CharSequence buffer = reader.read(file);
                int offset = reader.getCaretOffset(file);
                result = new PhpParseResult( file , model );
            } catch (IOException ioe) {
                listener.exception(ioe);
            }

            ParseEvent doneEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, result);
            listener.finished(doneEvent);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.Parser#resolveHandle(org.netbeans.modules.gsf.api.CompilationInfo, org.netbeans.modules.gsf.api.ElementHandle)
     */
    public <T extends Element> T resolveHandle( CompilationInfo info,
            ElementHandle<T> handle )
    {
        if(handle instanceof PhpElementHandle) {
            return (T)((PhpElementHandle)handle).getElement();
        }
        return (T)new Element() {

            public String getIn() {
                // TODO Auto-generated method stub
                return null;
            }

            public ElementKind getKind() {
                // TODO Auto-generated method stub
                return null;
            }

            public Set<Modifier> getModifiers() {
                // TODO Auto-generated method stub
                return null;
            }

            public String getName() {
                // TODO Auto-generated method stub
                return null;
            }
            
        };
    }

    /**
     * A wrapper for the {@link org.netbeans.modules.php.model.SourceElement} 
     * that implements the {@link org.netbeans.modules.gsf.api.Element} interface.
     * 
     * @todo move this class into appropriated package as top-level class,
     * e.g. org.netbeans.modules.php.model.PhpElement.
     */
    public static class PhpElement implements Element {
        SourceElement sourceElement;
        
        public PhpElement(SourceElement sourceElement) {
            this.sourceElement = sourceElement;
        }

        public String getName() {
            // TODO Return name for the named elements
            return null;
        }

        public String getIn() {
            // TODO ???
            return null;
        }

        public ElementKind getKind() {
            return null;
        }

        public Set<Modifier> getModifiers() {
            return null;
        }
        
        public SourceElement getSourceElement() {
            return sourceElement;
        }
        
    }

    private static class PhpElementHandle<T extends Element> 
            extends ElementHandle<T> {

        private final CompilationInfo info;
        private final T element;

        private PhpElementHandle(CompilationInfo info, T element) {
            this.element = element;
            this.info = info;
        }
        
        public T getElement() {
            return element;
        }

        /**
         * Return the FileObject associated with this handle, or null
         * if the file is unknown or in a parse tree (in which case the
         * file object is the same as the file object in the CompilationInfo
         * for the root of the parse tree.
         */
        @Override
        public FileObject getFileObject() {
            return info.getFileObject(); 
        }

        /** 
         * Tests if the handle has the same signature as the parameter.
         * The handles with the same signatures are resolved into the same
         * element in the same {@link javax.tools.JavaCompiler} task, but may be
         * resolved into the different {@link Element}s in the different 
         * {@link javax.tools.JavaCompiler} tasks.z
         * @param handle to be checked
         * @return true if the handles resolve into the same {@link Element}s
         * in the same {@link javax.tools.JavaCompiler} task.
         */
        @Override
        public boolean signatureEquals(ElementHandle<? extends Element> handle) {
            // XXX TODO
            return false;
        }
        
    }

}
