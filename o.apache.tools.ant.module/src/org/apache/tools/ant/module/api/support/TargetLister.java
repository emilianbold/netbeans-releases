/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.api.support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.xml.AntProjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides a way to find targets from an Ant build script.
 * <p>
 * Note that scripts may import other scripts using
 * the <code>&lt;import&gt;</code> pseudotask, so you may need
 * to use {@link Target#getScript} to check which script a target came from.
 * </p>
 * <p>
 * <strong>Warning:</strong> the current implementation does not attempt to handle
 * import statements which use Ant properties in the imported file name, since
 * it is not possible to determine what the value of the file path will actually
 * be at runtime, at least not with complete accuracy. A future implementation
 * may be enhanced to handle most such cases, based on property definitions found
 * in the Ant script. Currently such imports are quietly ignored.
 * </p>
 * <p>
 * The imported file path is considered relative to the project
 * base directory, hopefully according to Ant's own rules.
 * </p>
 * <p>
 * If an import statement is marked as optional, and the imported script cannot
 * be found, it will be silently skipped (as Ant does). If it is marked as mandatory
 * (the default), this situation will result in an {@link IOException}.
 * </p>
 * @author Jesse Glick
 * @since org.apache.tools.ant.module/3 3.11
 */
public class TargetLister {
    
    private TargetLister() {}
    
    /**
     * Gets all targets in an Ant script.
     * Some may come from imported scripts.
     * There is no guarantee that the actual {@link Target} objects will be
     * the same from call to call.
     * @param script an Ant build script
     * @return an immutable, unchanging set of {@link Target}s; may be empty
     * @throws IOException in case there is a problem reading the script (or a subscript)
     */
    public static Set/*<Target>*/ getTargets(AntProjectCookie script) throws IOException {
        Set/*<File>*/ alreadyImported = new HashSet();
        Script main = new Script(null, script, alreadyImported);
        Set/*<Target>*/ targets = new HashSet();
        Set/*<AntProjectCookie>*/ visitedScripts = new HashSet();
        traverseScripts(main, targets, visitedScripts);
        return targets;
    }
    
    /**
     * Walk import tree in a depth-first search.
     * At each node, collect the targets.
     * Skip over nodes representing scripts which were already imported via a different path.
     */
    private static void traverseScripts(Script script, Set/*<Target>*/ targets, Set/*<AntProjectCookie>*/ visitedScripts) throws IOException {
        if (!visitedScripts.add(script.getScript())) {
            return;
        }
        targets.addAll(script.getTargets());
        Iterator it = script.getImports().iterator();
        while (it.hasNext()) {
            Script imported = (Script) it.next();
            traverseScripts(imported, targets, visitedScripts);
        }
    }
    
    /**
     * Representation of a target from an Ant script.
     */
    public static final class Target {
        
        private final Script script;
        private final Element el;
        private final String name;
        
        Target(Script script, Element el, String name) {
            this.script = script;
            this.el = el;
            this.name = name;
        }
        
        /**
         * Gets the simple name of the target.
         * This is just whatever is declared in the <code>name</code> attribute.
         * @return the target name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Gets the qualified name of the target.
         * This consists of the name of the project followed by a dot (<samp>.</samp>)
         * followed by the simple target name.
         * (Or just the simple target name in case the project has no defined name;
         * questionable whether this is even legal.)
         * The qualified name may be used in a <code>depends</code> attribute to
         * distinguish an imported target from a target of the same name in the
         * importing script.
         * @return the qualified name
         */
        public String getQualifiedName() {
            String n = script.getName();
            if (n != null) {
                return n + '.' + getName();
            } else {
                return getName();
            }
        }
        
        /**
         * Gets the XML element that defines the target.
         * @return an element with local name <code>target</code>
         */
        public Element getElement() {
            return el;
        }
        
        /**
         * Gets the actual Ant script this target was found in.
         * {@link #getElement} should be owned by {@link AntProjectCookie#getDocument}.
         * @return the script which defines this target
         */
        public AntProjectCookie getScript() {
            return script.getScript();
        }
        
        /**
         * Tests whether this target has a description.
         * This is the <code>description</code> attribute in XML.
         * Typically, targets with descriptions are intended to be exposed to the
         * user of the script, whereas undescribed targets may not be intended
         * for general use. However not all script authors use descriptions, so
         * described targets should only be given UI precedence.
         * @return true if the target has a description
         */
        public boolean isDescribed() {
            return el.getAttribute("description").length() > 0;
        }

        /**
         * Tests whether a target is marked as internal to the script.
         * Currently this means that the target name begins with a hyphen (<samp>-</samp>),
         * though the precise semantics may be changed according to changes in Ant.
         * Conventionally, internal targets are not intended to be run directly, and only
         * exist to be called from other targets. As such, they should not normally
         * be presented in the context of targets you might want to run.
         * @return true if this is marked as an internal target, false for a regular target
         * @see <a href="http://issues.apache.org/bugzilla/show_bug.cgi?id=22020">Ant issue #22020</a>
         */
        public boolean isInternal() {
            String n = getName();
            return n.length() > 0 && n.charAt(0) == '-';
        }
        
        /**
         * Tests whether this target is overridden in an importing script.
         * If an importing script has a target of the same name as a target
         * in an imported script, the latter is considered overridden, and may
         * not be called directly (though it may be used as a dependency, if
         * qualified via {@link #getQualifiedName}).
         * Note that this flag may be true when asked of a {@link Target} gotten
         * via the importing script, while false when asked of the same target
         * gotten directly from the imported script, since the meaning is dependent
         * on the import chain.
         * @return true if the target is overridden
         */
        public boolean isOverridden() {
            return !script.defines(getName());
        }
        
        /**
         * Tests whether this target is the default for the main script.
         * Note that a set of targets will have at most one default target;
         * any <code>default</code> attribute in an imported script is ignored.
         * However the default target might come from an imported script.
         * @return true if the target is the default target
         */
        public boolean isDefault() {
            return !isOverridden() && getName().equals(script.getMainScript().getDefaultTargetName());
        }
        
        public String toString() {
            return "Target " + getName() + " in " + getScript(); // NOI18N
        }
        
    }
    
    /**
     * Representation of one script full of targets.
     */
    private static final class Script {
        
        private final AntProjectCookie apc;
        private final Script importingScript;
        private final Map/*<String,Target>*/ targets;
        private final String defaultTarget;
        private final List/*<Script>*/ imports;
        private final String name;
        private final Set/*<File>*/ alreadyImported;
        
        private static final Set/*<String>*/ TRUE_VALS = new HashSet(5);
        static {
            TRUE_VALS.add("true"); // NOI18N
            TRUE_VALS.add("yes"); // NOI18N
            TRUE_VALS.add("on"); // NOI18N
        }
        
        public Script(Script importingScript, AntProjectCookie apc, Set/*<File>*/ alreadyImported) throws IOException {
            this.importingScript = importingScript;
            this.apc = apc;
            this.alreadyImported = alreadyImported;
            Element prj = apc.getProjectElement();
            if (prj == null) {
                throw new IOException("Could not parse " + apc); // NOI18N
            }
            File prjFile = apc.getFile();
            if (prjFile != null) {
                alreadyImported.add(prjFile);
            }
            String _defaultTarget = prj.getAttribute("default"); // NOI18N
            defaultTarget = _defaultTarget.length() > 0 ? _defaultTarget : null;
            String _name = prj.getAttribute("name"); // NOI18N
            name = _name.length() > 0 ? _name : null;
            NodeList nl = prj.getChildNodes();
            int len = nl.getLength();
            // For now, treat basedir as relative to the project file, regardless
            // of import context. Unclear what exactly Ant's semantics are here.
            String basedirS = prj.getAttribute("basedir"); // NOI18N
            if (basedirS.length() == 0) {
                basedirS = "."; // NOI18N
            } else {
                basedirS = basedirS.replace('/', File.separatorChar);
            }
            File _basedir = new File(basedirS);
            File basedir;
            if (_basedir.isAbsolute()) {
                basedir = _basedir;
            } else {
                if (prjFile != null) {
                    basedir = new File(prjFile.getParentFile(), basedirS);
                } else {
                    // Script not on disk.
                    basedir = null;
                }
            }
            // Go through top-level elements and look for <target> and <import>.
            targets = new HashMap();
            // Keep imported scripts in definition order so result is deterministic
            // if a subsubscript is imported via two different paths: first one (DFS)
            // takes precedence.
            imports = new ArrayList();
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element el = (Element)n;
                String elName = el.getLocalName();
                if (elName.equals("target")) { // NOI18N
                    String name = el.getAttribute("name"); // NOI18N
                    targets.put(name, new Target(this, el, name));
                } else if (elName.equals("import")) { // NOI18N
                    String fileS = el.getAttribute("file").replace('/', File.separatorChar); // NOI18N
                    if (fileS.indexOf("${") != -1) { // NOI18N
                        // Not yet handled.
                        // #45066: throwing an IOException might be more correct, but is undesirable in practice.
                        continue;
                    }
                    File _file = new File(fileS);
                    File file;
                    if (_file.isAbsolute()) {
                        file = _file;
                    } else {
                        if (prjFile == null) {
                            throw new IOException("Cannot import relative path " + fileS + " from a diskless script"); // NOI18N
                        }
                        // #50087: <import> resolves file against the script, *not* the basedir.
                        file = new File(prjFile.getParentFile(), fileS);
                    }
                    if (alreadyImported.contains(file)) {
                        // #55263: avoid a stack overflow on a recursive import.
                        continue;
                    }
                    if (file.canRead()) {
                        FileObject fileObj = FileUtil.toFileObject(FileUtil.normalizeFile(file));
                        assert fileObj != null : file;
                        AntProjectCookie importedApc = getAntProjectCookie(fileObj);
                        imports.add(new Script(this, importedApc, alreadyImported));
                    } else {
                        String optionalS = el.getAttribute("optional"); // NOI18N
                        boolean optional = TRUE_VALS.contains(optionalS.toLowerCase(Locale.US));
                        if (!optional) {
                            throw new IOException("Cannot find import " + file + " from " + apc); // NOI18N
                        }
                    }
                }
            }
        }
        
        /** Get the associated script. */
        public AntProjectCookie getScript() {
            return apc;
        }
        
        /** Get project name (or null). */
        public String getName() {
            return name;
        }
        
        /** Get targets defined in this script. */
        public Collection/*<Target>*/ getTargets() {
            return targets.values();
        }
        
        /** Get name of default target (or null). */
        public String getDefaultTargetName() {
            return defaultTarget;
        }
        
        /** Get imported scripts. */
        public Collection/*<Script>*/ getImports() {
            return imports;
        }
        
        /** Get the script importing this one (or null). */
        public Script getImportingScript() {
            return importingScript;
        }
        
        /** Get the main script (never null). */
        public Script getMainScript() {
            if (importingScript != null) {
                return importingScript.getMainScript();
            } else {
                return this;
            }
        }
        
        /** Test whether this script is the one to define a given target name. */
        public boolean defines(String targetName) {
            if (!targets.containsKey(targetName)) {
                return false;
            }
            if (importingScript == null) {
                return true;
            }
            return !importingScript.defines(targetName);
        }
        
    }

    /**
     * Try to find an AntProjectCookie for a file.
     */
    static AntProjectCookie getAntProjectCookie(FileObject fo) {
        try {
            DataObject d = DataObject.find(fo);
            AntProjectCookie apc = (AntProjectCookie) d.getCookie(AntProjectCookie.class);
            if (apc != null) {
                return apc;
            }
        } catch (DataObjectNotFoundException e) {
            assert false : e;
        }
        // AntProjectDataLoader probably not installed, e.g. from a unit test.
        synchronized (antProjectCookies) {
            AntProjectCookie apc = (AntProjectCookie) antProjectCookies.get(fo);
            if (apc == null) {
                apc = new AntProjectSupport(fo);
                antProjectCookies.put(fo, apc);
            }
            return apc;
        }
    }
    private static final Map/*<FileObject,AntProjectCookie>*/ antProjectCookies = new WeakHashMap();
    
}
