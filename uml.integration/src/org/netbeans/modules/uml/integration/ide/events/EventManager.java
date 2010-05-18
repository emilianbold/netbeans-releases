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

import java.lang.reflect.Modifier;

//import org.netbeans.modules.uml.integration.ide.UMLSupport;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;
import org.netbeans.modules.uml.integration.ide.UMLSupport;
//import org.netbeans.modules.uml.integration.ide.Log;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager;
import org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility;
import org.netbeans.modules.uml.core.roundtripframework.IMethodChangeFacility;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

/**
 * The EventManages manages the communication between an IDE integration and
 * Describe.  The event mangaer is a singleton.  Therefore, all communication
 * to and from Describe can only occur in one direction at a time.  If the
 * EventManager is handling a RoundTrip event any attemp to communciate with
 * Describe will be ignored.
 * <br>
 * In order to initiate communication with Describe a GDSymbolTransaction
 * must first be created.  Once a transaction has been initated communciation
 * may begin.  When Methods and Members are being changed GDMemberTransaction
 * and GDMethodTransactions must be created.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-05-27  Darshan     Removed call to set attribute native.
 *   2  2002-06-12  Darshan     Uncommented call to setIsAbstract().
 *
 * @see GDSymbolTransaction
 * @see GDMethodTransaction
 * @see GDMemberTransaction
 * @see #beginClassTransaction(ClassInfo info, IGDSystem system)
 * @see #beginMethodTransaction(GDSymbolTransaction trans, ConstructorInfo method)
 * @see #beginMemberTransaction(GDSymbolTransaction trans, MemberInfo member)
 */
public class EventManager
{

  private boolean                  mPreventEvents = false;

  //private _IGDAddInRoundTripEvents mRTEvents = new GDProRTEvents();
  //private GDEventProcessor         mIntegration   = null;
  private EventProcessor           mToGDPro       = null;
  private static EventManager      mManager       = null;
  private EventFilter              filter         = new EventFilter();

  private static boolean  roundTripActive    = false;

  private static IAttributeChangeFacility attributeFacility = null;
  private static IMethodChangeFacility    methodFacility    = null;

  /**
   * Constructor a EventManager object.  Upon creation the round trip connection
   * is establish with Describe.
   */
  protected EventManager()
  {
    connectGDPro();
  }

  public static synchronized void setRoundTripActive(boolean active)
  {
    roundTripActive = active;
  }

  public static synchronized boolean isRoundTripActive()
  {
    return roundTripActive;
  }

  public static IAttributeChangeFacility getAttributeFacility() {
      if (attributeFacility == null) {
          IFacilityManager facMan = UMLSupport.getUMLSupport().getProduct().getFacilityManager();
          attributeFacility = (IAttributeChangeFacility)
                  facMan.retrieveFacility(
                          "RoundTrip.JavaAttributeChangeFacility");
      }
      return attributeFacility;
  }

  public static IMethodChangeFacility getMethodFacility() {
      if (methodFacility == null) {
          IFacilityManager facMan = UMLSupport.getUMLSupport().getProduct()
                                                .getFacilityManager();
          methodFacility = (IMethodChangeFacility)
                  facMan.retrieveFacility(
                      "RoundTrip.JavaMethodChangeFacility");
      }
      return methodFacility;
  }

  /**
   * Retrieves the Event Process that is used to communicate to integated enviornment.
   * @return The event process to be used.
   */
   /*
  protected  GDEventProcessor getIDEProcessor(  )
  {
    return mIntegration;
  }
  */

  /**
   * Sets the Event Process that is used to communicate to integated enviornment.
   * @param ideProcessor The event process to be used.
   */
   /*
  public void setIDEProcessor( GDEventProcessor ideProcessor )
  {
    mIntegration = null;
    mIntegration = ideProcessor;
  }
  */

  /**
   * Retrieves the Event Process that is used to communicate to Describe.
   * New event processor can be created to communicate with different processes
   * or different Describe methodologies.
   * @return The event process to use when communicating with Describe, or null
   *         if the event proesses has not been initialized.
   */
  protected EventProcessor getGDProProcessor(  )
  {
    return mToGDPro;
  }

