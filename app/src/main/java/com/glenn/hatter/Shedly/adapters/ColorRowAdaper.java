package com.glenn.hatter.Shedly.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.glenn.hatter.Shedly.R;

/**
 * Created by hatter on 2015-08-29.
 */
public class ColorRowAdaper extends RecyclerView.Adapter<ColorRowAdaper.TestViewHolder> {

    private String[] colorReference = {
                    "#0088AA",
                    "#FF7F2A",
                    "#800000",
                    "#D45500",
                    "#FFCC00",
                    "#88AA00",
                    "#FCACAC",
                    "#2A2AFF",
                    "#4400AA"};

    private boolean[] mBooleans = new boolean[colorReference.length];

    private Context mContext;


    public ColorRowAdaper(Context context) {
        mContext = context;
        for (int i = 0; i < mBooleans.length; i++) {
            if (i == 0) {
                mBooleans[0] = true;
            } else {
                mBooleans[i] = false;
            }
        }
    }

    public void setColor(int colorRef) {
        int checked = 0;
        for (int i = 0; i < mBooleans.length; i++) {
            if (mBooleans[i]) {
                checked = i;
            }
            mBooleans[i] = false;
        }
        mBooleans[colorRef] = true;
        notifyItemChanged(colorRef);
        notifyItemChanged(checked);
    }


    public String getColor() {
        // Will search for the checked radioBtn and return the corresponding color.
        for (int i = 0; i < mBooleans.length; i++) {
            if (mBooleans[i]) {
                return colorReference[i];
            }
        }
        return colorReference[0];
    }

    @Override
    public ColorRowAdaper.TestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.color_item, parent, false);
        TestViewHolder viewHolder = new TestViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ColorRowAdaper.TestViewHolder holder, int position) {

        holder.bindhour(colorReference[position], mBooleans[position]);
    }

    @Override
    public int getItemCount() {
        return colorReference.length;
    }

    public class TestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public RadioButton mTestPageImage;


        public TestViewHolder(View itemView) {
            super(itemView);


            mTestPageImage = (RadioButton) itemView.findViewById(R.id.color_radio_btn);
        }

        public void bindhour(String color, final boolean checked) {

            mTestPageImage.setBackgroundColor(Color.parseColor(color));
            mTestPageImage.setChecked(checked);
            mTestPageImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int checked = 0;
            for (int i = 0; i < mBooleans.length; i++) {
                if (mBooleans[i]) {
                    checked = i;
                }
                mBooleans[i] = false;
            }
            mBooleans[getLayoutPosition()] = true;
            notifyItemChanged(getLayoutPosition());
            notifyItemChanged(checked);
        }

    }


}
