package org.netbeans.modules.php.editor;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.index.IndexedFullyQualified;
import org.netbeans.modules.php.editor.model.QualifiedName;
import org.netbeans.modules.php.editor.model.QualifiedNameKind;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;

public class NamespaceIndexFilter<T extends IndexedElement> {

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

    public Collection<T> filter(final Collection<T> originalElems, boolean strictCCOption) {
        if (getKind().isUnqualified()) {
            return originalElems;
        }
        Collection<T> retval = new ArrayList<T>();
        String namespaneNameLCase = getNamespaceName().toLowerCase();
        String namespaneNameLCaseSlashed = namespaneNameLCase;
        if (!namespaneNameLCaseSlashed.endsWith("\\")) {
            //NOI18N
            namespaneNameLCaseSlashed += "\\";
        }
        for (T elem : originalElems) {
            if (elem instanceof IndexedFullyQualified) {
                IndexedFullyQualified idxFqn = (IndexedFullyQualified) elem;
                String fqn = idxFqn.getFullyQualifiedName();
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
