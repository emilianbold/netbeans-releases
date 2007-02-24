/*
 * ITaskWorker.java
 *
 * Created on September 27, 2006, 11:41 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.util;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public interface ITaskWorker
{
    public ITaskSupervisor getTaskSupervisor();
    public void setTaskSupervisor(ITaskSupervisor val);
}
