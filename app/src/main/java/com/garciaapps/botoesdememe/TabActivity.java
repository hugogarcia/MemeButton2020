package com.garciaapps.botoesdememe;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

public class TabActivity extends AppCompatActivity {

    public static SectionsPageAdapter adapter;
    private static ViewPager viewPager;
    public static InterstitialAd mInterstitialAd;
    public static int contador, posicaoScroll, posicaoFragment, posicaoScrollM1 = 0, posicaoScrollM2 = 0;
    AdView adView;
    MainActivity m1;
    public static MainActivity2 m2;
    SharedPreferences sharedPreferences;
    boolean favorito, telaDePesquisa;
    AdRequest adRequest;
    private static TabLayout tabLayout;
    LinearLayout linearLista;
    ArrayList<String> listaAudio, listaMusica;
    public ArrayAdapter<String> adapterAudio, adapterMusica, adpterComparaAudio, adpterComparaMusica;
    public static ListView listaPesquisa;
    public static MenuItem botaoBuscar, botaoAtualizar;
    public SearchView searchView;
    public Filter filtroPesquisa = null;
    MenuItem itemVisivel;
    public static JSONObject jsonYoutube = null;
    public static boolean isFailedAdsLoad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tab);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tituloApp = (TextView) findViewById(R.id.tituloApp);
        sharedPreferences = this.getSharedPreferences("com.garciaapps.botoesdememe", Context.MODE_PRIVATE);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        FirebaseMessaging.getInstance().subscribeToTopic("update"); //TÓPICO PARA USAR NO PUSH NOTIFICATION
        //FirebaseMessaging.getInstance().subscribeToTopic("teste"); //TÓPICO PARA USAR NO PUSH NOTIFICATION

        //-----------------VALIDAÇÃO PESQUISA---------------
        posicaoFragment = 0;
        telaDePesquisa = false;
        linearLista = (LinearLayout) findViewById(R.id.linear_lista);
        listaAudio = new ArrayList<>();
        listaMusica = new ArrayList<>();

        carregarListaAudio();
        carregarListaMusica();

        adapterAudio = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaAudio);
        adapterMusica = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaMusica);

        adpterComparaAudio = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaAudio);
        adpterComparaMusica = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaMusica);

        listaPesquisa = (ListView) findViewById(R.id.lista_memes);
        listaPesquisa.setTextFilterEnabled(false);
        //--------------------------------------------------

        Intent intentFavorito = getIntent();
        if (intentFavorito != null) {
            favorito = intentFavorito.getBooleanExtra("Favorito", false);
            if (favorito) {
                tituloApp.setGravity(Gravity.LEFT);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                tituloApp.setText(R.string.favoritos);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("Favorito", favorito);
                editor.commit();
            } else {
                tituloApp.setText(getString(R.string.titulo));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("Favorito", favorito);
                editor.commit();
            }
        }

        /*  COMENTADO LÓGICA GERAR JSON YOUTUBE
        String jsonGravado = sharedPreferences.getString("Json", null);
        if(!favorito) {
            if (jsonGravado == null) {
                getJson();
            } else {
                try {
                    jsonYoutube = new JSONObject(jsonGravado);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }*/

        //----------------PROPAGANDA---------------
        MobileAds.initialize(this);
        adView = (AdView) findViewById(R.id.adViewBottom);
        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                adView.setVisibility(View.GONE);
                isFailedAdsLoad = true;
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                hideNavigationBar();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                isFailedAdsLoad = false;
                if(isFailedAdsLoad)
                    adView.setVisibility(View.VISIBLE);
            }
        });

        contador = 0;
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.banner_interstitial));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                hideNavigationBar();
                if (posicaoFragment == 0 && posicaoScroll > 0) {
                    m1.grid.setSelection(posicaoScroll);
                }
                if (posicaoFragment == 1 && posicaoScroll > 0) {
                    m2.grid.setSelection(posicaoScroll);
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
            }
        });
        requestNewInterstitial();
        //----------------PROPAGANDA---------------

        m1 = new MainActivity();
        m2 = new MainActivity2();

        //videoListFragment = new VideoListDemoActivity();
        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setOffscreenPageLimit(1);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        hideNavigationBar();
        /*if(favorito){
            ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(2).setVisibility(View.GONE);
        }else{
            ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(2).setVisibility(View.VISIBLE);
        }*/

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                posicaoFragment = position;
                hideNavigationBar();

                if(!favorito && botaoAtualizar != null && botaoBuscar != null) {
                    if (posicaoFragment == 2) {
                        botaoAtualizar.setVisible(true);
                        botaoBuscar.setVisible(false);
                    }else {
                        botaoAtualizar.setVisible(false);
                        botaoBuscar.setVisible(true);
                    }

                    /*if(posicaoFragmentAnterior == 2 &&
                            VideoListDemoActivity.VideoFragment.player != null &&
                            VideoListDemoActivity.VideoFragment.player.isPlaying()){
                        VideoListDemoActivity.VideoFragment.player.pause();
                    }*/
                }

                if(isFailedAdsLoad & adView != null){
                    adView.loadAd(adRequest);
                }

                if(!telaDePesquisa) {
                    if (posicaoFragment == 0) {
                        m1.grid.setSelection(posicaoScrollM1);
                    } else if (posicaoFragment == 1) {
                        m2.grid.setSelection(posicaoScrollM2);
                    }
                    posicaoScrollM1 = m1.grid.getFirstVisiblePosition();
                    posicaoScrollM2 = m2.grid.getFirstVisiblePosition();
                }

                if (telaDePesquisa) {
                    filtroPesquisa.filter("");
                    listaPesquisa.clearTextFilter();
                    incluirPesquisa();
                    searchView.setQuery("", false);
                } else if (!favorito) {
                    if (m1.sons != null) {
                        m1.desmontarAudioPesquisa();
                    }
                    if (m2.sons != null) {
                        if (m2.consultaPesquisa) {
                            m2.desmontarAudioPesquisa();
                        }
                    }
                }

                /*if ((telaDePesquisa | favorito) & posicaoFragment != 2) {
                    viewPager.setCurrentItem(1);
                }*/
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (m1.mp != null) {
                    m1.mp.stop();
                    m1.mp.reset();
                    m1.mp.release();
                    m1.mp = null;
                }

                if (posicaoFragment != 0) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    if (m2.mp != null) {
                        m2.pause(false);
                    }
                }
            }
        });

        listaPesquisa.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Object item = adapterView.getItemAtPosition(position);
                int posicaoAudio;
                if (posicaoFragment == 0) {
                    removerPesquisa();
                    posicaoAudio = adpterComparaAudio.getPosition(item.toString());
                    m1.montarAudioPesquisa(posicaoAudio);
                } else if (posicaoFragment == 1) {
                    removerPesquisa();
                    m2.pause(false);
                    posicaoAudio = adpterComparaMusica.getPosition(item.toString());
                    m2.montarAudioPesquisa(posicaoAudio);
                }

                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                hideNavigationBar();
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( TabActivity.this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Log.e("newToken",newToken);
            }
        });

        listaPesquisa.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        hideNavigationBar();
    }

    public void setupViewPager(ViewPager viewPager) {
        adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new MainActivity(), getString(R.string.Tab1));
        adapter.addFragment(new MainActivity2(), getString(R.string.Tab2));
        /*if(!favorito) {
            adapter.addFragment(videoListFragment, getString(R.string.videos));
        }*/

        viewPager.setAdapter(adapter);
    }

    public void hideNavigationBar() {
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    public void verificarAd() {
        contador++;
        if (mInterstitialAd.isLoaded() && contador >= 17) {
            if (posicaoFragment == 0) {
                posicaoScroll = m1.grid.getFirstVisiblePosition();
            }
            if (posicaoFragment == 1) {
                posicaoScroll = m2.grid.getFirstVisiblePosition();
            }
            m2.pause(true);
            mInterstitialAd.show();
            contador = 0;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent it;
        switch (id) {
            case android.R.id.home:
                it = new Intent(getApplicationContext(), TabActivity.class);
                it.putExtra("Favorito", false);
                //overridePendingTransition(0, 0);
                it.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                ;;overridePendingTransition(0, 0);
                startActivity(it);
                break;
            case R.id.action_favoritos:
                it = new Intent(getApplicationContext(), TabActivity.class);
                it.putExtra("Favorito", true);
                //overridePendingTransition(0, 0);
                it.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                //overridePendingTransition(0, 0);
                startActivity(it);
                break;
            case R.id.action_sugestao:
                it = new Intent(this, Sugestao.class);
                startActivityForResult(it, 1);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        super.onPanelClosed(featureId, menu);
        hideNavigationBar();
    }

    @Override
    protected void onDestroy() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tab, menu);
        itemVisivel = menu.findItem(R.id.action_favoritos);
        botaoBuscar = menu.findItem(R.id.action_search);
        botaoAtualizar = menu.findItem(R.id.refresh);
        if (!favorito) {
            if (posicaoFragment == 2) {
                botaoAtualizar.setVisible(true);
                botaoBuscar.setVisible(false);
            } else {
                botaoBuscar.setVisible(true);
                botaoAtualizar.setVisible(false);
            }
            itemVisivel.setVisible(true);
            SearchManager searchManager = (SearchManager) TabActivity.this.getSystemService(Context.SEARCH_SERVICE);

            searchView = (SearchView) botaoBuscar.getActionView();
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setSubmitButtonEnabled(false);
            searchView.setQueryHint(getString(R.string.buscar));

            searchView.setSearchableInfo(searchManager.getSearchableInfo(TabActivity.this.getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    telaDePesquisa = true;
                    incluirPesquisa();
                    String filtro = newText.trim();

                    if (posicaoFragment == 1) {
                        filtroPesquisa = adapterMusica.getFilter();
                    }else{
                        filtroPesquisa = adapterAudio.getFilter();
                    }
                    filtroPesquisa.filter(filtro);

                    if (newText.isEmpty()) {
                        filtroPesquisa.filter("");
                        listaPesquisa.clearTextFilter();
                    }
                    return false;
                }
            });

            botaoBuscar.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    //((ViewGroup) tabLayout.getChildAt(0)).getChildAt(2).setVisibility(View.GONE);
                    incluirPesquisa();
                    telaDePesquisa = true;

                    if (posicaoFragment == 1) {
                        m2.pause(false);
                    }

                    return true;// true para abrir a pesquisa
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    //((ViewGroup) tabLayout.getChildAt(0)).getChildAt(2).setVisibility(View.VISIBLE);
                    searchView.setQuery("", false);
                    removerPesquisa();
                    telaDePesquisa = false;

                    if (posicaoFragment == 0) {
                        m1.desmontarAudioPesquisa();
                    } else if (posicaoFragment == 1) {
                        m2.pause(false);
                        m2.desmontarAudioPesquisa();
                    }

                    hideNavigationBar();
                    return true;
                }
            });

            /*MenuItemCompat.setOnActionExpandListener(botaoBuscar, new MenuItemCompat.OnActionExpandListener() {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(2).setVisibility(View.GONE);
                    incluirPesquisa();
                    telaDePesquisa = true;

                    if (posicaoFragment == 1) {
                        m2.pause(false);
                    }

                    return true;// true para abrir a pesquisa
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(2).setVisibility(View.VISIBLE);
                    searchView.setQuery("", false);
                    removerPesquisa();
                    telaDePesquisa = false;

                    if (posicaoFragment == 0) {
                        m1.desmontarAudioPesquisa();
                    } else if (posicaoFragment == 1) {
                        m2.pause(false);
                        m2.desmontarAudioPesquisa();
                    }

                    hideNavigationBar();
                    return true;
                }
            });*/

        } else {
            botaoAtualizar.setVisible(false);
            itemVisivel.setVisible(false);
            botaoBuscar.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    public void incluirPesquisa() {
        viewPager.setVisibility(View.INVISIBLE);
        linearLista.setVisibility(View.VISIBLE);

        if (posicaoFragment == 0) {
            listaPesquisa.setAdapter(adapterAudio);
        } else if (posicaoFragment == 1) {
            listaPesquisa.setAdapter(adapterMusica);
        }
    }

    public void removerPesquisa() {
        viewPager.setVisibility(View.VISIBLE);
        linearLista.setVisibility(View.INVISIBLE);
    }

    public void carregarListaAudio() {
        listaAudio.add("SEM PRESSÃO AQUI É XANDÃO");
        listaAudio.add("NO FIM DA ESCURIDÃO TEM XANDÃO");
        listaAudio.add("TOMA ESSE DOUBLE BICEPS");
        listaAudio.add("Hi Lorena");
        listaAudio.add("Ain Nobru apelão");
        listaAudio.add("E lá vamos nós");
        listaAudio.add("Peraí! Apaga essa peste aí");
        listaAudio.add("MIAAAU");
        listaAudio.add("Água coca latão");
        listaAudio.add("Mary pfff");
        listaAudio.add("Pra gringo é mais caro");
        listaAudio.add("Tem rack cheque");
        listaAudio.add("Mano tu é gay?");
        listaAudio.add("Tu que é gay, tu que deixa");
        listaAudio.add("Eu tava testando ele");
        listaAudio.add("Mimimi seboso");
        listaAudio.add("Fala 300 - Tigas");
        listaAudio.add("Corona virus - Cardi b");
        listaAudio.add("Risada de ladrão");
        listaAudio.add("Fazer churrasco");
        listaAudio.add("Buduvieser - André vilão");
        listaAudio.add("AH ninguém gosta de mim - André vilão");
        listaAudio.add("AH to com depressão - André vilão");
        listaAudio.add("AH tá ardendo - André vilão");
        listaAudio.add("Por que cê tá chorando gatinha?");
        listaAudio.add("Você é a vergonha da profissão - Jacquin");
        listaAudio.add("Cala boca - Jacquin");
        listaAudio.add("Tá me ouvindo? - Jacquin");
        listaAudio.add("Tem como não ouvir?");
        listaAudio.add("Vou contar pro meu pai, tia");
        listaAudio.add("Tá saindo da jaula o monstro - Bambam");
        listaAudio.add("Som de alerta metal gear");
        listaAudio.add("Gordinho rindo");
        listaAudio.add("Neeeiva");
        listaAudio.add("Só tem o sinarzinho");
        listaAudio.add("Não tinha um pingo de p*nto");
        listaAudio.add("O homi é bonito por b*sta");
        listaAudio.add("Ihuu taokey");
        listaAudio.add("Uma menina pescotapa");
        listaAudio.add("Vai todo mundo perder - Dilma");
        listaAudio.add("Olha só, olha lá");
        listaAudio.add("Que cara mais engraçado - Pica pau");
        listaAudio.add("Fatality");
        listaAudio.add("Ti novo - Léo stronda");
        listaAudio.add("Ah sh*t here we go again - CJ");
        listaAudio.add("Não entendi esse final - Caracol raivoso");
        listaAudio.add("Oi amor, tá podendo falar ou sua mulher tá perto?");
        listaAudio.add("Som Roblox morrendo");
        listaAudio.add("Não - Pelé");
        listaAudio.add("Cê vai sentar é na cabeça");
        listaAudio.add("Prefiro morrer do que sentar na cabeça");
        listaAudio.add("Cê gosta de queijo");
        listaAudio.add("Três reais");
        listaAudio.add("Essa vai ser a minha melhor vigarice");
        listaAudio.add("Olha o demonio");
        listaAudio.add("Oloquinho meu");
        listaAudio.add("Tá pegando fogo bicho");
        listaAudio.add("Oloco bicho");
        listaAudio.add("Essa fera aí meu");
        listaAudio.add("O fila da p*t*");
        listaAudio.add("Eu me sinto foda");
        listaAudio.add("Primeira vez que eu vi de novo");
        listaAudio.add("Rusbé");
        listaAudio.add("Vem tranquilo");
        listaAudio.add("Vem afobado assim não");
        listaAudio.add("Corre negada");
        listaAudio.add("Que mama bonito");
        listaAudio.add("Nóis vai entrar na porrada em");
        listaAudio.add("Oi meu nome é Bettina");
        listaAudio.add("Vá a m*erda - Alborguetti");
        listaAudio.add("Já avisei que vai dar merda");
        listaAudio.add("Nego ney");
        listaAudio.add("Bigodinho fininho - Nego ney");
        listaAudio.add("Todo mundo morreu, acabou");
        listaAudio.add("É tá bom - Silvio Santos");
        listaAudio.add("Tomou na jabiraca");
        listaAudio.add("To nem ai");
        listaAudio.add("Tá chovendo pra caraca");
        listaAudio.add("Seu ousado");
        listaAudio.add("Badum Tss");
        listaAudio.add("Palmas");
        listaAudio.add("Sobrezizencia");
        listaAudio.add("Aqui choveu e relampagou");
        listaAudio.add("Aqui ta chovendo e repangalejando");
        listaAudio.add("Aqui relampeou e agalou tudo");
        listaAudio.add("Um que?");
        listaAudio.add("Um ah");
        listaAudio.add("Caberero - hulk");
        listaAudio.add("Saguadin");
        listaAudio.add("Bota aqui pra ver se não cabe");
        listaAudio.add("Eu não quero - Chaves");
        listaAudio.add("Pra que tanta violência");
        listaAudio.add("Zabuza Momochi");
        listaAudio.add("To lascado - Nando Moura");
        listaAudio.add("Mande aldo");
        listaAudio.add("Dimiscosta");
        listaAudio.add("To na praia");
        listaAudio.add("Rapaz deixa de ignorância");
        listaAudio.add("Cê ta doido");
        listaAudio.add("Meu nome é Ari");
        listaAudio.add("Mojang é meu ovo");
        listaAudio.add("Ele não vai não");
        listaAudio.add("Duas palavras PARA BENS");
        listaAudio.add("São 9 horas da manhã - Daciolo");
        listaAudio.add("Vem pro fut");
        listaAudio.add("O futebol tá estralando");
        listaAudio.add("É só tapa, só tapa");
        listaAudio.add("Voz de galinha");
        listaAudio.add("Eu to invicto - Voz de galinha");
        listaAudio.add("Glória a Deux - Daciolo");
        listaAudio.add("O miserável é um gênio");
        listaAudio.add("Azideia - Zóio");
        listaAudio.add("Quer namorar comigo? - Ronaldinho");
        listaAudio.add("Toma no c* hahaha");
        listaAudio.add("Ta bom não vou aguentar mais - Zóio");
        listaAudio.add("Orra tudo isso - papaco");
        listaAudio.add("Me da minha garrafa");
        listaAudio.add("Fi duma égua");
        listaAudio.add("Eu também");
        listaAudio.add("E quem disse que isso é problema meu");
        listaAudio.add("Eu não quero - Tiririca");
        listaAudio.add("Fiz uma baguncinha dentro da califórnia");
        listaAudio.add("Baby do birulaibe");
        listaAudio.add("Ah vá te tomar no c* rapaz");
        listaAudio.add("Bigodinho fininho, cabelinho na régua");
        listaAudio.add("Cachorro");
        listaAudio.add("Do you know the way?");
        listaAudio.add("Certamente que sim");
        listaAudio.add("Sai que é sua Taffarel ");
        listaAudio.add("Vou pegar minha marreta");
        listaAudio.add("Te dou uma facadona");
        listaAudio.add("Que onda é essa mermão");
        listaAudio.add("Corre cadeirante");
        listaAudio.add("Olha o bicho vindo moleque");
        listaAudio.add("Bom dia, vai tomar no c*");
        listaAudio.add("É mesmo é, foda-se");
        listaAudio.add("Dois mil anos depois");
        listaAudio.add("Moleque do c*ralho tem que matar uma porra dessa");
        listaAudio.add("Uma perda de tempo");
        listaAudio.add("Vai tomar no seu rabo");
        listaAudio.add("Olha o pastel");
        listaAudio.add("Moleque neutro - Cocielo");
        listaAudio.add("Really nigga");
        listaAudio.add("Ha gay");
        listaAudio.add("Eita desgraça");
        listaAudio.add("Eita porra do c*ralho");
        listaAudio.add("Eu não sei");
        listaAudio.add("Rachamo o zóio dele de tiro");
        listaAudio.add("Noice");
        listaAudio.add("É a melhor hora do dia");
        listaAudio.add("Wow Eddy Wally");
        listaAudio.add("É verdade, quer dizer às vezes não! - Drauzio");
        listaAudio.add("Eu quero dormir porra");
        listaAudio.add("Como sou bonito - Misty");
        listaAudio.add("Imitando moto");
        listaAudio.add("Seu pai tem gonorréia");
        listaAudio.add("Fala um A pra você ver");
        listaAudio.add("Seus zé ruela - Neto");
        listaAudio.add("Não vai dar! - Neto");
        listaAudio.add("Sumiu!");
        listaAudio.add("É uma cilada bino");
        listaAudio.add("I can hugs?");
        listaAudio.add("Bilada - BrksEdu");
        listaAudio.add("Impressionante como vocês tentam me derrubar");
        listaAudio.add("Se fodeu GTA");
        listaAudio.add("Mamilos polêmicos");
        listaAudio.add("É dois");
        listaAudio.add("Vegeta olha bem");
        listaAudio.add("Mentira hahaha");
        listaAudio.add("Eita c*zão");
        listaAudio.add("Ah vá, é mesmo?");
        listaAudio.add("Fala fiote");
        listaAudio.add("No céu tem pão?");
        listaAudio.add("O que é isso? - Maria do Rosário");
        listaAudio.add("Ai que dor - Zuzu");
        listaAudio.add("Se fudeu - Cauê Moura");
        listaAudio.add("Arregou");
        listaAudio.add("Meu deus - Dona Irene");
        listaAudio.add("c*ralho minha bola");
        listaAudio.add("Vingança do pernilongo");
        listaAudio.add("É mesmo é? - Homer");
        listaAudio.add("Ta me tirando?");
        listaAudio.add("Sou bem macho");
        listaAudio.add("Vou cagar na calça");
        listaAudio.add("Filha da puta - Torcedor América");
        listaAudio.add("Boa noite - Senhor poliglota");
        listaAudio.add("Nijuiti - Senhor poliglota");
        listaAudio.add("La paloma - Senhor poliglota");
        listaAudio.add("Ai mãe - Desliga esse computador");
        listaAudio.add("Uma dádiva dos ninjas");
        listaAudio.add("Pão de batata");
        listaAudio.add("Sai daqui demônio - Mustafary");
        listaAudio.add("Sai daqui seu cachorro");
        listaAudio.add("Você é o bichão mesmo em");
        listaAudio.add("Sou suspeito");
        listaAudio.add("Capeta prego");
        listaAudio.add("Aaaaaa marmota gritando");
        listaAudio.add("Não to lembrado não");
        listaAudio.add("Nunca nem vi");
        listaAudio.add("Que dia foi isso");
        listaAudio.add("Surprise motherfucker");
        listaAudio.add("É de mais de 8000");
        listaAudio.add("É você satanás?");
        listaAudio.add("Ai que burro da zero pra ele");
        listaAudio.add("Eu fui feito pra comer");
        listaAudio.add("Turn down for what");
        listaAudio.add("Me derrubaram aqui ô");
        listaAudio.add("Achou que eu tava brincando - Julius");
        listaAudio.add("Sai do meio menino oxi");
        listaAudio.add("Risada Carlos Alberto");
        listaAudio.add("Sou eu o Tiririca");
        listaAudio.add("O jardineiro é Jesus");
        listaAudio.add("Eu tenho probleminha - Fallen");
        listaAudio.add("É o que");
        listaAudio.add("Já avisei para calar sua boca");
        listaAudio.add("Não vai subir ninguém");
        listaAudio.add("Quero te quebrar filho - João da nica");
        listaAudio.add("Gatos não - João da nica");
        listaAudio.add("Sabe onde eu tô? - João da nica");
        listaAudio.add("Lionel messenger - João da nica");
        listaAudio.add("Fudeu de vez");
        listaAudio.add("Humm boiola");
        listaAudio.add("Nada nada nada nada");
        listaAudio.add("Que deselegante");
        listaAudio.add("É mentira");
        listaAudio.add("Hotel, trivago");
        listaAudio.add("Já procurou hotel na internet?");
        listaAudio.add("Allahu akbar som");
        listaAudio.add("Não dá pra ficar só em casa jogando videogame né?");
        listaAudio.add("Aloha, arola");
        listaAudio.add("Dollynho");
        listaAudio.add("Higor nera gay?");
        listaAudio.add("Woah crash");
        listaAudio.add("Eu sou rolezeira");
        listaAudio.add("Que demais, ai que delicia");
        listaAudio.add("Papa capim dos sonhos");
        listaAudio.add("Oi meu chapa");
        listaAudio.add("Omae wa mou shindeiru");
        listaAudio.add("Luan gameplay minecraft");
        listaAudio.add("Uh minha nossa");
        listaAudio.add("Senhora ?!");
        listaAudio.add("Que velocidade é essa menino!");
        listaAudio.add("Eu sou seu pai eu sou Jesus");
        listaAudio.add("Rodolfo porco");
        listaAudio.add("Música Illuminati");
        listaAudio.add("Cê é louco cachoeira");
        listaAudio.add("Só por deus irmão");
        listaAudio.add("Eita giovana");
        listaAudio.add("What? Whatafuck?");
        listaAudio.add("Sanduíche-iche");
        listaAudio.add("Sou o Jô Soares sua piranha");
        listaAudio.add("Acelera Jesus, acelera");
        listaAudio.add("Puta que pariu, passei");
        listaAudio.add("Faleceu - Maguila");
        listaAudio.add("Ai misericórdia");
        listaAudio.add("c*ralho eu sou um merda");
        listaAudio.add("E a vontade de chorar é inevitável");
        listaAudio.add("A vontade de rir é grande mas a de chorar é maior - Juão");
        listaAudio.add("O que foi que eu fiz com minha vida Jesus - Juão");
        listaAudio.add("Será que um dia eu vou vencer na vida? - Juão");
        listaAudio.add("Você maconheiro vai morrer daqui pro natal - Sikera");
        listaAudio.add("É o crime é nóis - Sikera Junior");
        listaAudio.add("Some daqui - João Gordo");
        listaAudio.add("Já olhou pra alguém e pensou o que passa na cabeça dela?");
        listaAudio.add("Mais ou menos - Poderoso castiga");
        listaAudio.add("Nhaa");
        listaAudio.add("Cavalo imundo do c*ralho - Hastad");
        listaAudio.add("Porra jamelão");
        listaAudio.add("Sou faraó");
        listaAudio.add("A lua é redonda e não tem porta");
        listaAudio.add("E por que o homem não foi no sol ?");
        listaAudio.add("Elemento vagabundo, cara de pau");
        listaAudio.add("Tem que rir pra não chorar - Davy Jones");
        listaAudio.add("Envolvente, diferente, interessante - Davy Jones");
        listaAudio.add("Pimba - Davy Jones");
        listaAudio.add("Eu gostei em - Davy Joves");
        listaAudio.add("Esse jogo é maneirão em - Davy Joves");
        listaAudio.add("Maneiro pra c*ralho - Davy Joves");
        listaAudio.add("Quero foder com os outros - Davy Joves");
        listaAudio.add("Eu to brabo - Davy Joves");
        listaAudio.add("É mas não vou devolver não");
        listaAudio.add("Morri");
        listaAudio.add("E morreu - Didi");
        listaAudio.add("Fon - Yoda");
        listaAudio.add("Orra diacho - Whindersson");
        listaAudio.add("Nossa que bosta");
        listaAudio.add("Lá vem os babacas");
        listaAudio.add("Da que te dou outra - Bolsonaro");
        listaAudio.add("Pô não to de novo que decepção - Bolsonaro");
        listaAudio.add("Pepino de novo");
        listaAudio.add("Malakoi, do hebraico - Nando");
        listaAudio.add("Mas o que é isso que está aparecendo no meu computador - Nando");
        listaAudio.add("Você é zueiro mesmo em cara - Nando");
        listaAudio.add("Para com essa porra");
        listaAudio.add("Eu não acredito nessa porra");
        listaAudio.add("Sai da frente satanás");
        listaAudio.add("Sou uma foca - Luccas Neto");
        listaAudio.add("Cê tem demência");
        listaAudio.add("Essa gente inventa cada coisa");
        listaAudio.add("Deuses são reais se acredita neles");
        listaAudio.add("c*ralho mané");
        listaAudio.add("Já acabou jéssica");
        listaAudio.add("Já vai? - Lucas");
        listaAudio.add("Qué ota eu vou da ota - Lucas");
        listaAudio.add("Não sei - Preso");
        listaAudio.add("Iziiiii - Inhegas");
        listaAudio.add("Feijão torpedo");
        listaAudio.add("Ih Rapaz");
        listaAudio.add("Eu vou cair");
        listaAudio.add("Nunca mexa");
        listaAudio.add("Leite compensado");
        listaAudio.add("Elaio é fistaile");
        listaAudio.add("Eita porra");
        listaAudio.add("Ahhh");
        listaAudio.add("Hum é mesmo");
        listaAudio.add("Seu b*ceta");
        listaAudio.add("Não consegue né");
        listaAudio.add("Oe - Silvio Santos");
        listaAudio.add("E o bambu?");
        listaAudio.add("Aham senta lá Cláudia");
        listaAudio.add("Ah agora eu entendi");
        listaAudio.add("Cepo de madeira");
        listaAudio.add("Bem duro, para dar mais impacto");
        listaAudio.add("Amanda klein");
        listaAudio.add("Taca-le pau nesse carrinho marcos");
        listaAudio.add("Cade o chinelo");
        listaAudio.add("Mati");
        listaAudio.add("Ouch Charlie");
        listaAudio.add("Desgraça - Filósofo piton");
        listaAudio.add("Trollei");
        listaAudio.add("c*ralho olha os deuses mano");
        listaAudio.add("Quem é esse cara velho");
        listaAudio.add("Sem nexo");
        listaAudio.add("Nada a ver");
        listaAudio.add("To fingindo");
        listaAudio.add("Já entendi agora");
        listaAudio.add("Oh o pau quebrando");
        listaAudio.add("Fui tapeado");
        listaAudio.add("Oh se fodeu");
        listaAudio.add("Eu não entendi o que ele falou");
        listaAudio.add("Corre berg");
        listaAudio.add("Jubileu está esquisito hoje");
        listaAudio.add("John Cena");
        listaAudio.add("Hora do show");
        listaAudio.add("Ajuda o maluco que tá doente");
        listaAudio.add("Birl");
        listaAudio.add("Saí de casa comi pra c*ralho");
        listaAudio.add("Faz isso comigo não velho");
        listaAudio.add("Guarapari búzio é minha arte");
        listaAudio.add("Sai daí doido");
        listaAudio.add("Samu seu c*");
        listaAudio.add("É ué");
        listaAudio.add("Cê acha que eu ia fazer isso?");
        listaAudio.add("Comprar alimento - Galo cego");
        listaAudio.add("Nada a ver irmão");
        listaAudio.add("Não sabia");
        listaAudio.add("Tudo errado essas perguntas");
        listaAudio.add("Animal burrão");
        listaAudio.add("Ai meu c*");
        listaAudio.add("Você é maluco é");
        listaAudio.add("Me dê licença que agora vou cagar");
        listaAudio.add("Ei seu bunda mole - Papaco");
        listaAudio.add("Fala o que você quer de uma vez - Papaco");
        listaAudio.add("Até um outro dia - Papaco");
        listaAudio.add("E quem foi o cagão - Papaco");
        listaAudio.add("Falou comigo - Papaco");
        listaAudio.add("Ainda bem - Papaco");
        listaAudio.add("Esse programa aqui tá uma porra");
        listaAudio.add("Viadão bonito");
        listaAudio.add("Estou sentindo uma treta");
        listaAudio.add("Tenho 2 real");
        listaAudio.add("Já tava bom");
        listaAudio.add("Que cachorro o que");
        listaAudio.add("Me de papai");
        listaAudio.add("Miopia");
        listaAudio.add("Tá pegando fogo bicho");
        listaAudio.add("Errou");
        listaAudio.add("É tetra");
        listaAudio.add("Errou feio errou rude");
        listaAudio.add("Hoje não");
        listaAudio.add("Negros maravilhosos");
        listaAudio.add("Você é burro cara");
        listaAudio.add("Cachaça carai");
        listaAudio.add("Esse é o tal do mula");
        listaAudio.add("Que merda hein sabia não");
        listaAudio.add("Puta falta de sacanagem");
        listaAudio.add("Pai chorando");
        listaAudio.add("Morre diabo");
        listaAudio.add("Não interessa pra você");
        listaAudio.add("Tô falando não tô falando");
        listaAudio.add("Sai desgraça");
        listaAudio.add("Ai papai, beija na boca");
        listaAudio.add("Cadê o meu pau - Alborghetti");
        listaAudio.add("Bem feito - Alborghetti");
        listaAudio.add("Tá uma merda - Alborghetti");
        listaAudio.add("Eu tô louco - Alborghetti");
        listaAudio.add("Ah vá a merda - Alborghetti");
        listaAudio.add("Agaragã");
        listaAudio.add("Ai pai para");
        listaAudio.add("Esquece essa merda aí");
        listaAudio.add("Que satisfação aspira");
        listaAudio.add("Você está impossível");
        listaAudio.add("Faz o urro");
        listaAudio.add("Que viagem é essa");
        listaAudio.add("c*ralho borracha mano");
        listaAudio.add("Coé rapaziada");
        listaAudio.add("Cê é feio em fi - Dougras");
        listaAudio.add("Eu sou o dogras");
        listaAudio.add("Barulho onça - Serjão");
        listaAudio.add("Aqui tem coragem - Serjão");
        listaAudio.add("Autoridades do ibama");
        listaAudio.add("Com fé no pai eterno sempre aqui estou e vou estar - Serjão");
        listaAudio.add("É a verdade não minto - Serjão");
        listaAudio.add("Mais conhecido como matador de onça - Serjão");
        listaAudio.add("É perigo na calça ele cagar - Serjão");
        listaAudio.add("Na hora que o burrai do pipoco come - Serjão");
        listaAudio.add("Mas valeu por duas ou mais - Serjão");
        listaAudio.add("Foi ótimo");
        listaAudio.add("Exatamente");
        listaAudio.add("Eu pito muito");
        listaAudio.add("Se eu pudesse eu matava mil");
        listaAudio.add("Irineu você não sabe nem eu");
        listaAudio.add("O maluco é brabo");
        listaAudio.add("HAN HEIN!");
        listaAudio.add("A firma que mandou né");
        listaAudio.add("Cacete de agulha");
        listaAudio.add("Olá marilene");
        listaAudio.add("Miau");
        listaAudio.add("Acertou miserável");
        listaAudio.add("Não sei");
        listaAudio.add("Chega manteiga derrete");
        listaAudio.add("Do cacetinho");
        listaAudio.add("Tá quentinho");
        listaAudio.add("Pesadão");
        listaAudio.add("Bem louco");
        listaAudio.add("Empolgante");
        listaAudio.add("Tô cagado de fome");
        listaAudio.add("Eu vim é pra passear");
        listaAudio.add("Essa peça que você queria?");
        listaAudio.add("Bicho piruleta");
        listaAudio.add("Tira as calças sem zorba - Bicho piruleta");
        listaAudio.add("Quero café");
        listaAudio.add("Me desculpe");
        listaAudio.add("Vai morrer viado");
        listaAudio.add("E ae vamo fecha");
        listaAudio.add("Isso ai é mó ideia errada");
        listaAudio.add("Tem uns meninos que são muitos brutos");
        listaAudio.add("Tapita camisa rosa é coisa de viado");
        listaAudio.add("Unimed - Tapita");
        listaAudio.add("Tapita");
        listaAudio.add("Ai");
        listaAudio.add("Vai filhão");
        listaAudio.add("Ai ai");
        listaAudio.add("E só");
    }

    public void carregarListaMusica() {
        listaMusica.add("Cheiro de Somebody That I Used to Know");
        listaMusica.add("Cheiro de Don't Start Now");
        listaMusica.add("Cheiro de Blinding Lights");
        listaMusica.add("Blinding Azeitona");
        listaMusica.add("Cabeleleila Leila");
        listaMusica.add("Meme do caixão");
        listaMusica.add("Corona virus brega funk");
        listaMusica.add("Rato dorime");
        listaMusica.add("Buttercup");
        listaMusica.add("Funk bom dia meu consagrado");
        listaMusica.add("Caneta azul");
        listaMusica.add("Beat ticolé");
        listaMusica.add("Quero café - Remix Atilakw");
        listaMusica.add("Funk deep web");
        listaMusica.add("Baby I'm yours meme");
        listaMusica.add("Intro universal grito");
        listaMusica.add("Cachorrinho fia da p*ta");
        listaMusica.add("Boate azul meme");
        listaMusica.add("Naruto flauta desafinada");
        listaMusica.add("Música cachorro chorando");
        listaMusica.add("Música Ricardo Milos");
        listaMusica.add("Beat do Nego Ney");
        listaMusica.add("Beat da Tempestade");
        listaMusica.add("Beat da Recuperação");
        listaMusica.add("O nome dele é Alan - funk");
        listaMusica.add("Oh lokinho meu trap");
        listaMusica.add("Randandandan funk");
        listaMusica.add("Vuc Vuc remix");
        listaMusica.add("Chovendo e repanguelejando remix");
        listaMusica.add("The lion sleeps tonight");
        listaMusica.add("Mini boi");
        listaMusica.add("Papa capim remix");
        listaMusica.add("Ticolé");
        listaMusica.add("Sopa de macaco");
        listaMusica.add("Oh o gás");
        listaMusica.add("Solta a pisadinha forró");
        listaMusica.add("Ela é uma boa menina");
        listaMusica.add("Cara ela tá tão na sua funk");
        listaMusica.add("Sai cocozinho do butico do miguel");
        listaMusica.add("Mission passed GTA");
        listaMusica.add("É na palma da bota");
        listaMusica.add("Harry Potter Estourado");
        listaMusica.add("Hold On - Baguncinha");
        listaMusica.add("Alone - Minguado");
        listaMusica.add("Believer - Minguado");
        listaMusica.add("Tema Evil Morty");
        listaMusica.add("A Thousand Years Funk");
        listaMusica.add("Marcelo e o suco de caju");
        listaMusica.add("Que tiro foi esse");
        listaMusica.add("Sangue de Jesus tem poder");
        listaMusica.add("New Rules - Sangue de Jesus");
        listaMusica.add("Agora tem poder");
        listaMusica.add("Moleque neutro - Júlio Cocielo");
        listaMusica.add("No céu tem pão pão pão");
        listaMusica.add("Aleluia - Júlio Cocielo");
        listaMusica.add("Banda Djavu meme");
        listaMusica.add("Vitas - grito");
        listaMusica.add("7h Element - Vitas");
        listaMusica.add("Vitas lindinho 2009");
        listaMusica.add("Super Vitas World");
        listaMusica.add("Vitas espetacular");
        listaMusica.add("Let it go funk");
        listaMusica.add("Abusadamente - MC Moto Véia");
        listaMusica.add("Let me love you - MC Moto Véia");
        listaMusica.add("Brincadeira tem hora");
        listaMusica.add("Harry Potter funk");
        listaMusica.add("Dragon Ball Super Funk");
        listaMusica.add("Ameno funk remix");
        listaMusica.add("I'm yours - Mc Doguinha");
        listaMusica.add("XXXampion");
        listaMusica.add("Sou nou");
        listaMusica.add("Run - Awolnation");
        listaMusica.add("I have a sampley");
        listaMusica.add("Eu não te perguntei");
        listaMusica.add("Sanic tema");
        listaMusica.add("Sai da frente satanás remix");
        listaMusica.add("Olha eu com boné");
        listaMusica.add("What's going on He-man");
        listaMusica.add("Gilderlan xenhenhém");
        listaMusica.add("Rap do solitário");
        listaMusica.add("Jocelyn flores");
        listaMusica.add("whatcha say");
        listaMusica.add("Sound of silence funk");
        listaMusica.add("Tô com fome - Larica dos mulekes");
        listaMusica.add("You're beautiful - Daniel Gavião");
        listaMusica.add("Uni duni tê funk");
        listaMusica.add("Shake it bololo");
        listaMusica.add("Moto dos profetas");
        listaMusica.add("Pumped up kicks funk");
        listaMusica.add("Soco bate funk - Xuxa");
        listaMusica.add("Acertou miserável remix");
        listaMusica.add("Rap do homem macaco");
        listaMusica.add("Vai tomar no c* música");
        listaMusica.add("Suco de maracuja - I Took a Pill in Ibiza");
        listaMusica.add("Agora vai maracujar");
        listaMusica.add("Havana - Suco de maracuja");
        listaMusica.add("Suco de maracujá");
        listaMusica.add("Suco de maracujá runaway");
        listaMusica.add("Mc Champion  - Onde chego paro tudo");
        listaMusica.add("Estocando vento - Dilma");
        listaMusica.add("Saudação à mandioca - Dilma");
        listaMusica.add("Teclado lindinho 2009");
        listaMusica.add("Irineu remix");
        listaMusica.add("Stressed out - Irineu");
        listaMusica.add("Irineu mandela");
        listaMusica.add("Chaves funk triste");
        listaMusica.add("Mc mudinho");
        listaMusica.add("Chupava até o suvaco");
        listaMusica.add("É mais de 300 reais remix");
        listaMusica.add("Illuminati funk");
        listaMusica.add("Serjão paródia rabetão");
        listaMusica.add("Ta tranquilo - Bin Laden");
        listaMusica.add("Tchu tcha");
        listaMusica.add("É maconha doido - Música Sikera");
        listaMusica.add("Música da goiaba");
        listaMusica.add("Shooting Stars inhegas");
        listaMusica.add("Ta ficando apertado remix - Venom");
        listaMusica.add("Ta chovendo aí?");
        listaMusica.add("Abertura Different Strokes");
        listaMusica.add("To be continued");
        listaMusica.add("Oração pai nosso funk");
        listaMusica.add("Dirigindo meu carro funk");
        listaMusica.add("Magrelinho");
        listaMusica.add("Balão mágico funk");
        listaMusica.add("Titanic flauta");
        listaMusica.add("Dei um soco na preguiça - Maguila");
        listaMusica.add("Proerd funk");
        listaMusica.add("Clube das Winx Funk");
        listaMusica.add("Carreta furacão");
        listaMusica.add("Nunca mais eu vou dormir");
        listaMusica.add("Jooj chaves");
        listaMusica.add("Poxa crush");
        listaMusica.add("Sample de guitarra GTA");
        listaMusica.add("Sample de guitarra");
        listaMusica.add("Deixa os garoto brincar");
        listaMusica.add("C*aralho sem nexo");
        listaMusica.add("Rhythm of the night");
        listaMusica.add("Toca pisadinha");
        listaMusica.add("Sonim bleinem Diff'rent Strokes");
        listaMusica.add("Sonim bleinem All Star");
        listaMusica.add("Sweet Dreams");
        listaMusica.add("Panificadora alfa");
        listaMusica.add("Trololo");
        listaMusica.add("Funk inglês");
        listaMusica.add("Música do Foda-se");
        listaMusica.add("Monstro de um olho só");
        listaMusica.add("Música Isolados");
        listaMusica.add("Flauta triste");
        listaMusica.add("Harry potter flauta");
        listaMusica.add("Vai dar merda");
        listaMusica.add("Chama - Cleiton Rasta");
        listaMusica.add("Tongo - Pumped up kicks");
        listaMusica.add("Numb Tongo");
        listaMusica.add("Chop Suey Tongo");
        listaMusica.add("Naruto funk");
    }

    @Override
    protected void onPause() {
        if (!favorito) {
            if (botaoBuscar != null) {
                botaoBuscar.collapseActionView();
            }

            removerPesquisa();
            if (m1.sons != null) {
                m1.desmontarAudioPesquisa();
            }

            if (m2.sons != null) {
                m2.desmontarAudioPesquisa();
            }
        }
        super.onPause();
    }

    public String buscarNomeAudio(int posicao) {
        listaAudio = new ArrayList<>();
        carregarListaAudio();
        return listaAudio.get(posicao);
    }

    public String buscarNomeMusica(int posicao) {
        listaMusica = new ArrayList<>();
        carregarListaMusica();
        return listaMusica.get(posicao);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        hideNavigationBar();
        super.onConfigurationChanged(newConfig);
    }

    /*public void getJson() {
        String HTTP_REQUEST_VIDEOS = "https://www.googleapis.com/youtube/v3/search/?";
        String part = "snippet";
        String channelId = "UCx_RRmmNeGn2QoqnXF2q2aA";
        String maxResults = "15";
        String order = "date";
        String key = DeveloperKey.DEVELOPER_KEY;
        String urlRequest = HTTP_REQUEST_VIDEOS + "part=" + part + "&channelId=" + channelId + "&maxResults=" + maxResults + "&order=" + order + "&key=" + key;

        if (!isConnected()) {
            Toast.makeText(getApplicationContext(), R.string.semConexao, Toast.LENGTH_LONG).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        final JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, urlRequest, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        jsonYoutube = response;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("Json", jsonYoutube.toString());
                        editor.commit();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        
                    }
                });
        queue.add(jsObjRequest);
    }*/

    /*public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }*/

      /*
    private void recreateActivity() {
        //Delaying activity recreate by 1 millisecond. If the recreate is not delayed and is done
        // immediately in onResume() you will get RuntimeException: Performing pause of activity that is not resumed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                recreate();
            }
        }, 1);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            //m2.pause(false);//pausar a musica quando trocar a tela
        }
    }
    */


}
