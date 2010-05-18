/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.soa.palette.java.util;

import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.CompilationInfo;

/**
 *
 * @author echou
 */
public interface JavacTreeModel {

    public List<TypeElement> getEnclosedTypes();
    public TypeElement getEnclosedTypeByType(String type);
    
    public List<ExecutableElement> getMethods();
    public List<ExecutableElement> getMethodsByName(String name);
    
    public List<VariableElement> getVariables();
    public List<VariableElement> getVariablesByName(String name);
    
    public boolean isContainerManaged();
    public CompilationInfo getCompilationInfo();
    
}
