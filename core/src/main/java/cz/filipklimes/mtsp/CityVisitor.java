package cz.filipklimes.mtsp;

public interface CityVisitor
{

    void visit(RealCity city);

    void visit(SplitterCity city);

}
