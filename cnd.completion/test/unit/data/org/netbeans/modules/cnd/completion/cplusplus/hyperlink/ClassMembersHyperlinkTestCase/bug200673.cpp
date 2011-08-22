class bug200673_ParCompactionManager {
public:
    bug200673_ParCompactionManager();
    bug200673_ParCompactionManager(const bug200673_ParCompactionManager& orig);
    virtual ~bug200673_ParCompactionManager();
private:

};

class bug200673_Class {
public:
    bug200673_Class();
    bug200673_Class(const bug200673_Class& orig);
    virtual ~bug200673_Class();
private:

public:
  void follow_header(void);
  void follow_header(bug200673_ParCompactionManager* cm);
};

void bug200673_Class::follow_header(void) {

}

void bug200673_Class::follow_header(bug200673_ParCompactionManager* cm) {

}