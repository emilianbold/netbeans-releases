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
package org.netbeans.modules.xslt.validation.reference;

import org.netbeans.modules.xslt.model.CallTemplate;
import org.netbeans.modules.xslt.model.Instruction;
import org.netbeans.modules.xslt.model.TypeSpec;
import org.netbeans.modules.xslt.model.QualifiedNameable;

import org.netbeans.modules.xslt.model.UseAttributesSetsSpec;
import org.netbeans.modules.xslt.model.UseCharacterMapsSpec;
import org.netbeans.modules.xslt.model.WithParam;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.validation.core.XsltValidator;
import static org.netbeans.modules.xml.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.03
 */
public final class Validator extends XsltValidator {

  @Override
  protected final void visit(XslComponent component)
  {
//out("see: " + component.getClass().getName() + " " + (component instanceof QualifiedNameable));
    checkCallTemplate(component);
    checkInstruction(component);
    checkTypeSpec(component);
    checkUseAttributesSetsSpec(component);
    checkUseCharacterMapsSpec(component);
    checkWithParam(component);
  }

  private void checkCallTemplate(XslComponent component) {
    if ( !(component instanceof CallTemplate)) {
      return;
    }
    CallTemplate callTemplate = (CallTemplate) component;
out("see templat 1: " + component.getClass().getName());
  }
  
  private void checkInstruction(XslComponent component) {
    if ( !(component instanceof Instruction)) {
      return;
    }
    Instruction instruction = (Instruction) component;
out("see instruc 2: " + component.getClass().getName());
  }
  
  private void checkTypeSpec(XslComponent component) {
    if ( !(component instanceof TypeSpec)) {
      return;
    }
    TypeSpec typeSpec = (TypeSpec) component;
out("see    type 3: " + component.getClass().getName());
  }

  private void checkUseAttributesSetsSpec(XslComponent component) {
    if ( !(component instanceof UseAttributesSetsSpec)) {
      return;
    }
    UseAttributesSetsSpec useAttributesSetsSpec = (UseAttributesSetsSpec) component;
out("see use set 4: " + component.getClass().getName());
  }

  private void checkUseCharacterMapsSpec(XslComponent component) {
    if ( !(component instanceof UseCharacterMapsSpec)) {
      return;
    }
    UseCharacterMapsSpec useCharacterMapsSpec = (UseCharacterMapsSpec) component;
out("see use map 5: " + component.getClass().getName());
  }

  private void checkWithParam(XslComponent component) {
    if ( !(component instanceof WithParam)) {
      return;
    }
    WithParam withParam = (WithParam) component;
out("see   param 6: " + component.getClass().getName());
  }

//todo a  addQuickFix("FIX_Reference", entity, tag, attr, QuickFix.get(entity, (Reference<Referenceable>) reference));
}
