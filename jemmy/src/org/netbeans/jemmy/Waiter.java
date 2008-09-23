/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 *
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy;

import java.awt.Component;

/**
 *
 * Waits for something defined by Waitable interface to be happened.
 *
 * <BR><BR>Timeouts used: <BR>
 * Waiter.TimeDelta - time delta to check actionProduced result.<BR>
 * Waiter.WaitingTime - maximal waiting time<BR>
 * Waiter.AfterWaitingTime - time to sleep after waiting has been finished.<BR>
 *
 * @see Timeouts
 * @see Waitable
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class Waiter implements Waitable, Timeoutable, Outputable{

    private final static long TIME_DELTA = 10;
    private final static long WAIT_TIME = 60000;
    private final static long AFTER_WAIT_TIME = 0;

    private Waitable waitable;
    private long startTime = 0;
    private long endTime = -1;
    private Object result;
    private Timeouts timeouts;
    private TestOut out;

    /**
     * Constructor.
     * @param	w Waitable object defining waiting criteria.
     */
    public Waiter(Waitable w) {
	super();
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
	setOutput(JemmyProperties.getProperties().getOutput());
	waitable = w;
    }

    /**
     * Can be used from subclass.
     */
    protected Waiter() {
	super();
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
	setOutput(JemmyProperties.getProperties().getOutput());
    }

    static {
	Timeouts.initDefault("Waiter.TimeDelta", TIME_DELTA);
	Timeouts.initDefault("Waiter.WaitingTime", WAIT_TIME);
	Timeouts.initDefault("Waiter.AfterWaitingTime", AFTER_WAIT_TIME);
    }

    /**
     * Defines current timeouts.
     * 
     * @param	timeouts A collection of timeout assignments.
     * @see	org.netbeans.jemmy.Timeoutable
     * @see	org.netbeans.jemmy.Timeouts
     * @see #getTimeouts
     */
    public void setTimeouts(Timeouts timeouts) {
	this.timeouts = timeouts;
    }

    /**
     * Return current timeouts.
     * @return the collection of current timeout assignments.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     * @see #setTimeouts
     */
    public Timeouts getTimeouts() {
	return(timeouts);
    }

    /**
     * Defines print output streams or writers.
     * @param out Identify the streams or writers used for print output.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     * @see #getOutput
     */
    public void setOutput(TestOut out) {
	this.out = out;
    }

    /**
     * Returns print output streams or writers.
     * @return an object that contains references to objects for
     * printing to output and err streams.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     * @see #setOutput
     */
    public TestOut getOutput() {
	return(out);
    }

    /**
     * Waits for not null result of actionProduced method of Waitable implementation passed into constructor.
     * @param	waitableObject Object to be passed into actionProduced method.
     * @return non null result of action.
     * @throws	TimeoutExpiredException
     * @exception	InterruptedException
     */
    public Object waitAction(Object waitableObject) throws InterruptedException {
	startTime = System.currentTimeMillis();
	out.printTrace(getWaitingStartedMessage());
	out.printGolden(getGoldenWaitingStartedMessage());
	long timeDelta = timeouts.getTimeout("Waiter.TimeDelta");
	while((result = checkActionProduced(waitableObject)) == null) {
	    Thread.sleep(timeDelta);
	    if(timeoutExpired()) {
		out.printError(getTimeoutExpiredMessage(timeFromStart()));
		out.printGolden(getGoldenTimeoutExpiredMessage());
		throw(new TimeoutExpiredException(getActualDescription()));
	    }
	}
	endTime = System.currentTimeMillis();
	out.printTrace(getActionProducedMessage(endTime - startTime, result));
	out.printGolden(getGoldenActionProducedMessage());
	Thread.sleep(timeouts.getTimeout("Waiter.AfterWaitingTime"));
	return(result);
    }

    /**
     * @see	Waitable
     * @param	obj
     */
    public Object actionProduced(Object obj) {
	return(Boolean.TRUE);
    }

    /** 
     * @see Waitable
     */
    public String getDescription() {
	return("Unknown waiting");
    }

    /**
     * Returns message to be printed before waiting start.
     * @return a message.
     */
    protected String getWaitingStartedMessage() {
	return("Start to wait action \"" + getActualDescription() + "\"");
    }

    /**
     * Returns message to be printed when waiting timeout has been expired.
     * @param timeSpent time from waiting start (milliseconds)
     * @return a message.
     */
    protected String getTimeoutExpiredMessage(long timeSpent) {
	return("\"" + getActualDescription() + "\" action has not been produced in " +
	       (new Long(timeSpent)).toString() + " milliseconds");
    }

    /**
     * Returns message to be printed when waiting has been successfully finished.
     * @param timeSpent time from waiting start (milliseconds)
     * @param result result of Waitable.actionproduced method.
     * @return a message.
     */
    protected String getActionProducedMessage(long timeSpent, final Object result) {
        String resultToString;
        if(result instanceof Component) {
            // run toString in dispatch thread
            resultToString = (String)new QueueTool().invokeSmoothly(
                new QueueTool.QueueAction("result.toString()") {
                    public Object launch() {
                        return result.toString();
                    }
                }
            );
        } else {
            resultToString = result.toString();
        }
	return("\"" + getActualDescription() + "\" action has been produced in " +
	       (new Long(timeSpent)).toString() + " milliseconds with result " +
	       "\n    : " + resultToString);
    }

    /**
     * Returns message to be printed int golden output before waiting start.
     * @return a message.
     */
    protected String getGoldenWaitingStartedMessage() {
	return("Start to wait action \"" + getActualDescription() + "\"");
    }

    /**
     * Returns message to be printed int golden output when waiting timeout has been expired.
     * @return a message.
     */
    protected String getGoldenTimeoutExpiredMessage() {
	return("\"" + getActualDescription() + "\" action has not been produced");
    }

    /**
     * Returns message to be printed int golden output when waiting has been successfully finished.
     * @return a message.
     */
    protected String getGoldenActionProducedMessage() {
	return("\"" + getActualDescription() + "\" action has been produced");
    }

    /**
     * Returns time from waiting start.
     * @return Time spent for waiting already.
     */
    protected long timeFromStart() {
	return(System.currentTimeMillis() - startTime);
    }

    private Object checkActionProduced(Object obj) {
	if(waitable != null) {
	    return(waitable.actionProduced(obj));
	} else {
	    return(actionProduced(obj));
	}
    }

    private String getActualDescription() {
	if(waitable != null) {
	    return(waitable.getDescription());
	} else {
	    return(getDescription());
	}
    }

    private boolean timeoutExpired() {
	return(timeFromStart() > timeouts.getTimeout("Waiter.WaitingTime"));
    }

}
