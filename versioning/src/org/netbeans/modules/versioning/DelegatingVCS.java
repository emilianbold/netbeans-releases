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
package org.netbeans.modules.versioning;

import java.awt.Image;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.versioning.core.util.Utils;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import org.netbeans.modules.versioning.core.spi.VersioningSystem;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSVisibilityQuery;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.spi.queries.CollocationQueryImplementation;
import org.openide.util.ContextAwareAction;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka
 */
public class DelegatingVCS extends org.netbeans.modules.versioning.core.spi.VersioningSystem implements VCSSystemProvider.VersioningSystem<org.netbeans.modules.versioning.spi.VersioningSystem> {

    private final Map<?, ?> map;
    private org.netbeans.modules.versioning.spi.VersioningSystem delegate;
    private Set<String> metadataFolderNames;
    private final Object DELEGATE_LOCK = new Object();
    
    private final String displayName;
    private final String menuLabel;
    
    private static Logger LOG = Logger.getLogger(DelegatingVCS.class.getName());
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    public static DelegatingVCS create(Map<?, ?> map) {
        return new DelegatingVCS(map);
    }
    private VCSAnnotator annotator;
    private VCSVisibilityQuery visibilityQuery;
    private VCSInterceptor interceptor;
    
    private DelegatingVCS(Map<?, ?> map) {
        this.map = map;
        this.displayName = (String) map.get("displayName");
        this.menuLabel = (String) map.get("menuLabel");
        
        LOG.log(Level.FINE, "Created DelegatingVCS for : {0}", map.get("displayName")); // NOI18N
    }

    public DelegatingVCS(org.netbeans.modules.versioning.spi.VersioningSystem vs) {
        this.map = null;
        this.displayName = (String) vs.getProperty(org.netbeans.modules.versioning.spi.VersioningSystem.PROP_DISPLAY_NAME);
        this.menuLabel = (String) vs.getProperty(org.netbeans.modules.versioning.spi.VersioningSystem.PROP_MENU_LABEL);
        this.delegate = vs;
        
        LOG.log(Level.FINE, "Created DelegatingVCS for : {0}", displayName); // NOI18N
    }

    public org.netbeans.modules.versioning.spi.VersioningSystem getDelegate() {
        synchronized(DELEGATE_LOCK) {
            if(delegate == null) {
                Utils.flushNullOwners();   
                delegate = (org.netbeans.modules.versioning.spi.VersioningSystem) map.get("delegate");                  // NOI18N
                if(delegate != null) {
                    synchronized(support) {
                        PropertyChangeListener[] listeners = support.getPropertyChangeListeners();
                        for (PropertyChangeListener l : listeners) {
                            delegate.addPropertyChangeListener(l);
                            support.removePropertyChangeListener(l);
                        }
                    }
                } else {
                    LOG.log(Level.WARNING, "Couldn't create delegate for : {0}", map.get("displayName")); // NOI18N
                }
            }
            return delegate;
        }
    }
    
    @Override
    public org.netbeans.modules.versioning.core.spi.VCSVisibilityQuery getVisibilityQuery() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public org.netbeans.modules.versioning.core.spi.VCSInterceptor getVCSInterceptor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public org.netbeans.modules.versioning.core.spi.VCSAnnotator getVCSAnnotator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getOriginalFile(VCSFileProxy workingCopy, VCSFileProxy originalFile) {
        getDelegate().getOriginalFile(workingCopy.toFile(), originalFile.toFile());
    }

    @Override
    public CollocationQueryImplementation getCollocationQueryImplementation() {
        return getDelegate().getCollocationQueryImplementation();
    }

