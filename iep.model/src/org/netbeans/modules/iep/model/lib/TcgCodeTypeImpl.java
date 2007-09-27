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


package org.netbeans.modules.iep.model.lib;

/**
 * Concrete class implements interface TcgCodeType.
 *
 * @author Bing Lu
 *
 * @see TcgCodeType
 * @since April 30, 2002
 */
class TcgCodeTypeImpl implements TcgCodeType {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(TcgCodeTypeImpl.class.getName());

    private String mName = null;
    private String mTemplateName = null;

    /**
     * Constructor for the TcgCodeType object
     *
     * @param name String name of this TcgCodeType
     * @param templateName String Velocity template file name
     */
    TcgCodeTypeImpl(String name, String templateName) {
        mName = name;
        mTemplateName = templateName;
    }

    /**
     * Gets the name attribute of the TcgCodeType object
     *
     * @return The name value
     */
    public String getName() {
        return mName;
    }



    /**
     * Gets the Velocity template file name of this TcgCodeType object
     *
     * @return The templateName value
     */
    public String getTemplateName() {
        return mTemplateName;
    }

    /**
     * Override Object's
     *
     * @return DOCUMENT ME!
     */
    public String toString() {

        StringBuffer sb = new StringBuffer();

        sb.append(super.toString());
        sb.append("\t[");
        sb.append("mName: " + mName + ", ");
        sb.append("mTemplateName: " + mTemplateName);
        sb.append("]");

        return sb.toString();
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
