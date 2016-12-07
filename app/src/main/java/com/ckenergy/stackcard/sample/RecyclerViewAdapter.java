package com.ckenergy.stackcard.sample;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ckenergy.stackcard.stackcardlayoutmanager.ItemTouchHelperCallBack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by chengkai on 2016/11/28.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.TestViewHolder> implements ItemTouchHelperCallBack.onSwipListener {

    @Override
    public void onSwip(RecyclerView.ViewHolder viewHolder, int position) {
        remove(position);
    }

    class Bean {
        int mColor;
        int mPosition;
    }

    private final Random mRandom = new Random();
    List<Bean> cards = new ArrayList<>();

    RecyclerViewAdapter(int count) {
        for (int i = 0; count > i; ++i) {
            Bean card = new Bean();
            card.mColor = Color.argb(255, mRandom.nextInt(256), mRandom.nextInt(256), mRandom.nextInt(256));
            card.mPosition = i;
            cards.add(card);
        }
    }

    @Override
    public TestViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TestViewHolder holder, final int position) {
        holder.top.setText(String.valueOf(cards.get(position).mPosition));
        holder.bottom.setText(String.valueOf(cards.get(position).mPosition));
        holder.itemView.setBackgroundColor(cards.get(position).mColor);
        Log.d(this.getClass().getSimpleName(), "position:" + position);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public void remove(int position) {
        cards.remove(position);
        notifyItemRemoved(position);
    }


    class TestViewHolder extends RecyclerView.ViewHolder {

        private TextView top;
        private TextView bottom;

        TestViewHolder(View view) {
            super(view);
            top = (TextView) view.findViewById(R.id.top);
            bottom = (TextView) view.findViewById(R.id.bottom);
        }

    }



}
