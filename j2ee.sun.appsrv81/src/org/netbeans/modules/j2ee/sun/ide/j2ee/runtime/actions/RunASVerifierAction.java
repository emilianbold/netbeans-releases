/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;


import org.netbeans.modules.j2ee.sun.ide.j2ee.VerifierSupport;
/** Action that can always be invoked and work procedurally.
 * This action will display the verifier tool from app server
 * @author  ludo
 */
public class RunASVerifierAction extends NodeAction {
    
    protected Class[] cookieClasses() {
        return new Class[] {/* SourceCookie.class */};
    }
    
    
    protected void performAction(Node[] nodes) {
        if(nodes.length==0 || nodes.length>1) {
            return;
        }else{
            Node node=nodes[0];
            DataObject dob = (DataObject) node.getCookie(DataObject.class);
            if(dob!=null){
                //System.out.println("Found a dob " + dob+ " which is loaded by "+dob.getLoader());
                FileObject fo=dob.getPrimaryFile();
                String ext=fo.getExt();
                //System.out.println(fo + " " + ext);
                if("jar".equals(ext) || "war".equals(ext) || "ear".equals(ext) || "rar".equals(ext)){
                     final String jname = FileUtil.toFile(fo).getAbsolutePath();
                     RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            launchVerifier(jname);
                        }
                    });
                }
            }
            
        }
    }
    
    public void launchVerifier(String archiveLocation){
        String installRoot = System.getProperty("com.sun.aas.installRoot");
        System.setProperty("com.sun.aas.configRoot", installRoot+"/config");
        System.setProperty("com.sun.aas.verifier.xsl", installRoot+"/lib/verifier");
        System.setProperty("server.name", "server");
  //      ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
  //      Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        
        try{
            VerifierSupport.launchVerifier(archiveLocation,null);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
   //         Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }    
    
    public String getName() {
        return NbBundle.getMessage(ShowAdminToolAction.class, "LBL_RunASVeriferAction");
    }
    
    protected String iconResource() {
        return "org/netbeans/modules/j2ee/sun/ide/resources/ServerInstanceIcon.gif";
    }
    
    public HelpCtx getHelpCtx() {
        return null; // HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(RefreshAction.class);
    }
    
    protected boolean enable(Node[] nodes) {
        boolean result=false;
        if(nodes.length==0 || nodes.length>1) {
            result=false;
        }else{
            Node node=nodes[0];
            DataObject dob = (DataObject) node.getCookie(DataObject.class);
            if(dob!=null){
                //System.out.println("Found a dob " + dob+ " which is loaded by "+dob.getLoader());
                FileObject fo=dob.getPrimaryFile();
                String ext=fo.getExt();
                //System.out.println(fo + " " + ext);
                if("jar".equals(ext) || "war".equals(ext) || "ear".equals(ext) || "rar".equals(ext)){
                    result=true;
                }
            }
        }
        return result;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    
}
