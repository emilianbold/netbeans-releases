/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.editor.hints.common;

import com.sun.source.tree.Tree;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Encapsulate often reused and sometimes expensive to calculate
 * properties of the class being examined
 *
 * @author Ajit.Bhate@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public class ProblemContext implements Lookup.Provider {
    private FileObject fileObject;
    private CompilationInfo info;
    private boolean cancelled = false;
    private Tree elementToAnnotate;
    private TypeElement javaClass;
    private AbstractLookup lookup;
    private InstanceContent ic;
    
    public FileObject getFileObject(){
        return fileObject;
    }
    
    public void setFileObject(FileObject fileObject){
        this.fileObject = fileObject;
    }
    
    public CompilationInfo getCompilationInfo(){
        return info;
    }
    
    public void setCompilationInfo(CompilationInfo info){
        this.info = info;
    }
    
    public void setCancelled(boolean cancelled){
        this.cancelled = cancelled;
    }
    
    /**
     * @return true if the problem finding task was cancelled
     */
    public boolean isCancelled(){
        return cancelled;
    }
    
    public Tree getElementToAnnotate(){
        return elementToAnnotate;
    }
    
    public void setElementToAnnotate(Tree elementToAnnotate){
        this.elementToAnnotate = elementToAnnotate;
    }
    
    public TypeElement getJavaClass(){
        return javaClass;
    }
    
    public void setJavaClass(TypeElement element){
        this.javaClass = element;
    }

    public Lookup getLookup() {
        if(lookup == null) {
            if (ic == null) ic = new InstanceContent();
            lookup = new AbstractLookup(ic);
        }
        return lookup;
    }
    
    public void addUserObject(Object info) {
        if (ic == null) ic = new InstanceContent();
        ic.add(info);
    }

    public void removeUserObject(Object info) {
        if (ic == null) return;
        ic.remove(info);
    }

}
