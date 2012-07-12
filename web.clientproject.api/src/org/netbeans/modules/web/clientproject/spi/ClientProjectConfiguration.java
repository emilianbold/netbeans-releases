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
package org.netbeans.modules.web.clientproject.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.Comparator;
import java.util.Properties;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.spi.project.ProjectConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Becicka
 */
public final class ClientProjectConfiguration implements ProjectConfiguration {

    private final String name;
    private String displayName;
    private final String type;
    private final EditableProperties props;
    private final FileObject file;
    private final int importance;
    private WebBrowser browser;
    
    public static final Comparator COMPARATOR = new ConfigurationComparator();

    private ClientProjectConfiguration(FileObject kid, String name, String displayName, String type) {
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        props = new EditableProperties(true);
        props.put("type", type);
        props.put("display.name", displayName);
        this.file = kid;
        this.importance = 999;
    }
    
    private ClientProjectConfiguration(WebBrowser browser, int importance) {
        this.name = browser.getId();
        this.type = browser.getId();
        this.importance = importance;
        this.file = null;
        this.props = null;
        this.browser = browser;
    }

    @Override
    public String getDisplayName() {
        if (displayName == null) {
            assert browser != null;
            // delay retrieving browser's name; it is accessed via DataObject.getNodeDelegate
            // and if retrieved too early it will cause "IllegalStateException: Should not acquire Children.MUTEX while holding ProjectManager.mutex()"
            displayName = browser.getName();
        }
        return displayName;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ClientProjectConfiguration) && Utilities.compareObjects(name, ((ClientProjectConfiguration) o).name);
    }

    @Override
    public String toString() {
        return "ClientProjectConfiguration[" + name + "," + displayName + "]"; // NOI18N
    }

    public WebBrowser getBrowser() {
        return browser;
    }
    
    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
    
    public String getProperty(String prop) {
        return props.getProperty(prop);
    }
    
    public String putProperty(String prop, String value) {
        return props.put(prop, value);
    }

    public static ClientProjectConfiguration create(FileObject configFile) {

        try {
            InputStream is = configFile.getInputStream();
            try {
                Properties p = new Properties();
                p.load(is);
                String name = configFile.getName();
                String label = p.getProperty("display.name"); // NOI18N
                String type = p.getProperty("type");
                return new ClientProjectConfiguration(configFile, name, label != null ? label : name, type);
            } finally {
                is.close();
            }
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }
    
    public static ClientProjectConfiguration create(WebBrowser browser) {
        if (browser.getId().endsWith("webviewBrowser")) {
            return new ClientProjectConfiguration(browser, 1);
        } else if (browser.getBrowserFamily() == BrowserFamilyId.CHROME || browser.getId().endsWith("ChromeBrowser")) {
            return new ClientProjectConfiguration(browser, 2);
        } else if (browser.getBrowserFamily() == BrowserFamilyId.CHROMIUM || browser.getId().endsWith("ChromiumBrowser")) {
            return new ClientProjectConfiguration(browser, 3);
        } else {
            return null;
        }
    }
    
    private static class ConfigurationComparator implements Comparator<ClientProjectConfiguration> {

        @Override
        public int compare(ClientProjectConfiguration o1, ClientProjectConfiguration o2) {
            if (o1.importance != o2.importance) {
                return o1.importance - o2.importance;
            } else {
                Collator c = Collator.getInstance();
                if (o1.importance <= 3) {
                    // TODO: do not sort browsers by their displayName otherwise you get
                    //       "IllegalStateException: Should not acquire Children.MUTEX while holding ProjectManager.mutex()"
                    return c.compare(o1.getName(), o2.getName());
                } else {
                    return c.compare(o1.getDisplayName(), o2.getDisplayName());
                }
            }
        }
        
    }

    public void save() {
        if (file == null) {
            return;
        }
        OutputStream os = null;
        try {
            os = file.getOutputStream();
            try {
                props.store(os);
            } finally {
                os.close();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                os.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
