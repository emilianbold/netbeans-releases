/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.team.server.ui.spi;

/**
 * Abstraction of a query result (a single link on a line in Issues section).
 *
 * @see ActionsFactory.getOpenQueryResultAction
 * 
 * @author S. Aubrecht
 */
public abstract class QueryResultHandle {

    public enum ResultType {
        ALL_CHANGES_RESULT,
        NAMED_RESULT
    }

    /**
     * 
     * @return The text of the link (e.g. '25 total')
     */
    public abstract String getText();

    /**
     * 
     * @return The tooltiptext of the link 
     */
    public abstract String getToolTipText();

    /**
     * Determines the result type
     *
     * @return the result type
     */
    public abstract ResultType getResultType();

}
