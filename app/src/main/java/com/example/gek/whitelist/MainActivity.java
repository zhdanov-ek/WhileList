package com.example.gek.whitelist;

//todo Выяснить следующие вопросы:
/**
 * 1) Как ограничить доступ для подключения к БД через белый список
 * 2) В документации пишут, что данные при офлайне должны отображаться. Как это делать?
 *    Эти данные храняться где-то или нужно их сохранять самостоятельно
 * 3) Каким образом лучше всего заносить данные в БД?
 * 4) Как переносить весь проект из одной учетки гугл в другую? Например по окончанию разработки
 * */


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.example.gek.whitelist.data.ContactCard;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener{

    TextView tvInfo;

    public static final String ANONYMOUS = "anonymous";

    // Firebase instance variables
    // Аутентификация
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    // Приглашение на установку
    private GoogleApiClient mGoogleApiClient;

    // обращение к БД
    private DatabaseReference mFirebaseDatabaseReference;

    private String mUsername;
    private String mPhotoUrl;

    private final static String TAG = "11111";

    private LinearLayoutManager mLinearLayoutManager;

    private FirebaseRecyclerAdapter<ContactCard, ContactViewHolder> mFirebaseAdapter;
    private RecyclerView mRecyclerView;


    /** Вью холдер для отображения в списке */
    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public TextView tvPhone;
        public TextView tvEmail;

        public ContactViewHolder(View v) {
            super(v);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvPhone = (TextView) itemView.findViewById(R.id.tvPhone);
            tvEmail = (TextView) itemView.findViewById(R.id.tvEmail);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvInfo = (TextView) findViewById(R.id.tvInfo);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycleView);
        // Set default username is anonymous.
        mUsername = ANONYMOUS;

        // --------- АВТОРИЗАЦИЯ ----------
        // Получаем состояние FirebaseAuth и смотрим есть ли в нем юзер
        // Если нет то открываем окно авторизации и завершаем работу с этой активити.
        // Если есть то получаем URL иконки и продолжаем работать с этой активити.
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            print("mUsername = " + mUsername);
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        findViewById(R.id.btnLogOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              signOut();
            }
        });


        // ----------- Обращение к БД и получение данных ----------------
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<ContactCard, ContactViewHolder>(
                ContactCard.class,
                R.layout.item_contact,
                ContactViewHolder.class,
                mFirebaseDatabaseReference.child("contacts")) {

            @Override
            protected void populateViewHolder(ContactViewHolder viewHolder,
                                              ContactCard contactCard,
                                              int position) {
                viewHolder.tvName.setText(contactCard.getName());
                viewHolder.tvPhone.setText(contactCard.getPhone());
                viewHolder.tvEmail.setText(contactCard.getEmail());
                }
        };

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mFirebaseAdapter);


    }

    // Отзываем авторизацию
    private void signOut(){
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        mUsername = ANONYMOUS;
        startActivity(new Intent(this, SignInActivity.class));
    }

    private void print(String s){
        String str = tvInfo.getText().toString().concat("\n" + s);
        tvInfo.setText(str);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
