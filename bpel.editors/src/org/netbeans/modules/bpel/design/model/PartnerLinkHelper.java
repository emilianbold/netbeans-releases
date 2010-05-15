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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.bpel.design.model;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.connections.MessageConnection;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.PartnerlinkPattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.xml.retriever.Retriever;
import org.netbeans.modules.xml.schema.model.Schema;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.xam.Model;
import org.openide.filesystems.FileUtil;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.modules.bpel.design.model.patterns.CollapsedPattern;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.reference.ReferenceUtil;

/**
 * @author Alexey Yarmolenko
 */
public class PartnerLinkHelper {

  private DiagramModel myModel;

  public PartnerLinkHelper(DiagramModel model) {
      myModel = model;
  }

  public void updateMessageFlowLinks(CollapsedPattern collapsedPattern) {
      VisualElement element = collapsedPattern.getFirstElement();
      List<Connection> connections = element.getAllConnections();

      for (Connection c : connections) {
          if (c instanceof MessageConnection) {
              c.remove();
          }
      }
      updateMessageFlowLinks(collapsedPattern.getOMReference(), element);
  }

  private void updateMessageFlowLinks(BpelEntity entity, VisualElement element) {
      if (entity instanceof OperationReference && entity instanceof PartnerLinkReference) {
          List<Connection> conns = element.getAllConnections();

          VisualElement op = getOperation(entity);

          if (op != null) {
              Connection c = new MessageConnection(element.getPattern());
              connect(c, element, op);
          }
      } else {
          List<BpelEntity> children = entity.getChildren();

          if (children != null) {
              for (BpelEntity child : children) {
                  updateMessageFlowLinks(child, element);
              }
          }
      }
  }

  private void connect(Connection c, VisualElement wsa, VisualElement op) {
      Pattern pl = op.getPattern();
      boolean isConsumer = (((PartnerlinkPattern) pl).getType() == PartnerRole.CONSUMER);
      BpelEntity wsa_ent = wsa.getPattern().getOMReference();
      boolean isCaller = (wsa_ent instanceof OnMessage ||
              wsa_ent instanceof OnEvent ||
              wsa_ent instanceof Receive);

      if (isConsumer) {
          c.connect(isCaller ? op : wsa,
                  isCaller ? Direction.RIGHT : Direction.LEFT,
                  isCaller ? wsa : op,
                  isCaller ? Direction.LEFT : Direction.RIGHT);
      } else {
          c.connect(isCaller ? op : wsa,
                  isCaller ? Direction.LEFT : Direction.RIGHT,
                  isCaller ? wsa : op,
                  isCaller ? Direction.RIGHT : Direction.LEFT);
      }

  }

  public void updateMessageFlowLinks(Pattern pattern) {
      Object omRef = pattern.getOMReference();

      if (omRef instanceof OperationReference && omRef instanceof PartnerLinkReference) {
          VisualElement elem = pattern.getFirstElement();
          List<Connection> conns = elem.getAllConnections();

          for (Connection c : conns) {
              if (c instanceof MessageConnection) {
                  c.remove();
              }
          }
          VisualElement op = getOperation((BpelEntity) omRef);

          if (op != null) {
              Connection c = new MessageConnection(pattern);
              connect(c, pattern.getFirstElement(), op);
          }
      }
  }

  private VisualElement getOperation(BpelEntity entity) {
      assert entity instanceof OperationReference;
      assert entity instanceof PartnerLinkReference;

      WSDLReference<Operation> op_ref = ((OperationReference) entity).getOperation();
      if (op_ref == null) {
          return null;
      }

      BpelReference<PartnerLink> pl_ref = ((PartnerLinkReference) entity).getPartnerLink();
      if (pl_ref == null) {
          return null;
      }

      PartnerLink pl = pl_ref.get();
      if (pl == null) {
          return null;
      }

      Pattern pattern = myModel.getPattern(pl);

      if (pattern == null) {
          return null;
      } else if (myModel.isCollapsed(pattern.getOMReference())) {
          return pattern.getFirstElement();
      }
      return ((PartnerlinkPattern) pattern).getElement(op_ref);
  }

