/*
 * AbstractTask.java
 *
 * Created on March 22, 2007, 1:16 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.identity.profile.ui.support;

import org.netbeans.api.java.source.CancellableTask;

/**
 *
 * @author PeterLiu
 */
public abstract class AbstractTask<P> implements CancellableTask<P> {
    
    /** Creates a new instance of AbstractTask */
    public AbstractTask() {
    }
  
    public void cancel() {};
}
