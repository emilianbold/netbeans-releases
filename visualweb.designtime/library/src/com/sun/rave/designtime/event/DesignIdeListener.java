/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.event;

import com.sun.rave.designtime.DesignIde;
import com.sun.rave.designtime.DesignProject;

/**
 * <p>DesignIdeListener is the event listener interface for {@link DesignIde}.
 * These methods are called when a {@link DesignProject} is opened or closed.</p>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed
 * to be implemented by the component (bean) author.</P>
 *
 * @since Mako
 */
public interface DesignIdeListener extends java.util.EventListener {


    /**
     * <p>A DesignProject has been opened in the IDE.</p>
     *
     * @param project the DesignProject that was opened
     */
    public void projectOpened(DesignProject project);


    /**
     * <p>A DesignProject has been closed in the IDE.</p>
     *
     * @param project the DesignProject that was closed
     */
    public void projectClosed(DesignProject project);


}
