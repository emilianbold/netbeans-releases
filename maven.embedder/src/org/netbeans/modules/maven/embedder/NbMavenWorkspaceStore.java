package org.netbeans.modules.maven.embedder;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import java.util.HashMap;
import java.util.Map;
import org.apache.maven.workspace.MavenWorkspaceStore;

public class NbMavenWorkspaceStore
    implements MavenWorkspaceStore, LogEnabled
{

    private Map<String, TimedWeakReference<Map>> caches = new HashMap<String, TimedWeakReference<Map>>();
//    private Logger logger;

    public void clear() {

    //no manual clearing, it happens automatically when the reference is freed.

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
