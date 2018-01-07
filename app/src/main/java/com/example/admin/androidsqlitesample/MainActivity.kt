package com.example.admin.androidsqlitesample

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
import com.example.admin.androidsqlitesample.FeedReaderContract.FeedEntry
import android.content.ContentValues
import android.util.Log


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // DB操作用のオブジェクトをwriteモードで取得
        val mDbHelper = FeedReaderDbHelper(this)
        val dbWrite = mDbHelper.writableDatabase

        // カラム名と値のmapを作成
        val values = ContentValues()
        values.put(FeedEntry.COLUMN_NAME_TITLE, "mytitle")
        values.put(FeedEntry.COLUMN_NAME_SUBTITLE, "mysubtitle")

        // テーブル名指定して、行を書き込み
        val newRowId = dbWrite.insert(FeedEntry.TABLE_NAME, null, values)

        dbWrite.close()


        // DB操作用のオブジェクトをreadモードで取得
        val dbRead = mDbHelper.readableDatabase

        // SELECTするカラムを指定
        val projection = arrayOf(FeedEntry._ID, FeedEntry.COLUMN_NAME_TITLE, FeedEntry.COLUMN_NAME_SUBTITLE)

        // WHERE条件の指定
        val selection = FeedEntry.COLUMN_NAME_TITLE + " = ?"
        val selectionArgs = arrayOf("mytitle")

        // ソートの指定
        val sortOrder = FeedEntry.COLUMN_NAME_SUBTITLE + " DESC"

        // カーソル取得
        val cursor = dbRead.query(
                FeedEntry.TABLE_NAME, // 対象テーブル
                projection, // 列指定
                selection, // WHERE指定
                selectionArgs, // WHERE指定条件
                null, null, // groupby,havingは指定しない
                sortOrder  // ソート条件
        )

        // カーソルを動かしながら値取得する
        var mov = cursor.moveToFirst()
        while (mov) {
            val id = cursor.getInt(0)
            val title = cursor.getString(1)
            val subtitle = cursor.getString(2)

            Log.i("dbInfo", "id: ${id}, title: ${title}, subtitle: ${subtitle}")

            mov = cursor.moveToNext()
        }
        cursor.close()
        dbRead.close()
    }
}

// DB操作に使うヘルパークラス
class FeedReaderDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // DB操作用のSQL
    private val TEXT_TYPE = " TEXT"
    private val COMMA_SEP = ","
    private val SQL_CREATE_ENTRIES = "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
            FeedEntry._ID + " INTEGER PRIMARY KEY," +
            FeedEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
            FeedEntry.COLUMN_NAME_SUBTITLE + TEXT_TYPE + " )"

    private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME

    // イベントハンドラ
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 外部サービスからAPIなどで取得したデータをDBにキャッシュしている想定
        // このためアップグレードのときは、単純に既存のキャッシュをクリアして、DB再作成している
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        // スキーマのアップグレードのときは、DATABASE_VERSIONの数字をふやす
        val DATABASE_VERSION = 1
        val DATABASE_NAME = "FeedReader.db"
    }
}

// DB内のテーブル定義クラス
object FeedReaderContract {
    // 個々のテーブル行の定義
    class FeedEntry : BaseColumns {
        companion object {
            val _ID = BaseColumns._ID // BaseColumnsの継承で使えるはずだがうまく入らなかったので追加している
            val TABLE_NAME = "entry"
            val COLUMN_NAME_TITLE = "title"
            val COLUMN_NAME_SUBTITLE = "subtitle"
        }
    }
}
