package com.snalopainen.coordinatorlayout.alipay.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import com.snalopainen.coordinatorlayout.alipay.demo.model.Action;


/**
 * Created by snajdan on 2017/30/3.
 */

public class OperationActionNavigationView extends NavigationView<Action> {

    private String FirstTypeId;
    private Context context;
    public OperationActionNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setMaxColumn(4);
    }

    public void setFirstTypeId(String firstTypeId) {
        FirstTypeId = firstTypeId;
    }

    @Override
    protected int getNavigationItemImage(Action data) {
        return data.TypeId;
    }

    @Override
    protected String getNavigationItemTitle(Action data) {
        return data.TypeName;
    }

    @Override
    protected void onNavigationItemClick(Action data, int index) {
//        Intent intent = GoodsListActivity.getIntent(getContext(), FirstTypeId, data.TypeId, null, null);
//        getContext().startActivity(intent);
        Toast.makeText(context , "Item:  "+index+"  Clicked" ,Toast.LENGTH_SHORT).show();
    }
}
