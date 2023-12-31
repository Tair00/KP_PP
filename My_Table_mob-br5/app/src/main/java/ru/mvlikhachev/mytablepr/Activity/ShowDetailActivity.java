package ru.mvlikhachev.mytablepr.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import com.squareup.picasso.Picasso;
import ru.mvlikhachev.mytablepr.Domain.RestoranDomain;
import ru.mvlikhachev.mytablepr.Helper.ManagementCart;
import ru.mvlikhachev.mytablepr.Interface.CartListener;
import ru.mvlikhachev.mytablepr.R;



public class ShowDetailActivity extends AppCompatActivity implements CartListener {
    private TextView addToCartBtn;
    private TextView titleTxt, feeTxt, description, starTxt, tableTxt;
    private ImageView heart, restoranPic;
    private RestoranDomain object;
    private TextView numberOrderTxt;
    private ImageView plusBtn, minusBtn,star;
    private ManagementCart managementCart;
    private int numberOrder = 1;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_detail);

        initView();
        getBundle();
        CartActivity cartActivity = new CartActivity();
        managementCart = ManagementCart.getInstance(this, cartActivity);
        setupButtonListeners();

        token = getIntent().getStringExtra("access_token");
    }

    private void getBundle() {
        object = (RestoranDomain) getIntent().getSerializableExtra("object");
        if (object != null) {
            titleTxt.setText(object.getName());
            feeTxt.setText(String.valueOf(object.getPrice()));
            description.setText(object.getDescription());
            starTxt.setText(String.valueOf(object.getStar()));

            Picasso.get().load(object.getPicture()).into(restoranPic);
        } else {

        }
    }


    private void setupButtonListeners() {
        addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = feeTxt.getText().toString();
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("title", title);
                editor.apply();
                String email = getIntent().getStringExtra("email");
                Integer restaurantId = getIntent().getIntExtra("restorantId",0);
                Intent intent1 = new Intent(ShowDetailActivity.this, BookingActivity2.class);
                String  token = getIntent().getStringExtra("access_token");
                intent1.putExtra("email", email);
                intent1.putExtra("access_token",token);
                System.out.println("+++++++++++++++++2" + token);
                intent1.putExtra("restorantId", restaurantId);
                intent1.putExtra("feeTxt", title);
                intent1.putExtra("restoranPic", object.getPicture()); // Pass the restoranPic value
                startActivity(intent1);
            }
        });
        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showRatingDialog();
            }
        });
        heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                managementCart.addItem(object);
            }
        });

        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberOrder++;
                updateOrderQuantity();
            }
        });

        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberOrder > 1) {
                    numberOrder--;
                    updateOrderQuantity();
                }
            }
        });
    }

    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Оцените заведение");

        final String[] ratings = {"1", "2", "3", "4", "5"};
        builder.setItems(ratings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedRating = ratings[which];
                sendRatingToServer(selectedRating);

                Toast.makeText(ShowDetailActivity.this, "Оценка ресторана: " + selectedRating, Toast.LENGTH_SHORT).show();
                getBundle();
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void updateOrderQuantity() {
        numberOrderTxt.setText(String.valueOf(numberOrder));
        feeTxt.setText(String.valueOf(numberOrder * object.getPrice()));
    }
    private void sendRatingToServer(String selectedRating) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String email = getIntent().getStringExtra("email");
        String userUrl = "https://losermaru.pythonanywhere.com/user/" + email;

        StringRequest userRequest = new StringRequest(Request.Method.GET, userUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject userJson = new JSONObject(response);
                            int userId = userJson.getInt("id");
                            int restaurantId = getIntent().getIntExtra("restorantId", 0);

                            String ratingUrl = "https://losermaru.pythonanywhere.com/rating";

                            JSONObject jsonBody = new JSONObject();
                            jsonBody.put("rating", Integer.parseInt(selectedRating));
                            jsonBody.put("user_id", userId);
                            jsonBody.put("restaurant_id", restaurantId);

                            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ratingUrl, jsonBody,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            Toast.makeText(ShowDetailActivity.this, "Rating sent successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            String errorMessage = "Error sending rating: " + error.getMessage();
                                            Toast.makeText(ShowDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                            if (error.networkResponse != null) {
                                                int statusCode = error.networkResponse.statusCode;
                                                String responseData = new String(error.networkResponse.data);
                                                Log.e("ErrorResponse", "Status Code: " + statusCode);
                                                Log.e("ErrorResponse", "Response Data: " + responseData);
                                            }
                                        }
                                    }) {
                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    Map<String, String> headers = new HashMap<>();

                                    token = getIntent().getStringExtra("access_token");
                                    headers.put("Authorization", "Bearer " + token);
                                    return headers;
                                }
                            };

                            queue.add(request);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ShowDetailActivity.this, "Error parsing user response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = "Error: " + error.getMessage();
                        Toast.makeText(ShowDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String responseData = new String(error.networkResponse.data);
                            Log.e("ErrorResponse", "Status Code: " + statusCode);
                            Log.e("ErrorResponse", "Response Data: " + responseData);
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                token = getIntent().getStringExtra("access_token");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(userRequest);
    }


    @Override
    public void onCartUpdated() {
        Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
    }

    private void initView() {
        star=findViewById(R.id.star);
        tableTxt = findViewById(R.id.tableTxt);
        numberOrderTxt = findViewById(R.id.numberItemTxt);
        heart = findViewById(R.id.heart);
        addToCartBtn = findViewById(R.id.addToCartBtn);
        titleTxt = findViewById(R.id.titleTxt);
        feeTxt = findViewById(R.id.priceTxt);
        description = findViewById(R.id.descriptionTxt);
        restoranPic = findViewById(R.id.restoranPic);
        starTxt = findViewById(R.id.starTxt);
        plusBtn = findViewById(R.id.plusCardBtn);
        minusBtn = findViewById(R.id.minusCardBtn);
    }
}