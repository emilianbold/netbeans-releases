/*
 * VerifierImpl.java
 *
 * Created on December 8, 2004, 2:54 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.net.URL;
import javax.swing.SwingUtilities;
import java.io.OutputStream;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.deployment.common.api.ValidationException;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.FileOwnerQuery;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeAppProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

import org.netbeans.modules.j2ee.sun.api.InstrumentAVK; 
import org.netbeans.modules.j2ee.sun.ide.j2ee.AVKLayerUtil;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;

/**
 *
 * @author ludo
 */
public  class VerifierImpl extends org.netbeans.modules.j2ee.deployment.plugins.api.VerifierSupport {
    
    private RequestProcessor processor = new RequestProcessor("instrument"); //NOI18N    
    
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
        DeploymentManager dm = getAssociatedSunDM(target);
        SunDeploymentManagerInterface sdm = (SunDeploymentManagerInterface)dm;
        InstrumentAVK avkSupport = getAVKImpl();
        if((avkSupport != null) && (dm != null) && sdm.isLocal()){
            AddAVKSupport support = new AddAVKSupport();
            support.createAVKSupport();
            if(support.instrumentServer()){
                instrument(dm, target);
            }else if(support.verify()){
                VerifierSupport.launchVerifier(jname, logger);
            }
        }else{
            VerifierSupport.launchVerifier(jname,logger);
        }     
    }
    
    private DeploymentManager getAssociatedSunDM(FileObject target){
        DeploymentManager dm = null;
        J2eeModuleProvider modProvider = getModuleProvider(target);
        if (modProvider != null){
            InstanceProperties serverName = modProvider.getInstanceProperties();
            dm = serverName.getDeploymentManager();
        }
        return dm;
    }

    private void instrument(final DeploymentManager dm, final FileObject target){
        processor.post(new Runnable() {
            public void run() {
                InstrumentAVK avkSupport = getAVKImpl();
                if(avkSupport != null){
                    avkSupport.setDeploymentManager((SunDeploymentManagerInterface)dm);
                    avkSupport.setAVK(true);
                }
                launchApp(dm, getContextRoot(target));
            }
        });
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
    
    private String getContextRoot(FileObject target){
        String url = null;
        J2eeModuleProvider modProvider = getModuleProvider(target);
        if(modProvider != null){
            if(modProvider instanceof J2eeAppProvider){
                //Get contextRoot of first web module in application.xml
                Application appXml = (Application)modProvider.getJ2eeModule().getDeploymentDescriptor(J2eeModule.APP_XML);
                Module[] mods = appXml.getModule();
                for(int i=0; i<mods.length; i++){
                    Web webMod = mods[i].getWeb();
                    if(webMod != null){
                        url = webMod.getContextRoot();
                        break;
                    }
                }
            }else{
                url = modProvider.getConfigSupport().getWebContextRoot();
            }
        }
        return url;
    }

    private void launchApp(DeploymentManager dm, String contextRoot){
        try{
            if(contextRoot != null){
                SunDeploymentManagerInterface sdm = (SunDeploymentManagerInterface)dm;
                DeploymentManagerProperties dmProps = new DeploymentManagerProperties(dm);
                String start = "http://" + sdm.getHost() + ":" + dmProps.getHttpPortNumber(); //NOI18N
                if (contextRoot.startsWith("/")) //NOI18N
                    contextRoot = start + contextRoot;
                else
                    contextRoot = start + "/" + contextRoot; //NOI18N
                
                URLDisplayer.getDefault().showURL(new URL(contextRoot));
            }
        }catch(Exception ex){}
    }
    
}