    @Override
    public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {
        if(!isAlive()) {
            if(getMetadataFolderNames().contains(file.getName()) && file.isDirectory()) {
                LOG.log(
                        Level.FINE, 
                        "will awake VCS {0} because of metadata folder {1}",// NOI18N 
                        new Object[]{displayName, file}); 

                File f = getDelegate().getTopmostManagedAncestor(file.toFile());
                if(f != null) {
                    return VCSFileProxy.createFileProxy(f);
                }
            } 
            if(hasMetadata(file)) {
                LOG.log(
                        Level.FINE, 
                        "will awake VCS {0} because {1} contains matadata",     // NOI18N
                        new Object[]{displayName, file});
                
                
                File f = getDelegate().getTopmostManagedAncestor(file.toFile());
                if(f != null) {
                    return VCSFileProxy.createFileProxy(f);
                }
            }
        } else {
            File f = getDelegate().getTopmostManagedAncestor(file.toFile());
            if(f != null) {
                return VCSFileProxy.createFileProxy(f);
            }
        }
        return null;
    }

    @Override
    public boolean isLocalHistory() {
        if(!isAlive()) {
            return false;
        }
        return getDelegate().getProperty(org.netbeans.modules.versioning.spi.VersioningSystem.PROP_LOCALHISTORY_VCS) != null;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String getMenuLabel() {
        return menuLabel;
    }

    public final void addPropertyCL(PropertyChangeListener listener) {
        synchronized(support) {
            support.addPropertyChangeListener(listener);
        }
    }
    
    public final void removePropertyCL(PropertyChangeListener listener) {
        synchronized(support) {
            support.removePropertyChangeListener(listener);
        }
    }
    
    @Override
    public boolean isExcluded(VCSFileProxy file) {
        return VersioningSupport.isExcluded(file.toFile());
    }

    @Override
    public VCSAnnotator getAnnotator() {
        if(annotator == null && getDelegate().getVCSAnnotator() != null) {
            annotator = new VCSAnnotator() {
                @Override
                public String annotateName(String name, org.netbeans.modules.versioning.core.spi.VCSContext context) {
                    return getDelegate().getVCSAnnotator().annotateName(name, Accessor.IMPL.createVCSContext(context));
                }
                @Override
                public Image annotateIcon(Image icon, org.netbeans.modules.versioning.core.spi.VCSContext context) {
                    return getDelegate().getVCSAnnotator().annotateIcon(icon, Accessor.IMPL.createVCSContext(context));
                }
                @Override
                public Action[] getActions(org.netbeans.modules.versioning.core.spi.VCSContext context, ActionDestination destination) {
                    org.netbeans.modules.versioning.spi.VCSAnnotator.ActionDestination ad;
                    switch(destination) {
                        case MainMenu:
                            ad = org.netbeans.modules.versioning.spi.VCSAnnotator.ActionDestination.MainMenu;
                            break;
                        case PopupMenu:
                            ad = org.netbeans.modules.versioning.spi.VCSAnnotator.ActionDestination.PopupMenu;
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                    return getDelegate().getVCSAnnotator().getActions(Accessor.IMPL.createVCSContext(context), ad);
                }
            };
        }
        return annotator;
    }
    
    @Override
    public VCSVisibilityQuery getVisibility() { 
        if(visibilityQuery == null && getDelegate().getVisibilityQuery() != null) {
            visibilityQuery = new VCSVisibilityQuery() {
                @Override
                public boolean isVisible(VCSFileProxy proxy) {
                    return getDelegate().getVisibilityQuery().isVisible(proxy.toFile());
                }
            };
        }
        return visibilityQuery;
    }
    
    @Override
    public VCSInterceptor getInterceptor() {
        if(interceptor == null && getDelegate().getVCSInterceptor() != null) {
            interceptor = new VCSInterceptor() {

                @Override
                public boolean isMutable(VCSFileProxy file) {
                    return getDelegate().getVCSInterceptor().isMutable(file.toFile());
                }

                @Override
                public Object getAttribute(VCSFileProxy file, String attrName) {
                    return getDelegate().getVCSInterceptor().getAttribute(file.toFile(), attrName);
                }

                @Override
                public boolean beforeDelete(VCSFileProxy file) {
                    return getDelegate().getVCSInterceptor().beforeDelete(file.toFile());
                }

                @Override
                public void doDelete(VCSFileProxy file) throws IOException {
                    getDelegate().getVCSInterceptor().doDelete(file.toFile());
                }

                @Override
                public void afterDelete(VCSFileProxy file) {
                    getDelegate().getVCSInterceptor().afterDelete(file.toFile());
                }

                @Override
                public boolean beforeMove(VCSFileProxy from, VCSFileProxy to) {
                    return getDelegate().getVCSInterceptor().beforeMove(from.toFile(), to.toFile());
                }

                @Override
                public void doMove(VCSFileProxy from, VCSFileProxy to) throws IOException {
                    getDelegate().getVCSInterceptor().doMove(from.toFile(), to.toFile());
                }

                @Override
                public void afterMove(VCSFileProxy from, VCSFileProxy to) {
                    getDelegate().getVCSInterceptor().afterMove(from.toFile(), to.toFile());
                }

                @Override
                public boolean beforeCopy(VCSFileProxy from, VCSFileProxy to) {
                    return getDelegate().getVCSInterceptor().beforeCopy(from.toFile(), to.toFile());
                }

                @Override
                public void doCopy(VCSFileProxy from, VCSFileProxy to) throws IOException {
                    getDelegate().getVCSInterceptor().doCopy(from.toFile(), to.toFile());
                }

                @Override
                public void afterCopy(VCSFileProxy from, VCSFileProxy to) {
                    getDelegate().getVCSInterceptor().afterCopy(from.toFile(), to.toFile());
                }

                @Override
                public boolean beforeCreate(VCSFileProxy file, boolean isDirectory) {
                    return getDelegate().getVCSInterceptor().beforeCreate(file.toFile(), isDirectory);
                }

                @Override
                public void doCreate(VCSFileProxy file, boolean isDirectory) throws IOException {
                    getDelegate().getVCSInterceptor().doCreate(file.toFile(), isDirectory);
                }

                @Override
                public void afterCreate(VCSFileProxy file) {
                    getDelegate().getVCSInterceptor().afterCreate(file.toFile());
                }

                @Override
                public void afterChange(VCSFileProxy file) {
                    getDelegate().getVCSInterceptor().afterChange(file.toFile());
                }

                @Override
                public void beforeChange(VCSFileProxy file) {
                    getDelegate().getVCSInterceptor().beforeChange(file.toFile());
                }

                @Override
                public void beforeEdit(VCSFileProxy file) {
                    getDelegate().getVCSInterceptor().beforeEdit(file.toFile());
                }

                @Override
                public long refreshRecursively(VCSFileProxy dir, long lastTimeStamp, List<VCSFileProxy> children) {
                    List<? super File> files = new ArrayList<File>(children.size());
                    for (Iterator<? super VCSFileProxy> it = children.iterator(); it.hasNext();) {
                        VCSFileProxy fileProxy = (VCSFileProxy) it.next();
                        files.add(fileProxy.toFile());
                    }
                    return getDelegate().getVCSInterceptor().refreshRecursively(dir.toFile(), lastTimeStamp, files);
                }
            };
        }
        return interceptor;
    }
    
    boolean isMetadataFile(VCSFileProxy file) {
        return getMetadataFolderNames().contains(file.getName());
    }

    private Collection<String> getMetadataFolderNames() {
        if(metadataFolderNames == null) {
            metadataFolderNames = new HashSet<String>();
            int i = 0;
            while(true) {
                String name = (String) map.get("metadataFolderName" + i++);
                if(name == null) {
                    break;
                }
                name = parseName(name);
                if(name == null) {
                    continue;
                }
                metadataFolderNames.add(name);
            }
        }
        return metadataFolderNames;
    }
    
    Action[] getActions(org.netbeans.modules.versioning.core.spi.VCSContext ctx, VCSAnnotator.ActionDestination actionDestination) {
        if(map == null || isAlive()) {
            VCSAnnotator annotator = getAnnotator();
            return annotator != null ? annotator.getActions(ctx, actionDestination) : new Action[0];
        } else {
            Action[] ia = getInitActions(ctx);
            Action[] ga = getGlobalActions(ctx);
            
            List<Action> l = new ArrayList<Action>(ia.length + ga.length + 1); // +1 if separator needed
            
            // init actions
            l.addAll(Arrays.asList(ia));
            // add separator if necessary 
            if(ga.length > 0 && ia.length > 0 && l.get(l.size() - 1) != null) {
                l.add(null); 
            }
            // global actions
            l.addAll(Arrays.asList(ga));
            
            return  l.toArray(new Action[l.size()]);
        }        
    }
    
    Action[] getGlobalActions(org.netbeans.modules.versioning.core.spi.VCSContext ctx) {
        assert !isAlive();
        String category = (String) map.get("actionsCategory");              // NOI18N
        List<? extends Action> l = Utilities.actionsForPath("Versioning/" + category + "/Actions/Global"); // NOI18N
        List<Action> ret = new ArrayList<Action>(l.size());
        for (Action action : l) {
            if(action instanceof ContextAwareAction) {
                ret.add(((ContextAwareAction)action).createContextAwareInstance(Lookups.singleton(ctx)));
            } else {
                ret.add(action);
            }
        }        
        return ret != null ? ret.toArray(new Action[ret.size()]) : new Action[0];
    }
    
    Action[] getInitActions(org.netbeans.modules.versioning.core.spi.VCSContext ctx) {
        String category = (String) map.get("actionsCategory");              // NOI18N
        List<? extends Action> l = Utilities.actionsForPath("Versioning/" + category + "/Actions/Unversioned"); // NOI18N
        List<Action> ret = new ArrayList<Action>(l.size());
        for (Action action : l) {
            if(action instanceof ContextAwareAction) {
                ret.add(((ContextAwareAction)action).createContextAwareInstance(Lookups.singleton(ctx)));
            } else {
                ret.add(action);
            }
        }
        return ret.toArray(new Action[ret.size()]);
    }

    boolean isAlive() {
        synchronized(DELEGATE_LOCK) {
            return delegate != null;
        }
    }
    
    private boolean hasMetadata(VCSFileProxy file) {
        if(file == null) {
            return false;
        }
        for(String folderName : getMetadataFolderNames()) {
            VCSFileProxy parent;
            if(file.isDirectory()) {
                parent = file;
            } else {
                parent = file.getParentFile();
            }
            while(parent != null) {
                final boolean metadataFolder = VCSFileProxy.createFileProxy(parent, folderName).exists();
                if(metadataFolder) {
                    LOG.log(
                            Level.FINER, 
                            "found metadata folder {0} for file {1}",           // NOI18N
                            new Object[]{metadataFolder, file});
                    
                    return true;
                }
                parent = parent.getParentFile();
            }
        }
        return false;
    }
    
    /**
     * Testing purposes only!
     */
    void reset() {
        if(map != null) {
            synchronized(DELEGATE_LOCK) {
                delegate = null;
            }
        }
    }

    private String parseName(String name) {
        if(name == null) {
            return null;
        }
        int idx = name.indexOf(":");
        
        if(idx < 0) {
            return name;
        }
        
        String cmd[] = name.split(":");
        
        // "_svn:getenv:SVN_ASP_DOT_NET_HACK:notnull"
        if(cmd.length != 4 || !cmd[1].contains("getenv")) {
            return name;
        } else {
            assert cmd[3].equals("notnull") || cmd[3].equals("null");
            
            boolean notnull = cmd[3].trim().equals("notnull");
            if(notnull) {
                return System.getenv(cmd[2]) != null ? cmd[0] : null;
            } else {
                return System.getenv(cmd[2]) == null ? cmd[0] : null;
            }
        }
    }

}
