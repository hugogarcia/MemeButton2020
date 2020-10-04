package com.garciaapps.botoesdememe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;

public final class MainActivity2 extends Fragment {
    private static final String TAG = "Músicas";
    public static MediaPlayer mp;
    public static int img[];
    public static int sons[], posicaoAudioPesquisa;
    public TabActivity tabActivity;
    public static int audioAnterior, position, posicaoPrimeiroItem;
    GestureDetectorCompat gestureDetectorCompat;
    public static GridView grid;
    public static Handler mHandler;
    public static Runnable runnable;
    SharedPreferences sharedPreferences;
    int posicaoDuploClique;
    DatabaseHelper databaseHelper;
    boolean favorito, share_Youtube, focoFragment, mStopHandler;
    TextView txtInicio, txtFim, txtInicioBackup, txtFimBackup;
    SeekBar progressBar, progressBarBackup;
    ImageView imgPlay;
    public static View view;
    Resources resources;
    public static boolean consultaPesquisa;
    public String itemSelecionado;
    public static Adaptador listAdapter;
    static String[] listaMusica;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view  = inflater.inflate(R.layout.activity_main2, container, false);
        tabActivity = new TabActivity();
        audioAnterior = 999999;
        share_Youtube = false;
        posicaoDuploClique = 0;
        sharedPreferences = this.getActivity().getSharedPreferences("com.garciaapps.botoesdememe", getContext().MODE_PRIVATE);
        favorito = sharedPreferences.getBoolean("Favorito", false);
        databaseHelper = new DatabaseHelper(getContext());
        resources = getResources();
        consultaPesquisa = false;

        carregarControlePlayer();

        if(favorito){
            carregarFavoritos();
            position = 0;
        }else {
            carregarTabela();
            carregarListaMusica();
            progressBarBackup= progressBar;
            txtInicioBackup = txtInicio;
            txtFimBackup = txtFim;
        }

