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

package org.netbeans.modules.uml.integration.ide.events;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.integration.ide.ChangeUtils;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;
import org.netbeans.modules.uml.integration.ide.UMLSupport;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import java.util.Iterator;

public class LiteralTransaction {
    
  private IEnumerationLiteral mLiteral = null;

  /** The system that is to be updated. */
  private IProject    mProj      = null;

  /** Flag to indicate if this is an implementation attribute. */
  private boolean      mIsImplLiteral = false;

  private IClassifier mSym = null;

  public LiteralTransaction(IProject proj)
  {
    setLiteral(null);
    setProject(proj);
  }

  /**
   * Create a new MemberTransaction and specify the symbol to search and
   * data member to find.  If the member will be created if one is needed.
   * @param trans The symbol transaction used when searching for the memeber.
   * @param member The information required to locate the data member.
   */
  public LiteralTransaction(SymbolTransaction trans, final LiteralInfo lit)
  {
    this(UMLSupport.getCurrentProject());
    if(lit != null)
    {
        if(trans.getSymbol() != null) {
            setSymbol(trans.getSymbol());
        }
        setLiteral(trans, lit);
    }
  }

  protected void setProject(IProject proj)
  {
    mProj = proj;
  }

  public IProject getProject()
  {
    return mProj;
  }

  public IEnumerationLiteral getLiteral()
  {
    return mLiteral;
  }

  public void setLiteral(IEnumerationLiteral lit)
  {
    mLiteral = lit;
  }

  public boolean isImplLiteral()
  {
    return mIsImplLiteral;
  }

  public void setIsImplLiteral(boolean value)
  {
    mIsImplLiteral = value;
  }

  public void setLiteral(SymbolTransaction trans, final LiteralInfo lit)
  {
    // First set the symbol to null to allow the current symbol to be GC
    setLiteral(null);
    IEnumeration sym = (IEnumeration) trans.getSymbol();

    if(sym != null)
    {
      mLiteral = findLiteral(sym, lit.getName());

      if(mLiteral == null && lit.getChangeType() == ElementInfo.CREATE)
        mLiteral = createLiteral(sym, lit.getName());
    }
  }

  public void setSymbol(IClassifier clazz) {
    mSym = clazz;
  }

  public IClassifier getSymbol() {
    return mSym;
  }
  
  protected IEnumerationLiteral createLiteral(IEnumeration sym, String name) {
    IEnumerationLiteral retVal = null;
    EventManager.getEventManager().getEventFilter().blockEventType(ChangeUtils.RDT_DEPENDENCY_ADDED);
    try
    {
        if (findLiteral(sym, name) == null) {
            sym.addLiteral(sym.createLiteral(name));
        }
    }
    catch(Exception e)
    {
        Log.stackTrace(e);
    } finally {
        EventManager.getEventManager().getEventFilter().unblockEventType(ChangeUtils.RDT_DEPENDENCY_ADDED);   
    }
    return retVal;
  }

  private IEnumerationLiteral findLiteral(IEnumeration sym, String name) {
      for (Iterator iter = sym.getLiterals().iterator(); iter.hasNext(); ) {
          IEnumerationLiteral lit = (IEnumerationLiteral) iter.next();
          if (name.equals(lit.getName())) {
              return lit;
          }
      }
      return null;
  }
  
}
