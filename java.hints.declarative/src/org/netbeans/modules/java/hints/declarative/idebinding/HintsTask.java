/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.declarative.idebinding;

import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser.HintTextDescription;
import org.netbeans.modules.java.hints.jackpot.impl.Utilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class HintsTask extends ParserResultTask<Result> {

    @Override
    public void run(final Result result, SchedulerEvent event) {
        final DeclarativeHintsParser.Result res = ParserImpl.getResult(result);
        final List<ErrorDescription> errors = new LinkedList<ErrorDescription>();

        if (res != null) {
            errors.addAll(res.errors);
            
            ClasspathInfo cpInfo = ClasspathInfo.create(result.getSnapshot().getSource().getFileObject());
            try {
                JavaSource.create(cpInfo).runUserActionTask(new Task<CompilationController>() {
                    public void run(CompilationController parameter) throws Exception {
                        for (HintTextDescription hd : res.hints) {
                            String code = result.getSnapshot().getText().subSequence(hd.textStart, hd.textEnd).toString();
                            Collection<Diagnostic<? extends JavaFileObject>> parsedErrors = new LinkedList<Diagnostic<? extends JavaFileObject>>();
                            Tree parsed = Utilities.parseAndAttribute(parameter, code, null, parsedErrors);

                            for (Diagnostic<? extends JavaFileObject> d : parsedErrors) {
                                errors.add(ErrorDescriptionFactory.createErrorDescription(Severity.ERROR/*XXX*/, d.getMessage(null), result.getSnapshot().getSource().getFileObject(), (int) (hd.textStart + d.getStartPosition()), (int) (hd.textStart + d.getEndPosition())));
                            }
                        }
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        HintsController.setErrors(result.getSnapshot().getSource().getFileObject(),
                                  HintsTask.class.getName(),
                                  errors);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
    }

    public static final class FactoryImpl extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singleton(new HintsTask());
        }
        
    }

}
