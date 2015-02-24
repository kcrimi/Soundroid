package com.example.kevin.soundroid;

import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.kevin.soundroid.com.example.kevin.soundroid.soundcloud.SoundCloud;
import com.example.kevin.soundroid.com.example.kevin.soundroid.soundcloud.SoundCloudService;
import com.example.kevin.soundroid.com.example.kevin.soundroid.soundcloud.Track;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends ActionBarActivity implements SearchView.OnQueryTextListener{
    private static final String TAG = "MainActivity";

    private TracksAdapter mAdapter;
    private List<Track> mTracks;
    private TextView mSelectedTitle;
    private ImageView mSelectedThumbnail;
    private MediaPlayer mMediaPlayer;
    private ImageView mPlayerStateButton;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                toggleSongState();
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlayerStateButton.setImageResource(R.drawable.ic_play);
            }
        });

        Toolbar toolbar = (Toolbar)findViewById(R.id.player_toolbar);
        mSelectedTitle = (TextView)findViewById(R.id.selected_title);
        mSelectedThumbnail = (ImageView)findViewById(R.id.selected_thumbnail);

        mPlayerStateButton = (ImageView)findViewById(R.id.player_state);
        mPlayerStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSongState();
            }
        });

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.songs_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mTracks = new ArrayList<Track>();
        mAdapter = new TracksAdapter(this, mTracks);
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Track selectedTrack = mTracks.get(position);
                mSelectedTitle.setText(selectedTrack.getTitle());
                Picasso.with(MainActivity.this).load(selectedTrack.getArtworkURL()).into(mSelectedThumbnail);

                if (mMediaPlayer.isPlaying()){
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                }

                try {
                    mMediaPlayer.setDataSource(selectedTrack.getStreamURL() + "?client_id=" + SoundCloudService.CLIENT_ID);
                    mMediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        recyclerView.setAdapter(mAdapter);

        SoundCloudService service = SoundCloud.getService();
        service.getRecentSongs(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()), new Callback<List<Track>>() {
            @Override
            public void success(List<Track> tracks, Response response) {
                mTracks.clear();
                mTracks.addAll((tracks));
                Log.d(TAG, tracks.get(0).getTitle());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "Failed call: " + error.toString());
            }
        });
    }

    private void toggleSongState() {
        if (mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
            mPlayerStateButton.setImageResource(R.drawable.ic_play);
        }else{
            mMediaPlayer.start();
            mPlayerStateButton.setImageResource(R.drawable.ic_pause);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(TAG, query);
        mSearchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mMediaPlayer != null){
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mSearchView = (SearchView) menu.findItem(R.id.search_view).getActionView();
        mSearchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search_view) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
