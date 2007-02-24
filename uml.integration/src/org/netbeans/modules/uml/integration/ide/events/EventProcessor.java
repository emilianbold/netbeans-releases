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
