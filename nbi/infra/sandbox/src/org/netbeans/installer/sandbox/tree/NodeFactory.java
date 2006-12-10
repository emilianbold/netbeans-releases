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
package org.netbeans.installer.sandbox.tree;

/**
 * @author Danila_Dugurov
 *
 */
public class NodeFactory {
  private static final Class[] ARGS_CLASSES = new Class[0];
  
  private static final Object[] ARGS = new Object[0];
  
  public static <T extends Node> Node createNode(Class<T> nodeClass) {
    try {
      return nodeClass.getConstructor(ARGS_CLASSES).newInstance(ARGS);
    } catch (IllegalAccessException ex) {
      throw new RuntimeException(nodeClass.getName()
      + ": construction should be public");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(nodeClass.getName()
      + " don't have appropriate constructor for this factory");
    } catch (Exception e) {
      throw new RuntimeException("Node creation failed", e);
    }
  }
}
