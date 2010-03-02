/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mercurial.options;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.Collection;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.MercurialAnnotator;
import org.netbeans.modules.mercurial.MercurialVCS;
import org.netbeans.modules.versioning.util.OptionsPanelColorProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author ondra
 */
@ServiceProviders({@ServiceProvider(service=OptionsPanelColorProvider.class), @ServiceProvider(service=AnnotationColorProvider.class)})
public class AnnotationColorProvider extends OptionsPanelColorProvider {

    private static String name;
    private static AnnotationColorProvider INSTANCE;

    public final AnnotationFormat UP_TO_DATE_FILE = createAnnotationFormat("uptodate", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_UpToDate"), null, false); //NOI18N
    public final AnnotationFormat NEW_LOCALLY_FILE = createAnnotationFormat("newLocally", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_NewLocally"), new Color(0, 0x80, 0), false); //NOI18N
    public final AnnotationFormat NEW_LOCALLY_FILE_TOOLTIP = createAnnotationFormat("newLocallyTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_NewLocally"), new Color(0, 0x80, 0), true); //NOI18N
    public final AnnotationFormat ADDED_LOCALLY_FILE = createAnnotationFormat("addedLocally", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_AddedLocally"), new Color(0, 0x80, 0), false); //NOI18N
    public final AnnotationFormat ADDED_LOCALLY_FILE_TOOLTIP = createAnnotationFormat("addedLocallyTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_AddedLocally"), new Color(0, 0x80, 0), true); //NOI18N
    public final AnnotationFormat COPIED_LOCALLY_FILE = createAnnotationFormat("copiedLocally", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_AddedLocallyCopied"), new Color(0, 0x80, 0), false); //NOI18N
    public final AnnotationFormat COPIED_LOCALLY_FILE_TOOLTIP = createAnnotationFormat("copiedLocallyTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_AddedLocallyCopied"), new Color(0, 0x80, 0), true); //NOI18N
    public final AnnotationFormat MODIFIED_LOCALLY_FILE = createAnnotationFormat("modifiedLocally", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_ModifiedLocally"), new Color(0, 0, 0xff), false); //NOI18N
    public final AnnotationFormat MODIFIED_LOCALLY_FILE_TOOLTIP = createAnnotationFormat("modifiedLocallyTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_ModifiedLocally"), new Color(0, 0, 0xff), true); //NOI18N
    public final AnnotationFormat REMOVED_LOCALLY_FILE = createAnnotationFormat("removedLocally", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_RemovedLocally"), new Color(0x99, 0x99, 0x99), false); //NOI18N
    public final AnnotationFormat REMOVED_LOCALLY_FILE_TOOLTIP = createAnnotationFormat("removedLocallyTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_RemovedLocally"), new Color(0x99, 0x99, 0x99), true); //NOI18N
    public final AnnotationFormat DELETED_LOCALLY_FILE = createAnnotationFormat("deletedLocally", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_DeletedLocally"), new Color(0x99, 0x99, 0x99), false); //NOI18N
    public final AnnotationFormat DELETED_LOCALLY_FILE_TOOLTIP = createAnnotationFormat("deletedLocallyTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_DeletedLocally"), new Color(0x99, 0x99, 0x99), true); //NOI18N
    public final AnnotationFormat EXCLUDED_FILE = createAnnotationFormat("excluded", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_Excluded"), new Color(0x99, 0x99, 0x99), false); //NOI18N
    public final AnnotationFormat EXCLUDED_FILE_TOOLTIP = createAnnotationFormat("excludedTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_Excluded"), new Color(0x99, 0x99, 0x99), true); //NOI18N
    public final AnnotationFormat CONFLICT_FILE = createAnnotationFormat("conflict", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_Conflict"), new Color(0xff, 0, 0), false); //NOI18N
    public final AnnotationFormat CONFLICT_FILE_TOOLTIP = createAnnotationFormat("conflictTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_Conflict"), new Color(0xff, 0, 0), true); //NOI18N

    public static synchronized AnnotationColorProvider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = Lookup.getDefault().lookup(AnnotationColorProvider.class);
            if (INSTANCE == null) {
                INSTANCE = new AnnotationColorProvider();
            }
            INSTANCE.initColors();
        }
        return INSTANCE;
    }

    @Override
    public String getName() {
        if (name == null) {
            MercurialVCS vcs = Lookup.getDefault().lookup(MercurialVCS.class);
            name = (String)vcs.getProperty(MercurialVCS.PROP_DISPLAY_NAME);
        }
        return name;
    }

    @Override
    protected Color getSavedColor (String key, Color defaultColor) {
        return HgModuleConfig.getDefault().getColor(key, defaultColor);
    }

    @Override
    protected MessageFormat createFormat (Color color, boolean isTooltip) {
        StringBuilder annotationFormatString = new StringBuilder("{0}"); //NOI18N
        if (color != null) {
            annotationFormatString = new StringBuilder("<font color=\"") //NOI18N
                    .append(to2Hex(color.getRed())).append(to2Hex(color.getGreen())).append(to2Hex(color.getBlue())).append("\"").append(">{0}</font>"); //NOI18N
        }
        if (!isTooltip) {
            annotationFormatString.append("<font color=\"#999999\">{1}</font>"); //NOI18N
        }
        return new MessageFormat(annotationFormatString.toString());
    }

    @Override
    protected void saveColors (Collection<AnnotationFormat> colors) {
        for (AnnotationFormat af : colors) {
            if (af != null) {
                HgModuleConfig.getDefault().setColor(getColorKey(af.getKey()), af.getActualColor());
            }
        }
        Mercurial.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                Mercurial.getInstance().refreshAllAnnotations();
            }
        });
    }

    private void initColors() {
        putColor(ADDED_LOCALLY_FILE);
        putColor(CONFLICT_FILE);
        putColor(COPIED_LOCALLY_FILE);
        putColor(DELETED_LOCALLY_FILE);
        putColor(EXCLUDED_FILE);
        putColor(MODIFIED_LOCALLY_FILE);
        putColor(NEW_LOCALLY_FILE);
        putColor(REMOVED_LOCALLY_FILE);
    }
}
