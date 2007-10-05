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
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
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
 *
 *
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.drivers;

import org.netbeans.jemmy.JemmyException;

/**
 * Is thrown as a result of attempt to use driver for unsupported operator type.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public class UnsupportedOperatorException extends JemmyException {

    /**
     * Constructor.
     * @param driver a driver
     * @param operator an operator
     */
    public UnsupportedOperatorException(Class driver, Class operator) {
	super(driver.getName() + " operators are not supported by " +
	      operator.getName() + " driver!");
    }

    /**
     * Checks if operator class is in the list of supported classes.
     * @param driver Driver class
     * @param supported Supported classes.
     * @param operator Operator class.
     * @throws UnsupportedOperatorException if class is not supported.
     */
    public static void checkSupported(Class driver, Class[] supported, Class operator) {
	for(int i = 0; i < supported.length; i++) {
	    if(supported[i].isAssignableFrom(operator)) {
		return;
	    }
	}
	throw(new UnsupportedOperatorException(driver, operator));
    }

    /**
     * Checks if operator class name is in the list of supported classes names.
     * @param driver Driver class
     * @param supported Supported classes names.
     * @param operator Operator class.
     * @throws UnsupportedOperatorException if class is not supported.
     */
    public static void checkSupported(Class driver, String[] supported, Class operator) {
        Class opClass = operator;
        do {
            for(int i = 0; i < supported.length; i++) {
                if(opClass.getName().equals(supported[i])) {
                    return;
                }
            }
        } while((opClass = opClass.getSuperclass()) != null);
	throw(new UnsupportedOperatorException(driver, operator));
    }
}
