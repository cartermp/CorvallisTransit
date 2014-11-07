package phillipcarter.com.mapsthing.model;

/**
 * Basic Tuple class.
 */
public class Tuple<T, U> {
    public final T item1;
    public final U item2;

    /**
     * Instantiates a new Tuple.
     */
    public Tuple(T item1, U item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    /**
     * Instantiates a Tuple in a similar fashion as Tuple.Create() in C#.
     */
    public static <T, U> Tuple<T, U> create(T item1, U item2) {
        return new Tuple<T, U>(item1, item2);
    }
}
