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


package org.netbeans.modules.uml.project.ui.nodes;

import org.netbeans.modules.uml.common.RelationshipCookie;
import org.openide.util.Lookup;

import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeRelElement;


/**
 *
 * @author Trey Spiva
 */
public class UMLRelationshipNode extends UMLModelElementNode implements ITreeRelElement
{

   /**
    *
    */
   public UMLRelationshipNode()
   {
      super();
	  getCookieSet().add(new RelationshipCookie());
   }


   /**
    * 
    */
   public UMLRelationshipNode(Lookup lookup)
   {
      super(lookup);
   }

   /**
    * @param item
    * @throws NullPointerException
    */
   public UMLRelationshipNode(IProjectTreeItem item) throws NullPointerException
   {
      super(item);
   }

   /**
    * @param item
    * @throws NullPointerException
    */
   public UMLRelationshipNode(Lookup lookup, IProjectTreeItem item) throws NullPointerException
   {
      super(lookup, item);
   }
}
