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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.editor.NbEditorSettingsInitializer;
import org.netbeans.modules.editor.lib.KitsTracker;
import org.openide.nodes.BeanNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/** Node representing the Editor Settings main node.
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 *  @deprecate If you think you need this class you are doing something wrong. It should've never been made public.
 */
public class AllOptionsNode extends FilterNode {
    
    private static final String HELP_ID = "editing.global"; // !!! NOI18N
    
    /** Creates new AllOptionsNode as BeanNode with Children.Array */
    public AllOptionsNode() throws IntrospectionException {
        super(new BeanNode(AllOptionsFolder.getDefault()), new EditorSubnodes());
        NbEditorSettingsInitializer.init();
    }
    
    /** Gets display name of all options node from bundle */
    public @Override String getDisplayName(){
        return NbBundle.getMessage(AllOptionsNode.class, "OPTIONS_all"); //NOI18N
    }

    public @Override String getShortDescription(){
        return null;
    }
    
    // #7925
    public @Override boolean canDestroy() {
        return false;
    }        
    
    public @Override HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }
    
    /** Class representing subnodes of Editor Settings node.*/
    private static class EditorSubnodes extends Children.Keys implements PropertyChangeListener {

        private PropertyChangeListener weakListener = null;
        
        /** Constructor.*/
        public EditorSubnodes() {
        }        
        
        /** Called to notify that the children has lost all of its references to
         * its nodes associated to keys and that the keys could be cleared without
         * affecting any nodes (because nobody listens to that nodes). 
         * Overrides superclass method. */
        protected @Override void removeNotify () {
            if (weakListener != null) {
                KitsTracker.getInstance().removePropertyChangeListener(weakListener);
                weakListener = null;
            }
            setKeys(Collections.EMPTY_SET);
        }
        
        /** Called to notify that the children has been asked for children
         * after and that they should set its keys. Overrides superclass method. */
        protected @Override void addNotify() {
            setKeys(KitsTracker.getInstance().getMimeTypes());
            if (weakListener != null) {
                weakListener = WeakListeners.propertyChange(this, KitsTracker.getInstance());
                KitsTracker.getInstance().addPropertyChangeListener(weakListener);
            }
        }
       
        
        /** Create nodes for a given key.
         * @param key the key
         * @return child nodes for this key or null if there should be no
         *   nodes for this key
         */
        protected Node[] createNodes(Object key) {
            if (key instanceof String) {
                String mimeType = (String) key;
                String genericMimeType = KitsTracker.getGenericPartOfCompoundMimeType(mimeType);
                
                BaseOptions baseOptions = MimeLookup.getLookup(mimeType).lookup(BaseOptions.class);
                
                if (baseOptions != null) {
                    if (genericMimeType != null) {
                        BaseOptions gBO = MimeLookup.getLookup(genericMimeType).lookup(BaseOptions.class);
                        if (gBO == baseOptions) {
                            // Options for the compound mime type were inherited
                            // from the generic mime type. Ignore them, we will show them
                            // for the generic mime type.
                            baseOptions = null;
                        }
                    }
                }
                
                if (baseOptions != null) {
                    baseOptions.loadXMLSettings();
                    Node node = baseOptions.getMimeNode();
                    if (node != null) {
                        return new Node[] { node.cloneNode() };
                    }
                }
            }
            return null;
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            setKeys(KitsTracker.getInstance().getMimeTypes());
        }
    } // End of EditorSubnodes class
    
}
