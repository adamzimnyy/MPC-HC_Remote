package adamzimny.mpc_hc_remote.util;


import adamzimny.mpc_hc_remote.R;
import adamzimny.mpc_hc_remote.api.FileInfo;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class FileItem extends AbstractItem<FileItem, FileItem.ViewHolder> {
    private FileInfo info;

    public FileInfo getInfo() {
        return info;
    }

    public void setInfo(FileInfo info) {
        this.info = info;
    }

    public FileItem(FileInfo file) {
        this.info = file;
    }

    //The unique ID for this type of item
    @Override
    public int getType() {
        return R.id.file_info_list_element;
    }

    //The layout to be used for this type of item
    @Override
    public int getLayoutRes() {
        return R.layout.file_item;
    }

    //The logic to bind your data to the view
    @Override
    public void bindView(ViewHolder viewHolder, List payloads) {
        //call super so the selection is already handled for you
        super.bindView(viewHolder, payloads);

        //bind our data
        //set the text for the name
        viewHolder.filename.setText(info.getFileName());
        //set the text for the description or hide
        viewHolder.type.setText(info.getFileType());

        if (info.isDirectory()) {
            viewHolder.icon.setImageResource(R.drawable.folder);
            viewHolder.icon.setVisibility(View.VISIBLE);
            viewHolder.type.setVisibility(View.GONE);
        } else if (info.getFileType() != null && StringUtil.isVideoFile(info.getFileName())) {
            viewHolder.icon.setImageResource(R.drawable.movie);
            viewHolder.icon.setVisibility(View.VISIBLE);

            viewHolder.type.setVisibility(View.VISIBLE);
        } else {
            viewHolder.icon.setVisibility(View.INVISIBLE);
            viewHolder.type.setVisibility(View.VISIBLE);
        }

    }

    //The viewHolder used for this item. This viewHolder is always reused by the RecyclerView so scrolling is blazing fast
    protected static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.filename)
        protected TextView filename;

        @BindView(R.id.type)
        protected TextView type;

        @BindView(R.id.icon)
        ImageView icon;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}