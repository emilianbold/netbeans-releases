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
package org.netbeans.modules.bpel.refactoring;

import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.03.16
 */
public final class Factory implements RefactoringPluginFactory {
   
  /**{@inheritDoc}*/
  public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
    Referenceable referenceable =
      refactoring.getRefactoringSource().lookup(Referenceable.class);
  
    if (referenceable == null) {
      // this is not my object, don't participate in refactoring
      return null;
    }
    if (refactoring instanceof WhereUsedQuery) {
      return new Finder((WhereUsedQuery) refactoring);
    } 
    if (refactoring instanceof RenameRefactoring) {
      return new Renamer((RenameRefactoring) refactoring);
    }
    if (refactoring instanceof SafeDeleteRefactoring) {
      // do nothing
      return null;
    }
    if (refactoring instanceof MoveRefactoring) {
      return new Mover((MoveRefactoring) refactoring);
    }
    return null;
  }
}
