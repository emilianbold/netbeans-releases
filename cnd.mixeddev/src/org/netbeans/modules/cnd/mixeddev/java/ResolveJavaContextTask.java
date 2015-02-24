/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.mixeddev.java;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
public interface ResolveJavaContextTask<T> extends CancellableTask<CompilationController> {
    
    boolean hasResult();
    
    T getResult();
    
}
