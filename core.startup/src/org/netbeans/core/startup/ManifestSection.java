/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

// Permitted to reference API classes but should not directly do stuff with
// the core, and should try to avoid loading TopManager.
// ManifestSection itself can only reference generic module things and
// utility APIs; other API references are made in the create() method
// and in its subclasses.

import java.beans.Beans;
import java.util.*;
import java.util.jar.Attributes;

import org.openide.filesystems.FileSystem;
import org.openide.ServiceType;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.SharedClassObject;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

import org.netbeans.*;

// XXX probably should delete deprecated section handlers now (NbInstaller does not load them)

// XXX synchronization?

/** Class representing one specially-treated section in a module's manifest file.
 * For example, one section may describe a single action provided by the module.
 *
 * @author Jaroslav Tulach, Jesse Glick
 */
public abstract class ManifestSection {
    /** superclass of section either Class or String name of the class*/
    private final Object superclazz;
    /** name of the class file, e.g. foo/Bar.class, or foo/bar.ser */
    private final String name;
    /** name of the class, e.g. foo.bar */
    private final String className;
    /** the class involved */
    private Class clazz;
    /** instance of the class if possible */
    private Object result;
    /** any exception associated with loading the object */
    private Exception problem;
    /** associated module */
    private final Module module;
    
    /** Create a manifest section generally.
     * @param name name of section, should be a class file, e.g. foo/Bar.class
     * @param module associated module
     * @param superclazz super-class of instances of this section
     * @throws InvalidException if the name is not valid for an OpenIDE section
     *         (exception must include module for reference)
     */
    protected ManifestSection(String name, Module module, Object superclazz) throws InvalidException {
        this.name = name;
        this.module = module;
        this.superclazz = superclazz;
        try {
            className = Util.createPackageName(name);
        } catch (IllegalArgumentException iae) {
            InvalidException ie = new InvalidException(module, iae.toString());
            Util.err.annotate(ie, iae);
            throw ie;
        }
    }
    
    /** Get the associated module. */
    public final Module getModule() {
        return module;
    }
    
    /** Get the classloader used to load this section. */
    protected final ClassLoader getClassLoader() {
        return module.getClassLoader();
    }
    
    /** Does this section represent a default instance?
     * Normally true, but false when deserializing beans.
     */
    public final boolean isDefaultInstance() {
        return name.endsWith(".class"); // NOI18N
    }
    
    /** Get the class which the generated instances will have.
     * @return the class
     * @throws Exception for various reasons
     */
    public final Class getSectionClass() throws Exception {
        if (clazz != null) {
            return clazz;
        }
        if (problem != null) {
            throw problem;
        }
        if (isDefaultInstance()) {
            try {
                clazz = getClassLoader().loadClass(className);
                if (! getSuperclass().isAssignableFrom(clazz)) {
                    throw new ClassCastException("Class " + clazz.getName() + " is not a subclass of " + getSuperclass().getName()); // NOI18N
                }
                // Don't try to check .ser files: it is quite legitimate to
                // serialize in a module objects whose class is from elsewhere
                // (e.g. the core).
                if (clazz.getClassLoader() != getClassLoader()) { // NOI18N
                    Events ev = module.getManager().getEvents();
                    ev.log(Events.WRONG_CLASS_LOADER, module, clazz, getClassLoader());
                }
                return clazz;
            } catch (ClassNotFoundException cnfe) {
                Util.err.annotate(cnfe, ErrorManager.UNKNOWN, "Loader for ClassNotFoundException: " + getClassLoader(), null, null, null);
                problem = cnfe;
                throw problem;
            } catch (Exception e) {
                problem = e;
                throw problem;
            } catch (LinkageError t) {
                problem = new ClassNotFoundException(t.toString(), t);
                throw problem;
            }
        } else {
            return (clazz = getInstance().getClass());
        }
    }
    
    /** Same as {@link #getSectionClass}, but only provides the name of the class.
     * Could be more efficient because it will not try to load the class unless
     * a serialized bean is in use.
     */
    public String getSectionClassName() throws Exception {
        if (isDefaultInstance()) {
            return className;
        } else {
            return getSectionClass().getName();
        }
    }
    
