class BaseClass {
public:
    void DoThat(void) {}
};

template
<
typename T,
template <class> class StoragePolicy
>
class SmartPtr
: public StoragePolicy<T> {
public:
    typedef StoragePolicy<T> SP;
    typedef typename SP::PointerType PointerType;
    PointerType operator->() {
        return 0;
    }
};

template <class T>
class DefaultSPStorage {
public:
    typedef T* PointerType;
};

typedef SmartPtr< BaseClass, DefaultSPStorage >
NonConstBase_RefLink_NoConvert_Assert_DontPropagate_ptr;

int iz151194_main() {
    NonConstBase_RefLink_NoConvert_Assert_DontPropagate_ptr p1;
    p1->DoThat(); // unresolved
    return 0;
}