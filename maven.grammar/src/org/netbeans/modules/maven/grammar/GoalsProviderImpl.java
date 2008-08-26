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

package org.netbeans.modules.maven.grammar;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.maven.spi.grammar.GoalsProvider;
import org.netbeans.modules.maven.embedder.MavenSettingsSingleton;
import hidden.org.codehaus.plexus.util.IOUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author mkleint
 */
public class GoalsProviderImpl implements GoalsProvider {
    
    /** Creates a new instance of GoalsProviderImpl */
    public GoalsProviderImpl() {
    }
    
    private WeakReference<Set<String>> goalsRef = null;
    
    public Set<String> getAvailableGoals() {
        Set<String> cached = goalsRef != null ? goalsRef.get() :null;
        if (cached == null) {
            File expandedPath = InstalledFileLocator.getDefault().locate("maven2/maven-plugins-xml", null, false); //NOI18N
            assert expandedPath != null : "Shall have path expanded.."; //NOI18N
            //TODO we should have a "resolved instance here with defaults injected correctly
            List<String> groups = MavenSettingsSingleton.getInstance().getSettings().getPluginGroups();
            groups.add("org.apache.maven.plugins"); //NOI18N
            groups.add("org.codehaus.mojo"); //NOI18N
            cached = new TreeSet<String>();
            for (String group : groups) {
                File folder = new File(expandedPath, group.replace('.', File.separatorChar));
                checkFolder(folder, cached, false);
            }
            goalsRef = new WeakReference<Set<String>>(cached);
        }
        return cached;
        
    }

    private void checkFolder(File parent, Set<String> list, boolean recurs) {
        File[] files = parent.listFiles();
        if (files == null) {
            //fix for #100894, happens when a plugin group is defined but not in our list.
            return;
        }
        boolean hasFile = false;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory() && recurs) {
                checkFolder(file, list, recurs);
            }
            if (file.isFile()) {
                InputStream str = null;
                try {
                    str = new FileInputStream(file);
                    SAXBuilder builder = new SAXBuilder();
                    //TODO jdom document tree is probably not the most memory effective way of doing things..
                    Document doc = builder.build(str);
                    Iterator it = doc.getRootElement().getDescendants(new Filter() {
                        public boolean matches(Object object) {
                            if (object instanceof Element) {
                                Element el = (Element)object;
                                if ("goal".equals(el.getName()) &&  //NOI18N
                                        el.getParentElement() != null && 
                                        "mojo".equals(el.getParentElement().getName())) { //NOI18N
                                    return true;
                                }
                            }
                            return false;
                        }
                    });
                    String prefix = doc.getRootElement().getChildTextTrim("goalPrefix"); //NOI18N
                    assert prefix != null : "No prefix for " + file.getAbsolutePath(); //NOI18N
                    while (it.hasNext()) {
                        Element goal = (Element)it.next();
                        list.add(prefix + ":" + goal.getText());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    IOUtil.close(str);
                }
                
            }
        }
    }
    
    
}