    /** Create a fresh instance.
     * @return the instance
     * @exception Exception if there is an error
     */
    protected final Object createInstance() throws Exception {
        if (! isDefaultInstance()) {
            try {
                Object o = Beans.instantiate(getClassLoader(), className);
                clazz = o.getClass();
                if (! getSectionClass().isAssignableFrom(clazz)) {
                    throw new ClassCastException("Class " + clazz.getName() + " is not a subclass of " + getSuperclass().getName()); // NOI18N
                }
                return o;
            } catch (ClassNotFoundException cnfe) {
                Util.err.annotate(cnfe, ErrorManager.UNKNOWN, "Loader for ClassNotFoundException: " + getClassLoader(), null, null, null);
                throw cnfe;
            } catch (LinkageError le) {
                throw new ClassNotFoundException(le.toString(), le);
            }
        } else {
            getSectionClass(); // might throw some exceptions
            if (SharedClassObject.class.isAssignableFrom(clazz)) {
                return SharedClassObject.findObject(clazz, true);
            } else {
                return clazz.newInstance();
            }
        }
    }
    
    /** Get a single cached instance.
     * @return the instance
     * @exception Exception if there is an error
     */
    public final Object getInstance() throws Exception {
        if (problem != null) {
            problem.fillInStackTrace(); // XXX is this a good idea?
            throw problem;
        }
        if (result == null) {
            try {
                result = createInstance();
            } catch (Exception ex) {
                // remember the exception
                problem = ex;
                throw problem;
            } catch (LinkageError t) {
                problem = new ClassNotFoundException(t.toString(), t);
                throw problem;
            }
        }
        return result;
    }
    
    /** Get the superclass which all instances of this section are expected to
     * be assignable to.
     */
    public final Class getSuperclass() {
        if (superclazz instanceof Class) {
            return (Class)superclazz;
        } else {
            try {
                return getClazz ((String)superclazz, module);
            } catch (InvalidException ex) {
                throw (IllegalStateException)new IllegalStateException (superclazz.toString()).initCause (ex);
            }
        }
    }
    
    /** Dispose of a section. Used when a module will be uninstalled and all its
     * resources should be released.
     */
    public void dispose() {
        result = null;
        problem = null;
        clazz = null;
    }
    
    /** String representation for debugging. */
    public String toString() {
        return "ManifestSection[" + className + "]"; // NOI18N
    }
    
    /** Parse a manifest section and make an object representation of it.
     * @param name name of the section (i.e. file to load)
     * @param attr attributes of the manifest section
     * @param module the associated module
     * @return the section or null if this manifest section is not related to module installation
     * @exception InvalidException if the attributes are not valid
     */
    public static ManifestSection create(String name, Attributes attr, Module module) throws InvalidException {
        String sectionName = attr.getValue("OpenIDE-Module-Class"); // NOI18N
        if (sectionName == null) {
            // no section tag
            return null;
        } else if (sectionName.equalsIgnoreCase("Action")) { // NOI18N
            return new ActionSection(name, attr, module);
        } else if (sectionName.equalsIgnoreCase("Option")) { // NOI18N
            warnObsolete(sectionName, module);
            return new OptionSection(name, attr, module);
        } else if (sectionName.equalsIgnoreCase("Loader")) { // NOI18N
            return new LoaderSection(name, attr, module);
        } else if (sectionName.equalsIgnoreCase("Filesystem")) { // NOI18N
            warnObsolete(sectionName, module);
            return new FileSystemSection(name, attr, module);
        } else if (sectionName.equalsIgnoreCase("Node")) { // NOI18N
            warnObsolete(sectionName, module);
            Util.err.log(ErrorManager.WARNING, "(See http://www.netbeans.org/issues/show_bug.cgi?id=19609, last comment, for howto.)");
            return new NodeSection(name, attr, module);
        } else if (sectionName.equalsIgnoreCase("Service")) { // NOI18N
            warnObsolete(sectionName, module);
            return new ServiceSection(name, attr, module);
        } else if (sectionName.equalsIgnoreCase("Debugger")) { // NOI18N
            warnObsolete(sectionName, module);
            return new DebuggerSection(name, attr, module);
        } else if (sectionName.equalsIgnoreCase("ClipboardConvertor")) { // NOI18N
            warnObsolete(sectionName, module);
            return new ClipboardConvertorSection(name, attr, module);
        } else {
            throw new InvalidException(module, "Illegal manifest section type: " + sectionName); // NOI18N
        }
    }
    
