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
import java.util.*;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
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

    private static Set remoteMethods = new HashSet();
    static {
        remoteMethods.add("checkout");  // NOI19N
        remoteMethods.add("commit"); // NOI19N
        remoteMethods.add("commitAcrossWC"); // NOI19N
        remoteMethods.add("getList"); // NOI19N
        remoteMethods.add("getDirEntry"); // NOI19N
        remoteMethods.add("copy"); // NOI19N
        remoteMethods.add("remove"); // NOI19N
        remoteMethods.add("doExport"); // NOI19N
        remoteMethods.add("doImport"); // NOI19N
        remoteMethods.add("mkdir"); // NOI19N
        remoteMethods.add("move"); // NOI19N
        remoteMethods.add("update"); // NOI19N
        remoteMethods.add("getLogMessages"); // NOI19N
        remoteMethods.add("getContent"); // NOI19N
        remoteMethods.add("setRevProperty"); // NOI19N
        remoteMethods.add("diff"); // NOI19N
        remoteMethods.add("annotate"); // NOI19N
        remoteMethods.add("getInfo"); // NOI19N
        remoteMethods.add("switchToUrl"); // NOI19N
        remoteMethods.add("merge"); // NOI19N
        remoteMethods.add("lock"); // NOI19N
        remoteMethods.add("unlock"); // NOI19N
    }

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

        assert noRemoteCallinAWT(method, args) : "noRemoteCallinAWT(): " + method.getName();

        try {             
            Class[] parameters = method.getParameterTypes();        
            Method thisMethod = adapter.getClass().getMethod(method.getName(), parameters);        
            return thisMethod.invoke(adapter, args);                                             
        } catch (Exception e) {
            try {
                if(handleException(e)) {
                    return invoke(proxy, method, args); // XXX hm...
                } else {
                    // XXX some action canceled by user message ... wrap the exception ???
                    throw e;
                }                        
            } catch (Exception ex) {
                throw ex;
            }                        
        }                      
    }

    /**
     * @return false for methods that perform calls over network
     */
    private static boolean noRemoteCallinAWT(Method method, Object[] args) {

        if(!SwingUtilities.isEventDispatchThread()) {
            return true;
        }

        String name = method.getName();
        if (remoteMethods.contains(name)) {
            return false;
        } else if ("getStatus".equals(name)) { // NOI18N
            return args.length != 4 || (Boolean.TRUE.equals(args[3]) == false);
        }

        return true;
    }

    private boolean handleException(Throwable t) throws Throwable {
        SVNClientException svnException = null;
        if( t instanceof InvocationTargetException ) {
            t = ((InvocationTargetException) t).getCause();            
        } 
        if( !(t instanceof SVNClientException) ) {
            throw t;
        }
        
        ExceptionInformation ei = new ExceptionInformation((SVNClientException) t);
        if(ei.isAuthentication()) {
            return handleAuthenticationError();
        }

        // no handling for this exception -> throw it, so the caller may decide what to do...
        throw t;
    }

    private boolean handleAuthenticationError() {
        Repository repository = new Repository(false, false, "Correct the password, username and proxy settings for ths URL:"); 
        DialogDescriptor dialogDescriptor = new DialogDescriptor(repository.getPanel(), "Authentication failed"); 

        JButton retryButton = new JButton("Retry"); 
        dialogDescriptor.setOptions(new Object[] {retryButton, "Cancel"}); 
        
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
