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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.web.common.reload;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.netbeans.core.IDESettings;
import org.openide.awt.HtmlBrowser.Factory;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public class BrowserReload {
    
    
    private BrowserReload(){
        pluginMap =new ConcurrentHashMap<FileObject, Couple<BrowserPlugin,? extends Couple>>();
    }
    
    public void register( FileObject localFileObject, String browserUrl ){
        // TODO : probably url should be modified somehow for identification it in the browser plugin
        if ( browserUrl == null || localFileObject == null ){
            return;
        }
        BrowserPlugin plugin = getPlugin();
        if ( plugin == null ){
            return;
        }
        plugin.register(localFileObject, browserUrl);
    }
    
    public boolean canReload( FileObject fileObject ){
        Couple<BrowserPlugin, ? extends Couple> couple = getPluginMap().get( fileObject );
        if ( couple == null ){
            return false;
        }
        return  couple.getStart()!= null && couple.getEnd()!= null ;
    }
    
    public void reload( FileObject fileObject ){
        Couple<BrowserPlugin, ? extends Couple> couple = getPluginMap().get( fileObject );
        if ( couple == null  ){
            return;
        }
        couple.getStart().reload( fileObject );
    }
    
    public void clear( FileObject fileObject ){
        if ( fileObject == null ){
            return;
        }
        Couple<BrowserPlugin, ? extends Couple> couple = pluginMap.get( fileObject );
        if ( couple!= null ){
            couple.getStart().clear(fileObject);
        }
        pluginMap.remove( fileObject );
    }
    
    public boolean isScopedArtifact( FileObject artifact ){
        /*
         *  TODO : check project of artifact against "registered" projects. 
         *  Return false is it doesn't match.
         */
        return true;
    }
    
    private BrowserPlugin getPlugin(){
        Factory wwwBrowser = IDESettings.getWWWBrowser();
        // TODO: handle only known accessors
        if ( wwwBrowser != null && "org.netbeans.core.browser.webview.BrowserFactory".
                equals( wwwBrowser.getClass().getCanonicalName()))
        {
            return webViewAccessor.getPlugin();
        }            
        return defaultAccessor.getPlugin();
    }
    
    Map<FileObject,Couple<BrowserPlugin,? extends Couple>> getPluginMap(){
        return pluginMap;
    }
    
    public static BrowserReload getInstance(){
        return INSTANCE;
    }
    
    private static final BrowserReload INSTANCE = new BrowserReload();
    private Map<FileObject,Couple<BrowserPlugin,? extends Couple>> pluginMap;
    private PluginAccessor webViewAccessor = new WebViewAccessor();
    private PluginAccessor defaultAccessor = new ExternalBrowserAccessor();
}
