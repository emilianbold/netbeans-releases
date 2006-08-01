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

package org.netbeans.modules.editor.mimelookup.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author vita
 */
public final class InstanceProviderLookup extends AbstractLookup {

    private static final Logger LOG = Logger.getLogger(InstanceProvider.class.getName());
    
    private InstanceContent content;
    private InstanceProvider instanceProvider;
    
    private CompoundFolderChildren children;
    private PCL listener = new PCL();

    private final String LOCK = new String("InstanceProviderLookup.LOCK"); //NOI18N
    
    /** Creates a new instance of InstanceProviderLookup */
    public InstanceProviderLookup(String [] paths, InstanceProvider instanceProvider) {
        this(paths, instanceProvider, new InstanceContent());
    }
    
    private InstanceProviderLookup(String [] paths, InstanceProvider instanceProvider, InstanceContent content) {
        super(content);
        
        this.content = content;
        this.instanceProvider = instanceProvider;
        
        this.children = new CompoundFolderChildren(paths, true);
        this.children.addPropertyChangeListener(listener);
        
        rebuild();
    }

    private void rebuild() {
        List files = children.getChildren();
        Object instance = instanceProvider.createInstance(files);
        content.set(Collections.singleton(instance), null);
    }
    
    private class PCL implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            rebuild();
        }
    } // End of PCL class
}
