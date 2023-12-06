package ru.mvlikhachev.mytablepr.Activity;

import static ru.mvlikhachev.mytablepr.Activity.MainActivity.orderlist;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.mvlikhachev.mytablepr.Adapter.CartListAdapter;
import ru.mvlikhachev.mytablepr.Adapter.RestoranAdapter;
import ru.mvlikhachev.mytablepr.Domain.RestoranDomain;
import ru.mvlikhachev.mytablepr.Interface.ApiService;
import ru.mvlikhachev.mytablepr.Interface.ChangeNumberItemsListener;
import ru.mvlikhachev.mytablepr.Helper.ManagementCart;
import ru.mvlikhachev.mytablepr.R;

public class CartActivity extends AppCompatActivity implements ManagementCart.CartListener {

    private RecyclerView recyclerViewList;
    static CartListAdapter priceAdapter;
    private String token;
    private CartListAdapter cartListAdapter;
    private double tax;
    private ScrollView scrollView;
    private ConstraintLayout orderbtn, profileIcon;
    private ArrayList<RestoranDomain> orderlist = new ArrayList<>(); // Новый список для ресторанов

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        String email = getIntent().getStringExtra("email");
        System.out.println("123123123123123123123123"+email);
//        managementCart = ManagementCart.getInstance(this, this);
        executeGetRequest();
        initView();
//        initList();
        bottomNavigation();
        fetchRestaurantsFromServer();
    }
    private void executeGetRequest() {
        String token = getIntent().getStringExtra("access_token"); // Получение значения токена
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://losermaru.pythonanywhere.com/favorite";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        System.out.println("1232133123123123123123");
                        // Обработка успешного ответа от сервера
                        parseResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Обработка ошибки запроса
                        handleErrorResponse(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token); // Добавление заголовка авторизации
                return headers;
            }
        };

        queue.add(request);
    }

    private void parseResponse(JSONArray response) {
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject item = response.getJSONObject(i);
                int id = item.getInt("id");
                int userId = item.getInt("user_id");
                int restaurantId = item.getInt("restaurant_id");

            }
        } catch (JSONException e) {
            e.printStackTrace();
            // Обработка ошибки парсинга JSON
            Toast.makeText(CartActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleErrorResponse(VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            String errorResponse = new String(error.networkResponse.data);
            // Обработка текста ошибки
            Log.e("ErrorResponse", "Error: " + errorResponse);
            // Дополнительная логика для обработки текста ошибки
        } else {
            // Обработка других видов ошибок (например, отсутствие сети и т. д.)
            Toast.makeText(CartActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ErrorResponse", "Error: " + error.getMessage());
        }
    }
    protected void bottomNavigation() {
        // Добавьте свою логику для нижней навигации
    }

//    private void initSwipeToDelete() {
//        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(cartListAdapter);
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
//        itemTouchHelper.attachToRecyclerView(recyclerViewList);
//    }

//    protected void initList() {
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//        recyclerViewList.setLayoutManager(linearLayoutManager);
//        cartListAdapter = new CartListAdapter((ArrayList<RestoranDomain>) managementCart.getListCart(), this, new CartListAdapter.ChangeNumberItemsListener() {
//            @Override
//            public void changed() {
//                calculateCart();
//            }
//        });
//
//        recyclerViewList.setAdapter(cartListAdapter);
//        initSwipeToDelete();
//
//        if (managementCart.getListCart().isEmpty()) {
//            scrollView.setVisibility(View.GONE);
//        } else {
//            scrollView.setVisibility(View.VISIBLE);
//        }
//    }

    protected void calculateCart() {
        // Выполните расчет общей суммы корзины и обновите соответствующие представления
    }

    protected void initView() {
        profileIcon = findViewById(R.id.profile_icon);
        recyclerViewList = findViewById(R.id.view);
        scrollView = findViewById(R.id.scrollView);
    }

    @Override
    public void onCartUpdated() {
        // Обработка обновления корзины
        calculateCart();
    }


    private void fetchRestaurantsFromServer() {
        token = getIntent().getStringExtra("access_token");
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Request originalRequest = chain.request();
                // Создание нового запроса с добавленным заголовком авторизации
                okhttp3.Request newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();

                return chain.proceed(newRequest);
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://losermaru.pythonanywhere.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<RestoranDomain>> call = apiService.getRestaurants();
        call.enqueue(new Callback<List<RestoranDomain>>() {
            @Override
            public void onResponse(Call<List<RestoranDomain>> call, retrofit2.Response<List<RestoranDomain>> response) {
                if (response.isSuccessful()) {
                    List<RestoranDomain> restaurants = response.body();
                    if (restaurants != null) {
                        Collections.sort(restaurants, new Comparator<RestoranDomain>() {
                            @Override
                            public int compare(RestoranDomain o1, RestoranDomain o2) {
                                // Сравниваем по убыванию рейтинга
                                return Double.compare(o2.getStar(), o1.getStar());
                            }
                        });

                        // Устанавливаем данные в RecyclerView после сортировки
                        orderlist.clear();
                        orderlist.addAll(restaurants);
                        setProductRecycler(orderlist);
                        priceAdapter.notifyDataSetChanged();
                    }
                } else {
                    // Обработка ошибки
                }
            }
            private void executePostRequest() {
                String token = getIntent().getStringExtra("access_token");
                RequestQueue queue = Volley.newRequestQueue(this);
                String url = "https://losermaru.pythonanywhere.com/user_info"; // замените на ваш эндпоинт для получения информации о пользователе

                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("token", token); // передача токена для идентификации пользователя
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Обработка успешного ответа от сервера
                                parseUserInfo(response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Обработка ошибки запроса
                                handleErrorResponse(error);
                            }
                        });

                queue.add(request);
            }

            private void parseUserInfo(JSONObject response) {
                try {
                    String userName = response.getString("name");
                    String userEmail = response.getString("email");
                    // Дальнейшая обработка полученных данных о пользователе
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Обработка ошибки парсинга JSON
                    Toast.makeText(CartActivity.this, "Error parsing user info", Toast.LENGTH_SHORT).show();
                }
            }

            private void handleErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    String errorResponse = new String(error.networkResponse.data);
                    // Обработка текста ошибки
                    Log.e("ErrorResponse", "Error: " + errorResponse);
                    // Дополнительная логика для обработки текста ошибки
                } else {
                    // Обработка других видов ошибок (например, отсутствие сети и т. д.)
                    Toast.makeText(CartActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ErrorResponse", "Error: " + error.getMessage());
                }
            }

            private void setProductRecycler(ArrayList<RestoranDomain> restorans) {
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(CartActivity.this, RecyclerView.VERTICAL, false);
                String email = getIntent().getStringExtra("email");
                System.out.println("====================================" + token);
                recyclerViewList = findViewById(R.id.view);
                recyclerViewList.setLayoutManager(layoutManager);
                priceAdapter = new CartListAdapter(CartActivity.this, email, token);
                recyclerViewList.setAdapter(priceAdapter);
                recyclerViewList.smoothScrollToPosition(100000);
                recyclerViewList.setHasFixedSize(true);
                priceAdapter.updateProducts(restorans);
            }

            @Override
            public void onFailure(Call<List<RestoranDomain>> call, Throwable t) {

            }
        });
    }


}