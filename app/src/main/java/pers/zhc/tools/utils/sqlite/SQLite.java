package pers.zhc.tools.utils.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author bczhc
 */
public class SQLite {
    public static boolean checkRecordExistence(SQLiteDatabase database, String tableName, String columnName, String value) {
        final Cursor cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE " + columnName + "=?", new String[]{value});
        final boolean r = cursor.getCount() > 0;
        cursor.close();
        return r;
    }

    public static boolean checkRecordExistence(MySQLite3 database, String tableName, String columnName, String value) {
        final boolean[] r = {false};
        try {
            database.exec("SELECT * FROM " + tableName + " WHERE " + columnName + "=" + value, contents -> {
                r[0] = true;
                return 1;
            });
        } catch (Exception ignored) {
        }
        return r[0];
    }
}