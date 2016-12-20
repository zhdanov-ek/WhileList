package com.example.gek.whitelist.data;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by gek on 18.12.16.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Включаем кеширование данных что позволяет отображать данные офлайн
        // Инициализация этого значения делается до начала работы с FireBase
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}