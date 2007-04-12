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

import javax.swing.Icon;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.xml.xam.Component;

import org.netbeans.modules.bpel.editors.api.utils.RefactorUtil;
import org.netbeans.modules.bpel.editors.api.utils.Util;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.03.16
 */
final class Element
  extends SimpleRefactoringElementImplementation implements TreeElement
{
  Element(Component component) {
    myComponent = component;
  }

  public Lookup getLookup() {
     return Lookups.singleton(myComponent);
  }

  public FileObject getParentFile() {
    return (FileObject) myComponent.getModel().
      getModelSource().getLookup().lookup(FileObject.class);
  }

  public TreeElement getParent(boolean isLogical) {
    if (myComponent.getParent() != null) {
      return TreeElementFactory.getTreeElement(myComponent.getParent());
    }
    return TreeElementFactory.getTreeElement(getParentFile());
  }

  public String getText() {
    return RefactorUtil.getName(myComponent);
  }

  public String getText(boolean isLogical) {
    return getText();
  }

  public String getDisplayText() {
    return RefactorUtil.getHtmlName(myComponent);
  }

  public Icon getIcon() {
    return RefactorUtil.getIcon(myComponent);
  }

  public PositionBounds getPosition() {
    return null;
  }
       
  public void openInEditor() {
    Util.goToSource(myComponent);
  }

  public Object getUserObject() {
    return myComponent;
  }

  public void showPreview() {
     UI.setComponentForRefactoringPreview(null);
  }
  public void performChange() {}

  private Component myComponent;
}
