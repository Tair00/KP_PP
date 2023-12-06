package ru.mvlikhachev.mytablepr.Adapter;


import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.behavior.SwipeDismissBehavior;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ru.mvlikhachev.mytablepr.Activity.CartActivity;
import ru.mvlikhachev.mytablepr.Activity.ShowDetailActivity;
import ru.mvlikhachev.mytablepr.Domain.RestoranDomain;
import ru.mvlikhachev.mytablepr.Helper.ManagementCart;
import ru.mvlikhachev.mytablepr.R;


public class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.CartListViewHolder> {
    private Context context;

    private ArrayList<RestoranDomain> products;
    private String email,token;
    private Integer restorantId;
    public CartListAdapter(Context context, String email,String token ) {
        this.token = token;
        this.context = context;
        this.products = new ArrayList<>();
        this.email = email;
    }

    public void updateProducts(ArrayList<RestoranDomain> newProducts) {
        products.clear();
        products.addAll(newProducts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.restoran_item, parent, false);
        return new CartListViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull CartListViewHolder holder, int position) {
        ArrayList<RestoranDomain> sortedProducts = new ArrayList<>(products);
        Collections.sort(sortedProducts, new Comparator<RestoranDomain>() {
            @Override
            public int compare(RestoranDomain o1, RestoranDomain o2) {
                // Сравниваем по убыванию рейтинга
                return Double.compare(o2.getStar(), o1.getStar());
            }
        });

        RestoranDomain product = products.get(position);


        holder.productTitles.setText(product.getName());
        holder.productPrice.setText(String.valueOf(product.getPrice()));

        Picasso.get().load(product.getPicture()).into(holder.productImage);

        holder.grade.setText(String.valueOf(product.getStar()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context,
                        new Pair<View, String>(holder.productImage, "productImage"));
                restorantId = product.getId();
                Intent intent = new Intent(holder.itemView.getContext(), ShowDetailActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("access_token",token);
                intent.putExtra("object", product);
                intent.putExtra("restorantId",restorantId);
                holder.itemView.getContext().startActivity(intent, options.toBundle());
            }
        });

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.bounce_animation);
        holder.itemView.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class CartListViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productTitles, productPrice;
        TextView grade;

        public CartListViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.pic);
            productTitles = itemView.findViewById(R.id.title);
            productPrice = itemView.findViewById(R.id.fee);
            grade = itemView.findViewById(R.id.grade);
        }
    }
}


