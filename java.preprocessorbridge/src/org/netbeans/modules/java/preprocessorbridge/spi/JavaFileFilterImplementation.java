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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.preprocessorbridge.spi;

import java.io.Reader;
import java.io.Writer;
import javax.swing.event.ChangeListener;

/**
 * This interface in a friend contract among the j2me project and java/source
 * module. The implementation preprocesses the java file content when it's red by the
 * java infrastructure if needed. From the performance reasons there can be just one
 * implementation of this interface for all sources in the project.
 * 
 * @author Tomas Zezula
 */
public interface JavaFileFilterImplementation {
        
    /**
     * Filters an {@link Reader} by the preprocessor.
     * @param r {@link Reader} to be preprocessed
     * @return an preprocessed {@link Reader}
     */
    public Reader filterReader (Reader r);
    
    /**
     * Filters an input {@link CharSequence} by the preprocessor. From the performance reason
     * it's highly recomended to implement the method using decorator pattern.
     * @param charSequence {@link CharSequence} to be preprocessed
     * @return an preprocessed {@link CharSequence}
     */
    public CharSequence filterCharSequence (CharSequence charSequence);
        
    /**
     * Filters an {@link Writer} by the preprocessor.
     * @param w {@link Writer} to be preprocessed
     * @return an preprocessed {@link Writer}
     */
    public Writer filterWriter (Writer w);
    
    /**
     * Adds an {@link ChangeListener} to the {@link JavaFileFilterImplementation}
     * The implementor should fire a change when the rules for preprocessing has changed
     * and files should be rescanned.
     * @param listener to be added
     */
    public void addChangeListener (ChangeListener listener);
    

    /**
     * Removes an {@link ChangeListener} to the {@link JavaFileFilterImplementation}
     * The implementor should fire a change when the rules for preprocessing has changed
     * and files should be rescanned.
     * @param listener to be removed
     */
    public void removeChangeListener (ChangeListener listener);
}
