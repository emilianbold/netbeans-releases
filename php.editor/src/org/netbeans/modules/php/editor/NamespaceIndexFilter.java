package org.netbeans.modules.php.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.php.editor.index.IndexedClassMember;
import org.netbeans.modules.php.editor.index.IndexedFullyQualified;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.QualifiedName;
import org.netbeans.modules.php.editor.model.QualifiedNameKind;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;

public class NamespaceIndexFilter<T extends ElementHandle> {

    private final String requestPrefix;
    private final QualifiedName prefix;
    private QualifiedNameKind kind;
    private String namespaceName;
    private String name;
    private int segmentSize;

    public NamespaceIndexFilter(String requestPrefix) {
        super();
        this.requestPrefix = requestPrefix;
        this.prefix = QualifiedName.create(requestPrefix);
    }

    /**
     * @return the prefixStr
     */
    public String getRequestPrefix() {
        return requestPrefix;
    }

    /**
     * @return the namespaceName
     */
    public String getNamespaceName() {
        if (namespaceName == null) {
            namespaceName = prefix.toNamespaceName(true).toString();
        }
        return namespaceName;
    }

    /**
     * @return the name
     */
    public String getName() {
        if (name == null) {
            name = prefix.toName().toString();
        }
        return name;
    }

    public QualifiedNameKind getKind() {
        if (kind == null) {
            kind = prefix.getKind();
        }
        return kind;
    }

    public int getSegmentSize() {
        if (segmentSize != -1) {
            segmentSize = prefix.getSegments().size();
        }
        return segmentSize;
    }

    public Collection<T> filter(final Collection<T> originalElems) {
        return filter(originalElems, getName().trim().length() == 0);
    }

    public Collection<? extends ModelElement> filterModelElements(final Collection<? extends ModelElement> originalElems, boolean strictCCOption) {
        if (getKind().isUnqualified()) {
            return originalElems;
        }
        List<ModelElement> retval = new ArrayList<ModelElement>();
        String namespaneNameLCase = getNamespaceName().toLowerCase();
        String namespaneNameLCaseSlashed = namespaneNameLCase;
        if (!namespaneNameLCaseSlashed.endsWith("\\")) {//NOI18N
            namespaneNameLCaseSlashed += "\\";
        }
        for (ModelElement elem : originalElems) {
            final Scope inScope = elem.getInScope();
            ModelElement originalElem = null;
            if (inScope instanceof TypeScope) {
                originalElem = elem;
                elem = inScope;
            }
            String fqn = elem.getNamespaceName().append(elem.getName()).toFullyQualified().toString();
            final int indexOf = fqn.toLowerCase().indexOf(namespaneNameLCaseSlashed);
            final boolean fullyQualified = getKind().isFullyQualified();
            if (fullyQualified ? indexOf == 0 : indexOf != -1) {
                if (strictCCOption && (fullyQualified || getSegmentSize() > 1)) {
                    final QualifiedName nsFqn = QualifiedName.create(fqn).toNamespaceName(true);
                    if (nsFqn.toString().toLowerCase().indexOf(namespaneNameLCase) == -1) {
                        continue;
                    }
                    final String elemName = fqn.substring(indexOf + namespaneNameLCaseSlashed.length());
                    if (elemName.indexOf(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR) != -1) {
                        continue;
                    }
                }
                retval.add(originalElem != null ? originalElem : elem);
            }
        }
        return retval;
    }
    public Collection<T> filter(final Collection<T> originalElems, boolean strictCCOption) {
        if (getKind().isUnqualified()) {
            return originalElems;
        }
        Collection<T> retval = new ArrayList<T>();
        String namespaneNameLCase = getNamespaceName().toLowerCase();
        String namespaneNameLCaseSlashed = namespaneNameLCase;
        if (!namespaneNameLCaseSlashed.endsWith("\\")) {//NOI18N
            namespaneNameLCaseSlashed += "\\";
        }
        for (T elem : originalElems) {
            if (elem instanceof IndexedFullyQualified || elem instanceof IndexedClassMember) {
                if (elem instanceof IndexedClassMember) {
                    int idx = ((IndexedClassMember) elem).getType().getName().toLowerCase().indexOf(getName().toLowerCase());
                    if (idx == -1) {
                        retval.add(elem);
                        continue;
                    }
                }
                String fqn = elem instanceof IndexedFullyQualified ? ((IndexedFullyQualified) elem).getFullyQualifiedName() :
                    ((IndexedClassMember) elem).getType().getFullyQualifiedName();
                final int indexOf = fqn.toLowerCase().indexOf(namespaneNameLCaseSlashed);
                final boolean fullyQualified = getKind().isFullyQualified();
                if (fullyQualified ? indexOf == 0 : indexOf != -1) {
                    if (strictCCOption && (fullyQualified || getSegmentSize() > 1)) {
                        final QualifiedName nsFqn = QualifiedName.create(fqn).toNamespaceName(true);
                        if (nsFqn.toString().toLowerCase().indexOf(namespaneNameLCase) == -1) {
                            continue;
                        }
                        final String elemName = fqn.substring(indexOf + namespaneNameLCaseSlashed.length());
                        if (elemName.indexOf(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR) != -1) {
                            continue;
                        }
                    }
                    retval.add(elem);
                }
            } else if (namespaneNameLCase.equals(NamespaceDeclarationInfo.DEFAULT_NAMESPACE_NAME)) {
                retval.add(elem);
            }
        }
        return retval;
    }
}
