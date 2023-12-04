package ru.mvlikhachev.mytablepr.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ru.mvlikhachev.mytablepr.Adapter.CartListAdapter;
import ru.mvlikhachev.mytablepr.Domain.RestoranDomain;
import ru.mvlikhachev.mytablepr.Interface.ChangeNumberItemsListener;
import ru.mvlikhachev.mytablepr.Helper.ManagementCart;
import ru.mvlikhachev.mytablepr.R;

public class CartActivity extends AppCompatActivity implements ManagementCart.CartListener {
    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerViewList;
    private ManagementCart managementCart;
    private TextView totalFeeTxt, taxTxt, deliveryTxt;
    public TextView totalTxt;
    private CartListAdapter cartListAdapter;
    private double tax;
    private ScrollView scrollView;
    private ConstraintLayout orderbtn, profileIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        managementCart = ManagementCart.getInstance(this, this);

        initView();
        initList();
        bottomNavigation();
    }

    protected void bottomNavigation() {
        // Добавьте свою логику для нижней навигации
    }

    private void initSwipeToDelete() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(cartListAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewList);
    }

    protected void initList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewList.setLayoutManager(linearLayoutManager);
        cartListAdapter = new CartListAdapter((ArrayList<RestoranDomain>) managementCart.getListCart(), this, new CartListAdapter.ChangeNumberItemsListener() {
            @Override
            public void changed() {
                calculateCart();
            }
        });

        recyclerViewList.setAdapter(cartListAdapter);
        initSwipeToDelete();

        if (managementCart.getListCart().isEmpty()) {
            scrollView.setVisibility(View.GONE);
        } else {
            scrollView.setVisibility(View.VISIBLE);
        }
    }

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
}