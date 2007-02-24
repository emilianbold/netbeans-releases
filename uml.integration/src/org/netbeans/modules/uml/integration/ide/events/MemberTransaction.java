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

import org.netbeans.modules.uml.integration.ide.ChangeUtils;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;
import org.netbeans.modules.uml.integration.ide.UMLSupport;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

/**
 * The MemberTransaction is use to maintain a context when updating data
 * members.  The MemberTransaction will locate the data member in Describes
 * database.  When locating the data member the class symbol specified by
 * the SymbolTransaction will be searched for any attributes that match
 * the requested attribute.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-06-19  Darshan     Fixed fullScopeName not using the fully scoped
 *                              name in setAttribute().
 *   2  2002-06-21  Darshan     Added support for IStructuralFeature instead of
 *                              IAttribute and extended createAttribute to
 *                              create a navigable association where required.
 */
public class MemberTransaction
{
  /**
   *  The attribute that this context is managing. Note that this will be an
   * instance of INavigableEnd or IAttribute and can be downcast without
   * creating a proxy.
   */
  private IStructuralFeature mAttribute  = null;

  /**
   * If this attribute is an implmentation attribute the mMemberRel will contain
   * the relationship that contains teh attribute.
   */
  // private IGDRelation  mMemberRel  = null;

  /** The system that is to be updated. */
  private IProject    mProj      = null;

  /** Flag to indicate if this is an implementation attribute. */
  private boolean      mIsImplAttr = false;

  private IClassifier mSym = null;
  /**
   * Creates new MemberTransaction and specify which system to search for
   * the attribute.
   */
  public MemberTransaction(IProject proj)
  {
    setAttribute(null);
    setProject(proj);
  }

  /**
   * Create a new MemberTransaction and specify the symbol to search and
   * data member to find.  If the member will be created if one is needed.
   * @param trans The symbol transaction used when searching for the memeber.
   * @param member The information required to locate the data member.
   */
  public MemberTransaction(SymbolTransaction trans, final MemberInfo member)
  {
    this(UMLSupport.getCurrentProject());
    if(member != null)
    {
      if(trans.getSymbol() != null)
        setSymbol(trans.getSymbol());
      setAttribute(trans, member);
    }
  }

  /**
   * Sets the Describe system to use when retrieving the symbols information.  Therefore,
   * the symbol will also reside in the Describe system.
   * @param value The Describe system.
   */
  protected void setProject(IProject proj)
  {
    mProj = proj;
  }

  /**
   * Retrieves the Describe system to use when retrieving the symbols information.  Therefore,
   * the symbol will also reside in the Describe system.
   * @param value The Describe system.
   */
  public IProject getProject()
  {
    return mProj;
  }

  /**
   * Retrieve the Describe representation of the data member.
   * <i>In the future this may be abstracted. </i>
   */
  public IStructuralFeature getAttribute()
  {
    return mAttribute;
  }

  /**
   * Set the Describe representation of the data member.
   * <i>In the future this may be abstracted. </i>
   */
  public void setAttribute(IStructuralFeature attr)
  {
    mAttribute = attr;
  }

  /**
   * Checks if the data memeber is an implementation attribute.
   * @return true if the an implementation attribute, false otherwise.
   */
  public boolean isImplAttribute()
  {
    return mIsImplAttr;
  }

  /**
   * Sets if the data memeber is an implementation attribute.
   * @param value true if the an implementation attribute, false otherwise.
   */
  public void setIsImplAttribute(boolean value)
  {
    mIsImplAttr = value;
  }

  /**
   * Sets the Describe attribute that defines data member.  A Describe symbol
   * is search for a data member that matches the requested data member name.  If
   * a Describe attribute is not found then one is created for the data member.
   * @param trans The symbol transaction used to locate the data memeber.
   * @param member The information needed to locate the data memeber.
   */
  public void setAttribute(SymbolTransaction trans, final MemberInfo member)
  {
    // First set the symbol to null to allow the current symbol to be GC
    setAttribute(null);
    IClassifier sym = trans.getSymbol();

    if(sym != null)
    {
      String fullScopeName = member.getQualifiedType();
      String sourceName = member.getType();
      Log.out("the fullScopeName = " + fullScopeName);

      // if(member.getChangeType() != ElementInfo.CREATE)
      mAttribute = JavaClassUtils.findAttribute(sym, member.getName());

      if(mAttribute == null && member.getChangeType() == ElementInfo.CREATE)
        mAttribute = createAttribute(sym, member.getName(), fullScopeName, sourceName,member.getmodifierMask());
    }
  }

