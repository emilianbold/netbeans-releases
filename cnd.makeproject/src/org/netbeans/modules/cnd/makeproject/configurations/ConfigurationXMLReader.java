/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.configurations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLDocReader;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;

/**
 * was: ConfigurationDescriptorHelper
 */

public class ConfigurationXMLReader extends XMLDocReader {
    private static int DEPRECATED_VERSIONS = 26;
    
    
    private FileObject projectDirectory;
    
    public ConfigurationXMLReader(FileObject projectDirectory) {
        this.projectDirectory = projectDirectory;
        // LATER configurationDescriptor = new
    }
    
    
    /*
     * was: readFromDisk
     */
    
    public ConfigurationDescriptor read(String relativeOffset) throws IOException {
        
        String tag = null;
        
        // Try first new style file
        tag = CommonConfigurationXMLCodec.CONFIGURATION_DESCRIPTOR_ELEMENT;
        FileObject xml = projectDirectory.getFileObject("nbproject/configurations.xml");
        if (xml == null) {
            // then try old style file....
            tag = CommonConfigurationXMLCodec.PROJECT_DESCRIPTOR_ELEMENT;
            xml = projectDirectory.getFileObject("nbproject/projectDescriptor.xml");
        }
        
        if (xml == null) {
            displayErrorDialog();
            return null;
        }
        
        boolean success;
        
        String path = FileUtil.toFile(projectDirectory).getPath();
        final MakeConfigurationDescriptor configurationDescriptor = new MakeConfigurationDescriptor(path);
        XMLDecoder decoder =
                new ConfigurationXMLCodec(tag,
                projectDirectory,
                configurationDescriptor,
                relativeOffset);
        registerXMLDecoder(decoder);
        InputStream inputStream = xml.getInputStream();
        success = read(inputStream, FileUtil.toFile(xml).getPath());
        deregisterXMLDecoder(decoder);
        
        if (!success) {
            displayErrorDialog();
            return null;
        }
        
        
        //
        // Now for the auxiliary/private entry
        //
        
        xml = projectDirectory.getFileObject("nbproject/private/configurations.xml");
        if (xml == null) {
            // Don't post an error.
            // It's OK to sometimes not have a private config
            return configurationDescriptor;
        }
        
        XMLDecoder auxDecoder =
                new AuxConfigurationXMLCodec(tag, configurationDescriptor);
        registerXMLDecoder(auxDecoder);
        inputStream = xml.getInputStream();
        success = read(inputStream, projectDirectory.getName());
        deregisterXMLDecoder(auxDecoder);
        
        if (!success) {
            return configurationDescriptor;
        }
        
        // Some samples are generated without generated makefile. Don't mark these 'not modified'. Then
        // the makefiles will be generated before the project is being built
        FileObject makeImpl = projectDirectory.getFileObject("nbproject/Makefile-impl.mk");
        configurationDescriptor.setModified(makeImpl == null || relativeOffset != null);
        
        // Check version and display deprecation warning if too old
        if (configurationDescriptor.getVersion() >= 0 && configurationDescriptor.getVersion() <= DEPRECATED_VERSIONS) {
            File projectFile = FileUtil.toFile(projectDirectory);
            final String message = NbBundle.getMessage(ConfigurationXMLReader.class, "OLD_VERSION_WARNING", projectFile.getName());
            Runnable warning = new Runnable() {
                public void run() {
                    NotifyDescriptor nd = new NotifyDescriptor(message,
                        NbBundle.getMessage(ConfigurationXMLReader.class, "CONVERT_DIALOG_TITLE"), NotifyDescriptor.YES_NO_OPTION,
                        NotifyDescriptor.QUESTION_MESSAGE,
                        null, NotifyDescriptor.YES_OPTION);
                    Object ret = DialogDisplayer.getDefault().notify(nd);
                    if (ret == NotifyDescriptor.YES_OPTION) {
                        configurationDescriptor.setModified(true);
                    }
                }
            };
            SwingUtilities.invokeLater(warning);
        }
        
        return configurationDescriptor;
    }
    
    private void displayErrorDialog() {
        //String errormsg = NbBundle.getMessage(ConfigurationXMLReader.class, "CANTREADDESCRIPTOR", projectDirectory.getName());
        //DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
    }
    
    
    // interface XMLDecoder
    protected String tag() {
        return null;
    }
    
    // interface XMLDecoder
    public void start(Attributes atts) {
    }
    
    // interface XMLDecoder
    public void end() {
    }
    
    // interface XMLDecoder
    public void startElement(String name, Attributes atts) {
    }
    
    // interface XMLDecoder
    public void endElement(String name, String currentText) {
    }
}
