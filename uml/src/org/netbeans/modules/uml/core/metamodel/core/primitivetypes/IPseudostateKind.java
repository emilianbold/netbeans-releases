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
 * File       : IPseudostateKind.java
 * Created on : Sep 16, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.core.primitivetypes;

/**
 * @author Aztec
 */
public interface IPseudostateKind
{
    // Splits an incoming transition into several disjoint outgoing transition.
    public static final int PK_CHOICE = 0;

    // When reached as the target of a transition, restores the full state
    // configuration that was active just before the enclosing composite state
    // was last exited.
    public static final int PK_DEEPHISTORY = 1;

    // Splits an incoming transition into several concurrent outgoing 
    // transitions. All of the transitions fire together.
    public static final int PK_FORK = 2;

    // The default target of a transition to the enclosing composite state.
    public static final int PK_INITIAL = 3;

    // Merges transitions from concurrent regions into a single outgoing 
    // transition. All the transitions fire together.
    public static final int PK_JOIN = 4;
   
    // Chains together transitions into a single run-to-completion path. 
    // May have multiple input and/or output transitions. Each complete path 
    // involving a junction is logically independent and only one such path 
    // fires at one time.
    public static final int PK_JUNCTION = 5;
   
    // When reached as the target of a transition, restores the state within 
    // the enclosing composite state that was active just before the enclosing 
    // state was last exited.
    public static final int PK_SHALLOWHISTORY = 6;

    public static final int PK_ENTRYPOINT = 7;

    public static final int PK_STOP = 8;
}
