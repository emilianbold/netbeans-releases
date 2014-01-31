package org.netbeans.modules.j2me.project.ui.customizer;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.Collator;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.openide.util.Exceptions;
import org.openide.util.Utilities;

public class PermissionsProvider {

    private static final String BASE_CLASS_NAME = "java.security.Permission";

    private static final String IMPNG_PERMISSIONS_FILE_NAME = "impng.permissions";

    private static final String NULL_PARAMETER = "null";

    // Use double-quote to mark absent parameter since these parameters cannot contain double-quote
    private static final String NO_PARAMETER = "\"";

    private Map<String, PermissionDescriptor> permissions;

    public PermissionsProvider(File libsDir) {
        fill(libsDir);
    }

    private void listClasses(File jarFilePointer, Set<String> allClassesNames) {
        try (JarFile jarFile = new JarFile(jarFilePointer)) {
            for (Enumeration< ? extends JarEntry> jarEntryEnum = jarFile.entries(); jarEntryEnum.hasMoreElements();) {
                JarEntry jarEntry = jarEntryEnum.nextElement();
                String entryName = jarEntry.getName();
                if (!jarEntry.isDirectory() && entryName != null && entryName.endsWith(".class")) {
                    allClassesNames.add(entryName.substring(0, entryName.lastIndexOf('.'))
                            .replace('/', '.')
                            .replace('$', '.'));
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void loadIMPNGPermissions(File libsDir) {
        if (libsDir != null) {
            File permissionsFile = new File(libsDir, IMPNG_PERMISSIONS_FILE_NAME);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(permissionsFile), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0 && !line.startsWith("#")) {
                        permissions.put(line, new PermissionDescriptor(line, false));
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void fill(File libsDir) {
        permissions = new HashMap<>();

        loadIMPNGPermissions(libsDir);
        loadMEEPPermissions(libsDir);
    }

    private void loadMEEPPermissions(File libsDir) {
        Set<String> allClassNames = new HashSet<>();
        List<URL> jarURLs = new ArrayList<>();

        if (libsDir != null) {
            File[] children = libsDir.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (child.isFile() && child.getName().toLowerCase().endsWith(".jar")) {
                        try {
                            jarURLs.add(Utilities.toURI(child).toURL());
                            listClasses(child, allClassNames);
                        } catch (MalformedURLException t) {
                        }
                    }
                }
            }
        }

        URLClassLoader classLoader = new URLClassLoader(jarURLs.toArray(new URL[jarURLs.size()]));
        Class< ?> baseClass;

        try {
            try {
                baseClass = classLoader.loadClass(BASE_CLASS_NAME);
            } catch (ClassNotFoundException ex) {
                baseClass = null;
            }

            if (baseClass != null) {
                for (String className : allClassNames) {
                    try {
                        Class< ?> candidate = classLoader.loadClass(className);

                        if (!Modifier.isAbstract(candidate.getModifiers()) && baseClass.isAssignableFrom(candidate)) {
                            PermissionDescriptor descriptor = new PermissionDescriptor(className, true);

                            Constructor< ?>[] constructors = candidate.getConstructors();

                            if (constructors != null) {
                                for (Constructor< ?> constructor : constructors) {
                                    Class< ?>[] parameterTypes = constructor.getParameterTypes();
                                    if (parameterTypes != null && parameterTypes.length <= 2) {
                                        boolean accept = true;

                                        for (int i = 0; i < parameterTypes.length && accept; i++) {
                                            accept &= parameterTypes[i] == String.class;
                                        }

                                        if (accept) {
                                            descriptor.numberOfSupportedArgumentes.add(parameterTypes.length);
                                        }
                                    }
                                }
                            }

                            if (!descriptor.numberOfSupportedArgumentes.isEmpty()) {
                                permissions.put(className, descriptor);
                            }
                        }
                    } catch (ClassNotFoundException | SecurityException t) {
                    }
                }
            }
        } finally {
            // Casting to Closeable is the trick to support running under JDK 6 and early
            if (classLoader instanceof Closeable) {
                try {
                    ((Closeable) classLoader).close();
                } catch (IOException t) {
                }
            }
        }
    }

    public PermissionsFactory getPermissionsFactory(Collection<String> excludePermissions) {
        return new PermissionsFactory(excludePermissions);
    }

    public static enum PermissionError {

        OK, INVALID_NAME, INVALID_ACTION, UNDEFINED_ACTION, UNDEFINED_NAME, CONFLICT;
    }

    public class PermissionsFactory {

        private final Map<String, Map<String, Set<String>>> existingMEEPPermissions = new HashMap<>();
        private final PermissionDescriptor[] availablePermissions;

        private PermissionsFactory(Collection<String> excludePermissions) {
            if (excludePermissions == null) {
                excludePermissions = Collections.emptySet();
            }

            Set<String> excludePermissionNames = new HashSet<>();

            for (String excludePermission : excludePermissions) {
                String[] permissionComponents = splitPermissionComponents(excludePermission);

                if (permissionComponents != null) {
                    PermissionDescriptor descriptor = permissions.get(permissionComponents[0]);

                    if (descriptor != null) {
                        String permission = descriptor.getPermission();

                        if (descriptor.isPermissionClass()) {
                            if (!descriptor.supportsName()) {
                                excludePermissionNames.add(permission);
                            } else {
                                Map<String, Set<String>> existingNames = existingMEEPPermissions.get(permission);

                                if (existingNames == null) {
                                    existingNames = new HashMap<>();
                                    existingMEEPPermissions.put(permission, existingNames);
                                }

                                Set<String> existingActions = existingNames.get(permissionComponents[1]);

                                if (existingActions == null) {
                                    existingActions = new HashSet<>();
                                    existingNames.put(permissionComponents[1], existingActions);
                                }

                                if (permissionComponents[2] == null) {
                                    existingActions.add(null);
                                } else {
                                    for (String action : permissionComponents[2].split(",")) {
                                        existingActions.add(action.trim());
                                    }
                                }
                            }
                        } else {
                            excludePermissionNames.add(permission);
                        }
                    }
                }
            }

            List<PermissionDescriptor> avilablePermissionsList = new ArrayList<>();

            for (PermissionDescriptor permissionDescriptor : permissions.values()) {
                if (!excludePermissionNames.contains(permissionDescriptor.getPermission())) {
                    avilablePermissionsList.add(permissionDescriptor);
                }
            }

            Collections.sort(avilablePermissionsList);

            availablePermissions = avilablePermissionsList.toArray(new PermissionDescriptor[avilablePermissionsList.size()]);
        }
        
        public PermissionDescriptor getDescriptor(PermissionDefinition permission) {
            return permissions.get(permission.getPermission());
        }

        public PermissionDefinition getPermission(String permission) {
            String[] permissionComponents = splitPermissionComponents(permission);

            if (permissionComponents != null) {
                PermissionDescriptor descriptor = permissions.get(permissionComponents[0]);

                if (descriptor != null) {
                    int numberOfParameters = 0;

                    if (descriptor.supportsName() && !NO_PARAMETER.equals(permissionComponents[1])) {
                        numberOfParameters = numberOfParameters + 1;

                        if (descriptor.supportsAction() && !NO_PARAMETER.equals(permissionComponents[2])) {
                            numberOfParameters = numberOfParameters + 1;
                        }
                    }

                    if (numberOfParameters == 0 && !descriptor.nameCanBeOptional() && descriptor.isPermissionClass()) {
                        return null;
                    } else if (numberOfParameters == 1 && !descriptor.actionsCanBeOptional(true)) {
                        return null;
                    }

                    return getPermissionWithoutValidation(
                            descriptor,
                            permissionComponents[1],
                            permissionComponents[2],
                            NO_PARAMETER.equals(permissionComponents[2]) ? (NO_PARAMETER.equals(permissionComponents[1]) ? 0
                            : 1)
                            : 2);
                }
            }

            return null;
        }

        private String[] splitPermissionComponents(String permission) {
            String[] result = new String[]{null, NO_PARAMETER, NO_PARAMETER};
            int idx = permission.indexOf(' ');

            if (idx < 0) {
                result[0] = permission.trim();
                permission = "";
            } else {
                result[0] = permission.substring(0, idx).trim();
                permission = permission.substring(idx).trim();
            }

            for (int i = 1; i < 3 && permission.length() > 0; i++) {
                if (permission.startsWith("\"")) {
                    idx = permission.indexOf('"', 1);

                    if (idx < 0) {
                        return null;
                    } else {
                        result[i] = permission.substring(1, idx);
                        permission = permission.substring(idx + 1).trim();
                    }
                } else {
                    if (!permission.startsWith(NULL_PARAMETER)) {
                        return null;
                    }

                    idx = permission.indexOf(' ');
                    result[i] = null;

                    if (idx < 0) {
                        if (NULL_PARAMETER.length() < permission.length()) {
                            return null;
                        }

                        permission = "";
                    } else {
                        if (idx != NULL_PARAMETER.length()) {
                            return null;
                        }

                        permission = permission.substring(idx).trim();
                    }
                }
            }

            return result;
        }
        
        public PermissionDescriptor[] getAvailablePermissions() {
            return availablePermissions;
        }

        public PermissionError validatePermission(PermissionDescriptor descriptor, String name, String actions,
                int numberOfParameters) {
            if (descriptor.isPermissionClass()) {
                if (numberOfParameters > 0) {
                    if (descriptor.supportsName()) {
                        if (name != null && name.indexOf('"') >= 0) {
                            return PermissionError.INVALID_NAME;
                        }

                        if (numberOfParameters > 1) {
                            if (descriptor.supportsAction()) {
                                if (actions != null && actions.indexOf('"') >= 0) {
                                    return PermissionError.INVALID_ACTION;
                                }
                            }
                        } else {
                            if (!descriptor.numberOfSupportedArgumentes.contains(1)) {
                                return PermissionError.UNDEFINED_ACTION;
                            }
                        }
                    }
                } else {
                    if (!descriptor.numberOfSupportedArgumentes.contains(0)) {
                        return PermissionError.UNDEFINED_NAME;
                    }
                }

                PermissionDefinition permission = getPermissionWithoutValidation(descriptor, name, actions,
                        numberOfParameters);
                Map<String, Set<String>> existingNames = existingMEEPPermissions.get(permission.getPermission());

                if (existingNames != null) {
                    Set<String> existingActions = existingNames.get(permission.getNumberOfParameters() > 0 ? permission.getName()
                            : NO_PARAMETER);

                    if (existingActions != null) {
                        Set<String> definedActions = permission.getActionsSet();

                        if (definedActions.isEmpty()) {
                            if (existingActions.contains(null) && permission.getNumberOfParameters() > 1
                                    || existingActions.contains(NO_PARAMETER) && permission.getNumberOfParameters() < 2) {
                                return PermissionError.CONFLICT;
                            }
                        } else {
                            for (String action : definedActions) {
                                if (existingActions.contains(action)) {
                                    return PermissionError.CONFLICT;
                                }
                            }
                        }
                    }
                }
            }

            return PermissionError.OK;
        }

        private PermissionDefinition getPermissionWithoutValidation(PermissionDescriptor descriptor, String name,
                String actions, int numberOfParameters) {
            if (descriptor.isPermissionClass()) {
                boolean hasName = descriptor.supportsName() && numberOfParameters > 0;
                boolean hasAction = descriptor.supportsAction() && numberOfParameters > 1;

                return new PermissionDefinition(descriptor.getPermission(), hasName ? name : null, hasName && hasAction
                        && actions != null ? actions.trim() : null, true, numberOfParameters);
            } else {
                return new PermissionDefinition(descriptor.getPermission(), null, null, false, 0);
            }
        }

        public PermissionDefinition getPermission(PermissionDescriptor descriptor, String name, String actions,
                int numberOfParameters) {
            if (validatePermission(descriptor, name, actions, numberOfParameters) != PermissionError.OK) {
                return null;
            }

            return getPermissionWithoutValidation(descriptor, name, actions, numberOfParameters);
        }
    }

    public static class PermissionDefinition implements Comparable<PermissionDefinition> {

        private final String permission;
        private final String name;
        private final LinkedHashSet<String> actions = new LinkedHashSet<>();
        private final boolean permissionClass;
        private final int numberOfParameters;

        private PermissionDefinition(String permission, String name, String actions, boolean permissionClass,
                int numberOfParameters) {
            this.permission = permission;
            this.name = name;
            this.permissionClass = permissionClass;
            this.numberOfParameters = numberOfParameters;

            if (numberOfParameters > 1) {
                parseActions(actions);
            }
        }

        private void parseActions(String actionsString) {
            if (actionsString != null) {
                for (String action : actionsString.split(",")) {
                    actions.add(action.trim());
                }
            }
        }

        public String getPermission() {
            return permission;
        }

        private Set<String> getActionsSet() {
            return actions;
        }

        public String getName() {
            return name;
        }

        public int getNumberOfParameters() {
            return numberOfParameters;
        }

        public String getActions() {
            if (numberOfParameters < 2 || actions.isEmpty()) {
                return null;
            }

            StringBuilder result = new StringBuilder();

            for (String action : actions) {
                if (result.length() > 0) {
                    result.append(",");
                }
                result.append(action);
            }
            return result.toString();
        }

        public boolean isPermissionClass() {
            return permissionClass;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder(permission);

            if (isPermissionClass()) {
                if (numberOfParameters > 0) {
                    result.append(" ");

                    if (name == null) {
                        result.append(NULL_PARAMETER);
                    } else {
                        result.append("\"");
                        result.append(name);
                        result.append("\"");
                    }

                    String actionsString = getActions();

                    if (numberOfParameters > 1) {
                        result.append(" ");
                        if (actionsString == null) {
                            result.append(NULL_PARAMETER);
                        } else {
                            result.append("\"");
                            result.append(actionsString);
                            result.append("\"");
                        }
                    }
                }
            }
            return result.toString();
        }

        @Override
        public int compareTo(PermissionDefinition o) {
            return Collator.getInstance().compare(toString(), o.toString());
        }
    }

    public static class PermissionDescriptor implements Comparable<PermissionDescriptor> {

        private final String permission;
        private final boolean permissionClass;
        private Set<Integer> numberOfSupportedArgumentes = new HashSet<>();

        private PermissionDescriptor(String permission, boolean permissionClass) {
            this.permission = permission;
            this.permissionClass = permissionClass;
        }

        public boolean isPermissionClass() {
            return permissionClass;
        }

        public String getPermission() {
            return permission;
        }

        @Override
        public String toString() {
            return getPermission();
        }

        @Override
        public int compareTo(PermissionDescriptor o) {
            return Collator.getInstance().compare(getPermission(), o.getPermission());
        }

        @Override
        public int hashCode() {
            return getPermission().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PermissionDescriptor) {
                return getPermission().equals(((PermissionDescriptor) obj).getPermission());
            }

            return false;
        }

        public boolean supportsName() {
            return numberOfSupportedArgumentes.contains(1) || numberOfSupportedArgumentes.contains(2);
        }

        public boolean supportsAction() {
            return numberOfSupportedArgumentes.contains(2);
        }

        public boolean nameCanBeOptional() {
            return numberOfSupportedArgumentes.contains(0);
        }

        public boolean actionsCanBeOptional(boolean nameRequired) {
            return numberOfSupportedArgumentes.contains(nameRequired ? 1 : 0);
        }
    }
}
