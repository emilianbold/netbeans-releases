/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
