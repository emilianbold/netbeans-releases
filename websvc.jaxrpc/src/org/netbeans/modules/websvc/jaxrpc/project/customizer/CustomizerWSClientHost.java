/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.websvc.jaxrpc.project.customizer;
import java.util.List;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;


/** Host for WsCompile features editor for editing the features enabled for
 *  running WsCompile on a web service or a web service client.
 *
 *  property format: 'webservice.client.[servicename].features=xxx,yyy,zzz
 *
 * @author Peter Williams
 */
public class CustomizerWSClientHost extends javax.swing.JPanel implements PropertyChangeListener, HelpCtx.Provider {
    
    private WsCompileClientEditorSupport.Panel wsCompileEditor;
    private Project project;
    
    private List serviceSettings;
    
    public CustomizerWSClientHost( Project project, List serviceSettings) {
        assert serviceSettings != null;
        initComponents();
        
        this.wsCompileEditor = null;
        this.serviceSettings = serviceSettings;
        this.project = project;
        
        if (serviceSettings.size() > 0)
            initValues();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.BorderLayout());

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    public void addNotify() {
        super.addNotify();
        
        JPanel component = wsCompileEditor.getComponent();
        
        removeAll(); // !PW is this necessary?
        add(component);
        
        component.addPropertyChangeListener(WsCompileClientEditorSupport.PROP_FEATURES_CHANGED, this);
        component.addPropertyChangeListener(WsCompileClientEditorSupport.PROP_OPTIONS_CHANGED, this);
    }
    
    public void removeNotify() {
        super.removeNotify();
        
        JPanel component = wsCompileEditor.getComponent();
        component.removePropertyChangeListener(WsCompileClientEditorSupport.PROP_FEATURES_CHANGED, this);
        component.removePropertyChangeListener(WsCompileClientEditorSupport.PROP_OPTIONS_CHANGED, this);
    }
    
    public void initValues() {
        if (wsCompileEditor == null) {
            WsCompileClientEditorSupport editorSupport = (WsCompileClientEditorSupport) Lookup.getDefault().lookup(WsCompileClientEditorSupport.class);
            wsCompileEditor = editorSupport.getWsCompileSupport();
        }
        
        wsCompileEditor.initValues(serviceSettings);
    }
    
    
    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        EditableProperties ep = null;
        try{
            ep = getEditableProperties(project, AntProjectHelper.PROJECT_PROPERTIES_PATH );
        } catch(IOException e){
            ErrorManager.getDefault().notify(e);
        }
        if(ep == null) return;
        boolean changed = false;
        
        if (WsCompileClientEditorSupport.PROP_FEATURES_CHANGED.equals(prop)) {
            WsCompileClientEditorSupport.FeatureDescriptor newFeatureDesc = (WsCompileClientEditorSupport.FeatureDescriptor) evt.getNewValue();
            String propertyName = "wscompile.client." + newFeatureDesc.getServiceName() + ".features";
            ep.put(propertyName, newFeatureDesc.getFeatures());
            changed = true;
        } else if (WsCompileClientEditorSupport.PROP_OPTIONS_CHANGED.equals(prop)) {
            WsCompileClientEditorSupport.OptionDescriptor oldOptionDesc = (WsCompileClientEditorSupport.OptionDescriptor) evt.getOldValue();
            WsCompileClientEditorSupport.OptionDescriptor newOptionDesc = (WsCompileClientEditorSupport.OptionDescriptor) evt.getNewValue();
            boolean[] oldOptions = oldOptionDesc.getOptions();
            boolean[] newOptions = newOptionDesc.getOptions();
            String serviceName = newOptionDesc.getServiceName();
            String[] propertyNames=new String[]{"verbose","debug","xPrintStackTrace","xSerializable","optimize"}; //NOI18N
            for (int i=0;i<newOptions.length;i++) {
                if (oldOptions[i]!=newOptions[i]) {
                    ep.put("wscompile.client."+serviceName+"."+propertyNames[i],        //NOI18N
                            newOptions[i]?"true":"false");  //NOI18N
                    if(changed == false) changed = true;
                }
            }
        }
        if(changed){
            try {
                storeEditableProperties(project, org.netbeans.spi.project.support.ant.AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                org.netbeans.api.project.ProjectManager.getDefault().saveProject(project);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
    private static EditableProperties getEditableProperties(final Project prj,final  String propertiesPath)
            throws IOException {
        try {
            return
                    ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<EditableProperties>() {
                public EditableProperties run() throws IOException {
                    FileObject propertiesFo = prj.getProjectDirectory().getFileObject(propertiesPath);
                    EditableProperties ep = null;
                    if (propertiesFo!=null) {
                        InputStream is = null;
                        ep = new EditableProperties();
                        try {
                            is = propertiesFo.getInputStream();
                            ep.load(is);
                        } finally {
                            if (is!=null) is.close();
                        }
                    }
                    return ep;
                }
            });
        } catch (MutexException ex) {
            return null;
        }
    }
    
    private static void storeEditableProperties(final Project prj, final  String propertiesPath, final EditableProperties ep)
            throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    FileObject propertiesFo = prj.getProjectDirectory().getFileObject(propertiesPath);
                    if (propertiesFo!=null) {
                        OutputStream os = null;
                        try {
                            os = propertiesFo.getOutputStream();
                            ep.store(os);
                        } finally {
                            if (os!=null) os.close();
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException ex) {
        }
    }
    
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerWSClientHost.class);
    }
    
}
