package com.gpnu.yuan.livewords.adapter;

import android.util.Log;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.gpnu.yuan.livewords.R;
import com.gpnu.yuan.livewords.model.GetDiscernResultResponse;

import java.util.List;

/**
 * 识别结果列表适配器
 * @param <T> 数据类型
 * @param <K> ViewHolder类型
 */

/**
 * 识别结果列表适配器
 * @author yuan
 */
public class DiscernResultAdapter extends BaseQuickAdapter<GetDiscernResultResponse.ResultBean, BaseViewHolder> {
    public DiscernResultAdapter(int layoutResId, @Nullable List<GetDiscernResultResponse.ResultBean> data) {
        super(layoutResId, data);
    }

    // 在DiscernResultAdapter的convert方法中
    @Override
    protected void convert(BaseViewHolder helper, GetDiscernResultResponse.ResultBean item) {
        helper.setText(R.id.tv_keyword, item.getKeyword())
                .setText(R.id.tv_image_translation, item.getTranslate())
                .setText(R.id.tv_root, item.getRoot())
                .setText(R.id.tv_score, String.valueOf(item.getScore()));
        Log.d("translateadapter",item.getRoot());
        Log.d("translateadapter",String.valueOf(item.getScore()));
        //Log.d("translateDiscernResultAdapter",item.getTranslate());

    }

}
