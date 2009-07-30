
package org.netbeans.modules.bugtracking;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoriesSupport {

    public final static String EVENT_REPOSITORIES_CHANGED = "bugtracking.repositories.changed"; // NOI18N
    
    private static RepositoriesSupport instance;
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private RepositoriesSupport(){}

    public static RepositoriesSupport getInstance() {
        if(instance == null) {
            instance = new RepositoriesSupport();
        }
        return instance;
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void fireRepositoriesChanged() {
        // XXX should be connectors responsibility;
        //     for now its ok as nobody listens for events on a particular connector
        changeSupport.firePropertyChange(EVENT_REPOSITORIES_CHANGED, null, null);
    }
}
