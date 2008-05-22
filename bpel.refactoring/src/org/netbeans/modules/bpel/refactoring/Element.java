/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.bpel.refactoring;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.text.Position.Bias;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionRef;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess;

import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import static org.netbeans.modules.xml.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.03.16
 */
final class Element extends SimpleRefactoringElementImplementation implements TreeElement {

  Element(Component component) {
    myComponent = component;
  }

  public Lookup getLookup() {
    return Lookups.fixed(myComponent, new GotoSource(), new GotoDiagram());
  }

  public FileObject getParentFile() {
    return (FileObject) myComponent.getModel().getModelSource().getLookup().lookup(FileObject.class);
  }

  public TreeElement getParent(boolean isLogical) {
    if (myComponent.getParent() != null) {
      return TreeElementFactory.getTreeElement(myComponent.getParent());
    }
    return TreeElementFactory.getTreeElement(getParentFile());
  }

  public String getText() {
    return EditorUtil.getName(myComponent);
  }

  public String getText(boolean isLogical) {
    return getText();
  }

  public String getDisplayText() {
    return EditorUtil.getHtmlName(myComponent);
  }

  public Icon getIcon() {
    return EditorUtil.getIcon(myComponent);
  }

  public PositionBounds getPosition() {
    if ( !(myComponent.getModel() instanceof AbstractDocumentModel)) {
      return null;
    }
    DocumentModelAccess access = ((AbstractDocumentModel) myComponent.getModel()).getAccess();
    String text = access.getXmlFragmentInclusive(((DocumentComponent) myComponent).getPeer());
    
    int startPos = ((DocumentComponent) myComponent).findPosition();
    int endPos = startPos + text.length();
    DataObject data = null;
  
    try {
      data = DataObject.find((FileObject) myComponent.getModel().getModelSource().getLookup().lookup(FileObject.class));
    }
    catch (DataObjectNotFoundException e) {
      return null;
    }
    CloneableEditorSupport editor = SharedUtils.findCloneableEditorSupport(data);

    if (editor == null) {
      return null;
    }
    PositionRef start = editor.createPositionRef(startPos, Bias.Forward);
    PositionRef end = editor.createPositionRef(endPos, Bias.Forward);

    return new PositionBounds(start, end);
  }
       
  @Override
  public void openInEditor() {
    EditorUtil.goToSource(myComponent);
  }

  public Object getUserObject() {
    return myComponent;
  }
  
  void setTransactionObject(XMLRefactoringTransaction transaction) {
    myTransaction = transaction;
  }

  @Override
  protected String getNewFileContent() {
    if (myComponent.getModel() instanceof AbstractDocumentModel && myTransaction != null) {
      return myTransaction.refactorForPreview(myComponent.getModel());
    }
    return null;
  }

  public void performChange() {}

  private class GotoSource extends AbstractAction {
    public GotoSource() {
      super(i18n(Element.class, "LBL_Go_to_Source")); // NOI18N
    }

    public void actionPerformed(ActionEvent event) {
      openInEditor();
    }
  }

  private class GotoDiagram extends AbstractAction {
    public GotoDiagram() {
      super(i18n(Element.class, "LBL_Go_to_Diagram")); // NOI18N
    }

    public void actionPerformed(ActionEvent event) {
      EditorUtil.goToDesign(myComponent);
    }

    @Override
    public boolean isEnabled() {
      return myComponent instanceof BpelEntity;
    }
  }

  private Component myComponent;
  private XMLRefactoringTransaction myTransaction;
}
