/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.j2me.cdc.platform.platformdefinition;

import java.beans.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.List;
import java.net.URL;
import java.net.MalformedURLException;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2me.cdc.platform.CDCDevice;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;

import org.openide.ErrorManager;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.lookup.*;
import org.openide.xml.*;

import org.xml.sax.*;

import org.netbeans.api.java.platform.*;
import org.netbeans.api.java.classpath.ClassPath;

/**
 * Reads and writes the standard platform format implemented by PlatformImpl2.
 *
 * @author Svata Dedic
 */
public class PlatformConvertor implements Environment.Provider, InstanceCookie.Of, PropertyChangeListener, Runnable, InstanceContent.Convertor {

    private static final String PLATFORM_DTD_ID = "-//NetBeans//DTD CDC PlatformDefinition 1.0//EN"; // NOI18N
    private static final String PLATFORM_DTD_ID_1_1 = "-//NetBeans//DTD CDC PlatformDefinition 1.1//EN"; // NOI18N

    private PlatformConvertor() {}

    public static PlatformConvertor createProvider(FileObject reg) {
        return new PlatformConvertor();
    }
    
    public Lookup getEnvironment(DataObject obj) {
        return new PlatformConvertor((XMLDataObject)obj).getLookup();
    }
    
    InstanceContent cookies = new InstanceContent();
    
    private XMLDataObject   holder;

    private Lookup  lookup;
    
    private RequestProcessor.Task    saveTask;
    
    private Reference<CDCPlatform>   refPlatform = new WeakReference<CDCPlatform>(null);
    
    private LinkedList<PropertyChangeEvent> keepAlive = new LinkedList<PropertyChangeEvent>();
    