  public PartnerLink createPartnerLink(DataObject dataObj) {
      try {
          FileObject fo = dataObj.getPrimaryFile();
          if (fo == null) {
              return null;
          }
          URI uri = fo.getURL().toURI();
          final String name = fo.getName();
          WSDLModel wsdlModel = myModel.getView().getProcessHelper().getWSDLModelFromUri(uri);

          if (wsdlModel == null) {
              return null;
          }
          final List<PartnerLinkType> pltList = wsdlModel.getDefinitions().getExtensibilityElements(PartnerLinkType.class);

          if (pltList == null || pltList.size() == 0) {
              return null;
          }
          final BpelModel bpelModel = myModel.getView().getBPELModel();
          assert (bpelModel != null) : "Broken WSDL model"; // NOI18N

          Callable<PartnerLink> createPLCallable = new Callable<PartnerLink>() {

              public PartnerLink call() throws Exception {
                  PartnerLink pl = bpelModel.getBuilder().createPartnerLink();
                  pl.setName(name);
                  pl.setPartnerLinkType(pl.createWSDLReference(pltList.get(0), PartnerLinkType.class));
                  return pl;
              }
          };

          return bpelModel.invoke(createPLCallable, this);
      } catch (Exception ex) {
          ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
      }
      return null;
  }

  public PartnerLink createPartnerLink() {
      return myModel.getView().getBPELModel().getBuilder().createPartnerLink();
  }

  private boolean isServiceRole(Role role) {
      if (role == null || role.getPortType() == null) {
          return false;
      }
      PortType pt = role.getPortType().get();

      if (pt == null) {
          return false;
      }

      WSDLModel wsdlModel = pt.getModel();
      Collection<Service> services = wsdlModel.getDefinitions().getServices();

      if (services == null) {
          return false;
      }

      for (Service service : services) {
          for (Port port : service.getPorts()) {
              Binding b = port.getBinding().get();
              if (b == null) {
                  continue;
              }
              PortType t = b.getType().get();
              if (t != null && pt.equals(t)) {
                  return true;
              }
          }
      }
      return false;
  }

  public static void saveModel(Model model) {
      DataObject model_do = (DataObject) model.getModelSource().getLookup().lookup(DataObject.class);

      if (model_do != null) {
          SaveCookie sc = (SaveCookie) model_do.getCookie(SaveCookie.class);

          if (sc != null) {
              try {
                  sc.save();
              } catch (IOException ex) {
                  ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
              }
          }
      }
  }

  private void fixSoapAddress(WSDLModel model) {
      Definitions defs = model.getDefinitions();

      if (defs == null) {
          return;
      }
      model.startTransaction();
      try {
          Collection<Service> svcs = defs.getServices();
          if (svcs == null) {
              return;
          }
          for (Service svc : svcs) {
              Collection<Port> ports = svc.getPorts();
              if (ports == null) {
                  continue;
              }
              for (Port port : ports) {
                  Collection<SOAPAddress> addrs = port.getExtensibilityElements(SOAPAddress.class);

                  for (SOAPAddress addr : addrs) {
                      URL url;
                      try {
                          url = new URL(addr.getLocation());

                          if (!isExternalResource(url)) {
                              url = new URL(url.getProtocol(), "localhost", url.getPort(), url.getFile());
                              addr.setLocation(url.toString());
                          }

                      } catch (MalformedURLException ex) {
                          ex.printStackTrace();
                      }
                  }
              }
          }
          if (true) {
              Types types = defs.getTypes();

              if (types != null) {
                  Collection<Schema> schemas = types.getSchemas();
                  if (schemas != null) {
                      for (Schema s : schemas) {
                          Collection<org.netbeans.modules.xml.schema.model.Import> imps = s.getImports();
                          for (org.netbeans.modules.xml.schema.model.Import imp : imps) {
                              String location = imp.getSchemaLocation();

                              if (location == null) {
                                  continue;
                              }
                              if (isExternalResource(location)) {
                                  continue;
                              }
                              String new_location = fixLocation(model, location);

                              if (new_location != null) {
                                  imp.setSchemaLocation(new_location);
                              }
                          }
                      }
                  }
              }
              Collection<Import> imports = defs.getImports();

              for (Import imp : imports) {
                  String location = imp.getLocation();
              
                  if (location == null) {
                      continue;
                  }
                  if (isExternalResource(location)) {
                      continue;
                  }
                  String new_location = getFileName(location);

                  if (new_location != null) {
                      imp.setLocation(new_location);
                  }
              }
          }
      } finally {
          model.endTransaction();
      }
      saveModel(model);
      Collection<Import> imports = defs.getImports();

      for (Import imp : imports) {
          WSDLModel m = null;
          
          try {
              String location = imp.getLocation();

              if (location != null && !isExternalResource(location)) {
                  m = imp.getImportedWSDLModel();
              }
          } catch (CatalogModelException ex) {}

          if (m != null) {
              fixSoapAddress(m);
          }
      }
  }

