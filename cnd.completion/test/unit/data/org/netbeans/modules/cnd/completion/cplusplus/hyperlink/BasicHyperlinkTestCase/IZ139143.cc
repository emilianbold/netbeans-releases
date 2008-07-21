class OrderedStaticCreatorFunc {};
typedef void (OrderedStaticCreatorFunc::*Creator)();
struct Data {
    OrderedStaticCreatorFunc* object;
    Creator creator;
};
int main() {
    Data cur;
    ( (cur.object)->*cur.creator )();
    ( (*cur.object).*cur.creator )();
}
