/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * Support for working with Ant properties and property files.
 * @author Jesse Glick
 */
public class PropertyUtils {
    
    // XXX fire changes from putGlobalProperties?
    
    private PropertyUtils() {}
    
    /**
     * Location in user directory of per-user global properties.
     * May be null if <code>netbeans.user</code> is not set.
     */
    static final File USER_BUILD_PROPERTIES;
    static {
        String nbuser = System.getProperty("netbeans.user"); // NOI18N
        if (nbuser != null) {
            USER_BUILD_PROPERTIES = new File(nbuser, "build.properties"); // NOI18N
        } else {
            USER_BUILD_PROPERTIES = null;
        }
    }
    
    /**
     * Load global properties defined by the IDE in the user directory.
     * Currently loads ${netbeans.user}/build.properties if it exists.
     * <p>
     * Acquires read access.
     * @return user properties (empty if missing or malformed)
     */
    public static EditableProperties getGlobalProperties() {
        // XXX cache between calls
        return (EditableProperties)ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                if (USER_BUILD_PROPERTIES != null && USER_BUILD_PROPERTIES.isFile() &&
                USER_BUILD_PROPERTIES.canRead()) {
                    try {
                        InputStream is = new FileInputStream(USER_BUILD_PROPERTIES);
                        try {
                            EditableProperties properties = new EditableProperties(true);
                            properties.load(is);
                            return properties;
                        } finally {
                            is.close();
                        }
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
                // Missing or erroneous.
                return new EditableProperties(true);
            }
        });
    }
    
    /**
     * Edit global properties defined by the IDE in the user directory.
     * <p>
     * Acquires write access.
     * @param properties user properties to set
     * @throws IOException if they could not be stored
     * @see #getGlobalProperties
     */
    public static void putGlobalProperties(final EditableProperties properties) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    if (USER_BUILD_PROPERTIES != null) {
                        OutputStream os = new FileOutputStream(USER_BUILD_PROPERTIES);
                        try {
                            properties.store(os);
                        } finally {
                            os.close();
                        }
                    } else {
                        throw new IOException("Do not know where to store build.properties; must set netbeans.user!"); // NOI18N
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    /**
     * Evaluate an Ant-style property according to a list of property files.
     * <p>
     * The earlier maps take precedence if more than one map defines a property.
     * A property value may include Ant-style <samp>${propname}</samp> substitutions,
     * where the reference must be defined in this map or an earlier one.
     * <samp>$$</samp> escapes to <samp>$</samp> and other <samp>$</samp>s are left alone.
     * <p>
     * As with Ant, property dereferences that fail just leave the literal reference string;
     * but the return value of this method will be null if no attempt was made to define
     * this property at all.
     * <p>
     * Cyclic references within one map are not permitted and will result in the return
     * value of this method being null if a cyclic reference would have been required
     * to evaluate the property. The implementation may or may not return null in case
     * some properties in a mapping were cyclically defined but the values of those
     * properties were not required in order to evaluate the given property.
     * <p>
     * The net effect is similar to a sequence of <samp>&lt;property file="..."/&gt;</samp> calls.
     * <p>
     * You may wish to start the list with a hand-constructed map containing
     * any critical built-in properties such as <samp>${basedir}</samp> in case
     * these are referred to.
     * <p>
     * Besides the list of substitutable maps, a single map is passed
     * (effectively first in the list - none of its definitions can be overridden)
     * for which no <samp>$</samp> substitutions are performed. This map can be
     * used for fixed properties which should be considered predefined; for example,
     * <samp>basedir</samp> to permit file location resolution, or
     * {@link System#getProperties} to mirror Ant's automatic exposure of system
     * properties as Ant properties.
     * @param prop a property name to try to evaluate
     * @param predefs an unevaluated set of initial definitions
     * @param defs an ordered list of property mappings, e.g. {@link EditableProperties} instances
     * @return a value for the property, or null if it was not defined or a circularity error was detected
     */
    public static String evaluate(String prop, Map/*<String,String>*/ predefs, List/*<Map<String,String>>*/ defs) {
        // XXX could be faster for large mappings by using reverse analysis
        // (but caching is probably much easier if the entire map is kept)
        Map m = evaluateAll(predefs, defs);
        if (m != null) {
            return (String)m.get(prop);
        } else {
            return null;
        }
    }
    
    /**
     * Evaluate all properties in a list of property mappings.
     * Similar to {@link #evaluate} but produces all the values
     * at once.
     * <p>
     * If there are any cyclic definitions within a single mapping,
     * the evaluation will fail and return null.
     * @param defs an ordered list of property mappings, e.g. {@link EditableProperties} instances
     * @param predefs an unevaluated set of initial definitions
     * @return values for all defined properties, or null if a circularity error was detected
     */
    public static Map/*<String,String>*/ evaluateAll(Map/*<String,String>*/ predefs, List/*<Map<String,String>>*/ defs) {
        Map/*<String,String>*/ m = new HashMap(predefs);
        Iterator it = defs.iterator();
        while (it.hasNext()) {
            Map/*<String,String>*/ curr = (Map/*<String,String>*/)it.next();
            // Set of properties which we are deferring because they subst sibling properties:
            Map/*<String,Set<String>>*/ dependOnSiblings = new HashMap();
            Iterator it2 = curr.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry entry = (Map.Entry)it2.next();
                String prop = (String)entry.getKey();
                if (!m.containsKey(prop)) {
                    String rawval = (String)entry.getValue();
                    //System.err.println("subst " + prop + "=" + rawval + " with " + m);
                    Object o = subst(rawval, m, curr.keySet());
                    if (o instanceof String) {
                        m.put(prop, (String)o);
                    } else {
                        dependOnSiblings.put(prop, (Set)o);
                    }
                }
            }
            Set/*<String>*/ toSort = new HashSet(dependOnSiblings.keySet());
            it2 = dependOnSiblings.values().iterator();
            while (it2.hasNext()) {
                toSort.addAll((Set)it2.next());
            }
            List/*<String>*/ sorted;
            try {
                sorted = Utilities.topologicalSort(toSort, dependOnSiblings);
            } catch (TopologicalSortException e) {
                //System.err.println("Cyclic property refs: " + Arrays.asList(e.unsortableSets()));
                return null;
            }
            Collections.reverse(sorted);
            it2 = sorted.iterator();
            while (it2.hasNext()) {
                String prop = (String)it2.next();
                if (!m.containsKey(prop)) {
                    String rawval = (String)curr.get(prop);
                    m.put(prop, (String)subst(rawval, m, /*Collections.EMPTY_SET*/curr.keySet()));
                }
            }
        }
        return m;
    }
    
    /**
     * Evaluate a string that may have some Ant-style property references embedded in it.
     * See {@link #evaluate} for basic semantics information.
     * @param text a string with possible embedded property references
     * @param predefs an unevaluated set of initial definitions
     * @param defs an ordered list of property mappings, e.g. {@link EditableProperties} instances
     * @return a value for the text, or null if a circularity error was detected
     */
    public static String evaluateString(String text, Map/*<String,String>*/ predefs, List/*<Map<String,String>>*/ defs) {
        // XXX could be faster for large mappings; see evaluate(...)
        Map m = evaluateAll(predefs, defs);
        if (m != null) {
            return (String)subst(text, m, Collections.EMPTY_SET);
        } else {
            return null;
        }
    }
    
    /**
     * Try to substitute property references etc. in an Ant property value string.
     * @param rawval the raw value to be substituted
     * @param predefs a set of properties already defined
     * @param siblingProperties a set of property names that are yet to be defined
     * @return either a String, in case everything can be evaluated now;
     *         or a Set<String> of elements from siblingProperties in case those properties
     *         need to be defined in order to evaluate this one
     */
    private static Object subst(String rawval, Map/*<String,String>*/ predefs, Set/*<String>*/ siblingProperties) {
        if (rawval.indexOf('$') == -1) {
            // Shortcut:
            //System.err.println("shortcut");
            return rawval;
        }
        // May need to subst something.
        int idx = 0;
        // Result in progress, if it is to be a String:
        StringBuffer val = new StringBuffer();
        // Or, result in progress, if it is to be a Set<String>:
        Set/*<String>*/ needed = new HashSet();
        while (true) {
            int shell = rawval.indexOf('$', idx);
            if (shell == -1 || shell == rawval.length() - 1) {
                // No more $, or only as last char -> copy all.
                //System.err.println("no more $");
                if (needed.isEmpty()) {
                    val.append(rawval.substring(idx));
                    return val.toString();
                } else {
                    return needed;
                }
            }
            char c = rawval.charAt(shell + 1);
            if (c == '$') {
                // $$ -> $
                //System.err.println("$$");
                if (needed.isEmpty()) {
                    val.append('$');
                }
                idx += 2;
            } else if (c == '{') {
                // Possibly a property ref.
                int end = rawval.indexOf('}', shell + 2);
                if (end != -1) {
                    // Definitely a property ref.
                    String otherprop = rawval.substring(shell + 2, end);
                    //System.err.println("prop ref to " + otherprop);
                    if (predefs.containsKey(otherprop)) {
                        // Well-defined.
                        if (needed.isEmpty()) {
                            val.append(rawval.substring(idx, shell));
                            val.append((String)predefs.get(otherprop));
                        }
                        idx = end + 1;
                    } else if (siblingProperties.contains(otherprop)) {
                        needed.add(otherprop);
                        // don't bother updating val, it will not be used anyway
                        idx = end + 1;
                    } else {
                        // No def, leave as is.
                        if (needed.isEmpty()) {
                            val.append(rawval.substring(idx, end + 1));
                        }
                        idx = end + 1;
                    }
                } else {
                    // Unclosed ${ sequence, leave as is.
                    if (needed.isEmpty()) {
                        val.append(rawval.substring(idx));
                        return val.toString();
                    } else {
                        return needed;
                    }
                }
            } else {
                // $ followed by some other char, leave as is.
                if (needed.isEmpty()) {
                    val.append(rawval.substring(idx, idx + 2));
                }
                idx += 2;
            }
        }
    }
    
    /* public? */ static File resolveFile(File basedir, String filename) {
        String machinePath = filename.replace('/', File.separatorChar).replace('\\', File.separatorChar);
        File f = new File(machinePath);
        if (!f.isAbsolute()) {
            f = new File(basedir, machinePath);
        }
        assert f.isAbsolute();
        return new File(f.toURI().normalize());
    }
    
    /**
     * Produce a machine-independent relativized version of a filename from a basedir.
     * Unlike {@link URI#relativize} this will produce "../" sequences as needed.
     * @param basedir a directory to resolve relative to (need not exist on disk)
     * @param file a file or directory to find a relative path for
     * @return a relativized path (slash-separated), or null if it is not possible (e.g. different DOS drives);
     *         just <samp>.</samp> in case the paths are the same
     * @throws IllegalArgumentException if the basedir is known to be a file and not a directory
     */
    /* public? */ static String relativizeFile(File basedir, File file) {
        if (basedir.isFile()) {
            throw new IllegalArgumentException("Cannot relative w.r.t. a data file " + basedir); // NOI18N
        }
        if (basedir.equals(file)) {
            return "."; // NOI18N
        }
        StringBuffer b = new StringBuffer();
        File base = basedir;
        String filepath = file.getAbsolutePath();
        while (!filepath.startsWith(slashify(base.getAbsolutePath()))) {
            base = base.getParentFile();
            if (base == null) {
                return null;
            }
            b.append("../"); // NOI18N
        }
        URI u = base.toURI().relativize(file.toURI());
        assert !u.isAbsolute() : u + " from " + basedir + " and " + file + " with common root " + base;
        b.append(u);
        if (b.charAt(b.length() - 1) == '/') {
            // file is an existing directory and file.toURI ends in /
            // we do not want the trailing slash
            b.setLength(b.length() - 1);
        }
        return b.toString();
    }
    
    private static String slashify(String path) {
        if (path.endsWith(File.separator)) {
            return path;
        } else {
            return path + File.separatorChar;
        }
    }
    
    /*public? */ static FileObject resolveFileObject(FileObject basedir, String filename) {
        return FileUtil.toFileObject(resolveFile(FileUtil.toFile(basedir), filename));
    }
    
    /*public? */ static String resolvePath(File basedir, String path) {
        StringBuffer b = new StringBuffer();
        String[] toks = tokenizePath(path);
        for (int i = 0; i < toks.length; i++) {
            if (i > 0) {
                b.append(File.pathSeparatorChar);
            }
            b.append(resolveFile(basedir, toks[i]).getAbsolutePath());
        }
        return b.toString();
    }
    
    /**
     * Split an Ant-style path specification into components.
     * Tokenizes on <code>:</code> and <code>;</code>, paying
     * attention to DOS-style components such as <samp>C:\FOO</samp>.
     * Also removes any empty components.
     * @param path an Ant-style path (elements arbitrary) using DOS or Unix separators
     * @return a tokenization of that path into components
     */
    public static String[] tokenizePath(String path) {
        List/*<String>*/ l = new ArrayList();
        StringTokenizer tok = new StringTokenizer(path, ":;", true); // NOI18N
        char dosHack = '\0';
        char lastDelim = '\0';
        int delimCount = 0;
        while (tok.hasMoreTokens()) {
            String s = tok.nextToken();
            if (s.length() == 0) {
                // Strip empty components.
                continue;
            }
            if (s.length() == 1) {
                char c = s.charAt(0);
                if (c == ':' || c == ';') {
                    // Just a delimiter.
                    lastDelim = c;
                    delimCount++;
                    continue;
                }
            }
            if (dosHack != '\0') {
                if (lastDelim == ':' && delimCount == 1 && s.charAt(0) == '\\') {
                    // We had a single letter followed by ':' now followed by \something
                    s = "" + dosHack + ':' + s;
                    // and use the new token with the drive prefix...
                } else {
                    // Something else, leave alone.
                    l.add(Character.toString(dosHack));
                    // and continue with this token too...
                }
                dosHack = '\0';
            }
            // Reset count of # of delimiters in a row.
            delimCount = 0;
            if (s.length() == 1) {
                char c = s.charAt(0);
                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                    // Probably a DOS drive letter. Leave it with the next component.
                    dosHack = c;
                    continue;
                }
            }
            l.add(s);
        }
        return (String[])l.toArray(new String[l.size()]);
    }
    
    /**
     * Produce a trivial property producer using only a fixed list of property definitions.
     * Its values are constant, and it never fires changes.
     * @param defs a map from property names to values (it is illegal to modify this map
     *             after passing it to this method)
     * @return a matching property producer
     */
    /*XXX public?*/ static PropertyProvider fixedPropertyProvider(Map/*<String,String>*/ defs) {
        return new FixedPropertyProvider(defs);
    }
    
    private static final class FixedPropertyProvider implements PropertyProvider {
        
        private final Map/*<String,String>*/ defs;
        
        public FixedPropertyProvider(Map/*<String,String>*/ defs) {
            this.defs = defs;
        }
        
        public Map getProperties() {
            return defs;
        }
        
        public void addChangeListener(ChangeListener l) {}
        
        public void removeChangeListener(ChangeListener l) {}
        
    }
    
    /**
     * Produce a property evaluator based on a series of definitions.
     * Each batch of definitions can refer to properties within itself
     * (so long as there is no cycle) or any previous batch.
     * However the special first provider cannot refer to properties within itself.
     * @param preprovider an initial context (may be null)
     * @param providers a sequential list of property groups
     * @return an evaluator
     */
    /*XXX public*/ static PropertyEvaluator sequentialPropertyEvaluator(PropertyProvider preprovider, PropertyProvider[] providers) {
        return new SequentialPropertyEvaluator(preprovider, providers);
    }
    
    private static final class SequentialPropertyEvaluator implements PropertyEvaluator, ChangeListener {
        
        private final PropertyProvider preprovider;
        private final PropertyProvider[] providers;
        private Map/*<String,String>*/ defs;
        private final List/*<PropertyChangeListener>*/ listeners = new ArrayList();
        
        public SequentialPropertyEvaluator(PropertyProvider preprovider, PropertyProvider[] providers) {
            this.preprovider = preprovider;
            this.providers = providers;
            // XXX defer until someone asks for them
            defs = compose(preprovider, providers);
            // XXX defer until someone is listening?
            if (preprovider != null) {
                preprovider.addChangeListener(WeakListeners.change(this, preprovider));
            }
            for (int i = 0; i < providers.length; i++) {
                providers[i].addChangeListener(WeakListeners.change(this, providers[i]));
            }
        }
        
        public String getProperty(String prop) {
            return (String)defs.get(prop);
        }
        
        public String evaluate(String text) {
            Object result = subst(text, defs, Collections.EMPTY_SET);
            assert result instanceof String : "Unexpected result " + result + " from " + text + " on " + defs;
            return (String)result;
        }
        
        public Map getProperties() {
            return defs;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            listeners.add(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            listeners.add(listener);
        }
        
        public void stateChanged(ChangeEvent e) {
            Map/*<String,String>*/ newdefs = compose(preprovider, providers);
            if (!defs.equals(newdefs)) {
                Set/*<String>*/ props = new HashSet(defs.keySet());
                props.addAll(newdefs.keySet());
                List/*<PropertyChangeEvent>*/ events = new LinkedList();
                Iterator it = props.iterator();
                while (it.hasNext()) {
                    String prop = (String)it.next();
                    assert prop != null;
                    String oldval = (String)defs.get(prop);
                    String newval = (String)newdefs.get(prop);
                    if (newval != null) {
                        if (newval.equals(oldval)) {
                            continue;
                        }
                    } else {
                        assert oldval != null : "should not have had " + prop;
                    }
                    events.add(new PropertyChangeEvent(this, prop, oldval, newval));
                }
                assert !events.isEmpty();
                defs = newdefs;
                Iterator it2 = listeners.iterator();
                while (it2.hasNext()) {
                    PropertyChangeListener l = (PropertyChangeListener)it2.next();
                    Iterator it3 = events.iterator();
                    while (it3.hasNext()) {
                        l.propertyChange((PropertyChangeEvent)it3.next());
                    }
                }
            }
        }
        
        private static Map/*<String,String>*/ compose(PropertyProvider preprovider, PropertyProvider[] providers) {
            Map/*<String,String>*/ predefs;
            if (preprovider != null) {
                predefs = preprovider.getProperties();
            } else {
                predefs = Collections.EMPTY_MAP;
            }
            Map/*<String,String>*/[] defs = new Map[providers.length];
            for (int i = 0; i < providers.length; i++) {
                defs[i] = providers[i].getProperties();
            }
            return evaluateAll(predefs, Arrays.asList(defs));
        }
        
    }
    
}
