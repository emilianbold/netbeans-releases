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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.java;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.api.queries.FileBuiltQuery.Status;
import org.netbeans.modules.java.source.usages.ExecutableFilesIndex;
import org.netbeans.spi.java.loaders.RenameHandler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

import static org.openide.util.ImageUtilities.assignToolTipToImage;
import static org.openide.util.ImageUtilities.loadImage;
import static org.openide.util.NbBundle.getMessage;
import org.openide.util.Utilities;

/**
 * The node representation of Java source files.
 */
public final class JavaNode extends DataNode implements ChangeListener {

    private static final String EXECUTABLE_BADGE_URL = "org/netbeans/modules/java/resources/executable-badge.png";
    private static final String NEEDS_COMPILE_BADGE_URL = "org/netbeans/modules/java/resources/needs-compile.png";

    /** generated Serialized Version UID */
    private static final long serialVersionUID = -7396485743899766258L;

    private static final String JAVA_ICON_BASE = "org/netbeans/modules/java/resources/class.png"; // NOI18N
    private static final String CLASS_ICON_BASE = "org/netbeans/modules/java/resources/clazz.gif"; // NOI18N

    private static final AtomicReference<Image> NEEDS_COMPILE = new AtomicReference<>();
    private static final AtomicReference<Image> IS_EXECUTABLE_CLASS = new AtomicReference<>();

    private static final Logger LOG = Logger.getLogger(JavaNode.class.getName());
    
    private Status status;
    private final AtomicReference<Image> isCompiled;
    private ChangeListener executableListener;
    private final AtomicReference<Image> isExecutable;

