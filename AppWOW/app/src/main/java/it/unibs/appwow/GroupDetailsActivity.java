package it.unibs.appwow;

import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import it.unibs.appwow.database.CostsDAO;
import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.database.UserDAO;
import it.unibs.appwow.database.UserGroupDAO;
import it.unibs.appwow.fragments.AmountsFragment;
import it.unibs.appwow.fragments.CostsFragment;
import it.unibs.appwow.fragments.GroupListFragment;
import it.unibs.appwow.fragments.TransactionsFragment;
import it.unibs.appwow.models.CostDummy;
import it.unibs.appwow.models.CostModel;
import it.unibs.appwow.models.UserGroupModel;
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.User;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.DateUtils;
import it.unibs.appwow.utils.dummy.DummyTransactionContent;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.ser.Group;

public class GroupDetailsActivity extends AppCompatActivity implements CostsFragment.OnListFragmentInteractionListener,
        AmountsFragment.OnListFragmentInteractionListener,
        TransactionsFragment.OnListFragmentInteractionListener,
        SwipeRefreshLayout.OnRefreshListener{

    private final String TAG_LOG = GroupDetailsActivity.class.getSimpleName();

    /**
     * Gruppo ricevuto, già "pieno"
     */
    private Group mGroup;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private User mUser;
    //private int mRequestPending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mRequestPending = 0;
        
        setContentView(R.layout.activity_group_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.single_group_swipe_refresh_layout);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.setCurrentItem(1);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.d(TAG_LOG,"Page Scrolled : "+position);
            }

            @Override
            public void onPageSelected(int position) {
                //Log.d(TAG_LOG,"Page Selected : "+position);
                GroupDetailsActivity.this.invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mUser = User.load(MyApplication.getAppContext());
        mGroup = getIntent().getParcelableExtra(GroupListFragment.PASSING_GROUP_TAG);
        setTitle(mGroup.getGroupName());
        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        mSwipeRefreshLayout.post(new Runnable() {
                                     @Override
                                     public void run() {
                                         mSwipeRefreshLayout.setRefreshing(true);
                                         fetchGroupDetails();
                                     }
                                 }
        );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(CostDummy item) {
        // TODO: 07/05/2016 Qui va implementato l'evento da gestire alla selezione dell'item
        Toast.makeText(GroupDetailsActivity.this, "Item: " + item.id, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onListFragmentInteraction(Amount item) {
        // TODO: 10/05/2016  Qui va implementato l'evento da gestire alla selezione dell'item
        Toast.makeText(GroupDetailsActivity.this, "Item: " + item.id, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onListFragmentInteraction(DummyTransactionContent.Transaction item) {
        // TODO: 10/05/2016  Qui va implementato l'evento da gestire alla selezione dell'item
    }

    @Override
    public void onRefresh() {
        fetchGroupDetails();
    }

    private void fetchGroupDetails(){
        Log.d(TAG_LOG,"fetchGroupDetails");
        // showing refresh animation before making http call

        mSwipeRefreshLayout.setRefreshing(true);
        fetchUsers();
        //mRequestPending = 0;
        /**
         * ATTENZIONE QUI SI AGGIORNA L'INTERO GRUPPO COMPRENDENDO:
         * Users
         * Costs
         * Transactions
         * Balancings
         */
        //CONTROLLO che il gruppo sia da aggiornare
        Uri groupUri = WebServiceUri.getGroupUri(mGroup.getId());
        URL url = WebServiceUri.uriToUrl(groupUri);
        JsonObjectRequest groupRequest = new JsonObjectRequest(url.toString(), null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    GroupDAO dao = new GroupDAO();
                    dao.open();
                    try{
                        String server_updated_at_string = response.getString("updated_at");
                        long server_updated_at = DateUtils.dateToLong(server_updated_at_string);
                        long local_updated_at = dao.getUpdatedAt(mGroup.getId());

                        if (server_updated_at > local_updated_at) {
                            fetchCosts();
                        } else {
                            //se il gruppo locale è più aggiornato di quello del server?
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    } catch(JSONException e){
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG_LOG, "VOLLEY_ERROR - " + "Server Error: " + error.getMessage());
                    Toast.makeText(MyApplication.getAppContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    // stopping swipe refresh
                    mSwipeRefreshLayout.setRefreshing(false);
                }
        });
        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(groupRequest);
        //mRequestPending++;


        /**
         * AGGIORNAMENTO TRANSACTIONS
         */
        // TODO: 08/06/2016
        /**
         * AGGIORNAMENTO BALANCING?
         */
        // TODO: 08/06/2016


        //mSwipeRefreshLayout.setRefreshing(false);


    }

    private void fetchUsers(){
        /**
         * AGGIORNAMENTO USERS (USER_GROUP)
         */
        // Volley's json array request object
        Uri groupUsersUri = WebServiceUri.getGroupUsersUri(mGroup.getId());
        URL url = WebServiceUri.uriToUrl(groupUsersUri);
        JsonArrayRequest usersRequest = new JsonArrayRequest(url.toString(),
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Log.d(TAG_LOG, "Response = " + response.toString());

                    if (response.length() > 0) {
                        UserDAO dao = new UserDAO();
                        dao.open();
                        UserGroupDAO dao1 = new UserGroupDAO();
                        dao1.open();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                // TODO: 15/06/2016 SISTEMARE COME PER PIVOT PER RENDERLO PIU' COMPRENSIBILE
                                JSONObject userJs = response.getJSONObject(i);
                                int id = userJs.getInt("id");
                                String server_updated_at_string = userJs.getString("updated_at");
                                long server_updated_at = DateUtils.dateToLong(server_updated_at_string);
                                long local_updated_at = dao.getUpdatedAt(id);
                                //aggiorno lo user solo se subito modifiche
                                if (server_updated_at > local_updated_at) {
                                    String fullName = userJs.getString("fullName");
                                    String email = userJs.getString("email");
                                    String created_at_string = userJs.getString("created_at");
                                    long created_at = DateUtils.dateToLong(created_at_string);
                                    UserModel u = UserModel.create(id).withFullName(fullName)
                                            .withEmail(email)
                                            .withCreatedAt(created_at)
                                            .withUpdatedAt(server_updated_at);
                                    dao.insertUser(u);
                                } else {
                                    //per ora non faccio niente
                                }

                                JSONObject pivot = userJs.getJSONObject("pivot");
                                UserGroupModel piv = UserGroupModel.create(pivot);
                                if(!piv.isUpdated()){
                                    dao1.insertUserGroup(piv);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        dao.close();
                        dao1.close();
                        /*// mAdapter.notifyDataSetChanged();
                        mAdapter = new GroupAdapter(getActivity());
                        mGridView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();*/

                    } else {
                       // Toast.makeText(MyApplication.getAppContext(), "ERRORE SCONOSCIUTO", Toast.LENGTH_LONG).show();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG_LOG, "VOLLEY_ERROR - " + "Server Error: " + error.getMessage());
                    Toast.makeText(MyApplication.getAppContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                }
        });
        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(usersRequest);
    }

    private void fetchCosts(){
        Uri groupUsersUri = WebServiceUri.getGroupUsersUri(mGroup.getId());
        Uri groupCostsUri = Uri.withAppendedPath(groupUsersUri,"costs");
        URL url = WebServiceUri.uriToUrl(groupCostsUri);

        JsonArrayRequest costsRequest = new JsonArrayRequest(url.toString(),
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response.length() > 0){
                            CostsDAO dao = new CostsDAO();
                            dao.open();
                            for(int i = 0; i<response.length();i++){
                                try{
                                    JSONObject costJs = response.getJSONObject(i);
                                    CostModel cost = CostModel.create(costJs);
                                } catch(JSONException e){
                                    e.printStackTrace();
                                }
                            }
                            dao.close();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG_LOG, "VOLLEY_ERROR - " + "Server Error: " + error.getMessage());
                        Toast.makeText(MyApplication.getAppContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
        MyApplication.getInstance().addToRequestQueue(costsRequest);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1)
            switch (position) {
                case 0:
                    return CostsFragment.newInstance(1);
                case 1:
                    return AmountsFragment.newInstance(1);
                case 2:
                    return TransactionsFragment.newInstance(1);
            }
            return null;
            //Log.d(TAG_LOG,"Position: "+position);

        }


        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // TODO: 10/05/2016  STRINGHE
            switch (position) {
                case 0:
                    return "SPESE";
                case 1:
                    return "RIASSUNTO";
                case 2:
                    return "SCAMBI";
            }
            return null;
        }
    }
}
