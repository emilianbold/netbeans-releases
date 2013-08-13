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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.java.navigation;
import org.netbeans.modules.java.navigation.base.Utils;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeUtilities.TypeNameOptions;
import org.netbeans.modules.java.navigation.ElementNode.Description;
import org.openide.util.Parameters;

/** XXX Remove the ElementScanner class from here it should be enough to
 * consult the Elements class. It should also permit for showing inherited members.
 *
 * @author phrebejk
 */
public class ElementScanningTask implements CancellableTask<CompilationInfo>{
    
    private static final Logger LOG = Logger.getLogger(ElementScanningTask.class.getName());
    private final ClassMemberPanelUI ui;
    private final AtomicBoolean canceled = new AtomicBoolean ();        
    
    public ElementScanningTask(@NonNull final ClassMemberPanelUI ui ) {
        Parameters.notNull("ui", ui);   //NOI18N
        this.ui = ui;
    }
    
    @Override
    public void cancel() {
        //System.out.println("Element task canceled");
        canceled.set(true);
    }

    @Override
    public void run(CompilationInfo info) throws Exception {
        runImpl(info, false);
    }

    void runImpl(
            @NonNull final CompilationInfo info,
            final boolean userAction) throws Exception {
        ui.start();
        canceled.set (false); // Task shared for one file needs reset first
        long start = System.currentTimeMillis();        
        
        if (ClassMemberPanel.compareAndSetLastUsedFile(info.getFileObject()) && info.getChangedTree() != null) {
            //method body has been reparsed - no need to update the navigator
            long end = System.currentTimeMillis();
            Logger.getLogger("TIMER").log(Level.FINE, "Element Scanning Task", //NOI18N
                    new Object[]{info.getFileObject(), end - start});
            return ;
        }
        
        //System.out.println("The task is running" + info.getFileObject().getNameExt() + "=====================================" ) ;
        
        Description rootDescription = new Description( ui );
        rootDescription.fileObject = info.getFileObject();
        rootDescription.subs = new HashSet<Description>();
        
        // Get all outerclasses in the Compilation unit
        CompilationUnitTree cuTree = info.getCompilationUnit();        
        List<? extends TypeElement> elements = info.getTopLevelElements();
        
        final Map<Element,Long> pos = new HashMap<Element,Long>();
        if (!canceled.get()) {
            Trees trees = info.getTrees();
            PositionVisitor posVis = new PositionVisitor (trees, canceled);
            posVis.scan(cuTree, pos);
        }
        final boolean fqn = ui.getFilters().isFqn();
        if ( !canceled.get() && elements != null) {
            for (Element element : elements) {
                Description topLevel = element2description(element, null, false, info, pos, fqn);
                if( null != topLevel ) {
                    if (!rootDescription.subs.add( topLevel )) {
                        LOG.log(Level.INFO, "Duplicate top level class: {0}", topLevel.name);   //NOI18N
                    }
                    addMembers( (TypeElement)element, topLevel, info, pos, fqn);
                }
            }
        }
        
        if ( !canceled.get()) {
            ui.refresh(rootDescription, userAction);
        }
        long end = System.currentTimeMillis();
        Logger.getLogger("TIMER").log(Level.FINE, "Element Scanning Task",  //NOI18N
                new Object[] {info.getFileObject(), end - start});
    }

    private static class PositionVisitor extends TreePathScanner<Void, Map<Element,Long>> {

        private final Trees trees;
        private final SourcePositions sourcePositions;
        private final AtomicBoolean canceled;
        private CompilationUnitTree cu;

        public PositionVisitor (final Trees trees, final AtomicBoolean canceled) {
            assert trees != null;
            assert canceled != null;
            this.trees = trees;
            this.sourcePositions = trees.getSourcePositions();
            this.canceled = canceled;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree node, Map<Element, Long> p) {
            this.cu = node;
            return super.visitCompilationUnit(node, p);
        }

        @Override
        public Void visitClass(ClassTree node, Map<Element, Long> p) {
            Element e = this.trees.getElement(this.getCurrentPath());
            if (e != null) {
                long pos = this.sourcePositions.getStartPosition(cu, node);
                p.put(e, pos);
            }
            return super.visitClass(node, p);
        }

        @Override
        public Void visitMethod(MethodTree node, Map<Element, Long> p) {
            Element e = this.trees.getElement(this.getCurrentPath());
            if (e != null) {
                long pos = this.sourcePositions.getStartPosition(cu, node);
                p.put(e, pos);
            }
            return null;
        }

        @Override
        public Void visitVariable(VariableTree node, Map<Element, Long> p) {
            Element e = this.trees.getElement(this.getCurrentPath());
            if (e != null) {
                long pos = this.sourcePositions.getStartPosition(cu, node);
                p.put(e, pos);
            }
            return null;
        }

        @Override
        public Void scan(Tree tree, Map<Element, Long> p) {
            if (!canceled.get()) {
                return super.scan(tree, p);
            }
            else {                
                return null;
            }
        }        
    }
     
