package com.garciaapps.botoesdememe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;

public class MainActivity extends Fragment{
    private static final String TAG = "Instant buttons";
    public static MediaPlayer mp;
    public static  int sons[], posicaoAudioPesquisa, posicaoPrimeiroItem;
    public static int[] img;
    public TabActivity tabActivity;
    GestureDetectorCompat gestureDetectorCompat;
    public static GridView grid;
    SharedPreferences sharedPreferences;
    int posicaoDuploClique, audioAnterior;
    DatabaseHelper databaseHelper;
    boolean favorito, focoFragment, share_Youtube;
    Resources resources;
    public static View view;
    public static boolean consultaPesquisa;
    public String itemSelecionado;
    public static Adaptador listAdapter;
    static String[] listaAudio;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view  = inflater.inflate(R.layout.activity_main, container, false);
        tabActivity = new TabActivity();
        audioAnterior = 999999;
        share_Youtube = false;
        posicaoDuploClique = 0;
        sharedPreferences = this.getActivity().getSharedPreferences("com.garciaapps.botoesdememe", getContext().MODE_PRIVATE);
        favorito = sharedPreferences.getBoolean("Favorito", false);
        databaseHelper = new DatabaseHelper(getContext());
        resources = getResources();
        consultaPesquisa = false;

        if(favorito){
            carregarFavoritos();
        }else {
            carregarTabela();
            carregarListaAudio();
        }

        /*gestureDetectorCompat = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener(){
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
                return false;
            }
        });*/


        listAdapter = new Adaptador(view.getContext(), img, listaAudio);
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
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                posicaoDuploClique = position;
                try {
                    if(position >= 0) {
                        if(position != audioAnterior) {
                            tabActivity.verificarAd();
                        }
                        if(consultaPesquisa){
                            position = 0;
                            consultaPesquisa = false;
                        }

                        if(mp != null && mp.isPlaying() && position == audioAnterior){
                            pause();
                        }else{
                            pause();
                            mp = MediaPlayer.create(view.getContext(), sons[position]);
                            mp.start();
                        }

                        audioAnterior = position;
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });

