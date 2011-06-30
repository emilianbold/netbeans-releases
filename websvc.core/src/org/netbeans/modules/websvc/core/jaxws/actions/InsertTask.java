package org.netbeans.modules.websvc.core.jaxws.actions;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.openide.filesystems.FileObject;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;

/** This is task for inserting field annotated with @WebServiceRef annotation
 * (only available since Java EE 5 version - in objects manageable by container(servlets, EJBs, Web Services)
 */
class InsertTask implements CancellableTask<WorkingCopy> {
    
    public static final String WEB_SERVICE_REF = "webServiceRef";      // NOI18N
    
    private final String serviceJavaName;
    private final String serviceFName;
    private final String wsdlUrl;
    private final boolean needWsRef;

    public InsertTask(String serviceJavaName, String serviceFName, String wsdlUrl,
            Map<String, Object>  context ) 
    {
        this.serviceJavaName = serviceJavaName;
        this.serviceFName = serviceFName;
        this.wsdlUrl = wsdlUrl;
        this.needWsRef = !(Boolean)context.get(WEB_SERVICE_REF);
    }

    public void run(WorkingCopy workingCopy) throws IOException {
        workingCopy.toPhase(Phase.RESOLVED);
        TreeMaker make = workingCopy.getTreeMaker();
        ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
        
        
        TypeElement classElement = (TypeElement)workingCopy.getTrees().
            getElement(TreePath.getPath( workingCopy.getCompilationUnit(), 
                javaClass));
        
        if (javaClass != null) {
            ClassTree modifiedClass = generateWsServiceRef(workingCopy, make, javaClass);
            modifiedClass = modifyJavaClass(workingCopy, make,
                    modifiedClass, classElement);
            workingCopy.rewrite(javaClass, modifiedClass);
        }
    }

    public void cancel() {
    }
    
    protected ClassTree generateWsServiceRef(WorkingCopy workingCopy,
            TreeMaker make, ClassTree javaClass)
    {
        if ( !needWsRef ) {
            return javaClass;
        }
        TypeElement wsRefElement = workingCopy.getElements().getTypeElement("javax.xml.ws.WebServiceRef"); //NOI18N
        AnnotationTree wsRefAnnotation = make.Annotation(
                make.QualIdent(wsRefElement),
                Collections.<ExpressionTree>singletonList(make.Assignment(make.Identifier("wsdlLocation"), make.Literal(wsdlUrl)))); //NOI18N
        // create field modifier: private(static) with @WebServiceRef annotation
        FileObject targetFo = workingCopy.getFileObject();
        Set<Modifier> modifiers = new HashSet<Modifier>();
        if (Car.getCar(targetFo) != null) {
            modifiers.add(Modifier.STATIC);
        }
        modifiers.add(Modifier.PRIVATE);
        ModifiersTree methodModifiers = make.Modifiers(
                modifiers,
                Collections.<AnnotationTree>singletonList(wsRefAnnotation));
        TypeElement typeElement = workingCopy.getElements().getTypeElement(serviceJavaName);
        VariableTree serviceRefInjection = make.Variable(
            methodModifiers,
            serviceFName,
            (typeElement != null ? make.Type(typeElement.asType()) : make.Identifier(serviceJavaName)),
            null);
        
        ClassTree modifiedClass = make.insertClassMember(javaClass, 0, serviceRefInjection);
        return modifiedClass;
    }
    
    protected ClassTree modifyJavaClass( WorkingCopy workingCopy,
            TreeMaker make, ClassTree javaClass, TypeElement classElement )
    {
        return javaClass;
    }
}