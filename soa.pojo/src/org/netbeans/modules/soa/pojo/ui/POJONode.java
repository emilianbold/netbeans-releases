/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.pojo.ui;

import java.awt.Image;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.pojo.schema.POJOProvider;
import org.netbeans.modules.soa.pojo.ui.actions.OpenJavaFileAction;
import org.netbeans.modules.soa.pojo.ui.actions.POJODisableWSDLRefreshOnBuild;
import org.netbeans.modules.soa.pojo.ui.actions.POJOEnableWSDLRefreshOnBuild;
import org.netbeans.modules.soa.pojo.util.Util;
import org.openide.actions.PropertiesAction;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author gpatil
 * @author Sreenivasan Genipudi
 */
public class POJONode extends AbstractNode {
    private String pojoName;
    private Project project;
    private POJOProvider pjo;
    private static Action[] actions = null;
    
    public POJONode(Project project, POJOProvider pj, DataObject doObj) throws DataObjectNotFoundException {
        this(new InstanceContent(), project, pj, doObj);
    }

    private POJONode(InstanceContent ic, Project project, POJOProvider pj, DataObject doObj) throws DataObjectNotFoundException {
        super(Children.LEAF, new AbstractLookup(ic));
        ic.add(project);
        ic.add(pj);
        ic.add(doObj);
        ic.add(this);
        
        this.pojoName = pj.getClassName();
        this.project = project;
        this.pjo = pj;
        this.setShortDescription(this.pjo.getPackage() + "." + this.pojoName); //No I18N
        this.initActions();
    }
    
    @Override
    public Action[] getActions(boolean b) {
        return actions;
    }
    
    public POJOProvider getPOJO(){
        return this.pjo;
    }

    public void setPOJO(POJOProvider pojo){
        this.pjo = pojo;
    }
    
    public Project getProject(){
        return this.project;
    }    
    private void initActions() {
        if ( actions == null ) {
             actions = new Action[] {
                SystemAction.get(OpenJavaFileAction.class),
                null,
                SystemAction.get(POJOEnableWSDLRefreshOnBuild.class),
                SystemAction.get(POJODisableWSDLRefreshOnBuild.class),
                null,
                SystemAction.get(PropertiesAction.class)
            };
        }
    }
    
    @Override
    public String getDisplayName() {
        return pojoName;
    }
    
    @Override
    public boolean canDestroy(){
        return false;
    }
    
    @Override
    public void destroy() throws IOException {
        super.destroy();
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenJavaFileAction.class);
    }

    
    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage( "org/netbeans/modules/soa/pojo/resources/pojo.png" ); // No I18N
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage( "org/netbeans/modules/soa/pojo/resources/pojo.png" ); // No I18N
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();
        
        try {
            POJOProviderProxy proxy = new POJOProviderProxy(this.project, this.pjo);
            Property updWSDL = new PropertySupport.Reflection(proxy, Boolean.class, "UpdateWsdlDuringBuild");
            Property clsName = new PropertySupport.Reflection(proxy, String.class, "getClassName", null);
            Property pkgName = new PropertySupport.Reflection(proxy, String.class, "getPackage", null);

            clsName.setName(NbBundle.getBundle(this.getClass()).getString("LBL_clsName"));//NOI18N
            pkgName.setName(NbBundle.getBundle(this.getClass()).getString("LBL_pkgName"));//NOI18N
            updWSDL.setName(NbBundle.getBundle(this.getClass()).getString("LBL_updateWSDL"));//NOI18N
            
            set.put(clsName);
            set.put(pkgName);
            set.put(updWSDL);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        sheet.put(set);
        return sheet;

    }

    public static class POJOProviderProxy {
        Project prj = null;
        POJOProvider delegate = null;

        POJOProviderProxy(Project project, POJOProvider orig){
            this.delegate = orig;
            this.prj = project;
        }

        public void setUpdateWsdlDuringBuild(Boolean c){
            this.delegate.setUpdateWsdlDuringBuild(c);
            Util.changePOJOInModel(prj, delegate, delegate);
        }

        public String getClassName(){
            return this.delegate.getClassName();
        }

        public String getPackage(){
            return this.delegate.getPackage();
        }

        public Boolean getUpdateWsdlDuringBuild(){
            return Boolean.valueOf(this.delegate.isUpdateWsdlDuringBuild());
        }
    }
    
}