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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.schema;

import java.awt.Container;

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.multiview.SchemaColumnViewMultiViewDesc;
import org.netbeans.modules.xml.schema.multiview.SchemaMultiViewSupport;
import org.netbeans.modules.xml.schema.ui.basic.SchemaColumnsView;
import org.netbeans.modules.xml.schema.ui.basic.SchemaTreeView;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.validation.ui.ShowCookie;
import org.netbeans.modules.xml.search.api.SearchTarget;
import org.openide.filesystems.FileObject;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.04.16
 */
final class SearchProvider extends org.netbeans.modules.xml.search.spi.SearchProvider.Adapter {

  public SearchProvider(DataObject data) {
    super(null, data);
  }

  @Override
  protected final Component getRoot(DataObject data)
  {
    SchemaModel model = getSchemaModel(data);
    myView = null;

    if (model == null) {
      return null;
    }
    myView = getView();

    return model.getSchema();
  }

  @Override
  protected final String getType(Component component)
  {
    return ((SchemaComponent) component).getComponentType().getName();
  }

  @Override
  protected final Node getNode(Component comp)
  {
    SchemaComponent component = (SchemaComponent) comp;
    return new CategorizedSchemaNodeFactory(component.getModel(),
      Lookups.singleton(component)).createNode(component);
  }
  
  @Override
  protected void gotoSource(Component component)
  {
    getDataObject().getCookie(ShowCookie.class).show(new ResultItem(null, null, component, null));
  }

  @Override
  protected final void gotoVisual(Component component) {
    openDocument(component);
    if (myView instanceof SchemaTreeView) {
        SchemaMultiViewSupport.requestMultiviewActive(SchemaColumnViewMultiViewDesc.PREFERRED_ID);
        ((SchemaTreeView) myView).showComponent((SchemaComponent) component);
    } else if (myView instanceof SchemaColumnsView) {
        SchemaMultiViewSupport.requestMultiviewActive(SchemaColumnViewMultiViewDesc.PREFERRED_ID);
        ((SchemaColumnsView) myView).showComponent((SchemaComponent) component);
    }
    highlight(component);
  }
  
  //The searched component is in output window. The document may/maynot be open
  //hence just open the document and this will make the TC active as well.
  private void openDocument(Component component) {
        try {
            FileObject file = component.getModel().getModelSource().getLookup().lookup(FileObject.class);
            SchemaDataObject sdo = (SchemaDataObject) DataObject.find(file);
            sdo.getSchemaEditorSupport().open();
        } catch (Exception ex) {
            //swallow: nothing breaks 'coz of this exception
        }
  }

  private Object getView() {
    Container container = getActiveTopComponent();
    Object view = getTreeView(container, "  "); // NOI18N

    if (view != null) {
      return view;
    }
    return getColumnView(container, "  "); // NOI18N
  }

  private SchemaTreeView getTreeView(Container container, String indent) {
//out(indent + container.getClass().getName());
    if (container instanceof SchemaTreeView) {
      return (SchemaTreeView) container;
    }
    java.awt.Component [] components = container.getComponents();
    SchemaTreeView view;

    for (java.awt.Component component : components) {
      if (component instanceof Container) {
        view = getTreeView((Container) component, "    " + indent); // NOI18N

        if (view != null) {
          return view;
        }
      }
    }
    return null;
  }

  private SchemaColumnsView getColumnView(Container container, String indent) {
//out(indent + container.getClass().getName());
    if (container instanceof SchemaColumnsView) {
      return (SchemaColumnsView) container;
    }
    java.awt.Component [] components = container.getComponents();
    SchemaColumnsView view;

    for (java.awt.Component component : components) {
      if (component instanceof Container) {
        view = getColumnView((Container) component, "    " + indent); // NOI18N

        if (view != null) {
          return view;
        }
      }
    }
    return null;
  }

  private SchemaModel getSchemaModel(DataObject data) {
    Model model = getModel(data);

    if (model instanceof SchemaModel) {
      return (SchemaModel) model;
    }
    return null;
  }

  @Override
  public SearchTarget [] getTargets()
  {
    return TARGETS;
  }

  private static SearchTarget create(Class<? extends Object> clazz) {
    return new SearchTarget.Adapter(SearchProvider.class, clazz);
  }

