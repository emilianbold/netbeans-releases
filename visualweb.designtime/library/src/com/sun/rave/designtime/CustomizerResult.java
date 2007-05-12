/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
