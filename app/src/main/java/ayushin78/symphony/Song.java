package ayushin78.symphony;

/**
 * Created by Dell1 on 7/27/2017.
 */

public class Song {

    private long id;
    private String title;
    private String artist;


    public Song(long songID, String songTitle, String songArtist)
    {
        id=songID;
        title=songTitle;
        artist=songArtist;
    }

    public long getId()
    {
        return this.id;
    }

    public String getTitle()
    {
        return this.title;
    }
    public String getArtist()
    {
        return this.artist;
    }

}
