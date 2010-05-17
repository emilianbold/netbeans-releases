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

package org.netbeans.modules.mobility.cldcplatform;
import java.beans.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;

import org.openide.ErrorManager;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.*;
import org.openide.xml.*;

import org.xml.sax.*;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Reads and writes the standard platform format implemented by PlatformImpl2.
 *
 * @author Adam Sotona, Svata Dedic
 */
public class PlatformConvertor implements Environment.Provider, InstanceCookie.Of,
        PropertyChangeListener, Runnable, InstanceContent.Convertor {
    
    private PlatformConvertor() {
        //to avoid instantiation
    }
    
    public static PlatformConvertor createProvider(@SuppressWarnings("unused")
	final FileObject reg) {
        return new PlatformConvertor();
    }
    
    public Lookup getEnvironment(final DataObject obj) {
        return new PlatformConvertor((XMLDataObject)obj).getLookup();
    }
    
    InstanceContent cookies = new InstanceContent();
    
    private XMLDataObject   holder;
    private Lookup  lookup;
    private RequestProcessor.Task    saveTask;
    protected Reference<J2MEPlatform>   refPlatform = new WeakReference<J2MEPlatform>(null);
    final private LinkedList<PropertyChangeEvent> keepAlive = new LinkedList<PropertyChangeEvent>();
    
    @SuppressWarnings("unchecked")
	private PlatformConvertor(XMLDataObject  object) {
        this.holder = object;
        this.holder.getPrimaryFile().addFileChangeListener( new FileChangeAdapter() {
            public void fileDeleted(final FileEvent fe) {
                try {
                    ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction() {
                        public Object run() throws IOException {
                            final String systemName = fe.getFile().getName();
                            final String propPrefix =  "platforms." + systemName + ".";   //NOI18N
                            boolean changed = false;
                            final EditableProperties props = PropertyUtils.getGlobalProperties();
                            for (final Iterator it = props.keySet().iterator(); it.hasNext(); ) {
                                final String key = (String) it.next();
                                if (key.startsWith(propPrefix)) {
                                    it.remove();
                                    changed =true;
                                }
                            }
                            if (changed) {
                                PropertyUtils.putGlobalProperties(props);
                            }
                            return null;
                        }
                    });
                } catch (MutexException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        });
        cookies = new InstanceContent();
        cookies.add(this);
        lookup = new AbstractLookup(cookies);
        cookies.add(Node.class, this);
    }
    
    Lookup getLookup() {
        return lookup;
    }
    
    public Class instanceClass() {
        return J2MEPlatform.class;
    }
    
    public Object instanceCreate() throws java.io.IOException {
        synchronized (this) {
            final Object o = refPlatform.get();
            if (o != null)
                return o;
            final H handler = new H();
            try {
                final XMLReader reader = XMLUtil.createXMLReader();
                final InputSource is = new org.xml.sax.InputSource(
                        holder.getPrimaryFile().getInputStream());
                is.setSystemId(holder.getPrimaryFile().getPath());
                reader.setContentHandler(handler);
                reader.setErrorHandler(handler);
                reader.setEntityResolver(EntityCatalog.getDefault());
                
                reader.parse(is);
            } catch (SAXException ex) {
                final Exception x = ex.getException();
                ex.printStackTrace();
                if (x instanceof java.io.IOException)
                    throw (IOException)x;
                throw new java.io.IOException(ex.getMessage());
            }
            
            final J2MEPlatform inst = createPlatform(handler);
            refPlatform = new WeakReference<J2MEPlatform>(inst);
            
            updateBuildProperties(inst);
            return inst;
        }
    }
    
    J2MEPlatform createPlatform(final H handler) {
        final J2MEPlatform p = handler.platform;
        
        p.addPropertyChangeListener(this);
        return p;
    }
    
    public String instanceName() {
        return holder.getName();
    }
    
    @SuppressWarnings("unchecked")
	public boolean instanceOf(final Class type) {
        return type.isAssignableFrom(J2MEPlatform.class);
    }
    
    final static int DELAY = 200;
    
    public void propertyChange(final PropertyChangeEvent evt) {
        synchronized (this) {
            if (saveTask == null)
                saveTask = RequestProcessor.getDefault().create(this);
        }
        synchronized (this) {
            keepAlive.add(evt);
        }
        saveTask.schedule(DELAY);
    }
    
    public void run() {
        PropertyChangeEvent e;
        
        synchronized (this) {
            e = keepAlive.removeFirst();
        }
        final J2MEPlatform plat = (J2MEPlatform)e.getSource();
        updateBuildProperties(plat);
        try {
            holder.getPrimaryFile().getFileSystem().runAtomicAction(
                    new W(plat, holder));
        } catch (java.io.IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    static interface J2MEPlatformCookie extends Node.Cookie {
        public J2MEPlatform getPlatform();
    }
    
    static class J2MEPlatformNode extends AbstractNode implements PropertyChangeListener {
        
        J2MEPlatform platform;
        
        J2MEPlatformNode(J2MEPlatform platform, DataObject holder) {
            super(Children.LEAF, Lookups.fixed(new Object[] { platform, holder }));
            this.platform = platform;
            platform.addPropertyChangeListener(WeakListeners.propertyChange(this, platform));
            setIconBaseWithExtension("org/netbeans/modules/mobility/cldcplatform/resources/platform.gif"); //NOI18N
        }
        
        public String getDisplayName() {
            return platform.getDisplayName();
        }
        
        public String getHtmlDisplayName() {
            if (platform.isValid()) {
                return null;
            } 
            return "<font color=\"#A40000\">"+this.platform.getDisplayName()+"</font>";
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(J2MEPlatformNode.class);
        }
        
        public boolean hasCustomizer() {
            return true;
        }
        
        public java.awt.Component getCustomizer() {
            if (platform.getInstallFolders().size() == 0) {
                return new BrokenPlatformCustomizer();
            } 
            return new CustomizerPanel(this.platform);
        }
        
        public void propertyChange(@SuppressWarnings("unused")
		final PropertyChangeEvent evt) {
            fireDisplayNameChange(null, getDisplayName());
        }
    }
    
    public Object convert(final Object obj) {
        if (obj == Node.class) {
            Object p;
            
            try {
                p = instanceCreate();
                return new J2MEPlatformNode((J2MEPlatform) p, holder);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        return null;
    }
    
    public String displayName(final Object obj) {
        return ((Class)obj).getName();
    }
    
    public String id(final Object obj) {
        return obj.toString();
    }
    
    public Class type(final Object obj) {
        return (Class)obj;
    }
    
    public static DataObject create(final J2MEPlatform plat, final DataFolder f, final String idName) throws IOException {
        final W w = new W(plat, f, idName);
        f.getPrimaryFile().getFileSystem().runAtomicAction(w);
        return w.holder;
    }
    
    static class W implements FileSystem.AtomicAction {
        J2MEPlatform instance;
        MultiDataObject holder;
        String name;
        DataFolder f;
        
        W(J2MEPlatform instance, MultiDataObject holder) {
            this.instance = instance;
            this.holder = holder;
        }
        
        W(J2MEPlatform instance, DataFolder f, String n) {
            this.instance = instance;
            this.name = n;
            this.f = f;
        }
        
        public void run() throws java.io.IOException {
            FileLock lck;
            FileObject data;
            
            if (holder != null) {
                data = holder.getPrimaryEntry().getFile();
                lck = holder.getPrimaryEntry().takeLock();
            } else {
                final FileObject folder = f.getPrimaryFile();
                final String fn = FileUtil.findFreeFileName(folder, name, "xml"); //NOI18N
                instance.setName(fn);
                data = folder.createData(fn, "xml"); //NOI18N
                lck = data.lock();
            }
            try {
                final OutputStream ostm = data.getOutputStream(lck);
                final PrintWriter writer = new PrintWriter(
                        new OutputStreamWriter(ostm, "UTF8")); //NOI18N
                write(writer);
                writer.flush();
                writer.close();
                ostm.close();
            } finally {
                lck.releaseLock();
            }
            if (holder == null) {
                holder = (MultiDataObject)DataObject.find(data);
            }
        }
        
        void write(final PrintWriter pw) throws IOException {
            pw.println("<?xml version='1.0'?>"); //NOI18N
            pw.println(
                    "<!DOCTYPE platform PUBLIC '-//NetBeans//DTD J2ME PlatformDefinition 1.0//EN' 'http://www.netbeans.org/dtds/j2me-platformdefinition-1_0.dtd'>"); //NOI18N
            pw.print("<platform name=\"" + XMLUtil.toAttributeValue(instance.getName()) //NOI18N
            + "\" home=\"" + XMLUtil.toAttributeValue(instance.getHomePath()) //NOI18N
            + "\" type=\"" + XMLUtil.toAttributeValue(instance.getType()) //NOI18N
            + "\" displayname=\"" + XMLUtil.toAttributeValue(instance.getDisplayName()) //NOI18N
            + "\" srcpath=\"" + XMLUtil.toAttributeValue(instance.getSourcePath()) //NOI18N
            + "\" docpath=\"" + XMLUtil.toAttributeValue(instance.getJavadocPath())); //NOI18N
            if (instance.getPreverifyCmd() != null) pw.print("\" preverifycmd=\"" + XMLUtil.toAttributeValue(instance.getPreverifyCmd())); //NOI18N
            if (instance.getRunCmd() != null) pw.print("\" runcmd=\"" + XMLUtil.toAttributeValue(instance.getRunCmd())); //NOI18N
            if (instance.getDebugCmd() != null) pw.print("\" debugcmd=\"" + XMLUtil.toAttributeValue(instance.getDebugCmd())); //NOI18N
            pw.println("\">"); //NOI18N
            final J2MEPlatform.Device[] devices = instance.getDevices();
            for (int i=0; i<devices.length; i++) {
                final J2MEPlatform.Device d = devices[i];
                pw.print("    <device name=\"" + XMLUtil.toAttributeValue(d.getName())); //NOI18N
                final String securitydomains = PlatformConvertor.array2string(d.getSecurityDomains());
                if (securitydomains != null)
                    pw.print("\" securitydomains=\"" + XMLUtil.toAttributeValue(securitydomains)); //NOI18N
                if (d.getDescription() != null) pw.print("\" description=\"" + XMLUtil.toAttributeValue(d.getDescription())); //NOI18N
                pw.println("\">"); //NOI18N
                final J2MEPlatform.J2MEProfile[] profiles = d.getProfiles();
                for (int j=0; j<profiles.length; j++) {
                    final J2MEPlatform.J2MEProfile p = profiles[j];
                    pw.print("        <" + p.getType() //NOI18N
                    + " name=\"" + XMLUtil.toAttributeValue(p.getName()) //NOI18N
                    + "\" version=\"" + XMLUtil.toAttributeValue(p.getVersion().toString()) //NOI18N
                    + "\" displayname=\"" + XMLUtil.toAttributeValue(p.getDisplayName()) //NOI18N
                    + "\" classpath=\"" + XMLUtil.toAttributeValue(p.getClassPath())); //NOI18N
                    if (p.getDependencies() != null) pw.print("\" dependencies=\"" + XMLUtil.toAttributeValue(p.getDependencies())); //NOI18N
                    pw.println("\" default=\"" + Boolean.toString(p.isDefault()) + "\"/>"); //NOI18N
                }
                final J2MEPlatform.Screen s = d.getScreen();
                if (s != null) {
                    final Integer sw = s.getWidth();
                    final Integer sh = s.getHeight();
                    final Integer sd = s.getBitDepth();
                    final Boolean sc = s.getColor();
                    final Boolean st = s.getTouch();
                    if (sw != null  ||  sh != null  ||  sd != null  ||  sc != null  ||  st != null) {
                        pw.print("        <screen"); //NOI18N
                        if (sw != null)
                            pw.print(" width=\"" + sw + "\""); //NOI18N
                        if (sh != null)
                            pw.print(" height=\"" + sh + "\""); //NOI18N
                        if (sd != null)
                            pw.print(" bitDepth=\"" + sd + "\""); //NOI18N
                        if (sc != null)
                            pw.print(" isColor=\"" + sc + "\""); //NOI18N
                        if (st != null)
                            pw.print(" isTouch=\"" + st + "\""); //NOI18N
                        pw.println("/>"); //NOI18N
                    }
                }
                pw.println("    </device>"); //NOI18N
            }
            pw.println("</platform>"); //NOI18N
        }
        
        void writeProperties(final Map<String,String> props, final PrintWriter pw) throws IOException {
            final Collection<String> sortedProps = new TreeSet<String>(props.keySet());
            for ( final String n : sortedProps ) { //= (String)it.next();
                final String val = props.get(n);
                pw.println("    <property name='" + //NOI18N
                        XMLUtil.toAttributeValue(n) + "' value='" + //NOI18N
                        XMLUtil.toAttributeValue(val) + "'/>"); //NOI18N
            }
        }
    }
    
    static final String ELEMENT_PLATFORM = "platform"; // NOI18N
    static final String ELEMENT_DEVICE = "device"; // NOI18N
    static final String ELEMENT_CONFIGURATION = "configuration"; // NOI18N
    static final String ELEMENT_PROFILE = "profile"; // NOI18N
    static final String ELEMENT_OPTIONAL = "optional"; // NOI18N
    static final String ELEMENT_SCREEN = "screen"; // NOI18N
    static final String ATTR_PROPERTY_NAME = "name"; // NOI18N
    static final String ATTR_PROPERTY_HOME = "home"; // NOI18N
    static final String ATTR_PROPERTY_TYPE = "type"; // NOI18N
    static final String ATTR_PROPERTY_DISPLAYNAME = "displayname"; // NOI18N
    static final String ATTR_PROPERTY_SRCPATH = "srcpath"; // NOI18N
    static final String ATTR_PROPERTY_DOCPATH = "docpath"; // NOI18N
    static final String ATTR_PROPERTY_DESCRIPTION = "description"; // NOI18N
    static final String ATTR_PROPERTY_SECURITY_DOMAINS = "securitydomains"; // NOI18N
    static final String ATTR_PROPERTY_PREVERIFYCMD = "preverifycmd"; // NOI18N
    static final String ATTR_PROPERTY_RUNCMD = "runcmd"; // NOI18N
    static final String ATTR_PROPERTY_DEBUGCMD = "debugcmd"; // NOI18N
    static final String ATTR_PROPERTY_VERSION = "version"; // NOI18N
    static final String ATTR_PROPERTY_DEPENDENCIES = "dependencies"; // NOI18N
    static final String ATTR_PROPERTY_CLASSPATH = "classpath"; // NOI18N
    static final String ATTR_PROPERTY_DEFAULT = "default"; // NOI18N
    static final String ATTR_SCREEN_WIDTH = "width"; // NOI18N
    static final String ATTR_SCREEN_HEIGHT = "height"; // NOI18N
    static final String ATTR_SCREEN_BITDEPTH = "bitDepth"; // NOI18N
    static final String ATTR_SCREEN_ISCOLOR = "isColor"; // NOI18N
    static final String ATTR_SCREEN_ISTOUCH = "isTouch"; // NOI18N
    
    static class H extends org.xml.sax.helpers.DefaultHandler {
        
        J2MEPlatform platform;
        
        private String name;
        private String home;
        private String type;
        private String displayName;
        private String srcPath;
        private String docPath;
        
        private ArrayList<J2MEPlatform.Device> devices;
        private String devName;
        private String devDesc;
        private String[] devSecurityDomains;
        private String devPrevCmd;
        private String devRunCmd;
        private String devDebugCmd;
        private ArrayList<J2MEPlatform.J2MEProfile> profiles;
        
        private J2MEPlatform.Screen screen;
        
        public J2MEPlatform.Device[]  getDevices() {
            return devices.toArray(new J2MEPlatform.Device[devices.size()]);
        }
        
        @SuppressWarnings("unused")
		public void startDocument() throws org.xml.sax.SAXException {
        }
        
        @SuppressWarnings("unused")
		public void endDocument() throws org.xml.sax.SAXException {
        }
        
        private String getMandatoryValue(final org.xml.sax.Attributes attrs, final String name) throws SAXException {
            final String val = attrs.getValue(name);
            if (val == null || val.length()<1) throw new SAXException("Missing " + name); //NOI18N
            return val;
        }
        
        public void startElement(@SuppressWarnings("unused")
		final String uri, @SuppressWarnings("unused")
		final String localName, final String qName, final org.xml.sax.Attributes attrs) throws SAXException {
            if (ELEMENT_PLATFORM.equals(qName)) {
                if (platform != null || devices != null) throw new SAXException("Invalid start of element " + ELEMENT_PLATFORM); //NOI18N
                name = getMandatoryValue(attrs, ATTR_PROPERTY_NAME);
                home = getMandatoryValue(attrs, ATTR_PROPERTY_HOME);
                type = getMandatoryValue(attrs, ATTR_PROPERTY_TYPE);
                displayName = attrs.getValue(ATTR_PROPERTY_DISPLAYNAME);
                srcPath = attrs.getValue(ATTR_PROPERTY_SRCPATH);
                docPath = attrs.getValue(ATTR_PROPERTY_DOCPATH);
                devPrevCmd = attrs.getValue(ATTR_PROPERTY_PREVERIFYCMD);
                devRunCmd = attrs.getValue(ATTR_PROPERTY_RUNCMD);
                devDebugCmd = attrs.getValue(ATTR_PROPERTY_DEBUGCMD);
            } else if (ELEMENT_DEVICE.equals(qName)) {
                if (profiles != null) throw new SAXException("Invalid start of element " + ELEMENT_DEVICE); //NOI18N
                devName = getMandatoryValue(attrs, ATTR_PROPERTY_NAME);
                devDesc = attrs.getValue(ATTR_PROPERTY_DESCRIPTION);
                devSecurityDomains = PlatformConvertor.string2array(attrs.getValue(ATTR_PROPERTY_SECURITY_DOMAINS));
                profiles = new ArrayList<J2MEPlatform.J2MEProfile>();
                screen = null;
            } else if (ELEMENT_CONFIGURATION.equals(qName) || ELEMENT_PROFILE.equals(qName) || ELEMENT_OPTIONAL.equals(qName)) {
                final String pname = getMandatoryValue(attrs, ATTR_PROPERTY_NAME);
                final String pversion = getMandatoryValue(attrs, ATTR_PROPERTY_VERSION);
                final String pdependencies = attrs.getValue(ATTR_PROPERTY_DEPENDENCIES);
                final String pdisplayname = attrs.getValue(ATTR_PROPERTY_DISPLAYNAME);
                final String pclasspath = attrs.getValue(ATTR_PROPERTY_CLASSPATH);
                final boolean pdefault = Boolean.valueOf(attrs.getValue(ATTR_PROPERTY_DEFAULT)).booleanValue();
                profiles.add(new J2MEPlatform.J2MEProfile(pname, pversion, pdisplayname, qName, pdependencies, pclasspath, pdefault));
            } else if (ELEMENT_SCREEN.equals(qName)) {
                screen = new J2MEPlatform.Screen(attrs.getValue(ATTR_SCREEN_WIDTH), attrs.getValue(ATTR_SCREEN_HEIGHT), attrs.getValue(ATTR_SCREEN_BITDEPTH), attrs.getValue(ATTR_SCREEN_ISCOLOR), attrs.getValue(ATTR_SCREEN_ISTOUCH));
            }
        }
        
        public void endElement(@SuppressWarnings("unused")
		final String uri, @SuppressWarnings("unused")
		final String localName, final String qName) throws org.xml.sax.SAXException {
            if (ELEMENT_PLATFORM.equals(qName)) {
                if (platform != null) throw new SAXException("Invalid end of element " + ELEMENT_PLATFORM); //NOI18N
                if (devices == null) throw new SAXException("Missing " + ELEMENT_DEVICE); //NOI18N
                platform = new J2MEPlatform(name, home, type, displayName, srcPath, docPath, devPrevCmd, devRunCmd, devDebugCmd, devices.toArray(new J2MEPlatform.Device[devices.size()]));
            } else if (ELEMENT_DEVICE.equals(qName)) {
                if (devices == null)
                    devices = new ArrayList<J2MEPlatform.Device>();
                devices.add(new J2MEPlatform.Device(devName, devDesc, devSecurityDomains, profiles.toArray(new J2MEPlatform.J2MEProfile[profiles.size()]), screen));
                profiles = null;
            }
        }
    }
    
    private static void updateBuildProperties(final J2MEPlatform p) {
        final String name = p.getName();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    ProjectManager.mutex().writeAccess(
                            new Mutex.ExceptionAction() {
                        public Object run() throws Exception{
                            final EditableProperties props = PropertyUtils.getGlobalProperties();
                            final Iterator it = props.entrySet().iterator();
                            final String prefix = createName(name, "");//NOI18N
                            while (it.hasNext()) {
                                if (((String)((Map.Entry)it.next()).getKey()).startsWith(prefix)) it.remove();
                            }
                            props.setProperty(createName(name,"home"), p.getHomePath()); //NOI18N
                            props.setProperty(createName(name,"type"), p.getType());   //NOI18N
                            props.setProperty(createName(name, "preverifycommandline"), p.getPreverifyCmd() != null ? p.getPreverifyCmd() : ""); // NOI18N
                            props.setProperty(createName(name, "runcommandline"), p.getRunCmd() != null ? p.getRunCmd() : ""); // NOI18N
                            props.setProperty(createName(name, "debugcommandline"), p.getDebugCmd() != null ? p.getDebugCmd() : ""); // NOI18N
                            PropertyUtils.putGlobalProperties(props);
                            return null;
                        }
                    }
                    );
                } catch (MutexException me) {
                    ErrorManager.getDefault().notify(me.getException());
                }
            }
        });
    }
    
    protected static String createName(final String propName, final String propType) {
        return "platforms." + propName + "." + propType;        //NOI18N
    }
    
    public static String array2string(final String[] array) {
        if (array == null)
            return null;
        StringBuffer sb = null;
        for (int a = 0; a < array.length; a ++) {
            if (sb == null)
                sb = new StringBuffer(array[a].length() * array.length);
            else
                sb.append(',');
            sb.append(array[a]);
        }
        return (sb != null) ? sb.toString() : ""; //NOI18N
    }
    
    public static String[] string2array(final String string) {
        if (string == null)
            return null;
        final ArrayList<String> list = new ArrayList<String>();
        final StringTokenizer st = new StringTokenizer(string, ","); //NOI18N
        while (st.hasMoreTokens())
            list.add(st.nextToken().trim());
        return list.toArray(new String[list.size()]);
    }
    
    protected static String toJavaIdentifier(final String s) {
        final StringBuffer sb = new StringBuffer();
        for (int i=0; i<s.length(); i++) {
            final char ch = s.charAt(i);
            if (sb.length() == 0 && Character.isJavaIdentifierStart(ch)) sb.append(ch);
            else if (Character.isJavaIdentifierPart(ch)) sb.append(ch);
        }
        return sb.toString();
    }
    
    private static final String EMPTY = ""; //NOI18N
    
    public static Map<String, String> extractPlatformProperties(final String prefix, final J2MEPlatform platform, J2MEPlatform.Device device, final String reqConfiguration, final String reqProfile) {
        final HashMap<String, String> props = new HashMap();
        String pname, pdesc, dname, type;
        J2MEPlatform.J2MEProfile configuration = null, profile = null;
        final StringBuffer apis = new StringBuffer(), classpath = new StringBuffer();
        final HashMap<String,String> abilities = new HashMap<String,String>();
        if (platform == null) {
            pname = ".default"; //NOI18N
            pdesc = EMPTY;
            dname = EMPTY;
            type = EMPTY;
        } else {
            pname = platform.getName();
            pdesc = platform.getDisplayName();
            type = platform.getType();
            if (device == null && platform.getDevices() != null && platform.getDevices().length > 0) device = platform.getDevices()[0];
            if (device == null) {
                dname = EMPTY;
            } else {
                dname = device.getName();
                final J2MEPlatform.J2MEProfile profs[] = device.getProfiles();
                for (int i=0; i<profs.length; i++) {
                    if (J2MEPlatform.J2MEProfile.TYPE_CONFIGURATION.equals(profs[i].getType())) {
                        if (configuration == null //this code select by priority: 1.requested 2.default 3.any configuration
                                || (!configuration.toString().equals(reqConfiguration) && profs[i].isDefault())
                                || (profs[i].toString().equals(reqConfiguration))) configuration = profs[i];
                    } else if (J2MEPlatform.J2MEProfile.TYPE_PROFILE.equals(profs[i].getType())) {
                        if (profile == null //this code select by priority: 1.requested 2.default 3.any profile
                                || (!profile.toString().equals(reqProfile) && profs[i].isDefault())
                                || (profs[i].toString().equals(reqProfile))) profile = profs[i];
                    } else if (profs[i].isDefault()) {
                        if (apis.length() > 0) apis.append(',');
                        apis.append(profs[i].toString());

                        if (classpath.length() > 0) classpath.append(':');
                        classpath.append(profs[i].getClassPath());
                    }
                    final String version = profs[i].getVersion() == null ? null : profs[i].getVersion().toString();
                    final String ability = toValidAbility(profs[i].getName());
                    if (ability != null) {
                        final String val = abilities.get(ability);
                        if (val == null || (version != null && version.compareTo(val) > 0))
                            abilities.put(ability, version);
                    }
                }
                if (configuration != null) {
                    if (classpath.length() > 0) classpath.append(':');
                    classpath.append(configuration.getClassPath());
                }
                if (profile != null) {
                    if (classpath.length() > 0) classpath.append(':');
                    classpath.append(profile.getClassPath());
                }
                final J2MEPlatform.Screen scr = device.getScreen();
                if (scr != null) {
                    if (scr.getColor() != null && scr.getColor().booleanValue()) abilities.put("ColorScreen", null); //NOI18N
                    if (scr.getTouch() != null && scr.getTouch().booleanValue()) abilities.put("TouchScreen", null); //NOI18N
                    if (scr.getBitDepth() != null)  abilities.put("ScreenColorDepth", scr.getBitDepth().toString()); //NOI18N
                    if (scr.getWidth() != null) abilities.put("ScreenWidth", scr.getWidth().toString()); //NOI18N
                    if (scr.getHeight() != null) abilities.put("ScreenHeight", scr.getHeight().toString()); //NOI18N
                }
            }
        }
        props.put(prefix+"platform.active", pname); //NOI18N
        props.put(prefix+"platform.active.description", pdesc); //NOI18N
        props.put(prefix+"platform.device", dname); //NOI18N
        props.put(prefix+"platform.configuration", configuration == null ? "CLDC-1.0" : configuration.toString()); //NOI18N
        props.put(prefix+"platform.profile", profile == null ? "MIDP-1.0" : profile.toString()); //NOI18N
        props.put(prefix+"platform.apis", apis.toString()); //NOI18N
        props.put(prefix+"platform.bootclasspath", device == null ? classpath.toString() : device.sortClasspath(classpath.toString())); //NOI18N
        props.put(prefix+"abilities", encodeAbilities(abilities)); //NOI18N
        props.put(prefix+"platform.type", type); //NOI18N
        props.put(prefix+"platform.trigger", "CLDC"); //NOI18N
        return props;
    }
    
    private static String toValidAbility(final String s) {
        final StringBuffer sb = new StringBuffer();
        for (int i=0; i<s.length(); i++) {
            final char c = s.charAt(i);
            if (Character.isJavaIdentifierPart(c) || c == '.' || c == '/' || c == '\\') sb.append(c);
        }
        if (sb.length() == 0) return null;
        if (!Character.isJavaIdentifierStart(sb.charAt(0))) sb.insert(0, '_');
        return sb.toString();
    }
    
    private static String encodeAbilities(final Map<String,String> value) {
        final StringBuffer sb = new StringBuffer();
        for ( final Map.Entry<String,String> e : value.entrySet() ) {
            sb.append(e.getKey());
            if (e.getValue() != null && (e.getValue()).length() > 0) sb.append('=').append(e.getValue());
            sb.append(',');
        }
        return sb.toString();
    }
    
}
