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



/**
 * The EventProcessor defines a interface to update a Describe with changes
 * made to classes in a integration.  The EventManager is able to manage the
 * communication and direct the source code event to the correct EventProcessor.
 * @see EventManager
 */
public interface EventProcessor
{
  public void deleteClass(SymbolTransaction state);

  /**
   * Issue a command to Describe to delete a method from a class symbol.
   * @param state The transaction to act upon.
   */
  public void deleteMethod(MethodTransaction state);

  /**
   * Issue a command to Describe to delete a data member from a class symbol.
   * @param state The transaction to act upon.
   */
  public void deleteMember(MemberTransaction state);

  /**
   * Issue a command to Describe remove all imports from a class symbol.
   * @param state The transaction to act upon.
   */
  public void clearImports(SymbolTransaction state);

  /**
   * Issue a command to Describe remove all exceptions from a class symbol.
   * @param state The transaction to act upon.
   */
  public void clearExceptions(MethodTransaction state);

  /**
   * Issue a command to Describe add an import to a class symbol.
   * @param state The transaction to act upon.
   * @parma value The import to add.
   */
  public void addImport(SymbolTransaction state,  String value);

  /**
   * Issue a command to Describe add an interface implementation to a class symbol.
   * @param state The transaction to act upon.
   * @param pName The name of the package that contains the interface.
   * @param name The name of the interface.
   */
  public void addInterface(SymbolTransaction state,  String pName, String name);

  /**
   * Issue a command to Describe remvoe an interface implementation from a class symbol.
   * @param state The transaction to act upon.
   * @param pName The name of the package that contains the interface.
   * @param name The name of the interface.
   */
  public void removeInterface(SymbolTransaction state, String pName, String name);

  /**
   * Issue a command to Describe add an exception to a class symbol.
   * @param state The transaction to act upon.
   * @param value The exception to add.
   */
  public void addException(MethodTransaction state,  String value);

  /**
   * Issue a command to Describe add a collection of exceptions to a class symbol.
   * @param state The transaction to act upon.
   * @param value The exceptions to add.
   */
  public void setExceptions(MethodTransaction state,  String value);

  /**
   * Issue a command to Describe to updates a attibute on a class symbol.  The
   * attribute must be specified in a fully qualified manner.
   * <br>
   * <b>Example:</b> setAttribute("ClassIdentifier.FullyScopedName", name);
   *
   * @param state The transaction to act upon.
   * @param attr The fully qualified name of the attribute.
   * @param value The new value of the attribute.
   */
  public void setAttribute(SymbolTransaction state, String attr, String value);

  /**
   * Issue a command to Describe to updates a attibute on a <b>Operations</b>
   * attribute.  The attribute must be specified in a fully qualified manner.
   * <br>
   * <b>Example:</b> setAttribute("ClassIdentifier.FullyScopedName", name);
   *
   * @param state The transaction to act upon.
   * @param attr The fully qualified name of the attribute.
   * @param value The new value of the attribute.
   */
  public void setAttribute(MethodTransaction state, String attr, String value);

  /**
   * Issue a command to Describe to updates a attibute on a <b>Attributes</b>
   * attribute.  The attribute must be specified in a fully qualified manner.
   * <br>
   * <b>Example:</b> setAttribute("ClassIdentifier.FullyScopedName", name);
   *
   * @param state The transaction to act upon.
   * @param attr The fully qualified name of the attribute.
   * @param value The new value of the attribute.
   */
  public void setAttribute(MemberTransaction state, String attr, String value);

  /**
   * Issue a command to Describe to updates a tagged value on a <b>Attributes</b>
   * attribute.
   *
   * @param state The transaction to act upon.
   * @param tag The name of the tag to be set.
   * @param value The new value of the attribute.
   */
  public void setTaggedValue(MemberTransaction state, String tag, String value);

  /**
   * Issue a command to Describe to updates a tagged value on a <b>Operations</b>
   * attribute.
   *
   * @param state The transaction to act upon.
   * @param tag The name of the tag to be set.
   * @param value The new value of the attribute.
   */
  public void setTaggedValue(MethodTransaction state, String tag, String value);

  /**
   * Issue a command to Describe to updates a tagged value on a class symbol.
   *
   * @param state The transaction to act upon.
   * @param tag The name of the tag to be set.
   * @param value The new value of the attribute.
   */
  public void setTaggedValue(SymbolTransaction state, String tag, String value);

  /**
   * Issue a command to Describe update return type of a method.
   * @param state The transaction to act upon.
   * @param value The return type.
   */
  public void updateMemberType(MemberTransaction state, String fullName, String sourceName);

  /**
   * Issue a command to Describe update the parameters for a method.
   * @param state The transaction to act upon.
   * @param value An array or parameters.
   */
  public void setMethodParameters(MethodTransaction state, String params);

  /**
   * Issue a command to Describe to remove a generalization associated with a
   * class symbol.
   * @param state The transaction to act upon.
   * @param value The value.
   */
  public void removeSuperClass(SymbolTransaction state, String className, String packageName);

  /**
   * Issue a command to Describe to add a generalization associated with a
   * class symbol.
   * @param state The transaction to act upon.
   * @param value The value.
   */
  public void addSuperClass(SymbolTransaction state, String className, String packageName);
}
