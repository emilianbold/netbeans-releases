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
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.InstanceContent.Convertor;

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
            @NonNull final ClasspathInfo cpInfo,
            @NonNull final Context ctx) {
        final TypeElement element = (TypeElement)type.asElement();
        final TypeMirror superClass = element.getSuperclass();
        final List<? extends TypeMirror> interfaces = element.getInterfaces();
        final List<Node> childNodes = new ArrayList<Node>(interfaces.size()+1);
        if (superClass.getKind() != TypeKind.NONE) {
            childNodes.add(superTypeHierarchy((DeclaredType)superClass, cpInfo, ctx));
        }
        for (TypeMirror superInterface : interfaces) {
            childNodes.add(superTypeHierarchy((DeclaredType)superInterface, cpInfo, ctx));
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
            new Description(
                cpInfo,
                ElementHandle.create(element)),
            ctx,
            new Action[0]);
        
    }


    static interface Context {
        String PROP_FQN = "fqn";    //NOI8N
        String PROP_ORDERED = "ordered";    //NOI18N
        boolean isFQN();
        boolean isOrdered();
        void addPropertyChangeListener(@NonNull PropertyChangeListener listener);
        void removePropertyChangeListener(@NonNull PropertyChangeListener listener);
    }

    private static final class Description {

        private final ClasspathInfo cpInfo;
        private final ElementHandle<TypeElement> handle;


        Description(
                @NonNull final ClasspathInfo cpInfo,
                @NonNull final ElementHandle<TypeElement> handle) {
            assert cpInfo != null;
            assert handle != null;
            this.cpInfo = cpInfo;
            this.handle = handle;
        }

        ClasspathInfo getClasspathInfo() {
            return cpInfo;
        }

        ElementHandle<TypeElement> getHandle() {
            return handle;
        }

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

        private final Description description;
        private final Context ctx;
        private final Action[] globalActions;
        //@GuardedBy("this")
        private Action openAction;

        TypeNode(
            @NonNull final Children cld,
            @NonNull final Description description,
            @NonNull final Context ctx,
            @NonNull final Action[] globalActions) {
            super(cld, createLookup(description));
            assert description != null;
            assert ctx != null;
            assert globalActions != null;
            this.description = description;
            this.ctx = ctx;
            this.globalActions = globalActions;
            this.ctx.addPropertyChangeListener(this);
            updateDisplayName();
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.icon2Image(
                    ElementIcons.getElementIcon(
                    description.getHandle().getKind(),
                    EnumSet.noneOf(Modifier.class)));
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

        @Override
        public Action[] getActions(boolean context) {
            if (context) {
                return globalActions;
            } else {
                Action actions[]  = new Action[ 3 + globalActions.length ];
                actions[0] = getOpenAction();
                actions[1] = RefactoringActionsFactory.whereUsedAction();
                actions[2] = null;
                System.arraycopy(globalActions, 0, actions, 3, globalActions.length);
                return actions;
            }
        }

        @Override
        public Action getPreferredAction() {
            return getOpenAction();
        }

        @Override
        public boolean canCopy() {
            return false;
        }

        @Override
        public boolean canCut() {
            return false;
        }

        @Override
        public boolean canDestroy() {
            return false;
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public PasteType getDropType(Transferable t, int action, int index) {
            return null;
        }

        @Override
        public Transferable drag() throws IOException {
            return null;
        }

        @Override
        protected void createPasteTypes(Transferable t, List<PasteType> s) {
            // Do nothing
        }

        private synchronized Action getOpenAction() {
            if ( openAction == null) {
                openAction = new OpenAction();
            }
            return openAction;
        }

        private void updateDisplayName() {
            String name = description.handle.getQualifiedName();
            if (!ctx.isFQN()) {
                name = HierarchyHistory.getSimpleName(name);
            }
            setDisplayName(name);
        }

        @NonNull
        private static Lookup createLookup (@NonNull Description desc) {
            final InstanceContent ic = new InstanceContent();
            ic.add(desc, ConvertDescription2TreePathHandle);
            ic.add(desc, ConvertDescription2FileObject);
            ic.add(desc, ConvertDescription2DataObject);
            return new AbstractLookup(ic);
        }

        private class OpenAction extends AbstractAction {

            @NbBundle.Messages({"LBL_GoTo=Go to Source"})
            OpenAction() {
                putValue ( Action.NAME, Bundle.LBL_GoTo());
            }

            @NbBundle.Messages({"MSG_NoSource=Source not available for {0}"})
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!ElementOpen.open(
                        description.getClasspathInfo(),
                        description.getHandle())) {
                    Toolkit.getDefaultToolkit().beep();
                    StatusDisplayer.getDefault().setStatusText(
                        Bundle.MSG_NoSource(description.getHandle().getQualifiedName()));
                }
            }
        }


        private static final Convertor<Description, TreePathHandle> ConvertDescription2TreePathHandle =
                new InstanceContent.Convertor<Description, TreePathHandle>() {
                    @Override
                    public TreePathHandle convert(Description desc) {
                        return TreePathHandle.from(desc.getHandle(), desc.getClasspathInfo());
                    }
                    @Override
                    public Class<? extends TreePathHandle> type(Description desc) {
                        return TreePathHandle.class;
                    }
                    @Override
                    public String id(Description desc) {
                        return "IL[" + desc.toString();
                    }
                    @Override
                    public String displayName(Description desc) {
                        return id(desc);
                    }
            };

        private static final Convertor<Description, FileObject> ConvertDescription2FileObject =
                new InstanceContent.Convertor<Description, FileObject>() {
                    @Override
                    public FileObject convert(Description desc) {
                        return SourceUtils.getFile(
                                desc.getHandle(),
                                desc.getClasspathInfo());
                    }
                    @Override
                    public Class<? extends FileObject> type(Description desc) {
                        return FileObject.class;
                    }
                    @Override
                    public String id(Description desc) {
                        return "IL[" + desc.toString();
                    }
                    @Override
                    public String displayName(Description desc) {
                        return id(desc);
                    }
            };

    private static final Convertor<Description, DataObject> ConvertDescription2DataObject =
            new InstanceContent.Convertor<Description, DataObject>(){
                @Override
                public DataObject convert(Description desc) {
                    try {
                        final FileObject file = SourceUtils.getFile(
                                desc.getHandle(),
                                desc.getClasspathInfo());
                        return file == null ? null : DataObject.find(file);
                    } catch (DataObjectNotFoundException ex) {
                        return null;
                    }
                }
                @Override
                public Class<? extends DataObject> type(Description desc) {
                    return DataObject.class;
                }
                @Override
                public String id(Description desc) {
                    return "IL[" + desc.toString();
                }
                @Override
                public String displayName(Description desc) {
                    return id(desc);
                }
        };

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
        @Override
        public int compare(Node n1, Node n2) {
            return n1.getDisplayName().compareTo(n2.getDisplayName());
        }
    }
}
