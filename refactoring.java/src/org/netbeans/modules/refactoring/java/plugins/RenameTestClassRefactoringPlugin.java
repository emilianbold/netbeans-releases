/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.plugins;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.ui.JavaRenameProperties;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.spi.gototest.TestLocator;
import org.netbeans.spi.gototest.TestLocator.LocationResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ralph Ruijs
 */
public class RenameTestClassRefactoringPlugin extends JavaRefactoringPlugin {

    private RenameRefactoring refactoring;
    private TreePathHandle treePathHandle;
    private RenameRefactoring[] renameDelegates;

    /** Creates a new instance of RenamePropertyRefactoringPlugin */
    public RenameTestClassRefactoringPlugin(RenameRefactoring rename) {
        this.refactoring = rename;
        treePathHandle = rename.getRefactoringSource().lookup(TreePathHandle.class);
    }

    @Override
    protected JavaSource getJavaSource(Phase phase) {
        return JavaSource.forFileObject(treePathHandle.getFileObject());
    }

    @Override
    public Problem checkParameters() {
        if (!isRenameTestClass()) {
            return null;
        }

        initDelegates();

        Problem p = null;
        for (RenameRefactoring delegate : renameDelegates) {
            p = chainProblems(p, delegate.checkParameters());
            if (p != null && p.isFatal()) {
                return p;
            }
        }
        return chainProblems(p, super.checkParameters());
    }

    @Override
    public Problem fastCheckParameters() {
        if (!isRenameTestClass()) {
            return null;
        }
        initDelegates();

        Problem p = null;
        for (RenameRefactoring delegate : renameDelegates) {
            FileObject delegateFile = delegate.getRefactoringSource().lookup(FileObject.class);
            delegate.setNewName(newName(treePathHandle.getFileObject(), delegateFile, refactoring.getNewName()));
            p = chainProblems(p, delegate.fastCheckParameters());
            if (p != null && p.isFatal()) {
                return p;
            }
        }
        return chainProblems(p, super.fastCheckParameters());
    }

    @Override
    protected Problem preCheck(CompilationController javac) throws IOException {
        if (!isRenameTestClass()) {
            return null;
        }
        initDelegates();
        Problem p = null;
        for (RenameRefactoring delegate : renameDelegates) {
            p = chainProblems(p, delegate.preCheck());
            if (p != null && p.isFatal()) {
                return p;
            }
        }
        return chainProblems(p, super.preCheck(javac));
    }

    @Override
    public Problem prepare(RefactoringElementsBag reb) {
        if (!isRenameTestClass()) {
            return null;
        }
        initDelegates();
        fireProgressListenerStart(ProgressEvent.START, renameDelegates.length);
        Problem p = null;
        for (RenameRefactoring delegate : renameDelegates) {
            p = chainProblems(p, delegate.prepare(reb.getSession()));
            if (p != null && p.isFatal()) {
                return p;
            }
            fireProgressListenerStep();
        }
        fireProgressListenerStop();
        return p;
    }

    private boolean isRenameTestClass() {
        JavaRenameProperties renameProps = refactoring.getContext().lookup(JavaRenameProperties.class);
        if (renameProps != null && renameProps.isIsRenameTestClass()) {
            return true;
        }
        return false;
    }

    private static Problem chainProblems(Problem p, Problem p1) {
        Problem problem;

        if (p == null) {
            return p1;
        }
        if (p1 == null) {
            return p;
        }
        problem = p;
        while (problem.getNext() != null) {
            problem = problem.getNext();
        }
        problem.setNext(p1);
        return p;
    }
    private boolean inited = false;

    private void initDelegates() {
        if (inited) {
            return;
        }
        
        final LinkedList<RenameRefactoring> renameRefactoringsList = new LinkedList<RenameRefactoring>();

        if(RetoucheUtils.getElementKind(treePathHandle) == ElementKind.CLASS) {
            final FileObject fileObject = treePathHandle.getFileObject();
            Collection<? extends TestLocator> testLocators = Lookup.getDefault().lookupAll(TestLocator.class);
            for (final TestLocator testLocator : testLocators) {
                if(testLocator.appliesTo(fileObject)) {
                    if(testLocator.asynchronous()) {
                        final Lock lock = new ReentrantLock();
                        final Condition condition = lock.newCondition();
                        lock.lock();
                        try {
                            testLocator.findOpposite(fileObject, -1, new TestLocator.LocationListener() {

                                @Override
                                public void foundLocation(FileObject fo, LocationResult location) {
                                    lock.lock();
                                    try {
                                        addIfMatch(location, testLocator, fo, renameRefactoringsList);
                                        condition.signalAll();
                                    } finally {
                                        lock.unlock();
                                    }
                                }
                            });
                            try {
                                condition.awaitNanos(10000000000L);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(RenamePropertyRefactoringPlugin.class.getName())
                                        .fine("Finding test class took too long, or it was interupted"); //NOI18N
                            }
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        LocationResult location = testLocator.findOpposite(fileObject, -1);
                        addIfMatch(location, testLocator, fileObject, renameRefactoringsList);
                    }
                }
            }
        }
        
        renameDelegates = renameRefactoringsList.toArray(new RenameRefactoring[0]);
        inited = true;
    }
    
    private static String newName(FileObject testedFile, FileObject testFile, String newName) {
        String testedName = testedFile.getName();
        String testName = testFile.getName();
        
        return testName.replace(testedName, newName);
    }
    
    private void addIfMatch(LocationResult location, final TestLocator testLocator, final FileObject fileObject, final LinkedList<RenameRefactoring> renameRefactoringsList) {
        if(location.getFileObject() != null && testLocator.getFileType(location.getFileObject()).equals(TestLocator.FileType.TEST)) {
            RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(location.getFileObject()));
            renameRefactoring.setNewName(newName(fileObject, location.getFileObject(), refactoring.getNewName()));
            renameRefactoring.setSearchInComments(true);
            renameRefactoringsList.add(renameRefactoring);
        }
    }
}
