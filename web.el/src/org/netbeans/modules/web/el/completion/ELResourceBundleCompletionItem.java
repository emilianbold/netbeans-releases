/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.web.el.completion;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Erno Mononen
 */
final class ELResourceBundleCompletionItem extends DefaultCompletionProposal {

    private static final String ICON_PATH = "org/netbeans/modules/web/el/completion/resources/propertiesLocale.gif";//NOI18N
    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    private final String bundle;

    public ELResourceBundleCompletionItem(String bundle) {
        this.bundle = bundle;
    }

    @Override
    public String getName() {
        return bundle;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.OTHER;
    }

    @Override
    public int getSortPrioOverride() {
        return 20;
    }

    @Override
    public ElementHandle getElement() {
        return null;
    }

    @Override
    public ImageIcon getIcon() {
        return ImageUtilities.loadImageIcon(ICON_PATH, false);
    }

    @Override
    public String getCustomInsertTemplate() {
        StringBuilder result = new StringBuilder();
        result.append(getInsertPrefix())
                .append("['")
                .append("${cursor}")
                .append("']");
        scheduleShowingCompletion();
        return result.toString();
    }

    private static void scheduleShowingCompletion() {
        service.schedule(new Runnable() {

            @Override
            public void run() {
                Completion.get().showCompletion();
            }
        }, 250, TimeUnit.MILLISECONDS);
    }
}
