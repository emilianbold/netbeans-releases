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

package com.sun.rave.designtime.faces;

import com.sun.rave.designtime.DesignBean;

/**
 * This class wraps the return value from FacesDesignContext.resolveBindingExprToBean(String expr).
 * The passed expression will be resolved to the deepest DesignBean instance within the context.
 * Any remaining expression will be returned in the 'remainder' property of this object.
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see FacesDesignContext#resolveBindingExprToBean(String)
 */
public class ResolveResult {

    /**
     * Constructs a ResolveResult with the specified DesignBean and remainder expression.
     *
     * @param designBean The deepest resolved DesignBean
     * @param remainder The remaining expression from the resolving process
     */
    public ResolveResult(DesignBean designBean, String remainder) {
        this.designBean = designBean;
        this.remainder = remainder;
    }

    /**
     * Constructs a ResolveResult with the specified remainder expression.
     *
     * @param remainder The remaining expression from the resolving process
     */
    public ResolveResult(String remainder) {
        this.designBean = null;
        this.remainder = remainder;
    }

    /**
     * protected storage for the 'designBean' property
     */
    protected DesignBean designBean;

    /**
     * Rertuns the deepest resolved DesignBean
     *
     * @return The deepest resolved DesignBean
     */
    public DesignBean getDesignBean() {
        return this.designBean;
    }

    /**
     * protected storage for the 'remainder' property
     */
    protected String remainder;

    /**
     * Rertuns the remaining expression that did could not be resolved to a DesignBean
     *
     * @return The remaining expression that did could not be resolved to a DesignBean
     */
    public String getRemainder() {
        return this.remainder;
    }

    public String toString() {
        return "[RR bean:[" + designBean + "] \"" + remainder + "\"]"; // NOI18N
    }
}
