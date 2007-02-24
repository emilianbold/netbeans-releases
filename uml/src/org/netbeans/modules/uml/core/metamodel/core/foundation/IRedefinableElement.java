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

public interface IRedefinableElement extends INamedElement
{
  /**
   * Sets / Gets the final flag on this element. If true, this redefinable element can not be further redefined. The default value is false.
  */
  public boolean getIsFinal();

  /**
   * Sets / Gets the final flag on this element. If true, this redefinable element can not be further redefined. The default value is false.
  */
  public void setIsFinal( boolean value );

  /**
   * Adds an element that will be redefined by this element.
  */
  public long addRedefinedElement( IRedefinableElement element );

  /**
   * Removes an element that is being redefined by this element.
  */
  public long removeRedefinedElement( IRedefinableElement element );

  /**
   * Retrieves the collection of elements that are currently being redefined by this element.
  */
  public ETList<IRedefinableElement> getRedefinedElements();

  /**
   * Adds an element that is redefining this element.
  */
  public long addRedefiningElement( IRedefinableElement element );

  /**
   * Removes an element that is redefining this element.
  */
  public long removeRedefiningElement( IRedefinableElement element );

  /**
   * Retrieves the collection of elements that are currently redefining this element.
  */
  public ETList<IRedefinableElement> getRedefiningElements();

  /**
   * .
  */
  public long getRedefinedElementCount();

  /**
   * .
  */
  public long getRedefiningElementCount();

  /**
   * Determines if this element is being redefined by some other element..
  */
  public boolean getIsRedefined();

  /**
   * Determines if this element is being redefined by some other element..
  */
  public boolean getIsRedefining();

}
