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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import org.openide.nodes.Node;

/**
 * A Cookie that indicates the direction of the operation containing
 * this cookie in its Lookup. All operations have a direction associated
 * with them, either pointing to the right, or pointing to the left.
 *
 * @author  Nathan Fiedler
 */
public class DirectionCookie implements Node.Cookie {
    /** True if this cookie represents a right-sided operation. */
    private boolean rightSided;

    /**
     * Creates a new instance of DirectionCookie.
     *
     * @param  rightSided  true if this is a right-sided cookie, false for left.
     */
    public DirectionCookie(boolean rightSided) {
        this.rightSided = rightSided;
    }

    /**
     * Returns true to indicate that the associated operation is
     * left-sided, and false for right-sided. This merely returns the
     * opposite of whatever the isRightSided() method returns.
     *
     * @return  true if this is a left-sided cookie.
     */
    public boolean isLeftSided() {
        return !rightSided;
    }

    /**
     * Returns true to indicate that the associated operation is
     * right-sided, and false for left-sided.
     *
     * @return  true if this is a right-sided cookie.
     */
    public boolean isRightSided() {
        return rightSided;
    }

    /**
     * Set the right-sided value for this cookie.
     *
     * @param  rightSided  true if this is a right-sided cookie, false for left.
     */
    public void setRightSided(boolean rightSided) {
        this.rightSided = rightSided;
    }
}
