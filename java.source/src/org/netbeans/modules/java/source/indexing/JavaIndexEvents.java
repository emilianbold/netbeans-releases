/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.indexing;

import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * Events related to Java Indexer operations. Currently only supports an event informing
 * that a source root has been scanned and index created. 
 * 
 * @author sdedic
 */
public final class JavaIndexEvents {
    private static final JavaIndexEvents DEFAULT = new JavaIndexEvents();
    
    public interface Listener {
        public void indexUpdated(IndexEvent event);
    }
    
    public final static class IndexEvent extends EventObject { 
        private URL root;

        IndexEvent(URL root, Object source) {
            super(source);
            this.root = root;
        }

        public URL getRoot() {
            return root;
        }
    }

    public static JavaIndexEvents getDefault() {
        return DEFAULT;
    }
    
    public void addIndexEventListener(Listener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeIndexEventListener(Listener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    void fireIndexUpdated(URL root) {
        List<Listener> ll;
        
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
            ll = new ArrayList<Listener>(listeners);
        }
        IndexEvent evt = new IndexEvent(root, this);
        for (Listener l : ll) {
            l.indexUpdated(evt);
        }
    }
    
    private List<Listener>  listeners = new ArrayList<Listener>();
}
