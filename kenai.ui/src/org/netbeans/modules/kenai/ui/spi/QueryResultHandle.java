/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.kenai.ui.spi;

/**
 * Abstraction of a query result (a single link on a line in Issues section).
 *
 * @see ActionsFactory.getOpenQueryResultAction
 * 
 * @author S. Aubrecht
 */
public abstract class QueryResultHandle {

    /**
     * 
     * @return The text of the link (e.g. '25 total')
     */
    public abstract String getText();
}
