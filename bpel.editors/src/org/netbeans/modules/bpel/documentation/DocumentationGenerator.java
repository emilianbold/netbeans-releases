/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.bpel.documentation;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.xml.namespace.QName;

import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.Documentation;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;

import org.netbeans.modules.reportgenerator.api.Report;
import org.netbeans.modules.reportgenerator.api.ReportAttribute;
import org.netbeans.modules.reportgenerator.api.ReportBody;
import org.netbeans.modules.reportgenerator.spi.ReportCookie;
import org.netbeans.modules.reportgenerator.api.ReportElement;
import org.netbeans.modules.reportgenerator.api.ReportElementFactory;
import org.netbeans.modules.reportgenerator.api.ReportException;
import org.netbeans.modules.reportgenerator.api.ReportSection;

import org.netbeans.modules.bpel.editors.api.EditorUtil;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.11.06
 */
public class DocumentationGenerator implements ReportCookie {

    public DocumentationGenerator(DataObject dataObject, JComponent canvas) {
        myDataObject = dataObject;
        myCanvas = canvas;
    }

    public Report generateReport() {
        try {
            myFactory = ReportElementFactory.getDefault();

            if (myFactory == null) {
                return null;
            }
            return createReport(EditorUtil.getBpelModel(myDataObject).getProcess(), myFactory.createReport());
        } catch (ReportException e) {
            ErrorManager.getDefault().notify(e);
        }
        return null;
    }

    private Report createReport(Process process, Report report) throws ReportException {
        if (process == null) {
            return report;
        }
        // description
        report.setName(i18n("LBL_Report_of", process.getName())); // NOI18N
        String description = process.getDocumentation();

        if (description == null) {
            description = i18n("LBL_Report_for", myDataObject.getPrimaryFile().getNameExt()); // NOI18N
        }
        report.setDescription(description);

        // image
        report.setOverViewImage(createImage());

        // summary
        ReportAttribute attribute;
        File file = FileUtil.toFile(myDataObject.getPrimaryFile());

        attribute = myFactory.createReportAttribute();
        attribute.setName(i18n("LBL_Name")); // NOI18N
        attribute.setValue(myDataObject.getName());
        report.addAttribute(attribute);

        attribute = myFactory.createReportAttribute();
        attribute.setName(i18n("LBL_Location")); // NOI18N
        attribute.setValue(file.getAbsolutePath());
        report.addAttribute(attribute);

        attribute = myFactory.createReportAttribute();
        attribute.setName(i18n("LBL_Size")); // NOI18N
        attribute.setValue(i18n("LBL_Bytes", Long.toString(file.length()))); // NOI18N
        report.addAttribute(attribute);

        attribute = myFactory.createReportAttribute();
        attribute.setName(i18n("LBL_Created")); // NOI18N
        attribute.setValue(new Date(System.currentTimeMillis()));
        report.addAttribute(attribute);

        attribute = myFactory.createReportAttribute();
        attribute.setName(i18n("LBL_Last_Modified")); // NOI18N
        attribute.setValue(new Date(file.lastModified()));
        report.addAttribute(attribute);

        // body
        ReportBody body = myFactory.createReportBody();
        report.setBody(body);
        ReportSection section;

        // imports
        section = createSection("LBL_Import", "LBL_Section_Import"); // NOI18N
        fillImport(process, body, section);

        // partner links
        section = createSection("LBL_Partner_Link", "LBL_Section_Partner_Link"); // NOI18N
        fillPartnerLink(process, body, section);

        // variables
        section = createSection("LBL_Variable", "LBL_Section_Variable"); // NOI18N
        fillVariable(process, body, section);

        // correlation sets
        section = createSection("LBL_Correlation_Set", "LBL_Section_Correlation_Set");//NOI18N
        fillCorrelationSet(process, body, section);

        // another elements
        section = createSection("LBL_Element", "LBL_Section_Element"); // NOI18N
        body.addReportSection(section);
        travelElement(process, section);

        return report;
    }

    private ReportSection createSection(String name, String description) {
        ReportSection section = myFactory.createReportSection();
        section.setName(i18n(name));
        section.setDescription(i18n(description));
        return section;
    }

    private void fillImport(Process process, ReportBody body, ReportSection section) {
        Import[] imports = process.getImports();

        if (imports == null || imports.length == 0) {
            return;
        }
        for (Import imp : imports) {
            fillElement(imp, section);
        }
        body.addReportSection(section);
    }

    private void fillPartnerLink(Process process, ReportBody body, ReportSection section) {
        PartnerLinkContainer container = process.getPartnerLinkContainer();

        if (container == null) {
            return;
        }
        PartnerLink[] partners = container.getPartnerLinks();

        if (partners == null || partners.length == 0) {
            return;
        }
        for (PartnerLink partner : partners) {
            fillElement(partner, section);
        }
        body.addReportSection(section);
    }

