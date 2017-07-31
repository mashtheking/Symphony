package ayushin78.symphony;

import java.util.Comparator;

/**
 * Created by Dell1 on 7/27/2017.
 */

public class SortByTitle implements Comparator<Song> {
    @Override
    public int compare(Song a, Song b) {
            return a.getTitle().compareToIgnoreCase(b.getTitle());
    }
}
