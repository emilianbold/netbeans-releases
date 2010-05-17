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
package org.netbeans.modules.edm.model;

/**
 * UI wrapper class for SQLObjects which serve as inputs to SQLConnectableObjects.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public interface SQLInputObject {

    public static String ATTR_ARGNAME = "argName";

    public static String ATTR_DISPLAY_NAME = "displayName";

    public static String TAG_INPUT = "input";

    /**
     * Gets argument name associated with this input.
     * 
     * @return argument name
     */
    public String getArgName();

    /**
     * Gets display name of this input.
     * 
     * @return current display name
     */
    public String getDisplayName();

    /**
     * Gets reference to SQLObject holding value of this input
     * 
     * @return input object
     */
    public SQLObject getSQLObject();

    /**
     * Sets display name of this input.
     * 
     * @param newName new display name
     */
    public void setDisplayName(String newName);

    /**
     * Sets reference to SQLObject holding value of this input
     * 
     * @param newInput reference to new input object
     */
    public void setSQLObject(SQLObject newInput);

    /**
     * Writes contents of this instance to an XML element.
     * 
     * @param prefix String to prepend to the start of each new line
     * @return XML element representing the contents of this instance
     */
    public String toXMLString(String prefix);
}

