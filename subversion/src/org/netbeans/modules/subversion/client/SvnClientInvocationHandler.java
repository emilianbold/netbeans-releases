/*
 * SvnClientInvocationHandler.java
 *
 * Created on September 9, 2005, 12:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.subversion.client;

import java.awt.Dialog;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JButton;
import org.netbeans.modules.subversion.ui.repository.Repository;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 *
 *
 * @author Tomas Stupka
 */
public class SvnClientInvocationHandler implements InvocationHandler {        
    
    private final ISVNClientAdapter adapter;
    
    /**
     *
     */
    public SvnClientInvocationHandler (ISVNClientAdapter adapter) {
        this.adapter = adapter;
    }
    
    /**
     * @see InvocationHandler#invoke(Object proxy, Method method, Object[] args)
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {               
        
        // TODO assert that no method is called from AWT
        
        try {             
            Class[] parameters = method.getParameterTypes();        
            Method thisMethod = adapter.getClass().getMethod(method.getName(), parameters);        
            return thisMethod.invoke(adapter, args);                                             
        } catch (Exception e) {
            try {
                if(handleException(e)) {
                    return invoke(proxy, method, args); // XXX hm...
                } else {
                    // XXX some action canceled by user message ...
                    return null;
                }                        
            } catch (Exception ex) {
                throw ex;
            }                        
        }                      
    }

    private boolean handleException(Throwable t) throws Throwable {
        SVNClientException svnException = null;
        if( t instanceof InvocationTargetException ) {
            t = ((InvocationTargetException) t).getCause();            
        } 
        if( !(t instanceof SVNClientException) ) {
            throw t;
        } 
        
        String msg = t.getMessage();
        
        if( msg.indexOf("Authentication error from server: Username not found") > - 1 ||
            msg.indexOf("authorization failed") > - 1 )         
        {         
            return handleAuthenticationError();            
        }        

        // no handling for this exception -> throw it, so the caller may decide what to do...
        throw t;
    }

    private boolean handleAuthenticationError() {
        Repository repository = new Repository(false, "Authentication failed:"); // XXX
        DialogDescriptor dialogDescriptor = new DialogDescriptor(repository.getPanel(), ""); // XXX        

        JButton retryButton = new JButton("Retry"); // XXX
        dialogDescriptor.setOptions(new Object[] {retryButton, "Cancel"}); // XXX
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);     
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.setVisible(true);
        boolean ret = dialogDescriptor.getValue()==retryButton;
        if(ret) {
            adapter.setUsername(repository.getUserName());
            adapter.setPassword(repository.getPassword());

            // XXX here should be handled the proxy setting - the whole thing is a mess, its a wonder it works at all ...
        }        
        return ret;
    }        
}
