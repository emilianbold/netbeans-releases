 class Persistent {
public:
    int getDatabase();
};

class Person : public Persistent {
};

template <class T>
class RelationHandle {
protected:
    T* owner;
};

class MotherHandle : public RelationHandle<Person> {
public:
    void get();
};

void MotherHandle::get() {
    owner->getDatabase();
}
