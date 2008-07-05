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

package org.netbeans.modules.cnd.remote.server;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;

/**
 * The definition of a remote server and login. 
 * 
 * @author gordonp
 */
public class RemoteServerRecord implements ServerRecord, PropertyChangeListener, PlatformTypes  {
    
    private String user;
    private String server;
    private String name;
    private boolean editable;
    private boolean active;
    private int platform;
    private boolean inited = false;
    
    protected RemoteServerRecord(String name, boolean active) {
        this.name = name;
        this.active = active;
        editable = !name.equals("localhost"); // NOI18N
        //platform = getPlatform();
        //inited = true;
    }
    
    public boolean isEditable() {
        return editable;
    }

    public boolean isRemote() {
        return !name.equals("localhost"); // NOI18N
    }

    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        if (this.active != active) {
            RemoteServerList.getInstance().firePropertyChange(RemoteServerList.PROP_SET_AS_ACTIVE, this);
            RemoteServerList.getInstance().refresh();
        }
    }

    public String getName() {
        return name;
    }

    public String getServerName() {
        return server;
    }

    public String getUserName() {
        return user;
    }
    
    public int getPlatform() {
        if (!inited) {
            if (name.equals("localhost")) { // NOI18N
                String os = System.getProperty("os.name");
                if (os.equals("SunOS")) { // NOI18N
                    platform = System.getProperty("os.arch").equals("x86") ? PLATFORM_SOLARIS_INTEL : PLATFORM_SOLARIS_SPARC; // NOI18N
                } else if (os.startsWith("Windows ")) { // NOI18N
                    platform =  PLATFORM_WINDOWS;
                } else if (os.toLowerCase().contains("linux")) { // NOI18N
                    platform =  PLATFORM_LINUX;
                } else if (os.toLowerCase().contains("mac")) { // NOI18N
                    platform =  PLATFORM_MACOSX;
                } else {
                    platform =  PLATFORM_GENERIC;
                }
            } else {
                String cmd = "PATH=/bin:/usr/bin:$PATH uname -s -m"; // NOI18N
                RemoteCommandSupport support = new RemoteCommandSupport(name, cmd);
                String val = support.toString().toLowerCase();
                if (val.startsWith("linux")) { // NOI18N
                    platform =  PLATFORM_LINUX;
                } else if (val.startsWith("sunos")) { // NOI18N
                    String os = val.substring(val.indexOf(' '));
                    return os.startsWith("sun") ? PLATFORM_SOLARIS_SPARC : PLATFORM_SOLARIS_INTEL; // NOI18N
                } else if (val.startsWith("cygwin") || val.startsWith("mingw32")) { // NOI18N
                    platform =  PLATFORM_WINDOWS;
                }
            }
            inited = true;
        }
        return platform;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RemoteServerList.PROP_SET_AS_ACTIVE)) {
            Object n = evt.getNewValue();
            active = n == this;
        }
    }
}
