package org.netbeans.modules.xml.xam;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.locator.api.ModelSource;

/**
 *
 * @author Chris Webster
 * @author Nam Nguyen
 */
public abstract class AbstractModelFactory<M extends DocumentModel> {
    
    private Map<Document, WeakReference<M>> cachedModels = new WeakHashMap<Document,WeakReference<M>>();
    
    protected abstract M createModel(ModelSource source);
    
    public M getModel(ModelSource source) throws IOException {
        if (source == null) {
            return null;
        }
        Document doc = source.getDocument();
        WeakReference<M> modelRef = cachedModels.get(doc);
        M model = (modelRef == null ? null : modelRef.get());
        if (model == null && doc != null) {
            model = createModel(source);
            cachedModels.put(doc, new WeakReference<M>(model));
            model.sync();
        }
        return model;
    }
}
