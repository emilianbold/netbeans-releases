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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.netbeans.modules.uml.core.support.umlutils.ETList;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface INamespace extends INamedElement{

  /** Adds an element to this Namespace. */
  public boolean addOwnedElement(INamedElement elem);

  /** Removes an element from this Namespace. */
  public void removeOwnedElement(INamedElement elem);

  /** retrieves the collection of elements owned by this Namespace. */
  public ETList<INamedElement> getOwnedElements();

  /** Adds an element that will be visible within this namespace. */
  public void addVisibleMember(INamedElement elem);

  /** Removes an element that is currently visible within this namespace. */
  public void removeVisibleMember(INamedElement elem);

  /** Retrieves the collection of elements currently visible within the namespace. */
  public ETList<INamedElement> getVisibleMembers();

  /** Retrieves all members within this namespace by the passed in name. */
  public ETList<INamedElement> getOwnedElementsByName(String name);

  /** Retrieves the number of elements owned by the namespace. */
  public long getOwnedElementCount();

  /** Retrieves the number of visible members owned by the namespace */
  public long getVisibleMemberCount();

  public IPackage createPackageStructure(String packageStructure);
}