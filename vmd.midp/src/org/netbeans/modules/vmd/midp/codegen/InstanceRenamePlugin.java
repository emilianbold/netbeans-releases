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

package org.netbeans.modules.vmd.midp.codegen;

import com.sun.source.tree.Tree.Kind;
import java.util.Collection;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author ads
 */
public class InstanceRenamePlugin implements RefactoringPlugin{

    InstanceRenamePlugin(InstaceRenameRefactoring refactoring) {
        myRefactoring = refactoring;
        refactoring.setPlugin( this );
        Lookup lookup = refactoring.getRefactoringSource();
        Collection<? extends TreePathHandle> collection = lookup.lookupAll(
                TreePathHandle.class );
        Lookup methodLookup =null;
        Lookup fieldLookup =null;
        for (TreePathHandle treePathHandle : collection) {
            if ( treePathHandle.getKind() == Kind.METHOD ){
                methodLookup = Lookups.singleton( treePathHandle );
            }
            else if ( treePathHandle.getKind() == Kind.VARIABLE ){
                fieldLookup = Lookups.singleton( treePathHandle );
            }
        }
        if ( methodLookup != null ){
            myMethodRenameRefactoring = new RenameRefactoring( methodLookup );
            myMethodRenameRefactoring.getContext().add( InstaceRenameRefactoring.FLAG );
        }
        if ( fieldLookup != null ){
            myFieldRenameRefactoring = new RenameRefactoring( fieldLookup );
            myFieldRenameRefactoring.getContext().add( InstaceRenameRefactoring.FLAG );
        }
    }

    public Problem preCheck() {
        Problem problem =  myFieldRenameRefactoring.preCheck();
        Problem methodProblem = null;
        if ( myMethodRenameRefactoring == null ){
            return problem;
        }
        if ( problem == null ){
            problem = myMethodRenameRefactoring.preCheck();
        }
        else if ( ( methodProblem=myMethodRenameRefactoring.preCheck() )!= null ){
            Problem last = problem;
            while( last.getNext() != null ){
                last=last.getNext();
            }
            last.setNext( methodProblem );
        }
        return problem;
    }

    public Problem checkParameters() {
        Problem problem =  myFieldRenameRefactoring.checkParameters();
        Problem methodProblem = null;
        if ( myMethodRenameRefactoring == null ){
            return problem;
        }
        if ( problem == null ){
            problem = myMethodRenameRefactoring.checkParameters();
        }
        else if ( (methodProblem= myMethodRenameRefactoring.checkParameters())
                != null )
        {
            Problem last = problem;
            while( last.getNext() != null ){
                last=last.getNext();
            }
            last.setNext( methodProblem );
        }
        return problem;
    }

    public Problem fastCheckParameters() {
        Problem problem =  myFieldRenameRefactoring.fastCheckParameters();
        Problem methodProblem = null;
        if ( myMethodRenameRefactoring == null ){
            return problem;
        }
        if ( problem == null ){
            problem = myMethodRenameRefactoring.fastCheckParameters();
        }
        else if ( ( methodProblem = myMethodRenameRefactoring.fastCheckParameters())
                != null )
        {
            Problem last = problem;
            while( last.getNext() != null ){
                last=last.getNext();
            }
            last.setNext( methodProblem );
        }
        return problem;
    }

    public void cancelRequest() {
        myFieldRenameRefactoring.cancelRequest();
        if ( myMethodRenameRefactoring != null ){
            myMethodRenameRefactoring.cancelRequest();
        }
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {
        RefactoringSession session = refactoringElements.getSession();
        if ( myRefactoring.getListener() != null ){
            session.addProgressListener( myRefactoring.getListener() );
        }
        Problem problem = myFieldRenameRefactoring.prepare(session);

        if ( myMethodRenameRefactoring != null ){
            Problem methodProblem = myMethodRenameRefactoring.prepare(session);
            if ( problem == null ){
                problem = methodProblem;
            }
            else if ( methodProblem!= null ) {
                Problem last = problem;
                while( last.getNext() != null ){
                    last=last.getNext();
                }
                last.setNext( methodProblem );
            }
        }

        myRefactoring.setResult( problem );
        
        return problem;
    }

    void setNewFieldName(String name) {
        myFieldRenameRefactoring.setNewName(name);
    }

    void setNewGetterName(String name) {
        if ( myMethodRenameRefactoring != null ){
            myMethodRenameRefactoring.setNewName(name);
        }
    }

    private InstaceRenameRefactoring myRefactoring;
    private RenameRefactoring myFieldRenameRefactoring;
    private RenameRefactoring myMethodRenameRefactoring;


}
