package org.netbeans.modules.maven.embedder;

import java.io.File;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.build.model.ModelAndFile;
import org.apache.maven.project.workspace.ProjectWorkspace;
import org.apache.maven.workspace.MavenWorkspaceStore;

public class NbMavenWorkspaceStore
    implements MavenWorkspaceStore, LogEnabled
{

    private Map<String, TimedWeakReference<Map>> caches = new HashMap<String, TimedWeakReference<Map>>();
//    private Logger logger;

    public synchronized void clear() {
        @SuppressWarnings("unchecked")
        Map<String, ModelAndFile> cache1 = getWorkspaceCache(ProjectWorkspace.MODEL_AND_FILE_BYFILE_KEY);
        if (cache1 != null) {
            Iterator<Map.Entry<String, ModelAndFile>> it1 = cache1.entrySet().iterator();
            while (it1.hasNext()) {
                Map.Entry<String, ModelAndFile> ent = it1.next();
                if (ent == null || ent.getValue() == null) {
                    continue;
                }
                File f = ent.getValue().getFile();
                // remove entries based on workspace projects.
                if (f != null && f.getName().equals("pom.xml")) { //NOI18N
                    it1.remove();
                }
            }
        }
        @SuppressWarnings("unchecked")
        Map<String, ModelAndFile> cache2 = getWorkspaceCache(ProjectWorkspace.MODEL_AND_FILE_BYGAV_KEY);
        if (cache2 != null) {
            Iterator<Map.Entry<String, ModelAndFile>> it2 = cache2.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry<String, ModelAndFile> ent = it2.next();
                if (ent == null || ent.getValue() == null) {
                    continue;
                }
                File f = ent.getValue().getFile();
                // remove entries based on workspace projects.
                if (f != null && f.getName().equals("pom.xml")) { //NOI18N
                    it2.remove();
                }
            }
        }
        @SuppressWarnings("unchecked")
        Map<String, MavenProject> cache3 = getWorkspaceCache(ProjectWorkspace.PROJECT_INSTANCE_BYFILE_KEY);
        if (cache3 != null) {
            Iterator<Map.Entry<String, MavenProject>> it3 = cache3.entrySet().iterator();
            while (it3.hasNext()) {
                Map.Entry<String, MavenProject> ent = it3.next();
                if (ent == null || ent.getKey() == null) {
                    continue;
                }
                // remove entries based on workspace projects.
                if (ent.getKey().endsWith("pom.xml")) { //NOI18N
                    it3.remove();
                }
            }
        }
        @SuppressWarnings("unchecked")
        Map<String, MavenProject> cache4 = getWorkspaceCache(ProjectWorkspace.PROJECT_INSTANCE_BYGAV_KEY);
        if (cache4 != null) {
            Iterator<Map.Entry<String, MavenProject>> it4 = cache4.entrySet().iterator();
            while (it4.hasNext()) {
                Map.Entry<String, MavenProject> ent = it4.next();
                // remove entries based on workspace projects.
                if (ent == null || ent.getValue() == null) {
                    continue;
                }
                File f = ent.getValue().getFile();
                if (f != null && f.getName().equals("pom.xml")) { //NOI18N
                    it4.remove();
                }
            }
        }
    }

    //only called from netbeans code on occasions like when a pom file was changed or build was performed
    public synchronized void doManualClear() {
        caches.clear();
    }

    public synchronized Map getWorkspaceCache( String cacheType )
    {
        TimedWeakReference<Map> val = caches.get( cacheType );
        Map result = null;
        if ( val != null ) {
            result = val.get();
        }
        if (result == null) {
            result = new HashMap();
            initWorkspaceCache( cacheType, result );
        }

//        getLogger().debug( "Retrieving workspace cache for: " + cacheType + " (" + result.size() + " entries)" );
        return result;
    }

    public synchronized void initWorkspaceCache( String cacheType,
                           Map cache )
    {
//        getLogger().debug( "Initializing workspace cache for: " + cacheType + " (" + cache.size() + " entries)" );
        TimedWeakReference<Map> ref = new TimedWeakReference<Map>(cache);
        caches.put( cacheType, ref );
    }

//    protected Logger getLogger()
//    {
//        if ( logger == null )
//        {
//            logger = new ConsoleLogger ( Logger.LEVEL_INFO, "internal" );
//        }
//
//        return logger;
//    }

    public void enableLogging( Logger logger )
    {
//        this.logger = logger;
    }

}