        setRetainInstance(true);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mp != null) {
            mp.setLooping(false);
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
        }
    }

    public static void carregarTabela(){
        img = new int[]{
                R.drawable.xandao,
                R.drawable.xandao,
                R.drawable.xandao,
                R.drawable.hilorena,
                R.drawable.nobru,
                R.drawable.picapau,
                R.drawable.tiringa,
                R.drawable.velhomiau,
                R.drawable.aguacocalatao,
                R.drawable.aguacocalatao,
                R.drawable.aguacocalatao,
                R.drawable.aguacocalatao,
                R.drawable.imggeral,
                R.drawable.imggeral,
                R.drawable.imggeral,
                R.drawable.seboso,
                R.drawable.tigas,
                R.drawable.coronavirus,
                R.drawable.imggeral,
                R.drawable.imggeral,
                R.drawable.andrevilao,
                R.drawable.andrevilao,
                R.drawable.andrevilao,
                R.drawable.andrevilao,
                R.drawable.gatinha,
                R.drawable.jacquin,
                R.drawable.jacquin,
                R.drawable.jacquin,
                R.drawable.temcomonaoouvir,
                R.drawable.imggeral,
                R.drawable.bambam,
                R.drawable.imggeral,
                R.drawable.gordinhorindo,
                R.drawable.neiva,
                R.drawable.neiva,
                R.drawable.neiva,
                R.drawable.neiva,
                R.drawable.bolsonaro,
                R.drawable.pescotapa,
                R.drawable.dilma,
                R.drawable.olhaso,
                R.drawable.picapau,
                R.drawable.fatality,
                R.drawable.leo,
                R.drawable.agaraga,
                R.drawable.caracol,
                R.drawable.imggeral,
                R.drawable.roblox,
                R.drawable.pele,
                R.drawable.vaisentar,
                R.drawable.vaisentar,
                R.drawable.vaisentar,
                R.drawable.tresreais,
                R.drawable.zecaurubu,
                R.drawable.imggeral,
                R.drawable.oloquinho,
                R.drawable.oloquinho,
                R.drawable.oloquinho,
                R.drawable.oloquinho,
                R.drawable.ofiladaputa,
                R.drawable.leo,
                R.drawable.imggeral,
                R.drawable.rusbe,
                R.drawable.vemtranquilo,
                R.drawable.vemtranquilo,
                R.drawable.hermanoteu,
                R.drawable.imggeral,
                R.drawable.cairnaporrada,
                R.drawable.bettina,
                R.drawable.alborguetti,
                R.drawable.capitaonascimento,
                R.drawable.negoney,
                R.drawable.negoney,
                R.drawable.patrick,
                R.drawable.silvio,
                R.drawable.jabiraca,
                R.drawable.tonemai,
                R.drawable.tonemai,
                R.drawable.seuousado,
                R.drawable.imggeral,
                R.drawable.palmas,
                R.drawable.sobrevivencia,
                R.drawable.imggeral,
                R.drawable.imggeral,
                R.drawable.imggeral,
                R.drawable.umah,
                R.drawable.umah,
                R.drawable.hulk,
                R.drawable.whindersson,
                R.drawable.botaaqui,
                R.drawable.chaves,
                R.drawable.praqueviolencia,
                R.drawable.zabuza,
                R.drawable.nandomoura,
                R.drawable.imggeral,
                R.drawable.dimiscosta,
                R.drawable.tonapraia,
                R.drawable.tonapraia,
                R.drawable.cetadoido,
                R.drawable.michael,
                R.drawable.gamesedu,
                R.drawable.elenaovai,
                R.drawable.imggeral,
                R.drawable.daciolo,
                R.drawable.vemprofut,
                R.drawable.vemprofut,
                R.drawable.vemprofut,
                R.drawable.vozdegalinha,
                R.drawable.vozdegalinha,
                R.drawable.daciolo,
                R.drawable.vegeta,
                R.drawable.eversonzoio,
                R.drawable.ronaldinho,
                R.drawable.tomanocu,
                R.drawable.eversonzoio,
                R.drawable.papaco,
                R.drawable.garrafa,
                R.drawable.garrafa,
                R.drawable.eutambem,
                R.drawable.homemaranha,
                R.drawable.tiririca,
                R.drawable.baguncinha,
                R.drawable.babydobiruleibe,
                R.drawable.babydobiruleibe,
                R.drawable.bigodinfinin,
                R.drawable.imggeral,
                R.drawable.uganda,
                R.drawable.certamentequesim,
                R.drawable.galvaobueno,
                R.drawable.eversonzoio,
                R.drawable.tedouumafacadona,
                R.drawable.queondaeessa,
                R.drawable.imggeral,
                R.drawable.olhaobichovindo,
                R.drawable.bomdiavaitomarnocu,
                R.drawable.emesmoe,
                R.drawable.imggeral,
                R.drawable.pingu,
                R.drawable.umaperdadetempo,
                R.drawable.vaitomarnoseurabo,
                R.drawable.olhaopastel,
                R.drawable.cocielo,
                R.drawable.reallynigga,
                R.drawable.hagay,
                R.drawable.eitadesgraca,
                R.drawable.imggeral,
                R.drawable.eunaosei,
                R.drawable.fruet,
                R.drawable.noice,
                R.drawable.imggeral,
                R.drawable.wow,
                R.drawable.drauzio,
                R.drawable.imggeral,
                R.drawable.misty,
                R.drawable.mcmotoveia,
                R.drawable.seupaitemgonorreia,
                R.drawable.imggeral,
                R.drawable.neto,
                R.drawable.neto,
                R.drawable.sumiutete,
                R.drawable.ciladabino,
                R.drawable.vindiesel,
                R.drawable.brksedu,
                R.drawable.adriano,
                R.drawable.sefodeugta,
                R.drawable.mamilos,
                R.drawable.edois,
                R.drawable.goku,
                R.drawable.mentira,
                R.drawable.eitacuzao,
                R.drawable.bolapanico,
                R.drawable.falafiote,
                R.drawable.didi,
                R.drawable.mariadorosario,
                R.drawable.zuzu,
                R.drawable.cauemoura,
                R.drawable.silviopanico,
                R.drawable.donairene,
                R.drawable.imggeral,
                R.drawable.vingancapernilongo,
                R.drawable.homer,
                R.drawable.eaimanobrown,
                R.drawable.soubemmacho,
                R.drawable.voucagarnacalca,
                R.drawable.joaoamerica,
                R.drawable.senhorpoliglota,
                R.drawable.senhorpoliglota,
                R.drawable.senhorpoliglota,
                R.drawable.imggeral,
                R.drawable.lionman,
                R.drawable.paodebatata,
                R.drawable.mustafary,
                R.drawable.imggeral,
                R.drawable.filmaefala,
                R.drawable.filmaefala,
                R.drawable.filmaefala,
                R.drawable.marmotagritando,
                R.drawable.nuncanemvi,
                R.drawable.nuncanemvi,
                R.drawable.nuncanemvi,
                R.drawable.surprisemotherfucker,
                R.drawable.vegeta,
                R.drawable.chaves,
                R.drawable.chaves,
                R.drawable.bocafoifeitapracomer,
                R.drawable.turndown,
                R.drawable.imggeral,
                R.drawable.julius,
                R.drawable.reidasserpente,
                R.drawable.carlosalberto,
                R.drawable.tiririca,
                R.drawable.jesusjardineiro,
                R.drawable.fallen,
                R.drawable.eoque,
                R.drawable.capitaonascimento,
                R.drawable.capitaonascimento,
                R.drawable.joaodanica,
                R.drawable.joaodanica,
                R.drawable.joaodanica,
                R.drawable.joaodanica,
                R.drawable.leandrohassum,
                R.drawable.hmmboiola,
                R.drawable.velhanada,
                R.drawable.sandra,
                R.drawable.ementira,
                R.drawable.trivago,
                R.drawable.trivago,
                R.drawable.allahuakbar,
                R.drawable.imggeral,
                R.drawable.aloha,
                R.drawable.dollynho,
                R.drawable.igorneragay,
                R.drawable.crash,
                R.drawable.rolezeiras,
                R.drawable.aiquedelicia,
                R.drawable.papacapim,
                R.drawable.oimeuchapa,
                R.drawable.omaewa,
                R.drawable.luangameplay_minecraft,
                R.drawable.imggeral,
                R.drawable.senhora,
                R.drawable.quevelocidadejunior,
                R.drawable.eusouseupai,
                R.drawable.rodolfo,
                R.drawable.illuminati,
                R.drawable.celoucocachoeira,
                R.drawable.celoucocachoeira,
                R.drawable.giovana,
                R.drawable.whatafuck,
                R.drawable.sanduiche,
                R.drawable.pele,
                R.drawable.acelera,
                R.drawable.acelera,
                R.drawable.maguilacantando,
                R.drawable.imggeral,
                R.drawable.juao,
                R.drawable.juao,
                R.drawable.juao,
                R.drawable.juao,
                R.drawable.juao,
                R.drawable.sikerajunior,
                R.drawable.sikerajunior,
                R.drawable.joaogordo,
                R.drawable.divertidamente,
                R.drawable.poderosocastiga,
                R.drawable.hastad,
                R.drawable.hastad,
                R.drawable.imggeral,
                R.drawable.soufarao,
                R.drawable.luanaotemporta,
                R.drawable.homemfoiaosol,
                R.drawable.elementocaradepau,
                R.drawable.davyjones,
                R.drawable.davyjones,
                R.drawable.davyjones,
                R.drawable.davyjones,
                R.drawable.davyjones,
                R.drawable.davyjones,
                R.drawable.davyjones,
                R.drawable.davyjones,
                R.drawable.seubuceta,
                R.drawable.formigamorri,
                R.drawable.didi,
                R.drawable.yoda,
                R.drawable.whindersson,
                R.drawable.nossaquebosta,
                R.drawable.professor,
                R.drawable.bolsonaro,
                R.drawable.bolsonaro,
                R.drawable.pepino,
                R.drawable.nandomoura,
                R.drawable.nandomoura,
                R.drawable.nandomoura,
                R.drawable.away,
                R.drawable.away,
                R.drawable.imggeral,
                R.drawable.lucasnetto,
                R.drawable.cetemdemencia,
                R.drawable.picapau,
                R.drawable.deusessaoreais,
                R.drawable.imggeral,
                R.drawable.jaacaboujessica,
                R.drawable.lucascsgo,
                R.drawable.lucascsgo,
                R.drawable.naoseipreso,
                R.drawable.pesadao,
                R.drawable.feijaotorpedo,
                R.drawable.fred,
                R.drawable.imggeral,
                R.drawable.nuncamexa,
                R.drawable.saborsorvete,
                R.drawable.efistaile,
                R.drawable.eitaporra,
                R.drawable.familyguy,
                R.drawable.humemesmo,
                R.drawable.seubuceta,
                R.drawable.silvio,
                R.drawable.silvio,
                R.drawable.silvio,
                R.drawable.xuxa,
                R.drawable.agoraentendi,
                R.drawable.cepodemadeira,
                R.drawable.cepodemadeira,
                R.drawable.amandaklein,
                R.drawable.marco,
                R.drawable.imggeral,
                R.drawable.matiformiga,
                R.drawable.ouch,
                R.drawable.filosofopiton,
                R.drawable.imggeral,
                R.drawable.react,
                R.drawable.react,
                R.drawable.rima,
                R.drawable.rima,
                R.drawable.racanegra,
                R.drawable.racanegra,
                R.drawable.imggeral,
                R.drawable.picapau,
                R.drawable.ohsefodeu,
                R.drawable.naoentendi,
                R.drawable.imggeral,
                R.drawable.jubileu,
                R.drawable.johncena,
                R.drawable.bambam,
                R.drawable.bambam,
                R.drawable.bambam,
                R.drawable.bambam,
                R.drawable.felipe,
                R.drawable.felipe,
                R.drawable.felipe,
                R.drawable.felipe,
                R.drawable.galocego,
                R.drawable.galocego,
                R.drawable.galocego,
                R.drawable.galocego,
                R.drawable.galocego,
                R.drawable.galocego,
                R.drawable.imggeral,
                R.drawable.imggeral,
                R.drawable.maluco,
                R.drawable.medelicenca,
                R.drawable.papaco,
                R.drawable.papaco,
                R.drawable.papaco,
                R.drawable.papaco,
                R.drawable.papaco,
                R.drawable.papaco,
                R.drawable.taumaporra,
                R.drawable.viadaobonito,
                R.drawable.treta,
                R.drawable.tenho2real,
                R.drawable.tavabom,
                R.drawable.quecachorro,
                R.drawable.medepapai,
                R.drawable.gatomiopia,
                R.drawable.faustao_errou,
                R.drawable.faustao_errou,
                R.drawable.etetra,
                R.drawable.erroufeio,
                R.drawable.cleber_negros,
                R.drawable.luisroberto,
                R.drawable.voceeburro,
                R.drawable.tomacachaca,
                R.drawable.taldomula,
                R.drawable.quemerdahein,
                R.drawable.putafalta,
                R.drawable.paichorando,
                R.drawable.morrediabo,
                R.drawable.morrediabo,
                R.drawable.morrediabo,
                R.drawable.imggeral,
                R.drawable.beijanaboca,
                R.drawable.alborguetti,
                R.drawable.alborguetti,
                R.drawable.alborguetti,
                R.drawable.alborguetti,
                R.drawable.alborguetti,
                R.drawable.agaraga,
                R.drawable.aipaipara,
                R.drawable.bope,
                R.drawable.bope,
                R.drawable.marciocanuto,
                R.drawable.fazourro,
                R.drawable.queviagem,
                R.drawable.borracha,
                R.drawable.coerapaziada,
                R.drawable.douglas,
                R.drawable.douglas,
                R.drawable.serjao,
                R.drawable.serjao,
                R.drawable.serjao,
                R.drawable.serjao,
                R.drawable.serjao,
                R.drawable.serjao,
                R.drawable.serjao,
                R.drawable.serjao,
                R.drawable.serjao,
                R.drawable.foiotimo,
                R.drawable.foiotimo,
                R.drawable.eupito,
                R.drawable.jeremias,
                R.drawable.irineu,
                R.drawable.leo,
                R.drawable.leo,
                R.drawable.caceteagulha,
                R.drawable.caceteagulha,
                R.drawable.marilene,
                R.drawable.miau,
                R.drawable.miseravel,
                R.drawable.naosei,
                R.drawable.panificadora,
                R.drawable.panificadora,
                R.drawable.pesadao,
                R.drawable.pesadao,
                R.drawable.bemlouco,
                R.drawable.bemlouco,
                R.drawable.cagadodefome,
                R.drawable.cagadodefome,
                R.drawable.quedelicia,
                R.drawable.piruleta,
                R.drawable.piruleta,
                R.drawable.querocafe,
                R.drawable.querocafe,
                R.drawable.imggeral,
                R.drawable.rolezeiras,
                R.drawable.rolezeiras,
                R.drawable.rolezeiras2,
                R.drawable.tapita,
                R.drawable.tapita,
                R.drawable.tapita,
                R.drawable.tiro,
                R.drawable.vaifilhao,
                R.drawable.velhoaiai,
                R.drawable.gordinhoesoh,
        };

        sons = new int[]{
                R.raw.xandao_sempressao,
                R.raw.xandao_escuridao,
                R.raw.xandao_biceps,
                R.raw.hilorena,
                R.raw.nobru,
                R.raw.picapau_lavamosnos,
                R.raw.tiringa,
                R.raw.velhomiau,
                R.raw.aguacocalatao,
                R.raw.aguacocalatao_mary,
                R.raw.aguacocalatao_gringo,
                R.raw.aguacocalatao_rackcheque,
                R.raw.imggeral_manotuegay,
                R.raw.imggeral_manotuegay_tuquedeixa,
                R.raw.imggeral_manotuegay_testando,
                R.raw.seboso,
                R.raw.tigas_fala300,
                R.raw.coronavirus,
                R.raw.imggeral_risadaladrao,
                R.raw.imggeral_fazerchurrasco,
                R.raw.andrevilao_buduvieser,
                R.raw.andrevilao_ninguemgosta,
                R.raw.andrevilao_tocomdepressao,
                R.raw.andrevilao_taardendo,
                R.raw.gatinha,
                R.raw.jacquin_vergonhaprofissao,
                R.raw.jacquin_calaboca,
                R.raw.jacquin_tameouvindo,
                R.raw.temcomonaoouvir,
                R.raw.imggeral_voucontarpromeupai,
                R.raw.bambam_tasaindo,
                R.raw.imggeral_alerta,
                R.raw.gordinhorindo,
                R.raw.neiva,
                R.raw.neiva_sinarzinho,
                R.raw.neiva_pingo,
                R.raw.neiva_bonito,
                R.raw.bolsonaro_ihu,
                R.raw.pescotapa,
                R.raw.dilma_vaiperder,
                R.raw.olhaso,
                R.raw.picapau_caraengracado,
                R.raw.fatality,
                R.raw.leo_tinovo,
                R.raw.agaraga_shit,
                R.raw.caracol_naoentendi,
                R.raw.imggeral_oiamor,
                R.raw.roblox,
                R.raw.pele,
                R.raw.vaisentar,
                R.raw.vaisentar_morrer,
                R.raw.vaisentar_queijo,
                R.raw.tresreais,
                R.raw.zecaurubu,
                R.raw.imggeral_olhaodemonio,
                R.raw.oloquinhomeu,
                R.raw.oloquinho_tapegandofogo,
                R.raw.oloquinho_olocobicho,
                R.raw.oloquinho_essafera,
                R.raw.ofiladaputa,
                R.raw.leo_mesintofoda,
                R.raw.imggeral_videnovo,
                R.raw.rusbe,
                R.raw.vemtranquilo,
                R.raw.vemtranquilo_afobado,
                R.raw.hermanoteu_correnegada,
                R.raw.imggeral_mamabonito,
                R.raw.cairnaporrada,
                R.raw.bettina,
                R.raw.alborguetti_vaonde,
                R.raw.capitaonascimento_vaidarmerda,
                R.raw.negoney,
                R.raw.negoney_bigodinho,
                R.raw.patrick_morreu,
                R.raw.silviosantos_tabom,
                R.raw.tomounajabiraca,
                R.raw.tonemai,
                R.raw.tonemai_tachovendo,
                R.raw.seuousado,
                R.raw.imggeral_badumtss,
                R.raw.palmas,
                R.raw.sobrevivencia,
                R.raw.imggeral_relampagou,
                R.raw.imggeral_repangalejando,
                R.raw.imggeral_agalou,
                R.raw.umah_que,
                R.raw.umah,
                R.raw.hulk_cabeleireiro,
                R.raw.whindersson_saguadin,
                R.raw.botaaqui,
                R.raw.chaves_eunaoquero,
                R.raw.praqueviolencia,
                R.raw.zabuza,
                R.raw.nandomoura_tolascado,
                R.raw.imggeral_mandealdo,
                R.raw.dimiscosta,
                R.raw.tonapraia,
                R.raw.tonapraia_ignorancia,
                R.raw.cetadoido,
                R.raw.michael_ari,
                R.raw.gamesedu_mojang,
                R.raw.elenaovai,
                R.raw.imggeral_parabens,
                R.raw.daciolo_sao9horas,
                R.raw.vemprofut,
                R.raw.vemprofut_tapegado,
                R.raw.vemprofut_sotapa,
                R.raw.vozdegalinha,
                R.raw.vozdegalinha_toinvicto,
                R.raw.daciolo_gloriadeux,
                R.raw.vegeta_omiseravel,
                R.raw.eversonzoio_azideiamermao,
                R.raw.ronaldinho_namorar,
                R.raw.tomanocu,
                R.raw.eversonzoio_tira,
                R.raw.papaco_tudoisso,
                R.raw.garrafa_meda,
                R.raw.garrafa_fidumaegua,
                R.raw.eutambem,
                R.raw.homemaranha_problemameu,
                R.raw.tiririca_eunaoquero,
                R.raw.baguncinha,
                R.raw.babydobiruleibe,
                R.raw.babydobiruleibe_vatetomarnocu,
                R.raw.bigodinfinin,
                R.raw.imggeral_cachorro,
                R.raw.uganda_doyouknowtheway,
                R.raw.certamentequesim,
                R.raw.galvaobueno_taffarel,
                R.raw.eversonzoio_voupegarminhamarreta,
                R.raw.tedouumafacadona,
                R.raw.queondaeessa,
                R.raw.imggeral_correcadeirante,
                R.raw.olhaobichovindo,
                R.raw.bomdiavaitomarnocu,
                R.raw.emesmoe,
                R.raw.imggeral_doismilanosdepois,
                R.raw.pingu_molequedocaralho,
                R.raw.umaperdadetempo,
                R.raw.vaitomarnoseurabo,
                R.raw.olhaopastel,
                R.raw.cocielo_molequeneutro,
                R.raw.reallynigga,
                R.raw.hagay,
                R.raw.eitadesgraca,
                R.raw.imggeral_eitaporradocaralho,
                R.raw.eunaosei,
                R.raw.fruet_rachamo,
                R.raw.noice,
                R.raw.imggeral_eamelhorhoradodia,
                R.raw.wow,
                R.raw.drauzio_everdade,
                R.raw.imggeral_euquerodormirporra,
                R.raw.misty_comosoubonito,
                R.raw.mcmotoveia,
                R.raw.seupaitemgonorreia,
                R.raw.imggeral_falauma,
                R.raw.neto_zeruela,
                R.raw.neto_naovaidar,
                R.raw.sumiutete,
                R.raw.ciladabino,
                R.raw.vindiesel_canhugs,
                R.raw.brksedu_bilada,
                R.raw.adriano_impressionante,
                R.raw.sefodeugta,
                R.raw.mamilos,
                R.raw.edois,
                R.raw.goku_vegetaolhabem,
                R.raw.mentira,
                R.raw.eitacuzao,
                R.raw.bolapanico_ahva,
                R.raw.falafiote,
                R.raw.didi_noceutempao,
                R.raw.mariadorosario_oqueeisso,
                R.raw.zuzu_aiquedor,
                R.raw.cauemoura_sefudeu,
                R.raw.silviopanico_arregou,
                R.raw.donairene_meudeus,
                R.raw.imggeral_caralhominhabola,
                R.raw.vingancapernilongo,
                R.raw.homer_emesmo,
                R.raw.eaimanobrown_tametirando,
                R.raw.soubemmacho,
                R.raw.voucagarnacalca,
                R.raw.joaoamerica_filhadaputa,
                R.raw.senhorpoliglota_camboya,
                R.raw.senhorpoliglota_niju,
                R.raw.senhorpoliglota_lapaloma,
                R.raw.imggeral_aimae,
                R.raw.lionman_umadadiva,
                R.raw.paodebatata,
                R.raw.mustafary_saidaqui,
                R.raw.imggeral_saidaquiseucachorro,
                R.raw.filmaefala,
                R.raw.filmaefala_suspeito,
                R.raw.filmaefala_capeteprego,
                R.raw.marmotagritando,
                R.raw.nuncanemvi_naotolembrado,
                R.raw.nuncanemvi,
                R.raw.nuncanemvi_quediafoiisso,
                R.raw.surprisemotherfucker,
                R.raw.vegeta,
                R.raw.chaves_evocesatanas,
                R.raw.chaves_aiqueburro,
                R.raw.bocafoifeitapracomer,
                R.raw.turndown,
                R.raw.imggeral_mederrubaramaqui,
                R.raw.julius_tavabrincando,
                R.raw.reidasserpente_saidomeiomenino,
                R.raw.carlosalberto_risada,
                R.raw.tiririca_soueu,
                R.raw.jesusjardineiro,
                R.raw.fallen_tenhoprobleminha,
                R.raw.eoque,
                R.raw.capitaonascimento_calaraboca,
                R.raw.capitaonascimento_naovaisubir,
                R.raw.joaodanica_querotequebrar,
                R.raw.joaodanica_gatosnao,
                R.raw.joaodanica_sabeondeto,
                R.raw.joaodanica_lionelmessenger,
                R.raw.leandrohassum_fudeudevez,
                R.raw.hmmboiola,
                R.raw.velhanada,
                R.raw.sandra_quedeselegante,
                R.raw.ementira,
                R.raw.trivago_final,
                R.raw.trivago_hotel,
                R.raw.allahuakbar,
                R.raw.imggeral_jogandovideogame,
                R.raw.aloha,
                R.raw.dollynho,
                R.raw.igorneragay,
                R.raw.crash_woah,
                R.raw.rolezeiras_eusourolezeira,
                R.raw.aiquedelicia,
                R.raw.papacapim,
                R.raw.oimeuchapa,
                R.raw.omaewa,
                R.raw.luangameplay_minecraft,
                R.raw.imggeral_uhminhanossa,
                R.raw.senhora,
                R.raw.quevelocidadejunior,
                R.raw.eusouseupai,
                R.raw.rodolfo,
                R.raw.illuminati,
                R.raw.celoucocachoeira_cachoeira,
                R.raw.celoucocachoeira_sopordeus,
                R.raw.giovana_eita,
                R.raw.whatafuck,
                R.raw.sanduiche,
                R.raw.pele_soujosoares,
                R.raw.acelera_jesus,
                R.raw.acelera_passei,
                R.raw.maguilacantando_faleceu,
                R.raw.imggeral_aimisericordia,
                R.raw.juao_souummerda,
                R.raw.juao_vontadedechorar,
                R.raw.juao_vontadedechorarmaior,
                R.raw.juao_oquefizcomminhavida,
                R.raw.juao_seraquevouvencer,
                R.raw.sikerajunior_vaimorrer,
                R.raw.sikerajunior_eocrimeenois,
                R.raw.joaogordo_somedaqui,
                R.raw.divertidamente,
                R.raw.poderosocastiga_maisoumenos,
                R.raw.hastad_nha,
                R.raw.hastad_cavaloimundo,
                R.raw.imggeral_jamelao,
                R.raw.soufarao,
                R.raw.luanaotemporta,
                R.raw.homemfoiaosol,
                R.raw.elementocaradepau,
                R.raw.davyjones_temquerir,
                R.raw.davyjones_envolvente,
                R.raw.davyjones_pimba,
                R.raw.davyjones_eugostei,
                R.raw.davyjones_jogomaneirao,
                R.raw.davyjones_maneiropracaralho,
                R.raw.davyjones_querofuder,
                R.raw.davyjones_tobrabo,
                R.raw.seubuceta_voudevolvernao,
                R.raw.formigamorri,
                R.raw.didi_emorreu,
                R.raw.yoda_fon,
                R.raw.whindersson_orradiacho,
                R.raw.nossaquebosta,
                R.raw.professor,
                R.raw.bolsonaro_daquetedououtra,
                R.raw.bolsonaro_naoto,
                R.raw.pepino,
                R.raw.nandomoura_malakoi,
                R.raw.nandomoura_oqueeisso,
                R.raw.nandomoura_voceezoeiro,
                R.raw.away_para,
                R.raw.away_naoacredito,
                R.raw.imggeral_saidafrentesatanas,
                R.raw.lucasnetto_foca,
                R.raw.cetemdemencia,
                R.raw.picapau_inventa,
                R.raw.deusessaoreais,
                R.raw.imggeral_caralhomane,
                R.raw.jaacaboujessica,
                R.raw.lucascsgo_javai,
                R.raw.lucascsgo_querota,
                R.raw.naoseipreso,
                R.raw.pesadao_izi,
                R.raw.feijaotorpedo,
                R.raw.fred,
                R.raw.imggeral_euvoucair,
                R.raw.nuncamexa,
                R.raw.saborsorvete,
                R.raw.efistaile,
                R.raw.eitaporra,
                R.raw.familyguy_ah,
                R.raw.humemesmo,
                R.raw.seubuceta,
                R.raw.silvio_naoconsegue,
                R.raw.silvio_oi,
                R.raw.silvio_enfianoteucu,
                R.raw.xuxa_sentalaclaudia,
                R.raw.agoraentendi,
                R.raw.cepodemadeira,
                R.raw.cepodemadeira_bemduro,
                R.raw.amandaklein,
                R.raw.marco_tacalepau,
                R.raw.imggeral_cadeochinelo,
                R.raw.matiformiga,
                R.raw.ouch,
                R.raw.filosofopiton_desgraca,
                R.raw.imggeral_trollei,
                R.raw.react_olhaosdeuses,
                R.raw.react_quemeessecara,
                R.raw.rima_semnexo,
                R.raw.rima_nadaaver,
                R.raw.racanegra_fingindo,
                R.raw.racanegra_jaentendi,
                R.raw.imggeral_pauquebrando,
                R.raw.picapau_fuitapeado,
                R.raw.ohsefodeu,
                R.raw.naoentendi,
                R.raw.imggeral_correberg,
                R.raw.jubileu,
                R.raw.johncena,
                R.raw.bambam_horadoshow,
                R.raw.bambam_ajuda,
                R.raw.bambam_birl,
                R.raw.bambam_saidecasa,
                R.raw.felipe_fazcomigonao,
                R.raw.felipe_guarapari,
                R.raw.felipe_saidaedoido,
                R.raw.felipe_samu,
                R.raw.galocego_eue,
                R.raw.galocego_achaqueiafazerisso,
                R.raw.galocego_compraralimento,
                R.raw.galocego_nadaaver,
                R.raw.galocego_naosabia,
                R.raw.galocego_tudoerrado,
                R.raw.imggeral_animalburrao,
                R.raw.imggeral_aimeucu,
                R.raw.maluco,
                R.raw.medelicenca,
                R.raw.papaco_bundamole,
                R.raw.papaco_falaoquequer,
                R.raw.papaco_ateumoutrodia,
                R.raw.papaco_cagao,
                R.raw.papaco_faloucomigo,
                R.raw.papaco_aindabem,
                R.raw.taumaporra,
                R.raw.viadaobonito,
                R.raw.treta,
                R.raw.tenho2real,
                R.raw.tavabom,
                R.raw.quecachorro,
                R.raw.medepapai,
                R.raw.gatomiopia,
                R.raw.faustao_tapegandofogo,
                R.raw.faustao_errou,
                R.raw.etetra,
                R.raw.erroufeio,
                R.raw.cleber_hojenao,
                R.raw.cleber_negros,
                R.raw.voceeburro,
                R.raw.tomacachaca,
                R.raw.taldomula,
                R.raw.quemerdahein,
                R.raw.putafalta,
                R.raw.paichorando,
                R.raw.morrediabo_morre,
                R.raw.morrediabo_naointeressa,
                R.raw.morrediabo_tofalando,
                R.raw.imggeral_saidesgraca,
                R.raw.beijanaboca,
                R.raw.alborguetti_cademeupau,
                R.raw.alborguetti_bemfeito,
                R.raw.alborguetti_taumamerda,
                R.raw.alborguetti_toloco,
                R.raw.alborguetti_vaamerda,
                R.raw.agaraga,
                R.raw.aipaipara,
                R.raw.bope_esqueceessamerda,
                R.raw.bope_satisfacaoaspira,
                R.raw.marciocanuto_impossivel,
                R.raw.fazourro,
                R.raw.queviagem,
                R.raw.borracha,
                R.raw.coerapaziada,
                R.raw.douglas_ceefeio,
                R.raw.douglas,
                R.raw.serjao_barulhoonca,
                R.raw.serjao_aquitemcoragem,
                R.raw.serjao_autoridadesdoibama,
                R.raw.serjao_comfenopaieterno,
                R.raw.serjao_eaverdade,
                R.raw.serjao_matadordeonca,
                R.raw.serjao_nacalcacagar,
                R.raw.serjao_oncanozoologico,
                R.raw.serjao_valeuporduasoumais,
                R.raw.foiotimo_foiotimo,
                R.raw.foiotimo_exatamente,
                R.raw.eupito,
                R.raw.jeremias,
                R.raw.irineu,
                R.raw.leo_malucoebrabo,
                R.raw.leo_hanhein,
                R.raw.caceteagulha_firma,
                R.raw.caceteagulha_cacete,
                R.raw.marilene,
                R.raw.miau,
                R.raw.miseravel,
                R.raw.naosei,
                R.raw.panificadora_manteiga,
                R.raw.panificadora_cacetinho,
                R.raw.pesadao_quentinho,
                R.raw.pesadao_pesadao,
                R.raw.bemlouco,
                R.raw.bemlouco_empolgante,
                R.raw.cagadodefome,
                R.raw.vimprapassear,
                R.raw.quedelicia_essapeca,
                R.raw.piruleta_bicho,
                R.raw.piruleta_tiraascalca,
                R.raw.querocafe,
                R.raw.querocafe_desculpe,
                R.raw.imggeral_vaimorrerviado,
                R.raw.rolezeiras,
                R.raw.rolezeiras_ideiaerrada,
                R.raw.rolezeiras2,
                R.raw.tapita_camisarosa,
                R.raw.tapita_unimed,
                R.raw.tapita,
                R.raw.tiro,
                R.raw.vaifilhao,
                R.raw.velhoaiai,
                R.raw.gordinhoesoh
        };
    }

    public void compartilhar(int som){

        File soundPath = new File(Objects.requireNonNull(getContext()).getFilesDir(), "sons");
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
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(focoFragment) {
            itemSelecionado = item.getTitle().toString();
            if (item.getTitle() == getString(R.string.adicionar)) {
                addFavorito(img[posicaoDuploClique], sons[posicaoDuploClique], "B", listaAudio[posicaoDuploClique]);
            } else if (item.getTitle() == getString(R.string.compartilharMenu)) {
                if(checarPermissaoGravar()) {
                    compartilhar(posicaoDuploClique);
                }
            } else if (item.getTitle() == getString(R.string.excluir)) {
                String nomeSom = resources.getResourceEntryName(sons[posicaoDuploClique]);
                databaseHelper.delete(nomeSom);
                carregarFavoritos();
                grid.setAdapter(new Adaptador(getContext(), img, listaAudio));
                toastMessage(getString(R.string.deletarAudio));
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

    private void buscarYoutube(int posicaoAudio) {
        if(sons.length <= 1){
            posicaoAudio = posicaoAudioPesquisa;
        }
        if(isAppInstalled("com.google.android.youtube")) {
            Intent intent = new Intent(Intent.ACTION_SEARCH);
            intent.setPackage("com.google.android.youtube");
            intent.putExtra("query", tabActivity.buscarNomeAudio(posicaoAudio));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            }catch (Exception e){
                toastMessage("Não foi possível abrir o youtube");
            }
        }else {
            String url = "https://www.youtube.com/results?search_query="+tabActivity.buscarNomeAudio(posicaoAudio);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            try {
                startActivity(browserIntent);
            }catch (Exception e){
                toastMessage("Não foi possível abrir o youtube");
            }
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
        Cursor data = databaseHelper.getData("B");
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
        listaAudio = new String[descricao.size()];
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
                listaAudio[i] = descricaoSom;
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
        carregarTabela();
        posicaoAudioPesquisa = posicao;
        int idAudio = sons[posicao];
        int idImagem = img[posicao];
        carregarListaAudio();
        String nomeAudio = listaAudio[posicao];

        sons = new int[]{idAudio};
        img = new int[]{idImagem};
        listaAudio = new String[]{nomeAudio};

        this.grid.setAdapter(new Adaptador(this.view.getContext(), img, listaAudio));
    }

    public void desmontarAudioPesquisa(){
        consultaPesquisa = false;
        audioAnterior = 9999999;
        carregarTabela();
        carregarListaAudio();
        this.grid.setAdapter(new Adaptador(this.view.getContext(), img, listaAudio));
    }

    protected boolean isAppInstalled(String packageName) {
        Intent mIntent = Objects.requireNonNull(getContext()).getPackageManager().getLaunchIntentForPackage(packageName);
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
            //String nomeAudio = retirarCaracteres(tabActivity.buscarNomeAudio(posicaoClique)) +".mp3";
            String nomeAudio = retirarCaracteres(listaAudio[som]) +".mp3";
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
        nome = nome.replace(".", "");
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

    public void carregarListaAudio() {
        listaAudio = new String[]{
                "SEM PRESSÃO AQUI É XANDÃO",
                "NO FIM DA ESCURIDÃO TEM XANDÃO",
                "TOMA ESSE DOUBLE BICEPS",
                "Hi Lorena",
                "Ain Nobru apelão",
                "E lá vamos nós",
                "Peraí! Apaga essa peste aí",
                "MIAAAU",
                "Água coca latão",
                "Mary pfff",
                "Pra gringo é mais caro",
                "Tem rack cheque",
                "Mano tu é gay?",
                "Tu que é gay, tu que deixa",
                "Eu tava testando ele",
                "Mimimi seboso",
                "Fala 300 - Tigas",
                "Corona virus - Cardi b",
                "Risada de ladrão",
                "Fazer churrasco",
                "Buduvieser - André vilão",
                "AH ninguém gosta de mim - André vilão",
                "AH to com depressão - André vilão",
                "AH tá ardendo - André vilão",
                "Por que cê tá chorando gatinha?",
                "Você é a vergonha da profissão - Jacquin",
                "Cala boca - Jacquin",
                "Tá me ouvindo? - Jacquin",
                "Tem como não ouvir?",
                "Vou contar pro meu pai, tia",
                "Tá saindo da jaula o monstro - Bambam",
                "Som de alerta metal gear",
                "Gordinho rindo",
                "Neeeiva",
                "Só tem o sinarzinho",
                "Não tinha um pingo de p*nto",
                "O homi é bonito por b*sta",
                "Ihuu taokey",
                "Uma menina pescotapa",
                "Vai todo mundo perder - Dilma",
                "Olha só, olha lá",
                "Que cara mais engraçado - Pica pau",
                "Fatality",
                "Ti novo - Léo stronda",
                "Ah sh*t here we go again - CJ",
                "Não entendi esse final - Caracol raivoso",
                "Oi amor, tá podendo falar ou sua mulher tá perto?",
                "Som Roblox morrendo",
                "Não - Pelé",
                "Cê vai sentar é na cabeça",
                "Prefiro morrer do que sentar na cabeça",
                "Cê gosta de queijo",
                "Três reais",
                "Essa vai ser a minha melhor vigarice",
                "Olha o demonio",
                "Oloquinho meu",
                "Tá pegando fogo bicho",
                "Oloco bicho",
                "Essa fera aí meu",
                "O fila da p*t*",
                "Eu me sinto foda",
                "Primeira vez que eu vi de novo",
                "Rusbé",
                "Vem tranquilo",
                "Vem afobado assim não",
                "Corre negada",
                "Que mama bonito",
                "Nóis vai entrar na porrada em",
                "Oi meu nome é Bettina",
                "Vá a m*erda - Alborguetti",
                "Já avisei que vai dar merda",
                "Nego ney",
                "Bigodinho fininho - Nego ney",
                "Todo mundo morreu, acabou",
                "É tá bom - Silvio Santos",
                "Tomou na jabiraca",
                "To nem ai",
                "Tá chovendo pra caraca",
                "Seu ousado",
                "Badum Tss",
                "Palmas",
                "Sobrezizencia",
                "Aqui choveu e relampagou",
                "Aqui ta chovendo e repangalejando",
                "Aqui relampeou e agalou tudo",
                "Um que?",
                "Um ah",
                "Caberero - hulk",
                "Saguadin",
                "Bota aqui pra ver se não cabe",
                "Eu não quero - Chaves",
                "Pra que tanta violência",
                "Zabuza Momochi",
                "To lascado - Nando Moura",
                "Mande aldo",
                "Dimiscosta",
                "To na praia",
                "Rapaz deixa de ignorância",
                "Cê ta doido",
                "Meu nome é Ari",
                "Mojang é meu ovo",
                "Ele não vai não",
                "Duas palavras PARA BENS",
                "São 9 horas da manhã - Daciolo",
                "Vem pro fut",
                "O futebol tá estralando",
                "É só tapa, só tapa",
                "Voz de galinha",
                "To invicto - Voz de galinha",
                "Glória a Deux - Daciolo",
                "O miserável é um gênio",
                "Azideia - Zóio",
                "Quer namorar comigo? - Ronaldinho",
                "Toma no c* hahaha",
                "Ta bom não vou aguentar mais - Zóio",
                "Orra tudo isso - papaco",
                "Me da minha garrafa",
                "Fi duma égua",
                "Eu também",
                "E quem disse que isso é problema meu",
                "Eu não quero - Tiririca",
                "Fiz uma baguncinha dentro da califórnia",
                "Baby do birulaibe",
                "Ah vá te tomar no c* rapaz",
                "Bigodinho fininho, cabelinho na régua",
                "Cachorro",
                "Do you know the way?",
                "Certamente que sim",
                "Sai que é sua Taffarel ",
                "Vou pegar minha marreta",
                "Te dou uma facadona",
                "Que onda é essa mermão",
                "Corre cadeirante",
                "Olha o bicho vindo moleque",
                "Bom dia, vai tomar no c*",
                "É mesmo é, foda-se",
                "Dois mil anos depois",
                "Moleque do c*ralho, tem que matar uma porra dessa",
                "Uma perda de tempo",
                "Vai tomar no seu rabo",
                "Olha o pastel",
                "Moleque neutro - Cocielo",
                "Really nigga",
                "Ha gay",
                "Eita desgraça",
                "Eita porra do c*ralho",
                "Eu não sei",
                "Rachamo o zóio dele de tiro",
                "Noice",
                "É a melhor hora do dia",
                "Wow Eddy Wally",
                "É verdade, quer dizer às vezes não! - Drauzio",
                "Eu quero dormir porra",
                "Como sou bonito - Misty",
                "Imitando moto",
                "Seu pai tem gonorréia",
                "Fala um A pra você ver",
                "Seus zé ruela - Neto",
                "Não vai dar! - Neto",
                "Sumiu!",
                "É uma cilada bino",
                "I can hugs?",
                "Bilada - BrksEdu",
                "Impressionante como vocês tentam me derrubar",
                "Se fodeu GTA",
                "Mamilos polêmicos",
                "É dois",
                "Vegeta olha bem",
                "Mentira hahaha",
                "Eita c*zão",
                "Ah vá, é mesmo?",
                "Fala fiote",
                "No céu tem pão?",
                "O que é isso? - Maria do Rosário",
                "Ai que dor - Zuzu",
                "Se fudeu - Cauê Moura",
                "Arregou",
                "Meu deus - Dona Irene",
                "c*ralho minha bola",
                "Vingança do pernilongo",
                "É mesmo é? - Homer",
                "Ta me tirando?",
                "Sou bem macho",
                "Vou cagar na calça",
                "Filha da puta - Torcedor América",
                "Boa noite - Senhor poliglota",
                "Nijuiti - Senhor poliglota",
                "La paloma - Senhor poliglota",
                "Ai mãe - Desliga esse computador",
                "Uma dádiva dos ninjas",
                "Pão de batata",
                "Sai daqui demônio - Mustafary",
                "Sai daqui seu cachorro",
                "Você é o bichão mesmo em",
                "Sou suspeito",
                "Capeta prego",
                "Aaaaaa marmota gritando",
                "Não to lembrado não",
                "Nunca nem vi",
                "Que dia foi isso",
                "Surprise motherfucker",
                "É de mais de 8000",
                "É você satanás?",
                "Ai que burro da zero pra ele",
                "Eu fui feito pra comer",
                "Turn down for what",
                "Me derrubaram aqui ô",
                "Achou que eu tava brincando - Julius",
                "Sai do meio menino oxi",
                "Risada Carlos Alberto",
                "Sou eu o Tiririca",
                "O jardineiro é Jesus",
                "Eu tenho probleminha - Fallen",
                "É o que",
                "Já avisei para calar sua boca",
                "Não vai subir ninguém",
                "Quero te quebrar filho - João da nica",
                "Gatos não - João da nica",
                "Sabe onde eu tô? - João da nica",
                "Lionel messenger - João da nica",
                "Fudeu de vez",
                "Humm boiola",
                "Nada nada nada nada",
                "Que deselegante",
                "É mentira",
                "Hotel, trivago",
                "Já procurou hotel na internet?",
                "Allahu akbar som",
                "Não dá pra ficar só em casa jogando videogame né?",
                "Aloha, arola",
                "Dollynho",
                "Higor nera gay?",
                "Woah crash",
                "Eu sou rolezeira",
                "Que demais, ai que delicia",
                "Papa capim dos sonhos",
                "Oi meu chapa",
                "Omae wa mou shindeiru",
                "Luan gameplay minecraft",
                "Uh minha nossa",
                "Senhora ?!",
                "Que velocidade é essa menino!",
                "Eu sou seu pai, eu sou Jesus",
                "Rodolfo porco",
                "Música Illuminati",
                "Cê é louco cachoeira",
                "Só por deus irmão",
                "Eita giovana",
                "What? Whatafuck?",
                "Sanduíche-iche",
                "Sou o Jô Soares sua piranha",
                "Acelera Jesus, acelera",
                "Puta que pariu, passei",
                "Faleceu - Maguila",
                "Ai misericórdia",
                "c*ralho eu sou um merda",
                "E a vontade de chorar é inevitável",
                "A vontade de rir é grande mas a de chorar é maior - Juão",
                "O que foi que eu fiz com minha vida Jesus - Juão",
                "Será que um dia eu vou vencer na vida? - Juão",
                "Você maconheiro vai morrer daqui pro natal - Sikera",
                "É o crime é nóis - Sikera Junior",
                "Some daqui - João Gordo",
                "Já olhou pra alguém e pensou o que passa na cabeça dela?",
                "Mais ou menos - Poderoso castiga",
                "Nhaa",
                "Cavalo imundo do c*ralho - Hastad",
                "Porra jamelão",
                "Sou faraó",
                "A lua é redonda e não tem porta",
                "E por que o homem não foi no sol ?",
                "Elemento vagabundo, cara de pau",
                "Tem que rir pra não chorar - Davy Jones",
                "Envolvente, diferente, interessante - Davy Jones",
                "Pimba - Davy Jones",
                "Eu gostei em - Davy Joves",
                "Esse jogo é maneirão em - Davy Joves",
                "Maneiro pra c*ralho - Davy Joves",
                "Quero foder com os outros - Davy Joves",
                "Eu to brabo - Davy Joves",
                "É mas não vou devolver não",
                "Morri",
                "E morreu - Didi",
                "Fon - Yoda",
                "Orra diacho - Whindersson",
                "Nossa que bosta",
                "Lá vem os babacas",
                "Da que te dou outra - Bolsonaro",
                "Pô não to de novo, que decepção - Bolsonaro",
                "Pepino de novo",
                "Malakoi, do hebraico - Nando",
                "Mas o que é isso que está aparecendo no meu computador - Nando",
                "Você é zueiro mesmo em cara - Nando",
                "Para com essa porra",
                "Eu não acredito nessa porra",
                "Sai da frente satanás",
                "Sou uma foca - Luccas Neto",
                "Cê tem demência",
                "Essa gente inventa cada coisa",
                "Deuses são reais se acredita neles",
                "c*ralho mané",
                "Já acabou jéssica",
                "Já vai? - Lucas",
                "Qué ota eu vou da ota - Lucas",
                "Não sei - Preso",
                "Iziiiii - Inhegas",
                "Feijão torpedo",
                "Ih Rapaz",
                "Eu vou cair",
                "Nunca mexa",
                "Leite compensado",
                "Elaio é fistaile",
                "Eita porra",
                "Ahhh",
                "Hum é mesmo",
                "Seu b*ceta",
                "Não consegue né",
                "Oe - Silvio Santos",
                "E o bambu?",
                "Aham senta lá Cláudia",
                "Ah agora eu entendi",
                "Cepo de madeira",
                "Bem duro, para dar mais impacto",
                "Amanda klein",
                "Taca-le pau nesse carrinho marcos",
                "Cade o chinelo",
                "Mati",
                "Ouch Charlie",
                "Desgraça - Filósofo piton",
                "Trollei",
                "c*ralho olha os deuses mano",
                "Quem é esse cara velho",
                "Sem nexo",
                "Nada a ver",
                "To fingindo",
                "Já entendi agora",
                "Oh o pau quebrando",
                "Fui tapeado",
                "Oh se fodeu",
                "Eu não entendi o que ele falou",
                "Corre berg",
                "Jubileu está esquisito hoje",
                "John Cena",
                "Hora do show",
                "Ajuda o maluco que tá doente",
                "Birl",
                "Saí de casa comi pra c*ralho",
                "Faz isso comigo não velho",
                "Guarapari búzio é minha arte",
                "Sai daí doido",
                "Samu seu c*",
                "É ué",
                "Cê acha que eu ia fazer isso?",
                "Comprar alimento - Galo cego",
                "Nada a ver irmão",
                "Não sabia",
                "Tudo errado essas perguntas",
                "Animal burrão",
                "Ai meu c*",
                "Você é maluco é",
                "Me dê licença que agora vou cagar",
                "Ei seu bunda mole - Papaco",
                "Fala o que você quer de uma vez - Papaco",
                "Até um outro dia - Papaco",
                "E quem foi o cagão - Papaco",
                "Falou comigo - Papaco",
                "Ainda bem - Papaco",
                "Esse programa aqui tá uma porra",
                "Viadão bonito",
                "Estou sentindo uma treta",
                "Tenho 2 real",
                "Já tava bom",
                "Que cachorro o que",
                "Me de papai",
                "Miopia",
                "Tá pegando fogo bicho",
                "Errou",
                "É tetra",
                "Errou feio errou rude",
                "Hoje não",
                "Negros maravilhosos",
                "Você é burro cara",
                "Cachaça carai",
                "Esse é o tal do mula",
                "Que merda hein sabia não",
                "Puta falta de sacanagem",
                "Pai chorando",
                "Morre diabo",
                "Não interessa pra você",
                "Tô falando não tô falando",
                "Sai desgraça",
                "Ai papai, beija na boca",
                "Cadê o meu pau - Alborghetti",
                "Bem feito - Alborghetti",
                "Tá uma merda - Alborghetti",
                "Eu tô louco - Alborghetti",
                "Ah vá a merda - Alborghetti",
                "Agaragã",
                "Ai pai para",
                "Esquece essa merda aí",
                "Que satisfação aspira",
                "Você está impossível",
                "Faz o urro",
                "Que viagem é essa",
                "c*ralho borracha mano",
                "Coé rapaziada",
                "Cê é feio em fi - Dougras",
                "Eu sou o dogras",
                "Barulho onça - Serjão",
                "Aqui tem coragem - Serjão",
                "Autoridades do ibama",
                "Com fé no pai eterno sempre aqui estou e vou estar - Serjão",
                "É a verdade não minto - Serjão",
                "Mais conhecido como matador de onça - Serjão",
                "É perigo na calça ele cagar - Serjão",
                "Na hora que o burrai do pipoco come - Serjão",
                "Mas valeu por duas ou mais - Serjão",
                "Foi ótimo",
                "Exatamente",
                "Eu pito muito",
                "Se eu pudesse eu matava mil",
                "Irineu você não sabe nem eu",
                "O maluco é brabo",
                "HAN HEIN!",
                "A firma que mandou né",
                "Cacete de agulha",
                "Olá marilene",
                "Miau",
                "Acertou miserável",
                "Não sei",
                "Chega manteiga derrete",
                "Do cacetinho",
                "Tá quentinho",
                "Pesadão",
                "Bem louco",
                "Empolgante",
                "Tô cagado de fome",
                "Eu vim é pra passear",
                "Essa peça que você queria?",
                "Bicho piruleta",
                "Tira as calças sem zorba - Bicho piruleta",
                "Quero café",
                "Me desculpe",
                "Vai morrer viado",
                "E ae vamo fecha",
                "Isso ai é mó ideia errada",
                "Tem uns meninos que são muitos brutos",
                "Tapita camisa rosa é coisa de viado",
                "Unimed - Tapita",
                "Tapita",
                "Ai",
                "Vai filhão",
                "Ai ai",
                "E só"
        };

    }

    public void pause(){
        try {
            if(mp != null) {
                mp.setLooping(false);
                mp.stop();
                mp.reset();
                mp.release();
                mp = null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

}
