/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.openide.ErrorManager;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;
import org.openide.xml.XMLUtil;
import org.netbeans.*;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * Parser and interpreter for automatic module dependencies.
 * Public for possible access from AU (see #29577).
 * Usage: see implementation of {@link NbInstaller#refineDependencies}.
 * <p>You may run this class standalone, with just openide.jar and core.jarin the classpath.
 * @author Jesse Glick, with help from NB XML module
 * @see "#30161"
 * @see <a href="module-auto-deps-1_0.dtd"><samp>-//NetBeans//DTD Module Automatic Dependencies 1.0//EN</samp></a>
 * @since org.netbeans.core/1 1.12
 */
public final class AutomaticDependencies {

    private AutomaticDependencies() {}
    
    /**
     * Create an empty list of transformations.
     * @return an empty list
     */
    public static AutomaticDependencies empty() {
        return new AutomaticDependencies();
    }
    
    /**
     * Create a list of transformations based on some XML files.
     * They must be valid (this method may not validate them).
     * Doctype must be <samp>&lt;transformations&gt;</samp> from
     * <samp>-//NetBeans//DTD Module Automatic Dependencies 1.0//EN</samp>.
     * @param urls the XML files
     * @throws SAXException if malformed
     * @throws IOException if unloadable
     * @return a list of parsed transformations
     */
    public static AutomaticDependencies parse(URL[] urls) throws SAXException, IOException {
        AutomaticDependencies h = new AutomaticDependencies();
        Parser p = new Parser(h.new Handler());
        for (int i = 0; i < urls.length; i++) {
            String id = urls[i].toExternalForm();
            try {
                p.parse(new InputSource(id));
            } catch (SAXException e) {
                Util.err.annotate(e, ErrorManager.UNKNOWN, "While parsing: " + id, null, null, null);
                throw e;
            } catch (IOException e) {
                Util.err.annotate(e, ErrorManager.UNKNOWN, "While parsing: " + id, null, null, null);
                throw e;
            }
        }
        return h;
    }
    
    /**
     * For testing purposes only. Should dump file structure.
     * @param x list of URLs to parse
     */
    public static void main(String[] x) throws Exception {
        URL[] urls = new URL[x.length];
        for (int i = 0; i < x.length; i++) {
            urls[i] = new URL(x[i]);
        }
        parse(urls); // warm up classes
        long time = System.currentTimeMillis();
        System.out.println(parse(urls));
        long taken = System.currentTimeMillis() - time;
        System.out.println("Time taken: " + taken + " msec");
    }
    
    // ------------- INTERPRETATION --------------
    
    /**
     * A struct for holding information on the result of dependency refinement.
     * @see #refineDependenciesAndReport
     * @since org.netbeans.core/1 1.19
     */
    public static final class Report {
        private final Set/*<Dependency>*/ added;
        private final Set/*<Dependency>*/ removed;
        private final Set/*<String>*/ messages;
        Report(Set added, Set removed, Set messages) {
            this.added = added;
            this.removed = removed;
            this.messages = messages;
        }
        /**
         * Get a set of dependencies that were added.
         * @return a set of {@link Dependency} (may be empty)
         */
        public Set/*<Dependency>*/ getAdded() {
            return added;
        }
        /**
         * Get a set of dependencies that were removed.
         * @return a set of {@link Dependency} (may be empty)
         */
        public Set/*<Dependency>*/ getRemoved() {
            return removed;
        }
        /**
         * Get a set of messages describing why transformations occurred.
         * These messages are not necessarily localized but are intended for developers.
         * @return a set of <code>String</code> messages (may be empty)
         */
        public Set/*<String>*/ getMessages() {
            return messages;
        }
        /**
         * Check if any changes were made.
         * @return true if there is anything to report
         */
        public boolean isModified() {
            return !added.isEmpty() || !removed.isEmpty();
        }
    }
    
    /**
     * Interpret the transformations on a module, and report the changes.
     * The module's dependencies may be added to, changed, or removed, according
     * to the configuration of this transformations list.
     * @param cnb the code name base of the module being considered
     * @param dependencies a mutable set of type {@link Dependency}; call-by-reference
     * @since org.netbeans.core/1 1.19
     */
    public Report refineDependenciesAndReport(String cnb, Set dependencies) {
        Set/*<Dependency>*/ oldDependencies = new HashSet(dependencies);
        // First, collect all deps of each type by name, for quick access.
        // Also note that transformations apply *in parallel* so we cannot
        // check triggers of a transformation based on the in-progress set.
        Map modDeps = new HashMap(); // Map<String,Dependency>
        Map tokDeps = new HashMap(); // Map<String,Dependency>
        Map pkgDeps = new HashMap(); // Map<String,Dependency>
        Iterator it = dependencies.iterator();
        while (it.hasNext()) {
            Dependency d = (Dependency)it.next();
            switch (d.getType()) {
            case Dependency.TYPE_MODULE:
                String dcnb = (String)Util.parseCodeName(d.getName())[0];
                modDeps.put(dcnb, d);
                break;
            case Dependency.TYPE_PACKAGE:
                String name = packageBaseName(d.getName());
                pkgDeps.put(name, d);
                break;
            case Dependency.TYPE_REQUIRES:
                tokDeps.put(d.getName(), d);
                break;
            case Dependency.TYPE_IDE:
                throw new IllegalArgumentException("No TYPE_IDE dependencies permitted for " + cnb);
            case Dependency.TYPE_JAVA:
                // ignored
                break;
            default:
                throw new IllegalStateException(d.toString());
            }
        }
        Set/*<String>*/ messages = new TreeSet();
        // Now go through transformations and see if they apply.
        it = groups.iterator();
        while (it.hasNext()) {
            TransformationGroup g = (TransformationGroup)it.next();
            if (g.isExcluded(cnb)) {
                continue;
            }
            Set/*<Dependency>*/ oldRunningDependencies = new HashSet(dependencies);
            Iterator it2 = g.transformations.iterator();
            while (it2.hasNext()) {
                Transformation t = (Transformation)it2.next();
                t.apply(modDeps, tokDeps, pkgDeps, dependencies);
            }
            if (!oldRunningDependencies.equals(dependencies)) {
                messages.add(g.description);
            }
        }
        if (!oldDependencies.equals(dependencies)) {
            assert !messages.isEmpty();
            Set/*<Dependency>*/ added = new HashSet(dependencies);
            added.removeAll(oldDependencies);
            oldDependencies.removeAll(dependencies);
            return new Report(added, oldDependencies, messages);
        } else {
            assert messages.isEmpty();
            return new Report(Collections.EMPTY_SET, Collections.EMPTY_SET, Collections.EMPTY_SET);
        }
    }
    
    /**
     * Interpret the transformations on a module.
     * The module's dependencies may be added to, changed, or removed, according
     * to the configuration of this transformations list.
     * Does the same thing as {@link #refineDependenciesAndReport} but does not report on the details.
     * @param cnb the code name base of the module being considered
     * @param dependencies a mutable set of type {@link Dependency}; call-by-reference
     */
    public void refineDependencies(String cnb, Set dependencies) {
        refineDependenciesAndReport(cnb, dependencies);
    }
    
    // ---------------- STRUCTS --------------------
    
    private final List groups = new ArrayList(); // List<TransformationGroup>
    
    public String toString() {
        return "AutomaticDependencies[" + groups + "]";
    }
    
    private static final class Exclusion {
        public Exclusion() {}
        public String codenamebase;
        public boolean prefix;
        public String toString() {
            return "Exclusion[" + codenamebase + ",prefix=" + prefix + "]";
        }
        
        /**
         * Does this exclusion apply to the given code name base?
         */
        public boolean matches(String cnb) {
            return cnb.equals(codenamebase) ||
                (prefix && cnb.startsWith(codenamebase + ".")); // NOI18N
        }
        
    }
    
    private static final class TransformationGroup {
        public TransformationGroup() {}
        public String description;
        public final List exclusions = new ArrayList(); // List<Exclusion>
        public final List transformations = new ArrayList(); // List<Transformation>
        public String toString() {
            return "TransformationGroup[" + exclusions + "," + transformations + "]";
        }
        
        /**
         * Is the given code name base excluded from this group of transformations?
         */
        public boolean isExcluded(String cnb) {
            Iterator it = exclusions.iterator();
            while (it.hasNext()) {
                if (((Exclusion)it.next()).matches(cnb)) {
                    return true;
                }
            }
            return false;
        }
        
    }
    
    private static final class Transformation {
        public Transformation() {}
        public Dep trigger;
        public String triggerType;
        public final List results = new ArrayList(); // List<Dep>
        public String toString() {
            return "Transformation[trigger=" + trigger + ",triggerType=" + triggerType + ",results=" + results + "]";
        }
        
        /**
         * Transform some dependencies.
         */
        public void apply(Map modDeps, Map tokDeps, Map pkgDeps, Set dependencies) {
            Dependency d = trigger.applies(modDeps, tokDeps, pkgDeps, dependencies, triggerType);
            if (d != null) {
                // It matched.
                if (triggerType.equals("cancel")) {
                    // Remove it now.
                    dependencies.remove(d);
                } else if (triggerType.equals("older")) {
                    // Fine, don't.
                } else {
                    throw new IllegalStateException(triggerType);
                }
                // Add in results.
                Iterator it = results.iterator();
                while (it.hasNext()) {
                    Dep nue = (Dep)it.next();
                    nue.update(modDeps, tokDeps, pkgDeps, dependencies);
                }
            }
        }
        
    }
    
    private static abstract class Dep {
        public Dep() {}
        public final String toString() {
            return manifestKey() + ": " + toManifestForm();
        }
        
        /**
         * The form of the dependency in a manifest (value only).
         */
        public abstract String toManifestForm();
        
        /**
         * The form of the dependency in a manifest (key part).
         */
        public abstract String manifestKey();
        
        /**
         * The type of dependency according to {@link Dependency}.
         */
        public abstract int type();
        
        /**
         * Make a real dependency from this pattern.
         */
        public final Dependency createDependency() {
            return (Dependency)Dependency.create(type(), toManifestForm()).iterator().next();
        }
        
        /**
         * Check whether this dependency pattern applies as a trigger.
         * @return the triggered actual dependency if so, else null
         */
        public abstract Dependency applies(Map modDeps, Map tokDeps, Map pkgDeps, Set dependencies, String type);
        
        /**
         * Update actual dependencies assuming a trigger matched.
         * This dependency pattern is to be added to the dependencies set
         * (possibly upgrading an existing dependency).
         */
        public abstract void update(Map modDeps, Map tokDeps, Map pkgDeps, Set dependencies);
        
    }
    
    private static final class ModuleDep extends Dep {
        public ModuleDep() {}
        public String codenamebase;
        public int major = -1;
        public SpecificationVersion spec = null;
        
        public String toManifestForm() {
            return codenamebase + (major == -1 ? "" : "/" + major) + (spec == null ? "" : " > " + spec);
        }
        
        public String manifestKey() {
            return "OpenIDE-Module-Module-Dependencies";
        }
        
        public int type() {
            return Dependency.TYPE_MODULE;
        }
        
        public Dependency applies(Map modDeps, Map tokDeps, Map pkgDeps, Set dependencies, String type) {
            Dependency d = (Dependency)modDeps.get(codenamebase);
            if (d == null) return null;
            if (type.equals("cancel")) {
                // That's enough.
                return d;
            } else if (type.equals("older")) {
                // Compare. Check that d < this
                return older(d) ? d : null;
            } else {
                throw new IllegalArgumentException(type);
            }
        }
        
        /**
         * Is the given dependency older than this pattern?
         */
        private boolean older(Dependency d) {
            if (d.getType() != Dependency.TYPE_MODULE) throw new IllegalArgumentException();
            if (d.getComparison() == Dependency.COMPARE_IMPL) {
                // #46961; do not upgrade impl deps like this.
                return false;
            }
            Integer dRelI = (Integer)Util.parseCodeName(d.getName())[1];
            int dRel = (dRelI == null) ? -1 : dRelI.intValue();
            if (dRel < major) return true;
            if (dRel > major) return false;
            if (spec == null) return false;
            String dSpec = d.getVersion();
            if (dSpec == null) return true;
            assert d.getComparison() == Dependency.COMPARE_SPEC : d.getComparison();
            return new SpecificationVersion(dSpec).compareTo(spec) < 0;
        }
        
        public void update(Map modDeps, Map tokDeps, Map pkgDeps, Set dependencies) {
            Dependency d = (Dependency)modDeps.get(codenamebase);
            if (d != null && older(d)) {
                dependencies.remove(d);
                Dependency nue = createDependency();
                assert !nue.equals(d) : "older() claimed to be true on itself for " + d;
                dependencies.add(nue);
            } else if (d == null) {
                dependencies.add(createDependency());
            }
        }
        
    }
    
    /**
     * Find actual package name from a package dep name.
     * "javax.tv" -> "javax.tv"
     * "javax.tv[Tuner]" -> "javax.tv"
     * "[javax.tv.Tuner]" -> "javax.tv"
     */
    private static String packageBaseName(String name) {
        int i = name.indexOf('[');
        if (i == -1) {
            return name;
        } else if (i > 0) {
            return name.substring(0, i);
        } else {
            int i2 = name.lastIndexOf('.');
            return name.substring(1, i2);
        }
    }
        
    private static final class PackageDep extends Dep {
        public PackageDep() {}
        public String name;
        public String bname;
        public SpecificationVersion spec = null;
        
        public String toManifestForm() {
            return name + (spec == null ? "" : " > " + spec);
        }
        
        public String manifestKey() {
            return "OpenIDE-Module-Package-Dependencies";
        }
        
        public int type() {
            return Dependency.TYPE_PACKAGE;
        }
        
        /**
         * Is the given dependency older than this pattern?
         */
        private boolean older(Dependency d) {
            if (d.getType() != Dependency.TYPE_PACKAGE) throw new IllegalArgumentException();
            if (d.getComparison() == Dependency.COMPARE_IMPL) {
                // #46961; do not upgrade impl deps like this.
                return false;
            }
            if (spec == null) return false;
            String dSpec = d.getVersion();
            if (dSpec == null) return true;
            assert d.getComparison() == Dependency.COMPARE_SPEC : d.getComparison();
            return new SpecificationVersion(dSpec).compareTo(spec) < 0;
        }
        
        public Dependency applies(Map modDeps, Map tokDeps, Map pkgDeps, Set dependencies, String type) {
            Dependency d = (Dependency)pkgDeps.get(bname);
            if (d == null) {
                return null;
            }
            if (type.equals("cancel")) {
                // That's enough.
                return d;
            } else if (type.equals("older")) {
                if (spec == null) throw new IllegalStateException();
                // Compare. Check that d < this
                return older(d) ? d : null;
            } else {
                throw new IllegalStateException(type);
            }
        }
        
        public void update(Map modDeps, Map tokDeps, Map pkgDeps, Set dependencies) {
            Dependency d = (Dependency)pkgDeps.get(bname);
            if (d != null && older(d)) {
                dependencies.remove(d);
                dependencies.add(createDependency());
            } else if (d == null) {
                dependencies.add(createDependency());
            }
        }
        
    }
    
    private static final class TokenDep extends Dep {
        public TokenDep() {}
        public String name;
        
        public String toManifestForm() {
            return name;
        }
        
        public String manifestKey() {
            return "OpenIDE-Module-Requires";
        }
        
        public int type() {
            return Dependency.TYPE_REQUIRES;
        }
        
        public Dependency applies(Map modDeps, Map tokDeps, Map pkgDeps, Set dependencies, String type) {
            Dependency d = (Dependency)tokDeps.get(name);
            if (d == null) {
                return null;
            }
            if (type.equals("cancel")) {
                // That's enough.
                return d;
            } else {
                // older is not supported
                throw new IllegalStateException(type);
            }
        }
        
        public void update(Map modDeps, Map tokDeps, Map pkgDeps, Set dependencies) {
            if (tokDeps.get(name) == null) {
                dependencies.add(createDependency());
            }
        }
        
    }
    
    // ---------------------- PARSING -----------------------
    
    private final class Handler {
        
        private TransformationGroup currentGroup = null;
        private Transformation currentTransformation = null;
        private boolean inTrigger = false;

        Handler() {}
    
        public void start_trigger(final Attributes meta) throws SAXException {
            inTrigger = true;
            currentTransformation.triggerType = meta.getValue("type");
        }

        public void end_trigger() throws SAXException {
            inTrigger = false;
        }

        public void start_transformation(final Attributes meta) throws SAXException {
            currentTransformation = new Transformation();
        }

        public void end_transformation() throws SAXException {
            currentGroup.transformations.add(currentTransformation);
            currentTransformation = null;
        }

        private void handleDep(Dep d) throws SAXException {
            if (inTrigger) {
                currentTransformation.trigger = d;
            } else {
                currentTransformation.results.add(d);
            }
        }

        public void handle_module_dependency(final Attributes meta) throws SAXException {
            ModuleDep d = new ModuleDep();
            String major = meta.getValue("major");
            if (major != null) {
                d.major = Integer.parseInt(major);
            }
            d.codenamebase = meta.getValue("codenamebase");
            String s = meta.getValue("spec");
            d.spec = (s == null) ? null : new SpecificationVersion(s);
            handleDep(d);
        }

        public void handle_token_dependency(final Attributes meta) throws SAXException {
            TokenDep d = new TokenDep();
            d.name = meta.getValue("name");
            handleDep(d);
        }

        public void handle_package_dependency(final Attributes meta) throws SAXException {
            PackageDep d = new PackageDep();
            d.name = meta.getValue("name");
            d.bname = packageBaseName(d.name);
            if (inTrigger) {
                if (!d.name.equals(d.bname)) throw new SAXException("Cannot use test class in trigger");
            }
            String s = meta.getValue("spec");
            d.spec = (s == null) ? null : new SpecificationVersion(s);
            handleDep(d);
        }

        public void start_transformationgroup(final Attributes meta) throws SAXException {
            currentGroup = new TransformationGroup();
        }

        public void end_transformationgroup() throws SAXException {
            groups.add(currentGroup);
            currentGroup = null;
        }

        public void start_result(final Attributes meta) throws SAXException {
            // do nothing
        }

        public void end_result() throws SAXException {
            // do nothing
        }

        public void handle_exclusion(final Attributes meta) throws SAXException {
            Exclusion excl = new Exclusion();
            excl.codenamebase = meta.getValue("codenamebase");
            excl.prefix = Boolean.valueOf(meta.getValue("prefix")).booleanValue();
            currentGroup.exclusions.add(excl);
        }

        public void handle_description(final String data, final Attributes meta) throws SAXException {
            currentGroup.description = data;
        }

        public void start_transformations(final Attributes meta) throws SAXException {
            if (!"1.0".equals(meta.getValue("version"))) throw new SAXException("Unsupported DTD");
            // do nothing
        }

        public void end_transformations() throws SAXException {
            // do nothing
        }

        public void start_results(final Attributes meta) throws SAXException {
            // do nothing
        }

        public void end_results() throws SAXException {
            // do nothing
        }
    
    }
    
    private static final class Parser implements ContentHandler, ErrorHandler, EntityResolver {

        private java.lang.StringBuffer buffer;

        private Handler handler;

        private java.util.Stack context;

        public Parser(final Handler handler) {
            this.handler = handler;
            buffer = new StringBuffer(111);
            context = new java.util.Stack();
        }

        public final void setDocumentLocator(Locator locator) {
        }

        public final void startDocument() throws SAXException {
        }

        public final void endDocument() throws SAXException {
        }

        public final void startElement(java.lang.String ns, java.lang.String name, java.lang.String qname, Attributes attrs) throws SAXException {
            dispatch(true);
            context.push(new Object[] {qname, new org.xml.sax.helpers.AttributesImpl(attrs)});
            if ("trigger-dependency".equals(qname)) {
                handler.start_trigger(attrs);
            } else if ("transformation".equals(qname)) {
                handler.start_transformation(attrs);
            } else if ("module-dependency".equals(qname)) {
                handler.handle_module_dependency(attrs);
            } else if ("transformationgroup".equals(qname)) {
                handler.start_transformationgroup(attrs);
            } else if ("result".equals(qname)) {
                handler.start_result(attrs);
            } else if ("exclusion".equals(qname)) {
                handler.handle_exclusion(attrs);
            } else if ("token-dependency".equals(qname)) {
                handler.handle_token_dependency(attrs);
            } else if ("package-dependency".equals(qname)) {
                handler.handle_package_dependency(attrs);
            } else if ("transformations".equals(qname)) {
                handler.start_transformations(attrs);
            } else if ("implies".equals(qname)) {
                handler.start_results(attrs);
            }
        }

        public final void endElement(java.lang.String ns, java.lang.String name, java.lang.String qname) throws SAXException {
            dispatch(false);
            context.pop();
            if ("trigger-dependency".equals(qname)) {
                handler.end_trigger();
            } else if ("transformation".equals(qname)) {
                handler.end_transformation();
            } else if ("transformationgroup".equals(qname)) {
                handler.end_transformationgroup();
            } else if ("result".equals(qname)) {
                handler.end_result();
            } else if ("transformations".equals(qname)) {
                handler.end_transformations();
            } else if ("implies".equals(qname)) {
                handler.end_results();
            }
        }

        public final void characters(char[] chars, int start, int len) throws SAXException {
            buffer.append(chars, start, len);
        }

        public final void ignorableWhitespace(char[] chars, int start, int len) throws SAXException {
        }

        public final void processingInstruction(java.lang.String target, java.lang.String data) throws SAXException {
        }

        public final void startPrefixMapping(final java.lang.String prefix, final java.lang.String uri) throws SAXException {
        }

        public final void endPrefixMapping(final java.lang.String prefix) throws SAXException {
        }

        public final void skippedEntity(java.lang.String name) throws SAXException {
        }

        private void dispatch(final boolean fireOnlyIfMixed) throws SAXException {
            if (fireOnlyIfMixed && buffer.length() == 0) return; //skip it

            Object[] ctx = (Object[]) context.peek();
            String here = (String) ctx[0];
            Attributes attrs = (Attributes) ctx[1];
            if ("description".equals(here)) {
                if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected characters() event! (Missing DTD?)");
                handler.handle_description(buffer.length() == 0 ? null : buffer.toString(), attrs);
            } else {
                //do not care
            }
            buffer.delete(0, buffer.length());
        }

        /**
         * The recognizer entry method taking an InputSource.
         * @param input InputSource to be parsed.
         * @throws IOException on I/O error.
         * @throws SAXException propagated exception thrown by a DocumentHandler.
         */
        public void parse(final InputSource input) throws SAXException, IOException {
            XMLReader parser = XMLUtil.createXMLReader(false, false); // fastest mode
            parser.setContentHandler(this);
            parser.setErrorHandler(this);
            parser.setEntityResolver(this);
            parser.parse(input);
        }

        public void error(SAXParseException ex) throws SAXException  {
            //if (context.isEmpty()) System.err.println("Missing DOCTYPE.");
            throw ex;
        }

        public void fatalError(SAXParseException ex) throws SAXException {
            throw ex;
        }

        public void warning(SAXParseException ex) throws SAXException {
            // ignore
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            // Not validating, so skip DTD.
            return new InputSource(new ByteArrayInputStream(new byte[0]));
        }

    }

}
