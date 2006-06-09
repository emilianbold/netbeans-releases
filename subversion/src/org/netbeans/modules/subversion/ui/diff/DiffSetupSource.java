/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.diff;

import java.util.*;

/**
 * Identifies components rendering file differences.
 * It allows to access diff parameters, multifile setup.
 *
 * <p>It can be implemented directly by respective
 * TopComponets or it can be added in their lookup
 * (it allows proxing).
 *
 * @author Maros Sandor
 */
public interface DiffSetupSource {

    /**
     * Access actually user-visible diff setup.
     *
     * @return read-only {@link Setup}s copy never <code>null</code>
     */
    Collection getSetups();

    /**
     * Prefered display name or null.
     */
    String getSetupDisplayName();
}
