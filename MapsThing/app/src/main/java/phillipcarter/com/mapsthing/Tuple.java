package phillipcarter.com.mapsthing;

/**
 * Created by Phillip on 11/1/2014.
 */
public class Tuple<T extends Comparable<? super T>, U extends Comparable<? super U>> {
    public final T item1;
    public final U item2;

    public Tuple(T item1, U item2) {
        this.item1 = item1;
        this.item2 = item2;
    }
}
