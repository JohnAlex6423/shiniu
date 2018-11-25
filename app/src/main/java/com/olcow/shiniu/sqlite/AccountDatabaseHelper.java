package com.olcow.shiniu.sqlite;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

public class AccountDatabaseHelper extends SQLiteOpenHelper {
    private static Integer Version = 1;

    /**
     * 构造函数
     * 在SQLiteOpenHelper的子类中，必须有该构造函数
     */
    public AccountDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        // 参数说明
        // context：上下文对象
        // name：数据库名称
        // param：一个可选的游标工厂（通常是 Null）
        // version：当前数据库的版本，值必须是整数并且是递增的状态

        // 必须通过super调用父类的构造函数
        super(context, name, factory, version);
    }

    /**
     * 构造函数
     * 在SQLiteOpenHelper的子类中，必须有该构造函数
     */
    public AccountDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    /**
     * 构造函数
     * 在SQLiteOpenHelper的子类中，必须有该构造函数
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    public AccountDatabaseHelper(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    /**
     * 复写onCreate（）
     * 调用时刻：当数据库第1次创建时调用
     * 作用：创建数据库 表 & 初始化数据
     * SQLite数据库创建支持的数据类型： 整型数据、字符串类型、日期类型、二进制
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("sqlite", "onCreate: 创建数据库");
        String sql = "create table account(session varchar(32),uid integer primary key,username varchar(64),email varchar(64),permission integer)";
        db.execSQL(sql);
        db.execSQL("create table userinfo (uid Integer primary key,name varchar(32),avatar varchar(128),introduction varchar(128))");
        // 注：数据库实际上是没被创建 / 打开的（因该方法还没调用）
        // 直到getWritableDatabase() / getReadableDatabase() 第一次被调用时才会进行创建 / 打开
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 参数说明：
        // db ： 数据库
        // oldVersion ： 旧版本数据库
        // newVersion ： 新版本数据库

        // 使用 SQL的ALTER语句
//        String sql = "alter table account add permission integer";
//        db.execSQL(sql);
    }
}
