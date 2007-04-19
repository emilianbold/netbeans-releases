/*
 * MethodLocator.java
 *
 * Created on April 7, 2007, 3:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.core;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;

/**
 *
 * @author rico
 */
public class MethodVisitor {
    
    private String operationName;
    private ExecutableElement method;
    private CompilationInfo info;
    private boolean hasWebMethod;
    private boolean hasPublicMethod;
    private List<ExecutableElement> publicMethods;
    
    /** Creates a new instance of MethodLocator */
    public MethodVisitor(CompilationInfo info) {
        this.info = info;
    }
    
    public ExecutableElement getMethod(String operationName){
        this.operationName = operationName;
        new JavaMethodVisitor().scan(info.getCompilationUnit(), null);
        return method;
    }
    
    public boolean hasWebMethod(){
        new WebMethodVisitor().scan(info.getCompilationUnit(), null);
        return hasWebMethod;
    }
    
    public List<ExecutableElement> getPublicMethods(){
        new PublicMethodVisitor().scan(info.getCompilationUnit(), null);
        return publicMethods;
    }
    
    public boolean hasPublicMethod(){
        new PublicMethodVisitor().scan(info.getCompilationUnit(), null);
        return hasPublicMethod;
    }
    
    private class PublicMethodVisitor extends TreePathScanner<Void, Void>{
        public PublicMethodVisitor(){
            publicMethods = new ArrayList<ExecutableElement>();
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if(el != null){
                TypeElement te = (TypeElement) el;
                List<ExecutableElement> methods = ElementFilter.methodsIn(te.getEnclosedElements());
                for(ExecutableElement m: methods){
                    if(m.getModifiers().contains(Modifier.PUBLIC)){
                        hasPublicMethod = true;
                        publicMethods.add(m);
                    }
                    
                }
            }
            return null;
        }
    }
    
    private class WebMethodVisitor extends TreePathScanner<Void, Void>{
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if(el != null){
                TypeElement te = (TypeElement) el;
                List<ExecutableElement> methods = ElementFilter.methodsIn(te.getEnclosedElements());
                for(ExecutableElement m: methods){
                    if(hasWebMethodAnnotation(m)){
                        hasWebMethod = true;
                        break;
                    }
                }
            }
            return null;
        }
    }
    
    private class JavaMethodVisitor extends TreePathScanner<Void, Void>{
        
        public JavaMethodVisitor(){
            
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if(el != null){
                TypeElement te = (TypeElement) el;
                List<ExecutableElement> methods = ElementFilter.methodsIn(te.getEnclosedElements());
                for(ExecutableElement m: methods){
                    if(isMethodFor(m, operationName)){
                        method = m;
                        break;
                    }
                    
                }
            }
            return null;
        }
    }
    
    /**
     *  Determines if the method has a WebMethod annotation and if it does
     *  that the exclude attribute is not set
     */
    private boolean hasWebMethodAnnotation(ExecutableElement method){
        boolean isWebMethod = false;
        TypeElement methodAnotationEl = info.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
        List<? extends AnnotationMirror> methodAnnotations = method.getAnnotationMirrors();
        for (AnnotationMirror anMirror : methodAnnotations) {
            if (info.getTypes().isSameType(methodAnotationEl.asType(), anMirror.getAnnotationType())) {
                //WebMethod found, set boolean to true
                isWebMethod = true;
                //Now determine if "exclude" is present and set to true
                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                for(ExecutableElement ex:expressions.keySet()) {
                    if (ex.getSimpleName().contentEquals("exclude")) { //NOI18N
                        String value = (String)expressions.get(ex).getValue();
                        if ("true".equals(value)){
                            isWebMethod = false;
                            break;
                        }
                    }
                }
            }
            break;
        }
        return isWebMethod;
    }
    
    /**
     *  Determines if the WSDL operation is the corresponding Java method
     */
    private boolean isMethodFor(ExecutableElement method, String operationName){
        if(method.getSimpleName().toString().equals(operationName)){
            return true;
        }
        
        //if method name is not the same as the operation name, look at WebMethod annotation
        TypeElement methodAnotationEl = info.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
        List<? extends AnnotationMirror> methodAnnotations = method.getAnnotationMirrors();
        for (AnnotationMirror anMirror : methodAnnotations) {
            if (info.getTypes().isSameType(methodAnotationEl.asType(), anMirror.getAnnotationType())) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                for(ExecutableElement ex:expressions.keySet()) {
                    if (ex.getSimpleName().contentEquals("operationName")) { //NOI18N
                        String name = (String)expressions.get(ex).getValue();
                        if (operationName.equals(name)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
