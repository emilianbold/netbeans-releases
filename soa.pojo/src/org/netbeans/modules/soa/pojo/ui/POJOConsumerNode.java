/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.pojo.schema.POJOConsumer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.ImageUtilities;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author sgenipudi
 */
class POJOConsumerNode extends AbstractNode {
    private Project project;
    private POJOConsumer mPojoCons;
    private Action[] actions;
    private POJOConsumerEditorDrop editorDrop;
    public POJOConsumerNode(Project prj, POJOConsumer pjcs) {
        this(prj, new InstanceContent());
        this.mPojoCons = pjcs;
        this.project = prj;
        editorDrop = new POJOConsumerEditorDrop(this);
        initActions();        
    }
    
    private POJOConsumerNode(Project prj, InstanceContent content) {
        super(Children.LEAF, 
                new AbstractLookup(content));
        // adds the node to our own lookup
        content.add (this);
        // adds additional items to the lookup
        content.add (prj);
    }
    
    @Override
    public String getDisplayName() {
        return this.mPojoCons.getInterface().toString()+"|"+this.mPojoCons.getOperation().toString();
    }
    
   
    @Override
    public Cookie getCookie(Class clz) {
        if  ( clz == Project.class ) {
            if ( project instanceof Cookie )
                return (Cookie)project;
        }
        return super.getCookie(clz);
    }
    
    
    private void initActions() {
        if ( actions == null ) {
            actions = new Action[] {
//                actions = new Action[] {
              //  SystemAction.get(POJOPackageLibrariesInSUAction.class),
              //  null,
            //    SystemAction.get(POJODisablePackageAllAction.class),

            };
        }
    }
    
    @Override
    public Action[] getActions(boolean b) {
        return actions;
    }
        
    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(
                "org/netbeans/modules/soa/pojo/resources/soa_pojo_consumer_16.png" ); // No I18N
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage(
                "org/netbeans/modules/soa/pojo/resources/soa_pojo_consumer_16.png" );// No I18N
    }
    
    POJOConsumer getConsumer() {
        return this.mPojoCons;
    }
    
@Override
    public Transferable clipboardCopy() throws IOException {

        ExTransferable t = ExTransferable.create( super.clipboardCopy() );
        ActiveEditorDropTransferable s = new ActiveEditorDropTransferable(editorDrop);
        t.put(s);

        return t;
    }

    private static class ActiveEditorDropTransferable extends ExTransferable.Single {
        
        private ActiveEditorDrop drop;

        ActiveEditorDropTransferable(ActiveEditorDrop drop) {
            super(ActiveEditorDrop.FLAVOR);
            
            this.drop = drop;
        }
               
        public Object getData () {
            return drop;
        }
        
    }    
}
