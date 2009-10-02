/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.bpel.mapper.palette;

import java.util.Date;
import javax.swing.Icon;

import org.netbeans.modules.xml.xpath.ext.CoreFunctionType;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;

import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.model.ItemHandler;
import org.netbeans.modules.bpel.mapper.model.VertexFactory;
import org.netbeans.modules.soa.validation.util.DurationDialog;
import static org.netbeans.modules.xml.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.11.27
 */
class Handler implements ItemHandler {

  Handler(CoreFunctionType type) {
    this(
      type.getMetadata().getDisplayName(),
      type.getMetadata().getIcon(),
      type);
  }

  Handler(CoreOperationType type) {
    this(
      type.getMetadata().getDisplayName(),
      type.getMetadata().getIcon(),
      type);
  }

  Handler(String string) {
    this(
      i18n(Handler.class, "LBL_String_Literal"), // NOI18N
      icon(Handler.class, "string"), // NOI18N
      string);
  }

  Handler(Number number) {
    this(
      i18n(Handler.class, "LBL_Numeric_Literal"), // NOI18N
      icon(Handler.class, "numeric"), // NOI18N
      number);
  }

  Handler(ExtFunctionMetadata data) {
    this(
      data.getDisplayName(),
      data.getIcon(),
      data);
  }

  protected Handler(String name, Icon icon, Object object) {
    myName = name;
    myIcon = icon;
    myObject = object;
  }
  
  public Icon getIcon() {
    return myIcon;
  }

  public String getDisplayName() {
    return myName;
  }

  public boolean canAddGraphSubset() {
    return true;
  }

  public GraphSubset createGraphSubset() {
    return VertexFactory.getInstance().createGraphSubset(myObject);
  }
  
  protected void setObject(Object object) {
    myObject = object;
  }

  // ------------------------------------
  static class Duration extends Handler {
    Duration() {
      super(
        i18n(Handler.class, "LBL_Duration_Literal"), // NOI18N
        icon(Handler.class, "duration"), ""); // NOI18N
    }

    @Override
    public boolean canAddGraphSubset() {
      DurationDialog dialog = new DurationDialog();
      dialog.setVisible(true);
      Object duration = dialog.getDuration();

      if (duration != null) {
        setObject(duration);
      }
//out("DURATION: " + duration);
      return duration != null;
    }
  }

  private Icon myIcon;
  private String myName;
  private Object myObject;
}
