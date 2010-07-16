/**
 * Copyright (c) 2010, Oracle. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Oracle nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sun.glassfishesb.wlm.console;

import java.util.logging.Level;
import java.util.logging.Logger;
import sun.com.jbi.wfse.wsdl.taskcommon.ResultCodeType;
import sun.com.jbi.wfse.wsdl.taskcommon.TaskCommonPortType;
import sun.com.jbi.wfse.wsdl.taskcommon.TaskCommonService;
import sun.com.jbi.wfse.wsdl.taskcommon.TaskFaultMsg;
import sun.com.jbi.wfse.wsdl.taskcommon.TaskType;

/**
 *
 * @author Kirill Sorokin, Kirill.Sorokin@Sun.COM
 */
public final class WsUtils implements Constants {
    private static final Logger LOGGER = Logger.getLogger("com.sun.glassfishesb.wlm.console");

    public static TaskType getTask(
            final long taskId,
            final String userId) throws TaskFaultMsg {

        TaskType result = null;
        try {
            result = getPort().getTask(taskId, userId);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        return result;
    }

    public static ResultCodeType claimTask(
            final long taskId,
            final String userId) throws TaskFaultMsg {

        return getPort().claimTask(taskId, userId);
    }

    public static ResultCodeType reassignTask(
            final long taskId,
            final String userId,
            final String toUser,
            final String toGroup,
            final String toExcludedUser,
            final String toExcludedGroup) throws TaskFaultMsg {

        return getPort().reassignTask(
                taskId, toGroup, toUser, toExcludedGroup, toExcludedUser, userId);
    }

    public static ResultCodeType completeTask(
            final long taskId,
            final String userId) throws TaskFaultMsg {

        return getPort().completeTask(taskId, userId);
    }

    public static ResultCodeType revokeTask(
            final long taskId,
            final String userId) throws TaskFaultMsg {

        return getPort().revokeTask(taskId, userId);
    }

    public static Object getTaskInputData(
            final long taskId,
            final String userId) throws TaskFaultMsg {

        return getPort().getTaskInput(taskId, userId);
    }

    public static Object getTaskOutputData(
            final long taskId,
            final String userId) throws TaskFaultMsg {

        return getPort().getTaskOutput(taskId, userId);
    }

    public static ResultCodeType setTaskOutputData(
            final long taskId,
            final String userId,
            final Object data) throws TaskFaultMsg {

        return getPort().setTaskOutput(taskId, data, userId);
    }

    // Private -------------------------------------------------------------------------------------
    private static TaskCommonPortType getPort() {
        final TaskCommonService service = new TaskCommonService();
        return service.getTaskCommonPort();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Instance
    private WsUtils() {
        // Does nothing.
    }
}
