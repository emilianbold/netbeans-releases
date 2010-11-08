struct bug188925_Real {
};

template<class NodeData, class Real = float>
class bug188925_OctNode {
public:
    NodeData nodeData;

};

typedef bug188925_OctNode<class bug188925_TreeNodeData, bug188925_Real> TreeOctNode;

class bug188925_Octree {
    void setNodeIndices(TreeOctNode& tree, int& idx);
};

class bug188925_TreeNodeData {
public:

    static int UseIndex;

    union {
        int mcIndex;

        struct {
            int nodeIndex;

        };

    };

    bug188925_Real value;

    TreeNodeData(void);

    ~TreeNodeData(void);
};

void bug188925_Octree::setNodeIndices(TreeOctNode& node) {
    node.nodeData.nodeIndex = 1; // UNABLE TO RESOLVE identifier nodeIndex
}