/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy;

/**
 * 
 * Defines an action to be executed by <code>ActionProducer</code> instance.
 * @see org.netbeans.jemmy.ActionProducer
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public interface Action{
    /**
     * Executes this action.
     * @param obj action argument. This argument might be the method
     * parameter in an invocation of
     * <code>ActionProducer.produceAction(Object)</code>.  This argument
     * might be a <code>java.lang.String[]</code> that lists the
     * command line arguments used to execute a test (or not).
     * @return action result.
     */
    public Object launch(Object obj);

    /**
     * @return Action description.
     */
    public String getDescription();
}
