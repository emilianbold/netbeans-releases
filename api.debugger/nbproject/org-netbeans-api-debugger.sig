#Signature file v4.0
#Version 1.17.1

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected Enum(java.lang.String,int)
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} valueOf(java.lang.Class<{%%0}>,java.lang.String)
supr java.lang.Object
hfds name,ordinal

CLSS public java.lang.Object
cons public Object()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public abstract interface java.lang.annotation.Annotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation java.lang.annotation.Documented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Retention
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.RetentionPolicy value()

CLSS public abstract interface !annotation java.lang.annotation.Target
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.ElementType[] value()

CLSS public abstract interface java.util.EventListener

CLSS public final org.netbeans.api.debugger.ActionsManager
fld public final static java.lang.Object ACTION_CONTINUE
fld public final static java.lang.Object ACTION_FIX
fld public final static java.lang.Object ACTION_KILL
fld public final static java.lang.Object ACTION_MAKE_CALLEE_CURRENT
fld public final static java.lang.Object ACTION_MAKE_CALLER_CURRENT
fld public final static java.lang.Object ACTION_PAUSE
fld public final static java.lang.Object ACTION_POP_TOPMOST_CALL
fld public final static java.lang.Object ACTION_RESTART
fld public final static java.lang.Object ACTION_RUN_INTO_METHOD
fld public final static java.lang.Object ACTION_RUN_TO_CURSOR
fld public final static java.lang.Object ACTION_START
fld public final static java.lang.Object ACTION_STEP_INTO
fld public final static java.lang.Object ACTION_STEP_OPERATION
fld public final static java.lang.Object ACTION_STEP_OUT
fld public final static java.lang.Object ACTION_STEP_OVER
fld public final static java.lang.Object ACTION_TOGGLE_BREAKPOINT
meth public final boolean isEnabled(java.lang.Object)
meth public final org.openide.util.Task postAction(java.lang.Object)
meth public final void doAction(java.lang.Object)
meth public void addActionsManagerListener(java.lang.String,org.netbeans.api.debugger.ActionsManagerListener)
meth public void addActionsManagerListener(org.netbeans.api.debugger.ActionsManagerListener)
meth public void destroy()
meth public void removeActionsManagerListener(java.lang.String,org.netbeans.api.debugger.ActionsManagerListener)
meth public void removeActionsManagerListener(org.netbeans.api.debugger.ActionsManagerListener)
supr java.lang.Object
hfds actionListener,actionProviders,actionProvidersLock,aps,destroy,doiingDo,lazyListeners,listener,listeners,listerersLoaded,lookup
hcls AsynchActionTask,MyActionListener

CLSS public org.netbeans.api.debugger.ActionsManagerAdapter
cons public ActionsManagerAdapter()
intf org.netbeans.api.debugger.ActionsManagerListener
meth public void actionPerformed(java.lang.Object)
meth public void actionStateChanged(java.lang.Object,boolean)
supr java.lang.Object

CLSS public abstract interface org.netbeans.api.debugger.ActionsManagerListener
fld public final static java.lang.String PROP_ACTION_PERFORMED = "actionPerformed"
fld public final static java.lang.String PROP_ACTION_STATE_CHANGED = "actionStateChanged"
intf java.util.EventListener
meth public abstract void actionPerformed(java.lang.Object)
meth public abstract void actionStateChanged(java.lang.Object,boolean)

CLSS public abstract org.netbeans.api.debugger.Breakpoint
cons public Breakpoint()
fld public final static java.lang.String PROP_DISPOSED = "disposed"
fld public final static java.lang.String PROP_ENABLED = "enabled"
fld public final static java.lang.String PROP_GROUP_NAME = "groupName"
fld public final static java.lang.String PROP_HIT_COUNT_FILTER = "hitCountFilter"
fld public final static java.lang.String PROP_VALIDITY = "validity"
innr public final static !enum HIT_COUNT_FILTERING_STYLE
innr public final static !enum VALIDITY
meth protected final void setValidity(org.netbeans.api.debugger.Breakpoint$VALIDITY,java.lang.String)
meth protected void dispose()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract boolean isEnabled()
meth public abstract void disable()
meth public abstract void enable()
meth public final int getHitCountFilter()
meth public final java.lang.String getValidityMessage()
meth public final org.netbeans.api.debugger.Breakpoint$HIT_COUNT_FILTERING_STYLE getHitCountFilteringStyle()
meth public final org.netbeans.api.debugger.Breakpoint$VALIDITY getValidity()
meth public final void setHitCountFilter(int,org.netbeans.api.debugger.Breakpoint$HIT_COUNT_FILTERING_STYLE)
meth public java.lang.String getGroupName()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void setGroupName(java.lang.String)
supr java.lang.Object
hfds groupName,hitCountFilter,hitCountFilteringStyle,pcs,validity,validityMessage

