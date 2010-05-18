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

import java.util.Collection;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.GuardedBlockHandler;
import org.netbeans.modules.refactoring.spi.GuardedBlockHandlerFactory;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;

/**
 *
 * @author ads
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.GuardedBlockHandlerFactory.class)
public class GuardedBlockHandlerFactoryImpl implements GuardedBlockHandlerFactory {

    
    public GuardedBlockHandler createInstance(AbstractRefactoring refactoring) {
        /*
         * It is unknown for me why custom refactoring InstaceRenameRefactoring
         * is not passed here as argument ( never I mean ).
         * Instead of InstaceRenameRefactoring refactoring engine
         * passes here delegate refactoring instances . In my case
         * this is two RenameRefactoring ( if accessor is also refactored ).
         * So I changed code to support RenameRefactoring with special Context.
         
         if ( refactoring instanceof InstaceRenameRefactoring ){
            return new GuardedBlockHandlerImpl(
                    (InstaceRenameRefactoring) refactoring );
        }*/

        if ( refactoring instanceof RenameRefactoring &&
                refactoring.getContext().lookup( 
                InstaceRenameRefactoring.RefactoringInfo.class )!= null )
        {
            return new GuardedBlockHandlerImpl( refactoring );
        }
        return null;
    }

    private static class GuardedBlockHandlerImpl implements GuardedBlockHandler {

        GuardedBlockHandlerImpl(AbstractRefactoring refactoring) {
            myRefactoring = refactoring;
        }

        public Problem handleChange(RefactoringElementImplementation proposedChange,
                Collection<RefactoringElementImplementation> replacements,
                Collection<Transaction> transaction)
        {
            FileObject changedFileObject = proposedChange.getParentFile();
            TreePathHandle handle = myRefactoring.getRefactoringSource().lookup(
                    TreePathHandle.class );
            boolean flag = ( handle!= null ) && handle.getFileObject()!=null
                    && handle.getFileObject().equals( changedFileObject );
            if ( flag )
            {
                //proposedChange.setStatus(RefactoringElementImplementation.NORMAL);
                //proposedChange.setEnabled( false );
                proposedChange =
                        new GuardedElementImplementation(proposedChange);
            }
            replacements.add(proposedChange);
            return null;
        }

        private AbstractRefactoring myRefactoring;
    }

    private static class GuardedElementImplementation
            extends SimpleRefactoringElementImplementation
    {

        GuardedElementImplementation(RefactoringElementImplementation delegate){
            myDelegate = delegate;
        }

        public String getText() {
            return myDelegate.getText();
        }

        public String getDisplayText() {
            return myDelegate.getDisplayText();
        }

        public void performChange() {
            // do nothing
        }

        public Lookup getLookup() {
            return myDelegate.getLookup();
        }

        public FileObject getParentFile() {
            return myDelegate.getParentFile();
        }

        public PositionBounds getPosition() {
            return myDelegate.getPosition();
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void undoChange() {
            // do nothing
        }

        private RefactoringElementImplementation myDelegate;
        
    }
}
