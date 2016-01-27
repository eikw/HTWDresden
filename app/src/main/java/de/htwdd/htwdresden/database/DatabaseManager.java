package de.htwdd.htwdresden.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.htwdd.htwdresden.classes.Const;

/**
 * Zugriff auf die SQLiteDatenbank
 *
 * @author Kay Förster
 */
public class DatabaseManager extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "HTWDresden.db";
    public static final int DATABASE_VERSION = 1;

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        // Aktiviere Fremdschlüsselüberprüfung
        if (!db.isReadOnly())
            db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + Const.database.TimetableEntry.TABLE_NAME + " (" +
                Const.database.TimetableEntry._ID + Const.database.TYPE_INT + " PRIMARY KEY" + Const.database.COMMA_SEP +
                Const.database.TimetableEntry.COLUMN_NAME_LESSONTAG + Const.database.TYPE_TEXT + Const.database.COMMA_SEP +
                Const.database.TimetableEntry.COLUMN_NAME_NAME + Const.database.TYPE_TEXT + Const.database.COMMA_SEP +
                Const.database.TimetableEntry.COLUMN_NAME_TYP + Const.database.TYPE_TEXT + Const.database.COMMA_SEP +
                Const.database.TimetableEntry.COLUMN_NAME_WEEK + Const.database.TYPE_INT + Const.database.COMMA_SEP +
                Const.database.TimetableEntry.COLUMN_NAME_DAY + Const.database.TYPE_INT + Const.database.COMMA_SEP +
                Const.database.TimetableEntry.COLUMN_NAME_DS + Const.database.TYPE_INT + Const.database.COMMA_SEP +
                Const.database.TimetableEntry.COLUMN_NAME_PROFESSOR + Const.database.TYPE_TEXT + Const.database.COMMA_SEP +
                Const.database.TimetableEntry.COLUMN_NAME_WEEKSONLY + Const.database.TYPE_TEXT + Const.database.COMMA_SEP +
                Const.database.TimetableEntry.COLUMN_NAME_ROOMS + Const.database.TYPE_TEXT + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Const.database.TimetableEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