  /**
   * The singleton implementation used to retrieve an instance of the EventManager.
   * @return A event manager instance.
   */
  public synchronized static EventManager getEventManager()
  {
    if (mManager == null) {
      mManager = new EventManager();
    }
    return mManager;
  }


  public EventFilter getEventFilter() {
    return filter;
  }

  /**
   * Initializes the event processor to be used to communicate with Descibe.
   */
  public  void connectGDPro()
  {
    // // **## Log.out("Disconnected from GDPro...");
    mToGDPro = new IDEProcessor();
  }

  /**
   * Disconnects the communication from the integrated environment to Describe.
   * After calling this routine communication to Describe will be broken.
   */
  public  void disconnectGDPro()
  {
    // // **## Log.out("Disconnected from GDPro...");
    mToGDPro = null;
  }

  // Operations to allow the IDE to send events to GDPro.

  /**
   * Issue a command to Describe to delete a class symbol.
   * @param state The transaction to act upon.
   */
  public void deleteClass(SymbolTransaction state)
  {
    if(isBlocked() == false)
    {
      EventProcessor processor = getGDProProcessor();
      if(getGDProProcessor() != null)
      {
        processor.deleteClass(state);
      }
    }
  }

  /**
   * Issue a command to Describe to delete a method from a class symbol.
   * @param state The transaction to act upon.
   */
  public void deleteMethod(MethodTransaction state)
  {
    if(isBlocked() == false)
    {
      EventProcessor processor = getGDProProcessor();
      if(getGDProProcessor() != null)
        processor.deleteMethod(state);
    }
  }

  /**
   * Issue a command to Describe add an interface implementation to a class symbol.
   * @param state The transaction to act upon.
   * @param pName The name of the package that contains the interface.
   * @param name The name of the interface.
   */
  public void addInterface(SymbolTransaction state,  String pName, String name)
  {
    if(isBlocked() == false)
    {
      EventProcessor processor = getGDProProcessor();
      if(processor != null)
      {
        processor.addInterface(state, pName, name);
      }
    }
  }

  /**
   * Issue a command to Describe remvoe an interface implementation from a class symbol.
   * @param state The transaction to act upon.
   * @param pName The name of the package that contains the interface.
   * @param name The name of the interface.
   */
  public void removeInterface(SymbolTransaction state,  String pName, String name)
  {
    if(isBlocked() == false)
    {
      EventProcessor processor = getGDProProcessor();
      if(processor != null)
      {
        processor.removeInterface(state, pName, name);
      }
    } else {
        Log.out("Roundtrip is blocked in removeInterface, aborting");
    }
  }

  /**
   * Issue a command to Describe add a collection of exceptions to a class symbol.
   * @param state The transaction to act upon.
   * @param value The exceptions to add.
   */
  public void setExceptions(MethodTransaction state,  String[] value)
  {
    if(isBlocked() == false)
    {
      EventProcessor processor = getGDProProcessor();
      if(processor != null)
      {
        String exceptions = "";
        for(int i = 0; i < value.length; i++)
        {
          if(i > 0)
            exceptions += ",";
          exceptions += value[i];
        }
        processor.setExceptions(state, exceptions);
      }
    }
  }

  /**
   * Issue a command to Describe update the package that contains the class symbol.
   * @param state The transaction to act upon.
   * @param value The name of the pacakge.
   */
  public void updatePackage(SymbolTransaction state, String value)
  {
    if (!isBlocked()) {
        EventProcessor processor = getGDProProcessor();
        if (processor != null) {
            processor.setAttribute(state, "ClassIdentifier.PackageName", value);
        }
    }
  }

