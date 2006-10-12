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

package org.netbeans.modules.xml.xam;


import org.openide.util.Lookup;
/**
 * This is the class that encapsulates the physical file for each model.
 * @author girix
 */
public class ModelSource implements Lookup.Provider {
    
    private Lookup lookup;
    private boolean editable;
    
    /**
     * Create a model source object given the lookup context.  If editable is false
     * the model cannot be mutated.  Note that editable is static attribute of the
     * model source, and does not reflect the access attribute of the associated file.
     *
     * @param lookup Lookup object associated with this ModelSource. Lookup minimally 
     * contains a File path of the backing file of the model and a javax.swing.text.Document object.
     * @param editable whether the model is supposed to be mutated.
     */
    public ModelSource(Lookup lookup, boolean editable){
        this.editable = editable;
        this.lookup = lookup;
    }
    
    /**
     * Returns the lookup object associated with this ModelSource. Lookup minimally 
     * contains a File absolute path or FileObject of the backing file of the model 
     * and javax.swing.text.Document object.  If model is DOM, the lookup should 
     * also contains javax.xml.transform.Source object for use in cases of relative 
     * resolution of resource such as validation.
     */
    public Lookup getLookup(){
        return lookup;
    }
    
    /**
     * States if the backing file can be edited.
     * @return true if the model source file is writable.
     */
    public boolean isEditable(){
        return editable;
    }
}
