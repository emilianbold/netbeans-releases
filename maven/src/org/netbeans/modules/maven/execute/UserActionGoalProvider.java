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

import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.openide.filesystems.FileObject;

/**
 * user defined definitions, to be found in the project directory in the nbactions.xml file.
 * @author mkleint
 */
public class UserActionGoalProvider extends AbstractMavenActionsProvider {
    
    public static final String FILENAME = "nbactions.xml"; //NOI18N
    
    private final NbMavenProjectImpl project;
    private Date lastModified = new Date();
    private boolean lastTimeExists = true;
    /** Creates a new instance of UserActionGoalProvider */
    public UserActionGoalProvider(NbMavenProjectImpl project) {
        this.project = project;
    }
    
    public InputStream getActionDefinitionStream() {
        FileObject fo = project.getProjectDirectory().getFileObject(FILENAME);
        lastTimeExists = fo != null;
        if (fo != null) {
            try {
                lastModified = fo.lastModified();
                return fo.getInputStream();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        lastModified = new Date();
        return null;
    }
  
    
   /**
     * get custom action maven mapping configuration
     * No replacements happen.
     * The instances returned is always a new copy, can be modified or reused.
     * Same method in NbGlobalActionGolaProvider 
     */
    public NetbeansActionMapping[] getCustomMappings() {
        NetbeansActionMapping[] fallbackActions = new NetbeansActionMapping[0];
        
        try {
            List<NetbeansActionMapping> toRet = new ArrayList<NetbeansActionMapping>();
            // just a converter for the To-Object reader..
            Reader read = performDynamicSubstitutions(Collections.EMPTY_MAP, getRawMappingsAsString());
            // basically doing a copy here..
            ActionToGoalMapping mapping = reader.read(read);    
            List lst = mapping.getActions();
            if (lst != null) {
                Iterator it = lst.iterator();
                while(it.hasNext()) {
                    NetbeansActionMapping mapp = (NetbeansActionMapping) it.next();
                    if (mapp.getActionName().startsWith("CUSTOM-")) { //NOI18N
                        toRet.add(mapp);
                    }
                }
            }
            return toRet.toArray(new NetbeansActionMapping[toRet.size()]);
        } catch (XmlPullParserException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return fallbackActions;
    }
    
    @Override
    protected boolean reloadStream() {
        FileObject fo = project.getProjectDirectory().getFileObject(FILENAME);
        boolean prevExists = lastTimeExists;
        lastTimeExists = fo != null;
        return ((fo == null && prevExists) || (fo != null && fo.lastModified().after(lastModified)));
    }
}
