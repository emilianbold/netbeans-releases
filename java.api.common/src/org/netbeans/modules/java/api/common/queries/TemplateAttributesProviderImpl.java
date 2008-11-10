package org.netbeans.modules.java.api.common.queries;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Default implementation of {@link CreateFromTemplateAttributesProvider}.
 *
 * @author Andrei Badea
 */
class TemplateAttributesProviderImpl implements CreateFromTemplateAttributesProvider {

    private final AntProjectHelper helper;
    private final FileEncodingQueryImplementation encodingQuery;

    public TemplateAttributesProviderImpl(AntProjectHelper helper, FileEncodingQueryImplementation encodingQuery) {
        super();
        this.helper = helper;
        this.encodingQuery = encodingQuery;
    }

    public Map<String, ?> attributesFor(DataObject template, DataFolder target, String name) {
        Map<String, String> values = new HashMap<String, String>();
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String license = props.getProperty("project.license"); // NOI18N
        if (license != null) {
            values.put("license", license); // NOI18N
        }
        Charset charset = encodingQuery.getEncoding(target.getPrimaryFile());
        String encoding = (charset != null) ? charset.name() : null;
        if (encoding != null) {
            values.put("encoding", encoding); // NOI18N
        }
        try {
            Project prj = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
            ProjectInformation info = prj.getLookup().lookup(ProjectInformation.class);
            if (info != null) {
                String pname = info.getName();
                if (pname != null) {
                    values.put("name", pname);// NOI18N
                }
                String pdname = info.getDisplayName();
                if (pdname != null) {
                    values.put("displayName", pdname);// NOI18N
                }
            }
        } catch (Exception ex) {
            //not really important, just log.
            Logger.getLogger(TemplateAttributesProviderImpl.class.getName()).log(Level.FINE, "", ex);
        }

        if (values.size() == 0) {
            return null;
        } else {
            return Collections.singletonMap("project", values); // NOI18N
        }
    }
}
