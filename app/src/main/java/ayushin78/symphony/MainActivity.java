package ayushin78.symphony;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {

    private ArrayList<Song> songList;
    private ListView songListView;

    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound=false;


    private MusicController controller;

    private boolean paused=false, playbackPaused=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

// MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
// app-defined int constant

                return;
            }}
        songListView = (ListView) findViewById(R.id.song_list);

        songList = new ArrayList<>();
        getSongList();

        Collections.sort(songList, new SortByTitle());

        SongAdapter songAdt = new SongAdapter(this, songList);
        songListView.setAdapter(songAdt);
        setController();

    }

    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.getCount() > 0) {
            songList = new ArrayList<>();
            while (musicCursor.moveToNext()) {
                long id = musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String title = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
//                String album = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                // Save to audioList
                songList.add(new Song(id, title, artist));
            }
        }
        musicCursor.close();
    }


    public void songPicked(View view){
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    private ServiceConnection musicConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicService = binder.getService();
            //pass list
            musicService.setSongList(songList);
            musicBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main, menu);
            return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                musicService.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicService=null;
                System.exit(0);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (musicBound)
            unbindService(musicConnection);
        stopService(playIntent);
        musicService=null;
        super.onDestroy();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() { // a function in MainActivity
        moveTaskToBack(true);

    }

    @Override
    public void onPause()
    {
        super.onPause();
        paused = true;
    }


    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
        setController();

        LocalBroadcastManager.getInstance(this).registerReceiver(onPrepareReceiver,
                new IntentFilter("MEDIA_PLAYER_PREPARED"));
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }
    //mediaplayer methods
    @Override
    public void pause() {
        playbackPaused=true;
        musicService.pausePlayer();
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public void start() {
        musicService.go();
    }

    @Override
    public int getDuration() {
        if(musicService!=null && musicBound && musicService.isPng())
        return musicService.getDur();
        else
            return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicService!=null && musicBound && musicService.isPng())
        return musicService.getPosn();

        else
            return 0;
    }



    @Override
    public boolean isPlaying() {
        if(musicService!=null && musicBound)
        return musicService.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }



    public class MusicController extends MediaController {

        public MusicController(Context c){
            super(c);
        }

        public void hide(){}

    }

    private void setController(){
        //set the controller up
        if (controller == null) controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }


        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
        }

    //play next
    private void playNext(){
        musicService.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }

        controller.show(0);
    }

    private void playPrev(){
        musicService.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }

        controller.show(0);
    }

    private BroadcastReceiver onPrepareReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            // When music player has been prepared, show controller
            controller.show(0);
        }
    };
}
