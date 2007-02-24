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

package org.netbeans.modules.uml.core.scm;

/**
 * Provides versioning information for an IProxyDiagram.
 */
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.BundleSupport;

/**
 */
public interface ISCMDiagramItem extends ISCMItem
{

   /**
    * Retreives the  IProxyDiagram associated with the SCM item.
    */
   public IProxyDiagram getProxyDiagram();

   /**
    * Sets the  IProxyDiagram associated with the SCM item.
    */
   public void setProxyDiagram(IProxyDiagram val);

   /**
    * Retrieves the layout portion of the diagram. FileName is the
    * presentation portion.
    */
   public String getLayoutFile();

   /**
    * Sets the layout portion of the diagram. FileName is the
    * presentation portion.
    */
   public void setLayoutFile(String val);
}
