/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.terminal.api;

import org.openide.windows.IOContainer;
import org.openide.windows.TopComponent;

/**
 * A mixin interface for specific TopComponents which provide an IOContainer.
 * SHOULD go into org.openide.windows?
 * @author ivan
 */
public interface IOTopComponent {
    /**
     * Return the IOContainer contained in this object.
     * @return The IOContainer contained in this object.
     */
    public IOContainer ioContainer();

    /**
     * Return itself as a TopComponent.
     * @return itself as a TopComponent.
     */
    public TopComponent topComponent();
}
