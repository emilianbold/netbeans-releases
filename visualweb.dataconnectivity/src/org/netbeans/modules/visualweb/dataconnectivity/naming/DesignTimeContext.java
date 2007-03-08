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
package org.netbeans.modules.visualweb.dataconnectivity.naming;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.spi.NamingManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

/**
 * Creator's naming context
 *
 * @author John Kline
 */
class DesignTimeContext implements Context {
    static final String  USER_CTX     = "context.xml"; // NOI18N
    public static final String  ROOT_CTX_TAG = "rootContext"; // NOI18N
    public static final String  CTX_TAG      = "context"; // NOI18N
    public static final String  OBJ_TAG      = "object"; // NOI18N
    public static final String  ARG_TAG      = "arg"; // NOI18N
    public static final String  NAME_ATTR    = "name"; // NOI18N
    public static final String  CLASS_ATTR   = "class"; // NOI18N
    public static final String  VALUE_ATTR   = "value"; // NOI18N
    private static final int    TAB_WIDTH    = 4;
    private DesignTimeContext   parent;
    private String       ctxName;
    private TreeMap      map;
    private Hashtable    env;
    private NameParser   nameParser = DesignTimeNameParser.getInstance();
    private String       ctxPathName;
    private File         userCtxFile;
    private boolean      initMode;   /* used only by initial context ctor, signals not to
                                      * call saveContext during InitialContext construction
                                      */

    private static ResourceBundle rb = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataconnectivity.naming.Bundle", // NOI18N
        Locale.getDefault());

    private ObjectChangeListener objectChangeListener = new ObjectChangeListener() {
        public void objectChanged(ObjectChangeEvent evt) throws NamingException {
            Log.getLogger().entering(getClass().getName(), "objectChanged", evt); //NOI18N
            saveContext();
        }
    };

    // entry for subcontexts in a context's TreeMap (map)
    private class Subcontext {
        private String            subcontextName;
        private DesignTimeContext subcontext;
            Subcontext(String subcontextName, DesignTimeContext subcontext) {
            this.subcontextName = subcontextName;
            this.subcontext     = subcontext;
        }
    }

    // ctor for creating initial context
    public DesignTimeContext(Hashtable env) {
        Log.getLogger().entering(getClass().getName(), "DesignTimeContext", env); //NOI18N
        initMode    = true;
        parent      = null;
        ctxName     = null;
        ctxPathName = System.getProperty("netbeans.user") + File.separator + File.separator + USER_CTX; // NOI18N
        userCtxFile = new File(ctxPathName);
        map         = new TreeMap();
        this.env    = new Hashtable(env);
        //EnvProcessor envProcessor = new EnvProcessor(this);

        /*
         * Look for context.xml in the user's directory.  If doesn't exist, copy
         * the master file from netbeans.home/../ide4/startup/samples.
         */
        try {
            if (!userCtxFile.exists()) {
                writeNewUserContextFile();
            }
            try {
                parseContextFile();
            } catch (SAXException e) {
                rebuildContextFile(e);
            } catch (IOException e) {
                rebuildContextFile(e);
            }
        } catch (Exception e) {
            Log.getLogger().log(java.util.logging.Level.SEVERE, "DesignTimeContext()", e); // NOI18N
            e.printStackTrace();
        } finally {
            initMode = false;
        }
    }

    private void rebuildContextFile(Exception e)
        throws IOException, SAXException, NamingException, ParserConfigurationException {

        Log.getLogger().log(java.util.logging.Level.FINER, "parseContextFile()", e); // NOI18N
        // try again after writing default context
        Log.getLogger().log(java.util.logging.Level.FINER, "saveUserContextFile()"); // NOI18N
        saveUserContextFile();
        Log.getLogger().log(java.util.logging.Level.FINER, "writeNewUserContextFile()"); // NOI18N
        writeNewUserContextFile();
        close();
        map = new TreeMap();
        this.env = new Hashtable(env);
        Log.getLogger().log(java.util.logging.Level.FINER, "parseConextFile()"); // NOI18N
        parseContextFile();
    }

    // ctor for creating subcontexts
    public DesignTimeContext(DesignTimeContext parent, String ctxName, Hashtable env) {
        Log.getLogger().entering(getClass().getName(), "DesignTimeContext", //NOI18N
            new Object[] {parent, ctxName, env});
        this.parent  = parent;
        this.ctxName = ctxName;
        this.map     = new TreeMap();
        this.env     = new Hashtable(env);
    }

