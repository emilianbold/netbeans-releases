/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.event;

import com.sun.rave.designtime.DesignProject;

/**
 * <p>DesignIdeAdapter is a standard event adapter class for the
 * {@link DesignIdeListener} event interface.</p>
 *
 * @since Mako
 */
public class DesignIdeAdapter implements DesignIdeListener {

    /** {@inheritDoc} */
    public void projectOpened(DesignProject project) {}

    /** {@inheritDoc} */
    public void projectClosed(DesignProject project) {}

}
