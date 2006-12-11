/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java;

import java.awt.Image;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.api.queries.FileBuiltQuery.Status;
import org.netbeans.spi.java.loaders.RenameHandler;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * The node representation of Java source files.
 */
public final class JavaNode extends DataNode implements ChangeListener {

    /** generated Serialized Version UID */
    private static final long serialVersionUID = -7396485743899766258L;

    private static final String JAVA_ICON_BASE = "org/netbeans/modules/java/resources/class.gif"; // NOI18N
    private static final String CLASS_ICON_BASE = "org/netbeans/modules/java/resources/clazz.gif"; // NOI18N

    private static final Image NEEDS_COMPILE = Utilities.loadImage("org/netbeans/modules/java/resources/needs-compile.gif");
    
    private final Status status;
    private final AtomicBoolean isCompiled;

    /** Create a node for the Java data object using the default children.
    * @param jdo the data object to represent
    */
    public JavaNode (DataObject jdo, boolean isJavaSource) {
        super (jdo, Children.LEAF);
        this.setIconBaseWithExtension(isJavaSource ? JAVA_ICON_BASE : CLASS_ICON_BASE);
        
        if (isJavaSource) {
            FileObject jf = jdo.getPrimaryFile();
            
            this.isCompiled = new AtomicBoolean(true);
            
            status = FileBuiltQuery.getStatus(jf);
            
            if (status != null) {
                status.addChangeListener(WeakListeners.change(this, status));
                
                queue.add(new Task(false, true, this));
            }
        } else {
            this.status = null;
            this.isCompiled = null;
        }
    }
    
    public void setName(String name) {
        RenameHandler handler = getRenameHandler();
        if (handler == null) {
            super.setName(name);
        } else {
            try {
                handler.handleRename(JavaNode.this, name);
            } catch (IllegalArgumentException ioe) {
                super.setName(name);
            }
        }
    }
    
    private static synchronized RenameHandler getRenameHandler() {
        Collection<? extends RenameHandler> handlers = (Lookup.getDefault().lookupAll(RenameHandler.class)) ;
        if (handlers.size()==0)
            return null;
        if (handlers.size()>1)
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Multiple instances of RenameHandler found in Lookup; only using first one: " + handlers); //NOI18N
        return handlers.iterator().next();
    }    

    public void stateChanged(ChangeEvent e) {
        queue.add(new Task(false, true, this));
    }
    
    public Image getIcon(int type) {
        Image i = super.getIcon(type);
        
        return enhanceIcon(i);
    }
    
    public Image getOpenedIcon(int type) {
        Image i = super.getOpenedIcon(type);
        
        return enhanceIcon(i);
    }
    
    private Image enhanceIcon(Image i) {
        if (!isCompiled.get()) {
            i = Utilities.mergeImages(i, NEEDS_COMPILE, 16, 0);
        }
        
        return i;
    }
    
    static {
        new RequestProcessor("Java Node Badge Processor", 1).post(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Task t = queue.poll(Long.MAX_VALUE, TimeUnit.SECONDS);
                        
                        if (t != null) {
                            boolean fire = t.fire;
                            JavaNode node = (JavaNode) t.node;
                            
                            if (t.computeBuiltStatus) {
                                boolean newIsCompiled = node.status != null ? node.status.isBuilt() : true;
                                boolean oldIsCompiled = node.isCompiled.getAndSet(newIsCompiled);
                                
                                fire |= newIsCompiled != oldIsCompiled;
                            }
                            
                            if (fire) {
                                node.fireIconChange();
                                node.fireOpenedIconChange();
                            }
                        }
                    } catch (ThreadDeath e) {
                        throw e;
                    } catch (Throwable e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
        });
    }

    private static BlockingQueue<Task> queue = new LinkedBlockingQueue<Task>();
    
    private static class Task {
        private boolean  fire;
        private boolean  computeBuiltStatus;
        private JavaNode node;
        
        public Task(boolean fire, boolean computeBuiltStatus, JavaNode node) {
            this.fire = fire;
            this.computeBuiltStatus = computeBuiltStatus;
            this.node = node;
        }
    }
    
}
