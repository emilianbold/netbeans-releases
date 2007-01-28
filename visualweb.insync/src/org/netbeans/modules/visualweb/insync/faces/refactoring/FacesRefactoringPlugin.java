/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.lang.reflect.Field;

import org.netbeans.modules.javacore.internalapi.ProgressEvent;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveClassRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

//!EAT TODO Must verify support for inner classes in refactoring !!!  At the moment they are not supported.

public abstract class FacesRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin {

    protected volatile boolean cancelRequest = false;

    protected static final Problem createProblem(Problem result, boolean isFatal, String message) {
        Problem problem = new Problem(isFatal, message);
        if (result == null) {
            return problem;
        } else if (isFatal) {
            problem.setNext(result);
            return problem;
        } else {
            //problem.setNext(result.getNext());
            //result.setNext(problem);

            // [TODO] performance
            Problem p = result;
            while (p.getNext() != null)
                p = p.getNext();
            p.setNext(problem);
            return result;
        }
    }
    protected static final String getRefactoringString(String key) {
        return NbBundle.getMessage(RenameRefactoring.class, key);
    }

    protected static final String getString(String key) {
        return NbBundle.getMessage(FacesRefactoringPlugin.class, key);
    }

    public FacesRefactoringPlugin() {
    }

    public void cancelRequest() {
        cancelRequest = true;
    }

    public Problem checkParameters() {
        return null;
    }

    /**
     * Quick checking being done on typing within rename panel
     */
    public abstract Problem fastCheckParameters();

    public Problem preCheck() {
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 1);
        fireProgressListenerStop();
        return null;
    }

    public abstract Problem prepare(RefactoringElementsBag refactoringElements);

    public void start(ProgressEvent event) {
        fireProgressListenerStart(event.getOperationType(), event.getCount());
    }

    public void step(ProgressEvent event) {
        fireProgressListenerStep();
    }

    public void stop(ProgressEvent event) {
        fireProgressListenerStop();
    }

    protected static boolean isPackageRename(AbstractRefactoring refactoring) {
        if (refactoring instanceof MoveClassRefactoring) {
            MoveClassRefactoring moveRefactoring = (MoveClassRefactoring) refactoring;
            // XXX NB changed, revise!
//          return (moveRefactoring.getPackageName() == null && moveRefactoring.getTargetPackageName() != null);
			// The above code was commented out and replaced with the following code. However
			// the following check is not sufficient to determine if refactoring is
			// a package rename.
			// return (moveRefactoring.getTargetPackageName() != null);
			// HACK The Netbean API does not have a way to determine if this is a
			// package rename. We have to use reflection.
          try {
              Field field = moveRefactoring.getClass().getDeclaredField("isPackageRename"); // NOI18N
              field.setAccessible(true);
              return field.getBoolean(moveRefactoring);
          } catch (SecurityException e) {
              ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
          } catch (NoSuchFieldException e) {
              ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
          } catch (IllegalArgumentException e) {
              ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
          } catch (IllegalAccessException e) {
              ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
          }
        }
        return false;
    }
}
