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
package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

// XXX should not use synchronization, rather PM.mutex() -jglick

/**
 * This class represents a project source roots. It is used to obtain roots as Ant properties, FileObject's
 * or URLs.
 * @author Tomas Zezula
 */
public final class SourceRoots {

    public static final String PROP_ROOT_PROPERTIES = "rootProperties";    //NOI18N
    public static final String PROP_ROOTS = "roots";   //NOI18N

    private AntProjectHelper helper;
    private PropertyEvaluator evaluator;
    private String elementName;
    private List sourceRootProperties;
    private List sourceRootNames;
    private List sourceRoots;
    private List sourceRootURLs;
    private PropertyChangeSupport support;
    private ProjectMetadataListener listener;

    /**
     * Creates new SourceRoots
     * @param helper
     * @param evaluator
     * @param elementName the name of XML element under which are declared the roots
     */
    SourceRoots (AntProjectHelper helper, PropertyEvaluator evaluator, String elementName) {
        assert helper != null && evaluator != null && elementName != null;
        this.helper = helper;
        this.evaluator = evaluator;
        this.elementName = elementName;
        this.support = new PropertyChangeSupport(this);
        this.listener = new ProjectMetadataListener();
        this.evaluator.addPropertyChangeListener (WeakListeners.propertyChange(this.listener,this.evaluator));
        this.helper.addAntProjectListener ((AntProjectListener)WeakListeners.create(AntProjectListener.class, this.listener,this.helper));
    }


    /**
     * Returns the display names of soruce roots
     * The returned array has the same length as an array returned by the getRootProperties.
     * It may contain empty strings but not null.
     * @return an array of String
     */
    public synchronized  String[] getRootNames () {
        if (this.sourceRootNames == null) {
            readProjectMetadata();
        }
        return (String[]) this.sourceRootNames.toArray (new String[this.sourceRootNames.size()]);
    }

    /**
     * Returns names of Ant properties in the project.properties file holding the source roots.
     * @return an array of String
     */
    public synchronized String[] getRootProperties () {
        if (this.sourceRootProperties == null) {
            readProjectMetadata();
        }
        return (String[]) this.sourceRootProperties.toArray (new String[this.sourceRootProperties.size()]);
    }

    /**
     * Returns the source roots
     * @return an array of FileObject
     */
    public synchronized FileObject[] getRoots () {
        if (this.sourceRoots == null) {
            String[] srcProps = this.getRootProperties();
            List result = new ArrayList();
            for (int i = 0; i<srcProps.length; i++) {
                String prop = this.evaluator.getProperty(srcProps[i]);
                if (prop != null) {
                    FileObject f = helper.resolveFileObject(prop);
                    if (f == null) {
                        continue;
                    }
                    if (FileUtil.isArchiveFile(f)) {
                        f = FileUtil.getArchiveRoot(f);
                    }
                    result.add(f);
                }
            }
            this.sourceRoots = Collections.unmodifiableList (result);
        }
        return (FileObject[]) this.sourceRoots.toArray(new FileObject[sourceRoots.size()]);
    }

    /**
     * Returns the source roots as URLs.
     * @return an array of URL
     */
    public synchronized URL[] getRootURLs() {
        if (this.sourceRootURLs == null) {
            String[] srcProps = this.getRootProperties();
            List result = new ArrayList();
            for (int i = 0; i<srcProps.length; i++) {
                String prop = this.evaluator.getProperty(srcProps[i]);
                if (prop != null) {
                    File f = helper.resolveFile (prop);
                    try {
                        URL url = f.toURI().toURL();
                        if (FileUtil.isArchiveFile(url)) {
                            url = FileUtil.getArchiveRoot(url);
                        }
                        else if (!f.exists()) {
                            url = new URL(url.toExternalForm() + "/"); // NOI18N
                        }
                        result.add(url);
                    } catch (MalformedURLException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            }
            this.sourceRootURLs = Collections.unmodifiableList (result);
        }
        return (URL[]) this.sourceRootURLs.toArray(new URL[sourceRootURLs.size()]);
    }

    /**
     * Adds PropertyChangeListener
     * @param listener
     */
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        this.support.addPropertyChangeListener (listener);
    }

    /**
     * Removes PropertyChangeListener
     * @param listener
     */
    public void removePropertyChangeListener (PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }

    private void resetCache (boolean isXMLChange, String propName) {
        boolean fire = false;
        synchronized (this) {
            if (isXMLChange) {
                this.sourceRootProperties = null;
                this.sourceRootNames = null;
                this.sourceRoots = null;
                this.sourceRootURLs = null;
                fire = true;
            }
            else if (propName == null || (this.sourceRootProperties != null && this.sourceRootProperties.contains(propName))) {
                this.sourceRoots = null;
                this.sourceRootURLs = null;
                fire = true;
            }
        }
        if (fire) {
            if (isXMLChange) {
                this.support.firePropertyChange (PROP_ROOT_PROPERTIES,null,null);
            }
            this.support.firePropertyChange (PROP_ROOTS,null,null);
        }
    }

    private void readProjectMetadata () {
        Element cfgEl = helper.getPrimaryConfigurationData(true);
        NodeList nl = cfgEl.getElementsByTagNameNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, elementName);
        assert nl.getLength() == 0 || nl.getLength() == 1 : "Illegal project.xml"; //NOI18N
        List rootProps = new ArrayList ();
        List rootNames = new ArrayList ();
        // It can be 0 in the case when the project is created by J2SEProjectGenerator and not yet customized
        if (nl.getLength()==1) {
            NodeList roots = ((Element)nl.item(0)).getElementsByTagNameNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root");    //NOI18N
            for (int i=0; i<roots.getLength(); i++) {
                Element root = (Element) roots.item(i);
                // XXX do *not* use namespace-qualified attributes -jglick
                String value = root.getAttributeNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "id");  //NOI18N
                assert value.length() > 0 : "Illegal project.xml";
                rootProps.add(value);
                value = root.getAttributeNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");  //NOI18N
                rootNames.add (value);
            }
        }
        this.sourceRootProperties = Collections.unmodifiableList(rootProps);
        this.sourceRootNames = Collections.unmodifiableList(rootNames);
    }

    private class ProjectMetadataListener implements PropertyChangeListener,AntProjectListener {

        public void propertyChange(PropertyChangeEvent evt) {
            resetCache (false,evt.getPropertyName());
        }

        public void configurationXmlChanged(AntProjectEvent ev) {
            resetCache (true,null);
        }

        public void propertiesChanged(AntProjectEvent ev) {
            //Handled by propertyChange
        }
    }

}
