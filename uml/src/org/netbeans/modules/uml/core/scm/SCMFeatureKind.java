/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
