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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Referenceable;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.03.16
 */
final class Finder extends Plugin {
    
  Finder(WhereUsedQuery query) {
    myQuery = query;
  }

  public Problem prepare(RefactoringElementsBag refactoringElements) {
    Referenceable reference =
      myQuery.getRefactoringSource().lookup(Referenceable.class);

    if (reference == null) {
      return null;
    }
    Component component = myQuery.getContext().lookup(Component.class);
    Set<Component> roots = new HashSet<Component>();

    if (component == null) {
      roots = getRoots(reference);
    }
    else {
      roots.add(component);
    }
    List<Element> elements = null;

    for (Component root : roots) {
      elements = find(reference, root);
  
      if (elements != null) {
        for (Element element : elements) {
          refactoringElements.add(myQuery, element);
        }
      }
    }
    return null;
  }

  public Problem fastCheckParameters() {
    return null;
  }

  public Problem checkParameters() {
    return null;
  }

  public void doRefactoring(List<RefactoringElementImplementation> elements)
    throws IOException
  {}

  private WhereUsedQuery myQuery;
}
