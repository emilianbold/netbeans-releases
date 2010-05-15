/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.soa.palette.java.codegen;

import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author gpatil
 */
public abstract class BaseGenTask<T extends WorkingCopy> implements Task<WorkingCopy> {

    protected Exception myException = null;

    public Exception getException() {
        return myException;
    }
    
    public abstract void run(WorkingCopy workingCopy) throws Exception;
}
