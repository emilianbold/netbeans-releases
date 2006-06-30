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
 */

package org.netbeans.modules.j2ee.deployment.common.api;

/**
 * This interface is used to communicate information about changes in
 * ejb remote interfaces and deployment descriptors in support of
 * incremental and iterative deployment.  An instance of this interface
 * is supplied by a development module and passed to a plugin module
 * during an incremental deploy.  The plugin can use this information
 * to potentially skip steps in the deploy process.  For example, if
 * no ejbs have changed, the stub generation step may be skipped.
 * @author  George Finklang
 */
public interface EjbChangeDescriptor {

   /* @returns true if signatures of the remote interfaces of any ejbs have
    * changed, or if any part of the deployment descriptor that relates to
    * ejb code generation has changed. */
   public boolean ejbsChanged();
   
   /* @returns String array of the names of the ejbs that have changed */
   public String[] getChangedEjbs();
}
