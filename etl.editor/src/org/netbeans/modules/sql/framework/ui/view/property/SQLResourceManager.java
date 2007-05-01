package org.netbeans.modules.sql.framework.ui.view.property;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.netbeans.modules.sql.framework.ui.editor.property.IResource;
import org.openide.util.NbBundle;


/**
 */
public class SQLResourceManager implements IResource {

    private ResourceBundle bundle;

    /** Creates a new instance of ResourceManager */
    public SQLResourceManager() {
        try {
            bundle = NbBundle.getBundle(SQLResourceManager.class);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
    }

    public String getLocalizedValue(String key) {
        if (bundle != null) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }
}

