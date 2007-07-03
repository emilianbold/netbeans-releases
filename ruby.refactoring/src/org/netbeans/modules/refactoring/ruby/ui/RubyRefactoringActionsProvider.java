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

package org.netbeans.modules.refactoring.ruby.ui;

import org.netbeans.modules.refactoring.ruby.spi.ui.RubyActionsImplementationProvider;

/**
 *
 * @author Jan Becicka
 */
public class RubyRefactoringActionsProvider extends RubyActionsImplementationProvider{
    
    public RubyRefactoringActionsProvider() {
    }
//    @Override
//    public void doExtractInterface(final Lookup lookup) {
//        Runnable task;
//        EditorCookie ec = lookup.lookup(EditorCookie.class);
//        if (RefactoringActionsProvider.isFromEditor(ec)) {
//            task = new RefactoringActionsProvider.TextComponentTask(ec) {
//                @Override
//                protected RefactoringUI createRefactoringUI(RubyElementCtx selectedElement,int startOffset,int endOffset, CompilationInfo info) {
//                    return new ExtractInterfaceRefactoringUI(selectedElement, info);
//                }
//            };
//        } else {
//            task = new NodeToFileObjectTask(lookup.lookupAll(Node.class)) {
//                @Override
//                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<RubyElementCtx> handles) {
//                    RubyElementCtx tph = handles.iterator().next();
//                    return new ExtractInterfaceRefactoringUI(tph, cinfo.get());
//                }
//            };
//        }
//        task.run();
//    }
//
//    @Override
//    public boolean canExtractInterface(Lookup lookup) {
//        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
//        if (nodes.size() != 1) {
//            return false;
//        }
//        Node n = nodes.iterator().next();
//        DataObject dob = n.getCookie(DataObject.class);
//        if (dob==null) {
//            return false;
//        }
//        FileObject fo = dob.getPrimaryFile();
//        if (RetoucheUtils.isRefactorable(fo)) { //NOI18N
//            return true;
//        }
//        return false;
//    }
}
