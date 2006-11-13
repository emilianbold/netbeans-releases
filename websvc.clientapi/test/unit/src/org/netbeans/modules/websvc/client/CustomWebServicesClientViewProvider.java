package org.netbeans.modules.websvc.client;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.api.client.WebServicesClientView;
import org.netbeans.modules.websvc.spi.client.WebServicesClientViewFactory;
import org.netbeans.modules.websvc.spi.client.WebServicesClientViewImpl;
import org.netbeans.modules.websvc.spi.client.WebServicesClientViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Lukas Jungmann
 */
public class CustomWebServicesClientViewProvider implements WebServicesClientViewProvider {
    
    private Map<FileObject, WebServicesClientView> cache = new HashMap<FileObject, WebServicesClientView>();
    
    /** Creates a new instance of WebServicesClientViewProvider */
    public CustomWebServicesClientViewProvider() {
    }
    
    public WebServicesClientView findWebServicesClientView(FileObject file) {
        if (file.getExt().equals("ws")) {
            WebServicesClientView em = cache.get(file.getParent());
            if (em == null) {
                em = WebServicesClientViewFactory.createWebServicesClientView(new CustomWebServicesClientViewImpl(file));
                cache.put(file.getParent(), em);
            }
            return em;
        }
        return null;
    }
    
    private static final class CustomWebServicesClientViewImpl implements WebServicesClientViewImpl {
        private FileObject fo;
        
        CustomWebServicesClientViewImpl(FileObject fo) {
            this.fo = fo;
        }
        
        public Node createWebServiceClientView(Project p) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public Node createWebServiceClientView(SourceGroup sg) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public Node createWebServiceClientView(FileObject wsdlFolder) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