    private static void warnObsolete(String sectionName, Module module) {
        Util.err.log(ErrorManager.WARNING, "Use of OpenIDE-Module-Class: " + sectionName + " in " + module.getCodeNameBase() + " is obsolete.");
        Util.err.log(ErrorManager.WARNING, "(Please use layer-based installation of objects instead.)");
    }
    
    /** Module section for an Action.
     * @see SystemAction
     */
    public static final class ActionSection extends ManifestSection {
        ActionSection(String name, Attributes attrs, Module module) throws InvalidException {
            super(name, module, SystemAction.class);
        }
    }
    
    /** Module section for an Option.
     * @see SystemOption
     */
    public static final class OptionSection extends ManifestSection {
        OptionSection(String name, Attributes attrs, Module module) throws InvalidException {
            super(name, module, getClazz ("org.openide.options.SystemOption", module)); // NOI18N
        }
    }
    
    /** Module section for a Data Loader.
     * @see DataLoader
     */
    public static final class LoaderSection extends ManifestSection {
        /** class name(s) of data object to
         * be inserted after loader that recognizes its
         */
        private final String[] installAfter;
        /** class name(s) of data object to be inserted before its recognizing
         * data loader
         */
        private final String[] installBefore;
        
        LoaderSection(String name, Attributes attrs, Module module) throws InvalidException {
            super (name, module, "org.openide.loaders.DataLoader"); // NOI18N
            String val = attrs.getValue("Install-After"); // NOI18N
            StringTokenizer tok;
            List res;
            // XXX validate classnames etc.
            if (val != null) {
                tok = new StringTokenizer(val, ", "); // NOI18N
                res = new LinkedList();
                while (tok.hasMoreTokens()) {
                    String clazz = tok.nextToken();
                    if (! clazz.equals("")) // NOI18N
                        res.add(clazz);
                }
                installAfter = (String[])res.toArray(new String[res.size()]);
            } else {
                installAfter = null;
            }
            val = attrs.getValue("Install-Before"); // NOI18N
            if (val != null) {
                tok = new StringTokenizer(val, ", "); // NOI18N
                res = new LinkedList();
                while (tok.hasMoreTokens()) {
                    String clazz = tok.nextToken();
                    if (! clazz.equals("")) // NOI18N
                        res.add(clazz);
                }
                installBefore = (String[])res.toArray(new String[res.size()]);
            } else {
                installBefore = null;
            }
        }
        
        /** Get the representation class(es) of the loader(s) that this one should be installed after.
         * @return a list of class names, or <code>null</code>
         */
        public String[] getInstallAfter() {
            return installAfter;
        }
        
        /** Get the representation class(es) of the loader(s) that this one should be installed before.
         * @return a list of class names, or <code>null</code>
         */
        public String[] getInstallBefore() {
            return installBefore;
        }
    }
    
    /** Module section for a Debugger.
     * @see Debugger
     */
    public static final class DebuggerSection extends ManifestSection {
        DebuggerSection(String name, Attributes attrs, Module module) throws InvalidException {
            super(name, module, getClazz("org.openide.debugger.Debugger", module)); // NOI18N
        }
    }
    
    /** Module section for a service type.
     * @see ServiceType
     */
    public static final class ServiceSection extends ManifestSection {
        private final boolean deflt;
        
        ServiceSection(String name, Attributes attrs, Module module) throws InvalidException {
            super(name, module, ServiceType.class);
            deflt = Boolean.valueOf(attrs.getValue("Default")).booleanValue(); // NOI18N
        }
        
        /** Is this service type default? That means should it be placed
         * in front of other services?
         *
         * @return true if it is
         */
        public boolean isDefault() {
            return deflt;
        }
        
