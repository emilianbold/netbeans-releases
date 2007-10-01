/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.editor.options;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import org.netbeans.modules.editor.NbEditorSettingsInitializer;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.nodes.BeanNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Node representing the Editor Settings main node.
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public class AllOptionsNode extends FilterNode {
    
    private static final String HELP_ID = "editing.global"; // !!! NOI18N
    
    /** Creates new AllOptionsNode as BeanNode with Children.Array */
    public AllOptionsNode() throws IntrospectionException {
        super(new BeanNode(AllOptionsFolder.getDefault()), new EditorSubnodes());
        NbEditorSettingsInitializer.init();
    }
    
    /** Gets display name of all options node from bundle */
    public String getDisplayName(){
        return NbBundle.getMessage(AllOptionsNode.class, "OPTIONS_all"); //NOI18N
    }

    public String getShortDescription(){
        return null;
    }
    
    // #7925
    public boolean canDestroy() {
        return false;
    }        
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx (HELP_ID);
    }
    
    /** Class representing subnodes of Editor Settings node.*/
    private static class EditorSubnodes extends Children.Keys {

        /** Listens to changes on the Modules folder */
        private FileChangeListener moduleRegListener;
        
        /** Constructor.*/
        EditorSubnodes() {
            super();
        }        
        
        private void mySetKeys() {
            setKeys(AllOptionsFolder.getDefault().getInstalledOptions());
        }
        
        /** Called to notify that the children has lost all of its references to
         * its nodes associated to keys and that the keys could be cleared without
         * affecting any nodes (because nobody listens to that nodes). 
         * Overrides superclass method. */
        protected void removeNotify () {
            setKeys(new ArrayList());
        }
        
        /** Called to notify that the children has been asked for children
         * after and that they should set its keys. Overrides superclass method. */
        protected void addNotify() {
            mySetKeys();
            
            // listener
            if(moduleRegListener == null) {
                moduleRegListener = new FileChangeAdapter() {
                    public void fileChanged(FileEvent fe){
                        mySetKeys();
                    }
                };
                
                FileObject moduleRegistry = Repository.getDefault().getDefaultFileSystem().findResource("Modules"); //NOI18N
                
                if (moduleRegistry !=null){ //NOI18N
                    moduleRegistry.addFileChangeListener(
                        FileUtil.weakFileChangeListener(moduleRegListener, moduleRegistry));
                }
            }
        }
       
        
        /** Create nodes for a given key.
         * @param key the key
         * @return child nodes for this key or null if there should be no
         *   nodes for this key
         */
        protected Node[] createNodes(Object key) {
            if(key == null)
                return null;

            if(!(key instanceof Class))
                return null;            
            
            BaseOptions baseOptions
            = (BaseOptions)BaseOptions.findObject((Class)key, true);
            
            if (baseOptions == null) return null;
            
            return new Node[] {baseOptions.getMimeNode()/* #18678 */.cloneNode()};
        }
        
    }
    
}
