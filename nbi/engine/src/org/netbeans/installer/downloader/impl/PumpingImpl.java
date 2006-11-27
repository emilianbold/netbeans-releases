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

import org.netbeans.installer.downloader.Pumping;
import org.netbeans.installer.downloader.PumpingMode;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.downloader.queue.QueueBase;
import org.netbeans.installer.utils.exceptions.UnexpectedExceptionError;
import org.netbeans.installer.utils.helper.URLUtil;
import org.netbeans.installer.utils.xml.DomExternalizable;
import org.netbeans.installer.utils.xml.DomUtil;
import org.netbeans.installer.utils.xml.visitors.DomVisitor;
import org.netbeans.installer.utils.xml.visitors.RecursiveDomVisitor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Danila_Dugurov
 */

public class PumpingImpl implements Pumping, DomExternalizable {

  private static int nextId;

  private final transient String id;
  private final QueueBase queue;

  protected URL url;
  protected URL realUrl;
  protected File folder;
  protected File file;
  protected long length = -2;
  protected boolean acceptBytes = false;
  protected Date lastModif = new Date(0);
  protected List<SectionImpl> sections = new LinkedList<SectionImpl>();
  protected State state = State.NOT_PROCESSED;
  protected PumpingMode mode = PumpingMode.SINGLE_THREAD;

  public PumpingImpl(URL url, File folder, QueueBase queue) {
    this(queue);
    this.url = url;
    this.folder = folder;
  }

  //before read from xml
  public PumpingImpl(QueueBase queue) {
    this.id = getClass().getName() + nextId++;
    this.queue = queue;
  }

  public String getId() {
    return id;
  }

  public URL declaredURL() {
    return url;
  }

  public URL realURL() {
    return realUrl;
  }

  public File outputFile() {
    return file;
  }

  public File folder() {
    return folder;
  }

  public long length() {
    return length;
  }

  public PumpingMode mode() {
    return mode;
  }

  public State state() {
    return state;
  }

  public void changeState(State newState) {
    this.state = newState;
    fireChanges("pumpingStateChange");
  }

  public Section[] getSections() {
    return sections.toArray(new Section[sections.size()]);
  }

  public void fireChanges(String method) {
    try {
      queue.fire(method, id);
    } catch (NoSuchMethodException ex) {
      throw new UnexpectedExceptionError("Listener contract was changed", ex);
    }
  }

  public SectionImpl getSection() {
    if (mode == PumpingMode.SINGLE_THREAD || !acceptBytes) {
      if (sections.isEmpty()) sections.add(new SectionImpl(this, 0, length));
      return sections.get(0);
    }
    throw new UnsupportedOperationException("multi mode not supported yet!");
  }

  public void readXML(Element element) {
    final DomVisitor visitor = new RecursiveDomVisitor() {
      public void visit(Element element) {
        final String name = element.getNodeName();
        if ("url".equals(name)) {
          url = URLUtil.create(element.getTextContent());
        } else if ("realUrl".equals(name)) {
          realUrl = URLUtil.create(element.getTextContent());
        } else if ("length".equals(name)) {
          length = Long.valueOf(element.getTextContent());
        } else if ("lastModif".equals(name)) {
          lastModif = new Date(Long.valueOf(element.getTextContent()));
        } else if ("acceptBytes".equals(name)) {
          acceptBytes = Boolean.valueOf(element.getTextContent());
        } else if ("state".equals(name)) {
          state = State.valueOf(element.getTextContent());
        } else if ("file".equals(name)) {
          if (!"".equals(element.getTextContent().trim()))
            file = new File(element.getTextContent());
        } else if ("folder".equals(name)) {
          folder = new File(element.getTextContent());
        } else if ("section".equals(name)) {
          SectionImpl section = new SectionImpl(PumpingImpl.this);
          section.readXML(element);
          sections.add(section);
        } else
          super.visit(element);
      }
    };
    visitor.visit(element);
  }

  public Element writeXML(Document document) {
    final Element root = document.createElement("pumping");
    DomUtil.addElemet(root, "url", url.toString());
    DomUtil.addElemet(root, "realUrl", realUrl != null ? realUrl.toString() : null);
    DomUtil.addElemet(root, "length", String.valueOf(length));
    DomUtil.addElemet(root, "lastModif", String.valueOf(lastModif.getTime()));
    DomUtil.addElemet(root, "acceptBytes", String.valueOf(acceptBytes));
    DomUtil.addElemet(root, "state", state.toString());
    DomUtil.addElemet(root, "file", file != null ? file.getAbsolutePath() : null);
    DomUtil.addElemet(root, "folder", folder.getAbsolutePath());
    for (SectionImpl section : sections) {
      DomUtil.addChild(root, section);
    }
    return root;
  }

  public void init(URL realUrl, long length, Date lastModif, boolean acceptBytes) throws IOException {
    if (wasModified(realUrl, length, lastModif, acceptBytes)) {
      reset();
      this.realUrl = realUrl;
      this.length = length;
      this.acceptBytes = acceptBytes;
      if (lastModif != null) this.lastModif = lastModif;
      this.file = PumpingUtil.getFileNameFromURL(folder, this.realUrl.getPath());
      if (file.getParentFile() != null) file.getParentFile().mkdirs();
      file.createNewFile();
    }
  }

  public void reset() {
    realUrl = null;
    length = -2;
    acceptBytes = false;
    lastModif = new Date(0);
    sections = new LinkedList<SectionImpl>();
    if (file != null) file.delete();
  }

  protected boolean wasModified(URL realUrl, long length, Date lastModif, boolean acceptBytes) {
    if (this.realUrl == null || !this.realUrl.equals(realUrl)) return true;
    if (this.length == -2 || this.length != length) return true;
    if (lastModif != null && this.lastModif.before(lastModif)) return true;
    return this.acceptBytes != acceptBytes;
  }
}
