/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.core.stack.model.support;

import java.util.List;

/**
 *
 * @author masha
 */
public interface StackSupport {
   /**
     * Submits new stack to the storage and returns its id.
     *
     * @param stack  call stack represented as a list of function names.
     * @param cpuId  CPU executing given stack
     * @param threadId  thread executing given stack
     * @param timestamp  nanosecond time when the stack was seen
     * @return stack id which uniquely identifies the sequence of function calls
     */
  int getStackId(List<CharSequence> stack, int cpuId, int threadId, long timestamp);
}