  private String fixLocation(WSDLModel model, String location) {
      FileObject file = (FileObject) model.getModelSource().getLookup().lookup(FileObject.class);
      CatalogWriteModel catalog;

      try {
          catalog = CatalogWriteModelFactory.getInstance().getCatalogWriteModelForProject(file);
      } catch (CatalogModelException e) {
          return null;
      }
      Collection<CatalogEntry> entries = catalog.getCatalogEntries();

      for (CatalogEntry entry : entries) {
          if (location.equals(entry.getSource())) {
              String value = entry.getTarget();
              int k = value.lastIndexOf('/');

              if (k != -1) {
                  value = value.substring(k + 1);
              }
              return value;
          }
      }
      return null;
  }

  public static String getFileName(String filepath) {
      if (filepath == null) {
          return null;
      }
      int pos = filepath.lastIndexOf('/');

      if (pos >= 0) {
          filepath = filepath.substring(pos + 1);
      }
      pos = filepath.indexOf('?');

      if (pos >= 0) {
          filepath = filepath.substring(0, pos);
      }
      return filepath;
  }

  public static boolean isExternalResource(String url) {
      try {
          return isExternalResource(new URL(url));
      } catch (MalformedURLException ex) {

      }
      return false;
  }

  public static boolean isExternalResource(URL url) {
      if (url == null || url.getHost() == null) {
          return false;
      }
      InetAddress inet_addr;
      try {
          inet_addr = InetAddress.getByName(url.getHost());

          if (inet_addr == null) {
              return false;
          }
          return (NetworkInterface.getByInetAddress(inet_addr) == null);
      } catch (SocketException ex) {
      } catch (UnknownHostException ex) {
      }
      return false;
  }

  public static PartnerRole getPartnerlinkRole(PartnerLink pl) {
      if (pl == null) {
          return null;
      }
      WSDLReference<Role> myRole = pl.getMyRole();
      WSDLReference<Role> partnerRole = pl.getPartnerRole();
      WSDLReference<PartnerLinkType> plt_ref = pl.getPartnerLinkType();

      if (plt_ref != null && plt_ref.get() != null) {
          PartnerLinkType plt = plt_ref.get();
          if (myRole != null && myRole.get() != null ) {
              return (plt.getRole1() != null && myRole.references(plt.getRole1())) ? PartnerRole.CONSUMER : PartnerRole.PROVIDER;
              
          } else if (partnerRole != null && partnerRole.get() != null) {
              return (plt.getRole1() != null && partnerRole.references(plt.getRole1())) ? PartnerRole.PROVIDER : PartnerRole.CONSUMER;
          }
      }
      return null;
  }

  public static void setPartnerlinkRole(PartnerLink pl, PartnerRole type) {
      WSDLReference<PartnerLinkType> plt_ref = pl.getPartnerLinkType();

      if (plt_ref != null && plt_ref.get() != null) {
          PartnerLinkType plt = plt_ref.get();
          Role role = plt.getRole1();
          WSDLReference<Role> role_ref = pl.createWSDLReference(role, Role.class);
          if (type == PartnerRole.CONSUMER) {
              pl.setMyRole(role_ref);
              pl.removePartnerRole();
          } else {
              pl.removeMyRole();
              pl.setPartnerRole(role_ref);
          }
      }
  }

  public static WSDLModel getWSDLModel(FileObject fo) {
      WSDLModel wsdlModel = null;

      if (fo == null) {
          return null;
      }
//System.out.println();
//System.out.println("!!!!! fo: " + fo);

      try {
//System.out.println("!!!!! fo: " + fo.getURL());
//System.out.println();
          File file = new File(fo.getURL().toURI());
          ModelSource modelSource = Utilities.getModelSource(FileUtil.toFileObject(file), true);
          wsdlModel = org.netbeans.modules.xml.wsdl.model.WSDLModelFactory.getDefault().getModel(modelSource);
      }
      catch (Exception ex) {
          ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
      }
      return wsdlModel;
  }

  private FileObject getBpel() {
    return (FileObject) myModel.getView().getBPELModel().getModelSource().getLookup().lookup(FileObject.class);
  }

  // vlv: dnd
  public FileObject retrieveWSDL(FileObject wsdl) {
    if (wsdl == null) {
      return null;
    }

    FileObject bpel = getBpel();

    if (bpel == null) {
      return null;
    }
    ReferenceUtil.addFile(bpel, wsdl);

    return wsdl;
  }
}