        gestureDetectorCompat = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                float x = e.getX();
                float y = e.getY();
                posicaoDuploClique = grid.pointToPosition((int) x, (int) y);
                if(posicaoDuploClique >= 0){
                    registerForContextMenu(grid);
                    grid.showContextMenu();
                    unregisterForContextMenu(grid);
                }
                return true;
            }
        });

        listAdapter = new Adaptador(view.getContext(), img, listaMusica);
        grid = (GridView)view.findViewById(R.id.gridView);
        grid.setAdapter(listAdapter);
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                posicaoDuploClique = position;
                if(posicaoDuploClique >= 0){
                    registerForContextMenu(grid);
                    grid.showContextMenu();
                    unregisterForContextMenu(grid);
                }
                return true;
            }
        });

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int posicao, long id) {
                position = posicao;
                try {
                    if(position >= 0) {
                        play();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mp != null && mp.isPlaying()){
                    pause(false);
                }else{
                    play();
                }
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                if(mp != null) {
                    if(mp.getCurrentPosition() < mp.getDuration()) {
                        progressBar.setMax(mp.getDuration());
                        progressBar.setProgress(mp.getCurrentPosition());
                        txtFim.setText(milliSecondsToTimer(mp.getDuration()));
                        txtInicio.setText(milliSecondsToTimer(mp.getCurrentPosition()));
                    }
                }
                if (!mStopHandler) {
                    mHandler.postDelayed(this, 1000);
                }
            }
        };

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    if(mp != null && mp.isPlaying()) {
                        mp.seekTo(i);
                    }else if(mp == null){
                        seekBar.setProgress(0);
                    }else if(mp != null && !mp.isPlaying()){
                        mp.seekTo(i);
                        seekBar.setProgress(i);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setRetainInstance(true);
        return view;
    }

    private void carregarControlePlayer() {
        mHandler = new Handler();
        mStopHandler = false;
        txtInicio = (TextView)view.findViewById(R.id.txtInicio);
        txtFim = (TextView)view.findViewById(R.id.txtFim);
        imgPlay = (ImageView)view.findViewById(R.id.imgPlayPause);
        progressBar = (SeekBar) view.findViewById(R.id.seekBar);
        progressBar.getProgressDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        if(Build.VERSION.SDK_INT >= 16) {
            progressBar.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        }
    }

    public static void carregarTabela() {
        img = new int[]{
                R.drawable.cheiropneu,
                R.drawable.cheiropneu,
                R.drawable.cheiropneu,
                R.drawable.djazeitona,
                R.drawable.imggeral,
                R.drawable.memecaixao,
                R.drawable.coronavirus,
                R.drawable.dorimerato,
                R.drawable.imggeral,
                R.drawable.imggeral,
                R.drawable.canetaazul,
                R.drawable.ticole,
                R.drawable.querocafe,
                R.drawable.funkdeepweb,
                R.drawable.imggeral,
                R.drawable.universalgrito,
                R.drawable.cachorro,
                R.drawable.imggeral,
                R.drawable.naruto,
                R.drawable.cachorro,
                R.drawable.ricardomilos,
                R.drawable.negoney,
                R.drawable.imggeral,
                R.drawable.imggeral,
                R.drawable.alanzoka,
                R.drawable.oloquinho,
                R.drawable.imggeral,
                R.drawable.imggeral,
                R.drawable.tonemai,
                R.drawable.imggeral,
                R.drawable.imggeral,
                R.drawable.papacapim,
                R.drawable.ticole,
                R.drawable.imggeral,
                R.drawable.ohogas,
                R.drawable.imggeral,
                R.drawable.viniciusjunior,
                R.drawable.greg,
                R.drawable.buticodomiguel,
                R.drawable.missionpassed,
                R.drawable.palmadabota,
                R.drawable.harrypotter,
                R.drawable.baguncinha,
                R.drawable.minguado,
                R.drawable.minguado,
                R.drawable.evilmorty,
                R.drawable.imggeral,
                R.drawable.marcelocaju,
                R.drawable.jojotodynho,
                R.drawable.sanguedejesus,
                R.drawable.sanguedejesus,
                R.drawable.sanguedejesus,
                R.drawable.cocielo,
                R.drawable.didi,
                R.drawable.cocielo,
                R.drawable.imggeral,
                R.drawable.vitas,
                R.drawable.vitas,
                R.drawable.vitas,
                R.drawable.vitas,
                R.drawable.vitas,
                R.drawable.letitgo,
                R.drawable.mcmotoveia,
                R.drawable.mcmotoveia,
                R.drawable.xande,
                R.drawable.harrypotter,
                R.drawable.goku,
                R.drawable.ameno,
                R.drawable.mcdoguinha,
                R.drawable.mcchampion,
                R.drawable.sounou,
                R.drawable.imggeral,
                R.drawable.ppap,
                R.drawable.eunaoteperguntei,
                R.drawable.sonicbugado,
                R.drawable.imggeral,
                R.drawable.olhaeucombone,
                R.drawable.heman,
                R.drawable.xenhenhem,
                R.drawable.soundofsilence,
                R.drawable.soundofsilence,
                R.drawable.soundofsilence,
                R.drawable.soundofsilence,
                R.drawable.laricadosmoleque,
                R.drawable.yourebeautiful,
                R.drawable.imggeral,
                R.drawable.mcbinladen,
                R.drawable.motoprofeta,
                R.drawable.pumpedupkicks,
                R.drawable.xuxa,
                R.drawable.miseravel,
                R.drawable.imggeral,
                R.drawable.vaitomarnocu,
                R.drawable.sucodemaracuja,
                R.drawable.sucodemaracuja,
                R.drawable.sucodemaracuja,
                R.drawable.sucodemaracuja,
                R.drawable.sucodemaracuja,
                R.drawable.mcchampion,
                R.drawable.dilma,
                R.drawable.dilma,
                R.drawable.imggeral,
                R.drawable.irineu,
                R.drawable.irineu,
                R.drawable.irineu,
                R.drawable.chaves,
                R.drawable.mcmudinho,
                R.drawable.imggeral,
                R.drawable.maisde300reais,
                R.drawable.illuminati,
                R.drawable.serjao,
                R.drawable.mcbinladen,
                R.drawable.imggeral,
                R.drawable.sikerajunior,
                R.drawable.goiaba,
                R.drawable.inhegasshooting,
                R.drawable.venomextreme,
                R.drawable.velha,
                R.drawable.differentstrokes,
                R.drawable.tobecontinued,
                R.drawable.imggeral,
                R.drawable.carro,
                R.drawable.magrelinho,
                R.drawable.imggeral,
                R.drawable.titanic,
                R.drawable.maguilacantando,
                R.drawable.proerdfunk,
                R.drawable.winx,
                R.drawable.carretafuracao,
                R.drawable.imggeral,
                R.drawable.jooj,
                R.drawable.poxacrush,
                R.drawable.agaraga,
                R.drawable.andremarques,
                R.drawable.andremarques,
                R.drawable.rima,
                R.drawable.rhythm,
                R.drawable.tocapisadinha,
                R.drawable.lucas,
                R.drawable.lucas,
                R.drawable.imggeral,
                R.drawable.panificadora,
                R.drawable.trololo,
                R.drawable.imggeral,
                R.drawable.fodase,
                R.drawable.monstroolhoso,
                R.drawable.imggeral,
                R.drawable.flautatriste,
                R.drawable.harrypotter,
                R.drawable.vaidarmerda,
                R.drawable.cleiton,
                R.drawable.mexicano,
                R.drawable.mexicano,
                R.drawable.mexicano,
                R.drawable.naruto
        };

        sons = new int[]{
                R.raw.cheiropneu_somebody,
                R.raw.cheiropneu_dontstart,
                R.raw.cheiropneu_blindinglights,
                R.raw.djazeitona,
                R.raw.imggeral_cabeleleilaleila,
                R.raw.memecaixao,
                R.raw.coronavirus_remix,
                R.raw.dorime_remix,
                R.raw.imggeral_buttercup,
                R.raw.imggeral_bomdiaconsagrado,
                R.raw.canetaazul,
                R.raw.ticole_beat,
                R.raw.querocafe_musica,
                R.raw.funkdeepweb,
                R.raw.imggeral_babyimyours,
                R.raw.universalgrito,
                R.raw.cachorro_fiadaputa,
                R.raw.imggeral_boateazul,
                R.raw.naruto_flauta,
                R.raw.cachorro,
                R.raw.ricardomilos,
                R.raw.negoney_remix,
                R.raw.imggeral_beatdatempestade,
                R.raw.imggeral_beatrecuperacao,
                R.raw.alanzoka_remix,
                R.raw.oloquinho_trap,
                R.raw.imggeral_randandan,
                R.raw.imggeral_vucvuc,
                R.raw.tonemai_tachovendoremix,
                R.raw.imggeral_lionsleep,
                R.raw.imggeral_miniboi,
                R.raw.papacapim_remix,
                R.raw.ticole,
                R.raw.imggeral_sopademacaco,
                R.raw.ohogas,
                R.raw.soltaapisadinha,
                R.raw.viniciusjunior,
                R.raw.greg_taonasua,
                R.raw.buticodomiguel,
                R.raw.missionpassed,
                R.raw.palmadabota,
                R.raw.harrypotter_estourado,
                R.raw.baguncinha_holdon,
                R.raw.minguado_alone,
                R.raw.minguado_believer,
                R.raw.evilmorty,
                R.raw.imggeral_thousandyearsfunk,
                R.raw.marcelocaju,
                R.raw.jojotodynho_quetirofoiesse,
                R.raw.sanguedejesus,
                R.raw.sanguedejesus_newrules,
                R.raw.sanguedejesus_tempoderremix,
                R.raw.cocielo_molequemusica,
                R.raw.didi_tempaopaopao,
                R.raw.cocielo_aleluia,
                R.raw.imggeral_bandadjavu,
                R.raw.vitas,
                R.raw.vitas_7helement,
                R.raw.vitas_lindinho,
                R.raw.vitas_supermario,
                R.raw.vitas_espetacular,
                R.raw.letitgo_funk,
                R.raw.mcmotoveia_abusadamente,
                R.raw.mcmotoveia_letmeloveyou,
                R.raw.xande_brincadeiratemhora,
                R.raw.harrypotter_funk,
                R.raw.goku_funk,
                R.raw.ameno_remix,
                R.raw.mcdoguinha_imyours,
                R.raw.mcchampion_xxxampion,
                R.raw.sounou,
                R.raw.imggeral_run,
                R.raw.ppap_ihaveasampley,
                R.raw.eunaoteperguntei,
                R.raw.sonicbugado,
                R.raw.imggeral_saidafrenteremix,
                R.raw.olhaeucombone,
                R.raw.heman_whatsgoingon,
                R.raw.xenhenhem,
                R.raw.soundofsilence_rapdosolitario,
                R.raw.soundofsilence_jocelyn,
                R.raw.soundofsilence_whatchasay,
                R.raw.soundofsilence,
                R.raw.laricadosmoleque_tocomfome,
                R.raw.yourebeautiful,
                R.raw.imggeral_unidunite,
                R.raw.mcbinladen_shakeitbololo,
                R.raw.motoprofeta,
                R.raw.pumpedupkicks_funk,
                R.raw.xuxa_socobate,
                R.raw.miseravel_acertou,
                R.raw.imggeral_homemmacaco,
                R.raw.vaitomarnocu,
                R.raw.sucodemaracuja_inibiza,
                R.raw.sucodemaracuja_vaimaracujar,
                R.raw.sucodemaracuja_havana,
                R.raw.sucodemaracuja_original,
                R.raw.sucodemaracuja_runaway,
                R.raw.mcchampion,
                R.raw.dilma_estocandovento,
                R.raw.dilma_saudandoamandioca,
                R.raw.imggeral_tecladolindinho,
                R.raw.irineu_musica,
                R.raw.irineu_stressedout,
                R.raw.irineu_mandela,
                R.raw.chaves_funktriste,
                R.raw.mcmudinho,
                R.raw.imggeral_chupavaosuvaco,
                R.raw.maisde300reais,
                R.raw.illuminati_funk,
                R.raw.serjao_mclan,
                R.raw.mcbinladen_tatranquilo,
                R.raw.imggeral_tchutcha,
                R.raw.sikerajunior_maconhamusica,
                R.raw.goiaba,
                R.raw.inhegasshooting,
                R.raw.venomextreme_taficandoapertado,
                R.raw.velha_tachovendoai,
                R.raw.differentstrokes,
                R.raw.tobecontinued,
                R.raw.imggeral_painossofunk,
                R.raw.carro_dirigindofunk,
                R.raw.magrelinho,
                R.raw.imggeral_balaomagicofunk,
                R.raw.titanic_flauta,
                R.raw.maguilacantando,
                R.raw.proerdfunk,
                R.raw.winx,
                R.raw.carretafuracao,
                R.raw.imggeral_nuncamaisvoudormir,
                R.raw.jooj,
                R.raw.poxacrush,
                R.raw.agaraga_abertura,
                R.raw.andremarques_sampleguitarra,
                R.raw.andremarques_deixaosgarotos,
                R.raw.rima_teucu,
                R.raw.rhythm,
                R.raw.tocapisadinha,
                R.raw.lucas_sonimbleinem1,
                R.raw.lucas_sonimbleinem2,
                R.raw.imggeral_sweetdreams,
                R.raw.panificadora_musica,
                R.raw.trololo,
                R.raw.imggeral_funkingles,
                R.raw.fodase,
                R.raw.monstroolhoso,
                R.raw.imggeral_isolados,
                R.raw.flautatriste,
                R.raw.harrypotter,
                R.raw.vaidarmerda,
                R.raw.cleiton_chama,
                R.raw.tongo_pumpedupkicks,
                R.raw.mexicano_numb,
                R.raw.mexicano_chopsuey,
                R.raw.naruto
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mp != null) {
            pause(false);
        }
    }

    public void compartilhar(int som){
        File soundPath = new File(getContext().getFilesDir(), "sons");
        soundPath.mkdirs();
        File arquivoSom = new File(soundPath, "sound.mp3");

        InputStream inputStream;
        FileOutputStream fileOutputStream;
        try {
            inputStream = getResources().openRawResource(sons[som]);
            fileOutputStream = new FileOutputStream(arquivoSom);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }

            inputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri imageUri = Uri.parse("content://com.garciaapps.botoesdememe/sons/sound.mp3");

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.setType("audio/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, getString(R.string.compartilhar)));
        share_Youtube = true;
    }

    public void play(){
        if(favorito && sons.length == 0) return;

        if(position < 0 || position >= sons.length) position = 0;

        if(audioAnterior == position){
            if(mp != null) {
                mp.start();
                //mp.setLooping(true);
            }

            mHandler.post(runnable);
        }else {
            if(mp != null) {
                //tempoPlayer.interrupt();
                mp.stop();
                mp.reset();
                mp.release();
                mp = null;
                tabActivity.verificarAd();
            }
            if(consultaPesquisa && position > 0){
                position = 0;
                consultaPesquisa = false;
            }

            mp = MediaPlayer.create(getContext(), sons[position]);
            mp.start();
            //mp.setLooping(true);
            mHandler.post(runnable);
        }
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(mp != null) {
                    mp.start();
                }
            }
        });
        audioAnterior = position;
    }

    public void pause(boolean ads){
        if(ads){
            mHandler.removeCallbacks(runnable);
        }
        if(mp != null) {
            mp.setLooping(false);
            mp.pause();
            mHandler.removeCallbacks(runnable);
        }

        if(audioAnterior >= 0 && audioAnterior <= sons.length){
            position = audioAnterior;
        }else{
            position = 0;
        }
    }

    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(favorito){
            menu.add(getString(R.string.excluir));
        }else{
            menu.add(getString(R.string.adicionar));
            menu.add(getString(R.string.origemMeme));
        }
        menu.add(getString(R.string.compartilharMenu));
        menu.add(getString(R.string.baixar_audiomenu));

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(focoFragment) {
            itemSelecionado = item.getTitle().toString();
            if (item.getTitle() == getString(R.string.adicionar)) {
                addFavorito(img[posicaoDuploClique], sons[posicaoDuploClique], "M", listaMusica[posicaoDuploClique]);
            } else if (item.getTitle() == getString(R.string.compartilharMenu)) {
                if(checarPermissaoGravar()) {
                    compartilhar(posicaoDuploClique);
                }
            } else if (item.getTitle() == getString(R.string.excluir)) {
                pause(false);
                progressBar.setProgress(0);
                txtFim.setText(milliSecondsToTimer(0));
                txtInicio.setText(milliSecondsToTimer(0));
                String nomeSom = resources.getResourceEntryName(sons[posicaoDuploClique]);
                databaseHelper.delete(nomeSom);
                carregarFavoritos();
                grid.setAdapter(new Adaptador(getContext(), img, listaMusica));
                toastMessage(getString(R.string.deletarAudio));
                if (mp != null) {
                    mp.stop();
                    mp.reset();
                    mp.release();
                    mp = null;
                }
                position = 0;
                audioAnterior = 999999;
            }else if(item.getTitle() == getString(R.string.origemMeme)){
                buscarYoutube(posicaoDuploClique);
            }else if(item.getTitle() == getString(R.string.baixar_audiomenu)){
                if(checarPermissaoGravar()) {
                    salvarAudio(posicaoDuploClique);
                }
            }
        }

        return super.onContextItemSelected(item);
    }

    private void buscarYoutube(int posicaoDuploClique) {
        if(sons.length <= 1){
            posicaoDuploClique = posicaoAudioPesquisa;
        }
        if(isAppInstalled("com.google.android.youtube")) {
            Intent intent = new Intent(Intent.ACTION_SEARCH);
            intent.setPackage("com.google.android.youtube");
            intent.putExtra("query", tabActivity.buscarNomeMusica(posicaoDuploClique));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else {
            String url = "https://www.youtube.com/results?search_query="+tabActivity.buscarNomeMusica(posicaoDuploClique);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }
        share_Youtube = true;
    }

    public void addFavorito(int imagem, int som, String tipo, String descricao){
        String nomeImg = resources.getResourceEntryName(imagem);
        String nomeSom = resources.getResourceEntryName(som);
        if(!databaseHelper.checarAudio(nomeSom)) {
            boolean insertData = databaseHelper.addData(nomeImg, nomeSom, tipo, descricao);

            if (insertData) {
                toastMessage(getString(R.string.addSucesso));
            } else {
                toastMessage(getString(R.string.addFalha));
            }
        }else{
            toastMessage(getString(R.string.addVerificar));
        }
    }

    public void carregarFavoritos(){
        Cursor data = databaseHelper.getData("M");
        ArrayList<String> imagem = new ArrayList<>();
        ArrayList<String> som = new ArrayList<>();
        ArrayList<String> descricao = new ArrayList<>();
        while (data.moveToNext()){
            imagem.add(data.getString(1));
            som.add(data.getString(2));
            descricao.add(data.getString(4));
        }

        sons = new int[som.size()];
        img = new int[imagem.size()];
        listaMusica = new String[descricao.size()];
        String nomeImg;
        String nomeSom;
        String descricaoSom;
        for (int i = 0; i < imagem.size(); i++){
            nomeImg = imagem.get(i);
            nomeSom = som.get(i);
            descricaoSom = descricao.get(i);
            if(resources.getIdentifier(nomeSom, "raw", "com.garciaapps.botoesdememe") > 0) {
                img[i] = resources.getIdentifier(nomeImg, "drawable", "com.garciaapps.botoesdememe");
                sons[i] = resources.getIdentifier(nomeSom, "raw", "com.garciaapps.botoesdememe");
                listaMusica[i] = descricaoSom;
            }
        }

        if(imagem.size() < 1) {
            if (mp != null) {
                mp.stop();
                mp.reset();
                mp.release();
                mp = null;
            }
        }

    }

    public void toastMessage(String msg){
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void toastMessageLong(String msg){
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!favorito){
            carregarTabela();
            carregarControlePlayer();

            progressBar = progressBarBackup;
            txtInicio = txtInicioBackup;
            txtFim = txtFimBackup;

            if(posicaoDuploClique > 0){
                grid.setSelection(posicaoDuploClique);
                //share_Youtube = false;
            }

        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            focoFragment = true;
        }else{
            focoFragment = false;
        }
    }

    public void montarAudioPesquisa(int posicao){
        consultaPesquisa = true;
        audioAnterior = 9999999;
        posicaoAudioPesquisa = posicao;
        carregarTabela();
        int idAudio = sons[posicao];
        int idImagem = img[posicao];
        carregarListaMusica();
        String nomeAudio = listaMusica[posicao];

        sons = new int[]{idAudio};
        img = new int[]{idImagem};
        listaMusica = new String[]{nomeAudio};

        if(mp != null) {
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
            mp = MediaPlayer.create(this.view.getContext(), sons[0]);
            position = 0;
        }

        this.grid.setAdapter(new Adaptador(this.view.getContext(), img, listaMusica));
    }

    public void desmontarAudioPesquisa(){
        consultaPesquisa = false;
        audioAnterior = 9999999;
        carregarTabela();
        carregarListaMusica();
        if(mp != null) {
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
            position = 0;
        }
        this.grid.setAdapter(new Adaptador(this.view.getContext(), img, listaMusica));
    }

    protected boolean isAppInstalled(String packageName) {
        Intent mIntent = getContext().getPackageManager().getLaunchIntentForPackage(packageName);
        if (mIntent != null) {
            return true;
        }
        else {
            return false;
        }
    }

    public void salvarAudio(int som){

        final InputStream inputStream;
        final FileOutputStream fileOutputStream;
        try {
            File criarDir = new File(Environment.getExternalStorageDirectory(), "Memes Baixados");
            if(!criarDir.exists()){
                if(!criarDir.mkdirs()){
                    Log.d("App", "failed to create directory");
                }
            }
            String nomeAudio = retirarCaracteres(listaMusica[som]) +".mp3";
            File arquivo = new File(criarDir, nomeAudio);
            inputStream = getResources().openRawResource(sons[som]);
            fileOutputStream = new FileOutputStream(arquivo);

            final byte buffer[] = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer, 0, 1024)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }

            inputStream.close();
            fileOutputStream.close();

            scanFile(arquivo.toString());
            toastMessage(getString(R.string.download_audio));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String retirarCaracteres(String nome){
        nome = nome.replace("?", "");
        nome = nome.replace("!", "");
        return nome;
    }

    private void scanFile(String path) {

        MediaScannerConnection.scanFile(getContext(),
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });
    }

    public  boolean checarPermissaoGravar() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(itemSelecionado == getString(R.string.compartilharMenu)){
                        compartilhar(posicaoDuploClique);
                    }else {
                        salvarAudio(posicaoDuploClique);
                    }
                } else {
                    toastMessageLong(getString(R.string.permissaoBloqueada));
                }
                break;
        }
    }

    public void carregarListaMusica() {
        listaMusica = new String[]{
                "Cheiro de Somebody That I Used to Know",
                "Cheiro de Don't Start Now",
                "Cheiro de Blinding Lights",
                "Blinding Azeitona",
                "Cabeleleila Leila",
                "Meme do caixão",
                "Corona virus brega funk",
                "Rato dorime",
                "Buttercup",
                "Funk bom dia meu consagrado",
                "Caneta azul",
                "Beat ticolé",
                "Quero café - Remix Atilakw",
                "Funk deep web",
                "Baby I'm yours meme",
                "Intro universal grito",
                "Cachorrinho fia da p*ta",
                "Boate azul meme",
                "Naruto flauta desafinada",
                "Música cachorro chorando",
                "Música Ricardo Milos",
                "Beat do Nego Ney",
                "Beat da Tempestade",
                "Beat da Recuperação",
                "O nome dele é Alan - funk",
                "Oh lokinho meu trap",
                "Randandandan funk",
                "Vuc Vuc remix",
                "Chovendo e repanguelejando remix",
                "The lion sleeps tonight",
                "Mini boi",
                "Papa capim - Remix atilakw",
                "Ticolé",
                "Sopa de macaco",
                "Oh o gás",
                "Solta a pisadinha forró",
                "Ela é uma boa menina",
                "Cara ela tá tão na sua funk",
                "Sai cocozinho do butico do miguel",
                "Mission passed GTA",
                "É na palma da bota",
                "Harry Potter estourado",
                "Hold On - Baguncinha",
                "Alone - Minguado",
                "Believer - Minguado",
                "Tema Evil Morty",
                "A Thousand Years Funk",
                "Marcelo e o suco de caju",
                "Que tiro foi esse",
                "Sangue de Jesus tem poder",
                "New Rules - Sangue de Jesus",
                "Agora tem poder",
                "Moleque neutro - Júlio Cocielo",
                "No céu tem pão pão pão",
                "Aleluia - Júlio Cocielo",
                "Banda Djavu meme",
                "Vitas - grito",
                "7h Element - Vitas",
                "Vitas lindinho 2009",
                "Super Vitas World",
                "Vitas espetacular",
                "Let it go funk",
                "Abusadamente - MC Moto Véia",
                "Let me love you - MC Moto Véia",
                "Brincadeira tem hora",
                "Harry Potter funk",
                "Dragon Ball Super Funk",
                "Ameno funk remix",
                "I'm yours - Mc Doguinha",
                "XXXampion",
                "Sou nou",
                "Run - Awolnation",
                "I have a sampley",
                "Eu não te perguntei",
                "Sanic tema",
                "Sai da frente satanás remix",
                "Olha eu com boné",
                "What's going on He-man",
                "Gilderlan xenhenhém",
                "Rap do solitário",
                "Jocelyn flores",
                "whatcha say",
                "Sound of silence funk",
                "Tô com fome - Larica dos mulekes",
                "You're beautiful - Daniel Gavião",
                "Uni duni tê funk",
                "Shake it bololo",
                "Moto dos profetas",
                "Pumped up kicks funk",
                "Soco bate funk - Xuxa",
                "Acertou miserável remix",
                "Rap do homem macaco",
                "Vai tomar no c* música",
                "Suco de maracuja - I Took a Pill in Ibiza",
                "Agora vai maracujar",
                "Havana - Suco de maracuja",
                "Suco de maracujá",
                "Suco de maracujá runaway",
                "Mc Champion  - Onde chego paro tudo",
                "Estocando vento - Dilma",
                "Saudação à mandioca - Dilma",
                "Teclado lindinho 2009",
                "Irineu remix",
                "Stressed out - Irineu",
                "Irineu mandela",
                "Chaves funk triste",
                "Mc mudinho",
                "Chupava até o suvaco",
                "É mais de 300 reais remix",
                "Illuminati funk",
                "Serjão paródia rabetão",
                "Ta tranquilo - Bin Laden",
                "Tchu tcha",
                "É maconha doido - Música Sikera",
                "Música da goiaba",
                "Shooting Stars inhegas",
                "Ta ficando apertado remix - Venom",
                "Ta chovendo aí?",
                "Abertura Different Strokes",
                "To be continued",
                "Oração pai nosso funk",
                "Dirigindo meu carro funk",
                "Magrelinho",
                "Balão mágico funk",
                "Titanic flauta",
                "Dei um soco na preguiça - Maguila",
                "Proerd funk",
                "Clube das Winx Funk",
                "Carreta furacão",
                "Nunca mais eu vou dormir",
                "Jooj chaves",
                "Poxa crush",
                "Sample de guitarra GTA",
                "Sample de guitarra",
                "Deixa os garoto brincar",
                "C*aralho sem nexo",
                "Rhythm of the night",
                "Toca pisadinha",
                "Sonim bleinem Diff'rent Strokes",
                "Sonim bleinem All Star",
                "Sweet Dreams",
                "Panificadora alfa",
                "Trololo",
                "Funk inglês",
                "Música do Foda-se",
                "Monstro de um olho só",
                "Música Isolados",
                "Flauta triste",
                "Harry potter flauta",
                "Vai dar merda",
                "Chama - Cleiton Rasta",
                "Tongo - Pumped up kicks",
                "Numb Tongo",
                "Chop Suey Tongo",
                "Naruto funk"
        };

    }

}
