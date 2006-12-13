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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.properties.editors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.11
 */
final class WsdlWrapper {

  WsdlWrapper(FileObject folder, String name) {
    myFolder = folder;
    myName = name;
  }

  WSDLModel getModel() {
//out("Create in: " + folder);
    FileObject file = getFile();

    if (file == null) {
      return null;
    }
    ModelSource source = Utilities.getModelSource(file, file.canWrite());
    WSDLModel model = WSDLModelFactory.getDefault().getModel(source);

    model.startTransaction();
    Definitions definitions = model.getDefinitions();
    definitions.setName(myName);
    definitions.setTargetNamespace(HOST + myName);
    model.endTransaction();

    return model;
  }

  FileObject getFile() {
    FileObject file = myFolder.getFileObject(myName, EXTENSION);

    if (file != null) { // todo m canCreate
//out("exists: " + name);
      return file;
//      file = folder.getFileObject(name, EXTENSION);
//
//      try {
//        file.delete();
//      }
//      catch (IOException e) {
//        e.printStackTrace(); // todo m
//      }
    }
/*
    try {
      file = folder.createData(name, EXTENSION);

      File content = FileUtil.toFile(file);
      PrintWriter writer = new PrintWriter(content, "UTF-8"); // NOI18N
      StringBuffer buffer = new StringBuffer();

      buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LS);
      buffer.append("<definitions" + LS);
      buffer.append("    xmlns=\"http://schemas.xmlsoap.org/wsdl/\"" + LS);
      buffer.append("    xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"" + LS);
      buffer.append("    xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"" + LS);
      buffer.append("    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" + LS);
      buffer.append("</definitions>" + LS);

      writer.write(buffer.toString());
      writer.flush();
      writer.close();
    }
    catch (IOException e) {
      e.printStackTrace(); // todo m
    }
*/
    return copyFile(myFolder, "wsdl/", "wsdl.wsdl", myName);
  }

  private FileObject copyFile(
    FileObject destination,
    String path,
    String file,
    String name)
  {
//    int k = file.lastIndexOf ("."); // NOI18N
//    String name = k == -1 ? file : file.substring(0, k);
//out();
//out("file: " + file);
//out(" dst: " + destination);
//out("name: " + name);
//out("path: " + path);
//out("find: " + Repository.getDefault().getDefaultFileSystem().findResource(path + file));
    try {
      return FileUtil.copyFile(
        Repository.getDefault().getDefaultFileSystem().findResource(path + file),
        destination,
        name);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private void out() {
    System.out.println();
  }

  private void out(Object object) {
    System.out.println("*** " + object); // NOI18N
  }

  private String myName;
  private FileObject myFolder;
  
  private static final String EXTENSION = "wsdl"; // NOI18N
  private static final String HOST = "http://enterprise.netbeans.org/bpel/"; // NOI18N
}
