/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
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
     * Returns the description value.
     * @return Action description.
     */
    public String getDescription();
}
