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

public interface IDirectedRelationship extends IRelationship{

//   Adds a target element to this relationship.
//     HRESULT AddTarget([in] IElement* element);
  public void addTarget(IElement elem);

//             Removes a target element from this relationship.
//     HRESULT RemoveTarget([in] IElement* element);
  public void removeTarget(IElement elem);

//             Retrieves the collection of target elements on this relationship.
//     HRESULT Targets([out, retval] IElements** pVal);
  public ETList<IElement> getTargets();

//   Adds a source element to this relationship.
//     HRESULT AddSource([in] IElement* element);
  public void addSource(IElement elem);

//             Removes a source element from this relationship
//     HRESULT RemoveSource([in] IElement* element);
  public void removeSource(IElement elem);

//             Retrieves the collection of source elements on this relationship.
//     HRESULT Sources([out, retval] IElements** pVal);
  public ETList<IElement> getSources();

//   .
//     HRESULT TargetCount([out, retval] long* pVal);
  public long getTargetCount();

//   .
//     HRESULT SourceCount([out, retval] long* pVal);
  public long getSourceCount();

}
