class Container
{
public:
  struct ForwardStruct;

  bool do_cast(const Container* __dst_type,
                     ForwardStruct & __result) const;
};

struct Container::ForwardStruct
{
  int whole_details;
  int foo();
  ForwardStruct (int details_) : whole_details (details_) { }
};

bool Container::do_cast (const Container *dst_type,
                      ForwardStruct & result) const
{
  ForwardStruct result2 (result.whole_details);
  result2.foo();
  result.foo();
  return true;
}