  public void setSymbol(IClassifier clazz) {
    mSym = clazz;
  }

  public IClassifier getSymbol() {
    return mSym;
  }
  /**
   * Create a new Describe attribute to represent the data member.  This is helper
   * routine.
   * @param sym The symbol that will contain the attribute.
   * @param name The name of the attribute.
   * @param The type of the data member.
   */
  protected IStructuralFeature createAttribute(IClassifier sym, String name,
                                               String fullName, String sourceName,int modifierMask)
  {
    IAttribute retVal = null;
    EventManager.getEventManager().getEventFilter().blockEventType(
            ChangeUtils.RDT_DEPENDENCY_ADDED);
    try
    {
        int mul = MemberInfo.getMultiplicity(sourceName);
        sourceName = MemberInfo.getTypeName(sourceName);
        fullName   = MemberInfo.getTypeName(fullName);

        if (JavaClassUtils.findAttribute(sym, name) == null) {
            IAttributeChangeFacility facility =
                                     EventManager.getAttributeFacility();
            if (facility != null) {
                Log.out("createAttribute: Creating attribute : " + name +
                        ": " + sourceName + " : " + fullName);
                //retVal = facility.createAttribute(name, sourceName, sym);
                String umlFullName = JavaClassUtils.convertJavaToUML(fullName);
               retVal = facility.addAttribute3(name, umlFullName, sym, true, false,modifierMask);
                if (retVal != null)
                    MemberInfo.setMultiplicity(retVal, mul, 0);
                else
                    Log.err("createAttribute(): Couldn't create attribute");

                if (retVal != null &&
                            retVal instanceof INavigableEnd) {
                    IDEProcessor.addRelationshipLinkToDiagram(retVal, null);
                }
            } else {
                Log.err("createAttribute(): IAttributeChangeFacility is null");
            }
//            IClassifier clazz = JavaClassUtils.findClassSymbol(fullName);
//
//            if (clazz == null && !JavaClassUtils.isPrimitive(fullName))
//                clazz = JavaClassUtils.createDataType(fullName);
//
//            if (clazz == null) {
//                retVal = sym.createAttribute(sourceName, name);
//                sym.addAttribute(retVal);
//                sym.addOwnedElement(retVal);
//                Log.out("Setting multiplicity for '" + name
//                        + "' to " + mul);
//                MemberInfo.setMultiplicity(retVal, mul, 0);
//            }
//            else {
//                IStructuralFeature feat = IDEProcessor.
//                    makeNavigableAssociation(sym, clazz, name);
//                MemberInfo.setMultiplicity(feat, mul, 0);
//                return feat;
//            }
            Log.out("Successfully added the attrib ....... " + name);
        }
        else {
            Log.out("Attrib with same name already exists .....");
        }
    }
    catch(Exception e)
    {
        Log.stackTrace(e);
    }finally {
        EventManager.getEventManager().getEventFilter().unblockEventType(
                ChangeUtils.RDT_DEPENDENCY_ADDED);   
    }
    return retVal;
  }

  /**
   * Checks if the type is a Java primitive.
   * @return true if the type is a Java primitive.
   */
  protected boolean isPrimative(String type)
  {
    boolean retVal = false;

    if((type.equals("int") == true) || (type.equals("short") == true) ||
       (type.equals("char") == true) || (type.equals("boolean") == true) ||
       (type.equals("double") == true) || (type.equals("float") == true) ||
       (type.equals("long") == true) || (type.equals("byte") == true))
    {
       retVal = true;
    }

    return retVal;
  }
}
