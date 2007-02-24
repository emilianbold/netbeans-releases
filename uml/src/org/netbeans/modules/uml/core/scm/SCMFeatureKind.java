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

/*
 * SCMFeatureKind.java
 *
 * Created on July 13= 0; 2004= 0; 4:33 PM
 */

package org.netbeans.modules.uml.core.scm;

/**
 * The various features that should be supported by any SCM tool.
 *
 * @author  Trey Spiva
 */
public interface SCMFeatureKind
{
   /** Retrieves the latest version of a file or files.*/
   public final static int FK_GET_LATEST_VERSION = 2;

   /** Retrieves all the files from a user selected SCM directory.*/
   public final static int FK_GET_FROM_SCM_DIR = 3;

   /** Retrieves all the diagrams under the passed in Namespace element.*/
   public final static int FK_GET_SCOPED_DIAGRAMS = 4;

   /** Checks a file(s) into the SCM tool.*/
   public final static int FK_CHECK_IN = 5;

   /** Checks out a file(s) from the SCM tool.*/
   public final static int FK_CHECK_OUT = 6;

   /** Undo a previous check out.*/
   public final static int FK_UNDO_CHECK_OUT = 7;

   /** Show the version history of a particular file.*/
   public final static int FK_SHOW_HISTORY = 8;

   /**
    * Show the differences between a file in SCM and that same file on the
    * local file system.
    */
   public final static int FK_SHOW_DIFF = 9;

   /** Perform a a difference with no GUI involved.*/
   public final static int FK_SILENT_DIFF = 10;

   /** Add a file(s) to source control.*/
   public final static int FK_ADD_TO_SOURCE_CONTROL = 11;

   /** Remove a file(s) from source control.*/
   public final static int FK_REMOVE_FROM_SOURCE_CONTROL = 12;

   /** Display the GUI of the native SCM tool.*/
   public final static int FK_LAUNCH_PROVIDER = 13;
   
   /** Refresh the status of the UML elements.*/
   public final static int REFRESH_STATUS = 14;
}
