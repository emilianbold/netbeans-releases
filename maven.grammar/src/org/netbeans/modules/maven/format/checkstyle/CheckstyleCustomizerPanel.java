/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.maven.format.checkstyle;

import javax.swing.JComponent;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class CheckstyleCustomizerPanel implements ProjectCustomizer.CompositeCategoryProvider {

    public ProjectCustomizer.Category createCategory(Lookup look) {
        return ProjectCustomizer.Category.create(
                "checkstyle",
                NbBundle.getMessage(CheckstyleCustomizerPanel.class, "TIT_CheckStyle"),
                null,
                (ProjectCustomizer.Category[])null);
    }

    public JComponent createComponent(ProjectCustomizer.Category cat, Lookup look) {
        ModelHandle handle = look.lookup(ModelHandle.class);
        return new CheckstylePanel(handle, cat);
    }


}
