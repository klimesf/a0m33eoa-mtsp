package cz.filipklimes.mtsp;

public class SplitterCity implements City
{

    @Override
    public void accept(final CityVisitor visitor)
    {
        visitor.visit(this);
    }

}
