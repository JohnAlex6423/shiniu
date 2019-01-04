package com.olcow.shiniu.adapter;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.olcow.shiniu.R;
import com.olcow.shiniu.entity.Post;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Post> posts;
    private int imgWidth;
    private RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.olcowlog_loading);

    public PostAdapter(List<Post> posts, int imgWidth) {
        this.posts = posts;
        this.imgWidth = imgWidth;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new PostAdapter.ViewHolder(LayoutInflater.from(
                viewGroup.getContext()).inflate(R.layout.recy_post,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        int size = posts.get(i).getImgs().size();
        Glide.with(viewHolder.itemView.getContext())
                .load(posts.get(i).getAvatar())
                .into(viewHolder.avatar);
        viewHolder.nameText.setText(posts.get(i).getName());
        viewHolder.contentText.setText(posts.get(i).getContent());
        viewHolder.topImgCon.setVisibility(View.VISIBLE);
//        if (size>0&&size<4){
//            viewHolder.topImgCon.setVisibility(View.VISIBLE);
//            viewHolder.img1 = viewHolder.itemView.findViewById(R.id.recy_post_img1);
//            viewHolder.img2 = viewHolder.itemView.findViewById(R.id.recy_post_img2);
//            viewHolder.img3 = viewHolder.itemView.findViewById(R.id.recy_post_img3);
//            ViewGroup.LayoutParams params = viewHolder.img1.getLayoutParams();
//            params.width = imgWidth;
//            params.height = imgWidth;
//            params = viewHolder.img2.getLayoutParams();
//            params.width = imgWidth;
//            params.height = imgWidth;
//            params = viewHolder.img3.getLayoutParams();
//            params.width = imgWidth;
//            params.height = imgWidth;
//            switch (size){
//                case 1:
//                    Glide.with(viewHolder.itemView.getContext())
//                            .load(posts.get(i).getImgs().get(0))
//                            .apply(requestOptions)
//                            .into(viewHolder.img1);
//                    break;
//                case 2:
//                    Glide.with(viewHolder.itemView.getContext())
//                            .load(posts.get(i).getImgs().get(0))
//                            .into(viewHolder.img1);
//                    Glide.with(viewHolder.itemView.getContext())
//                            .load(posts.get(i).getImgs().get(1))
//                            .into(viewHolder.img2);
//                    break;
//                case 3:
//                    Glide.with(viewHolder.itemView.getContext())
//                            .load(posts.get(i).getImgs().get(0))
//                            .into(viewHolder.img1);
//                    Glide.with(viewHolder.itemView.getContext())
//                            .load(posts.get(i).getImgs().get(1))
//                            .into(viewHolder.img2);
//                    Glide.with(viewHolder.itemView.getContext())
//                            .load(posts.get(i).getImgs().get(2))
//                            .into(viewHolder.img3);
//                    break;
//            }
//        }else if (size>3){
//            viewHolder.topImgCon.setVisibility(View.VISIBLE);
//            viewHolder.bottomImgCon.setVisibility(View.VISIBLE);
//            viewHolder.img1 = viewHolder.itemView.findViewById(R.id.recy_post_img1);
//            viewHolder.img2 = viewHolder.itemView.findViewById(R.id.recy_post_img2);
//            viewHolder.img3 = viewHolder.itemView.findViewById(R.id.recy_post_img3);
//            viewHolder.img4 = viewHolder.itemView.findViewById(R.id.recy_post_img4);
//            viewHolder.img5 = viewHolder.itemView.findViewById(R.id.recy_post_img5);
//            viewHolder.img6 = viewHolder.itemView.findViewById(R.id.recy_post_img6);
//            ViewGroup.LayoutParams params = viewHolder.img1.getLayoutParams();
//            params.width = imgWidth;
//            params.height = imgWidth;
//            params = viewHolder.img2.getLayoutParams();
//            params.width = imgWidth;
//            params.height = imgWidth;
//            params = viewHolder.img3.getLayoutParams();
//            params.width = imgWidth;
//            params.height = imgWidth;
//            params = viewHolder.img4.getLayoutParams();
//            params.width = imgWidth;
//            params.height = imgWidth;
//            params = viewHolder.img5.getLayoutParams();
//            params.width = imgWidth;
//            params.height = imgWidth;
//            params = viewHolder.img6.getLayoutParams();
//            params.width = imgWidth;
//            params.height = imgWidth;
//            Glide.with(viewHolder.itemView.getContext())
//                    .load(posts.get(i).getImgs().get(0))
//                    .into(viewHolder.img1);
//            Glide.with(viewHolder.itemView.getContext())
//                    .load(posts.get(i).getImgs().get(1))
//                    .into(viewHolder.img2);
//            Glide.with(viewHolder.itemView.getContext())
//                    .load(posts.get(i).getImgs().get(2))
//                    .into(viewHolder.img3);
//            switch (size){
//                case 4:
//                    Glide.with(viewHolder.itemView.getContext())
//                            .load(posts.get(i).getImgs().get(3))
//                            .into(viewHolder.img4);
//                    break;
//                case 5:
//                    Glide.with(viewHolder.itemView.getContext())
//                            .load(posts.get(i).getImgs().get(4))
//                            .into(viewHolder.img4);
//                    Glide.with(viewHolder.itemView.getContext())
//                            .load(posts.get(i).getImgs().get(5))
//                            .into(viewHolder.img5);
//                    break;
//                case 6:
//                    Glide.with(viewHolder.itemView.getContext())
//                            .load(posts.get(i).getImgs().get(3))
//                            .into(viewHolder.img4);
//                    Glide.with(viewHolder.itemView.getContext())
//                            .load(posts.get(i).getImgs().get(4))
//                            .into(viewHolder.img5);
//                    Glide.with(viewHolder.itemView.getContext())
//                            .load(posts.get(i).getImgs().get(5))
//                            .into(viewHolder.img6);
//                    break;
//            }
//        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView nameText;
        TextView contentText;
        ConstraintLayout topImgCon;
        ConstraintLayout bottomImgCon;
        ImageView img1;
        ImageView img2;
        ImageView img3;
        ImageView img4;
        ImageView img5;
        ImageView img6;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.recy_post_avatar);
            nameText = itemView.findViewById(R.id.recy_post_name);
            contentText = itemView.findViewById(R.id.recy_post_content);
            topImgCon = itemView.findViewById(R.id.recy_post_topimg_con);
            bottomImgCon = itemView.findViewById(R.id.recy_post_bottomimg_con);
            img1 = itemView.findViewById(R.id.recy_post_img1);
        }
    }
}
