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
package org.netbeans.modules.bpel.model.api.support;

import javax.swing.text.StyledDocument;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.12.10
 */
public class Util {

  private Util() {}

  public static Line getLine(ResultItem item) {
    int number;
    Component component = item.getComponents();

    if (component != null) {
      number = -1;

      if (component instanceof DocumentComponent) {
        number = getLineNumber((DocumentComponent) item.getComponents());
      } 
    }
    else {
      number = item.getLineNumber() - 1;
    }
//System.out.println("  number: " + number);

    if (number < 1) {
      return null;
    }
    FileObject file = getFileObjectByModel(component == null ? item.getModel() : component.getModel());

    if (file == null) {
      return null;
    }
    LineCookie cookie = null;

    try {
      DataObject data = DataObject.find(file);
      cookie = (LineCookie) data.getCookie(LineCookie.class);
    }
    catch (DataObjectNotFoundException e) {
      e.printStackTrace();
    }
    if (cookie == null) {
      return null;
    }
    return cookie.getLineSet().getCurrent(number);
  }
  
  private static int getLineNumber(DocumentComponent entity) {
    if (entity == null) {
      return -1;
    }
    Model model = entity.getModel();

    if (model == null) {
      return -1;
    }
    ModelSource source = model.getModelSource();

    if (source == null) {
      return -1;
    }
    Lookup lookup = source.getLookup();

    if (lookup == null) {
      return -1;
    }
    StyledDocument document = (StyledDocument) lookup.lookup(StyledDocument.class);

    if (document == null) {
      return -1;
    }
    return NbDocument.findLineNumber(document, entity.findPosition());
  }

  private static FileObject getFileObjectByModel(Model model) {
    if (model == null) {
      return null;
    }
    ModelSource src = model.getModelSource();

    if (src == null) {
     return null;
    }
    Lookup lookup = src.getLookup();

    if (lookup == null) {
      return null;
    }
    return lookup.lookup(FileObject.class);
  }
}