CLSS public final static !enum org.netbeans.api.debugger.Breakpoint$HIT_COUNT_FILTERING_STYLE
fld public final static org.netbeans.api.debugger.Breakpoint$HIT_COUNT_FILTERING_STYLE EQUAL
fld public final static org.netbeans.api.debugger.Breakpoint$HIT_COUNT_FILTERING_STYLE GREATER
fld public final static org.netbeans.api.debugger.Breakpoint$HIT_COUNT_FILTERING_STYLE MULTIPLE
meth public static org.netbeans.api.debugger.Breakpoint$HIT_COUNT_FILTERING_STYLE valueOf(java.lang.String)
meth public static org.netbeans.api.debugger.Breakpoint$HIT_COUNT_FILTERING_STYLE[] values()
supr java.lang.Enum<org.netbeans.api.debugger.Breakpoint$HIT_COUNT_FILTERING_STYLE>

CLSS public final static !enum org.netbeans.api.debugger.Breakpoint$VALIDITY
fld public final static org.netbeans.api.debugger.Breakpoint$VALIDITY INVALID
fld public final static org.netbeans.api.debugger.Breakpoint$VALIDITY UNKNOWN
fld public final static org.netbeans.api.debugger.Breakpoint$VALIDITY VALID
meth public static org.netbeans.api.debugger.Breakpoint$VALIDITY valueOf(java.lang.String)
meth public static org.netbeans.api.debugger.Breakpoint$VALIDITY[] values()
supr java.lang.Enum<org.netbeans.api.debugger.Breakpoint$VALIDITY>

CLSS public final org.netbeans.api.debugger.DebuggerEngine
innr public Destructor
intf org.netbeans.spi.debugger.ContextProvider
meth public <%0 extends java.lang.Object> java.util.List<? extends {%%0}> lookup(java.lang.String,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} lookupFirst(java.lang.String,java.lang.Class<{%%0}>)
meth public org.netbeans.api.debugger.ActionsManager getActionsManager()
supr java.lang.Object
hfds actionsManager,lookup,privateLookup