    private void fillVariable(Process process, ReportBody body, ReportSection section) {
        VariableContainer container = process.getVariableContainer();

        if (container == null) {
            return;
        }
        Variable[] variables = container.getVariables();

        if (variables == null || variables.length == 0) {
            return;
        }
        for (Variable variable : variables) {
            fillElement(variable, section);
        }
        body.addReportSection(section);
    }

    private void fillCorrelationSet(Process process, ReportBody body, ReportSection section) {
        CorrelationSetContainer container = process.getCorrelationSetContainer();

        if (container == null) {
            return;
        }
        CorrelationSet[] correlations = container.getCorrelationSets();

        if (correlations == null || correlations.length == 0) {
            return;
        }
        for (CorrelationSet correlation : correlations) {
            fillElement(correlation, section);
        }
        body.addReportSection(section);
    }

    private void travelElement(Object object, ReportSection section) {
        if (!(object instanceof BpelEntity)) {
            return;
        }
        BpelEntity entity = (BpelEntity) object;

        if (skipElement(entity)) {
            return;
        }
        if (!(object instanceof Process)) {
            fillElement(entity, section);
        }
        List children = entity.getChildren();

        for (Object child : children) {
            travelElement(child, section);
        }
    }

    private boolean skipElement(BpelEntity entity) {
       return
           entity instanceof Import ||
           entity instanceof CorrelationSet ||
           entity instanceof CorrelationSetContainer ||
           entity instanceof CorrelationContainer ||
           entity instanceof PartnerLink ||
           entity instanceof PartnerLinkContainer ||
           entity instanceof Variable ||
           entity instanceof VariableContainer ||
           entity instanceof Documentation ||
           entity instanceof PatternedCorrelationContainer;
    }

    private void fillElement(BpelEntity entity, ReportSection section) {
        ReportElement element = myFactory.createReportElement();
        Icon icon = EditorUtil.getIcon(entity);

        if (icon instanceof ImageIcon) {
            element.setImage(((ImageIcon) icon).getImage());
        }
        element.setName(getInfo(entity));

        if (entity instanceof ExtensibleElements) {
            String documentation = ((ExtensibleElements) entity).getDocumentation();

            if (documentation != null) {
                element.setDescription(documentation);
            }
        }
        fillAttributes(entity, element);
        section.addReportElement(element);
    }

    private void fillAttributes(BpelEntity entity, ReportElement element) {
        AbstractDocumentComponent component = (AbstractDocumentComponent) entity;
        Map map = component.getAttributeMap();
        Iterator iterator = map.keySet().iterator();

        while (iterator.hasNext()) {
            QName name = (QName) iterator.next();
            String value = (String) map.get(name);

            if (value == null || value.length() == 0) {
                continue;
            }
            ReportAttribute attribute = myFactory.createReportAttribute();
            attribute.setName(name.toString());
            attribute.setValue(value);
            element.addAttribute(attribute);
        }
    }

    private Image createImage() {
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = environment.getDefaultScreenDevice();
        GraphicsConfiguration configuration = device.getDefaultConfiguration();
        BufferedImage image = configuration.createCompatibleImage(myCanvas.getWidth(), myCanvas.getHeight(), Transparency.BITMASK);
        myCanvas.print(image.createGraphics());
        return image;
    }

    private String getInfo(BpelEntity entity) {
        String info = getType(entity);
        String name = getName(entity);

        if (name != null) {
            info += " '" + ((Named) entity).getName() + "'"; // NOI18N
        } else if (entity instanceof ContentElement) {
            String content = ((ContentElement) entity).getContent();

            if (content != null && content.length() > 0) {
                info += ": " + content; // NOI18N
            }
        }
        return info;
    }

    private String getType(BpelEntity entity) {
        String type = entity.getElementType().getName();
        int k = type.lastIndexOf("."); // NOI18N

        if (k == -1) {
            return type;
        }
        return type.substring(k + 1);
    }

    private String getName(BpelEntity entity) {
        if (!(entity instanceof Named)) {
            return null;
        }
        return ((Named) entity).getName();
    }

    private String i18n(String key) {
        return org.netbeans.modules.xml.misc.UI.i18n(DocumentationGenerator.class, key);
    }

    private String i18n(String key, String param) {
        return org.netbeans.modules.xml.misc.UI.i18n(DocumentationGenerator.class, key, param);
    }

    private JComponent myCanvas;
    private DataObject myDataObject;
    private ReportElementFactory myFactory;
}
