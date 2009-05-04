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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.java.navigation.ElementNode.Description;

/** XXX Remove the ElementScanner class from here it should be wenough to
 * consult the Elements class. It should also permit for showing inherited members.
 *
 * @author phrebejk
 */
public class ElementScanningTask implements CancellableTask<CompilationInfo>{
    
    private ClassMemberPanelUI ui;
    private final AtomicBoolean canceled = new AtomicBoolean ();
    
    private static final String TYPE_COLOR = "#707070";
    private static final String INHERITED_COLOR = "#7D694A";
    
    public ElementScanningTask( ClassMemberPanelUI ui ) {
        this.ui = ui;
    }
    
    public void cancel() {
        //System.out.println("Element task canceled");
        canceled.set(true);
    }

    public void run(CompilationInfo info) throws Exception {        
        canceled.set (false); // Task shared for one file needs reset first
        long start = System.currentTimeMillis();        
        
        if (info.getChangedTree() != null) {
            //method body has been reparsed - no need to update the navigator
            long end = System.currentTimeMillis();
            Logger.getLogger("TIMER").log(Level.FINE, "Element Scanning Task", //NOI18N
                    new Object[]{info.getFileObject(), end - start});
            return ;
        }
        
        //System.out.println("The task is running" + info.getFileObject().getNameExt() + "=====================================" ) ;
        
        Description rootDescription = new Description( ui );
        rootDescription.fileObject = info.getFileObject();
        rootDescription.subs = new ArrayList<Description>();
        
        // Get all outerclasses in the Compilation unit
        CompilationUnitTree cuTree = info.getCompilationUnit();        
        List<? extends TypeElement> elements = info.getTopLevelElements();
        
        final Map<Element,Long> pos = new HashMap<Element,Long>();
        if (!canceled.get()) {
            Trees trees = info.getTrees();
            PositionVisitor posVis = new PositionVisitor (trees, canceled);
            posVis.scan(cuTree, pos);
        }
        
        if ( !canceled.get() && elements != null) {
            for (Element element : elements) {
                Description topLevel = element2description(element, null, false, info, pos);
                if( null != topLevel ) {
                    rootDescription.subs.add( topLevel );
                    addMembers( (TypeElement)element, topLevel, info, pos);
                }
            }
        }
        
        if ( !canceled.get()) {
            ui.refresh( rootDescription );            
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
     
    private void addMembers( final TypeElement e, final Description parentDescription, final CompilationInfo info, final Map<Element,Long> pos) {        
        List<? extends Element> members = info.getElements().getAllMembers( e );
        for( Element m : members ) {
            if( canceled.get() )
                return;
            
            Description d = element2description(m, e, parentDescription.isInherited, info, pos);
            if( null != d ) {
                parentDescription.subs.add( d );
                if( m instanceof TypeElement && !d.isInherited ) {
                    addMembers( (TypeElement)m, d, info, pos);
                }
            }
        }
    }
    
    private Description element2description(final Element e, final Element parent,
            final boolean isParentInherited, final CompilationInfo info,
            final Map<Element,Long> pos) {
        if( info.getElementUtilities().isSynthetic(e) )
            return null;
        
        boolean inherited = isParentInherited || (null != parent && !parent.equals( e.getEnclosingElement() ));
        Description d = new Description( ui, e.getSimpleName().toString(), ElementHandle.create(e), e.getKind(), TreePathHandle.create(e, info), inherited );
        
        if( e instanceof TypeElement ) {
            d.subs = new ArrayList<Description>();
            d.htmlHeader = createHtmlHeader(  (TypeElement)e, info.getElements().isDeprecated(e),d.isInherited ); 
        } else if( e instanceof ExecutableElement ) {
            d.htmlHeader = createHtmlHeader(  (ExecutableElement)e, info.getElements().isDeprecated(e),d.isInherited ); 
        } else if( e instanceof VariableElement ) {
            if( !(e.getKind() == ElementKind.FIELD || e.getKind() == ElementKind.ENUM_CONSTANT) )
                return null;
            d.htmlHeader = createHtmlHeader(  (VariableElement)e, info.getElements().isDeprecated(e),d.isInherited ); 
        }
        
        d.modifiers = e.getModifiers();
        d.pos = getPosition(e, info, pos);
        
        if( inherited ) {
            d.cpInfo = info.getClasspathInfo();
        }
        
        return d;
    }
        
    
    private long getPosition(final Element e, final CompilationInfo info, final Map<Element,Long> pos) {
         Long res = pos.get(e);
         if (res == null) {
//                java.util.logging.Logger.getLogger(ElementScanningTask.class.getName()).warning("No pos for: " + e);
            return -1;
         }
         return res.longValue();
    }
        
   /** Creates HTML display name of the Executable element */
    private String createHtmlHeader(  ExecutableElement e, boolean isDeprecated,boolean isInherited ) {

        StringBuilder sb = new StringBuilder();
        if ( isDeprecated ) {
            sb.append("<s>"); // NOI18N
        }
        if( isInherited ) {
            sb.append( "<font color=" + INHERITED_COLOR + ">" ); // NOI18N
        }
        if ( e.getKind() == ElementKind.CONSTRUCTOR ) {
            sb.append(e.getEnclosingElement().getSimpleName());
        }
        else {
            sb.append(e.getSimpleName());
        }
        if ( isDeprecated ) {
            sb.append("</s>"); // NOI18N
        }

        sb.append("("); // NOI18N

        List<? extends VariableElement> params = e.getParameters();
        for( Iterator<? extends VariableElement> it = params.iterator(); it.hasNext(); ) {
            VariableElement param = it.next(); 
            sb.append( "<font color=" + TYPE_COLOR + ">" ); // NOI18N
            sb.append(print( param.asType()));
            sb.append("</font>"); // NOI18N
            sb.append(" "); // NOI18N
            sb.append(param.getSimpleName());
            if ( it.hasNext() ) {
                sb.append(", "); // NOI18N
            }
        }


        sb.append(")"); // NOI18N

        if ( e.getKind() != ElementKind.CONSTRUCTOR ) {
            TypeMirror rt = e.getReturnType();
            if ( rt.getKind() != TypeKind.VOID ) {                               
                sb.append(" : "); // NOI18N     
                sb.append( "<font color=" + TYPE_COLOR + ">" ); // NOI18N
                sb.append(print(e.getReturnType()));
                sb.append("</font>"); // NOI18N                    
            }
        }

        return sb.toString();
    }

    private String createHtmlHeader(  VariableElement e, boolean isDeprecated,boolean isInherited ) {

        StringBuilder sb = new StringBuilder();

        if ( isDeprecated ) {
            sb.append("<s>"); // NOI18N
        }
        if( isInherited ) {
            sb.append( "<font color=" + INHERITED_COLOR + ">" ); // NOI18N
        }
        sb.append(e.getSimpleName());
        if ( isDeprecated ) {
            sb.append("</s>"); // NOI18N
        }

        if ( e.getKind() != ElementKind.ENUM_CONSTANT ) {
            sb.append( " : " ); // NOI18N
            sb.append( "<font color=" + TYPE_COLOR + ">" ); // NOI18N
            sb.append(print(e.asType()));
            sb.append("</font>"); // NOI18N
        }

        return sb.toString();            
    }

    private String createHtmlHeader( TypeElement e, boolean isDeprecated, boolean isInherited ) {

        StringBuilder sb = new StringBuilder();            
        if ( isDeprecated ) {
            sb.append("<s>"); // NOI18N
        }
        if( isInherited ) {
            sb.append( "<font color=" + INHERITED_COLOR + ">" ); // NOI18N
        }
        sb.append(e.getSimpleName());
        if ( isDeprecated ) {
            sb.append("</s>"); // NOI18N
        }
        // sb.append(print(e.asType()));            
        List<? extends TypeParameterElement> typeParams = e.getTypeParameters();

        //System.out.println("Element " + e + "type params" + typeParams.size() );

        if ( typeParams != null && !typeParams.isEmpty() ) {
            sb.append("&lt;"); // NOI18N

            for( Iterator<? extends TypeParameterElement> it = typeParams.iterator(); it.hasNext(); ) {
                TypeParameterElement tp = it.next();
                sb.append( tp.getSimpleName() );                    
                try { // XXX Verry ugly -> file a bug against Javac?
                    List<? extends TypeMirror> bounds = tp.getBounds();
                    //System.out.println( tp.getSimpleName() + "   bounds size " + bounds.size() );
                    if ( !bounds.isEmpty() ) {
                        sb.append(printBounds(bounds));
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
        String scName = print( sc );

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
                sb.append( "<font color=" + TYPE_COLOR + ">" ); // NOI18N                
                sb.append( scName );
                sb.append("</font>"); // NOI18N
            }
            if ( !ifaces.isEmpty() ) {
                if ( scName != null ) {
                    sb.append( " : " ); // NOI18N
                }
                for (Iterator<? extends TypeMirror> it = ifaces.iterator(); it.hasNext();) {
                    TypeMirror typeMirror = it.next();
                    sb.append( "<font color=" + TYPE_COLOR + ">" ); // NOI18N                
                    sb.append( print(typeMirror) );
                    sb.append("</font>"); // NOI18N
                    if ( it.hasNext() ) {
                        sb.append(", "); // NOI18N
                    }
                }

            }
        }

        return sb.toString();            
    }

    private String printBounds( List<? extends TypeMirror> bounds ) {
        if ( bounds.size() == 1 && "java.lang.Object".equals( bounds.get(0).toString() ) ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        sb.append( " extends " ); // NOI18N

        for (Iterator<? extends TypeMirror> it = bounds.iterator(); it.hasNext();) {
            TypeMirror bound = it.next();
            sb.append(print(bound));
            if ( it.hasNext() ) {
                sb.append(" & " ); // NOI18N
            }

        }

        return sb.toString();
    }

    private String print( TypeMirror tm ) {
        StringBuilder sb;

        switch ( tm.getKind() ) {
            case DECLARED:
                DeclaredType dt = (DeclaredType)tm;
                sb = new StringBuilder( dt.asElement().getSimpleName().toString() );
                List<? extends TypeMirror> typeArgs = dt.getTypeArguments();
                if ( !typeArgs.isEmpty() ) {
                    sb.append("&lt;");

                    for (Iterator<? extends TypeMirror> it = typeArgs.iterator(); it.hasNext();) {
                        TypeMirror ta = it.next();
                        sb.append(print(ta));
                        if ( it.hasNext() ) {
                            sb.append(", ");
                        }
                    }
                    sb.append("&gt;");
                }

                return sb.toString(); 
            case TYPEVAR:
                TypeVariable tv = (TypeVariable)tm;  
                sb = new StringBuilder( tv.asElement().getSimpleName().toString() );
                return sb.toString();
            case ARRAY:
                ArrayType at = (ArrayType)tm;
                sb = new StringBuilder( print(at.getComponentType()) );
                sb.append("[]");
                return sb.toString();
            case WILDCARD:
                WildcardType wt = (WildcardType)tm;
                sb = new StringBuilder("?");
                if ( wt.getExtendsBound() != null ) {
                    sb.append(" extends "); // NOI18N
                    sb.append(print(wt.getExtendsBound()));
                }
                if ( wt.getSuperBound() != null ) {
                    sb.append(" super "); // NOI18N
                    sb.append(print(wt.getSuperBound()));
                }
                return sb.toString();
            default:
                return tm.toString();
        }
    }
}
