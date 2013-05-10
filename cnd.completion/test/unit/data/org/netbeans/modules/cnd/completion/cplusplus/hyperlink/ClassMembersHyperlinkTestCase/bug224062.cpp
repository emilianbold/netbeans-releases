#define bug224062_Q_DISABLE_COPY(Class) \
Class(const Class &); \
Class &operator=(const Class &); 


template <typename A, typename B>
class bug224062_QMap {
    
}; 

class bug224062_ObjectTypeDescriptor {
public:
    
  bug224062_ObjectTypeDescriptor() {}
  
  bug224062_Q_DISABLE_COPY(bug224062_ObjectTypeDescriptor)
  
private:
    
    const bug224062_ObjectTypeDescriptor *superclass_descriptor_; 

    static bug224062_QMap<int, bug224062_ObjectTypeDescriptor> class_map;
    
    int fooo() {
        bug224062_ObjectTypeDescriptor *var = new bug224062_ObjectTypeDescriptor();
    }
     
};
