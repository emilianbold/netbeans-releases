/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.CCCCompiler;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSets;
import org.netbeans.modules.cnd.makeproject.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerRootNodeProvider;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.runprofiles.RunProfileNodeProvider;
import org.netbeans.modules.cnd.makeproject.runprofiles.RunProfileProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.OperationEvent;
import org.openide.loaders.OperationListener;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.modules.ModuleInstall;

public class MakeProjectModule extends ModuleInstall {

    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(MakeProjectModule.class.getName());
        
    private ActionWrapper debugWrapper = null;
    private ActionWrapper runWrapper = null;
    private CustomizerNode profileCustomizerNode;
    
    public void restored() {
	RunProfileProvider profileProvider = new RunProfileProvider();
	ConfigurationDescriptorProvider.addAuxObjectProvider(profileProvider);
	profileCustomizerNode = new RunProfileNodeProvider().createProfileNode();
	CustomizerRootNodeProvider.getInstance().addCustomizerNode(profileCustomizerNode);

	//see issue #64393
        DataLoaderPool.getDefault().addOperationListener(new L());
    }
    
    public void close() {
        for (int i = 0; i < CompilerSets.getCompilerSets().length; i++) {
            CompilerSet compilerCollection = CompilerSets.getCompilerSets()[i];
            Tool[] tools = compilerCollection.getTools();
            for (int j = 0; j < tools.length; j++) {
                if (tools[j] instanceof CCCCompiler) { // FIXUP: should implement/use 'capability' of tool
                    ((CCCCompiler)(tools[j])).saveSystemIncludesAndDefines();
                }
            }
        }
    }
            
    public static class ActionWrapper extends CallableSystemAction implements ContextAwareAction, PropertyChangeListener {
        
        private Action action;
        
        public ActionWrapper( Action action ) {
            this.action = action;            
        }
            
        public String getName() {
            return (String)action.getValue( Action.NAME );
        }

        public String iconResource() {
            return null;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        protected boolean asynchronous() {
            return false;
        }

        public void actionPerformed( ActionEvent ev ) {
            action.actionPerformed(ev);
        }
        
        public boolean isEnabled() {
            return action.isEnabled();            
        }

        protected void addNotify() {
            this.action.addPropertyChangeListener( this );
            super.addNotify();
        }
        
        protected void removeNotify() {
            this.action.removePropertyChangeListener( this );
            super.removeNotify();
        }
        
        public void performAction() {
            actionPerformed( new ActionEvent( this, 0, "" ) ); // NOI18N
        }
        
        public Action createContextAwareInstance( Lookup actionContext ) {
            return ((ContextAwareAction)action).createContextAwareInstance( actionContext );
        }
        
        public void propertyChange( PropertyChangeEvent evt ) {
            firePropertyChange( evt.getPropertyName(), evt.getOldValue(), evt.getNewValue() );
        }
        
        
    }
    
    
    public static class CompileWrapper extends ActionWrapper {
        
        CompileWrapper() {
            super( FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_COMPILE_SINGLE, 
                       NbBundle.getMessage( MakeProjectModule.class, "LBL_CompileFile_Action" ), // NOI18N
                       null ) );
        }
        
    }
    
    public static class RunWrapper extends ActionWrapper {
        RunWrapper() {
            super( FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_RUN_SINGLE, 
                       NbBundle.getMessage( MakeProjectModule.class, "LBL_RunFile_Action" ), // NOI18N
                       null ) );
            
        }
    }
    
    public static class DebugWrapper extends ActionWrapper {
        DebugWrapper() {
            super( FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_DEBUG_SINGLE, 
                       NbBundle.getMessage( MakeProjectModule.class, "LBL_DebugFile_Action" ), // NOI18N
                       null ) );
        }
    }
    
    /**See issue #64393
     */
    private static class L implements OperationListener {
        
        public L() {
        }
        
        public void operationPostCreate(OperationEvent operationEvent) {
        }

        public void operationCopy(OperationEvent.Copy copy) {
        }

        public void operationMove(OperationEvent.Move move) {
        }

        public void operationDelete(OperationEvent operationEvent) {
        }

        public void operationRename(OperationEvent.Rename rename) {
        }

        public void operationCreateShadow(OperationEvent.Copy copy) {
        }

        private MakeConfigurationDescriptor getMakeConfigurationDescriptor(Project p) {
            ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)p.getLookup().lookup(ConfigurationDescriptorProvider.class );
            
            if (pdp == null) {
                return null;
            }
            
            return (MakeConfigurationDescriptor)pdp.getConfigurationDescriptor();
        }
        
        public void operationCreateFromTemplate(OperationEvent.Copy copy) {
            Folder  folder = (Folder) Utilities.actionsGlobalContext().lookup(Folder.class);
            Project p      = (Project) Utilities.actionsGlobalContext().lookup(Project.class);
            
            if (folder == null || p == null) {
                //maybe a file belonging into a project is selected. Try:
                DataObject od = (DataObject) Utilities.actionsGlobalContext().lookup(DataObject.class);
                
                if (od == null) {
                    //no file:
                    return ;
                }
                
                FileObject file = od.getPrimaryFile();
                
                p = FileOwnerQuery.getOwner(file);
                
                if (p == null) {
                    //no project:
                    return ;
                }
                
                File f = FileUtil.toFile(file);
                
                if (f == null) {
                    //not a physical file:
                    return ;
                }
                
                //check if the project is a Makefile project:
                MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor(p);
                
                if (makeConfigurationDescriptor == null) {
                    //no:
                    return ;
                }
                
                Item i = makeConfigurationDescriptor.findProjectItemByPath(f.getAbsolutePath());
                
                if (i == null) {
                    //no item, does not really belong into this project:
                    return ;
                }
                
                //found:
                folder = i.getFolder();
            }
            
            MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor(p);
                
            assert makeConfigurationDescriptor != null;
                
            FileObject file = copy.getObject().getPrimaryFile();
            Project owner = FileOwnerQuery.getOwner(file);
            
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "processing file=" + file); // NOI18N
                ERR.log(ErrorManager.INFORMATIONAL, "FileUtil.toFile(file.getPrimaryFile())=" + FileUtil.toFile(file)); // NOI18N
                ERR.log(ErrorManager.INFORMATIONAL, "into folder = " + folder); // NOI18N
                ERR.log(ErrorManager.INFORMATIONAL, "in project = " + p.getProjectDirectory()); // NOI18N
            }
            
            if (owner != null && owner.getProjectDirectory() == p.getProjectDirectory()) {
                File ioFile = FileUtil.toFile((FileObject) file);
                if (ioFile.isDirectory())
                    return; // don't add directories. 
                String itemPath = IpeUtils.toAbsoluteOrRelativePath(makeConfigurationDescriptor.getBaseDir(), ioFile.getPath());
                itemPath = FilePathAdaptor.mapToRemote(itemPath);
                itemPath = FilePathAdaptor.normalize(itemPath);
                Item item = new Item(itemPath);

                folder.addItem(item);
                
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log(ErrorManager.INFORMATIONAL, "folder: " + folder + ", added: " + file); // NOI18N
                }
            } else {
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log(ErrorManager.INFORMATIONAL, "not adding: " + file + " because it is not owned by this project"); // NOI18N
                }
            }
        }
        
    }
    
}
