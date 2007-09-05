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

package org.netbeans.modules.cnd.highlight.error;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.makeproject.api.ui.BrokenIncludes;

/**
 *
 * @author Alexander Simon
 */
public class BrokenProjectService implements BrokenIncludes, ChangeListener {
    private static WeakHashMap<ChangeListener,Boolean> listeners = new WeakHashMap<ChangeListener,Boolean>();
    
    public BrokenProjectService() {
    }

    public boolean isBroken(NativeProject project) {
        return BadgeProvider.getInstance().isBroken(project);
    }

    public void addChangeListener(ChangeListener provider){
        listeners.put(provider,Boolean.TRUE);
    }

    public void removeChangeListener(ChangeListener provider){
        listeners.remove(provider);
    }

    public void stateChanged(ChangeEvent e) {
        List<ChangeListener> list = new ArrayList<ChangeListener>(listeners.keySet());
        for (ChangeListener provider : list){
            provider.stateChanged(e);
        }
    }
}
