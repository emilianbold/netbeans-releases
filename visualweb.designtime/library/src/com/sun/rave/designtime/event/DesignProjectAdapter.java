/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.event;

import com.sun.rave.designtime.DesignContext;

/**
 * DesignProjectAdapter is a standard event adapter class for the DesignProjectListener event
 * interface.
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public class DesignProjectAdapter implements DesignProjectListener {
    public void contextOpened(DesignContext context) {}
    public void contextClosed(DesignContext context) {}
}
