/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
/*
 * VerifierImpl.java
 *
 * Created on December 8, 2004, 2:54 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.netbeans.modules.glassfish.eecommon.api.ExecSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.modules.j2ee.deployment.common.api.ValidationException;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.FileOwnerQuery;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.swing.SwingUtilities;
import org.netbeans.modules.glassfish.eecommon.api.VerifierSupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

import org.netbeans.modules.j2ee.sun.api.InstrumentAVK; 
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Appclient;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Application;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Connector;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Ejb;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.StaticVerification;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Web;
import org.netbeans.modules.j2ee.sun.ide.dm.SunDeploymentFactory;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author ludo
 */
public class VerifierImpl extends org.netbeans.modules.j2ee.deployment.plugins.spi.VerifierSupport {

    public static class VerifierToolSupport extends VerifierSupport {
        /**
         * Creates a new instance of verifier tool support.
         * @param archiveName
         */
        public VerifierToolSupport(String archiveName) {
            super(archiveName);
        }
    /**
     *
     * @param fileName
     * @param outs
     * @param irf
     */
    public static  void launchVerifier(final String fileName, OutputStream outs, File irf){
        final File f = new File(fileName);
        final File dir = f.getParentFile();
        final VerifierSupport verifierSupport=new VerifierSupport(fileName);
        
        //File irf = org.netbeans.modules.j2ee.sun.api.ServerLocationManager.getLatestPlatformLocation();
        if (null == irf || !irf.exists()) {
            //org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Util.showWarning(NbBundle.getMessage(VerifierSupport.class, "ERR_CannotFind"));// NOI18N
            return;
        }
        String installRoot = irf.getAbsolutePath();
        
        try{
            String cmd = installRoot+File.separator+"bin"+File.separator+"verifier";//NOI18N
            if (File.separatorChar != '/') {
                cmd =cmd + ".bat";      // NOI18N
            }
            File verifierFile = new File(cmd);
            if (!verifierFile.exists()) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(VerifierSupport.class, "MSG_INSTALL_VERIFIER")); // NOI18N
                DialogDisplayer.getDefault().notify(nd);
                return;
            } else {
                SwingUtilities.invokeLater( new Runnable(){
                    @Override
                    public void run() {
                        verifierSupport.initUI();
                        verifierSupport.showInMode();
                    }
                });
                Runtime rt = Runtime.getRuntime();
                String arr[] = {cmd, "-ra", "-d" , dir.getAbsolutePath(), fileName};//NOI18N

                String cmdName="";      // NOI18N
                for (int j=0;j<arr.length;j++){
                    cmdName= cmdName+arr[j]+" ";        // NOI18N
                }
                System.out.println(NbBundle.getMessage(VerifierSupport.class,"running_", cmdName));     // NOI18N
                final Process child = rt.exec(arr);

                //
                // Attach to the process's stdout, and ignore what comes back.
                //
                final Thread[] copyMakers = new Thread[2];
                OutputStreamWriter oss=null;
                if (outs!=null) {
                    oss=new OutputStreamWriter(outs);
                }
                (copyMakers[0] = new ExecSupport.OutputCopier(new InputStreamReader(child.getInputStream()), oss, true)).start();
                (copyMakers[1] = new ExecSupport.OutputCopier(new InputStreamReader(child.getErrorStream()), oss, true)).start();
                try {
                    child.waitFor();
                    Thread.sleep(1000);  // time for copymakers
                } catch (InterruptedException e) {
                } finally {
                    try {
                        copyMakers[0].interrupt();
                        copyMakers[1].interrupt();
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String onlyJarFile   = f.getName();
        File ff = new File(dir, onlyJarFile+".xml");        // NOI18N
        org.netbeans.modules.j2ee.sun.dd.impl.verifier.Error err = null;
        if (!ff.exists()) {
            err = StaticVerification.createGraph().newError();
            err.setErrorName(NbBundle.getMessage(VerifierSupport.class,"ERR_PARSING_OUTPUT"));  // NOI18N
            err.setErrorDescription(NbBundle.getMessage(VerifierSupport.class,"ERR_NO_OUTPUT_TO_PARSE", ff));
            verifierSupport.saveErrorResultsForDisplay( err);
            verifierSupport.setVerifierIsStillRunning(false);// we are done
            verifierSupport.updateDisplay();
            return;
        }
        FileInputStream in = null;
        StaticVerification sv = null;
        try {
            in = new FileInputStream(ff);
            
            sv = StaticVerification.createGraph(in);  // this can throw a RT exception
            err = sv.getError();
            if (err!=null){
                verifierSupport.saveErrorResultsForDisplay( err);
                
            }
            Ejb e = sv.getEjb();
            if (e!=null){
                Failed fail= e.getFailed();
                if (fail!=null){
                    Test t[] =fail.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveFailResultsForDisplay( t[i]);
                    }
                }
                Warning w= e.getWarning();
                if (w!=null){
                    Test t[] =w.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveWarnResultsForDisplay( t[i]);
                    }
                }
                Passed p= e.getPassed();
                if (p!=null){
                    Test t[] =p.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.savePassResultsForDisplay(t[i]);
                    }
                }
                NotApplicable na= e.getNotApplicable();
                if (na!=null){
                    Test t[] =na.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveNaResultsForDisplay( t[i]);
                    }
                }
            }
            Web we = sv.getWeb();
            if (we!=null){
                Failed fail= we.getFailed();
                if (fail!=null){
                    Test t[] =fail.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveFailResultsForDisplay(t[i]);
                    }
                }
                Warning w= we.getWarning();
                if (w!=null){
                    Test t[] =w.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveWarnResultsForDisplay(t[i]);
                    }
                }
                Passed p= we.getPassed();
                if (p!=null){
                    Test t[] =p.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.savePassResultsForDisplay(t[i]);
                    }
                }
                NotApplicable na= we.getNotApplicable();
                if (na!=null){
                    Test t[] =na.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveNaResultsForDisplay(t[i]);
                    }
                }
            }
            Appclient ac = sv.getAppclient();
            if (ac!=null){
                Failed fail= ac.getFailed();
                if (fail!=null){
                    Test t[] =fail.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveFailResultsForDisplay(t[i]);
                    }
                }
                Warning w= ac.getWarning();
                if (w!=null){
                    Test t[] =w.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveWarnResultsForDisplay(t[i]);
                    }
                }
                Passed p= ac.getPassed();
                if (p!=null){
                    Test t[] =p.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.savePassResultsForDisplay(t[i]);
                    }
                }
                NotApplicable na= ac.getNotApplicable();
                if (na!=null){
                    Test t[] =na.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveNaResultsForDisplay(t[i]);
                    }
                }
            }
            Application  app = sv.getApplication();
            if (app!=null){
                Failed fail= app.getFailed();
                if (fail!=null){
                    Test t[] =fail.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveFailResultsForDisplay(t[i]);
                    }
                }
                Warning w= app.getWarning();
                if (w!=null){
                    Test t[] =w.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveWarnResultsForDisplay(t[i]);
                    }
                }
                Passed p= app.getPassed();
                if (p!=null){
                    Test t[] =p.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.savePassResultsForDisplay(t[i]);
                    }
                }
                NotApplicable na= app.getNotApplicable();
                if (na!=null){
                    Test t[] =na.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveNaResultsForDisplay(t[i]);
                    }
                }
            }
            Connector rar = sv.getConnector();
            if (rar!=null){
                Failed fail= rar.getFailed();
                if (fail!=null){
                    Test t[] =fail.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveFailResultsForDisplay(t[i]);
                    }
                }
                Warning w= rar.getWarning();
                if (w!=null){
                    Test t[] =w.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveWarnResultsForDisplay(t[i]);
                    }
                }
                Passed p= rar.getPassed();
                if (p!=null){
                    Test t[] =p.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.savePassResultsForDisplay(t[i]);
                    }
                }
                NotApplicable na= rar.getNotApplicable();
                if (na!=null){
                    Test t[] =na.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveNaResultsForDisplay(t[i]);
                    }
                }
            }
        } catch (RuntimeException rte) {
            err = StaticVerification.createGraph().newError();
            err.setErrorName(NbBundle.getMessage(VerifierSupport.class,"ERR_PARSING_OUTPUT"));  // NOI18N
            err.setErrorDescription(rte.getMessage());
            if (rte.getMessage().indexOf("error-name") > -1) {
                // TODO do the reparse, correct error-name and error-description
                // currently, tell user to look in the output window
                err.setErrorDescription(NbBundle.getMessage(VerifierSupport.class,"READ_OUTPUT_WINDOW"));   // NOI18N
            }
            verifierSupport.saveErrorResultsForDisplay( err);
        } catch (IOException ioe){
            ioe.printStackTrace();
            err = StaticVerification.createGraph().newError();
            err.setErrorName(NbBundle.getMessage(VerifierSupport.class,"ERR_PARSING_OUTPUT"));  // NOI18N
            err.setErrorDescription(ioe.getMessage());
            verifierSupport.saveErrorResultsForDisplay( err);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    // I cannot do anything here...
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                }
            }
        }
        verifierSupport.setVerifierIsStillRunning(false);// we are done
        verifierSupport.updateDisplay();
    }
    

    }
    /** Creates a new instance of VerifierImpl */
    public VerifierImpl() {
    }
    /**
     * Verify the provided target J2EE module or application, including both
     * standard J2EE and platform specific deployment info.  The provided
     * service could include invoking its own specific UI displaying of verification
     * result. In this case, the service could have limited or no output to logger stream.
     *
     * @param target The an archive, directory or file to verify.
     * @param logger Log stream to write verification output to.
     * @exception ValidationException if the target fails the validation.
     */
    public void verify(FileObject target, OutputStream logger) throws ValidationException {
        //System.out.println("In Verifier...."+target);
        final String jname = FileUtil.toFile(target).getAbsolutePath();
        DeploymentManager dm;
        try {
            dm = getAssociatedSunDM(target);
        SunDeploymentManagerInterface sdm = (SunDeploymentManagerInterface)dm;
        InstrumentAVK avkSupport = getAVKImpl();
            if ((avkSupport != null) && (dm != null) && sdm.isLocal()) {
                J2eeModuleProvider modProvider = getModuleProvider(target);
                boolean verificationType = avkSupport.createAVKSupport(dm, modProvider);
                if (verificationType) {
                    VerifierToolSupport.launchVerifier(jname, logger, sdm.getPlatformRoot());
                }
            } else {
                VerifierToolSupport.launchVerifier(jname, logger, sdm.getPlatformRoot());
            }
        } catch (DeploymentManagerCreationException ex) {
            ValidationException ve = new ValidationException("Bad DM");
            ve.initCause(ex);
            throw ve;
        }
    }
    
    private DeploymentManager getAssociatedSunDM(FileObject target) throws DeploymentManagerCreationException{
        DeploymentManager dm = null;
        J2eeModuleProvider modProvider = getModuleProvider(target);
        if (modProvider != null){
            InstanceProperties serverName = modProvider.getInstanceProperties();
            dm = (new SunDeploymentFactory()).getDisconnectedDeploymentManager(serverName.getProperty(InstanceProperties.URL_ATTR)); // serverName.getDeploymentManager();
        }
        return dm;
    }

    private InstrumentAVK getAVKImpl(){
        InstrumentAVK avkSupport = AVKLayerUtil.getAVKImplemenation();
        return avkSupport;
    }
    
    private J2eeModuleProvider getModuleProvider(FileObject target){
        J2eeModuleProvider modProvider = null;
        Project holdingProj = FileOwnerQuery.getOwner(target);
        if (holdingProj != null){
            modProvider = (J2eeModuleProvider) holdingProj.getLookup().lookup(J2eeModuleProvider.class);
        }
        return modProvider;
    }

}