  /**
   * Issue a command to Describe update the modifiers of a class symbols.
   * @param state The transaction to act upon.
   * @param value The java modifiers that have been changed.
   */
  public void updateClassModifers(SymbolTransaction state, int value)
  {
    if(isBlocked() == false)
    {
      IClassifier clazz = state.getSymbol();
      if(clazz != null)
      {
          if (clazz.getIsAbstract() != Modifier.isAbstract(value))
              clazz.setIsAbstract(Modifier.isAbstract(value));
          if (clazz.getIsLeaf() != Modifier.isFinal(value))
              clazz.setIsLeaf(Modifier.isFinal(value));
        // TO DO
        //processor.setTaggedValue(state, "GDHidden^strictfp", (Modifier.isStrict(value) == true ? "True" : "False"));

          if (clazz.getVisibility() !=
                  JavaClassUtils.getDescribeModifier(value))
              clazz.setVisibility(JavaClassUtils.getDescribeModifier(value));
      }
    }
  }

  /**
   * Issue a command to Describe update the modifiers of a class's method.
   * @param state The transaction to act upon.
   * @param value The java modifiers that have been changed.
   */
  public void updateMethodModifers(MethodTransaction state, int value)
  {
    if(isBlocked() == false)
    {
      IOperation oper = state.getOperation();
      if(oper != null)
      {
        Log.out("Setting diff modifiers for the method .................. ");
        if (oper.getIsAbstract() != Modifier.isAbstract(value))
            oper.setIsAbstract(Modifier.isAbstract(value));
        if (oper.getIsFinal() != Modifier.isFinal(value))
            oper.setIsFinal(Modifier.isFinal(value));
        if (oper.getIsStatic() != Modifier.isStatic(value))
            oper.setIsStatic(Modifier.isStatic(value));
        if (oper.getIsNative() != Modifier.isNative(value))
            oper.setIsNative(Modifier.isNative(value));
        if (oper.getIsStrictFP() != Modifier.isStrict(value))
            oper.setIsStrictFP(Modifier.isStrict(value));
        if(Modifier.isSynchronized(value)) {
            if (oper.getConcurrency() != 1)
                oper.setConcurrency(1);
        } else {
            if (oper.getConcurrency() != 0)
                oper.setConcurrency(0);
        }

        if (oper.getIsNative() != Modifier.isNative(value))
            oper.setIsNative(Modifier.isNative(value));
        // TO DO Native, Synchronized,
        //oper.setIsQuery(Modifier.isNative(value) == true ? 0 : 1));
        //processor.setNativeMethod(state(Modifier.isNative(value));
        //processor.setTaggedValue(state, "GDHidden^native", (Modifier.isNative(value) == true ? "True" : "False"));

        if (oper.getVisibility() != JavaClassUtils.getDescribeModifier(value))
            oper.setVisibility(JavaClassUtils.getDescribeModifier(value));
      }
     }
   }

  /**
   * Issue a command to Describe update return type of a method.
   * @param state The transaction to act upon.
   * @param value The return type.
   */
  public void updateMethodType(MethodTransaction state, String type)
  {
    if(isBlocked() == false)
    {
      EventProcessor processor = getGDProProcessor();
      if(processor != null)
      {
        processor.setAttribute(state, "ReturnType", type);
      }
    }
  }

  /**
   * Issue a command to Describe update type of a data membere.
   * @param state The transaction to act upon.
   * @param value The type.
   */
  public void updateMemberType(MemberTransaction state, String fullName, String sourceName)
  {
    if(isBlocked() == false)
    {
      EventProcessor processor = getGDProProcessor();
      if(processor != null)
      {
        //processor.setAttribute(state, "Type", type);
        processor.updateMemberType(state, fullName, sourceName);
      }
    }
  }

  /**
   * Issue a command to Describe update the modifiers of a class's data member.
   * @param state The transaction to act upon.
   * @param value The java modifiers that have been changed.
   */
  public void updateMemberModifers(MemberTransaction state, int value)
  {
    if(isBlocked() == false)
    {
      IStructuralFeature attribute = state.getAttribute();
      if(attribute != null)
      {

        attribute.setIsFinal(Modifier.isFinal(value));
        attribute.setIsStatic(Modifier.isStatic(value));
        //attribute.setIsNative(Modifier.isNative(value));
        attribute.setIsVolatile(Modifier.isVolatile(value));
        attribute.setIsTransient(Modifier.isTransient(value));
        // TO DO Transient, Volatile
        //attribute.setI(Modifier.isTransient(value));
        //attribute.setI(Modifier.isVolatile(value));
        //(Modifier.isStrict(value));
        attribute.setVisibility(JavaClassUtils.getDescribeModifier(value));
      }
    }
  }

