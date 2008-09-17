/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.jconsole;

import java.util.prefs.Preferences;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;


public class JConsoleSettings implements ChangeListener
{
    private static final JConsoleSettings INSTANCE = new JConsoleSettings();

    private static final String PROP_POLLING     = "POLLING"; // NOI18N
    private static final String PROP_TILE   = "TILE"; // NOI18N
    private static final String PROP_CLASSPATH  = "CLASSPATH"; // NOI18N
    private static final String PROP_PLUGINSPATH  = "PLUGINS"; // NOI18N
    private static final String PROP_VM_OPTIONS = "VM_OPTIONS"; //NOI18N
    private static final String PROP_OTHER_ARGS = "OTHER_ARGS"; //NOI18N
    private static final String PROP_URL = "DEFAULT_URL"; //NOI18N
    
    public static String NETBEANS_CLASS_PATH;
    
    private static Boolean greater;
    static {
         NETBEANS_CLASS_PATH = System.getProperty("java.class.path");// NOI18N
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(JConsoleSettings.class);
    }
    protected void initialize () 
    {        
        setPolling(4);
    }
    
    public static boolean isNetBeansJVMGreaterThanJDK15() {
        if(greater == null) {
            //Check if we are running on 1.6 minimum
            try {
                Class.forName("javax.swing.SwingWorker");// NOI18N
               greater = true;
            }catch(ClassNotFoundException e) {
                greater = false;
            }
        }
        return greater;
    }
    
    public String displayName ()
    {
        return "JConsole settings";// NOI18N
    }    

    public static JConsoleSettings getDefault () 
    {
        return INSTANCE;
    }
    
    public void stateChanged (ChangeEvent e)
    {
    }
    
    public Boolean getTile()
    {
        return getPreferences().getBoolean(PROP_TILE, true);
    }

    public void setTile(Boolean tile)
    {
        getPreferences().putBoolean(PROP_TILE, tile);
    }
    
    public String getClassPath()
    {
        return getPreferences().get(PROP_CLASSPATH, null);
    }

    public void setClassPath(String value)
    {
        getPreferences().put(PROP_CLASSPATH, value);
    }
    
    public String getPluginsPath()
    {
        return getPreferences().get(PROP_PLUGINSPATH, null);
    }

    public void setPluginsPath(String value)
    {
        getPreferences().put(PROP_PLUGINSPATH, value);
    }
    
    public Integer getPolling()
    {
        return getPreferences().getInt(PROP_POLLING, 4);
    }
    
    public void setPolling(Integer polling)
    {   
        getPreferences().putInt(PROP_POLLING, polling);
    }
    
    public String getVMOptions()
    {
        return getPreferences().get(PROP_VM_OPTIONS, null);
    }
    
    public void setVMOptions(String other)
    {
        getPreferences().put(PROP_VM_OPTIONS, other);
    }
    
    public String getOtherArgs()
    {
        return getPreferences().get(PROP_OTHER_ARGS, null);
    }
    
    public void setOtherArgs(String other)
    {
        getPreferences().put(PROP_OTHER_ARGS, other);
    }
    
    public String getDefaultUrl()
    {
        return getPreferences().get(PROP_URL, null);
    }
    
    public void setDefaultUrl(String url)
    {
        getPreferences().put(PROP_URL, url);
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx("jconsole_standalone"); // NOI18N
    }
}