  private static final SearchTarget [] TARGETS = new SearchTarget [] {
    create(org.netbeans.modules.xml.schema.model.SchemaComponent.class),
    create(org.netbeans.modules.xml.schema.model.Annotation.class),
    create(org.netbeans.modules.xml.schema.model.AppInfo.class),
    create(org.netbeans.modules.xml.schema.model.Attribute.class),
    create(org.netbeans.modules.xml.schema.model.BoundaryFacet.class),
    create(org.netbeans.modules.xml.schema.model.Choice.class),
    create(org.netbeans.modules.xml.schema.model.ComplexContent.class),
    create(org.netbeans.modules.xml.schema.model.ComplexContentDefinition.class),
    create(org.netbeans.modules.xml.schema.model.ComplexContentRestriction.class),
    create(org.netbeans.modules.xml.schema.model.ComplexExtension.class),
    create(org.netbeans.modules.xml.schema.model.ComplexExtensionDefinition.class),
    create(org.netbeans.modules.xml.schema.model.ComplexType.class),
    create(org.netbeans.modules.xml.schema.model.ComplexTypeDefinition.class),
    create(org.netbeans.modules.xml.schema.model.Constraint.class),
    create(org.netbeans.modules.xml.schema.model.Documentation.class),
    create(org.netbeans.modules.xml.schema.model.Element.class),
    create(org.netbeans.modules.xml.schema.model.Enumeration.class),
    create(org.netbeans.modules.xml.schema.model.Extension.class),
    create(org.netbeans.modules.xml.schema.model.Field.class),
    create(org.netbeans.modules.xml.schema.model.FractionDigits.class),
    create(org.netbeans.modules.xml.schema.model.GlobalAttribute.class),
    create(org.netbeans.modules.xml.schema.model.GlobalAttributeGroup.class),
    create(org.netbeans.modules.xml.schema.model.GlobalComplexType.class),
    create(org.netbeans.modules.xml.schema.model.GlobalElement.class),
    create(org.netbeans.modules.xml.schema.model.GlobalGroup.class),
    create(org.netbeans.modules.xml.schema.model.GlobalSimpleType.class),
    create(org.netbeans.modules.xml.schema.model.GlobalType.class),
    create(org.netbeans.modules.xml.schema.model.Import.class),
    create(org.netbeans.modules.xml.schema.model.Include.class),
    create(org.netbeans.modules.xml.schema.model.Key.class),
    create(org.netbeans.modules.xml.schema.model.Length.class),
    create(org.netbeans.modules.xml.schema.model.LengthFacet.class),
    create(org.netbeans.modules.xml.schema.model.List.class),
    create(org.netbeans.modules.xml.schema.model.LocalAttribute.class),
    create(org.netbeans.modules.xml.schema.model.LocalAttributeContainer.class),
    create(org.netbeans.modules.xml.schema.model.LocalComplexType.class),
    create(org.netbeans.modules.xml.schema.model.LocalElement.class),
    create(org.netbeans.modules.xml.schema.model.LocalGroupDefinition.class),
    create(org.netbeans.modules.xml.schema.model.LocalSimpleType.class),
    create(org.netbeans.modules.xml.schema.model.LocalType.class),
    create(org.netbeans.modules.xml.schema.model.MaxExclusive.class),
    create(org.netbeans.modules.xml.schema.model.MaxInclusive.class),
    create(org.netbeans.modules.xml.schema.model.MaxLength.class),
    create(org.netbeans.modules.xml.schema.model.MinExclusive.class),
    create(org.netbeans.modules.xml.schema.model.MinInclusive.class),
    create(org.netbeans.modules.xml.schema.model.MinLength.class),
    create(org.netbeans.modules.xml.schema.model.Notation.class),
    create(org.netbeans.modules.xml.schema.model.Pattern.class),
    create(org.netbeans.modules.xml.schema.model.Redefine.class),
    create(org.netbeans.modules.xml.schema.model.Schema.class),
    create(org.netbeans.modules.xml.schema.model.Selector.class),
    create(org.netbeans.modules.xml.schema.model.Sequence.class),
    create(org.netbeans.modules.xml.schema.model.SequenceDefinition.class),
    create(org.netbeans.modules.xml.schema.model.SimpleContent.class),
    create(org.netbeans.modules.xml.schema.model.SimpleContentDefinition.class),
    create(org.netbeans.modules.xml.schema.model.SimpleContentRestriction.class),
    create(org.netbeans.modules.xml.schema.model.SimpleExtension.class),
    create(org.netbeans.modules.xml.schema.model.SimpleRestriction.class),
    create(org.netbeans.modules.xml.schema.model.SimpleType.class),
    create(org.netbeans.modules.xml.schema.model.SimpleTypeDefinition.class),
    create(org.netbeans.modules.xml.schema.model.SimpleTypeRestriction.class),
    create(org.netbeans.modules.xml.schema.model.TotalDigits.class),
    create(org.netbeans.modules.xml.schema.model.Union.class),
    create(org.netbeans.modules.xml.schema.model.Unique.class),
  };

  private Object myView;
}
