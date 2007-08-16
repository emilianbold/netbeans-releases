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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java;

import java.awt.Image;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.api.queries.FileBuiltQuery.Status;
import org.netbeans.spi.java.loaders.RenameHandler;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * The node representation of Java source files.
 */
public final class JavaNode extends DataNode implements ChangeListener {

    /** generated Serialized Version UID */
    private static final long serialVersionUID = -7396485743899766258L;

    private static final String JAVA_ICON_BASE = "org/netbeans/modules/java/resources/class.png"; // NOI18N
    private static final String CLASS_ICON_BASE = "org/netbeans/modules/java/resources/clazz.gif"; // NOI18N

    private static final Image NEEDS_COMPILE = Utilities.loadImage("org/netbeans/modules/java/resources/needs-compile.png");
    
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
                
                WORKER.post(new BuildStatusTask(this));
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
    
    /** Create the property sheet.
     * @return the sheet
     */
    @Override
    protected final Sheet createSheet () {
        Sheet sheet = super.createSheet();
        
        //if there is any rename handler installed
        //push under our own property
        if (getRenameHandler() != null)
            sheet.get(Sheet.PROPERTIES).put(createNameProperty());
        
        // Add classpath-related properties.
        Sheet.Set ps = new Sheet.Set();
        ps.setName("classpaths"); // NOI18N
        ps.setDisplayName(NbBundle.getMessage(JavaNode.class, "LBL_JavaNode_sheet_classpaths"));
        ps.setShortDescription(NbBundle.getMessage(JavaNode.class, "HINT_JavaNode_sheet_classpaths"));
        ps.put(new Node.Property[] {
            new ClasspathProperty(ClassPath.COMPILE,
                    NbBundle.getMessage(JavaNode.class, "PROP_JavaNode_compile_classpath"),
                    NbBundle.getMessage(JavaNode.class, "HINT_JavaNode_compile_classpath")),
                    new ClasspathProperty(ClassPath.EXECUTE,
                    NbBundle.getMessage(JavaNode.class, "PROP_JavaNode_execute_classpath"),
                    NbBundle.getMessage(JavaNode.class, "HINT_JavaNode_execute_classpath")),
                    new ClasspathProperty(ClassPath.BOOT,
                    NbBundle.getMessage(JavaNode.class, "PROP_JavaNode_boot_classpath"),
                    NbBundle.getMessage(JavaNode.class, "HINT_JavaNode_boot_classpath")),
        });
        sheet.put(ps);
        return sheet;
    }
    
    private Node.Property createNameProperty () {
        Node.Property p = new PropertySupport.ReadWrite<String> (
                DataObject.PROP_NAME,
                String.class,
                NbBundle.getMessage (DataObject.class, "PROP_name"),
                NbBundle.getMessage (DataObject.class, "HINT_name")
                ) {
            public String getValue () {
                return JavaNode.this.getName();
            }
            @Override
            public Object getValue(String key) {
                if ("suppressCustomEditor".equals (key)) { //NOI18N
                    return Boolean.TRUE;
                } else {
                    return super.getValue (key);
                }
            }
            public void setValue(String val) throws IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException {
                if (!canWrite())
                    throw new IllegalAccessException();
                JavaNode.this.setName(val);
            }
            @Override
            public boolean canWrite() {
                return JavaNode.this.canRename();
            }
            
        };
        
        return p;
    }
    
    /**
     * Displays one kind of classpath for this Java source.
     * Tries to use the normal format (directory or JAR names), falling back to URLs if necessary.
     */
    private final class ClasspathProperty extends PropertySupport.ReadOnly<String> {
        
        private final String id;
        
        public ClasspathProperty(String id, String displayName, String shortDescription) {
            super(id, /*XXX NbClassPath would be preferable, but needs org.openide.execution*/String.class, displayName, shortDescription);
            this.id = id;
            // XXX the following does not always work... why?
            setValue("oneline", false); // NOI18N
        }
        
        public String getValue() {
            ClassPath cp = ClassPath.getClassPath(getDataObject().getPrimaryFile(), id);
            if (cp != null) {
                StringBuffer sb = new StringBuffer();
                for (ClassPath.Entry entry : cp.entries()) {
                    URL u = entry.getURL();
                    String item = u.toExternalForm(); // fallback
                    if (u.getProtocol().equals("file")) { // NOI18N
                        item = new File(URI.create(item)).getAbsolutePath();
                    } else if (u.getProtocol().equals("jar") && item.endsWith("!/")) { // NOI18N
                        URL embedded = FileUtil.getArchiveFile(u);
                        assert embedded != null : u;
                        if (embedded.getProtocol().equals("file")) { // NOI18N
                            item = new File(URI.create(embedded.toExternalForm())).getAbsolutePath();
                        }
                    }
                    if (sb.length() > 0) {
                        sb.append(File.pathSeparatorChar);
                    }
                    sb.append(item);
                }
                return sb.toString();
            } else {
                return NbBundle.getMessage(JavaNode.class, "LBL_JavaNode_classpath_unknown");
            }
        }
    }

    public void stateChanged(ChangeEvent e) {
        WORKER.post(new BuildStatusTask(this));
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
        if (isCompiled != null && !isCompiled.get()) {
            i = Utilities.mergeImages(i, NEEDS_COMPILE, 16, 0);
        }
        
        return i;
    }
    
    private static final RequestProcessor WORKER = new RequestProcessor("Java Node Badge Processor", 1);
    
    private static class BuildStatusTask implements Runnable {
        private JavaNode node;
        
        public BuildStatusTask(JavaNode node) {
            this.node = node;
        }

        public void run() {
            boolean newIsCompiled = node.status != null ? node.status.isBuilt() : true;
            boolean oldIsCompiled = node.isCompiled.getAndSet(newIsCompiled);

            if (newIsCompiled != oldIsCompiled) {
                node.fireIconChange();
                node.fireOpenedIconChange();
            }
        }
    }
    
}
