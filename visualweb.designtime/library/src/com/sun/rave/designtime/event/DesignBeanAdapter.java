/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.event;

import com.sun.rave.designtime.*;

/**
 * DesignBeanAdapter is a standard event adapter class for the DesignBeanListener event interface.
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public class DesignBeanAdapter implements DesignBeanListener {
    public void beanContextActivated(DesignBean designBean) {}
    public void beanContextDeactivated(DesignBean designBean) {}
    public void instanceNameChanged(DesignBean designBean, String oldInstanceName) {}
    public void beanChanged(DesignBean designBean) {}
    public void propertyChanged(DesignProperty prop, Object oldValue) {}
    public void eventChanged(DesignEvent event) {}
}
