/*
 * FakePeerInvocationHandler.java
 *
 * Created on September 9, 2005, 12:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.form.fakepeer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.openide.ErrorManager;

/**
 *
 * An {@link InvocationHandler} used for dynamic interface implementation
 * in {@link FakePeerSupport}.
 *
 * @author Tomas Stupka
 */
public class FakePeerInvocationHandler implements InvocationHandler {        
    
    private final FakeComponentPeer comp;
    
    /**
     *
     */
    public FakePeerInvocationHandler (FakeComponentPeer comp) {
        this.comp = comp;
    }
    
    /**
     * @see InvocationHandler#invoke(Object proxy, Method method, Object[] args)
     */
    public Object invoke(Object proxy, Method method, Object[] args) 
       throws Throwable {        
       
        try {
            
            Class[] parameters = method.getParameterTypes();        
            Method thisMethod = comp.getClass().getMethod(method.getName(), parameters);        
            return thisMethod.invoke(comp, args);                     
            
            /* 
             * jdk 1.6 redefines the requestFocus() method in PeerComponent with a new parameter  
             * which is from a new type, unknown in previous jdk releases (<1.6), so we cannot 
             * just reimplement the method in FakePeerComponent.
             *
             * In case we should in future get a NoSuchMethodException, because of 
             * invoking the requestFocus() method with the jdk1.6 signature, we can implement 
             * here a special routine which return a proper value to the caller... .
             *
             */
            
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
            throw e;
        }        
        
    }
    
}