  /**
   * Issue a command to Describe make a class symbol an interface.
   * @param state The transaction to act upon.
   * @param isInterface if true then the class is an inteface otherwise it is a class.
   */
  public void setAsInterface(SymbolTransaction state, boolean isInterface)
  {
    if(isBlocked() == false)
    {
      EventProcessor processor = getGDProProcessor();
      if(processor != null)
      {
        if(isInterface)
        {
          //processor.setAttribute(state, "ClassFormat", "interface");
          //processor.setAttribute(state, "ClassType", "Abstract");
          if(state.getSymbol() instanceof IClass) {
              IClassifier intfc =  state.getSymbol()
                                        .transform(ClassInfo.DS_INTERFACE);
              state.setSymbol(intfc);
          }
        }
        else
        {
         // processor.setAttribute(state, "ClassFormat", "class");
          if(state.getSymbol() instanceof IInterface) {
              IClassifier cls =  state.getSymbol()
                                      .transform(ClassInfo.DS_CLASS);
              cls.removeStereotype2(ClassInfo.DS_STE_INTERFACE);
              state.setSymbol(cls);
          }
        }
      }
    }
  }

  /**
   * Issue a command to Describe make a class symbol an interface.
   * @param state The transaction to act upon.
   * @param isInterface if true then the class is an inteface otherwise it is a class.
   */
  public void setAsEnumeration(SymbolTransaction state, boolean isEnumeration)
  {
    if(isBlocked() == false)
    {
      EventProcessor processor = getGDProProcessor();
      if(processor != null)
      {
        if(isEnumeration)
        {
          //processor.setAttribute(state, "ClassFormat", "interface");
          //processor.setAttribute(state, "ClassType", "Abstract");
          if(state.getSymbol() instanceof IClass) {
              IClassifier intfc =  state.getSymbol()
                                        .transform(ClassInfo.DS_ENUMERATION);
              state.setSymbol(intfc);
          }
        }
        else
        {
         // processor.setAttribute(state, "ClassFormat", "class");
          if(state.getSymbol() instanceof IEnumeration) {
              IClassifier cls =  state.getSymbol()
                                      .transform(ClassInfo.DS_CLASS);
              cls.removeStereotype2(ClassInfo.DS_STE_ENUMERATION);
              state.setSymbol(cls);
          }
        }
      }
    }
  }

  /**
   * When GDPro issues round trip events and we update the IDE we do not want to
   * execute event back to GDPro.  To pervent this from occuring I will set
   * a flag to prevent events from firing.  This is like the batch trigger flag
   * in GDPro.
   * @param turnOn True if the events are blocked.
   */
  protected final synchronized void blockTransactions(boolean turnOn)
  {
    // **## Log.out("BLOCKTRANSACTIONS: " + turnOn);
    mPreventEvents = turnOn;
  }

  protected final synchronized boolean isBlocked()
  {
    return mPreventEvents;
  }

  /**
   * A class that implements the _IGDAddInRoundTripEvents.  The _IGDAddInRoundTripEvents
   * interface allows a class to listener to round trip notification.  When a
   * round trip event occurs the source file will be update using the
   * Event Processor returned by getIDEProcessor.
   * @author  Trey Spiva
   * @version 1.0
   *
   * @see EventManager#getIDEProcessor()
   * @see GDEventProcessor
   */
  public class GDProRTEvents /* extends _IGDAddInRoundTripEventsAdapter */
  {
    /** Creates new GDProRTEvents   */
    public GDProRTEvents()
    {
    }

    protected boolean isJavaFile(String filename)
    {
      return filename.endsWith(".java");
    }
  }

}