/* don't support cloning - at least not now
    // ctor for cloning contexts (lookup with empty name)
    public DesignTimeContext(DesignTimeContext parent, String ctxName,
        TreeMap map, Hashtable env) {

        this.parent  = parent;
        this.ctxName = ctxName;
        this.map     = new TreeMap(map);
        this.env     = new Hashtable(env);
    }
 */

    protected String getFullName() {
        if (parent == null) {
            return "";
        } else {
            return parent.getFullName() + ctxName + "/"; // NOI18N
        }
    }

    public Object lookup(Name name) throws NamingException {
        Log.getLogger().entering(getClass().getName(), "lookup", name); //NOI18N
        // get to the correct context and then look up in map
        if (name.size() == 0) {
            // we don't allow cloning a context
            throw new UnsupportedOperationException(rb.getString("CLONING_NOT_SUPPORTED"));
            //return new DesignTimeContext(parent, ctxName, map, env);
        } else if (name.size() == 1) {
            Object obj = map.get(name.get(0));
            if (obj == null) {
                NameNotFoundException e = new NameNotFoundException(getFullName() + name);
                e.setRemainingName(name);
                e.setResolvedObj(this);
                throw e;
            } else if (obj instanceof Subcontext) {
                return ((Subcontext)obj).subcontext;
            } else {
                return obj;
            }
        } else {
            Object obj = map.get(name.get(0));
            if (obj == null || (!(obj instanceof Subcontext))) {
                NameNotFoundException e = new NameNotFoundException(getFullName() + name);
                e.setRemainingName(name);
                e.setResolvedObj(this);
                throw e;
            } else {
                Context subcontext = ((Subcontext)obj).subcontext;
                return subcontext.lookup(name.getSuffix(1));
            }
        }
    }

    public Object lookup(String name) throws NamingException {
        return lookup(new CompositeName(name));
    }

    public void bind(Name name, Object obj) throws NamingException {
        Log.getLogger().entering(getClass().getName(), "bind", new Object[] {name, obj}); //NOI18N
        if (name.size() == 0) {
            throw new NamingException(rb.getString("NAME_IS_EMPTY"));
        }
        boolean nameExists = false;
        try {
            lookup(name);
            nameExists = true;
        } catch (NamingException e) {
        }
        if (nameExists) {
            throw new NameAlreadyBoundException(name.toString());
        }
        if (name.size() == 1) {
            map.put(name.get(0), obj);
            if (obj instanceof ContextPersistance) {
                ((ContextPersistance)obj).addObjectChangeListener(objectChangeListener);
            }
            saveContext();
        } else {
            Object subCtx = lookup(name.getPrefix(name.size() - 1));
            if (!(subCtx instanceof Context)) {
                NamingException e = new NamingException(
                MessageFormat.format(rb.getString("NAME_NOT_INSTANCE_OF_CONTEXT"),
                                new Object[] { name.getPrefix(name.size() - 1).toString() }));
                e.setRemainingName(new CompositeName(name.get(name.size() - 1)));
                e.setResolvedObj(subCtx);
                throw e;
            }
            ((Context)subCtx).bind(name.get(name.size() - 1), obj);
        }
    }

    public void bind(String name, Object obj) throws NamingException {
        bind(new CompositeName(name), obj);
    }

    public void rebind(Name name, Object obj) throws NamingException {
        Log.getLogger().entering(getClass().getName(), "rebind", new Object[] {name, obj}); //NOI18N
        try {
            unbindInternal(name, false);
        } catch (NamingException e) {
        }
        bind(name, obj);
    }

    public void rebind(String name, Object obj) throws NamingException {
        rebind(new CompositeName(name), obj);
    }

    public void unbind(Name name) throws NamingException {
        Log.getLogger().entering(getClass().getName(), "unbind", name); //NOI18N
        unbindInternal(name, true);
    }

    private void unbindInternal(Name name, boolean callSaveContext) throws NamingException {
        if (name.size() == 0) {
            return;
        } else if (name.size() == 1) {
            Object obj = map.remove(name.get(0));
            if (obj != null && obj instanceof ContextPersistance) {
                ((ContextPersistance)obj).removeObjectChangeListener(objectChangeListener);
            }
            if (callSaveContext) {
                saveContext();
            }
        } else {
            Object obj = lookup(name.getPrefix(name.size() - 1));
            if (!(obj instanceof Context)) {
                throw new NameNotFoundException(name.getPrefix(name.size() - 1).toString());
            } else {
                ((Context)obj).unbind(name.getSuffix(name.size() - 1));
            }
        }
    }

    public void unbind(String name) throws NamingException {
        unbind(new CompositeName(name));
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        Log.getLogger().entering(getClass().getName(), "rename", new Object[] {oldName, newName}); //NOI18N
        Object obj = lookup(oldName);
        unbind(oldName);
        bind(newName, obj);
    }

    public void rename(String oldName, String newName) throws NamingException {
        rename(new CompositeName(oldName), new CompositeName(newName));
    }

    public NamingEnumeration list(Name name) throws NamingException {
        Log.getLogger().entering(getClass().getName(), "list", name); //NOI18N
        if (name.size() == 0) {
            Vector v = new Vector();
            for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                String key = (String)i.next();
                Object obj = map.get(key);
                if (obj instanceof Subcontext) {
                    obj = ((Subcontext)obj).subcontext;
                }
                v.add(new NameClassPair(key, obj.getClass().getName(), true));
            }
            return new DesignTimeNamingEnumeration(v.elements());
        } else {
            Object obj = lookup(name);
            if (!(obj instanceof Context)) {
                throw new NameNotFoundException(name.toString());
            } else {
                return ((Context)obj).list(new CompositeName());
            }
        }
    }

    public NamingEnumeration list(String name) throws NamingException {
        return list(new CompositeName(name));
    }

    public NamingEnumeration listBindings(Name name) throws NamingException {
        Log.getLogger().entering(getClass().getName(), "listBindings", name); //NOI18N
        if (name.size() == 0) {
            Vector v = new Vector();
            for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                String key = (String)i.next();
                Object obj = map.get(key);
                if (obj instanceof Subcontext) {
                    obj = ((Subcontext)obj).subcontext;
                }
                v.add(new Binding(key, obj, true));
            }
            return new DesignTimeNamingEnumeration(v.elements());
        } else {
            Object obj = lookup(name);
            if (!(obj instanceof Context)) {
                throw new NameNotFoundException(name.toString());
                } else {
                return ((Context)obj).listBindings(new CompositeName());
            }
        }
    }

    public NamingEnumeration listBindings(String name) throws NamingException {
        return listBindings(new CompositeName(name));
    }

    public void destroySubcontext(Name name) throws NamingException {
        Log.getLogger().entering(getClass().getName(), "destroySubcontext", name); //NOI18N
        if (name.size() == 1) {
            Object obj = lookup(name);
            if (!(obj instanceof Subcontext)) {
                throw new NotContextException(name.toString());
            }
            Subcontext subCtx = (Subcontext)obj;
            subCtx.subcontext.close();
            map.remove(name.get(0));
        } else {
            Object obj = lookup(name.getPrefix(name.size() - 1));
            if (!(obj instanceof Context)) {
                throw new NameNotFoundException(name.getPrefix(name.size() - 1).toString());
            } else {
                ((Context)obj).destroySubcontext(name.getSuffix(name.size() - 1));
            }
        }

    }

    public void destroySubcontext(String name) throws NamingException {
        destroySubcontext(new CompositeName(name));
    }

    public Context createSubcontext(Name name) throws NamingException {
        Log.getLogger().entering(getClass().getName(), "createSubcontext", name); //NOI18N
        if (name == null || name.size() == 0) {
            throw new NamingException(rb.getString("NAME_IS_EMPTY"));
        }
        if (name.size() == 1) {
            try {
                lookup(name);
                throw new NameAlreadyBoundException(name.toString());
            } catch (NameNotFoundException e) {
                // This is good, we want the name to not be found
            }
            DesignTimeContext subcontext = new DesignTimeContext(this, name.get(0), env);
            map.put(name.get(0), new Subcontext(name.get(0), subcontext));
            return subcontext;
        } else {
            Object subCtx = lookup(name.getPrefix(name.size() - 1));
            if (!(subCtx instanceof Context)) {
                NamingException e = new NamingException(
                MessageFormat.format(rb.getString("NAME_NOT_INSTANCE_OF_CONTEXT"),
                                new Object[] { name.getPrefix(name.size() - 1).toString() }));
                e.setRemainingName(new CompositeName(name.get(name.size() - 1)));
                e.setResolvedObj(subCtx);
                throw e;
            }
            return ((Context)subCtx).createSubcontext(name.get(name.size() - 1));
        }

    }

    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(new CompositeName(name));
    }

    public Object lookupLink(Name name) throws NamingException {
        Log.getLogger().entering(getClass().getName(), "lookupLink", name); //NOI18N
        if (name.size() == 1) {
            Object obj = map.get(name.get(0));
            if (obj == null) {
                NameNotFoundException e = new NameNotFoundException(getFullName() + name);
                e.setRemainingName(name);
                e.setResolvedObj(this);
                throw e;
            } else {
                try {
                    return NamingManager.getObjectInstance(obj, name, this, env);
                } catch (Exception e) {
                    return obj;
                }
            }
        } else {
            Object subCtx = lookup(name.getPrefix(name.size() - 1));
            if (!(subCtx instanceof Context)) {
                throw new NotContextException(name.getPrefix(name.size() - 1).toString());
            }
            return ((Context)subCtx).lookupLink(name.getSuffix(name.size() - 1));
        }
    }

    public Object lookupLink(String name) throws NamingException {
        return lookupLink(new CompositeName(name));
    }

    public NameParser getNameParser(Name name) throws NamingException {
        Log.getLogger().entering(getClass().getName(), "getNameParser", name); //NOI18N
        return nameParser;
    }

    public NameParser getNameParser(String name) throws NamingException {
        return getNameParser(new CompositeName(name));
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        Log.getLogger().entering(getClass().getName(), "composeName", new Object[] {name, prefix}); //NOI18N
        prefix = (Name) prefix.clone();
        return prefix.addAll(name);
    }

    public String composeName(String name, String prefix) throws NamingException {
        return composeName(new CompositeName(name), new CompositeName(prefix)).toString();
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        Log.getLogger().entering(getClass().getName(), "addToEnvironment", new Object[] {propName, propVal}); //NOI18N
        return null;
    }

    private void saveContext() throws NamingException {
        if (parent != null) {
            parent.saveContext();
        } else {
            if (!initMode) {
                try {
                    if (!userCtxFile.exists()) {
                        userCtxFile.createNewFile();
                    }
                    FileOutputStream os = new FileOutputStream(userCtxFile);
                    writeTag(os, 0);
                    os.close();
                } catch (IOException e) {
                    throw new NamingException(getClass().getName() + ": saveContext: " + e); // NOI18N
                }
            }
        }
    }

    private void writeTag(OutputStream os, int level) throws IOException {
        writeSpaces(os, level, TAB_WIDTH);
        if (parent == null) {
            os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes("UTF-8")); // NOI18N
            os.write(("<" + ROOT_CTX_TAG + ">\n").getBytes("UTF-8")); // NOI18N
        } else {
            os.write(("<" + CTX_TAG + " name=\"" + ctxName + "\">\n").getBytes("UTF-8")); // NOI18N
        }

        for (Iterator i = map.keySet().iterator(); i.hasNext();) {
            String key = (String)i.next();
            Object obj = map.get(key);
            if (obj instanceof Subcontext) {
                ((Subcontext)obj).subcontext.writeTag(os, level + 1);
            } else {
                if (obj instanceof ContextPersistance) {
                    os.write(((ContextPersistance)obj).getTag(key, level+1, TAB_WIDTH).getBytes("UTF-8"));
                }
            }
        }

        writeSpaces(os, level, TAB_WIDTH);
        if (parent == null) {
            os.write(("</" + ROOT_CTX_TAG + ">\n").getBytes("UTF-8")); // NOI18N
        } else {
            os.write(("</" + CTX_TAG + ">\n").getBytes("UTF-8")); // NOI18N
        }
    }

    private void writeSpaces(OutputStream os, int level, int tabWidth) throws IOException {
        for (int i = 0; i < level; i++) {
            for (int j = 0; j < tabWidth; j++) {
                os.write(" ".getBytes("UTF-8"));
            }
        }
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        Log.getLogger().entering(getClass().getName(), "removeFromEnvironment", propName); //NOI18N
        return env.remove(propName);
    }

    public Hashtable getEnvironment() throws NamingException {
        Log.getLogger().entering(getClass().getName(), "getEnvironment"); //NOI18N
        return env;
    }

    public void close() throws NamingException {
        Log.getLogger().entering(getClass().getName(), "close"); //NOI18N
        // assume close child contexts too
        for (Iterator i = map.keySet().iterator(); i.hasNext();) {
            String key = (String)i.next();
            Object obj = map.get(key);
            if (obj instanceof Subcontext) {
                ((Subcontext)obj).subcontext.close();
            }
        }
        map.clear();
        env.clear();
    }

    public String getNameInNamespace() throws NamingException {
        Log.getLogger().entering(getClass().getName(), "getNameInNameSpace"); //NOI18N
        throw new OperationNotSupportedException();
    }

    private void parseContextFile() throws ParserConfigurationException, SAXException,
        IOException {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();
            parser.parse(userCtxFile, new DefaultHandler() {
                private Stack ctxStack = new Stack();
                private ArrayList args = new ArrayList();
                private String objectName;
                private String className;
                public void startElement(String uri, String localName, String qName,
                    Attributes attributes) throws SAXException {
                    if (qName.equals(ROOT_CTX_TAG)) {
                        ctxStack.push(DesignTimeContext.this);
                    } else if (qName.equals(CTX_TAG)) {
                        String nameValue = attributes.getValue(NAME_ATTR);
                        if (nameValue == null) {
                            throw new SAXException(
                                MessageFormat.format(rb.getString("MISSING_ATTR"),
                                new Object[] { "name" })); // NOI18N
                        }
                        try {
                            ctxStack.push(
                                ((Context)ctxStack.peek()).createSubcontext(nameValue));
                        } catch (NamingException e) {
                            throw new SAXException(e);
                        }
                    } else if (qName.equals(OBJ_TAG)) {
                        args.clear();
                        objectName = attributes.getValue(NAME_ATTR);
                        className  = attributes.getValue(CLASS_ATTR);
                        if (objectName == null) {
                            throw new SAXException(
                                MessageFormat.format(rb.getString("MISSING_ATTR"),
                                new Object[] { "name" })); // NOI18N
                        }
                    } else if (qName.equals("arg")) { // NOI18N
                        String classValue = attributes.getValue(CLASS_ATTR);
                        if (classValue == null) {
                            throw new SAXException(
                                MessageFormat.format(rb.getString("MISSING_ATTR"),
                                new Object[] { "class" })); // NOI18N
                        }
                        String valueValue = attributes.getValue(VALUE_ATTR);
                        args.add(new ArgPair(classValue, valueValue));
                    } else {
                        throw new SAXNotRecognizedException(qName);
                    }
                }
                public void endElement(String uri, String localName, String qName)
                    throws SAXException {
                    if (qName.equals(ROOT_CTX_TAG)) {
                    } else if (qName.equals(CTX_TAG)) {
                        ctxStack.pop();
                    } else if (qName.equals(OBJ_TAG)) {
                        try {
                            ((DesignTimeContext)ctxStack.peek()).createObject(objectName,
                                className, (ArgPair[])args.toArray(new ArgPair[0]));
                        } catch (Exception e) {
                            throw new SAXException(e);
                        }
                    } else if (qName.equals("arg")) { // NOI18N
                    } else {
                        throw new SAXNotRecognizedException(qName);
                    }
                }
            });
    }

    private void createObject(String objectName, String className, ArgPair[] argPairs)
        throws ClassNotFoundException, NamingException, NoSuchMethodException, SecurityException,
        Exception {

        Class clazz = loadClass(className);

        Class  clazzes[] = new Class[argPairs.length];
        Object args[]    = new Object[argPairs.length];

        for (int i = 0; i < argPairs.length; i++) {
            clazzes[i] = loadClass(argPairs[i].getClazz());
            args[i]    = makeArg(clazzes[i], argPairs[i].getValue());
        }

        Object obj = executeConstructor(clazz, clazzes, args);
        bind(objectName, obj);
    }

    private Class loadClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    private Object makeArg(Class clazz, String value) throws Exception {
        if (value == null) {
            return null;
        } else {
            Constructor ctor = clazz.getConstructor(
                new Class[] {Class.forName("java.lang.String")}); // NOI18N
            return ctor.newInstance(new Object[] {value});
        }
    }

    private void writeNewUserContextFile() throws IOException {
        // Solve the location of the file (should be used the layers, or in the worst case InstalledFileLocator)
        // But we can't do that here, because we don't know about anything netbeans.
        FileInputStream master = new FileInputStream(System.getProperty("netbeans.home") // NOI18N
                   + File.separator + ".." + File.separator + "visualweb1" //NOI18N
                   + File.separator + "startup" + File.separator + "samples" + File.separator // NOI18N
                   + USER_CTX);
        if (!userCtxFile.getParentFile().exists()) {
            userCtxFile.getParentFile().mkdirs();
        }
        FileOutputStream os = new FileOutputStream(userCtxFile);
        byte[] buf= new byte[1024];
        int n = master.read(buf);
        while (n > -1) {
            os.write(buf, 0, n);
            n = master.read(buf);
        }
        master.close();
        os.close();
    }

    private void saveUserContextFile() throws IOException {
        FileInputStream master = new FileInputStream(userCtxFile);
        File saveUserCtxFile = new File(ctxPathName + ".save"); // NOI18N
        FileOutputStream os = new FileOutputStream(saveUserCtxFile);

        byte[] buf= new byte[1024];
        int n = master.read(buf);
        while (n > -1) {
            os.write(buf, 0, n);
            n = master.read(buf);
        }
        master.close();
        os.close();
    }

    private Object executeConstructor(Class clazz, Class[] clazzes, Object[] args)
        throws NoSuchMethodException, SecurityException, InstantiationException,
        IllegalAccessException, InvocationTargetException {
        Constructor ctor = clazz.getConstructor(clazzes);
        return ctor.newInstance(args);
    }

    private class ArgPair {
        private String clazz;
        private String value;
        ArgPair(String clazz, String value) {
            this.clazz = clazz;
            this.value = value;
        }
        String getClazz() {
            return clazz;
        }
        String getValue() {
            return value;
        }
    }
}
