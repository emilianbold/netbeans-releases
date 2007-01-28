/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.event;

import com.sun.rave.designtime.DesignContext;

/**
 * DesignProjectListener is the event listener interface for DesignProject.  These methods are
 * called when a DesignContext is opened or closed.
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface DesignProjectListener extends java.util.EventListener {

    /**
     * A DesignContext has been opened in the project
     *
     * @param context the DesignContext that was opened
     */
    public void contextOpened(DesignContext context);

    /**
     * A DesignContext has been closed in the project
     *
     * @param context the DesignContext that was closed
     */
    public void contextClosed(DesignContext context);
}
