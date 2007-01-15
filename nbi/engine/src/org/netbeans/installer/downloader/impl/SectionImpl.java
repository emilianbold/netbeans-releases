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
 * 
 * $Id$
 */
package org.netbeans.installer.downloader.impl;

import org.netbeans.installer.downloader.Pumping.Section;


import java.util.Collections;
import java.util.List;
import org.netbeans.installer.utils.helper.Pair;
import org.netbeans.installer.utils.xml.DomExternalizable;
import org.netbeans.installer.utils.xml.DomUtil;
import org.netbeans.installer.utils.xml.visitors.DomVisitor;
import org.netbeans.installer.utils.xml.visitors.RecursiveDomVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Danila_Dugurov
 */
public class SectionImpl implements Section, DomExternalizable {
  
  /////////////////////////////////////////////////////////////////////////////////
  // Instance
  protected long start;
  protected long length;
  protected long offset;
  private PumpingImpl owner;
  
  protected SectionImpl(PumpingImpl owner, long start, long length) {
    this.owner = owner;
    this.start = start;
    this.length = length;
    this.offset = start;
  }
  
  //before readXML
  protected SectionImpl(PumpingImpl owner) {
    this.owner = owner;
  }
  
  public Pair<Long, Long> getRange() {
    return Pair.create(start, start + length);
  }
  
  public long offset() {
    return offset;
  }
  
  public long length() {
    return length;
  }
  
  public long start() {
    return start;
  }
  
  public void shiftOffset(long delta) {
    offset += delta;
    owner.fireChanges("pumpingUpdate");
  }
  
  public List<Pair<String, String>> headers() {
    if (owner.acceptBytes) {
      if (length > 0) {
        final long end = start + length - 1;
        return Collections.singletonList(Pair.create("Range", "bytes=" + offset + "-" + end));
      } else if (length == -1) {
        return Collections.singletonList(Pair.create("Range", "bytes=" + offset + "-"));
      }
    } else {
      offset = start;
    }
    return Collections.emptyList();
  }
  
  public void readXML(Element element) {
    final DomVisitor visitor = new RecursiveDomVisitor() {
      public void visit(Element element) {
        final String name = element.getNodeName();
        if ("start".equals(name)) {
          start = Long.valueOf(element.getTextContent());
        } else if ("length".equals(name)) {
          length = Long.valueOf(element.getTextContent());
        } else if ("offset".equals(name)) {
          offset = Long.valueOf(element.getTextContent());
        } else
          super.visit(element);
      }
    };
    visitor.visit(element);
  }
  
  public Element writeXML(Document document) {
    final Element root = document.createElement("section");
    DomUtil.addElemet(root, "start", String.valueOf(start));
    DomUtil.addElemet(root, "length", String.valueOf(length));
    DomUtil.addElemet(root, "offset", String.valueOf(offset));
    return root;
  }
}
