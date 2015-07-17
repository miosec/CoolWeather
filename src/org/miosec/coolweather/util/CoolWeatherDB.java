package org.miosec.coolweather.util;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.miosec.coolweather.db.CoolWeatherOpenHelper;
import org.miosec.coolweather.model.City;
import org.miosec.coolweather.model.County;
import org.miosec.coolweather.model.Province;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CoolWeatherDB {
	/**
	 * ���ݿ���
	 */
	public static final String DB_NAME = "cool_weather";
	/**
	 * ���ݿ�汾
	 */
	public static final String PROVINCE_TABLE_NAME = "Province";
	public static final String CITY_TABLE_NAME = "City";
	public static final String COUNTY_TABLE_NAME = "County";

	public static final int VERSION = 1;

	private static CoolWeatherDB coolWeatherDB;

	private SQLiteDatabase db;

	/**
	 * �����췽��˽�л�
	 */
	private CoolWeatherDB(Context context) {
		CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context,
				DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
	}

	/**
	 * ��ȡCoolWeatherDB��ʵ��
	 */
	public synchronized static CoolWeatherDB getInstance(Context context) {
		if (coolWeatherDB == null) {
			coolWeatherDB = new CoolWeatherDB(context);
		}
		return coolWeatherDB;
	}

	/**
	 * ��provinceʵ���洢�����ݿ���
	 */
	public void saveProvince(Province province) {
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
		}
	}

	/**
	 * �����ݿ��ȡȫ�����е�ʡ����Ϣ
	 */
	public List<Province> loadProvinces() {
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db.query(PROVINCE_TABLE_NAME, null, null, null, null,
				null, null);
		if (cursor.moveToNext()) {
			do {
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor
						.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor
						.getColumnIndex("province_code")));
				list.add(province);
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}

	/**
	 * ��Cityʵ���洢�����ݿ���
	 */
	public void saveCity(City city) {
		if (city != null) {
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert(CITY_TABLE_NAME, null, values);
		}
	}

	/**
	 * �����ݿ��ȡĳʡ�µ����е��м���Ϣ
	 */
	public List<City> loadCities(int provinceId) {
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query(CITY_TABLE_NAME, null, "province_id=?",
				new String[] { String.valueOf(provinceId) }, null, null, null,
				null);
		if (cursor.moveToNext()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor
						.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor
						.getColumnIndex("city_code")));
				city.setProvinceId(cursor.getInt(cursor
						.getColumnIndex("province_id")));
				list.add(city);
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}

	/**
	 * ��Countyʵ���洢�����ݿ���
	 */
	public void saveCounty(County county) {
		if (county != null) {
			ContentValues values = new ContentValues();
			values.put("county_name", county.getCountyName());
			values.put("county_code", county.getCountyCode());
			values.put("city_id", county.getCityId());
			db.insert(COUNTY_TABLE_NAME, null, values);
		}
	}

	/**
	 * �����ݿ��ȡĳ�����µ��ؼ���Ϣ
	 */
	public List<County> loadCounties(int cityId) {
		List<County> list = new ArrayList<County>();
		Cursor cursor = db
				.query(COUNTY_TABLE_NAME, null, "city_id=?",
						new String[] { String.valueOf(cityId) }, null, null,
						null, null);
		if (cursor.moveToNext()) {
			do {
				County county = new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setCountyName(cursor.getString(cursor
						.getColumnIndex("county_name")));
				county.setCountyCode(cursor.getString(cursor
						.getColumnIndex("county_code")));
				county.setCityId(cursor.getInt(cursor.getColumnIndex("city_id")));
				list.add(county);
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}
}