    private PlatformConvertor(XMLDataObject  object) {
        this.holder = object;
        this.holder.getPrimaryFile().addFileChangeListener( new FileChangeAdapter () {
            public void fileDeleted (final FileEvent fe) {
                try {
                    ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction () {
                        public Object run () throws IOException {
                            String systemName = fe.getFile().getName();
                            String propPrefix =  "platforms." + systemName + ".";   //NOI18N
                            boolean changed = false;
                            EditableProperties props = PropertyUtils.getGlobalProperties();
                            for (Iterator it = props.keySet().iterator(); it.hasNext(); ) {
                                String key = (String) it.next ();
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
        return JavaPlatform.class;
    }
    
    public Object instanceCreate() throws java.io.IOException {
        synchronized (this) {
            Object o = refPlatform.get();
            if (o != null)
                return o;
            H handler = new H();
            try {
                XMLReader reader = XMLUtil.createXMLReader();
                InputSource is = new org.xml.sax.InputSource(
                    holder.getPrimaryFile().getInputStream());
                is.setSystemId(holder.getPrimaryFile().getURL().toExternalForm());
                reader.setContentHandler(handler);
                reader.setErrorHandler(handler);
                reader.setEntityResolver(handler);

                reader.parse(is);
            } catch (SAXException ex) {
                Exception x = ex.getException();
                ex.printStackTrace();
                if (x instanceof java.io.IOException)
                    throw (IOException)x;
                IOException ioe = new IOException(ex.getMessage());
                ioe.initCause(x);
                throw ioe;
            }

            CDCPlatform inst = createPlatform(handler);
            //Write back old config in the new form
            if (handler.dtdVersion == 0)
            {
                PropertyChangeEvent evt=new PropertyChangeEvent(inst,null,null,null);
                this.propertyChange(evt);
            }
            refPlatform = new WeakReference<CDCPlatform>(inst);
            updateBuildProperties(inst);
            return inst;
        }
    }
    
    CDCPlatform createPlatform(H handler) {
        CDCPlatform p = new CDCPlatform(handler.name, handler.antname, handler.type, handler.classVersion, handler.installFolders, 
                                        handler.sources, handler.javadoc, handler.devices.toArray(new CDCDevice[handler.devices.size()]),
                                        handler.fatJar);
        p.addPropertyChangeListener(this);
        return p;
    }
    
    public String instanceName() {
        return holder.getName();
    }
    
    public boolean instanceOf(Class type) {
        return (type.isAssignableFrom(CDCPlatform.class));
    }
    
    static int DELAY = 2000;
    
    public void propertyChange(PropertyChangeEvent evt) {
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
        CDCPlatform plat = (CDCPlatform)e.getSource();
        try {
            holder.getPrimaryFile().getFileSystem().runAtomicAction(
                new W(plat, holder));
        } catch (java.io.IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    public Object convert(Object obj) {
        if (obj == Node.class) {
            try {
                CDCPlatform p = (CDCPlatform) instanceCreate();
                return new CDCPlatformNode (p,this.holder);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        return null;
    }
    
    public String displayName(Object obj) {
        return ((Class)obj).getName();
    }
    
    public String id(Object obj) {
        return obj.toString();
    }
    
    public Class type(Object obj) {
        return (Class)obj;
    }
    
    public static DataObject create(final CDCPlatform plat, final DataFolder f, final String idName) throws IOException {
        W w = new W(plat, f, idName);
        f.getPrimaryFile().getFileSystem().runAtomicAction(w);
        return w.holder;
    }
    
    static class W implements FileSystem.AtomicAction {
        CDCPlatform instance;
        MultiDataObject holder;
        String name;
        DataFolder f;

        W(CDCPlatform instance, MultiDataObject holder) {
            this.instance = instance;
            this.holder = holder;
        }
        
        W(CDCPlatform instance, DataFolder f, String n) {
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
                FileObject folder = f.getPrimaryFile();
                String fn = FileUtil.findFreeFileName(folder, name, "xml");
                data = folder.createData(fn, "xml");
                lck = data.lock();
            }
            try {
                OutputStream ostm = data.getOutputStream(lck);
                PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(ostm, "UTF8"));
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
        
        void write(PrintWriter pw) throws IOException {
            pw.println("<?xml version='1.0'?>");
            pw.println(
            "<!DOCTYPE platform PUBLIC '"+PLATFORM_DTD_ID_1_1+"' 'http://www.netbeans.org/dtds/cdc-platformdefinition-1_1.dtd'>");

            pw.print("<platform name=\"" + XMLUtil.toAttributeValue(instance.getDisplayName()) //NOI18N
            + "\" antname=\"" + XMLUtil.toAttributeValue(instance.getAntName()) //NOI18N
            + "\" type=\"" + XMLUtil.toAttributeValue(instance.getType()) //NOI18N
            + "\" classversion=\"" + XMLUtil.toAttributeValue(instance.getClassVersion()) //NOI18N
            + "\" displayname=\"" + XMLUtil.toAttributeValue(instance.getDisplayName())
            + "\" fatjar=\"" + instance.isFatJar()); //NOI18N
            pw.println("\">"); //NOI18N

            pw.println("  <platformhome>");
            for (Iterator it = instance.getInstallFolders().iterator(); it.hasNext();) {
                URL url = ((FileObject)it.next ()).getURL();
                pw.println("    <resource>"+url.toExternalForm()+"</resource>");
            }
            pw.println("  </platformhome>");
            List pl = this.instance.getSourceFolders().entries();
            if (pl.size()>0) {
                pw.println ("  <sources>");
                for (Iterator it = pl.iterator(); it.hasNext();) {
                    URL url = ((ClassPath.Entry)it.next ()).getURL();
                    pw.println("    <resource>"+url.toExternalForm()+"</resource>");
                }
                pw.println ("  </sources>");
            }
            pl = this.instance.getJavadocFolders();
            if (pl.size()>0) {
                pw.println("  <javadoc>");
                for (Iterator it = pl.iterator(); it.hasNext();) {
                    URL url = (URL) it.next ();
                    pw.println("<resource>"+url.toExternalForm()+"</resource>");
                }
                pw.println("  </javadoc>");
            }
            
            CDCDevice[] devices = instance.getDevices();
            for (int i=0; i<devices.length; i++) {
                CDCDevice d = devices[i];
                pw.print("  <device name=\"" + XMLUtil.toAttributeValue(d.getName())); //NOI18N
//                String securitydomains = PlatformConvertor.array2string(d.getSecurityDomains());
//                if (securitydomains != null)
//                    pw.print("\" securitydomains=\"" + XMLUtil.toAttributeValue(securitydomains)); //NOI18N
                if (d.getDescription() != null) pw.print("\" description=\"" + XMLUtil.toAttributeValue(d.getDescription())); //NOI18N
                pw.println("\">"); //NOI18N
                CDCDevice.CDCProfile[] profiles = d.getProfiles();
                for (int j=0; j<profiles.length; j++) {
                    CDCDevice.CDCProfile p = profiles[j];
                    pw.print("    <profile"  //NOI18N
                    + " name=\"" + XMLUtil.toAttributeValue(p.getName()) //NOI18N
                    + "\" description=\"" + XMLUtil.toAttributeValue(p.getDescription()) //NOI18N
                    + "\" version=\"" + XMLUtil.toAttributeValue(p.getVersion().toString()) //NOI18N
                    + "\" classpath=\"" + XMLUtil.toAttributeValue(p.getBootClassPath())); //NOI18N
                    if (p.getRunClassPath() != null ) {
                        pw.print("\" runclasspath=\"" + XMLUtil.toAttributeValue(p.getRunClassPath())); //NOI18N
                    }
                    pw.print("\" isDefault=\"" + p.isDefault()); //NOI18N
                    pw.println("\">"); //NOI18N
                    Map exec = p.getExecutionModes();
                    if (exec != null){
                        for (Iterator it = exec.entrySet().iterator(); it.hasNext();){
                            Map.Entry entry = (Map.Entry)it.next();
                            pw.print("      <execution type=\"" + entry.getKey() + "\" ");
                            if (entry.getValue() != null)
                                pw.print("class=\"" + entry.getValue() + "\"");
                            pw.println("/>");
                        }                    
                    }
                    pw.println("    </profile>"); //NOI18N
                }
                CDCDevice.Screen[] s = d.getScreens();
                for (int j = 0; s != null && j < s.length; j++) {
                    if (s[j] != null) {
                        final Integer sw = s[j].getWidth();
                        final Integer sh = s[j].getHeight();
                        final Integer sd = s[j].getBitDepth();
                        final Boolean sc = s[j].getColor();
                        final Boolean st = s[j].getTouch();
                        final Boolean mn = s[j].istMain();
                        if (sw != null  ||  sh != null  ||  sd != null  ||  sc != null  ||  st != null) {
                            pw.print("    <screen"); //NOI18N
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
                            if (mn != null)
                                pw.print(" isMain=\"" + mn + "\""); //NOI18N
                            pw.println("/>"); //NOI18N
                        }
                    }
                }                
                pw.println("  </device>"); //NOI18N
            }
            pw.println("</platform>"); //NOI18N
        }
        
        void writeProperties(Map<String,String> props, PrintWriter pw) throws IOException {
            Collection<String> sortedProps = new TreeSet<String>(props.keySet());
            for (String n : sortedProps) {
                String val = props.get(n);
                String xmlName = XMLUtil.toAttributeValue(n);
                try {
                    String xmlValue = XMLUtil.toAttributeValue(val);
                    pw.println("    <property name='" + xmlName + "' value='" + xmlValue + "'/>"); //NOI18N
                } catch (CharConversionException ce) {
                    //Ignore the invalid property
                    ErrorManager.getDefault().log("PlatformConvertor: invalid property name="+n+" value="+val);
                }                
            }
        }
    }
    
    static final String ELEMENT_PROPERTIES = "properties"; // NOI18N
    static final String ELEMENT_SYSPROPERTIES = "sysproperties"; // NOI18N
    static final String ELEMENT_PROPERTY = "property"; // NOI18N
    static final String ELEMENT_PLATFORM = "platform"; // NOI18N
    static final String ELEMENT_JDKHOME = "jdkhome";    //NOI18N
    static final String ELEMENT_SOURCEPATH = "sources";  //NOI18N
    static final String ELEMENT_JAVADOC = "javadoc";    //NOI18N
    static final String ELEMENT_RESOURCE = "resource";  //NOI18N
    static final String ATTR_PLATFORM_NAME = "name"; // NOI18N
    static final String ATTR_PLATFORM_ANT_NAME = "antname"; // NOI18N
    static final String ATTR_PLATFORM_TYPE = "type";    //NOI18N
    static final String ATTR_PLATFORM_DEFAULT = "default"; // NOI18N
    static final String ATTR_PLATFORM_FATJAR = "fatjar"; // NOI18N
    static final String ATTR_PROPERTY_NAME = "name"; // NOI18N
    static final String ATTR_PROPERTY_VALUE = "value"; // NOI18N
    
    static final String ELEMENT_PLATFORMHOME = "platformhome";
    static final String ELEMENT_DEVICE       = "device";
    static final String ELEMENT_PROFILE      = "profile";
    static final String ELEMENT_EXECUTION    = "execution";
    static final String ELEMENT_SCREEN       = "screen";
    static final String ATTR_PLATFORM_CLASSVERSION    = "classversion"; // NOI18N
    static final String ATTR_PLATFORM_DISPLAYNAME     = "displayname"; // NOI18N
    static final String ATTR_DEVICE_NAME              = "name";
    static final String ATTR_DEVICE_DESCRIPTION       = "description";
    static final String ATTR_PROFILE_NAME             = "name";
    static final String ATTR_PROFILE_DESCRIPTION      = "description";
    static final String ATTR_PROFILE_VERSION          = "version";
    static final String ATTR_PROFILE_CLASSPATH        = "classpath";
    static final String ATTR_PROFILE_RUNCLASSPATH     = "runclasspath";
    static final String ATTR_PROFILE_DEFAULT          = "isDefault";
    static final String ATTR_EXECUTION_TYPE           = "type";
    static final String ATTR_EXECUTION_CLASS          = "class";
    static final String ATTR_SCREEN_WIDTH             = "width"; // NOI18N
    static final String ATTR_SCREEN_HEIGHT            = "height"; // NOI18N
    static final String ATTR_SCREEN_BITDEPTH          = "bitDepth"; // NOI18N
    static final String ATTR_SCREEN_ISCOLOR           = "isColor"; // NOI18N
    static final String ATTR_SCREEN_ISTOUCH           = "isTouch"; // NOI18N
    static final String ATTR_SCREEN_ISMAIN            = "isMain"; // NOI18N
            
    static class H extends org.xml.sax.helpers.DefaultHandler implements EntityResolver {
        String  name;
        String  antname;
        String  type;
        String  classVersion;
        String  displayName;
        
        List<URL>    sources;
        List<URL>    javadoc;
        List<URL>    installFolders;
        List<CDCDevice>    devices;

        Map<String,String>     properties;
        Map<String,String>     sysProperties;
        
        private Map<String,String> propertyMap;
        private StringBuffer buffer;
        private List<URL> path;

        private String deviceName;
        private String deviceDescription;
        private List<CDCDevice.CDCProfile> profiles;        
        private List<CDCDevice.Screen> screens;
        private Map<String,String> executionModes;
        private CDCDevice.CDCProfile profile;

        protected boolean fatJar;
        
        protected int dtdVersion;

        public void startDocument () {
        }
        
        public void endDocument () {            
        }
        
        public void startElement (String uri, String localName, String qName, org.xml.sax.Attributes attrs)
        throws org.xml.sax.SAXException {
            if (dtdVersion>0)
            {
                if (ELEMENT_PLATFORM.equals(qName)) {
                    name         = getMandatoryValue(attrs, ATTR_PLATFORM_NAME);
                    antname      = getMandatoryValue(attrs, ATTR_PLATFORM_ANT_NAME);
                    classVersion = getMandatoryValue(attrs, ATTR_PLATFORM_CLASSVERSION);
                    displayName  = getMandatoryValue(attrs, ATTR_PLATFORM_DISPLAYNAME);
                    type         = getMandatoryValue(attrs, ATTR_PLATFORM_TYPE);
                    fatJar       = Boolean.parseBoolean(getMandatoryValue(attrs, ATTR_PLATFORM_FATJAR));                    
                } 
            }
            else
            {
                if (ELEMENT_PLATFORM.equals(qName)) {
                    name = getMandatoryValue(attrs, ATTR_PLATFORM_NAME);
                    antname = name.replace(' ','_');                    
                    displayName=new String(name);
                    String origType = getMandatoryValue(attrs, ATTR_PLATFORM_TYPE);
                    if (origType.equals("0"))
                    {
                        type="semc";
                        classVersion = "1.2";
                    }
                    else if (origType.equals("1")) 
                    {
                        type="nokiaS80";
                        classVersion = "1.4";
                    }
                    else 
                        throw new org.xml.sax.SAXException("Unrecognised platform type: "+origType);
                        
                    fatJar=true;
                }
            } 
            if (ELEMENT_PROPERTIES.equals(qName)) {
                if (properties == null)
                    properties = new HashMap<String,String>(17);
                propertyMap = properties;
            } else if (ELEMENT_PLATFORMHOME.equals(qName) || ELEMENT_JDKHOME.equals(qName)) {
                this.installFolders = new ArrayList<URL> ();
                this.path =  this.installFolders;
            }
            else if (ELEMENT_SYSPROPERTIES.equals(qName)) {
                if (sysProperties == null)
                    sysProperties = new HashMap<String,String>(17);
                propertyMap = sysProperties;
            } else if (ELEMENT_PROPERTY.equals(qName)) {
                if (propertyMap == null)
                    throw new SAXException("property w/o properties or sysproperties");
                String name = attrs.getValue(ATTR_PROPERTY_NAME);   
                if (name == null || "".equals(name))
                    throw new SAXException("missing name");
                String val = attrs.getValue(ATTR_PROPERTY_VALUE);
                propertyMap.put(name, val);
            }
            else if (ELEMENT_SOURCEPATH.equals(qName)) {
                this.sources = new ArrayList<URL> ();
                this.path = this.sources;
            }
            else if (ELEMENT_JAVADOC.equals(qName)) {
                this.javadoc = new ArrayList<URL> ();
                this.path = this.javadoc;
            }
            else if (ELEMENT_RESOURCE.equals(qName)) {
                this.buffer = new StringBuffer ();
            }
            else if (ELEMENT_DEVICE.equals(qName)) {
                deviceName = getMandatoryValue(attrs, ATTR_DEVICE_NAME);
                deviceDescription = attrs.getValue(ATTR_DEVICE_DESCRIPTION);       
            }
            else if (ELEMENT_PROFILE.equals(qName)) {
                boolean def = false;
                try{
                    def = Boolean.valueOf(attrs.getValue(ATTR_PROFILE_DEFAULT)).booleanValue();
                } catch (Exception ex){}
                profile = new CDCDevice.CDCProfile( getMandatoryValue(attrs, ATTR_PROFILE_NAME),
                                                    attrs.getValue(ATTR_PROFILE_DESCRIPTION),
                                                    getMandatoryValue(attrs, ATTR_PROFILE_VERSION),
                                                    null,
                                                    getMandatoryValue(attrs, ATTR_PROFILE_CLASSPATH),
                                                    attrs.getValue(ATTR_PROFILE_RUNCLASSPATH),
                                                    def);
            }
            else if (ELEMENT_EXECUTION.equals(qName)) {
                if (executionModes == null)
                    executionModes = new HashMap<String,String>();
                executionModes.put(attrs.getValue(ATTR_EXECUTION_TYPE), attrs.getValue(ATTR_EXECUTION_CLASS)); 
            }
            else if (ELEMENT_SCREEN.equals(qName)) {
                if (screens == null)
                    screens = new ArrayList<CDCDevice.Screen>();
                screens.add( new CDCDevice.Screen(attrs.getValue(ATTR_SCREEN_WIDTH), attrs.getValue(ATTR_SCREEN_HEIGHT), attrs.getValue(ATTR_SCREEN_BITDEPTH), attrs.getValue(ATTR_SCREEN_ISCOLOR), attrs.getValue(ATTR_SCREEN_ISTOUCH), attrs.getValue(ATTR_SCREEN_ISMAIN)));
            }
        }
        
        public void endElement (String uri, String localName, String qName) {
            if (ELEMENT_PROPERTIES.equals(qName)) {
                propertyMap = null;
            }
            else if (ELEMENT_SYSPROPERTIES.equals(qName)) {
                if (dtdVersion == 0) //no ELEMENENT_DEVICE in DTD 1.0
                {
                    if (devices == null)
                        devices = new ArrayList<CDCDevice>();
                    devices.add(new CDCDevice("Default","Default device",
                            new CDCDevice.CDCProfile[] {new CDCDevice.CDCProfile ( "PP-1.0",
                                    sysProperties.get("java.vm.name"),
                                    sysProperties.get("java.vm.version"),
                                    null,
                                    sysProperties.get("sun.boot.class.path"),
                                    null,
                                    true
                                    )},
                            null));
                }
                propertyMap = null;
            }
            else if (ELEMENT_SOURCEPATH.equals(qName) || ELEMENT_JAVADOC.equals(qName)) {
                path = null;
            }
            else if (ELEMENT_RESOURCE.equals(qName)) {
                try {
                    //make sure, that after install URL is resolved correctly
                    File f = new File(new URL(this.buffer.toString()).getPath());
                    this.path.add (f.toURI().toURL());
                } catch (MalformedURLException mue) {
                    ErrorManager.getDefault().notify(mue); 
                }
                this.buffer = null;
            }
            else if (ELEMENT_DEVICE.equals(qName)) {
                if (devices == null)
                    devices = new ArrayList<CDCDevice>();
                devices.add(new CDCDevice(deviceName, deviceDescription, 
                        profiles.toArray(new CDCDevice.CDCProfile[profiles.size()]), 
                        screens != null ? (CDCDevice.Screen[])screens.toArray(new CDCDevice.Screen[screens.size()]) : null));
                profiles = null;
                screens  = null;
            }
            else if (ELEMENT_PROFILE.equals(qName)) {
                if ( profiles == null )
                    profiles = new ArrayList<CDCDevice.CDCProfile>();
                profile.setExecutionModes(executionModes);
                profiles.add(profile);
                profile = null;
                executionModes = null;
            }
        }

        public void characters(char chars[], int start, int length){
            if (this.buffer != null) {
                this.buffer.append(chars, start, length);
            }
        }
        
        public org.xml.sax.InputSource resolveEntity(String publicId, String systemId)
        {
            if (PLATFORM_DTD_ID.equals (publicId)) {
                dtdVersion=0;
                return new org.xml.sax.InputSource (new ByteArrayInputStream (new byte[0]));
            } else if (PLATFORM_DTD_ID_1_1.equals (publicId)) {
                dtdVersion=1;
                return new org.xml.sax.InputSource (new ByteArrayInputStream (new byte[0]));
            } else {
                return null; // i.e. follow advice of systemID
            }
        }

        private String getMandatoryValue(org.xml.sax.Attributes attrs, String name) throws SAXException {
            String val = attrs.getValue(name);
            if (val == null || val.length()<1) throw new SAXException("Missing " + name); //NOI18N
            return val;
        }
        
    }

    private static void updateBuildProperties(final CDCPlatform p) {
        final String name = p.getAntName();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    ProjectManager.mutex().writeAccess(
                            new Mutex.ExceptionAction() {
                        public Object run() throws Exception{
                            EditableProperties props = PropertyUtils.getGlobalProperties();
                            Iterator it = props.entrySet().iterator();
                            String prefix = createName(name, "");//NOI18N
                            while (it.hasNext()) {
                                if (((String)((Map.Entry)it.next()).getKey()).startsWith(prefix)) it.remove();
                            }
                            String home = p.getHomePath();
                            if (home == null) //broken platform
                                return null;
                            props.setProperty(createName(name,"home"), home); //NOI18N
                            props.setProperty(createName(name,"type"), p.getType());   //NOI18N
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
    
    protected static String createName(String propName, String propType) {
        return "platforms." + propName + "." + propType;        //NOI18N
    }    
}
