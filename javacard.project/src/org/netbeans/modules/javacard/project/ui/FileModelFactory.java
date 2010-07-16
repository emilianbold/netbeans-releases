/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.javacard.project.ui;

import com.sun.javacard.AID;
import com.sun.javacard.filemodels.*;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author Tim Boudreau
 */
public class FileModelFactory {

    public static final String ORDER = "order"; //NOI18N
    public static final String DISPLAY_NAME = "display-name"; //NOI18N
    public static final String APPLET_CLASS = "applet-class"; //NOI18N
    public static final String APPLET_AID = "applet-AID"; //NOI18N
    public static final String DEFAULT = "default";
    public static final String SERVLET_MAPPING = "mapping"; //NOI18N
    public static final String SERVLET_NAME = "servletName"; //NOI18N

    public static AppletXmlModel appletXmlModel(FileObject fo, ParseErrorHandler handler) throws IOException {
        InputStream in = fo.getInputStream();
        try {
            AppletXmlModel mdl = new AppletXmlModel(in, handler);
            return mdl;
        } finally {
            in.close();
        }
    }

    public static AppletXmlModel appletXmlModel(Node[] nodes) {
        AppletXmlModel result = new AppletXmlModel();
        for (Node n : nodes) {
            String name = (String) n.getValue(DISPLAY_NAME);
            AID aid = (AID) n.getValue(APPLET_AID);
            Integer order = (Integer) n.getValue(ORDER);
            String clazz = n.getLookup().lookup(String.class);
            boolean selected = Boolean.TRUE.equals(n.getValue(CheckboxListView.SELECTED));
            if (clazz != null && selected) {
                int o = order == null ? Integer.MAX_VALUE : order;
                AppletXmlAppletEntry info = new AppletXmlAppletEntry(name, clazz, aid, o);
                result.add(info);
            }
        }
        result.close();
        return result;
    }

    public static WebXmlModel webXmlModel(FileObject fo, ParseErrorHandler handler) throws IOException {
        InputStream in = fo.getInputStream();
        try {
            WebXmlModel mdl = new WebXmlModel(in, handler);
            return mdl;
        } finally {
            in.close();
        }
    }

    public static WebXmlModel webXmlModel(Node[] nodes) {
        WebXmlModel result = new WebXmlModel();
        for (Node n : nodes) {
            String mapping = (String) n.getValue(SERVLET_MAPPING);
            String name = (String) n.getValue(SERVLET_NAME);
            Integer order = (Integer) n.getValue(ORDER);
            String clazz = n.getLookup().lookup(String.class);
            boolean selected = Boolean.TRUE.equals(n.getValue(CheckboxListView.SELECTED));
            boolean isDefault = Boolean.TRUE.equals(n.getValue(DEFAULT));
            if (isDefault) {
                result.setDefaultServlet(name);
                result.setDefaultMapping(mapping);
            }
            if (clazz != null && selected) {
                int o = order == null ? Integer.MAX_VALUE : order;
                WebXmlServletEntry info = new WebXmlServletEntry(name, clazz, mapping, o);
                result.add(info);
            }
        }
        return result;
    }

    public static DeploymentXmlModel deploymentXmlModel(FileObject fo, ParseErrorHandler handler) throws IOException {
        InputStream in = fo.getInputStream();
        try {
            return new DeploymentXmlModel(in, handler);
        } finally {
            in.close();
        }
    }

    public static final String DEPLOYMENT_ENTRY = "deploymentInfo";
    public static DeploymentXmlModel deploymentXmlModel(Node[] nodes) {
        DeploymentXmlModel result = new DeploymentXmlModel();
        for (Node n : nodes) {
            if (!Boolean.TRUE.equals(n.getValue(CheckboxListView.SELECTED))) {
                continue;
            }
            String classname = n.getLookup().lookup(String.class);
            AID aid = (AID) n.getValue (APPLET_AID);
            DeploymentXmlAppletEntry entry = (DeploymentXmlAppletEntry)
                    n.getValue(DEPLOYMENT_ENTRY);
            if (!classname.equals(entry.getClazzHint())) {
                entry.setClazzHint(classname);
            }
            if (entry.getAppletAid() == null || (aid != null && !aid.equals(entry.getAppletAid()))) {
                entry.setAppletAid(aid);
            }
            String displayName = (String) n.getValue (DISPLAY_NAME);
            if (displayName != null && !displayName.equals(entry.getDisplayNameHint())) {
                entry.setDisplayNameHint(displayName);
            }
            if (!entry.isEmpty()) {
                result.add (entry);
            }
        }
        result.close();
        return result;
    }

