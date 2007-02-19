/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.search.impl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openide.util.Lookup;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.17
 */
public final class Util {

  private Util() {}

  /**{@inheritDoc}*/
  public static <T> List<T> getInstances(Class<T> clazz) {
    Lookup.Result result = Lookup.getDefault().lookup(new Lookup.Template(clazz));
    Collection collection = result.allInstances();
    List<T> list = new ArrayList<T>();

    for (Object object : collection) {
      list.add(clazz.cast(object));
    }
    return list;
  }
}