    /** Create a node for the Java data object using the default children.
    * @param jdo the data object to represent
    */
    public JavaNode (final DataObject jdo, boolean isJavaSource) {
        super (jdo, Children.LEAF);
        this.setIconBaseWithExtension(isJavaSource ? JAVA_ICON_BASE : CLASS_ICON_BASE);
        Logger.getLogger("TIMER").log(Level.FINE, "JavaNode", new Object[] {jdo.getPrimaryFile(), this});
        if (isJavaSource) {
            this.isCompiled = new AtomicReference<Image>(null);                                        
            WORKER.post(new BuildStatusTask(this));
            this.isExecutable = new AtomicReference<Image>(null);
            WORKER.post(new ExecutableTask(this));
            
            jdo.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (DataObject.PROP_PRIMARY_FILE.equals(evt.getPropertyName())) {
                        Logger.getLogger("TIMER").log(Level.FINE, "JavaNode", new Object[]{jdo.getPrimaryFile(), this});
                        WORKER.post(new Runnable() {
                            public void run() {
                                synchronized (JavaNode.this) {
                                    status = null;
                                    executableListener = null;
                                    WORKER.post(new BuildStatusTask(JavaNode.this));
                                    WORKER.post(new ExecutableTask(JavaNode.this));
                                }
                            }
                        });
                    }
                }
            });
        } else {
            this.isCompiled = null;
            this.isExecutable = null;
        }
    }

    @Override
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
            LOG.warning("Multiple instances of RenameHandler found in Lookup; only using first one: " + handlers); //NOI18N
        return handlers.iterator().next();
    }

    private PropertySet[] propertySets;
    
    @Override
    public PropertySet[] getPropertySets() {
        getSheet(); //force initialization
        
        synchronized (this) {
            return Arrays.copyOf(propertySets, propertySets.length);
        }
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
        ps.setDisplayName(getMessage(JavaNode.class, "LBL_JavaNode_sheet_classpaths"));
        ps.setShortDescription(getMessage(JavaNode.class, "HINT_JavaNode_sheet_classpaths"));
        ps.put(new Node.Property[] {
            new ClasspathProperty(ClassPath.COMPILE,
                    getMessage(JavaNode.class, "PROP_JavaNode_compile_classpath"),
                    getMessage(JavaNode.class, "HINT_JavaNode_compile_classpath")),
                    new ClasspathProperty(ClassPath.EXECUTE,
                    getMessage(JavaNode.class, "PROP_JavaNode_execute_classpath"),
                    getMessage(JavaNode.class, "HINT_JavaNode_execute_classpath")),
                    new ClasspathProperty(ClassPath.BOOT,
                    getMessage(JavaNode.class, "PROP_JavaNode_boot_classpath"),
                    getMessage(JavaNode.class, "HINT_JavaNode_boot_classpath")),
        });
        sheet.put(ps);
        
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        PropertySet[] propertySets = sheet.toArray();
        
        synchronized (this) {
            this.propertySets = propertySets;
        }
        
        return sheet;
    }
    
    private Node.Property createNameProperty () {
        Node.Property p = new PropertySupport.ReadWrite<String> (
                DataObject.PROP_NAME,
                String.class,
                getMessage (DataObject.class, "PROP_name"),
                getMessage (DataObject.class, "HINT_name")
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
                        item = Utilities.toFile(URI.create(item)).getAbsolutePath();
                    } else if (u.getProtocol().equals("jar") && item.endsWith("!/")) { // NOI18N
                        URL embedded = FileUtil.getArchiveFile(u);
                        assert embedded != null : u;
                        if (embedded.getProtocol().equals("file")) { // NOI18N
                            item = Utilities.toFile(URI.create(embedded.toExternalForm())).getAbsolutePath();
                        }
                    }
                    if (sb.length() > 0) {
                        sb.append(File.pathSeparatorChar);
                    }
                    sb.append(item);
                }
                return sb.toString();
            } else {
                return getMessage(JavaNode.class, "LBL_JavaNode_classpath_unknown");
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
        Image needsCompile = isCompiled != null ? isCompiled.get() : null;
        
        if (needsCompile != null) {
            i = ImageUtilities.mergeImages(i, needsCompile, 16, 0);
        }
        
        Image executable = isExecutable != null ? isExecutable.get() : null;
        
        if (executable != null) {
            i = ImageUtilities.mergeImages(i, executable, 10, 6);
        }
        
        return i;
    }
    
    private static final RequestProcessor WORKER = new RequestProcessor("Java Node Badge Processor", 1, false, false);
    
    private static Image notCompiledBadge() {
        Image result = NEEDS_COMPILE.get();
        
        if (result == null) {
            URL needsCompileIconURL = JavaNode.class.getClassLoader().getResource(NEEDS_COMPILE_BADGE_URL);
            String needsCompileTP = "<img src=\"" + needsCompileIconURL + "\">&nbsp;" + getMessage(JavaNode.class, "TP_NeedsCompileBadge");
            NEEDS_COMPILE.set(result = assignToolTipToImage(loadImage(NEEDS_COMPILE_BADGE_URL), needsCompileTP)); // NOI18N
        }
        
        return result;
    }
    
    private static class BuildStatusTask implements Runnable {
        private JavaNode node;
        
        public BuildStatusTask(JavaNode node) {
            this.node = node;
        }

        public void run() {
            Status _status = null;
            synchronized (node) {
                _status = node.status;
            }            
            if (_status == null) {
                FileObject jf = node.getDataObject().getPrimaryFile();
                _status = FileBuiltQuery.getStatus(jf);                
                synchronized (node) {
                    if (_status != null && node.status == null) {
                        node.status = _status;
                        node.status.addChangeListener(WeakListeners.change(node, node.status));
                    }
                }
            }

            boolean isPackageInfo = "package-info.java".equals(node.getDataObject().getPrimaryFile().getNameExt());
            boolean newIsCompiled = _status != null && !isPackageInfo ?  _status.isBuilt() : true;
            boolean oldIsCompiled = node.isCompiled.getAndSet(newIsCompiled ? null : notCompiledBadge()) == null;

            if (newIsCompiled != oldIsCompiled) {
                node.fireIconChange();
                node.fireOpenedIconChange();
            }
        }
    }
    
    private static Image executableBadge() {
        Image result = IS_EXECUTABLE_CLASS.get();
        
        if (result == null) {
            URL executableIconURL = JavaNode.class.getClassLoader().getResource(EXECUTABLE_BADGE_URL);
            String executableTP = "<img src=\"" + executableIconURL + "\">&nbsp;" + getMessage(JavaNode.class, "TP_ExecutableBadge");
            IS_EXECUTABLE_CLASS.set(result = assignToolTipToImage(loadImage(EXECUTABLE_BADGE_URL), executableTP)); // NOI18N
        }
        
        return result;
    }
    
    private static class ExecutableTask implements Runnable {
        private final JavaNode node;
        
        public ExecutableTask(JavaNode node) {
            this.node = node;
        }

        public void run() {
            ChangeListener _executableListener;
            
            synchronized (node) {
                _executableListener = node.executableListener;
            }
            
            FileObject file = node.getDataObject().getPrimaryFile();

            if (_executableListener == null) {
                _executableListener = new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        WORKER.post(new ExecutableTask(node));
                    }
                };
                
                try {
                    ExecutableFilesIndex.DEFAULT.addChangeListener(file.getURL(), _executableListener);
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
                synchronized (node) {
                    if (node.executableListener == null) {
                        node.executableListener = _executableListener;
                    }
                }
            }
            
            ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
            FileObject root = cp != null ? cp.findOwnerRoot(file) : null;
            
            if (root != null) {
                try {
                    boolean newIsExecutable = ExecutableFilesIndex.DEFAULT.isMainClass(root.getURL(), file.getURL());
                    boolean oldIsExecutable = node.isExecutable.getAndSet(newIsExecutable ? executableBadge() : null) != null;

                    if (newIsExecutable != oldIsExecutable) {
                        node.fireIconChange();
                        node.fireOpenedIconChange();
                    }
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
}
