/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.versioning.core.spi;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.versioning.fileproxy.api.VCSFileProxy;
import org.netbeans.modules.versioning.fileproxy.spi.VCSContext;
import org.netbeans.spi.queries.CollocationQueryImplementation;
import org.openide.util.LookupListener;

/**
 *
 * @author tomas
 */
public abstract class VCSSystemProvider<S> {

    public abstract void addLookupListener(LookupListener l);
    
    public abstract Collection<S> getVersioningSystems();
    
    public interface VersioningSystem<S> {
        
        S getDelegate();
        
        public boolean isLocalHistory();
        
        public Object getProp(String key);

//        void putProperty(String key, Object value);

        public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file);

        public Annotator getAnnotator();

        public Interceptor getInterceptor();

        public void getOriginalFile(VCSFileProxy workingCopy, VCSFileProxy originalFile);

        public CollocationQueryImplementation getCollocationQueryImplementation();

        public VisibilityQuery getVisibility();

        public void addPropertyCL(PropertyChangeListener listener);

        public void removePropertyCL(PropertyChangeListener listener);

        public boolean isExcluded(VCSFileProxy file);
        
    }
    
    public interface VisibilityQuery {
        boolean isVisible(VCSFileProxy file);
    }
    
    public interface Annotator {
        public enum ActionDestination { MainMenu, PopupMenu }; 

        public String annotateName(String name, VCSContext context);

        public Image annotateIcon(Image icon, VCSContext context);
                
        public Action[] getActions(VCSContext context, ActionDestination destination);
    }
    
    public interface Interceptor {
        public boolean isMutable(VCSFileProxy file);
        public Object getAttribute(VCSFileProxy file, String attrName);
        public boolean beforeDelete(VCSFileProxy file);
        public void doDelete(VCSFileProxy file) throws IOException;
        public void afterDelete(VCSFileProxy file);
        public boolean beforeMove(VCSFileProxy from, VCSFileProxy to);
        public void doMove(VCSFileProxy from, VCSFileProxy to) throws IOException;
        public void afterMove(VCSFileProxy from, VCSFileProxy to);
        public boolean beforeCopy(VCSFileProxy from, VCSFileProxy to);
        public void doCopy(VCSFileProxy from, VCSFileProxy to) throws IOException;
        public void afterCopy(VCSFileProxy from, VCSFileProxy to);
        public boolean beforeCreate(VCSFileProxy file, boolean isDirectory);
        public void doCreate(VCSFileProxy file, boolean isDirectory) throws IOException;
        public void afterCreate(VCSFileProxy file);
        public void afterChange(VCSFileProxy file);
        public void beforeChange(VCSFileProxy file);
        public void beforeEdit(VCSFileProxy file);
        public long refreshRecursively(VCSFileProxy dir, long lastTimeStamp, List<VCSFileProxy> children);
    }
    
}
