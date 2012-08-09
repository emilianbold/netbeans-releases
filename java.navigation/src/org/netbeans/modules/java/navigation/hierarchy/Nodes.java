/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.navigation.hierarchy;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
class Nodes {

    @StaticResource
    private static final String ICON = "org/netbeans/modules/java/navigation/resources/wait.gif";   //NOI18N
    private static final WaitNode WAIT_NODE = new WaitNode();

    
    private Nodes() {
        throw new IllegalStateException();
    }

    static Node waitNode() {
        return WAIT_NODE;
    }

    static Node superTypeHierarchy(
            @NonNull final DeclaredType type,
            @NonNull final Context ctx) {
        final TypeElement element = (TypeElement)type.asElement();
        final TypeMirror superClass = element.getSuperclass();
        final List<? extends TypeMirror> interfaces = element.getInterfaces();
        final List<Node> childNodes = new ArrayList<Node>(interfaces.size()+1);
        if (superClass.getKind() != TypeKind.NONE) {
            childNodes.add(superTypeHierarchy((DeclaredType)superClass, ctx));
        }
        for (TypeMirror superInterface : interfaces) {
            childNodes.add(superTypeHierarchy((DeclaredType)superInterface, ctx));
        }
        final Children cld;
        if (childNodes.isEmpty()) {
            cld = Children.LEAF;
        } else {
            cld = new SuperTypeChildren(ctx);
            cld.add(childNodes.toArray(new Node[childNodes.size()]));
        }
        return new TypeNode(
            cld,
            ElementHandle.create(element),
            ctx);
        
    }


    static interface Context {
        String PROP_FQN = "fqn";    //NOI8N
        String PROP_ORDERED = "ordered";    //NOI18N
        boolean isFQN();
        boolean isOrdered();
        void addPropertyChangeListener(@NonNull PropertyChangeListener listener);
        void removePropertyChangeListener(@NonNull PropertyChangeListener listener);
    }
    
    private static class WaitNode extends AbstractNode {
        @NbBundle.Messages({
            "LBL_PleaseWait=Please Wait..."
        })
        WaitNode() {
            super(Children.LEAF);
            setIconBaseWithExtension(ICON);
            setDisplayName(Bundle.LBL_PleaseWait());
        }
    }


    private static class TypeNode extends AbstractNode implements PropertyChangeListener {

        private final ElementHandle<TypeElement> handle;
        private final Context ctx;

        TypeNode(
            @NonNull final Children cld,
            @NonNull final ElementHandle<TypeElement> handle,
            @NonNull final Context ctx) {
            super(cld);
            assert handle != null;
            assert ctx != null;
            this.handle = handle;
            this.ctx = ctx;
            this.ctx.addPropertyChangeListener(this);
            updateDisplayName();
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.icon2Image(ElementIcons.getElementIcon(handle.getKind(), EnumSet.noneOf(Modifier.class)));
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (Context.PROP_FQN.equals(evt.getPropertyName())) {
                updateDisplayName();
            }
        }

        private void updateDisplayName() {
            String name = handle.getQualifiedName();
            if (!ctx.isFQN()) {
                name = HierarchyHistory.getSimpleName(name);
            }
            setDisplayName(name);
        }

    }

    private static class SuperTypeChildren extends Children.SortedArray implements PropertyChangeListener {

        private final Context ctx;

        SuperTypeChildren(@NonNull final Context ctx) {
            assert ctx != null;
            this.ctx = ctx;
            this.ctx.addPropertyChangeListener(this);
            updateComparator();
        }


        private void updateComparator() {
            if (ctx.isOrdered()) {
                setComparator(new LexicographicComparator());
            } else {
                setComparator(null);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (Context.PROP_ORDERED.equals(evt.getPropertyName())) {
                updateComparator();
            }
        }

    }

    private static final class LexicographicComparator implements Comparator<Node> {

        LexicographicComparator() {
        }

        @Override
        public int compare(Node n1, Node n2) {
            return n1.getDisplayName().compareTo(n2.getDisplayName());
        }
    }
}
