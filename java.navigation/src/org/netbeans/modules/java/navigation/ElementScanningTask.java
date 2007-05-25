/*
 * ElementScanningTask.java
 *
 * Created on November 9, 2006, 6:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
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
import javax.lang.model.util.ElementScanner6;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.timers.TimesCollector;
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
        //System.out.println("The task is running" + info.getFileObject().getNameExt() + "=====================================" ) ;
        
        Description rootDescription = new Description( ui );
        rootDescription.fileObject = info.getFileObject();
        rootDescription.subs = new ArrayList<Description>();
        
        // Get all outerclasses in the Compilation unit
        CompilationUnitTree cuTree = info.getCompilationUnit();
        List<? extends Tree> typeDecls = cuTree.getTypeDecls();
        List<Element> elements = new ArrayList<Element>( typeDecls.size() );
        TreePath cuPath = new TreePath( cuTree );
        for( Tree t : typeDecls ) {
            TreePath p = new TreePath( cuPath, t );
            Element e = info.getTrees().getElement( p );
            if ( e != null ) {
                elements.add( e );
            }
        }
        Map<Element,Long> pos = new HashMap<Element,Long>();
        if (!canceled.get()) {
            Trees trees = info.getTrees();
            PositionVisitor posVis = new PositionVisitor (trees, canceled);
            posVis.scan(cuTree, pos);
        }
        
        if ( !canceled.get()) {
            FindChildrenElementVisitor scanner = new FindChildrenElementVisitor(info, pos, canceled);                      
            for (Element element : elements) {
                scanner.scan(element, rootDescription);
            }
        }
        
        if ( !canceled.get()) {
            ui.refresh( rootDescription );            
        }
        long end = System.currentTimeMillis();
        TimesCollector.getDefault().reportTime(info.getFileObject(),  "element-scanning-task",   //NOI18N
            "Element Scanning Task", end - start);   //NOI18N
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
     
    private static class FindChildrenElementVisitor extends ElementScanner6<Void, ElementNode.Description> {
        
        private CompilationInfo info;
        private final Map<Element,Long> pos;
        private final AtomicBoolean canceled;
        
        public FindChildrenElementVisitor(CompilationInfo info, Map<Element,Long> pos, AtomicBoolean canceled) {
            assert info != null;
            assert pos != null;
            assert canceled != null;
            this.info = info;
            this.pos = pos;
            this.canceled = canceled;
        }        
        
        public Void visitPackage(PackageElement e, Description p) {
            return null; // No interest in packages here
        }
        
        public Void visitType(TypeElement e, Description p) {
            if ( !canceled.get()  && !info.getElementUtilities().isSynthetic(e) ) {            
                
                Description d = new Description(p.ui, e.getSimpleName().toString(), ElementHandle.create(e), e.getKind());
                d.modifiers = e.getModifiers();
                d.subs = new ArrayList<Description>();
                d.pos = getPosition( e );
                d.htmlHeader = createHtmlHeader( e, info.getElements().isDeprecated(e) );                
                if ( d.pos == -1 ) {
                    return null;
                }
                super.visitType(e, d);
                p.subs.add(d);
            }
            return null;
        }
        
        public Void visitVariable(VariableElement e, Description p) {
            if ( !canceled.get() && !info.getElementUtilities().isSynthetic(e) && 
                ( e.getKind() == ElementKind.FIELD || e.getKind() == ElementKind.ENUM_CONSTANT ) ) {
                Description d = new Description(p.ui, e.getSimpleName().toString(), ElementHandle.create(e), e.getKind());
                d.modifiers = e.getModifiers();
                d.pos = getPosition( e );
                d.htmlHeader = createHtmlHeader( e, info.getElements().isDeprecated(e) ); 
                if ( d.pos == -1 ) {
                    return null;
                }
                super.visitVariable(e,d);
                p.subs.add(d);            
            }
            return null;
        }
        
        public Void visitExecutable(ExecutableElement e, Description p) {
            if ( !canceled.get()  && !info.getElementUtilities().isSynthetic(e) ) {
                Description d = new Description(p.ui, e.getSimpleName().toString(), ElementHandle.create(e), e.getKind());
                d.modifiers = e.getModifiers();
                d.pos = getPosition( e );
                if ( d.pos == -1 ) {
                    return null;
                }
                d.htmlHeader = createHtmlHeader(e, info.getElements().isDeprecated(e));
                super.visitExecutable(e, d);
                p.subs.add(d);            
            }
            return null;
        }
        
        
        public Void visitTypeParameter(TypeParameterElement e, Description p) {
            return null;
        }
        
        private long getPosition( Element e ) {   
             Long res = pos.get(e);
             if (res == null) {
//                java.util.logging.Logger.getLogger(ElementScanningTask.class.getName()).warning("No pos for: " + e);
                return -1;
             }
             return res.longValue();
        }
        
                
        /** Creates HTML display name of the Executable element */
        private String createHtmlHeader( ExecutableElement e, boolean isDeprecated ) {
            
            StringBuilder sb = new StringBuilder();
            if ( isDeprecated ) {
                sb.append("<s>"); // NOI18N
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
        
        private String createHtmlHeader( VariableElement e, boolean isDeprecated ) {
            
            StringBuilder sb = new StringBuilder();
            
            if ( isDeprecated ) {
                sb.append("<s>"); // NOI18N
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
        
        private String createHtmlHeader( TypeElement e, boolean isDeprecated ) {
            
            StringBuilder sb = new StringBuilder();            
            if ( isDeprecated ) {
                sb.append("<s>"); // NOI18N
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
    
}
