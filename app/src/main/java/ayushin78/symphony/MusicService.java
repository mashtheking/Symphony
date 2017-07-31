package ayushin78.symphony;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener {

    private MediaPlayer mediaPlayer;
    private ArrayList<Song>songList;
    private int resumePosition;

    private String songTitle;
    private static final int NOTIFY_ID=1;

    private boolean shuffle=false;
    private Random rand;

    private final IBinder musicBind = new MusicBinder();

    public MusicService() {
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        resumePosition=0;
//create player
        mediaPlayer = new MediaPlayer();
        initMediaPlayer();
        rand=new Random();
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source


        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void setSongList(ArrayList<Song> theSongs)
    {
        songList = theSongs;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return musicBind;
    }



    public void playSong()
    {
        mediaPlayer.reset();
        Song playSong = songList.get(resumePosition);
        songTitle=playSong.getTitle();
//get id
        long currSong = playSong.getId();
//set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);


        try{
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        mediaPlayer.prepareAsync();
    }

    public void setSong(int songIndex) {
        resumePosition=songIndex;
    }
    public void setShuffle(){
        if(shuffle) shuffle=false;
        else shuffle=true;
        playNext();
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mediaPlayer.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer)
    {
        mediaPlayer.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker("")
                .setOngoing(true)
                .setContentTitle("Playing")
               .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);

        Intent onPreparedIntent = new Intent("MEDIA_PLAYER_PREPARED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);
    }
    @Override // service lifecycle methods
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();;
            mediaPlayer.release();
        }
        stopForeground(true);

    }


    ///controller helper methods

    public int getPosn(){
        return mediaPlayer.getCurrentPosition();
    }

    public int getDur(){
        return mediaPlayer.getDuration();
    }

    public boolean isPng(){
        return mediaPlayer.isPlaying();
    }

    public void pausePlayer(){
        mediaPlayer.pause();
    }

    public void seek(int posn){
        mediaPlayer.seekTo(posn);
    }

    public void go(){
        mediaPlayer.start();
    }

    public void playPrev(){
        resumePosition--;
        if(resumePosition<0) resumePosition=songList.size()-1;
        playSong();
    }
    //skip to next
    public void playNext(){
        if(shuffle){
            int newSong = resumePosition;
            while(newSong==resumePosition){
                newSong=rand.nextInt(songList.size());
            }
            resumePosition=newSong;
        }
        else{
            resumePosition++;
            if(resumePosition >= songList.size()) resumePosition=0;
        }
        playSong();
    }
}
