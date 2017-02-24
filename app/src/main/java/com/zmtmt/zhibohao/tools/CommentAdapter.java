package com.zmtmt.zhibohao.tools;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zmtmt.zhibohao.R;
import com.zmtmt.zhibohao.entity.Comment;
import com.zmtmt.zhibohao.entity.CommentContent;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/8/2.
 */
public class CommentAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Comment> cList;
    private Utils utils;

    public CommentAdapter(Context context, ArrayList<Comment> cList) {
        this.context = context;
        this.cList = cList;
        this.utils = new Utils();
    }

    @Override
    public int getCount() {
        return cList.size();
    }

    @Override
    public Object getItem(int i) {
        return cList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder vh;
        if (view == null) {
            vh = new ViewHolder();
            view=LayoutInflater.from(context).inflate(R.layout.item_comment_layout,viewGroup,false);
            vh.iv_pop_comment_head = (ImageView) view.findViewById(R.id.iv_pop_comment_head);
            vh.tv_pop_comment_nickName = (TextView) view.findViewById(R.id.tv_pop_comment_nickName);
            vh.tv_pop_comment_content = (TextView) view.findViewById(R.id.tv_pop_comment_content);
            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }
        //获取评论对象
        Comment comment = cList.get(position);
        vh.tv_pop_comment_content.setTag(position);
        CommentContent comment_content = comment.getComment_content();//获取评论内容对象
        if (comment.getCommenttype().equals("1")) {
            vh.iv_pop_comment_head.setVisibility(View.VISIBLE);
            vh.tv_pop_comment_nickName.setVisibility(View.VISIBLE);
            //设置tag 避免加载图片的时候出现位置不对和加载多次的现象
            vh.iv_pop_comment_head.setTag(comment.getComment_head_url());
            utils.showImageFromAsyncTask(vh.iv_pop_comment_head, comment.getComment_head_url(), context);//设置评论人的Img
            vh.tv_pop_comment_nickName.setText(comment.getComment_nick_name() + ":");//设置评论人的昵称
            vh.tv_pop_comment_nickName.setTextColor(Color.WHITE);
            vh.tv_pop_comment_content.setText(comment_content.getCommentContent());
            vh.tv_pop_comment_content.setTextColor(Color.WHITE);
        } else if (comment.getCommenttype().equals("2")) {
            vh.iv_pop_comment_head.setVisibility(View.GONE);
            vh.tv_pop_comment_nickName.setVisibility(View.GONE);
            vh.tv_pop_comment_content.setText("推荐成功!您推荐的商品为:" + comment_content.getName());
            vh.tv_pop_comment_content.setTextColor(Color.WHITE);
        } else if (comment.getCommenttype().equals("3")) {
            vh.iv_pop_comment_head.setVisibility(View.GONE);
            vh.tv_pop_comment_nickName.setVisibility(View.GONE);
            vh.tv_pop_comment_content.setText(comment.getComment_nick_name() + "给主播打赏了一个" + comment_content.getName());
            vh.tv_pop_comment_content.setTextColor(Color.WHITE);
        } else if (comment.getCommenttype().equals("4")) {
            vh.iv_pop_comment_head.setVisibility(View.GONE);
            vh.tv_pop_comment_nickName.setVisibility(View.GONE);
            vh.tv_pop_comment_content.setText(comment.getComment_nick_name() + "购买了主播的" + comment_content.getName());
            vh.tv_pop_comment_content.setTextColor(Color.WHITE);
        }
        return view;
    }

    public class ViewHolder {
        public ImageView iv_pop_comment_head;
        public TextView tv_pop_comment_nickName, tv_pop_comment_content;
    }
}
