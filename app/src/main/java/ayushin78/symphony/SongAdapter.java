package ayushin78.symphony;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Dell1 on 7/27/2017.
 */

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songList;
    private LayoutInflater songInf;


    public SongAdapter(Context c, ArrayList<Song>songList) {
        this.songList = songList;
        songInf = LayoutInflater.from(c);
    }
    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Object getItem(int i) {
        return songList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return songList.get(i).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout songLinearLayout = (LinearLayout) songInf.inflate(R.layout.song, parent, false);

        TextView songView = (TextView) songLinearLayout.findViewById(R.id.song_title);
        TextView artistView = (TextView)songLinearLayout.findViewById(R.id.song_artist);

        Song currentSong = songList.get(position);

        songView.setText(currentSong.getTitle());
        artistView.setText(currentSong.getArtist());

        if(songList.get(position) != null )
        {
            songView.setTextColor(Color.WHITE);
            artistView.setTextColor(Color.WHITE);
//            int color = Color.argb( 200, 255, 64, 64 );
//            songView.setBackgroundColor( color );
//            songView.setBackgroundColor(Color.BLUE);
//            int color = Color.argb( 200, 255, 64, 64 );
//            text.setBackgroundColor( color );

        }
        songLinearLayout.setTag(position);
        return songLinearLayout;

    }

}
