/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.parsing.api.MultiLanguageUserTask;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.source.JavaParserResultTask;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
//@NotThreadSafe
class ClassParser extends Parser {
    
    private static final Logger LOGGER = Logger.getLogger(Parser.class.getName());
    
    private final ClasspathInfo info;
    private CompilationInfoImpl ciImpl;
    private final ChangeSupport changeSupport;
    private final ClasspathInfoListener cpInfoListener;

    public ClassParser(ClasspathInfo info) {
        assert info != null;
        this.info = info;
        this.changeSupport = new ChangeSupport(this);
        this.cpInfoListener = new ClasspathInfoListener(this.changeSupport);
        info.addChangeListener(WeakListeners.change(this.cpInfoListener, info));
    }

    @Override
    public void parse(final Snapshot snapshot, Task task, final SchedulerEvent event) throws ParseException {
        assert snapshot != null;
        final Source source = snapshot.getSource();
        assert source != null;
        final FileObject file = source.getFileObject();
        assert file != null;
        final ClassPath bootPath = info.getClassPath(ClasspathInfo.PathKind.BOOT);
        assert bootPath != null;
        ClassPath compilePath = info.getClassPath(ClasspathInfo.PathKind.COMPILE);
        if (compilePath == null) {
            compilePath = ClassPathSupport.createClassPath(new URL[0]);
        }
        ClassPath srcPath = info.getClassPath(ClasspathInfo.PathKind.SOURCE);
        if (srcPath == null) {
            srcPath = ClassPathSupport.createClassPath(new URL[0]);
        }
        final FileObject root = ClassPathSupport.createProxyClassPath(bootPath,compilePath,srcPath).findOwnerRoot(file);
        try {
            this.ciImpl = new CompilationInfoImpl(info,file,root);
        } catch (final IOException ioe) {
            throw new ParseException ("ClassParser failure", ioe);            //NOI18N
        }
    }

    @Override
    public Result getResult(Task task, SchedulerEvent event) throws ParseException {
        assert ciImpl != null;
        final boolean isParserResultTask = task instanceof ParserResultTask;
        final boolean isJavaParserResultTask = task instanceof JavaParserResultTask;
        final boolean isUserTask = task instanceof MultiLanguageUserTask || task instanceof UserTask;
        JavacParserResult result = null;
        if (isParserResultTask) {
            final JavaSource.Phase currentPhase = ciImpl.getPhase();
            JavaSource.Phase requiredPhase;
            if (isJavaParserResultTask) {
                requiredPhase = ((JavaParserResultTask)task).getPhase();
            }
            else {
                requiredPhase = JavaSource.Phase.RESOLVED;
            }
            if (currentPhase.compareTo(requiredPhase)<0) {
                ciImpl.setPhase(requiredPhase);
            }
            result = new JavacParserResult(JavaSourceAccessor.getINSTANCE().createCompilationInfo(ciImpl));
        }
        else if (isUserTask) {
            result = new JavacParserResult(JavaSourceAccessor.getINSTANCE().createCompilationController(ciImpl));
        }
        else {
            LOGGER.warning("Ignoring unknown task: " + task);                   //NOI18N
        }
        return result;
    }

    @Override
    public void cancel() {
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        Parameters.notNull("changeListener", changeListener);   //NOI18N
        this.changeSupport.addChangeListener(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        Parameters.notNull("changeListener", changeListener);   //NOI18N
        this.changeSupport.removeChangeListener(changeListener);
    }

}
