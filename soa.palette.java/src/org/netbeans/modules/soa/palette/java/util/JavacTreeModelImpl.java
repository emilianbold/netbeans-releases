/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.soa.palette.java.util;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;

/**
 *
 * @author echou
 */
public class JavacTreeModelImpl extends TreePathScanner<Void, Void> implements JavacTreeModel {

    private CompilationController cc;
    private TypeElement classElem;
    
    public JavacTreeModelImpl() {
        
    }
    
    public void setCompilationController(CompilationController cc) {
        this.cc = cc;
    }
    
    public List<TypeElement> getEnclosedTypes() {
        return Collections.unmodifiableList(ElementFilter.typesIn(classElem.getEnclosedElements()));
    }
    
    public TypeElement getEnclosedTypeByType(String type) {
        for (TypeElement t : getEnclosedTypes()) {
            if (t.getSimpleName().contentEquals(type)) {
                return t;
            }
        }
        return null;
    }
    
    public List<ExecutableElement> getMethods() {
        return Collections.unmodifiableList(ElementFilter.methodsIn(classElem.getEnclosedElements()));
    }
    
    public List<ExecutableElement> getMethodsByName(String name) {
        List<ExecutableElement> result = new ArrayList<ExecutableElement> ();
        for (ExecutableElement e : getMethods()) {
            if (e.getSimpleName().contentEquals(name)) {
                result.add(e);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public List<VariableElement> getVariables() {
        return Collections.unmodifiableList(ElementFilter.fieldsIn(classElem.getEnclosedElements()));
    }
    
    public List<VariableElement> getVariablesByName(String name) {
        List<VariableElement> result = new ArrayList<VariableElement> ();
        for (VariableElement e : getVariables()) {
            if (e.getSimpleName().contentEquals(name)) {
                result.add(e);
            }
        }
        return Collections.unmodifiableList(result);
    }
    
    public boolean isContainerManaged() {
        Types types = cc.getTypes();
        Elements elements = cc.getElements();
        TypeMirror annoType = elements.getTypeElement("javax.ejb.TransactionManagement").asType();
        for (AnnotationMirror anno : classElem.getAnnotationMirrors()) {
            if (types.isSameType(anno.getAnnotationType(), annoType)) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> map = elements.getElementValuesWithDefaults(anno);
                for (AnnotationValue annoValue : map.values()) {
                    if (annoValue != null && annoValue.getValue().toString().equals("BEAN")) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public CompilationInfo getCompilationInfo() {
        return cc;
    }

    
    @Override
    public Void visitClass(ClassTree t, Void v) {
        // only visit outter-most class, do not call super.visitClass() method
        Element elem = cc.getTrees().getElement(getCurrentPath());
        if (elem != null) {
            classElem = (TypeElement) elem;
        }
        return null;
    }
    
}
