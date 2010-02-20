/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions;

import java.io.File;
import org.netbeans.modules.glassfish.eecommon.api.VerifierSupport;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;


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
                     File file = FileUtil.toFile(fo);
                     final String jname = file.getAbsolutePath();
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
        java.io.File irf = org.netbeans.modules.j2ee.sun.api.ServerLocationManager.getLatestPlatformLocation();
        if (null != irf && irf.exists()) {
            String installRoot = irf.getAbsolutePath(); //System.getProperty("com.sun.aas.installRoot");
            System.setProperty("com.sun.aas.configRoot", installRoot+"/config");
            System.setProperty("com.sun.aas.verifier.xsl", installRoot+"/lib/verifier");
            System.setProperty("server.name", "server");
      //      ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
      //      Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        }        
        try{
            VerifierSupport.launchVerifier(archiveLocation,null,irf);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
   //         Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }    
    
    public String getName() {
        return NbBundle.getMessage(RunASVerifierAction.class, "LBL_RunASVeriferAction");
    }
    
    protected String iconResource() {
        return "org/netbeans/modules/j2ee/sun/ide/resources/ServerInstanceIcon.png";
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
                    File archive = FileUtil.toFile(fo);
                    result = null != archive;
                }
            }
        }
        return result;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    
}
