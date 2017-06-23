package pl.lednica.arpark.activities.object_explorer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;

import pl.lednica.arpark.R;
import pl.lednica.arpark.helpers.ObjectModel;

/**
 * Created by admin on 24.03.2017.
 */

public class ObjectExplorerListFragmentAdapter extends RecyclerView.Adapter<ObjectExplorerListFragmentAdapter.ItemViewHolder>{

    private ObjectModel object;
    private Context context;
    private ObjectExplorerTabActivity activity;

    public ObjectExplorerListFragmentAdapter(ObjectModel object,Activity activity ,Context context) {
        this.object = object;
        this.context = context;
        this.activity = (ObjectExplorerTabActivity) activity;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_object_explorer_list, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.info_name.setText(Html.fromHtml("<h2>"+object.getDesc()+"</h2>"));
        String tekst="";
        for(int i=0;i<object.getInformations().size();i++){
            tekst = tekst+"<h3>"+object.getInformations().get(i).name+"</h3>";
            tekst = tekst+object.getInformations().get(i).desc;
        }
        holder.info_desc.setText(Html.fromHtml(tekst));

        Resources resources =  context.getResources();
        final int resourceId = resources.getIdentifier(object.getFoto(), "drawable",
                context.getPackageName());
        Glide.with(activity).load(resourceId)
                .into(holder.info_image);
    }

    @Override
    public int getItemCount() {
        if(object != null) {
            return 1;
        }else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        final ImageView info_image;
        final TextView info_name, info_desc;

        ItemViewHolder(View itemView) {
            super(itemView);
            info_name = (TextView) itemView.findViewById(R.id.info_name);
            info_desc = (TextView) itemView.findViewById(R.id.info_desc);
            info_image = (ImageView) itemView.findViewById(R.id.info_image);
        }

    }
}
