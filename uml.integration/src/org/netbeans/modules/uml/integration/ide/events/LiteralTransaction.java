/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
