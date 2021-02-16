package com.example.andres.audiolibros.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.example.andres.audiolibros.Aplicacion;
import com.example.andres.audiolibros.Libro;
import com.example.andres.audiolibros.MainActivity;
import com.example.andres.audiolibros.MyServicio;
import com.example.andres.audiolibros.R;

import java.io.IOException;

public class DetalleFragment extends Fragment implements View.OnTouchListener, MediaPlayer.OnPreparedListener,
        MediaController.MediaPlayerControl {

    public static String ARG_ID_LIBRO = "id_libro";
    MediaPlayer mediaPlayer;
    MediaController mediaController;
    String guardar="";


    @Override public View onCreateView(LayoutInflater inflador, ViewGroup
            contenedor, Bundle savedInstanceState) {
        View vista = inflador.inflate(R.layout.fragment_detalle,
                contenedor, false);
        Bundle args = getArguments();
        if (args != null) {
            int position = args.getInt(ARG_ID_LIBRO);
            ponInfoLibro(position, vista);
        } else {
            ponInfoLibro(0, vista);
        }
        return vista;
    }

    private void ponInfoLibro(int id, View vista) {
        Libro libro = ((Aplicacion) getActivity().getApplication()).getVectorLibros().elementAt(id);
        ((TextView) vista.findViewById(R.id.titulo)).setText(libro.titulo);
        ((TextView) vista.findViewById(R.id.autor)).setText(libro.autor);
        ((ImageView) vista.findViewById(R.id.portada))
                .setImageResource(libro.recursoImagen);
        vista.setOnTouchListener(this);
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaController = new MediaController(getActivity());
        guardar = libro.urlAudio;
        Uri audio = Uri.parse(libro.urlAudio);
        try {
            mediaPlayer.setDataSource(getActivity(), audio);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e("Audiolibros", "ERROR: No se puede reproducir " + audio, e);
        }
    }

    public void ponInfoLibro(int id) {
            ponInfoLibro(id, getView());
        }


    MyServicio servicioLibros;
    boolean mBound = false;

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d("Audiolibros", "Entramos en onPrepared de MediaPlayer");

        SharedPreferences preferencias = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preferencias.getBoolean("pref_autoreproducir", true)) {
            Intent serviceIntent = new Intent(getActivity().getApplicationContext(), MyServicio.class);
            serviceIntent.putExtra("inputExtra", guardar);
            ContextCompat.startForegroundService(getActivity(), serviceIntent);
            //bindService();
        }

        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(getView().findViewById(R.id.fragment_detalle));
        //mediaController.setPadding(0, 0, 0,110);
        mediaController.setEnabled(true);
        mediaController.show();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyServicio.MiBinder miBinder = (MyServicio.MiBinder) service;
            servicioLibros = miBinder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };


    void bindService() {
        getActivity().bindService(new Intent(getActivity(), MyServicio.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    void unBindService() {
        if (mBound == true) {
            getActivity().unbindService(serviceConnection);
            mBound = false;
        }
    }


    @Override public boolean onTouch(View vista, MotionEvent evento) {
        mediaController.show();
        return false;
    }
    @Override public void onStop() {
        mediaController.hide();
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
        } catch (Exception e) {
            Log.d("Audiolibros", "Error en mediaPlayer.stop()");
        }
        super.onStop();
    }

    @Override public boolean canPause() {
        return true;
    }
    @Override public boolean canSeekBackward() {
        return true;
    }
    @Override public boolean canSeekForward() {
        return true;
    }
    @Override public int getBufferPercentage() {
        return 0;
    }

    @Override public int getCurrentPosition() {
        try {
            return mediaPlayer.getCurrentPosition();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override public int getDuration() {
        return mediaPlayer.getDuration();
    }
    @Override public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }
    @Override public void pause() {
        mediaPlayer.pause();
    }
    @Override public void seekTo(int pos) {
        mediaPlayer.seekTo(pos);
    }
    @Override public void start() {
        mediaPlayer.start();
    }
    @Override public int getAudioSessionId() {
        return 0;
    }

    @Override public void onResume(){
        DetalleFragment detalleFragment = (DetalleFragment)
                getFragmentManager().findFragmentById(R.id.detalle_fragment);
        if (detalleFragment == null ) {
            ((MainActivity) getActivity()).mostrarElementos(false);
        }
        super.onResume();
    }
}

