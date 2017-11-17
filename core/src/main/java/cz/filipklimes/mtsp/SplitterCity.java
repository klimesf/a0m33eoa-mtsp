package cz.filipklimes.mtsp;

public class SplitterCity implements City
{

    public static final SplitterCity INSTANCE = new SplitterCity();

    @Override
    public void accept(final CityVisitor visitor)
    {
        visitor.visit(this);
    }

}
