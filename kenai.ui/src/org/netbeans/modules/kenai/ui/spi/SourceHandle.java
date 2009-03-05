/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.kenai.ui.spi;

/**
 * Abstraction for a single source repository (a line in 'Sources' section).
 *
 * @author S. Aubrecht
 */
public abstract class SourceHandle {

    /**
     *
     * @return Display name
     */
    public abstract String getDisplayName();

    /**
     *
     * @return True if 'get' link is available, false to render the repository as disabled.
     */
    public abstract boolean isSupported();
}
