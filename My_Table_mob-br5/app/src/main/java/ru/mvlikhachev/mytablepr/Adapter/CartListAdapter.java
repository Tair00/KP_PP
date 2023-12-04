package ru.mvlikhachev.mytablepr.Adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.behavior.SwipeDismissBehavior;
import java.util.ArrayList;
import ru.mvlikhachev.mytablepr.Activity.CartActivity;
import ru.mvlikhachev.mytablepr.Domain.RestoranDomain;
import ru.mvlikhachev.mytablepr.Helper.ManagementCart;
import ru.mvlikhachev.mytablepr.R;

public class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.ViewHolder> {
    private ArrayList<RestoranDomain> listRestSelected;
    private ManagementCart managementCart;
    private ChangeNumberItemsListener changeNumberItemsListener;

    public CartListAdapter(ArrayList<RestoranDomain> listRestSelected, CartActivity activity,
                           ChangeNumberItemsListener changeNumberItemsListener) {
        this.listRestSelected = listRestSelected;
        managementCart = ManagementCart.getInstance(activity, activity);
        this.changeNumberItemsListener = changeNumberItemsListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.restoran_item, parent, false);
        return new ViewHolder(inflate);
    }

    public void updateCartList(ArrayList<RestoranDomain> list) {
        this.listRestSelected = list;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(listRestSelected.get(position).getName());
        holder.feeEachItem.setText(String.valueOf(listRestSelected.get(position).getPrice()));
        holder.grade.setText(String.valueOf(listRestSelected.get(position).getStar()));

        String imageUrl = listRestSelected.get(position).getPicture();
        Glide.with(holder.itemView.getContext()).load(imageUrl).into(holder.pic);

        SwipeDismissBehavior<View> swipe = new SwipeDismissBehavior<>();
        swipe.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY);
        swipe.setListener(new SwipeDismissBehavior.OnDismissListener() {
            @Override
            public void onDismiss(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    RestoranDomain removedItem = listRestSelected.get(adapterPosition);
                    managementCart.removeItem(listRestSelected, adapterPosition);
                    listRestSelected.remove(adapterPosition);
                    notifyDataSetChanged();
                    changeNumberItemsListener.changed();
                }
            }

            @Override
            public void onDragStateChanged(int state) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return listRestSelected.size();
    }

    public void deleteItem(int position) {
        if (!listRestSelected.isEmpty() && position >= 0 && position < listRestSelected.size()) {
            managementCart.deleteCartItem(listRestSelected.get(position));
            listRestSelected.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, listRestSelected.size());
            changeNumberItemsListener.changed();
        }
    }

    public interface ChangeNumberItemsListener {
        void changed();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, feeEachItem;
        ImageView pic;
        TextView num, grade;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            pic = itemView.findViewById(R.id.pic);
            feeEachItem = itemView.findViewById(R.id.fee);
            grade = itemView.findViewById(R.id.grade);
        }
    }
}



    interface ItemTouchHelperAdapter {
        void onItemMove(int fromPosition, int toPosition);

        void onItemDismiss(int position);
    }



