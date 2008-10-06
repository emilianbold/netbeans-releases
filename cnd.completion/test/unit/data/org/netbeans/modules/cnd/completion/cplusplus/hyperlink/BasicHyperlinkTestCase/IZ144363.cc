template<class K, class V>
class normal_iterator {
};

template<class K, class V>
class AssocVector {
public:
    typedef normal_iterator<K, V> iterator;
    typedef V value_type;
};


template<class AbstractProduct, typename IdentifierType>
class Factory {
    typedef AssocVector<IdentifierType, AbstractProduct> IdToProductMap;
    void foo() {
        for(typename IdToProductMap::iterator iter; ;) {
            iter;
        }
        foo(typename IdToProductMap::value_type(0, 0));
    }
};
