/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.event;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.Position;

/**
 * DesignContextAdapter is a standard event adapter class for the DesignContextListener event
 * interface.
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public class DesignContextAdapter extends DesignBeanAdapter implements DesignContextListener {
    public void contextChanged(DesignContext context) {}
    public void contextActivated(DesignContext context) {}
    public void contextDeactivated(DesignContext context) {}
    public void beanCreated(DesignBean designBean) {}
    public void beanDeleted(DesignBean designBean) {}
    public void beanMoved(DesignBean designBean, DesignBean oldParent, Position pos) {}
}
