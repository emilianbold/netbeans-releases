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


package org.netbeans.modules.iep.editor.tcg.exception;

/**
 * For use to throw internationalized compiler related exception
 *
 * @author Bing Lu
 */
public class ModelSaveException
    extends I18nException {

    /**
     * Constructor for the ModelSaveException object
     *
     * @param keyName The internationalization key to look up the error
     *        template.
     * @param bundleName The bundle where the error template resides.
     * @param params Arguments passed to fill in parameters in the template.
     */
    public ModelSaveException(String keyName, String bundleName,
                                 Object[] params) {

        // Call the super class.
        super(keyName, bundleName, params);
    }

    /**
     * Constructor for the ModelSaveException object
     *
     * @param keyName The internationalization key.
     * @param bundleName The internationalizaiton bundle.
     * @param params Bits of information about what went wrong.
     * @param t The exception we wish to embed.
     */
    public ModelSaveException(String keyName, String bundleName,
                                 Object[] params, Throwable t) {
        super(keyName, bundleName, params, t);
    }

    /**
     * Convenience constructor for the I18nException object. Used when a method
     * catches one kind of I18nException and needs to throw a different kind
     * due to the throws clause in its contract. This constructor should be
     * used sparingly -- only when there is no useful additional information
     * that can be provided by supplying a list of arguments.
     *
     * @param original The original exception being caught, nested and
     *        rethrown.
     */
    public ModelSaveException(I18nException original) {
        super(original);
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
