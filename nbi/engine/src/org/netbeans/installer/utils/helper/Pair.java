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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * $Id$
 */
package org.netbeans.installer.utils.helper;

/**
 *
 * @author Danila_Dugurov
 */
public class Pair<F, S> {
   
   private F first;
   private S second;
   
   public Pair(F first, S second) {
      this.first = first;
      this.second = second;
   }
   
   public static <F, S> Pair<F,S> create (F first, S second) {
      return new Pair<F,S>(first, second);
   }
   
   public F getFirst() {
      return first;
   }
   
   public S getSecond() {
      return second;
   }
   
   public String toString() {
      return "(" + first + "," + second + ")";
   }
   
   public boolean equals(Object other) {
      if (other == this) return true;
      if (other == null) return false;
      if (other instanceof Pair) {
         Pair pair = (Pair) other;
         if (first != null ? first.equals(pair.first): pair.first == null)
            return second != null ? second.equals(pair.second): pair.second == null;
      }
      return false;
   }
   
   public int hashCode() {
      int result;
      result = (first != null ? first.hashCode() : 0);
      result = 29 * result + (second != null ? second.hashCode() : 0);
      return result;
   }
}