        /** Create a new service type of the specified type.
         * @return the service type
         * @throws InstantiationException if the Service could not be created
         */
        public ServiceType createServiceType() throws InstantiationException {
            try {
                return (ServiceType)createInstance();
            } catch (Exception ex) {
                InstantiationException ie = new InstantiationException(ex.toString());
                Util.err.annotate(ie, ex);
                throw ie;
            }
        }
    }
    
    /** Module section for a File System.
     * @see FileSystem
     */
    public static final class FileSystemSection extends ManifestSection {
        private final String locAttr;
        private final HelpCtx help;
        FileSystemSection(String name, Attributes attr, Module module) throws InvalidException {
            super (name, module, FileSystem.class);
            locAttr = name + "/Display-Name"; // NOI18N
            String s = attr.getValue("Help"); // NOI18N
            if (s == null) {
                help = HelpCtx.DEFAULT_HELP;
            } else {
                // [PENDING] this constructor looks for a JavaHelp tag, but docs say /com/mycom/index.html!
                help = new HelpCtx(s);
            }
        }
        
        /** Get the display name of the file system.
         * This could be used e.g. in a context menu on the Repository.
         * If none was specified, a default name will be created.
         *
         * @return the name
         */
        public String getName() {
            String s = (String)getModule().getLocalizedAttribute(locAttr);
            if (s == null) {
                return "<unnamed filesystem>"; // NOI18N
            } else {
                return s;
            }
        }
        
        /** Get a help context for the file system.
         * If none was specified, a default context will be created.
         * @return the help context
         */
        public HelpCtx getHelpCtx() {
            return help;
        }
        
        /** Create a new file system.
         * @return the file system
         * @throws InstantiationException if it could not be created
         */
        public FileSystem createFileSystem() throws InstantiationException {
            try {
                return (FileSystem)createInstance();
            } catch (Exception ex) {
                InstantiationException ie = new InstantiationException(ex.toString());
                Util.err.annotate(ie, ex);
                throw ie;
            }
        }
    }
    
    /** Module section for a node.
     * @see Node
     */
    public static final class NodeSection extends ManifestSection {
        /** Type to add an entry to the root nodes. */
        public static final String TYPE_ROOTS = "roots"; // NOI18N
        /** Type to add an entry to the Environment (in the Explorer). */
        public static final String TYPE_ENVIRONMENT = "environment"; // NOI18N
        /** Type to add an entry to the Session settings. */
        public static final String TYPE_SESSION = "session"; // NOI18N
        
        
        /** type of the node */
        private String type;
        
        NodeSection(String name, Attributes attrs, Module module) throws InvalidException {
            super(name, module, getClazz ("org.openide.nodes.Node", module)); // NOI18N
            type = attrs.getValue("Type"); // NOI18N
            if (type == null) {
                type = TYPE_ENVIRONMENT;
            }
            type = type.toLowerCase();
            if (! type.equalsIgnoreCase(TYPE_ROOTS) &&
                    ! type.equalsIgnoreCase(TYPE_ENVIRONMENT) &&
                    ! type.equalsIgnoreCase(TYPE_SESSION)) {
                throw new InvalidException(module, "Unrecognized node section type: " + type); // NOI18N
            }
        }
        
        /** Get the node type. Determines where the node should be placed in
         * the IDE.
         * @return one of TYPE_*
         */
        public String getType() {
            return type;
        }
    }
    
    /** Module section for a Clipboard convertor.
     * @see ExClipboard.Convertor
     */
    public static final class ClipboardConvertorSection extends ManifestSection {
        ClipboardConvertorSection(String name, Attributes attrs, Module module) throws InvalidException {
            super(name, module, ExClipboard.Convertor.class);
        }
    }

    /** Loads class of given name.
     */
    static Class getClazz(String name, Module m) throws InvalidException {
        try {
            return ((ClassLoader)Lookup.getDefault().lookup(ClassLoader.class)).loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            InvalidException e = new InvalidException(m, "Unable to locate class: " + name + " maybe you do not have its module enabled!?"); // NOI18N
            Util.err.annotate(e, cnfe);
            throw e;
        }
    }
    
}