    private void addMembers( final TypeElement e, final Description parentDescription, final CompilationInfo info, final Map<Element,Long> pos, boolean fqn) {
        List<? extends Element> members = info.getElements().getAllMembers( e );
        for( Element m : members ) {
            if( canceled.get() )
                return;
            
            Description d = element2description(m, e, parentDescription.isInherited, info, pos, fqn);
            if( null != d ) {
                if (!parentDescription.subs.add( d )) {
                    LOG.log(Level.INFO, "Duplicate enclosed element: {0}", d.name);   //NOI18N  Should never happen
                }
                if( m instanceof TypeElement && !d.isInherited ) {
                    addMembers( (TypeElement)m, d, info, pos, fqn);
                }
            }
        }
    }
    
    private Description element2description(final Element e, final Element parent,
            final boolean isParentInherited, final CompilationInfo info,
            final Map<Element,Long> pos, boolean  fqn) {
        if( info.getElementUtilities().isSynthetic(e) )
            return null;
        
        boolean inherited = isParentInherited || (null != parent && !parent.equals( e.getEnclosingElement() ));
        final Element encElement = e.getEnclosingElement();
        Description d = new Description(
                ui,
                getSimpleName(e),
                ElementHandle.create(e),
                e.getKind(),
                inherited,
                encElement != null && encElement.getKind() == ElementKind.PACKAGE);
        
        if( e instanceof TypeElement ) {
            d.subs = new HashSet<Description>();
            d.htmlHeader = createHtmlHeader(info,  (TypeElement)e, info.getElements().isDeprecated(e),d.isInherited, fqn );
        } else if( e instanceof ExecutableElement ) {
            d.htmlHeader = createHtmlHeader(info,  (ExecutableElement)e, info.getElements().isDeprecated(e),d.isInherited, fqn );
        } else if( e instanceof VariableElement ) {
            if( !(e.getKind() == ElementKind.FIELD || e.getKind() == ElementKind.ENUM_CONSTANT) )
                return null;
            d.htmlHeader = createHtmlHeader(info,  (VariableElement)e, info.getElements().isDeprecated(e),d.isInherited, fqn );
        }
        
        d.modifiers = e.getModifiers();
        d.pos = getPosition(e, info, pos);
        d.cpInfo = info.getClasspathInfo();
        
        return d;
    }
        
    
    private long getPosition(final Element e, final CompilationInfo info, final Map<Element,Long> pos) {
         Long res = pos.get(e);
         if (res == null) {
            return -1;
         }
         return res.longValue();
    }

    private static String getSimpleName(@NonNull final Element e) {
        if (e.getKind() == ElementKind.CONSTRUCTOR) {
            return e.getEnclosingElement().getSimpleName().toString();
        } else {
            return e.getSimpleName().toString();
        }
    }
        
   /** Creates HTML display name of the Executable element */
    private String createHtmlHeader(CompilationInfo info, ExecutableElement e, boolean isDeprecated,boolean isInherited, boolean fqn ) {

        StringBuilder sb = new StringBuilder();
        if ( isDeprecated ) {
            sb.append("<s>"); // NOI18N
        }
        if( isInherited ) {
            sb.append( "<font color=" + ui.getInheritedColor() + ">" ); // NOI18N
        }
        Name name = e.getKind() == ElementKind.CONSTRUCTOR ? e.getEnclosingElement().getSimpleName() : e.getSimpleName();
        sb.append(Utils.escape(name.toString()));        
        if ( isDeprecated ) {
            sb.append("</s>"); // NOI18N
        }

        sb.append("("); // NOI18N

        List<? extends VariableElement> params = e.getParameters();
        for( Iterator<? extends VariableElement> it = params.iterator(); it.hasNext(); ) {
            VariableElement param = it.next(); 
            sb.append( "<font color=" + ui.getTypeColor() + ">" ); // NOI18N
            final boolean vararg = !it.hasNext() && e.isVarArgs();
            sb.append(printArg(info, param.asType(),vararg, fqn));
            sb.append("</font>"); // NOI18N
            sb.append(" "); // NOI18N
            sb.append(Utils.escape(param.getSimpleName().toString()));
            if ( it.hasNext() ) {
                sb.append(", "); // NOI18N
            }
        }


        sb.append(")"); // NOI18N

        if ( e.getKind() != ElementKind.CONSTRUCTOR ) {
            TypeMirror rt = e.getReturnType();
            if ( rt.getKind() != TypeKind.VOID ) {                               
                sb.append(" : "); // NOI18N     
                sb.append( "<font color=" + ui.getTypeColor() + ">" ); // NOI18N
                sb.append(print(info, e.getReturnType(), fqn));
                sb.append("</font>"); // NOI18N                    
            }
        }

        return sb.toString();
    }

    private String createHtmlHeader(CompilationInfo info, VariableElement e, boolean isDeprecated,boolean isInherited, boolean fqn) {

        StringBuilder sb = new StringBuilder();

        if ( isDeprecated ) {
            sb.append("<s>"); // NOI18N
        }
        if( isInherited ) {
            sb.append( "<font color=" + ui.getInheritedColor() + ">" ); // NOI18N
        }
        sb.append(Utils.escape(e.getSimpleName().toString()));
        if ( isDeprecated ) {
            sb.append("</s>"); // NOI18N
        }

        if ( e.getKind() != ElementKind.ENUM_CONSTANT ) {
            sb.append( " : " ); // NOI18N
            sb.append( "<font color=" + ui.getTypeColor() + ">" ); // NOI18N
            sb.append(print(info, e.asType(), fqn));
            sb.append("</font>"); // NOI18N
        }

        return sb.toString();            
    }

