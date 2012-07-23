/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.completion.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.java.source.parsing.ClasspathInfoProvider;
import org.netbeans.modules.java.source.parsing.JavacParserResult;
import org.netbeans.modules.javafx2.editor.completion.beans.BeanModelBuilder;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBeanInfo;
import org.netbeans.modules.javafx2.editor.completion.impl.ErrorMark;
import org.netbeans.modules.javafx2.editor.completion.impl.XmlLexerParser;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.xml.sax.SAXException;

/**
 *
 * @author sdedic
 */
public class FxmlParser extends Parser implements ErrorReporter {
    private FxmlParserResult result;
    
    private Collection<ErrorMark>   problems = new ArrayList<ErrorMark>();
    
    private FxModel model;
    
    private CompilationInfo info;
    
    private Snapshot snapshot;
    
    private BuildEnvironment env;
    
    private List<ModelBuilderStep> steps;

    @Override
    public void addError(ErrorMark em) {
        problems.add(em);
    }
    
    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        this.snapshot = snapshot;
        TokenHierarchy h = snapshot.getTokenHierarchy();
        XmlLexerParser tokenParser = new XmlLexerParser(h);
        FxModelBuilder builder = new FxModelBuilder();
        
        FileObject fo = snapshot.getSource().getFileObject();
        if (fo != null) {
            builder.setBaseURL(fo.toURL());
        }
        
        tokenParser.setContentHandler(builder);
        
        try {
            tokenParser.parse();
        } catch (SAXException ex) {
            throw new ParseException("Parsing failed", ex);
        }
        
        final ClasspathInfo cpInfo = ClasspathInfo.create(snapshot.getSource().getFileObject());
        
        problems.addAll(builder.getErrors());
        model = builder.getModel();
        
        class UT extends UserTask implements ClasspathInfoProvider {

            @Override
            public ClasspathInfo getClasspathInfo() {
                return cpInfo;
            }

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                JavacParserResult res = (JavacParserResult)resultIterator.getParserResult();
                info = res.get(CompilationInfo.class);
                env = createBuildEnvironment();
                initModelSteps();

                for (ModelBuilderStep step : steps) {
                    FxNodeVisitor visitor = step.createVisitor(env);
                    model.accept(visitor);
                }
            }
        };
        
        UT ut = new UT();
        
        ParserManager.parse("text/x-java", ut);
        
        result = new FxmlParserResult(snapshot, model, problems);
        result.setTokenHierarchy(h);
    }
    
    private void initModelSteps() {
        steps = new ArrayList<ModelBuilderStep>();
        steps.add(new NamedInstancesCollector());
        steps.add(new TypeResolver());
        steps.add(new ReferenceResolver());
        steps.add(new PropertyResolver());
    }
    
    private BuildEnvironment createBuildEnvironment() {
        BuildEnvironment env = new BuildEnvironment();
        env.setAccessor(new ModelBuilderAccessor());
        env.setBeanProvider(new FxBeanInfo.Provider() {

            @Override
            public FxBeanInfo getBeanInfo(String fqn) {
                return BeanModelBuilder.getBeanInfo(info, fqn);
            }
        });
        env.setCompilationInfo(info);
        env.setHierarchy(snapshot.getTokenHierarchy());
        env.setModel(model);
        env.setReporter(this);
        
        return env;
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        return result;
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }
    
}
