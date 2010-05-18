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

package com.sun.rave.designtime;

/**
 * <p>The CustomizerResult is a special Result object that triggers the customizer dialog to be
 * displayed.  This Result object can be returned from any component-author operation and thus pop
 * up a customizer dialog.  Common uses include a context-menu item, which allows a right-click menu
 * item to launch a customizer, and a return value from a beanCreated method to pop up the
 * customizer dialog just as a component is dropped from the palette.</p>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see Customizer2
 * @see Result
 */
public class CustomizerResult extends Result {

    /**
     * Constructs a CustomizerResult without a DesignBean or Customizer2 (which must be
     * specified via 'setDesignBean(...)' and 'setCustomizer(...)' before being returned).
     */
    public CustomizerResult() {
        super(true);
    }

    /**
     * Constructs a CustomizerResult with the specified DesignBean and no Customizer2 (which
     * must be specified via 'setCustomizer2' before being returned).
     */
    public CustomizerResult(DesignBean customizeBean) {
        super(true);
        this.customizeBean = customizeBean;
    }

    /**
     * Constructs a CustomizerResult with the specified DesignBean and Customizer2
     */
    public CustomizerResult(DesignBean customizeBean, Customizer2 customizer) {
        this(customizeBean);
        this.customizer = customizer;
    }

    /**
     * Storage for the 'customizeBean' property
     */
    protected DesignBean customizeBean;

    /**
     * Sets the 'customizeBean' property
     *
     * @param customizeBean DesignBean the desired DesignBean to be customized
     */
    public void setCustomizeBean(DesignBean customizeBean) {
        this.customizeBean = customizeBean;
    }

    /**
     * Retrieves the 'customizeBean' property
     *
     * @return the current value of the 'customizeBean' property
     */
    public DesignBean getCustomizeBean() {
        return customizeBean;
    }

    /**
     * Storage for the 'customizer' property
     */
    protected Customizer2 customizer;

    /**
     * Sets the 'customizer' property
     *
     * @param customizer the desired Customizer2 to use on this DesignBean
     */
    public void setCustomizer(Customizer2 customizer) {
        this.customizer = customizer;
    }

    /**
     * Retrieves the 'customizer' property
     *
     * @return the current value of the 'customizer' property
     */
    public Customizer2 getCustomizer() {
        return customizer;
    }
}