    private String createHtmlHeader(CompilationInfo info, TypeElement e, boolean isDeprecated, boolean isInherited, boolean fqn) {

        StringBuilder sb = new StringBuilder();            
        if ( isDeprecated ) {
            sb.append("<s>"); // NOI18N
        }
        if( isInherited ) {
            sb.append( "<font color=" + ui.getInheritedColor() + ">" ); // NOI18N
        }
        sb.append(Utils.escape(
            fqn?
            e.getQualifiedName().toString():
            e.getSimpleName().toString()));
        if ( isDeprecated ) {
            sb.append("</s>"); // NOI18N
        }
        // sb.append(print(info, e.asType()));
        List<? extends TypeParameterElement> typeParams = e.getTypeParameters();

        //System.out.println("Element " + e + "type params" + typeParams.size() );

        if ( typeParams != null && !typeParams.isEmpty() ) {
            sb.append("&lt;"); // NOI18N

            for( Iterator<? extends TypeParameterElement> it = typeParams.iterator(); it.hasNext(); ) {
                TypeParameterElement tp = it.next();
                sb.append( Utils.escape(tp.getSimpleName().toString()) );                    
                try { // XXX Verry ugly -> file a bug against Javac?
                    List<? extends TypeMirror> bounds = tp.getBounds();
                    //System.out.println( tp.getSimpleName() + "   bounds size " + bounds.size() );
                    if ( !bounds.isEmpty() ) {
                        sb.append(printBounds(info, bounds, fqn));
                    }
                }
                catch ( NullPointerException npe ) {
                    System.err.println("El " + e );
                    npe.printStackTrace();
                }                    
                if ( it.hasNext() ) {
                    sb.append(", "); // NOI18N
                }
            }

            sb.append("&gt;"); // NOI18N
        }

        // Add superclass and implemented interfaces

        TypeMirror sc = e.getSuperclass();
        String scName = print(info, sc, fqn);

        if ( sc == null || 
             e.getKind() == ElementKind.ENUM ||
             e.getKind() == ElementKind.ANNOTATION_TYPE ||
             "Object".equals(scName) || // NOI18N
             "<none>".equals(scName)) { // NOI18N
            scName = null;
        }

        List<? extends TypeMirror> ifaces = e.getInterfaces();

        if ( ( scName != null || !ifaces.isEmpty() ) &&
              e.getKind() != ElementKind.ANNOTATION_TYPE ) {
            sb.append( " :: " ); // NOI18N
            if (scName != null) {                
                sb.append( "<font color=" + ui.getTypeColor() + ">" ); // NOI18N
                sb.append( scName );
                sb.append("</font>"); // NOI18N
            }
            if ( !ifaces.isEmpty() ) {
                if ( scName != null ) {
                    sb.append( " : " ); // NOI18N
                }
                for (Iterator<? extends TypeMirror> it = ifaces.iterator(); it.hasNext();) {
                    TypeMirror typeMirror = it.next();
                    sb.append( "<font color=" + ui.getTypeColor() + ">" ); // NOI18N
                    sb.append( print(info, typeMirror, fqn) );
                    sb.append("</font>"); // NOI18N
                    if ( it.hasNext() ) {
                        sb.append(", "); // NOI18N
                    }
                }

            }
        }

        return sb.toString();            
    }

    private String printBounds(CompilationInfo info,  List<? extends TypeMirror> bounds, boolean fqn) {
        if ( bounds.size() == 1 && "java.lang.Object".equals( bounds.get(0).toString() ) ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        sb.append( " extends " ); // NOI18N

        for (Iterator<? extends TypeMirror> it = bounds.iterator(); it.hasNext();) {
            TypeMirror bound = it.next();
            sb.append(print(info, bound, fqn));
            if ( it.hasNext() ) {
                sb.append(" & " ); // NOI18N
            }

        }

        return sb.toString();
    }

    private String printArg(CompilationInfo info, final TypeMirror tm, final boolean varArg, boolean fqn) {
        if (varArg) {
            if (tm.getKind() == TypeKind.ARRAY) {
                final ArrayType at = (ArrayType)tm;
                final StringBuilder sb = new StringBuilder( print(info, at.getComponentType(), fqn) );
                sb.append("...");   //NOI18N
                return sb.toString();
            } else {
                assert false : "Expected array: " + tm.toString() + " ( " +tm.getKind() + " )"; //NOI18N
            }
        }
        return print(info, tm, fqn);
    }

    private String print(CompilationInfo info, TypeMirror tm, boolean fqn) {
        return Utils.escape(fqn ? info.getTypeUtilities().getTypeName(tm, TypeNameOptions.PRINT_FQN).toString()
                                : info.getTypeUtilities().getTypeName(tm).toString());
    }    
}
