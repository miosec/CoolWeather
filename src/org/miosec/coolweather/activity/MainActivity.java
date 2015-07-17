package org.miosec.coolweather.activity;

import java.util.ArrayList;
import java.util.List;

import org.miosec.coolweather.R;
import org.miosec.coolweather.model.City;
import org.miosec.coolweather.model.County;
import org.miosec.coolweather.model.Province;
import org.miosec.coolweather.util.CoolWeatherDB;
import org.miosec.coolweather.util.HttpCallbackListener;
import org.miosec.coolweather.util.HttpUtils;
import org.miosec.coolweather.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private ListView listView;// listviewչʾ����
	private TextView titlte_text;// ����
	private List<String> dataList = new ArrayList<String>();// ���鼯�϶���
	private CoolWeatherDB coolWeatherDB;// ���ݿ�ʵ������
	private int currentLevel;// ��ǰѡ�еļ���
	private int LEVEL_PROVINCE = 0;
	private int LEVEL_CITY = 1;
	private int LEVLE_COUNTY = 2;
	private Province selectedProvince;
	private City selectedCity;
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	private ArrayAdapter<String> arrayAdapter;
	private ProgressDialog progressDialog;
	private boolean isFromWeatherActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		isFromWeatherActivity = getIntent().getBooleanExtra(
				"from_weather_activity", false);

		if (sp.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			// �Ѿ�ѡ���˳����Ҳ��Ǵ�DisplayWeather��ת�����Ĳ�ֱ����ת��DisplayWeather.
			Intent intent = new Intent(this, DisplayWeather.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		listView = (android.widget.ListView) findViewById(R.id.lv);
		titlte_text = (TextView) findViewById(R.id.title_text);

		arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(arrayAdapter);

		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// ���ݵȼ��жϸ�ȥ��ѯ�ĸ��ȼ�������
				System.out.println(currentLevel);
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(position);
					queryCounties();
				} else if (currentLevel == LEVLE_COUNTY) {
					String countyCode = countyList.get(position)
							.getCountyCode();
					Intent intent = new Intent(MainActivity.this,
							DisplayWeather.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces();// ����ʡ������
	}

	/**
	 * ��ѯȫ�����е�ʡ,���ȴ����ݿ��ѯ,���û�в�ѯ����ȥ�������ϲ�ѯ
	 */
	private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			arrayAdapter.notifyDataSetChanged();
			listView.setSelection(0);
			titlte_text.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}

	protected void queryCities() {
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			arrayAdapter.notifyDataSetChanged();
			listView.setSelection(0);
			titlte_text.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}

	protected void queryCounties() {
		countyList = coolWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			arrayAdapter.notifyDataSetChanged();
			titlte_text.setText(selectedCity.getCityName());
			currentLevel = LEVLE_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}

	private void queryFromServer(final String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code
					+ ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();// ��ʾ���ؽ���
		HttpUtils.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(coolWeatherDB,
							response);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(coolWeatherDB,
							response, selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(coolWeatherDB,
							response, selectedCity.getId());
				}
				if (result) {
					// ͨ��runOnUiThread()�����ص����̴߳����߼�
					runOnUiThread(new Runnable() {
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				} else {
					runOnUiThread(new Runnable() {
						public void run() {
							closeProgressDialog();
							Toast.makeText(MainActivity.this, "����ʧ��",
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {

			}
		});
	}

	/**
	 * ��ʾ���ȶԻ���
	 */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	/**
	 * �رս��ȶԻ���
	 */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	/**
	 * ����Back����,���ݵ�ǰ�ļ������ж�,��ʱӦ�÷������б�,ʡ�б�,����ֱ���˳�
	 */
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVLE_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, DisplayWeather.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
