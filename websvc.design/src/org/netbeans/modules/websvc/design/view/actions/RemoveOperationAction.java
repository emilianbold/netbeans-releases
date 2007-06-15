/*
 * RemoveOperationAction.java
 *
 * Created on April 6, 2007, 10:25 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.view.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.core.MethodGenerator;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.schema2java.OperationGeneratorHelper;
import org.netbeans.modules.websvc.design.util.WSDLUtils;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 *
 * @author rico
 */
public class RemoveOperationAction extends AbstractAction{
    
    private MethodModel method;
    private Service service;
    private FileObject implementationClass;
    private File wsdlFile;
    private String methodName;
    
    /** Creates a new instance of RemoveOperationAction */
    public RemoveOperationAction(Service service, MethodModel method) {
        super(getName());
        this.service = service;
        this.method = method;
        this.implementationClass = getImplementationClass(service.getLocalWsdlFile() != null);
        this.wsdlFile = getWSDLFile();
        this.methodName = method.getOperationName();
    }
    
    public void actionPerformed(ActionEvent arg0) {
        NotifyDescriptor desc = new NotifyDescriptor.Confirmation
                (NbBundle.getMessage(RemoveOperationAction.class, "MSG_OPERATION_DELETE", methodName));
        Object retVal = DialogDisplayer.getDefault().notify(desc);
        if (retVal == NotifyDescriptor.YES_OPTION) {
            final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.
                    getMessage(RemoveOperationAction.class, "MSG_RemoveOperation", methodName)); //NOI18N
            Task task = new Task(new Runnable() {
                public void run() {
                    handle.start();
                    try{
                        removeOperation();
                    }catch(IOException e){
                        handle.finish();
                        ErrorManager.getDefault().notify(e);
                    } finally{
                        handle.finish();
                    }
                }});
                RequestProcessor.getDefault().post(task);
        }
    }
    
    private void removeOperation() throws IOException{
        OperationGeneratorHelper generatorHelper = new OperationGeneratorHelper(wsdlFile);
        if(wsdlFile != null){
            WSDLModel wsdlModel = WSDLUtils.getWSDLModel(FileUtil.toFileObject(wsdlFile), true);
            //TODO: methodName should be the equivalent operation name in the WSDL
            //i.e., should look at operationName annotation if present
            generatorHelper.removeWSOperation(wsdlModel, generatorHelper.
                    getPortTypeName(implementationClass), methodName);
            generatorHelper.generateJavaArtifacts(service.getName(), implementationClass, methodName, true);
            
        } else{
            //WS from Java
            MethodGenerator.deleteMethod(implementationClass, methodName);
        }
        //save the changes so events will be fired
        
        DataObject dobj = DataObject.find(implementationClass);
        if(dobj.isModified()) {
            SaveCookie cookie = dobj.getCookie(SaveCookie.class);
            if(cookie!=null) cookie.save();
        }
    }
    
    private File getWSDLFile(){
        String localWsdlUrl = service.getLocalWsdlFile();
        if (localWsdlUrl!=null) { //WS from e
            JAXWSSupport support = JAXWSSupport.getJAXWSSupport(implementationClass);
            if (support!=null) {
                FileObject localWsdlFolder = support.getLocalWsdlFolderForService(service.getName(),false);
                if (localWsdlFolder!=null) {
                    File wsdlFolder = FileUtil.toFile(localWsdlFolder);
                    return  new File(wsdlFolder.getAbsolutePath()+File.separator+localWsdlUrl);
                }
            }
        }
        return null;
    }
    
    private static String getName() {
        return NbBundle.getMessage(RemoveOperationAction.class, "LBL_RemoveOperation");
    }
    
    private FileObject getImplementationClass(boolean fromWSDL){
        FileObject implementationClass= null;
        FileObject classFO = method.getImplementationClass();
        String implClassName = service.getImplementationClass();
        if(fromWSDL){
            Project project = FileOwnerQuery.getOwner(classFO);
            SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if(sgs.length > 0){
                ClassPath classPath = null;
                for(int i = 0; i < sgs.length; i++){
                    classPath = ClassPath.getClassPath(sgs[i].getRootFolder(),ClassPath.SOURCE);
                    if(classPath != null){
                        implementationClass = classPath.findResource(implClassName.replace('.', '/') + ".java");
                        if(implementationClass != null){
                            break;
                        }
                    }
                }
            }
        }else{
            implementationClass = classFO;
        }
        return implementationClass;
    }
    
}