    public static void writeTo(DeploymentXmlModel mdl, Node[] nodes) {
        for (Node n : nodes) {
            String classname = n.getLookup().lookup(String.class);
            DeploymentXmlAppletEntry byAid = null;
            DeploymentXmlAppletEntry byClassName = null;
            DeploymentXmlAppletEntry byDisplayName = null;
            for (DeploymentXmlAppletEntry e : mdl.getData()) {
                AID aid = e.getAppletAid();
                if (aid != null && aid.equals(n.getValue(APPLET_AID))) {
                    byAid = e;
                }
                if (classname.equals(e.getClazzHint())) {
                    byClassName = e;
                }

                String displayName = e.getDisplayNameHint();
                if (displayName != null && displayName.equals(n.getValue(DISPLAY_NAME))) {
                    byDisplayName = e;
                }

                if (byClassName != null && byAid != null) {
                    break;
                }
            }
            DeploymentXmlAppletEntry entry = byAid != null ? byAid :
                byClassName != null ? byClassName : byDisplayName != null ?
                    byDisplayName : null;
            if (entry == null) {
                entry = new DeploymentXmlAppletEntry();
            } else {
                //Make a new instance so we aren't altering the original
                //model by calling methods here
                entry = (DeploymentXmlAppletEntry) entry.clone();
            }
            n.setValue (DEPLOYMENT_ENTRY, entry);
        }
    }

    public static void writeTo(AppletXmlModel mdl, Node[] nodes) {
        if (mdl.isError()) {
            return;
        }
        List<? extends AppletXmlAppletEntry> dataCopy = mdl.getData();
        for (Node n : nodes) {
            String classname = n.getLookup().lookup(String.class);
            if (classname != null) {
                boolean matched = false;
                for (AppletXmlAppletEntry info : dataCopy) {
                    if (classname.equals(info.getClassname())) {
                        matched = true;
                        if (info.getAID() != null) {
                            n.setValue(APPLET_AID, info.getAID());
                        }
                        if (info.getDisplayName() != null) {
                            n.setValue(DISPLAY_NAME, info.getDisplayName());
                        }
                        n.setValue(ORDER, info.getOrder());
                        n.setValue(CheckboxListView.SELECTED, Boolean.TRUE);
                        break;
                    }
                }
                if (!matched) {
                    n.setValue(CheckboxListView.SELECTED, Boolean.FALSE);
                }
            }
        }
    }

    public static void writeTo(WebXmlModel mdl, Node[] nodes) {
        if (mdl.isError()) {
            return;
        }
        List<? extends WebXmlServletEntry> dataCopy = mdl.getData();
        for (Node n : nodes) {
            String classname = n.getLookup().lookup(String.class);
            if (classname != null) {
                boolean matched = false;
                for (WebXmlServletEntry info : dataCopy) {
                    if (classname.equals(info.getType())) {
                        matched = true;
                        if (info.getMapping() != null) {
                            n.setValue(SERVLET_MAPPING, info.getMapping());
                        }
                        if (info.getName() != null) {
                            n.setValue(SERVLET_NAME, info.getName());
                        }
                        n.setValue(ORDER, info.getOrder());
                        n.setValue(CheckboxListView.SELECTED, Boolean.TRUE);
                        break;
                    }
                }
                if (!matched) {
                    n.setValue(CheckboxListView.SELECTED, Boolean.FALSE);
                }
            }
        }
    }
}
