/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.execute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Action;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * an output tab manager. 
 * @author mkleint
 */
public abstract class OutputTabMaintainer {

    /**
     * All tabs which were used for some process which has now ended.
     * These are closed when you start a fresh process.
     * Map from tab to tab display name.
     */
    protected static final Map<InputOutput, Collection<String>> freeTabs =
            new WeakHashMap<InputOutput, Collection<String>>();
    
    protected InputOutput io;
    private String name;
    
    protected OutputTabMaintainer(String name) {
        this.name = name;
    }
    
    
    protected final void markFreeTab() {
        synchronized (freeTabs) {
            assert io != null;
            freeTabs.put(io, createContext());
        }
    }
    
    protected void reassignAdditionalContext(Iterator it) {
        
    }
    
    protected Collection<String> createContext() {
        Collection<String> toRet = new ArrayList<String>();
        toRet.add(name);
        toRet.add(this.getClass().getName());
        return toRet;
    }
    
    protected Action[] createNewTabActions() {
        return new Action[0];
    }
    
    public final InputOutput getInputOutput() {
        if (io == null) {
            io = createInputOutput();
        }
        return io;
    }
    
    protected final InputOutput createInputOutput() {
        synchronized (freeTabs) {
            for (Map.Entry<InputOutput, Collection<String>> entry : freeTabs.entrySet()) {
                InputOutput free = entry.getKey();
                Iterator<String> vals = entry.getValue().iterator();
                String freeName = vals.next();
                String type = vals.next();
                if (io == null && freeName.equals(name) && type.equals(this.getClass().getName())) {
                    // Reuse it.
                    io = free;
                    reassignAdditionalContext(vals);
                    try {
                        io.getOut().reset();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    // useless: io.flushReader();
                } else {
                    // Discard it.
                    free.closeInputOutput();
                }
            }
            freeTabs.clear();
        }
        //                }
        if (io == null) {
            io = IOProvider.getDefault().getIO(name, createNewTabActions());
            io.setInputVisible(true);
        }
        return io;
    }    

}
