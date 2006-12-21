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
import java.io.OutputStream;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.api.xml.XMLDocWriter;
import org.netbeans.modules.cnd.api.xml.XMLEncoderStream;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class ConfigurationXMLWriter extends XMLDocWriter {

    private FileObject projectDirectory;
    private ConfigurationDescriptor projectDescriptor;

    private CommonConfigurationXMLCodec encoder;

    public ConfigurationXMLWriter(FileObject projectDirectory,
				  ConfigurationDescriptor projectDescriptor) {
	this.projectDirectory = projectDirectory;
	this.projectDescriptor = projectDescriptor;
    }

    public void write() {
	if (projectDescriptor == null)
	    return;

	String tag = CommonConfigurationXMLCodec.CONFIGURATION_DESCRIPTOR_ELEMENT;

	encoder = new ConfigurationXMLCodec(tag, null, projectDescriptor, null);
	write("nbproject/configurations.xml");

	encoder = new AuxConfigurationXMLCodec(tag, projectDescriptor);
	write("nbproject/private/configurations.xml");
    }

    /*
     * was: ConfigurationDescriptorHelper.storeDescriptor()
     */
    private void write(String relPath) {
	File projectDirectoryFile = FileUtil.toFile(projectDirectory);
	File projectDescriptorFile = new File(projectDirectoryFile.getPath() + '/' + relPath); // UNIX path

        if (!projectDescriptorFile.exists()) {
            try {
		// make sure folder is created first...
                projectDescriptorFile.getParentFile().mkdir();
                projectDescriptorFile.createNewFile();
            }
            catch (IOException ioe) {
                ;// FIXUP...
            }
        }

        FileObject xml = projectDirectory.getFileObject(relPath);
        try {
            org.openide.filesystems.FileLock lock = xml.lock();
            try {
                OutputStream os = xml.getOutputStream(lock);
		write(os);
            }
            finally {
                lock.releaseLock();
            }
        }
        catch (Exception e) {
        }
    }

    // interface XMLEncoder
    public void encode(XMLEncoderStream xes) {
	encoder.encode(xes);
    }
}