CLSS public org.netbeans.api.debugger.DebuggerEngine$Destructor
cons public Destructor(org.netbeans.api.debugger.DebuggerEngine)
meth public void killEngine()
meth public void killLanguage(org.netbeans.api.debugger.Session,java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.api.debugger.DebuggerInfo
intf org.netbeans.spi.debugger.ContextProvider
meth public <%0 extends java.lang.Object> java.util.List<? extends {%%0}> lookup(java.lang.String,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} lookupFirst(java.lang.String,java.lang.Class<{%%0}>)
meth public static org.netbeans.api.debugger.DebuggerInfo create(java.lang.String,java.lang.Object[])
supr java.lang.Object
hfds lookup

CLSS public final org.netbeans.api.debugger.DebuggerManager
fld public final static java.lang.String PROP_BREAKPOINTS = "breakpoints"
fld public final static java.lang.String PROP_BREAKPOINTS_INIT = "breakpointsInit"
fld public final static java.lang.String PROP_CURRENT_ENGINE = "currentEngine"
fld public final static java.lang.String PROP_CURRENT_SESSION = "currentSession"
fld public final static java.lang.String PROP_DEBUGGER_ENGINES = "debuggerEngines"
fld public final static java.lang.String PROP_SESSIONS = "sessions"
fld public final static java.lang.String PROP_WATCHES = "watches"
fld public final static java.lang.String PROP_WATCHES_INIT = "watchesInit"
intf org.netbeans.spi.debugger.ContextProvider
meth public <%0 extends java.lang.Object> java.util.List<? extends {%%0}> lookup(java.lang.String,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} lookupFirst(java.lang.String,java.lang.Class<{%%0}>)
meth public org.netbeans.api.debugger.ActionsManager getActionsManager()
meth public org.netbeans.api.debugger.Breakpoint[] getBreakpoints()
meth public org.netbeans.api.debugger.DebuggerEngine getCurrentEngine()
meth public org.netbeans.api.debugger.DebuggerEngine[] getDebuggerEngines()
meth public org.netbeans.api.debugger.DebuggerEngine[] startDebugging(org.netbeans.api.debugger.DebuggerInfo)
meth public org.netbeans.api.debugger.Session getCurrentSession()
meth public org.netbeans.api.debugger.Session[] getSessions()
meth public org.netbeans.api.debugger.Watch createWatch(java.lang.String)
meth public org.netbeans.api.debugger.Watch[] getWatches()
meth public static org.netbeans.api.debugger.DebuggerManager getDebuggerManager()
meth public static org.netbeans.spi.debugger.ContextProvider join(org.netbeans.spi.debugger.ContextProvider,org.netbeans.spi.debugger.ContextProvider)
meth public void addBreakpoint(org.netbeans.api.debugger.Breakpoint)
meth public void addDebuggerListener(java.lang.String,org.netbeans.api.debugger.DebuggerManagerListener)
meth public void addDebuggerListener(org.netbeans.api.debugger.DebuggerManagerListener)
meth public void finishAllSessions()
meth public void removeAllWatches()
meth public void removeBreakpoint(org.netbeans.api.debugger.Breakpoint)
meth public void removeDebuggerListener(java.lang.String,org.netbeans.api.debugger.DebuggerManagerListener)
meth public void removeDebuggerListener(org.netbeans.api.debugger.DebuggerManagerListener)
meth public void setCurrentSession(org.netbeans.api.debugger.Session)
supr java.lang.Object
hfds actionsManager,breakpoints,breakpointsByClassLoaders,breakpointsInitialized,breakpointsInitializing,createdBreakpoints,currentEngine,currentSession,debuggerManager,engines,listeners,listenersLookupList,listenersMap,loadedListeners,lookup,sessionListener,sessions,watches,watchesInitialized
hcls SessionListener

CLSS public org.netbeans.api.debugger.DebuggerManagerAdapter
cons public DebuggerManagerAdapter()
intf org.netbeans.api.debugger.LazyDebuggerManagerListener
meth public java.lang.String[] getProperties()
meth public org.netbeans.api.debugger.Breakpoint[] initBreakpoints()
meth public void breakpointAdded(org.netbeans.api.debugger.Breakpoint)
meth public void breakpointRemoved(org.netbeans.api.debugger.Breakpoint)
meth public void engineAdded(org.netbeans.api.debugger.DebuggerEngine)
meth public void engineRemoved(org.netbeans.api.debugger.DebuggerEngine)
meth public void initWatches()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void sessionAdded(org.netbeans.api.debugger.Session)
meth public void sessionRemoved(org.netbeans.api.debugger.Session)
meth public void watchAdded(org.netbeans.api.debugger.Watch)
meth public void watchRemoved(org.netbeans.api.debugger.Watch)
supr java.lang.Object

CLSS public abstract interface org.netbeans.api.debugger.DebuggerManagerListener
intf java.beans.PropertyChangeListener
meth public abstract org.netbeans.api.debugger.Breakpoint[] initBreakpoints()
meth public abstract void breakpointAdded(org.netbeans.api.debugger.Breakpoint)
meth public abstract void breakpointRemoved(org.netbeans.api.debugger.Breakpoint)
meth public abstract void engineAdded(org.netbeans.api.debugger.DebuggerEngine)
meth public abstract void engineRemoved(org.netbeans.api.debugger.DebuggerEngine)
meth public abstract void initWatches()
meth public abstract void sessionAdded(org.netbeans.api.debugger.Session)
meth public abstract void sessionRemoved(org.netbeans.api.debugger.Session)
meth public abstract void watchAdded(org.netbeans.api.debugger.Watch)
meth public abstract void watchRemoved(org.netbeans.api.debugger.Watch)

CLSS public abstract org.netbeans.api.debugger.LazyActionsManagerListener
cons public LazyActionsManagerListener()
innr public abstract interface static !annotation Registration
meth protected abstract void destroy()
meth public abstract java.lang.String[] getProperties()
supr org.netbeans.api.debugger.ActionsManagerAdapter
hcls ContextAware

CLSS public abstract interface static !annotation org.netbeans.api.debugger.LazyActionsManagerListener$Registration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String path()

CLSS public abstract interface org.netbeans.api.debugger.LazyDebuggerManagerListener
intf org.netbeans.api.debugger.DebuggerManagerListener
meth public abstract java.lang.String[] getProperties()

CLSS public abstract org.netbeans.api.debugger.Properties
cons public Properties()
innr public abstract interface static Initializer
innr public abstract interface static Reader
meth public abstract boolean getBoolean(java.lang.String,boolean)
meth public abstract byte getByte(java.lang.String,byte)
meth public abstract char getChar(java.lang.String,char)
meth public abstract double getDouble(java.lang.String,double)
meth public abstract float getFloat(java.lang.String,float)
meth public abstract int getInt(java.lang.String,int)
meth public abstract java.lang.Object getObject(java.lang.String,java.lang.Object)
meth public abstract java.lang.Object[] getArray(java.lang.String,java.lang.Object[])
meth public abstract java.lang.String getString(java.lang.String,java.lang.String)
meth public abstract java.util.Collection getCollection(java.lang.String,java.util.Collection)
meth public abstract java.util.Map getMap(java.lang.String,java.util.Map)
meth public abstract long getLong(java.lang.String,long)
meth public abstract org.netbeans.api.debugger.Properties getProperties(java.lang.String)
meth public abstract short getShort(java.lang.String,short)
meth public abstract void setArray(java.lang.String,java.lang.Object[])
meth public abstract void setBoolean(java.lang.String,boolean)
meth public abstract void setByte(java.lang.String,byte)
meth public abstract void setChar(java.lang.String,char)
meth public abstract void setCollection(java.lang.String,java.util.Collection)
meth public abstract void setDouble(java.lang.String,double)
meth public abstract void setFloat(java.lang.String,float)
meth public abstract void setInt(java.lang.String,int)
meth public abstract void setLong(java.lang.String,long)
meth public abstract void setMap(java.lang.String,java.util.Map)
meth public abstract void setObject(java.lang.String,java.lang.Object)
meth public abstract void setShort(java.lang.String,short)
meth public abstract void setString(java.lang.String,java.lang.String)
meth public static org.netbeans.api.debugger.Properties getDefault()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds defaultProperties
hcls DelegatingProperties,PrimitiveRegister,PropertiesImpl

CLSS public abstract interface static org.netbeans.api.debugger.Properties$Initializer
meth public abstract java.lang.Object getDefaultPropertyValue(java.lang.String)
meth public abstract java.lang.String[] getSupportedPropertyNames()

CLSS public abstract interface static org.netbeans.api.debugger.Properties$Reader
meth public abstract java.lang.Object read(java.lang.String,org.netbeans.api.debugger.Properties)
meth public abstract java.lang.String[] getSupportedClassNames()
meth public abstract void write(java.lang.Object,org.netbeans.api.debugger.Properties)

CLSS public final org.netbeans.api.debugger.Session
fld public final static java.lang.String PROP_CURRENT_LANGUAGE = "currentLanguage"
fld public final static java.lang.String PROP_SUPPORTED_LANGUAGES = "supportedLanguages"
intf org.netbeans.spi.debugger.ContextProvider
meth public <%0 extends java.lang.Object> java.util.List<? extends {%%0}> lookup(java.lang.String,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} lookupFirst(java.lang.String,java.lang.Class<{%%0}>)
meth public java.lang.String getCurrentLanguage()
meth public java.lang.String getLocationName()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.lang.String[] getSupportedLanguages()
meth public org.netbeans.api.debugger.DebuggerEngine getCurrentEngine()
meth public org.netbeans.api.debugger.DebuggerEngine getEngineForLanguage(java.lang.String)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void kill()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void setCurrentLanguage(java.lang.String)
supr java.lang.Object
hfds currentDebuggerEngine,currentLanguage,engines,enginesLookups,languages,locationName,lookup,name,pcs,privateLookup

CLSS public final org.netbeans.api.debugger.Watch
fld public final static java.lang.String PROP_EXPRESSION = "expression"
fld public final static java.lang.String PROP_VALUE = "value"
meth public java.lang.String getExpression()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void remove()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setExpression(java.lang.String)
supr java.lang.Object
hfds expression,pcs

CLSS public abstract org.netbeans.spi.debugger.ActionsProvider
cons public ActionsProvider()
innr public abstract interface static !annotation Registration
meth public abstract boolean isEnabled(java.lang.Object)
meth public abstract java.util.Set getActions()
meth public abstract void addActionsProviderListener(org.netbeans.spi.debugger.ActionsProviderListener)
meth public abstract void doAction(java.lang.Object)
meth public abstract void removeActionsProviderListener(org.netbeans.spi.debugger.ActionsProviderListener)
meth public void postAction(java.lang.Object,java.lang.Runnable)
supr java.lang.Object
hcls ContextAware

CLSS public abstract interface static !annotation org.netbeans.spi.debugger.ActionsProvider$Registration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String path()

CLSS public abstract interface org.netbeans.spi.debugger.ActionsProviderListener
intf java.util.EventListener
meth public abstract void actionStateChange(java.lang.Object,boolean)

CLSS public abstract org.netbeans.spi.debugger.ActionsProviderSupport
cons public ActionsProviderSupport()
meth protected final void setEnabled(java.lang.Object,boolean)
meth protected void fireActionStateChanged(java.lang.Object,boolean)
meth public abstract void doAction(java.lang.Object)
meth public boolean isEnabled(java.lang.Object)
meth public final void addActionsProviderListener(org.netbeans.spi.debugger.ActionsProviderListener)
meth public final void removeActionsProviderListener(org.netbeans.spi.debugger.ActionsProviderListener)
supr org.netbeans.spi.debugger.ActionsProvider
hfds enabled,listeners

CLSS public abstract interface org.netbeans.spi.debugger.ContextAwareService<%0 extends java.lang.Object>
meth public abstract {org.netbeans.spi.debugger.ContextAwareService%0} forContext(org.netbeans.spi.debugger.ContextProvider)

CLSS public final org.netbeans.spi.debugger.ContextAwareSupport
meth public static java.lang.Object createInstance(java.lang.String,org.netbeans.spi.debugger.ContextProvider)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.debugger.ContextProvider
meth public abstract <%0 extends java.lang.Object> java.util.List<? extends {%%0}> lookup(java.lang.String,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} lookupFirst(java.lang.String,java.lang.Class<{%%0}>)

CLSS public abstract org.netbeans.spi.debugger.DebuggerEngineProvider
cons public DebuggerEngineProvider()
innr public abstract interface static !annotation Registration
meth public abstract java.lang.Object[] getServices()
meth public abstract java.lang.String getEngineTypeID()
meth public abstract java.lang.String[] getLanguages()
meth public abstract void setDestructor(org.netbeans.api.debugger.DebuggerEngine$Destructor)
supr java.lang.Object
hcls ContextAware

CLSS public abstract interface static !annotation org.netbeans.spi.debugger.DebuggerEngineProvider$Registration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String path()

CLSS public abstract interface !annotation org.netbeans.spi.debugger.DebuggerServiceRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String path()
meth public abstract java.lang.Class[] types()

CLSS public abstract org.netbeans.spi.debugger.DelegatingDebuggerEngineProvider
cons public DelegatingDebuggerEngineProvider()
meth public abstract java.lang.String[] getLanguages()
meth public abstract org.netbeans.api.debugger.DebuggerEngine getEngine()
meth public abstract void setDestructor(org.netbeans.api.debugger.DebuggerEngine$Destructor)
supr java.lang.Object

CLSS public abstract org.netbeans.spi.debugger.DelegatingSessionProvider
cons public DelegatingSessionProvider()
meth public abstract org.netbeans.api.debugger.Session getSession(org.netbeans.api.debugger.DebuggerInfo)
supr java.lang.Object

CLSS public abstract org.netbeans.spi.debugger.SessionProvider
cons public SessionProvider()
innr public abstract interface static !annotation Registration
meth public abstract java.lang.Object[] getServices()
meth public abstract java.lang.String getLocationName()
meth public abstract java.lang.String getSessionName()
meth public abstract java.lang.String getTypeID()
supr java.lang.Object
hcls ContextAware

CLSS public abstract interface static !annotation org.netbeans.spi.debugger.SessionProvider$Registration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String path()

