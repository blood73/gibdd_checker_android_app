package ru.bloodsoft.gibddchecker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import ru.bloodsoft.gibddchecker.models.Eaisto;
import ru.bloodsoft.gibddchecker.models.Fine;
import ru.bloodsoft.gibddchecker.models.Fssp;
import ru.bloodsoft.gibddchecker.models.Insurance;
import ru.bloodsoft.gibddchecker.models.Mileage;
import ru.bloodsoft.gibddchecker.models.Phone;
import ru.bloodsoft.gibddchecker.models.Plate;
import ru.bloodsoft.gibddchecker.models.Polis;
import ru.bloodsoft.gibddchecker.models.Vin;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class HistoryDatabaseHelper extends SQLiteOpenHelper {

    private static HistoryDatabaseHelper sInstance;

    //Log TAG
    private static final String TAG = makeLogTag(HistoryDatabaseHelper.class);

    // Database Info
    private static final String DATABASE_NAME = "history_database";
    private static final int DATABASE_VERSION = 8;

    // Table Names
    private static final String TABLE_VINS = "vins";
    private static final String TABLE_INSURANCES = "insurances";
    private static final String TABLE_EAISTO = "eaisto";
    private static final String TABLE_FSSP = "fssp";
    private static final String TABLE_PHONE = "phone";
    private static final String TABLE_POLIS = "polis";
    private static final String TABLE_PLATE = "plate";
    private static final String TABLE_FINES = "fines";
    private static final String TABLE_MILEAGE = "mileage";
    private static final String TABLE_MILEAGE_HISTORY = "mileage_history";

    // Vins Table Columns
    private static final String KEY_VIN_ID = "id";
    private static final String KEY_VIN_TEXT = "vin";
    private static final String KEY_VIN_TYPE = "type";

    // Insurances Table Columns
    private static final String KEY_INSURANCE_ID = "id";
    private static final String KEY_INSURANCE_TEXT = "insurance";
    private static final String KEY_INSURANCE_SERIAL = "serial";

    //Eaisto Table Columns
    private static final String KEY_EAISTO_ID = "id";
    private static final String KEY_EAISTO_VIN = "vin";
    private static final String KEY_EAISTO_BODY_NUMBER = "body_number";
    private static final String KEY_EAISTO_FRAME_NUMBER = "frame_number";
    private static final String KEY_EAISTO_REG_NUMBER = "reg_number";

    //Fssp Table Columns
    private static final String KEY_FSSP_ID = "id";
    private static final String KEY_FSSP_REGION = "region";
    private static final String KEY_FSSP_FIRSTNAME = "firstname";
    private static final String KEY_FSSP_LASTNAME = "lastname";
    private static final String KEY_FSSP_PATRONYMIC = "patronymic";
    private static final String KEY_FSSP_DOB = "dob";

    //Phone Table Columns
    private static final String KEY_PHONE_ID = "id";
    private static final String KEY_PHONE = "phone";

    //Polis Table Columns
    private static final String KEY_POLIS_ID = "id";
    private static final String KEY_POLIS_VIN = "vin";
    private static final String KEY_POLIS_BODY_NUMBER = "body_number";
    private static final String KEY_POLIS_FRAME_NUMBER = "frame_number";
    private static final String KEY_POLIS_REG_NUMBER = "reg_number";

    //Plate Table Columns
    private static final String KEY_PLATE_ID = "id";
    private static final String KEY_PLATE = "plate";

    //Fines Table Columns
    private static final String KEY_FINE_ID = "id";
    private static final String KEY_FINE_REGNUMBER = "regnumber";
    private static final String KEY_FINE_STS = "sts";

    //Mileage Table Columns
    private static final String KEY_MILEAGE_ID = "id";
    private static final String KEY_MILEAGE_NUMBER = "mileage";
    private static final int MILEAGE_KEY = 1;

    //Mileage history Table Columns
    private static final String KEY_MILEAGE_H_ID = "id";
    private static final String KEY_MILEAGE_H_VIN = "vin";
    private static final String KEY_MILEAGE_H_MILEAGE = "mileage";
    private static final String KEY_MILEAGE_H_DATE = "date";

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_VINS_TABLE = "CREATE TABLE " + TABLE_VINS +
                "(" +
                KEY_VIN_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_VIN_TEXT + " TEXT," +
                KEY_VIN_TYPE + " TEXT" +
                ")";

        String CREATE_INSURANCES_TABLE = "CREATE TABLE " + TABLE_INSURANCES +
                "(" +
                KEY_INSURANCE_ID + " INTEGER PRIMARY KEY," +
                KEY_INSURANCE_TEXT + " TEXT," +
                KEY_INSURANCE_SERIAL + " TEXT" +
                ")";

        String CREATE_EAISTO_TABLE = "CREATE TABLE " + TABLE_EAISTO +
                "(" +
                KEY_EAISTO_ID + " INTEGER PRIMARY KEY," +
                KEY_EAISTO_VIN + " TEXT," +
                KEY_EAISTO_BODY_NUMBER + " TEXT," +
                KEY_EAISTO_FRAME_NUMBER + " TEXT," +
                KEY_EAISTO_REG_NUMBER + " TEXT"+
                ")";

        String CREATE_FSSP_TABLE = "CREATE TABLE " + TABLE_FSSP +
                "(" +
                KEY_FSSP_ID + " INTEGER PRIMARY KEY," +
                KEY_FSSP_REGION + " TEXT," +
                KEY_FSSP_FIRSTNAME + " TEXT," +
                KEY_FSSP_LASTNAME + " TEXT," +
                KEY_FSSP_PATRONYMIC + " TEXT," +
                KEY_FSSP_DOB + " TEXT" +
                ")";

        String CREATE_PHONE_TABLE = "CREATE TABLE " + TABLE_PHONE +
                "(" +
                KEY_PHONE_ID + " INTEGER PRIMARY KEY," +
                KEY_PHONE + " TEXT" +
                ")";

        String CREATE_POLIS_TABLE = "CREATE TABLE " + TABLE_POLIS +
                "(" +
                KEY_POLIS_ID + " INTEGER PRIMARY KEY," +
                KEY_POLIS_VIN + " TEXT," +
                KEY_POLIS_BODY_NUMBER + " TEXT," +
                KEY_POLIS_FRAME_NUMBER + " TEXT," +
                KEY_POLIS_REG_NUMBER + " TEXT"+
                ")";

        String CREATE_PLATE_TABLE = "CREATE TABLE " + TABLE_PLATE +
                "(" +
                KEY_PLATE_ID + " INTEGER PRIMARY KEY," +
                KEY_PLATE + " TEXT" +
                ")";

        String CREATE_FINES_TABLE = "CREATE TABLE " + TABLE_FINES +
                "(" +
                KEY_FINE_ID + " INTEGER PRIMARY KEY," +
                KEY_FINE_REGNUMBER + " TEXT," +
                KEY_FINE_STS + " TEXT" +
                ")";

        String CREATE_MILEAGE_TABLE = "CREATE TABLE " + TABLE_MILEAGE +
                "(" +
                KEY_MILEAGE_ID + " INTEGER PRIMARY KEY," +
                KEY_MILEAGE_NUMBER + " INTEGER" +
                ")";

        String CREATE_MILEAGE_H_TABLE = "CREATE TABLE " + TABLE_MILEAGE_HISTORY +
                "(" +
                KEY_MILEAGE_H_ID + " INTEGER PRIMARY KEY," +
                KEY_MILEAGE_H_VIN + " TEXT," +
                KEY_MILEAGE_H_MILEAGE + " TEXT," +
                KEY_MILEAGE_H_DATE + " TEXT" +
                ")";

        db.execSQL(CREATE_VINS_TABLE);
        db.execSQL(CREATE_INSURANCES_TABLE);
        db.execSQL(CREATE_EAISTO_TABLE);
        db.execSQL(CREATE_FSSP_TABLE);
        db.execSQL(CREATE_PHONE_TABLE);
        db.execSQL(CREATE_POLIS_TABLE);
        db.execSQL(CREATE_PLATE_TABLE);
        db.execSQL(CREATE_FINES_TABLE);
        db.execSQL(CREATE_MILEAGE_TABLE);
        db.execSQL(CREATE_MILEAGE_H_TABLE);

    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            String CREATE_PHONE_TABLE = "CREATE TABLE " + TABLE_PHONE +
                    "(" +
                    KEY_PHONE_ID + " INTEGER PRIMARY KEY," +
                    KEY_PHONE + " TEXT" +
                    ")";

            String CREATE_POLIS_TABLE = "CREATE TABLE " + TABLE_POLIS +
                    "(" +
                    KEY_POLIS_ID + " INTEGER PRIMARY KEY," +
                    KEY_POLIS_VIN + " TEXT," +
                    KEY_POLIS_BODY_NUMBER + " TEXT," +
                    KEY_POLIS_FRAME_NUMBER + " TEXT," +
                    KEY_POLIS_REG_NUMBER + " TEXT"+
                    ")";

            String CREATE_PLATE_TABLE = "CREATE TABLE " + TABLE_PLATE +
                    "(" +
                    KEY_PLATE_ID + " INTEGER PRIMARY KEY," +
                    KEY_PLATE + " TEXT" +
                    ")";

            String CREATE_FINES_TABLE = "CREATE TABLE " + TABLE_FINES +
                    "(" +
                    KEY_FINE_ID + " INTEGER PRIMARY KEY," +
                    KEY_FINE_REGNUMBER + " TEXT," +
                    KEY_FINE_STS + " TEXT" +
                    ")";

            String CREATE_MILEAGE_TABLE = "CREATE TABLE " + TABLE_MILEAGE +
                    "(" +
                    KEY_MILEAGE_ID + " INTEGER PRIMARY KEY," +
                    KEY_MILEAGE_NUMBER + " INTEGER" +
                    ")";

            String CREATE_MILEAGE_H_TABLE = "CREATE TABLE " + TABLE_MILEAGE_HISTORY +
                    "(" +
                    KEY_MILEAGE_H_ID + " INTEGER PRIMARY KEY," +
                    KEY_MILEAGE_H_VIN + " TEXT," +
                    KEY_MILEAGE_H_MILEAGE + " TEXT," +
                    KEY_MILEAGE_H_DATE + " TEXT" +
                    ")";

            if (newVersion == 2) {
                db.execSQL(CREATE_PHONE_TABLE);
            }

            if (newVersion == 3) {
                db.execSQL(CREATE_POLIS_TABLE);
            }

            if (newVersion == 4) {
                db.execSQL(CREATE_PLATE_TABLE);
            }

            if (newVersion == 5) {
                db.execSQL(CREATE_FINES_TABLE);
            }

            if (newVersion == 6) {
                db.execSQL(CREATE_MILEAGE_TABLE);
            }

            if (newVersion == 7) {
                db.execSQL(CREATE_MILEAGE_H_TABLE);
            }

            if (newVersion == 8) {
                if (!isTableExists(db, TABLE_MILEAGE)) {
                    db.execSQL(CREATE_MILEAGE_TABLE);
                }

                if (!isTableExists(db, TABLE_MILEAGE_HISTORY)) {
                    db.execSQL(CREATE_MILEAGE_H_TABLE);
                }

                if (!isTableExists(db, TABLE_FINES)) {
                    db.execSQL(CREATE_FINES_TABLE);
                }

                if (!isTableExists(db, TABLE_PLATE)) {
                    db.execSQL(CREATE_PLATE_TABLE);
                }

                if (!isTableExists(db, TABLE_POLIS)) {
                    db.execSQL(CREATE_POLIS_TABLE);
                }

                if (!isTableExists(db, TABLE_PHONE)) {
                    db.execSQL(CREATE_PHONE_TABLE);
                }
            }
        }
    }

    public static synchronized HistoryDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new HistoryDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     *
     * In any activity just pass the context and use the singleton method
     * HistoryDatabaseHelper helper = HistoryDatabaseHelper.getInstance(this);
     */
    private HistoryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private boolean isTableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"
                + tableName + "'", null);
        int count = cursor.getCount();

        return count > 0;
    }

    // Insert a vin to the database
    public long addVin(Vin vin) {
        // The database connection is cached so it's not expensive to call getWritableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long vinId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_VIN_TEXT, vin.vinText.toUpperCase());
            if (vin.vinType.isEmpty() || vin.vinType == null) {
                values.put(KEY_VIN_TYPE, "history");
            } else {
                values.put(KEY_VIN_TYPE, vin.vinType);
            }

            String vinsSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ?",
                    KEY_VIN_ID, TABLE_VINS, KEY_VIN_TEXT, KEY_VIN_TYPE);

            Cursor cursor = db.rawQuery(vinsSelectQuery,
                    new String[]{String.valueOf(vin.vinText.toUpperCase()), String.valueOf(vin.vinType)});

            try {
                if (cursor.moveToFirst()) {
                    // vin already exist, return vinId
                    vinId = cursor.getInt(0);
                    db.setTransactionSuccessful();
                } else {
                    // vin did not already exist, so insert new vin
                    vinId = db.insertOrThrow(TABLE_VINS, null, values);
                    db.setTransactionSuccessful();
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

        } catch (Exception e) {
            logD(TAG, "Error while trying to add or update vin");
        } finally {
            db.endTransaction();
        }

        return vinId;
    }

    // Insert a insurance to the database
    public long addInsurance(Insurance insurance) {
        // The database connection is cached so it's not expensive to call getWritableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long insuranceId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_INSURANCE_TEXT, insurance.insuranceText);
            values.put(KEY_INSURANCE_SERIAL, insurance.insuranceSerial);

            String vinsSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ?",
                    KEY_INSURANCE_ID, TABLE_INSURANCES, KEY_INSURANCE_TEXT, KEY_INSURANCE_SERIAL);

            Cursor cursor = db.rawQuery(vinsSelectQuery,
                    new String[]{String.valueOf(insurance.insuranceText.toUpperCase()), String.valueOf(insurance.insuranceSerial)});

            try {
                if (cursor.moveToFirst()) {
                    // insurance already exist, return insuranceId
                    insuranceId = cursor.getInt(0);
                    db.setTransactionSuccessful();
                } else {
                    // insuranceId did not already exist, so insert new insuranceId
                    insuranceId = db.insertOrThrow(TABLE_INSURANCES, null, values);
                    db.setTransactionSuccessful();
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

        } catch (Exception e) {
            logD(TAG, "Error while trying to add or update insurance");
        } finally {
            db.endTransaction();
        }

        return insuranceId;
    }

    // Insert an eaisto to the database
    public long addEaisto(Eaisto eaisto) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long eaistoId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_EAISTO_VIN, eaisto.vin.toUpperCase());
            values.put(KEY_EAISTO_BODY_NUMBER, eaisto.bodyNumber.toUpperCase());
            values.put(KEY_EAISTO_FRAME_NUMBER, eaisto.frameNumber.toUpperCase());
            values.put(KEY_EAISTO_REG_NUMBER, eaisto.regNumber.toUpperCase());

            String eaistoSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ? " +
                            "AND %s = ? AND %s = ?",
                    KEY_EAISTO_ID, TABLE_EAISTO, KEY_EAISTO_VIN, KEY_EAISTO_BODY_NUMBER,
                    KEY_EAISTO_FRAME_NUMBER, KEY_EAISTO_REG_NUMBER);

            Cursor cursor = db.rawQuery(eaistoSelectQuery,
                    new String[]{String.valueOf(eaisto.vin.toUpperCase()), String.valueOf(eaisto.bodyNumber.toUpperCase()),
                            String.valueOf(eaisto.frameNumber.toUpperCase()), String.valueOf(eaisto.regNumber.toUpperCase())});

            try {
                if (cursor.moveToFirst()) {
                    // eaisto already exist, return eaistoId
                    eaistoId = cursor.getInt(0);
                    db.setTransactionSuccessful();
                } else {
                    // eaistoId did not already exist, so insert new eaistoId
                    eaistoId = db.insertOrThrow(TABLE_EAISTO, null, values);
                    db.setTransactionSuccessful();
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

        } catch (Exception e) {
            logD(TAG, "Error while trying to add or update eaisto");
        } finally {
            db.endTransaction();
        }

        return eaistoId;
    }

    // Insert a fssp to the database
    public long addFssp(Fssp fssp) {
        // The database connection is cached so it's not expensive to call getWritableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long fsspId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_FSSP_FIRSTNAME, fssp.firstname);
            values.put(KEY_FSSP_LASTNAME, fssp.lastname);
            values.put(KEY_FSSP_PATRONYMIC, fssp.patronymic);
            values.put(KEY_FSSP_REGION, fssp.region);
            values.put(KEY_FSSP_DOB, fssp.dob);

            String fsspSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ? " +
                            "AND %s = ? AND %s = ? AND %s = ?",
                    KEY_FSSP_ID, TABLE_FSSP, KEY_FSSP_FIRSTNAME, KEY_FSSP_LASTNAME,
                    KEY_FSSP_PATRONYMIC, KEY_FSSP_REGION, KEY_FSSP_DOB);

            Cursor cursor = db.rawQuery(fsspSelectQuery,
                    new String[]{String.valueOf(fssp.firstname), String.valueOf(fssp.lastname),
                            String.valueOf(fssp.patronymic), String.valueOf(fssp.region),
                            String.valueOf(fssp.dob)});

            try {
                if (cursor.moveToFirst()) {
                    // fssp already exist, return fsspId
                    fsspId = cursor.getInt(0);
                    db.setTransactionSuccessful();
                } else {
                    // fsspId did not already exist, so insert new fsspId
                    fsspId = db.insertOrThrow(TABLE_FSSP, null, values);
                    db.setTransactionSuccessful();
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

        } catch (Exception e) {
            logD(TAG, "Error while trying to add or update fssp");
        } finally {
            db.endTransaction();
        }

        return fsspId;
    }

    // Insert a phone to the database
    public long addPhone(Phone phone) {
        // The database connection is cached so it's not expensive to call getWritableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long phoneId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_PHONE, phone.phoneNumber);

            String phoneSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                    KEY_PHONE_ID, TABLE_PHONE, KEY_PHONE);

            Cursor cursor = db.rawQuery(phoneSelectQuery,
                    new String[]{String.valueOf(phone.phoneNumber)});

            try {
                if (cursor.moveToFirst()) {
                    // phone already exist, return phoneId
                    phoneId = cursor.getInt(0);
                    db.setTransactionSuccessful();
                } else {
                    // phone did not already exist, so insert new phone
                    phoneId = db.insertOrThrow(TABLE_PHONE, null, values);
                    db.setTransactionSuccessful();
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

        } catch (Exception e) {
            logD(TAG, "Error while trying to add or update phone");
        } finally {
            db.endTransaction();
        }

        return phoneId;
    }

    // Insert a polis to the database
    public long addPolis(Polis polis) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long polisId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_POLIS_VIN, polis.vin.toUpperCase());
            values.put(KEY_POLIS_BODY_NUMBER, polis.bodyNumber.toUpperCase());
            values.put(KEY_POLIS_FRAME_NUMBER, polis.frameNumber.toUpperCase());
            values.put(KEY_POLIS_REG_NUMBER, polis.regNumber.toUpperCase());

            String polisSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ? " +
                            "AND %s = ? AND %s = ?",
                    KEY_POLIS_ID, TABLE_POLIS, KEY_POLIS_VIN, KEY_POLIS_BODY_NUMBER,
                    KEY_POLIS_FRAME_NUMBER, KEY_POLIS_REG_NUMBER);

            Cursor cursor = db.rawQuery(polisSelectQuery,
                    new String[]{String.valueOf(polis.vin.toUpperCase()), String.valueOf(polis.bodyNumber.toUpperCase()),
                            String.valueOf(polis.frameNumber.toUpperCase()), String.valueOf(polis.regNumber.toUpperCase())});

            try {
                if (cursor.moveToFirst()) {
                    // polis already exist, return polisId
                    polisId = cursor.getInt(0);
                    db.setTransactionSuccessful();
                } else {
                    // polisId did not already exist, so insert new polisId
                    polisId = db.insertOrThrow(TABLE_POLIS, null, values);
                    db.setTransactionSuccessful();
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

        } catch (Exception e) {
            logD(TAG, "Error while trying to add or update polis");
        } finally {
            db.endTransaction();
        }

        return polisId;
    }

    // Insert a plate to the database
    public long addPlate(Plate plate) {
        // The database connection is cached so it's not expensive to call getWritableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long plateId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_PLATE, plate.plateNumber);

            String plateSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                    KEY_PLATE_ID, TABLE_PLATE, KEY_PLATE);

            Cursor cursor = db.rawQuery(plateSelectQuery,
                    new String[]{String.valueOf(plate.plateNumber)});

            try {
                if (cursor.moveToFirst()) {
                    // plate already exist, return plateId
                    plateId = cursor.getInt(0);
                    db.setTransactionSuccessful();
                } else {
                    // plate did not already exist, so insert new plate
                    plateId = db.insertOrThrow(TABLE_PLATE, null, values);
                    db.setTransactionSuccessful();
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

        } catch (Exception e) {
            logD(TAG, "Error while trying to add or update plate");
        } finally {
            db.endTransaction();
        }

        return plateId;
    }

    // Insert a fines to the database
    public long addFine(Fine fine) {
        // The database connection is cached so it's not expensive to call getWritableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long fineId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_FINE_REGNUMBER, fine.regNumber.toUpperCase());
            values.put(KEY_FINE_STS, fine.stsNumber.toUpperCase());

            String finesSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ?",
                    KEY_FINE_ID, TABLE_FINES, KEY_FINE_REGNUMBER, KEY_FINE_STS);

            Cursor cursor = db.rawQuery(finesSelectQuery,
                    new String[]{String.valueOf(fine.regNumber.toUpperCase()), String.valueOf(fine.stsNumber.toUpperCase())});

            try {
                if (cursor.moveToFirst()) {
                    // fine already exist, return fineId
                    fineId = cursor.getInt(0);
                    db.setTransactionSuccessful();
                } else {
                    // fineId did not already exist, so insert new fineId
                    fineId = db.insertOrThrow(TABLE_FINES, null, values);
                    db.setTransactionSuccessful();
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

        } catch (Exception e) {
            logD(TAG, "Error while trying to add or update fines");
        } finally {
            db.endTransaction();
        }

        return fineId;
    }

    // Insert a mileage to the database
    public long addMileage(Mileage mileage) {
        // The database connection is cached so it's not expensive to call getWritableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long mileageId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_MILEAGE_H_VIN, mileage.vin.toUpperCase());
            values.put(KEY_MILEAGE_H_MILEAGE, mileage.mileage);
            values.put(KEY_MILEAGE_H_DATE, mileage.date);

            String vinsSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                    KEY_MILEAGE_H_ID, TABLE_MILEAGE_HISTORY, KEY_MILEAGE_H_VIN);

            Cursor cursor = db.rawQuery(vinsSelectQuery,
                    new String[]{String.valueOf(mileage.vin.toUpperCase())});

            try {
                if (cursor.moveToFirst()) {
                    // mileage already exist, return mileageId
                    mileageId = cursor.getInt(0);
                    db.setTransactionSuccessful();
                } else {
                    // mileage did not already exist, so insert new mileage
                    mileageId = db.insertOrThrow(TABLE_MILEAGE_HISTORY, null, values);
                    db.setTransactionSuccessful();
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

        } catch (Exception e) {
            logD(TAG, "Error while trying to add or update mileage");
        } finally {
            db.endTransaction();
        }

        return mileageId;
    }

    public boolean isMileageExists(String vin) {
        boolean result = false;
        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        try {
            String vinsSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                    KEY_MILEAGE_H_ID, TABLE_MILEAGE_HISTORY, KEY_MILEAGE_H_VIN);

            Cursor cursor = db.rawQuery(vinsSelectQuery,
                    new String[]{String.valueOf(vin.toUpperCase())});

            try {
                if (cursor.moveToFirst()) {
                    // mileage already exist
                    result = true;
                    db.setTransactionSuccessful();
                } else {
                    // mileage did not already exist
                    result = false;
                    db.setTransactionSuccessful();
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

        } catch (Exception e) {
            logD(TAG, "Error while trying to search mileage");
        } finally {
            db.endTransaction();
        }

        return result;
    }

    public List<Vin> getAllVins() {
        List<Vin> vins = new ArrayList<>();

        // SELECT * FROM vins
        String VINS_SELECT_QUERY = String.format("SELECT * FROM %s ORDER BY %s DESC", TABLE_VINS, KEY_VIN_ID);

        // "getReadableDatabase()" and "getWritableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(VINS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Vin newVin = new Vin();
                    newVin.vinText = cursor.getString(cursor.getColumnIndex(KEY_VIN_TEXT));
                    newVin.vinType = cursor.getString(cursor.getColumnIndex(KEY_VIN_TYPE));
                    vins.add(newVin);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            logD(TAG, "Error while trying to get vins from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return vins;
    }

    public List<Insurance> getAllInsurances() {
        List<Insurance> insurances = new ArrayList<>();

        // SELECT * FROM insurances
        String INSURANCES_SELECT_QUERY = String.format("SELECT * FROM %s ORDER BY %s DESC", TABLE_INSURANCES, KEY_INSURANCE_ID);

        // "getReadableDatabase()" and "getWritableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(INSURANCES_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Insurance newInsurance = new Insurance();
                    newInsurance.insuranceSerial = cursor.getString(cursor.getColumnIndex(KEY_INSURANCE_SERIAL));
                    newInsurance.insuranceText = cursor.getString(cursor.getColumnIndex(KEY_INSURANCE_TEXT));
                    insurances.add(newInsurance);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            logD(TAG, "Error while trying to get insurances from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return insurances;
    }

    public List<Eaisto> getAllEaisto() {
        List<Eaisto> eaisto = new ArrayList<>();

        // SELECT * FROM eaisto
        String EAISTO_SELECT_QUERY = String.format("SELECT * FROM %s ORDER BY %s DESC", TABLE_EAISTO, KEY_EAISTO_ID);

        // "getReadableDatabase()" and "getWritableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(EAISTO_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Eaisto newEaisto = new Eaisto();
                    newEaisto.vin = cursor.getString(cursor.getColumnIndex(KEY_EAISTO_VIN));
                    newEaisto.bodyNumber = cursor.getString(cursor.getColumnIndex(KEY_EAISTO_BODY_NUMBER));
                    newEaisto.frameNumber = cursor.getString(cursor.getColumnIndex(KEY_EAISTO_FRAME_NUMBER));
                    newEaisto.regNumber = cursor.getString(cursor.getColumnIndex(KEY_EAISTO_REG_NUMBER));

                    eaisto.add(newEaisto);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            logD(TAG, "Error while trying to get eaisto from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return eaisto;
    }

    public List<Fssp> getAllFssp() {
        List<Fssp> fssp = new ArrayList<>();

        // SELECT * FROM fssp
        String FSSP_SELECT_QUERY = String.format("SELECT * FROM %s ORDER BY %s DESC", TABLE_FSSP, KEY_FSSP_ID);

        // "getReadableDatabase()" and "getWritableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(FSSP_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Fssp newFssp = new Fssp();
                    newFssp.firstname = cursor.getString(cursor.getColumnIndex(KEY_FSSP_FIRSTNAME));
                    newFssp.lastname = cursor.getString(cursor.getColumnIndex(KEY_FSSP_LASTNAME));
                    newFssp.patronymic = cursor.getString(cursor.getColumnIndex(KEY_FSSP_PATRONYMIC));
                    newFssp.region = cursor.getString(cursor.getColumnIndex(KEY_FSSP_REGION));
                    newFssp.dob = cursor.getString(cursor.getColumnIndex(KEY_FSSP_DOB));
                    fssp.add(newFssp);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            logD(TAG, "Error while trying to get fssp from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return fssp;
    }

    public List<Phone> getAllPhones() {
        List<Phone> phones = new ArrayList<>();

        // SELECT * FROM phone
        String PHONES_SELECT_QUERY = String.format("SELECT * FROM %s ORDER BY %s DESC", TABLE_PHONE, KEY_PHONE_ID);

        // "getReadableDatabase()" and "getWritableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(PHONES_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Phone newPhone = new Phone();
                    newPhone.phoneNumber = cursor.getString(cursor.getColumnIndex(KEY_PHONE));
                    phones.add(newPhone);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            logD(TAG, "Error while trying to get phones from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return phones;
    }

    public List<Polis> getAllPolises() {
        List<Polis> polises = new ArrayList<>();

        // SELECT * FROM polis
        String POLIS_SELECT_QUERY = String.format("SELECT * FROM %s ORDER BY %s DESC", TABLE_POLIS, KEY_POLIS_ID);

        // "getReadableDatabase()" and "getWritableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(POLIS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Polis newPolis = new Polis();
                    newPolis.vin = cursor.getString(cursor.getColumnIndex(KEY_POLIS_VIN));
                    newPolis.bodyNumber = cursor.getString(cursor.getColumnIndex(KEY_POLIS_BODY_NUMBER));
                    newPolis.frameNumber = cursor.getString(cursor.getColumnIndex(KEY_POLIS_FRAME_NUMBER));
                    newPolis.regNumber = cursor.getString(cursor.getColumnIndex(KEY_POLIS_REG_NUMBER));

                    polises.add(newPolis);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            logD(TAG, "Error while trying to get polices from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return polises;
    }

    public List<Plate> getAllPlates() {
        List<Plate> plates = new ArrayList<>();

        // SELECT * FROM plate
        String PLATES_SELECT_QUERY = String.format("SELECT * FROM %s ORDER BY %s DESC", TABLE_PLATE, KEY_PLATE_ID);

        // "getReadableDatabase()" and "getWritableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(PLATES_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Plate newPlate = new Plate();
                    newPlate.plateNumber = cursor.getString(cursor.getColumnIndex(KEY_PLATE));
                    plates.add(newPlate);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            logD(TAG, "Error while trying to get plates from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return plates;
    }

    public List<Fine> getAllFines() {
        List<Fine> fines = new ArrayList<>();

        // SELECT * FROM fines
        String FINES_SELECT_QUERY = String.format("SELECT * FROM %s ORDER BY %s DESC", TABLE_FINES, KEY_FINE_ID);

        // "getReadableDatabase()" and "getWritableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(FINES_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Fine newFine = new Fine();
                    newFine.regNumber = cursor.getString(cursor.getColumnIndex(KEY_FINE_REGNUMBER));
                    newFine.stsNumber = cursor.getString(cursor.getColumnIndex(KEY_FINE_STS));
                    fines.add(newFine);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            logD(TAG, "Error while trying to get fines from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return fines;
    }

    public List<Mileage> getAllMileages() {
        List<Mileage> mileages = new ArrayList<>();

        // SELECT * FROM vins
        String MILEAGE_SELECT_QUERY = String.format("SELECT * FROM %s ORDER BY %s DESC", TABLE_MILEAGE_HISTORY, KEY_MILEAGE_H_ID);

        // "getReadableDatabase()" and "getWritableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(MILEAGE_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Mileage newMileage = new Mileage();
                    newMileage.vin = cursor.getString(cursor.getColumnIndex(KEY_MILEAGE_H_VIN));
                    newMileage.mileage = cursor.getString(cursor.getColumnIndex(KEY_MILEAGE_H_MILEAGE));
                    newMileage.date = cursor.getString(cursor.getColumnIndex(KEY_MILEAGE_H_DATE));
                    mileages.add(newMileage);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            logD(TAG, "Error while trying to get mileages from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return mileages;
    }

    public void deleteAllHistory() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_VINS, null, null);
            db.delete(TABLE_INSURANCES, null, null);
            db.delete(TABLE_EAISTO, null, null);
            db.delete(TABLE_FSSP, null, null);
            db.delete(TABLE_PHONE, null, null);
            db.delete(TABLE_POLIS, null, null);
            db.delete(TABLE_PLATE, null, null);
            db.delete(TABLE_FINES, null, null);
            db.delete(TABLE_MILEAGE_HISTORY, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            logD(TAG, "Error while trying to delete all history");
        } finally {
            db.endTransaction();
        }
    }

    public void deleteVin(String vin, String vinType) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_VINS, KEY_VIN_TEXT + " = ? AND " + KEY_VIN_TYPE + " = ?", new String[]{vin, vinType});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            logD(TAG, "Error while trying to delete vin");
        } finally {
            db.endTransaction();
        }
    }

    public void deleteInsurance(String insurance, String serial) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_INSURANCES, KEY_INSURANCE_TEXT + " = ? AND " + KEY_INSURANCE_SERIAL + " = ?", new String[]{insurance, serial});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            logD(TAG, "Error while trying to delete insurance");
        } finally {
            db.endTransaction();
        }
    }

    public void deleteEaisto(String vin, String bodyNumber, String frameNumber, String regNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_EAISTO, KEY_EAISTO_VIN + " = ? AND " + KEY_EAISTO_BODY_NUMBER + " = ? AND "
                    + KEY_EAISTO_FRAME_NUMBER + " = ? AND " + KEY_EAISTO_REG_NUMBER + " =?", new String[]{vin, bodyNumber, frameNumber, regNumber});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            logD(TAG, "Error while trying to delete eaisto");
        } finally {
            db.endTransaction();
        }
    }

    public void deleteFssp(String region, String firstname, String lastname, String patronymic,
                           String dob) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_FSSP, KEY_FSSP_REGION + " = ? AND " + KEY_FSSP_FIRSTNAME + " = ? AND "
                            + KEY_FSSP_LASTNAME + " = ? AND "  + KEY_FSSP_PATRONYMIC + " = ? AND "
                            + KEY_FSSP_DOB + " = ?",
                    new String[]{region, firstname, lastname, patronymic, dob});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            logD(TAG, "Error while trying to delete fssp");
        } finally {
            db.endTransaction();
        }
    }

    public void deletePhone(String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_PHONE, KEY_PHONE + " = ?", new String[]{phone});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            logD(TAG, "Error while trying to delete phone");
        } finally {
            db.endTransaction();
        }
    }

    public void deletePolis(String vin, String bodyNumber, String frameNumber, String regNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_POLIS, KEY_POLIS_VIN + " = ? AND " + KEY_POLIS_BODY_NUMBER + " = ? AND "
                    + KEY_POLIS_FRAME_NUMBER + " = ? AND " + KEY_POLIS_REG_NUMBER + " =?", new String[]{vin, bodyNumber, frameNumber, regNumber});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            logD(TAG, "Error while trying to delete polis");
        } finally {
            db.endTransaction();
        }
    }

    public void deletePlate(String plate) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_PLATE, KEY_PLATE + " = ?", new String[]{plate});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            logD(TAG, "Error while trying to delete plate");
        } finally {
            db.endTransaction();
        }
    }

    public void deleteFine(String regNumber, String stsNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_FINES, KEY_FINE_REGNUMBER + " = ? AND " + KEY_FINE_STS + " = ?", new String[]{regNumber, stsNumber});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            logD(TAG, "Error while trying to delete fines");
        } finally {
            db.endTransaction();
        }
    }

    public void deleteMileage(String vin) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_MILEAGE_HISTORY, KEY_MILEAGE_H_VIN + " = ?", new String[]{vin});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            logD(TAG, "Error while trying to delete mileage");
        } finally {
            db.endTransaction();
        }
    }

    public int getMileageCount() {
        int mileageCount = 0;
        // SELECT FROM mileage
        String MILEAGE_SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = 1 LIMIT 0,1", TABLE_MILEAGE, KEY_MILEAGE_ID);

        // "getReadableDatabase()" and "getWritableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(MILEAGE_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    mileageCount = cursor.getInt(cursor.getColumnIndex(KEY_MILEAGE_NUMBER));
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            logD(TAG, "Error while trying to get mileage from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return mileageCount;
    }

    public void increaseMileageCount(int increaseNumber) {
        int currentNumber = this.getMileageCount();
        int newMileage = currentNumber + increaseNumber;

        // The database connection is cached so it's not expensive to call getWritableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_MILEAGE_ID, MILEAGE_KEY);
            values.put(KEY_MILEAGE_NUMBER, newMileage);

            db.replaceOrThrow(TABLE_MILEAGE, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            logD(TAG, "Error while trying to encrease mileage");
        } finally {
            db.endTransaction();
        }
    }

    public void updateMileageCount(int mileageNumber) {

        // The database connection is cached so it's not expensive to call getWritableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_MILEAGE_ID, MILEAGE_KEY);
            values.put(KEY_MILEAGE_NUMBER, mileageNumber);

            db.replaceOrThrow(TABLE_MILEAGE, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            logD(TAG, "Error while trying to update mileage");
        } finally {
            db.endTransaction();
        }
    }

    public void resetMileageCount() {
        int newMileage = 0;

        // The database connection is cached so it's not expensive to call getWritableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_MILEAGE_ID, MILEAGE_KEY);
            values.put(KEY_MILEAGE_NUMBER, newMileage);

            db.replaceOrThrow(TABLE_MILEAGE, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            logD(TAG, "Error while trying to delete mileage");
        } finally {
            db.endTransaction();
        }
    }

    public void decreaseMileageCount(int increaseNumber) {
        int currentNumber = this.getMileageCount();
        int newMileage = currentNumber - increaseNumber;

        // The database connection is cached so it's not expensive to call getWritableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_MILEAGE_ID, MILEAGE_KEY);
            values.put(KEY_MILEAGE_NUMBER, newMileage);

            db.replaceOrThrow(TABLE_MILEAGE, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            logD(TAG, "Error while trying to decrease mileage");
        } finally {
            db.endTransaction();
        }
    }
}