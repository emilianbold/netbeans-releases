/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

// This sample program is named quote. It models a simple hardware support service. The program
// runs in a console window. It is written in standard C++. For the user interface the
// program uses C++ standard stream IO and is portable across Windows XP, Linux
// and Solaris.
// 
// The user is prompted to identify the customer and the system for which a support quote
// is required. The system can consist of CPU, Disk and Memory modules. The individual
// modules types can have multiple physical units. For example, the Disk module can
// consist of 1 to 10 standard or raid disks. The quote program computes an ad-hoc
// complexity metric for each module, and from these the program computes an ad-hoc
// support metric for the system. The quote is tailored for the customer through an ad-hoc
// discount code.
//
// The program is structured in three parts: (1) system, (2) customer, and
// (3) user interface.

// The system consists of 5 classes:
//	Module: Base class
//	CPU: derived from Module
//	Disk: derived from Module
//	Memory: derived from Module
//	System: collection of Modules (implememted using vector from the STL)

//The customer consists of 2 classes
//	NameList: collection of known customers (implemented as a singleton class)
//	Customer

//The user interface consists of a sequence of cout and cin statements, that serve to
// prompt the user for the customer name and the system description. The user interface
// is in the main function. The user may choose to identify the customer at the time the
// quote program is launched, through a command line parameter.


