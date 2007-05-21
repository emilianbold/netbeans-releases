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

package org.netbeans.modules.uml.codegen.java.merging;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;



/**
 *
 * @author Thuy
 */
public class PositionMapper
{
   private TreeMap<Long, Long> positionMap = new TreeMap<Long, Long>();
   
   /** Creates a new instance of PositionMapper */
   public PositionMapper()
   {
   }
   
   /**
    * Add to the map the start position where the change occurs and the number of bytes being 
    * changed
    * @param position start position in byte where the change occurs.
    * @param bytesShift number of bytes being either removed or added.
    */
   public void addToMap(long position, long bytesShift)
   {
      positionMap.put(new Long(position), new Long(bytesShift));
   }
   
   public long getMappedPositionFor(long position)
   {
      long bytesShift = 0;
      Long posKey = null;
      Set<Long> sortedKeys = this.positionMap.keySet();
      Iterator<Long> iter = sortedKeys.iterator();
      while (iter.hasNext())
      {
         posKey = iter.next();
         if (posKey != null && posKey.longValue() <= position)
         {
            Long value = this.positionMap.get(posKey);
            if (value != null) 
            {
               bytesShift += value.longValue();
            }
         } 
      }
      
      return position + bytesShift;
   }
   
   public String toString() {
      StringBuffer strBuffer = new StringBuffer();
      Set<Long> keys = positionMap.keySet();
      Long aKey = null;
      Iterator<Long> iter = keys.iterator();
      while (iter.hasNext()){
         aKey = iter.next();
         strBuffer.append(aKey.longValue());
         strBuffer.append(" = ");
         strBuffer.append(((Long)positionMap.get(aKey)).longValue());
         strBuffer.append("\n");
      }
      return strBuffer.toString();
   }
   
}
